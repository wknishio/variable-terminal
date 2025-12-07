package vate.com.googlecode.lanterna.bundle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Properties;

import vate.com.googlecode.lanterna.graphics.PropertyTheme;

class DefaultTheme extends PropertyTheme {
    DefaultTheme() {
        super(definitionAsProperty(), false);
    }

    private static Properties definitionAsProperty() {
        Properties properties = new Properties();
        try {
            properties.load(new ByteArrayInputStream(definition.getBytes()));
            return properties;
        }
        catch(IOException e) {
            // We should never get here!
            throw new RuntimeException("Unexpected I/O error", e);
        }
    }

    private static final String definition = "# This is the default properties\n" +
            "\n" +
            "foreground = black\n" +
            "background = white\n" +
            "sgr        =\n" +
            "foreground[SELECTED] = white\n" +
            "background[SELECTED] = blue\n" +
            "sgr[SELECTED]        = bold\n" +
            "foreground[PRELIGHT] = white\n" +
            "background[PRELIGHT] = blue\n" +
            "sgr[PRELIGHT]        = bold\n" +
            "foreground[ACTIVE]   = white\n" +
            "background[ACTIVE]   = blue\n" +
            "sgr[ACTIVE]          = bold\n" +
            "foreground[INSENSITIVE] = white\n" +
            "background[INSENSITIVE] = blue\n" +
            "sgr[INSENSITIVE]     =\n" +
            "\n" +
            "# By default use the shadow post-renderer\n" +
            "postrenderer = vate.com.googlecode.lanterna.gui2.WindowShadowRenderer\n" +
            "\n" +
            "#Borders\n" +
            "vate.com.googlecode.lanterna.gui2.AbstractBorder.background[PRELIGHT] = white\n" +
            "vate.com.googlecode.lanterna.gui2.AbstractBorder.foreground[ACTIVE] = black\n" +
            "vate.com.googlecode.lanterna.gui2.AbstractBorder.background[ACTIVE] = white\n" +
            "vate.com.googlecode.lanterna.gui2.AbstractBorder.sgr[ACTIVE] =\n" +
            "vate.com.googlecode.lanterna.gui2.AbstractBorder.foreground[INSENSITIVE] = black\n" +
            "vate.com.googlecode.lanterna.gui2.AbstractBorder.background[INSENSITIVE] = white\n" +
            "vate.com.googlecode.lanterna.gui2.Borders$SingleLine.char[HORIZONTAL_LINE] = \\u2500\n" +
            "vate.com.googlecode.lanterna.gui2.Borders$SingleLine.char[VERTICAL_LINE] = \\u2502\n" +
            "vate.com.googlecode.lanterna.gui2.Borders$SingleLine.char[BOTTOM_LEFT_CORNER] = \\u2514\n" +
            "vate.com.googlecode.lanterna.gui2.Borders$SingleLine.char[TOP_LEFT_CORNER] = \\u250c\n" +
            "vate.com.googlecode.lanterna.gui2.Borders$SingleLine.char[BOTTOM_RIGHT_CORNER] = \\u2518\n" +
            "vate.com.googlecode.lanterna.gui2.Borders$SingleLine.char[TOP_RIGHT_CORNER] = \\u2510\n" +
            "vate.com.googlecode.lanterna.gui2.Borders$SingleLine.char[TITLE_LEFT] = \\u2500\n" +
            "vate.com.googlecode.lanterna.gui2.Borders$SingleLine.char[TITLE_RIGHT] = \\u2500\n" +
            "vate.com.googlecode.lanterna.gui2.Borders$DoubleLine.char[HORIZONTAL_LINE] = \\u2550\n" +
            "vate.com.googlecode.lanterna.gui2.Borders$DoubleLine.char[VERTICAL_LINE] = \\u2551\n" +
            "vate.com.googlecode.lanterna.gui2.Borders$DoubleLine.char[BOTTOM_LEFT_CORNER] = \\u255a\n" +
            "vate.com.googlecode.lanterna.gui2.Borders$DoubleLine.char[TOP_LEFT_CORNER] = \\u2554\n" +
            "vate.com.googlecode.lanterna.gui2.Borders$DoubleLine.char[BOTTOM_RIGHT_CORNER] = \\u255d\n" +
            "vate.com.googlecode.lanterna.gui2.Borders$DoubleLine.char[TOP_RIGHT_CORNER] = \\u2557\n" +
            "vate.com.googlecode.lanterna.gui2.Borders$DoubleLine.char[TITLE_LEFT] = \\u2550\n" +
            "vate.com.googlecode.lanterna.gui2.Borders$DoubleLine.char[TITLE_RIGHT] = \\u2550\n" +
            "\n" +
            "#Button\n" +
            "vate.com.googlecode.lanterna.gui2.Button.renderer = vate.com.googlecode.lanterna.gui2.Button$DefaultButtonRenderer\n" +
            "vate.com.googlecode.lanterna.gui2.Button.sgr = bold\n" +
            "vate.com.googlecode.lanterna.gui2.Button.foreground[SELECTED] = yellow\n" +
            "vate.com.googlecode.lanterna.gui2.Button.foreground[PRELIGHT] = red\n" +
            "vate.com.googlecode.lanterna.gui2.Button.background[PRELIGHT] = white\n" +
            "vate.com.googlecode.lanterna.gui2.Button.sgr[PRELIGHT] =\n" +
            "vate.com.googlecode.lanterna.gui2.Button.foreground[INSENSITIVE] = black\n" +
            "vate.com.googlecode.lanterna.gui2.Button.background[INSENSITIVE] = white\n" +
            "vate.com.googlecode.lanterna.gui2.Button.char[LEFT_BORDER] = <\n" +
            "vate.com.googlecode.lanterna.gui2.Button.char[RIGHT_BORDER] = >\n" +
            "\n" +
            "# CheckBox\n" +
            "vate.com.googlecode.lanterna.gui2.CheckBox.foreground[INSENSITIVE] = black\n" +
            "vate.com.googlecode.lanterna.gui2.CheckBox.background[INSENSITIVE] = white\n" +
            "vate.com.googlecode.lanterna.gui2.CheckBox.char[MARKER] = x\n" +
            "\n" +
            "# CheckBoxList\n" +
            "vate.com.googlecode.lanterna.gui2.CheckBoxList.foreground[SELECTED] = black\n" +
            "vate.com.googlecode.lanterna.gui2.CheckBoxList.background[SELECTED] = white\n" +
            "vate.com.googlecode.lanterna.gui2.CheckBoxList.sgr[SELECTED] =\n" +
            "vate.com.googlecode.lanterna.gui2.CheckBoxList.char[LEFT_BRACKET] = [\n" +
            "vate.com.googlecode.lanterna.gui2.CheckBoxList.char[RIGHT_BRACKET] = ]\n" +
            "vate.com.googlecode.lanterna.gui2.CheckBoxList.char[MARKER] = x\n" +
            "\n" +
            "# ComboBox\n" +
            "vate.com.googlecode.lanterna.gui2.ComboBox.sgr[PRELIGHT] =\n" +
            "vate.com.googlecode.lanterna.gui2.ComboBox.foreground[INSENSITIVE] = black\n" +
            "vate.com.googlecode.lanterna.gui2.ComboBox.background[INSENSITIVE] = white\n" +
            "vate.com.googlecode.lanterna.gui2.ComboBox.foreground[SELECTED] = black\n" +
            "vate.com.googlecode.lanterna.gui2.ComboBox.background[SELECTED] = white\n" +
            "\n" +
            "# Default color and style for the window decoration renderer\n" +
            "vate.com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer.foreground[ACTIVE] = black\n" +
            "vate.com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer.background[ACTIVE] = white\n" +
            "vate.com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer.sgr[ACTIVE] =\n" +
            "vate.com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer.foreground[INSENSITIVE] = black\n" +
            "vate.com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer.background[INSENSITIVE] = white\n" +
            "vate.com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer.background[PRELIGHT] = white\n" +
            "vate.com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer.char[HORIZONTAL_LINE] = \\u2500\n" +
            "vate.com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer.char[VERTICAL_LINE] = \\u2502\n" +
            "vate.com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer.char[BOTTOM_LEFT_CORNER] = \\u2514\n" +
            "vate.com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer.char[TOP_LEFT_CORNER] = \\u250c\n" +
            "vate.com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer.char[BOTTOM_RIGHT_CORNER] = \\u2518\n" +
            "vate.com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer.char[TOP_RIGHT_CORNER] = \\u2510\n" +
            "vate.com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer.char[TITLE_SEPARATOR_LEFT] = \\u2500\n" +
            "vate.com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer.char[TITLE_SEPARATOR_RIGHT] = \\u2500\n" +
            "vate.com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer.property[TITLE_PADDING] = false\n" +
            "vate.com.googlecode.lanterna.gui2.DefaultWindowDecorationRenderer.property[CENTER_TITLE] = false\n" +
            "\n" +
            "# GUI Backdrop\n" +
            "vate.com.googlecode.lanterna.gui2.GUIBackdrop.foreground = cyan\n" +
            "vate.com.googlecode.lanterna.gui2.GUIBackdrop.background = blue\n" +
            "vate.com.googlecode.lanterna.gui2.GUIBackdrop.sgr = bold\n" +
            "\n" +
            "# List boxes default\n" +
            "vate.com.googlecode.lanterna.gui2.AbstractListBox.foreground[INSENSITIVE] = black\n" +
            "vate.com.googlecode.lanterna.gui2.AbstractListBox.background[INSENSITIVE] = white\n" +
            "\n" +
            "# ProgressBar\n" +
            "vate.com.googlecode.lanterna.gui2.ProgressBar.foreground = white\n" +
            "vate.com.googlecode.lanterna.gui2.ProgressBar.background = blue\n" +
            "vate.com.googlecode.lanterna.gui2.ProgressBar.sgr = bold\n" +
            "vate.com.googlecode.lanterna.gui2.ProgressBar.background[ACTIVE] = red\n" +
            "vate.com.googlecode.lanterna.gui2.ProgressBar.foreground[PRELIGHT] = red\n" +
            "vate.com.googlecode.lanterna.gui2.ProgressBar.sgr[PRELIGHT] =\n" +
            "vate.com.googlecode.lanterna.gui2.ProgressBar.char[FILLER] =\n" +
            "\n" +
            "# RadioBoxList\n" +
            "vate.com.googlecode.lanterna.gui2.RadioBoxList.foreground[SELECTED] = black\n" +
            "vate.com.googlecode.lanterna.gui2.RadioBoxList.background[SELECTED] = white\n" +
            "vate.com.googlecode.lanterna.gui2.RadioBoxList.sgr[SELECTED] =\n" +
            "vate.com.googlecode.lanterna.gui2.RadioBoxList.char[LEFT_BRACKET] = <\n" +
            "vate.com.googlecode.lanterna.gui2.RadioBoxList.char[RIGHT_BRACKET] = >\n" +
            "vate.com.googlecode.lanterna.gui2.RadioBoxList.char[MARKER] = o\n" +
            "\n" +
            "# ScrollBar\n" +
            "vate.com.googlecode.lanterna.gui2.ScrollBar.char[UP_ARROW]=\\u25b2\n" +
            "vate.com.googlecode.lanterna.gui2.ScrollBar.char[DOWN_ARROW]=\\u25bc\n" +
            "vate.com.googlecode.lanterna.gui2.ScrollBar.char[LEFT_ARROW]=\\u25c4\n" +
            "vate.com.googlecode.lanterna.gui2.ScrollBar.char[RIGHT_ARROW]=\\u25ba\n" +
            "\n" +
            "vate.com.googlecode.lanterna.gui2.ScrollBar.char[VERTICAL_BACKGROUND]=\\u2592\n" +
            "vate.com.googlecode.lanterna.gui2.ScrollBar.char[VERTICAL_SMALL_TRACKER]=\\u2588\n" +
            "vate.com.googlecode.lanterna.gui2.ScrollBar.char[VERTICAL_TRACKER_BACKGROUND]=\\u2588\n" +
            "vate.com.googlecode.lanterna.gui2.ScrollBar.char[VERTICAL_TRACKER_TOP]=\\u2588\n" +
            "vate.com.googlecode.lanterna.gui2.ScrollBar.char[VERTICAL_TRACKER_BOTTOM]=\\u2588\n" +
            "\n" +
            "vate.com.googlecode.lanterna.gui2.ScrollBar.char[HORIZONTAL_BACKGROUND]=\\u2592\n" +
            "vate.com.googlecode.lanterna.gui2.ScrollBar.char[HORIZONTAL_SMALL_TRACKER]=\\u2588\n" +
            "vate.com.googlecode.lanterna.gui2.ScrollBar.char[HORIZONTAL_TRACKER_BACKGROUND]=\\u2588\n" +
            "vate.com.googlecode.lanterna.gui2.ScrollBar.char[HORIZONTAL_TRACKER_LEFT]=\\u2588\n" +
            "vate.com.googlecode.lanterna.gui2.ScrollBar.char[HORIZONTAL_TRACKER_RIGHT]=\\u2588\n" +
            "\n" +
            "# Separator\n" +
            "vate.com.googlecode.lanterna.gui2.Separator.sgr = bold\n" +
            "\n" +
            "# Table\n" +
            "vate.com.googlecode.lanterna.gui2.table.Table.sgr[HEADER] = underline,bold\n" +
            "vate.com.googlecode.lanterna.gui2.table.Table.foreground[SELECTED] = black\n" +
            "vate.com.googlecode.lanterna.gui2.table.Table.background[SELECTED] = white\n" +
            "vate.com.googlecode.lanterna.gui2.table.Table.sgr[SELECTED] =\n" +
            "\n" +
            "# TextBox\n" +
            "vate.com.googlecode.lanterna.gui2.TextBox.foreground = white\n" +
            "vate.com.googlecode.lanterna.gui2.TextBox.background = blue\n" +
            "\n" +
            "# Window shadow\n" +
            "vate.com.googlecode.lanterna.gui2.WindowShadowRenderer.background = black\n" +
            "vate.com.googlecode.lanterna.gui2.WindowShadowRenderer.sgr = bold\n" +
            "vate.com.googlecode.lanterna.gui2.WindowShadowRenderer.property[DOUBLE_WIDTH] = true\n" +
            "vate.com.googlecode.lanterna.gui2.WindowShadowRenderer.property[TRANSPARENT] = true";
}
