package org.apache.fop.util;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.xmlgraphics.java2d.color.CIELabColorSpace;
import org.apache.xmlgraphics.java2d.color.ColorSpaceOrigin;
import org.apache.xmlgraphics.java2d.color.ColorSpaces;
import org.apache.xmlgraphics.java2d.color.ColorWithAlternatives;
import org.apache.xmlgraphics.java2d.color.DeviceCMYKColorSpace;
import org.apache.xmlgraphics.java2d.color.NamedColorSpace;
import org.apache.xmlgraphics.java2d.color.RenderingIntent;
import org.apache.xmlgraphics.java2d.color.profile.NamedColorProfile;
import org.apache.xmlgraphics.java2d.color.profile.NamedColorProfileParser;

public final class ColorUtil {
   public static final String CMYK_PSEUDO_PROFILE = "#CMYK";
   public static final String SEPARATION_PSEUDO_PROFILE = "#Separation";
   private static Map colorMap;
   private static final Log log = LogFactory.getLog(ColorUtil.class);

   private ColorUtil() {
   }

   public static Color parseColorString(FOUserAgent foUserAgent, String value) throws PropertyException {
      if (value == null) {
         return null;
      } else {
         Color parsedColor = (Color)colorMap.get(value.toLowerCase());
         if (parsedColor == null) {
            if (value.startsWith("#")) {
               parsedColor = parseWithHash(value);
            } else if (value.startsWith("rgb(")) {
               parsedColor = parseAsRGB(value);
            } else {
               if (value.startsWith("url(")) {
                  throw new PropertyException("Colors starting with url( are not yet supported!");
               }

               if (value.startsWith("java.awt.Color")) {
                  parsedColor = parseAsJavaAWTColor(value);
               } else if (value.startsWith("system-color(")) {
                  parsedColor = parseAsSystemColor(value);
               } else if (value.startsWith("fop-rgb-icc")) {
                  parsedColor = parseAsFopRgbIcc(foUserAgent, value);
               } else if (value.startsWith("fop-rgb-named-color")) {
                  parsedColor = parseAsFopRgbNamedColor(foUserAgent, value);
               } else if (value.startsWith("cie-lab-color")) {
                  parsedColor = parseAsCIELabColor(foUserAgent, value);
               } else if (value.startsWith("cmyk")) {
                  parsedColor = parseAsCMYK(value);
               } else if (value.startsWith("oca")) {
                  parsedColor = parseAsOCA(value);
               }
            }

            if (parsedColor == null) {
               throw new PropertyException("Unknown Color: " + value);
            }

            colorMap.put(value, parsedColor);
         }

         return parsedColor;
      }
   }

   private static Color parseAsSystemColor(String value) throws PropertyException {
      int poss = value.indexOf("(");
      int pose = value.indexOf(")");
      if (poss != -1 && pose != -1) {
         value = value.substring(poss + 1, pose);
         return (Color)colorMap.get(value);
      } else {
         throw new PropertyException("Unknown color format: " + value + ". Must be system-color(x)");
      }
   }

   private static Color parseAsJavaAWTColor(String value) throws PropertyException {
      float red = 0.0F;
      float green = 0.0F;
      float blue = 0.0F;
      int poss = value.indexOf("[");
      int pose = value.indexOf("]");

      try {
         if (poss != -1 && pose != -1) {
            value = value.substring(poss + 1, pose);
            String[] args = value.split(",");
            if (args.length != 3) {
               throw new PropertyException("Invalid number of arguments for a java.awt.Color: " + value);
            } else {
               red = Float.parseFloat(args[0].trim().substring(2)) / 255.0F;
               green = Float.parseFloat(args[1].trim().substring(2)) / 255.0F;
               blue = Float.parseFloat(args[2].trim().substring(2)) / 255.0F;
               if (!((double)red < 0.0) && !((double)red > 1.0) && !((double)green < 0.0) && !((double)green > 1.0) && !((double)blue < 0.0) && !((double)blue > 1.0)) {
                  return new Color(red, green, blue);
               } else {
                  throw new PropertyException("Color values out of range");
               }
            }
         } else {
            throw new IllegalArgumentException("Invalid format for a java.awt.Color: " + value);
         }
      } catch (RuntimeException var7) {
         throw new PropertyException(var7);
      }
   }

