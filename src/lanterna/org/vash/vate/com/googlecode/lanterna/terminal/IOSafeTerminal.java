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
package org.vash.vate.com.googlecode.lanterna.terminal;

import java.util.concurrent.TimeUnit;

import org.vash.vate.com.googlecode.lanterna.SGR;
import org.vash.vate.com.googlecode.lanterna.TerminalPosition;
import org.vash.vate.com.googlecode.lanterna.TerminalSize;
import org.vash.vate.com.googlecode.lanterna.TextColor;
import org.vash.vate.com.googlecode.lanterna.input.KeyStroke;

/**
 * Interface extending Terminal that removes the IOException throw clause. You can for example use this instead of 
 * Terminal if you use an implementation that doesn't throw any IOExceptions or if you wrap your terminal in an 
 * IOSafeTerminalAdapter. Please note that readInput() still throws IOException when it is interrupted, in order to fit
 * better in with what normal terminal do when they are blocked on input and you interrupt them.
 * @author Martin
 */
public interface IOSafeTerminal extends Terminal {
    
    void enterPrivateMode();
    
    void exitPrivateMode();
    
    void clearScreen();
    
    void setCursorPosition(int x, int y);
    
    void setCursorPosition(TerminalPosition position);
    
    TerminalPosition getCursorPosition();
    
    void setCursorVisible(boolean visible);
    
    void putCharacter(char c);
    
    void enableSGR(SGR sgr);
    
    void disableSGR(SGR sgr);
    
    void resetColorAndSGR();
    
    void setForegroundColor(TextColor color);
    
    void setBackgroundColor(TextColor color);
    
    TerminalSize getTerminalSize();
    
    byte[] enquireTerminal(int timeout, TimeUnit timeoutUnit);
    
    void bell();
    
    void flush();
    
    KeyStroke pollInput();
    
    KeyStroke readInput();
    
    void close();
    
    void putString(String string);
}
