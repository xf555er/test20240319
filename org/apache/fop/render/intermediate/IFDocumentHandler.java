package org.apache.fop.render.intermediate;

import java.awt.Dimension;
import java.util.Locale;
import javax.xml.transform.Result;
import org.apache.fop.accessibility.StructureTreeEventHandler;
import org.apache.fop.fonts.FontInfo;

public interface IFDocumentHandler {
   IFContext getContext();

   void setResult(Result var1) throws IFException;

   void setFontInfo(FontInfo var1);

   FontInfo getFontInfo();

   void setDefaultFontInfo(FontInfo var1);

   IFDocumentHandlerConfigurator getConfigurator();

   StructureTreeEventHandler getStructureTreeEventHandler();

   IFDocumentNavigationHandler getDocumentNavigationHandler();

   boolean supportsPagesOutOfOrder();

   String getMimeType();

   void startDocument() throws IFException;

   void endDocument() throws IFException;

   void setDocumentLocale(Locale var1);

   void startDocumentHeader() throws IFException;

   void endDocumentHeader() throws IFException;

   void startDocumentTrailer() throws IFException;

   void endDocumentTrailer() throws IFException;

   void startPageSequence(String var1) throws IFException;

   void endPageSequence() throws IFException;

   void startPage(int var1, String var2, String var3, Dimension var4) throws IFException;

   void endPage() throws IFException;

   void startPageHeader() throws IFException;

   void endPageHeader() throws IFException;

   IFPainter startPageContent() throws IFException;

   void endPageContent() throws IFException;

   void startPageTrailer() throws IFException;

   void endPageTrailer() throws IFException;

   void handleExtensionObject(Object var1) throws IFException;
}
