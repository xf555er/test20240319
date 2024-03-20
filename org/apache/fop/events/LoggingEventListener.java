package org.apache.fop.events;

import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.events.model.EventSeverity;

public class LoggingEventListener implements EventListener {
   private static Log defaultLog = LogFactory.getLog(LoggingEventListener.class);
   private Log log;
   private boolean skipFatal;
   private final Set loggedMessages;

   public LoggingEventListener() {
      this(defaultLog);
   }

   public LoggingEventListener(Log log) {
      this(log, true);
   }

   public LoggingEventListener(Log log, boolean skipFatal) {
      this.loggedMessages = new HashSet();
      this.log = log;
      this.skipFatal = skipFatal;
   }

   public Log getLog() {
      return this.log;
   }

   public void processEvent(Event event) {
      String msg = EventFormatter.format(event);
      EventSeverity severity = event.getSeverity();
      if (severity == EventSeverity.INFO) {
         this.log.info(msg);
      } else if (severity == EventSeverity.WARN) {
         String eventGroupID = event.getEventGroupID();
         if (eventGroupID.equals("org.apache.fop.fonts.FontEventProducer")) {
            if (!this.loggedMessages.contains(msg)) {
               this.loggedMessages.add(msg);
               this.log.warn(msg);
            }
         } else {
            this.log.warn(msg);
         }
      } else if (severity == EventSeverity.ERROR) {
         if (event.getParam("e") != null) {
            this.log.error(msg, (Throwable)event.getParam("e"));
         } else {
            this.log.error(msg);
         }
      } else if (severity == EventSeverity.FATAL) {
         if (!this.skipFatal) {
            if (event.getParam("e") != null) {
               this.log.fatal(msg, (Throwable)event.getParam("e"));
            } else {
               this.log.fatal(msg);
            }
         }
      } else {
         assert false;
      }

   }
}
