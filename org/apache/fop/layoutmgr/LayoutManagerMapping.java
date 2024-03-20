package org.apache.fop.layoutmgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.area.AreaTreeHandler;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FOText;
import org.apache.fop.fo.FObjMixed;
import org.apache.fop.fo.extensions.ExternalDocument;
import org.apache.fop.fo.flow.BasicLink;
import org.apache.fop.fo.flow.BidiOverride;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.flow.BlockContainer;
import org.apache.fop.fo.flow.ChangeBarBegin;
import org.apache.fop.fo.flow.ChangeBarEnd;
import org.apache.fop.fo.flow.Character;
import org.apache.fop.fo.flow.ExternalGraphic;
import org.apache.fop.fo.flow.Float;
import org.apache.fop.fo.flow.Footnote;
import org.apache.fop.fo.flow.Inline;
import org.apache.fop.fo.flow.InlineContainer;
import org.apache.fop.fo.flow.InlineLevel;
import org.apache.fop.fo.flow.InstreamForeignObject;
import org.apache.fop.fo.flow.Leader;
import org.apache.fop.fo.flow.ListBlock;
import org.apache.fop.fo.flow.ListItem;
import org.apache.fop.fo.flow.MultiCase;
import org.apache.fop.fo.flow.MultiSwitch;
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
import org.apache.fop.fo.pagination.SideRegion;
import org.apache.fop.fo.pagination.StaticContent;
import org.apache.fop.fo.pagination.Title;
import org.apache.fop.layoutmgr.inline.BasicLinkLayoutManager;
import org.apache.fop.layoutmgr.inline.BidiLayoutManager;
import org.apache.fop.layoutmgr.inline.CharacterLayoutManager;
import org.apache.fop.layoutmgr.inline.ContentLayoutManager;
import org.apache.fop.layoutmgr.inline.ExternalGraphicLayoutManager;
import org.apache.fop.layoutmgr.inline.FloatLayoutManager;
import org.apache.fop.layoutmgr.inline.FootnoteLayoutManager;
import org.apache.fop.layoutmgr.inline.InlineContainerLayoutManager;
import org.apache.fop.layoutmgr.inline.InlineLayoutManager;
import org.apache.fop.layoutmgr.inline.InstreamForeignObjectLM;
import org.apache.fop.layoutmgr.inline.LeaderLayoutManager;
import org.apache.fop.layoutmgr.inline.PageNumberCitationLastLayoutManager;
import org.apache.fop.layoutmgr.inline.PageNumberCitationLayoutManager;
import org.apache.fop.layoutmgr.inline.PageNumberLayoutManager;
import org.apache.fop.layoutmgr.inline.TextLayoutManager;
import org.apache.fop.layoutmgr.inline.WrapperLayoutManager;
import org.apache.fop.layoutmgr.list.ListBlockLayoutManager;
import org.apache.fop.layoutmgr.list.ListItemLayoutManager;
import org.apache.fop.layoutmgr.table.TableLayoutManager;

public class LayoutManagerMapping implements LayoutManagerMaker {
   private static final Log LOG = LogFactory.getLog(LayoutManagerMapping.class);
   private final Map makers = new HashMap();
   private FOUserAgent userAgent;

   public LayoutManagerMapping(FOUserAgent userAgent) {
      this.userAgent = userAgent;
      this.initialize();
   }

