package com.sixlegs.png.examples;

import com.sixlegs.png.*;
import java.io.IOException;
import java.io.File;
import java.util.*;

public class PngCheck
{
    public static void main(String[] args)
    throws IOException
    {
        (new PngImage(new PngConfig.Builder().readLimit(PngConfig.READ_UNTIL_DATA).build()){
            public void handleWarning(PngException e) {
                System.out.println("Warning: " + e.getMessage());
            }
        }).read(new File(args[0]));
    }
}
