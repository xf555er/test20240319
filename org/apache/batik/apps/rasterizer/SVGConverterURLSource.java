package org.apache.batik.apps.rasterizer;

import java.io.IOException;
import java.io.InputStream;
import org.apache.batik.util.ParsedURL;

public class SVGConverterURLSource implements SVGConverterSource {
   protected static final String SVG_EXTENSION = ".svg";
   protected static final String SVGZ_EXTENSION = ".svgz";
   public static final String ERROR_INVALID_URL = "SVGConverterURLSource.error.invalid.url";
   ParsedURL purl;
   String name;

   public SVGConverterURLSource(String url) throws SVGConverterException {
      this.purl = new ParsedURL(url);
      String path = this.purl.getPath();
      int n = path.lastIndexOf(47);
      String file = path;
      if (n != -1) {
         file = path.substring(n + 1);
      }

      if (file.length() == 0) {
         int idx = path.lastIndexOf(47, n - 1);
         file = path.substring(idx + 1, n);
      }

      if (file.length() == 0) {
         throw new SVGConverterException("SVGConverterURLSource.error.invalid.url", new Object[]{url});
      } else {
         n = file.indexOf(63);
         String args = "";
         if (n != -1) {
            args = file.substring(n + 1);
            file = file.substring(0, n);
         }

         this.name = file;
         String ref = this.purl.getRef();
         if (ref != null && ref.length() != 0) {
            this.name = this.name + "_" + ref.hashCode();
         }

         if (args != null && args.length() != 0) {
            this.name = this.name + "_" + args.hashCode();
         }

      }
   }

   public String toString() {
      return this.purl.toString();
   }

   public String getURI() {
      return this.toString();
   }

   public boolean equals(Object o) {
      return o != null && o instanceof SVGConverterURLSource ? this.purl.equals(((SVGConverterURLSource)o).purl) : false;
   }

   public int hashCode() {
      return this.purl.hashCode();
   }

   public InputStream openStream() throws IOException {
      return this.purl.openStream();
   }

   public boolean isSameAs(String srcStr) {
      return this.toString().equals(srcStr);
   }

   public boolean isReadable() {
      return true;
   }

   public String getName() {
      return this.name;
   }
}
