package org.apache.fop.render.afp;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.afp.fonts.AFPFont;
import org.apache.fop.afp.fonts.AFPFontInfo;
import org.apache.fop.afp.fonts.CharacterSet;
import org.apache.fop.afp.fonts.CharacterSetBuilder;
import org.apache.fop.afp.fonts.CharacterSetType;
import org.apache.fop.afp.fonts.DoubleByteFont;
import org.apache.fop.afp.fonts.OutlineFont;
import org.apache.fop.afp.fonts.RasterFont;
import org.apache.fop.afp.util.AFPResourceAccessor;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.configuration.ConfigurationException;
import org.apache.fop.events.EventProducer;
import org.apache.fop.fonts.EmbedFontInfo;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.EncodingMode;
import org.apache.fop.fonts.FontConfig;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.FontManagerConfigurator;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.FontUris;
import org.apache.fop.fonts.FontUtil;
import org.apache.fop.fonts.LazyFont;
import org.apache.fop.fonts.Typeface;

public final class AFPFontConfig implements FontConfig {
   private static final Log LOG = LogFactory.getLog(AFPFontConfig.class);
   private final List fontsConfig;

   private AFPFontConfig() {
      this.fontsConfig = new ArrayList();
   }

   public List getFontConfig() {
      return this.fontsConfig;
   }

   private static Typeface getTypeFace(String base14Name) throws ClassNotFoundException {
      try {
         Class clazz = Class.forName("org.apache.fop.fonts.base14." + base14Name).asSubclass(Typeface.class);
         return (Typeface)clazz.getDeclaredConstructor().newInstance();
      } catch (IllegalAccessException var2) {
         LOG.error(var2.getMessage());
      } catch (ClassNotFoundException var3) {
         LOG.error(var3.getMessage());
      } catch (InstantiationException var4) {
         LOG.error(var4.getMessage());
      } catch (NoSuchMethodException var5) {
         LOG.error(var5.getMessage());
      } catch (InvocationTargetException var6) {
         LOG.error(var6.getMessage());
      }

      throw new ClassNotFoundException("Couldn't load file for AFP font with base14 name: " + base14Name);
   }

   // $FF: synthetic method
   AFPFontConfig(Object x0) {
      this();
   }

   static final class RasterCharactersetData {
      private final String characterset;
      private final int size;
      private final String base14;

      private RasterCharactersetData(String characterset, int size, String base14) {
         this.characterset = characterset;
         this.size = size;
         this.base14 = base14;
      }

      // $FF: synthetic method
      RasterCharactersetData(String x0, int x1, String x2, Object x3) {
         this(x0, x1, x2);
      }
   }

   static final class RasterFontConfig extends AFPFontConfigData {
      private final List charsets;

      private RasterFontConfig(List triplets, String type, String codePage, String encoding, String characterset, String name, String uri, List csetData, boolean embeddable) {
         super(triplets, type, codePage, encoding, name, embeddable, uri);
         this.charsets = Collections.unmodifiableList(csetData);
      }

      AFPFontInfo getFontInfo(InternalResourceResolver resourceResolver, AFPEventProducer eventProducer) throws IOException {
         RasterFont rasterFont = new RasterFont(super.name, super.embeddable);
         Iterator var4 = this.charsets.iterator();

         while(var4.hasNext()) {
            RasterCharactersetData charset = (RasterCharactersetData)var4.next();
            if (charset.base14 != null) {
               String msg;
               try {
                  Typeface tf = AFPFontConfig.getTypeFace(charset.base14);
                  rasterFont.addCharacterSet(charset.size, CharacterSetBuilder.getSingleByteInstance().build(charset.characterset, super.codePage, super.encoding, tf, eventProducer));
               } catch (ClassNotFoundException var8) {
                  msg = "The base 14 font class for " + charset.characterset + " could not be found";
                  AFPFontConfig.LOG.error(msg);
               } catch (IOException var9) {
                  msg = "The base 14 font class " + charset.characterset + " could not be instantiated";
                  AFPFontConfig.LOG.error(msg);
               }
            } else {
               AFPResourceAccessor accessor = this.getAccessor(resourceResolver);
               rasterFont.addCharacterSet(charset.size, CharacterSetBuilder.getSingleByteInstance().buildSBCS(charset.characterset, super.codePage, super.encoding, accessor, eventProducer));
            }
         }

         return getFontInfo(rasterFont, this);
      }

