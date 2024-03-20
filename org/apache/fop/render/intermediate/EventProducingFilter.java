package org.apache.fop.render.intermediate;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.RendererEventProducer;
import org.apache.fop.render.intermediate.util.IFDocumentHandlerProxy;

public class EventProducingFilter extends IFDocumentHandlerProxy {
   private int pageNumberEnded;
   private FOUserAgent userAgent;

   public EventProducingFilter(IFDocumentHandler ifDocumentHandler, FOUserAgent userAgent) {
      super(ifDocumentHandler);
      this.userAgent = userAgent;
   }

   public void endPage() throws IFException {
      super.endPage();
      ++this.pageNumberEnded;
      RendererEventProducer.Provider.get(this.userAgent.getEventBroadcaster()).endPage(this, this.pageNumberEnded);
   }
}
