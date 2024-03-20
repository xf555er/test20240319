package org.apache.fop.render.rtf.rtflib.rtfdoc;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

public final class RtfColorTable {
   private static final int RED = 16;
   private static final int GREEN = 8;
   private static final int BLUE = 0;
   private static RtfColorTable instance = new RtfColorTable();
   private Hashtable colorIndex = new Hashtable();
   private Vector colorTable = new Vector();
   private Hashtable namedColors = new Hashtable();

   private RtfColorTable() {
      this.init();
   }

   public static RtfColorTable getInstance() {
      return instance;
   }

   private void init() {
      this.addNamedColor("black", this.getColorNumber(0, 0, 0));
      this.addNamedColor("white", this.getColorNumber(255, 255, 255));
      this.addNamedColor("red", this.getColorNumber(255, 0, 0));
      this.addNamedColor("green", this.getColorNumber(0, 255, 0));
      this.addNamedColor("blue", this.getColorNumber(0, 0, 255));
      this.addNamedColor("cyan", this.getColorNumber(0, 255, 255));
      this.addNamedColor("magenta", this.getColorNumber(255, 0, 255));
      this.addNamedColor("yellow", this.getColorNumber(255, 255, 0));
      this.getColorNumber(0, 0, 128);
      this.getColorNumber(0, 128, 128);
      this.getColorNumber(0, 128, 0);
      this.getColorNumber(128, 0, 128);
      this.getColorNumber(128, 0, 0);
      this.getColorNumber(128, 128, 0);
      this.getColorNumber(128, 128, 128);
      this.addNamedColor("gray", this.getColorNumber(128, 128, 128));
      this.getColorNumber(192, 192, 192);
   }

   private void addNamedColor(String name, int colorNumber) {
      this.namedColors.put(name.toLowerCase(), colorNumber);
   }

   public Integer getColorNumber(String name) {
      return (Integer)this.namedColors.get(name.toLowerCase());
   }

   public Integer getColorNumber(int red, int green, int blue) {
      Integer identifier = this.determineIdentifier(red, green, blue);
      Object o = this.colorIndex.get(identifier);
      int retVal;
      if (o == null) {
         this.addColor(identifier);
         retVal = this.colorTable.size();
      } else {
         retVal = (Integer)o + 1;
      }

      return retVal;
   }

   public void writeColors(RtfHeader header) throws IOException {
      if (this.colorTable != null && this.colorTable.size() != 0) {
         header.newLine();
         header.writeGroupMark(true);
         header.write("\\colortbl;");
         int len = this.colorTable.size();
         Iterator var3 = this.colorTable.iterator();

         while(var3.hasNext()) {
            Object aColorTable = var3.next();
            int identifier = (Integer)aColorTable;
            header.newLine();
            header.write("\\red" + this.determineColorLevel(identifier, 16));
            header.write("\\green" + this.determineColorLevel(identifier, 8));
            header.write("\\blue" + this.determineColorLevel(identifier, 0) + ";");
         }

         header.newLine();
         header.writeGroupMark(false);
      }
   }

   private void addColor(Integer i) {
      this.colorIndex.put(i, this.colorTable.size());
      this.colorTable.addElement(i);
   }

   private int determineIdentifier(int red, int green, int blue) {
      int c = red << 16;
      c += green << 8;
      c += blue << 0;
      return c;
   }

   private int determineColorLevel(int identifier, int color) {
      int retVal = (byte)(identifier >> color);
      return retVal < 0 ? retVal + 256 : retVal;
   }
}
