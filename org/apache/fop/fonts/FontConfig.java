package org.apache.fop.fonts;

import org.apache.fop.apps.FOPException;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.events.EventProducer;

public interface FontConfig {
   public interface FontConfigParser {
      FontConfig parse(Configuration var1, FontManager var2, boolean var3, EventProducer var4) throws FOPException;
   }
}
