package org.apache.fop.accessibility.fo;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.fo.DelegatingFOEventHandler;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.extensions.ExternalDocument;
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
import org.apache.fop.fo.properties.CommonAccessibility;
import org.apache.fop.fo.properties.CommonAccessibilityHolder;
import org.xml.sax.SAXException;

public class FO2StructureTreeConverter extends DelegatingFOEventHandler {
   protected FOEventHandler converter;
   private Stack converters = new Stack();
   private final StructureTreeEventTrigger structureTreeEventTrigger;
   private final FOEventHandler eventSwallower = new FOEventHandler() {
   };
   private final Map states = new HashMap();
   private Event root = new Event((Event)null);
   private Event currentNode;

   private void startContent(Event event, boolean hasContent) {
      if (this.getUserAgent().isKeepEmptyTags()) {
         event.run();
      } else {
         Event node = new Event(this.currentNode);
         event.hasContent = hasContent;
         node.add(event);
         this.currentNode.add(node);
         this.currentNode = node;
      }

   }

   private void content(Event event, boolean hasContent) {
      if (this.getUserAgent().isKeepEmptyTags()) {
         event.run();
      } else {
         this.currentNode.add(event);
         event.hasContent = hasContent;
      }

   }

   private void endContent(Event event) {
      if (this.getUserAgent().isKeepEmptyTags()) {
         event.run();
      } else {
         this.currentNode.add(event);
         this.currentNode = this.currentNode.parent;
         if (this.currentNode == this.root) {
            this.root.run();
         }
      }

   }

   public FO2StructureTreeConverter(StructureTreeEventHandler structureTreeEventHandler, FOEventHandler delegate) {
      super(delegate);
      this.currentNode = this.root;
      this.structureTreeEventTrigger = new StructureTreeEventTrigger(structureTreeEventHandler);
      this.converter = this.structureTreeEventTrigger;
   }

   public void startDocument() throws SAXException {
      this.converter.startDocument();
      super.startDocument();
   }

   public void endDocument() throws SAXException {
      this.converter.endDocument();
      super.endDocument();
   }

   public void startRoot(Root root) {
      this.converter.startRoot(root);
      super.startRoot(root);
   }

   public void endRoot(Root root) {
      this.converter.endRoot(root);
      super.endRoot(root);
   }

   public void startPageSequence(PageSequence pageSeq) {
      this.converter.startPageSequence(pageSeq);
      super.startPageSequence(pageSeq);
   }

   public void endPageSequence(PageSequence pageSeq) {
      this.converter.endPageSequence(pageSeq);
      super.endPageSequence(pageSeq);
   }

