package org.apache.fop.accessibility.fo;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import org.apache.fop.accessibility.StructureTreeElement;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.flow.AbstractRetrieveMarker;
import org.apache.fop.fo.flow.BasicLink;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.Footnote;
import org.apache.fop.fo.flow.FootnoteBody;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.ListItemBody;
import org.apache.fop.fo.flow.ListItemLabel;
import org.apache.fop.fo.flow.PageNumber;
import org.apache.fop.fo.flow.PageNumberCitation;
import org.apache.fop.fo.flow.PageNumberCitationLast;
import org.apache.fop.fo.flow.RetrieveMarker;
import org.apache.fop.fo.flow.RetrieveTableMarker;
import org.apache.fop.fo.flow.Wrapper;
import org.apache.fop.fo.flow.table.Table;
import org.apache.fop.fo.flow.table.TableBody;
import org.apache.fop.fo.flow.table.TableCell;
import org.apache.fop.fo.flow.table.TableFooter;
import org.apache.fop.fo.flow.table.TableHeader;
import org.apache.fop.fo.flow.table.TableRow;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.LayoutMasterSet;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;
import org.apache.fop.fo.properties.CommonHyphenation;
import org.apache.fop.util.LanguageTags;
import org.xml.sax.helpers.AttributesImpl;

class StructureTreeEventTrigger extends FOEventHandler {
   private StructureTreeEventHandler structureTreeEventHandler;
   private LayoutMasterSet layoutMasterSet;
   private Stack tables = new Stack();
   private Stack inTableHeader = new Stack();
   private Stack locales = new Stack();
   private final Map states = new HashMap();

   public StructureTreeEventTrigger(StructureTreeEventHandler structureTreeEventHandler) {
      this.structureTreeEventHandler = structureTreeEventHandler;
   }

   public void startRoot(Root root) {
      this.locales.push(root.getLocale());
   }

   public void endRoot(Root root) {
      this.locales.pop();
   }

   public void startPageSequence(PageSequence pageSeq) {
      if (this.layoutMasterSet == null) {
         this.layoutMasterSet = pageSeq.getRoot().getLayoutMasterSet();
      }

      Locale locale = pageSeq.getLocale();
      if (locale != null) {
         this.locales.push(locale);
      } else {
         this.locales.push(this.locales.peek());
      }

      String role = pageSeq.getCommonAccessibility().getRole();
      this.structureTreeEventHandler.startPageSequence(locale, role);
   }

   public void endPageSequence(PageSequence pageSeq) {
      this.structureTreeEventHandler.endPageSequence();
      this.locales.pop();
   }

   public void startPageNumber(PageNumber pagenum) {
      this.startElementWithID(pagenum);
   }

   public void endPageNumber(PageNumber pagenum) {
      this.endElement(pagenum);
   }

   public void startPageNumberCitation(PageNumberCitation pageCite) {
      this.startElementWithID(pageCite);
   }

   public void endPageNumberCitation(PageNumberCitation pageCite) {
      this.endElement(pageCite);
   }

   public void startPageNumberCitationLast(PageNumberCitationLast pageLast) {
      this.startElementWithID(pageLast);
   }

   public void endPageNumberCitationLast(PageNumberCitationLast pageLast) {
      this.endElement(pageLast);
   }

   public void startStatic(StaticContent staticContent) {
      AttributesImpl flowName = this.createFlowNameAttribute(staticContent.getFlowName());
      this.startElement(staticContent, flowName);
   }

   private AttributesImpl createFlowNameAttribute(String flowName) {
      String regionName = this.layoutMasterSet.getDefaultRegionNameFor(flowName);
      AttributesImpl attribute = new AttributesImpl();
      this.addNoNamespaceAttribute(attribute, "flow-name", regionName);
      return attribute;
   }

   public void endStatic(StaticContent staticContent) {
      this.endElement(staticContent);
   }

   public void startFlow(Flow fl) {
      AttributesImpl flowName = this.createFlowNameAttribute(fl.getFlowName());
      this.startElement(fl, flowName);
   }

   public void endFlow(Flow fl) {
      this.endElement(fl);
   }

   public void startBlock(Block bl) {
      CommonHyphenation hyphProperties = bl.getCommonHyphenation();
      AttributesImpl attributes = this.createLangAttribute(hyphProperties);
      this.startElement(bl, attributes);
   }

   private AttributesImpl createLangAttribute(CommonHyphenation hyphProperties) {
      Locale locale = hyphProperties.getLocale();
      AttributesImpl attributes = new AttributesImpl();
      if (locale != null && !locale.equals(this.locales.peek())) {
         this.locales.push(locale);
         this.addAttribute(attributes, "http://www.w3.org/XML/1998/namespace", "lang", "xml", LanguageTags.toLanguageTag(locale));
      } else {
         this.locales.push(this.locales.peek());
      }

      return attributes;
   }

