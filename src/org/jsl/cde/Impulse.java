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

public class Impulse
{
    public static final int SIZE = 3;

    public static int set( double [] dv, int offs, double vx, double vy, double m )
    {
        dv[offs+0] = vx;
        dv[offs+1] = vy;
        dv[offs+2] = m;
        return (offs + SIZE);
    }

    public static void rotate( double [] idv, int ioffs, double [] mdv, int moffs )
    {
        final double vx = (Matrix.get11(mdv, moffs) * getVx(idv, ioffs) +
                           Matrix.get12(mdv, moffs) * getVy(idv, ioffs));
        final double vy = (Matrix.get21(mdv, moffs) * getVx(idv, ioffs) +
                           Matrix.get22(mdv, moffs) * getVy(idv, ioffs));
        idv[ioffs+0] = vx;
        idv[ioffs+1] = vy;
    }

    public static double getVx( double [] dv, int offs ) { return dv[offs+0]; }
    public static double getVy( double [] dv, int offs ) { return dv[offs+1]; }
    public static double getM( double [] dv, int offs ) { return dv[offs+2]; }

    public static void setVx( double [] dv, int offs, double vx ) { dv[offs+0] = vx; }
}
