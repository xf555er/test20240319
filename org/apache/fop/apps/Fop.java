package org.apache.fop.apps;

import java.io.OutputStream;
import org.apache.fop.fo.FOTreeBuilder;
import org.xml.sax.helpers.DefaultHandler;

public class Fop {
   private String outputFormat;
   private OutputStream stream;
   private final FOUserAgent foUserAgent;
   private FOTreeBuilder foTreeBuilder;

   Fop(String outputFormat, FOUserAgent ua, OutputStream stream) throws FOPException {
      if (ua == null) {
         throw new FOPException("Cannot create a new Fop instance without a User Agent.");
      } else {
         this.outputFormat = outputFormat;
         this.foUserAgent = ua;
         this.stream = stream;
         this.createDefaultHandler();
      }
   }

   /** @deprecated */
   public FOUserAgent getUserAgent() {
      return this.foUserAgent;
   }

   private void createDefaultHandler() throws FOPException {
      this.foTreeBuilder = new FOTreeBuilder(this.outputFormat, this.foUserAgent, this.stream);
   }

   public DefaultHandler getDefaultHandler() throws FOPException {
      if (this.foTreeBuilder == null) {
         this.createDefaultHandler();
      }

      return this.foTreeBuilder;
   }

   public FormattingResults getResults() {
      if (this.foTreeBuilder == null) {
         throw new IllegalStateException("Results are only available after calling getDefaultHandler().");
      } else {
         return this.foTreeBuilder.getResults();
      }
   }
}
