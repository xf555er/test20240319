package org.apache.fop.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PDFOutline extends PDFObject {
   private List subentries = new ArrayList();
   private PDFOutline parent = null;
   private PDFOutline prev = null;
   private PDFOutline next = null;
   private PDFOutline first = null;
   private PDFOutline last = null;
   private int count = 0;
   private boolean openItem;
   private String title;
   private PDFReference actionRef;

   public PDFOutline(String title, PDFReference action, boolean openItem) {
      this.title = title;
      this.actionRef = action;
      this.openItem = openItem;
   }

   public void setTitle(String t) {
      this.title = t;
   }

   public void addOutline(PDFOutline outline) {
      if (this.subentries.size() > 0) {
         outline.prev = (PDFOutline)this.subentries.get(this.subentries.size() - 1);
         outline.prev.next = outline;
      } else {
         this.first = outline;
      }

      this.subentries.add(outline);
      outline.parent = this;
      this.incrementCount();
      this.last = outline;
   }

   private void incrementCount() {
      ++this.count;
      if (this.parent != null) {
         this.parent.incrementCount();
      }

   }

   protected byte[] toPDF() {
      ByteArrayOutputStream bout = new ByteArrayOutputStream(128);

      try {
         bout.write(encode("<<"));
         if (this.parent == null) {
            if (this.first != null && this.last != null) {
               bout.write(encode(" /First " + this.first.referencePDF() + "\n"));
               bout.write(encode(" /Last " + this.last.referencePDF() + "\n"));
            }
         } else {
            bout.write(encode(" /Title "));
            bout.write(this.encodeText(this.title));
            bout.write(encode("\n"));
            bout.write(encode(" /Parent " + this.parent.referencePDF() + "\n"));
            if (this.prev != null) {
               bout.write(encode(" /Prev " + this.prev.referencePDF() + "\n"));
            }

            if (this.next != null) {
               bout.write(encode(" /Next " + this.next.referencePDF() + "\n"));
            }

            if (this.first != null && this.last != null) {
               bout.write(encode(" /First " + this.first.referencePDF() + "\n"));
               bout.write(encode(" /Last " + this.last.referencePDF() + "\n"));
            }

            if (this.count > 0) {
               bout.write(encode(" /Count " + (this.openItem ? "" : "-") + this.count + "\n"));
            }

            if (this.actionRef != null) {
               bout.write(encode(" /A " + this.actionRef + "\n"));
            }
         }

         bout.write(encode(">>"));
      } catch (IOException var3) {
         log.error("Ignored I/O exception", var3);
      }

      return bout.toByteArray();
   }

   public void getChildren(Set children) {
      if (this.parent != null) {
         children.add(this.parent);
      }

      if (this.first != null && this.last != null) {
         children.add(this.first);
         children.add(this.last);
         this.first.getChildren(children);
         this.last.getChildren(children);
      }

      if (this.actionRef != null) {
         children.add(this.actionRef.getObject());
      }

   }
}
