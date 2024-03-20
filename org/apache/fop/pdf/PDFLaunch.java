package org.apache.fop.pdf;

public class PDFLaunch extends PDFAction {
   private PDFReference externalFileSpec;
   private boolean newWindow;

   public PDFLaunch(PDFFileSpec fileSpec) {
      this(fileSpec.makeReference());
      this.newWindow = false;
   }

   public PDFLaunch(PDFFileSpec fileSpec, boolean newWindow) {
      this(fileSpec.makeReference());
      this.newWindow = newWindow;
   }

   public PDFLaunch(PDFReference fileSpec) {
      PDFObject fs = fileSpec.getObject();

      assert fs == null || fs instanceof PDFFileSpec;

      this.externalFileSpec = fileSpec;
   }

   public String getAction() {
      return this.referencePDF();
   }

   public String toPDFString() {
      StringBuffer sb = new StringBuffer(64);
      sb.append("<<\n/S /Launch\n/F ");
      sb.append(this.externalFileSpec.toString());
      if (this.newWindow) {
         sb.append("\n/NewWindow true");
      }

      sb.append("\n>>");
      return sb.toString();
   }

   protected boolean contentEquals(PDFObject obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && obj instanceof PDFLaunch) {
         PDFLaunch launch = (PDFLaunch)obj;
         return launch.externalFileSpec.toString().equals(this.externalFileSpec.toString());
      } else {
         return false;
      }
   }
}
