package com.sixlegs.png;

import java.io.*;

public class SuiteLoop
{
    public static void main(String[] args)
    throws Exception
    {
        String[] empty = new String[0];
        int count = Integer.parseInt(args[0]);
        while (count-- > 0) {
            SuiteViewer.main(empty);
        }
    }
}
