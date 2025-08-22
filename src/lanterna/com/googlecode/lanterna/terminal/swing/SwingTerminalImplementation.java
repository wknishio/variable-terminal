/*
 * This file is part of lanterna (https://github.com/mabe02/lanterna).
 *
 * lanterna is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010-2020 Martin Berglund
 */
package com.googlecode.lanterna.terminal.swing;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.terminal.MouseCaptureMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Collections;

/**
 * Concrete implementation of {@link GraphicalTerminalImplementation} that adapts it to Swing
 */
public class SwingTerminalImplementation extends GraphicalTerminalImplementation {

    private final JComponent component;
    private final SwingTerminalFontConfiguration fontConfiguration;
    private MouseAdapter mouseListener;

    /**
     * Creates a new {@code SwingTerminalImplementation}
     * @param component JComponent that is the Swing terminal surface
     * @param fontConfiguration Font configuration to use
     * @param initialTerminalSize Initial size of the terminal
     * @param deviceConfiguration Device configuration
     * @param colorConfiguration Color configuration
     * @param scrollController Controller to be used when inspecting scroll status
     */
    SwingTerminalImplementation(
            JComponent component,
            SwingTerminalFontConfiguration fontConfiguration,
            TerminalSize initialTerminalSize,
            TerminalEmulatorDeviceConfiguration deviceConfiguration,
            TerminalEmulatorColorConfiguration colorConfiguration,
            TerminalScrollController scrollController) {

        super(initialTerminalSize, deviceConfiguration, colorConfiguration, scrollController, null);
        this.component = component;
        this.fontConfiguration = fontConfiguration;

        //Prevent us from shrinking beyond one character
        component.setMinimumSize(new Dimension(fontConfiguration.getFontWidth(), fontConfiguration.getFontHeight()));

        //noinspection unchecked
        component.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.<AWTKeyStroke>emptySet());
        //noinspection unchecked
        component.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.<AWTKeyStroke>emptySet());

        //Make sure the component is double-buffered to prevent flickering
        component.setDoubleBuffered(true);

        component.addKeyListener(new TerminalInputListener());
        //Mouse support
        updateMouseCaptureMode(this.mouseCaptureMode);
//        component.addMouseListener(new TerminalMouseListener() {
//            
//            public void mouseClicked(MouseEvent e) {
//                super.mouseClicked(e);
//                SwingTerminalImplementation.this.component.requestFocusInWindow();
//            }
//        });
        component.addHierarchyListener(new HierarchyListener() {
            
            public void hierarchyChanged(HierarchyEvent e) {
                if(e.getChangeFlags() == HierarchyEvent.DISPLAYABILITY_CHANGED) {
                    if(e.getChanged().isDisplayable()) {
                        onCreated();
                    }
                    else {
                        onDestroyed();
                    }
                }
            }
        });
    }
    
    protected void updateMouseCaptureMode(MouseCaptureMode mouseCaptureMode)
    {
        if(this.mouseListener!=null)
        {
            component.removeMouseListener(this.mouseListener);
            component.removeMouseWheelListener(this.mouseListener);
            component.removeMouseMotionListener(this.mouseListener);
        }
        this.mouseListener=new TerminalMouseListener(this.mouseCaptureMode)
        {
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                SwingTerminalImplementation.this.component.requestFocusInWindow();
            }
        };
        component.addMouseListener(this.mouseListener);
        component.addMouseWheelListener(this.mouseListener);
        component.addMouseMotionListener(this.mouseListener);
    }

    /**
     * Returns the current font configuration. Note that it is immutable and cannot be changed.
     * @return This SwingTerminal's current font configuration
     */
    public SwingTerminalFontConfiguration getFontConfiguration() {
        return fontConfiguration;
    }

    
    protected int getFontHeight() {
        return fontConfiguration.getFontHeight();
    }

    
    protected int getFontWidth() {
        return fontConfiguration.getFontWidth();
    }

    
    protected int getHeight() {
        return component.getHeight();
    }

    
    protected int getWidth() {
        return component.getWidth();
    }

    
    protected Font getFontForCharacter(TextCharacter character) {
        return fontConfiguration.getFontForCharacter(character);
    }

    
    protected boolean isTextAntiAliased() {
        return fontConfiguration.isAntiAliased();
    }

    
    protected void repaint() {
        if(SwingUtilities.isEventDispatchThread()) {
            component.repaint();
        }
        else {
            SwingUtilities.invokeLater(new Runnable() {
                
                public void run() {
                    component.repaint();
                }
            });
        }
    }

    
    public com.googlecode.lanterna.input.KeyStroke readInput() {
        if(SwingUtilities.isEventDispatchThread()) {
            throw new UnsupportedOperationException("Cannot call SwingTerminal.readInput() on the AWT thread");
        }
        return super.readInput();
    }
}
