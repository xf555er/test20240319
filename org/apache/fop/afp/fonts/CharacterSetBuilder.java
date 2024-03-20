package org.apache.fop.afp.fonts;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.AFPEventProducer;
import org.apache.fop.afp.util.AFPResourceAccessor;
import org.apache.fop.afp.util.StructuredFieldReader;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.Typeface;
import org.apache.xmlgraphics.image.loader.util.SoftMapCache;

public abstract class CharacterSetBuilder {
   protected static final Log LOG = LogFactory.getLog(CharacterSetBuilder.class);
   private static final CharacterSetOrientation[] EMPTY_CSO_ARRAY = new CharacterSetOrientation[0];
   private static final byte[] CODEPAGE_SF = new byte[]{-45, -88, -121};
   private static final byte[] CHARACTER_TABLE_SF = new byte[]{-45, -116, -121};
   private static final byte[] FONT_DESCRIPTOR_SF = new byte[]{-45, -90, -119};
   private static final byte[] FONT_CONTROL_SF = new byte[]{-45, -89, -119};
   private static final byte[] FONT_ORIENTATION_SF = new byte[]{-45, -82, -119};
   private static final byte[] FONT_POSITION_SF = new byte[]{-45, -84, -119};
   private static final byte[] FONT_INDEX_SF = new byte[]{-45, -116, -119};
   private final Map codePagesCache;
   private final SoftMapCache characterSetsCache;

   private CharacterSetBuilder() {
      this.codePagesCache = Collections.synchronizedMap(new WeakHashMap());
      this.characterSetsCache = new SoftMapCache(true);
   }

   public static CharacterSetBuilder getSingleByteInstance() {
      return CharacterSetBuilder.SingleByteLoader.getInstance();
   }

   public static CharacterSetBuilder getDoubleByteInstance() {
      return CharacterSetBuilder.DoubleByteLoader.getInstance();
   }

   private InputStream openInputStream(AFPResourceAccessor accessor, String uriStr, AFPEventProducer eventProducer) throws IOException {
      URI uri;
      try {
         uri = InternalResourceResolver.cleanURI(uriStr.trim());
      } catch (URISyntaxException var6) {
         throw new MalformedURLException("Invalid uri: " + uriStr + " (" + var6.getMessage() + ")");
      }

      if (LOG.isDebugEnabled()) {
         LOG.debug("Opening " + uri);
      }

      return accessor.createInputStream(uri);
   }

   private void closeInputStream(InputStream inputStream) {
      try {
         if (inputStream != null) {
            inputStream.close();
         }
      } catch (Exception var3) {
         LOG.error(var3.getMessage());
      }

   }

   public CharacterSet buildSBCS(String characterSetName, String codePageName, String encoding, AFPResourceAccessor accessor, AFPEventProducer eventProducer) throws IOException {
      return this.processFont(characterSetName, codePageName, encoding, CharacterSetType.SINGLE_BYTE, accessor, eventProducer);
   }

   public CharacterSet buildDBCS(String characterSetName, String codePageName, String encoding, CharacterSetType charsetType, AFPResourceAccessor accessor, AFPEventProducer eventProducer) throws IOException {
      return this.processFont(characterSetName, codePageName, encoding, charsetType, accessor, eventProducer);
   }

   public CharacterSet build(String characterSetName, String codePageName, String encoding, Typeface typeface, AFPEventProducer eventProducer) throws IOException {
      return new FopCharacterSet(codePageName, encoding, characterSetName, typeface, eventProducer);
   }

   public CharacterSet build(String characterSetName, String codePageName, String encoding, Typeface typeface, AFPResourceAccessor accessor, AFPEventProducer eventProducer) throws IOException {
      return new FopCharacterSet(codePageName, encoding, characterSetName, typeface, accessor, eventProducer);
   }

