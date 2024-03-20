package org.apache.fop.fonts;

import org.apache.fop.events.EventBroadcaster;

public class FontEventAdapter implements FontEventListener {
   private final EventBroadcaster eventBroadcaster;
   private FontEventProducer eventProducer;

   public FontEventAdapter(EventBroadcaster broadcaster) {
      this.eventBroadcaster = broadcaster;
   }

   private FontEventProducer getEventProducer() {
      if (this.eventProducer == null) {
         this.eventProducer = FontEventProducer.Provider.get(this.eventBroadcaster);
      }

      return this.eventProducer;
   }

   public void fontSubstituted(Object source, FontTriplet requested, FontTriplet effective) {
      this.getEventProducer().fontSubstituted(source, requested, effective);
   }

   public void fontLoadingErrorAtAutoDetection(Object source, String fontURL, Exception e) {
      this.getEventProducer().fontLoadingErrorAtAutoDetection(source, fontURL, e);
   }

   public void glyphNotAvailable(Object source, char ch, String fontName) {
      this.getEventProducer().glyphNotAvailable(source, ch, fontName);
   }

   public void fontDirectoryNotFound(Object source, String dir) {
      this.getEventProducer().fontDirectoryNotFound(source, dir);
   }

   public void svgTextStrokedAsShapes(Object source, String fontFamily) {
      this.getEventProducer().svgTextStrokedAsShapes(source, fontFamily);
   }

   public void fontFeatureNotSuppprted(Object source, String feature, String onlySupportedIn) {
      this.getEventProducer().fontFeatureNotSuppprted(source, feature, onlySupportedIn);
   }
}
