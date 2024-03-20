package org.apache.fop.pdf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.apache.fop.fonts.FontDescriptor;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.Typeface;
import org.apache.fop.fonts.base14.Symbol;
import org.apache.fop.fonts.base14.ZapfDingbats;
import org.apache.xmlgraphics.java2d.color.profile.ColorProfileUtil;

public class PDFResources extends PDFDictionary {
   protected Map fonts = new LinkedHashMap();
   protected Set xObjects = new LinkedHashSet();
   protected Map colorSpaces = new LinkedHashMap();
   protected Map iccColorSpaces = new LinkedHashMap();
   private PDFResources parent;
   private PDFDictionary fontsObj;
   private Map fontsObjDict = new LinkedHashMap();
   protected Map properties = new LinkedHashMap();
   protected Set contexts = new LinkedHashSet();

   public PDFResources(PDFDocument doc) {
      this.setObjectNumber(doc);
   }

   public void addContext(PDFResourceContext c) {
      this.contexts.add(c);
   }

   public void setParentResources(PDFResources p) {
      this.parent = p;
   }

   public PDFResources getParentResources() {
      return this.parent;
   }

   public void addFont(PDFFont font) {
      this.addFont(font.getName(), font);
   }

   public void addFont(String name, PDFDictionary font) {
      if (this.fontsObj != null) {
         this.fontsObj.put(name, font);
         this.fontsObjDict.put(name, font);
      } else {
         this.fonts.put(name, font);
      }

   }

   public void createFontsAsObj() {
      this.fontsObj = new PDFDictionary();
      this.getDocument().registerTrailerObject(this.fontsObj);
      this.put("Font", this.fontsObj);
   }

   public void addFonts(PDFDocument doc, FontInfo fontInfo) {
      Map usedFonts = fontInfo.getUsedFonts();
      Iterator var4 = usedFonts.entrySet().iterator();

      while(true) {
         String f;
         Typeface font;
         do {
            if (!var4.hasNext()) {
               return;
            }

            Map.Entry e = (Map.Entry)var4.next();
            f = (String)e.getKey();
            font = (Typeface)e.getValue();
         } while(!font.hadMappingOperations());

         FontDescriptor desc = null;
         if (font instanceof FontDescriptor) {
            desc = (FontDescriptor)font;
         }

         String encoding = font.getEncodingName();
         if (font instanceof Symbol || font instanceof ZapfDingbats) {
            encoding = null;
         }

         this.addFont(doc.getFactory().makeFont(f, font.getEmbedFontName(), encoding, font, desc));
      }
   }

   public void addXObject(PDFXObject xObject) {
      this.xObjects.add(xObject);
   }

   public void addColorSpace(PDFColorSpace colorSpace) {
      this.colorSpaces.put(new LazyName(colorSpace), colorSpace);
      if (colorSpace instanceof PDFICCBasedColorSpace) {
         PDFICCBasedColorSpace icc = (PDFICCBasedColorSpace)colorSpace;
         String desc = ColorProfileUtil.getICCProfileDescription(icc.getICCStream().getICCProfile());
         this.iccColorSpaces.put(desc, icc);
      }

   }

   public PDFICCBasedColorSpace getICCColorSpaceByProfileName(String desc) {
      PDFICCBasedColorSpace cs = (PDFICCBasedColorSpace)this.iccColorSpaces.get(desc);
      return cs;
   }

   public PDFColorSpace getColorSpace(PDFName name) {
      Iterator var2 = this.colorSpaces.entrySet().iterator();

      Map.Entry x;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         x = (Map.Entry)var2.next();
      } while(!((LazyName)x.getKey()).getName().equals(name));

