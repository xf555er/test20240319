package org.apache.fop.pdf;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlgraphics.java2d.color.CIELabColorSpace;
import org.apache.xmlgraphics.java2d.color.ColorUtil;
import org.apache.xmlgraphics.java2d.color.ColorWithAlternatives;
import org.apache.xmlgraphics.java2d.color.DeviceCMYKColorSpace;
import org.apache.xmlgraphics.java2d.color.NamedColorSpace;
import org.apache.xmlgraphics.java2d.color.profile.ColorProfileUtil;
import org.apache.xmlgraphics.util.DoubleFormatUtil;

public class PDFColorHandler {
   private Log log = LogFactory.getLog(PDFColorHandler.class);
   private PDFResources resources;
   private Map cieLabColorSpaces;

   public PDFColorHandler(PDFResources resources) {
      this.resources = resources;
   }

   private PDFDocument getDocument() {
      return this.resources.getDocumentSafely();
   }

   public void establishColor(StringBuffer codeBuffer, Color color, boolean fill) {
      if (color instanceof ColorWithAlternatives) {
         ColorWithAlternatives colExt = (ColorWithAlternatives)color;
         Color[] alt = colExt.getAlternativeColors();
         Color[] var6 = alt;
         int var7 = alt.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            Color col = var6[var8];
            boolean established = this.establishColorFromColor(codeBuffer, col, fill);
            if (established) {
               return;
            }
         }

         if (this.log.isDebugEnabled() && alt.length > 0) {
            this.log.debug("None of the alternative colors are supported. Using fallback: " + color);
         }
      }

