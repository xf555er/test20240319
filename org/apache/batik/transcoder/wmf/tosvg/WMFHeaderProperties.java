package org.apache.batik.transcoder.wmf.tosvg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.batik.ext.awt.geom.Polygon2D;
import org.apache.batik.ext.awt.geom.Polyline2D;

public class WMFHeaderProperties extends AbstractWMFReader {
   private static final Integer INTEGER_0 = 0;
   protected DataInputStream stream;
   private int _bleft;
   private int _bright;
   private int _btop;
   private int _bbottom;
   private int _bwidth;
   private int _bheight;
   private int _ileft;
   private int _iright;
   private int _itop;
   private int _ibottom;
   private float scale = 1.0F;
   private int startX = 0;
   private int startY = 0;
   private int currentHorizAlign = 0;
   private int currentVertAlign = 0;
   private WMFFont wf = null;
   private static final FontRenderContext fontCtx = new FontRenderContext(new AffineTransform(), false, true);
   private transient boolean firstEffectivePaint = true;
   public static final int PEN = 1;
   public static final int BRUSH = 2;
   public static final int FONT = 3;
   public static final int NULL_PEN = 4;
   public static final int NULL_BRUSH = 5;
   public static final int PALETTE = 6;
   public static final int OBJ_BITMAP = 7;
   public static final int OBJ_REGION = 8;

   public WMFHeaderProperties(File wmffile) throws IOException {
      this.reset();
      this.stream = new DataInputStream(new BufferedInputStream(new FileInputStream(wmffile)));
      this.read(this.stream);
      this.stream.close();
   }

   public WMFHeaderProperties() {
   }

   public void closeResource() {
      try {
         if (this.stream != null) {
            this.stream.close();
         }
      } catch (IOException var2) {
      }

   }

   public void setFile(File wmffile) throws IOException {
      this.stream = new DataInputStream(new BufferedInputStream(new FileInputStream(wmffile)));
      this.read(this.stream);
      this.stream.close();
   }

   public void reset() {
      this.left = 0;
      this.right = 0;
      this.top = 1000;
      this.bottom = 1000;
      this.inch = 84;
      this._bleft = -1;
      this._bright = -1;
      this._btop = -1;
      this._bbottom = -1;
      this._ileft = -1;
      this._iright = -1;
      this._itop = -1;
      this._ibottom = -1;
      this._bwidth = -1;
      this._bheight = -1;
      this.vpW = -1;
      this.vpH = -1;
      this.vpX = 0;
      this.vpY = 0;
      this.startX = 0;
      this.startY = 0;
      this.scaleXY = 1.0F;
      this.firstEffectivePaint = true;
   }

   public DataInputStream getStream() {
      return this.stream;
   }

