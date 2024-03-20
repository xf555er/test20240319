package org.apache.batik.anim.timing;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import org.apache.batik.anim.AnimationException;
import org.apache.batik.i18n.LocalizableSupport;
import org.apache.batik.parser.ClockHandler;
import org.apache.batik.parser.ClockParser;
import org.apache.batik.parser.ParseException;
import org.apache.batik.util.SMILConstants;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;

public abstract class TimedElement implements SMILConstants {
   public static final int FILL_REMOVE = 0;
   public static final int FILL_FREEZE = 1;
   public static final int RESTART_ALWAYS = 0;
   public static final int RESTART_WHEN_NOT_ACTIVE = 1;
   public static final int RESTART_NEVER = 2;
   public static final float INDEFINITE = Float.POSITIVE_INFINITY;
   public static final float UNRESOLVED = Float.NaN;
   protected TimedDocumentRoot root;
   protected TimeContainer parent;
   protected TimingSpecifier[] beginTimes = new TimingSpecifier[0];
   protected TimingSpecifier[] endTimes;
   protected float simpleDur;
   protected boolean durMedia;
   protected float repeatCount;
   protected float repeatDur;
   protected int currentRepeatIteration;
   protected float lastRepeatTime;
   protected int fillMode;
   protected int restartMode;
   protected float min;
   protected boolean minMedia;
   protected float max;
   protected boolean maxMedia;
   protected boolean isActive;
   protected boolean isFrozen;
   protected float lastSampleTime;
   protected float repeatDuration;
   protected List beginInstanceTimes = new ArrayList();
   protected List endInstanceTimes = new ArrayList();
   protected Interval currentInterval;
   protected float lastIntervalEnd;
   protected Interval previousInterval;
   protected LinkedList beginDependents = new LinkedList();
   protected LinkedList endDependents = new LinkedList();
   protected boolean shouldUpdateCurrentInterval = true;
   protected boolean hasParsed;
   protected Map handledEvents = new HashMap();
   protected boolean isSampling;
   protected boolean hasPropagated;
   protected static final String RESOURCES = "org.apache.batik.anim.resources.Messages";
   protected static LocalizableSupport localizableSupport = new LocalizableSupport("org.apache.batik.anim.resources.Messages", TimedElement.class.getClassLoader());

   public TimedElement() {
      this.endTimes = this.beginTimes;
      this.simpleDur = Float.NaN;
      this.repeatCount = Float.NaN;
      this.repeatDur = Float.NaN;
      this.lastRepeatTime = Float.NaN;
      this.max = Float.POSITIVE_INFINITY;
      this.lastSampleTime = Float.NaN;
      this.lastIntervalEnd = Float.NEGATIVE_INFINITY;
   }

   public TimedDocumentRoot getRoot() {
      return this.root;
   }

   public float getActiveTime() {
      return this.lastSampleTime;
   }

   public float getSimpleTime() {
      return this.lastSampleTime - this.lastRepeatTime;
   }

   protected float addInstanceTime(InstanceTime time, boolean isBegin) {
      this.hasPropagated = true;
      List instanceTimes = isBegin ? this.beginInstanceTimes : this.endInstanceTimes;
      int index = Collections.binarySearch(instanceTimes, time);
      if (index < 0) {
         index = -(index + 1);
      }

      instanceTimes.add(index, time);
      this.shouldUpdateCurrentInterval = true;
      float ret;
      if (this.root.isSampling() && !this.isSampling) {
         ret = this.sampleAt(this.root.getCurrentTime(), this.root.isHyperlinking());
      } else {
         ret = Float.POSITIVE_INFINITY;
      }

      this.hasPropagated = false;
      this.root.currentIntervalWillUpdate();
      return ret;
   }

