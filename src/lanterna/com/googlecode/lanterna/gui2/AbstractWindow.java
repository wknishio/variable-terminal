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
package com.googlecode.lanterna.gui2;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalRectangle;
import com.googlecode.lanterna.gui2.menu.MenuBar;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyType;

import java.util.*;

/**
 * Abstract Window has most of the code requiring for a window to function, all concrete window implementations extends
 * from this in one way or another. You can define your own window by extending from this, as an alternative to building
 * up the GUI externally by constructing a {@code BasicWindow} and adding components to it.
 * @author Martin
 */
public abstract class AbstractWindow extends AbstractBasePane<Window> implements Window {
    private String title;
    private WindowBasedTextGUI textGUI;
    private boolean visible;
    private TerminalSize lastKnownSize;
    private TerminalSize lastKnownDecoratedSize;
    private TerminalPosition lastKnownPosition;
    private TerminalPosition contentOffset;
    private Set<Hint> hints;
    private WindowPostRenderer windowPostRenderer;
    private boolean closeWindowWithEscape;

    /**
     * Default constructor, this creates a window with no title
     */
    public AbstractWindow() {
        this("");
    }

    /**
     * Creates a window with a specific title that will (probably) be drawn in the window decorations
     * @param title Title of this window
     */
    public AbstractWindow(String title) {
        super();
        this.title = title;
        this.textGUI = null;
        this.visible = true;
        this.contentOffset = TerminalPosition.TOP_LEFT_CORNER;
        this.lastKnownPosition = null;
        this.lastKnownSize = null;
        this.lastKnownDecoratedSize = null;
        this.closeWindowWithEscape = false;

        this.hints = new HashSet<Hint>();
    }

    /**
     * Setting this property to {@code true} will cause pressing the ESC key to close the window. This used to be the
     * default behaviour of lanterna 3 during the development cycle but is not longer the case. You are encouraged to
     * put proper buttons or other kind of components to clearly mark to the user how to close the window instead of
     * magically taking ESC, but sometimes it can be useful (when doing testing, for example) to enable this mode.
     * @param closeWindowWithEscape If {@code true}, this window will self-close if you press ESC key
     */
    public void setCloseWindowWithEscape(boolean closeWindowWithEscape) {
        this.closeWindowWithEscape = closeWindowWithEscape;
    }

    
    public void setTextGUI(WindowBasedTextGUI textGUI) {
        //This is kind of stupid check, but might cause it to blow up on people using the library incorrectly instead of
        //just causing weird behaviour
        if(this.textGUI != null && textGUI != null) {
            throw new UnsupportedOperationException("Are you calling setTextGUI yourself? Please read the documentation"
                    + " in that case (this could also be a bug in Lanterna, please report it if you are sure you are "
                    + "not calling Window.setTextGUI(..) from your code)");
        }
        this.textGUI = textGUI;
    }

    
    public WindowBasedTextGUI getTextGUI() {
        return textGUI;
    }

    /**
     * Alters the title of the window to the supplied string
     * @param title New title of the window
     */
    public void setTitle(String title) {
        this.title = title;
        invalidate();
    }

    
    public String getTitle() {
        return title;
    }

    
    public boolean isVisible() {
        return visible;
    }

    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public void draw(TextGUIGraphics graphics) {
        if(!graphics.getSize().equals(lastKnownSize)) {
            getComponent().invalidate();
        }
        setSize(graphics.getSize(), false);
        super.draw(graphics);
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        boolean handled = super.handleInput(key);
        if(!handled && closeWindowWithEscape && key.getKeyType() == KeyType.Escape) {
            close();
            return true;
        }
        return handled;
    }

    /**
     * @see Window#toGlobal()
     */
    
    @Deprecated
    public TerminalPosition toGlobal(TerminalPosition localPosition) {
        return toGlobalFromContentRelative(localPosition);
    }
    
    
    public TerminalPosition toGlobalFromContentRelative(TerminalPosition contentLocalPosition) {
        if(contentLocalPosition == null) {
            return null;
        }
        return lastKnownPosition.withRelative(contentOffset.withRelative(contentLocalPosition));
    }
    
    
    @Deprecated
    public TerminalPosition toGlobalFromDecoratedRelative(TerminalPosition localPosition) {
        if(localPosition == null) {
            return null;
        }
        return lastKnownPosition.withRelative(localPosition);
    }

    /**
     * @see Window#fromGlobal()
     */
    
