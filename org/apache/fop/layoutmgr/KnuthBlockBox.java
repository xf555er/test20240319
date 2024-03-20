package org.apache.fop.layoutmgr;

import java.util.LinkedList;
import java.util.List;
import org.apache.fop.traits.MinOptMax;

public class KnuthBlockBox extends KnuthBox {
   private MinOptMax ipdRange;
   private int bpd;
   private List footnoteList;
   private List floatContentLMs;
   private List elementLists;

   public KnuthBlockBox(int width, MinOptMax range, int bpdim, Position pos, boolean auxiliary) {
      super(width, pos, auxiliary);
      this.ipdRange = range;
      this.bpd = bpdim;
      this.footnoteList = new LinkedList();
      this.floatContentLMs = new LinkedList();
   }

   public KnuthBlockBox(int width, List list, Position pos, boolean auxiliary) {
      super(width, pos, auxiliary);
      this.ipdRange = MinOptMax.ZERO;
      this.bpd = 0;
      this.footnoteList = new LinkedList(list);
      this.floatContentLMs = new LinkedList();
   }

   public KnuthBlockBox(int width, List list, Position pos, boolean auxiliary, List fclms) {
      super(width, pos, auxiliary);
      this.ipdRange = MinOptMax.ZERO;
      this.bpd = 0;
      this.footnoteList = new LinkedList(list);
      this.floatContentLMs = new LinkedList(fclms);
   }

   public List getFootnoteBodyLMs() {
      return this.footnoteList;
   }

   public boolean hasAnchors() {
      return this.footnoteList.size() > 0;
   }

   public void addElementList(List list) {
      if (this.elementLists == null) {
         this.elementLists = new LinkedList();
      }

      this.elementLists.add(list);
   }

   public List getElementLists() {
      return this.elementLists;
   }

   public MinOptMax getIPDRange() {
      return this.ipdRange;
   }

   public int getBPD() {
      return this.bpd;
   }

   public List getFloatContentLMs() {
      return this.floatContentLMs;
   }

   public boolean hasFloatAnchors() {
      return this.floatContentLMs.size() > 0;
   }
}
