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
import java.awt.image.*;
import java.io.*;
import java.util.*;
import static com.sixlegs.png.examples.ArgumentProcessor.*;

// TODO: this is a work in progress
// TODO: support crop
public class Png2AnimatedPng
{
    private static final ArgumentProcessor PROC;

    static
    {
        PROC = new ArgumentProcessor(
            option("iter", Integer.class).defaultValue(0).range(0, Integer.MAX_VALUE),
            option("delay", Integer.class).range(0, (int)Short.MAX_VALUE),
            option("blend", Integer.class).defaultValue(0).range(0, 1),
            option("dispose", Integer.class).defaultValue(0).range(0, 2),
            option("skip"),
            option("crop")
        );
    }
    
    public static void main(String[] args) throws Exception {
        try {
            run(args);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void run(String... orig) throws Exception {
        List<String> args = new ArrayList<String>();
        Map<String,Object> opts = PROC.parse(Arrays.asList(orig), args);
        final int iter = ((Number)opts.get("iter")).intValue();
        final int delay = ((Number)opts.get("delay")).intValue();
        final int blend = ((Number)opts.get("blend")).intValue();
        final int dispose = ((Number)opts.get("dispose")).intValue();
        final boolean skip = ((Boolean)opts.get("skip")).booleanValue();
        
        // TODO: handle numIterations, delay, skip
        final List<File> files = new ArrayList<File>();
        if (args.size() < 2)
            throw new IllegalArgumentException("Not enough arguments");
        for (String arg : args)
            files.add(new File(arg));
        File target = files.remove(files.size() - 1);

        final DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(target)));
        final PngConfig config = new PngConfig.Builder().readLimit(PngConfig.READ_EXCEPT_DATA).build();
        out.writeLong(PngConstants.SIGNATURE);
        (new PngImage(config) {
            private ChunkWriter chunk = new ChunkWriter();
            private int seq = 0;
            protected void readChunk(int type, DataInput in, long offset, int length) throws IOException {
                byte[] data = new byte[length];
                in.readFully(data);

                if (type == PngConstants.IEND) {
                    for (File file : files.subList(1, files.size())) {
                        nextFrame();
                        (new PngImage(config) {
                            protected void readChunk(int type, DataInput in, long offset, int length) throws IOException {
                                if (type == PngConstants.IDAT) {
                                    byte[] data = new byte[length];
                                    in.readFully(data);
                                    chunk.start(AnimatedPngImage.fdAT);
                                    chunk.writeInt(seq++);
                                    chunk.write(data);
                                    chunk.finish(out);
                                } else {
                                    super.readChunk(type, in, offset, length);
                                }
                            }
                        }).read(file);
                    }
                }

                chunk.start(type);
                chunk.write(data);
                chunk.finish(out);
                super.readChunk(type, new DataInputStream(new ByteArrayInputStream(data)), offset, length);

                if (type == PngConstants.IHDR) {
                    chunk.start(AnimatedPngImage.acTL);
                    chunk.writeInt(files.size());
                    chunk.writeInt(iter);
                    chunk.finish(out);
                    if (!skip)
                        nextFrame();
                }
            }

            private void nextFrame() throws IOException {
                chunk.start(AnimatedPngImage.fcTL);
                chunk.writeInt(seq++);
                chunk.writeInt(getWidth());
                chunk.writeInt(getHeight());
                chunk.writeInt(0);
                chunk.writeInt(0);
                chunk.writeShort(delay);
                chunk.writeShort(1000);
                chunk.writeByte((byte)dispose);
                chunk.writeByte((!skip && seq == 1) ? 0 : (byte)blend);
                chunk.finish(out);
            }
        }).read(files.get(0));
        out.close();
    }
}