   protected void initialize() {
      this.registerMaker(FOText.class, new FOTextLayoutManagerMaker());
      this.registerMaker(FObjMixed.class, new Maker());
      this.registerMaker(BidiOverride.class, new BidiOverrideLayoutManagerMaker());
      this.registerMaker(Inline.class, new InlineLayoutManagerMaker());
      this.registerMaker(Footnote.class, new FootnoteLayoutManagerMaker());
      this.registerMaker(InlineContainer.class, new InlineContainerLayoutManagerMaker());
      this.registerMaker(BasicLink.class, new BasicLinkLayoutManagerMaker());
      this.registerMaker(Block.class, new BlockLayoutManagerMaker());
      this.registerMaker(Leader.class, new LeaderLayoutManagerMaker());
      this.registerMaker(RetrieveMarker.class, new RetrieveMarkerLayoutManagerMaker());
      this.registerMaker(RetrieveTableMarker.class, new RetrieveTableMarkerLayoutManagerMaker());
      this.registerMaker(Character.class, new CharacterLayoutManagerMaker());
      this.registerMaker(ExternalGraphic.class, new ExternalGraphicLayoutManagerMaker());
      this.registerMaker(BlockContainer.class, new BlockContainerLayoutManagerMaker());
      this.registerMaker(ListItem.class, new ListItemLayoutManagerMaker());
      this.registerMaker(ListBlock.class, new ListBlockLayoutManagerMaker());
      this.registerMaker(InstreamForeignObject.class, new InstreamForeignObjectLayoutManagerMaker());
      this.registerMaker(PageNumber.class, new PageNumberLayoutManagerMaker());
      this.registerMaker(PageNumberCitation.class, new PageNumberCitationLayoutManagerMaker());
      this.registerMaker(PageNumberCitationLast.class, new PageNumberCitationLastLayoutManagerMaker());
      this.registerMaker(Table.class, new TableLayoutManagerMaker());
      this.registerMaker(TableBody.class, new Maker());
      this.registerMaker(TableColumn.class, new Maker());
      this.registerMaker(TableRow.class, new Maker());
      this.registerMaker(TableCell.class, new Maker());
      this.registerMaker(TableFooter.class, new Maker());
      this.registerMaker(TableHeader.class, new Maker());
      this.registerMaker(Wrapper.class, new WrapperLayoutManagerMaker());
      this.registerMaker(Title.class, new InlineLayoutManagerMaker());
      this.registerMaker(ChangeBarBegin.class, new Maker());
      this.registerMaker(ChangeBarEnd.class, new Maker());
      this.registerMaker(MultiCase.class, new MultiCaseLayoutManagerMaker());
      this.registerMaker(MultiSwitch.class, new MultiSwitchLayoutManagerMaker());
      this.registerMaker(Float.class, new FloatLayoutManagerMaker());
   }

   protected void registerMaker(Class clazz, Maker maker) {
      this.makers.put(clazz, maker);
   }

   public void makeLayoutManagers(FONode node, List lms) {
      Maker maker = (Maker)this.makers.get(node.getClass());
      if (maker == null) {
         if ("http://www.w3.org/1999/XSL/Format".equals(node.getNamespaceURI())) {
            LOG.error("No LayoutManager maker for class " + node.getClass());
         } else if (LOG.isDebugEnabled()) {
            LOG.debug("Skipping the creation of a layout manager for " + node.getClass());
         }
      } else {
         maker.make(node, lms, this.userAgent);
      }

   }

   public LayoutManager makeLayoutManager(FONode node) {
      List lms = new ArrayList();
      this.makeLayoutManagers(node, lms);
      if (lms.size() == 0) {
         throw new IllegalStateException("LayoutManager for class " + node.getClass() + " is missing.");
      } else if (lms.size() > 1) {
         throw new IllegalStateException("Duplicate LayoutManagers for class " + node.getClass() + " found, only one may be declared.");
      } else {
         return (LayoutManager)lms.get(0);
      }
   }

   public PageSequenceLayoutManager makePageSequenceLayoutManager(AreaTreeHandler ath, PageSequence ps) {
      return new PageSequenceLayoutManager(ath, ps);
   }

   public ExternalDocumentLayoutManager makeExternalDocumentLayoutManager(AreaTreeHandler ath, ExternalDocument ed) {
      return new ExternalDocumentLayoutManager(ath, ed);
   }

   public FlowLayoutManager makeFlowLayoutManager(PageSequenceLayoutManager pslm, Flow flow) {
      return new FlowLayoutManager(pslm, flow);
   }

   public ContentLayoutManager makeContentLayoutManager(PageSequenceLayoutManager pslm, Title title) {
      return new ContentLayoutManager(pslm, title);
   }

   public StaticContentLayoutManager makeStaticContentLayoutManager(PageSequenceLayoutManager pslm, StaticContent sc, SideRegion reg) {
      return new StaticContentLayoutManager(pslm, sc, reg);
   }

   public StaticContentLayoutManager makeStaticContentLayoutManager(PageSequenceLayoutManager pslm, StaticContent sc, org.apache.fop.area.Block block) {
      return new StaticContentLayoutManager(pslm, sc, block);
   }

