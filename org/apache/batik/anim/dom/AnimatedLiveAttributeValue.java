package org.apache.batik.anim.dom;

import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.dom.svg.LiveAttributeValue;

public interface AnimatedLiveAttributeValue extends LiveAttributeValue {
   String getNamespaceURI();

   String getLocalName();

   AnimatableValue getUnderlyingValue(AnimationTarget var1);

   void addAnimatedAttributeListener(AnimatedAttributeListener var1);

   void removeAnimatedAttributeListener(AnimatedAttributeListener var1);
}
