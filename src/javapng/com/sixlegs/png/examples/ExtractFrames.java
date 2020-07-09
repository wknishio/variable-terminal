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

import com.sixlegs.png.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

public class ExtractFrames
{
    public static void main(String[] args)
    throws Exception
    {
        DecimalFormat fmt = new DecimalFormat("000");
        for (int i = 0; i < args.length; i++) {
            File in = new File(args[i]);
            PngSplitter splitter = new PngSplitter(in);
            int numFrames = splitter.getNumFrames();
            for (int frame = 0; frame < numFrames; frame++) {
                File out = new File(fmt.format(frame) + "-" + in.getName());
                if (out.exists()) {
                    System.err.println("File exists, skipping: " + out);
                    break;
                }
                System.err.println("Writing " + out);
                splitter.write(out, frame);
            }
        }
    }

    private static class PngSplitter
    extends PngImage
    {
        private static final PngConfig CONFIG = new PngConfig.Builder()
            .warningsFatal(true)
            .readLimit(PngConfig.READ_EXCEPT_DATA)
            .build();

        private File in;
        private List<Chunk> commonBefore = new ArrayList<Chunk>();
        private List<Chunk> commonAfter = new ArrayList<Chunk>();
        private List<Chunk> data = new ArrayList<Chunk>();
        private List<Chunk> bySequence = new ArrayList<Chunk>();
        private List<List<Chunk>> byFrame = new ArrayList<List<Chunk>>();
        private byte[] buf = new byte[0x2000];

        public PngSplitter(File in)
        throws IOException
        {
            super(CONFIG);
            this.in = in;
            read(in);
            byFrame.add(data);
            List<Chunk> cur = null;
            for (Chunk chunk : bySequence) {
                if (chunk.type == AnimatedPngImage.fcTL) {
                    byFrame.add(cur = new ArrayList<Chunk>());
                } else {
                    cur.add(chunk);
                }
            }
        }

        public int getNumFrames()
        {
            return byFrame.size();
        }

        public void write(File file, int index)
        throws IOException
        {
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            ChunkWriter cw = new ChunkWriter();
            try {
                out.writeLong(PngConstants.SIGNATURE);
                echo(out, commonBefore);
                echo(out, byFrame.get(index));
                echo(out, commonAfter);
            } finally {
                out.close();
            }
        }

        private void echo(DataOutput out, List<Chunk> chunks)
        throws IOException
        {
            ChunkWriter cw = new ChunkWriter();
            RandomAccessFile rf = new RandomAccessFile(in, "r");
            try {
                for (Chunk chunk : chunks) {
                    cw.start(chunk.type);
                    rf.seek(chunk.offset);
                    rf.readFully(buf, 0, chunk.length);
                    cw.write(buf, 0, chunk.length);
                    cw.close();
                    cw.finish(out);
                }
            } finally {
                rf.close();
            }
        }

        protected void readChunk(int type, DataInput in, long offset, int length)
        throws IOException
        {
            Chunk chunk = new Chunk();
            chunk.type = type;
            chunk.offset = offset;
            chunk.length = length;
            switch (type) {
            case PngConstants.IDAT:
                data.add(chunk);
                break;
            case AnimatedPngImage.acTL:
                break;
            case AnimatedPngImage.fdAT:
                chunk.type = PngConstants.IDAT;
                chunk.offset += 4;
                chunk.length -= 4;
                /* fall-through */
            case AnimatedPngImage.fcTL:
                int seq = in.readInt();
                while (bySequence.size() <= seq)
                    bySequence.add(null);
                bySequence.set(seq, chunk);
                break;
            default:
                (data.isEmpty() ? commonBefore : commonAfter).add(chunk);
                super.readChunk(type, in, offset, length);
            }
        }
    }

    private static class Chunk
    {
        int type;
        long offset;
        int length;

        public String toString()
        {
            return PngConstants.getChunkName(type);
        }
    }
}
