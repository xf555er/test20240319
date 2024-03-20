package org.apache.fop.render.intermediate;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.IOUtils;
import org.apache.fop.fonts.FontCollection;
import org.apache.fop.fonts.FontEventAdapter;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.fonts.FontManager;
import org.apache.fop.fonts.base14.Base14FontCollection;

public abstract class AbstractBinaryWritingIFDocumentHandler extends AbstractIFDocumentHandler {
   protected OutputStream outputStream;
   private boolean ownOutputStream;
   protected FontInfo fontInfo;

   public AbstractBinaryWritingIFDocumentHandler(IFContext ifContext) {
      super(ifContext);
   }

   public void setResult(Result result) throws IFException {
      if (result instanceof StreamResult) {
         StreamResult streamResult = (StreamResult)result;
         OutputStream out = streamResult.getOutputStream();
         if (out == null) {
            if (streamResult.getWriter() != null) {
               throw new IllegalArgumentException("FOP cannot use a Writer. Please supply an OutputStream!");
            }

            try {
               URI resultURI = URI.create(streamResult.getSystemId());
               out = new BufferedOutputStream(this.getUserAgent().getResourceResolver().getOutputStream(resultURI));
            } catch (IOException var5) {
               throw new IFException("I/O error while opening output stream", var5);
            }

            this.ownOutputStream = true;
         }

         this.outputStream = (OutputStream)out;
      } else {
         throw new UnsupportedOperationException("Unsupported Result subclass: " + result.getClass().getName());
      }
   }

   public FontInfo getFontInfo() {
      return this.fontInfo;
   }

   public void setFontInfo(FontInfo fontInfo) {
      this.fontInfo = fontInfo;
   }

   public void setDefaultFontInfo(FontInfo fontInfo) {
      FontManager fontManager = this.getUserAgent().getFontManager();
      FontCollection[] fontCollections = new FontCollection[]{new Base14FontCollection(fontManager.isBase14KerningEnabled())};
      FontInfo fi = fontInfo != null ? fontInfo : new FontInfo();
      fi.setEventListener(new FontEventAdapter(this.getUserAgent().getEventBroadcaster()));
      fontManager.setup(fi, fontCollections);
      this.setFontInfo(fi);
   }

   public void startDocument() throws IFException {
      super.startDocument();
      if (this.outputStream == null) {
         throw new IllegalStateException("OutputStream hasn't been set through setResult()");
      }
   }

   public void endDocument() throws IFException {
      if (this.ownOutputStream) {
         IOUtils.closeQuietly(this.outputStream);
         this.outputStream = null;
      }

   }
}
