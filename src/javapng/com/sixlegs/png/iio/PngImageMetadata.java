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

import com.sixlegs.png.*;
import java.util.*;
import javax.imageio.metadata.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PngImageMetadata 
extends IIOMetadata 
{
    // Format defined by sun - with bug
    static final String nativeMetadataFormatName = 
      "javax_imageio_png_1.0";

    private Map props;
    private Map unknownChunks;
    private Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

    PngImageMetadata(Map props, Map unknownChunks)
    {
        super(true, nativeMetadataFormatName,
              "com.sixlegs.png.iio.PngImageMetadata", 
              null, null);
        this.props = props;
        this.unknownChunks = unknownChunks;
    }

    public IIOMetadataFormat getMetadataFormat(String formatName) 
    {
        if (formatName.equals(nativeMetadataFormatName))
            return PngImageMetadataFormat.getDefaultInstance();
        if (formatName.equals(IIOMetadataFormatImpl.standardMetadataFormatName))
            return IIOMetadataFormatImpl.getStandardFormatInstance();
        throw new IllegalArgumentException(formatName);
    }

    public Node getAsTree(String formatName) 
    {
        if (formatName.equals(nativeMetadataFormatName))
            return getNativeTree();
        if (formatName.equals(IIOMetadataFormatImpl.standardMetadataFormatName))
            return getStandardTree();
        throw new IllegalArgumentException(formatName);
    }

    public boolean isReadOnly() 
    {
        //TODO
        return true;
    }
    
    public void reset() 
    {
        //TODO
    }

    public void mergeTree(String formatName, Node root)
    {
        //TODO
    }

    private static void appendSimpleNode(IIOMetadataNode parent, String name, String attName, String attValue)
    {
        IIOMetadataNode node = new IIOMetadataNode(name);
        node.setAttribute(attName, attValue);
        parent.appendChild(node);
    }

    // ----------- Methods needed to support the Standard metadata format
    //
    // The DTD is available from
    // http://java.sun.com/j2se/1.4.2/docs/api/javax/imageio/metadata/doc-files/standard_metadata.html
    //

    // Used to separate elements such as in a List of Integers
    private static final String list_separator = " ";

    protected IIOMetadataNode getStandardChromaNode()
    {
        IIOMetadataNode parent = new IIOMetadataNode("Chroma");
        
        int colorType = getInt(PngConstants.COLOR_TYPE);
        appendSimpleNode(parent, "ColorSpaceType", "name",
                         ((colorType & 2) != 0) ? "RGB" : "GRAY");
        appendSimpleNode(parent, "NumChannels", "value", String.valueOf(getNumChannels()));

        Float gamma = (Float)get(PngConstants.GAMMA);
        if (gamma != null)
            appendSimpleNode(parent, "Gamma", "value", gamma.toString());
        appendSimpleNode(parent, "BlackIsZero", "value", "true");

        IIOMetadataNode node = getPalette("Palette", "PaletteEntry");
        if (node != null) {
            byte[] alpha = (byte[])get(PngConstants.PALETTE_ALPHA);
            if (alpha != null) {
                NodeList children = node.getChildNodes();
                for (int i = 0, len = children.getLength(); i < len; i++) {
                    int alphaValue = (i < alpha.length) ? (0xFF & alpha[i]) : 255;
                    ((IIOMetadataNode)children.item(i)).setAttribute("alpha", String.valueOf(alphaValue));
                }
            }
            parent.appendChild(node);
        }

        int[] bg = (int[])get(PngConstants.BACKGROUND);
        if (bg != null) {
            if (colorType == PngConstants.COLOR_TYPE_PALETTE) {
                appendSimpleNode(parent, "BackgroundIndex", "value", String.valueOf(bg[0]));
            } else {
                node = new IIOMetadataNode("BackgroundColor");
                int r, g, b;
                if (bg.length == 3) {
                    r = bg[0];
                    g = bg[1];
                    b = bg[2];
                } else {
                    r = g = b = bg[0];
                }
                node.setAttribute("red",   String.valueOf(r));
                node.setAttribute("green", String.valueOf(g));
                node.setAttribute("blue",  String.valueOf(b));
                parent.appendChild(node);
            }
        }
        return parent;
    }

    private int getNumChannels()
    {
        switch (getInt(PngConstants.COLOR_TYPE)) {
        case PngConstants.COLOR_TYPE_GRAY:
            return 1;
        case PngConstants.COLOR_TYPE_GRAY_ALPHA:
            return 2;
        case PngConstants.COLOR_TYPE_PALETTE:
            return (get(PngConstants.PALETTE_ALPHA) != null) ? 4 : 3;
        case PngConstants.COLOR_TYPE_RGB:
            return 3;
        case PngConstants.COLOR_TYPE_RGB_ALPHA:
            return 4;
        }
        return 0;
    }

    protected IIOMetadataNode getStandardCompressionNode()
    {
        IIOMetadataNode parent = new IIOMetadataNode("Compression");
        appendSimpleNode(parent, "CompressionTypeName", "value", "deflate");
        appendSimpleNode(parent, "Lossless", "value", "true");
        appendSimpleNode(parent, "NumProgressiveScans", "value", isInterlaced() ? "7" : "1");
        return parent;
    }

    protected IIOMetadataNode getStandardDataNode()
    {
        int colorType = getInt(PngConstants.COLOR_TYPE);
        IIOMetadataNode parent = new IIOMetadataNode("Data");
        appendSimpleNode(parent, "PlanarConfiguration", "value", "PixelInterleaved");
        appendSimpleNode(parent, "SampleFormat", "value",
                      (colorType == PngConstants.COLOR_TYPE_PALETTE) ? "Index" : "UnsignedIntegral");
        appendSimpleNode(parent, "BitsPerSample", "value", getBitsPerSample());

        byte[] sbit = (byte[])get(PngConstants.SIGNIFICANT_BITS);
        if (sbit != null) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < sbit.length; i++) {
                if (i > 0)
                    sb.append(' ');
                sb.append(sbit[i]);
            }
            appendSimpleNode(parent, "SignificantBitsPerSample", "value", sb.toString());
        }
        return parent;
    }

    private String getBitsPerSample()
    {
        String d = String.valueOf(getInt(PngConstants.BIT_DEPTH));
        switch (getInt(PngConstants.COLOR_TYPE)) {
        case PngConstants.COLOR_TYPE_GRAY:
            return d;
        case PngConstants.COLOR_TYPE_GRAY_ALPHA:
            return d + " " + d;
        case PngConstants.COLOR_TYPE_RGB:
            return d + " " + d + " " + d;
        case PngConstants.COLOR_TYPE_RGB_ALPHA:
            return d + " " + d + " " + d + " " + d;
        case PngConstants.COLOR_TYPE_PALETTE:
            int size = getPaletteSize(((byte[])get(PngConstants.PALETTE)).length / 3);
            d = String.valueOf((int)Math.round(Math.log(size) / Math.log(2)));
            if (get(PngConstants.PALETTE_ALPHA) != null)
                return d + " " + d + " " + d + " " + d;
            return d + " " + d + " " + d;
        }
        return null;
    }

    protected IIOMetadataNode getStandardDimensionNode()
    {
        IIOMetadataNode parent = new IIOMetadataNode("Dimension");
        float aspect = 1.0f;
        Integer unit = (Integer)get(PngConstants.UNIT);
        if (unit != null) {
            aspect = (float)getInt(PngConstants.PIXELS_PER_UNIT_X) /
                getInt(PngConstants.PIXELS_PER_UNIT_Y);
        }
        appendSimpleNode(parent, "PixelAspectRatio", "value", String.valueOf(aspect));
        appendSimpleNode(parent, "ImageOrientation", "value", "Normal");
//         appendSimpleNode(parent, "HorizontalScreenSize", "value",
//                          String.valueOf(getInt(PngConstants.WIDTH)));
//         appendSimpleNode(parent, "VerticalScreenSize", "value",
//                          String.valueOf(getInt(PngConstants.HEIGHT)));
        if (unit != null && unit.intValue() == PngConstants.UNIT_METER) {
            appendSimpleNode(parent, "HorizontalPixelSize", "value",
                             String.valueOf(1000f / getInt(PngConstants.PIXELS_PER_UNIT_X)));
            appendSimpleNode(parent, "VerticalPixelSize", "value",
                             String.valueOf(1000f / getInt(PngConstants.PIXELS_PER_UNIT_Y)));
        }
        return parent;
    }

    protected IIOMetadataNode getStandardDocumentNode()
    {
        IIOMetadataNode time = getTime("ImageModificationTime");
        if (time != null) {
            IIOMetadataNode parent = new IIOMetadataNode("Document");
            parent.appendChild(time);
            return parent;
        }
        return null;
    }

    protected IIOMetadataNode getStandardTextNode()
    {
        List textChunks = (List)get(PngConstants.TEXT_CHUNKS);
        if (textChunks == null)
            return null;
        
        IIOMetadataNode node = new IIOMetadataNode("Text");
        for (Iterator it = textChunks.iterator(); it.hasNext();) {
            TextChunk chunk = (TextChunk)it.next();
            IIOMetadataNode child = new IIOMetadataNode("TextEntry");
            child.setAttribute("keyword", chunk.getKeyword());
            child.setAttribute("value", chunk.getText());

            if (chunk.getType() == PngConstants.tEXt)
                child.setAttribute("encoding", "ISO-8859-1");
                
            //FIXME what about compressed iTXt?
            if (chunk.getType() == PngConstants.zTXt) {
                child.setAttribute("compression", "deflate");
            } else {
                child.setAttribute("compression", "none");
            }
            node.appendChild(child);
        }
        return node;
    }

    protected IIOMetadataNode getStandardTransparencyNode()
    {
        int colorType = getInt(PngConstants.COLOR_TYPE);
        IIOMetadataNode parent = new IIOMetadataNode("Transparency");

        boolean hasAlpha = colorType == PngConstants.COLOR_TYPE_RGB_ALPHA ||
            colorType == PngConstants.COLOR_TYPE_GRAY_ALPHA ||
            get(PngConstants.PALETTE_ALPHA) != null;
        // TODO: sun has spelling error: nonpremultipled instead of premultiplied
        appendSimpleNode(parent, "Alpha", "value", hasAlpha ? "nonpremultipled" : "none");

        Object transObj = get(PngConstants.TRANSPARENCY);
        if (transObj instanceof int[]) {            
            int[] trans = (int[])transObj;
            String value;
            if (trans.length == 3) {
                value = trans[0] + " " + trans[1] + " " + trans[2];
            } else {
                value = String.valueOf(trans[0]);
            }
            appendSimpleNode(parent, "TransparencyColor", "value", value);
        } else if (get(PngConstants.PALETTE_ALPHA) != null) {
            // hack
            parent.appendChild(new IIOMetadataNode("TransparencyColor"));
        }
        return parent;
    }

    // ----------- Methods needed to support the Native metadata format
    //
    // This format is very similar to the javax_imageio_png_1.0
    //
    // It differs in that it is not susceptible to the bug described here
    // http://developer.java.sun.com/developer/bugParade/bugs/4518989.html
    //

    private IIOMetadataNode getNativeTree()
    {
        IIOMetadataNode root = new IIOMetadataNode(nativeMetadataFormatName);
        add_IHDR(root);
        add_PLTE(root);
        add_bKGD(root);
        add_cHRM(root);
        add_gAMA(root);
        add_hIST(root);
        add_iCCP(root);
        add_pHYS(root);
        add_sBIT(root);
        add_sRGB(root);
        add_tIME(root);
        add_tRNS(root);
        add_text_chunks(root);
        add_unknown_chunks(root);
        return root;
    }

    private int getInt(String name)
    {
        return ((Number)get(name)).intValue();
    }
    
    private Object get(String name)
    {
        return props.get(name);
    }

    private void add_IHDR(IIOMetadataNode root)
    {
        IIOMetadataNode node = new IIOMetadataNode("IHDR");
        node.setAttribute("width", String.valueOf(getInt(PngConstants.WIDTH)));
        node.setAttribute("height", String.valueOf(getInt(PngConstants.HEIGHT)));
        node.setAttribute("bitDepth", String.valueOf(getInt(PngConstants.BIT_DEPTH)));
        node.setAttribute("colorType", getColorType(getInt(PngConstants.COLOR_TYPE)));
        node.setAttribute("compressionMethod", "deflate");
        node.setAttribute("filterMethod", "adaptive");
        node.setAttribute("interlaceMethod", isInterlaced() ? "adam7" : "none");
        root.appendChild(node);
    }

    private boolean isInterlaced()
    {
        return getInt(PngConstants.INTERLACE) == PngConstants.INTERLACE_ADAM7;
    }

    private static String getColorType(int colorType)
    {
        switch (colorType) {
        case PngConstants.COLOR_TYPE_GRAY:
            return "Grayscale";
        case PngConstants.COLOR_TYPE_GRAY_ALPHA:
            return "GrayAlpha";
        case PngConstants.COLOR_TYPE_PALETTE:
            return "Palette";
        case PngConstants.COLOR_TYPE_RGB:
            return "RGB";
        case PngConstants.COLOR_TYPE_RGB_ALPHA:
            return "RGBAlpha";
        }
        return null;
    }

    private void add_PLTE(IIOMetadataNode root)
    {
        IIOMetadataNode node = getPalette("PLTE", "PLTEEntry");
        if (node != null)
            root.appendChild(node);
    }       

    private IIOMetadataNode getPalette(String nodeName, String entryName)
    {
        byte[] palette = (byte[])get(PngConstants.PALETTE);
        if (palette == null)
            return null;

        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        int entries = palette.length / 3;
        int extraEntries = getPaletteSize(entries) - entries;

        int index = 0;
        while (index < palette.length) {
            IIOMetadataNode entry = new IIOMetadataNode(entryName);
            add_PaletteEntry(node,entryName, index / 3,
                             0xFF & palette[index++],
                             0xFF & palette[index++],
                             0xFF & palette[index++]);
        }
        for (int i = 0; i < extraEntries; i++)
            add_PaletteEntry(node, entryName, i + entries, 0, 0, 0);
        return node;
    }

    private void add_PaletteEntry(IIOMetadataNode root, String name, int i, int r, int g, int b)
    {
        IIOMetadataNode node = new IIOMetadataNode(name);
        node.setAttribute("index", Integer.toString(i));
        node.setAttribute("red",   Integer.toString(r));
        node.setAttribute("green", Integer.toString(g));
        node.setAttribute("blue",  Integer.toString(b));
        root.appendChild(node);
    }

    // Sun seems to insist on having extra (enpty) palette entries
    private static int getPaletteSize(int entries)
    {
        if (entries == 0)
            return 0;
        if (entries <= 2)
            return 2;
        if (entries <= 4)
            return 4;
        if (entries <= 16)
            return 16;
        return 256;
    }

    private void add_gAMA(IIOMetadataNode root)
    {
        Float gamma = (Float)get(PngConstants.GAMMA);
        if (gamma == null)
            return;
        IIOMetadataNode node = new IIOMetadataNode("gAMA");
        node.setAttribute("value", formatFloat(gamma.floatValue()));
        root.appendChild(node);
    }

    private void add_tIME(IIOMetadataNode root)
    {
        IIOMetadataNode node = getTime("tIME");
        if (node != null)
            root.appendChild(node);
    }

    private IIOMetadataNode getTime(String nodeName)
    {
        Date time = (Date)get(PngConstants.TIME);
        if (time == null)
            return null;
        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        cal.setTime(time);
        node.setAttribute("year",   String.valueOf(cal.get(Calendar.YEAR)));
        node.setAttribute("month",  String.valueOf(cal.get(Calendar.MONTH) + 1));
        node.setAttribute("day",    String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
        node.setAttribute("hour",   String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
        node.setAttribute("minute", String.valueOf(cal.get(Calendar.MINUTE)));
        node.setAttribute("second", String.valueOf(cal.get(Calendar.SECOND)));
        return node;
    }

    private void add_pHYS(IIOMetadataNode root)
    {
        if (!props.containsKey(PngConstants.PIXELS_PER_UNIT_X))
            return;
        IIOMetadataNode node = new IIOMetadataNode("pHYS");
        node.setAttribute("pixelsPerUnitXAxis",
                          String.valueOf(getInt(PngConstants.PIXELS_PER_UNIT_X)));
        node.setAttribute("pixelsPerUnitYAxis",
                          String.valueOf(getInt(PngConstants.PIXELS_PER_UNIT_Y)));
        boolean meter = getInt(PngConstants.UNIT) == PngConstants.UNIT_METER;
        node.setAttribute("unitSpecifier", meter ? "meter" : "unknown");
        root.appendChild(node);
    }

    private void add_bKGD(IIOMetadataNode root)
    {
        int[] background = (int[])get(PngConstants.BACKGROUND);
        if (background == null)
            return;

        IIOMetadataNode node = new IIOMetadataNode("bKGD");
        IIOMetadataNode child = null;
        switch (getInt(PngConstants.COLOR_TYPE)) {
        case PngConstants.COLOR_TYPE_GRAY:
        case PngConstants.COLOR_TYPE_GRAY_ALPHA:
            child = new IIOMetadataNode("bKGD_Grayscale");
            child.setAttribute("gray", String.valueOf(background[0]));
            break;
        case PngConstants.COLOR_TYPE_RGB:
        case PngConstants.COLOR_TYPE_RGB_ALPHA:
            child = new IIOMetadataNode("bKGD_RGB");
            child.setAttribute("red",   String.valueOf(background[0]));
            child.setAttribute("green", String.valueOf(background[1]));
            child.setAttribute("blue",  String.valueOf(background[2]));
            break;  
        case PngConstants.COLOR_TYPE_PALETTE:
            child = new IIOMetadataNode("bKGD_Palette");
            child.setAttribute("index", String.valueOf(background[0]));
            break;
        }
        node.appendChild(child);
        root.appendChild(node);
    }

    private void add_cHRM(IIOMetadataNode root)
    {
        float[] chrom = (float[])get(PngConstants.CHROMATICITY);
        if (chrom == null)
            return;

        IIOMetadataNode node = new IIOMetadataNode("cHRM");
        node.setAttribute("whitePointX", formatFloat(chrom[0]));
        node.setAttribute("whitePointY", formatFloat(chrom[1]));
        node.setAttribute("redX",   formatFloat(chrom[2]));
        node.setAttribute("redY",   formatFloat(chrom[3]));
        node.setAttribute("greenX", formatFloat(chrom[4]));
        node.setAttribute("greenY", formatFloat(chrom[5]));
        node.setAttribute("blueX",  formatFloat(chrom[6]));
        node.setAttribute("blueY",  formatFloat(chrom[7]));
        root.appendChild(node);
    }
    
    private static String formatFloat(float value)
    {
        return String.valueOf((int)Math.round(value * 1e5));
    }
    
    private void add_tRNS(IIOMetadataNode root)
    {
        Object trans = get(PngConstants.TRANSPARENCY);
        byte[] alpha = (byte[])get(PngConstants.PALETTE_ALPHA);
        if (trans == null && alpha == null)
            return;

        IIOMetadataNode node = new IIOMetadataNode("tRNS");
        IIOMetadataNode child = null;

        switch (getInt(PngConstants.COLOR_TYPE)) {
        case PngConstants.COLOR_TYPE_GRAY:
            child = new IIOMetadataNode("tRNS_Grayscale");
            child.setAttribute("gray", String.valueOf(((int[])trans)[0]));
            break;

        case PngConstants.COLOR_TYPE_RGB:
            child = new IIOMetadataNode("tRNS_RGB");
            child.setAttribute("red",   String.valueOf(((int[])trans)[0]));
            child.setAttribute("green", String.valueOf(((int[])trans)[1]));
            child.setAttribute("blue",  String.valueOf(((int[])trans)[2]));
            break;

        case PngConstants.COLOR_TYPE_PALETTE:
            child = new IIOMetadataNode("tRNS_Palette");
            for (int i = 0; i < alpha.length; i++) {
                IIOMetadataNode entry = new IIOMetadataNode("tRNS_PaletteEntry");
                entry.setAttribute("index", String.valueOf(i));
                entry.setAttribute("alpha", String.valueOf(alpha[i]));
                child.appendChild(entry);
            }
            break;
        }
        node.appendChild(child);
        root.appendChild(node);
    }

    private void add_sBIT(IIOMetadataNode root)
    {
        byte[] sbit = (byte[])get(PngConstants.SIGNIFICANT_BITS);
        if (sbit == null)
            return;

        IIOMetadataNode node = new IIOMetadataNode("sBIT");
        int colorType = getInt(PngConstants.COLOR_TYPE);
        IIOMetadataNode child = new IIOMetadataNode("sBIT_" + getColorType(colorType));
        int index = 0;
        if ((colorType & 2) != 0) {
            child.setAttribute("red",   String.valueOf(sbit[index++]));
            child.setAttribute("green", String.valueOf(sbit[index++]));
            child.setAttribute("blue",  String.valueOf(sbit[index++]));
        } else {
            child.setAttribute("gray", String.valueOf(sbit[index++]));
        }
        if ((colorType & 4) != 0)
            child.setAttribute("alpha", String.valueOf(sbit[index++]));
        node.appendChild(child);
        root.appendChild(node);
    }

    private void add_sRGB(IIOMetadataNode root)
    {
        Integer ri = (Integer)get(PngConstants.RENDERING_INTENT);
        if (ri == null)
            return;
        appendSimpleNode(root, "sRGB", "renderingIntent", getRenderingIntent(ri.intValue()));
    }

    private static String getRenderingIntent(int ri)
    {
        switch (ri) {
        case PngConstants.SRGB_PERCEPTUAL:
            return "Perceptual";
        case PngConstants.SRGB_RELATIVE_COLORIMETRIC:
            return "Relative colorimetric";
        case PngConstants.SRGB_SATURATION_PRESERVING:
            return "Saturation";
        case PngConstants.SRGB_ABSOLUTE_COLORIMETRIC:
            return "Absolute colorimetric";
        }
        return null;
    }

    private void add_iCCP(IIOMetadataNode root)
    {
        String name = (String)get(PngConstants.ICC_PROFILE_NAME);
        if (name == null)
            return;

        IIOMetadataNode node = new IIOMetadataNode("iCCP");
        node.setAttribute("profileName", name);
        node.setAttribute("compressionMethod", "deflate");
        node.setUserObject(get(PngConstants.ICC_PROFILE));
        root.appendChild(node);
    }

    private void add_unknown_chunks(IIOMetadataNode root)
    {
        if (unknownChunks.isEmpty())
            return;

        IIOMetadataNode node = new IIOMetadataNode("UnknownChunks");
        for (Iterator it = unknownChunks.keySet().iterator(); it.hasNext();) {
            Integer type = (Integer)it.next();
            IIOMetadataNode child = new IIOMetadataNode("UnknownChunk");
            child.setAttribute("type", PngConstants.getChunkName(type.intValue()));
            child.setUserObject(unknownChunks.get(type));
            node.appendChild(child);
        }
        root.appendChild(node);
    }

    private void add_hIST(IIOMetadataNode root)
    {
        int[] hist = (int[])get(PngConstants.HISTOGRAM);
        if (hist == null)
            return;

        IIOMetadataNode node = new IIOMetadataNode("hIST");
        for (int i = 0; i < hist.length; i++) {
            IIOMetadataNode child = new IIOMetadataNode("hISTEntry");
            child.setAttribute("index", String.valueOf(i));
            child.setAttribute("value", String.valueOf(hist[i]));
            node.appendChild(child);
        }
        root.appendChild(node);
    }

    private void add_text_chunks(IIOMetadataNode root)
    {
        List textChunks = (List)get(PngConstants.TEXT_CHUNKS);
        if (textChunks == null)
            return;

        Map nodes = new HashMap();
        nodes.put("tEXt", new IIOMetadataNode("tEXt"));
        nodes.put("zTXt", new IIOMetadataNode("zTXt"));
        nodes.put("iTXt", new IIOMetadataNode("iTXt"));
        for (Iterator it = textChunks.iterator(); it.hasNext();) {
            TextChunk chunk = (TextChunk)it.next();
            String name = PngConstants.getChunkName(chunk.getType());
            IIOMetadataNode child = new IIOMetadataNode(name + "Entry");
            child.setAttribute("keyword", chunk.getKeyword());
            switch (chunk.getType()) {
            case PngConstants.zTXt:
                child.setAttribute("compressionMethod", "deflate");
                child.setAttribute("text", chunk.getText());
                break;
            case PngConstants.iTXt:
                child.setAttribute("compressionMethod", "deflate");
                child.setAttribute("value", chunk.getText());
                child.setAttribute("languageTag", chunk.getLanguage());
                child.setAttribute("translatedKeyword", chunk.getTranslatedKeyword());
                child.setAttribute("compressionFlag", "FALSE"); // TODO
                break;
            case PngConstants.tEXt:
                child.setAttribute("value", chunk.getText());
            }
            IIOMetadataNode parent = (IIOMetadataNode)nodes.get(name);
            if (parent.getLength() == 0)
                root.appendChild(parent);
            parent.appendChild(child);
        }
    }
}
