package org.vash.vate.com.googlecode.lanterna.terminal.swing;

import java.awt.*;
import java.awt.font.TextHitInfo;
import java.awt.im.InputMethodRequests;
import java.text.AttributedCharacterIterator;

import org.vash.vate.com.googlecode.lanterna.TerminalPosition;

class TerminalInputMethodRequests implements InputMethodRequests {

    private Component owner;
    private GraphicalTerminalImplementation terminalImplementation;
    
    public TerminalInputMethodRequests(Component owner, GraphicalTerminalImplementation terminalImplementation) {
        this.owner = owner;
        this.terminalImplementation = terminalImplementation;
    }
    
    
    public Rectangle getTextLocation(TextHitInfo offset) {
        Point location = owner.getLocationOnScreen();
        TerminalPosition cursorPosition = terminalImplementation.getCursorPosition();

        int offsetX = cursorPosition.getColumn() * terminalImplementation.getFontWidth();
        int offsetY = cursorPosition.getRow() * terminalImplementation.getFontHeight() + terminalImplementation.getFontHeight();

        return new Rectangle(location.x + offsetX, location.y + offsetY, 0, 0);
    }

    
    public TextHitInfo getLocationOffset(int x, int y) {
        return null;
    }

    
    public int getInsertPositionOffset() {
        return 0;
    }

    
    public AttributedCharacterIterator getCommittedText(int beginIndex, int endIndex, AttributedCharacterIterator.Attribute[] attributes) {
        return null;
    }

    
    public int getCommittedTextLength() {
        return 0;
    }

    
    public AttributedCharacterIterator cancelLatestCommittedText(AttributedCharacterIterator.Attribute[] attributes) {
        return null;
    }

    
    public AttributedCharacterIterator getSelectedText(AttributedCharacterIterator.Attribute[] attributes) {
        return null;
    }
}
