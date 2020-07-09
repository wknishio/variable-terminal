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

import com.sixlegs.png.*;
import java.awt.image.BufferedImage;
import java.awt.color.ColorSpace;
import javax.imageio.ImageReader;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.io.DataInput;
import java.io.IOException;
import java.util.*;

public class PngImageReader
extends ImageReader
{
    private PngImage png = new IIOPngImage(new PngConfig.Builder().build());
	private PngHeader pngHeader;
	private PngImageMetadata pngMetadata;
	private BufferedImage pngImage;
    private Map unknownChunks = new HashMap();
    
    public PngImageReader(PngImageReaderSpi provider)
    {
        super(provider);
    }

    public void dispose()
    {
        png = null;
		pngHeader = null;
		pngMetadata = null;
		pngImage = null;
        unknownChunks = null;
    }

    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata)
    {
		if (!(input instanceof ImageInputStream))
			throw new IllegalArgumentException("Expected ImageInputStream, got " + input);
        super.setInput(input, seekForwardOnly, ignoreMetadata);
		pngHeader = null;
		pngMetadata = null;
		pngImage = null;
        unknownChunks.clear();
    }

    public int getNumImages(boolean allowSearch)
    {
        return 1;
    }

	private void checkIndex(int imageIndex)
    {
		if (imageIndex != 0)
			throw new IndexOutOfBoundsException("Requested image " + imageIndex);
	}

	private void readHeader() 
	throws IOException
    {
		if (pngHeader == null)
			pngHeader = new PngHeader((ImageInputStream)input);
	}

    public int getWidth(int imageIndex)
	throws IOException
    {
		checkIndex(imageIndex);
		readHeader();
		return pngHeader.width;
    }

    public int getHeight(int imageIndex)
	throws IOException
    {
		checkIndex(imageIndex);
		readHeader();
		return pngHeader.height;
    }

    public Iterator getImageTypes(int imageIndex)
    throws IOException
    {
		checkIndex(imageIndex);
		readHeader();

		int datatype = java.awt.image.DataBuffer.TYPE_BYTE;
		int[] bandOffsets = null;
		ImageTypeSpecifier imageType = null;
		ColorSpace rgb = null;

		// FIXME: These need to be checked
		switch (pngHeader.colorType) {
			case PngConstants.COLOR_TYPE_GRAY:
				imageType = ImageTypeSpecifier.createGrayscale(
						pngHeader.bitDepth,
						datatype,
						false);
				break;
			case PngConstants.COLOR_TYPE_GRAY_ALPHA:
				imageType = ImageTypeSpecifier.createGrayscale(
						pngHeader.bitDepth,
						datatype,
						false,
						true);
				break;
			//FIXME Are the palette entries needed if PngImage deals with them?
			case PngConstants.COLOR_TYPE_PALETTE:
				imageType = ImageTypeSpecifier.createIndexed(
						null,//redLUT,
						null,//greenLUT,
						null,//blueLUT,
						null,//alphaLUT,
						pngHeader.bitDepth,
						datatype);
				break;
			case PngConstants.COLOR_TYPE_RGB:
				rgb = ColorSpace.getInstance(ColorSpace.CS_sRGB);
				bandOffsets = new int[3];
				bandOffsets[0] = 0;
				bandOffsets[1] = 1;
				bandOffsets[2] = 2;
				imageType = ImageTypeSpecifier.createInterleaved(rgb,
								bandOffsets,
								datatype,
								false,
								false);
				break;				
			case PngConstants.COLOR_TYPE_RGB_ALPHA:
				rgb = ColorSpace.getInstance(ColorSpace.CS_sRGB);
				bandOffsets = new int[3];
				bandOffsets[0] = 0;
				bandOffsets[1] = 1;
				bandOffsets[2] = 2;
				imageType = ImageTypeSpecifier.createInterleaved(rgb,
								bandOffsets,
								datatype,
								true,
								false);
				break;
		}

		return Arrays.asList(new ImageTypeSpecifier[]{
			imageType
		}).iterator();
	}

    public IIOMetadata getImageMetadata(int imageIndex)
	throws IOException
    {
		checkIndex(imageIndex);
		readMetadata();
        return pngMetadata;
    }

    public IIOMetadata getStreamMetadata()
    {
		return null;
    }

	public void readMetadata() 
	throws IOException 
	{
		if (pngMetadata == null && !ignoreMetadata) {
			readHeader();
			readImage();
			pngMetadata = new PngImageMetadata(new HashMap(png.getProperties()),
                                               new HashMap(unknownChunks));
		}
	}

	private void readImage()
	throws IOException
	{
		if (pngImage == null) {
			clearAbortRequest();
			processImageStarted(0);
			pngImage = png.read(new StreamWrapper((ImageInputStream)input), false);
			processImageComplete();
		}
	}

    public BufferedImage read(int imageIndex, ImageReadParam param)
    throws IOException
    {
		checkIndex(imageIndex);
 		readMetadata();
		readImage();
        
        /*
        PngConfig config = new PngConfig.Builder().build();
        if (param != null) {
            config.setSourceSubsampling(param.getSourceXSubsampling(),
                                        param.getSourceYSubsampling(),
                                        param.getSubsamplingXOffset(),
                                        param.getSubsamplingYOffset());
        }
        */

		//FIXME use param to get a destination image. Not exactly sure how to
		//go about this.
		//
		//BufferedImage dst = getDestination(param, getImageTypes(0),
		//		pngHeader.width, pngHeader.height);
		//
		//Copy pixels from pngImage to dst???

		return pngImage;
    }

    private class IIOPngImage
    extends PngImage
    {
        public IIOPngImage(PngConfig cfg)
        {
            super(cfg);
        }
        
        protected void handleWarning(PngException e)
        throws PngException
        {
            processWarningOccurred(e.getMessage());
        }

        protected boolean handlePass(BufferedImage image, int pass)
        {
            // TODO: processPassXXX
            if (pass == 6 || !isInterlaced()) {
                processImageComplete();
            } else if (abortRequested()) {
                processReadAborted();
                return false;
            }
            return true;
        }

        protected boolean handleProgress(BufferedImage image, float pct)
        {
            processImageProgress(pct);
            if (abortRequested()) {
                processReadAborted();
                return false;
            }
            return true;
        }

        protected void readChunk(int type, DataInput in, long offset, int length)
        throws IOException
        {
            if (ignoreMetadata) {
                switch (type) {
                case PngConstants.IHDR:
                case PngConstants.PLTE:
                case PngConstants.tRNS:
                case PngConstants.IEND:
                case PngConstants.gAMA:
                    break;
                default:
                    return;
                }
            }
            if (isKnownChunkType(type)) {
                super.readChunk(type, in, offset, length);
            } else {
                byte[] bytes = new byte[length];
                in.readFully(bytes);
                unknownChunks.put(new Integer(type), bytes);
            }
        }
    }

    private static boolean isKnownChunkType(int type)
    {
        switch (type) {
        case PngConstants.IHDR:
        case PngConstants.IEND:
        case PngConstants.IDAT:
        case PngConstants.PLTE:
        case PngConstants.bKGD:
        case PngConstants.tRNS:
        case PngConstants.sBIT:
        case PngConstants.cHRM:
        case PngConstants.gAMA:
        case PngConstants.hIST:
        case PngConstants.iCCP:
        case PngConstants.pHYs:
        case PngConstants.sRGB:
        case PngConstants.tIME:
        case PngConstants.sPLT:
        case PngConstants.gIFg:
        case PngConstants.oFFs:
        case PngConstants.sCAL:
        case PngConstants.sTER:
        case PngConstants.iTXt:
        case PngConstants.tEXt:
        case PngConstants.zTXt:
            return true;
        }
        return false;
    }

	/* Reads a PNG header */
	private static class PngHeader
	{
		public final int width;
		public final int height;
		public final int colorType;
		public final int bitDepth;
		public final int interlaceType;

		public PngHeader(ImageInputStream in)
		throws IOException
		{
			in.mark();

			in.seek(16);
			width 			= in.readInt();
			height 			= in.readInt();
			colorType 		= in.read();

			in.skipBytes(2);
			bitDepth 		= in.read();
			interlaceType 	= in.read();

			in.reset();
		}
	}
}
