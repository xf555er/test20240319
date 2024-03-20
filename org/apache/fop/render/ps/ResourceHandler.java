package org.apache.fop.render.ps;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.ResourceEventProducer;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.ImageHandler;
import org.apache.fop.render.ImageHandlerRegistry;
import org.apache.xmlgraphics.image.loader.Image;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageFlavor;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageManager;
import org.apache.xmlgraphics.image.loader.ImageSessionContext;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;
import org.apache.xmlgraphics.ps.PSGenerator;
import org.apache.xmlgraphics.ps.PSResource;
import org.apache.xmlgraphics.ps.dsc.DSCException;
import org.apache.xmlgraphics.ps.dsc.DSCFilter;
import org.apache.xmlgraphics.ps.dsc.DSCListener;
import org.apache.xmlgraphics.ps.dsc.DSCParser;
import org.apache.xmlgraphics.ps.dsc.DSCParserConstants;
import org.apache.xmlgraphics.ps.dsc.DefaultNestedDocumentHandler;
import org.apache.xmlgraphics.ps.dsc.ResourceTracker;
import org.apache.xmlgraphics.ps.dsc.events.DSCComment;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentBoundingBox;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentDocumentNeededResources;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentDocumentSuppliedResources;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentHiResBoundingBox;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentIncludeResource;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentLanguageLevel;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentPage;
import org.apache.xmlgraphics.ps.dsc.events.DSCCommentPages;
import org.apache.xmlgraphics.ps.dsc.events.DSCEvent;
import org.apache.xmlgraphics.ps.dsc.events.DSCHeaderComment;
import org.apache.xmlgraphics.ps.dsc.events.PostScriptComment;
import org.apache.xmlgraphics.ps.dsc.events.PostScriptLine;
import org.apache.xmlgraphics.ps.dsc.tools.DSCTools;
import org.xml.sax.Locator;

public class ResourceHandler implements DSCParserConstants, PSSupportedFlavors {
   private static Log log = LogFactory.getLog(ResourceHandler.class);
   private FOUserAgent userAgent;
   private FontInfo fontInfo;
   private PSEventProducer eventProducer;
   private ResourceTracker resTracker;
   private Map globalFormResources = new HashMap();
   private Map inlineFormResources = new HashMap();

   public ResourceHandler(FOUserAgent userAgent, PSEventProducer eventProducer, FontInfo fontInfo, ResourceTracker resTracker, Map formResources) {
      this.userAgent = userAgent;
      this.eventProducer = eventProducer;
      this.fontInfo = fontInfo;
      this.resTracker = resTracker;
      this.determineInlineForms(formResources);
   }

   private void determineInlineForms(Map formResources) {
      if (formResources != null) {
         Iterator var2 = formResources.entrySet().iterator();

         while(var2.hasNext()) {
            Object o = var2.next();
            Map.Entry entry = (Map.Entry)o;
            PSResource res = (PSResource)entry.getValue();
            long count = this.resTracker.getUsageCount(res);
            if (count > 1L) {
               this.globalFormResources.put(entry.getKey(), res);
            } else {
               this.inlineFormResources.put(res, res);
               this.resTracker.declareInlined(res);
            }
         }

      }
   }

   public void process(InputStream in, OutputStream out, int pageCount, Rectangle2D documentBoundingBox, PSRenderingUtil psUtil) throws DSCException, IOException {
      DSCParser parser = new DSCParser(in);
      parser.setCheckEOF(false);
      PSGenerator gen = new PSGenerator(out);
      gen.setAcrobatDownsample(psUtil.isAcrobatDownsample());
      parser.addListener(new DefaultNestedDocumentHandler(gen));
      parser.addListener(new IncludeResourceListener(gen));
      DSCHeaderComment header = DSCTools.checkAndSkipDSC30Header(parser);
      header.generate(gen);
      parser.setFilter(new DSCFilter() {
         private final Set filtered = new HashSet();

         {
            this.filtered.add("Pages");
            this.filtered.add("BoundingBox");
            this.filtered.add("HiResBoundingBox");
            this.filtered.add("DocumentNeededResources");
            this.filtered.add("DocumentSuppliedResources");
         }

         public boolean accept(DSCEvent event) {
            if (event.isDSCComment()) {
               return !this.filtered.contains(event.asDSCComment().getName());
            } else {
               return true;
            }
         }
      });

      while(true) {
         DSCEvent event = parser.nextEvent();
         if (event == null) {
            reportInvalidDSC();
         }

         DSCComment pageOrTrailer;
         if (DSCTools.headerCommentsEndHere(event)) {
            DSCCommentPages pages = new DSCCommentPages(pageCount);
            pages.generate(gen);
            (new DSCCommentBoundingBox(documentBoundingBox)).generate(gen);
            (new DSCCommentHiResBoundingBox(documentBoundingBox)).generate(gen);
            PSFontUtils.determineSuppliedFonts(this.resTracker, this.fontInfo, this.fontInfo.getUsedFonts());
            registerSuppliedForms(this.resTracker, this.globalFormResources);
            DSCCommentDocumentSuppliedResources supplied = new DSCCommentDocumentSuppliedResources(this.resTracker.getDocumentSuppliedResources());
            supplied.generate(gen);
            DSCCommentDocumentNeededResources needed = new DSCCommentDocumentNeededResources(this.resTracker.getDocumentNeededResources());
            needed.generate(gen);
            event.generate(gen);
            PostScriptComment var13 = parser.nextPSComment("FOPFontSetup", gen);
            if (var13 == null) {
               throw new DSCException("Didn't find %FOPFontSetup comment in stream");
            }

            PSFontUtils.writeFontDict(gen, this.fontInfo, this.fontInfo.getUsedFonts(), this.eventProducer);
            this.generateForms(this.globalFormResources, gen);
            pageOrTrailer = parser.nextDSCComment("Page", gen);
            if (pageOrTrailer == null) {
               throw new DSCException("Page expected, but none found");
            }

            while(true) {
               while(true) {
                  DSCCommentPage page = (DSCCommentPage)pageOrTrailer;
                  page.generate(gen);
                  pageOrTrailer = DSCTools.nextPageOrTrailer(parser, gen);
                  if (pageOrTrailer == null) {
                     reportInvalidDSC();
                  } else if (!"Page".equals(pageOrTrailer.getName())) {
                     pageOrTrailer.generate(gen);

                     while(parser.hasNext()) {
                        DSCEvent event = parser.nextEvent();
                        event.generate(gen);
                     }

                     gen.flush();
                     return;
                  }
               }
            }
         }

         if (event.isDSCComment()) {
            pageOrTrailer = event.asDSCComment();
            if ("LanguageLevel".equals(pageOrTrailer.getName())) {
               DSCCommentLanguageLevel level = (DSCCommentLanguageLevel)pageOrTrailer;
               gen.setPSLevel(level.getLanguageLevel());
            }
         }

         event.generate(gen);
      }
   }

