package org.apache.fop.render;

import java.util.Collections;
import java.util.List;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.CustomFontCollection;
import org.apache.fop.fonts.DefaultFontConfigurator;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontConfigurator;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.render.pdf.PDFRendererConfig;

public abstract class PrintRendererConfigurator extends AbstractRendererConfigurator implements IFDocumentHandlerConfigurator {
   private final RendererConfig.RendererConfigParser rendererConfigParser;
   private final FontConfigurator fontInfoConfigurator;

   public PrintRendererConfigurator(FOUserAgent userAgent, RendererConfig.RendererConfigParser rendererConfigParser) {
      this(userAgent, rendererConfigParser, new DefaultFontConfigurator(userAgent.getFontManager(), new FontEventAdapter(userAgent.getEventBroadcaster()), userAgent.validateUserConfigStrictly()));
   }

   public PrintRendererConfigurator(FOUserAgent userAgent, RendererConfig.RendererConfigParser rendererConfigParser, FontConfigurator fontInfoConfigurator) {
      super(userAgent);
      this.rendererConfigParser = rendererConfigParser;
      this.fontInfoConfigurator = fontInfoConfigurator;
   }

   protected RendererConfig getRendererConfig(IFDocumentHandler documentHandler) throws FOPException {
      return this.getRendererConfig(documentHandler.getMimeType());
   }

   protected RendererConfig getRendererConfig(String mimeType) throws FOPException {
      return this.userAgent.getRendererConfig(mimeType, this.rendererConfigParser);
   }

   protected RendererConfig getRendererConfig(Renderer renderer) throws FOPException {
      return this.getRendererConfig(renderer.getMimeType());
   }

   public void configure(Renderer renderer) throws FOPException {
      PrintRenderer printRenderer = (PrintRenderer)renderer;
      List embedFontInfoList = this.buildFontList(renderer.getMimeType());
      printRenderer.addFontList(embedFontInfoList);
   }

   public void configure(IFDocumentHandler documentHandler) throws FOPException {
   }

   public void setupFontInfo(String mimeType, FontInfo fontInfo) throws FOPException {
      FontManager fontManager = this.userAgent.getFontManager();
      List fontCollections = this.getDefaultFontCollection();
      fontCollections.add(this.getCustomFontCollection(fontManager.getResourceResolver(), mimeType));
      fontManager.setup(fontInfo, (FontCollection[])fontCollections.toArray(new FontCollection[fontCollections.size()]));
   }

   protected abstract List getDefaultFontCollection();

   protected FontCollection getCustomFontCollection(InternalResourceResolver resolver, String mimeType) throws FOPException {
      List fontList;
      if (this.rendererConfigParser == null) {
         fontList = Collections.emptyList();
      } else {
         fontList = this.fontInfoConfigurator.configure(this.getRendererConfig(mimeType).getFontInfoConfig());
      }

      return this.createCollectionFromFontList(resolver, fontList);
   }

   protected FontCollection createCollectionFromFontList(InternalResourceResolver resolver, List fontList) {
      return new CustomFontCollection(resolver, fontList, this.userAgent.isComplexScriptFeaturesEnabled());
   }

   private List buildFontList(String mimeType) throws FOPException {
      return this.fontInfoConfigurator.configure(this.getRendererConfig(mimeType).getFontInfoConfig());
   }

   public static PrintRendererConfigurator createDefaultInstance(FOUserAgent userAgent) {
      return new PrintRendererConfigurator(userAgent, new PDFRendererConfig.PDFRendererConfigParser()) {
         protected List getDefaultFontCollection() {
            throw new UnsupportedOperationException();
         }
      };
   }
}
