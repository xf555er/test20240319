package org.apache.fop.tools.fontlist;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.regex.Pattern;
import org.apache.fop.fonts.FontTriplet;
import org.apache.fop.util.GenerationHelperContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class FontListSerializer {
   private static final String FONTS = "fonts";
   private static final String FAMILY = "family";
   private static final String FONT = "font";
   private static final String TRIPLETS = "triplets";
   private static final String TRIPLET = "triplet";
   private static final String NAME = "name";
   private static final String STRIPPED_NAME = "stripped-name";
   private static final String TYPE = "type";
   private static final String KEY = "key";
   private static final String STYLE = "style";
   private static final String WEIGHT = "weight";
   private static final String CDATA = "CDATA";
   private final Pattern quotePattern = Pattern.compile("'");

   public void generateSAX(SortedMap fontFamilies, GenerationHelperContentHandler handler) throws SAXException {
      this.generateSAX(fontFamilies, (String)null, handler);
   }

   public void generateSAX(SortedMap fontFamilies, String singleFamily, GenerationHelperContentHandler handler) throws SAXException {
      handler.startDocument();
      AttributesImpl atts = new AttributesImpl();
      handler.startElement((String)"fonts", atts);
      Iterator var5 = fontFamilies.entrySet().iterator();

      while(true) {
         Map.Entry entry;
         String familyName;
         do {
            if (!var5.hasNext()) {
               handler.endElement("fonts");
               handler.endDocument();
               return;
            }

            Object o = var5.next();
            entry = (Map.Entry)o;
            familyName = (String)entry.getKey();
         } while(singleFamily != null && !singleFamily.equals(familyName));

         atts.clear();
         atts.addAttribute("", "name", "name", "CDATA", familyName);
         atts.addAttribute("", "stripped-name", "stripped-name", "CDATA", this.stripQuotes(familyName));
         handler.startElement((String)"family", atts);
         List containers = (List)entry.getValue();
         this.generateXMLForFontContainers(handler, containers);
         handler.endElement("family");
      }
   }

   private String stripQuotes(String name) {
      return this.quotePattern.matcher(name).replaceAll("");
   }

   private void generateXMLForFontContainers(GenerationHelperContentHandler handler, List containers) throws SAXException {
      AttributesImpl atts = new AttributesImpl();
      Iterator var4 = containers.iterator();

      while(var4.hasNext()) {
         Object container = var4.next();
         FontSpec cont = (FontSpec)container;
         atts.clear();
         atts.addAttribute("", "key", "key", "CDATA", cont.getKey());
         atts.addAttribute("", "type", "type", "CDATA", cont.getFontMetrics().getFontType().getName());
         handler.startElement((String)"font", atts);
         this.generateXMLForTriplets(handler, cont.getTriplets());
         handler.endElement("font");
      }

   }

   private void generateXMLForTriplets(GenerationHelperContentHandler handler, Collection triplets) throws SAXException {
      AttributesImpl atts = new AttributesImpl();
      atts.clear();
      handler.startElement((String)"triplets", atts);
      Iterator var4 = triplets.iterator();

      while(var4.hasNext()) {
         Object triplet1 = var4.next();
         FontTriplet triplet = (FontTriplet)triplet1;
         atts.clear();
         atts.addAttribute("", "name", "name", "CDATA", triplet.getName());
         atts.addAttribute("", "style", "style", "CDATA", triplet.getStyle());
         atts.addAttribute("", "weight", "weight", "CDATA", Integer.toString(triplet.getWeight()));
         handler.element((String)"triplet", atts);
      }

      handler.endElement("triplets");
   }
}
