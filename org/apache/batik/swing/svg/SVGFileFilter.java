package org.apache.batik.swing.svg;

import java.io.File;
import javax.swing.filechooser.FileFilter;

public class SVGFileFilter extends FileFilter {
   public boolean accept(File f) {
      boolean accept = false;
      String fileName = null;
      if (f != null) {
         if (f.isDirectory()) {
            accept = true;
         } else {
            fileName = f.getPath().toLowerCase();
            if (fileName.endsWith(".svg") || fileName.endsWith(".svgz")) {
               accept = true;
            }
         }
      }

      return accept;
   }

   public String getDescription() {
      return ".svg, .svgz";
   }
}
