package org.apache.batik.anim;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.batik.anim.dom.AnimationTarget;
import org.apache.batik.anim.dom.AnimationTargetListener;
import org.apache.batik.anim.timing.TimedDocumentRoot;
import org.apache.batik.anim.timing.TimedElement;
import org.apache.batik.anim.timing.TimegraphListener;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.util.DoublyIndexedTable;
import org.w3c.dom.Document;

public abstract class AnimationEngine {
   public static final short ANIM_TYPE_XML = 0;
   public static final short ANIM_TYPE_CSS = 1;
   public static final short ANIM_TYPE_OTHER = 2;
   protected Document document;
   protected TimedDocumentRoot timedDocumentRoot;
   protected long pauseTime;
   protected HashMap targets = new HashMap();
   protected HashMap animations = new HashMap();
   protected Listener targetListener = new Listener();
   protected static final Map.Entry[] MAP_ENTRY_ARRAY = new Map.Entry[0];

   public AnimationEngine(Document doc) {
      this.document = doc;
      this.timedDocumentRoot = this.createDocumentRoot();
   }

   public void dispose() {
      Iterator var1 = this.targets.entrySet().iterator();

      while(var1.hasNext()) {
         Object o = var1.next();
         Map.Entry e = (Map.Entry)o;
         AnimationTarget target = (AnimationTarget)e.getKey();
         TargetInfo info = (TargetInfo)e.getValue();
         Iterator j = info.xmlAnimations.iterator();

         String propertyName;
         while(j.hasNext()) {
            DoublyIndexedTable.Entry e2 = (DoublyIndexedTable.Entry)j.next();
            propertyName = (String)e2.getKey1();
            String localName = (String)e2.getKey2();
            Sandwich sandwich = (Sandwich)e2.getValue();
            if (sandwich.listenerRegistered) {
               target.removeTargetListener(propertyName, localName, false, this.targetListener);
            }
         }

         j = info.cssAnimations.entrySet().iterator();

         while(j.hasNext()) {
            Map.Entry e2 = (Map.Entry)j.next();
            propertyName = (String)e2.getKey();
            Sandwich sandwich = (Sandwich)e2.getValue();
            if (sandwich.listenerRegistered) {
               target.removeTargetListener((String)null, propertyName, true, this.targetListener);
            }
         }
      }

   }

   public void pause() {
      if (this.pauseTime == 0L) {
         this.pauseTime = System.currentTimeMillis();
      }

   }

   public void unpause() {
      if (this.pauseTime != 0L) {
         Calendar begin = this.timedDocumentRoot.getDocumentBeginTime();
         int dt = (int)(System.currentTimeMillis() - this.pauseTime);
         begin.add(14, dt);
         this.pauseTime = 0L;
      }

   }

   public boolean isPaused() {
      return this.pauseTime != 0L;
   }

   public float getCurrentTime() {
      return this.timedDocumentRoot.getCurrentTime();
   }

   public float setCurrentTime(float t) {
      boolean p = this.pauseTime != 0L;
      this.unpause();
      Calendar begin = this.timedDocumentRoot.getDocumentBeginTime();
      float now = this.timedDocumentRoot.convertEpochTime(System.currentTimeMillis());
      begin.add(14, (int)((now - t) * 1000.0F));
      if (p) {
         this.pause();
      }

      return this.tick(t, true);
   }

   public void addAnimation(AnimationTarget target, short type, String ns, String an, AbstractAnimation anim) {
      this.timedDocumentRoot.addChild(anim.getTimedElement());
      AnimationInfo animInfo = this.getAnimationInfo(anim);
      animInfo.type = type;
      animInfo.attributeNamespaceURI = ns;
      animInfo.attributeLocalName = an;
      animInfo.target = target;
      this.animations.put(anim, animInfo);
      Sandwich sandwich = this.getSandwich(target, type, ns, an);
      if (sandwich.animation == null) {
         anim.lowerAnimation = null;
         anim.higherAnimation = null;
      } else {
         sandwich.animation.higherAnimation = anim;
         anim.lowerAnimation = sandwich.animation;
         anim.higherAnimation = null;
      }

      sandwich.animation = anim;
      if (anim.lowerAnimation == null) {
         sandwich.lowestAnimation = anim;
      }

   }

