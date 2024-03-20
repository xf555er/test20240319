package org.apache.fop.fonts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.events.EventProducer;
import org.apache.fop.util.LogUtil;

public final class DefaultFontConfig implements FontConfig {
   private static final Log log = LogFactory.getLog(DefaultFontConfig.class);
   private final List directories;
   private final List fonts;
   private final List referencedFontFamilies;
   private final boolean autoDetectFonts;

   private DefaultFontConfig(boolean autoDetectFonts) {
      this.directories = new ArrayList();
      this.fonts = new ArrayList();
      this.referencedFontFamilies = new ArrayList();
      this.autoDetectFonts = autoDetectFonts;
   }

   public List getFonts() {
      return Collections.unmodifiableList(this.fonts);
   }

   public List getDirectories() {
      return Collections.unmodifiableList(this.directories);
   }

   public List getReferencedFontFamily() {
      return Collections.unmodifiableList(this.referencedFontFamilies);
   }

   public boolean isAutoDetectFonts() {
      return this.autoDetectFonts;
   }

   // $FF: synthetic method
   DefaultFontConfig(boolean x0, Object x1) {
      this(x0);
   }

   public static final class Font {
      private final String metrics;
      private final String embedUri;
      private String afm;
      private String pfm;
      private final String subFont;
      private final boolean kerning;
      private final boolean advanced;
      private final String encodingMode;
      private final String embeddingMode;
      private final boolean embedAsType1;
      private final boolean simulateStyle;
      private final boolean useSVG;
      private final List tripletList;

      public String getEncodingMode() {
         return this.encodingMode;
      }

      public List getTripletList() {
         return Collections.unmodifiableList(this.tripletList);
      }

      private Font(String metrics, String embed, String afm, String pfm, String subFont, boolean kerning, boolean advanced, String encodingMode, String embeddingMode, boolean simulateStyle, boolean embedAsType1, boolean useSVG) {
         this.tripletList = new ArrayList();
         this.metrics = metrics;
         this.embedUri = embed;
         this.afm = afm;
         this.pfm = pfm;
         this.subFont = subFont;
         this.kerning = kerning;
         this.advanced = advanced;
         this.encodingMode = encodingMode;
         this.embeddingMode = embeddingMode;
         this.simulateStyle = simulateStyle;
         this.embedAsType1 = embedAsType1;
         this.useSVG = useSVG;
      }

      public boolean isKerning() {
         return this.kerning;
      }

      public boolean isAdvanced() {
         return this.advanced;
      }

      public String getMetrics() {
         return this.metrics;
      }

      public String getEmbedURI() {
         return this.embedUri;
      }

      public String getSubFont() {
         return this.subFont;
      }

      public String getEmbeddingMode() {
         return this.embeddingMode;
      }

      public String getAfm() {
         return this.afm;
      }

      public String getPfm() {
         return this.pfm;
      }

      public boolean getSimulateStyle() {
         return this.simulateStyle;
      }

      public boolean getEmbedAsType1() {
         return this.embedAsType1;
      }

      public boolean getUseSVG() {
         return this.useSVG;
      }

      // $FF: synthetic method
      Font(String x0, String x1, String x2, String x3, String x4, boolean x5, boolean x6, String x7, String x8, boolean x9, boolean x10, boolean x11, Object x12) {
         this(x0, x1, x2, x3, x4, x5, x6, x7, x8, x9, x10, x11);
      }
   }

   public static final class Directory {
      private final String directory;
      private final boolean recursive;

      private Directory(String directory, boolean recurse) {
         this.directory = directory;
         this.recursive = recurse;
      }

      public String getDirectory() {
         return this.directory;
      }

      public boolean isRecursive() {
         return this.recursive;
      }

      // $FF: synthetic method
      Directory(String x0, boolean x1, Object x2) {
         this(x0, x1);
      }
   }

   private static final class ParserHelper {
      private boolean strict;
      private Configuration config;
      private Configuration fontInfoCfg;
      private FontEventAdapter eventAdapter;
      private DefaultFontConfig instance;

      private ParserHelper(Configuration cfg, boolean strict) throws FOPException {
         this(cfg, strict, (FontEventAdapter)null);
      }

      private ParserHelper(Configuration cfg, boolean strict, FontEventAdapter eventAdapter) throws FOPException {
         this.eventAdapter = eventAdapter;
         if (cfg != null && cfg.getChild("fonts", false) != null) {
            this.strict = strict;
            this.config = cfg;
            this.fontInfoCfg = cfg.getChild("fonts", false);
            this.instance = new DefaultFontConfig(this.fontInfoCfg.getChild("auto-detect", false) != null);
            this.parse();
         } else {
            this.instance = null;
         }

      }

      private void parse() throws FOPException {
         this.parseFonts();
         this.parseReferencedFonts();
         this.parseDirectories();
      }

