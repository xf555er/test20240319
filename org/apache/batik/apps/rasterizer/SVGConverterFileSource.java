package org.apache.batik.apps.rasterizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;

public class SVGConverterFileSource implements SVGConverterSource {
   File file;
   String ref;

   public SVGConverterFileSource(File file) {
      this.file = file;
   }

   public SVGConverterFileSource(File file, String ref) {
      this.file = file;
      this.ref = ref;
   }

   public String getName() {
      String name = this.file.getName();
      if (this.ref != null && !"".equals(this.ref)) {
         name = name + '#' + this.ref;
      }

      return name;
   }

   public File getFile() {
      return this.file;
   }

   public String toString() {
      return this.getName();
   }

   public String getURI() {
      try {
         String uri = this.file.toURI().toURL().toString();
         if (this.ref != null && !"".equals(this.ref)) {
            uri = uri + '#' + this.ref;
         }

         return uri;
      } catch (MalformedURLException var2) {
         throw new RuntimeException(var2.getMessage());
      }
   }

   public boolean equals(Object o) {
      return o != null && o instanceof SVGConverterFileSource ? this.file.equals(((SVGConverterFileSource)o).file) : false;
   }

   public int hashCode() {
      return this.file.hashCode();
   }

   public InputStream openStream() throws FileNotFoundException {
      return new FileInputStream(this.file);
   }

   public boolean isSameAs(String srcStr) {
      return this.file.toString().equals(srcStr);
   }

   public boolean isReadable() {
      return this.file.canRead();
   }
}