   public void removeAnimation(AbstractAnimation anim) {
      this.timedDocumentRoot.removeChild(anim.getTimedElement());
      AbstractAnimation nextHigher = anim.higherAnimation;
      if (nextHigher != null) {
         nextHigher.markDirty();
      }

      this.moveToBottom(anim);
      if (anim.higherAnimation != null) {
         anim.higherAnimation.lowerAnimation = null;
      }

      AnimationInfo animInfo = this.getAnimationInfo(anim);
      Sandwich sandwich = this.getSandwich(animInfo.target, animInfo.type, animInfo.attributeNamespaceURI, animInfo.attributeLocalName);
      if (sandwich.animation == anim) {
         sandwich.animation = null;
         sandwich.lowestAnimation = null;
         sandwich.shouldUpdate = true;
      }

   }

   protected Sandwich getSandwich(AnimationTarget target, short type, String ns, String an) {
      TargetInfo info = this.getTargetInfo(target);
      Sandwich sandwich;
      if (type == 0) {
         sandwich = (Sandwich)info.xmlAnimations.get(ns, an);
         if (sandwich == null) {
            sandwich = new Sandwich();
            info.xmlAnimations.put(ns, an, sandwich);
         }
      } else if (type == 1) {
         sandwich = (Sandwich)info.cssAnimations.get(an);
         if (sandwich == null) {
            sandwich = new Sandwich();
            info.cssAnimations.put(an, sandwich);
         }
      } else {
         sandwich = (Sandwich)info.otherAnimations.get(an);
         if (sandwich == null) {
            sandwich = new Sandwich();
            info.otherAnimations.put(an, sandwich);
         }
      }

      return sandwich;
   }

   protected TargetInfo getTargetInfo(AnimationTarget target) {
      TargetInfo info = (TargetInfo)this.targets.get(target);
      if (info == null) {
         info = new TargetInfo();
         this.targets.put(target, info);
      }

      return info;
   }

   protected AnimationInfo getAnimationInfo(AbstractAnimation anim) {
      AnimationInfo info = (AnimationInfo)this.animations.get(anim);
      if (info == null) {
         info = new AnimationInfo();
         this.animations.put(anim, info);
      }

      return info;
   }

