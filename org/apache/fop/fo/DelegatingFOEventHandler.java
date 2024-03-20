package org.apache.fop.fo;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.fo.extensions.ExternalDocument;
import org.apache.fop.fo.flow.BasicLink;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.Footnote;
import org.apache.fop.fo.flow.FootnoteBody;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Leader;
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
import org.apache.fop.fo.flow.table.TableColumn;
import org.apache.fop.fo.flow.table.TableFooter;
import org.apache.fop.fo.flow.table.TableHeader;
import org.apache.fop.fo.flow.table.TableRow;
import org.apache.fop.fo.pagination.Flow;
import org.apache.fop.fo.pagination.PageSequence;
import org.apache.fop.fo.pagination.Root;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.fonts.FontInfo;
import org.xml.sax.SAXException;

public abstract class DelegatingFOEventHandler extends FOEventHandler {
   private final FOEventHandler delegate;

   public DelegatingFOEventHandler(FOEventHandler delegate) {
      super(delegate.getUserAgent());
      this.delegate = delegate;
   }

   public FOUserAgent getUserAgent() {
      return this.delegate.getUserAgent();
   }

   public FontInfo getFontInfo() {
      return this.delegate.getFontInfo();
   }

   public void startDocument() throws SAXException {
      this.delegate.startDocument();
   }

   public void endDocument() throws SAXException {
      this.delegate.endDocument();
   }

   public void startRoot(Root root) {
      this.delegate.startRoot(root);
   }

   public void endRoot(Root root) {
      this.delegate.endRoot(root);
   }

   public void startPageSequence(PageSequence pageSeq) {
      this.delegate.startPageSequence(pageSeq);
   }

   public void endPageSequence(PageSequence pageSeq) {
      this.delegate.endPageSequence(pageSeq);
   }

   public void startPageNumber(PageNumber pagenum) {
      this.delegate.startPageNumber(pagenum);
   }

   public void endPageNumber(PageNumber pagenum) {
      this.delegate.endPageNumber(pagenum);
   }

   public void startPageNumberCitation(PageNumberCitation pageCite) {
      this.delegate.startPageNumberCitation(pageCite);
   }

   public void endPageNumberCitation(PageNumberCitation pageCite) {
      this.delegate.endPageNumberCitation(pageCite);
   }

   public void startPageNumberCitationLast(PageNumberCitationLast pageLast) {
      this.delegate.startPageNumberCitationLast(pageLast);
   }

   public void endPageNumberCitationLast(PageNumberCitationLast pageLast) {
      this.delegate.endPageNumberCitationLast(pageLast);
   }

   public void startStatic(StaticContent staticContent) {
      this.delegate.startStatic(staticContent);
   }

   public void endStatic(StaticContent statisContent) {
      this.delegate.endStatic(statisContent);
   }

   public void startFlow(Flow fl) {
      this.delegate.startFlow(fl);
   }

   public void endFlow(Flow fl) {
      this.delegate.endFlow(fl);
   }

   public void startBlock(Block bl) {
      this.delegate.startBlock(bl);
   }

   public void endBlock(Block bl) {
      this.delegate.endBlock(bl);
   }

   public void startBlockContainer(BlockContainer blc) {
      this.delegate.startBlockContainer(blc);
   }

   public void endBlockContainer(BlockContainer blc) {
      this.delegate.endBlockContainer(blc);
   }

   public void startInline(Inline inl) {
      this.delegate.startInline(inl);
   }

   public void endInline(Inline inl) {
      this.delegate.endInline(inl);
   }

   public void startTable(Table tbl) {
      this.delegate.startTable(tbl);
   }

   public void endTable(Table tbl) {
      this.delegate.endTable(tbl);
   }

   public void startColumn(TableColumn tc) {
      this.delegate.startColumn(tc);
   }

   public void endColumn(TableColumn tc) {
      this.delegate.endColumn(tc);
   }

   public void startHeader(TableHeader header) {
      this.delegate.startHeader(header);
   }

   public void endHeader(TableHeader header) {
      this.delegate.endHeader(header);
   }

   public void startFooter(TableFooter footer) {
      this.delegate.startFooter(footer);
   }

   public void endFooter(TableFooter footer) {
      this.delegate.endFooter(footer);
   }

