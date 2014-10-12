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

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main extends JFrame
{
    private final TestPanel m_testPanel;

    private class MyWindowAdapter extends WindowAdapter
    {
        public void windowClosing( WindowEvent e )
        {
            m_testPanel.stop();
        }
    }

    private Main()
    {
        super( "Visual distance" );
        setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        addWindowListener( new MyWindowAdapter() );
        m_testPanel = new TestPanel();
        add( m_testPanel );
        pack();
        setVisible( true );
    }

    public static void main( String [] args )
    {
        new Main();
    }
}
