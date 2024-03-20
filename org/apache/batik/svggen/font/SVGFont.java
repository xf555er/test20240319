package org.apache.batik.svggen.font;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import org.apache.batik.constants.XMLConstants;
import org.apache.batik.svggen.font.table.CmapFormat;
import org.apache.batik.svggen.font.table.Feature;
import org.apache.batik.svggen.font.table.FeatureTags;
import org.apache.batik.svggen.font.table.GsubTable;
import org.apache.batik.svggen.font.table.KernSubtable;
import org.apache.batik.svggen.font.table.KernTable;
import org.apache.batik.svggen.font.table.KerningPair;
import org.apache.batik.svggen.font.table.LangSys;
import org.apache.batik.svggen.font.table.PostTable;
import org.apache.batik.svggen.font.table.Script;
import org.apache.batik.svggen.font.table.ScriptTags;
import org.apache.batik.svggen.font.table.SingleSubst;
import org.apache.batik.util.SVGConstants;

public class SVGFont implements XMLConstants, SVGConstants, ScriptTags, FeatureTags {
   static final String EOL;
   static final String PROPERTY_LINE_SEPARATOR = "line.separator";
   static final String PROPERTY_LINE_SEPARATOR_DEFAULT = "\n";
   static final int DEFAULT_FIRST = 32;
   static final int DEFAULT_LAST = 126;
   private static String QUOT_EOL;
   private static String CONFIG_USAGE;
   private static String CONFIG_SVG_BEGIN;
   private static String CONFIG_SVG_TEST_CARD_START;
   private static String CONFIG_SVG_TEST_CARD_END;
   public static final char ARG_KEY_START_CHAR = '-';
   public static final String ARG_KEY_CHAR_RANGE_LOW = "-l";
   public static final String ARG_KEY_CHAR_RANGE_HIGH = "-h";
   public static final String ARG_KEY_ID = "-id";
   public static final String ARG_KEY_ASCII = "-ascii";
   public static final String ARG_KEY_TESTCARD = "-testcard";
   public static final String ARG_KEY_AUTO_RANGE = "-autorange";
   public static final String ARG_KEY_OUTPUT_PATH = "-o";

   protected static String encodeEntities(String s) {
      StringBuffer sb = new StringBuffer();

      for(int i = 0; i < s.length(); ++i) {
         if (s.charAt(i) == '<') {
            sb.append("&lt;");
         } else if (s.charAt(i) == '>') {
            sb.append("&gt;");
         } else if (s.charAt(i) == '&') {
            sb.append("&amp;");
         } else if (s.charAt(i) == '\'') {
            sb.append("&apos;");
         } else if (s.charAt(i) == '"') {
            sb.append("&quot;");
         } else {
            sb.append(s.charAt(i));
         }
      }

      return sb.toString();
   }

   protected static String getContourAsSVGPathData(Glyph glyph, int startIndex, int count) {
      if (glyph.getPoint(startIndex).endOfContour) {
         return "";
      } else {
         StringBuffer sb = new StringBuffer();
         int offset = 0;

         while(offset < count) {
            Point point = glyph.getPoint(startIndex + offset % count);
            Point point_plus1 = glyph.getPoint(startIndex + (offset + 1) % count);
            Point point_plus2 = glyph.getPoint(startIndex + (offset + 2) % count);
            if (offset == 0) {
               sb.append("M").append(String.valueOf(point.x)).append(" ").append(String.valueOf(point.y));
            }

            if (point.onCurve && point_plus1.onCurve) {
               if (point_plus1.x == point.x) {
                  sb.append("V").append(String.valueOf(point_plus1.y));
               } else if (point_plus1.y == point.y) {
                  sb.append("H").append(String.valueOf(point_plus1.x));
               } else {
                  sb.append("L").append(String.valueOf(point_plus1.x)).append(" ").append(String.valueOf(point_plus1.y));
               }

               ++offset;
            } else if (point.onCurve && !point_plus1.onCurve && point_plus2.onCurve) {
               sb.append("Q").append(String.valueOf(point_plus1.x)).append(" ").append(String.valueOf(point_plus1.y)).append(" ").append(String.valueOf(point_plus2.x)).append(" ").append(String.valueOf(point_plus2.y));
               offset += 2;
            } else if (point.onCurve && !point_plus1.onCurve && !point_plus2.onCurve) {
               sb.append("Q").append(String.valueOf(point_plus1.x)).append(" ").append(String.valueOf(point_plus1.y)).append(" ").append(String.valueOf(midValue(point_plus1.x, point_plus2.x))).append(" ").append(String.valueOf(midValue(point_plus1.y, point_plus2.y)));
               offset += 2;
            } else if (!point.onCurve && !point_plus1.onCurve) {
               sb.append("T").append(String.valueOf(midValue(point.x, point_plus1.x))).append(" ").append(String.valueOf(midValue(point.y, point_plus1.y)));
               ++offset;
            } else {
               if (point.onCurve || !point_plus1.onCurve) {
                  System.out.println("drawGlyph case not catered for!!");
                  break;
               }

               sb.append("T").append(String.valueOf(point_plus1.x)).append(" ").append(String.valueOf(point_plus1.y));
               ++offset;
            }
         }

         sb.append("Z");
         return sb.toString();
      }
   }