   public void startBody(TableBody body) {
      this.delegate.startBody(body);
   }

   public void endBody(TableBody body) {
      this.delegate.endBody(body);
   }

   public void startRow(TableRow tr) {
      this.delegate.startRow(tr);
   }

   public void endRow(TableRow tr) {
      this.delegate.endRow(tr);
   }

   public void startCell(TableCell tc) {
      this.delegate.startCell(tc);
   }

   public void endCell(TableCell tc) {
      this.delegate.endCell(tc);
   }

   public void startList(ListBlock lb) {
      this.delegate.startList(lb);
   }

   public void endList(ListBlock lb) {
      this.delegate.endList(lb);
   }

   public void startListItem(ListItem li) {
      this.delegate.startListItem(li);
   }

   public void endListItem(ListItem li) {
      this.delegate.endListItem(li);
   }

   public void startListLabel(ListItemLabel listItemLabel) {
      this.delegate.startListLabel(listItemLabel);
   }

   public void endListLabel(ListItemLabel listItemLabel) {
      this.delegate.endListLabel(listItemLabel);
   }

   public void startListBody(ListItemBody listItemBody) {
      this.delegate.startListBody(listItemBody);
   }

   public void endListBody(ListItemBody listItemBody) {
      this.delegate.endListBody(listItemBody);
   }

   public void startMarkup() {
      this.delegate.startMarkup();
   }

   public void endMarkup() {
      this.delegate.endMarkup();
   }

   public void startLink(BasicLink basicLink) {
      this.delegate.startLink(basicLink);
   }

   public void endLink(BasicLink basicLink) {
      this.delegate.endLink(basicLink);
   }

   public void image(ExternalGraphic eg) {
      this.delegate.image(eg);
   }

   public void pageRef() {
      this.delegate.pageRef();
   }

   public void startInstreamForeignObject(InstreamForeignObject ifo) {
      this.delegate.startInstreamForeignObject(ifo);
   }

   public void endInstreamForeignObject(InstreamForeignObject ifo) {
      this.delegate.endInstreamForeignObject(ifo);
   }

   public void startFootnote(Footnote footnote) {
      this.delegate.startFootnote(footnote);
   }

   public void endFootnote(Footnote footnote) {
      this.delegate.endFootnote(footnote);
   }

   public void startFootnoteBody(FootnoteBody body) {
      this.delegate.startFootnoteBody(body);
   }

   public void endFootnoteBody(FootnoteBody body) {
      this.delegate.endFootnoteBody(body);
   }

   public void startLeader(Leader l) {
      this.delegate.startLeader(l);
   }

   public void endLeader(Leader l) {
      this.delegate.endLeader(l);
   }

   public void startWrapper(Wrapper wrapper) {
      this.delegate.startWrapper(wrapper);
   }

   public void endWrapper(Wrapper wrapper) {
      this.delegate.endWrapper(wrapper);
   }

   public void startRetrieveMarker(RetrieveMarker retrieveMarker) {
      this.delegate.startRetrieveMarker(retrieveMarker);
   }

   public void endRetrieveMarker(RetrieveMarker retrieveMarker) {
      this.delegate.endRetrieveMarker(retrieveMarker);
   }

   public void restoreState(RetrieveMarker retrieveMarker) {
      this.delegate.restoreState(retrieveMarker);
   }

   public void startRetrieveTableMarker(RetrieveTableMarker retrieveTableMarker) {
      this.delegate.startRetrieveTableMarker(retrieveTableMarker);
   }

   public void endRetrieveTableMarker(RetrieveTableMarker retrieveTableMarker) {
      this.delegate.endRetrieveTableMarker(retrieveTableMarker);
   }

   public void restoreState(RetrieveTableMarker retrieveTableMarker) {
      this.delegate.restoreState(retrieveTableMarker);
   }

   public void character(Character c) {
      this.delegate.character(c);
   }

   public void characters(FOText foText) {
      this.delegate.characters(foText);
   }

   public void startExternalDocument(ExternalDocument document) {
      this.delegate.startExternalDocument(document);
   }

   public void endExternalDocument(ExternalDocument document) {
      this.delegate.endExternalDocument(document);
   }

   public FormattingResults getResults() {
      return this.delegate.getResults();
   }
}
