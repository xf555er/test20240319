package net.jsign.poi.poifs.filesystem;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.jsign.poi.util.POILogFactory;
import net.jsign.poi.util.POILogger;

public class POIFSDocumentPath {
   private static final POILogger log = POILogFactory.getLogger(POIFSDocumentPath.class);
   private final String[] components;
   private int hashcode;

   public POIFSDocumentPath() {
      this.components = new String[0];
   }

   public POIFSDocumentPath(POIFSDocumentPath path, String[] components) throws IllegalArgumentException {
      String[] s1 = path == null ? new String[0] : path.components;
      String[] s2 = components == null ? new String[0] : components;
      Predicate p = path != null ? Objects::isNull : (s) -> {
         return s == null || s.isEmpty();
      };
      if (Stream.of(s2).anyMatch(p)) {
         throw new IllegalArgumentException("components cannot contain null or empty strings");
      } else {
         this.components = (String[])Stream.concat(Stream.of(s1), Stream.of(s2)).toArray((x$0) -> {
            return new String[x$0];
         });
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && o.getClass() == this.getClass()) {
         POIFSDocumentPath path = (POIFSDocumentPath)o;
         return Arrays.equals(this.components, path.components);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.hashcode == 0 ? (this.hashcode = Arrays.hashCode(this.components)) : this.hashcode;
   }

   public String toString() {
      return File.separatorChar + String.join(String.valueOf(File.separatorChar), this.components);
   }
}
