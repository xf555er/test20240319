package org.apache.batik.anim.dom;

import org.w3c.dom.Element;

public interface AnimatedAttributeListener {
   void animatedAttributeChanged(Element var1, AnimatedLiveAttributeValue var2);

   void otherAnimationChanged(Element var1, String var2);
}