   private static Color parseAsRGB(String value) throws PropertyException {
      int poss = value.indexOf("(");
      int pose = value.indexOf(")");
      if (poss != -1 && pose != -1) {
         value = value.substring(poss + 1, pose);

         try {
            String[] args = value.split(",");
            if (args.length != 3) {
               throw new PropertyException("Invalid number of arguments: rgb(" + value + ")");
            } else {
               float red = parseComponent255(args[0], value);
               float green = parseComponent255(args[1], value);
               float blue = parseComponent255(args[2], value);
               int r = (int)((double)(red * 255.0F) + 0.5);
               int g = (int)((double)(green * 255.0F) + 0.5);
               int b = (int)((double)(blue * 255.0F) + 0.5);
               Color parsedColor = new Color(r, g, b);
               return parsedColor;
            }
         } catch (RuntimeException var11) {
            throw new PropertyException(var11);
         }
      } else {
         throw new PropertyException("Unknown color format: " + value + ". Must be rgb(r,g,b)");
      }
   }

   private static float parseComponent255(String str, String function) throws PropertyException {
      str = str.trim();
      float component;
      if (str.endsWith("%")) {
         component = Float.parseFloat(str.substring(0, str.length() - 1)) / 100.0F;
      } else {
         component = Float.parseFloat(str) / 255.0F;
      }

      if (!((double)component < 0.0) && !((double)component > 1.0)) {
         return component;
      } else {
         throw new PropertyException("Color value out of range for " + function + ": " + str + ". Valid range: [0..255] or [0%..100%]");
      }
   }

   private static float parseComponent1(String argument, String function) throws PropertyException {
      return parseComponent(argument, 0.0F, 1.0F, function);
   }

   private static float parseComponent(String argument, float min, float max, String function) throws PropertyException {
      float component = Float.parseFloat(argument.trim());
      if (!(component < min) && !(component > max)) {
         return component;
      } else {
         throw new PropertyException("Color value out of range for " + function + ": " + argument + ". Valid range: [" + min + ".." + max + "]");
      }
   }

   private static Color parseFallback(String[] args, String value) throws PropertyException {
      float red = parseComponent1(args[0], value);
      float green = parseComponent1(args[1], value);
      float blue = parseComponent1(args[2], value);
      Color sRGB = new Color(red, green, blue);
      return sRGB;
   }

   private static Color parseWithHash(String value) throws PropertyException {
      try {
         int len = value.length();
         int alpha;
         if (len != 5 && len != 9) {
            alpha = 255;
         } else {
            alpha = Integer.parseInt(value.substring(len == 5 ? 3 : 7), 16);
         }

         int red = false;
         int green = false;
         int blue = false;
         int red;
         int green;
         int blue;
         if (len != 4 && len != 5) {
            if (len != 7 && len != 9) {
               throw new NumberFormatException();
            }

            red = Integer.parseInt(value.substring(1, 3), 16);
            green = Integer.parseInt(value.substring(3, 5), 16);
            blue = Integer.parseInt(value.substring(5, 7), 16);
         } else {
            red = Integer.parseInt(value.substring(1, 2), 16) * 17;
            green = Integer.parseInt(value.substring(2, 3), 16) * 17;
            blue = Integer.parseInt(value.substring(3, 4), 16) * 17;
         }

         Color parsedColor = new Color(red, green, blue, alpha);
         return parsedColor;
      } catch (RuntimeException var7) {
         throw new PropertyException("Unknown color format: " + value + ". Must be #RGB. #RGBA, #RRGGBB, or #RRGGBBAA");
      }
   }

