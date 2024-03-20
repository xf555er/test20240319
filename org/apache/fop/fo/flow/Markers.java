package org.apache.fop.fo.flow;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class Markers {
   private Map firstQualifyingIsFirst;
   private Map firstQualifyingIsAny;
   private Map lastQualifyingIsFirst;
   private Map lastQualifyingIsLast;
   private Map lastQualifyingIsAny;
   private static Log log = LogFactory.getLog(Markers.class);

   public void register(Map marks, boolean starting, boolean isfirst, boolean islast) {
      if (marks != null) {
         if (log.isDebugEnabled()) {
            log.debug("--" + marks.keySet() + ": " + (starting ? "starting" : "ending") + (isfirst ? ", first" : "") + (islast ? ", last" : ""));
         }

         if (starting) {
            if (this.firstQualifyingIsAny == null) {
               this.firstQualifyingIsAny = new HashMap();
            }

            Set entries;
            Iterator var6;
            Map.Entry entry;
            String key;
            Marker marker;
            if (isfirst) {
               if (this.firstQualifyingIsFirst == null) {
                  this.firstQualifyingIsFirst = new HashMap();
               }

               entries = marks.entrySet();
               var6 = entries.iterator();

               while(var6.hasNext()) {
                  entry = (Map.Entry)var6.next();
                  key = (String)entry.getKey();
                  marker = (Marker)entry.getValue();
                  if (!this.firstQualifyingIsFirst.containsKey(key)) {
                     this.firstQualifyingIsFirst.put(key, marker);
                     if (log.isTraceEnabled()) {
                        log.trace("Adding marker " + key + " to firstQualifyingIsFirst");
                     }
                  }

                  if (!this.firstQualifyingIsAny.containsKey(key)) {
                     this.firstQualifyingIsAny.put(key, marker);
                     if (log.isTraceEnabled()) {
                        log.trace("Adding marker " + key + " to firstQualifyingIsAny");
                     }
                  }
               }

               if (this.lastQualifyingIsFirst == null) {
                  this.lastQualifyingIsFirst = new HashMap();
               }

               this.lastQualifyingIsFirst.putAll(marks);
               if (log.isTraceEnabled()) {
                  log.trace("Adding all markers to LastStart");
               }
            } else {
               entries = marks.entrySet();
               var6 = entries.iterator();

               while(var6.hasNext()) {
                  entry = (Map.Entry)var6.next();
                  key = (String)entry.getKey();
                  marker = (Marker)entry.getValue();
                  if (!this.firstQualifyingIsAny.containsKey(key)) {
                     this.firstQualifyingIsAny.put(key, marker);
                     if (log.isTraceEnabled()) {
                        log.trace("Adding marker " + key + " to firstQualifyingIsAny");
                     }
                  }
               }
            }
         } else {
            if (islast) {
               if (this.lastQualifyingIsLast == null) {
                  this.lastQualifyingIsLast = new HashMap();
               }

               this.lastQualifyingIsLast.putAll(marks);
               if (log.isTraceEnabled()) {
                  log.trace("Adding all markers to lastQualifyingIsLast");
               }
            }

            if (this.lastQualifyingIsAny == null) {
               this.lastQualifyingIsAny = new HashMap();
            }

            this.lastQualifyingIsAny.putAll(marks);
            if (log.isTraceEnabled()) {
               log.trace("Adding all markers to lastQualifyingIsAny");
            }
         }

      }
   }

   public Marker resolve(AbstractRetrieveMarker arm) {
      Marker mark = null;
      int pos = arm.getPosition();
      String name = arm.getRetrieveClassName();
      String posName = arm.getPositionLabel();
      String localName = arm.getLocalName();
      switch (pos) {
         case 49:
         case 204:
            if (this.firstQualifyingIsAny != null) {
               mark = (Marker)this.firstQualifyingIsAny.get(name);
            }
            break;
         case 54:
         case 189:
            if (this.firstQualifyingIsFirst != null) {
               mark = (Marker)this.firstQualifyingIsFirst.get(name);
            }

            if (mark == null && this.firstQualifyingIsAny != null) {
               mark = (Marker)this.firstQualifyingIsAny.get(name);
               posName = "FirstAny after " + posName;
            }
            break;
         case 74:
         case 191:
            if (this.lastQualifyingIsLast != null) {
               mark = (Marker)this.lastQualifyingIsLast.get(name);
            }

            if (mark == null && this.lastQualifyingIsAny != null) {
               mark = (Marker)this.lastQualifyingIsAny.get(name);
               posName = "LastAny after " + posName;
            }
            break;
         case 81:
         case 190:
            if (this.lastQualifyingIsFirst != null) {
               mark = (Marker)this.lastQualifyingIsFirst.get(name);
            }

            if (mark == null && this.lastQualifyingIsAny != null) {
               mark = (Marker)this.lastQualifyingIsAny.get(name);
               posName = "LastAny after " + posName;
            }
            break;
         default:
            throw new RuntimeException("Invalid position attribute in " + localName + ".");
      }

      if (log.isTraceEnabled()) {
         log.trace(localName + ": name[" + name + "]; position [" + posName + "]");
      }

      return mark;
   }

   public void dump() {
      if (log.isTraceEnabled()) {
         log.trace("FirstAny: " + this.firstQualifyingIsAny);
         log.trace("FirstStart: " + this.firstQualifyingIsFirst);
         log.trace("LastAny: " + this.lastQualifyingIsAny);
         log.trace("LastEnd: " + this.lastQualifyingIsLast);
         log.trace("LastStart: " + this.lastQualifyingIsFirst);
      }

   }
}
