package org.apache.batik.bridge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.FontFaceRule;
import org.apache.batik.gvt.font.GVTFontFace;
import org.apache.batik.gvt.font.GVTFontFamily;
import org.apache.batik.gvt.font.UnresolvedFontFamily;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class SVGFontUtilities implements SVGConstants {
   public static List getFontFaces(Document doc, BridgeContext ctx) {
      Map fontFamilyMap = ctx.getFontFamilyMap();
      List ret = (List)fontFamilyMap.get(doc);
      if (ret != null) {
         return ret;
      } else {
         List ret = new LinkedList();
         NodeList fontFaceElements = doc.getElementsByTagNameNS("http://www.w3.org/2000/svg", "font-face");
         SVGFontFaceElementBridge fontFaceBridge = (SVGFontFaceElementBridge)ctx.getBridge("http://www.w3.org/2000/svg", "font-face");

         for(int i = 0; i < fontFaceElements.getLength(); ++i) {
            Element fontFaceElement = (Element)fontFaceElements.item(i);
            ret.add(fontFaceBridge.createFontFace(ctx, fontFaceElement));
         }

         CSSEngine engine = ((SVGOMDocument)doc).getCSSEngine();
         List sms = engine.getFontFaces();
         Iterator var8 = sms.iterator();

         while(var8.hasNext()) {
            Object sm = var8.next();
            FontFaceRule ffr = (FontFaceRule)sm;
            ret.add(CSSFontFace.createCSSFontFace(engine, ffr));
         }

         return ret;
      }
   }

   public static GVTFontFamily getFontFamily(Element textElement, BridgeContext ctx, String fontFamilyName, String fontWeight, String fontStyle) {
      String fontKeyName = fontFamilyName.toLowerCase() + " " + fontWeight + " " + fontStyle;
      Map fontFamilyMap = ctx.getFontFamilyMap();
      GVTFontFamily fontFamily = (GVTFontFamily)fontFamilyMap.get(fontKeyName);
      if (fontFamily != null) {
         return fontFamily;
      } else {
         Document doc = textElement.getOwnerDocument();
         List fontFaces = (List)fontFamilyMap.get(doc);
         if (fontFaces == null) {
            fontFaces = getFontFaces(doc, ctx);
            fontFamilyMap.put(doc, fontFaces);
         }

         Iterator iter = fontFaces.iterator();
         List svgFontFamilies = new LinkedList();

         while(true) {
            FontFace fontFace;
            String fontFaceStyle;
            do {
               do {
                  if (!iter.hasNext()) {
                     if (svgFontFamilies.size() == 1) {
                        fontFamilyMap.put(fontKeyName, svgFontFamilies.get(0));
                        return (GVTFontFamily)svgFontFamilies.get(0);
                     }

                     if (svgFontFamilies.size() <= 1) {
                        GVTFontFamily gvtFontFamily = new UnresolvedFontFamily(fontFamilyName);
                        fontFamilyMap.put(fontKeyName, gvtFontFamily);
                        return gvtFontFamily;
                     }

                     String fontWeightNumber = getFontWeightNumberString(fontWeight);
                     List fontFamilyWeights = new ArrayList(svgFontFamilies.size());
                     Iterator var28 = svgFontFamilies.iterator();

                     while(var28.hasNext()) {
                        Object svgFontFamily = var28.next();
                        GVTFontFace fontFace = ((GVTFontFamily)svgFontFamily).getFontFace();
                        String fontFaceWeight = fontFace.getFontWeight();
                        fontFaceWeight = getFontWeightNumberString(fontFaceWeight);
                        fontFamilyWeights.add(fontFaceWeight);
                     }

                     List newFontFamilyWeights = new ArrayList(fontFamilyWeights);

                     int i;
                     String weightString;
                     for(i = 100; i <= 900; i += 100) {
                        weightString = String.valueOf(i);
                        boolean matched = false;
                        int minDifference = 1000;
                        int minDifferenceIndex = 0;

                        for(int j = 0; j < fontFamilyWeights.size(); ++j) {
                           String fontFamilyWeight = (String)fontFamilyWeights.get(j);
                           if (fontFamilyWeight.indexOf(weightString) > -1) {
                              matched = true;
                              break;
                           }

                           StringTokenizer st = new StringTokenizer(fontFamilyWeight, " ,");

                           while(st.hasMoreTokens()) {
                              int weightNum = Integer.parseInt(st.nextToken());
                              int difference = Math.abs(weightNum - i);
                              if (difference < minDifference) {
                                 minDifference = difference;
                                 minDifferenceIndex = j;
                              }
                           }
                        }

                        if (!matched) {
                           String newFontFamilyWeight = newFontFamilyWeights.get(minDifferenceIndex) + ", " + weightString;
                           newFontFamilyWeights.set(minDifferenceIndex, newFontFamilyWeight);
                        }
                     }

                     for(i = 0; i < svgFontFamilies.size(); ++i) {
                        weightString = (String)newFontFamilyWeights.get(i);
                        if (weightString.indexOf(fontWeightNumber) > -1) {
                           fontFamilyMap.put(fontKeyName, svgFontFamilies.get(i));
                           return (GVTFontFamily)svgFontFamilies.get(i);
                        }
                     }

                     fontFamilyMap.put(fontKeyName, svgFontFamilies.get(0));
                     return (GVTFontFamily)svgFontFamilies.get(0);
                  }

                  fontFace = (FontFace)iter.next();
               } while(!fontFace.hasFamilyName(fontFamilyName));

               fontFaceStyle = fontFace.getFontStyle();
            } while(!fontFaceStyle.equals("all") && fontFaceStyle.indexOf(fontStyle) == -1);

            GVTFontFamily ffam = fontFace.getFontFamily(ctx);
            if (ffam != null) {
               svgFontFamilies.add(ffam);
            }
         }
      }
   }

   protected static String getFontWeightNumberString(String fontWeight) {
      if (fontWeight.equals("normal")) {
         return "400";
      } else if (fontWeight.equals("bold")) {
         return "700";
      } else {
         return fontWeight.equals("all") ? "100, 200, 300, 400, 500, 600, 700, 800, 900" : fontWeight;
      }
   }
}
