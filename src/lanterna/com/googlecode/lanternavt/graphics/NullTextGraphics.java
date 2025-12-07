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
package com.googlecode.lanternavt.graphics;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

import com.googlecode.lanternavt.*;
import com.googlecode.lanternavt.screen.TabBehaviour;

/**
 * TextGraphics implementation that does nothing, but has a pre-defined size
 * @author martin
 */
class NullTextGraphics implements TextGraphics {
    private final TerminalSize size;
    private TextColor foregroundColor;
    private TextColor backgroundColor;
    private TabBehaviour tabBehaviour;
    private final EnumSet<SGR> activeModifiers;

    /**
     * Creates a new {@code NullTextGraphics} that will return the specified size value if asked how big it is but other
     * than that ignore all other calls.
     * @param size The size to report
     */
    public NullTextGraphics(TerminalSize size) {
        this.size = size;
        this.foregroundColor = TextColor.ANSI.DEFAULT;
        this.backgroundColor = TextColor.ANSI.DEFAULT;
        this.tabBehaviour = TabBehaviour.CONVERT_TO_ONE_SPACE;
        this.activeModifiers = EnumSet.noneOf(SGR.class);
    }

    
    public TerminalSize getSize() {
        return size;
    }

    
    public TextGraphics newTextGraphics(TerminalPosition topLeftCorner, TerminalSize size) throws IllegalArgumentException {
        return this;
    }

    
    public TextColor getBackgroundColor() {
        return backgroundColor;
    }

    
    public TextGraphics setBackgroundColor(TextColor backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    
    public TextColor getForegroundColor() {
        return foregroundColor;
    }

    
    public TextGraphics setForegroundColor(TextColor foregroundColor) {
        this.foregroundColor = foregroundColor;
        return this;
    }

    
    public TextGraphics enableModifiers(SGR... modifiers) {
        activeModifiers.addAll(Arrays.asList(modifiers));
        return this;
    }

    
    public TextGraphics disableModifiers(SGR... modifiers) {
        activeModifiers.removeAll(Arrays.asList(modifiers));
        return this;
    }

    
    public TextGraphics setModifiers(EnumSet<SGR> modifiers) {
        clearModifiers();
        activeModifiers.addAll(modifiers);
        return this;
    }

    
    public TextGraphics clearModifiers() {
        activeModifiers.clear();
        return this;
    }

    
    public EnumSet<SGR> getActiveModifiers() {
        return EnumSet.copyOf(activeModifiers);
    }

    
    public TabBehaviour getTabBehaviour() {
        return tabBehaviour;
    }

    
    public TextGraphics setTabBehaviour(TabBehaviour tabBehaviour) {
        this.tabBehaviour = tabBehaviour;
        return this;
    }

    
    public TextGraphics fill(char c) {
        return this;
    }

    
    public TextGraphics setCharacter(int column, int row, char character) {
        return this;
    }

    
    public TextGraphics setCharacter(int column, int row, TextCharacter character) {
        return this;
    }

    
    public TextGraphics setCharacter(TerminalPosition position, char character) {
        return this;
    }

    
    public TextGraphics setCharacter(TerminalPosition position, TextCharacter character) {
        return this;
    }

    
    public TextGraphics drawLine(TerminalPosition fromPoint, TerminalPosition toPoint, char character) {
        return this;
    }

    
    public TextGraphics drawLine(TerminalPosition fromPoint, TerminalPosition toPoint, TextCharacter character) {
        return this;
    }

    
    public TextGraphics drawLine(int fromX, int fromY, int toX, int toY, char character) {
        return this;
    }

    
    public TextGraphics drawLine(int fromX, int fromY, int toX, int toY, TextCharacter character) {
        return this;
    }

    
    public TextGraphics drawTriangle(TerminalPosition p1, TerminalPosition p2, TerminalPosition p3, char character) {
        return this;
    }

    
    public TextGraphics drawTriangle(TerminalPosition p1, TerminalPosition p2, TerminalPosition p3, TextCharacter character) {
        return this;
    }

    
    public TextGraphics fillTriangle(TerminalPosition p1, TerminalPosition p2, TerminalPosition p3, char character) {
        return this;
    }

    
    public TextGraphics fillTriangle(TerminalPosition p1, TerminalPosition p2, TerminalPosition p3, TextCharacter character) {
        return this;
    }

    
    public TextGraphics drawRectangle(TerminalPosition topLeft, TerminalSize size, char character) {
        return this;
    }

    
    public TextGraphics drawRectangle(TerminalPosition topLeft, TerminalSize size, TextCharacter character) {
        return this;
    }

    
    public TextGraphics fillRectangle(TerminalPosition topLeft, TerminalSize size, char character) {
        return this;
    }

    
    public TextGraphics fillRectangle(TerminalPosition topLeft, TerminalSize size, TextCharacter character) {
        return this;
    }

    
    public TextGraphics drawImage(TerminalPosition topLeft, TextImage image) {
        return this;
    }

    
    public TextGraphics drawImage(TerminalPosition topLeft, TextImage image, TerminalPosition sourceImageTopLeft, TerminalSize sourceImageSize) {
        return this;
    }

    
    public TextGraphics putString(int column, int row, String string) {
        return this;
    }

    
    public TextGraphics putString(TerminalPosition position, String string) {
        return this;
    }

    
    public TextGraphics putString(int column, int row, String string, SGR extraModifier, SGR... optionalExtraModifiers) {
        return this;
    }

    
    public TextGraphics putString(TerminalPosition position, String string, SGR extraModifier, SGR... optionalExtraModifiers) {
        return this;
    }

    
    public TextGraphics putString(int column, int row, String string, Collection<SGR> extraModifiers) {
        return this;
    }

    
    public TextGraphics putCSIStyledString(int column, int row, String string) {
        return this;
    }

    
    public TextGraphics putCSIStyledString(TerminalPosition position, String string) {
        return this;
    }

    
    public TextCharacter getCharacter(int column, int row) {
        return null;
    }

    
    public TextCharacter getCharacter(TerminalPosition position) {
        return null;
    }

    
    public TextGraphics setStyleFrom(StyleSet<?> source) {
        setBackgroundColor(source.getBackgroundColor());
        setForegroundColor(source.getForegroundColor());
        setModifiers(source.getActiveModifiers());
        return this;
    }

}
