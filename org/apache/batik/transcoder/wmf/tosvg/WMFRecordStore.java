package org.apache.batik.transcoder.wmf.tosvg;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WMFRecordStore extends AbstractWMFReader {
   private URL url;
   protected int numRecords;
   protected float vpX;
   protected float vpY;
   protected List records;
   private boolean _bext = true;

   public WMFRecordStore() {
      this.reset();
   }

   public void reset() {
      this.numRecords = 0;
      this.vpX = 0.0F;
      this.vpY = 0.0F;
      this.vpW = 1000;
      this.vpH = 1000;
      this.scaleX = 1.0F;
      this.scaleY = 1.0F;
      this.scaleXY = 1.0F;
      this.inch = 84;
      this.records = new ArrayList(20);
   }

   protected boolean readRecords(DataInputStream is) throws IOException {
      short functionId = 1;
      int recSize = false;

      for(this.numRecords = 0; functionId > 0; ++this.numRecords) {
         int recSize = this.readInt(is);
         recSize -= 3;
         functionId = this.readShort(is);
         if (functionId <= 0) {
            break;
         }

         MetaRecord mr = new MetaRecord();
         int yVal;
         int xVal;
         int heightSrc;
         int yVal;
         int read;
         int nPoints;
         int x1;
         int y1;
         int x2;
         int y2;
         int i;
         byte[] bstr;
         int i;
         int j;
         MetaRecord.ByteRecord mr;
         short sy;
         short lenText;
         short count;
         short flag;
         byte[] bstr;
         short height;
         switch (functionId) {
            case 258:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               count = this.readShort(is);
               ((MetaRecord)mr).addElement(count);
               if (recSize > 1) {
                  for(xVal = 1; xVal < recSize; ++xVal) {
                     this.readShort(is);
                  }
               }

               this.records.add(mr);
               break;
            case 259:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               count = this.readShort(is);
               if (count == 8) {
                  this.isotropic = false;
               }

               ((MetaRecord)mr).addElement(count);
               this.records.add(mr);
               break;
            case 260:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               if (recSize == 1) {
                  yVal = this.readShort(is);
               } else {
                  yVal = this.readInt(is);
               }

               ((MetaRecord)mr).addElement(yVal);
               this.records.add(mr);
               break;
            case 262:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               count = this.readShort(is);
               if (recSize > 1) {
                  for(xVal = 1; xVal < recSize; ++xVal) {
                     this.readShort(is);
                  }
               }

               ((MetaRecord)mr).addElement(count);
               this.records.add(mr);
               break;
            case 302:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               count = this.readShort(is);
               if (recSize > 1) {
                  for(xVal = 1; xVal < recSize; ++xVal) {
                     this.readShort(is);
                  }
               }

               ((MetaRecord)mr).addElement(count);
               this.records.add(mr);
               break;
            case 322:
               yVal = is.readInt() & 255;
               xVal = 2 * recSize - 4;
               bstr = new byte[xVal];

               for(yVal = 0; yVal < xVal; ++yVal) {
                  bstr[yVal] = is.readByte();
               }

               mr = new MetaRecord.ByteRecord(bstr);
               mr.numPoints = recSize;
               mr.functionId = functionId;
               mr.addElement(yVal);
               this.records.add(mr);
               break;
            case 513:
            case 521:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               yVal = this.readInt(is);
               xVal = yVal & 255;
               heightSrc = (yVal & '\uff00') >> 8;
               yVal = (yVal & 16711680) >> 16;
               read = (yVal & 50331648) >> 24;
               ((MetaRecord)mr).addElement(xVal);
               ((MetaRecord)mr).addElement(heightSrc);
               ((MetaRecord)mr).addElement(yVal);
               this.records.add(mr);
               break;
            case 523:
            case 524:
            case 525:
            case 526:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               yVal = this.readShort(is);
               xVal = this.readShort(is);
               if (xVal < 0) {
                  xVal = -xVal;
                  this.xSign = -1;
               }

               if (yVal < 0) {
                  yVal = -yVal;
                  this.ySign = -1;
               }

               if (this._bext && functionId == 524) {
                  this.vpW = xVal;
                  this.vpH = yVal;
                  this._bext = false;
               }

               if (!this.isAldus) {
                  this.width = this.vpW;
                  this.height = this.vpH;
               }

               ((MetaRecord)mr).addElement((int)((float)xVal * this.scaleXY));
               ((MetaRecord)mr).addElement(yVal);
               this.records.add(mr);
               break;
            case 527:
            case 529:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               yVal = this.readShort(is) * this.ySign;
               xVal = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               ((MetaRecord)mr).addElement(xVal);
               ((MetaRecord)mr).addElement(yVal);
               this.records.add(mr);
               break;
            case 531:
            case 532:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               yVal = this.readShort(is) * this.ySign;
               xVal = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               ((MetaRecord)mr).addElement(xVal);
               ((MetaRecord)mr).addElement(yVal);
               this.records.add(mr);
               break;
            case 762:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               ((MetaRecord)mr).addElement(this.readShort(is));
               yVal = this.readInt(is);
               xVal = this.readInt(is);
               if (recSize == 6) {
                  this.readShort(is);
               }

               heightSrc = xVal & 255;
               yVal = (xVal & '\uff00') >> 8;
               read = (xVal & 16711680) >> 16;
               nPoints = (xVal & 50331648) >> 24;
               ((MetaRecord)mr).addElement(heightSrc);
               ((MetaRecord)mr).addElement(yVal);
               ((MetaRecord)mr).addElement(read);
               ((MetaRecord)mr).addElement(yVal);
               this.records.add(mr);
               break;
            case 763:
               count = this.readShort(is);
               this.readShort(is);
               lenText = this.readShort(is);
               flag = this.readShort(is);
               height = this.readShort(is);
               int lfItalic = is.readByte();
               int lfUnderline = is.readByte();
               int lfStrikeOut = is.readByte();
               x2 = is.readByte() & 255;
               int lfOutPrecision = is.readByte();
               int lfClipPrecision = is.readByte();
               int lfQuality = is.readByte();
               int lfPitchAndFamily = is.readByte();
               j = 2 * (recSize - 9);
               byte[] lfFaceName = new byte[j];

               for(int i = 0; i < j; ++i) {
                  lfFaceName[i] = is.readByte();
               }

               String str = new String(lfFaceName);
               MetaRecord mr = new MetaRecord.StringRecord(str);
               mr.numPoints = recSize;
               mr.functionId = functionId;
               mr.addElement(count);
               mr.addElement(lfItalic);
               mr.addElement(height);
               mr.addElement(x2);
               mr.addElement(lfUnderline);
               mr.addElement(lfStrikeOut);
               mr.addElement(flag);
               mr.addElement(lenText);
               this.records.add(mr);
               break;
            case 764:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               ((MetaRecord)mr).addElement(this.readShort(is));
               yVal = this.readInt(is);
               xVal = yVal & 255;
               heightSrc = (yVal & '\uff00') >> 8;
               yVal = (yVal & 16711680) >> 16;
               read = (yVal & 50331648) >> 24;
               ((MetaRecord)mr).addElement(xVal);
               ((MetaRecord)mr).addElement(heightSrc);
               ((MetaRecord)mr).addElement(yVal);
               ((MetaRecord)mr).addElement(this.readShort(is));
               this.records.add(mr);
               break;
            case 804:
            case 805:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               count = this.readShort(is);
               ((MetaRecord)mr).addElement(count);

               for(xVal = 0; xVal < count; ++xVal) {
                  ((MetaRecord)mr).addElement((int)((float)(this.readShort(is) * this.xSign) * this.scaleXY));
                  ((MetaRecord)mr).addElement(this.readShort(is) * this.ySign);
               }

               this.records.add(mr);
               break;
            case 1040:
            case 1042:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               count = this.readShort(is);
               sy = this.readShort(is);
               lenText = this.readShort(is);
               flag = this.readShort(is);
               ((MetaRecord)mr).addElement(lenText);
               ((MetaRecord)mr).addElement(count);
               ((MetaRecord)mr).addElement(flag);
               ((MetaRecord)mr).addElement(sy);
               this.records.add(mr);
               this.scaleX = this.scaleX * (float)lenText / (float)flag;
               this.scaleY = this.scaleY * (float)count / (float)sy;
               break;
            case 1046:
            case 1048:
            case 1051:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               yVal = this.readShort(is) * this.ySign;
               xVal = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               heightSrc = this.readShort(is) * this.ySign;
               yVal = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               ((MetaRecord)mr).addElement(yVal);
               ((MetaRecord)mr).addElement(heightSrc);
               ((MetaRecord)mr).addElement(xVal);
               ((MetaRecord)mr).addElement(yVal);
               this.records.add(mr);
               break;
            case 1313:
               count = this.readShort(is);
               int read = 1;
               bstr = new byte[count];

               for(yVal = 0; yVal < count; ++yVal) {
                  bstr[yVal] = is.readByte();
               }

               if (count % 2 != 0) {
                  is.readByte();
               }

               xVal = read + (count + 1) / 2;
               yVal = this.readShort(is) * this.ySign;
               read = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               xVal += 2;
               if (xVal < recSize) {
                  for(nPoints = xVal; nPoints < recSize; ++nPoints) {
                     this.readShort(is);
                  }
               }

               mr = new MetaRecord.ByteRecord(bstr);
               mr.numPoints = recSize;
               mr.functionId = functionId;
               mr.addElement(read);
               mr.addElement(yVal);
               this.records.add(mr);
               break;
            case 1336:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               count = this.readShort(is);
               int[] pts = new int[count];
               heightSrc = 0;

               for(yVal = 0; yVal < count; ++yVal) {
                  pts[yVal] = this.readShort(is);
                  heightSrc += pts[yVal];
               }

               ((MetaRecord)mr).addElement(count);

               for(yVal = 0; yVal < count; ++yVal) {
                  ((MetaRecord)mr).addElement(pts[yVal]);
               }

               yVal = count + 1;

               for(read = 0; read < count; ++read) {
                  nPoints = pts[read];

                  for(x1 = 0; x1 < nPoints; ++x1) {
                     ((MetaRecord)mr).addElement((int)((float)(this.readShort(is) * this.xSign) * this.scaleXY));
                     ((MetaRecord)mr).addElement(this.readShort(is) * this.ySign);
                  }
               }

               this.records.add(mr);
               break;
            case 1564:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               yVal = this.readShort(is) * this.ySign;
               xVal = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               heightSrc = this.readShort(is) * this.ySign;
               yVal = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               read = this.readShort(is) * this.ySign;
               nPoints = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               ((MetaRecord)mr).addElement(nPoints);
               ((MetaRecord)mr).addElement(read);
               ((MetaRecord)mr).addElement(yVal);
               ((MetaRecord)mr).addElement(heightSrc);
               ((MetaRecord)mr).addElement(xVal);
               ((MetaRecord)mr).addElement(yVal);
               this.records.add(mr);
               break;
            case 1565:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               yVal = this.readInt(is);
               xVal = this.readShort(is) * this.ySign;
               heightSrc = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               yVal = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               read = this.readShort(is) * this.ySign;
               ((MetaRecord)mr).addElement(yVal);
               ((MetaRecord)mr).addElement(xVal);
               ((MetaRecord)mr).addElement(heightSrc);
               ((MetaRecord)mr).addElement(read);
               ((MetaRecord)mr).addElement(yVal);
               this.records.add(mr);
               break;
            case 1583:
               for(yVal = 0; yVal < recSize; ++yVal) {
                  this.readShort(is);
               }

               --this.numRecords;
               break;
            case 1791:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               yVal = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               xVal = this.readShort(is) * this.ySign;
               heightSrc = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               yVal = this.readShort(is) * this.ySign;
               ((MetaRecord)mr).addElement(yVal);
               ((MetaRecord)mr).addElement(xVal);
               ((MetaRecord)mr).addElement(heightSrc);
               ((MetaRecord)mr).addElement(yVal);
               this.records.add(mr);
               break;
            case 2071:
            case 2074:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;
               yVal = this.readShort(is) * this.ySign;
               xVal = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               heightSrc = this.readShort(is) * this.ySign;
               yVal = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               read = this.readShort(is) * this.ySign;
               nPoints = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               x1 = this.readShort(is) * this.ySign;
               y1 = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               ((MetaRecord)mr).addElement(y1);
               ((MetaRecord)mr).addElement(x1);
               ((MetaRecord)mr).addElement(nPoints);
               ((MetaRecord)mr).addElement(read);
               ((MetaRecord)mr).addElement(yVal);
               ((MetaRecord)mr).addElement(heightSrc);
               ((MetaRecord)mr).addElement(xVal);
               ((MetaRecord)mr).addElement(yVal);
               this.records.add(mr);
               break;
            case 2368:
               yVal = is.readInt() & 255;
               sy = this.readShort(is);
               lenText = this.readShort(is);
               this.readShort(is);
               height = this.readShort(is);
               nPoints = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               int dy = this.readShort(is);
               y1 = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               x2 = 2 * recSize - 18;
               if (x2 <= 0) {
                  ((MetaRecord)mr).numPoints = recSize;
                  ((MetaRecord)mr).functionId = functionId;

                  for(y2 = 0; y2 < x2; ++y2) {
                     is.readByte();
                  }
               } else {
                  byte[] bitmap = new byte[x2];

                  for(i = 0; i < x2; ++i) {
                     bitmap[i] = is.readByte();
                  }

                  mr = new MetaRecord.ByteRecord(bitmap);
                  ((MetaRecord)mr).numPoints = recSize;
                  ((MetaRecord)mr).functionId = functionId;
               }

               ((MetaRecord)mr).addElement(yVal);
               ((MetaRecord)mr).addElement(height);
               ((MetaRecord)mr).addElement(nPoints);
               ((MetaRecord)mr).addElement(sy);
               ((MetaRecord)mr).addElement(lenText);
               ((MetaRecord)mr).addElement(dy);
               ((MetaRecord)mr).addElement(y1);
               this.records.add(mr);
               break;
            case 2610:
               yVal = this.readShort(is) * this.ySign;
               xVal = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               lenText = this.readShort(is);
               flag = this.readShort(is);
               read = 4;
               boolean clipped = false;
               x1 = 0;
               y1 = 0;
               x2 = 0;
               y2 = 0;
               if ((flag & 4) != 0) {
                  x1 = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
                  y1 = this.readShort(is) * this.ySign;
                  x2 = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
                  y2 = this.readShort(is) * this.ySign;
                  read += 4;
                  clipped = true;
               }

               bstr = new byte[lenText];

               for(i = 0; i < lenText; ++i) {
                  bstr[i] = is.readByte();
               }

               read += (lenText + 1) / 2;
               if (lenText % 2 != 0) {
                  is.readByte();
               }

               if (read < recSize) {
                  for(j = read; j < recSize; ++j) {
                     this.readShort(is);
                  }
               }

               mr = new MetaRecord.ByteRecord(bstr);
               mr.numPoints = recSize;
               mr.functionId = functionId;
               mr.addElement(xVal);
               mr.addElement(yVal);
               mr.addElement(flag);
               if (clipped) {
                  mr.addElement(x1);
                  mr.addElement(y1);
                  mr.addElement(x2);
                  mr.addElement(y2);
               }

               this.records.add(mr);
               break;
            case 2881:
               yVal = is.readInt() & 255;
               xVal = this.readShort(is) * this.ySign;
               heightSrc = this.readShort(is) * this.xSign;
               yVal = this.readShort(is) * this.ySign;
               read = this.readShort(is) * this.xSign;
               nPoints = this.readShort(is) * this.ySign;
               x1 = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               y1 = this.readShort(is) * this.ySign;
               x2 = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               y2 = 2 * recSize - 20;
               byte[] bitmap = new byte[y2];

               for(int i = 0; i < y2; ++i) {
                  bitmap[i] = is.readByte();
               }

               mr = new MetaRecord.ByteRecord(bitmap);
               mr.numPoints = recSize;
               mr.functionId = functionId;
               mr.addElement(yVal);
               mr.addElement(xVal);
               mr.addElement(heightSrc);
               mr.addElement(yVal);
               mr.addElement(read);
               mr.addElement(nPoints);
               mr.addElement(x1);
               mr.addElement(y1);
               mr.addElement(x2);
               this.records.add(mr);
               break;
            case 3907:
               yVal = is.readInt() & 255;
               this.readShort(is);
               heightSrc = this.readShort(is) * this.ySign;
               yVal = this.readShort(is) * this.xSign;
               read = this.readShort(is) * this.ySign;
               nPoints = this.readShort(is) * this.xSign;
               x1 = this.readShort(is) * this.ySign;
               y1 = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               x2 = this.readShort(is) * this.ySign;
               y2 = (int)((float)(this.readShort(is) * this.xSign) * this.scaleXY);
               i = 2 * recSize - 22;
               bstr = new byte[i];

               for(i = 0; i < i; ++i) {
                  bstr[i] = is.readByte();
               }

               mr = new MetaRecord.ByteRecord(bstr);
               mr.numPoints = recSize;
               mr.functionId = functionId;
               mr.addElement(yVal);
               mr.addElement(heightSrc);
               mr.addElement(yVal);
               mr.addElement(read);
               mr.addElement(nPoints);
               mr.addElement(x1);
               mr.addElement(y1);
               mr.addElement(x2);
               mr.addElement(y2);
               this.records.add(mr);
               break;
            default:
               ((MetaRecord)mr).numPoints = recSize;
               ((MetaRecord)mr).functionId = functionId;

               for(yVal = 0; yVal < recSize; ++yVal) {
                  ((MetaRecord)mr).addElement(this.readShort(is));
               }

               this.records.add(mr);
         }
      }

      if (!this.isAldus) {
         this.right = (int)this.vpX;
         this.left = (int)(this.vpX + (float)this.vpW);
         this.top = (int)this.vpY;
         this.bottom = (int)(this.vpY + (float)this.vpH);
      }

      this.setReading(false);
      return true;
   }

   public URL getUrl() {
      return this.url;
   }

   public void setUrl(URL newUrl) {
      this.url = newUrl;
   }

   public MetaRecord getRecord(int idx) {
      return (MetaRecord)this.records.get(idx);
   }

   public int getNumRecords() {
      return this.numRecords;
   }

   public float getVpX() {
      return this.vpX;
   }

   public float getVpY() {
      return this.vpY;
   }

   public void setVpX(float newValue) {
      this.vpX = newValue;
   }

   public void setVpY(float newValue) {
      this.vpY = newValue;
   }
}