      // $FF: synthetic method
      RasterFontConfig(List x0, String x1, String x2, String x3, String x4, String x5, String x6, List x7, boolean x8, Object x9) {
         this(x0, x1, x2, x3, x4, x5, x6, x7, x8);
      }
   }

   static final class OutlineFontConfig extends AFPFontConfigData {
      private final String base14;
      private final String characterset;

      private OutlineFontConfig(List triplets, String type, String codePage, String encoding, String characterset, String name, String base14, boolean embeddable, String uri) {
         super(triplets, type, codePage, encoding, name, embeddable, uri);
         this.characterset = characterset;
         this.base14 = base14;
      }

      AFPFontInfo getFontInfo(InternalResourceResolver resourceResolver, AFPEventProducer eventProducer) throws IOException {
         CharacterSet characterSet = null;
         if (this.base14 != null) {
            try {
               Typeface tf = AFPFontConfig.getTypeFace(this.base14);
               characterSet = CharacterSetBuilder.getSingleByteInstance().build(this.characterset, super.codePage, super.encoding, tf, eventProducer);
            } catch (ClassNotFoundException var6) {
               String msg = "The base 14 font class for " + this.characterset + " could not be found";
               AFPFontConfig.LOG.error(msg);
            }
         } else {
            AFPResourceAccessor accessor = this.getAccessor(resourceResolver);
            characterSet = CharacterSetBuilder.getSingleByteInstance().buildSBCS(this.characterset, super.codePage, super.encoding, accessor, eventProducer);
         }

         return getFontInfo(new OutlineFont(super.name, super.embeddable, characterSet, eventProducer), this);
      }

      // $FF: synthetic method
      OutlineFontConfig(List x0, String x1, String x2, String x3, String x4, String x5, String x6, boolean x7, String x8, Object x9) {
         this(x0, x1, x2, x3, x4, x5, x6, x7, x8);
      }
   }

   public static class AFPTrueTypeFont extends OutlineFont {
      private String ttc;
      private URI uri;

      public AFPTrueTypeFont(String name, boolean embeddable, CharacterSet charSet, AFPEventProducer eventProducer, String ttc, URI uri) {
         super(name, embeddable, charSet, eventProducer);
         this.ttc = ttc;
         this.uri = uri;
      }

      public FontType getFontType() {
         return FontType.TRUETYPE;
      }

      public String getTTC() {
         return this.ttc;
      }

      public URI getUri() {
         return this.uri;
      }
   }

   static final class TrueTypeFontConfig extends AFPFontConfigData {
      private String characterset;
      private String subfont;
      private String fontUri;

      private TrueTypeFontConfig(List triplets, String type, String codePage, String encoding, String characterset, String name, String subfont, boolean embeddable, String uri) {
         super(triplets, type, codePage, encoding, name, embeddable, (String)null);
         this.characterset = characterset;
         this.subfont = subfont;
         this.fontUri = uri;
      }

      AFPFontInfo getFontInfo(InternalResourceResolver resourceResolver, AFPEventProducer eventProducer) throws IOException {
         try {
            FontUris fontUris = new FontUris(new URI(this.fontUri), (URI)null);
            EmbedFontInfo embedFontInfo = new EmbedFontInfo(fontUris, false, true, (List)null, this.subfont, EncodingMode.AUTO, EmbeddingMode.FULL, false, false, true);
            Typeface tf = (new LazyFont(embedFontInfo, resourceResolver, false)).getRealFont();
            AFPResourceAccessor accessor = this.getAccessor(resourceResolver);
            CharacterSet characterSet = CharacterSetBuilder.getDoubleByteInstance().build(this.characterset, super.codePage, super.encoding, tf, accessor, eventProducer);
            OutlineFont font = new AFPTrueTypeFont(super.name, super.embeddable, characterSet, eventProducer, this.subfont, new URI(this.fontUri));
            return getFontInfo(font, this);
         } catch (URISyntaxException var9) {
            throw new IOException(var9);
         }
      }

