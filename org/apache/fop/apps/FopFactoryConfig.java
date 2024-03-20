package org.apache.fop.apps;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.configuration.Configuration;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.layoutmgr.LayoutManagerMaker;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.impl.AbstractImageSessionContext;
import org.apache.xmlgraphics.io.ResourceResolver;

public interface FopFactoryConfig {
   boolean DEFAULT_BREAK_INDENT_INHERITANCE = false;
   boolean DEFAULT_STRICT_USERCONFIG_VALIDATION = true;
   boolean DEFAULT_STRICT_FO_VALIDATION = true;
   String DEFAULT_PAGE_WIDTH = "8.26in";
   String DEFAULT_PAGE_HEIGHT = "11in";
   float DEFAULT_SOURCE_RESOLUTION = 72.0F;
   float DEFAULT_TARGET_RESOLUTION = 72.0F;

   boolean isAccessibilityEnabled();

   boolean isKeepEmptyTags();

   LayoutManagerMaker getLayoutManagerMakerOverride();

   ResourceResolver getResourceResolver();

   URI getBaseURI();

   boolean validateStrictly();

   boolean validateUserConfigStrictly();

   boolean isBreakIndentInheritanceOnReferenceAreaBoundary();

   float getSourceResolution();

   float getTargetResolution();

   String getPageHeight();

   String getPageWidth();

   Set getIgnoredNamespaces();

   boolean isNamespaceIgnored(String var1);

   Configuration getUserConfig();

   boolean preferRenderer();

   FontManager getFontManager();

   ImageManager getImageManager();

   boolean isComplexScriptFeaturesEnabled();

   boolean isTableBorderOverpaint();

   Map getHyphenationPatternNames();

   InternalResourceResolver getHyphenationResourceResolver();

   AbstractImageSessionContext.FallbackResolver getFallbackResolver();
}