   protected boolean readRecords(DataInputStream is) throws IOException {
      short functionId = 1;
      int recSize = false;
      int brushObject = -1;
      int penObject = -1;
      int fontObject = -1;

      label296:
      while(functionId > 0) {
         int recSize = this.readInt(is);
         recSize -= 3;
         functionId = this.readShort(is);
         if (functionId <= 0) {
            break;
         }

         short gdiIndex;
         GdiObject gdiObj;
         short objIndex;
         float heightDst;
         float size;
         float dy;
         float dx;
         int read;
         int var10000;
         int x;
         int style;
         int lfWidth;
         int y;
         boolean objIndex;
         int x;
         int lenText;
         short count;
         int offset;
         short top;
         short escape;
         int j;
         float[] _ypts;
         Rectangle2D.Float rec;
         float[] _xpts;
         short orient;
         int y;
         Color color;
         switch (functionId) {
            case 247:
               objIndex = false;

               for(x = 0; x < recSize; ++x) {
                  this.readShort(is);
               }

               y = this.addObjectAt(8, INTEGER_0, 0);
               break;
            case 259:
               objIndex = this.readShort(is);
               if (objIndex == 8) {
                  this.isotropic = false;
               }
               break;
            case 301:
               gdiIndex = this.readShort(is);
               if ((gdiIndex & Integer.MIN_VALUE) == 0) {
                  gdiObj = this.getObject(gdiIndex);
                  if (gdiObj.used) {
                     switch (gdiObj.type) {
                        case 1:
                           penObject = gdiIndex;
                           break;
                        case 2:
                           brushObject = gdiIndex;
                           break;
                        case 3:
                           this.wf = (WMFFont)gdiObj.obj;
                           fontObject = gdiIndex;
                           break;
                        case 4:
                           penObject = -1;
                           break;
                        case 5:
                           brushObject = -1;
                     }
                  }
               }
               break;
            case 302:
               objIndex = this.readShort(is);
               if (recSize > 1) {
                  for(y = 1; y < recSize; ++y) {
                     this.readShort(is);
                  }
               }

               this.currentHorizAlign = WMFUtilities.getHorizontalAlignment(objIndex);
               this.currentVertAlign = WMFUtilities.getVerticalAlignment(objIndex);
               break;
            case 496:
               gdiIndex = this.readShort(is);
               gdiObj = this.getObject(gdiIndex);
               if (gdiIndex == brushObject) {
                  brushObject = -1;
               } else if (gdiIndex == penObject) {
                  penObject = -1;
               } else if (gdiIndex == fontObject) {
                  fontObject = -1;
               }

               gdiObj.clear();
               break;
            case 523:
               this.vpY = this.readShort(is);
               this.vpX = this.readShort(is);
               break;
            case 524:
               this.vpH = this.readShort(is);
               this.vpW = this.readShort(is);
               if (!this.isotropic) {
                  this.scaleXY = (float)this.vpW / (float)this.vpH;
               }

               this.vpW = (int)((float)this.vpW * this.scaleXY);
               break;
            case 531:
               count = this.readShort(is);
               x = (int)((float)this.readShort(is) * this.scaleXY);
               if (penObject >= 0) {
                  this.resizeBounds(this.startX, this.startY);
                  this.resizeBounds(x, count);
                  this.firstEffectivePaint = false;
               }

               this.startX = x;
               this.startY = count;
               break;
            case 532:
               this.startY = this.readShort(is);
               this.startX = (int)((float)this.readShort(is) * this.scaleXY);
               break;
            case 762:
               objIndex = 0;
               count = this.readShort(is);
               this.readInt(is);
               x = this.readInt(is);
               lenText = x & 255;
               offset = (x & '\uff00') >> 8;
               read = (x & 16711680) >> 16;
               color = new Color(lenText, offset, read);
               if (recSize == 6) {
                  this.readShort(is);
               }

               if (count == 5) {
                  this.addObjectAt(4, color, objIndex);
               } else {
                  this.addObjectAt(1, color, objIndex);
               }
               break;
            case 763:
               count = this.readShort(is);
               size = (float)((int)(this.scaleY * (float)count));
               this.readShort(is);
               escape = this.readShort(is);
               orient = this.readShort(is);
               int weight = this.readShort(is);
               int italic = is.readByte();
               int underline = is.readByte();
               int strikeOut = is.readByte();
               int charset = is.readByte() & 255;
               int lfOutPrecision = is.readByte();
               int lfClipPrecision = is.readByte();
               int lfQuality = is.readByte();
               int lfPitchAndFamily = is.readByte();
               style = italic > 0 ? 2 : 0;
               style |= weight > 400 ? 1 : 0;
               lfWidth = 2 * (recSize - 9);
               byte[] lfFaceName = new byte[lfWidth];

               for(int i = 0; i < lfWidth; ++i) {
                  lfFaceName[i] = is.readByte();
               }

               String face = new String(lfFaceName);

               int d;
               for(d = 0; d < face.length() && (Character.isLetterOrDigit(face.charAt(d)) || Character.isWhitespace(face.charAt(d))); ++d) {
               }

               if (d > 0) {
                  face = face.substring(0, d);
               } else {
                  face = "System";
               }

               if (size < 0.0F) {
                  size = -size;
               }

               int objIndex = 0;
               Font f = new Font(face, style, (int)size);
               f = f.deriveFont(size);
               WMFFont wf = new WMFFont(f, charset, underline, strikeOut, italic, weight, orient, escape);
               this.addObjectAt(3, wf, objIndex);
               break;
            case 764:
               objIndex = 0;
               count = this.readShort(is);
               x = this.readInt(is);
               lenText = x & 255;
               offset = (x & '\uff00') >> 8;
               read = (x & 16711680) >> 16;
               color = new Color(lenText, offset, read);
               this.readShort(is);
               if (count == 5) {
                  this.addObjectAt(5, color, objIndex);
               } else {
                  this.addObjectAt(2, color, objIndex);
               }
               break;
            case 804:
               count = this.readShort(is);
               _xpts = new float[count + 1];
               _ypts = new float[count + 1];

               for(offset = 0; offset < count; ++offset) {
                  _xpts[offset] = (float)this.readShort(is) * this.scaleXY;
                  _ypts[offset] = (float)this.readShort(is);
               }

               _xpts[count] = _xpts[0];
               _ypts[count] = _ypts[0];
               Polygon2D pol = new Polygon2D(_xpts, _ypts, count);
               this.paint(brushObject, penObject, pol);
               break;
            case 805:
               count = this.readShort(is);
               _xpts = new float[count];
               _ypts = new float[count];

               for(offset = 0; offset < count; ++offset) {
                  _xpts[offset] = (float)this.readShort(is) * this.scaleXY;
                  _ypts[offset] = (float)this.readShort(is);
               }

               Polyline2D pol = new Polyline2D(_xpts, _ypts, count);
               this.paintWithPen(penObject, pol);
               break;
            case 1046:
            case 1048:
            case 1051:
               count = this.readShort(is);
               x = (int)((float)this.readShort(is) * this.scaleXY);
               top = this.readShort(is);
               offset = (int)((float)this.readShort(is) * this.scaleXY);
               rec = new Rectangle2D.Float((float)offset, (float)top, (float)(x - offset), (float)(count - top));
               this.paint(brushObject, penObject, rec);
               break;
            case 1313:
            case 1583:
               count = this.readShort(is);
               int read = 1;
               byte[] bstr = new byte[count];

               for(offset = 0; offset < count; ++offset) {
                  bstr[offset] = is.readByte();
               }

               String sr = WMFUtilities.decodeString(this.wf, bstr);
               if (count % 2 != 0) {
                  is.readByte();
               }

               x = read + (count + 1) / 2;
               orient = this.readShort(is);
               var10000 = (int)((float)this.readShort(is) * this.scaleXY);
               x += 2;
               if (x < recSize) {
                  for(x = x; x < recSize; ++x) {
                     this.readShort(is);
                  }
               }

               TextLayout layout = new TextLayout(sr, this.wf.font, fontCtx);
               y = (int)layout.getBounds().getWidth();
               j = (int)layout.getBounds().getX();
               int lfHeight = (int)this.getVerticalAlignmentValue(layout, this.currentVertAlign);
               this.resizeBounds(j, orient);
               this.resizeBounds(j + y, orient + lfHeight);
               break;
            case 1336:
               count = this.readShort(is);
               int[] pts = new int[count];
               lenText = 0;

               for(offset = 0; offset < count; ++offset) {
                  pts[offset] = this.readShort(is);
                  lenText += pts[offset];
               }

               offset = count + 1;

               for(read = 0; read < count; ++read) {
                  for(j = 0; j < pts[read]; ++j) {
                     x = (int)((float)this.readShort(is) * this.scaleXY);
                     y = this.readShort(is);
                     if (brushObject >= 0 || penObject >= 0) {
                        this.resizeBounds(x, y);
                     }
                  }
               }

               this.firstEffectivePaint = false;
               break;
            case 1564:
               this.readShort(is);
               this.readShort(is);
               count = this.readShort(is);
               x = (int)((float)this.readShort(is) * this.scaleXY);
               top = this.readShort(is);
               offset = (int)((float)this.readShort(is) * this.scaleXY);
               rec = new Rectangle2D.Float((float)offset, (float)top, (float)(x - offset), (float)(count - top));
               this.paint(brushObject, penObject, rec);
               break;
            case 1565:
               this.readInt(is);
               count = this.readShort(is);
               x = (int)((float)this.readShort(is) * this.scaleXY);
               lenText = (int)((float)this.readShort(is) * this.scaleXY);
               escape = this.readShort(is);
               if (penObject >= 0) {
                  this.resizeBounds(lenText, escape);
               }

               if (penObject >= 0) {
                  this.resizeBounds(lenText + x, escape + count);
               }
               break;
            case 1791:
               objIndex = false;

               for(x = 0; x < recSize; ++x) {
                  this.readShort(is);
               }

               y = this.addObjectAt(6, INTEGER_0, 0);
               break;
            case 2071:
            case 2074:
            case 2096:
               this.readShort(is);
               this.readShort(is);
               this.readShort(is);
               this.readShort(is);
               y = this.readShort(is);
               x = (int)((float)this.readShort(is) * this.scaleXY);
               lenText = this.readShort(is);
               offset = (int)((float)this.readShort(is) * this.scaleXY);
               rec = new Rectangle2D.Float((float)offset, (float)lenText, (float)(x - offset), (float)(y - lenText));
               this.paint(brushObject, penObject, rec);
               break;
            case 2368:
               is.readInt();
               this.readShort(is);
               this.readShort(is);
               this.readShort(is);
               heightDst = (float)this.readShort(is) * (float)this.inch / PIXEL_PER_INCH * this.getVpHFactor();
               size = (float)this.readShort(is) * (float)this.inch / PIXEL_PER_INCH * this.getVpWFactor() * this.scaleXY;
               dy = (float)this.inch / PIXEL_PER_INCH * this.getVpHFactor() * (float)this.readShort(is);
               dx = (float)this.inch / PIXEL_PER_INCH * this.getVpWFactor() * (float)this.readShort(is) * this.scaleXY;
               this.resizeImageBounds((int)dx, (int)dy);
               this.resizeImageBounds((int)(dx + size), (int)(dy + heightDst));
               break;
            case 2610:
               y = this.readShort(is);
               var10000 = (int)((float)this.readShort(is) * this.scaleXY);
               lenText = this.readShort(is);
               offset = this.readShort(is);
               read = 4;
               boolean clipped = false;
               int x1 = false;
               int y1 = false;
               int x2 = false;
               int y2 = false;
               if ((offset & 4) != 0) {
                  var10000 = (int)((float)this.readShort(is) * this.scaleXY);
                  this.readShort(is);
                  var10000 = (int)((float)this.readShort(is) * this.scaleXY);
                  this.readShort(is);
                  read += 4;
                  clipped = true;
               }

               byte[] bstr = new byte[lenText];

               for(int i = 0; i < lenText; ++i) {
                  bstr[i] = is.readByte();
               }

               String sr = WMFUtilities.decodeString(this.wf, bstr);
               read += (lenText + 1) / 2;
               if (lenText % 2 != 0) {
                  is.readByte();
               }

               if (read < recSize) {
                  for(style = read; style < recSize; ++style) {
                     this.readShort(is);
                  }
               }

               TextLayout layout = new TextLayout(sr, this.wf.font, fontCtx);
               lfWidth = (int)layout.getBounds().getWidth();
               x = (int)layout.getBounds().getX();
               int lfHeight = (int)this.getVerticalAlignmentValue(layout, this.currentVertAlign);
               this.resizeBounds(x, y);
               this.resizeBounds(x + lfWidth, y + lfHeight);
               this.firstEffectivePaint = false;
               break;
            case 2881:
               is.readInt();
               this.readShort(is);
               this.readShort(is);
               this.readShort(is);
               this.readShort(is);
               heightDst = (float)this.readShort(is);
               size = (float)this.readShort(is) * this.scaleXY;
               dy = (float)this.readShort(is) * this.getVpWFactor() * (float)this.inch / PIXEL_PER_INCH;
               dx = (float)this.readShort(is) * this.getVpWFactor() * (float)this.inch / PIXEL_PER_INCH * this.scaleXY;
               size = size * this.getVpWFactor() * (float)this.inch / PIXEL_PER_INCH;
               heightDst = heightDst * this.getVpHFactor() * (float)this.inch / PIXEL_PER_INCH;
               this.resizeImageBounds((int)dx, (int)dy);
               this.resizeImageBounds((int)(dx + size), (int)(dy + heightDst));
               read = 2 * recSize - 20;
               j = 0;

               while(true) {
                  if (j >= read) {
                     continue label296;
                  }

                  is.readByte();
                  ++j;
               }
            case 3907:
               is.readInt();
               this.readShort(is);
               this.readShort(is);
               this.readShort(is);
               this.readShort(is);
               this.readShort(is);
               heightDst = (float)this.readShort(is);
               size = (float)this.readShort(is) * this.scaleXY;
               dy = (float)this.readShort(is) * this.getVpHFactor() * (float)this.inch / PIXEL_PER_INCH;
               dx = (float)this.readShort(is) * this.getVpHFactor() * (float)this.inch / PIXEL_PER_INCH * this.scaleXY;
               size = size * this.getVpWFactor() * (float)this.inch / PIXEL_PER_INCH;
               heightDst = heightDst * this.getVpHFactor() * (float)this.inch / PIXEL_PER_INCH;
               this.resizeImageBounds((int)dx, (int)dy);
               this.resizeImageBounds((int)(dx + size), (int)(dy + heightDst));
               read = 2 * recSize - 22;
               byte[] bitmap = new byte[read];
               x = 0;

               while(true) {
                  if (x >= read) {
                     continue label296;
                  }

                  bitmap[x] = is.readByte();
                  ++x;
               }
            default:
               for(y = 0; y < recSize; ++y) {
                  this.readShort(is);
               }
         }
      }

      if (!this.isAldus) {
         this.width = this.vpW;
         this.height = this.vpH;
         this.right = this.vpX;
         this.left = this.vpX + this.vpW;
         this.top = this.vpY;
         this.bottom = this.vpY + this.vpH;
      }

      this.resetBounds();
      return true;
   }

