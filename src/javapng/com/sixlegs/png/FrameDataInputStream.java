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

package com.sixlegs.png;

import java.io.*;
import java.util.*;

class FrameDataInputStream
extends InputStream
{
    private final RandomAccessFile file;
    private final Iterator it;
    private InputStream in;

    public FrameDataInputStream(File file, List frameData)
    throws IOException
    {
        this.file = new RandomAccessFile(file, "r");
        this.it = frameData.iterator();
        advance();
    }

    public void close()
    throws IOException
    {
        if (in != null) {
            in.close();
            in = null;
            while (it.hasNext())
                it.next();
        }
        file.close();
    }

    private void advance()
    throws IOException
    {
        if (in != null)
            in.close();
        in = null;
        if (it.hasNext()) {
            // TODO: enable streaming
            FrameData data = (FrameData)it.next();
            file.seek(data.getOffset());
            byte[] bytes = new byte[data.getLength()];
            file.readFully(bytes);
            in = new ByteArrayInputStream(bytes);
        }
    }
        
    public int available()
    throws IOException
    {
        if (in == null)
            return 0;
        return in.available();
    }

    public boolean markSupported()
    {
        return false;
    }

    public int read()
    throws IOException
    {
        if (in == null)
            return -1;
        int result = in.read();
        if (result == -1) {
            advance();
            return read();
        }
        return result;
    }
    
    public int read(byte[] b, int off, int len)
    throws IOException
    {
        if (in == null)
            return -1;
        int result = in.read(b, off, len);
        if (result == -1) {
            advance();
            return read(b, off, len);
        }
        return result;
    }

    public long skip(long n)
    throws IOException
    {
        if (in == null)
            return 0;
        long result = in.skip(n);
        if (result != 0)
            return result;
        if (read() == -1)
            return 0;
        return 1;
    }
}
