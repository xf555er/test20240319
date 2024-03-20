package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import org.apache.fop.fonts.CIDFontType;

public class PDFCIDFont extends PDFObject {
   private String basefont;
   private CIDFontType cidtype;
   private Integer dw;
   private PDFWArray w;
   private int[] dw2;
   private PDFWArray w2;
   private PDFCIDSystemInfo systemInfo;
   private PDFCIDFontDescriptor descriptor;
   private PDFCMap cmap;
   private PDFStream cidMap;

   public PDFCIDFont(String basefont, CIDFontType cidtype, int dw, int[] w, String registry, String ordering, int supplement, PDFCIDFontDescriptor descriptor) {
      this(basefont, cidtype, dw, new PDFWArray(w), new PDFCIDSystemInfo(registry, ordering, supplement), descriptor);
   }

   public PDFCIDFont(String basefont, CIDFontType cidtype, int dw, int[] w, PDFCIDSystemInfo systemInfo, PDFCIDFontDescriptor descriptor) {
      this(basefont, cidtype, dw, new PDFWArray(w), systemInfo, descriptor);
   }

   public PDFCIDFont(String basefont, CIDFontType cidtype, int dw, PDFWArray w, PDFCIDSystemInfo systemInfo, PDFCIDFontDescriptor descriptor) {
      this.basefont = basefont;
      this.cidtype = cidtype;
      this.dw = dw;
      this.w = w;
      this.dw2 = null;
      this.w2 = null;
      systemInfo.setParent(this);
      this.systemInfo = systemInfo;
      this.descriptor = descriptor;
      this.cidMap = null;
      this.cmap = null;
   }

   public void setDW(int dw) {
      this.dw = dw;
   }

   public void setW(PDFWArray w) {
      this.w = w;
   }

   public void setDW2(int[] dw2) {
      this.dw2 = dw2;
   }

   public void setDW2(int posY, int displacementY) {
      this.dw2 = new int[]{posY, displacementY};
   }

   public void setCMAP(PDFCMap cmap) {
      this.cmap = cmap;
   }

   public void setW2(PDFWArray w2) {
      this.w2 = w2;
   }

   public void setCIDMap(PDFStream map) {
      this.cidMap = map;
   }

   public void setCIDMapIdentity() {
      this.cidMap = null;
   }

   protected String getPDFNameForCIDFontType(CIDFontType cidFontType) {
      if (cidFontType == CIDFontType.CIDTYPE0) {
         return cidFontType.getName();
      } else if (cidFontType == CIDFontType.CIDTYPE2) {
         return cidFontType.getName();
      } else {
         throw new IllegalArgumentException("Unsupported CID font type: " + cidFontType.getName());
      }
   }

   public String toPDFString() {
      StringBuffer p = new StringBuffer(128);
      p.append("<< /Type /Font");
      p.append("\n/BaseFont /");
      p.append(this.basefont);
      p.append(" \n/CIDToGIDMap ");
      if (this.cidMap != null) {
         p.append(this.cidMap.referencePDF());
      } else {
         p.append("/Identity");
      }

      p.append(" \n/Subtype /");
      p.append(this.getPDFNameForCIDFontType(this.cidtype));
      p.append("\n");
      p.append(this.systemInfo.toPDFString());
      p.append("\n/FontDescriptor ");
      p.append(this.descriptor.referencePDF());
      if (this.cmap != null) {
         p.append("\n/ToUnicode ");
         p.append(this.cmap.referencePDF());
      }

      if (this.dw != null) {
         p.append("\n/DW ");
         p.append(this.dw);
      }

      if (this.w != null) {
         p.append("\n/W ");
         p.append(this.w.toPDFString());
      }

      if (this.dw2 != null) {
         p.append("\n/DW2 [");
         p.append(this.dw2[0]);
         p.append(this.dw2[1]);
         p.append("]");
      }

      if (this.w2 != null) {
         p.append("\n/W2 ");
         p.append(this.w2.toPDFString());
      }

      p.append("\n>>");
      return p.toString();
   }

   public byte[] toPDF() {
      ByteArrayOutputStream bout = new ByteArrayOutputStream(128);

      try {
         bout.write(encode("<< /Type /Font\n"));
         bout.write(encode("/BaseFont /"));
         bout.write(encode(this.basefont));
         bout.write(encode(" \n"));
         bout.write(encode("/CIDToGIDMap "));
         bout.write(encode(this.cidMap != null ? this.cidMap.referencePDF() : "/Identity"));
         bout.write(encode(" \n"));
         bout.write(encode("/Subtype /"));
         bout.write(encode(this.getPDFNameForCIDFontType(this.cidtype)));
         bout.write(encode("\n"));
         bout.write(encode("/CIDSystemInfo "));
         bout.write(this.systemInfo.toPDF());
         bout.write(encode("\n"));
         bout.write(encode("/FontDescriptor "));
         bout.write(encode(this.descriptor.referencePDF()));
         bout.write(encode("\n"));
         if (this.cmap != null) {
            bout.write(encode("/ToUnicode "));
            bout.write(encode(this.cmap.referencePDF()));
            bout.write(encode("\n"));
         }

         if (this.dw != null) {
            bout.write(encode("/DW "));
            bout.write(encode(this.dw.toString()));
            bout.write(encode("\n"));
         }

         if (this.w != null) {
            bout.write(encode("/W "));
            bout.write(encode(this.w.toPDFString()));
            bout.write(encode("\n"));
         }

         if (this.dw2 != null) {
            bout.write(encode("/DW2 ["));
            bout.write(encode(Integer.toString(this.dw2[0])));
            bout.write(encode(Integer.toString(this.dw2[1])));
            bout.write(encode("]\n"));
         }

         if (this.w2 != null) {
            bout.write(encode("/W2 "));
            bout.write(encode(this.w2.toPDFString()));
            bout.write(encode("\n"));
         }

         bout.write(encode(">>"));
      } catch (IOException var3) {
         log.error("Ignored I/O exception", var3);
      }

      return bout.toByteArray();
   }

   public void getChildren(Set children) {
      super.getChildren(children);
      if (this.cidMap != null) {
         children.add(this.cidMap);
         this.cidMap.getChildren(children);
      }

      children.add(this.descriptor);
      this.descriptor.getChildren(children);
      if (this.cmap != null) {
         children.add(this.cmap);
         this.cmap.getChildren(children);
      }

   }
}
