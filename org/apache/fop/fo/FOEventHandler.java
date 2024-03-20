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
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.fonts.FontInfo;
import org.xml.sax.SAXException;

public abstract class FOEventHandler {
   protected FOUserAgent foUserAgent;
   protected FontInfo fontInfo;

   public FOEventHandler(FOUserAgent foUserAgent) {
      this.foUserAgent = foUserAgent;
      this.fontInfo = new FontInfo();
      this.fontInfo.setEventListener(new FontEventAdapter(foUserAgent.getEventBroadcaster()));
   }

   protected FOEventHandler() {
   }

   public FOUserAgent getUserAgent() {
      return this.foUserAgent;
   }

   public FontInfo getFontInfo() {
      return this.fontInfo;
   }

   public void startDocument() throws SAXException {
   }

   public void endDocument() throws SAXException {
   }

   public void startRoot(Root root) {
   }

   public void endRoot(Root root) {
   }

   public void startPageSequence(PageSequence pageSeq) {
   }

   public void endPageSequence(PageSequence pageSeq) {
   }

   public void startPageNumber(PageNumber pagenum) {
   }

   public void endPageNumber(PageNumber pagenum) {
   }

   public void startPageNumberCitation(PageNumberCitation pageCite) {
   }

   public void endPageNumberCitation(PageNumberCitation pageCite) {
   }

   public void startPageNumberCitationLast(PageNumberCitationLast pageLast) {
   }

   public void endPageNumberCitationLast(PageNumberCitationLast pageLast) {
   }

   public void startStatic(StaticContent staticContent) {
   }

   public void endStatic(StaticContent staticContent) {
   }

   public void startFlow(Flow fl) {
   }

   public void endFlow(Flow fl) {
   }

   public void startBlock(Block bl) {
   }

   public void endBlock(Block bl) {
   }

   public void startBlockContainer(BlockContainer blc) {
   }

   public void endBlockContainer(BlockContainer blc) {
   }

   public void startInline(Inline inl) {
   }

   public void endInline(Inline inl) {
   }

   public void startTable(Table tbl) {
   }

   public void endTable(Table tbl) {
   }

   public void startColumn(TableColumn tc) {
   }

   public void endColumn(TableColumn tc) {
   }

   public void startHeader(TableHeader header) {
   }

   public void endHeader(TableHeader header) {
   }

   public void startFooter(TableFooter footer) {
   }

   public void endFooter(TableFooter footer) {
   }

   public void startBody(TableBody body) {
   }

   public void endBody(TableBody body) {
   }

   public void startRow(TableRow tr) {
   }

   public void endRow(TableRow tr) {
   }

   public void startCell(TableCell tc) {
   }

   public void endCell(TableCell tc) {
   }

   public void startList(ListBlock lb) {
   }

   public void endList(ListBlock lb) {
   }

   public void startListItem(ListItem li) {
   }

   public void endListItem(ListItem li) {
   }

   public void startListLabel(ListItemLabel listItemLabel) {
   }

   public void endListLabel(ListItemLabel listItemLabel) {
   }

   public void startListBody(ListItemBody listItemBody) {
   }

   public void endListBody(ListItemBody listItemBody) {
   }

   public void startMarkup() {
   }

   public void endMarkup() {
   }

   public void startLink(BasicLink basicLink) {
   }

   public void endLink(BasicLink basicLink) {
   }

   public void image(ExternalGraphic eg) {
   }

   public void pageRef() {
   }

   public void startInstreamForeignObject(InstreamForeignObject ifo) {
   }

   public void endInstreamForeignObject(InstreamForeignObject ifo) {
   }

   public void startFootnote(Footnote footnote) {
   }

   public void endFootnote(Footnote footnote) {
   }

   public void startFootnoteBody(FootnoteBody body) {
   }

   public void endFootnoteBody(FootnoteBody body) {
   }

   public void startLeader(Leader l) {
   }

   public void endLeader(Leader l) {
   }

   public void startWrapper(Wrapper wrapper) {
   }

   public void endWrapper(Wrapper wrapper) {
   }

   public void startRetrieveMarker(RetrieveMarker retrieveMarker) {
   }

   public void endRetrieveMarker(RetrieveMarker retrieveMarker) {
   }

   public void restoreState(RetrieveMarker retrieveMarker) {
   }

   public void startRetrieveTableMarker(RetrieveTableMarker retrieveTableMarker) {
   }

   public void endRetrieveTableMarker(RetrieveTableMarker retrieveTableMarker) {
   }

   public void restoreState(RetrieveTableMarker retrieveTableMarker) {
   }

   public void character(Character c) {
   }

   public void characters(FOText foText) {
   }

   public void startExternalDocument(ExternalDocument document) {
   }

   public void endExternalDocument(ExternalDocument document) {
   }

   public FormattingResults getResults() {
      return null;
   }
}
