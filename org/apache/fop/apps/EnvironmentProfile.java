package org.apache.fop.apps;

import java.net.URI;
import org.apache.fop.fonts.FontManager;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageSessionContext;
import org.apache.xmlgraphics.io.ResourceResolver;

public interface EnvironmentProfile {
   ResourceResolver getResourceResolver();

   FontManager getFontManager();

   URI getDefaultBaseURI();

   AbstractImageSessionContext.FallbackResolver getFallbackResolver();
}
