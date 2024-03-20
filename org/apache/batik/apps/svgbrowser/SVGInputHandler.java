package org.apache.batik.apps.svgbrowser;

import java.io.File;
import org.apache.batik.util.ParsedURL;

public class SVGInputHandler implements SquiggleInputHandler {
   public static final String[] SVG_MIME_TYPES = new String[]{"image/svg+xml"};
   public static final String[] SVG_FILE_EXTENSIONS = new String[]{".svg", ".svgz"};

   public String[] getHandledMimeTypes() {
      return SVG_MIME_TYPES;
   }

   public String[] getHandledExtensions() {
      return SVG_FILE_EXTENSIONS;
   }

   public String getDescription() {
      return "";
   }

   public void handle(ParsedURL purl, JSVGViewerFrame svgViewerFrame) {
      svgViewerFrame.getJSVGCanvas().loadSVGDocument(purl.toString());
   }

   public boolean accept(File f) {
      return f != null && f.isFile() && this.accept(f.getPath());
   }

   public boolean accept(ParsedURL purl) {
      if (purl == null) {
         return false;
      } else {
         String path = purl.getPath();
         return path == null ? false : this.accept(path);
      }
   }

   public boolean accept(String path) {
      if (path == null) {
         return false;
      } else {
         String[] var2 = SVG_FILE_EXTENSIONS;
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            String SVG_FILE_EXTENSION = var2[var4];
            if (path.endsWith(SVG_FILE_EXTENSION)) {
               return true;
            }
         }

         return false;
      }
   }
}
