/*
 * @(#)ARGBPixelGrabber.java
 *
 * Copyright (c) 2007 Jeremy Wood. All Rights Reserved.
 *
 * You have a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) You do not utilize the software in a manner
 * which is disparaging to the original author.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. THE AUTHOR SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL THE
 * AUTHOR BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF THE AUTHOR HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 */

package com.bric.image;

import java.awt.image.*;
import java.awt.*;
//import java.util.*;

/** This is based conceptually on the <code>java.awt.image.PixelGrabber</code> class,
 * except this is designed to be optimized for several types of <code>BufferedImages</code>.
 * This should both save time and prevent <code>BufferedImage</code>'s from becoming un-managed.
 * <P>If this is asked to iterate over the image data for a non-supported <code>Image</code>,
 * then this class will defer to a <code>PixelGrabber</code>.
 * <P>You'll get the best performance out of this class if you iterate over 1 row of data
 * at a time and recycle the array you're using.
 *
 * @see http://javagraphics.blogspot.com/
 * @author mickleness@gmail.com
 */
public final class VTARGBPixelGrabber {
    
    /*public static void main(String[] args) {
        BufferedImage bi = new BufferedImage(1000,1000,BufferedImage.TYPE_INT_ARGB);
        Random r = new Random(0);
        int[] row = new int[bi.getWidth()];
        for(int y = 0; y<bi.getHeight(); y++) {
            for(int x = 0; x<bi.getWidth(); x++) {
                row[x] = (int)(r.nextDouble()*(0xffffffff));
            }
            bi.getRaster().setDataElements(0,y,bi.getWidth(),1,row);
        }
        BufferedImage bi2 = new BufferedImage(bi.getWidth(),bi.getHeight(),BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi2.getGraphics();
        g.drawImage(bi,0,0,null);
        g.dispose();
        
        long[] t = new long[100];
        long time;
        for(int a = 0; a<t.length; a++) {
            time = System.currentTimeMillis();
            PixelGrabber pg = new PixelGrabber(bi, 0, 0, -1, -1, true);
            
            String errmsg = null;
            try {
                if (!pg.grabPixels())
                    errmsg = "can't grab pixels from image";
            } catch (InterruptedException e) {
                errmsg = "interrupted grabbing pixels from image";
            }
            
            if (errmsg != null)
                throw new RuntimeException(errmsg);
            
            int[] argbPixels = (int[]) pg.getPixels();
            time = System.currentTimeMillis()-time;
            t[a] = time;
            System.runFinalization();
            System.gc();
        }
        Arrays.sort(t);
        System.out.println("PixelGrabber.grabPixels: "+t[t.length/2]+" ms");
        
        int[] argb = null;
        for(int a = 0; a<t.length; a++) {
            time = System.currentTimeMillis();
            ARGBPixelGrabber pg = new ARGBPixelGrabber(bi2);
            argb = pg.getPixels(argb);
            time = System.currentTimeMillis()-time;
            t[a] = time;
            System.runFinalization();
            System.gc();
        }
        Arrays.sort(t);
        System.out.println("ARGBPixelGrabber.grabPixels: "+t[t.length/2]+" ms");

        int[] row2 = new int[bi.getWidth()];
        for(int a = 0; a<t.length; a++) {
            time = System.currentTimeMillis();
            ARGBPixelGrabber pg = new ARGBPixelGrabber(bi2);
            for(int y = 0; y<pg.getHeight(); y++) {
                pg.next(row2);
            }
            time = System.currentTimeMillis()-time;
            t[a] = time;
            System.runFinalization();
            System.gc();
        }
        Arrays.sort(t);
        System.out.println("ARGBPixelGrabber.next: "+t[t.length/2]+" ms");
        System.exit(0);
        
    }*/
	
	public VTARGBPixelGrabber()
	{
		
	}
	
	public final void dispose()
	{
		this.argbPixels = null;
		this.icm = null;
		this.r = null;
		this.row = null;
		this.scratchByteArray = null;
		this.scratchIntArray = null;
	}
    
    /** If grabber is using a supported BufferedImage, then this is
     * the <code>BufferedImage.getType()</code> value for that image.
     */
    private int t;
    