   public int getWidthBoundsPixels() {
      return this._bwidth;
   }

   public int getHeightBoundsPixels() {
      return this._bheight;
   }

   public int getWidthBoundsUnits() {
      return (int)((float)this.inch * (float)this._bwidth / PIXEL_PER_INCH);
   }

   public int getHeightBoundsUnits() {
      return (int)((float)this.inch * (float)this._bheight / PIXEL_PER_INCH);
   }

   public int getXOffset() {
      return this._bleft;
   }

   public int getYOffset() {
      return this._btop;
   }

   private void resetBounds() {
      this.scale = (float)this.getWidthPixels() / (float)this.vpW;
      if (this._bright != -1) {
         this._bright = (int)(this.scale * (float)(this.vpX + this._bright));
         this._bleft = (int)(this.scale * (float)(this.vpX + this._bleft));
         this._bbottom = (int)(this.scale * (float)(this.vpY + this._bbottom));
         this._btop = (int)(this.scale * (float)(this.vpY + this._btop));
      }

      if (this._iright != -1) {
         this._iright = (int)((float)this._iright * (float)this.getWidthPixels() / (float)this.width);
         this._ileft = (int)((float)this._ileft * (float)this.getWidthPixels() / (float)this.width);
         this._ibottom = (int)((float)this._ibottom * (float)this.getWidthPixels() / (float)this.width);
         this._itop = (int)((float)this._itop * (float)this.getWidthPixels() / (float)this.width);
         if (this._bright == -1 || this._iright > this._bright) {
            this._bright = this._iright;
         }

         if (this._bleft == -1 || this._ileft < this._bleft) {
            this._bleft = this._ileft;
         }

         if (this._btop == -1 || this._itop < this._btop) {
            this._btop = this._itop;
         }

         if (this._bbottom == -1 || this._ibottom > this._bbottom) {
            this._bbottom = this._ibottom;
         }
      }

      if (this._bleft != -1 && this._bright != -1) {
         this._bwidth = this._bright - this._bleft;
      }

      if (this._btop != -1 && this._bbottom != -1) {
         this._bheight = this._bbottom - this._btop;
      }

   }

