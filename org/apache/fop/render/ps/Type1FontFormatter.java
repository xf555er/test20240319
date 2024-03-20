package org.apache.fop.render.ps;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import org.apache.fontbox.cff.CFFCIDFont;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFType1Font;
import org.apache.fontbox.cff.DataOutput;
import org.apache.fontbox.cff.Type1FontUtil;

public final class Type1FontFormatter {
   private Map gids;

   public Type1FontFormatter(Map gids) {
      this.gids = gids;
   }

   public byte[] format(CFFFont font, String i) throws IOException {
      DataOutput output = new DataOutput();
      this.printFont(font, output, i);
      return output.getBytes();
   }

   private void printFont(CFFFont font, DataOutput output, String iStr) throws IOException {
      output.println("%!FontType1-1.0 " + font.getName() + iStr + " " + font.getTopDict().get("version"));
      this.printFontDictionary(font, output, iStr);

      for(int i = 0; i < 8; ++i) {
         StringBuilder sb = new StringBuilder();

         for(int j = 0; j < 64; ++j) {
            sb.append("0");
         }

         output.println(sb.toString());
      }

      output.println("cleartomark");
   }

   private void printFontDictionary(CFFFont font, DataOutput output, String iStr) throws IOException {
      output.println("10 dict begin");
      output.println("/FontInfo 10 dict dup begin");
      output.println("/version (" + font.getTopDict().get("version") + ") readonly def");
      output.println("/Notice (" + font.getTopDict().get("Notice") + ") readonly def");
      output.println("/FullName (" + font.getTopDict().get("FullName") + ") readonly def");
      output.println("/FamilyName (" + font.getTopDict().get("FamilyName") + ") readonly def");
      output.println("/Weight (" + font.getTopDict().get("Weight") + ") readonly def");
      output.println("/ItalicAngle " + font.getTopDict().get("ItalicAngle") + " def");
      output.println("/isFixedPitch " + font.getTopDict().get("isFixedPitch") + " def");
      output.println("/UnderlinePosition " + font.getTopDict().get("UnderlinePosition") + " def");
      output.println("/UnderlineThickness " + font.getTopDict().get("UnderlineThickness") + " def");
      output.println("end readonly def");
      output.println("/FontName /" + font.getName() + iStr + " def");
      output.println("/PaintType " + font.getTopDict().get("PaintType") + " def");
      output.println("/FontType 1 def");
      NumberFormat matrixFormat = new DecimalFormat("0.########", new DecimalFormatSymbols(Locale.US));
      output.println("/FontMatrix " + formatArray(font.getTopDict().get("FontMatrix"), matrixFormat, false) + " readonly def");
      output.println("/FontBBox " + formatArray(font.getTopDict().get("FontBBox"), false) + " readonly def");
      output.println("/StrokeWidth " + font.getTopDict().get("StrokeWidth") + " def");
      int max = 0;
      StringBuilder sb = new StringBuilder();

      Map.Entry gid;
      for(Iterator var7 = this.gids.entrySet().iterator(); var7.hasNext(); max = Math.max(max, (Integer)gid.getValue())) {
         gid = (Map.Entry)var7.next();
         String name = "gid_" + gid.getKey();
         if ((Integer)gid.getKey() == 0) {
            name = ".notdef";
         }

         if (font instanceof CFFType1Font) {
            name = font.getCharset().getNameForGID((Integer)gid.getKey());
         }

         sb.append(String.format("dup %d /%s put", gid.getValue(), name)).append('\n');
      }

      output.println("/Encoding " + (max + 1) + " array");
      output.println("0 1 " + max + " {1 index exch /.notdef put} for");
      output.print(sb.toString());
      output.println("readonly def");
      output.println("currentdict end");
      DataOutput eexecOutput = new DataOutput();
      this.printEexecFontDictionary(font, eexecOutput);
      output.println("currentfile eexec");
      byte[] eexecBytes = Type1FontUtil.eexecEncrypt(eexecOutput.getBytes());
      output.write(eexecBytes);
   }

