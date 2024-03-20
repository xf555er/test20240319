package org.apache.fop.render.java2d;

import java.awt.Dimension;
import java.util.Locale;
import javax.xml.transform.Result;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.intermediate.IFContext;
import org.apache.fop.render.intermediate.IFDocumentHandler;
import org.apache.fop.render.intermediate.IFDocumentHandlerConfigurator;
import org.apache.fop.render.intermediate.IFDocumentNavigationHandler;
import org.apache.fop.render.intermediate.IFException;
import org.apache.fop.render.intermediate.IFPainter;

public class Java2DDocumentHandler implements IFDocumentHandler {
   public void setContext(IFContext context) {
      throw new UnsupportedOperationException();
   }

   public IFContext getContext() {
      throw new UnsupportedOperationException();
   }

   public void setResult(Result result) throws IFException {
      throw new UnsupportedOperationException();
   }

   public void setFontInfo(FontInfo fontInfo) {
      throw new UnsupportedOperationException();
   }

   public FontInfo getFontInfo() {
      throw new UnsupportedOperationException();
   }

   public void setDefaultFontInfo(FontInfo fontInfo) {
      throw new UnsupportedOperationException();
   }

   public IFDocumentHandlerConfigurator getConfigurator() {
      throw new UnsupportedOperationException();
   }

   public StructureTreeEventHandler getStructureTreeEventHandler() {
      throw new UnsupportedOperationException();
   }

   public IFDocumentNavigationHandler getDocumentNavigationHandler() {
      throw new UnsupportedOperationException();
   }

   public boolean supportsPagesOutOfOrder() {
      throw new UnsupportedOperationException();
   }

   public String getMimeType() {
      throw new UnsupportedOperationException();
   }

   public void startDocument() throws IFException {
      throw new UnsupportedOperationException();
   }

   public void endDocument() throws IFException {
      throw new UnsupportedOperationException();
   }

   public void setDocumentLocale(Locale locale) {
      throw new UnsupportedOperationException();
   }

   public void startDocumentHeader() throws IFException {
      throw new UnsupportedOperationException();
   }

   public void endDocumentHeader() throws IFException {
      throw new UnsupportedOperationException();
   }

   public void startDocumentTrailer() throws IFException {
      throw new UnsupportedOperationException();
   }

   public void endDocumentTrailer() throws IFException {
      throw new UnsupportedOperationException();
   }

   public void startPageSequence(String id) throws IFException {
      throw new UnsupportedOperationException();
   }

   public void endPageSequence() throws IFException {
      throw new UnsupportedOperationException();
   }

   public void startPage(int index, String name, String pageMasterName, Dimension size) throws IFException {
      throw new UnsupportedOperationException();
   }

   public void endPage() throws IFException {
      throw new UnsupportedOperationException();
   }

   public void startPageHeader() throws IFException {
      throw new UnsupportedOperationException();
   }

   public void endPageHeader() throws IFException {
      throw new UnsupportedOperationException();
   }

   public IFPainter startPageContent() throws IFException {
      throw new UnsupportedOperationException();
   }

   public void endPageContent() throws IFException {
      throw new UnsupportedOperationException();
   }

   public void startPageTrailer() throws IFException {
      throw new UnsupportedOperationException();
   }

   public void endPageTrailer() throws IFException {
      throw new UnsupportedOperationException();
   }

   public void handleExtensionObject(Object extension) throws IFException {
      throw new UnsupportedOperationException();
   }
}
