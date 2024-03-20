package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;

public class PDFTransitionAction extends PDFNavigatorAction {
   private Resolver resolver;

   public PDFTransitionAction(String id) {
      super(id);
      this.put("Type", new PDFName("Action"));
      this.put("S", new PDFName("Trans"));
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

   public void populate(Object transition, Object nextAction) {
      if (transition != null) {
         this.put("Trans", transition);
      }

      if (nextAction != null) {
         this.put("Next", nextAction);
      }

   }

   public abstract static class Resolver {
      private boolean resolved;
      private PDFTransitionAction action;
      private Object extension;

      public Resolver(PDFTransitionAction action, Object extension) {
         this.action = action;
         this.extension = extension;
      }

      public PDFTransitionAction getAction() {
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
