package org.apache.fop.render.pdf;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.events.EventBroadcaster;
import org.apache.fop.pdf.PDFFactory;
import org.apache.fop.pdf.PDFParentTree;
import org.apache.fop.pdf.PDFStructElem;
import org.apache.fop.pdf.PDFStructTreeRoot;
import org.apache.fop.pdf.StandardStructureAttributes;
import org.apache.fop.pdf.StandardStructureTypes;
import org.apache.fop.pdf.StructureHierarchyMember;
import org.apache.fop.pdf.StructureType;
import org.apache.fop.util.LanguageTags;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

public class PDFStructureTreeBuilder implements StructureTreeEventHandler {
   private static final String ROLE = "role";
   private static final Map BUILDERS = new HashMap();
   private static final StructureElementBuilder DEFAULT_BUILDER;
   private PDFFactory pdfFactory;
   private EventBroadcaster eventBroadcaster;
   private LinkedList ancestors = new LinkedList();
   private PDFStructElem rootStructureElement;

   private static void addBuilder(String fo, StructureType structureType) {
      addBuilder(fo, (StructureElementBuilder)(new DefaultStructureElementBuilder(structureType)));
   }

   private static void addBuilder(String fo, StructureElementBuilder mapper) {
      BUILDERS.put(fo, mapper);
   }

   void setPdfFactory(PDFFactory pdfFactory) {
      this.pdfFactory = pdfFactory;
   }

   void setEventBroadcaster(EventBroadcaster eventBroadcaster) {
      this.eventBroadcaster = eventBroadcaster;
   }

   void setLogicalStructureHandler(PDFLogicalStructureHandler logicalStructureHandler) {
      this.createRootStructureElement(logicalStructureHandler);
   }

   private void createRootStructureElement(PDFLogicalStructureHandler logicalStructureHandler) {
      assert this.rootStructureElement == null;

      PDFParentTree parentTree = logicalStructureHandler.getParentTree();
      PDFStructTreeRoot structTreeRoot = this.pdfFactory.getDocument().makeStructTreeRoot(parentTree);
      this.rootStructureElement = createStructureElement("root", structTreeRoot, new AttributesImpl(), this.pdfFactory, this.eventBroadcaster);
   }

   public static PDFStructElem createStructureElement(String name, StructureHierarchyMember parent, Attributes attributes, PDFFactory pdfFactory, EventBroadcaster eventBroadcaster) {
      StructureElementBuilder builder = (StructureElementBuilder)BUILDERS.get(name);
      if (builder == null) {
         builder = DEFAULT_BUILDER;
      }

      return builder.build(parent, attributes, pdfFactory, eventBroadcaster);
   }

   public void startPageSequence(Locale language, String role) {
      this.ancestors = new LinkedList();
      AttributesImpl attributes = new AttributesImpl();
      attributes.addAttribute("", "role", "role", "CDATA", role);
      PDFStructElem structElem = createStructureElement("page-sequence", this.rootStructureElement, attributes, this.pdfFactory, this.eventBroadcaster);
      if (language != null) {
         structElem.setLanguage(language);
      }

      this.ancestors.add(structElem);
   }

   public void endPageSequence() {
   }

   public StructureTreeElement startNode(String name, Attributes attributes, StructureTreeElement parent) {
      if (!this.isPDFA1Safe(name)) {
         return null;
      } else {
         assert parent == null || parent instanceof PDFStructElem;

         PDFStructElem parentElem = parent == null ? (PDFStructElem)this.ancestors.getFirst() : (PDFStructElem)parent;
         PDFStructElem structElem = createStructureElement(name, parentElem, attributes, this.pdfFactory, this.eventBroadcaster);
         this.ancestors.addFirst(structElem);
         return structElem;
      }
   }

   public void endNode(String name) {
      if (this.isPDFA1Safe(name)) {
         this.ancestors.removeFirst();
      }

   }

   private boolean isPDFA1Safe(String name) {
      return !this.pdfFactory.getDocument().getProfile().getPDFAMode().isPart1() && !this.pdfFactory.getDocument().getProfile().getPDFUAMode().isEnabled() || !name.equals("table-body") && !name.equals("table-header") && !name.equals("table-footer");
   }

   public StructureTreeElement startImageNode(String name, Attributes attributes, StructureTreeElement parent) {
      return this.startNode(name, attributes, parent);
   }

   public StructureTreeElement startReferencedNode(String name, Attributes attributes, StructureTreeElement parent) {
      return this.startNode(name, attributes, parent);
   }

