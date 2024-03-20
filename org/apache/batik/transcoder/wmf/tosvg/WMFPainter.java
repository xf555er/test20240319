package org.apache.batik.transcoder.wmf.tosvg;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedInputStream;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import org.apache.batik.ext.awt.geom.Polygon2D;
import org.apache.batik.ext.awt.geom.Polyline2D;
import org.apache.batik.util.Platform;

public class WMFPainter extends AbstractWMFPainter {
   private static final int INPUT_BUFFER_SIZE = 30720;
   private static final Integer INTEGER_0 = 0;
   private float scale;
   private float scaleX;
   private float scaleY;
   private float conv;
   private float xOffset;
   private float yOffset;
   private float vpX;
   private float vpY;
   private float vpW;
   private float vpH;
   private Color frgdColor;
   private Color bkgdColor;
   private boolean opaque;
   private transient boolean firstEffectivePaint;
   private static BasicStroke solid = new BasicStroke(1.0F, 0, 1);
   private static BasicStroke textSolid = new BasicStroke(1.0F, 0, 1);
   private transient ImageObserver observer;
   private transient BufferedInputStream bufStream;

   public WMFPainter(WMFRecordStore currentStore, float scale) {
      this(currentStore, 0, 0, scale);
   }

   public WMFPainter(WMFRecordStore currentStore, int xOffset, int yOffset, float scale) {
      this.opaque = false;
      this.firstEffectivePaint = true;
      this.observer = new ImageObserver() {
         public boolean imageUpdate(Image img, int flags, int x, int y, int width, int height) {
            return false;
         }
      };
      this.bufStream = null;
      this.setRecordStore(currentStore);
      TextureFactory.getInstance().reset();
      this.conv = scale;
      this.xOffset = (float)(-xOffset);
      this.yOffset = (float)(-yOffset);
      this.scale = (float)currentStore.getWidthPixels() / (float)currentStore.getWidthUnits() * scale;
      this.scale = this.scale * (float)currentStore.getWidthPixels() / (float)currentStore.getVpW();
      float xfactor = (float)currentStore.getVpW() / (float)currentStore.getWidthPixels() * (float)currentStore.getWidthUnits() / (float)currentStore.getWidthPixels();
      float yfactor = (float)currentStore.getVpH() / (float)currentStore.getHeightPixels() * (float)currentStore.getHeightUnits() / (float)currentStore.getHeightPixels();
      this.xOffset *= xfactor;
      this.yOffset *= yfactor;
      this.scaleX = this.scale;
      this.scaleY = this.scale;
   }