      // $FF: synthetic method
      TrueTypeFontConfig(List x0, String x1, String x2, String x3, String x4, String x5, String x6, boolean x7, String x8, Object x9) {
         this(x0, x1, x2, x3, x4, x5, x6, x7, x8);
      }
   }

   static final class CIDKeyedFontConfig extends AFPFontConfigData {
      private final CharacterSetType charsetType;
      private final String characterset;

      private CIDKeyedFontConfig(List triplets, String type, String codePage, String encoding, String characterset, String name, CharacterSetType charsetType, boolean embeddable, String uri) {
         super(triplets, type, codePage, encoding, name, embeddable, uri);
         this.characterset = characterset;
         this.charsetType = charsetType;
      }

      AFPFontInfo getFontInfo(InternalResourceResolver resourceResolver, AFPEventProducer eventProducer) throws IOException {
         AFPResourceAccessor accessor = this.getAccessor(resourceResolver);
         CharacterSet characterSet = CharacterSetBuilder.getDoubleByteInstance().buildDBCS(this.characterset, super.codePage, super.encoding, this.charsetType, accessor, eventProducer);
         return getFontInfo(new DoubleByteFont(super.codePage, super.embeddable, characterSet, eventProducer), this);
      }

      // $FF: synthetic method
      CIDKeyedFontConfig(List x0, String x1, String x2, String x3, String x4, String x5, CharacterSetType x6, boolean x7, String x8, Object x9) {
         this(x0, x1, x2, x3, x4, x5, x6, x7, x8);
      }
   }

   abstract static class AFPFontConfigData {
      protected final List triplets;
      private final String codePage;
      private final String encoding;
      private final String name;
      private final boolean embeddable;
      protected final String uri;

      AFPFontConfigData(List triplets, String type, String codePage, String encoding, String name, boolean embeddable, String uri) {
         this.triplets = Collections.unmodifiableList(triplets);
         this.codePage = codePage;
         this.encoding = encoding;
         this.name = name;
         this.embeddable = embeddable;
         this.uri = uri;
      }

      static AFPFontInfo getFontInfo(AFPFont font, AFPFontConfigData config) {
         return font != null ? new AFPFontInfo(font, config.triplets) : null;
      }

      abstract AFPFontInfo getFontInfo(InternalResourceResolver var1, AFPEventProducer var2) throws IOException;

      AFPResourceAccessor getAccessor(InternalResourceResolver resourceResolver) {
         return new AFPResourceAccessor(resourceResolver, this.uri);
      }
   }

   private static final class ParserHelper {
      private static final Log LOG = LogFactory.getLog(ParserHelper.class);
      private final AFPFontConfig fontConfig;
      private final FontTriplet.Matcher matcher;

      private ParserHelper(Configuration cfg, FontManager fontManager, boolean strict, AFPEventProducer eventProducer) throws FOPException, ConfigurationException {
         Configuration fonts = cfg.getChild("fonts");
         FontTriplet.Matcher localMatcher = null;
         Configuration referencedFontsCfg = fonts.getChild("referenced-fonts", false);
         if (referencedFontsCfg != null) {
            localMatcher = FontManagerConfigurator.createFontsMatcher(referencedFontsCfg, strict);
         }

         this.matcher = new AggregateMatcher(new FontTriplet.Matcher[]{fontManager.getReferencedFontsMatcher(), localMatcher});
         this.fontConfig = new AFPFontConfig();
         Configuration[] var8 = fonts.getChildren("font");
         int var9 = var8.length;

         for(int var10 = 0; var10 < var9; ++var10) {
            Configuration font = var8[var10];
            this.buildFont(font, eventProducer);
         }

      }