    @Deprecated
    public TerminalPosition fromGlobal(TerminalPosition globalPosition) {
        return fromGlobalToContentRelative(globalPosition);
    }
    
    
    public TerminalPosition fromGlobalToContentRelative(TerminalPosition globalPosition) {
        if(globalPosition == null || lastKnownPosition == null) {
            return null;
        }
        return globalPosition.withRelative(
                -lastKnownPosition.getColumn() - contentOffset.getColumn(),
                -lastKnownPosition.getRow() - contentOffset.getRow());
    }
    
    
    public TerminalPosition fromGlobalToDecoratedRelative(TerminalPosition globalPosition) {
        if(globalPosition == null || lastKnownPosition == null) {
            return null;
        }
        return globalPosition.withRelative(
                -lastKnownPosition.getColumn(),
                -lastKnownPosition.getRow());
    }

    
    public TerminalSize getPreferredSize() {
        TerminalSize preferredSize = contentHolder.getPreferredSize();
        MenuBar menuBar = getMenuBar();
        if (menuBar.getMenuCount() > 0) {
            TerminalSize menuPreferredSize = menuBar.getPreferredSize();
            preferredSize = preferredSize.withRelativeRows(menuPreferredSize.getRows())
                    .withColumns(Math.max(menuPreferredSize.getColumns(), preferredSize.getColumns()));
        }
        return preferredSize;
    }

    
    public void setHints(Collection<Hint> hints) {
        this.hints = new HashSet<Hint>(hints);
        invalidate();
    }

    
    public Set<Hint> getHints() {
        return Collections.unmodifiableSet(hints);
    }

    
    public WindowPostRenderer getPostRenderer() {
        return  windowPostRenderer;
    }

    
    public void addWindowListener(WindowListener windowListener) {
        addBasePaneListener(windowListener);
    }

    
    public void removeWindowListener(WindowListener windowListener) {
        removeBasePaneListener(windowListener);
    }

    /**
     * Sets the post-renderer to use for this window. This will override the default from the GUI system (if there is
     * one set, otherwise from the theme).
     * @param windowPostRenderer Window post-renderer to assign to this window
     */
    public void setWindowPostRenderer(WindowPostRenderer windowPostRenderer) {
        this.windowPostRenderer = windowPostRenderer;
    }

    
    public final TerminalPosition getPosition() {
        return lastKnownPosition;
    }

    
    public final void setPosition(TerminalPosition topLeft) {
        TerminalPosition oldPosition = this.lastKnownPosition;
        this.lastKnownPosition = topLeft;

        // Fire listeners
        for(BasePaneListener<?> listener: getBasePaneListeners()) {
            if(listener instanceof WindowListener) {
                ((WindowListener)listener).onMoved(this, oldPosition, topLeft);
            }
        }
    }

    
    public final TerminalSize getSize() {
        return lastKnownSize;
    }

    
    @Deprecated
    public void setSize(TerminalSize size) {
        setSize(size, true);
    }

    
    public void setFixedSize(TerminalSize size) {
        hints.add(Hint.FIXED_SIZE);
        setSize(size);
    }

    private void setSize(TerminalSize size, boolean invalidate) {
        TerminalSize oldSize = this.lastKnownSize;
        this.lastKnownSize = size;
        if(invalidate) {
            invalidate();
        }

        // Fire listeners
        for(BasePaneListener<?> listener: getBasePaneListeners()) {
            if(listener instanceof WindowListener) {
                ((WindowListener)listener).onResized(this, oldSize, size);
            }
        }
    }

    
    public final TerminalSize getDecoratedSize() {
        return lastKnownDecoratedSize;
    }

    
    public final void setDecoratedSize(TerminalSize decoratedSize) {
        this.lastKnownDecoratedSize = decoratedSize;
    }

    
    public void setContentOffset(TerminalPosition offset) {
        this.contentOffset = offset;
    }

    
    public void close() {
        if(textGUI != null) {
            textGUI.removeWindow(this);
        }
        setComponent(null);
    }

    
    public void waitUntilClosed() {
        WindowBasedTextGUI textGUI = getTextGUI();
        if(textGUI != null) {
            textGUI.waitForWindowToClose(this);
        }
    }

    Window self() { return this; }
    
    /**
     * Return the last known size of the window including window decoration and the window position as a TerminalRectangle.
     * @return the decorated size and position of the window
     */
    public TerminalRectangle getBounds() {
        TerminalPosition position = getPosition();
        TerminalSize size = getDecoratedSize();
        return new TerminalRectangle(position.getColumn(), position.getRow(), size.getColumns(), size.getRows());
    }
}
