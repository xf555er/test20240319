package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;

public class PDFLayer extends PDFIdentifiedDictionary {
   private Resolver resolver;

   public PDFLayer(String id) {
      super(id);
      this.put("Type", new PDFName("OCG"));
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

   public void populate(Object name, Object intent, Object usage) {
      if (name != null) {
         this.put("Name", name);
      }

      if (intent != null) {
         this.put("Intent", intent);
      }

      if (usage != null) {
         this.put("Usage", usage);
      }

   }

   public abstract static class Resolver {
      private boolean resolved;
      private PDFLayer layer;
      private Object extension;

      public Resolver(PDFLayer layer, Object extension) {
         this.layer = layer;
         this.extension = extension;
      }

      public PDFLayer getLayer() {
         return this.layer;
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