      private void buildFont(Configuration fontCfg, AFPEventProducer eventProducer) throws ConfigurationException {
         Configuration[] triplets = fontCfg.getChildren("font-triplet");
         List tripletList = new ArrayList();
         if (triplets.length == 0) {
            eventProducer.fontConfigMissing(this, "<font-triplet...", fontCfg.getLocation());
         } else {
            Configuration[] var5 = triplets;
            int var6 = triplets.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               Configuration triplet = var5[var7];
               int weight = FontUtil.parseCSS2FontWeight(triplet.getAttribute("weight"));
               FontTriplet fontTriplet = new FontTriplet(triplet.getAttribute("name"), triplet.getAttribute("style"), weight);
               tripletList.add(fontTriplet);
            }

            String tturi = fontCfg.getAttribute("embed-url", (String)null);
            if (tturi != null) {
               this.fontFromType(tripletList, "truetype", (String)null, "UTF-16BE", fontCfg, eventProducer, tturi);
            } else {
               Configuration[] config = fontCfg.getChildren("afp-font");
               if (config.length == 0) {
                  eventProducer.fontConfigMissing(this, "<afp-font...", fontCfg.getLocation());
               } else {
                  Configuration afpFontCfg = config[0];
                  String uri = afpFontCfg.getAttribute("base-uri", (String)null);

                  try {
                     String type = afpFontCfg.getAttribute("type");
                     if (type == null) {
                        eventProducer.fontConfigMissing(this, "type attribute", fontCfg.getLocation());
                        return;
                     }

                     String codepage = afpFontCfg.getAttribute("codepage");
                     if (codepage == null) {
                        eventProducer.fontConfigMissing(this, "codepage attribute", fontCfg.getLocation());
                        return;
                     }

                     String encoding = afpFontCfg.getAttribute("encoding");
                     if (encoding == null) {
                        eventProducer.fontConfigMissing(this, "encoding attribute", fontCfg.getLocation());
                        return;
                     }

                     this.fontFromType(tripletList, type, codepage, encoding, afpFontCfg, eventProducer, uri);
                  } catch (ConfigurationException var12) {
                     eventProducer.invalidConfiguration(this, var12);
                  }

               }
            }
         }
      }

      private void fontFromType(List fontTriplets, String type, String codepage, String encoding, Configuration cfg, AFPEventProducer eventProducer, String embedURI) throws ConfigurationException {
         AFPFontConfigData config = null;
         if ("raster".equalsIgnoreCase(type)) {
            config = this.getRasterFont(fontTriplets, type, codepage, encoding, cfg, eventProducer, embedURI);
         } else if ("outline".equalsIgnoreCase(type)) {
            config = this.getOutlineFont(fontTriplets, type, codepage, encoding, cfg, eventProducer, embedURI);
         } else if ("CIDKeyed".equalsIgnoreCase(type)) {
            config = this.getCIDKeyedFont(fontTriplets, type, codepage, encoding, cfg, eventProducer, embedURI);
         } else if ("truetype".equalsIgnoreCase(type)) {
            config = this.getTruetypeFont(fontTriplets, type, codepage, encoding, cfg, eventProducer, embedURI);
         } else {
            LOG.error("No or incorrect type attribute: " + type);
         }

         if (config != null) {
            this.fontConfig.fontsConfig.add(config);
         }

      }

      private CIDKeyedFontConfig getCIDKeyedFont(List fontTriplets, String type, String codepage, String encoding, Configuration cfg, AFPEventProducer eventProducer, String uri) throws ConfigurationException {
         String characterset = cfg.getAttribute("characterset");
         if (characterset == null) {
            eventProducer.fontConfigMissing(this, "characterset attribute", cfg.getLocation());
            return null;
         } else {
            String name = cfg.getAttribute("name", characterset);
            CharacterSetType charsetType = cfg.getAttributeAsBoolean("ebcdic-dbcs", false) ? CharacterSetType.DOUBLE_BYTE_LINE_DATA : CharacterSetType.DOUBLE_BYTE;
            return new CIDKeyedFontConfig(fontTriplets, type, codepage, encoding, characterset, name, charsetType, this.isEmbbedable(fontTriplets), uri);
         }
      }

      private OutlineFontConfig getOutlineFont(List fontTriplets, String type, String codepage, String encoding, Configuration cfg, AFPEventProducer eventProducer, String uri) throws ConfigurationException {
         String characterset = cfg.getAttribute("characterset");
         if (characterset == null) {
            eventProducer.fontConfigMissing(this, "characterset attribute", cfg.getLocation());
            return null;
         } else {
            String name = cfg.getAttribute("name", characterset);
            String base14 = cfg.getAttribute("base14-font", (String)null);
            return new OutlineFontConfig(fontTriplets, type, codepage, encoding, characterset, name, base14, this.isEmbbedable(fontTriplets), uri);
         }
      }

