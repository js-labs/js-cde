/*
 * Copyright (C) 2013 Sergey Zubarev, info@js-labs.org
 *
 * This file is a part of JS-CDE
 * (Collision Detection Engine) framework.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jsl.tests.SimpleTest;

import org.jsl.cde.CDE;
import org.jsl.cde.Impulse;
import org.jsl.cde.Obj;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;

public class TestPanel extends JPanel
{
    private static final int FPS = 20;
    private static double FRAME_INTERVAL_S = (1.0d / FPS);
    private static long FRAME_INTERVAL_MS = (1000 / FPS);

    private static abstract class DObj extends Obj
    {
        public abstract void draw( Graphics g, double [] tdv );
    }

    private static class Table extends DObj
    {
        private final double m_x1;
        private final double m_y1;
        private final double m_x2;
        private final double m_y2;

        public Table( double x1, double y1, double x2, double y2 )
        {
            m_x1 = x1;
            m_y1 = y1;
            m_x2 = x2;
            m_y2 = y2;
        }

        public int getPrCount()
        {
            return 4;
        }

        public int getPrType( int id )
        {
            return Obj.SEGMENT;
        }

        public int getPrPosition( double t, int id, double [] dv, int offs )
        {
            /* All other objects are inside, use non-clockwise direction.
             *  (x1, y1)
             *      +---------+
             *      |         |
             *      |         |
             *      +---------+
             *             (x2, y2)
             */
            switch (id)
            {
                case 0: /* left side */
                    return Obj.Segment.set(dv, offs, m_x1, m_y1, m_x1, m_y2);

                case 1: /* bottom side */
                    return Obj.Segment.set(dv, offs, m_x1, m_y2, m_x2, m_y2);

                case 2: /* right side */
                    return Obj.Segment.set(dv, offs, m_x2, m_y2, m_x2, m_y1);

                case 3: /* bottom side */
                    return Obj.Segment.set(dv, offs, m_x2, m_y1, m_x1, m_y1);
            }
            throw new RuntimeException( "Invalid object ID=" + id );
        }

        public int getPrImpulse( int id, double x, double y, double [] dv, int offs )
        {
            return Impulse.set( dv, offs, 0.0d, 0.0d, Double.MAX_VALUE );
        }

        public int applyPrImpulse( int id, double x, double y, double vx, double vy )
        {
            /* Table does not move. */
            return 0;
        }

        public void move( double t )
        {
            /* Table does not move. */
        }

        public void draw( Graphics g, double [] tdv )
        {
            g.setColor( Color.green );
            for (int idx=0; idx<getPrCount(); idx++)
            {
                getPrPosition( 0, idx, tdv, 0 );
                g.drawLine(
                        (int) Obj.Segment.getX1(tdv, 0), (int) Obj.Segment.getY1(tdv, 0),
                        (int) Obj.Segment.getX2(tdv, 0), (int) Obj.Segment.getY2(tdv, 0) );
            }
        }
    }

    public static class Fence extends DObj
    {
        private double m_x;
        private double m_y;
        private double m_r1;
        private double m_r2;
        private double m_rv;
        private double m_angle;

        public Fence( double x, double y, double r1, double r2, double rv, double angle )
        {
            m_x = x;
            m_y = y;
            m_r1 = r1;
            m_r2 = r2;
            m_rv = rv;
            m_angle = angle;
        }

        public int getPrCount()
        {
            return 4;
        }

        public int getPrType( int id )
        {
            assert( id < 4 );
            return Obj.SEGMENT;
        }

        public int getPrPosition( double t, int id, double[] dv, int offs )
        {
            assert (id < 4);
            final double angle = m_angle + m_rv * t + Math.PI / 2 * id;
            double sin = Math.sin( angle );
            double cos = Math.cos( angle );
            double r1, r2;
            if ((id % 2) == 0)
            {
                r1 = m_r1;
                r2 = m_r2;
            }
            else
            {
                r1 = m_r2;
                r2 = m_r1;
            }
            return Segment.set( dv, offs,
                    m_x + r1 * cos, m_y + r1 * sin,
                    m_x - r2 * sin, m_y + r2 * cos );
        }

        public int getPrImpulse( int id, double x, double y, double [] dv, int offs )
        {
            /* m_rv is rotation speed in radians */
            assert( id < 4 );
            x -= m_x;
            y -= m_y;

            /* velocity vector is (x,y) vector rotated clockwise by pi/2,
             * rotation matrix is | 0 -1 |
             *                    | 1  0 |
             */
            final double vx = -y;
            final double vy = x;

            final double vl = Math.sqrt( x*x + y*y );
            final double v = (vl * m_rv);

            return Impulse.set( dv, offs, /*v*cos*/ v*(vx/vl) , /*v*sin*/ v*(vy/vl), Double.MAX_VALUE );
        }

        public int applyPrImpulse( int id, double x, double y, double vx, double vy )
        {
            return 0;
        }

        public void move( double t )
        {
            m_angle += (m_rv * t );
        }

        public void draw( Graphics g, double [] tdv )
        {
            for (int idx=0; idx<getPrCount(); idx++)
            {
                getPrPosition( 0, idx, tdv, 0 );
                g.drawLine(
                        (int) Obj.Segment.getX1(tdv, 0), (int) Obj.Segment.getY1(tdv, 0),
                        (int) Obj.Segment.getX2(tdv, 0), (int) Obj.Segment.getY2(tdv, 0) );
            }
            g.setColor( Color.red );
            g.drawOval( (int)m_x, (int)m_y, 2, 2 );
        }
    }

    public static class Ball extends DObj
    {
        private double m_x;
        private double m_y;
        private double m_r;
        private double m_vx;
        private double m_vy;

        public Ball( double x, double y, double r, double vx, double vy )
        {
            m_x = x;
            m_y = y;
            m_r = r;
            m_vx = vx;
            m_vy = vy;
        }

        public int getPrCount()
        {
            return 1;
        }

        public int getPrType( int id )
        {
            return BALL;
        }

        public int getPrPosition( double t, int id, double [] dv, int offs )
        {
            assert( id == 0 );
            return Obj.Ball.set(dv, offs, (m_x + t * m_vx), (m_y + t*m_vy), m_r);
        }

        public int getPrImpulse( int id, double x, double y, double [] dv, int offs )
        {
            assert( id == 0 );
            return Impulse.set( dv, offs, m_vx, m_vy, 1.0d );
        }

        public int applyPrImpulse( int id, double x, double y, double vx, double vy )
        {
            assert( id == 0 );
            m_vx = vx;
            m_vy = vy;
            return 0;
        }

        public void move( double t )
        {
            m_x += (m_vx * t);
            m_y += (m_vy * t);
        }

        public void draw( Graphics g, double [] tdv )
        {
            g.setColor( Color.red );
            g.drawOval( (int) (m_x - m_r), (int) (m_y - m_r), (int) (m_r * 2.0d), (int) (m_r * 2.0d) );
            g.setColor( Color.blue );
            g.drawLine( (int) m_x, (int) m_y, (int) (m_x + m_vx), (int) (m_y + m_vy) );
        }
    }

    private static class Segment extends DObj
    {
        private double m_x1;
        private double m_y1;
        private double m_x2;
        private double m_y2;

        public Segment( double x1, double y1, double x2, double y2 )
        {
            m_x1 = x1;
            m_y1 = y1;
            m_x2 = x2;
            m_y2 = y2;
        }

        public void draw( Graphics g, double [] tdv )
        {
            g.drawLine( (int) m_x1, (int) m_y1, (int) m_x2, (int) m_y2 );
        }

        public int getPrCount()
        {
            return 1;
        }

        public int getPrType( int id )
        {
            assert( id == 0 );
            return Obj.SEGMENT;
        }

        public int getPrPosition( double t, int id, double[] dv, int offs )
        {
            assert( id == 0 );
            return Obj.Segment.set( dv, offs, m_x1, m_y1, m_x2, m_y2 );
        }

        public int getPrImpulse( int id, double x, double y, double [] dv, int offs )
        {
            assert( id == 0 );
            return Impulse.set( dv, offs, /*vx*/ 0.0d, /*vy*/ 0.0d, Double.MAX_VALUE );
        }

        public int applyPrImpulse( int id, double x, double y, double vx, double vy )
        {
            assert( id == 0 );
            return 0;
        }

        public void move( double t )
        {
        }
    }

    private final CDE m_cde;
    private final ArrayDeque<DObj> m_objs;
    private final double [] m_tdv;

    private volatile boolean m_run;
    private final Thread m_worker;

    private void run_i()
    {
        while (m_run)
        {
            final long startTime = System.currentTimeMillis();
            synchronized (m_cde)
            {
                m_cde.run( FRAME_INTERVAL_S );
            }
            final long endTime = System.currentTimeMillis();
            final long sleepTime = (startTime + FRAME_INTERVAL_MS - endTime);
            if (sleepTime > 0)
            {
                try { Thread.sleep( sleepTime ); }
                catch (InterruptedException ex)
                { ex.printStackTrace(); }
            }
            repaint();
        }
    }

    public TestPanel()
    {
        setBorder( BorderFactory.createLineBorder( Color.black) );

        m_cde = new CDE();
        m_objs = new ArrayDeque<DObj>();
        m_tdv = new double[54];

        if (true)
        {
            m_objs.push( new Table(20, 20, 380, 580) );
            m_objs.push( new Ball(/*x*/ 50.0d, /*y*/ 50.0d, /*r*/ 10.0d, /*vx*/ 30.0d, /*vy*/ 40.0d) );
            m_objs.push( new Fence(/*x*/  20 + 180, /*y*/  20 + 280,
                                   /*r1*/ 140,      /*r2*/ 20,
                                   /*rv*/ Math.PI / 180.0d * 20,
                                   /*angle*/ 0.0d ) );
        }

        for (DObj obj : m_objs)
            m_cde.add( obj );

        m_run = true;
        m_worker = new Thread( new Runnable() { public void run() { run_i(); } } );
        m_worker.start();
    }

    public Dimension getPreferredSize()
    {
        return new Dimension(400, 600);
    }

    public void paintComponent( Graphics g )
    {
        super.paintComponent( g );

        synchronized (m_cde)
        {
            for (DObj obj : m_objs)
                obj.draw( g, m_tdv );
        }
    }

    public void stop()
    {
        m_run = false;
        try
        {
            m_worker.join();
        }
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
        }
    }
}
