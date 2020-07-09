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

package com.sixlegs.png.iio;

import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public class PngImageReaderSpi
extends ImageReaderSpi
{
    private static final long SIGNATURE = 0x89504E470D0A1A0AL;

    public PngImageReaderSpi()
    {
        super("Chris Nokleberg",
              "2.0",
              new String[]{ "png", "PNG" },
              new String[]{ "png", "PNG" },
              new String[]{ "image/png", "image/x-png"},
              "com.sixlegs.png.iio.PngImageReader",
              new Class[]{ ImageInputStream.class, },
              null, // writer
              false, null, null, null, null,
              false, null, null, null, null);
    }

    public String getDescription(Locale locale)
    {
        return "com.sixlegs.png ImageIO plugin";
    }

    public boolean canDecodeInput(Object source)
    throws IOException
    {
        if (source instanceof ImageInputStream) {
            ImageInputStream in = (ImageInputStream)source;
            in.mark();
            long sig = in.readLong();
            in.reset();
            return sig == SIGNATURE;
        }
        return false;
    }

    public ImageReader createReaderInstance(Object extension)
    {
        return new PngImageReader(this);
    }
}