   private static Color parseAsFopRgbIcc(FOUserAgent foUserAgent, String value) throws PropertyException {
      int poss = value.indexOf("(");
      int pose = value.indexOf(")");
      if (poss != -1 && pose != -1) {
         String[] args = value.substring(poss + 1, pose).split(",");

         try {
            if (args.length < 5) {
               throw new PropertyException("Too few arguments for rgb-icc() function");
            } else {
               Color sRGB = parseFallback(args, value);
               String iccProfileName = args[3].trim();
               if (iccProfileName != null && !"".equals(iccProfileName)) {
                  ColorSpace colorSpace = null;
                  String iccProfileSrc = null;
                  if (isPseudoProfile(iccProfileName)) {
                     if ("#CMYK".equalsIgnoreCase(iccProfileName)) {
                        colorSpace = ColorSpaces.getDeviceCMYKColorSpace();
                     } else if ("#Separation".equalsIgnoreCase(iccProfileName)) {
                        colorSpace = new NamedColorSpace(args[5], sRGB, "#Separation", (String)null);
                     } else {
                        assert false : "Incomplete implementation";
                     }
                  } else {
                     iccProfileSrc = args[4].trim();
                     if (iccProfileSrc == null || "".equals(iccProfileSrc)) {
                        throw new PropertyException("ICC profile source missing");
                     }

                     iccProfileSrc = unescapeString(iccProfileSrc);
                  }

                  int componentStart = 4;
                  if (colorSpace instanceof NamedColorSpace) {
                     ++componentStart;
                  }

                  float[] iccComponents = new float[args.length - componentStart - 1];
                  int ix = componentStart;

                  while(true) {
                     ++ix;
                     if (ix >= args.length) {
                        if (colorSpace instanceof NamedColorSpace && iccComponents.length == 0) {
                           iccComponents = new float[]{1.0F};
                        }

                        if (foUserAgent != null && iccProfileSrc != null) {
                           RenderingIntent renderingIntent = RenderingIntent.AUTO;
                           colorSpace = foUserAgent.getColorSpaceCache().get(iccProfileName, iccProfileSrc, renderingIntent);
                        }

                        Object parsedColor;
                        if (colorSpace != null) {
                           if (ColorSpaces.isDeviceColorSpace((ColorSpace)colorSpace)) {
                              Color deviceColor = new Color((ColorSpace)colorSpace, iccComponents, 1.0F);
                              float[] rgbComps = sRGB.getRGBColorComponents((float[])null);
                              parsedColor = new ColorWithAlternatives(rgbComps[0], rgbComps[1], rgbComps[2], new Color[]{deviceColor});
                           } else {
                              parsedColor = new ColorWithFallback((ColorSpace)colorSpace, iccComponents, 1.0F, (Color[])null, sRGB);
                           }
                        } else {
                           log.warn("Color profile '" + iccProfileSrc + "' not found. Using sRGB replacement values.");
                           parsedColor = sRGB;
                        }

                        return (Color)parsedColor;
                     }

                     iccComponents[ix - componentStart - 1] = Float.parseFloat(args[ix].trim());
                  }
               } else {
                  throw new PropertyException("ICC profile name missing");
               }
            }
         } catch (RuntimeException var14) {
            throw new PropertyException(var14);
         }
      } else {
         throw new PropertyException("Unknown color format: " + value + ". Must be fop-rgb-icc(r,g,b,NCNAME,src,....)");
      }
   }

   private static Color parseAsFopRgbNamedColor(FOUserAgent foUserAgent, String value) throws PropertyException {
      int poss = value.indexOf("(");
      int pose = value.indexOf(")");
      if (poss != -1 && pose != -1) {
         String[] args = value.substring(poss + 1, pose).split(",");

         try {
            if (args.length != 6) {
               throw new PropertyException("rgb-named-color() function must have 6 arguments");
            } else {
               Color sRGB = parseFallback(args, value);
               String iccProfileName = args[3].trim();
               if (iccProfileName != null && !"".equals(iccProfileName)) {
                  ICC_ColorSpace colorSpace = null;
                  if (isPseudoProfile(iccProfileName)) {
                     throw new IllegalArgumentException("Pseudo-profiles are not allowed with fop-rgb-named-color()");
                  } else {
                     String iccProfileSrc = args[4].trim();
                     if (iccProfileSrc != null && !"".equals(iccProfileSrc)) {
                        iccProfileSrc = unescapeString(iccProfileSrc);
                        String colorName = unescapeString(args[5].trim());
                        if (foUserAgent != null && iccProfileSrc != null) {
                           RenderingIntent renderingIntent = RenderingIntent.AUTO;
                           colorSpace = (ICC_ColorSpace)foUserAgent.getColorSpaceCache().get(iccProfileName, iccProfileSrc, renderingIntent);
                        }

                        Object parsedColor;
                        if (colorSpace != null) {
                           ICC_Profile profile = colorSpace.getProfile();
                           if (NamedColorProfileParser.isNamedColorProfile(profile)) {
                              NamedColorProfileParser parser = new NamedColorProfileParser();
                              NamedColorProfile ncp = parser.parseProfile(profile, iccProfileName, iccProfileSrc);
                              NamedColorSpace ncs = ncp.getNamedColor(colorName);
                              if (ncs != null) {
                                 parsedColor = new ColorWithFallback(ncs, new float[]{1.0F}, 1.0F, (Color[])null, sRGB);
                              } else {
                                 log.warn("Color '" + colorName + "' does not exist in named color profile: " + iccProfileSrc);
                                 parsedColor = sRGB;
                              }
                           } else {
                              log.warn("ICC profile is no named color profile: " + iccProfileSrc);
                              parsedColor = sRGB;
                           }
                        } else {
                           log.warn("Color profile '" + iccProfileSrc + "' not found. Using sRGB replacement values.");
                           parsedColor = sRGB;
                        }

                        return (Color)parsedColor;
                     } else {
                        throw new PropertyException("ICC profile source missing");
                     }
                  }
               } else {
                  throw new PropertyException("ICC profile name missing");
               }
            }
         } catch (IOException var15) {
            throw new PropertyException(var15);
         } catch (RuntimeException var16) {
            throw new PropertyException(var16);
         }
      } else {
         throw new PropertyException("Unknown color format: " + value + ". Must be fop-rgb-named-color(r,g,b,NCNAME,src,color-name)");
      }
   }

