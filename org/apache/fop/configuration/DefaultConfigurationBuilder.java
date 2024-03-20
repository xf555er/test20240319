package org.apache.fop.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class DefaultConfigurationBuilder {
   private static final Log LOG = LogFactory.getLog(DefaultConfigurationBuilder.class.getName());

   public DefaultConfiguration build(InputStream confStream) throws ConfigurationException {
      DefaultConfiguration var4;
      try {
         DocumentBuilder builder = DefaultConfiguration.DBF.newDocumentBuilder();
         Document document = builder.parse(confStream);
         var4 = new DefaultConfiguration(document.getDocumentElement());
      } catch (DOMException var16) {
         throw new ConfigurationException("xml parse error", var16);
      } catch (ParserConfigurationException var17) {
         throw new ConfigurationException("xml parse error", var17);
      } catch (IOException var18) {
         throw new ConfigurationException("xml parse error", var18);
      } catch (SAXException var19) {
         throw new ConfigurationException("xml parse error", var19);
      } finally {
         try {
            confStream.close();
         } catch (IOException var15) {
            throw new IllegalStateException(var15);
         }
      }

      return var4;
   }

   public DefaultConfiguration buildFromFile(File file) throws ConfigurationException {
      try {
         DocumentBuilder builder = DefaultConfiguration.DBF.newDocumentBuilder();
         Document document = builder.parse(file);
         return new DefaultConfiguration(document.getDocumentElement());
      } catch (DOMException var4) {
         throw new ConfigurationException("xml parse error", var4);
      } catch (ParserConfigurationException var5) {
         throw new ConfigurationException("xml parse error", var5);
      } catch (IOException var6) {
         throw new ConfigurationException("xml parse error", var6);
      } catch (SAXException var7) {
         throw new ConfigurationException("xml parse error", var7);
      }
   }
}
