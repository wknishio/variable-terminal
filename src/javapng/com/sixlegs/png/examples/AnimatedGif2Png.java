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
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.zip.*;
import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.FileImageInputStream;
import org.w3c.dom.Node;

public class AnimatedGif2Png
{
    private static final int MIN_DELAY = 75; // ms
    
    public static void main(String[] args)
    throws IOException
    {
        if (args.length != 2) {
            System.err.println("Usage: java -jar gif2apng.jar <in.gif> <out.png>");
            return;
        }
        convert(new File(args[0]), new File(args[1]));
    }

    public static void convert(File in, File out)
    throws IOException
    {
        ImageReader imageReader = ImageIO.getImageReadersByFormatName("GIF").next();
        imageReader.setInput(new FileImageInputStream(in));

        int numIterations = 1;
        byte[] netscape = getAppExtension(imageReader.getImageMetadata(0), "NETSCAPE", "2.0");
        if (netscape != null && netscape[0] != 0) {
            int repeat = ((0xFF & netscape[2]) << 8) | (0xFF & netscape[1]);
            if (repeat == 0) {
                numIterations = 0;
            } else {
                numIterations = 1 + repeat;
            }
        }

        int index = 0;
        Set<Integer> entries = new HashSet<Integer>();
        List<Frame> frames = new ArrayList<Frame>();
        int[] prev = null;
        boolean different = false;
        try {
            for (;;) {
                // TODO: get palette from metadata instead of decoding image?
                BufferedImage image = imageReader.read(index);
                ColorModel colorModel = image.getColorModel();
                IndexColorModel icm = (IndexColorModel)colorModel;
                int[] palette = new int[icm.getMapSize()];
                icm.getRGBs(palette);
                for (int i = 0; i < palette.length; i++)
                    entries.add(palette[i]);
                if (!different && prev != null && !Arrays.equals(palette, prev))
                    different = true;
                prev = palette;
                
                IIOMetadata metadata = imageReader.getImageMetadata(index);
                Node root = metadata.getAsTree(metadata.getNativeMetadataFormatName());
                Node desc = getChild(root, "ImageDescriptor");
                Node gce = getChild(root, "GraphicControlExtension");
                int blendOp = (index == 0) ? FrameControl.BLEND_SOURCE : FrameControl.BLEND_OVER;
                frames.add(new Frame(palette,
                                     new Rectangle(Integer.parseInt(getAttr(desc, "imageLeftPosition")),
                                                   Integer.parseInt(getAttr(desc, "imageTopPosition")),
                                                   Integer.parseInt(getAttr(desc, "imageWidth")),
                                                   Integer.parseInt(getAttr(desc, "imageHeight"))),
                                     Math.max(10 * Integer.parseInt(getAttr(gce, "delayTime")), MIN_DELAY),
                                     mapDisposal(getAttr(gce, "disposalMethod")),
                                     blendOp));
                index++;
            }
        } catch (IndexOutOfBoundsException e) {
            // no more frames
        }

        // expand first frame to fit all frames
        Frame first = frames.get(0);
        if (!first.bounds.getLocation().equals(new Point(0, 0)))
            throw new UnsupportedOperationException("TODO: first frame has non-zero origin");
        for (Frame frame : frames)
            first.bounds.add(frame.bounds);

        PngWriter w = new PngWriter(out);
        boolean paletted = entries.size() <= 256;
        int colorType = paletted ? PngConstants.COLOR_TYPE_PALETTE :
            PngConstants.COLOR_TYPE_RGB_ALPHA;
        w.start(first.bounds.getSize(), colorType, first.palette, frames.size(), numIterations);
        if (paletted) {
            if (different)
                throw new UnsupportedOperationException("implement palette remapping");
            writePaletted(w, imageReader, frames);
        } else {
            writeTruecolor(w, imageReader, frames);
        }
        w.finish();
        imageReader.dispose();
    }

    private static byte[] getAppExtension(IIOMetadata metadata, String id, String code)
    {
        Node root = metadata.getAsTree(metadata.getNativeMetadataFormatName());
        Node extensions = getChild(root, "ApplicationExtensions");
        if (extensions == null)
            return null;
        for (Node node = extensions.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (id.equals(getAttr(node, "applicationID")) &&
                code.equals(getAttr(node, "authenticationCode")))
                return (byte[])((IIOMetadataNode)node).getUserObject();
        }
        return null;
    }

