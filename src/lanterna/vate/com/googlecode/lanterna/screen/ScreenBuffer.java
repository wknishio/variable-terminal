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
package vate.com.googlecode.lanterna.screen;

import vate.com.googlecode.lanterna.TerminalPosition;
import vate.com.googlecode.lanterna.TerminalSize;
import vate.com.googlecode.lanterna.TextCharacter;
import vate.com.googlecode.lanterna.graphics.BasicTextImage;
import vate.com.googlecode.lanterna.graphics.TextGraphics;
import vate.com.googlecode.lanterna.graphics.TextImage;

/**
 * Defines a buffer used by AbstractScreen and its subclasses to keep its state of what's currently displayed and what 
 * the edit buffer looks like. A ScreenBuffer is essentially a two-dimensional array of TextCharacter with some utility
 * methods to inspect and manipulate it in a safe way.
 * @author martin
 */
public class ScreenBuffer implements TextImage {    
    private final BasicTextImage backend;
    
    /**
     * Creates a new ScreenBuffer with a given size and a TextCharacter to initially fill it with
     * @param size Size of the buffer
     * @param filler What character to set as the initial content of the buffer
     */
    public ScreenBuffer(TerminalSize size, TextCharacter filler) {
        this(new BasicTextImage(size, filler));
    }
    
    private ScreenBuffer(BasicTextImage backend) {
        this.backend = backend;
    }
    
    
    public ScreenBuffer resize(TerminalSize newSize, TextCharacter filler) {
        BasicTextImage resizedBackend = backend.resize(newSize, filler);
        return new ScreenBuffer(resizedBackend);
    }
    
    boolean isVeryDifferent(ScreenBuffer other, int threshold) {
        if(!getSize().equals(other.getSize())) {
            throw new IllegalArgumentException("Can only call isVeryDifferent comparing two ScreenBuffers of the same size!"
                    + " This is probably a bug in Lanterna.");
        }
        int differences = 0;
        for(int y = 0; y < getSize().getRows(); y++) {
            for(int x = 0; x < getSize().getColumns(); x++) {
                if(!getCharacterAt(x, y).equals(other.getCharacterAt(x, y))) {
                    if(++differences >= threshold) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    ///////////////////////////////////////////////////////////////////////////////
    //  Delegate all TextImage calls (except resize) to the backend BasicTextImage
    
    public TerminalSize getSize() {
        return backend.getSize();
    }

    
    public TextCharacter getCharacterAt(TerminalPosition position) {
        return backend.getCharacterAt(position);
    }

    
    public TextCharacter getCharacterAt(int column, int row) {
        return backend.getCharacterAt(column, row);
    }

    
    public void setCharacterAt(TerminalPosition position, TextCharacter character) {
        backend.setCharacterAt(position, character);
    }

    
    public void setCharacterAt(int column, int row, TextCharacter character) {
        backend.setCharacterAt(column, row, character);
    }

    
    public void setAll(TextCharacter character) {
        backend.setAll(character);
    }

    
    public TextGraphics newTextGraphics() {
        return backend.newTextGraphics();
    }

    
    public void copyTo(TextImage destination) {
        if(destination instanceof ScreenBuffer) {
            //This will allow the BasicTextImage's copy method to use System.arraycopy (micro-optimization?)
            destination = ((ScreenBuffer)destination).backend;
        }
        backend.copyTo(destination);
    }

    
    public void copyTo(TextImage destination, int startRowIndex, int rows, int startColumnIndex, int columns, int destinationRowOffset, int destinationColumnOffset) {
        if(destination instanceof ScreenBuffer) {
            //This will allow the BasicTextImage's copy method to use System.arraycopy (micro-optimization?)
            destination = ((ScreenBuffer)destination).backend;
        }
        backend.copyTo(destination, startRowIndex, rows, startColumnIndex, columns, destinationRowOffset, destinationColumnOffset);
    }

    /**
     * Copies the content from a TextImage into this buffer.
     * @param source Source to copy content from
     * @param startRowIndex Which row in the source image to start copying from
     * @param rows How many rows to copy
     * @param startColumnIndex Which column in the source image to start copying from
     * @param columns How many columns to copy
     * @param destinationRowOffset The row offset in this buffer of where to place the copied content
     * @param destinationColumnOffset The column offset in this buffer of where to place the copied content
     */
    public void copyFrom(TextImage source, int startRowIndex, int rows, int startColumnIndex, int columns, int destinationRowOffset, int destinationColumnOffset) {
        source.copyTo(backend, startRowIndex, rows, startColumnIndex, columns, destinationRowOffset, destinationColumnOffset);
    }

    
    public void scrollLines(int firstLine, int lastLine, int distance) {
        backend.scrollLines(firstLine, lastLine, distance);
    }
    
    
    public String toString() {
        return backend.toString();
    }
}
