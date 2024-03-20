package org.apache.batik.anim.timing;

import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.batik.util.DoublyIndexedSet;

public abstract class TimedDocumentRoot extends TimeContainer {
   protected Calendar documentBeginTime;
   protected boolean useSVG11AccessKeys;
   protected boolean useSVG12AccessKeys;
   protected DoublyIndexedSet propagationFlags = new DoublyIndexedSet();
   protected LinkedList listeners = new LinkedList();
   protected boolean isSampling;
   protected boolean isHyperlinking;

   public TimedDocumentRoot(boolean useSVG11AccessKeys, boolean useSVG12AccessKeys) {
      this.root = this;
      this.useSVG11AccessKeys = useSVG11AccessKeys;
      this.useSVG12AccessKeys = useSVG12AccessKeys;
   }

   protected float getImplicitDur() {
      return Float.POSITIVE_INFINITY;
   }

   public float getDefaultBegin(TimedElement child) {
      return 0.0F;
   }

   public float getCurrentTime() {
      return this.lastSampleTime;
   }

   public boolean isSampling() {
      return this.isSampling;
   }

   public boolean isHyperlinking() {
      return this.isHyperlinking;
   }

   public float seekTo(float time, boolean hyperlinking) {
      this.isSampling = true;
      this.lastSampleTime = time;
      this.isHyperlinking = hyperlinking;
      this.propagationFlags.clear();
      float mint = Float.POSITIVE_INFINITY;
      TimedElement[] es = this.getChildren();
      TimedElement[] var5 = es;
      int var6 = es.length;

      int var7;
      for(var7 = 0; var7 < var6; ++var7) {
         TimedElement e1 = var5[var7];
         float t = e1.sampleAt(time, hyperlinking);
         if (t < mint) {
            mint = t;
         }
      }

      boolean needsUpdates;
      do {
         needsUpdates = false;
         TimedElement[] var12 = es;
         var7 = es.length;

         for(int var13 = 0; var13 < var7; ++var13) {
            TimedElement e = var12[var13];
            if (e.shouldUpdateCurrentInterval) {
               needsUpdates = true;
               float t = e.sampleAt(time, hyperlinking);
               if (t < mint) {
                  mint = t;
               }
            }
         }
      } while(needsUpdates);

      this.isSampling = false;
      if (hyperlinking) {
         this.root.currentIntervalWillUpdate();
      }

      return mint;
   }

   public void resetDocument(Calendar documentBeginTime) {
      if (documentBeginTime == null) {
         this.documentBeginTime = Calendar.getInstance();
      } else {
         this.documentBeginTime = documentBeginTime;
      }

      this.reset(true);
   }

   public Calendar getDocumentBeginTime() {
      return this.documentBeginTime;
   }

   public float convertEpochTime(long t) {
      long begin = this.documentBeginTime.getTime().getTime();
      return (float)(t - begin) / 1000.0F;
   }

   public float convertWallclockTime(Calendar time) {
      long begin = this.documentBeginTime.getTime().getTime();
      long t = time.getTime().getTime();
      return (float)(t - begin) / 1000.0F;
   }

   public void addTimegraphListener(TimegraphListener l) {
      this.listeners.add(l);
   }

   public void removeTimegraphListener(TimegraphListener l) {
      this.listeners.remove(l);
   }

   void fireElementAdded(TimedElement e) {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         Object listener = var2.next();
         ((TimegraphListener)listener).elementAdded(e);
      }

   }

   void fireElementRemoved(TimedElement e) {
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         Object listener = var2.next();
         ((TimegraphListener)listener).elementRemoved(e);
      }

   }

   boolean shouldPropagate(Interval i, TimingSpecifier ts, boolean isBegin) {
      InstanceTime it = isBegin ? i.getBeginInstanceTime() : i.getEndInstanceTime();
      if (this.propagationFlags.contains(it, ts)) {
         return false;
      } else {
         this.propagationFlags.add(it, ts);
         return true;
      }
   }

   protected void currentIntervalWillUpdate() {
   }

   protected abstract String getEventNamespaceURI(String var1);

   protected abstract String getEventType(String var1);

   protected abstract String getRepeatEventName();
}
