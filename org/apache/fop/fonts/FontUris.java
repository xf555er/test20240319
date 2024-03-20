package org.apache.fop.fonts;

import java.io.Serializable;
import java.net.URI;

public class FontUris implements Serializable {
   private static final long serialVersionUID = 8571060588775532701L;
   private final URI embed;
   private final URI metrics;
   private final URI afm;
   private final URI pfm;

   public FontUris(URI embed, URI metrics, URI afm, URI pfm) {
      this.embed = embed;
      this.metrics = metrics;
      this.afm = afm;
      this.pfm = pfm;
   }

   public FontUris(URI embed, URI metrics) {
      this.embed = embed;
      this.metrics = metrics;
      this.afm = null;
      this.pfm = null;
   }

   public URI getEmbed() {
      return this.embed;
   }

   public URI getMetrics() {
      return this.metrics;
   }

   public URI getAfm() {
      return this.afm;
   }

   public URI getPfm() {
      return this.pfm;
   }
}
