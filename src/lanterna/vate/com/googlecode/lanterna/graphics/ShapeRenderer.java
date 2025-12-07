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
package vate.com.googlecode.lanterna.graphics;

import vate.com.googlecode.lanterna.TerminalPosition;
import vate.com.googlecode.lanterna.TerminalSize;
import vate.com.googlecode.lanterna.TextCharacter;

/**
 * This package private interface exposes methods for translating abstract lines, triangles and rectangles to discreet
 * points on a grid.
 * @author Martin
 */
interface ShapeRenderer {
    void drawLine(TerminalPosition p1, TerminalPosition p2, TextCharacter character);
    void drawTriangle(TerminalPosition p1, TerminalPosition p2, TerminalPosition p3, TextCharacter character);
    void drawRectangle(TerminalPosition topLeft, TerminalSize size, TextCharacter character);
    void fillTriangle(TerminalPosition p1, TerminalPosition p2, TerminalPosition p3, TextCharacter character);
    void fillRectangle(TerminalPosition topLeft, TerminalSize size, TextCharacter character);
}
