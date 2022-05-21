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
package com.googlecode.lanterna.terminal;

/**
 * Constant describing different modes for capturing mouse input. By default, no mouse capturing is enabled (unless
 * previously enabled before starting the Lanterna application. These are the different modes of input capturing
 * supported. Please note that terminal emulators vary widely in how these are implemented!
 *
 * Also, some terminals such as Konsole, appear to unfortunately round up and down the column which is in the generated MouseEvent
 * The column actually starts about halfway prior of the left border of the character and goes to about halfway past that edge.
 * This makes them rather unsuitable for precision clicking on specific characters on the screen.
 * The XTerm terminal appears to properly generate with the input stream with location along the boundaries of the characters as
 * needed for clicks within the bounds of the characters to properly map to that column.
 *
 * Created by martin on 26/07/15.
 */
public enum MouseCaptureMode {
    /**
     * Mouse clicks are captured on the down-motion but not the up-motion. This corresponds to the X10 xterm protocol.
     * KDE's Konsole (tested with 15.04) does not implement this extension, but xfce4-terminal, gnome-terminal and
     * xterm does.
     */
    CLICK,
    /**
     * Mouse clicks are captured both on down and up, this is the normal mode for capturing mouse input. KDE's konsole
     * interprets this as CLICK_RELEASE_DRAG.
     */
    CLICK_RELEASE,
    /**
     * Mouse clicks are captured both on down and up and if the mouse if moved while holding down one of the button, a
     * drag event is generated.
     */
    CLICK_RELEASE_DRAG,
    /**
     * Mouse clicks are captured both on down and up and also all mouse movements, no matter if any button is held down
     * or not.
     */
    CLICK_RELEASE_DRAG_MOVE,
    ;
}
