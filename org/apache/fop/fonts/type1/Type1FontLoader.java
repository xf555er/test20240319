package org.apache.fop.fonts.type1;

import java.awt.Rectangle;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.CodePointMapping;
import org.apache.fop.fonts.EmbeddingMode;
import org.apache.fop.fonts.FontLoader;
import org.apache.fop.fonts.FontType;
import org.apache.fop.fonts.FontUris;
import org.apache.fop.fonts.SingleByteEncoding;
import org.apache.fop.fonts.SingleByteFont;

public class Type1FontLoader extends FontLoader {
   private static final Log log = LogFactory.getLog(Type1FontLoader.class);
   private SingleByteFont singleFont;
   private EmbeddingMode embeddingMode;
   private final FontUris fontUris;
   private static final String[] AFM_EXTENSIONS = new String[]{".AFM", ".afm", ".Afm"};

   public Type1FontLoader(FontUris fontUris, boolean embedded, EmbeddingMode embeddingMode, boolean useKerning, InternalResourceResolver resourceResolver) throws IOException {
      super(fontUris.getEmbed(), embedded, useKerning, true, resourceResolver);
      this.embeddingMode = embeddingMode;
      this.fontUris = fontUris;
   }

   private String getPFMURI(String pfbURI) {
      String pfbExt = pfbURI.substring(pfbURI.length() - 3, pfbURI.length());
      String pfmExt = pfbExt.substring(0, 2) + (Character.isUpperCase(pfbExt.charAt(2)) ? "M" : "m");
      return pfbURI.substring(0, pfbURI.length() - 4) + "." + pfmExt;
   }

   protected void read() throws IOException {
      AFMFile afm = null;
      PFMFile pfm = null;
      InputStream afmIn = null;
      String fontFileStr = this.fontFileURI.toASCIIString();
      String partialAfmUri = fontFileStr.substring(0, fontFileStr.length() - 4);
      String afmUri = this.fontUris.getAfm() != null ? this.fontUris.getAfm().toASCIIString() : null;
      if (afmUri == null) {
         String[] var7 = AFM_EXTENSIONS;
         int var8 = var7.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            String afmExtension = var7[var9];

            try {
               afmUri = partialAfmUri + afmExtension;
               afmIn = this.resourceResolver.getResource(afmUri);
               if (afmIn != null) {
                  break;
               }
            } catch (IOException var36) {
            } catch (URISyntaxException var37) {
            }
         }
      } else {
         try {
            afmIn = this.resourceResolver.getResource(afmUri);
         } catch (URISyntaxException var33) {
            throw new IOException(var33);
         }
      }

      if (afmIn != null) {
         try {
            AFMParser afmParser = new AFMParser();
            afm = afmParser.parse((InputStream)afmIn, afmUri);
         } finally {
            IOUtils.closeQuietly((InputStream)afmIn);
         }
      }

      String pfmUri = this.fontUris.getPfm() == null ? this.getPFMURI(fontFileStr) : this.fontUris.getPfm().toASCIIString();
      InputStream pfmIn = null;

      try {
         pfmIn = this.resourceResolver.getResource(pfmUri);
      } catch (IOException var30) {
      } catch (URISyntaxException var31) {
      }

      if (pfmIn != null) {
         try {
            pfm = new PFMFile();
            pfm.load(pfmIn);
         } catch (IOException var34) {
            if (afm == null) {
               throw var34;
            }
         } finally {
            IOUtils.closeQuietly((InputStream)pfmIn);
         }
      }