   protected float removeInstanceTime(InstanceTime time, boolean isBegin) {
      this.hasPropagated = true;
      List instanceTimes = isBegin ? this.beginInstanceTimes : this.endInstanceTimes;
      int index = Collections.binarySearch(instanceTimes, time);

      int len;
      for(len = index; len >= 0; --len) {
         InstanceTime it = (InstanceTime)instanceTimes.get(len);
         if (it == time) {
            instanceTimes.remove(len);
            break;
         }

         if (it.compareTo(time) != 0) {
            break;
         }
      }

      len = instanceTimes.size();

      for(int i = index + 1; i < len; ++i) {
         InstanceTime it = (InstanceTime)instanceTimes.get(i);
         if (it == time) {
            instanceTimes.remove(i);
            break;
         }

         if (it.compareTo(time) != 0) {
            break;
         }
      }

      this.shouldUpdateCurrentInterval = true;
      float ret;
      if (this.root.isSampling() && !this.isSampling) {
         ret = this.sampleAt(this.root.getCurrentTime(), this.root.isHyperlinking());
      } else {
         ret = Float.POSITIVE_INFINITY;
      }

      this.hasPropagated = false;
      this.root.currentIntervalWillUpdate();
      return ret;
   }

   protected float instanceTimeChanged(InstanceTime time, boolean isBegin) {
      this.hasPropagated = true;
      this.shouldUpdateCurrentInterval = true;
      float ret;
      if (this.root.isSampling() && !this.isSampling) {
         ret = this.sampleAt(this.root.getCurrentTime(), this.root.isHyperlinking());
      } else {
         ret = Float.POSITIVE_INFINITY;
      }

      this.hasPropagated = false;
      return ret;
   }

   protected void addDependent(TimingSpecifier dependent, boolean forBegin) {
      if (forBegin) {
         this.beginDependents.add(dependent);
      } else {
         this.endDependents.add(dependent);
      }

   }

   protected void removeDependent(TimingSpecifier dependent, boolean forBegin) {
      if (forBegin) {
         this.beginDependents.remove(dependent);
      } else {
         this.endDependents.remove(dependent);
      }

   }

   public float getSimpleDur() {
      if (this.durMedia) {
         return this.getImplicitDur();
      } else if (isUnresolved(this.simpleDur)) {
         return isUnresolved(this.repeatCount) && isUnresolved(this.repeatDur) && this.endTimes.length > 0 ? Float.POSITIVE_INFINITY : this.getImplicitDur();
      } else {
         return this.simpleDur;
      }
   }

   public static boolean isUnresolved(float t) {
      return Float.isNaN(t);
   }

   public float getActiveDur(float B, float end) {
      float d = this.getSimpleDur();
      float PAD;
      if (!isUnresolved(end) && d == Float.POSITIVE_INFINITY) {
         PAD = this.minusTime(end, B);
         this.repeatDuration = this.minTime(this.max, this.maxTime(this.min, PAD));
         return this.repeatDuration;
      } else {
         float IAD;
         if (d == 0.0F) {
            IAD = 0.0F;
         } else if (isUnresolved(this.repeatDur) && isUnresolved(this.repeatCount)) {
            IAD = d;
         } else {
            float p1 = isUnresolved(this.repeatCount) ? Float.POSITIVE_INFINITY : this.multiplyTime(d, this.repeatCount);
            float p2 = isUnresolved(this.repeatDur) ? Float.POSITIVE_INFINITY : this.repeatDur;
            IAD = this.minTime(this.minTime(p1, p2), Float.POSITIVE_INFINITY);
         }

         if (!isUnresolved(end) && end != Float.POSITIVE_INFINITY) {
            PAD = this.minTime(IAD, this.minusTime(end, B));
         } else {
            PAD = IAD;
         }

         this.repeatDuration = IAD;
         return this.minTime(this.max, this.maxTime(this.min, PAD));
      }
   }

   protected float minusTime(float t1, float t2) {
      if (!isUnresolved(t1) && !isUnresolved(t2)) {
         return t1 != Float.POSITIVE_INFINITY && t2 != Float.POSITIVE_INFINITY ? t1 - t2 : Float.POSITIVE_INFINITY;
      } else {
         return Float.NaN;
      }
   }

   protected float multiplyTime(float t, float n) {
      return !isUnresolved(t) && t != Float.POSITIVE_INFINITY ? t * n : t;
   }

   protected float minTime(float t1, float t2) {
      if (t1 != 0.0F && t2 != 0.0F) {
         if ((t1 == Float.POSITIVE_INFINITY || isUnresolved(t1)) && t2 != Float.POSITIVE_INFINITY && !isUnresolved(t2)) {
            return t2;
         } else if ((t2 == Float.POSITIVE_INFINITY || isUnresolved(t2)) && t1 != Float.POSITIVE_INFINITY && !isUnresolved(t1)) {
            return t1;
         } else if ((t1 != Float.POSITIVE_INFINITY || !isUnresolved(t2)) && (!isUnresolved(t1) || t2 != Float.POSITIVE_INFINITY)) {
            return t1 < t2 ? t1 : t2;
         } else {
            return Float.POSITIVE_INFINITY;
         }
      } else {
         return 0.0F;
      }
   }