   public void endBlock(Block bl) {
      this.endElement(bl);
      this.locales.pop();
   }

   public void startBlockContainer(BlockContainer blc) {
      this.startElement(blc);
   }

   public void endBlockContainer(BlockContainer blc) {
      this.endElement(blc);
   }

   public void startInline(Inline inl) {
      this.startElement(inl);
   }

   public void endInline(Inline inl) {
      this.endElement(inl);
   }

   public void startTable(Table tbl) {
      this.tables.push(tbl);
      this.startElement(tbl);
   }

   public void endTable(Table tbl) {
      this.endElement(tbl);
      this.tables.pop();
   }

   public void startHeader(TableHeader header) {
      this.inTableHeader.push(Boolean.TRUE);
      this.startElement(header);
   }

   public void endHeader(TableHeader header) {
      this.endElement(header);
      this.inTableHeader.pop();
   }

   public void startFooter(TableFooter footer) {
      this.inTableHeader.push(Boolean.FALSE);
      this.startElement(footer);
   }

   public void endFooter(TableFooter footer) {
      this.endElement(footer);
      this.inTableHeader.pop();
   }

   public void startBody(TableBody body) {
      this.inTableHeader.push(Boolean.FALSE);
      this.startElement(body);
   }

   public void endBody(TableBody body) {
      this.endElement(body);
      this.inTableHeader.pop();
   }

   public void startRow(TableRow tr) {
      this.startElement(tr);
   }

   public void endRow(TableRow tr) {
      this.endElement(tr);
   }

   public void startCell(TableCell tc) {
      AttributesImpl attributes = new AttributesImpl();
      this.addSpanAttribute(attributes, "number-columns-spanned", tc.getNumberColumnsSpanned());
      this.addSpanAttribute(attributes, "number-rows-spanned", tc.getNumberRowsSpanned());
      boolean rowHeader = (Boolean)this.inTableHeader.peek();
      boolean columnHeader = ((Table)this.tables.peek()).getColumn(tc.getColumnNumber() - 1).isHeader();
      if (rowHeader || columnHeader) {
         String th = "TH";
         String role = tc.getCommonAccessibility().getRole();
         if (role == null) {
            role = "TH";
            this.addNoNamespaceAttribute(attributes, "role", "TH");
         }

         if (role.equals("TH") && columnHeader) {
            String scope = rowHeader ? "Both" : "Row";
            this.addAttribute(attributes, "http://xmlgraphics.apache.org/fop/internal", "scope", "foi", scope);
         }
      }

      this.startElement(tc, attributes);
   }

   private void addSpanAttribute(AttributesImpl attributes, String attributeName, int span) {
      if (span > 1) {
         this.addNoNamespaceAttribute(attributes, attributeName, Integer.toString(span));
      }

   }

   public void endCell(TableCell tc) {
      this.endElement(tc);
   }

   public void startList(ListBlock lb) {
      this.startElement(lb);
   }

   public void endList(ListBlock lb) {
      this.endElement(lb);
   }

   public void startListItem(ListItem li) {
      this.startElement(li);
   }

   public void endListItem(ListItem li) {
      this.endElement(li);
   }

   public void startListLabel(ListItemLabel listItemLabel) {
      this.startElement(listItemLabel);
   }

   public void endListLabel(ListItemLabel listItemLabel) {
      this.endElement(listItemLabel);
   }

   public void startListBody(ListItemBody listItemBody) {
      this.startElement(listItemBody);
   }

   public void endListBody(ListItemBody listItemBody) {
      this.endElement(listItemBody);
   }

   public void startLink(BasicLink basicLink) {
      this.startElementWithIDAndAltText(basicLink, basicLink.getAltText());
   }

   public void endLink(BasicLink basicLink) {
      this.endElement(basicLink);
   }

   public void image(ExternalGraphic eg) {
      this.startElementWithIDAndAltText(eg, eg.getAltText());
      this.endElement(eg);
   }

   public void startInstreamForeignObject(InstreamForeignObject ifo) {
      this.startElementWithIDAndAltText(ifo, ifo.getAltText());
   }

   public void endInstreamForeignObject(InstreamForeignObject ifo) {
      this.endElement(ifo);
   }

   public void startFootnote(Footnote footnote) {
      this.startElement(footnote);
   }

   public void endFootnote(Footnote footnote) {
      this.endElement(footnote);
   }

   public void startFootnoteBody(FootnoteBody body) {
      this.startElement(body);
   }

   public void endFootnoteBody(FootnoteBody body) {
      this.endElement(body);
   }

   public void startWrapper(Wrapper wrapper) {
      this.startElement(wrapper);
   }