      if (afm == null && pfm == null) {
         throw new FileNotFoundException("Neither an AFM nor a PFM file was found for " + this.fontFileURI);
      } else {
         this.buildFont(afm, pfm);
         this.loaded = true;
      }
   }

   private void buildFont(AFMFile afm, PFMFile pfm) {
      if (afm == null && pfm == null) {
         throw new IllegalArgumentException("Need at least an AFM or a PFM!");
      } else {
         this.singleFont = new SingleByteFont(this.resourceResolver, this.embeddingMode);
         this.singleFont.setFontType(FontType.TYPE1);
         if (this.embedded) {
            this.singleFont.setEmbedURI(this.fontFileURI);
         }

         this.returnFont = this.singleFont;
         this.handleEncoding(afm, pfm);
         this.handleFontName(afm, pfm);
         this.handleMetrics(afm, pfm);
      }
   }

   private void handleEncoding(AFMFile afm, PFMFile pfm) {
      if (afm != null) {
         String encoding = afm.getEncodingScheme();
         this.singleFont.setUseNativeEncoding(true);
         if ("AdobeStandardEncoding".equals(encoding)) {
            this.singleFont.setEncoding("StandardEncoding");
            this.addUnencodedBasedOnEncoding(afm);
         } else {
            String effEncodingName;
            if ("FontSpecific".equals(encoding)) {
               effEncodingName = afm.getFontName() + "Encoding";
            } else {
               effEncodingName = encoding;
            }

            if (log.isDebugEnabled()) {
               log.debug("Unusual font encoding encountered: " + encoding + " -> " + effEncodingName);
            }

            CodePointMapping mapping = this.buildCustomEncoding(effEncodingName, afm);
            this.singleFont.setEncoding(mapping);
            this.addUnencodedBasedOnAFM(afm);
         }
      } else if (pfm.getCharSet() == 2 && !pfm.getCharSetName().equals("Symbol")) {
         int[] table = new int[256];
         String[] charNameMap = new String[256];
         int j = 0;

         for(int i = pfm.getFirstChar(); i < pfm.getLastChar(); ++i) {
            if (j < table.length) {
               table[j] = i;
               table[j + 1] = i;
               j += 2;
            }

            charNameMap[i] = String.format("x%03o", i);
         }

         CodePointMapping mapping = new CodePointMapping("custom", table, charNameMap);
         this.singleFont.setEncoding(mapping);
      } else if (pfm.getCharSet() >= 0 && pfm.getCharSet() <= 2) {
         this.singleFont.setEncoding(pfm.getCharSetName() + "Encoding");
      } else {
         log.warn("The PFM reports an unsupported encoding (" + pfm.getCharSetName() + "). The font may not work as expected.");
         this.singleFont.setEncoding("WinAnsiEncoding");
      }

   }

   private Set toGlyphSet(String[] glyphNames) {
      Set glyphSet = new HashSet();
      Collections.addAll(glyphSet, glyphNames);
      return glyphSet;
   }

   private void addUnencodedBasedOnEncoding(AFMFile afm) {
      SingleByteEncoding encoding = this.singleFont.getEncoding();
      Set glyphNames = this.toGlyphSet(encoding.getCharNameMap());
      List charMetrics = afm.getCharMetrics();
      Iterator var5 = charMetrics.iterator();

      while(var5.hasNext()) {
         AFMCharMetrics metrics = (AFMCharMetrics)var5.next();
         String charName = metrics.getCharName();
         if (charName != null && !glyphNames.contains(charName)) {
            addUnencodedCharacter(this.singleFont, metrics);
         }
      }

   }

   private static void addUnencodedCharacter(SingleByteFont font, AFMCharMetrics metrics) {
      font.addUnencodedCharacter(metrics.getCharacter(), (int)Math.round(metrics.getWidthX()), metrics.getBBox());
   }

   private void addUnencodedBasedOnAFM(AFMFile afm) {
      List charMetrics = afm.getCharMetrics();
      int i = 0;

      for(int c = afm.getCharCount(); i < c; ++i) {
         AFMCharMetrics metrics = (AFMCharMetrics)charMetrics.get(i);
         if (!metrics.hasCharCode() && metrics.getCharacter() != null) {
            addUnencodedCharacter(this.singleFont, metrics);
         }
      }

   }

   private void handleFontName(AFMFile afm, PFMFile pfm) {
      if (afm != null) {
         this.returnFont.setFontName(afm.getFontName());
         this.returnFont.setFullName(afm.getFullName());
         Set names = new HashSet();
         names.add(afm.getFamilyName());
         this.returnFont.setFamilyNames(names);
      } else {
         this.returnFont.setFontName(pfm.getPostscriptName());
         String fullName = pfm.getPostscriptName();
         fullName = fullName.replace('-', ' ');
         this.returnFont.setFullName(fullName);
         Set names = new HashSet();
         names.add(pfm.getWindowsName());
         this.returnFont.setFamilyNames(names);
      }

   }

   private void handleMetrics(AFMFile afm, PFMFile pfm) {
      if (afm != null) {
         if (afm.getCapHeight() != null) {
            this.returnFont.setCapHeight(afm.getCapHeight().intValue());
         }

         if (afm.getXHeight() != null) {
            this.returnFont.setXHeight(afm.getXHeight().intValue());
         }

         if (afm.getAscender() != null) {
            this.returnFont.setAscender(afm.getAscender().intValue());
         }

         if (afm.getDescender() != null) {
            this.returnFont.setDescender(afm.getDescender().intValue());
         }

         this.returnFont.setFontBBox(afm.getFontBBoxAsIntArray());
         if (afm.getStdVW() != null) {
            this.returnFont.setStemV(afm.getStdVW().intValue());
         } else {
            this.returnFont.setStemV(80);
         }

         AFMWritingDirectionMetrics metrics = afm.getWritingDirectionMetrics(0);
         this.returnFont.setItalicAngle((int)metrics.getItalicAngle());
         this.returnFont.setUnderlinePosition(metrics.getUnderlinePosition().intValue());
         this.returnFont.setUnderlineThickness(metrics.getUnderlineThickness().intValue());
      } else {
         this.returnFont.setFontBBox(pfm.getFontBBox());
         this.returnFont.setStemV(pfm.getStemV());
         this.returnFont.setItalicAngle(pfm.getItalicAngle());
      }

      if (pfm != null) {
         if (this.returnFont.getCapHeight() == 0) {
            this.returnFont.setCapHeight(pfm.getCapHeight());
         }

         if (this.returnFont.getXHeight(1) == 0) {
            this.returnFont.setXHeight(pfm.getXHeight());
         }

         if (this.returnFont.getAscender() == 0) {
            this.returnFont.setAscender(pfm.getLowerCaseAscent());
         }

         if (this.returnFont.getDescender() == 0) {
            this.returnFont.setDescender(pfm.getLowerCaseDescent());
         }
      }

      AFMCharMetrics chm;
      Rectangle rect;
      int desc;
      if (this.returnFont.getXHeight(1) == 0) {
         desc = 0;
         if (afm != null) {
            chm = afm.getChar("x");
            if (chm != null) {
               rect = chm.getBBox();
               if (rect != null) {
                  desc = (int)Math.round(rect.getMinX());
               }
            }
         }

         if (desc == 0) {
            desc = Math.round((float)this.returnFont.getFontBBox()[3] * 0.6F);
         }

         this.returnFont.setXHeight(desc);
      }

      if (this.returnFont.getAscender() == 0) {
         desc = 0;
         if (afm != null) {
            chm = afm.getChar("d");
            if (chm != null) {
               rect = chm.getBBox();
               if (rect != null) {
                  desc = (int)Math.round(rect.getMinX());
               }
            }
         }

         if (desc == 0) {
            desc = Math.round((float)this.returnFont.getFontBBox()[3] * 0.9F);
         }

         this.returnFont.setAscender(desc);
      }

      if (this.returnFont.getDescender() == 0) {
         desc = 0;
         if (afm != null) {
            chm = afm.getChar("p");
            if (chm != null) {
               rect = chm.getBBox();
               if (rect != null) {
                  desc = (int)Math.round(rect.getMinX());
               }
            }
         }

         if (desc == 0) {
            desc = this.returnFont.getFontBBox()[1];
         }

         this.returnFont.setDescender(desc);
      }

      if (this.returnFont.getCapHeight() == 0) {
         this.returnFont.setCapHeight(this.returnFont.getAscender());
      }

      int flags;
      if (afm != null) {
         String charSet = afm.getCharacterSet();
         flags = 0;
         if ("Special".equals(charSet)) {
            flags |= 4;
         } else if (this.singleFont.getEncoding().mapChar('A') == 'A') {
            flags |= 32;
         } else {
            flags |= 4;
         }

         if (afm.getWritingDirectionMetrics(0).isFixedPitch()) {
            flags |= 1;
         }

         if (afm.getWritingDirectionMetrics(0).getItalicAngle() != 0.0) {
            flags |= 64;
         }

         this.returnFont.setFlags(flags);
         this.returnFont.setFirstChar(afm.getFirstChar());
         this.returnFont.setLastChar(afm.getLastChar());
         Iterator var9 = afm.getCharMetrics().iterator();

         while(var9.hasNext()) {
            AFMCharMetrics chm = (AFMCharMetrics)var9.next();
            if (chm.hasCharCode()) {
               this.singleFont.setWidth(chm.getCharCode(), (int)Math.round(chm.getWidthX()));
               this.singleFont.setBoundingBox(chm.getCharCode(), chm.getBBox());
            }
         }

         if (this.useKerning) {
            this.returnFont.replaceKerningMap(afm.createXKerningMapEncoded());
         }
      } else {
         this.returnFont.setFlags(pfm.getFlags());
         this.returnFont.setFirstChar(pfm.getFirstChar());
         this.returnFont.setLastChar(pfm.getLastChar());

         for(short i = pfm.getFirstChar(); i <= pfm.getLastChar(); ++i) {
            flags = pfm.getCharWidth(i);
            this.singleFont.setWidth(i, flags);
            int[] bbox = pfm.getFontBBox();
            this.singleFont.setBoundingBox(i, new Rectangle(bbox[0], bbox[1], flags, bbox[3]));
         }

         if (this.useKerning) {
            this.returnFont.replaceKerningMap(pfm.getKerning());
         }
      }

   }

   private CodePointMapping buildCustomEncoding(String encodingName, AFMFile afm) {
      int mappingCount = 0;
      List chars = afm.getCharMetrics();
      Iterator var5 = chars.iterator();

      while(var5.hasNext()) {
         AFMCharMetrics charMetrics = (AFMCharMetrics)var5.next();
         if (charMetrics.getCharCode() >= 0) {
            ++mappingCount;
         }
      }

      int[] table = new int[mappingCount * 2];
      String[] charNameMap = new String[256];
      int idx = 0;
      Iterator var8 = chars.iterator();

      while(var8.hasNext()) {
         AFMCharMetrics charMetrics = (AFMCharMetrics)var8.next();
         if (charMetrics.getCharCode() >= 0) {
            charNameMap[charMetrics.getCharCode()] = charMetrics.getCharName();
            String unicodes = charMetrics.getUnicodeSequence();
            if (unicodes == null) {
               log.info("No Unicode mapping for glyph: " + charMetrics);
               table[idx] = charMetrics.getCharCode();
               ++idx;
               table[idx] = charMetrics.getCharCode();
               ++idx;
            } else if (unicodes.length() == 1) {
               table[idx] = charMetrics.getCharCode();
               ++idx;
               table[idx] = unicodes.charAt(0);
               ++idx;
            } else {
               log.warn("Multi-character representation of glyph not currently supported: " + charMetrics);
            }
         }
      }

      return new CodePointMapping(encodingName, table, charNameMap);
   }
}
