package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;

public class PDFNavigator extends PDFIdentifiedDictionary {
   private Resolver resolver;

   public PDFNavigator(String id) {
      super(id);
      this.put("Type", new PDFName("NavNode"));
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

   public void populate(Object nextAction, Object nextNode, Object prevAction, Object prevNode, Object duration) {
      if (nextAction != null) {
         this.put("NA", nextAction);
      }

      if (nextNode != null) {
         this.put("Next", nextNode);
      }

      if (prevAction != null) {
         this.put("PA", prevAction);
      }

      if (prevNode != null) {
         this.put("Prev", prevNode);
      }

      if (duration != null) {
         this.put("Dur", duration);
      }

   }

   public abstract static class Resolver {
      private boolean resolved;
      private PDFNavigator navigator;
      private Object extension;

      public Resolver(PDFNavigator navigator, Object extension) {
         this.navigator = navigator;
         this.extension = extension;
      }

      public PDFNavigator getNavigator() {
         return this.navigator;
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
