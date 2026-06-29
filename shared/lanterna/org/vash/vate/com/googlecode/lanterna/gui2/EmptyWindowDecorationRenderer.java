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
package org.vash.vate.com.googlecode.lanterna.gui2;

import org.vash.vate.com.googlecode.lanterna.TerminalPosition;
import org.vash.vate.com.googlecode.lanterna.TerminalRectangle;
import org.vash.vate.com.googlecode.lanterna.TerminalSize;

/**
 * Implementation of WindowDecorationRenderer that is doesn't render any window decorations
 * @author Martin
 */
public class EmptyWindowDecorationRenderer implements WindowDecorationRenderer {
    
    public TextGUIGraphics draw(WindowBasedTextGUI textGUI, TextGUIGraphics graphics, Window window) {
        return graphics;
    }

    
    public TerminalSize getDecoratedSize(Window window, TerminalSize contentAreaSize) {
        return contentAreaSize;
    }

    
    public TerminalPosition getOffset(Window window) {
        return TerminalPosition.TOP_LEFT_CORNER;
    }
    
    public TerminalRectangle getTitleBarRectangle(Window window) {
        return new TerminalRectangle(0, 0, window.getDecoratedSize().getColumns(), getOffset(window).getRow());
    }
}