    /** The width of the image.
     * This may be different from "width" if we're basically clipping a BufferedImage.
     **/
    private int imageW;
    
    /** The height of the image.
     * This may be different from "height" if we're basically clipping a BufferedImage.
     **/
    private int imageH;
    
    /** If we're iterating over a BufferedImage that uses an IndexColorModel,
     * this is that model.
     */
    private IndexColorModel icm;
    
    /** If we're iterating over a BufferedImage, this is its raster */
    private Raster r;
    
    /** This is the current x-value we are about to fetch */
    private int currentX;
    
    /** This is the current y-value we are fetching from */
    private int currentY;
    
    /** If we we're asked to return blocks that aren't entire rows, then this
     * little buffer represents the row of currentY */
    private int[] row;
    
    /** The x-value of the rectangle this grabber is clipped to.  This is
     * only used for optimized BufferedImages. */
    private int x;
    
    /** The y-value of the rectangle this grabber is clipped to.  This is
     * only used for optimized BufferedImages. */
    private int y;
    
    /** The width of the rectangle this grabber is clipped to. */
    public int width;
    
    /** The height of the rectangle this grabber is clipped to. */
    public int height;
    
    /** If we aren't optimized to play well with whatever Image was thrown at us,
     * then we use an old-fashioned PixelGrabber to get the pixel data the
     * old-fashioned way.  This is that data.
     */
    private int[] argbPixels;
    
    /** Create an ARGBPixelGrabber to iterate over <code>i</code> */
    public final void setImage(Image i) {
    	setImage(i,null);
    }
    
    /** Create an ARGBPixelGrabber to iterate over <code>i</code>
     * @param rect if non-null, then this object will only iterate over
     * this rectangle.  An exception may be thrown if <code>rect</code>
     * is not completely contained within <code>i</code>
     */
    public final void setImage(Image i,Rectangle rect) {
        if(i instanceof BufferedImage &&
                isBufferedImageTypeSupported( ((BufferedImage)i).getType() )) {
            BufferedImage bi = (BufferedImage)i;
            t = bi.getType();
            imageW = bi.getWidth();
            imageH = bi.getHeight();
            r = bi.getRaster();
            row = new int[imageW];
            if(t==BufferedImage.TYPE_BYTE_INDEXED) {
            //|| t==BufferedImage.TYPE_BYTE_BINARY) {
                icm = (IndexColorModel)bi.getColorModel();
            } else {
                icm = null;
            }
            argbPixels = null;
            
            if(rect==null) {
                x = 0;
                y = 0;
                width = imageW;
                height = imageH;
            } else {
                if(rect.x<0 || rect.y<0 || (rect.x+rect.width>imageW) || (rect.y+rect.height>imageH)) {
                    //System.err.println("Image size: "+imageW+" x "+imageH+" pixels");
                    //System.err.println("rect: "+rect);
                    throw new IllegalArgumentException("The rectangle provided falls outside of this image.");
                }
                x = rect.x;
                y = rect.y;
                width = rect.width;
                height = rect.height;
            }
        } else {
            t = BufferedImage.TYPE_CUSTOM;
            r = null;
            icm = null;
            
            PixelGrabber pg;
            if(rect==null) {
                pg = new PixelGrabber(i, 0, 0, -1, -1, true);
            } else {
                pg = new PixelGrabber(i, rect.x, rect.y, rect.width, rect.height, true);
            }
            String errmsg = null;
            try {
                if (!pg.grabPixels())
                    errmsg = "can't grab pixels from image";
            } catch (InterruptedException e) {
                errmsg = "interrupted grabbing pixels from image";
            }
            
            if (errmsg != null)
                throw new RuntimeException(errmsg + " (" + i.getClass().getSimpleName() + ")");
            
            imageW = pg.getWidth();
            imageH = pg.getHeight();
            argbPixels = (int[]) pg.getPixels();
            
            x = 0;
            y = 0;
            width = imageW;
            height = imageH;
        }
        currentX = x;
        currentY = y;
    }
    