   protected static String getSVGFontFaceElement(Font font) {
      StringBuffer sb = new StringBuffer();
      String fontFamily = font.getNameTable().getRecord((short)1);
      short unitsPerEm = font.getHeadTable().getUnitsPerEm();
      String panose = font.getOS2Table().getPanose().toString();
      short ascent = font.getHheaTable().getAscender();
      short descent = font.getHheaTable().getDescender();
      int baseline = 0;
      sb.append("<").append("font-face").append(EOL).append("    ").append("font-family").append("=\"").append(fontFamily).append(QUOT_EOL).append("    ").append("units-per-em").append("=\"").append(unitsPerEm).append(QUOT_EOL).append("    ").append("panose-1").append("=\"").append(panose).append(QUOT_EOL).append("    ").append("ascent").append("=\"").append(ascent).append(QUOT_EOL).append("    ").append("descent").append("=\"").append(descent).append(QUOT_EOL).append("    ").append("alphabetic").append("=\"").append(baseline).append('"').append(" />").append(EOL);
      return sb.toString();
   }

   protected static void writeFontAsSVGFragment(PrintStream ps, Font font, String id, int first, int last, boolean autoRange, boolean forceAscii) throws Exception {
      int horiz_advance_x = font.getOS2Table().getAvgCharWidth();
      ps.print("<");
      ps.print("font");
      ps.print(" ");
      if (id != null) {
         ps.print("id");
         ps.print("=\"");
         ps.print(id);
         ps.print('"');
         ps.print(" ");
      }

      ps.print("horiz-adv-x");
      ps.print("=\"");
      ps.print(horiz_advance_x);
      ps.print('"');
      ps.print(" >");
      ps.print(getSVGFontFaceElement(font));
      CmapFormat cmapFmt = null;
      if (forceAscii) {
         cmapFmt = font.getCmapTable().getCmapFormat((short)1, (short)0);
      } else {
         cmapFmt = font.getCmapTable().getCmapFormat((short)3, (short)1);
         if (cmapFmt == null) {
            cmapFmt = font.getCmapTable().getCmapFormat((short)3, (short)0);
         }
      }

      if (cmapFmt == null) {
         throw new Exception("Cannot find a suitable cmap table");
      } else {
         GsubTable gsub = (GsubTable)font.getTable(1196643650);
         SingleSubst initialSubst = null;
         SingleSubst medialSubst = null;
         SingleSubst terminalSubst = null;
         if (gsub != null) {
            Script s = gsub.getScriptList().findScript("arab");
            if (s != null) {
               LangSys ls = s.getDefaultLangSys();
               if (ls != null) {
                  Feature init = gsub.getFeatureList().findFeature(ls, "init");
                  Feature medi = gsub.getFeatureList().findFeature(ls, "medi");
                  Feature fina = gsub.getFeatureList().findFeature(ls, "fina");
                  if (init != null) {
                     initialSubst = (SingleSubst)gsub.getLookupList().getLookup(init, 0).getSubtable(0);
                  }

                  if (medi != null) {
                     medialSubst = (SingleSubst)gsub.getLookupList().getLookup(medi, 0).getSubtable(0);
                  }

                  if (fina != null) {
                     terminalSubst = (SingleSubst)gsub.getLookupList().getLookup(fina, 0).getSubtable(0);
                  }
               }
            }
         }

         ps.println(getGlyphAsSVG(font, font.getGlyph(0), 0, horiz_advance_x, initialSubst, medialSubst, terminalSubst, ""));

         try {
            if (first == -1) {
               if (!autoRange) {
                  first = 32;
               } else {
                  first = cmapFmt.getFirst();
               }
            }

            if (last == -1) {
               if (!autoRange) {
                  last = 126;
               } else {
                  last = cmapFmt.getLast();
               }
            }

            Set glyphSet = new HashSet();

            for(int i = first; i <= last; ++i) {
               int glyphIndex = cmapFmt.mapCharCode(i);
               if (glyphIndex > 0) {
                  glyphSet.add(glyphIndex);
                  ps.println(getGlyphAsSVG(font, font.getGlyph(glyphIndex), glyphIndex, horiz_advance_x, initialSubst, medialSubst, terminalSubst, 32 <= i && i <= 127 ? encodeEntities(String.valueOf((char)i)) : "&#x" + Integer.toHexString(i) + ";"));
               }
            }

            KernTable kern = (KernTable)font.getTable(1801810542);
            if (kern != null) {
               KernSubtable kst = kern.getSubtable(0);
               PostTable post = (PostTable)font.getTable(1886352244);

               for(int i = 0; i < kst.getKerningPairCount(); ++i) {
                  KerningPair kpair = kst.getKerningPair(i);
                  if (glyphSet.contains(kpair.getLeft()) && glyphSet.contains(kpair.getRight())) {
                     ps.println(getKerningPairAsSVG(kpair, post));
                  }
               }
            }
         } catch (Exception var19) {
            System.err.println(var19.getMessage());
         }

         ps.print("</");
         ps.print("font");
         ps.println(">");
      }
   }