   private CharacterSet processFont(String characterSetName, String codePageName, String encoding, CharacterSetType charsetType, AFPResourceAccessor accessor, AFPEventProducer eventProducer) throws IOException {
      URI charSetURI = accessor.resolveURI(characterSetName);
      String cacheKey = charSetURI.toASCIIString() + "_" + characterSetName + "_" + codePageName;
      CharacterSet characterSet = (CharacterSet)this.characterSetsCache.get(cacheKey);
      if (characterSet != null) {
         return characterSet;
      } else {
         characterSet = new CharacterSet(codePageName, encoding, charsetType, characterSetName, accessor, eventProducer);
         InputStream inputStream = null;

         try {
            Map codePage;
            synchronized(this.codePagesCache) {
               codePage = (Map)this.codePagesCache.get(codePageName);
               if (codePage == null) {
                  codePage = this.loadCodePage(codePageName, encoding, accessor, eventProducer);
                  this.codePagesCache.put(codePageName, codePage);
               }
            }

            inputStream = this.openInputStream(accessor, characterSetName, eventProducer);
            StructuredFieldReader structuredFieldReader = new StructuredFieldReader(inputStream);
            FontDescriptor fontDescriptor = processFontDescriptor(structuredFieldReader);
            characterSet.setNominalVerticalSize(fontDescriptor.getNominalFontSizeInMillipoints());
            FontControl fontControl = this.processFontControl(structuredFieldReader);
            if (fontControl == null) {
               throw new IOException("Missing D3AE89 Font Control structured field.");
            }

            CharacterSetOrientation[] characterSetOrientations = this.processFontOrientation(structuredFieldReader);
            double metricNormalizationFactor;
            if (fontControl.isRelative()) {
               metricNormalizationFactor = 1.0;
            } else {
               int dpi = fontControl.getDpi();
               metricNormalizationFactor = 7.2E7 / (double)fontDescriptor.getNominalFontSizeInMillipoints() / (double)dpi;
            }

            ValueNormalizer normalizer = new ValueNormalizer(metricNormalizationFactor);
            this.processFontPosition(structuredFieldReader, characterSetOrientations, normalizer);
            CharacterSetOrientation[] var19 = characterSetOrientations;
            int var20 = characterSetOrientations.length;

            for(int var21 = 0; var21 < var20; ++var21) {
               CharacterSetOrientation characterSetOrientation = var19[var21];
               this.processFontIndex(structuredFieldReader, characterSetOrientation, codePage, normalizer);
               characterSet.addCharacterSetOrientation(characterSetOrientation);
            }
         } finally {
            this.closeInputStream(inputStream);
         }

         this.characterSetsCache.put(cacheKey, characterSet);
         return characterSet;
      }
   }

   protected Map loadCodePage(String codePage, String encoding, AFPResourceAccessor accessor, AFPEventProducer eventProducer) throws IOException {
      Map codePages = new HashMap();
      InputStream inputStream = null;

      try {
         inputStream = this.openInputStream(accessor, codePage.trim(), eventProducer);
      } catch (IOException var18) {
         eventProducer.codePageNotFound(this, var18);
         throw var18;
      }

      try {
         StructuredFieldReader structuredFieldReader = new StructuredFieldReader(inputStream);
         byte[] data = structuredFieldReader.getNext(CHARACTER_TABLE_SF);
         int position = 0;
         byte[] gcgiBytes = new byte[8];
         byte[] charBytes = new byte[1];

         for(int index = 3; index < data.length; ++index) {
            if (position < 8) {
               gcgiBytes[position] = data[index];
               ++position;
            } else if (position == 9) {
               position = 0;
               charBytes[0] = data[index];
               String gcgiString = new String(gcgiBytes, "Cp1146");
               String charString = new String(charBytes, encoding);
               codePages.put(gcgiString, charString);
            } else {
               ++position;
            }
         }
      } finally {
         this.closeInputStream(inputStream);
      }

      return codePages;
   }

   private static FontDescriptor processFontDescriptor(StructuredFieldReader structuredFieldReader) throws IOException {
      byte[] fndData = structuredFieldReader.getNext(FONT_DESCRIPTOR_SF);
      return new FontDescriptor(fndData);
   }

   private FontControl processFontControl(StructuredFieldReader structuredFieldReader) throws IOException {
      byte[] fncData = structuredFieldReader.getNext(FONT_CONTROL_SF);
      FontControl fontControl = null;
      if (fncData != null) {
         fontControl = new FontControl();
         if (fncData[7] == 2) {
            fontControl.setRelative(true);
         }

         int metricResolution = getUBIN(fncData, 9);
         if (metricResolution == 1000) {
            fontControl.setUnitsPerEm(1000);
         } else {
            fontControl.setDpi(metricResolution / 10);
         }
      }

      return fontControl;
   }

