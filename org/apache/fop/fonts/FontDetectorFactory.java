package org.apache.fop.fonts;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fonts.autodetect.FontFileFinder;
import org.apache.fop.util.LogUtil;
import org.apache.xmlgraphics.util.ClasspathResource;

public final class FontDetectorFactory {
   private FontDetectorFactory() {
   }

   public static FontDetector createDefault() {
      return new DefaultFontDetector();
   }

   public static FontDetector createDisabled() {
      return new DisabledFontDetector();
   }

   private static class DefaultFontDetector implements FontDetector {
      private static Log log = LogFactory.getLog(DefaultFontDetector.class);
      private static final String[] FONT_MIMETYPES = new String[]{"application/x-font", "application/x-font-truetype"};

      private DefaultFontDetector() {
      }

      public void detect(FontManager fontManager, FontAdder fontAdder, boolean strict, FontEventListener eventListener, List fontInfoList) throws FOPException {
         try {
            FontFileFinder fontFileFinder = new FontFileFinder(eventListener);
            URI fontBaseURI = fontManager.getResourceResolver().getBaseURI();
            File fontBase = FileUtils.toFile(fontBaseURI.toURL());
            List systemFontList;
            if (fontBase != null) {
               systemFontList = fontFileFinder.find(fontBase.getAbsolutePath());
               fontAdder.add(systemFontList, fontInfoList);
            }

            systemFontList = fontFileFinder.find();
            fontAdder.add(systemFontList, fontInfoList);
            ClasspathResource resource = ClasspathResource.getInstance();
            String[] var11 = FONT_MIMETYPES;
            int var12 = var11.length;

            for(int var13 = 0; var13 < var12; ++var13) {
               String mimeTypes = var11[var13];
               fontAdder.add(resource.listResourcesOfMimeType(mimeTypes), fontInfoList);
            }
         } catch (IOException var15) {
            LogUtil.handleException(log, var15, strict);
         } catch (URISyntaxException var16) {
            LogUtil.handleException(log, var16, strict);
         }

      }

      // $FF: synthetic method
      DefaultFontDetector(Object x0) {
         this();
      }
   }

   private static class DisabledFontDetector implements FontDetector {
      private DisabledFontDetector() {
      }

      public void detect(FontManager fontManager, FontAdder fontAdder, boolean strict, FontEventListener eventListener, List fontInfoList) throws FOPException {
      }

      // $FF: synthetic method
      DisabledFontDetector(Object x0) {
         this();
      }
   }
}