   protected float maxTime(float t1, float t2) {
      if ((t1 == Float.POSITIVE_INFINITY || isUnresolved(t1)) && t2 != Float.POSITIVE_INFINITY && !isUnresolved(t2)) {
         return t1;
      } else if ((t2 == Float.POSITIVE_INFINITY || isUnresolved(t2)) && t1 != Float.POSITIVE_INFINITY && !isUnresolved(t1)) {
         return t2;
      } else if (t1 == Float.POSITIVE_INFINITY && isUnresolved(t2) || isUnresolved(t1) && t2 == Float.POSITIVE_INFINITY) {
         return Float.NaN;
      } else {
         return t1 > t2 ? t1 : t2;
      }
   }

   protected float getImplicitDur() {
      return Float.NaN;
   }

   protected float notifyNewInterval(Interval interval) {
      float dependentMinTime = Float.POSITIVE_INFINITY;
      Iterator i = this.beginDependents.iterator();

      TimingSpecifier ts;
      float t;
      while(i.hasNext()) {
         ts = (TimingSpecifier)i.next();
         t = ts.newInterval(interval);
         if (t < dependentMinTime) {
            dependentMinTime = t;
         }
      }

      i = this.endDependents.iterator();

      while(i.hasNext()) {
         ts = (TimingSpecifier)i.next();
         t = ts.newInterval(interval);
         if (t < dependentMinTime) {
            dependentMinTime = t;
         }
      }

      return dependentMinTime;
   }

   protected float notifyRemoveInterval(Interval interval) {
      float dependentMinTime = Float.POSITIVE_INFINITY;
      Iterator i = this.beginDependents.iterator();

      TimingSpecifier ts;
      float t;
      while(i.hasNext()) {
         ts = (TimingSpecifier)i.next();
         t = ts.removeInterval(interval);
         if (t < dependentMinTime) {
            dependentMinTime = t;
         }
      }

      i = this.endDependents.iterator();

      while(i.hasNext()) {
         ts = (TimingSpecifier)i.next();
         t = ts.removeInterval(interval);
         if (t < dependentMinTime) {
            dependentMinTime = t;
         }
      }

      return dependentMinTime;
   }

