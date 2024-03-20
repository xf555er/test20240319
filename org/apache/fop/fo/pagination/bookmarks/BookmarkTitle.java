package org.apache.fop.fo.pagination.bookmarks;

import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;
import org.xml.sax.Locator;

public class BookmarkTitle extends FObj implements CommonAccessibilityHolder {
   private CommonAccessibility commonAccessibility;
   private String title = "";

   public BookmarkTitle(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.commonAccessibility = CommonAccessibility.getInstance(pList);
   }

   protected void characters(char[] data, int start, int length, PropertyList pList, Locator locator) {
      this.title = this.title + new String(data, start, length);
   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI)) {
         this.invalidChildError(loc, nsURI, localName);
      }

   }

   public CommonAccessibility getCommonAccessibility() {
      return this.commonAccessibility;
   }

   public String getTitle() {
      return this.title;
   }

   public String getLocalName() {
      return "bookmark-title";
   }

   public int getNameId() {
      return 7;
   }
}