   private static void reportInvalidDSC() throws DSCException {
      throw new DSCException("File is not DSC-compliant: Unexpected end of file");
   }

   private static void registerSuppliedForms(ResourceTracker resTracker, Map formResources) throws IOException {
      if (formResources != null) {
         Iterator var2 = formResources.values().iterator();

         while(var2.hasNext()) {
            Object o = var2.next();
            PSImageFormResource form = (PSImageFormResource)o;
            resTracker.registerSuppliedResource(form);
         }

      }
   }

   private void generateForms(Map formResources, PSGenerator gen) throws IOException {
      if (formResources != null) {
         Iterator var3 = formResources.values().iterator();

         while(var3.hasNext()) {
            Object o = var3.next();
            PSImageFormResource form = (PSImageFormResource)o;
            this.generateFormForImage(gen, form);
         }

      }
   }

   private void generateFormForImage(PSGenerator gen, PSImageFormResource form) throws IOException {
      String uri = form.getImageURI();
      ImageManager manager = this.userAgent.getImageManager();
      ImageInfo info = null;

      try {
         ImageSessionContext sessionContext = this.userAgent.getImageSessionContext();
         info = manager.getImageInfo(uri, sessionContext);
         PSRenderingContext formContext = new PSRenderingContext(this.userAgent, gen, this.fontInfo, true);
         ImageHandlerRegistry imageHandlerRegistry = this.userAgent.getImageHandlerRegistry();
         ImageFlavor[] flavors = imageHandlerRegistry.getSupportedFlavors(formContext);
         Map hints = ImageUtil.getDefaultHints(sessionContext);
         Image img = manager.getImage(info, flavors, hints, sessionContext);
         ImageHandler basicHandler = imageHandlerRegistry.getHandler(formContext, img);
         if (basicHandler == null) {
            throw new UnsupportedOperationException("No ImageHandler available for image: " + img.getInfo() + " (" + img.getClass().getName() + ")");
         }

         if (!(basicHandler instanceof PSImageHandler)) {
            throw new IllegalStateException("ImageHandler implementation doesn't behave properly. It should have returned false in isCompatible(). Class: " + basicHandler.getClass().getName());
         }

         PSImageHandler handler = (PSImageHandler)basicHandler;
         if (log.isTraceEnabled()) {
            log.trace("Using ImageHandler: " + handler.getClass().getName());
         }

         handler.generateForm(formContext, img, form);
      } catch (ImageException var14) {
         ResourceEventProducer eventProducer = ResourceEventProducer.Provider.get(this.userAgent.getEventBroadcaster());
         eventProducer.imageError(this.resTracker, info != null ? info.toString() : uri, var14, (Locator)null);
      }

   }

   private class IncludeResourceListener implements DSCListener {
      private PSGenerator gen;

      public IncludeResourceListener(PSGenerator gen) {
         this.gen = gen;
      }

      public void processEvent(DSCEvent event, DSCParser parser) throws IOException, DSCException {
         if (event.isDSCComment() && event instanceof DSCCommentIncludeResource) {
            DSCCommentIncludeResource include = (DSCCommentIncludeResource)event;
            PSResource res = include.getResource();
            if (res.getType().equals("form")) {
               if (ResourceHandler.this.inlineFormResources.containsValue(res)) {
                  PSImageFormResource form = (PSImageFormResource)ResourceHandler.this.inlineFormResources.get(res);
                  this.gen.writeln("save");
                  ResourceHandler.this.generateFormForImage(this.gen, form);
                  boolean execformFound = false;
                  DSCEvent next = parser.nextEvent();
                  if (next.isLine()) {
                     PostScriptLine line = next.asLine();
                     if (line.getLine().endsWith(" execform")) {
                        line.generate(this.gen);
                        execformFound = true;
                     }
                  }

                  if (!execformFound) {
                     throw new IOException("Expected a PostScript line in the form: <form> execform");
                  }

                  this.gen.writeln("restore");
               }

               parser.next();
            }
         }

      }
   }
}