   private static Color parseAsCIELabColor(FOUserAgent foUserAgent, String value) throws PropertyException {
      int poss = value.indexOf("(");
      int pose = value.indexOf(")");
      if (poss != -1 && pose != -1) {
         try {
            String[] args = value.substring(poss + 1, pose).split(",");
            if (args.length != 6) {
               throw new PropertyException("cie-lab-color() function must have 6 arguments");
            } else {
               float red = parseComponent255(args[0], value);
               float green = parseComponent255(args[1], value);
               float blue = parseComponent255(args[2], value);
               Color sRGB = new Color(red, green, blue);
               float l = parseComponent(args[3], 0.0F, 100.0F, value);
               float a = parseComponent(args[4], -127.0F, 127.0F, value);
               float b = parseComponent(args[5], -127.0F, 127.0F, value);
               CIELabColorSpace cs = ColorSpaces.getCIELabColorSpaceD50();
               Color labColor = cs.toColor(l, a, b, 1.0F);
               Color parsedColor = new ColorWithFallback(labColor, sRGB);
               return parsedColor;
            }
         } catch (RuntimeException var15) {
            throw new PropertyException(var15);
         }
      } else {
         throw new PropertyException("Unknown color format: " + value + ". Must be cie-lab-color(r,g,b,Lightness,a-value,b-value)");
      }
   }

   private static String unescapeString(String iccProfileSrc) {
      if (iccProfileSrc.startsWith("\"") || iccProfileSrc.startsWith("'")) {
         iccProfileSrc = iccProfileSrc.substring(1);
      }

      if (iccProfileSrc.endsWith("\"") || iccProfileSrc.endsWith("'")) {
         iccProfileSrc = iccProfileSrc.substring(0, iccProfileSrc.length() - 1);
      }

      return iccProfileSrc;
   }

   private static Color parseAsCMYK(String value) throws PropertyException {
      int poss = value.indexOf("(");
      int pose = value.indexOf(")");
      if (poss != -1 && pose != -1) {
         value = value.substring(poss + 1, pose);
         String[] args = value.split(",");

         try {
            if (args.length != 4) {
               throw new PropertyException("Invalid number of arguments: cmyk(" + value + ")");
            } else {
               float cyan = parseComponent1(args[0], value);
               float magenta = parseComponent1(args[1], value);
               float yellow = parseComponent1(args[2], value);
               float black = parseComponent1(args[3], value);
               float[] comps = new float[]{cyan, magenta, yellow, black};
               Color cmykColor = DeviceCMYKColorSpace.createCMYKColor(comps);
               float[] rgbComps = cmykColor.getRGBColorComponents((float[])null);
               Color parsedColor = new ColorWithAlternatives(rgbComps[0], rgbComps[1], rgbComps[2], new Color[]{cmykColor});
               return parsedColor;
            }
         } catch (RuntimeException var12) {
            throw new PropertyException(var12);
         }
      } else {
         throw new PropertyException("Unknown color format: " + value + ". Must be cmyk(c,m,y,k)");
      }
   }

