package org.apache.fop.fonts;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.SAXParserFactory;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.apps.TTFReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/** @deprecated */
@Deprecated
public class XMLFontMetricsReader extends DefaultHandler {
   private boolean isCID;
   private CustomFont returnFont;
   private MultiByteFont multiFont;
   private SingleByteFont singleFont;
   private final InternalResourceResolver resourceResolver;
   private StringBuffer text = new StringBuffer();
   private List cidWidths;
   private Map currentKerning;
   private List bfranges;

   public XMLFontMetricsReader(InputSource source, InternalResourceResolver resourceResolver) throws FOPException {
      this.resourceResolver = resourceResolver;
      this.createFont(source);
   }

   private void createFont(InputSource source) throws FOPException {
      XMLReader parser = null;

      try {
         SAXParserFactory factory = SAXParserFactory.newInstance();
         factory.setNamespaceAware(true);
         parser = factory.newSAXParser().getXMLReader();
      } catch (Exception var7) {
         throw new FOPException(var7);
      }

      if (parser == null) {
         throw new FOPException("Unable to create SAX parser");
      } else {
         try {
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes", false);
         } catch (SAXException var6) {
            throw new FOPException("You need a SAX parser which supports SAX version 2", var6);
         }

         parser.setContentHandler(this);

         try {
            parser.parse(source);
         } catch (SAXException var4) {
            throw new FOPException(var4);
         } catch (IOException var5) {
            throw new FOPException(var5);
         }
      }
   }

   public void setFontEmbedURI(URI path) {
      this.returnFont.setEmbedURI(path);
   }

   public void setKerningEnabled(boolean enabled) {
      this.returnFont.setKerningEnabled(enabled);
   }

   public void setAdvancedEnabled(boolean enabled) {
      this.returnFont.setAdvancedEnabled(enabled);
   }

   public Typeface getFont() {
      return this.returnFont;
   }

