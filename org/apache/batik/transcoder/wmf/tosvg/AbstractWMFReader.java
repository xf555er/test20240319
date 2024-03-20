package org.apache.batik.transcoder.wmf.tosvg;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.batik.util.Platform;

public abstract class AbstractWMFReader {
   public static final float PIXEL_PER_INCH = (float)Platform.getScreenResolution();
   public static final float MM_PER_PIXEL = 25.4F / (float)Platform.getScreenResolution();
   protected int left;
   protected int right;
   protected int top;
   protected int bottom;
   protected int width;
   protected int height;
   protected int inch;
   protected float scaleX;
   protected float scaleY;
   protected float scaleXY;
   protected int vpW;
   protected int vpH;
   protected int vpX;
   protected int vpY;
   protected int xSign;
   protected int ySign;
   protected volatile boolean bReading;
   protected boolean isAldus;
   protected boolean isotropic;
   protected int mtType;
   protected int mtHeaderSize;
   protected int mtVersion;
   protected int mtSize;
   protected int mtNoObjects;
   protected int mtMaxRecord;
   protected int mtNoParameters;
   protected int windowWidth;
   protected int windowHeight;
   protected int numObjects;
   protected List objectVector;
   public int lastObjectIdx;

   public AbstractWMFReader() {
      this.xSign = 1;
      this.ySign = 1;
      this.bReading = false;
      this.isAldus = false;
      this.isotropic = true;
      this.scaleX = 1.0F;
      this.scaleY = 1.0F;
      this.scaleXY = 1.0F;
      this.left = -1;
      this.top = -1;
      this.width = -1;
      this.height = -1;
      this.right = this.left + this.width;
      this.bottom = this.top + this.height;
      this.numObjects = 0;
      this.objectVector = new ArrayList();
   }

   public AbstractWMFReader(int width, int height) {
      this();
      this.width = width;
      this.height = height;
   }

   protected short readShort(DataInputStream is) throws IOException {
      byte[] js = new byte[2];
      is.readFully(js);
      int iTemp = (255 & js[1]) << 8;
      short i = (short)('\uffff' & iTemp);
      i = (short)(i | 255 & js[0]);
      return i;
   }

   protected int readInt(DataInputStream is) throws IOException {
      byte[] js = new byte[4];
      is.readFully(js);
      int i = (255 & js[3]) << 24;
      i |= (255 & js[2]) << 16;
      i |= (255 & js[1]) << 8;
      i |= 255 & js[0];
      return i;
   }

   public float getViewportWidthUnits() {
      return (float)this.vpW;
   }

   public float getViewportHeightUnits() {
      return (float)this.vpH;
   }

   public float getViewportWidthInch() {
      return (float)this.vpW / (float)this.inch;
   }

   public float getViewportHeightInch() {
      return (float)this.vpH / (float)this.inch;
   }

   public float getPixelsPerUnit() {
      return PIXEL_PER_INCH / (float)this.inch;
   }

   public int getVpW() {
      return (int)(PIXEL_PER_INCH * (float)this.vpW / (float)this.inch);
   }

   public int getVpH() {
      return (int)(PIXEL_PER_INCH * (float)this.vpH / (float)this.inch);
   }

   public int getLeftUnits() {
      return this.left;
   }

   public int getRightUnits() {
      return this.right;
   }

   public int getTopUnits() {
      return this.top;
   }

   public int getWidthUnits() {
      return this.width;
   }

   public int getHeightUnits() {
      return this.height;
   }

   public int getBottomUnits() {
      return this.bottom;
   }

   public int getMetaFileUnitsPerInch() {
      return this.inch;
   }

   public Rectangle getRectangleUnits() {
      Rectangle rec = new Rectangle(this.left, this.top, this.width, this.height);
      return rec;
   }

   public Rectangle2D getRectanglePixel() {
      float _left = PIXEL_PER_INCH * (float)this.left / (float)this.inch;
      float _right = PIXEL_PER_INCH * (float)this.right / (float)this.inch;
      float _top = PIXEL_PER_INCH * (float)this.top / (float)this.inch;
      float _bottom = PIXEL_PER_INCH * (float)this.bottom / (float)this.inch;
      Rectangle2D.Float rec = new Rectangle2D.Float(_left, _top, _right - _left, _bottom - _top);
      return rec;
   }

