package org.apache.batik.transcoder.wmf.tosvg;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

/** @deprecated */
public class RecordStore {
   private transient URL url;
   protected transient int numRecords;
   protected transient int numObjects;
   public transient int lastObjectIdx;
   protected transient int vpX;
   protected transient int vpY;
   protected transient int vpW;
   protected transient int vpH;
   protected transient Vector records;
   protected transient Vector objectVector;
   protected transient boolean bReading = false;

   public RecordStore() {
      this.reset();
   }

   public void reset() {
      this.numRecords = 0;
      this.vpX = 0;
      this.vpY = 0;
      this.vpW = 1000;
      this.vpH = 1000;
      this.numObjects = 0;
      this.records = new Vector(20, 20);
      this.objectVector = new Vector();
   }

   synchronized void setReading(boolean state) {
      this.bReading = state;
   }

   synchronized boolean isReading() {
      return this.bReading;
   }

   public boolean read(DataInputStream is) throws IOException {
      this.setReading(true);
      this.reset();
      int functionId = 0;
      this.numRecords = 0;
      this.numObjects = is.readShort();
      this.objectVector.ensureCapacity(this.numObjects);

      for(int i = 0; i < this.numObjects; ++i) {
         this.objectVector.add(new GdiObject(i, false));
      }

      while(functionId != -1) {
         functionId = is.readShort();
         if (functionId == -1) {
            break;
         }

         short numPts;
         Object mr;
         switch (functionId) {
            case 763:
            case 1313:
            case 1583:
            case 2610:
               numPts = is.readShort();
               byte[] b = new byte[numPts];

               for(int i = 0; i < numPts; ++i) {
                  b[i] = is.readByte();
               }

               String str = new String(b);
               mr = new MetaRecord.StringRecord(str);
               break;
            default:
               mr = new MetaRecord();
         }

         numPts = is.readShort();
         ((MetaRecord)mr).numPoints = numPts;
         ((MetaRecord)mr).functionId = functionId;

         for(int j = 0; j < numPts; ++j) {
            ((MetaRecord)mr).AddElement(Integer.valueOf(is.readShort()));
         }

         this.records.add(mr);
         ++this.numRecords;
      }

      this.setReading(false);
      return true;
   }

   public void addObject(int type, Object obj) {
      for(int i = 0; i < this.numObjects; ++i) {
         GdiObject gdi = (GdiObject)this.objectVector.get(i);
         if (!gdi.used) {
            gdi.Setup(type, obj);
            this.lastObjectIdx = i;
            break;
         }
      }

   }

   public void addObjectAt(int type, Object obj, int idx) {
      if (idx != 0 && idx <= this.numObjects) {
         this.lastObjectIdx = idx;

         for(int i = 0; i < this.numObjects; ++i) {
            GdiObject gdi = (GdiObject)this.objectVector.get(i);
            if (i == idx) {
               gdi.Setup(type, obj);
               break;
            }
         }

      } else {
         this.addObject(type, obj);
      }
   }

   public URL getUrl() {
      return this.url;
   }

   public void setUrl(URL newUrl) {
      this.url = newUrl;
   }

   public GdiObject getObject(int idx) {
      return (GdiObject)this.objectVector.get(idx);
   }

   public MetaRecord getRecord(int idx) {
      return (MetaRecord)this.records.get(idx);
   }

   public int getNumRecords() {
      return this.numRecords;
   }

   public int getNumObjects() {
      return this.numObjects;
   }

   public int getVpX() {
      return this.vpX;
   }

   public int getVpY() {
      return this.vpY;
   }

   public int getVpW() {
      return this.vpW;
   }

   public int getVpH() {
      return this.vpH;
   }

   public void setVpX(int newValue) {
      this.vpX = newValue;
   }

   public void setVpY(int newValue) {
      this.vpY = newValue;
   }

   public void setVpW(int newValue) {
      this.vpW = newValue;
   }

   public void setVpH(int newValue) {
      this.vpH = newValue;
   }
}
