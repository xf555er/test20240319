package org.apache.fop.pdf;

import java.awt.geom.Point2D;

public class PDFGoTo extends PDFAction {
   private PDFReference pageReference;
   private String destination;
   private float xPosition;
   private float yPosition;
   private boolean isNamedDestination;

   public PDFGoTo(String destination, boolean isNamedDestination) {
      this.destination = destination;
      this.isNamedDestination = isNamedDestination;
   }

   public PDFGoTo(String pageReference) {
      if (pageReference != null) {
         this.setPageReference(new PDFReference(pageReference));
      }

   }

   public PDFGoTo(String pageReference, Point2D position) {
      this(pageReference);
      this.setPosition(position);
   }

   public void setPageReference(PDFReference pageReference) {
      this.pageReference = pageReference;
   }

   public void setPosition(Point2D position) {
      this.xPosition = (float)position.getX();
      this.yPosition = (float)position.getY();
   }

   public void setXPosition(float xPosition) {
      this.xPosition = xPosition;
   }

   public void setYPosition(float yPosition) {
      this.yPosition = yPosition;
   }

   public void setDestination(String dest) {
      this.destination = dest;
   }

   public String getAction() {
      return this.referencePDF();
   }

   public String toPDFString() {
      String dest;
      if (this.destination == null) {
         dest = "/D [" + this.pageReference + " /XYZ " + this.xPosition + " " + this.yPosition + " null]\n";
      } else {
         dest = "/D [" + this.pageReference + " " + this.destination + "]\n";
         if (this.isNamedDestination) {
            dest = "/D (" + this.destination + ")\n";
         } else {
            dest = "/D [" + this.pageReference + " " + this.destination + "]\n";
         }
      }

      return "<< /Type /Action\n/S /GoTo\n" + dest + ">>";
   }

   protected boolean contentEquals(PDFObject obj) {
      if (this == obj) {
         return true;
      } else if (obj != null && obj instanceof PDFGoTo) {
         PDFGoTo gt = (PDFGoTo)obj;
         if (gt.pageReference == null) {
            if (this.pageReference != null) {
               return false;
            }
         } else if (!gt.pageReference.equals(this.pageReference)) {
            return false;
         }

         if (this.destination == null) {
            if (gt.destination != null || gt.xPosition != this.xPosition || gt.yPosition != this.yPosition) {
               return false;
            }
         } else if (!this.destination.equals(gt.destination)) {
            return false;
         }

         return this.isNamedDestination == gt.isNamedDestination;
      } else {
         return false;
      }
   }
}