   public void startPageNumber(final PageNumber pagenum) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startPageNumber(pagenum);
         }
      }, true);
      super.startPageNumber(pagenum);
   }

   public void endPageNumber(final PageNumber pagenum) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endPageNumber(pagenum);
         }
      });
      super.endPageNumber(pagenum);
   }

   public void startPageNumberCitation(final PageNumberCitation pageCite) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startPageNumberCitation(pageCite);
         }
      }, true);
      super.startPageNumberCitation(pageCite);
   }

   public void endPageNumberCitation(final PageNumberCitation pageCite) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endPageNumberCitation(pageCite);
         }
      });
      super.endPageNumberCitation(pageCite);
   }

   public void startPageNumberCitationLast(final PageNumberCitationLast pageLast) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startPageNumberCitationLast(pageLast);
         }
      }, true);
      super.startPageNumberCitationLast(pageLast);
   }

   public void endPageNumberCitationLast(final PageNumberCitationLast pageLast) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endPageNumberCitationLast(pageLast);
         }
      });
      super.endPageNumberCitationLast(pageLast);
   }

   public void startStatic(final StaticContent staticContent) {
      this.handleStartArtifact(staticContent);
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startStatic(staticContent);
         }
      }, true);
      super.startStatic(staticContent);
   }

   public void endStatic(final StaticContent staticContent) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endStatic(staticContent);
         }
      });
      this.handleEndArtifact(staticContent);
      super.endStatic(staticContent);
   }

   public void startFlow(Flow fl) {
      this.converter.startFlow(fl);
      super.startFlow(fl);
   }

   public void endFlow(Flow fl) {
      this.converter.endFlow(fl);
      super.endFlow(fl);
   }

   public void startBlock(final Block bl) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startBlock(bl);
         }
      }, false);
      super.startBlock(bl);
   }

   public void endBlock(final Block bl) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endBlock(bl);
         }
      });
      super.endBlock(bl);
   }

   public void startBlockContainer(final BlockContainer blc) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startBlockContainer(blc);
         }
      }, false);
      super.startBlockContainer(blc);
   }

   public void endBlockContainer(final BlockContainer blc) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endBlockContainer(blc);
         }
      });
      super.endBlockContainer(blc);
   }

   public void startInline(final Inline inl) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startInline(inl);
         }
      }, true);
      super.startInline(inl);
   }

   public void endInline(final Inline inl) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endInline(inl);
         }
      });
      super.endInline(inl);
   }

   public void startTable(final Table tbl) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startTable(tbl);
         }
      }, true);
      super.startTable(tbl);
   }

   public void endTable(final Table tbl) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endTable(tbl);
         }
      });
      super.endTable(tbl);
   }

   public void startColumn(final TableColumn tc) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startColumn(tc);
         }
      }, true);
      super.startColumn(tc);
   }

   public void endColumn(final TableColumn tc) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endColumn(tc);
         }
      });
      super.endColumn(tc);
   }

   public void startHeader(final TableHeader header) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startHeader(header);
         }
      }, true);
      super.startHeader(header);
   }

   public void endHeader(final TableHeader header) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endHeader(header);
         }
      });
      super.endHeader(header);
   }

   public void startFooter(final TableFooter footer) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startFooter(footer);
         }
      }, true);
      super.startFooter(footer);
   }

   public void endFooter(final TableFooter footer) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endFooter(footer);
         }
      });
      super.endFooter(footer);
   }

   public void startBody(final TableBody body) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startBody(body);
         }
      }, true);
      super.startBody(body);
   }

   public void endBody(final TableBody body) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endBody(body);
         }
      });
      super.endBody(body);
   }

   public void startRow(final TableRow tr) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startRow(tr);
         }
      }, true);
      super.startRow(tr);
   }

   public void endRow(final TableRow tr) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endRow(tr);
         }
      });
      super.endRow(tr);
   }

   public void startCell(final TableCell tc) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startCell(tc);
         }
      }, true);
      super.startCell(tc);
   }

   public void endCell(final TableCell tc) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endCell(tc);
         }
      });
      super.endCell(tc);
   }

   public void startList(final ListBlock lb) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startList(lb);
         }
      }, true);
      super.startList(lb);
   }

   public void endList(final ListBlock lb) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endList(lb);
         }
      });
      super.endList(lb);
   }

   public void startListItem(final ListItem li) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startListItem(li);
         }
      }, true);
      super.startListItem(li);
   }

   public void endListItem(final ListItem li) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endListItem(li);
         }
      });
      super.endListItem(li);
   }

   public void startListLabel(final ListItemLabel listItemLabel) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startListLabel(listItemLabel);
         }
      }, true);
      super.startListLabel(listItemLabel);
   }

   public void endListLabel(final ListItemLabel listItemLabel) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endListLabel(listItemLabel);
         }
      });
      super.endListLabel(listItemLabel);
   }

   public void startListBody(final ListItemBody listItemBody) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startListBody(listItemBody);
         }
      }, true);
      super.startListBody(listItemBody);
   }

   public void endListBody(final ListItemBody listItemBody) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endListBody(listItemBody);
         }
      });
      super.endListBody(listItemBody);
   }

   public void startMarkup() {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startMarkup();
         }
      }, true);
      super.startMarkup();
   }

   public void endMarkup() {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endMarkup();
         }
      });
      super.endMarkup();
   }

   public void startLink(final BasicLink basicLink) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startLink(basicLink);
         }
      }, true);
      super.startLink(basicLink);
   }

   public void endLink(final BasicLink basicLink) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endLink(basicLink);
         }
      });
      super.endLink(basicLink);
   }

   public void image(final ExternalGraphic eg) {
      this.content(new Event(this) {
         public void run() {
            this.eventHandler.image(eg);
         }
      }, true);
      super.image(eg);
   }

   public void pageRef() {
      this.content(new Event(this) {
         public void run() {
            this.eventHandler.pageRef();
         }
      }, true);
      super.pageRef();
   }

   public void startInstreamForeignObject(final InstreamForeignObject ifo) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startInstreamForeignObject(ifo);
         }
      }, true);
      super.startInstreamForeignObject(ifo);
   }

   public void endInstreamForeignObject(final InstreamForeignObject ifo) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endInstreamForeignObject(ifo);
         }
      });
      super.endInstreamForeignObject(ifo);
   }

   public void startFootnote(final Footnote footnote) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startFootnote(footnote);
         }
      }, true);
      super.startFootnote(footnote);
   }

   public void endFootnote(final Footnote footnote) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endFootnote(footnote);
         }
      });
      super.endFootnote(footnote);
   }

   public void startFootnoteBody(final FootnoteBody body) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startFootnoteBody(body);
         }
      }, true);
      super.startFootnoteBody(body);
   }

   public void endFootnoteBody(final FootnoteBody body) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endFootnoteBody(body);
         }
      });
      super.endFootnoteBody(body);
   }

   public void startLeader(final Leader l) {
      this.converters.push(this.converter);
      this.converter = this.eventSwallower;
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startLeader(l);
         }
      }, false);
      super.startLeader(l);
   }

   public void endLeader(final Leader l) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endLeader(l);
         }
      });
      this.converter = (FOEventHandler)this.converters.pop();
      super.endLeader(l);
   }

   public void startWrapper(final Wrapper wrapper) {
      this.handleStartArtifact(wrapper);
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startWrapper(wrapper);
         }
      }, true);
      super.startWrapper(wrapper);
   }

   public void endWrapper(final Wrapper wrapper) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endWrapper(wrapper);
         }
      });
      this.handleEndArtifact(wrapper);
      super.endWrapper(wrapper);
   }

   public void startRetrieveMarker(final RetrieveMarker retrieveMarker) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startRetrieveMarker(retrieveMarker);
         }
      }, true);
      this.saveState(retrieveMarker);
      super.startRetrieveMarker(retrieveMarker);
   }

   private void saveState(AbstractRetrieveMarker retrieveMarker) {
      this.states.put(retrieveMarker, new State(this));
   }

   public void endRetrieveMarker(final RetrieveMarker retrieveMarker) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endRetrieveMarker(retrieveMarker);
         }
      });
      super.endRetrieveMarker(retrieveMarker);
   }

   public void restoreState(final RetrieveMarker retrieveMarker) {
      this.restoreRetrieveMarkerState(retrieveMarker);
      this.content(new Event(this) {
         public void run() {
            this.eventHandler.restoreState(retrieveMarker);
         }
      }, true);
      super.restoreState(retrieveMarker);
   }

   private void restoreRetrieveMarkerState(AbstractRetrieveMarker retrieveMarker) {
      State state = (State)this.states.get(retrieveMarker);
      this.converter = state.converter;
      this.converters = (Stack)state.converters.clone();
   }

   public void startRetrieveTableMarker(final RetrieveTableMarker retrieveTableMarker) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startRetrieveTableMarker(retrieveTableMarker);
         }
      }, true);
      this.saveState(retrieveTableMarker);
      super.startRetrieveTableMarker(retrieveTableMarker);
   }

   public void endRetrieveTableMarker(final RetrieveTableMarker retrieveTableMarker) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endRetrieveTableMarker(retrieveTableMarker);
         }
      });
      super.endRetrieveTableMarker(retrieveTableMarker);
   }

   public void restoreState(final RetrieveTableMarker retrieveTableMarker) {
      this.restoreRetrieveMarkerState(retrieveTableMarker);
      this.currentNode.add(new Event(this) {
         public void run() {
            this.eventHandler.restoreState(retrieveTableMarker);
         }
      });
      super.restoreState(retrieveTableMarker);
   }

   public void character(final Character c) {
      this.content(new Event(this) {
         public void run() {
            this.eventHandler.character(c);
         }
      }, true);
      super.character(c);
   }

   public void characters(final FOText foText) {
      this.content(new Event(this) {
         public void run() {
            this.eventHandler.characters(foText);
         }
      }, foText.length() > 0);
      super.characters(foText);
   }

   public void startExternalDocument(final ExternalDocument document) {
      this.startContent(new Event(this) {
         public void run() {
            this.eventHandler.startExternalDocument(document);
         }
      }, true);
      super.startExternalDocument(document);
   }

   public void endExternalDocument(final ExternalDocument document) {
      this.endContent(new Event(this) {
         public void run() {
            this.eventHandler.endExternalDocument(document);
         }
      });
      super.endExternalDocument(document);
   }

   private void handleStartArtifact(CommonAccessibilityHolder fobj) {
      if (this.isArtifact(fobj)) {
         this.converters.push(this.converter);
         this.converter = this.eventSwallower;
      }

   }

   private void handleEndArtifact(CommonAccessibilityHolder fobj) {
      if (this.isArtifact(fobj)) {
         this.converter = (FOEventHandler)this.converters.pop();
      }

   }

   private boolean isArtifact(CommonAccessibilityHolder fobj) {
      CommonAccessibility accessibility = fobj.getCommonAccessibility();
      return "artifact".equalsIgnoreCase(accessibility.getRole());
   }

   private static final class State {
      private final FOEventHandler converter;
      private final Stack converters;

      State(FO2StructureTreeConverter o) {
         this.converter = o.converter;
         this.converters = (Stack)o.converters.clone();
      }
   }
}