   private static Color parseAsOCA(String value) throws PropertyException {
      int poss = value.indexOf("(");
      int pose = value.indexOf(")");
      if (poss != -1 && pose != -1) {
         value = value.substring(poss + 1, pose);
         OCAColor.OCAColorValue colorValue;
         if (value.equals("blue")) {
            colorValue = OCAColor.OCAColorValue.BLUE;
         } else if (value.equals("red")) {
            colorValue = OCAColor.OCAColorValue.RED;
         } else if (value.equals("magenta")) {
            colorValue = OCAColor.OCAColorValue.MAGENTA;
         } else if (value.equals("green")) {
            colorValue = OCAColor.OCAColorValue.GREEN;
         } else if (value.equals("cyan")) {
            colorValue = OCAColor.OCAColorValue.CYAN;
         } else if (value.equals("yellow")) {
            colorValue = OCAColor.OCAColorValue.YELLOW;
         } else if (value.equals("black")) {
            colorValue = OCAColor.OCAColorValue.BLACK;
         } else if (value.equals("brown")) {
            colorValue = OCAColor.OCAColorValue.BROWN;
         } else if (value.equals("medium-color")) {
            colorValue = OCAColor.OCAColorValue.MEDIUM_COLOR;
         } else {
            if (!value.equals("device-default")) {
               throw new PropertyException("Unknwon OCA color: " + value);
            }

            colorValue = OCAColor.OCAColorValue.DEVICE_DEFAULT;
         }

         return new OCAColor(colorValue);
      } else {
         throw new PropertyException("Unknown color format: " + value + ". Must be oca(color-name)");
      }
   }

   public static String colorToString(Color color) {
      ColorSpace cs = color.getColorSpace();
      if (color instanceof ColorWithAlternatives) {
         return toFunctionCall((ColorWithAlternatives)color);
      } else if (cs != null && cs.getType() == 9) {
         StringBuffer sbuf = new StringBuffer(24);
         float[] cmyk = color.getColorComponents((float[])null);
         sbuf.append("cmyk(").append(cmyk[0]).append(",").append(cmyk[1]).append(",").append(cmyk[2]).append(",").append(cmyk[3]).append(")");
         return sbuf.toString();
      } else {
         return toRGBFunctionCall(color);
      }
   }

   private static String toRGBFunctionCall(Color color) {
      StringBuffer sbuf = new StringBuffer();
      sbuf.append('#');
      String s = Integer.toHexString(color.getRed());
      if (s.length() == 1) {
         sbuf.append('0');
      }

      sbuf.append(s);
      s = Integer.toHexString(color.getGreen());
      if (s.length() == 1) {
         sbuf.append('0');
      }

      sbuf.append(s);
      s = Integer.toHexString(color.getBlue());
      if (s.length() == 1) {
         sbuf.append('0');
      }

      sbuf.append(s);
      if (color.getAlpha() != 255) {
         s = Integer.toHexString(color.getAlpha());
         if (s.length() == 1) {
            sbuf.append('0');
         }

         sbuf.append(s);
      }

      return sbuf.toString();
   }

   private static Color getsRGBFallback(ColorWithAlternatives color) {
      Color fallbackColor;
      if (color instanceof ColorWithFallback) {
         fallbackColor = ((ColorWithFallback)color).getFallbackColor();
         if (!fallbackColor.getColorSpace().isCS_sRGB()) {
            fallbackColor = toSRGBColor(fallbackColor);
         }
      } else {
         fallbackColor = toSRGBColor(color);
      }

      return fallbackColor;
   }

   private static Color toSRGBColor(Color color) {
      ColorSpace sRGB = ColorSpace.getInstance(1000);
      float[] comps = color.getRGBColorComponents((float[])null);
      float[] allComps = color.getComponents((float[])null);
      float alpha = allComps[allComps.length - 1];
      return new Color(sRGB, comps, alpha);
   }

