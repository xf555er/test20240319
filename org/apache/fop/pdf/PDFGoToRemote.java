package org.apache.fop.pdf;

public class PDFGoToRemote extends PDFAction {
   private PDFReference pdfFileSpec;
   private int pageReference;
   private String destination;
   private boolean newWindow;

   public PDFGoToRemote(PDFFileSpec pdfFileSpec, boolean newWindow) {
      this.pdfFileSpec = pdfFileSpec.makeReference();
      this.newWindow = newWindow;
   }

   public PDFGoToRemote(PDFFileSpec pdfFileSpec, int page, boolean newWindow) {
      this(pdfFileSpec.makeReference(), page, newWindow);
   }

   public PDFGoToRemote(PDFReference pdfFileSpec, int page, boolean newWindow) {
      this.pdfFileSpec = pdfFileSpec;
      this.pageReference = page;
      this.newWindow = newWindow;
   }

   public PDFGoToRemote(PDFFileSpec pdfFileSpec, String dest, boolean newWindow) {
      this.pdfFileSpec = pdfFileSpec.makeReference();
      this.destination = dest;
      this.newWindow = newWindow;
   }

   public String getAction() {
      return this.referencePDF();
   }

   public String toPDFString() {
      StringBuffer sb = new StringBuffer(64);
      sb.append("<<\n/S /GoToR\n/F ");
      sb.append(this.pdfFileSpec.toString());
      sb.append("\n");
      if (this.destination != null) {
         sb.append("/D (").append(this.destination).append(")");
      } else {
         sb.append("/D [ ").append(this.pageReference).append(" /XYZ null null null ]");
      }

      if (this.newWindow) {
         sb.append("/NewWindow true");
      }

      sb.append("\n>>");
      return sb.toString();
   }

   protected boolean contentEquals(PDFObject obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && obj instanceof PDFGoToRemote) {
         PDFGoToRemote remote = (PDFGoToRemote)obj;
         if (!remote.pdfFileSpec.toString().equals(this.pdfFileSpec.toString())) {
            return false;
         } else {
            if (this.destination != null) {
               if (!this.destination.equals(remote.destination)) {
                  return false;
               }
            } else if (this.pageReference != remote.pageReference) {
               return false;
            }

            return this.newWindow == remote.newWindow;
         }
      } else {
         return false;
      }
   }
}