   static {
      DEFAULT_BUILDER = new DefaultStructureElementBuilder(StandardStructureTypes.Grouping.NON_STRUCT);
      StructureElementBuilder regionBuilder = new RegionBuilder();
      addBuilder("root", StandardStructureTypes.Grouping.DOCUMENT);
      addBuilder("page-sequence", (StructureElementBuilder)(new PageSequenceBuilder()));
      addBuilder("static-content", (StructureElementBuilder)regionBuilder);
      addBuilder("flow", (StructureElementBuilder)regionBuilder);
      addBuilder("block", (StructureElementBuilder)(new LanguageHolderBuilder(StandardStructureTypes.Paragraphlike.P)));
      addBuilder("block-container", StandardStructureTypes.Grouping.DIV);
      addBuilder("character", (StructureElementBuilder)(new LanguageHolderBuilder(StandardStructureTypes.InlineLevelStructure.SPAN)));
      addBuilder("external-graphic", (StructureElementBuilder)(new ImageBuilder()));
      addBuilder("instream-foreign-object", (StructureElementBuilder)(new ImageBuilder()));
      addBuilder("inline", (StructureElementBuilder)(new InlineHolderBuilder()));
      addBuilder("inline-container", StandardStructureTypes.Grouping.DIV);
      addBuilder("page-number", StandardStructureTypes.InlineLevelStructure.QUOTE);
      addBuilder("page-number-citation", StandardStructureTypes.InlineLevelStructure.QUOTE);
      addBuilder("page-number-citation-last", StandardStructureTypes.InlineLevelStructure.QUOTE);
      addBuilder("table-and-caption", StandardStructureTypes.Grouping.DIV);
      addBuilder("table", (StructureElementBuilder)(new TableBuilder()));
      addBuilder("table-caption", StandardStructureTypes.Grouping.CAPTION);
      addBuilder("table-header", StandardStructureTypes.Table.THEAD);
      addBuilder("table-footer", (StructureElementBuilder)(new TableFooterBuilder()));
      addBuilder("table-body", StandardStructureTypes.Table.TBODY);
      addBuilder("table-row", StandardStructureTypes.Table.TR);
      addBuilder("table-cell", (StructureElementBuilder)(new TableCellBuilder()));
      addBuilder("list-block", StandardStructureTypes.List.L);
      addBuilder("list-item", StandardStructureTypes.List.LI);
      addBuilder("list-item-body", StandardStructureTypes.List.LBODY);
      addBuilder("list-item-label", StandardStructureTypes.List.LBL);
      addBuilder("basic-link", (StructureElementBuilder)(new LinkBuilder()));
      addBuilder("float", StandardStructureTypes.Grouping.DIV);
      addBuilder("footnote", StandardStructureTypes.InlineLevelStructure.NOTE);
      addBuilder("footnote-body", StandardStructureTypes.Grouping.SECT);
      addBuilder("wrapper", StandardStructureTypes.InlineLevelStructure.SPAN);
      addBuilder("marker", StandardStructureTypes.Grouping.PRIVATE);
      addBuilder("retrieve-marker", (StructureElementBuilder)(new PlaceholderBuilder()));
      addBuilder("retrieve-table-marker", (StructureElementBuilder)(new PlaceholderBuilder()));
      addBuilder("#PCDATA", (StructureElementBuilder)(new PlaceholderBuilder()));
   }

   private static class PlaceholderBuilder implements StructureElementBuilder {
      private PlaceholderBuilder() {
      }

      public PDFStructElem build(StructureHierarchyMember parent, Attributes attributes, PDFFactory pdfFactory, EventBroadcaster eventBroadcaster) {
         PDFStructElem elem = new PDFStructElem.Placeholder(parent);
         parent.addKid(elem);
         return elem;
      }

      // $FF: synthetic method
      PlaceholderBuilder(Object x0) {
         this();
      }
   }

   private static class TableCellBuilder extends DefaultStructureElementBuilder {
      TableCellBuilder() {
         super(StandardStructureTypes.Table.TD);
      }

      protected void registerStructureElement(PDFStructElem structureElement, PDFFactory pdfFactory, Attributes attributes) {
         if (structureElement.getStructureType() == StandardStructureTypes.Table.TH) {
            String scopeAttribute = attributes.getValue("http://xmlgraphics.apache.org/fop/internal", "scope");
            StandardStructureAttributes.Table.Scope scope = scopeAttribute == null ? StandardStructureAttributes.Table.Scope.COLUMN : StandardStructureAttributes.Table.Scope.valueOf(scopeAttribute.toUpperCase(Locale.ENGLISH));
            pdfFactory.getDocument().registerStructureElement(structureElement, scope);
         } else {
            pdfFactory.getDocument().registerStructureElement(structureElement);
         }

      }

      protected void setAttributes(PDFStructElem structElem, Attributes attributes) {
         String columnSpan = attributes.getValue("number-columns-spanned");
         if (columnSpan != null) {
            structElem.setTableAttributeColSpan(Integer.parseInt(columnSpan));
         }

         String rowSpan = attributes.getValue("number-rows-spanned");
         if (rowSpan != null) {
            structElem.setTableAttributeRowSpan(Integer.parseInt(rowSpan));
         }

      }
   }

   private static class TableFooterBuilder extends DefaultStructureElementBuilder {
      public TableFooterBuilder() {
         super(StandardStructureTypes.Table.TFOOT);
      }

      protected void addKidToParent(PDFStructElem kid, StructureHierarchyMember parent, Attributes attributes) {
         ((TableStructElem)parent).addTableFooter(kid);
      }
   }