      return (PDFColorSpace)x.getValue();
   }

   public void addProperty(String name, PDFReference property) {
      this.properties.put(name, property);
   }

   public PDFReference getProperty(String name) {
      return (PDFReference)this.properties.get(name);
   }

   public int output(OutputStream stream) throws IOException {
      this.populateDictionary();
      return super.output(stream);
   }

   private void populateDictionary() {
      if (this.parent != null && this.parent.fontsObj != null) {
         this.put("Font", this.parent.fontsObj);
      }

      if (!this.fonts.isEmpty() || this.parent != null && !this.parent.fonts.isEmpty()) {
         PDFDictionary dict = new PDFDictionary(this);
         Iterator var2 = this.fonts.entrySet().iterator();

         Map.Entry entry;
         while(var2.hasNext()) {
            entry = (Map.Entry)var2.next();
            dict.put((String)entry.getKey(), entry.getValue());
         }

         if (this.parent != null) {
            var2 = this.parent.fonts.entrySet().iterator();

            while(var2.hasNext()) {
               entry = (Map.Entry)var2.next();
               dict.put((String)entry.getKey(), entry.getValue());
            }

            var2 = this.parent.fontsObjDict.entrySet().iterator();

            while(var2.hasNext()) {
               entry = (Map.Entry)var2.next();
               dict.put((String)entry.getKey(), entry.getValue());
            }
         }

         this.put("Font", dict);
      }

      Set patterns = new LinkedHashSet();
      Set shadings = new LinkedHashSet();
      Set gstates = new LinkedHashSet();
      Iterator var4 = this.contexts.iterator();

      PDFResourceContext c;
      while(var4.hasNext()) {
         c = (PDFResourceContext)var4.next();
         this.xObjects.addAll(c.getXObjects());
         patterns.addAll(c.getPatterns());
         shadings.addAll(c.getShadings());
         gstates.addAll(c.getGStates());
      }

      if (this.parent != null) {
         this.xObjects.addAll(this.parent.xObjects);
         var4 = this.parent.contexts.iterator();

         while(var4.hasNext()) {
            c = (PDFResourceContext)var4.next();
            patterns.addAll(c.getPatterns());
            shadings.addAll(c.getShadings());
            gstates.addAll(c.getGStates());
         }
      }

      PDFDictionary dict;
      Iterator var12;
      if (!shadings.isEmpty()) {
         dict = (PDFDictionary)this.get("Shading");
         if (dict == null) {
            dict = new PDFDictionary(this);
         }

         var12 = shadings.iterator();

         while(var12.hasNext()) {
            PDFShading shading = (PDFShading)var12.next();
            dict.put(shading.getName(), shading);
         }

         this.put("Shading", dict);
      }

      if (!patterns.isEmpty()) {
         dict = (PDFDictionary)this.get("Pattern");
         if (dict == null) {
            dict = new PDFDictionary(this);
         }

         var12 = patterns.iterator();

         while(var12.hasNext()) {
            PDFPattern pattern = (PDFPattern)var12.next();
            dict.put(pattern.getName(), pattern);
         }

         this.put("Pattern", dict);
      }

      PDFArray procset = new PDFArray(this);
      procset.add(new PDFName("PDF"));
      procset.add(new PDFName("ImageB"));
      procset.add(new PDFName("ImageC"));
      procset.add(new PDFName("Text"));
      this.put("ProcSet", procset);
      PDFDictionary dict;
      Iterator var15;
      if (!this.xObjects.isEmpty()) {
         dict = (PDFDictionary)this.get("XObject");
         if (dict == null) {
            dict = new PDFDictionary(this);
         }

         var15 = this.xObjects.iterator();

         while(var15.hasNext()) {
            PDFXObject xObject = (PDFXObject)var15.next();
            dict.put(xObject.getName().toString(), xObject);
         }

         this.put("XObject", dict);
      }

      if (!gstates.isEmpty()) {
         dict = (PDFDictionary)this.get("ExtGState");
         if (dict == null) {
            dict = new PDFDictionary(this);
         }

         var15 = gstates.iterator();

         while(var15.hasNext()) {
            PDFGState gstate = (PDFGState)var15.next();
            dict.put(gstate.getName(), gstate);
         }

         this.put("ExtGState", dict);
      }

      if (!this.colorSpaces.isEmpty() || this.parent != null && !this.parent.colorSpaces.isEmpty()) {
         dict = (PDFDictionary)this.get("ColorSpace");
         if (dict == null) {
            dict = new PDFDictionary(this);
         }

         PDFColorSpace colorSpace;
         if (this.parent != null) {
            var15 = this.parent.colorSpaces.values().iterator();

            while(var15.hasNext()) {
               colorSpace = (PDFColorSpace)var15.next();
               dict.put(colorSpace.getName(), colorSpace);
            }
         }

         var15 = this.colorSpaces.values().iterator();

         while(var15.hasNext()) {
            colorSpace = (PDFColorSpace)var15.next();
            dict.put(colorSpace.getName(), colorSpace);
         }

         this.put("ColorSpace", dict);
      }

      if (!this.properties.isEmpty()) {
         dict = new PDFDictionary(this);
         var15 = this.properties.entrySet().iterator();

         while(var15.hasNext()) {
            Map.Entry stringPDFReferenceEntry = (Map.Entry)var15.next();
            dict.put((String)stringPDFReferenceEntry.getKey(), stringPDFReferenceEntry.getValue());
         }

         this.put("Properties", dict);
      }

   }

   public void getChildren(Set children) {
      this.getChildren(children, false);
   }

   private void getChildren(Set children, boolean isParent) {
      super.getChildren(children);
      Iterator var3 = this.fonts.values().iterator();

      while(var3.hasNext()) {
         PDFDictionary f = (PDFDictionary)var3.next();
         children.add(f);
         f.getChildren(children);
      }

      var3 = this.contexts.iterator();

      while(var3.hasNext()) {
         PDFResourceContext c = (PDFResourceContext)var3.next();
         Iterator var5 = c.getXObjects().iterator();

         while(var5.hasNext()) {
            PDFXObject x = (PDFXObject)var5.next();
            children.add(x);
            x.getChildren(children);
         }

         var5 = c.getPatterns().iterator();

         while(var5.hasNext()) {
            PDFPattern x = (PDFPattern)var5.next();
            children.add(x);
            x.getChildren(children);
         }

         var5 = c.getShadings().iterator();

         while(var5.hasNext()) {
            PDFShading x = (PDFShading)var5.next();
            children.add(x);
            x.getChildren(children);
         }

         var5 = c.getGStates().iterator();

         while(var5.hasNext()) {
            PDFGState x = (PDFGState)var5.next();
            children.add(x);
            x.getChildren(children);
         }
      }

      if (!isParent) {
         var3 = this.colorSpaces.values().iterator();

         while(var3.hasNext()) {
            PDFColorSpace x = (PDFColorSpace)var3.next();
            children.add((PDFObject)x);
            ((PDFObject)x).getChildren(children);
         }
      }

      if (this.parent != null) {
         this.parent.getChildren(children, true);
      }

   }

   static class LazyName {
      private PDFColorSpace colorSpace;

      public LazyName(PDFColorSpace colorSpace) {
         this.colorSpace = colorSpace;
      }

      public PDFName getName() {
         return new PDFName(this.colorSpace.getName());
      }
   }
}
