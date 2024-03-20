package org.apache.fop.render.intermediate;

import java.util.Locale;
import org.apache.fop.accessibility.DummyStructureTreeEventHandler;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.apps.FOUserAgent;

public abstract class AbstractIFDocumentHandler implements IFDocumentHandler {
   private final IFContext ifContext;

   protected AbstractIFDocumentHandler(IFContext context) {
      this.ifContext = context;
   }

   public IFContext getContext() {
      return this.ifContext;
   }

   public FOUserAgent getUserAgent() {
      return this.getContext().getUserAgent();
   }

   public StructureTreeEventHandler getStructureTreeEventHandler() {
      return DummyStructureTreeEventHandler.INSTANCE;
   }

   public IFDocumentNavigationHandler getDocumentNavigationHandler() {
      return null;
   }

   public void startDocument() throws IFException {
      if (this.getUserAgent() == null) {
         throw new IllegalStateException("User agent must be set before starting document generation");
      }
   }

   public void setDocumentLocale(Locale locale) {
   }

   public void startDocumentHeader() throws IFException {
   }

   public void endDocumentHeader() throws IFException {
   }

   public void startDocumentTrailer() throws IFException {
   }

   public void endDocumentTrailer() throws IFException {
   }

   public void startPageHeader() throws IFException {
   }

   public void endPageHeader() throws IFException {
   }

   public void startPageTrailer() throws IFException {
   }

   public void endPageTrailer() throws IFException {
   }
}