   public static class FloatLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         lms.add(new FloatLayoutManager((Float)node));
      }
   }

   public class MultiCaseLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         lms.add(new MultiCaseLayoutManager((MultiCase)node));
      }
   }

   public class MultiSwitchLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         lms.add(new MultiSwitchLayoutManager((MultiSwitch)node));
      }
   }

   public class WrapperLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         lms.add(new WrapperLayoutManager((Wrapper)node));
         Iterator baseIter = node.getChildNodes();
         if (baseIter != null) {
            while(baseIter.hasNext()) {
               FONode child = (FONode)baseIter.next();
               LayoutManagerMapping.this.makeLayoutManagers(child, lms);
            }

         }
      }
   }

   public class RetrieveTableMarkerLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         FONode.FONodeIterator baseIter = node.getChildNodes();
         if (baseIter == null) {
            RetrieveTableMarker rtm = (RetrieveTableMarker)node;
            RetrieveTableMarkerLayoutManager rtmlm = new RetrieveTableMarkerLayoutManager(rtm);
            lms.add(rtmlm);
         } else {
            while(baseIter.hasNext()) {
               FONode child = baseIter.next();
               LayoutManagerMapping.this.makeLayoutManagers(child, lms);
            }

         }
      }
   }

   public class RetrieveMarkerLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         Iterator baseIter = node.getChildNodes();
         if (baseIter != null) {
            while(baseIter.hasNext()) {
               FONode child = (FONode)baseIter.next();
               LayoutManagerMapping.this.makeLayoutManagers(child, lms);
            }

         }
      }
   }

   public static class TableLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         Table table = (Table)node;
         TableLayoutManager tlm = new TableLayoutManager(table);
         lms.add(tlm);
      }
   }

   public static class PageNumberCitationLastLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         lms.add(new PageNumberCitationLastLayoutManager((PageNumberCitationLast)node));
      }
   }

   public static class PageNumberCitationLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         lms.add(new PageNumberCitationLayoutManager((PageNumberCitation)node));
      }
   }

   public static class PageNumberLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         lms.add(new PageNumberLayoutManager((PageNumber)node));
      }
   }

   public static class InstreamForeignObjectLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         lms.add(new InstreamForeignObjectLM((InstreamForeignObject)node));
      }
   }

   public static class ListBlockLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         lms.add(new ListBlockLayoutManager((ListBlock)node));
      }
   }

   public static class ListItemLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         lms.add(new ListItemLayoutManager((ListItem)node));
      }
   }

   public static class BlockContainerLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         lms.add(new BlockContainerLayoutManager((BlockContainer)node));
      }
   }

   public static class ExternalGraphicLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         ExternalGraphic eg = (ExternalGraphic)node;
         if (!eg.getSrc().equals("")) {
            lms.add(new ExternalGraphicLayoutManager(eg));
         }

      }
   }

   public static class CharacterLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         Character foCharacter = (Character)node;
         if (foCharacter.getCharacter() != 0) {
            lms.add(new CharacterLayoutManager(foCharacter));
         }

      }
   }

   public static class LeaderLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         lms.add(new LeaderLayoutManager((Leader)node));
      }
   }

   public static class BlockLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         lms.add(new BlockLayoutManager((Block)node));
      }
   }

   public static class BasicLinkLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         lms.add(new BasicLinkLayoutManager((BasicLink)node));
      }
   }

   public static class InlineContainerLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         lms.add(new InlineContainerLayoutManager((InlineContainer)node));
      }
   }

   public static class FootnoteLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         lms.add(new FootnoteLayoutManager((Footnote)node));
      }
   }

   public static class InlineLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         lms.add(new InlineLayoutManager((InlineLevel)node));
      }
   }

   public static class BidiOverrideLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         if (node instanceof BidiOverride) {
            lms.add(new BidiLayoutManager((BidiOverride)node));
         }

      }
   }

   public static class FOTextLayoutManagerMaker extends Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
         FOText foText = (FOText)node;
         if (foText.length() > 0) {
            lms.add(new TextLayoutManager(foText, userAgent));
         }

      }
   }

   public static class Maker {
      public void make(FONode node, List lms, FOUserAgent userAgent) {
      }
   }
}