   protected float sampleAt(float parentSimpleTime, boolean hyperlinking) {
      this.isSampling = true;
      float time = parentSimpleTime;
      Iterator var4 = this.handledEvents.entrySet().iterator();

      label299:
      while(true) {
         Event evt;
         Set ts;
         Iterator j;
         boolean useEnd;
         boolean useBegin;
         while(true) {
            if (!var4.hasNext()) {
               this.handledEvents.clear();
               if (this.currentInterval != null) {
                  float begin = this.currentInterval.getBegin();
                  if (this.lastSampleTime < begin && parentSimpleTime >= begin) {
                     if (!this.isActive) {
                        this.toActive(begin);
                     }

                     this.isActive = true;
                     this.isFrozen = false;
                     this.lastRepeatTime = begin;
                     this.fireTimeEvent("beginEvent", this.currentInterval.getBegin(), 0);
                  }
               }

               boolean hasEnded = this.currentInterval != null && parentSimpleTime >= this.currentInterval.getEnd();
               float dependentMinTime;
               float d;
               if (this.currentInterval != null) {
                  dependentMinTime = this.currentInterval.getBegin();
                  if (parentSimpleTime >= dependentMinTime) {
                     d = this.getSimpleDur();

                     while(time - this.lastRepeatTime >= d && this.lastRepeatTime + d < dependentMinTime + this.repeatDuration) {
                        this.lastRepeatTime += d;
                        ++this.currentRepeatIteration;
                        this.fireTimeEvent(this.root.getRepeatEventName(), this.lastRepeatTime, this.currentRepeatIteration);
                     }
                  }
               }

               dependentMinTime = Float.POSITIVE_INFINITY;
               if (hyperlinking) {
                  this.shouldUpdateCurrentInterval = true;
               }

               float t;
               boolean incl;
               while(this.shouldUpdateCurrentInterval || hasEnded) {
                  if (hasEnded) {
                     this.previousInterval = this.currentInterval;
                     this.isActive = false;
                     this.isFrozen = this.fillMode == 1;
                     this.toInactive(false, this.isFrozen);
                     this.fireTimeEvent("endEvent", this.currentInterval.getEnd(), 0);
                  }

                  boolean first = this.currentInterval == null && this.previousInterval == null;
                  if (this.currentInterval != null && hyperlinking) {
                     this.isActive = false;
                     this.isFrozen = false;
                     this.toInactive(false, false);
                     this.currentInterval = null;
                  }

                  float dmt;
                  if (this.currentInterval != null && !hasEnded) {
                     t = this.currentInterval.getBegin();
                     if (t > time) {
                        boolean incl = true;
                        float beginAfter;
                        if (this.previousInterval == null) {
                           beginAfter = Float.NEGATIVE_INFINITY;
                        } else {
                           beginAfter = this.previousInterval.getEnd();
                           incl = beginAfter != this.previousInterval.getBegin();
                        }

                        Interval interval = this.computeInterval(false, false, beginAfter, incl);
                        float dmt = this.notifyRemoveInterval(this.currentInterval);
                        if (dmt < dependentMinTime) {
                           dependentMinTime = dmt;
                        }

                        if (interval == null) {
                           this.currentInterval = null;
                        } else {
                           dmt = this.selectNewInterval(time, interval);
                           if (dmt < dependentMinTime) {
                              dependentMinTime = dmt;
                           }
                        }
                     } else {
                        Interval interval = this.computeInterval(false, true, t, true);
                        float newEnd = interval.getEnd();
                        if (this.currentInterval.getEnd() != newEnd) {
                           dmt = this.currentInterval.setEnd(newEnd, interval.getEndInstanceTime());
                           if (dmt < dependentMinTime) {
                              dependentMinTime = dmt;
                           }
                        }
                     }
                  } else if (!first && !hyperlinking && this.restartMode == 2) {
                     this.currentInterval = null;
                  } else {
                     incl = true;
                     if (!first && !hyperlinking) {
                        t = this.previousInterval.getEnd();
                        incl = t != this.previousInterval.getBegin();
                     } else {
                        t = Float.NEGATIVE_INFINITY;
                     }

                     Interval interval = this.computeInterval(first, false, t, incl);
                     if (interval == null) {
                        this.currentInterval = null;
                     } else {
                        dmt = this.selectNewInterval(time, interval);
                        if (dmt < dependentMinTime) {
                           dependentMinTime = dmt;
                        }
                     }
                  }

                  this.shouldUpdateCurrentInterval = false;
                  hyperlinking = false;
                  hasEnded = this.currentInterval != null && time >= this.currentInterval.getEnd();
               }

               d = this.getSimpleDur();
               if (this.isActive && !this.isFrozen) {
                  if (time - this.currentInterval.getBegin() >= this.repeatDuration) {
                     this.isFrozen = this.fillMode == 1;
                     this.toInactive(true, this.isFrozen);
                  } else {
                     this.sampledAt(time - this.lastRepeatTime, d, this.currentRepeatIteration);
                  }
               }

               if (this.isFrozen) {
                  if (this.isActive) {
                     t = this.currentInterval.getBegin() + this.repeatDuration - this.lastRepeatTime;
                     incl = this.lastRepeatTime + d == this.currentInterval.getBegin() + this.repeatDuration;
                  } else {
                     t = this.previousInterval.getEnd() - this.lastRepeatTime;
                     incl = this.lastRepeatTime + d == this.previousInterval.getEnd();
                  }

                  if (incl) {
                     this.sampledLastValue(this.currentRepeatIteration);
                  } else {
                     this.sampledAt(t % d, d, this.currentRepeatIteration);
                  }
               } else if (!this.isActive) {
               }

               this.isSampling = false;
               this.lastSampleTime = time;
               if (this.currentInterval == null) {
                  return dependentMinTime;
               }

               t = this.currentInterval.getBegin() - time;
               if (t <= 0.0F) {
                  t = !this.isConstantAnimation() && !this.isFrozen ? 0.0F : this.currentInterval.getEnd() - time;
               }

               if (dependentMinTime < t) {
                  return dependentMinTime;
               }

               return t;
            }

            Object o = var4.next();
            Map.Entry e = (Map.Entry)o;
            evt = (Event)e.getKey();
            ts = (Set)e.getValue();
            j = ts.iterator();
            boolean hasBegin = false;
            boolean hasEnd = false;

            while(j.hasNext() && (!hasBegin || !hasEnd)) {
               EventLikeTimingSpecifier t = (EventLikeTimingSpecifier)j.next();
               if (t.isBegin()) {
                  hasBegin = true;
               } else {
                  hasEnd = true;
               }
            }

            if (hasBegin && hasEnd) {
               useBegin = !this.isActive || this.restartMode == 0;
               useEnd = !useBegin;
               break;
            }

            if (!hasBegin || this.isActive && this.restartMode != 0) {
               if (!hasEnd || !this.isActive) {
                  continue;
               }

               useBegin = false;
               useEnd = true;
               break;
            }

            useBegin = true;
            useEnd = false;
            break;
         }

         j = ts.iterator();

         while(true) {
            EventLikeTimingSpecifier t;
            boolean isBegin;
            do {
               if (!j.hasNext()) {
                  continue label299;
               }

               t = (EventLikeTimingSpecifier)j.next();
               isBegin = t.isBegin();
            } while((!isBegin || !useBegin) && (isBegin || !useEnd));

            t.resolve(evt);
            this.shouldUpdateCurrentInterval = true;
         }
      }
   }