   protected float tick(float time, boolean hyperlinking) {
      float waitTime = this.timedDocumentRoot.seekTo(time, hyperlinking);
      Map.Entry[] targetEntries = (Map.Entry[])((Map.Entry[])this.targets.entrySet().toArray(MAP_ENTRY_ARRAY));
      Map.Entry[] var5 = targetEntries;
      int var6 = targetEntries.length;

      label123:
      for(int var7 = 0; var7 < var6; ++var7) {
         Map.Entry e = var5[var7];
         AnimationTarget target = (AnimationTarget)e.getKey();
         TargetInfo info = (TargetInfo)e.getValue();
         Iterator j = info.xmlAnimations.iterator();

         while(true) {
            String propertyName;
            String localName;
            Sandwich sandwich;
            do {
               if (!j.hasNext()) {
                  j = info.cssAnimations.entrySet().iterator();

                  while(true) {
                     Sandwich sandwich;
                     AnimatableValue av;
                     do {
                        Map.Entry e2;
                        if (!j.hasNext()) {
                           j = info.otherAnimations.entrySet().iterator();

                           while(true) {
                              do {
                                 if (!j.hasNext()) {
                                    continue label123;
                                 }

                                 e2 = (Map.Entry)j.next();
                                 propertyName = (String)e2.getKey();
                                 sandwich = (Sandwich)e2.getValue();
                              } while(!sandwich.shouldUpdate && (sandwich.animation == null || !sandwich.animation.isDirty));

                              av = null;
                              AbstractAnimation anim = sandwich.animation;
                              if (anim != null) {
                                 av = sandwich.animation.getComposedValue();
                                 anim.isDirty = false;
                              }

                              target.updateOtherValue(propertyName, av);
                              sandwich.shouldUpdate = false;
                           }
                        }

                        e2 = (Map.Entry)j.next();
                        propertyName = (String)e2.getKey();
                        sandwich = (Sandwich)e2.getValue();
                     } while(!sandwich.shouldUpdate && (sandwich.animation == null || !sandwich.animation.isDirty));

                     av = null;
                     boolean usesUnderlying = false;
                     AbstractAnimation anim = sandwich.animation;
                     if (anim != null) {
                        av = anim.getComposedValue();
                        usesUnderlying = sandwich.lowestAnimation.usesUnderlyingValue();
                        anim.isDirty = false;
                     }

                     if (usesUnderlying && !sandwich.listenerRegistered) {
                        target.addTargetListener((String)null, propertyName, true, this.targetListener);
                        sandwich.listenerRegistered = true;
                     } else if (!usesUnderlying && sandwich.listenerRegistered) {
                        target.removeTargetListener((String)null, propertyName, true, this.targetListener);
                        sandwich.listenerRegistered = false;
                     }

                     if (usesUnderlying) {
                        target.updatePropertyValue(propertyName, (AnimatableValue)null);
                     }

                     if (!usesUnderlying || av != null) {
                        target.updatePropertyValue(propertyName, av);
                     }

                     sandwich.shouldUpdate = false;
                  }
               }

               DoublyIndexedTable.Entry e2 = (DoublyIndexedTable.Entry)j.next();
               propertyName = (String)e2.getKey1();
               localName = (String)e2.getKey2();
               sandwich = (Sandwich)e2.getValue();
            } while(!sandwich.shouldUpdate && (sandwich.animation == null || !sandwich.animation.isDirty));

            AnimatableValue av = null;
            boolean usesUnderlying = false;
            AbstractAnimation anim = sandwich.animation;
            if (anim != null) {
               av = anim.getComposedValue();
               usesUnderlying = sandwich.lowestAnimation.usesUnderlyingValue();
               anim.isDirty = false;
            }

            if (usesUnderlying && !sandwich.listenerRegistered) {
               target.addTargetListener(propertyName, localName, false, this.targetListener);
               sandwich.listenerRegistered = true;
            } else if (!usesUnderlying && sandwich.listenerRegistered) {
               target.removeTargetListener(propertyName, localName, false, this.targetListener);
               sandwich.listenerRegistered = false;
            }

            target.updateAttributeValue(propertyName, localName, av);
            sandwich.shouldUpdate = false;
         }
      }

      return waitTime;
   }

   public void toActive(AbstractAnimation anim, float begin) {
      this.moveToTop(anim);
      anim.isActive = true;
      anim.beginTime = begin;
      anim.isFrozen = false;
      this.pushDown(anim);
      anim.markDirty();
   }

   protected void pushDown(AbstractAnimation anim) {
      TimedElement e = anim.getTimedElement();
      AbstractAnimation top = null;
      boolean moved = false;

      while(anim.lowerAnimation != null && (anim.lowerAnimation.isActive || anim.lowerAnimation.isFrozen) && (anim.lowerAnimation.beginTime > anim.beginTime || anim.lowerAnimation.beginTime == anim.beginTime && e.isBefore(anim.lowerAnimation.getTimedElement()))) {
         AbstractAnimation higher = anim.higherAnimation;
         AbstractAnimation lower = anim.lowerAnimation;
         AbstractAnimation lowerLower = lower.lowerAnimation;
         if (higher != null) {
            higher.lowerAnimation = lower;
         }

         if (lowerLower != null) {
            lowerLower.higherAnimation = anim;
         }

         lower.lowerAnimation = anim;
         lower.higherAnimation = higher;
         anim.lowerAnimation = lowerLower;
         anim.higherAnimation = lower;
         if (!moved) {
            top = lower;
            moved = true;
         }
      }

      if (moved) {
         AnimationInfo animInfo = this.getAnimationInfo(anim);
         Sandwich sandwich = this.getSandwich(animInfo.target, animInfo.type, animInfo.attributeNamespaceURI, animInfo.attributeLocalName);
         if (sandwich.animation == anim) {
            sandwich.animation = top;
         }

         if (anim.lowerAnimation == null) {
            sandwich.lowestAnimation = anim;
         }
      }

   }

