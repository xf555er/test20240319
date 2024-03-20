package org.apache.fop.pdf;

import java.awt.color.ICC_Profile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.xmlgraphics.java2d.color.profile.ColorProfileUtil;

public class PDFICCBasedColorSpace extends PDFObject implements PDFColorSpace {
   private PDFICCStream iccStream;
   private String explicitName;
   private int numComponents;

   public PDFICCBasedColorSpace(String explicitName, PDFICCStream iccStream) {
      this.explicitName = explicitName;
      this.iccStream = iccStream;
      this.numComponents = iccStream.getICCProfile().getNumComponents();
   }

   public PDFICCBasedColorSpace(PDFICCStream iccStream) {
      this((String)null, iccStream);
   }

   public PDFICCStream getICCStream() {
      return this.iccStream;
   }

   public int getNumComponents() {
      return this.numComponents;
   }

   public String getName() {
      return this.explicitName != null ? this.explicitName : "ICC" + this.iccStream.getObjectNumber();
   }

   public boolean isDeviceColorSpace() {
      return false;
   }

   public boolean isRGBColorSpace() {
      return this.getNumComponents() == 3;
   }

   public boolean isCMYKColorSpace() {
      return this.getNumComponents() == 4;
   }

   public boolean isGrayColorSpace() {
      return this.getNumComponents() == 1;
   }

   protected String toPDFString() {
      StringBuffer sb = new StringBuffer(64);
      sb.append("[/ICCBased ").append(this.getICCStream().referencePDF()).append("]");
      return sb.toString();
   }

   public static PDFICCBasedColorSpace setupsRGBAsDefaultRGBColorSpace(PDFDocument pdfDoc) {
      PDFICCStream sRGBProfile = setupsRGBColorProfile(pdfDoc);
      return pdfDoc.getFactory().makeICCBasedColorSpace((PDFResourceContext)null, "DefaultRGB", sRGBProfile);
   }

   public static PDFICCBasedColorSpace setupsRGBColorSpace(PDFDocument pdfDoc) {
      PDFICCStream sRGBProfile = setupsRGBColorProfile(pdfDoc);
      return pdfDoc.getFactory().makeICCBasedColorSpace((PDFResourceContext)null, (String)null, sRGBProfile);
   }

   public static PDFICCStream setupsRGBColorProfile(PDFDocument pdfDoc) {
      PDFICCStream sRGBProfile = pdfDoc.getFactory().makePDFICCStream();
      InputStream in = PDFDocument.class.getResourceAsStream("sRGB.icc");
      ICC_Profile profile;
      if (in != null) {
         try {
            profile = ColorProfileUtil.getICC_Profile(in);
         } catch (IOException var8) {
            throw new RuntimeException("Unexpected IOException loading the sRGB profile: " + var8.getMessage());
         } finally {
            IOUtils.closeQuietly(in);
         }
      } else {
         profile = ColorProfileUtil.getICC_Profile(1000);
      }

      sRGBProfile.setColorSpace(profile, (PDFDeviceColorSpace)null);
      return sRGBProfile;
   }

   public void getChildren(Set children) {
      super.getChildren(children);
      children.add(this.iccStream);
      this.iccStream.getChildren(children);
   }
}
