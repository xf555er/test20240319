package org.apache.batik.svggen;

import java.awt.Rectangle;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.util.Arrays;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SVGLookupOp extends AbstractSVGFilterConverter {
   private static final double GAMMA = 0.4166666666666667;
   private static final int[] linearToSRGBLut = new int[256];
   private static final int[] sRGBToLinear = new int[256];

   public SVGLookupOp(SVGGeneratorContext generatorContext) {
      super(generatorContext);
   }

   public SVGFilterDescriptor toSVG(BufferedImageOp filter, Rectangle filterRect) {
      return filter instanceof LookupOp ? this.toSVG((LookupOp)filter) : null;
   }

   public SVGFilterDescriptor toSVG(LookupOp lookupOp) {
      SVGFilterDescriptor filterDesc = (SVGFilterDescriptor)this.descMap.get(lookupOp);
      Document domFactory = this.generatorContext.domFactory;
      if (filterDesc == null) {
         Element filterDef = domFactory.createElementNS("http://www.w3.org/2000/svg", "filter");
         Element feComponentTransferDef = domFactory.createElementNS("http://www.w3.org/2000/svg", "feComponentTransfer");
         String[] lookupTables = this.convertLookupTables(lookupOp);
         Element feFuncR = domFactory.createElementNS("http://www.w3.org/2000/svg", "feFuncR");
         Element feFuncG = domFactory.createElementNS("http://www.w3.org/2000/svg", "feFuncG");
         Element feFuncB = domFactory.createElementNS("http://www.w3.org/2000/svg", "feFuncB");
         Element feFuncA = null;
         String type = "table";
         if (lookupTables.length == 1) {
            feFuncR.setAttributeNS((String)null, "type", type);
            feFuncG.setAttributeNS((String)null, "type", type);
            feFuncB.setAttributeNS((String)null, "type", type);
            feFuncR.setAttributeNS((String)null, "tableValues", lookupTables[0]);
            feFuncG.setAttributeNS((String)null, "tableValues", lookupTables[0]);
            feFuncB.setAttributeNS((String)null, "tableValues", lookupTables[0]);
         } else if (lookupTables.length >= 3) {
            feFuncR.setAttributeNS((String)null, "type", type);
            feFuncG.setAttributeNS((String)null, "type", type);
            feFuncB.setAttributeNS((String)null, "type", type);
            feFuncR.setAttributeNS((String)null, "tableValues", lookupTables[0]);
            feFuncG.setAttributeNS((String)null, "tableValues", lookupTables[1]);
            feFuncB.setAttributeNS((String)null, "tableValues", lookupTables[2]);
            if (lookupTables.length == 4) {
               feFuncA = domFactory.createElementNS("http://www.w3.org/2000/svg", "feFuncA");
               feFuncA.setAttributeNS((String)null, "type", type);
               feFuncA.setAttributeNS((String)null, "tableValues", lookupTables[3]);
            }
         }

         feComponentTransferDef.appendChild(feFuncR);
         feComponentTransferDef.appendChild(feFuncG);
         feComponentTransferDef.appendChild(feFuncB);
         if (feFuncA != null) {
            feComponentTransferDef.appendChild(feFuncA);
         }

         filterDef.appendChild(feComponentTransferDef);
         filterDef.setAttributeNS((String)null, "id", this.generatorContext.idGenerator.generateID("componentTransfer"));
         String filterAttrBuf = "url(#" + filterDef.getAttributeNS((String)null, "id") + ")";
         filterDesc = new SVGFilterDescriptor(filterAttrBuf, filterDef);
         this.defSet.add(filterDef);
         this.descMap.put(lookupOp, filterDesc);
      }

      return filterDesc;
   }

   private String[] convertLookupTables(LookupOp lookupOp) {
      LookupTable lookupTable = lookupOp.getTable();
      int nComponents = lookupTable.getNumComponents();
      if (nComponents != 1 && nComponents != 3 && nComponents != 4) {
         throw new SVGGraphics2DRuntimeException("BufferedImage LookupOp should have 1, 3 or 4 lookup arrays");
      } else {
         StringBuffer[] lookupTableBuf = new StringBuffer[nComponents];

         for(int i = 0; i < nComponents; ++i) {
            lookupTableBuf[i] = new StringBuffer();
         }

         int offset;
         int i;
         int j;
         if (!(lookupTable instanceof ByteLookupTable)) {
            int[] src = new int[nComponents];
            int[] dest = new int[nComponents];
            offset = lookupTable.getOffset();

            for(i = 0; i < offset; ++i) {
               for(j = 0; j < nComponents; ++j) {
                  lookupTableBuf[j].append(this.doubleString((double)i / 255.0)).append(" ");
               }
            }

            for(i = offset; i <= 255; ++i) {
               Arrays.fill(src, i);
               lookupTable.lookupPixel(src, dest);

               for(j = 0; j < nComponents; ++j) {
                  lookupTableBuf[j].append(this.doubleString((double)dest[j] / 255.0)).append(" ");
               }
            }
         } else {
            byte[] src = new byte[nComponents];
            byte[] dest = new byte[nComponents];
            offset = lookupTable.getOffset();

            for(i = 0; i < offset; ++i) {
               for(j = 0; j < nComponents; ++j) {
                  lookupTableBuf[j].append(this.doubleString((double)i / 255.0)).append(" ");
               }
            }

            for(i = 0; i <= 255; ++i) {
               Arrays.fill(src, (byte)(255 & i));
               ((ByteLookupTable)lookupTable).lookupPixel(src, dest);

               for(j = 0; j < nComponents; ++j) {
                  lookupTableBuf[j].append(this.doubleString((double)(255 & dest[j]) / 255.0)).append(" ");
               }
            }
         }

         String[] lookupTables = new String[nComponents];

         for(int i = 0; i < nComponents; ++i) {
            lookupTables[i] = lookupTableBuf[i].toString().trim();
         }

         return lookupTables;
      }
   }

   static {
      for(int i = 0; i < 256; ++i) {
         float value = (float)i / 255.0F;
         if ((double)value <= 0.0031308) {
            value *= 12.92F;
         } else {
            value = 1.055F * (float)Math.pow((double)value, 0.4166666666666667) - 0.055F;
         }

         linearToSRGBLut[i] = Math.round(value * 255.0F);
         value = (float)i / 255.0F;
         if ((double)value <= 0.04045) {
            value /= 12.92F;
         } else {
            value = (float)Math.pow((double)((value + 0.055F) / 1.055F), 2.4);
         }

         sRGBToLinear[i] = Math.round(value * 255.0F);
      }

   }
}
