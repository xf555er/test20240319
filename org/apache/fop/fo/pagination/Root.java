package org.apache.fop.fo.pagination;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOTreeBuilderContext;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.apache.fop.fo.extensions.destination.Destination;
import org.apache.fop.fo.pagination.bookmarks.BookmarkTree;
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;
import org.apache.fop.fo.properties.CommonHyphenation;
import org.xml.sax.Locator;

public class Root extends FObj implements CommonAccessibilityHolder {
   private CommonAccessibility commonAccessibility;
   private int mediaUsage;
   private LayoutMasterSet layoutMasterSet;
   private Declarations declarations;
   private BookmarkTree bookmarkTree;
   private List destinationList;
   private List pageSequences = new ArrayList();
   private Locale locale;
   private boolean pageSequenceFound;
   private int endingPageNumberOfPreviousSequence;
   private int totalPagesGenerated;
   private FOTreeBuilderContext builderContext;
   private FOEventHandler foEventHandler;
   private PageSequence lastSeq;

   public void setLastSeq(PageSequence seq) {
      this.lastSeq = seq;
   }

   public PageSequence getLastSeq() {
      return this.lastSeq;
   }

   public Root(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
      super.bind(pList);
      this.commonAccessibility = CommonAccessibility.getInstance(pList);
      this.mediaUsage = pList.get(161).getEnum();
      String language = pList.get(134).getString();
      String country = pList.get(81).getString();
      this.locale = CommonHyphenation.toLocale(language, country);
   }

   public void startOfNode() throws FOPException {
      this.foEventHandler.startRoot(this);
   }

   public void endOfNode() throws FOPException {
      if (!this.pageSequenceFound || this.layoutMasterSet == null) {
         this.missingChildElementError("(layout-master-set, declarations?, bookmark-tree?, (page-sequence|fox:external-document)+)");
      }

      this.foEventHandler.endRoot(this);
   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI)) {
         if (localName.equals("layout-master-set")) {
            if (this.layoutMasterSet != null) {
               this.tooManyNodesError(loc, "fo:layout-master-set");
            }
         } else if (localName.equals("declarations")) {
            if (this.layoutMasterSet == null) {
               this.nodesOutOfOrderError(loc, "fo:layout-master-set", "fo:declarations");
            } else if (this.declarations != null) {
               this.tooManyNodesError(loc, "fo:declarations");
            } else if (this.bookmarkTree != null) {
               this.nodesOutOfOrderError(loc, "fo:declarations", "fo:bookmark-tree");
            } else if (this.pageSequenceFound) {
               this.nodesOutOfOrderError(loc, "fo:declarations", "fo:page-sequence");
            }
         } else if (localName.equals("bookmark-tree")) {
            if (this.layoutMasterSet == null) {
               this.nodesOutOfOrderError(loc, "fo:layout-master-set", "fo:bookmark-tree");
            } else if (this.bookmarkTree != null) {
               this.tooManyNodesError(loc, "fo:bookmark-tree");
            } else if (this.pageSequenceFound) {
               this.nodesOutOfOrderError(loc, "fo:bookmark-tree", "fo:page-sequence");
            }
         } else if (localName.equals("page-sequence")) {
            if (this.layoutMasterSet == null) {
               this.nodesOutOfOrderError(loc, "fo:layout-master-set", "fo:page-sequence");
            } else {
               this.pageSequenceFound = true;
            }
         } else {
            this.invalidChildError(loc, nsURI, localName);
         }
      } else if ("http://xmlgraphics.apache.org/fop/extensions".equals(nsURI) && "external-document".equals(localName)) {
         this.pageSequenceFound = true;
      }

   }

   protected void validateChildNode(Locator loc, FONode child) throws ValidationException {
      if (child instanceof AbstractPageSequence) {
         this.pageSequenceFound = true;
      }

   }

   public CommonAccessibility getCommonAccessibility() {
      return this.commonAccessibility;
   }

   public void setFOEventHandler(FOEventHandler foEventHandler) {
      this.foEventHandler = foEventHandler;
   }

   public FOEventHandler getFOEventHandler() {
      return this.foEventHandler;
   }

   public void setBuilderContext(FOTreeBuilderContext context) {
      this.builderContext = context;
   }

   public FOTreeBuilderContext getBuilderContext() {
      return this.builderContext;
   }

   public int getEndingPageNumberOfPreviousSequence() {
      return this.endingPageNumberOfPreviousSequence;
   }

   public int getTotalPagesGenerated() {
      return this.totalPagesGenerated;
   }

   public void notifyPageSequenceFinished(int lastPageNumber, int additionalPages) throws IllegalArgumentException {
      if (additionalPages >= 0) {
         this.totalPagesGenerated += additionalPages;
         this.endingPageNumberOfPreviousSequence = lastPageNumber;
      } else {
         throw new IllegalArgumentException("Number of additional pages must be zero or greater.");
      }
   }

   public int getPageSequenceCount() {
      return this.pageSequences.size();
   }

   public PageSequence getSucceedingPageSequence(PageSequence current) {
      int currentIndex = this.pageSequences.indexOf(current);
      if (currentIndex == -1) {
         return null;
      } else {
         return currentIndex < this.pageSequences.size() - 1 ? (PageSequence)this.pageSequences.get(currentIndex + 1) : null;
      }
   }

   public void addPageSequence(PageSequence pageSequence) {
      this.pageSequences.add(pageSequence);
   }

   public PageSequence getLastPageSequence() {
      return this.getPageSequenceCount() > 0 ? (PageSequence)this.pageSequences.get(this.getPageSequenceCount() - 1) : null;
   }

   public LayoutMasterSet getLayoutMasterSet() {
      return this.layoutMasterSet;
   }

   public void setLayoutMasterSet(LayoutMasterSet layoutMasterSet) {
      this.layoutMasterSet = layoutMasterSet;
   }

   public Declarations getDeclarations() {
      return this.declarations;
   }

   public void setDeclarations(Declarations declarations) {
      this.declarations = declarations;
   }

   public void setBookmarkTree(BookmarkTree bookmarkTree) {
      this.bookmarkTree = bookmarkTree;
   }

   public void addDestination(Destination destination) {
      if (this.destinationList == null) {
         this.destinationList = new ArrayList();
      }

      this.destinationList.add(destination);
   }

   public List getDestinationList() {
      return this.destinationList;
   }

   public BookmarkTree getBookmarkTree() {
      return this.bookmarkTree;
   }

   public Root getRoot() {
      return this;
   }

   public String getLocalName() {
      return "root";
   }

   public int getNameId() {
      return 66;
   }

   public Locale getLocale() {
      return this.locale;
   }
}