   private void resizeBounds(int x, int y) {
      if (this._bleft == -1) {
         this._bleft = x;
      } else if (x < this._bleft) {
         this._bleft = x;
      }

      if (this._bright == -1) {
         this._bright = x;
      } else if (x > this._bright) {
         this._bright = x;
      }

      if (this._btop == -1) {
         this._btop = y;
      } else if (y < this._btop) {
         this._btop = y;
      }

      if (this._bbottom == -1) {
         this._bbottom = y;
      } else if (y > this._bbottom) {
         this._bbottom = y;
      }

   }

   private void resizeImageBounds(int x, int y) {
      if (this._ileft == -1) {
         this._ileft = x;
      } else if (x < this._ileft) {
         this._ileft = x;
      }

      if (this._iright == -1) {
         this._iright = x;
      } else if (x > this._iright) {
         this._iright = x;
      }

      if (this._itop == -1) {
         this._itop = y;
      } else if (y < this._itop) {
         this._itop = y;
      }

      if (this._ibottom == -1) {
         this._ibottom = y;
      } else if (y > this._ibottom) {
         this._ibottom = y;
      }

   }

   private Color getColorFromObject(int brushObject) {
      Color color = null;
      if (brushObject >= 0) {
         GdiObject gdiObj = this.getObject(brushObject);
         return (Color)gdiObj.obj;
      } else {
         return null;
      }
   }

