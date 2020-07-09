/*
com.sixlegs.png - Java package to read and display PNG images
Copyright (C) 1998-2006 Chris Nokleberg

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version.
*/

package com.sixlegs.png.examples;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.Arrays;

public class WriteAnimatedDemos
{
    public static void main(String[] args)
    throws Exception
    {
        Dimension size = new Dimension(200, 200);
        Png2AnimatedPng.run(
            "--delay", "1000",
            "--blend", "1",
            "--crop",
            writeBlank(size),
            writeFrame(size, 0x55FF0000, -90),
            writeFrame(size, 0x5500FF00, 30),
            writeFrame(size, 0x550000FF, 150),
            args[0]
        );
    }

    private static String writeBlank(Dimension size)
    throws IOException
    {
        File file = File.createTempFile("demo", ".png");
        file.deleteOnExit();
        javax.imageio.ImageIO.write(new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB),
                                    "PNG", file);
        return file.getPath();
    }

    private static String writeFrame(Dimension size, int rgba, int angle)
    throws IOException
    {
        Point center = new Point(size.width / 2, size.height / 2 + 10);
        Shape ellipse = createEllipse(getPoint(center, size.width / 5, angle), size.width / 4);
        
        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(rgba));
        g.draw(ellipse);
        g.setColor(new Color(rgba, true));
        g.fill(ellipse);
        g.dispose();
        
        File file = File.createTempFile("demo", ".png");
        file.deleteOnExit();
        javax.imageio.ImageIO.write(image, "PNG", file);
        return file.getPath();
    }

    private static Point getPoint(Point center, int offset, int angle)
    {
        double theta = Math.toRadians(angle);
        return new Point(center.x + (int)Math.round(offset * Math.cos(theta)),
                         center.y + (int)Math.round(offset * Math.sin(theta)));
    }

    private static Ellipse2D createEllipse(Point center, int r)
    {
        return new Ellipse2D.Float(center.x - r, center.y - r, r * 2, r * 2);
    }
}