      private TrueTypeFontConfig getTruetypeFont(List fontTriplets, String type, String codepage, String encoding, Configuration cfg, AFPEventProducer eventProducer, String uri) throws ConfigurationException {
         String name = cfg.getAttribute("name", (String)null);
         if (name == null) {
            eventProducer.fontConfigMissing(this, "font name attribute", cfg.getLocation());
            return null;
         } else {
            String subfont = cfg.getAttribute("sub-font", (String)null);
            return new TrueTypeFontConfig(fontTriplets, type, codepage, encoding, "", name, subfont, this.isEmbbedable(fontTriplets), uri);
         }
      }

      private RasterFontConfig getRasterFont(List triplets, String type, String codepage, String encoding, Configuration cfg, AFPEventProducer eventProducer, String uri) throws ConfigurationException {
         String name = cfg.getAttribute("name", "Unknown");
         Configuration[] rasters = cfg.getChildren("afp-raster-font");
         if (rasters.length == 0) {
            eventProducer.fontConfigMissing(this, "<afp-raster-font...", cfg.getLocation());
            return null;
         } else {
            List charsetData = new ArrayList();
            Configuration[] var11 = rasters;
            int var12 = rasters.length;

            for(int var13 = 0; var13 < var12; ++var13) {
               Configuration rasterCfg = var11[var13];
               String characterset = rasterCfg.getAttribute("characterset");
               if (characterset == null) {
                  eventProducer.fontConfigMissing(this, "characterset attribute", cfg.getLocation());
                  return null;
               }

               float size = rasterCfg.getAttributeAsFloat("size");
               int sizeMpt = (int)(size * 1000.0F);
               String base14 = rasterCfg.getAttribute("base14-font", (String)null);
               charsetData.add(new RasterCharactersetData(characterset, sizeMpt, base14));
            }

            return new RasterFontConfig(triplets, type, codepage, encoding, (String)null, name, uri, charsetData, this.isEmbbedable(triplets));
         }
      }

      private boolean isEmbbedable(List triplets) {
         Iterator var2 = triplets.iterator();

         FontTriplet triplet;
         do {
            if (!var2.hasNext()) {
               return true;
            }

            triplet = (FontTriplet)var2.next();
         } while(!this.matcher.matches(triplet));

         return false;
      }

      // $FF: synthetic method
      ParserHelper(Configuration x0, FontManager x1, boolean x2, AFPEventProducer x3, Object x4) throws FOPException, ConfigurationException {
         this(x0, x1, x2, x3);
      }
   }

   private static final class AggregateMatcher implements FontTriplet.Matcher {
      private final List matchers;

      private AggregateMatcher(FontTriplet.Matcher... matchers) {
         this.matchers = new ArrayList();
         FontTriplet.Matcher[] var2 = matchers;
         int var3 = matchers.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            FontTriplet.Matcher matcher = var2[var4];
            if (matcher != null) {
               this.matchers.add(matcher);
            }
         }

      }

      public boolean matches(FontTriplet triplet) {
         Iterator var2 = this.matchers.iterator();

         FontTriplet.Matcher matcher;
         do {
            if (!var2.hasNext()) {
               return false;
            }

            matcher = (FontTriplet.Matcher)var2.next();
         } while(!matcher.matches(triplet));

         return true;
      }

      // $FF: synthetic method
      AggregateMatcher(FontTriplet.Matcher[] x0, Object x1) {
         this(x0);
      }
   }

   static final class AFPFontInfoConfigParser implements FontConfig.FontConfigParser {
      public AFPFontConfig parse(Configuration cfg, FontManager fontManager, boolean strict, EventProducer eventProducer) throws FOPException {
         try {
            return (new ParserHelper(cfg, fontManager, strict, (AFPEventProducer)eventProducer)).fontConfig;
         } catch (ConfigurationException var6) {
            throw new FOPException(var6);
         }
      }

      AFPFontConfig getEmptyConfig() {
         return new AFPFontConfig();
      }
   }
}
