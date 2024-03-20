package org.apache.fop.pdf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PDFGState extends PDFObject {
   public static final String GSTATE_LINE_WIDTH = "LW";
   public static final String GSTATE_LINE_CAP = "LC";
   public static final String GSTATE_LINE_JOIN = "LJ";
   public static final String GSTATE_MITER_LIMIT = "ML";
   public static final String GSTATE_DASH_PATTERN = "D";
   public static final String GSTATE_RENDERING_INTENT = "RI";
   public static final String GSTATE_OVERPRINT_STROKE = "OP";
   public static final String GSTATE_OVERPRINT_FILL = "op";
   public static final String GSTATE_OVERPRINT_MODE = "OPM";
   public static final String GSTATE_FONT = "Font";
   public static final String GSTATE_BLACK_GENERATION = "BG";
   public static final String GSTATE_BLACK_GENERATION2 = "BG2";
   public static final String GSTATE_UNDERCOLOR_REMOVAL = "UCR";
   public static final String GSTATE_UNDERCOLOR_REMOVAL2 = "UCR2";
   public static final String GSTATE_TRANSFER_FUNCTION = "TR";
   public static final String GSTATE_TRANSFER_FUNCTION2 = "TR2";
   public static final String GSTATE_HALFTONE_DICT = "HT";
   public static final String GSTATE_HALFTONE_PHASE = "HTP";
   public static final String GSTATE_FLATNESS = "FL";
   public static final String GSTATE_SMOOTHNESS = "SM";
   public static final String GSTATE_STRIKE_ADJ = "SA";
   public static final String GSTATE_BLEND_MODE = "BM";
   public static final String GSTATE_SOFT_MASK = "SMask";
   public static final String GSTATE_ALPHA_STROKE = "CA";
   public static final String GSTATE_ALPHA_NONSTROKE = "ca";
   public static final String GSTATE_ALPHA_SOURCE_FLAG = "AIS";
   public static final String GSTATE_TEXT_KNOCKOUT = "TK";
   public static final PDFGState DEFAULT = new PDFGState();
   private Map values = new HashMap();
   private int objNum;

   public String getName() {
      if (this.objNum == 0) {
         this.objNum = ++this.getDocument().gStateObjectCount;
      }

      return "GS" + this.objNum;
   }

   public void setAlpha(float val, boolean fill) {
      if (fill) {
         this.values.put("ca", val);
      } else {
         this.values.put("CA", val);
      }

   }

   public void addValues(PDFGState state) {
      this.values.putAll(state.values);
   }

   public void addValues(Map vals) {
      this.values.putAll(vals);
   }

   public String toPDFString() {
      StringBuffer sb = new StringBuffer(64);
      sb.append("<<\n/Type /ExtGState\n");
      this.appendVal(sb, "ca");
      this.appendVal(sb, "CA");
      sb.append(">>");
      return sb.toString();
   }

   private void appendVal(StringBuffer sb, String name) {
      Object val = this.values.get(name);
      if (val != null) {
         sb.append("/" + name + " " + val + "\n");
      }

   }

   protected boolean contentEquals(PDFObject obj) {
      if (obj == this) {
         return true;
      } else if (!(obj instanceof PDFGState)) {
         return false;
      } else {
         Map vals1 = this.values;
         Map vals2 = ((PDFGState)obj).values;
         if (vals1.size() != vals2.size()) {
            return false;
         } else {
            Iterator var4 = vals2.entrySet().iterator();

            Map.Entry e;
            Object obj1;
            do {
               if (!var4.hasNext()) {
                  return true;
               }

               e = (Map.Entry)var4.next();
               Object str = e.getKey();
               obj1 = vals1.get(str);
            } while(obj1.equals(e.getValue()));

            return false;
         }
      }
   }

   static {
      Map vals = DEFAULT.values;
      vals.put("CA", 1.0F);
      vals.put("ca", 1.0F);
   }
}