   private void paint(int brushObject, int penObject, Shape shape) {
      if (brushObject >= 0 || penObject >= 0) {
         Color col;
         if (brushObject >= 0) {
            col = this.getColorFromObject(brushObject);
         } else {
            col = this.getColorFromObject(penObject);
         }

         if (!this.firstEffectivePaint || !col.equals(Color.white)) {
            Rectangle rec = shape.getBounds();
            this.resizeBounds((int)rec.getMinX(), (int)rec.getMinY());
            this.resizeBounds((int)rec.getMaxX(), (int)rec.getMaxY());
            this.firstEffectivePaint = false;
         }
      }

   }

   private void paintWithPen(int penObject, Shape shape) {
      if (penObject >= 0) {
         Color col = this.getColorFromObject(penObject);
         if (!this.firstEffectivePaint || !col.equals(Color.white)) {
            Rectangle rec = shape.getBounds();
            this.resizeBounds((int)rec.getMinX(), (int)rec.getMinY());
            this.resizeBounds((int)rec.getMaxX(), (int)rec.getMaxY());
            this.firstEffectivePaint = false;
         }
      }

   }

   private float getVerticalAlignmentValue(TextLayout layout, int vertAlign) {
      if (vertAlign == 24) {
         return -layout.getAscent();
      } else {
         return vertAlign == 0 ? layout.getAscent() + layout.getDescent() : 0.0F;
      }
   }
}
