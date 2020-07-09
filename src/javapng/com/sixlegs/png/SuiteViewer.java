package com.sixlegs.png;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class SuiteViewer
{
    private static final String[] IMAGES = {
        "basi0g01", "basi0g02", "basi0g04", "basi0g08", "basi0g16",
        "basi2c08", "basi2c16", "basi3p01", "basi3p02", "basi3p04",
        "basi3p08", "basi4a08", "basi4a16", "basi6a08", "basi6a16", null,
        "basn0g01", "basn0g02", "basn0g04", "basn0g08", "basn0g16",
        "basn2c08", "basn2c16", "basn3p01", "basn3p02", "basn3p04",
        "basn3p08", "basn4a08", "basn4a16", "basn6a08", "basn6a16", null,
        "bgai4a08", "bgai4a16", "bgan6a08", "bgan6a16",
        "bgbn4a08", "bggn4a16", "bgwn6a08", "bgyn6a16", null,
        "ccwn2c08", "ccwn3p08", "ch1n3p04", "ch2n3p08",
        "cdfn2c08", "cdhn2c08", "cdsn2c08", "cdun2c08", null,
        "cs3n2c16", "cs3n3p08", "cs5n2c08", "cs5n3p08", "cs8n2c08", "cs8n3p08",
        "cm0n0g04", "cm7n0g04", "cm9n0g04", "ct0n0g04", "ct1n0g04", "ctzn0g04", null,
        "f00n0g08", "f01n0g08", "f02n0g08", "f03n0g08", "f04n0g08",
        "f00n2c08", "f01n2c08", "f02n2c08", "f03n2c08", "f04n2c08", null,
        "g03n0g16", "g04n0g16", "g05n0g16", "g07n0g16", "g10n0g16", "g25n0g16", null,
        "g03n2c08", "g04n2c08", "g05n2c08", "g07n2c08", "g10n2c08", "g25n2c08", null,
        "g03n3p04", "g04n3p04", "g05n3p04", "g07n3p04", "g10n3p04", "g25n3p04", null,
        "oi1n0g16", "oi1n2c16", "oi2n0g16", "oi2n2c16",
        "oi4n0g16", "oi4n2c16", "oi9n0g16", "oi9n2c16", null,
        "s01i3p01", "s02i3p01", "s03i3p01", "s04i3p01",
        "s05i3p02", "s06i3p02", "s07i3p02", "s08i3p02", "s09i3p02", null,
        "s01n3p01", "s02n3p01", "s03n3p01", "s04n3p01",
        "s05n3p02", "s06n3p02", "s07n3p02", "s08n3p02", "s09n3p02", null,
        "s32i3p04", "s33i3p04", "s34i3p04", "s35i3p04",
        "s36i3p04", "s37i3p04", "s38i3p04", "s39i3p04", "s40i3p04", null,
        "s32n3p04", "s33n3p04", "s34n3p04", "s35n3p04", 
        "s36n3p04", "s37n3p04", "s38n3p04", "s39n3p04", "s40n3p04", null,
        "tbbn1g04", "tbbn2c16", "tbbn3p08", "tbgn2c16", "tbgn3p08",
        "tbrn2c08", "tbwn1g16", "tbwn3p08", "tbyn3p08", "tp0n1g08",
        "tp0n2c08", "tp0n3p08", "tp1n3p08", null,
        "z00n2c08", "z03n2c08", "z06n2c08", "z09n2c08",
        "pp0n2c16", "pp0n6a08", "ps1n0g08", "ps1n2c16", "ps2n0g08", "ps2n2c16",
    };

    private static final int PADDING = 8;
    private static final int ACROSS = 15;
    private static final int DOWN = 16;

    final private PngConfig config;

    public SuiteViewer()
    {
        this(new PngConfig.Builder()
             .readLimit(PngConfig.READ_EXCEPT_METADATA)
             .build());
    }

    public SuiteViewer(PngConfig config)
    {
        this.config = config;
    }

    public static void main(String[] args)
    throws Exception
    {
        ImageIO.write(new SuiteViewer().render(true), "PNG", new File(args[0]));
    }

    public BufferedImage render(boolean createImage)
    throws IOException
    {
        int w = (32 + PADDING) * ACROSS;
        int h = (32 + PADDING) * DOWN;
        BufferedImage test = null;
        Graphics2D g = null;
        if (createImage) {
            test = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            g = test.createGraphics();
            g.setPaint(Color.gray);
            g.fillRect(0, 0, w, h);
        }
        int i = 0, ix = 0, iy = 0;
        while (i < IMAGES.length) {
            if (IMAGES[i] == null) {
                i++;
                iy++;
                ix = 0;
            }
            int x = (32 + PADDING) * ix + PADDING / 2;
            int y = (32 + PADDING) * iy + PADDING / 2;
            String resource = "/images/suite/" + IMAGES[i] + ".png";
            PngImage png = new PngImage(config);
            BufferedImage img = png.read(SuiteViewer.class.getResourceAsStream(resource), true);
            if (createImage) {
                Color bg = png.getBackground();
                if (bg != null) {
                    g.setPaint(bg);
                    g.fillRect(x, y, img.getWidth(), img.getHeight());
                }
                g.drawImage(img, x, y, null);
            }
            i++;
            ix++;
        }
        if (createImage) {
            g.dispose();
            return test;
        }
        return null;
    }
}