   protected boolean endHasEventConditions() {
      TimingSpecifier[] var1 = this.endTimes;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         TimingSpecifier endTime = var1[var3];
         if (endTime.isEventCondition()) {
            return true;
         }
      }

      return false;
   }

   protected float selectNewInterval(float time, Interval interval) {
      this.currentInterval = interval;
      float dmt = this.notifyNewInterval(this.currentInterval);
      float beginEventTime = this.currentInterval.getBegin();
      if (time >= beginEventTime) {
         this.lastRepeatTime = beginEventTime;
         if (beginEventTime < 0.0F) {
            beginEventTime = 0.0F;
         }

         this.toActive(beginEventTime);
         this.isActive = true;
         this.isFrozen = false;
         this.fireTimeEvent("beginEvent", beginEventTime, 0);
         float d = this.getSimpleDur();
         float end = this.currentInterval.getEnd();

         while(time - this.lastRepeatTime >= d && this.lastRepeatTime + d < end) {
            this.lastRepeatTime += d;
            ++this.currentRepeatIteration;
            this.fireTimeEvent(this.root.getRepeatEventName(), this.lastRepeatTime, this.currentRepeatIteration);
         }
      }

      return dmt;
   }

   protected Interval computeInterval(boolean first, boolean fixedBegin, float beginAfter, boolean incl) {
      Iterator beginIterator = this.beginInstanceTimes.iterator();
      Iterator endIterator = this.endInstanceTimes.iterator();
      float parentSimpleDur = this.parent.getSimpleDur();
      InstanceTime endInstanceTime = endIterator.hasNext() ? (InstanceTime)endIterator.next() : null;
      boolean firstEnd = true;
      InstanceTime beginInstanceTime = null;
      InstanceTime nextBeginInstanceTime = null;

      while(true) {
         float tempBegin;
         if (fixedBegin) {
            tempBegin = beginAfter;

            while(beginIterator.hasNext()) {
               nextBeginInstanceTime = (InstanceTime)beginIterator.next();
               if (nextBeginInstanceTime.getTime() > tempBegin) {
                  break;
               }
            }
         } else {
            while(true) {
               do {
                  if (!beginIterator.hasNext()) {
                     return null;
                  }

                  beginInstanceTime = (InstanceTime)beginIterator.next();
                  tempBegin = beginInstanceTime.getTime();
               } while((!incl || !(tempBegin >= beginAfter)) && (incl || !(tempBegin > beginAfter)));

               if (!beginIterator.hasNext()) {
                  break;
               }

               nextBeginInstanceTime = (InstanceTime)beginIterator.next();
               if (beginInstanceTime.getTime() != nextBeginInstanceTime.getTime()) {
                  break;
               }

               nextBeginInstanceTime = null;
            }
         }

         if (tempBegin >= parentSimpleDur) {
            return null;
         }

         float tempEnd;
         float nextBegin;
         if (this.endTimes.length == 0) {
            tempEnd = tempBegin + this.getActiveDur(tempBegin, Float.POSITIVE_INFINITY);
         } else {
            if (this.endInstanceTimes.isEmpty()) {
               tempEnd = Float.NaN;
            } else {
               tempEnd = endInstanceTime.getTime();
               if (first && !firstEnd && tempEnd == tempBegin || !first && this.currentInterval != null && tempEnd == this.currentInterval.getEnd() && (incl && beginAfter >= tempEnd || !incl && beginAfter > tempEnd)) {
                  do {
                     if (!endIterator.hasNext()) {
                        if (!this.endHasEventConditions()) {
                           return null;
                        }

                        tempEnd = Float.NaN;
                        break;
                     }

                     endInstanceTime = (InstanceTime)endIterator.next();
                     tempEnd = endInstanceTime.getTime();
                  } while(!(tempEnd > tempBegin));
               }

               for(firstEnd = false; !(tempEnd >= tempBegin); tempEnd = endInstanceTime.getTime()) {
                  if (!endIterator.hasNext()) {
                     if (!this.endHasEventConditions()) {
                        return null;
                     }

                     tempEnd = Float.NaN;
                     break;
                  }

                  endInstanceTime = (InstanceTime)endIterator.next();
               }
            }

            nextBegin = this.getActiveDur(tempBegin, tempEnd);
            tempEnd = tempBegin + nextBegin;
         }

         if (!first || tempEnd > 0.0F || tempBegin == 0.0F && tempEnd == 0.0F || isUnresolved(tempEnd)) {
            if (this.restartMode == 0 && nextBeginInstanceTime != null) {
               nextBegin = nextBeginInstanceTime.getTime();
               if (nextBegin < tempEnd || isUnresolved(tempEnd)) {
                  tempEnd = nextBegin;
                  endInstanceTime = nextBeginInstanceTime;
               }
            }

            Interval i = new Interval(tempBegin, tempEnd, beginInstanceTime, endInstanceTime);
            return i;
         }

         if (fixedBegin) {
            return null;
         }

         beginAfter = tempEnd;
      }
   }

   protected void reset(boolean clearCurrentBegin) {
      Iterator i = this.beginInstanceTimes.iterator();

      while(true) {
         InstanceTime it;
         do {
            do {
               if (!i.hasNext()) {
                  i = this.endInstanceTimes.iterator();

                  while(i.hasNext()) {
                     it = (InstanceTime)i.next();
                     if (it.getClearOnReset()) {
                        i.remove();
                     }
                  }

                  if (this.isFrozen) {
                     this.removeFill();
                  }

                  this.currentRepeatIteration = 0;
                  this.lastRepeatTime = Float.NaN;
                  this.isActive = false;
                  this.isFrozen = false;
                  this.lastSampleTime = Float.NaN;
                  return;
               }

               it = (InstanceTime)i.next();
            } while(!it.getClearOnReset());
         } while(!clearCurrentBegin && this.currentInterval != null && this.currentInterval.getBeginInstanceTime() == it);

         i.remove();
      }
   }

   public void parseAttributes(String begin, String dur, String end, String min, String max, String repeatCount, String repeatDur, String fill, String restart) {
      if (!this.hasParsed) {
         this.parseBegin(begin);
         this.parseDur(dur);
         this.parseEnd(end);
         this.parseMin(min);
         this.parseMax(max);
         if (this.min > this.max) {
            this.min = 0.0F;
            this.max = Float.POSITIVE_INFINITY;
         }

         this.parseRepeatCount(repeatCount);
         this.parseRepeatDur(repeatDur);
         this.parseFill(fill);
         this.parseRestart(restart);
         this.hasParsed = true;
      }

   }

   protected void parseBegin(String begin) {
      try {
         if (begin.length() == 0) {
            begin = "0";
         }

         this.beginTimes = TimingSpecifierListProducer.parseTimingSpecifierList(this, true, begin, this.root.useSVG11AccessKeys, this.root.useSVG12AccessKeys);
      } catch (ParseException var3) {
         throw this.createException("attribute.malformed", new Object[]{null, "begin"});
      }
   }

   protected void parseDur(String dur) {
      if (dur.equals("media")) {
         this.durMedia = true;
         this.simpleDur = Float.NaN;
      } else {
         this.durMedia = false;
         if (dur.length() != 0 && !dur.equals("indefinite")) {
            try {
               this.simpleDur = this.parseClockValue(dur, false);
            } catch (ParseException var3) {
               throw this.createException("attribute.malformed", new Object[]{null, "dur"});
            }

            if (this.simpleDur < 0.0F) {
               this.simpleDur = Float.POSITIVE_INFINITY;
            }
         } else {
            this.simpleDur = Float.POSITIVE_INFINITY;
         }
      }

   }

   protected float parseClockValue(String s, boolean parseOffset) throws ParseException {
      ClockParser p = new ClockParser(parseOffset);

      class Handler implements ClockHandler {
         protected float v = 0.0F;

         public void clockValue(float newClockValue) {
            this.v = newClockValue;
         }
      }

      Handler h = new Handler();
      p.setClockHandler(h);
      p.parse(s);
      return h.v;
   }

   protected void parseEnd(String end) {
      try {
         this.endTimes = TimingSpecifierListProducer.parseTimingSpecifierList(this, false, end, this.root.useSVG11AccessKeys, this.root.useSVG12AccessKeys);
      } catch (ParseException var3) {
         throw this.createException("attribute.malformed", new Object[]{null, "end"});
      }
   }

   protected void parseMin(String min) {
      if (min.equals("media")) {
         this.min = 0.0F;
         this.minMedia = true;
      } else {
         this.minMedia = false;
         if (min.length() == 0) {
            this.min = 0.0F;
         } else {
            try {
               this.min = this.parseClockValue(min, false);
            } catch (ParseException var3) {
               this.min = 0.0F;
            }

            if (this.min < 0.0F) {
               this.min = 0.0F;
            }
         }
      }

   }

   protected void parseMax(String max) {
      if (max.equals("media")) {
         this.max = Float.POSITIVE_INFINITY;
         this.maxMedia = true;
      } else {
         this.maxMedia = false;
         if (max.length() != 0 && !max.equals("indefinite")) {
            try {
               this.max = this.parseClockValue(max, false);
            } catch (ParseException var3) {
               this.max = Float.POSITIVE_INFINITY;
            }

            if (this.max < 0.0F) {
               this.max = 0.0F;
            }
         } else {
            this.max = Float.POSITIVE_INFINITY;
         }
      }

   }

   protected void parseRepeatCount(String repeatCount) {
      if (repeatCount.length() == 0) {
         this.repeatCount = Float.NaN;
      } else if (repeatCount.equals("indefinite")) {
         this.repeatCount = Float.POSITIVE_INFINITY;
      } else {
         try {
            this.repeatCount = Float.parseFloat(repeatCount);
            if (this.repeatCount > 0.0F) {
               return;
            }
         } catch (NumberFormatException var3) {
            throw this.createException("attribute.malformed", new Object[]{null, "repeatCount"});
         }
      }

   }

   protected void parseRepeatDur(String repeatDur) {
      try {
         if (repeatDur.length() == 0) {
            this.repeatDur = Float.NaN;
         } else if (repeatDur.equals("indefinite")) {
            this.repeatDur = Float.POSITIVE_INFINITY;
         } else {
            this.repeatDur = this.parseClockValue(repeatDur, false);
         }

      } catch (ParseException var3) {
         throw this.createException("attribute.malformed", new Object[]{null, "repeatDur"});
      }
   }

   protected void parseFill(String fill) {
      if (fill.length() != 0 && !fill.equals("remove")) {
         if (!fill.equals("freeze")) {
            throw this.createException("attribute.malformed", new Object[]{null, "fill"});
         }

         this.fillMode = 1;
      } else {
         this.fillMode = 0;
      }

   }

   protected void parseRestart(String restart) {
      if (restart.length() != 0 && !restart.equals("always")) {
         if (restart.equals("whenNotActive")) {
            this.restartMode = 1;
         } else {
            if (!restart.equals("never")) {
               throw this.createException("attribute.malformed", new Object[]{null, "restart"});
            }

            this.restartMode = 2;
         }
      } else {
         this.restartMode = 0;
      }

   }

   public void initialize() {
      TimingSpecifier[] var1 = this.beginTimes;
      int var2 = var1.length;

      int var3;
      TimingSpecifier endTime;
      for(var3 = 0; var3 < var2; ++var3) {
         endTime = var1[var3];
         endTime.initialize();
      }

      var1 = this.endTimes;
      var2 = var1.length;

      for(var3 = 0; var3 < var2; ++var3) {
         endTime = var1[var3];
         endTime.initialize();
      }

   }

   public void deinitialize() {
      TimingSpecifier[] var1 = this.beginTimes;
      int var2 = var1.length;

      int var3;
      TimingSpecifier endTime;
      for(var3 = 0; var3 < var2; ++var3) {
         endTime = var1[var3];
         endTime.deinitialize();
      }

      var1 = this.endTimes;
      var2 = var1.length;

      for(var3 = 0; var3 < var2; ++var3) {
         endTime = var1[var3];
         endTime.deinitialize();
      }

   }

   public void beginElement() {
      this.beginElement(0.0F);
   }

   public void beginElement(float offset) {
      float t = this.root.convertWallclockTime(Calendar.getInstance());
      InstanceTime it = new InstanceTime((TimingSpecifier)null, t + offset, true);
      this.addInstanceTime(it, true);
   }

   public void endElement() {
      this.endElement(0.0F);
   }

   public void endElement(float offset) {
      float t = this.root.convertWallclockTime(Calendar.getInstance());
      InstanceTime it = new InstanceTime((TimingSpecifier)null, t + offset, true);
      this.addInstanceTime(it, false);
   }

   public float getLastSampleTime() {
      return this.lastSampleTime;
   }

   public float getCurrentBeginTime() {
      float begin;
      return this.currentInterval != null && !((begin = this.currentInterval.getBegin()) < this.lastSampleTime) ? begin : Float.NaN;
   }

   public boolean canBegin() {
      return this.currentInterval == null || this.isActive && this.restartMode != 2;
   }

   public boolean canEnd() {
      return this.isActive;
   }

   public float getHyperlinkBeginTime() {
      if (this.isActive) {
         return this.currentInterval.getBegin();
      } else {
         return !this.beginInstanceTimes.isEmpty() ? ((InstanceTime)this.beginInstanceTimes.get(0)).getTime() : Float.NaN;
      }
   }

   public TimingSpecifier[] getBeginTimingSpecifiers() {
      return (TimingSpecifier[])this.beginTimes.clone();
   }

   public TimingSpecifier[] getEndTimingSpecifiers() {
      return (TimingSpecifier[])this.endTimes.clone();
   }

   protected void fireTimeEvent(String eventType, float time, int detail) {
      Calendar t = (Calendar)this.root.getDocumentBeginTime().clone();
      t.add(14, (int)Math.round((double)time * 1000.0));
      this.fireTimeEvent(eventType, t, detail);
   }

   void eventOccurred(TimingSpecifier t, Event e) {
      Set ts = (HashSet)this.handledEvents.get(e);
      if (ts == null) {
         ts = new HashSet();
         this.handledEvents.put(e, ts);
      }

      ts.add(t);
      this.root.currentIntervalWillUpdate();
   }

   protected abstract void fireTimeEvent(String var1, Calendar var2, int var3);

   protected abstract void toActive(float var1);

   protected abstract void toInactive(boolean var1, boolean var2);

   protected abstract void removeFill();

   protected abstract void sampledAt(float var1, float var2, int var3);

   protected abstract void sampledLastValue(int var1);

   protected abstract TimedElement getTimedElementById(String var1);

   protected abstract EventTarget getEventTargetById(String var1);

   protected abstract EventTarget getRootEventTarget();

   public abstract Element getElement();

   protected abstract EventTarget getAnimationEventTarget();

   public abstract boolean isBefore(TimedElement var1);

   protected abstract boolean isConstantAnimation();

   public AnimationException createException(String code, Object[] params) {
      Element e = this.getElement();
      if (e != null) {
         params[0] = e.getNodeName();
      }

      return new AnimationException(this, code, params);
   }

   public static void setLocale(Locale l) {
      localizableSupport.setLocale(l);
   }

   public static Locale getLocale() {
      return localizableSupport.getLocale();
   }

   public static String formatMessage(String key, Object[] args) throws MissingResourceException {
      return localizableSupport.formatMessage(key, args);
   }

   public static String toString(float time) {
      if (Float.isNaN(time)) {
         return "UNRESOLVED";
      } else {
         return time == Float.POSITIVE_INFINITY ? "INDEFINITE" : Float.toString(time);
      }
   }
}
