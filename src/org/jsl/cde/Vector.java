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

public class Vector
{
    public static final int SIZE = 2;

    public static int set( double [] dv, int offs, double x, double y )
    {
        dv[offs+0] = x;
        dv[offs+1] = y;
        return (offs + SIZE);
    }

    public static double dotProduct( double [] dv1, int offs1, double [] dv2, int offs2 )
    {
        return (getX(dv1, offs1) * getX(dv2, offs2) +
                getY(dv1, offs1) * getY(dv2, offs2));
    }

    public static void rotate( double [] vdv, int voffs, double [] mdv, int moffs )
    {
        final double x = getX( vdv, voffs );
        final double y = getY( vdv, voffs );

        set( vdv, voffs,
                x * Matrix.get11(mdv, moffs) + y * Matrix.get12(mdv, moffs),
                x * Matrix.get21(mdv, moffs) + y * Matrix.get22(mdv, moffs) );
    }

    public static double getLength( double [] dv, int offs )
    {
        final double x = getX( dv, offs );
        final double y = getY( dv, offs );
        return Math.sqrt( x*x + y*y );
    }

    public static double getX( double [] dv, int offs ) { return dv[offs+0]; }
    public static double getY( double [] dv, int offs ) { return dv[offs+1]; }
}