    /*
    private static Color getGifBackground(ImageReader imageReader)
    throws IOException
    {
        IIOMetadata metadata = imageReader.getStreamMetadata();
        Node globalColorTable =
            getChild(metadata.getAsTree(metadata.getNativeMetadataFormatName()), "GlobalColorTable");
        if (globalColorTable == null)
            return null;
        String index = getAttr(globalColorTable, "backgroundColorIndex");
        for (Node node = globalColorTable.getFirstChild(); node != null; node = node.getNextSibling()) {
            if (getAttr(node, "index").equals(index)) {
                return new Color(Integer.parseInt(getAttr(node, "red")),
                                 Integer.parseInt(getAttr(node, "green")),
                                 Integer.parseInt(getAttr(node, "blue")));
            }                
        }
        return null;
    }
    */

    private static void writePaletted(PngWriter w, ImageReader imageReader, List<Frame> frames)
    throws IOException
    {
        // TODO: if palette is <= 64 entries, use smaller bit depth
        boolean animated = frames.size() > 1;
        Frame first = frames.get(0);
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        byte[] row = new byte[first.bounds.width];
        int index = 0;
        for (Frame frame : frames) {
            int width = frame.bounds.width;
            BufferedImage image = pad(imageReader.read(index++), frame.bounds);
            raw.reset();
            DeflaterOutputStream defl = new DeflaterOutputStream(raw);
            for (int y = 0, height = frame.bounds.height; y < height; y++) {
                image.getRaster().getDataElements(0, y, width, 1, row);
                defl.write(0); // filter byte
                defl.write(row, 0, width);
            }
            defl.close();
            w.frame(animated, frame, raw.toByteArray());
        }
    }

    private static void writeTruecolor(PngWriter w, ImageReader imageReader, List<Frame> frames)
    throws IOException
    {
        // TODO: could use tRNS instead of alpha channel in some cases for better compression
        boolean animated = frames.size() > 1;
        Frame first = frames.get(0);
        ByteArrayOutputStream raw = new ByteArrayOutputStream();
        byte[] row = new byte[first.bounds.width];
        byte[] rgbs = new byte[4 * row.length];
        byte[] prev = new byte[4 * row.length];
        Filterer filterer = new Filterer(4 * row.length, 4);
        int index = 0;
        for (Frame frame : frames) {
            int[] palette = frame.palette;
            int width = frame.bounds.width;
            BufferedImage image = pad(imageReader.read(index++), frame.bounds);
            raw.reset();
            DataOutputStream defl = new DataOutputStream(new DeflaterOutputStream(raw));
            for (int y = 0, height = frame.bounds.height; y < height; y++) {
                 image.getRaster().getDataElements(0, y, width, 1, row);
                 int toX = 0;
                 for (int x = 0; x < width; x++) {
                     int argb = palette[0xFF & row[x]];
                     rgbs[toX++] = (byte)(0xFF & (argb >>> 16));
                     rgbs[toX++] = (byte)(0xFF & (argb >>> 8));
                     rgbs[toX++] = (byte)(0xFF & argb);
                     rgbs[toX++] = (byte)(0xFF & (argb >>> 24));
                 }
                 int filterType = filterer.filter(rgbs, prev, 4 * width);
                 defl.write(filterType);
                 defl.write(rgbs, 0, 4 * width);
                 byte[] t = rgbs; rgbs = prev; prev = t; // swap
            }
            defl.close();
            w.frame(animated, frame, raw.toByteArray());
        }
    }