   public void toInactive(AbstractAnimation anim, boolean isFrozen) {
      anim.isActive = false;
      anim.isFrozen = isFrozen;
      anim.markDirty();
      if (!isFrozen) {
         anim.value = null;
         anim.beginTime = Float.NEGATIVE_INFINITY;
         this.moveToBottom(anim);
      }

   }

   public void removeFill(AbstractAnimation anim) {
      anim.isActive = false;
      anim.isFrozen = false;
      anim.value = null;
      anim.markDirty();
      this.moveToBottom(anim);
   }

   protected void moveToTop(AbstractAnimation anim) {
      AnimationInfo animInfo = this.getAnimationInfo(anim);
      Sandwich sandwich = this.getSandwich(animInfo.target, animInfo.type, animInfo.attributeNamespaceURI, animInfo.attributeLocalName);
      sandwich.shouldUpdate = true;
      if (anim.higherAnimation != null) {
         if (anim.lowerAnimation == null) {
            sandwich.lowestAnimation = anim.higherAnimation;
         } else {
            anim.lowerAnimation.higherAnimation = anim.higherAnimation;
         }

         anim.higherAnimation.lowerAnimation = anim.lowerAnimation;
         if (sandwich.animation != null) {
            sandwich.animation.higherAnimation = anim;
         }

         anim.lowerAnimation = sandwich.animation;
         anim.higherAnimation = null;
         sandwich.animation = anim;
      }
   }

   protected void moveToBottom(AbstractAnimation anim) {
      if (anim.lowerAnimation != null) {
         AnimationInfo animInfo = this.getAnimationInfo(anim);
         Sandwich sandwich = this.getSandwich(animInfo.target, animInfo.type, animInfo.attributeNamespaceURI, animInfo.attributeLocalName);
         AbstractAnimation nextLower = anim.lowerAnimation;
         nextLower.markDirty();
         anim.lowerAnimation.higherAnimation = anim.higherAnimation;
         if (anim.higherAnimation != null) {
            anim.higherAnimation.lowerAnimation = anim.lowerAnimation;
         } else {
            sandwich.animation = nextLower;
            sandwich.shouldUpdate = true;
         }

         sandwich.lowestAnimation.lowerAnimation = anim;
         anim.higherAnimation = sandwich.lowestAnimation;
         anim.lowerAnimation = null;
         sandwich.lowestAnimation = anim;
         if (sandwich.animation.isDirty) {
            sandwich.shouldUpdate = true;
         }

      }
   }

   public void addTimegraphListener(TimegraphListener l) {
      this.timedDocumentRoot.addTimegraphListener(l);
   }

   public void removeTimegraphListener(TimegraphListener l) {
      this.timedDocumentRoot.removeTimegraphListener(l);
   }

   public void sampledAt(AbstractAnimation anim, float simpleTime, float simpleDur, int repeatIteration) {
      anim.sampledAt(simpleTime, simpleDur, repeatIteration);
   }

   public void sampledLastValue(AbstractAnimation anim, int repeatIteration) {
      anim.sampledLastValue(repeatIteration);
   }

   protected abstract TimedDocumentRoot createDocumentRoot();

   protected static class AnimationInfo {
      public AnimationTarget target;
      public short type;
      public String attributeNamespaceURI;
      public String attributeLocalName;
   }

   protected static class Sandwich {
      public AbstractAnimation animation;
      public AbstractAnimation lowestAnimation;
      public boolean shouldUpdate;
      public boolean listenerRegistered;
   }

   protected static class TargetInfo {
      public DoublyIndexedTable xmlAnimations = new DoublyIndexedTable();
      public HashMap cssAnimations = new HashMap();
      public HashMap otherAnimations = new HashMap();
   }

   protected class Listener implements AnimationTargetListener {
      public void baseValueChanged(AnimationTarget t, String ns, String ln, boolean isCSS) {
         short type = isCSS ? 1 : 0;
         Sandwich sandwich = AnimationEngine.this.getSandwich(t, (short)type, ns, ln);
         sandwich.shouldUpdate = true;

         AbstractAnimation anim;
         for(anim = sandwich.animation; anim.lowerAnimation != null; anim = anim.lowerAnimation) {
         }

         anim.markDirty();
      }
   }
}