    /** @return the width of the image.
     * Note this <i>may</i> be different from <code>BufferedImage.getWidth()</code> if you
     * asked this grabber to clip to a subset of the image.
     * 
     */
    public final int getWidth() {
        return width;
    }
    
    /** @return the height of the image.
     * Note this <i>may</i> be different from <code>BufferedImage.getWidth()</code> if you
     * asked this grabber to clip to a subset of the image.
     */
    public final int getHeight() {
        return height;
    }
    
    /** This indicates whether a BufferedImageType is supported.
     * Currently support image types include:
     * <LI>BufferedImage.TYPE_INT_RGB
     * <LI>BufferedImage.TYPE_INT_ARGB 
     * <LI>BufferedImage.TYPE_INT_ARGB_PRE
     * <LI>BufferedImage.TYPE_INT_BGR
     * <LI>BufferedImage.TYPE_3BYTE_BGR
     * <LI>BufferedImage.TYPE_4BYTE_ABGR
     * <LI>BufferedImage.TYPE_4BYTE_ABGR_PRE
     * <LI>BufferedImage.TYPE_BYTE_GRAY
     * <LI>BufferedImage.TYPE_BYTE_INDEXED</LI>
     * @param t
     * @return
     */
    protected final static boolean isBufferedImageTypeSupported(int t) {
        return t==BufferedImage.TYPE_INT_RGB || 
        t==BufferedImage.TYPE_INT_ARGB || 
        t==BufferedImage.TYPE_INT_ARGB_PRE ||
        t==BufferedImage.TYPE_INT_BGR ||
        t==BufferedImage.TYPE_3BYTE_BGR ||
        t==BufferedImage.TYPE_4BYTE_ABGR || 
        t==BufferedImage.TYPE_4BYTE_ABGR_PRE ||
        t==BufferedImage.TYPE_BYTE_GRAY ||
        t==BufferedImage.TYPE_BYTE_INDEXED;
        //|| t==BufferedImage.TYPE_BYTE_BINARY;
    }
    
    /** Indicates whether all the data has been returned yet.
     * 
     * @return <code>true</code> if there is no more data.
     * If there is no more data, then all calls to <code>next</code>
     * will return zero.
     */
    public final boolean isDone() {
        return (currentY==y+height && currentX==x+width);
    }
    
    /** This returns all the pixels of this image at once.
     * <P>This method is provided largely to be consistant with
     * the <code>java.awt.image.PixelGrabber</code>.  If it is
     * possible, it is probably more efficient to iterate over the pixel
     * data one row at a time.
     * <P>This method returns the entire image in bulk, and does
     * not interact with the "iterator" nature of this object.
     * So calling this method will not affect the state
     * of <code>isDone</code>, or the state of future
     * calls to <code>next(...)</code>.
     * <P>Calling this method is equivalent to calling:
     * <BR><code>getPixels(new int[getWidth()*getHeight()]);</code>
     * @return an array that is <code>getWidth()*getHeight()</code>
     * units long, containing all the ARGB data of this image.
     */
    public final int[] getPixels() {
        return getPixels(new int[width*height]);
    }
    
    /** This will reset the iterator to begin iterating over
     * this image data again.
     *
     */
    public final void reset() {
        currentX = x;
        currentY = y;
    }
    
    /** This returns all the pixels of this image at once.
     * <P>This method is provided largely to be consistant with
     * the <code>java.awt.image.PixelGrabber</code>.  If it is
     * possible, it is probably more efficient to iterate over the pixel
     * data one row at a time.
     * <P>This method returns the entire image in bulk, and does
     * not interact with the "iterator" nature of this object.
     * So calling this method will not affect the state
     * of <code>isDone</code>, or the state of future
     * calls to <code>next(...)</code>.
     * @param array this is the destination array to store this
     * data in.  If this is <code>null</code> then a new
     * array will be created for you.
     * An exception will be thrown if the array is not large enough to accomodate
     * all the pixel data.
     * @return an array containing all the pixel data of this image.  If <code>array</code>
     * was non-null, then this returns the argument <code>array</code>
     */
    public final int[] getPixels(int[] array) {
        if(array==null) {
            array = new int[width*height];
        } else {
            if(array.length<width*height)
                throw new IllegalArgumentException("This array is not big enough to fit all the image data present.  ( "+array.length+" < "+width+"*"+height+" )");
        }
        if(argbPixels!=null) {
            System.arraycopy(argbPixels,0,array,0,argbPixels.length);
        } else if(t==BufferedImage.TYPE_INT_ARGB ||
                t==BufferedImage.TYPE_INT_ARGB_PRE ||
                t==BufferedImage.TYPE_INT_RGB) {
            //this is special case, we can really speed things up here!
            r.getDataElements(x,y,width,height,array);
        } else {
            for(int y2 = y; y2<y+height; y2++) {
                readRow(y2,array,(y2-y)*width);
            }
        }
        
        return array;
    }
    