   private static String toFunctionCall(ColorWithAlternatives color) {
      ColorSpace cs = color.getColorSpace();
      if (cs.isCS_sRGB() && !color.hasAlternativeColors()) {
         return toRGBFunctionCall(color);
      } else if (cs instanceof CIELabColorSpace) {
         return toCIELabFunctionCall(color);
      } else {
         Color specColor = color;
         if (color.hasAlternativeColors()) {
            Color alt = color.getAlternativeColors()[0];
            if (ColorSpaces.isDeviceColorSpace(alt.getColorSpace())) {
               cs = alt.getColorSpace();
               specColor = alt;
            }
         }

         ColorSpaceOrigin origin = ColorSpaces.getColorSpaceOrigin(cs);
         Color fallbackColor = getsRGBFallback(color);
         float[] rgb = fallbackColor.getColorComponents((float[])null);

         assert rgb.length == 3;

         StringBuffer sb = new StringBuffer(40);
         sb.append("(");
         sb.append(rgb[0]).append(",");
         sb.append(rgb[1]).append(",");
         sb.append(rgb[2]).append(",");
         String profileName = origin.getProfileName();
         sb.append(profileName).append(",");
         if (origin.getProfileURI() != null) {
            sb.append("\"").append(origin.getProfileURI()).append("\"");
         }

         String functionName;
         if (cs instanceof NamedColorSpace) {
            NamedColorSpace ncs = (NamedColorSpace)cs;
            if ("#Separation".equalsIgnoreCase(profileName)) {
               functionName = "fop-rgb-icc";
            } else {
               functionName = "fop-rgb-named-color";
            }

            sb.append(",").append(ncs.getColorName());
         } else {
            functionName = "fop-rgb-icc";
            float[] colorComponents = ((Color)specColor).getColorComponents((float[])null);
            float[] var10 = colorComponents;
            int var11 = colorComponents.length;

            for(int var12 = 0; var12 < var11; ++var12) {
               float colorComponent = var10[var12];
               sb.append(",");
               sb.append(colorComponent);
            }
         }

         sb.append(")");
         return functionName + sb;
      }
   }

   private static String toCIELabFunctionCall(ColorWithAlternatives color) {
      Color fallbackColor = getsRGBFallback(color);
      StringBuffer sb = new StringBuffer("cie-lab-color(");
      sb.append(fallbackColor.getRed()).append(',');
      sb.append(fallbackColor.getGreen()).append(',');
      sb.append(fallbackColor.getBlue());
      CIELabColorSpace cs = (CIELabColorSpace)color.getColorSpace();
      float[] lab = cs.toNativeComponents(color.getColorComponents((float[])null));

      for(int i = 0; i < 3; ++i) {
         sb.append(',').append(lab[i]);
      }

      sb.append(')');
      return sb.toString();
   }

   private static Color createColor(int r, int g, int b) {
      return new Color(r, g, b);
   }

