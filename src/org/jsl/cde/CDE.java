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

package org.jsl.cde;

import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CDE
{
    private static final Logger s_logger = Logger.getLogger( "org.jsl.ide" );
    private static final double GSC = (1.0d + Math.sqrt(5.0d)) / 2.0d;
    private static final double EPS = 0.000001d;

    private static class Impact
    {
        public Body o1;
        public int o1pi;
        public Body o2;
        public int o2pi;
        public double x;
        public double y;

        public final int getType()
        {
            return ((o1.getPrType(o1pi) << 8) | (o2.getPrType(o2pi)));
        }
    }

    private static abstract class Segment2Ball extends Body
    {
        public Body m_obj;
        public int m_id;

        public int getPrCount()
        {
            return 1;
        }

        public int getPrType( int id )
        {
            return Body.BALL;
        }

        public int getPrImpulse( int id, double x, double y, double [] dv, int offs )
        {
            assert( id == 0 );
            return m_obj.getPrImpulse( m_id, x, y, dv, offs );
        }

        public int applyPrImpulse( int id, double x, double y, double vx, double vy )
        {
            assert( id == 0 );
            return m_obj.applyPrImpulse( m_id, x, y, vx, vy );
        }

        public void move( double t )
        {
            throw new RuntimeException( "Method should never be called." );
        }
    }

    private static class SegmentE1Ball extends Segment2Ball
    {
        public int getPrPosition( double t, int id, double [] dv, int offs )
        {
            assert( id == 0 );
            m_obj.getPrPosition( t, m_id, dv, offs );
            return Body.Ball.set( dv, offs, Body.Segment.getX1(dv, offs), Body.Segment.getY1(dv, offs), 0.0d );
        }
    }

    private static class SegmentE2Ball extends Segment2Ball
    {
        public int getPrPosition( double t, int id, double [] dv, int offs )
        {
            assert( id == 0 );
            m_obj.getPrPosition( t, m_id, dv, offs );
            return Body.Ball.set( dv, offs, Body.Segment.getX2(dv, offs), Body.Segment.getY2(dv, offs), 0.0d );
        }
    }

    /**
     * Round up to the next power of 2
     * (returns x if already power of 2)
     */
    private static int clp2( int x )
    {
        x = x - 1;
        x = x | (x >> 1);
        x = x | (x >> 2);
        x = x | (x >> 4);
        x = x | (x >> 8);
        x = x | (x >> 16);
        return (x + 1);
    }

    /**
     * Returns distance between direct on points
     * (x1, y1) - (x2, y2) and point (px, py).
     * Distance is positive if point is on the left side
     * of the direct, negative otherwise.
     */
    private static double getDistanceDP(
            double x1, double y1, double x2, double y2, double px, double py )
    {
        x2 -= x1;
        y2 -= y1;
        px -= x1;
        py -= y1;

        final double segmentLength = Math.sqrt( x2*x2 + y2*y2 );
        if (segmentLength > 0.0d)
        {
            final double s = (px*y2 - x2*py);
            return (s / segmentLength);
        }
        else
        {
            /* Segment is actually a point, both ends consists. */
            return Math.sqrt(px*px + py*py);
        }
    }

    /**
     * Returns distance between direct on points
     * (x1, y1) - (x2, y2) and ball at point (bx, by) with radius (br).
     */
    private static double getDistanceDB(
            double x1, double y1, double x2, double y2, double bx, double by, double br )
    {
        return (getDistanceDP(x1, y1, x2, y2, bx, by) - br);
    }

    private static double getDistanceDB( double [] tdv, int segmentOffs, int ballOffs )
    {
        return getDistanceDB(
                Body.Segment.getX1(tdv, segmentOffs), Body.Segment.getY1(tdv, segmentOffs),
                Body.Segment.getX2(tdv, segmentOffs), Body.Segment.getY2(tdv, segmentOffs),
                Body.Ball.getX(tdv, ballOffs), Body.Ball.getY(tdv, ballOffs), Body.Ball.getR(tdv, ballOffs) );
    }

    /**
     * Returns distance between two balls.
     */
    private static double getDistanceBB(
            double b1x, double b1y, double b1r, double b2x, double b2y, double b2r )
    {
        final double dx = (b2x - b1x);
        final double dy = (b2y - b1y);
        return Math.sqrt( dx*dx + dy*dy ) - (b1r + b2r);
    }

    private static double getDistanceBB( double [] tdv, int ball1Offs, int ball2Offs )
    {
        return getDistanceBB(
                Body.Ball.getX(tdv, ball1Offs), Body.Ball.getY(tdv, ball1Offs), Body.Ball.getR(tdv, ball1Offs),
                Body.Ball.getX(tdv, ball2Offs), Body.Ball.getY(tdv, ball2Offs), Body.Ball.getR(tdv, ball2Offs) );
    }

    private static double getImpactTimeSS(
            double [] tdv, Body o1, int o1pi, Body o2, int o2pi, double frameTime, double impactTime, Impact impact )
    {
        //assert( false );
        return impactTime;
    }

    private static double getImpactTimeSB(
            double [] tdv, Body o1, int o1pi, Body o2, int o2pi, double frameTime, double impactTime, Impact impact )
    {
        /* o1[o1pi] - segment
         * o2[o2pi] - ball
         */
        double t1 = 0.0;
        final int segmentOffs = 0;
        final int ballOffs = o1.getPrPosition( t1, o1pi, tdv, segmentOffs );
        o2.getPrPosition( t1, o2pi, tdv, ballOffs );
        double d1 = getDistanceDB( tdv, segmentOffs, ballOffs );
        if (d1 < 0.0d)
            return impactTime;

        double t2 = frameTime;
        o1.getPrPosition( t2, o1pi, tdv, segmentOffs );
        o2.getPrPosition( t2, o2pi, tdv, ballOffs );
        double d2 = getDistanceDB( tdv, segmentOffs, ballOffs );

        if (d2 > 0.0d)
        {
            /* let's try t2 = (t2 / 2),
             * if distance still > 0 - no impact.
             */
            t2 /= 2.0d;
            o1.getPrPosition( t2, o1pi, tdv, segmentOffs );
            o2.getPrPosition( t2, o2pi, tdv, ballOffs );
            d2 = getDistanceDB( tdv, segmentOffs, ballOffs );
            if (d2 > 0.0d)
                return impactTime;
        }

        /* Ball definitely cross the direct on the time interval,
         * but not necessary on the segment, will check it later.
         */
        for (;;)
        {
            double tt = (t2 - t1);
            if (tt < EPS)
            {
                if (t1 < impactTime)
                {
                    final double sx = (Body.Segment.getX2(tdv, segmentOffs) - Body.Segment.getX1(tdv, segmentOffs));
                    final double sy = (Body.Segment.getY2(tdv, segmentOffs) - Body.Segment.getY1(tdv, segmentOffs));
                    final double segmentLength = Math.sqrt( sx*sx + sy*sy );
                    if (segmentLength > 0.0d)
                    {
                        final double bx = (Body.Ball.getX(tdv, ballOffs) - Body.Segment.getX1(tdv, segmentOffs));
                        final double by = (Body.Ball.getY(tdv, ballOffs) - Body.Segment.getY1(tdv, segmentOffs));
                        final double tbx = (((sx * bx) + (sy * by)) / segmentLength);
                        if ((tbx >= 0.0d) && (tbx <= segmentLength))
                        {
                            /* Impact happen on segment. */
                            impact.o1 = o1;
                            impact.o1pi = o1pi;
                            impact.o2 = o2;
                            impact.o2pi = o2pi;
                            impact.x = Body.Segment.getX1(tdv, segmentOffs) + (tbx * sx / segmentLength);
                            impact.y = Body.Segment.getY1(tdv, segmentOffs) + (tbx * sy / segmentLength);
                            impactTime = t1;
                        }
                    }
                    /* case when (segmentLength == 0.0d)
                     * will be handled anyway later when segment end points
                     * and ball impact time will be checked.
                     */
                }
                return impactTime;
            }

            tt = (t1 + (tt / 2.0d));
            o1.getPrPosition( tt, o1pi, tdv, segmentOffs );
            o2.getPrPosition( tt, o2pi, tdv, ballOffs );
            final double dt = getDistanceDB( tdv, segmentOffs, ballOffs );

            if (dt > 0.0d)
                t1 = tt;
            else
                t2 = tt;
        }
    }

    private static double getImpactTimeBB(
            double [] tdv, Body o1, int o1pi, Body o2, int o2pi, double frameTime, double impactTime, Impact impact )
    {
        /* It is not so simple to detect the impact time for balls,
         * especially if they are relatively small comparing to their speed.
         * We will try to find minimum distance first (distance between centers - (radius sum)),
         * and consider they impacts if minimum distance is less or equal zero.
         */
        double t1 = 0.0;
        double t2 = frameTime;
        double t11 = (t2 - (t2 - t1)/GSC);
        double t22 = (t1 + (t2 - t1)/GSC);

        final int ball1Offs = 0;
        final int ball2Offs = o1.getPrPosition( t11, o1pi, tdv, ball1Offs );
        o2.getPrPosition( t11, o2pi, tdv, ball2Offs );
        double d11 = getDistanceBB( tdv, ball1Offs, ball2Offs );

        /*
        o1.getPrPosition( 0.0d, o1pi, tdv, ball1Offs );
        o2.getPrPosition( 0.0d, o2pi, tdv, ball2Offs );
        System.out.println( "d1=" + getDistanceBB(tdv, ball1Offs, ball2Offs) );
        */

        o1.getPrPosition( t22, o1pi, tdv, ball1Offs );
        o2.getPrPosition( t22, o2pi, tdv, ball2Offs );
        double d22 = getDistanceBB( tdv, ball1Offs, ball2Offs );

        for (;;)
        {
            if ((t2 - t1) < EPS)
            {
                t2 = (t2 + t1) / 2.0d;
                break;
            }

            if (d11 >= d22)
            {
                t1 = t11;
                t11 = t22;
                d11 = d22;
                t22 = (t1 + (t2 - t1)/GSC);
                o1.getPrPosition( t22, o1pi, tdv, ball1Offs );
                o2.getPrPosition( t22, o2pi, tdv, ball2Offs );
                d22 = getDistanceBB( tdv, ball1Offs, ball2Offs );
            }
            else
            {
                t2 = t22;
                t22 = t11;
                d22 = d11;
                t11 = (t2 - (t2 - t1)/GSC);
                o1.getPrPosition( t11, o1pi, tdv, ball1Offs );
                o2.getPrPosition( t11, o2pi, tdv, ball2Offs );
                d11 = getDistanceBB( tdv, ball1Offs, ball2Offs );
            }
        }

        o1.getPrPosition( t2, o1pi, tdv, ball1Offs );
        o2.getPrPosition( t2, o2pi, tdv, ball2Offs );
        final double d2 = getDistanceBB( tdv, ball1Offs, ball2Offs );
        if (d2 > 0.0d)
            return impactTime;

        t1 = 0.0d;
        for (;;)
        {
            double tt = (t2 - t1);
            if (tt < EPS)
            {
                if (t1 < impactTime)
                {
                    /* Object state in the 'tdv' can be at time point 't1',
                     * as well as at time point 't2'. But we return impact time = t1 here,
                     * so we should take an impact point exactly at 't1'.
                     */
                    impactTime = t1;
                    impact.o1 = o1;
                    impact.o1pi = o1pi;
                    impact.o2 = o2;
                    impact.o2pi = o2pi;
                    if (Body.Ball.getR(tdv, ball1Offs) == 0.0d)
                    {
                        o1.getPrPosition( impactTime, o1pi, tdv, ball1Offs );
                        impact.x = Body.Ball.getX( tdv, ball1Offs );
                        impact.y = Body.Ball.getY( tdv, ball1Offs );
                    }
                    else if (Body.Ball.getR(tdv, ball2Offs) == 0.0d)
                    {
                        o2.getPrPosition( impactTime, o2pi, tdv, ball2Offs );
                        impact.x = Body.Ball.getX( tdv, ball2Offs );
                        impact.y = Body.Ball.getY( tdv, ball2Offs );
                    }
                    else
                    {
                        o1.getPrPosition( impactTime, o1pi, tdv, ball1Offs );
                        o2.getPrPosition( impactTime, o2pi, tdv, ball2Offs );
                        tt = (Body.Ball.getR(tdv, ball1Offs) + Body.Ball.getR(tdv, ball2Offs)) /
                                Body.Ball.getR(tdv, ball1Offs);
                        impact.x = Body.Ball.getX(tdv, ball1Offs) +
                                tt * (Body.Ball.getX(tdv, ball2Offs) - Body.Ball.getX(tdv, ball1Offs));
                        impact.y = Body.Ball.getY(tdv, ball1Offs) +
                                tt * (Body.Ball.getY(tdv, ball2Offs) - Body.Ball.getY(tdv, ball1Offs));
                    }
                }
                return impactTime;
            }

            tt = (t1 + (tt / 2.0d));
            o1.getPrPosition( tt, o1pi, tdv, ball1Offs );
            o2.getPrPosition( tt, o2pi, tdv, ball2Offs );
            final double dt = getDistanceBB( tdv, ball1Offs, ball2Offs );
            if (dt > 0.0d)
                t1 = tt;
            else
                t2 = tt;
        }
    }

    private double getImpactTimeSB(
            Body o1, int o1pi, Body o2, int o2pi, double frameTime, double impactTime, Impact impact )
    {
        m_segmentE1Ball.m_obj = o1;
        m_segmentE1Ball.m_id = o1pi;
        impactTime = getImpactTimeBB( m_tdv, m_segmentE1Ball, 0, o2, o2pi, frameTime, impactTime, impact );
        if (impact.o1 == m_segmentE1Ball)
        {
            impact.o1 = o1;
            impact.o1pi = o1pi;
        }

        impactTime = getImpactTimeSB( m_tdv, o1, o1pi, o2, o2pi, frameTime, impactTime, impact );

        m_segmentE2Ball.m_obj = o1;
        m_segmentE1Ball.m_id = o1pi;
        impactTime = getImpactTimeBB( m_tdv, m_segmentE2Ball, 0, o2, o2pi, frameTime, impactTime, impact );
        if (impact.o1 == m_segmentE2Ball)
        {
            impact.o1 = o1;
            impact.o1pi = o1pi;
        }

        return impactTime;
    }

    private double getImpactTime( Body o1, Body o2, double frameTime, double impactTime, Impact impact )
    {
        final int o1pc = o1.getPrCount();
        final int o2pc = o2.getPrCount();

        for (int o1pi=0; o1pi<o1pc; o1pi++)
        {
            for (int o2pi=0; o2pi<o2pc; o2pi++)
            {
                final int type = ((o1.getPrType(o1pi) << 8) | o2.getPrType(o2pi));
                switch (type)
                {
                    case ((Body.SEGMENT << 8) | Body.SEGMENT):
                        impactTime = getImpactTimeSS( m_tdv, o1, o1pi, o2, o2pi, frameTime, impactTime, impact );
                        break;

                    case ((Body.SEGMENT << 8) | Body.BALL):
                        impactTime = getImpactTimeSB( o1, o1pi, o2, o2pi, frameTime, impactTime, impact );
                        break;

                    case ((Body.BALL << 8) | Body.SEGMENT):
                        impactTime = getImpactTimeSB( o2, o2pi, o1, o1pi, frameTime, impactTime, impact );
                        break;

                    case ((Body.BALL << 8) | Body.BALL):
                        impactTime = getImpactTimeBB( m_tdv, o1, o1pi, o2, o2pi, frameTime, impactTime, impact );
                        break;

                    default:
                        throw new RuntimeException( "Invalid impact type: " + type );
                }

                if ((impactTime == 0.0d) || (impactTime == Double.MIN_VALUE))
                    return impactTime;
            }
        }
        return impactTime;
    }

    private boolean handleImpact(
            Impact impact, double [] tdv, int impulse1Offs, int impulse2Offs, int offs,
            double impactLineX, double impactLineY )
    {
        final double impactLineLength = Math.sqrt(
                impactLineX*impactLineX + impactLineY*impactLineY );

        final double cos = (impactLineX / impactLineLength);
        final double sin = (impactLineY / impactLineLength);
        Matrix.set( tdv, offs, cos, sin, -sin, cos );
        Impulse.rotate( tdv, impulse1Offs, tdv, offs );
        Impulse.rotate( tdv, impulse2Offs, tdv, offs );

        /*
        if (Impulse.getVx(tdv, impulse2Offs) > Impulse.getVx(tdv, impulse1Offs))
            return false;
        */

        if (Impulse.getM(tdv, impulse1Offs) == Double.MAX_VALUE)
        {
            Impulse.setVx( tdv, impulse2Offs,
                    Impulse.getVx(tdv, impulse1Offs) - Impulse.getVx(tdv, impulse2Offs) );
        }
        else if (Impulse.getM(tdv, impulse2Offs) == Double.MAX_VALUE)
        {
            Impulse.setVx( tdv, impulse1Offs,
                    Impulse.getVx(tdv, impulse2Offs) - Impulse.getVx(tdv, impulse1Offs) );
        }
        else
        {
            /* Now both impulses normalized by impact line,
             * so x part of impulses will be changed, y - not.
             */
            final double m1 = Impulse.getM( tdv, impulse1Offs );
            final double m2 = Impulse.getM( tdv, impulse2Offs );
            final double u1 = Impulse.getVx( tdv, impulse1Offs );
            final double u2 = Impulse.getVx( tdv, impulse2Offs );
            final double v1 = 2 * m2 * (u1 - u2) / (m1 + m2);
            final double v2 = (m1 - m2) * (u1 - u2) / (m1 + m2);
            Impulse.setVx( tdv, impulse1Offs, v1 );
            Impulse.setVx( tdv, impulse2Offs, v2 );
        }

        Matrix.set( tdv, offs, cos, -sin, sin, cos );
        Impulse.rotate( tdv, impulse1Offs, tdv, offs );
        Impulse.rotate( tdv, impulse2Offs, tdv, offs );

        impact.o1.applyPrImpulse( impact.o1pi, impact.x, impact.y,
                Impulse.getVx(tdv, impulse1Offs), Impulse.getVy(tdv, impulse1Offs) );

        impact.o2.applyPrImpulse( impact.o2pi, impact.x, impact.y,
                    Impulse.getVx(tdv, impulse2Offs), Impulse.getVy(tdv, impulse2Offs) );

        return true;
    }

    private boolean handleImpactSB( Impact impact, double [] tdv, int segmentOffs, int ballOffs, int offs )
    {
        /* o1[o1pi] - segment,
         * o2[o2pi] - ball
         *                   / offs
         * +---------+------X-----------------+--------------+
         * | segment | ball X segment impulse | ball impulse |
         * +---------+------X-----------------+--------------+
         */
        assert( impact.o2pi == 0 );

        final int segmentImpulseOffs = offs;
        final int ballImpulseOffs = impact.o1.getPrImpulse(
                impact.o1pi, impact.x, impact.y, tdv, segmentImpulseOffs );

        offs = impact.o2.getPrImpulse( impact.o2pi, impact.x, impact.y, tdv, ballImpulseOffs );

        double impactLineX, impactLineY;

        if ((impact.x == Body.Segment.getX1(tdv, segmentOffs)) &&
            (impact.y == Body.Segment.getY1(tdv, segmentOffs)))
        {
            /* impact line is a vector from impact point to the ball center */
            impactLineX = (Body.Ball.getX(tdv, ballOffs) - impact.x);
            impactLineY = (Body.Ball.getY(tdv, ballOffs) - impact.y);
        }
        else if ((impact.x == Body.Segment.getX2(tdv, segmentOffs)) &&
                 (impact.y == Body.Segment.getY2(tdv, segmentOffs)))
        {
            /* impact line is a vector from impact point to the ball center */
            impactLineX = (Body.Ball.getX(tdv, ballOffs) - impact.x);
            impactLineY = (Body.Ball.getY(tdv, ballOffs) - impact.y);
        }
        else
        {
            /* impact line is a perpendicular to the segment (non clock-wise),
             * rotate it by matrix  |  0  1 |
             *                      | -1  0 |
             */
            final double sx = (Body.Segment.getX2(tdv, segmentOffs) - Body.Segment.getX1(tdv, segmentOffs));
            final double sy = (Body.Segment.getY2(tdv, segmentOffs) - Body.Segment.getY1(tdv, segmentOffs));
            impactLineX = sy;
            impactLineY = -sx;
        }

        return handleImpact( impact, tdv, segmentImpulseOffs, ballImpulseOffs, offs, impactLineX, impactLineY );
    }

    private boolean handleImpactBB( Impact impact, double [] tdv, int ball1Offs, int ball2Offs, int offs )
    {
        /* o1[o1pi] - ball 1
         * o2[o2pi] - ball 2
         *                    / offs
         * +--------+--------X----------------+----------------+
         * | ball 1 | ball 2 X ball 1 impulse | ball 2 impulse |
         * +--------+--------X----------------+----------------+
         */
        assert( impact.o1pi == 0 );
        assert( impact.o2pi == 0 );

        final int ball1ImpulseOffs = offs;
        final int ball2ImpulseOffs = impact.o1.getPrImpulse( impact.o1pi, impact.x, impact.y, tdv, offs );
        offs = impact.o2.getPrImpulse( impact.o2pi, impact.x, impact.y, tdv, ball2ImpulseOffs );

        double impactLineX, impactLineY;

        if ((Body.Ball.getR(tdv, ball1Offs) == 0.0d) &&
            (Body.Ball.getR(tdv, ball2Offs) == 0.0d))
        {
            assert( false );
            impactLineX = 0.0d;
            impactLineY = 0.0d;
        }
        else
        {
            impactLineX = (Body.Ball.getX(tdv, ball2Offs) - Body.Ball.getX(tdv, ball1Offs));
            impactLineY = (Body.Ball.getY(tdv, ball2Offs) - Body.Ball.getY(tdv, ball1Offs));
        }

        return handleImpact( impact, tdv, ball1ImpulseOffs, ball2ImpulseOffs, offs, impactLineX, impactLineY );
    }

    private boolean handleImpactSS( Impact impact, double [] tdv, int segment1Offs, int segment2Offs, int offs )
    {
        /* o1[o1pi] - segment 1
         * o2[o2pi] - segment 2
         * +-----------+-----------X-------------------+-------------------+
         * | segment 1 | segment 2 X segment 1 impulse | segment 2 impulse |
         * +-----------+-----------X-------------------+-------------------+
         */
        return true;
    }

    private void handleImpact( Impact impact )
    {
        final double [] tdv = m_tdv;
        final int o1offs = 0;
        final int o2offs = impact.o1.getPrPosition( 0.0d, impact.o1pi, tdv, o1offs );
        final int offs = impact.o2.getPrPosition( 0.0d, impact.o2pi, tdv, o2offs );
        switch (impact.getType())
        {
            case ((Body.SEGMENT << 8) | Body.SEGMENT):
                handleImpactSS( impact, tdv, o1offs, o2offs, offs );
                break;

            case ((Body.SEGMENT << 8) | Body.BALL):
                handleImpactSB( impact, tdv, o1offs, o2offs, offs );
                break;

            case ((Body.BALL << 8) | Body.SEGMENT):
                /* Ball will be always the second impact object. */
                throw new RuntimeException( "Internal error" );

            case ((Body.BALL << 8) | Body.BALL):
                handleImpactBB( impact, tdv, o1offs, o2offs, offs );
                break;

            default:
                throw new RuntimeException( "Internal error" );
        }
    }

    private final HashSet<Body> m_objHash;
    private Body [] m_objArray;
    private int m_objects;

    private final Impact m_impact;

    private final Segment2Ball m_segmentE1Ball;
    private final Segment2Ball m_segmentE2Ball;
    private final double [] m_tdv; /* Temporary double vector */

    public CDE()
    {
        m_objHash = new HashSet<Body>();
        m_impact = new Impact();
        m_segmentE1Ball = new SegmentE1Ball();
        m_segmentE2Ball = new SegmentE2Ball();
        m_tdv = new double[32];
    }

    public final void add( Body obj )
    {
        m_objHash.add( obj );
        m_objects = -1;
    }

    public final void remove( Body obj )
    {
        if (m_objHash.remove(obj))
            m_objects = -1;
    }

    public final void run( final double runTime )
    {
        if (s_logger.isLoggable( Level.FINE))
            s_logger.fine( "runTime=" + runTime );

        if (m_objects == -1)
        {
            m_objects = m_objHash.size();
            if ((m_objArray == null) ||
                (m_objArray.length < m_objHash.size()))
            {
                final int newSize = (m_objects < 32) ? 32 : clp2(m_objects);
                m_objArray = new Body[newSize];
            }

            Iterator<Body> it = m_objHash.iterator();
            for (int idx=0; it.hasNext(); idx++)
                m_objArray[idx] = it.next();
        }

        double timeRemaining = runTime;
        for (;;)
        {
            double impactTime = Double.MAX_VALUE;

            for (int idx=0; idx<m_objects; idx++)
            {
                final Body obj1 = m_objArray[idx];
                for (int jdx=idx+1; jdx<m_objects; jdx++)
                {
                    final Body obj2 = m_objArray[jdx];
                    if (obj1.inTheSameGroup(obj2))
                        impactTime = getImpactTime( obj1, obj2, timeRemaining, impactTime, m_impact );
                }
            }

            if (impactTime < Double.MAX_VALUE)
            {
                assert( impactTime <= timeRemaining );

                if (impactTime > 0.0d)
                {
                    for (int idx = 0; idx < m_objects; idx++)
                        m_objArray[idx].move( impactTime );

                    handleImpact( m_impact );

                    timeRemaining -= impactTime;
                    if (timeRemaining == 0.0)
                        break;
                }
                else
                {
                    handleImpact( m_impact );
                }
            }
            else
            {
                for (int idx=0; idx<m_objects; idx++)
                    m_objArray[idx].move( timeRemaining );
                break;
            }
        }
    }
}
