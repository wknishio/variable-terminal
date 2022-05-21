/*
com.sixlegs.png - Java package to read and display PNG images
Copyright (C) 2006 Dimitri Koussa

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

import java.awt.*;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import java.util.*;

public class PngImageMetadataFormat 
extends IIOMetadataFormatImpl 
{
	private static final String rootName = "javax_imageio_png_1.0";

    private static final Set validNodes = Collections.unmodifiableSet(new HashSet(Arrays.asList(new String[]{
        "IHDR", "iCCP", "cHRM", "sRGB", "gAMA", "pHYS", "tIME",
        "sPLT", "sPLTEntry",
        "hIST", "hISTEntry",
        "sBIT", "sBIT_Grayscale", "sBIT_GrayAlpha", "sBIT_RGB", "sBIT_RGBAlpha", "sBIT_Palette",
        "PLTE", "PLTEEntry",
        "tRNS", "tRNS_Grayscale", "tRNS_RGB", "tRNS_Palette",
        "bKGD", "bKGD_Grayscale", "bKGD_Palette", "bKGD_Palette", "bKGD_RGB",
        "iTXt", "iTXtEntry", "zTXt", "zTXtEntry", "tEXt", "tEXtEntry",
        "UnknownChunks", "UnknownChunk",
    })));

	// singleton
	private static final PngImageMetadataFormat defaultInstance =
		new PngImageMetadataFormat();

	// Make constructor private to enforce the singleton pattern
	private PngImageMetadataFormat() 
	{
		// Set the name of the root node
		super(rootName, CHILD_POLICY_REPEAT);

		setup_IHDR();
		setup_PLTE();
		setup_bKGD();
		setup_cHRM();
		setup_gAMA();
		setup_hIST();
		setup_iCCP();
		setup_iTXt();
		setup_pHYS();
		setup_sBIT();
		setup_sPLT();
		setup_sRGB();
		setup_tEXt();
		setup_tIME();
		setup_tRNS();
		setup_zTXt();
		setup_unknownChunks();
	}

	private void setup_IHDR()
	{
		addElement("IHDR", rootName, CHILD_POLICY_EMPTY);

		addAttribute("IHDR", "width", DATATYPE_INTEGER, true, null);
		addAttribute("IHDR", "height", DATATYPE_INTEGER, true, null);
		addAttribute("IHDR", "bitDepth", DATATYPE_INTEGER, true, null);
		addAttribute("IHDR", "colorType", DATATYPE_STRING, true, null);
		addAttribute("IHDR", "compressionMethod", DATATYPE_STRING, true, null);
		addAttribute("IHDR", "filterMethod", DATATYPE_STRING, true, null);
		addAttribute("IHDR", "interlaceMethod", DATATYPE_STRING, true, null);
	}

	private void setup_sPLT()
	{
		addElement("sPLT", rootName, CHILD_POLICY_REPEAT);

		addElement("sPLTEntry", "sPLT", CHILD_POLICY_EMPTY);
		addAttribute("sPLTEntry", "index", DATATYPE_INTEGER, true, null);
		addAttribute("sPLTEntry", "red", DATATYPE_INTEGER, true, null);
		addAttribute("sPLTEntry", "green", DATATYPE_INTEGER, true, null);
		addAttribute("sPLTEntry", "blue", DATATYPE_INTEGER, true, null);
		addAttribute("sPLTEntry", "alpha", DATATYPE_INTEGER, true, null);
	}

	private void setup_hIST()
	{
		addElement("hIST", rootName, CHILD_POLICY_REPEAT);

		addElement("hISTEntry", "hIST", CHILD_POLICY_EMPTY);
		addAttribute("hISTEntry", "index", DATATYPE_INTEGER, true, null);
		addAttribute("hISTEntry", "value", DATATYPE_INTEGER, true, null);
	}

	private void setup_iCCP()
	{
		addElement("iCCP", rootName, CHILD_POLICY_EMPTY);
		addAttribute("iCCP", "profileName", DATATYPE_STRING, true, null);
		addAttribute("iCCP", "compressionMethod", DATATYPE_STRING, true, null);
	}

	private void setup_cHRM()
	{
		addElement("cHRM", rootName, CHILD_POLICY_EMPTY);

		addAttribute("cHRM", "whitePointX", DATATYPE_INTEGER, true, null);
		addAttribute("cHRM", "whitePointY", DATATYPE_INTEGER, true, null);
		addAttribute("cHRM", "redX", DATATYPE_INTEGER, true, null);
		addAttribute("cHRM", "redY", DATATYPE_INTEGER, true, null);
		addAttribute("cHRM", "greenX", DATATYPE_INTEGER, true, null);
		addAttribute("cHRM", "greenY", DATATYPE_INTEGER, true, null);
		addAttribute("cHRM", "blueX", DATATYPE_INTEGER, true, null);
		addAttribute("cHRM", "blueY", DATATYPE_INTEGER, true, null);
	}

	private void setup_sBIT()
	{
		addElement("sBIT", rootName, CHILD_POLICY_CHOICE);

		addElement("sBIT_Grayscale", "sBIT", CHILD_POLICY_EMPTY);
		addAttribute("sBIT_Grayscale", "gray", DATATYPE_INTEGER, true, null);

		addElement("sBIT_GrayAlpha", "sBIT", CHILD_POLICY_EMPTY);
		addAttribute("sBIT_GrayAlpha", "gray", DATATYPE_INTEGER, true, null);
		addAttribute("sBIT_GrayAlpha", "alpha", DATATYPE_INTEGER, true, null);

		addElement("sBIT_RGB", "sBIT", CHILD_POLICY_EMPTY);
		addAttribute("sBIT_RGB", "red", DATATYPE_INTEGER, true, null);
		addAttribute("sBIT_RGB", "green", DATATYPE_INTEGER, true, null);
		addAttribute("sBIT_RGB", "blue", DATATYPE_INTEGER, true, null);

		addElement("sBIT_RGBAlpha", "sBIT", CHILD_POLICY_EMPTY);
		addAttribute("sBIT_RGBAlpha", "red", DATATYPE_INTEGER, true, null);
		addAttribute("sBIT_RGBAlpha", "green", DATATYPE_INTEGER, true, null);
		addAttribute("sBIT_RGBAlpha", "blue", DATATYPE_INTEGER, true, null);
		addAttribute("sBIT_RGBAlpha", "alpha", DATATYPE_INTEGER, true, null);

		addElement("sBIT_Palette", "sBIT", CHILD_POLICY_EMPTY);
		addAttribute("sBIT_Palette", "red", DATATYPE_INTEGER, true, null);
		addAttribute("sBIT_Palette", "green", DATATYPE_INTEGER, true, null);
		addAttribute("sBIT_Palette", "blue", DATATYPE_INTEGER, true, null);
	}

	private void setup_PLTE()
	{
		addElement("PLTE", rootName, CHILD_POLICY_REPEAT);
		addElement("PLTEEntry", "PLTE", CHILD_POLICY_EMPTY);

		addAttribute("PLTEEntry", "index", DATATYPE_INTEGER, true, null);
		addAttribute("PLTEEntry", "red", DATATYPE_INTEGER, true, null);
		addAttribute("PLTEEntry", "green", DATATYPE_INTEGER, true, null);
		addAttribute("PLTEEntry", "blue", DATATYPE_INTEGER, true, null);
	}

	private void setup_tRNS()
	{
		addElement("tRNS", rootName, CHILD_POLICY_CHOICE);
		
		addElement("tRNS_Grayscale", rootName, CHILD_POLICY_CHOICE);
		addAttribute("tRNS_Grayscale", "gray", DATATYPE_INTEGER, true, null);

		addElement("tRNS_RGB", rootName, CHILD_POLICY_CHOICE);
		addAttribute("tRNS_RGB", "red", DATATYPE_INTEGER, true, null);
		addAttribute("tRNS_RGB", "green", DATATYPE_INTEGER, true, null);
		addAttribute("tRNS_RGB", "blue", DATATYPE_INTEGER, true, null);

		addElement("tRNS_Palette", rootName, CHILD_POLICY_CHOICE);
		addAttribute("tRNS_Palette", "index", DATATYPE_INTEGER, true, null);
		addAttribute("tRNS_Palette", "alpha", DATATYPE_INTEGER, true, null);
	}

	private void setup_bKGD()
	{
		addElement("bKGD", rootName, CHILD_POLICY_CHOICE);

		addElement("bKGD_Grayscale", rootName, CHILD_POLICY_EMPTY);
		addAttribute("bKGD_Grayscale", "gray", DATATYPE_INTEGER, true, null);

		addElement("bKGD_Palette", rootName, CHILD_POLICY_EMPTY);
		addAttribute("bKGD_Palette", "index", DATATYPE_INTEGER, true, null);

		addElement("bKGD_RGB", rootName, CHILD_POLICY_EMPTY);
		addAttribute("bKGD_RGB", "red", DATATYPE_INTEGER, true, null);
		addAttribute("bKGD_RGB", "green", DATATYPE_INTEGER, true, null);
		addAttribute("bKGD_RGB", "blue", DATATYPE_INTEGER, true, null);
	}

	private void setup_sRGB()
	{
		addElement("sRGB", rootName, CHILD_POLICY_EMPTY);
		addAttribute("sRGB", "renderingIntent", DATATYPE_STRING, true, null);
	}

	private void setup_gAMA()
	{
		addElement("gAMA", rootName, CHILD_POLICY_EMPTY);
		addAttribute("gAMA", "value", DATATYPE_INTEGER, true, null);
	}

	private void setup_iTXt()
	{
		addElement("iTXt", rootName, CHILD_POLICY_REPEAT);

		addElement("iTXtEntry", rootName, CHILD_POLICY_EMPTY);
		addAttribute("iTXtEntry", "keyword", DATATYPE_STRING, true, null);
		addAttribute("iTXtEntry", "compressionFlag", DATATYPE_STRING, true, null);
		addAttribute("iTXtEntry", "compressionMethod", DATATYPE_STRING, true, null);
		addAttribute("iTXtEntry", "languageTag", DATATYPE_STRING, true, null);
		addAttribute("iTXtEntry", "translatedKeyword", DATATYPE_STRING, true, null);
		addAttribute("iTXtEntry", "text", DATATYPE_STRING, true, null);
	}

	private void setup_zTXt()
	{
		addElement("zTXt", rootName, CHILD_POLICY_REPEAT);

		addElement("zTXtEntry", "zTXt", CHILD_POLICY_EMPTY);
		addAttribute("zTXtEntry", "keyword", DATATYPE_STRING, true, null);
		addAttribute("zTXtEntry", "compressionMethod", DATATYPE_STRING, true, null);
	}

	private void setup_tEXt()
	{
		addElement("tEXt", rootName, CHILD_POLICY_REPEAT);
		addElement("tEXtEntry", rootName, CHILD_POLICY_EMPTY);
		addAttribute("tEXtEntry", "keyword", DATATYPE_STRING, true, null);
		addAttribute("tEXtEntry", "value", DATATYPE_STRING, true, null);
	}

	private void setup_pHYS()
	{
		addElement("pHYS", rootName, CHILD_POLICY_EMPTY);
		addAttribute("pHYS", "pixelsPerUnitXAxis", DATATYPE_INTEGER, true, null);
		addAttribute("pHYS", "pixelsPerUnitYAxis", DATATYPE_INTEGER, true, null);
		addAttribute("pHYS", "unitSpecifier", DATATYPE_STRING, true, null);
	}

	private void setup_tIME()
	{
		addElement("tIME", rootName, CHILD_POLICY_EMPTY);
		addAttribute("tIME", "year",   DATATYPE_INTEGER, true, null);
		addAttribute("tIME", "month",  DATATYPE_INTEGER, true, null);
		addAttribute("tIME", "day",    DATATYPE_INTEGER, true, null);
		addAttribute("tIME", "hour",   DATATYPE_INTEGER, true, null);
		addAttribute("tIME", "minute", DATATYPE_INTEGER, true, null);
		addAttribute("tIME", "second", DATATYPE_INTEGER, true, null);
	}

	private void setup_unknownChunks()
	{
		addElement("UnknownChunks", rootName, CHILD_POLICY_REPEAT);
		addElement("UnknownChunk", "UnknownChunks", CHILD_POLICY_EMPTY);
		addAttribute("UnknownChunk", "type", DATATYPE_STRING, true, null);
	}

	public boolean canNodeAppear(String e, ImageTypeSpecifier imageType) 
	{
        // TODO: A PLTE chunk may not appear in a Gray or GrayAlpha image
        // TODO: A tRNS chunk may not appear in GrayAlpha and RGBA images
        return validNodes.contains(e);
	}

	// Return the singleton instance
	public static PngImageMetadataFormat getDefaultInstance() 
	{
		return defaultInstance;
	}
}