   public void endWrapper(Wrapper wrapper) {
      this.endElement(wrapper);
   }

   public void startRetrieveMarker(RetrieveMarker retrieveMarker) {
      this.startElementWithID(retrieveMarker);
      this.saveState(retrieveMarker);
   }

   void saveState(AbstractRetrieveMarker retrieveMarker) {
      this.states.put(retrieveMarker, new State(this));
   }

   public void endRetrieveMarker(RetrieveMarker retrieveMarker) {
      this.endElement(retrieveMarker);
   }

   public void restoreState(RetrieveMarker retrieveMarker) {
      this.restoreRetrieveMarkerState(retrieveMarker);
   }

   private void restoreRetrieveMarkerState(AbstractRetrieveMarker retrieveMarker) {
      State state = (State)this.states.get(retrieveMarker);
      this.tables = (Stack)state.tables.clone();
      this.inTableHeader = (Stack)state.inTableHeader.clone();
      this.locales = (Stack)state.locales.clone();
   }

   public void startRetrieveTableMarker(RetrieveTableMarker retrieveTableMarker) {
      this.startElementWithID(retrieveTableMarker);
      this.saveState(retrieveTableMarker);
   }

   public void endRetrieveTableMarker(RetrieveTableMarker retrieveTableMarker) {
      this.endElement(retrieveTableMarker);
   }

   public void restoreState(RetrieveTableMarker retrieveTableMarker) {
      this.restoreRetrieveMarkerState(retrieveTableMarker);
   }

   public void character(Character c) {
      AttributesImpl attributes = this.createLangAttribute(c.getCommonHyphenation());
      this.startElementWithID(c, attributes);
      this.endElement(c);
      this.locales.pop();
   }

   public void characters(FOText foText) {
      this.startElementWithID(foText);
      this.endElement(foText);
   }

   private StructureTreeElement startElement(FONode node) {
      AttributesImpl attributes = new AttributesImpl();
      if (node instanceof Inline) {
         Inline in = (Inline)node;
         if (!in.getAbbreviation().equals("")) {
            this.addAttribute(attributes, "http://xmlgraphics.apache.org/fop/extensions", "abbreviation", "fox", in.getAbbreviation());
         }
      }

      return this.startElement(node, attributes);
   }

   private void startElementWithID(FONode node) {
      this.startElementWithID(node, new AttributesImpl());
   }

   private void startElementWithID(FONode node, AttributesImpl attributes) {
      String localName = node.getLocalName();
      if (node instanceof CommonAccessibilityHolder) {
         this.addRole((CommonAccessibilityHolder)node, attributes);
      }

      node.setStructureTreeElement(this.structureTreeEventHandler.startReferencedNode(localName, attributes, node.getParent().getStructureTreeElement()));
   }

   private void startElementWithIDAndAltText(FObj node, String altText) {
      AttributesImpl attributes = new AttributesImpl();
      String localName = node.getLocalName();
      this.addRole((CommonAccessibilityHolder)node, attributes);
      this.addAttribute(attributes, "http://xmlgraphics.apache.org/fop/extensions", "alt-text", "fox", altText);
      node.setStructureTreeElement(this.structureTreeEventHandler.startImageNode(localName, attributes, node.getParent().getStructureTreeElement()));
   }

   private StructureTreeElement startElement(FONode node, AttributesImpl attributes) {
      String localName = node.getLocalName();
      if (node instanceof CommonAccessibilityHolder) {
         this.addRole((CommonAccessibilityHolder)node, attributes);
      }

      return this.structureTreeEventHandler.startNode(localName, attributes, node.getParent().getStructureTreeElement());
   }

   private void addNoNamespaceAttribute(AttributesImpl attributes, String name, String value) {
      attributes.addAttribute("", name, name, "CDATA", value);
   }

   private void addAttribute(AttributesImpl attributes, String namespace, String localName, String prefix, String value) {
      assert namespace.length() > 0 && prefix.length() > 0;

      String qualifiedName = prefix + ":" + localName;
      attributes.addAttribute(namespace, localName, qualifiedName, "CDATA", value);
   }

   private void addRole(CommonAccessibilityHolder node, AttributesImpl attributes) {
      String role = node.getCommonAccessibility().getRole();
      if (role != null) {
         this.addNoNamespaceAttribute(attributes, "role", role);
      }

   }

   private void endElement(FONode node) {
      String localName = node.getLocalName();
      this.structureTreeEventHandler.endNode(localName);
   }

   private static final class State {
      private final Stack tables;
      private final Stack inTableHeader;
      private final Stack locales;

      State(StructureTreeEventTrigger o) {
         this.tables = (Stack)o.tables.clone();
         this.inTableHeader = (Stack)o.inTableHeader.clone();
         this.locales = (Stack)o.locales.clone();
      }
   }
}