    /** This will fill the array <code>data</code> with incoming
     * pixel data.  (Unless the end of the image is reached.)
     * <P>This is equivalent to calling:
     * <BR><code>next(data,0,data.length);</code>
     * @param data the array to store the data in
     * @return the number of pixels read.  This will be the length
     * of <code>data</code> unless the end of the image is reached.
     */
    public final int next(int[] data) {
        return next(data,0,data.length);
    }
    
    /** Fills the array provided with
     * ARGB pixel data.
     * 
     * @param data the array to fill.
     * This array can be of arbitrary length, but you may
     * see better performance if you ask for <code>getWidth()</code>
     * many pixels at a time.
     * @param arrayOffset this is the index to begin
     * writing to in <code>data</code>.
     * <P>If the image involved is a <code>BufferedImage</code>
     * of type <code>ARGB_PRE</code>, <code>ARGB</code> or <code>RGB</code>
     * then you will see improved performance if this value is zero.
     * @param arrayLength this is the amount of data
     * that should be read into <code>data</code>.
     * An exception will be thrown if this method is asked
     * to write more pixel data than will fit in the array.
     * <P>If this value is a non-positive number, then an
     * arbitrary number of pixels will be stored in
     * the array.
     * @return the number of pixels written
     * in this array.  This will generally be equal
     * to <code>arrayLength</code>, except in 2 cases:
     * <LI>When this iterator has finished.
     * <LI>When <code>arrayLength</code> is non-positve.
     * In this case this object gets to decide how
     * many pixels to return at a time.</LI>
     * <P>This will only return 0
     * if <code>isDone()</code> returns
     * <code>true</code>.
     */
    public final int next(int[] data,int arrayOffset,int arrayLength) {
        if(arrayLength>0) {
            if(data.length-arrayOffset<arrayLength)
                throw new IllegalArgumentException("Illegal request for "+arrayLength+" pixels, when the array can only contain "+(data.length-arrayOffset)+" pixels.");
        } else {
            arrayLength = data.length-arrayOffset;
        }
        
        
        
        int copied = 0;
        while(!(isDone()) && copied<arrayLength) {
            if(currentX==x && arrayLength-copied>=width) {
                //this is the super-slick way:
                //just bite off one whole row at a time
                readRow(currentY,data,arrayOffset+copied);
                copied+=width;
                currentY++;
                //currentX stays the same
            } else {
                //we have to work in chunks, then:
                if(currentX==x) {
                    //we're ready for a new row:
                    row = readRow(currentY,row,0);
                }
                //how much are we going to copy into "data" right now?
                int amt = arrayLength;
                if(amt>row.length-(currentX-x)) {
                    amt = row.length-(currentX-x);
                }
                System.arraycopy(row,currentX-x,data,arrayOffset+copied,amt);
                currentX+=amt;
                copied+=amt;
                if(currentX==width) {
                    currentX = x;
                    currentY++;
                }
                
            }
        }
        return copied;
    }
    
    /** An optional array used for certain int-based BufferedImages   */
    private int[] scratchIntArray;
    
    /** An optional array used for certain byte-based BufferedImages   */
    private byte[] scratchByteArray = null;
    