   private void printEexecFontDictionary(CFFFont font, DataOutput output) throws IOException {
      output.println("dup /Private 15 dict dup begin");
      output.println("/RD {string currentfile exch readstring pop} executeonly def");
      output.println("/ND {noaccess def} executeonly def");
      output.println("/NP {noaccess put} executeonly def");
      Map privDict;
      if (font instanceof CFFCIDFont) {
         privDict = (Map)((CFFCIDFont)font).getPrivDicts().get(0);
      } else {
         privDict = ((CFFType1Font)font).getPrivateDict();
      }

      output.println("/BlueValues " + formatArray(privDict.get("BlueValues"), true) + " ND");
      output.println("/OtherBlues " + formatArray(privDict.get("OtherBlues"), true) + " ND");
      output.println("/BlueScale " + privDict.get("BlueScale") + " def");
      output.println("/BlueShift " + privDict.get("BlueShift") + " def");
      output.println("/BlueFuzz " + privDict.get("BlueFuzz") + " def");
      output.println("/StdHW " + formatArray(privDict.get("StdHW"), true) + " ND");
      output.println("/StdVW " + formatArray(privDict.get("StdVW"), true) + " ND");
      output.println("/ForceBold " + privDict.get("ForceBold") + " def");
      output.println("/MinFeature {16 16} def");
      output.println("/password 5839 def");
      output.println("2 index /CharStrings " + this.gids.size() + " dict dup begin");
      Type1CharStringFormatter formatter = new Type1CharStringFormatter();
      Iterator var5 = this.gids.keySet().iterator();

      while(var5.hasNext()) {
         int gid = (Integer)var5.next();
         String mapping = "gid_" + gid;
         if (gid == 0) {
            mapping = ".notdef";
         }

         byte[] type1Bytes;
         if (font instanceof CFFCIDFont) {
            int cid = font.getCharset().getCIDForGID(gid);
            type1Bytes = formatter.format(((CFFCIDFont)font).getType2CharString(cid).getType1Sequence());
         } else {
            mapping = font.getCharset().getNameForGID(gid);
            type1Bytes = formatter.format(((CFFType1Font)font).getType1CharString(mapping).getType1Sequence());
         }

         byte[] charstringBytes = Type1FontUtil.charstringEncrypt(type1Bytes, 4);
         output.print("/" + mapping + " " + charstringBytes.length + " RD ");
         output.write(charstringBytes);
         output.print(" ND");
         output.println();
      }

      output.println("end");
      output.println("end");
      output.println("readonly put");
      output.println("noaccess put");
      output.println("dup /FontName get exch definefont pop");
      output.println("mark currentfile closefile");
   }

   private static String formatArray(Object object, boolean executable) {
      return formatArray(object, (NumberFormat)null, executable);
   }

   private static String formatArray(Object object, NumberFormat format, boolean executable) {
      StringBuffer sb = new StringBuffer();
      sb.append(executable ? "{" : "[");
      if (object instanceof Collection) {
         String sep = "";
         Collection elements = (Collection)object;

         for(Iterator var6 = elements.iterator(); var6.hasNext(); sep = " ") {
            Object element = var6.next();
            sb.append(sep).append(formatElement(element, format));
         }
      } else if (object instanceof Number) {
         sb.append(formatElement(object, format));
      }

      sb.append(executable ? "}" : "]");
      return sb.toString();
   }

   private static String formatElement(Object object, NumberFormat format) {
      if (format != null) {
         Number number;
         if (object instanceof Double || object instanceof Float) {
            number = (Number)object;
            return format.format(number.doubleValue());
         }

         if (object instanceof Long || object instanceof Integer) {
            number = (Number)object;
            return format.format(number.longValue());
         }
      }

      return String.valueOf(object);
   }
}