   protected static String getGlyphAsSVG(Font font, Glyph glyph, int glyphIndex, int defaultHorizAdvanceX, String attrib, String code) {
      StringBuffer sb = new StringBuffer();
      int firstIndex = 0;
      int count = 0;
      int horiz_advance_x = font.getHmtxTable().getAdvanceWidth(glyphIndex);
      if (glyphIndex == 0) {
         sb.append("<");
         sb.append("missing-glyph");
      } else {
         sb.append("<").append("glyph").append(" ").append("unicode").append("=\"").append(code).append('"');
         String glyphName = font.getPostTable().getGlyphName(glyphIndex);
         if (glyphName != null) {
            sb.append(" ").append("glyph-name").append("=\"").append(glyphName).append('"');
         }
      }

      if (horiz_advance_x != defaultHorizAdvanceX) {
         sb.append(" ").append("horiz-adv-x").append("=\"").append(horiz_advance_x).append('"');
      }

      if (attrib != null) {
         sb.append(attrib);
      }

      if (glyph != null) {
         sb.append(" ").append("d").append("=\"");

         for(int i = 0; i < glyph.getPointCount(); ++i) {
            ++count;
            if (glyph.getPoint(i).endOfContour) {
               sb.append(getContourAsSVGPathData(glyph, firstIndex, count));
               firstIndex = i + 1;
               count = 0;
            }
         }

         sb.append('"');
      }

      sb.append(" />");
      chopUpStringBuffer(sb);
      return sb.toString();
   }

   protected static String getGlyphAsSVG(Font font, Glyph glyph, int glyphIndex, int defaultHorizAdvanceX, SingleSubst arabInitSubst, SingleSubst arabMediSubst, SingleSubst arabTermSubst, String code) {
      StringBuffer sb = new StringBuffer();
      boolean substituted = false;
      int arabInitGlyphIndex = glyphIndex;
      int arabMediGlyphIndex = glyphIndex;
      int arabTermGlyphIndex = glyphIndex;
      if (arabInitSubst != null) {
         arabInitGlyphIndex = arabInitSubst.substitute(glyphIndex);
      }

      if (arabMediSubst != null) {
         arabMediGlyphIndex = arabMediSubst.substitute(glyphIndex);
      }

      if (arabTermSubst != null) {
         arabTermGlyphIndex = arabTermSubst.substitute(glyphIndex);
      }

      if (arabInitGlyphIndex != glyphIndex) {
         sb.append(getGlyphAsSVG(font, font.getGlyph(arabInitGlyphIndex), arabInitGlyphIndex, defaultHorizAdvanceX, " arabic-form=\"initial\"", code));
         sb.append(EOL);
         substituted = true;
      }

      if (arabMediGlyphIndex != glyphIndex) {
         sb.append(getGlyphAsSVG(font, font.getGlyph(arabMediGlyphIndex), arabMediGlyphIndex, defaultHorizAdvanceX, " arabic-form=\"medial\"", code));
         sb.append(EOL);
         substituted = true;
      }

      if (arabTermGlyphIndex != glyphIndex) {
         sb.append(getGlyphAsSVG(font, font.getGlyph(arabTermGlyphIndex), arabTermGlyphIndex, defaultHorizAdvanceX, " arabic-form=\"terminal\"", code));
         sb.append(EOL);
         substituted = true;
      }

      if (substituted) {
         sb.append(getGlyphAsSVG(font, glyph, glyphIndex, defaultHorizAdvanceX, " arabic-form=\"isolated\"", code));
      } else {
         sb.append(getGlyphAsSVG(font, glyph, glyphIndex, defaultHorizAdvanceX, (String)null, code));
      }

      return sb.toString();
   }

