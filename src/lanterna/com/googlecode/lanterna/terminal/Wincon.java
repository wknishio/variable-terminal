/*
 * This file is part of lanterna (http://code.google.com/p/lanterna/).
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
 * Copyright (C) 2010-2019 Martin Berglund
 */
package com.googlecode.lanterna.terminal;

import com.googlecode.lanterna.terminal.WinDef.COORD;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

/**
 * Interface to Wincon, module in Win32 that can operate on the terminal
 */
interface Wincon extends StdCallLibrary {
    int STD_INPUT_HANDLE = -10;
    int STD_OUTPUT_HANDLE = -11;

    // SetConsoleMode input values
    
    int ENABLE_PROCESSED_INPUT = 0x1;
    int ENABLE_LINE_INPUT = 0x2;
    int ENABLE_ECHO_INPUT = 0x4;
    int ENABLE_WINDOW_INPUT = 0x8;
    int ENABLE_MOUSE_INPUT = 0x10;
    int ENABLE_INSERT_MODE = 0x20;
    int ENABLE_QUICK_EDIT_MODE = 0x40;
    int ENABLE_EXTENDED_FLAGS = 0x80;
    int ENABLE_AUTO_POSITION = 0x100;

    // SetConsoleMode screen buffer values
    int ENABLE_PROCESSED_OUTPUT = 0x1;
    int ENABLE_WRAP_AT_EOL_OUTPUT =  0x2;

    int ENABLE_VIRTUAL_TERMINAL_PROCESSING = 4;
    int DISABLE_NEWLINE_AUTO_RETURN = 8;

    WinDef.HANDLE GetStdHandle(int var1);
    boolean GetConsoleMode(WinDef.HANDLE var1, IntByReference var2);
    boolean SetConsoleMode(WinDef.HANDLE var1, int var2);
    boolean GetConsoleScreenBufferInfo(WinDef.HANDLE hConsoleOutput, WinDef.CONSOLE_SCREEN_BUFFER_INFO lpConsoleScreenBufferInfo);
    void SetConsoleCursorPosition(WinDef.HANDLE hConsoleOutput, COORD in_dwCursorPosition);
}
