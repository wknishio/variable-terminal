/*
 * This file is part of lanterna.
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
 * Copyright (C) 2010-2017 Martin Berglund
 * Copyright (C) 2017 Bruno Eberhard
 */
package org.vash.vate.com.googlecode.lanterna.gui2.menu;

import org.vash.vate.com.googlecode.lanterna.TerminalPosition;
import org.vash.vate.com.googlecode.lanterna.gui2.AbstractListBox;
import org.vash.vate.com.googlecode.lanterna.input.KeyStroke;
import org.vash.vate.com.googlecode.lanterna.input.KeyType;

public class MenuItemListBox extends AbstractListBox<Runnable, MenuItemListBox> {

	private final Runnable closeListener;

	/**
	 * Default constructor, creates an {@code MenuItemListBox} with no pre-defined
	 * size that will request to be big enough to display all items
	 */
	public MenuItemListBox(Runnable closeListener) {
		this.closeListener = closeListener;
	}

	
	public TerminalPosition getCursorLocation() {
		return null;
	}

	
	public Result handleKeyStroke(KeyStroke keyStroke) {
		Runnable selectedItem = getSelectedItem();
		KeyType keyType = keyStroke.getKeyType();
		if (selectedItem != null
				&& (keyType == KeyType.ENTER || (keyType == KeyType.CHARACTER && keyStroke.getCharacter() == ' '))) {
			selectedItem.run();
			return Result.HANDLED;
		}
		if (keyType == KeyType.ESCAPE) {
			closeListener.run();
			return Result.HANDLED;
		}
		return super.handleKeyStroke(keyStroke);
	}
}
