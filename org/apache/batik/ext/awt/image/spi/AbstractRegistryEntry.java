package org.apache.batik.ext.awt.image.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractRegistryEntry implements RegistryEntry, ErrorConstants {
   String name;
   float priority;
   List exts;
   List mimeTypes;

   public AbstractRegistryEntry(String name, float priority, String[] exts, String[] mimeTypes) {
      this.name = name;
      this.priority = priority;
      this.exts = new ArrayList(exts.length);
      String[] var5 = exts;
      int var6 = exts.length;

      int var7;
      String mimeType;
      for(var7 = 0; var7 < var6; ++var7) {
         mimeType = var5[var7];
         this.exts.add(mimeType);
      }

      this.exts = Collections.unmodifiableList(this.exts);
      this.mimeTypes = new ArrayList(mimeTypes.length);
      var5 = mimeTypes;
      var6 = mimeTypes.length;

      for(var7 = 0; var7 < var6; ++var7) {
         mimeType = var5[var7];
         this.mimeTypes.add(mimeType);
      }

      this.mimeTypes = Collections.unmodifiableList(this.mimeTypes);
   }

   public AbstractRegistryEntry(String name, float priority, String ext, String mimeType) {
      this.name = name;
      this.priority = priority;
      this.exts = new ArrayList(1);
      this.exts.add(ext);
      this.exts = Collections.unmodifiableList(this.exts);
      this.mimeTypes = new ArrayList(1);
      this.mimeTypes.add(mimeType);
      this.mimeTypes = Collections.unmodifiableList(this.mimeTypes);
   }

   public String getFormatName() {
      return this.name;
   }

   public List getStandardExtensions() {
      return this.exts;
   }

   public List getMimeTypes() {
      return this.mimeTypes;
   }

   public float getPriority() {
      return this.priority;
   }
}
