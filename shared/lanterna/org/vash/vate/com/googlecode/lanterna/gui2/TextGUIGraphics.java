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

import java.util.Collection;
import java.util.EnumSet;

import org.vash.vate.com.googlecode.lanterna.*;
import org.vash.vate.com.googlecode.lanterna.graphics.*;
import org.vash.vate.com.googlecode.lanterna.screen.TabBehaviour;

/**
 * TextGraphics implementation used by TextGUI when doing any drawing operation.
 * @author Martin
 */
public interface TextGUIGraphics extends ThemedTextGraphics, TextGraphics {
    /**
     * Returns the {@code TextGUI} this {@code TextGUIGraphics} belongs to
     * @return {@code TextGUI} this {@code TextGUIGraphics} belongs to
     */
    TextGUI getTextGUI();

    
    TextGUIGraphics newTextGraphics(TerminalPosition topLeftCorner, TerminalSize size) throws IllegalArgumentException;

    
    TextGUIGraphics applyThemeStyle(ThemeStyle themeStyle);

    
    TextGUIGraphics setBackgroundColor(TextColor backgroundColor);

    
    TextGUIGraphics setForegroundColor(TextColor foregroundColor);

    
    TextGUIGraphics enableModifiers(SGR... modifiers);

    
    TextGUIGraphics disableModifiers(SGR... modifiers);

    
    TextGUIGraphics setModifiers(EnumSet<SGR> modifiers);

    
    TextGUIGraphics clearModifiers();

    
    TextGUIGraphics setTabBehaviour(TabBehaviour tabBehaviour);

    
    TextGUIGraphics fill(char c);

    
    TextGUIGraphics fillRectangle(TerminalPosition topLeft, TerminalSize size, char character);

    
    TextGUIGraphics fillRectangle(TerminalPosition topLeft, TerminalSize size, TextCharacter character);

    
    TextGUIGraphics drawRectangle(TerminalPosition topLeft, TerminalSize size, char character);

    
    TextGUIGraphics drawRectangle(TerminalPosition topLeft, TerminalSize size, TextCharacter character);

    
    TextGUIGraphics fillTriangle(TerminalPosition p1, TerminalPosition p2, TerminalPosition p3, char character);

    
    TextGUIGraphics fillTriangle(TerminalPosition p1, TerminalPosition p2, TerminalPosition p3, TextCharacter character);

    
    TextGUIGraphics drawTriangle(TerminalPosition p1, TerminalPosition p2, TerminalPosition p3, char character);

    
    TextGUIGraphics drawTriangle(TerminalPosition p1, TerminalPosition p2, TerminalPosition p3, TextCharacter character);

    
    TextGUIGraphics drawLine(TerminalPosition fromPoint, TerminalPosition toPoint, char character);

    
    TextGUIGraphics drawLine(TerminalPosition fromPoint, TerminalPosition toPoint, TextCharacter character);

    
    TextGUIGraphics drawLine(int fromX, int fromY, int toX, int toY, char character);

    
    TextGUIGraphics drawLine(int fromX, int fromY, int toX, int toY, TextCharacter character);

    
    TextGUIGraphics drawImage(TerminalPosition topLeft, TextImage image);

    
    TextGUIGraphics drawImage(TerminalPosition topLeft, TextImage image, TerminalPosition sourceImageTopLeft, TerminalSize sourceImageSize);

    
    TextGUIGraphics setCharacter(TerminalPosition position, char character);

    
    TextGUIGraphics setCharacter(TerminalPosition position, TextCharacter character);

    
    TextGUIGraphics setCharacter(int column, int row, char character);

    
    TextGUIGraphics setCharacter(int column, int row, TextCharacter character);

    
    TextGUIGraphics putString(int column, int row, String string);

    
    TextGUIGraphics putString(TerminalPosition position, String string);

    
    TextGUIGraphics putString(int column, int row, String string, SGR extraModifier, SGR... optionalExtraModifiers);

    
    TextGUIGraphics putString(TerminalPosition position, String string, SGR extraModifier, SGR... optionalExtraModifiers);

    
    TextGUIGraphics putString(int column, int row, String string, Collection<SGR> extraModifiers);

    
    TextGUIGraphics putCSIStyledString(int column, int row, String string);

    
    TextGUIGraphics putCSIStyledString(TerminalPosition position, String string);

    
    TextGUIGraphics setStyleFrom(StyleSet<?> source);

}
