package org.apache.fop.complexscripts.bidi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import org.apache.fop.area.Area;
import org.apache.fop.area.LinkResolver;
import org.apache.fop.area.inline.BasicLinkArea;
import org.apache.fop.area.inline.FilledArea;
import org.apache.fop.area.inline.InlineArea;
import org.apache.fop.area.inline.InlineParent;
import org.apache.fop.area.inline.ResolvedPageNumber;
import org.apache.fop.area.inline.SpaceArea;
import org.apache.fop.area.inline.TextArea;
import org.apache.fop.area.inline.UnresolvedPageNumber;

class UnflattenProcessor {
   private List il;
   private List ilNew;
   private int iaLevelLast;
   private TextArea tcOrig;
   private TextArea tcNew;
   private Stack icOrig;
   private Stack icNew;

   UnflattenProcessor(List inlines) {
      this.il = inlines;
      this.ilNew = new ArrayList();
      this.iaLevelLast = -1;
      this.icOrig = new Stack();
      this.icNew = new Stack();
   }

   List unflatten() {
      if (this.il != null) {
         Iterator var1 = this.il.iterator();

         while(var1.hasNext()) {
            InlineArea anIl = (InlineArea)var1.next();
            this.process(anIl);
         }
      }

      this.finishAll();
      return this.ilNew;
   }

   private void process(InlineArea ia) {
      this.process(this.findInlineContainers(ia), this.findTextContainer(ia), ia);
   }

   private void process(List ich, TextArea tc, InlineArea ia) {
      if (this.tcNew == null || tc != this.tcNew) {
         this.maybeFinishTextContainer(tc, ia);
         this.maybeFinishInlineContainers(ich, tc, ia);
         this.update(ich, tc, ia);
      }

   }

   private boolean shouldFinishTextContainer(TextArea tc, InlineArea ia) {
      if (this.tcOrig != null && tc != this.tcOrig) {
         return true;
      } else {
         return this.iaLevelLast != -1 && ia.getBidiLevel() != this.iaLevelLast;
      }
   }

   private void finishTextContainer() {
      this.finishTextContainer((TextArea)null, (InlineArea)null);
   }

   private void finishTextContainer(TextArea tc, InlineArea ia) {
      if (this.tcNew != null) {
         this.updateIPD(this.tcNew);
         if (!this.icNew.empty()) {
            ((InlineParent)this.icNew.peek()).addChildArea(this.tcNew);
         } else {
            this.ilNew.add(this.tcNew);
         }
      }

      this.tcNew = null;
   }

   private void maybeFinishTextContainer(TextArea tc, InlineArea ia) {
      if (this.shouldFinishTextContainer(tc, ia)) {
         this.finishTextContainer(tc, ia);
      }

   }

   private boolean shouldFinishInlineContainer(List ich, TextArea tc, InlineArea ia) {
      if (ich != null && !ich.isEmpty()) {
         if (this.icOrig.empty()) {
            return false;
         } else {
            InlineParent ic = (InlineParent)ich.get(0);
            InlineParent ic0 = (InlineParent)this.icOrig.peek();
            return ic != ic0 && !this.isInlineParentOf(ic, ic0);
         }
      } else {
         return !this.icOrig.empty();
      }
   }

   private void finishInlineContainer() {
      this.finishInlineContainer((List)null, (TextArea)null, (InlineArea)null);
   }

   private void finishInlineContainer(List ich, TextArea tc, InlineArea ia) {
      InlineParent ic;
      if (ich != null && !ich.isEmpty()) {
         Iterator var9 = ich.iterator();

         while(var9.hasNext()) {
            ic = (InlineParent)var9.next();
            InlineParent ic0 = this.icOrig.empty() ? null : (InlineParent)this.icOrig.peek();
            if (ic0 == null) {
               assert this.icNew.empty();
            } else {
               if (ic == ic0) {
                  break;
               }

               assert !this.icNew.empty();

               InlineParent icO0 = (InlineParent)this.icOrig.pop();
               InlineParent icN0 = (InlineParent)this.icNew.pop();

               assert icO0 != null;

               assert icN0 != null;

               if (this.icNew.empty()) {
                  this.ilNew.add(icN0);
               } else {
                  ((InlineParent)this.icNew.peek()).addChildArea(icN0);
               }

               if (!this.icOrig.empty() && this.icOrig.peek() == ic) {
                  break;
               }
            }
         }
      } else {
         while(!this.icNew.empty()) {
            InlineParent icO0 = (InlineParent)this.icOrig.pop();
            ic = (InlineParent)this.icNew.pop();

            assert icO0 != null;

            assert ic != null;

            if (this.icNew.empty()) {
               this.ilNew.add(ic);
            } else {
               ((InlineParent)this.icNew.peek()).addChildArea(ic);
            }
         }
      }

   }

   private void maybeFinishInlineContainers(List ich, TextArea tc, InlineArea ia) {
      if (this.shouldFinishInlineContainer(ich, tc, ia)) {
         this.finishInlineContainer(ich, tc, ia);
      }

   }

   private void finishAll() {
      this.finishTextContainer();
      this.finishInlineContainer();
   }

   private void update(List ich, TextArea tc, InlineArea ia) {
      if (!this.alreadyUnflattened(ia)) {
         if (ich != null && !ich.isEmpty()) {
            this.pushInlineContainers(ich);
         }

         if (tc != null) {
            this.pushTextContainer(tc, ia);
         } else {
            this.pushNonTextInline(ia);
         }

         this.iaLevelLast = ia.getBidiLevel();
         this.tcOrig = tc;
      } else if (this.tcNew != null) {
         this.finishTextContainer();
         this.tcOrig = null;
      } else {
         this.tcOrig = null;
      }

   }