   public void startDocument() {
   }

   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if (localName.equals("font-metrics")) {
         if ("TYPE0".equals(attributes.getValue("type"))) {
            this.multiFont = new MultiByteFont(this.resourceResolver, EmbeddingMode.AUTO);
            this.returnFont = this.multiFont;
            this.isCID = true;
            TTFReader.checkMetricsVersion(attributes);
         } else if ("TRUETYPE".equals(attributes.getValue("type"))) {
            this.singleFont = new SingleByteFont(this.resourceResolver, EmbeddingMode.AUTO);
            this.singleFont.setFontType(FontType.TRUETYPE);
            this.returnFont = this.singleFont;
            this.isCID = false;
            TTFReader.checkMetricsVersion(attributes);
         } else {
            this.singleFont = new SingleByteFont(this.resourceResolver, EmbeddingMode.AUTO);
            this.singleFont.setFontType(FontType.TYPE1);
            this.returnFont = this.singleFont;
            this.isCID = false;
         }
      } else if ("embed".equals(localName)) {
         try {
            this.returnFont.setEmbedURI(InternalResourceResolver.cleanURI(attributes.getValue("file")));
         } catch (URISyntaxException var7) {
            throw new SAXException("URI syntax error in metrics file: " + var7.getMessage(), var7);
         }

         this.returnFont.setEmbedResourceName(attributes.getValue("class"));
      } else if ("cid-widths".equals(localName)) {
         this.cidWidths = new ArrayList();
      } else if ("kerning".equals(localName)) {
         this.currentKerning = new HashMap();
         this.returnFont.putKerningEntry(this.getInt(attributes.getValue("kpx1")), this.currentKerning);
      } else if ("bfranges".equals(localName)) {
         this.bfranges = new ArrayList();
      } else if ("bf".equals(localName)) {
         CMapSegment entry = new CMapSegment(this.getInt(attributes.getValue("us")), this.getInt(attributes.getValue("ue")), this.getInt(attributes.getValue("gi")));
         this.bfranges.add(entry);
      } else if ("wx".equals(localName)) {
         this.cidWidths.add(this.getInt(attributes.getValue("w")));
      } else if ("char".equals(localName)) {
         try {
            this.singleFont.setWidth(this.getInt(attributes.getValue("idx")), this.getInt(attributes.getValue("wdt")));
         } catch (NumberFormatException var6) {
            throw new SAXException("Malformed width in metric file: " + var6.getMessage(), var6);
         }
      } else if ("pair".equals(localName)) {
         this.currentKerning.put(this.getInt(attributes.getValue("kpx2")), this.getInt(attributes.getValue("kern")));
      }

   }

   private int getInt(String str) throws SAXException {
      int ret = false;

      try {
         int ret = Integer.parseInt(str);
         return ret;
      } catch (Exception var4) {
         throw new SAXException("Error while parsing integer value: " + str, var4);
      }
   }

   public void endElement(String uri, String localName, String qName) throws SAXException {
      String content = this.text.toString().trim();
      if ("font-name".equals(localName)) {
         this.returnFont.setFontName(content);
      } else if ("full-name".equals(localName)) {
         this.returnFont.setFullName(content);
      } else if ("family-name".equals(localName)) {
         Set s = new HashSet();
         s.add(content);
         this.returnFont.setFamilyNames(s);
      } else if ("ttc-name".equals(localName) && this.isCID) {
         this.multiFont.setTTCName(content);
      } else if ("encoding".equals(localName)) {
         if (this.singleFont != null && this.singleFont.getFontType() == FontType.TYPE1) {
            this.singleFont.setEncoding(content);
         }
      } else if ("cap-height".equals(localName)) {
         this.returnFont.setCapHeight(this.getInt(content));
      } else if ("x-height".equals(localName)) {
         this.returnFont.setXHeight(this.getInt(content));
      } else if ("ascender".equals(localName)) {
         this.returnFont.setAscender(this.getInt(content));
      } else if ("descender".equals(localName)) {
         this.returnFont.setDescender(this.getInt(content));
      } else {
         int[] wds;
         if ("left".equals(localName)) {
            wds = this.returnFont.getFontBBox();
            wds[0] = this.getInt(content);
            this.returnFont.setFontBBox(wds);
         } else if ("bottom".equals(localName)) {
            wds = this.returnFont.getFontBBox();
            wds[1] = this.getInt(content);
            this.returnFont.setFontBBox(wds);
         } else if ("right".equals(localName)) {
            wds = this.returnFont.getFontBBox();
            wds[2] = this.getInt(content);
            this.returnFont.setFontBBox(wds);
         } else if ("top".equals(localName)) {
            wds = this.returnFont.getFontBBox();
            wds[3] = this.getInt(content);
            this.returnFont.setFontBBox(wds);
         } else if ("first-char".equals(localName)) {
            this.returnFont.setFirstChar(this.getInt(content));
         } else if ("last-char".equals(localName)) {
            this.returnFont.setLastChar(this.getInt(content));
         } else if ("flags".equals(localName)) {
            this.returnFont.setFlags(this.getInt(content));
         } else if ("stemv".equals(localName)) {
            this.returnFont.setStemV(this.getInt(content));
         } else if ("italic-angle".equals(localName)) {
            this.returnFont.setItalicAngle(this.getInt(content));
         } else if ("missing-width".equals(localName)) {
            this.returnFont.setMissingWidth(this.getInt(content));
         } else if ("cid-type".equals(localName)) {
            this.multiFont.setCIDType(CIDFontType.byName(content));
         } else if ("default-width".equals(localName)) {
            this.multiFont.setDefaultWidth(this.getInt(content));
         } else if ("cid-widths".equals(localName)) {
            wds = new int[this.cidWidths.size()];
            int j = 0;

            Integer cidWidth;
            for(Iterator var7 = this.cidWidths.iterator(); var7.hasNext(); wds[j++] = cidWidth) {
               cidWidth = (Integer)var7.next();
            }

            this.multiFont.setWidthArray(wds);
         } else if ("bfranges".equals(localName)) {
            this.multiFont.setCMap((CMapSegment[])this.bfranges.toArray(new CMapSegment[this.bfranges.size()]));
         }
      }

      this.text.setLength(0);
   }

   public void characters(char[] ch, int start, int length) {
      this.text.append(ch, start, length);
   }
}