    private static BufferedImage pad(BufferedImage image, Rectangle bounds)
    {
        if (image.getWidth() == bounds.width && image.getHeight() == bounds.height)
            return image;
        BufferedImage padded =
            new BufferedImage(image.getColorModel(),
                              image.getRaster().createCompatibleWritableRaster(bounds.width, bounds.height),
                              image.isAlphaPremultiplied(),
                              null);
        Graphics2D g = padded.createGraphics();
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null, null);
        g.dispose();
        return padded;
    }

    private static class Filterer
    {
        private final byte[] work;
        private final byte[] best;
        private final int pixelStride;
        
        public Filterer(int maxLength, int pixelStride)
        {
            work = new byte[maxLength];
            best = new byte[maxLength];
            this.pixelStride = pixelStride;
        }

        public int filter(byte[] row, byte[] prev, int length)
        {
            int bestType = 0;
            int bestSum = Integer.MAX_VALUE;
            for (int type = 0; type <= 2; type++) {
                filter(row, prev, length, type);
                int sum = sumBytes(work, length);
                if (sum < bestSum) {
                    bestSum = sum;
                    bestType = type;
                    System.arraycopy(work, 0, best, 0, length);
                }
            }
            System.arraycopy(best, 0, row, 0, length);
            return bestType;
        }

        private static int sumBytes(byte[] bytes, int length)
        {
            int sum = 0;
            for (int i = 0; i < length; i++)
                sum += 0xFF & bytes[i];
            return sum;
        }

        private void filter(byte[] row, byte[] prev, int length, int type)
        {
            switch (type) {
            case 0: // None
                System.arraycopy(row, 0, work, 0, length);
                break;
            case 1: // Sub
                for (int i = 0; i < pixelStride; i++)
                    work[i] = row[i];
                for (int i = pixelStride, from = 0; i < length; i++, from++)
                    work[i] = (byte)((row[i] - row[from]) % 256);
                break;
            case 2: // Up
                for (int i = 0; i < length; i++)
                    work[i] = (byte)((row[i] - prev[i]) % 256);
                break;
            default:
                throw new UnsupportedOperationException("implement me");
            }
        }
    }    

    private static class PngWriter
    {
        private final DataOutputStream data;
        private final ChunkWriter chunk = new ChunkWriter();
        private int seq;
        
        public PngWriter(File out)
        throws IOException
        {
            this.data = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(out)));
        }

        public void start(Dimension size, int colorType, int[] palette, int numFrames, int numIterations)
        throws IOException
        {
            data.writeLong(PngConstants.SIGNATURE);
            chunk.start(PngConstants.IHDR);
            chunk.writeInt(size.width);
            chunk.writeInt(size.height);
            chunk.writeByte(8); // bit depth
            chunk.writeByte(colorType);
            chunk.writeByte(PngConstants.COMPRESSION_BASE);
            chunk.writeByte(PngConstants.FILTER_BASE);
            chunk.writeByte(PngConstants.INTERLACE_NONE);
            chunk.finish(data);

            if (colorType == PngConstants.COLOR_TYPE_PALETTE) {
                chunk.start(PngConstants.PLTE);
                int numTrans = 0;
                int maxTrans = 0;
                for (int i = 0; i < palette.length; i++) {
                    Color color = new Color(palette[i], true);
                    chunk.writeByte(color.getRed());
                    chunk.writeByte(color.getGreen());
                    chunk.writeByte(color.getBlue());
                    if (color.getAlpha() != 0xFF) {
                        numTrans++;
                        maxTrans = i;
                    }
                }
                chunk.finish(data);

                if (numTrans > 0) {
                    chunk.start(PngConstants.tRNS);
                    for (int i = 0; i <= maxTrans; i++)
                        chunk.writeByte(palette[i] >>> 24);
                    chunk.finish(data);
                }
            }

            if (numFrames > 1) {
                chunk.start(AnimatedPngImage.acTL);
                chunk.writeInt(numFrames);
                chunk.writeInt(numIterations);
                chunk.finish(data);
            }
        }

        public void frame(boolean animated, Frame frame, byte[] bytes)
        throws IOException
        {
            if (animated) {
                chunk.start(AnimatedPngImage.fcTL);
                chunk.writeInt(seq++);
                chunk.writeInt(frame.bounds.width);
                chunk.writeInt(frame.bounds.height);
                chunk.writeInt(frame.bounds.x);
                chunk.writeInt(frame.bounds.y);
                chunk.writeShort(frame.delayTime);
                chunk.writeShort(1000);
                chunk.writeByte(frame.dispose);
                chunk.writeByte(frame.blend);
                chunk.finish(data);
                if (seq == 1) {
                    chunk.start(PngConstants.IDAT);
                } else {
                    chunk.start(AnimatedPngImage.fdAT);
                    chunk.writeInt(seq++);
                }
                chunk.write(bytes);
                chunk.finish(data);
            } else {
                chunk.start(PngConstants.IDAT);
                chunk.write(bytes);
                chunk.finish(data);
            }
        }

        public void finish()
        throws IOException
        {
            chunk.start(PngConstants.IEND);
            chunk.finish(data);
            data.close();
        }
    }

    private static Node getChild(Node node, String name)
    {
        for (node = node.getFirstChild(); node != null; node = node.getNextSibling())
            if (name.equals(node.getNodeName()))
                break;
        return node;
    }
    
    private static String getAttr(Node element, String name)
    {
        return element.getAttributes().getNamedItem(name).getNodeValue();
    }

    private static int mapDisposal(String gifDisposalMethod)
    {
        if (gifDisposalMethod.equals("restoreToBackgroundColor"))
            return FrameControl.DISPOSE_BACKGROUND;
        if (gifDisposalMethod.equals("restoreToPrevious"))
            return FrameControl.DISPOSE_PREVIOUS;
        return FrameControl.DISPOSE_NONE;
    }

    private static class Frame
    {
        final int[] palette;
        final Rectangle bounds;
        final int delayTime;
        final int dispose;
        final int blend;

        public Frame(int[] palette, Rectangle bounds, int delayTime, int dispose, int blend)
        {
            this.palette = palette;
            this.bounds = bounds;
            this.delayTime = delayTime;
            this.dispose = dispose;
            this.blend = blend;
        }
    }
}