   protected static String getKerningPairAsSVG(KerningPair kp, PostTable post) {
      String leftGlyphName = post.getGlyphName(kp.getLeft());
      String rightGlyphName = post.getGlyphName(kp.getRight());
      StringBuffer sb = new StringBuffer();
      sb.append("<").append("hkern").append(" ");
      if (leftGlyphName == null) {
         sb.append("u1").append("=\"");
         sb.append(kp.getLeft());
      } else {
         sb.append("g1").append("=\"");
         sb.append(leftGlyphName);
      }

      sb.append('"').append(" ");
      if (rightGlyphName == null) {
         sb.append("u2").append("=\"");
         sb.append(kp.getRight());
      } else {
         sb.append("g2").append("=\"");
         sb.append(rightGlyphName);
      }

      sb.append('"').append(" ").append("k").append("=\"");
      sb.append(-kp.getValue());
      sb.append('"').append(" />");
      return sb.toString();
   }

   protected static void writeSvgBegin(PrintStream ps) {
      ps.println(Messages.formatMessage(CONFIG_SVG_BEGIN, new Object[]{"-//W3C//DTD SVG 1.0//EN", "http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd"}));
   }

   protected static void writeSvgDefsBegin(PrintStream ps) {
      ps.println("<defs >");
   }

   protected static void writeSvgDefsEnd(PrintStream ps) {
      ps.println("</defs>");
   }

   protected static void writeSvgEnd(PrintStream ps) {
      ps.println("</svg>");
   }

   protected static void writeSvgTestCard(PrintStream ps, String fontFamily) {
      ps.println(Messages.formatMessage(CONFIG_SVG_TEST_CARD_START, (Object[])null));
      ps.println(fontFamily);
      ps.println(Messages.formatMessage(CONFIG_SVG_TEST_CARD_END, (Object[])null));
   }

   public static void main(String[] args) {
      try {
         String path = parseArgs(args, (String)null);
         String low = parseArgs(args, "-l");
         String high = parseArgs(args, "-h");
         String id = parseArgs(args, "-id");
         String ascii = parseArgs(args, "-ascii");
         String testCard = parseArgs(args, "-testcard");
         String outPath = parseArgs(args, "-o");
         String autoRange = parseArgs(args, "-autorange");
         PrintStream ps = null;
         FileOutputStream fos = null;
         if (outPath != null) {
            fos = new FileOutputStream(outPath);
            ps = new PrintStream(fos);
         } else {
            ps = System.out;
         }

         if (path != null) {
            Font font = Font.create(path);
            writeSvgBegin(ps);
            writeSvgDefsBegin(ps);
            writeFontAsSVGFragment(ps, font, id, low != null ? Integer.parseInt(low) : -1, high != null ? Integer.parseInt(high) : -1, autoRange != null, ascii != null);
            writeSvgDefsEnd(ps);
            if (testCard != null) {
               String fontFamily = font.getNameTable().getRecord((short)1);
               writeSvgTestCard(ps, fontFamily);
            }

            writeSvgEnd(ps);
            if (fos != null) {
               fos.close();
            }
         } else {
            usage();
         }
      } catch (Exception var13) {
         var13.printStackTrace();
         System.err.println(var13.getMessage());
         usage();
      }

   }

   private static void chopUpStringBuffer(StringBuffer sb) {
      if (sb.length() >= 256) {
         for(int i = 240; i < sb.length(); ++i) {
            if (sb.charAt(i) == ' ') {
               sb.setCharAt(i, '\n');
               i += 240;
            }
         }

      }
   }

   private static int midValue(int a, int b) {
      return a + (b - a) / 2;
   }

   private static String parseArgs(String[] args, String name) {
      for(int i = 0; i < args.length; ++i) {
         if (name == null) {
            if (args[i].charAt(0) != '-') {
               return args[i];
            }
         } else if (name.equalsIgnoreCase(args[i])) {
            if (i < args.length - 1 && args[i + 1].charAt(0) != '-') {
               return args[i + 1];
            }

            return args[i];
         }
      }

      return null;
   }

   private static void usage() {
      System.err.println(Messages.formatMessage(CONFIG_USAGE, (Object[])null));
   }

   static {
      String temp;
      try {
         temp = System.getProperty("line.separator", "\n");
      } catch (SecurityException var2) {
         temp = "\n";
      }

      EOL = temp;
      QUOT_EOL = '"' + EOL;
      CONFIG_USAGE = "SVGFont.config.usage";
      CONFIG_SVG_BEGIN = "SVGFont.config.svg.begin";
      CONFIG_SVG_TEST_CARD_START = "SVGFont.config.svg.test.card.start";
      CONFIG_SVG_TEST_CARD_END = "SVGFont.config.svg.test.card.end";
   }
}