   public void paint(Graphics g) {
      float fontHeight = 10.0F;
      float fontAngle = 0.0F;
      float penWidth = 0.0F;
      float startX = 0.0F;
      float startY = 0.0F;
      int brushObject = true;
      int penObject = true;
      int fontObject = true;
      Font font = null;
      Stack dcStack = new Stack();
      int numRecords = this.currentStore.getNumRecords();
      int numObjects = this.currentStore.getNumObjects();
      this.vpX = this.currentStore.getVpX() * this.scale;
      this.vpY = this.currentStore.getVpY() * this.scale;
      this.vpW = (float)this.currentStore.getVpW() * this.scale;
      this.vpH = (float)this.currentStore.getVpH() * this.scale;
      if (!this.currentStore.isReading()) {
         g.setPaintMode();
         Graphics2D g2d = (Graphics2D)g;
         g2d.setStroke(solid);
         int brushObject = -1;
         int penObject = -1;
         int fontObject = -1;
         this.frgdColor = null;
         this.bkgdColor = Color.white;

         GdiObject gdiObj;
         for(int i = 0; i < numObjects; ++i) {
            gdiObj = this.currentStore.getObject(i);
            gdiObj.clear();
         }

         float w = this.vpW;
         float h = this.vpH;
         g2d.setColor(Color.black);

         for(int iRec = 0; iRec < numRecords; ++iRec) {
            MetaRecord mr = this.currentStore.getRecord(iRec);
            int gdiIndex;
            int numPolygons;
            int charset;
            float width;
            float left;
            int count;
            int d;
            float cp1X;
            byte[] bstr;
            float size;
            String sr;
            float height;
            int offset;
            int weight;
            float top;
            byte objIndex;
            float[] _xpts;
            FontRenderContext frc;
            int style;
            float dy;
            float dx;
            float[] _ypts;
            Color clr;
            byte[] bitmap;
            TextLayout layout;
            int k;
            BufferedImage img;
            Color oldClr;
            int underline;
            int strikeOut;
            int orient;
            int escape;
            switch (mr.functionId) {
               case 0:
               case 524:
                  this.vpW = (float)mr.elementAt(0);
                  this.vpH = (float)mr.elementAt(1);
                  this.scaleX = this.scale;
                  this.scaleY = this.scale;
                  solid = new BasicStroke(this.scaleX * 2.0F, 0, 1);
                  break;
               case 30:
                  dcStack.push(penWidth);
                  dcStack.push(startX);
                  dcStack.push(startY);
                  dcStack.push(brushObject);
                  dcStack.push(penObject);
                  dcStack.push(fontObject);
                  dcStack.push(this.frgdColor);
                  dcStack.push(this.bkgdColor);
               case 53:
               case 55:
               case 259:
               case 261:
               case 262:
               case 263:
               case 264:
               case 298:
               case 299:
               case 300:
               case 313:
               case 522:
               case 525:
               case 526:
               case 527:
               case 529:
               case 544:
               case 552:
               case 561:
               case 564:
               case 1040:
               case 1042:
               case 1045:
               case 1046:
               case 1049:
               case 1055:
               case 1065:
               case 1078:
               case 1574:
               case 2338:
               case 2851:
               case 3379:
               default:
                  break;
               case 247:
                  numPolygons = this.addObjectAt(this.currentStore, 8, INTEGER_0, 0);
                  break;
               case 248:
               case 505:
               case 765:
               case 1790:
               case 1791:
                  numPolygons = this.addObjectAt(this.currentStore, 6, INTEGER_0, 0);
                  break;
               case 258:
                  numPolygons = mr.elementAt(0);
                  this.opaque = numPolygons == 2;
                  break;
               case 260:
                  size = (float)mr.ElementAt(0);
                  Paint paint = null;
                  boolean ok = false;
                  if (size == 66.0F) {
                     paint = Color.black;
                     ok = true;
                  } else if (size == 1.6711778E7F) {
                     paint = Color.white;
                     ok = true;
                  } else if (size == 1.5728673E7F && brushObject >= 0) {
                     paint = this.getStoredPaint(this.currentStore, brushObject);
                     ok = true;
                  }

                  if (ok) {
                     if (paint != null) {
                        g2d.setPaint((Paint)paint);
                     } else {
                        this.setBrushPaint(this.currentStore, g2d, brushObject);
                     }
                  }
                  break;
               case 295:
                  this.bkgdColor = (Color)dcStack.pop();
                  this.frgdColor = (Color)dcStack.pop();
                  fontObject = (Integer)((Integer)dcStack.pop());
                  penObject = (Integer)((Integer)dcStack.pop());
                  brushObject = (Integer)((Integer)dcStack.pop());
                  startY = (Float)((Float)dcStack.pop());
                  startX = (Float)((Float)dcStack.pop());
                  penWidth = (Float)((Float)dcStack.pop());
                  break;
               case 301:
                  gdiIndex = mr.elementAt(0);
                  if ((gdiIndex & Integer.MIN_VALUE) == 0) {
                     if (gdiIndex >= numObjects) {
                        gdiIndex -= numObjects;
                        switch (gdiIndex) {
                           case 0:
                           case 1:
                           case 2:
                           case 3:
                           case 4:
                           case 6:
                           case 7:
                           case 9:
                           case 10:
                           case 11:
                           case 12:
                           case 13:
                           case 14:
                           case 15:
                           case 16:
                           default:
                              break;
                           case 5:
                              brushObject = -1;
                              break;
                           case 8:
                              penObject = -1;
                        }
                     } else {
                        gdiObj = this.currentStore.getObject(gdiIndex);
                        if (gdiObj.used) {
                           switch (gdiObj.type) {
                              case 1:
                                 g2d.setColor((Color)gdiObj.obj);
                                 penObject = gdiIndex;
                                 break;
                              case 2:
                                 if (gdiObj.obj instanceof Color) {
                                    g2d.setColor((Color)gdiObj.obj);
                                 } else if (gdiObj.obj instanceof Paint) {
                                    g2d.setPaint((Paint)gdiObj.obj);
                                 } else {
                                    g2d.setPaint(this.getPaint((byte[])((byte[])gdiObj.obj)));
                                 }

                                 brushObject = gdiIndex;
                                 break;
                              case 3:
                                 this.wmfFont = (WMFFont)gdiObj.obj;
                                 Font f = this.wmfFont.font;
                                 g2d.setFont(f);
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
                  }
                  break;
               case 302:
                  this.currentHorizAlign = WMFUtilities.getHorizontalAlignment(mr.elementAt(0));
                  this.currentVertAlign = WMFUtilities.getVerticalAlignment(mr.elementAt(0));
                  break;
               case 322:
                  objIndex = 0;
                  byte[] bitmap = ((MetaRecord.ByteRecord)mr).bstr;
                  this.addObjectAt(this.currentStore, 2, bitmap, objIndex);
                  break;
               case 496:
                  gdiIndex = mr.elementAt(0);
                  gdiObj = this.currentStore.getObject(gdiIndex);
                  if (gdiIndex == brushObject) {
                     brushObject = -1;
                  } else if (gdiIndex == penObject) {
                     penObject = -1;
                  } else if (gdiIndex == fontObject) {
                     fontObject = -1;
                  }

                  gdiObj.clear();
                  break;
               case 513:
                  this.bkgdColor = new Color(mr.elementAt(0), mr.elementAt(1), mr.elementAt(2));
                  g2d.setColor(this.bkgdColor);
                  break;
               case 521:
                  this.frgdColor = new Color(mr.elementAt(0), mr.elementAt(1), mr.elementAt(2));
                  g2d.setColor(this.frgdColor);
                  break;
               case 523:
                  this.currentStore.setVpX(this.vpX = (float)(-mr.elementAt(0)));
                  this.currentStore.setVpY(this.vpY = (float)(-mr.elementAt(1)));
                  this.vpX *= this.scale;
                  this.vpY *= this.scale;
                  break;
               case 531:
                  size = this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(0));
                  height = this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(1));
                  Line2D.Float line = new Line2D.Float(startX, startY, size, height);
                  this.paintWithPen(penObject, line, g2d);
                  startX = size;
                  startY = height;
                  break;
               case 532:
                  startX = this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(0));
                  startY = this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(1));
                  break;
               case 762:
                  objIndex = 0;
                  charset = mr.elementAt(0);
                  if (charset == 5) {
                     clr = Color.white;
                     this.addObjectAt(this.currentStore, 4, clr, objIndex);
                  } else {
                     penWidth = (float)mr.elementAt(4);
                     this.setStroke(g2d, charset, penWidth, this.scaleX);
                     clr = new Color(mr.elementAt(1), mr.elementAt(2), mr.elementAt(3));
                     this.addObjectAt(this.currentStore, 1, clr, objIndex);
                  }
                  break;
               case 763:
                  size = (float)((int)(this.scaleY * (float)mr.elementAt(0)));
                  charset = mr.elementAt(3);
                  offset = mr.elementAt(1);
                  weight = mr.elementAt(2);
                  style = offset > 0 ? 2 : 0;
                  style |= weight > 400 ? 1 : 0;
                  String face = ((MetaRecord.StringRecord)mr).text;

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
                  underline = mr.elementAt(4);
                  strikeOut = mr.elementAt(5);
                  orient = mr.elementAt(6);
                  escape = mr.elementAt(7);
                  WMFFont wf = new WMFFont(f, charset, underline, strikeOut, offset, weight, orient, escape);
                  this.addObjectAt(this.currentStore, 3, wf, objIndex);
                  break;
               case 764:
                  objIndex = 0;
                  charset = mr.elementAt(0);
                  clr = new Color(mr.elementAt(1), mr.elementAt(2), mr.elementAt(3));
                  if (charset == 0) {
                     this.addObjectAt(this.currentStore, 2, clr, objIndex);
                  } else if (charset == 2) {
                     weight = mr.elementAt(4);
                     Paint paint;
                     if (!this.opaque) {
                        paint = TextureFactory.getInstance().getTexture(weight, clr);
                     } else {
                        paint = TextureFactory.getInstance().getTexture(weight, clr, this.bkgdColor);
                     }

                     if (paint != null) {
                        this.addObjectAt(this.currentStore, 2, paint, objIndex);
                     } else {
                        clr = Color.black;
                        this.addObjectAt(this.currentStore, 5, clr, objIndex);
                     }
                  } else {
                     clr = Color.black;
                     this.addObjectAt(this.currentStore, 5, clr, objIndex);
                  }
                  break;
               case 804:
                  numPolygons = mr.elementAt(0);
                  _xpts = new float[numPolygons];
                  _ypts = new float[numPolygons];

                  for(weight = 0; weight < numPolygons; ++weight) {
                     _xpts[weight] = this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(weight * 2 + 1));
                     _ypts[weight] = this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(weight * 2 + 2));
                  }

                  Polygon2D pol = new Polygon2D(_xpts, _ypts, numPolygons);
                  this.paint(brushObject, penObject, pol, g2d);
                  break;
               case 805:
                  numPolygons = mr.elementAt(0);
                  _xpts = new float[numPolygons];
                  _ypts = new float[numPolygons];

                  for(weight = 0; weight < numPolygons; ++weight) {
                     _xpts[weight] = this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(weight * 2 + 1));
                     _ypts[weight] = this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(weight * 2 + 2));
                  }

                  Polyline2D pol = new Polyline2D(_xpts, _ypts, numPolygons);
                  this.paintWithPen(penObject, pol, g2d);
                  break;
               case 1048:
                  size = this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(0));
                  height = this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(2));
                  width = this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(1));
                  left = this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(3));
                  Ellipse2D.Float el = new Ellipse2D.Float(size, width, height - size, left - width);
                  this.paint(brushObject, penObject, el, g2d);
                  break;
               case 1051:
                  size = this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(0));
                  width = this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(2));
                  height = this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(1));
                  left = this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(3));
                  Rectangle2D.Float rec = new Rectangle2D.Float(size, height, width - size, left - height);
                  this.paint(brushObject, penObject, rec, g2d);
                  break;
               case 1313:
               case 1583:
                  try {
                     bstr = ((MetaRecord.ByteRecord)mr).bstr;
                     sr = WMFUtilities.decodeString(this.wmfFont, bstr);
                     width = this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(0));
                     left = this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(1));
                     if (this.frgdColor != null) {
                        g2d.setColor(this.frgdColor);
                     } else {
                        g2d.setColor(Color.black);
                     }

                     frc = g2d.getFontRenderContext();
                     new Point2D.Double(0.0, 0.0);
                     new GeneralPath(1);
                     layout = new TextLayout(sr, g2d.getFont(), frc);
                     this.firstEffectivePaint = false;
                     left += this.getVerticalAlignmentValue(layout, this.currentVertAlign);
                     this.drawString(-1, g2d, this.getCharacterIterator(g2d, sr, this.wmfFont), width, left, layout, this.wmfFont, this.currentHorizAlign);
                  } catch (Exception var49) {
                  }
                  break;
               case 1336:
                  numPolygons = mr.elementAt(0);
                  int[] pts = new int[numPolygons];

                  for(offset = 0; offset < numPolygons; ++offset) {
                     pts[offset] = mr.elementAt(offset + 1);
                  }

                  offset = numPolygons + 1;
                  List v = new ArrayList(numPolygons);

                  for(style = 0; style < numPolygons; ++style) {
                     count = pts[style];
                     float[] xpts = new float[count];
                     float[] ypts = new float[count];

                     for(k = 0; k < count; ++k) {
                        xpts[k] = this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(offset + k * 2));
                        ypts[k] = this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(offset + k * 2 + 1));
                     }

                     offset += count * 2;
                     Polygon2D pol = new Polygon2D(xpts, ypts, count);
                     v.add(pol);
                  }

                  if (brushObject >= 0) {
                     this.setBrushPaint(this.currentStore, g2d, brushObject);
                     this.fillPolyPolygon(g2d, v);
                     this.firstEffectivePaint = false;
                  }

                  if (penObject >= 0) {
                     this.setPenColor(this.currentStore, g2d, penObject);
                     this.drawPolyPolygon(g2d, v);
                     this.firstEffectivePaint = false;
                  }
                  break;
               case 1564:
                  size = this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(0));
                  width = this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(2));
                  top = this.scaleX * (float)mr.elementAt(4);
                  height = this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(1));
                  left = this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(3));
                  dy = this.scaleY * (float)mr.elementAt(5);
                  RoundRectangle2D rec = new RoundRectangle2D.Float(size, height, width - size, left - height, top, dy);
                  this.paint(brushObject, penObject, rec, g2d);
                  break;
               case 1565:
                  size = (float)mr.elementAt(0);
                  height = this.scaleY * (float)mr.elementAt(1);
                  width = this.scaleX * (float)mr.elementAt(2);
                  left = this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(3));
                  top = this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(4));
                  Paint paint = null;
                  boolean ok = false;
                  if (size == 66.0F) {
                     paint = Color.black;
                     ok = true;
                  } else if (size == 1.6711778E7F) {
                     paint = Color.white;
                     ok = true;
                  } else if (size == 1.5728673E7F && brushObject >= 0) {
                     paint = this.getStoredPaint(this.currentStore, brushObject);
                     ok = true;
                  }

                  if (ok) {
                     oldClr = g2d.getColor();
                     if (paint != null) {
                        g2d.setPaint((Paint)paint);
                     } else {
                        this.setBrushPaint(this.currentStore, g2d, brushObject);
                     }

                     Rectangle2D.Float rec = new Rectangle2D.Float(left, top, width, height);
                     g2d.fill(rec);
                     g2d.setColor(oldClr);
                  }
                  break;
               case 2071:
               case 2074:
               case 2096:
                  double left = (double)(this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(0)));
                  double top = (double)(this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(1)));
                  double right = (double)(this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(2)));
                  double bottom = (double)(this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(3)));
                  double xstart = (double)(this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(4)));
                  double ystart = (double)(this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(5)));
                  double xend = (double)(this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(6)));
                  double yend = (double)(this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(7)));
                  this.setBrushPaint(this.currentStore, g2d, brushObject);
                  double cx = left + (right - left) / 2.0;
                  double cy = top + (bottom - top) / 2.0;
                  double startAngle = -Math.toDegrees(Math.atan2(ystart - cy, xstart - cx));
                  double endAngle = -Math.toDegrees(Math.atan2(yend - cy, xend - cx));
                  double extentAngle = endAngle - startAngle;
                  if (extentAngle < 0.0) {
                     extentAngle += 360.0;
                  }

                  if (startAngle < 0.0) {
                     startAngle += 360.0;
                  }

                  Arc2D.Double arc;
                  switch (mr.functionId) {
                     case 2071:
                        arc = new Arc2D.Double(left, top, right - left, bottom - top, startAngle, extentAngle, 0);
                        g2d.draw(arc);
                        break;
                     case 2074:
                        arc = new Arc2D.Double(left, top, right - left, bottom - top, startAngle, extentAngle, 2);
                        this.paint(brushObject, penObject, arc, g2d);
                        break;
                     case 2096:
                        arc = new Arc2D.Double(left, top, right - left, bottom - top, startAngle, extentAngle, 1);
                        this.paint(brushObject, penObject, arc, g2d);
                  }

                  this.firstEffectivePaint = false;
                  break;
               case 2368:
                  numPolygons = mr.ElementAt(0);
                  height = (float)mr.ElementAt(1) * this.conv * this.currentStore.getVpWFactor();
                  width = (float)mr.ElementAt(2) * this.conv * this.currentStore.getVpHFactor();
                  weight = mr.ElementAt(3);
                  style = mr.ElementAt(4);
                  dy = this.conv * this.currentStore.getVpWFactor() * (this.vpY + this.yOffset + (float)mr.ElementAt(5));
                  dx = this.conv * this.currentStore.getVpHFactor() * (this.vpX + this.xOffset + (float)mr.ElementAt(6));
                  if (mr instanceof MetaRecord.ByteRecord) {
                     byte[] bitmap = ((MetaRecord.ByteRecord)mr).bstr;
                     BufferedImage img = this.getImage(bitmap);
                     if (img != null) {
                        underline = img.getWidth();
                        strikeOut = img.getHeight();
                        if (this.opaque) {
                           g2d.drawImage(img, (int)dx, (int)dy, (int)(dx + width), (int)(dy + height), style, weight, style + underline, weight + strikeOut, this.bkgdColor, this.observer);
                        } else {
                           g2d.drawImage(img, (int)dx, (int)dy, (int)(dx + width), (int)(dy + height), style, weight, style + underline, weight + strikeOut, this.observer);
                        }
                     }
                  } else if (this.opaque) {
                     oldClr = g2d.getColor();
                     g2d.setColor(this.bkgdColor);
                     g2d.fill(new Rectangle2D.Float(dx, dy, width, height));
                     g2d.setColor(oldClr);
                  }
                  break;
               case 2610:
                  try {
                     bstr = ((MetaRecord.ByteRecord)mr).bstr;
                     sr = WMFUtilities.decodeString(this.wmfFont, bstr);
                     width = this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(0));
                     left = this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(1));
                     if (this.frgdColor != null) {
                        g2d.setColor(this.frgdColor);
                     } else {
                        g2d.setColor(Color.black);
                     }

                     frc = g2d.getFontRenderContext();
                     new Point2D.Double(0.0, 0.0);
                     new GeneralPath(1);
                     layout = new TextLayout(sr, g2d.getFont(), frc);
                     k = mr.elementAt(2);
                     int x1 = false;
                     int y1 = false;
                     int x2 = false;
                     int y2 = false;
                     boolean clipped = false;
                     Shape clip = null;
                     if ((k & 4) != 0) {
                        clipped = true;
                        underline = mr.elementAt(3);
                        strikeOut = mr.elementAt(4);
                        orient = mr.elementAt(5);
                        escape = mr.elementAt(6);
                        clip = g2d.getClip();
                        g2d.setClip(underline, strikeOut, orient, escape);
                     }

                     this.firstEffectivePaint = false;
                     left += this.getVerticalAlignmentValue(layout, this.currentVertAlign);
                     this.drawString(k, g2d, this.getCharacterIterator(g2d, sr, this.wmfFont, this.currentHorizAlign), width, left, layout, this.wmfFont, this.currentHorizAlign);
                     if (clipped) {
                        g2d.setClip(clip);
                     }
                  } catch (Exception var50) {
                  }
                  break;
               case 2881:
                  numPolygons = mr.elementAt(1);
                  charset = mr.elementAt(2);
                  offset = mr.elementAt(3);
                  weight = mr.elementAt(4);
                  top = this.conv * this.currentStore.getVpWFactor() * (this.vpY + this.yOffset + (float)mr.elementAt(7));
                  dy = this.conv * this.currentStore.getVpHFactor() * (this.vpX + this.xOffset + (float)mr.elementAt(8));
                  dx = (float)mr.elementAt(5);
                  cp1X = (float)mr.elementAt(6);
                  cp1X = cp1X * this.conv * this.currentStore.getVpWFactor();
                  dx = dx * this.conv * this.currentStore.getVpHFactor();
                  bitmap = ((MetaRecord.ByteRecord)mr).bstr;
                  img = this.getImage(bitmap, charset, numPolygons);
                  if (img != null) {
                     g2d.drawImage(img, (int)dy, (int)top, (int)(dy + cp1X), (int)(top + dx), weight, offset, weight + charset, offset + numPolygons, this.bkgdColor, this.observer);
                  }
                  break;
               case 3907:
                  numPolygons = mr.elementAt(1);
                  charset = mr.elementAt(2);
                  offset = mr.elementAt(3);
                  weight = mr.elementAt(4);
                  top = this.conv * this.currentStore.getVpWFactor() * (this.vpY + this.yOffset + (float)mr.elementAt(7));
                  dy = this.conv * this.currentStore.getVpHFactor() * (this.vpX + this.xOffset + (float)mr.elementAt(8));
                  dx = (float)mr.elementAt(5);
                  cp1X = (float)mr.elementAt(6);
                  cp1X = cp1X * this.conv * this.currentStore.getVpWFactor();
                  dx = dx * this.conv * this.currentStore.getVpHFactor();
                  bitmap = ((MetaRecord.ByteRecord)mr).bstr;
                  img = this.getImage(bitmap, charset, numPolygons);
                  if (img != null) {
                     if (this.opaque) {
                        g2d.drawImage(img, (int)dy, (int)top, (int)(dy + cp1X), (int)(top + dx), weight, offset, weight + charset, offset + numPolygons, this.bkgdColor, this.observer);
                     } else {
                        g2d.drawImage(img, (int)dy, (int)top, (int)(dy + cp1X), (int)(top + dx), weight, offset, weight + charset, offset + numPolygons, this.observer);
                     }
                  }
                  break;
               case 4096:
                  try {
                     this.setPenColor(this.currentStore, g2d, penObject);
                     numPolygons = mr.elementAt(0);
                     charset = (numPolygons - 1) / 3;
                     width = this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(1));
                     left = this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(2));
                     GeneralPath gp = new GeneralPath(1);
                     gp.moveTo(width, left);

                     for(count = 0; count < charset; ++count) {
                        d = count * 6;
                        cp1X = this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(d + 3));
                        float cp1Y = this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(d + 4));
                        float cp2X = this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(d + 5));
                        float cp2Y = this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(d + 6));
                        float endX = this.scaleX * (this.vpX + this.xOffset + (float)mr.elementAt(d + 7));
                        float endY = this.scaleY * (this.vpY + this.yOffset + (float)mr.elementAt(d + 8));
                        gp.curveTo(cp1X, cp1Y, cp2X, cp2Y, endX, endY);
                     }

                     g2d.setStroke(solid);
                     g2d.draw(gp);
                     this.firstEffectivePaint = false;
                  } catch (Exception var51) {
                  }
            }
         }
      }

   }

   private Paint getPaint(byte[] bit) {
      Dimension d = this.getImageDimension(bit);
      BufferedImage img = this.getImage(bit);
      Rectangle2D rec = new Rectangle2D.Float(0.0F, 0.0F, (float)d.width, (float)d.height);
      TexturePaint paint = new TexturePaint(img, rec);
      return paint;
   }

   private void drawString(int flag, Graphics2D g2d, AttributedCharacterIterator ati, float x, float y, TextLayout layout, WMFFont wmfFont, int align) {
      if (wmfFont.escape == 0) {
         if (flag != -1) {
            this.fillTextBackground(-1, flag, g2d, x, y, 0.0F, layout);
         }

         float width = (float)layout.getBounds().getWidth();
         if (align == 6) {
            g2d.drawString(ati, x - width / 2.0F, y);
         } else if (align == 2) {
            g2d.drawString(ati, x - width, y);
         } else {
            g2d.drawString(ati, x, y);
         }
      } else {
         AffineTransform tr = g2d.getTransform();
         float angle = -((float)((double)wmfFont.escape * Math.PI / 1800.0));
         float width = (float)layout.getBounds().getWidth();
         float height = (float)layout.getBounds().getHeight();
         if (align == 6) {
            g2d.translate((double)(-width / 2.0F), (double)(height / 2.0F));
            g2d.rotate((double)angle, (double)(x - width / 2.0F), (double)y);
         } else if (align == 2) {
            g2d.translate((double)(-width / 2.0F), (double)(height / 2.0F));
            g2d.rotate((double)angle, (double)(x - width), (double)y);
         } else {
            g2d.translate(0.0, (double)(height / 2.0F));
            g2d.rotate((double)angle, (double)x, (double)y);
         }

         if (flag != -1) {
            this.fillTextBackground(align, flag, g2d, x, y, width, layout);
         }

         Stroke _st = g2d.getStroke();
         g2d.setStroke(textSolid);
         g2d.drawString(ati, x, y);
         g2d.setStroke(_st);
         g2d.setTransform(tr);
      }

   }

   private void fillTextBackground(int align, int flag, Graphics2D g2d, float x, float y, float width, TextLayout layout) {
      float _x = x;
      if (align == 6) {
         _x = x - width / 2.0F;
      } else if (align == 2) {
         _x = x - width;
      }

      Color c;
      AffineTransform tr;
      if ((flag & 2) != 0) {
         c = g2d.getColor();
         tr = g2d.getTransform();
         g2d.setColor(this.bkgdColor);
         g2d.translate((double)_x, (double)y);
         g2d.fill(layout.getBounds());
         g2d.setColor(c);
         g2d.setTransform(tr);
      } else if (this.opaque) {
         c = g2d.getColor();
         tr = g2d.getTransform();
         g2d.setColor(this.bkgdColor);
         g2d.translate((double)_x, (double)y);
         g2d.fill(layout.getBounds());
         g2d.setColor(c);
         g2d.setTransform(tr);
      }

   }

   private void drawPolyPolygon(Graphics2D g2d, List pols) {
      Iterator var3 = pols.iterator();

      while(var3.hasNext()) {
         Object pol1 = var3.next();
         Polygon2D pol = (Polygon2D)((Polygon2D)pol1);
         g2d.draw(pol);
      }

   }

   private void fillPolyPolygon(Graphics2D g2d, List pols) {
      if (pols.size() == 1) {
         g2d.fill((Polygon2D)((Polygon2D)pols.get(0)));
      } else {
         GeneralPath path = new GeneralPath(0);
         Iterator var4 = pols.iterator();

         while(var4.hasNext()) {
            Object pol1 = var4.next();
            Polygon2D pol = (Polygon2D)pol1;
            path.append(pol, false);
         }

         g2d.fill(path);
      }

   }

   private void setStroke(Graphics2D g2d, int penStyle, float penWidth, float scale) {
      float _width;
      if (penWidth == 0.0F) {
         _width = 1.0F;
      } else {
         _width = penWidth;
      }

      float _scale = (float)Platform.getScreenResolution() / (float)this.currentStore.getMetaFileUnitsPerInch();
      float factor = scale / _scale;
      _width = _width * _scale * factor;
      _scale = (float)this.currentStore.getWidthPixels() * 1.0F / 350.0F;
      BasicStroke stroke;
      if (penStyle == 0) {
         stroke = new BasicStroke(_width, 0, 1);
         g2d.setStroke(stroke);
      } else {
         float[] dash;
         if (penStyle == 2) {
            dash = new float[]{1.0F * _scale, 5.0F * _scale};
            stroke = new BasicStroke(_width, 0, 1, 10.0F * _scale, dash, 0.0F);
            g2d.setStroke(stroke);
         } else if (penStyle == 1) {
            dash = new float[]{5.0F * _scale, 2.0F * _scale};
            stroke = new BasicStroke(_width, 0, 1, 10.0F * _scale, dash, 0.0F);
            g2d.setStroke(stroke);
         } else if (penStyle == 3) {
            dash = new float[]{5.0F * _scale, 2.0F * _scale, 1.0F * _scale, 2.0F * _scale};
            stroke = new BasicStroke(_width, 0, 1, 10.0F * _scale, dash, 0.0F);
            g2d.setStroke(stroke);
         } else if (penStyle == 4) {
            dash = new float[]{5.0F * _scale, 2.0F * _scale, 1.0F * _scale, 2.0F * _scale, 1.0F * _scale, 2.0F * _scale};
            stroke = new BasicStroke(_width, 0, 1, 15.0F * _scale, dash, 0.0F);
            g2d.setStroke(stroke);
         } else {
            stroke = new BasicStroke(_width, 0, 1);
            g2d.setStroke(stroke);
         }
      }

   }

   private void setPenColor(WMFRecordStore currentStore, Graphics2D g2d, int penObject) {
      if (penObject >= 0) {
         GdiObject gdiObj = currentStore.getObject(penObject);
         g2d.setColor((Color)gdiObj.obj);
         int penObject = true;
      }

   }

   private int getHorizontalAlignement(int align) {
      int v = align % 24;
      v %= 8;
      if (v >= 6) {
         return 6;
      } else {
         return v >= 2 ? 2 : 0;
      }
   }

   private void setBrushPaint(WMFRecordStore currentStore, Graphics2D g2d, int brushObject) {
      if (brushObject >= 0) {
         GdiObject gdiObj = currentStore.getObject(brushObject);
         if (gdiObj.obj instanceof Color) {
            g2d.setColor((Color)gdiObj.obj);
         } else if (gdiObj.obj instanceof Paint) {
            g2d.setPaint((Paint)gdiObj.obj);
         } else {
            g2d.setPaint(this.getPaint((byte[])((byte[])gdiObj.obj)));
         }

         int brushObject = true;
      }

   }

   private Paint getStoredPaint(WMFRecordStore currentStore, int object) {
      if (object >= 0) {
         GdiObject gdiObj = currentStore.getObject(object);
         return gdiObj.obj instanceof Paint ? (Paint)gdiObj.obj : this.getPaint((byte[])((byte[])gdiObj.obj));
      } else {
         return null;
      }
   }

   private void paint(int brushObject, int penObject, Shape shape, Graphics2D g2d) {
      Paint paint;
      if (brushObject >= 0) {
         paint = this.getStoredPaint(this.currentStore, brushObject);
         if (!this.firstEffectivePaint || !paint.equals(Color.white)) {
            this.setBrushPaint(this.currentStore, g2d, brushObject);
            g2d.fill(shape);
            this.firstEffectivePaint = false;
         }
      }

      if (penObject >= 0) {
         paint = this.getStoredPaint(this.currentStore, penObject);
         if (!this.firstEffectivePaint || !paint.equals(Color.white)) {
            this.setPenColor(this.currentStore, g2d, penObject);
            g2d.draw(shape);
            this.firstEffectivePaint = false;
         }
      }

   }

   private void paintWithPen(int penObject, Shape shape, Graphics2D g2d) {
      if (penObject >= 0) {
         Paint paint = this.getStoredPaint(this.currentStore, penObject);
         if (!this.firstEffectivePaint || !paint.equals(Color.white)) {
            this.setPenColor(this.currentStore, g2d, penObject);
            g2d.draw(shape);
            this.firstEffectivePaint = false;
         }
      }

   }

   private float getVerticalAlignmentValue(TextLayout layout, int vertAlign) {
      if (vertAlign == 8) {
         return -layout.getDescent();
      } else {
         return vertAlign == 0 ? layout.getAscent() : 0.0F;
      }
   }

   public WMFRecordStore getRecordStore() {
      return this.currentStore;
   }
}
