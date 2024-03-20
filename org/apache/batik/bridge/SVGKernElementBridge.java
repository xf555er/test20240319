package org.apache.batik.bridge;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.batik.gvt.font.Kern;
import org.apache.batik.gvt.font.UnicodeRange;
import org.w3c.dom.Element;

public abstract class SVGKernElementBridge extends AbstractSVGBridge {
   public Kern createKern(BridgeContext ctx, Element kernElement, SVGGVTFont font) {
      String u1 = kernElement.getAttributeNS((String)null, "u1");
      String u2 = kernElement.getAttributeNS((String)null, "u2");
      String g1 = kernElement.getAttributeNS((String)null, "g1");
      String g2 = kernElement.getAttributeNS((String)null, "g2");
      String k = kernElement.getAttributeNS((String)null, "k");
      if (k.length() == 0) {
         k = "0";
      }

      float kernValue = Float.parseFloat(k);
      int firstGlyphLen = 0;
      int secondGlyphLen = 0;
      int[] firstGlyphSet = null;
      int[] secondGlyphSet = null;
      List firstUnicodeRanges = new ArrayList();
      List secondUnicodeRanges = new ArrayList();
      StringTokenizer st = new StringTokenizer(u1, ",");

      while(true) {
         String token;
         int[] secondGlyphs;
         int sz;
         int[] tmp;
         int var21;
         int glyphCode;
         int[] var24;
         int var25;
         while(st.hasMoreTokens()) {
            token = st.nextToken();
            if (token.startsWith("U+")) {
               firstUnicodeRanges.add(new UnicodeRange(token));
            } else {
               secondGlyphs = font.getGlyphCodesForUnicode(token);
               if (firstGlyphSet == null) {
                  firstGlyphSet = secondGlyphs;
                  firstGlyphLen = secondGlyphs.length;
               } else {
                  if (firstGlyphLen + secondGlyphs.length > firstGlyphSet.length) {
                     sz = firstGlyphSet.length * 2;
                     if (sz < firstGlyphLen + secondGlyphs.length) {
                        sz = firstGlyphLen + secondGlyphs.length;
                     }

                     tmp = new int[sz];
                     System.arraycopy(firstGlyphSet, 0, tmp, 0, firstGlyphLen);
                     firstGlyphSet = tmp;
                  }

                  var24 = secondGlyphs;
                  var25 = secondGlyphs.length;

                  for(var21 = 0; var21 < var25; ++var21) {
                     glyphCode = var24[var21];
                     firstGlyphSet[firstGlyphLen++] = glyphCode;
                  }
               }
            }
         }

         st = new StringTokenizer(u2, ",");

         while(true) {
            while(st.hasMoreTokens()) {
               token = st.nextToken();
               if (token.startsWith("U+")) {
                  secondUnicodeRanges.add(new UnicodeRange(token));
               } else {
                  secondGlyphs = font.getGlyphCodesForUnicode(token);
                  if (secondGlyphSet == null) {
                     secondGlyphSet = secondGlyphs;
                     secondGlyphLen = secondGlyphs.length;
                  } else {
                     if (secondGlyphLen + secondGlyphs.length > secondGlyphSet.length) {
                        sz = secondGlyphSet.length * 2;
                        if (sz < secondGlyphLen + secondGlyphs.length) {
                           sz = secondGlyphLen + secondGlyphs.length;
                        }

                        tmp = new int[sz];
                        System.arraycopy(secondGlyphSet, 0, tmp, 0, secondGlyphLen);
                        secondGlyphSet = tmp;
                     }

                     var24 = secondGlyphs;
                     var25 = secondGlyphs.length;

                     for(var21 = 0; var21 < var25; ++var21) {
                        glyphCode = var24[var21];
                        secondGlyphSet[secondGlyphLen++] = glyphCode;
                     }
                  }
               }
            }

            st = new StringTokenizer(g1, ",");

            while(true) {
               while(st.hasMoreTokens()) {
                  token = st.nextToken();
                  secondGlyphs = font.getGlyphCodesForName(token);
                  if (firstGlyphSet == null) {
                     firstGlyphSet = secondGlyphs;
                     firstGlyphLen = secondGlyphs.length;
                  } else {
                     if (firstGlyphLen + secondGlyphs.length > firstGlyphSet.length) {
                        sz = firstGlyphSet.length * 2;
                        if (sz < firstGlyphLen + secondGlyphs.length) {
                           sz = firstGlyphLen + secondGlyphs.length;
                        }

                        tmp = new int[sz];
                        System.arraycopy(firstGlyphSet, 0, tmp, 0, firstGlyphLen);
                        firstGlyphSet = tmp;
                     }

                     var24 = secondGlyphs;
                     var25 = secondGlyphs.length;

                     for(var21 = 0; var21 < var25; ++var21) {
                        glyphCode = var24[var21];
                        firstGlyphSet[firstGlyphLen++] = glyphCode;
                     }
                  }
               }

               st = new StringTokenizer(g2, ",");

               while(true) {
                  while(st.hasMoreTokens()) {
                     token = st.nextToken();
                     secondGlyphs = font.getGlyphCodesForName(token);
                     if (secondGlyphSet == null) {
                        secondGlyphSet = secondGlyphs;
                        secondGlyphLen = secondGlyphs.length;
                     } else {
                        if (secondGlyphLen + secondGlyphs.length > secondGlyphSet.length) {
                           sz = secondGlyphSet.length * 2;
                           if (sz < secondGlyphLen + secondGlyphs.length) {
                              sz = secondGlyphLen + secondGlyphs.length;
                           }

                           tmp = new int[sz];
                           System.arraycopy(secondGlyphSet, 0, tmp, 0, secondGlyphLen);
                           secondGlyphSet = tmp;
                        }

                        var24 = secondGlyphs;
                        var25 = secondGlyphs.length;

                        for(var21 = 0; var21 < var25; ++var21) {
                           glyphCode = var24[var21];
                           secondGlyphSet[secondGlyphLen++] = glyphCode;
                        }
                     }
                  }

                  int[] firstGlyphs;
                  if (firstGlyphLen != 0 && firstGlyphLen != firstGlyphSet.length) {
                     firstGlyphs = new int[firstGlyphLen];
                     System.arraycopy(firstGlyphSet, 0, firstGlyphs, 0, firstGlyphLen);
                  } else {
                     firstGlyphs = firstGlyphSet;
                  }

                  if (secondGlyphLen != 0 && secondGlyphLen != secondGlyphSet.length) {
                     secondGlyphs = new int[secondGlyphLen];
                     System.arraycopy(secondGlyphSet, 0, secondGlyphs, 0, secondGlyphLen);
                  } else {
                     secondGlyphs = secondGlyphSet;
                  }

                  UnicodeRange[] firstRanges = new UnicodeRange[firstUnicodeRanges.size()];
                  firstUnicodeRanges.toArray(firstRanges);
                  UnicodeRange[] secondRanges = new UnicodeRange[secondUnicodeRanges.size()];
                  secondUnicodeRanges.toArray(secondRanges);
                  return new Kern(firstGlyphs, secondGlyphs, firstRanges, secondRanges, kernValue);
               }
            }
         }
      }
   }
}
