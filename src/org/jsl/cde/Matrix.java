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

public class Matrix
{
    public static final int SIZE = 4;

    public static int set( double [] dv, int offs, double m11, double m12, double m21, double m22 )
    {
        dv[offs+0] = m11;
        dv[offs+1] = m12;
        dv[offs+2] = m21;
        dv[offs+3] = m22;
        return (offs + SIZE);
    }

    public static double get11( double [] dv, int offs ) { return dv[offs+0]; }
    public static double get12( double [] dv, int offs ) { return dv[offs+1]; }
    public static double get21( double [] dv, int offs ) { return dv[offs+2]; }
    public static double get22( double [] dv, int offs ) { return dv[offs+3]; }
}