   private static void initializeColorMap() {
      colorMap = Collections.synchronizedMap(new HashMap());
      colorMap.put("aliceblue", createColor(240, 248, 255));
      colorMap.put("antiquewhite", createColor(250, 235, 215));
      colorMap.put("aqua", createColor(0, 255, 255));
      colorMap.put("aquamarine", createColor(127, 255, 212));
      colorMap.put("azure", createColor(240, 255, 255));
      colorMap.put("beige", createColor(245, 245, 220));
      colorMap.put("bisque", createColor(255, 228, 196));
      colorMap.put("black", createColor(0, 0, 0));
      colorMap.put("blanchedalmond", createColor(255, 235, 205));
      colorMap.put("blue", createColor(0, 0, 255));
      colorMap.put("blueviolet", createColor(138, 43, 226));
      colorMap.put("brown", createColor(165, 42, 42));
      colorMap.put("burlywood", createColor(222, 184, 135));
      colorMap.put("cadetblue", createColor(95, 158, 160));
      colorMap.put("chartreuse", createColor(127, 255, 0));
      colorMap.put("chocolate", createColor(210, 105, 30));
      colorMap.put("coral", createColor(255, 127, 80));
      colorMap.put("cornflowerblue", createColor(100, 149, 237));
      colorMap.put("cornsilk", createColor(255, 248, 220));
      colorMap.put("crimson", createColor(220, 20, 60));
      colorMap.put("cyan", createColor(0, 255, 255));
      colorMap.put("darkblue", createColor(0, 0, 139));
      colorMap.put("darkcyan", createColor(0, 139, 139));
      colorMap.put("darkgoldenrod", createColor(184, 134, 11));
      colorMap.put("darkgray", createColor(169, 169, 169));
      colorMap.put("darkgreen", createColor(0, 100, 0));
      colorMap.put("darkgrey", createColor(169, 169, 169));
      colorMap.put("darkkhaki", createColor(189, 183, 107));
      colorMap.put("darkmagenta", createColor(139, 0, 139));
      colorMap.put("darkolivegreen", createColor(85, 107, 47));
      colorMap.put("darkorange", createColor(255, 140, 0));
      colorMap.put("darkorchid", createColor(153, 50, 204));
      colorMap.put("darkred", createColor(139, 0, 0));
      colorMap.put("darksalmon", createColor(233, 150, 122));
      colorMap.put("darkseagreen", createColor(143, 188, 143));
      colorMap.put("darkslateblue", createColor(72, 61, 139));
      colorMap.put("darkslategray", createColor(47, 79, 79));
      colorMap.put("darkslategrey", createColor(47, 79, 79));
      colorMap.put("darkturquoise", createColor(0, 206, 209));
      colorMap.put("darkviolet", createColor(148, 0, 211));
      colorMap.put("deeppink", createColor(255, 20, 147));
      colorMap.put("deepskyblue", createColor(0, 191, 255));
      colorMap.put("dimgray", createColor(105, 105, 105));
      colorMap.put("dimgrey", createColor(105, 105, 105));
      colorMap.put("dodgerblue", createColor(30, 144, 255));
      colorMap.put("firebrick", createColor(178, 34, 34));
      colorMap.put("floralwhite", createColor(255, 250, 240));
      colorMap.put("forestgreen", createColor(34, 139, 34));
      colorMap.put("fuchsia", createColor(255, 0, 255));
      colorMap.put("gainsboro", createColor(220, 220, 220));
      colorMap.put("ghostwhite", createColor(248, 248, 255));
      colorMap.put("gold", createColor(255, 215, 0));
      colorMap.put("goldenrod", createColor(218, 165, 32));
      colorMap.put("gray", createColor(128, 128, 128));
      colorMap.put("green", createColor(0, 128, 0));
      colorMap.put("greenyellow", createColor(173, 255, 47));
      colorMap.put("grey", createColor(128, 128, 128));
      colorMap.put("honeydew", createColor(240, 255, 240));
      colorMap.put("hotpink", createColor(255, 105, 180));
      colorMap.put("indianred", createColor(205, 92, 92));
      colorMap.put("indigo", createColor(75, 0, 130));
      colorMap.put("ivory", createColor(255, 255, 240));
      colorMap.put("khaki", createColor(240, 230, 140));
      colorMap.put("lavender", createColor(230, 230, 250));
      colorMap.put("lavenderblush", createColor(255, 240, 245));
      colorMap.put("lawngreen", createColor(124, 252, 0));
      colorMap.put("lemonchiffon", createColor(255, 250, 205));
      colorMap.put("lightblue", createColor(173, 216, 230));
      colorMap.put("lightcoral", createColor(240, 128, 128));
      colorMap.put("lightcyan", createColor(224, 255, 255));
      colorMap.put("lightgoldenrodyellow", createColor(250, 250, 210));
      colorMap.put("lightgray", createColor(211, 211, 211));
      colorMap.put("lightgreen", createColor(144, 238, 144));
      colorMap.put("lightgrey", createColor(211, 211, 211));
      colorMap.put("lightpink", createColor(255, 182, 193));
      colorMap.put("lightsalmon", createColor(255, 160, 122));
      colorMap.put("lightseagreen", createColor(32, 178, 170));
      colorMap.put("lightskyblue", createColor(135, 206, 250));
      colorMap.put("lightslategray", createColor(119, 136, 153));
      colorMap.put("lightslategrey", createColor(119, 136, 153));
      colorMap.put("lightsteelblue", createColor(176, 196, 222));
      colorMap.put("lightyellow", createColor(255, 255, 224));
      colorMap.put("lime", createColor(0, 255, 0));
      colorMap.put("limegreen", createColor(50, 205, 50));
      colorMap.put("linen", createColor(250, 240, 230));
      colorMap.put("magenta", createColor(255, 0, 255));
      colorMap.put("maroon", createColor(128, 0, 0));
      colorMap.put("mediumaquamarine", createColor(102, 205, 170));
      colorMap.put("mediumblue", createColor(0, 0, 205));
      colorMap.put("mediumorchid", createColor(186, 85, 211));
      colorMap.put("mediumpurple", createColor(147, 112, 219));
      colorMap.put("mediumseagreen", createColor(60, 179, 113));
      colorMap.put("mediumslateblue", createColor(123, 104, 238));
      colorMap.put("mediumspringgreen", createColor(0, 250, 154));
      colorMap.put("mediumturquoise", createColor(72, 209, 204));
      colorMap.put("mediumvioletred", createColor(199, 21, 133));
      colorMap.put("midnightblue", createColor(25, 25, 112));
      colorMap.put("mintcream", createColor(245, 255, 250));
      colorMap.put("mistyrose", createColor(255, 228, 225));
      colorMap.put("moccasin", createColor(255, 228, 181));
      colorMap.put("navajowhite", createColor(255, 222, 173));
      colorMap.put("navy", createColor(0, 0, 128));
      colorMap.put("oldlace", createColor(253, 245, 230));
      colorMap.put("olive", createColor(128, 128, 0));
      colorMap.put("olivedrab", createColor(107, 142, 35));
      colorMap.put("orange", createColor(255, 165, 0));
      colorMap.put("orangered", createColor(255, 69, 0));
      colorMap.put("orchid", createColor(218, 112, 214));
      colorMap.put("palegoldenrod", createColor(238, 232, 170));
      colorMap.put("palegreen", createColor(152, 251, 152));
      colorMap.put("paleturquoise", createColor(175, 238, 238));
      colorMap.put("palevioletred", createColor(219, 112, 147));
      colorMap.put("papayawhip", createColor(255, 239, 213));
      colorMap.put("peachpuff", createColor(255, 218, 185));
      colorMap.put("peru", createColor(205, 133, 63));
      colorMap.put("pink", createColor(255, 192, 203));
      colorMap.put("plum ", createColor(221, 160, 221));
      colorMap.put("plum", createColor(221, 160, 221));
      colorMap.put("powderblue", createColor(176, 224, 230));
      colorMap.put("purple", createColor(128, 0, 128));
      colorMap.put("red", createColor(255, 0, 0));
      colorMap.put("rosybrown", createColor(188, 143, 143));
      colorMap.put("royalblue", createColor(65, 105, 225));
      colorMap.put("saddlebrown", createColor(139, 69, 19));
      colorMap.put("salmon", createColor(250, 128, 114));
      colorMap.put("sandybrown", createColor(244, 164, 96));
      colorMap.put("seagreen", createColor(46, 139, 87));
      colorMap.put("seashell", createColor(255, 245, 238));
      colorMap.put("sienna", createColor(160, 82, 45));
      colorMap.put("silver", createColor(192, 192, 192));
      colorMap.put("skyblue", createColor(135, 206, 235));
      colorMap.put("slateblue", createColor(106, 90, 205));
      colorMap.put("slategray", createColor(112, 128, 144));
      colorMap.put("slategrey", createColor(112, 128, 144));
      colorMap.put("snow", createColor(255, 250, 250));
      colorMap.put("springgreen", createColor(0, 255, 127));
      colorMap.put("steelblue", createColor(70, 130, 180));
      colorMap.put("tan", createColor(210, 180, 140));
      colorMap.put("teal", createColor(0, 128, 128));
      colorMap.put("thistle", createColor(216, 191, 216));
      colorMap.put("tomato", createColor(255, 99, 71));
      colorMap.put("turquoise", createColor(64, 224, 208));
      colorMap.put("violet", createColor(238, 130, 238));
      colorMap.put("wheat", createColor(245, 222, 179));
      colorMap.put("white", createColor(255, 255, 255));
      colorMap.put("whitesmoke", createColor(245, 245, 245));
      colorMap.put("yellow", createColor(255, 255, 0));
      colorMap.put("yellowgreen", createColor(154, 205, 50));
      colorMap.put("transparent", new ColorWithAlternatives(0, 0, 0, 0, (Color[])null));
   }

   public static Color lightenColor(Color col, float factor) {
      return org.apache.xmlgraphics.java2d.color.ColorUtil.lightenColor(col, factor);
   }

   public static boolean isPseudoProfile(String colorProfileName) {
      return "#CMYK".equalsIgnoreCase(colorProfileName) || "#Separation".equalsIgnoreCase(colorProfileName);
   }

   public static boolean isGray(Color col) {
      return org.apache.xmlgraphics.java2d.color.ColorUtil.isGray(col);
   }

   public static Color toCMYKGrayColor(float black) {
      return org.apache.xmlgraphics.java2d.color.ColorUtil.toCMYKGrayColor(black);
   }

   static {
      initializeColorMap();
   }
}
