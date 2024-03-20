package org.apache.batik.bridge;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.batik.anim.dom.AnimationTarget;
import org.apache.batik.anim.dom.AnimationTargetListener;
import org.apache.batik.anim.dom.SVGAnimationTargetContext;
import org.w3c.dom.Element;

public abstract class AnimatableSVGBridge extends AbstractSVGBridge implements SVGAnimationTargetContext {
   protected Element e;
   protected BridgeContext ctx;
   protected HashMap targetListeners;

   public void addTargetListener(String pn, AnimationTargetListener l) {
      if (this.targetListeners == null) {
         this.targetListeners = new HashMap();
      }

      LinkedList ll = (LinkedList)this.targetListeners.get(pn);
      if (ll == null) {
         ll = new LinkedList();
         this.targetListeners.put(pn, ll);
      }

      ll.add(l);
   }

   public void removeTargetListener(String pn, AnimationTargetListener l) {
      LinkedList ll = (LinkedList)this.targetListeners.get(pn);
      ll.remove(l);
   }

   protected void fireBaseAttributeListeners(String pn) {
      if (this.targetListeners != null) {
         LinkedList ll = (LinkedList)this.targetListeners.get(pn);
         if (ll != null) {
            Iterator var3 = ll.iterator();

            while(var3.hasNext()) {
               Object aLl = var3.next();
               AnimationTargetListener l = (AnimationTargetListener)aLl;
               l.baseValueChanged((AnimationTarget)this.e, (String)null, pn, true);
            }
         }
      }

   }
}
