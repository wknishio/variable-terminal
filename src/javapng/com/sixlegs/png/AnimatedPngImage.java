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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * TODO
 */
public class AnimatedPngImage
extends PngImage
{
    /** TODO */
    public static final int acTL = 0x6163544C;
    /** TODO */
    public static final int fcTL = 0x6663544C;
    /** TODO */
    public static final int fdAT = 0x66644154;
        
    private static final PngConfig DEFAULT_CONFIG =
      new PngConfig.Builder().readLimit(PngConfig.READ_EXCEPT_DATA).build();
      
    private final List chunks = new ArrayList();
    private final List frames = new ArrayList();
    private final Map frameData = new HashMap();
    private final List defaultImageData = new ArrayList();

    private Rectangle headerBounds;
    private boolean animated;
    private boolean sawData;
    private boolean useDefaultImage;
    private int numFrames;
    private int numPlays;

    /**
     * TODO
     */
    public AnimatedPngImage()
    {
        super(DEFAULT_CONFIG);
    }

    /**
     * TODO
     */
    public AnimatedPngImage(PngConfig config)
    {
        super(new PngConfig.Builder(config).readLimit(PngConfig.READ_EXCEPT_DATA).build());
    }

    private void reset()
    {
        animated = sawData = useDefaultImage = false;
        chunks.clear();
        frames.clear();
        frameData.clear();
        defaultImageData.clear();
    }

    /**
     * TODO
     */
    public boolean isAnimated()
    {
        assertRead();
        return animated;
    }

    /**
     * TODO
     */
    public int getNumFrames()
    {
        assertRead();
        return frames.size();
    }

    /**
     * TODO
     */
    public int getNumPlays()
    {
        assertRead();
        return animated ? numPlays : 1;
    }

    /**
     * TODO
     */
    public FrameControl getFrame(int index)
    {
        assertRead();
        return (FrameControl)frames.get(index);
    }

    /**
     * TODO
     */
    public boolean isClearRequired()
    {
        assertRead();
        if (!animated)
            return false;
        FrameControl first = getFrame(0);
        return (first.getBlend() == FrameControl.BLEND_OVER) ||
            !first.getBounds().equals(new Rectangle(getWidth(), getHeight()));
    }

    /**
     * TODO
     */
    public BufferedImage[] readAllFrames(File file)
    throws IOException
    {
        read(file);
        BufferedImage[] images = new BufferedImage[getNumFrames()];
        for (int i = 0; i < images.length; i++)
            images[i] = readFrame(file, getFrame(i));
        return images;
    }

    // TODO: make sure that file is what we read before?
    // TODO: make sure that frame control is from this image?
    /**
     * TODO
     */
    public BufferedImage readFrame(File file, FrameControl frame)
    throws IOException
    {
        assertRead();
        if (frame == null)
            return readImage(file, defaultImageData, new Dimension(getWidth(), getHeight()));
        return readImage(file, (List)frameData.get(frame), frame.getBounds().getSize());
    }

    private BufferedImage readImage(File file, List data, Dimension size)
    throws IOException
    {
        FrameDataInputStream in = new FrameDataInputStream(file, data);
        try {
            return createImage(in, size);
        } finally {
            in.close();
        }
    }

    private void assertRead()
    {
        if (frames.isEmpty())
            throw new IllegalStateException("Image has not been read");
    }

    protected void readChunk(int type, DataInput in, long offset, int length)
    throws IOException
    {
        switch (type) {
        case PngConstants.IEND:
            validate();
            super.readChunk(type, in, offset, length);
            break;

        case PngConstants.IHDR:
            reset();
            super.readChunk(type, in, offset, length);
            headerBounds = new Rectangle(getWidth(), getHeight());
            break;

        case acTL:
            RegisteredChunks.checkLength(type, length, 8);
            if (sawData)
                error("acTL cannot appear after IDAT");
            animated = true;
            if ((numFrames = in.readInt()) <= 0)
                error("Invalid frame count: " + numFrames);
            if ((numPlays = in.readInt()) < 0)
                error("Invalid play count: " + numPlays);
            break;

        case fcTL:
            RegisteredChunks.checkLength(type, length, 26);
            add(in.readInt(), readFrameControl(in));
            break;

        case fdAT:
            if (!sawData)
                error("fdAT chunks cannot appear before IDAT");
            add(in.readInt(), new FrameData(offset + 4, length - 4));
            break;

        case PngConstants.IDAT:
            sawData = true;
            defaultImageData.add(new FrameData(offset, length));
            break;

        default:
            super.readChunk(type, in, offset, length);
        }
    }

    protected boolean isMultipleOK(int type)
    {
        switch (type) {
        case fcTL:
        case fdAT:
            return true;
        }
        return super.isMultipleOK(type);
    }

    private void add(int seq, Object chunk)
    throws PngException
    {
        if (chunks.size() != seq ||
            (seq == 0 && !(chunk instanceof FrameControl)))
            error("APNG chunks out of order");
        chunks.add(chunk);
    }

    private static void error(String message)
    throws PngException
    {
        throw new PngException(message, false);
    }

    private FrameControl readFrameControl(DataInput in)
    throws IOException
    {
        int w = in.readInt();
        int h = in.readInt();
        Rectangle bounds = new Rectangle(in.readInt(), in.readInt(), w, h);
        if (!sawData) {
            if (!chunks.isEmpty())
                error("Multiple fcTL chunks are not allowed before IDAT");
            if (!bounds.equals(headerBounds))
                error("Default image frame must match IHDR bounds");
            useDefaultImage = true;
        }
        if (!headerBounds.contains(bounds))
            error("Frame bounds must fall within IHDR bounds");
            
        int delayNum = in.readUnsignedShort();
        int delayDen = in.readUnsignedShort();
        if (delayDen == 0)
            delayDen = 100;

        int disposeOp = in.readByte();
        switch (disposeOp) {
        case FrameControl.DISPOSE_NONE:
        case FrameControl.DISPOSE_BACKGROUND:
            break;
        case FrameControl.DISPOSE_PREVIOUS:
            if (chunks.isEmpty())
                disposeOp = FrameControl.DISPOSE_BACKGROUND;
            break;
        default:
            error("Unknown APNG dispose op " + disposeOp);
        }

        int blendOp = in.readByte();
        switch (blendOp) {
        case FrameControl.BLEND_OVER:
        case FrameControl.BLEND_SOURCE:
            break;
        default:
            error("Unknown APNG blend op " + blendOp);
        }
        return new FrameControl(bounds, (float)delayNum / delayDen, disposeOp, blendOp);
    }

    private void validate()
    throws IOException
    {
        if (!animated) {
            frames.add(null);
            return;
        }
        try {
            if (chunks.isEmpty())
                error("Found zero frames");
            
            List list = null;
            for (int i = 0; i < chunks.size(); i++) {
                Object chunk = chunks.get(i);
                if (chunk instanceof FrameControl) {
                    frames.add(chunk);
                    frameData.put(chunk, list = new ArrayList());
                } else {
                    list.add(chunk);
                }
            }

            if (frames.size() != numFrames)
                error("Found " + frames.size() + " frames, expected " + numFrames);

            if (useDefaultImage)
                ((List)frameData.get(frames.get(0))).addAll(defaultImageData);

            for (int i = 0; i < frames.size(); i++) {
                if (((List)frameData.get(frames.get(i))).isEmpty())
                    error("Missing data for frame");
            }
            chunks.clear();
            
        } catch (IOException e) {
            animated = false;
            throw e;
        }
    }
}