   private CharacterSetOrientation[] processFontOrientation(StructuredFieldReader structuredFieldReader) throws IOException {
      byte[] data = structuredFieldReader.getNext(FONT_ORIENTATION_SF);
      int position = 0;
      byte[] fnoData = new byte[26];
      List orientations = new ArrayList();

      for(int index = 3; index < data.length; ++index) {
         fnoData[position] = data[index];
         ++position;
         if (position == 26) {
            position = 0;
            int orientation = determineOrientation(fnoData[2]);
            int spaceIncrement = getUBIN(fnoData, 8);
            int emIncrement = getUBIN(fnoData, 14);
            int nominalCharacterIncrement = getUBIN(fnoData, 20);
            orientations.add(new CharacterSetOrientation(orientation, spaceIncrement, emIncrement, nominalCharacterIncrement));
         }
      }

      return (CharacterSetOrientation[])orientations.toArray(EMPTY_CSO_ARRAY);
   }

   private void processFontPosition(StructuredFieldReader structuredFieldReader, CharacterSetOrientation[] characterSetOrientations, ValueNormalizer normalizer) throws IOException {
      byte[] data = structuredFieldReader.getNext(FONT_POSITION_SF);
      int position = 0;
      byte[] fpData = new byte[26];
      int characterSetOrientationIndex = 0;

      for(int index = 3; index < data.length; ++index) {
         if (position < 22) {
            fpData[position] = data[index];
            if (position == 9) {
               CharacterSetOrientation characterSetOrientation = characterSetOrientations[characterSetOrientationIndex];
               int xHeight = getSBIN(fpData, 2);
               int capHeight = getSBIN(fpData, 4);
               int ascHeight = getSBIN(fpData, 6);
               int dscHeight = getSBIN(fpData, 8);
               dscHeight *= -1;
               int underscoreWidth = getUBIN(fpData, 17);
               int underscorePosition = getSBIN(fpData, 20);
               characterSetOrientation.setXHeight(normalizer.normalize(xHeight));
               characterSetOrientation.setCapHeight(normalizer.normalize(capHeight));
               characterSetOrientation.setAscender(normalizer.normalize(ascHeight));
               characterSetOrientation.setDescender(normalizer.normalize(dscHeight));
               characterSetOrientation.setUnderscoreWidth(normalizer.normalize(underscoreWidth));
               characterSetOrientation.setUnderscorePosition(normalizer.normalize(underscorePosition));
            }
         } else if (position == 22) {
            position = 0;
            ++characterSetOrientationIndex;
            fpData[position] = data[index];
         }

         ++position;
      }

   }

   private void processFontIndex(StructuredFieldReader structuredFieldReader, CharacterSetOrientation cso, Map codepage, ValueNormalizer normalizer) throws IOException {
      byte[] data = structuredFieldReader.getNext(FONT_INDEX_SF);
      int position = 0;
      byte[] gcgid = new byte[8];
      byte[] fiData = new byte[20];
      String firstABCMismatch = null;

      for(int index = 3; index < data.length; ++index) {
         if (position < 8) {
            gcgid[position] = data[index];
            ++position;
         } else if (position < 27) {
            fiData[position - 8] = data[index];
            ++position;
         } else if (position == 27) {
            fiData[position - 8] = data[index];
            position = 0;
            String gcgiString = new String(gcgid, "Cp1146");
            String idx = (String)codepage.get(gcgiString);
            if (idx != null) {
               char cidx = idx.charAt(0);
               int width = getUBIN(fiData, 0);
               int ascendHt = getSBIN(fiData, 2);
               int descendDp = getSBIN(fiData, 4);
               int a = getSBIN(fiData, 10);
               int b = getUBIN(fiData, 12);
               int c = getSBIN(fiData, 14);
               int abc = a + b + c;
               int diff = Math.abs(abc - width);
               if (diff != 0 && width != 0) {
                  double diffPercent = (double)(100 * diff) / (double)width;
                  if (diffPercent > 2.0) {
                     if (LOG.isTraceEnabled()) {
                        LOG.trace(gcgiString + ": " + a + " + " + b + " + " + c + " = " + (a + b + c) + " but found: " + width);
                     }

                     if (firstABCMismatch == null) {
                        firstABCMismatch = gcgiString;
                     }
                  }
               }

               int normalizedWidth = normalizer.normalize(width);
               int x0 = normalizer.normalize(a);
               int y0 = normalizer.normalize(-descendDp);
               int dx = normalizer.normalize(b);
               int dy = normalizer.normalize(ascendHt + descendDp);
               cso.setCharacterMetrics(cidx, normalizedWidth, new Rectangle(x0, y0, dx, dy));
            }
         }
      }

      if (LOG.isDebugEnabled() && firstABCMismatch != null) {
         LOG.debug("Font has metrics inconsitencies where A+B+C doesn't equal the character increment. The first such character found: " + firstABCMismatch);
      }

   }

