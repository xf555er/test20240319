package org.apache.fop.render.intermediate;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.apps.FOUserAgent;
import org.apache.xmlgraphics.util.QName;

public class IFContext implements PageIndexContext {
   private FOUserAgent userAgent;
   private Map foreignAttributes;
   private Locale language;
   private StructureTreeElement structureTreeElement;
   private String id;
   private String location;
   private boolean hyphenated;
   private int pageIndex;
   private int pageNumber;
   private RegionType regionType;

   public IFContext(FOUserAgent ua) {
      this.foreignAttributes = Collections.EMPTY_MAP;
      this.id = "";
      this.pageIndex = -1;
      this.pageNumber = -1;
      this.setUserAgent(ua);
   }

   public void setUserAgent(FOUserAgent ua) {
      if (this.userAgent != null) {
         throw new IllegalStateException("The user agent was already set");
      } else {
         this.userAgent = ua;
      }
   }

   public FOUserAgent getUserAgent() {
      return this.userAgent;
   }

   public Map getForeignAttributes() {
      return this.foreignAttributes;
   }

   public Object getForeignAttribute(QName qName) {
      return this.foreignAttributes.get(qName);
   }

   public void setForeignAttributes(Map foreignAttributes) {
      if (foreignAttributes != null) {
         this.foreignAttributes = foreignAttributes;
      } else {
         this.foreignAttributes = Collections.EMPTY_MAP;
      }

   }

   public void resetForeignAttributes() {
      this.setForeignAttributes((Map)null);
   }

   public void setLanguage(Locale lang) {
      this.language = lang;
   }

   public Locale getLanguage() {
      return this.language;
   }

   public void setStructureTreeElement(StructureTreeElement structureTreeElement) {
      this.structureTreeElement = structureTreeElement;
   }

   public void resetStructureTreeElement() {
      this.setStructureTreeElement((StructureTreeElement)null);
   }

   public StructureTreeElement getStructureTreeElement() {
      return this.structureTreeElement;
   }

   void setID(String id) {
      assert id != null;

      this.id = id;
   }

   String getID() {
      return this.id;
   }

   public void setLocation(String location) {
      this.location = location;
   }

   public String getLocation() {
      return this.location;
   }

   public void setHyphenated(boolean hyphenated) {
      this.hyphenated = hyphenated;
   }

   public boolean isHyphenated() {
      return this.hyphenated;
   }

   public void setPageIndex(int pageIndex) {
      this.pageIndex = pageIndex;
   }

   public int getPageIndex() {
      return this.pageIndex;
   }

   public int getPageNumber() {
      return this.pageNumber;
   }

   public void setPageNumber(int pageNumber) {
      this.pageNumber = pageNumber;
   }

   public String getRegionType() {
      return this.regionType != null ? this.regionType.name() : null;
   }

   public void setRegionType(String type) {
      this.regionType = null;
      if (type != null) {
         this.regionType = IFContext.RegionType.valueOf(type);
      }

   }

   public void setRegionType(int type) {
      this.regionType = null;
      if (type == 56) {
         this.regionType = IFContext.RegionType.Footer;
      } else if (type == 57) {
         this.regionType = IFContext.RegionType.Header;
      }

   }

   private static enum RegionType {
      Footer,
      Header;
   }
}