   private static class TableBuilder extends DefaultStructureElementBuilder {
      TableBuilder() {
         super(StandardStructureTypes.Table.TABLE);
      }

      protected PDFStructElem createStructureElement(StructureHierarchyMember parent, StructureType structureType) {
         return new TableStructElem(parent, structureType);
      }
   }

   private static class LinkBuilder extends DefaultStructureElementBuilder {
      LinkBuilder() {
         super(StandardStructureTypes.InlineLevelStructure.LINK);
      }

      protected void setAttributes(PDFStructElem structElem, Attributes attributes) {
         super.setAttributes(structElem, attributes);
         String altTextNode = attributes.getValue("http://xmlgraphics.apache.org/fop/extensions", "alt-text");
         if (altTextNode == null) {
            altTextNode = "No alternate text specified";
         }

         structElem.put("Alt", altTextNode);
      }
   }

   private static class ImageBuilder extends DefaultStructureElementBuilder {
      ImageBuilder() {
         super(StandardStructureTypes.Illustration.FIGURE);
      }

      protected void setAttributes(PDFStructElem structElem, Attributes attributes) {
         String altTextNode = attributes.getValue("http://xmlgraphics.apache.org/fop/extensions", "alt-text");
         if (altTextNode == null) {
            altTextNode = "No alternate text specified";
         }

         structElem.put("Alt", altTextNode);
      }
   }

   private static class InlineHolderBuilder extends DefaultStructureElementBuilder {
      InlineHolderBuilder() {
         super(StandardStructureTypes.InlineLevelStructure.SPAN);
      }

      protected void setAttributes(PDFStructElem structElem, Attributes attributes) {
         String text = attributes.getValue("http://xmlgraphics.apache.org/fop/extensions", "abbreviation");
         if (text != null && !text.equals("")) {
            structElem.put("E", text);
         }

      }
   }

   private static class LanguageHolderBuilder extends DefaultStructureElementBuilder {
      LanguageHolderBuilder(StructureType structureType) {
         super(structureType);
      }

      protected void setAttributes(PDFStructElem structElem, Attributes attributes) {
         String xmlLang = attributes.getValue("http://www.w3.org/XML/1998/namespace", "lang");
         if (xmlLang != null) {
            Locale locale = LanguageTags.toLocale(xmlLang);
            structElem.setLanguage(locale);
         }

      }
   }

   private static class RegionBuilder extends DefaultStructureElementBuilder {
      RegionBuilder() {
         super(StandardStructureTypes.Grouping.SECT);
      }

      protected void addKidToParent(PDFStructElem kid, StructureHierarchyMember parent, Attributes attributes) {
         String flowName = attributes.getValue("flow-name");
         ((PageSequenceStructElem)parent).addContent(flowName, kid);
      }
   }

   private static class PageSequenceBuilder extends DefaultStructureElementBuilder {
      PageSequenceBuilder() {
         super(StandardStructureTypes.Grouping.PART);
      }

      protected PDFStructElem createStructureElement(StructureHierarchyMember parent, StructureType structureType) {
         return new PageSequenceStructElem(parent, structureType);
      }
   }

   private static class DefaultStructureElementBuilder implements StructureElementBuilder {
      private final StructureType defaultStructureType;

      DefaultStructureElementBuilder(StructureType structureType) {
         this.defaultStructureType = structureType;
      }

      public final PDFStructElem build(StructureHierarchyMember parent, Attributes attributes, PDFFactory pdfFactory, EventBroadcaster eventBroadcaster) {
         String role = attributes.getValue("role");
         StructureType structureType;
         if (role == null) {
            structureType = this.defaultStructureType;
         } else {
            structureType = StandardStructureTypes.get(role);
            if (structureType == null) {
               structureType = this.defaultStructureType;
               PDFEventProducer.Provider.get(eventBroadcaster).nonStandardStructureType(role, role, structureType.toString());
            }
         }

         PDFStructElem structElem = this.createStructureElement(parent, structureType);
         this.setAttributes(structElem, attributes);
         this.addKidToParent(structElem, parent, attributes);
         this.registerStructureElement(structElem, pdfFactory, attributes);
         return structElem;
      }

      protected PDFStructElem createStructureElement(StructureHierarchyMember parent, StructureType structureType) {
         return new PDFStructElem(parent, structureType);
      }

      protected void setAttributes(PDFStructElem structElem, Attributes attributes) {
      }

      protected void addKidToParent(PDFStructElem kid, StructureHierarchyMember parent, Attributes attributes) {
         parent.addKid(kid);
      }

      protected void registerStructureElement(PDFStructElem structureElement, PDFFactory pdfFactory, Attributes attributes) {
         pdfFactory.getDocument().registerStructureElement(structureElement);
      }
   }

   private interface StructureElementBuilder {
      PDFStructElem build(StructureHierarchyMember var1, Attributes var2, PDFFactory var3, EventBroadcaster var4);
   }
}
