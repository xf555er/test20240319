package org.apache.fop.configuration;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DefaultConfiguration implements Configuration {
   static final DocumentBuilderFactory DBF = DocumentBuilderFactory.newInstance();
   private Element element;

   /** @deprecated */
   public static String toString(Document document) {
      try {
         Transformer transformer = TransformerFactory.newInstance().newTransformer();
         transformer.setOutputProperty("indent", "yes");
         transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
         StreamResult result = new StreamResult(new StringWriter());
         DOMSource source = new DOMSource(document);
         transformer.transform(source, result);
         return result.getWriter().toString();
      } catch (TransformerException var4) {
         throw new IllegalStateException(var4);
      }
   }

   public DefaultConfiguration(String key) {
      DocumentBuilder builder = null;

      try {
         builder = DBF.newDocumentBuilder();
      } catch (ParserConfigurationException var4) {
         var4.printStackTrace();
         throw new IllegalStateException(var4);
      }

      Document doc = builder.newDocument();
      this.element = doc.createElement(key);
      doc.appendChild(this.element);
   }

   DefaultConfiguration(Element element) {
      this.element = element;
   }

   Element getElement() {
      return this.element;
   }

   public void addChild(DefaultConfiguration configuration) {
      Element node = (Element)this.element.getOwnerDocument().importNode(configuration.getElement(), true);
      this.element.appendChild(node);
   }

   String getValue0() {
      String result = this.element.getTextContent();
      if (result == null) {
         result = "";
      }

      return result;
   }

   public Configuration getChild(String key) {
      NodeList nl = this.element.getChildNodes();

      for(int i = 0; i < nl.getLength(); ++i) {
         Node n = nl.item(i);
         if (n.getNodeName().equals(key)) {
            return new DefaultConfiguration((Element)n);
         }
      }

      return NullConfiguration.INSTANCE;
   }

   public Configuration getChild(String key, boolean required) {
      Configuration result = this.getChild(key);
      if (!required && result == NullConfiguration.INSTANCE) {
         return null;
      } else {
         return (Configuration)(!required || result != null && result != NullConfiguration.INSTANCE ? result : NullConfiguration.INSTANCE);
      }
   }

   public Configuration[] getChildren(String key) {
      ArrayList result = new ArrayList(1);
      NodeList nl = this.element.getChildNodes();

      for(int i = 0; i < nl.getLength(); ++i) {
         Node n = nl.item(i);
         if (n.getNodeName().equals(key)) {
            result.add(new DefaultConfiguration((Element)n));
         }
      }

      return (Configuration[])result.toArray(new Configuration[0]);
   }

   public String[] getAttributeNames() {
      NamedNodeMap nnm = this.element.getAttributes();
      String[] result = new String[nnm.getLength()];

      for(int i = 0; i < nnm.getLength(); ++i) {
         Node n = nnm.item(i);
         result[i] = n.getNodeName();
      }

      return result;
   }

   public String getAttribute(String key) {
      String result = this.element.getAttribute(key);
      if ("".equals(result)) {
         result = null;
      }

      return result;
   }

   public String getAttribute(String key, String defaultValue) {
      String result = this.getAttribute(key);
      if (result == null || "".equals(result)) {
         result = defaultValue;
      }

      return result;
   }

   public boolean getAttributeAsBoolean(String key, boolean defaultValue) {
      String result = this.getAttribute(key);
      if (result != null && !"".equals(result)) {
         return "true".equalsIgnoreCase(result) || "yes".equalsIgnoreCase(result);
      } else {
         return defaultValue;
      }
   }

   public float getAttributeAsFloat(String key) throws ConfigurationException {
      return Float.parseFloat(this.getAttribute(key));
   }

   public float getAttributeAsFloat(String key, float defaultValue) {
      String result = this.getAttribute(key);
      return result != null && !"".equals(result) ? Float.parseFloat(result) : defaultValue;
   }

   public int getAttributeAsInteger(String key, int defaultValue) {
      String result = this.getAttribute(key);
      return result != null && !"".equals(result) ? Integer.parseInt(result) : defaultValue;
   }

   public String getValue() throws ConfigurationException {
      String result = this.getValue0();
      if (result != null && !"".equals(result)) {
         return result;
      } else {
         throw new ConfigurationException("No value in " + this.element.getNodeName());
      }
   }

   public String getValue(String defaultValue) {
      String result = this.getValue0();
      if (result == null || "".equals(result)) {
         result = defaultValue;
      }

      return result;
   }

   public boolean getValueAsBoolean() throws ConfigurationException {
      return Boolean.parseBoolean(this.getValue0());
   }

   public boolean getValueAsBoolean(boolean defaultValue) {
      String result = this.getValue0().trim();
      return "".equals(result) ? defaultValue : Boolean.parseBoolean(result);
   }

   public int getValueAsInteger() throws ConfigurationException {
      try {
         return Integer.parseInt(this.getValue0());
      } catch (NumberFormatException var2) {
         throw new ConfigurationException("Not an integer", var2);
      }
   }

   public int getValueAsInteger(int defaultValue) {
      String result = this.getValue0();
      return result != null && !"".equals(result) ? Integer.parseInt(result) : defaultValue;
   }

   public float getValueAsFloat() throws ConfigurationException {
      try {
         return Float.parseFloat(this.getValue0());
      } catch (NumberFormatException var2) {
         throw new ConfigurationException("Not a float", var2);
      }
   }

   public float getValueAsFloat(float defaultValue) {
      String result = this.getValue0();
      return result != null && !"".equals(result) ? Float.parseFloat(this.getValue0()) : defaultValue;
   }

   public String getLocation() {
      List path = new ArrayList();

      for(Node el = this.element; el != null; el = ((Node)el).getParentNode()) {
         if (el instanceof Element) {
            path.add(((Element)el).getTagName());
         }
      }

      Collections.reverse(path);
      StringBuilder sb = new StringBuilder();

      String s;
      for(Iterator var3 = path.iterator(); var3.hasNext(); sb.append(s)) {
         s = (String)var3.next();
         if (sb.length() > 0) {
            sb.append("/");
         }
      }

      return sb.toString();
   }

   static {
      DBF.setNamespaceAware(false);
      DBF.setValidating(false);
      DBF.setIgnoringComments(true);
      DBF.setIgnoringElementContentWhitespace(true);
      DBF.setExpandEntityReferences(true);
   }
}
