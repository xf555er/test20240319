package org.apache.fop.area;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.fonts.FontInfo;
import org.apache.xmlgraphics.io.TempResourceURIGenerator;
import org.xml.sax.SAXException;

public class CachedRenderPagesModel extends RenderPagesModel {
   private Map pageMap = new HashMap();
   private final URI tempBaseURI;
   private static final TempResourceURIGenerator TEMP_URI_GENERATOR = new TempResourceURIGenerator("cached-pages");

   public CachedRenderPagesModel(FOUserAgent userAgent, String outputFormat, FontInfo fontInfo, OutputStream stream) throws FOPException {
      super(userAgent, outputFormat, fontInfo, stream);
      this.tempBaseURI = TEMP_URI_GENERATOR.generate();
   }

   protected boolean checkPreparedPages(PageViewport newpage, boolean renderUnresolved) {
      Iterator iter = this.prepared.iterator();

      while(iter.hasNext()) {
         PageViewport pageViewport = (PageViewport)iter.next();
         if (!pageViewport.isResolved() && !renderUnresolved) {
            if (!this.renderer.supportsOutOfOrder()) {
               break;
            }
         } else {
            if (pageViewport != newpage) {
               try {
                  URI tempURI = (URI)this.pageMap.get(pageViewport);
                  log.debug("Loading page from: " + tempURI);
                  InputStream inStream = this.renderer.getUserAgent().getResourceResolver().getResource(tempURI);
                  ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(inStream));

                  try {
                     pageViewport.loadPage(in);
                  } finally {
                     IOUtils.closeQuietly((InputStream)inStream);
                     IOUtils.closeQuietly((InputStream)in);
                  }

                  this.pageMap.remove(pageViewport);
               } catch (Exception var12) {
                  AreaEventProducer eventProducer = AreaEventProducer.Provider.get(this.renderer.getUserAgent().getEventBroadcaster());
                  eventProducer.pageLoadError(this, pageViewport.getPageNumberString(), var12);
               }
            }

            this.renderPage(pageViewport);
            pageViewport.clear();
            iter.remove();
         }
      }

      if (newpage != null && newpage.getPage() != null) {
         this.savePage(newpage);
         newpage.clear();
      }

      return this.renderer.supportsOutOfOrder() || this.prepared.isEmpty();
   }

   protected void savePage(PageViewport page) {
      try {
         String fname = "/fop-page-" + page.getPageIndex() + ".ser";
         URI tempURI = URI.create(this.tempBaseURI + fname);
         OutputStream outStream = this.renderer.getUserAgent().getResourceResolver().getOutputStream(tempURI);
         ObjectOutputStream tempstream = new ObjectOutputStream(new BufferedOutputStream(outStream));

         try {
            page.savePage(tempstream);
         } finally {
            IOUtils.closeQuietly((OutputStream)tempstream);
         }

         this.pageMap.put(page, tempURI);
         if (log.isDebugEnabled()) {
            log.debug("Page saved to temporary file: " + tempURI);
         }
      } catch (IOException var10) {
         AreaEventProducer eventProducer = AreaEventProducer.Provider.get(this.renderer.getUserAgent().getEventBroadcaster());
         eventProducer.pageSaveError(this, page.getPageNumberString(), var10);
      }

   }

   public void endDocument() throws SAXException {
      super.endDocument();
   }
}