    /** This is the actual magic where we grab the innards of the raster r
     * one row at a time.
     * @param y the row to grab
     * @param dest the destination to write to
     * @param destOffset the offset in dest to write to
     */
    private final int[] readRow(int y,int[] dest,int destOffset) {
        if(dest.length-destOffset<width)
            throw new IllegalArgumentException("this array must have at least "+width+" pixels available in it.  ("+(dest.length-destOffset)+")");
        if(r!=null) {
            switch(t) {
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_ARGB_PRE:
            {
                if(destOffset==0) {
                    //yay!  the best case we can hope for:
                    r.getDataElements(x,y,width,1,dest);
                } else {
                    //erg.  This is a little inefficient... hopefully
                    //the caller took our advice and bit off row-sized chunks
                    //at a time so this isn't used too often.
                    if(scratchIntArray==null)
                        scratchIntArray = new int[width];
                    r.getDataElements(0,y,width,1,scratchIntArray);
                    System.arraycopy(scratchIntArray,0,dest,destOffset,width);
                }
                if(t==BufferedImage.TYPE_INT_RGB) {
                    //give everything an opaque alpha component
                    for(int a = destOffset; a<destOffset+width; a++) {
                        dest[a] = (dest[a] & 0xFFFFFF)+(0xff000000);
                    }
                }
                break;
            }
            case BufferedImage.TYPE_INT_BGR:
            {
                if(scratchIntArray==null)
                    scratchIntArray = new int[width];
                r.getDataElements(x,y,width,1,scratchIntArray);
                //flip it from BGR to ARGB
                for(int a = 0; a<scratchIntArray.length; a++) {
                    dest[destOffset+a] = ((scratchIntArray[a] & 0xFF) << 16) + //red
                    (scratchIntArray[a] & 0xFF00) + //green
                    ((scratchIntArray[a] >> 16) & 0xFF)+ //blue
                    (0xFF000000); //alpha
                }
                break;
            }
            case BufferedImage.TYPE_3BYTE_BGR:
            {
                if(scratchByteArray==null)
                    scratchByteArray = new byte[3*width];
                r.getDataElements(x,y,width,1,scratchByteArray);
                for(int a = 0; a<width; a++) {
                    dest[destOffset+a] = ((scratchByteArray[3*a+2] & 0xFF) << 16)+ //red
                    ((scratchByteArray[3*a+1] & 0xFF) << 8)+ //green
                    (scratchByteArray[3*a] & 0xFF)+ //blue
                    (0xFF000000); //alpha
                }
                break;
            }
            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_4BYTE_ABGR_PRE:
            {
                if(scratchByteArray==null)
                    scratchByteArray = new byte[4*width];
                r.getDataElements(x,y,width,1,scratchByteArray);
                for(int a = 0; a<width; a++) {
                    dest[destOffset+a] = ((scratchByteArray[4*a] & 0xFF) << 24)+ //alpha
                    ((scratchByteArray[4*a+3] & 0xFF) << 16)+ //red
                    ((scratchByteArray[4*a+2] & 0xFF) << 8)+ //green
                    (scratchByteArray[4*a+1] & 0xFF); //blue
                }
                break;
            }
            case BufferedImage.TYPE_BYTE_INDEXED:
            //case BufferedImage.TYPE_BYTE_BINARY:
            {
            	//int pixelsPerByte = 8 / icm.getPixelSize();
            	if(scratchByteArray==null)
            	{
            		scratchByteArray = new byte[width];
            		//scratchByteArray = new byte[width / pixelsPerByte];
            	}
                int t2, alpha;
                int transIndex = icm.getTransparentPixel();
                r.getDataElements(x,y,width,1,scratchByteArray);
                for(int a = 0; a<row.length; a++) {
                    t2 = scratchByteArray[a] & 0xff;
                    alpha = (t2==transIndex) ? 1 : 0;
                    dest[destOffset+a] = (icm.getRed(t2) << 16)+(icm.getGreen(t2) << 8)+icm.getBlue(t2)+alpha*(0xff000000);
                }
                break;
            }
            default:
                throw new RuntimeException("Unexpected condition.");
            }
        } else if(argbPixels!=null) {
            System.arraycopy(argbPixels,y*width,dest,destOffset,width);
        } else {
            throw new RuntimeException("Unexpected condition.");
        }
        return dest;
    }
}