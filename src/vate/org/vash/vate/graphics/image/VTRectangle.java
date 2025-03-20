package org.vash.vate.graphics.image;

public class VTRectangle
{
  public int x;
  public int y;
  public int width;
  public int height;
  
  public VTRectangle()
  {
    
  }
  
  public VTRectangle(int x, int y, int w, int h)
  {
    this.x = x;
    this.y = y;
    this.width = w;
    this.height = h;
  }
  
  public VTRectangle(VTRectangle r)
  {
    new VTRectangle(r.x, r.y, r.width, r.height);
  }
  
  public VTRectangle union(VTRectangle r)
  {
    int tx2 = this.width;
    int ty2 = this.height;
    if ((tx2 | ty2) < 0) {
        // This rectangle has negative dimensions...
        // If r has non-negative dimensions then it is the answer.
        // If r is non-existant (has a negative dimension), then both
        // are non-existant and we can return any non-existant rectangle
        // as an answer.  Thus, returning r meets that criterion.
        // Either way, r is our answer.
        return new VTRectangle(r);
    }
    int rx2 = r.width;
    int ry2 = r.height;
    if ((rx2 | ry2) < 0) {
        return new VTRectangle(this);
    }
    int tx1 = this.x;
    int ty1 = this.y;
    tx2 += tx1;
    ty2 += ty1;
    int rx1 = r.x;
    int ry1 = r.y;
    rx2 += rx1;
    ry2 += ry1;
    if (tx1 > rx1) tx1 = rx1;
    if (ty1 > ry1) ty1 = ry1;
    if (tx2 < rx2) tx2 = rx2;
    if (ty2 < ry2) ty2 = ry2;
    tx2 -= tx1;
    ty2 -= ty1;
    // tx2,ty2 will never underflow since both original rectangles
    // were already proven to be non-empty
    // they might overflow, though...
    if (tx2 > Integer.MAX_VALUE) tx2 = Integer.MAX_VALUE;
    if (ty2 > Integer.MAX_VALUE) ty2 = Integer.MAX_VALUE;
    return new VTRectangle(tx1, ty1, tx2, ty2);
  }
}