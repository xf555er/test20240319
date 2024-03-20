package org.apache.fop.render.rtf.rtflib.rtfdoc;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RtfTextrun extends RtfContainer {
   public static final int BREAK_NONE = 0;
   public static final int BREAK_PAGE = 1;
   public static final int BREAK_COLUMN = 2;
   public static final int BREAK_EVEN_PAGE = 3;
   public static final int BREAK_ODD_PAGE = 4;
   private boolean bSuppressLastPar;
   private RtfListItem rtfListItem;
   protected static final Log log = LogFactory.getLog(RtfTextrun.class);
   private RtfSpaceManager rtfSpaceManager = new RtfSpaceManager();

   RtfTextrun(RtfContainer parent, Writer w, RtfAttributes attrs) throws IOException {
      super(parent, w, attrs);
   }

   private void addOpenGroupMark(RtfAttributes attrs) throws IOException {
      new RtfOpenGroupMark(this, this.writer, attrs);
   }

   private void addCloseGroupMark(int breakType) throws IOException {
      new RtfCloseGroupMark(this, this.writer, breakType);
   }

   private void addCloseGroupMark() throws IOException {
      new RtfCloseGroupMark(this, this.writer, 0);
   }

   public void pushBlockAttributes(RtfAttributes attrs) throws IOException {
      this.rtfSpaceManager.stopUpdatingSpaceBefore();
      RtfSpaceSplitter splitter = this.rtfSpaceManager.pushRtfSpaceSplitter(attrs);
      this.addOpenGroupMark(splitter.getCommonAttributes());
   }

   public void popBlockAttributes(int breakType) throws IOException {
      this.rtfSpaceManager.popRtfSpaceSplitter();
      this.rtfSpaceManager.stopUpdatingSpaceBefore();
      this.addCloseGroupMark(breakType);
   }

   public void pushInlineAttributes(RtfAttributes attrs) throws IOException {
      this.rtfSpaceManager.pushInlineAttributes(attrs);
      this.addOpenGroupMark(attrs);
   }

   public void addPageNumberCitation(String refId) throws IOException {
      new RtfPageNumberCitation(this, this.writer, refId);
   }

   public void popInlineAttributes() throws IOException {
      this.rtfSpaceManager.popInlineAttributes();
      this.addCloseGroupMark();
   }

   public void addString(String s) throws IOException {
      if (!s.equals("")) {
         RtfAttributes attrs = this.rtfSpaceManager.getLastInlineAttribute();
         this.rtfSpaceManager.pushRtfSpaceSplitter(attrs);
         this.rtfSpaceManager.setCandidate(attrs);
         new RtfString(this, this.writer, s);
         this.rtfSpaceManager.popRtfSpaceSplitter();
      }
   }

   public RtfFootnote addFootnote() throws IOException {
      return new RtfFootnote(this, this.writer);
   }

   public RtfParagraphBreak addParagraphBreak() throws IOException {
      List children = this.getChildren();
      Stack tmp = new Stack();
      RtfParagraphBreak par = null;
      int deletedCloseGroupCount = 0;

      for(ListIterator lit = children.listIterator(children.size()); lit.hasPrevious() && lit.previous() instanceof RtfCloseGroupMark; ++deletedCloseGroupCount) {
         tmp.push(((RtfCloseGroupMark)lit.next()).getBreakType());
         lit.remove();
      }

      if (children.size() != 0) {
         this.setChildren(children);
         par = new RtfParagraphBreak(this, this.writer);

         for(int i = 0; i < deletedCloseGroupCount; ++i) {
            this.addCloseGroupMark((Integer)tmp.pop());
         }
      }

      return par;
   }

   public void addLeader(RtfAttributes attrs) throws IOException {
      new RtfLeader(this, this.writer, attrs);
   }

   public void addPageNumber(RtfAttributes attr) throws IOException {
      new RtfPageNumber(this, this.writer, attr);
   }

   public RtfHyperLink addHyperlink(RtfAttributes attr) throws IOException {
      return new RtfHyperLink(this, this.writer, attr);
   }

   public void addBookmark(String id) throws IOException {
      if (id.length() > 0) {
         new RtfBookmark(this, this.writer, id);
      }

   }

   public RtfExternalGraphic newImage() throws IOException {
      return new RtfExternalGraphic(this, this.writer);
   }

   public static RtfTextrun getTextrun(RtfContainer container, Writer writer, RtfAttributes attrs) throws IOException {
      List list = container.getChildren();
      if (list.size() == 0) {
         RtfTextrun textrun = new RtfTextrun(container, writer, attrs);
         list.add(textrun);
         return textrun;
      } else {
         Object obj = list.get(list.size() - 1);
         if (obj instanceof RtfTextrun) {
            return (RtfTextrun)obj;
         } else {
            RtfTextrun textrun = new RtfTextrun(container, writer, attrs);
            list.add(textrun);
            return textrun;
         }
      }
   }

   public void setSuppressLastPar(boolean bSuppress) {
      this.bSuppressLastPar = bSuppress;
   }

   protected void writeRtfContent() throws IOException {
      boolean bHasTableCellParent = this.getParentOfClass(RtfTableCell.class) != null;
      RtfAttributes attrBlockLevel = new RtfAttributes();
      boolean bLast = false;
      Iterator it = this.parent.getChildren().iterator();

      while(it.hasNext()) {
         if (it.next() == this) {
            bLast = !it.hasNext();
            break;
         }
      }

      RtfParagraphBreak lastParagraphBreak = null;
      if (bLast) {
         RtfElement aBefore = null;

         RtfElement e;
         for(Iterator var6 = this.getChildren().iterator(); var6.hasNext(); aBefore = e) {
            Object o = var6.next();
            e = (RtfElement)o;
            if (e instanceof RtfParagraphBreak) {
               if (!(aBefore instanceof RtfParagraphBreak) && !(aBefore instanceof RtfBookmark)) {
                  lastParagraphBreak = (RtfParagraphBreak)e;
               }
            } else if (!(e instanceof RtfOpenGroupMark) && !(e instanceof RtfCloseGroupMark) && e.isEmpty()) {
               lastParagraphBreak = null;
            }
         }
      }

      this.writeAttributes(this.attrib, (String[])null);
      if (this.rtfListItem != null) {
         this.rtfListItem.getRtfListStyle().writeParagraphPrefix(this);
      }

      boolean bPrevPar = false;
      boolean bBookmark = false;
      boolean bFirst = true;
      Iterator var17 = this.getChildren().iterator();

      while(true) {
         while(var17.hasNext()) {
            Object o = var17.next();
            RtfElement e = (RtfElement)o;
            boolean bRtfParagraphBreak = e instanceof RtfParagraphBreak;
            if (bHasTableCellParent) {
               attrBlockLevel.set(e.getRtfAttributes());
            }

            boolean bHide = false;
            bHide = bRtfParagraphBreak && (bPrevPar || bFirst || this.bSuppressLastPar && bLast && lastParagraphBreak != null && e == lastParagraphBreak || bBookmark) && ((RtfParagraphBreak)e).canHide();
            if (!bHide) {
               this.newLine();
               e.writeRtf();
               if (this.rtfListItem != null && e instanceof RtfParagraphBreak) {
                  this.rtfListItem.getRtfListStyle().writeParagraphPrefix(this);
               }
            }

            if (e instanceof RtfParagraphBreak) {
               bPrevPar = true;
            } else if (e instanceof RtfBookmark) {
               bBookmark = true;
            } else if (!(e instanceof RtfCloseGroupMark) && !(e instanceof RtfOpenGroupMark)) {
               bPrevPar = bPrevPar && e.isEmpty();
               bFirst = bFirst && e.isEmpty();
               bBookmark = false;
            }
         }

         if (bHasTableCellParent) {
            this.writeAttributes(attrBlockLevel, (String[])null);
         }

         return;
      }
   }

   public void setRtfListItem(RtfListItem listItem) {
      this.rtfListItem = listItem;
   }

   public RtfListItem getRtfListItem() {
      return this.rtfListItem;
   }

   private class RtfCloseGroupMark extends RtfElement {
      private int breakType = 0;

      RtfCloseGroupMark(RtfContainer parent, Writer w, int breakType) throws IOException {
         super(parent, w);
         this.breakType = breakType;
      }

      public boolean isEmpty() {
         return false;
      }

      public int getBreakType() {
         return this.breakType;
      }

      protected void writeRtfContent() throws IOException {
         this.writeGroupMark(false);
         boolean bHasTableCellParent = this.getParentOfClass(RtfTableCell.class) != null;
         if (this.breakType != 0) {
            if (!bHasTableCellParent) {
               this.writeControlWord("sect");
               switch (this.breakType) {
                  case 2:
                     this.writeControlWord("sbkcol");
                     break;
                  case 3:
                     this.writeControlWord("sbkeven");
                     break;
                  case 4:
                     this.writeControlWord("sbkodd");
                     break;
                  default:
                     this.writeControlWord("sbkpage");
               }
            } else {
               RtfTextrun.log.warn("Cannot create break-after for a paragraph inside a table.");
            }
         }

      }
   }

   private class RtfOpenGroupMark extends RtfElement {
      RtfOpenGroupMark(RtfContainer parent, Writer w, RtfAttributes attr) throws IOException {
         super(parent, w, attr);
      }

      public boolean isEmpty() {
         return false;
      }

      protected void writeRtfContent() throws IOException {
         this.writeGroupMark(true);
         this.writeAttributes(this.getRtfAttributes(), (String[])null);
      }
   }
}
