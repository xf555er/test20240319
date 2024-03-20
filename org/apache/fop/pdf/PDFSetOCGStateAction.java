package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;

public class PDFSetOCGStateAction extends PDFNavigatorAction {
   private Resolver resolver;

   public PDFSetOCGStateAction(String id) {
      super(id);
      this.put("Type", new PDFName("Action"));
      this.put("S", new PDFName("SetOCGState"));
   }

   public int output(OutputStream stream) throws IOException {
      if (this.resolver != null) {
         this.resolver.resolve();
      }

      return super.output(stream);
   }

   public void setResolver(Resolver resolver) {
      this.resolver = resolver;
   }

   public void populate(Object state, Object preserveRB, Object nextAction) {
      if (state != null) {
         this.put("State", state);
      }

      if (preserveRB != null) {
         this.put("PreserveRB", preserveRB);
      }

      if (nextAction != null) {
         this.put("Next", nextAction);
      }

   }

   public abstract static class Resolver {
      private boolean resolved;
      private PDFSetOCGStateAction action;
      private Object extension;

      public Resolver(PDFSetOCGStateAction action, Object extension) {
         this.action = action;
         this.extension = extension;
      }

      public PDFSetOCGStateAction getAction() {
         return this.action;
      }

      public Object getExtension() {
         return this.extension;
      }

      public void resolve() {
         if (!this.resolved) {
            this.performResolution();
            this.resolved = true;
         }

      }

      protected void performResolution() {
      }
   }
}