   private boolean alreadyUnflattened(InlineArea ia) {
      Iterator var2 = this.ilNew.iterator();

      InlineArea anIlNew;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         anIlNew = (InlineArea)var2.next();
      } while(!ia.isAncestorOrSelf(anIlNew));

      return true;
   }

   private void pushInlineContainers(List ich) {
      LinkedList icl = new LinkedList();
      Iterator var3 = ich.iterator();

      InlineParent ic;
      while(var3.hasNext()) {
         ic = (InlineParent)var3.next();
         if (this.icOrig.search(ic) >= 0) {
            break;
         }

         icl.addFirst(ic);
      }

      var3 = icl.iterator();

      while(var3.hasNext()) {
         ic = (InlineParent)var3.next();
         this.icOrig.push(ic);
         this.icNew.push(this.generateInlineContainer(ic));
      }

   }

   private void pushTextContainer(TextArea tc, InlineArea ia) {
      if (tc instanceof ResolvedPageNumber) {
         this.tcNew = tc;
      } else if (tc instanceof UnresolvedPageNumber) {
         this.tcNew = tc;
      } else {
         if (this.tcNew == null) {
            this.tcNew = this.generateTextContainer(tc);
         }

         this.tcNew.addChildArea(ia);
      }

   }

   private void pushNonTextInline(InlineArea ia) {
      if (this.icNew.empty()) {
         this.ilNew.add(ia);
      } else {
         ((InlineParent)this.icNew.peek()).addChildArea(ia);
      }

   }

   private InlineParent generateInlineContainer(InlineParent i) {
      if (i instanceof BasicLinkArea) {
         return this.generateBasicLinkArea((BasicLinkArea)i);
      } else {
         return i instanceof FilledArea ? this.generateFilledArea((FilledArea)i) : this.generateInlineContainer0(i);
      }
   }

   private InlineParent generateBasicLinkArea(BasicLinkArea l) {
      BasicLinkArea lc = new BasicLinkArea();
      if (l != null) {
         this.initializeInlineContainer(lc, l);
         this.initializeLinkArea(lc, l);
      }

      return lc;
   }

   private void initializeLinkArea(BasicLinkArea lc, BasicLinkArea l) {
      assert lc != null;

      assert l != null;

      LinkResolver r = l.getResolver();
      if (r != null) {
         String[] idrefs = r.getIDRefs();
         if (idrefs.length > 0) {
            String idref = idrefs[0];
            LinkResolver lr = new LinkResolver(idref, lc);
            lc.setResolver(lr);
            r.addDependent(lr);
         }
      }

   }

   private InlineParent generateFilledArea(FilledArea f) {
      FilledArea fc = new FilledArea();
      if (f != null) {
         this.initializeInlineContainer(fc, f);
         this.initializeFilledArea(fc, f);
      }

      return fc;
   }

   private void initializeFilledArea(FilledArea fc, FilledArea f) {
      assert fc != null;

      assert f != null;

      fc.setIPD(f.getIPD());
      fc.setUnitWidth(f.getUnitWidth());
      fc.setAdjustingInfo(f.getAdjustingInfo());
   }

   private InlineParent generateInlineContainer0(InlineParent i) {
      InlineParent ic = new InlineParent();
      if (i != null) {
         this.initializeInlineContainer(ic, i);
      }

      return ic;
   }

   private void initializeInlineContainer(InlineParent ic, InlineParent i) {
      assert ic != null;

      assert i != null;

      ic.setTraits(i.getTraits());
      ic.setBPD(i.getBPD());
      ic.setBlockProgressionOffset(i.getBlockProgressionOffset());
   }

   private TextArea generateTextContainer(TextArea t) {
      TextArea tc = new TextArea();
      if (t != null) {
         tc.setTraits(t.getTraits());
         tc.setBPD(t.getBPD());
         tc.setBlockProgressionOffset(t.getBlockProgressionOffset());
         tc.setBaselineOffset(t.getBaselineOffset());
         tc.setTextWordSpaceAdjust(t.getTextWordSpaceAdjust());
         tc.setTextLetterSpaceAdjust(t.getTextLetterSpaceAdjust());
      }

      return tc;
   }

   private void updateIPD(TextArea tc) {
      int numAdjustable = 0;
      Iterator var3 = tc.getChildAreas().iterator();

      while(var3.hasNext()) {
         InlineArea ia = (InlineArea)var3.next();
         if (ia instanceof SpaceArea) {
            SpaceArea sa = (SpaceArea)ia;
            if (sa.isAdjustable()) {
               ++numAdjustable;
            }
         }
      }

      if (numAdjustable > 0) {
         tc.setIPD(tc.getIPD() + numAdjustable * tc.getTextWordSpaceAdjust());
      }

   }

   private TextArea findTextContainer(InlineArea ia) {
      assert ia != null;

      TextArea t = null;

      while(t == null) {
         if (ia instanceof TextArea) {
            t = (TextArea)ia;
         } else {
            Area p = ia.getParentArea();
            if (!(p instanceof InlineArea)) {
               break;
            }

            ia = (InlineArea)p;
         }
      }

      return t;
   }

   private List findInlineContainers(InlineArea ia) {
      assert ia != null;

      List ich = new ArrayList();
      Area a = ia.getParentArea();

      while(a != null) {
         if (a instanceof InlineArea) {
            if (a instanceof InlineParent && !(a instanceof TextArea)) {
               ich.add((InlineParent)a);
            }

            a = ((InlineArea)a).getParentArea();
         } else {
            a = null;
         }
      }

      return ich;
   }

   private boolean isInlineParentOf(InlineParent ic0, InlineParent ic1) {
      assert ic0 != null;

      return ic0.getParentArea() == ic1;
   }
}