   public Rectangle2D getRectangleInch() {
      float _left = (float)this.left / (float)this.inch;
      float _right = (float)this.right / (float)this.inch;
      float _top = (float)this.top / (float)this.inch;
      float _bottom = (float)this.bottom / (float)this.inch;
      Rectangle2D.Float rec = new Rectangle2D.Float(_left, _top, _right - _left, _bottom - _top);
      return rec;
   }

   public int getWidthPixels() {
      return (int)(PIXEL_PER_INCH * (float)this.width / (float)this.inch);
   }

   public float getUnitsToPixels() {
      return PIXEL_PER_INCH / (float)this.inch;
   }

   public float getVpWFactor() {
      return PIXEL_PER_INCH * (float)this.width / (float)this.inch / (float)this.vpW;
   }

   public float getVpHFactor() {
      return PIXEL_PER_INCH * (float)this.height / (float)this.inch / (float)this.vpH;
   }

   public int getHeightPixels() {
      return (int)(PIXEL_PER_INCH * (float)this.height / (float)this.inch);
   }

   public int getXSign() {
      return this.xSign;
   }

   public int getYSign() {
      return this.ySign;
   }

   protected synchronized void setReading(boolean state) {
      this.bReading = state;
   }

   public synchronized boolean isReading() {
      return this.bReading;
   }

   public abstract void reset();

   protected abstract boolean readRecords(DataInputStream var1) throws IOException;

   public void read(DataInputStream is) throws IOException {
      this.reset();
      this.setReading(true);
      int dwIsAldus = this.readInt(is);
      if (dwIsAldus == -1698247209) {
         this.isAldus = true;
         this.readShort(is);
         this.left = this.readShort(is);
         this.top = this.readShort(is);
         this.right = this.readShort(is);
         this.bottom = this.readShort(is);
         this.inch = this.readShort(is);
         this.readInt(is);
         this.readShort(is);
         int _i;
         if (this.left > this.right) {
            _i = this.right;
            this.right = this.left;
            this.left = _i;
            this.xSign = -1;
         }

         if (this.top > this.bottom) {
            _i = this.bottom;
            this.bottom = this.top;
            this.top = _i;
            this.ySign = -1;
         }

         this.width = this.right - this.left;
         this.height = this.bottom - this.top;
         this.mtType = this.readShort(is);
         this.mtHeaderSize = this.readShort(is);
      } else {
         this.mtType = dwIsAldus << 16 >> 16;
         this.mtHeaderSize = dwIsAldus >> 16;
      }

      this.mtVersion = this.readShort(is);
      this.mtSize = this.readInt(is);
      this.mtNoObjects = this.readShort(is);
      this.mtMaxRecord = this.readInt(is);
      this.mtNoParameters = this.readShort(is);
      this.numObjects = this.mtNoObjects;
      List tempList = new ArrayList(this.numObjects);

      for(int i = 0; i < this.numObjects; ++i) {
         tempList.add(new GdiObject(i, false));
      }

      this.objectVector.addAll(tempList);
      boolean ret = this.readRecords(is);
      is.close();
      if (!ret) {
         throw new IOException("Unhandled exception while reading records");
      }
   }

   public int addObject(int type, Object obj) {
      int startIdx = 0;

      for(int i = startIdx; i < this.numObjects; ++i) {
         GdiObject gdi = (GdiObject)this.objectVector.get(i);
         if (!gdi.used) {
            gdi.Setup(type, obj);
            this.lastObjectIdx = i;
            break;
         }
      }

      return this.lastObjectIdx;
   }

   public int addObjectAt(int type, Object obj, int idx) {
      if (idx != 0 && idx <= this.numObjects) {
         this.lastObjectIdx = idx;

         for(int i = 0; i < this.numObjects; ++i) {
            GdiObject gdi = (GdiObject)this.objectVector.get(i);
            if (i == idx) {
               gdi.Setup(type, obj);
               break;
            }
         }

         return idx;
      } else {
         this.addObject(type, obj);
         return this.lastObjectIdx;
      }
   }

   public GdiObject getObject(int idx) {
      return (GdiObject)this.objectVector.get(idx);
   }

   public int getNumObjects() {
      return this.numObjects;
   }
}
