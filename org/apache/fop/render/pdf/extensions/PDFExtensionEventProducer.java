package org.apache.fop.render.pdf.extensions;

import org.apache.fop.events.EventProducer;
import org.xml.sax.Locator;

public interface PDFExtensionEventProducer extends EventProducer {
   void reservedKeyword(Object var1, Locator var2, String var3);
}