   private static int getUBIN(byte[] data, int start) {
      return ((data[start] & 255) << 8) + (data[start + 1] & 255);
   }

   private static int getSBIN(byte[] data, int start) {
      int ubin = ((data[start] & 255) << 8) + (data[start + 1] & 255);
      return (ubin & '耀') != 0 ? ubin | -65536 : ubin;
   }

   private static int determineOrientation(byte orientation) {
      int degrees = false;
      short degrees;
      switch (orientation) {
         case -121:
            degrees = 270;
            break;
         case 0:
            degrees = 0;
            break;
         case 45:
            degrees = 90;
            break;
         case 90:
            degrees = 180;
            break;
         default:
            throw new IllegalStateException("Invalid orientation: " + orientation);
      }

      return degrees;
   }

   // $FF: synthetic method
   CharacterSetBuilder(Object x0) {
      this();
   }

   private static final class DoubleByteLoader extends CharacterSetBuilder {
      private static final DoubleByteLoader INSTANCE = new DoubleByteLoader();

      private DoubleByteLoader() {
         super(null);
      }

      static DoubleByteLoader getInstance() {
         return INSTANCE;
      }

      protected Map loadCodePage(String codePage, String encoding, AFPResourceAccessor accessor, AFPEventProducer eventProducer) throws IOException {
         Map codePages = new HashMap();
         InputStream inputStream = null;

         try {
            inputStream = super.openInputStream(accessor, codePage.trim(), eventProducer);
         } catch (IOException var18) {
            eventProducer.codePageNotFound(this, var18);
            throw var18;
         }

         try {
            StructuredFieldReader structuredFieldReader = new StructuredFieldReader(inputStream);

            byte[] data;
            while((data = structuredFieldReader.getNext(CharacterSetBuilder.CHARACTER_TABLE_SF)) != null) {
               int position = 0;
               byte[] gcgiBytes = new byte[8];
               byte[] charBytes = new byte[2];

               for(int index = 3; index < data.length; ++index) {
                  if (position < 8) {
                     gcgiBytes[position] = data[index];
                     ++position;
                  } else if (position == 9) {
                     charBytes[0] = data[index];
                     ++position;
                  } else if (position == 10) {
                     position = 0;
                     charBytes[1] = data[index];
                     String gcgiString = new String(gcgiBytes, "Cp1146");
                     String charString = new String(charBytes, encoding);
                     codePages.put(gcgiString, charString);
                  } else {
                     ++position;
                  }
               }
            }
         } finally {
            super.closeInputStream(inputStream);
         }

         return codePages;
      }
   }

   private static final class SingleByteLoader extends CharacterSetBuilder {
      private static final SingleByteLoader INSTANCE = new SingleByteLoader();

      private SingleByteLoader() {
         super(null);
      }

      private static SingleByteLoader getInstance() {
         return INSTANCE;
      }
   }

   private static class FontDescriptor {
      private byte[] data;

      public FontDescriptor(byte[] data) {
         this.data = data;
      }

      public int getNominalFontSizeInMillipoints() {
         int nominalFontSize = 100 * CharacterSetBuilder.getUBIN(this.data, 39);
         return nominalFontSize;
      }
   }

   private class FontControl {
      private int dpi;
      private int unitsPerEm;
      private boolean isRelative;

      private FontControl() {
      }

      public int getDpi() {
         return this.dpi;
      }

      public void setDpi(int i) {
         this.dpi = i;
      }

      public int getUnitsPerEm() {
         return this.unitsPerEm;
      }

      public void setUnitsPerEm(int value) {
         this.unitsPerEm = value;
      }

      public boolean isRelative() {
         return this.isRelative;
      }

      public void setRelative(boolean b) {
         this.isRelative = b;
      }

      // $FF: synthetic method
      FontControl(Object x1) {
         this();
      }
   }

   private static class ValueNormalizer {
      private final double factor;

      public ValueNormalizer(double factor) {
         this.factor = factor;
      }

      public int normalize(int value) {
         return (int)Math.round((double)value * this.factor);
      }
   }
}
