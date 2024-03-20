package org.apache.batik.anim.dom;

import org.apache.batik.dom.svg.SVGContext;

public interface SVGAnimationTargetContext extends SVGContext {
   void addTargetListener(String var1, AnimationTargetListener var2);

   void removeTargetListener(String var1, AnimationTargetListener var2);
}
