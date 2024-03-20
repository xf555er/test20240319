package org.apache.fop.fonts.autodetect;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fonts.FontEventListener;

public class FontFileFinder extends DirectoryWalker implements FontFinder {
   private final Log log;
   public static final int DEFAULT_DEPTH_LIMIT = -1;
   private final FontEventListener eventListener;

   public FontFileFinder(FontEventListener listener) {
      this(-1, listener);
   }

   public FontFileFinder(int depthLimit, FontEventListener listener) {
      super(getDirectoryFilter(), getFileFilter(), depthLimit);
      this.log = LogFactory.getLog(FontFileFinder.class);
      this.eventListener = listener;
   }

   protected static IOFileFilter getDirectoryFilter() {
      return FileFilterUtils.andFileFilter(FileFilterUtils.directoryFileFilter(), FileFilterUtils.notFileFilter(FileFilterUtils.prefixFileFilter(".")));
   }

   protected static IOFileFilter getFileFilter() {
      return FileFilterUtils.andFileFilter(FileFilterUtils.fileFileFilter(), new WildcardFileFilter(new String[]{"*.ttf", "*.otf", "*.pfb", "*.ttc"}, IOCase.INSENSITIVE));
   }

   protected boolean handleDirectory(File directory, int depth, Collection results) {
      return true;
   }

   protected void handleFile(File file, int depth, Collection results) {
      try {
         results.add(file.toURI().toURL());
      } catch (MalformedURLException var5) {
         this.log.debug("MalformedURLException" + var5.getMessage());
      }

   }

   protected void handleDirectoryEnd(File directory, int depth, Collection results) {
      if (this.log.isDebugEnabled()) {
         this.log.debug(directory + ": found " + results.size() + " font" + (results.size() == 1 ? "" : "s"));
      }

   }

   public List find() throws IOException {
      String osName = System.getProperty("os.name");
      Object fontDirFinder;
      if (osName.startsWith("Windows")) {
         fontDirFinder = new WindowsFontDirFinder();
      } else if (osName.startsWith("Mac")) {
         fontDirFinder = new MacFontDirFinder();
      } else {
         fontDirFinder = new UnixFontDirFinder();
      }

      List fontDirs = ((FontDirFinder)fontDirFinder).find();
      List results = new ArrayList();
      Iterator var5 = fontDirs.iterator();

      while(var5.hasNext()) {
         File dir = (File)var5.next();
         super.walk(dir, results);
      }

      return results;
   }

   public List find(String dir) throws IOException {
      List results = new ArrayList();
      File directory = new File(dir);
      if (!directory.isDirectory()) {
         this.eventListener.fontDirectoryNotFound(this, dir);
      } else {
         super.walk(directory, results);
      }

      return results;
   }
}