      private void parseFonts() throws FOPException {
         Configuration[] var1 = this.fontInfoCfg.getChildren("font");
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            Configuration fontCfg = var1[var3];
            String embed = fontCfg.getAttribute("embed-url", (String)null);
            if (embed == null) {
               LogUtil.handleError(DefaultFontConfig.log, "Font configuration without embed-url attribute", this.strict);
            } else {
               Font font = new Font(fontCfg.getAttribute("metrics-url", (String)null), embed, fontCfg.getAttribute("embed-url-afm", (String)null), fontCfg.getAttribute("embed-url-pfm", (String)null), fontCfg.getAttribute("sub-font", (String)null), fontCfg.getAttributeAsBoolean("kerning", true), fontCfg.getAttributeAsBoolean("advanced", true), fontCfg.getAttribute("encoding-mode", EncodingMode.AUTO.getName()), fontCfg.getAttribute("embedding-mode", EncodingMode.AUTO.getName()), fontCfg.getAttributeAsBoolean("simulate-style", false), fontCfg.getAttributeAsBoolean("embed-as-type1", false), fontCfg.getAttributeAsBoolean("svg", true));
               this.instance.fonts.add(font);
               boolean hasTriplets = false;
               Configuration[] var8 = fontCfg.getChildren("font-triplet");
               int var9 = var8.length;

               for(int var10 = 0; var10 < var9; ++var10) {
                  Configuration tripletCfg = var8[var10];
                  FontTriplet fontTriplet = this.getFontTriplet(tripletCfg, this.strict);
                  font.tripletList.add(fontTriplet);
                  hasTriplets = true;
               }

               if (!hasTriplets) {
                  LogUtil.handleError(DefaultFontConfig.log, "font without font-triplet", this.strict);
               }

               try {
                  if (this.eventAdapter != null && font.getSimulateStyle() && !this.config.getAttribute("mime").equals("application/pdf")) {
                     this.eventAdapter.fontFeatureNotSuppprted(this, "simulate-style", "PDF");
                  }

                  if (this.eventAdapter != null && font.getEmbedAsType1() && !this.config.getAttribute("mime").equals("application/postscript")) {
                     throw new FOPException("The embed-as-type1 attribute is only supported in postscript");
                  }
               } catch (ConfigurationException var13) {
                  LogUtil.handleException(DefaultFontConfig.log, var13, true);
               }
            }
         }

      }

      private void parseReferencedFonts() throws FOPException {
         Configuration referencedFontsCfg = this.fontInfoCfg.getChild("referenced-fonts", false);
         if (referencedFontsCfg != null) {
            Configuration[] var2 = referencedFontsCfg.getChildren("match");
            int var3 = var2.length;

            for(int var4 = 0; var4 < var3; ++var4) {
               Configuration match = var2[var4];

               try {
                  this.instance.referencedFontFamilies.add(match.getAttribute("font-family"));
               } catch (ConfigurationException var7) {
                  LogUtil.handleException(DefaultFontConfig.log, var7, this.strict);
               }
            }
         }

      }

      private void parseDirectories() throws FOPException {
         Configuration[] var1 = this.fontInfoCfg.getChildren("directory");
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            Configuration directoriesCfg = var1[var3];
            boolean recursive = directoriesCfg.getAttributeAsBoolean("recursive", false);

            String directory;
            try {
               directory = directoriesCfg.getValue();
            } catch (ConfigurationException var8) {
               LogUtil.handleException(DefaultFontConfig.log, var8, this.strict);
               continue;
            }

            if (directory == null) {
               LogUtil.handleException(DefaultFontConfig.log, new FOPException("directory defined without value"), this.strict);
            } else {
               this.instance.directories.add(new Directory(directory, recursive));
            }
         }

      }

      private FontTriplet getFontTriplet(Configuration tripletCfg, boolean strict) throws FOPException {
         try {
            String name = tripletCfg.getAttribute("name");
            if (name == null) {
               LogUtil.handleError(DefaultFontConfig.log, "font-triplet without name", strict);
               return null;
            } else {
               String weightStr = tripletCfg.getAttribute("weight");
               if (weightStr == null) {
                  LogUtil.handleError(DefaultFontConfig.log, "font-triplet without weight", strict);
                  return null;
               } else {
                  int weight = FontUtil.parseCSS2FontWeight(FontUtil.stripWhiteSpace(weightStr));
                  String style = tripletCfg.getAttribute("style");
                  if (style == null) {
                     LogUtil.handleError(DefaultFontConfig.log, "font-triplet without style", strict);
                     return null;
                  } else {
                     style = FontUtil.stripWhiteSpace(style);
                     return FontInfo.createFontKey(name, style, weight);
                  }
               }
            }
         } catch (ConfigurationException var7) {
            LogUtil.handleException(DefaultFontConfig.log, var7, strict);
            return null;
         }
      }

      // $FF: synthetic method
      ParserHelper(Configuration x0, boolean x1, Object x2) throws FOPException {
         this(x0, x1);
      }

      // $FF: synthetic method
      ParserHelper(Configuration x0, boolean x1, FontEventAdapter x2, Object x3) throws FOPException {
         this(x0, x1, x2);
      }
   }

   public static final class DefaultFontConfigParser implements FontConfig.FontConfigParser {
      public DefaultFontConfig parse(Configuration cfg, boolean strict) throws FOPException {
         return (new ParserHelper(cfg, strict)).instance;
      }

      public DefaultFontConfig parse(Configuration cfg, boolean strict, FontEventAdapter eventAdapter) throws FOPException {
         return (new ParserHelper(cfg, strict, eventAdapter)).instance;
      }

      public FontConfig parse(Configuration cfg, FontManager fontManager, boolean strict, EventProducer eventProducer) throws FOPException {
         return this.parse(cfg, strict);
      }
   }
}