      boolean established = this.establishColorFromColor(codeBuffer, color, fill);
      if (!established) {
         this.establishDeviceRGB(codeBuffer, color, fill);
      }

   }

   private boolean establishColorFromColor(StringBuffer codeBuffer, Color color, boolean fill) {
      ColorSpace cs = color.getColorSpace();
      if (cs instanceof DeviceCMYKColorSpace) {
         this.establishDeviceCMYK(codeBuffer, color, fill);
         return true;
      } else {
         if (!cs.isCS_sRGB()) {
            if (cs instanceof ICC_ColorSpace) {
               PDFICCBasedColorSpace pdfcs = this.getICCBasedColorSpace((ICC_ColorSpace)cs);
               this.establishColor(codeBuffer, pdfcs, color, fill);
               return true;
            }

            if (cs instanceof NamedColorSpace) {
               PDFSeparationColorSpace sepcs = this.getSeparationColorSpace((NamedColorSpace)cs);
               this.establishColor(codeBuffer, sepcs, color, fill);
               return true;
            }

            if (cs instanceof CIELabColorSpace) {
               CIELabColorSpace labcs = (CIELabColorSpace)cs;
               PDFCIELabColorSpace pdflab = this.getCIELabColorSpace(labcs);
               this.selectColorSpace(codeBuffer, pdflab, fill);
               float[] comps = color.getColorComponents((float[])null);
               float[] nativeComps = labcs.toNativeComponents(comps);
               this.writeColor(codeBuffer, nativeComps, labcs.getNumComponents(), fill ? "sc" : "SC");
               return true;
            }
         }

         return false;
      }
   }

   private PDFICCBasedColorSpace getICCBasedColorSpace(ICC_ColorSpace cs) {
      ICC_Profile profile = cs.getProfile();
      String desc = ColorProfileUtil.getICCProfileDescription(profile);
      if (this.log.isDebugEnabled()) {
         this.log.trace("ICC profile encountered: " + desc);
      }

      PDFICCBasedColorSpace pdfcs = this.resources.getICCColorSpaceByProfileName(desc);
      if (pdfcs == null) {
         PDFFactory factory = this.getDocument().getFactory();
         PDFICCStream pdfICCStream = factory.makePDFICCStream();
         PDFDeviceColorSpace altSpace = PDFDeviceColorSpace.toPDFColorSpace(cs);
         pdfICCStream.setColorSpace(profile, altSpace);
         pdfcs = factory.makeICCBasedColorSpace((PDFResourceContext)null, desc, pdfICCStream);
      }

      return pdfcs;
   }

   private PDFSeparationColorSpace getSeparationColorSpace(NamedColorSpace cs) {
      PDFName colorName = new PDFName(cs.getColorName());
      PDFSeparationColorSpace sepcs = (PDFSeparationColorSpace)this.resources.getColorSpace(colorName);
      if (sepcs == null) {
         PDFFactory factory = this.getDocument().getFactory();
         sepcs = factory.makeSeparationColorSpace((PDFResourceContext)null, cs);
      }

      return sepcs;
   }

   private PDFCIELabColorSpace getCIELabColorSpace(CIELabColorSpace labCS) {
      if (this.cieLabColorSpaces == null) {
         this.cieLabColorSpaces = new HashMap();
      }

      float[] wp = labCS.getWhitePoint();
      StringBuilder sb = new StringBuilder();

      for(int i = 0; i < 3; ++i) {
         if (i > 0) {
            sb.append(',');
         }

         sb.append(wp[i]);
      }

      String key = sb.toString();
      PDFCIELabColorSpace cielab = (PDFCIELabColorSpace)this.cieLabColorSpaces.get(key);
      if (cielab == null) {
         float[] wp1 = new float[]{wp[0] / 100.0F, wp[1] / 100.0F, wp[2] / 100.0F};
         cielab = new PDFCIELabColorSpace(wp1, (float[])null);
         this.getDocument().registerObject(cielab);
         this.resources.addColorSpace(cielab);
         this.cieLabColorSpaces.put(key, cielab);
      }

      return cielab;
   }

   private void establishColor(StringBuffer codeBuffer, PDFColorSpace pdfcs, Color color, boolean fill) {
      this.selectColorSpace(codeBuffer, pdfcs, fill);
      this.writeColor(codeBuffer, color, pdfcs.getNumComponents(), fill ? "sc" : "SC");
   }

   private void selectColorSpace(StringBuffer codeBuffer, PDFColorSpace pdfcs, boolean fill) {
      codeBuffer.append(new PDFName(pdfcs.getName()));
      if (fill) {
         codeBuffer.append(" cs ");
      } else {
         codeBuffer.append(" CS ");
      }

   }

   private void establishDeviceRGB(StringBuffer codeBuffer, Color color, boolean fill) {
      float[] comps;
      if (color.getColorSpace().isCS_sRGB()) {
         comps = color.getColorComponents((float[])null);
      } else {
         if (this.log.isDebugEnabled()) {
            this.log.debug("Converting color to sRGB as a fallback: " + color);
         }

         ColorSpace sRGB = ColorSpace.getInstance(1000);
         comps = color.getColorComponents(sRGB, (float[])null);
      }

      if (ColorUtil.isGray(color)) {
         comps = new float[]{comps[0]};
         this.writeColor(codeBuffer, (float[])comps, 1, fill ? "g" : "G");
      } else {
         this.writeColor(codeBuffer, (float[])comps, 3, fill ? "rg" : "RG");
      }

   }

   private void establishDeviceCMYK(StringBuffer codeBuffer, Color color, boolean fill) {
      this.writeColor(codeBuffer, (Color)color, 4, fill ? "k" : "K");
   }

   private void writeColor(StringBuffer codeBuffer, Color color, int componentCount, String command) {
      float[] comps = color.getColorComponents((float[])null);
      this.writeColor(codeBuffer, comps, componentCount, command);
   }

   private void writeColor(StringBuffer codeBuffer, float[] comps, int componentCount, String command) {
      if (comps.length != componentCount) {
         throw new IllegalStateException("Color with unexpected component count encountered");
      } else {
         float[] var5 = comps;
         int var6 = comps.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            float comp = var5[var7];
            DoubleFormatUtil.formatDouble((double)comp, 4, 4, codeBuffer);
            codeBuffer.append(" ");
         }

         codeBuffer.append(command).append("\n");
      }
   }
}
