package org.apache.fop.fonts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.fontbox.cff.CFFFont;
import org.apache.fontbox.cff.CFFParser;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.type1.PFBData;
import org.apache.fop.fonts.type1.PFBParser;
import org.apache.fop.fonts.type1.Type1SubsetFile;
import org.apache.fop.render.ps.Type1FontFormatter;

public class CFFToType1Font extends MultiByteFont {
   public CFFToType1Font(InternalResourceResolver resourceResolver, EmbeddingMode embeddingMode) {
      super(resourceResolver, embeddingMode);
      this.setEmbeddingMode(EmbeddingMode.FULL);
      this.setFontType(FontType.TYPE1);
   }

   public InputStream getInputStream() throws IOException {
      return null;
   }

   public List getInputStreams() throws IOException {
      InputStream cff = super.getInputStream();
      return this.convertOTFToType1(cff);
   }

   private List convertOTFToType1(InputStream in) throws IOException {
      CFFFont f = (CFFFont)(new CFFParser()).parse(IOUtils.toByteArray(in)).get(0);
      List fonts = new ArrayList();
      Map glyphs = this.cidSet.getGlyphs();
      int i = 0;

      for(Iterator var6 = this.splitGlyphs(glyphs).iterator(); var6.hasNext(); ++i) {
         Map x = (Map)var6.next();
         String iStr = "." + i;
         fonts.add(this.convertOTFToType1(x, f, iStr));
      }

      return fonts;
   }

   private List splitGlyphs(Map glyphs) {
      List allGlyphs = new ArrayList();
      Iterator var3 = glyphs.entrySet().iterator();

      while(var3.hasNext()) {
         Map.Entry x = (Map.Entry)var3.next();
         int k = (Integer)x.getKey();
         int v = (Integer)x.getValue();
         int pot = v / 256;
         v %= 256;

         while(allGlyphs.size() < pot + 1) {
            Map glyphsPerFont = new HashMap();
            glyphsPerFont.put(0, 0);
            allGlyphs.add(glyphsPerFont);
         }

         ((Map)allGlyphs.get(pot)).put(k, v);
      }

      return allGlyphs;
   }

   private InputStream convertOTFToType1(Map glyphs, CFFFont cffFont, String splitGlyphsId) throws IOException {
      byte[] type1Bytes = (new Type1FontFormatter(glyphs)).format(cffFont, splitGlyphsId);
      PFBData pfb = (new PFBParser()).parsePFB(new ByteArrayInputStream(type1Bytes));
      ByteArrayOutputStream s1 = new ByteArrayOutputStream();
      s1.write(pfb.getHeaderSegment());
      ByteArrayOutputStream s2 = new ByteArrayOutputStream();
      s2.write(pfb.getEncryptedSegment());
      ByteArrayOutputStream s3 = new ByteArrayOutputStream();
      s3.write(pfb.getTrailerSegment());
      byte[] out = (new Type1SubsetFile()).stitchFont(s1, s2, s3);
      return new ByteArrayInputStream(out);
   }
}
