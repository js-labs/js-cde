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

public abstract class Obj
{
    public static final int SEGMENT = 1;
    public static final int BALL    = 2;

    public static class Point
    {
        public static final int SIZE = 2;

        public static int set( double [] dv, int offs, double x, double y )
        {
            dv[offs+0] = x;
            dv[offs+1] = y;
            return (offs + SIZE);
        }

        public static double getX( double [] dv, int offs ) { return dv[offs+0]; }
        public static double getY( double [] dv, int offs ) { return dv[offs+1]; }
    }

    public static class Ball
    {
        public static final int SIZE = 3;

        public static int set( double [] dv, int offs, double x, double y, double r )
        {
            dv[offs+0] = x;
            dv[offs+1] = y;
            dv[offs+2] = r;
            return (offs + SIZE);
        }

        public static double getX( double [] dv, int offs ) { return dv[offs+0]; }
        public static double getY( double [] dv, int offs ) { return dv[offs+1]; }
        public static double getR( double [] dv, int offs ) { return dv[offs+2]; }
    }

    public static class Segment
    {
        public static final int SIZE = 4;

        public static int set( double [] dv, int offs, double x1, double y1, double x2, double y2 )
        {
            dv[offs+0] = x1;
            dv[offs+1] = y1;
            dv[offs+2] = x2;
            dv[offs+3] = y2;
            return (offs + SIZE);
        }

        public static double getX1( double [] dv, int offs ) { return dv[offs+0]; }
        public static double getY1( double [] dv, int offs ) { return dv[offs+1]; }
        public static double getX2( double [] dv, int offs ) { return dv[offs+2]; }
        public static double getY2( double [] dv, int offs ) { return dv[offs+3]; }
    }

    public abstract int getPrCount();
    public abstract int getPrType( int id );
    public abstract int getPrPosition( double t, int id, double [] dv, int offs );
    public abstract int getPrImpulse( int id, double x, double y, double [] dv, int offs );
    public abstract int applyPrImpulse( int id, double x, double y, double vx, double vy );
    public abstract void move( double t );
}
