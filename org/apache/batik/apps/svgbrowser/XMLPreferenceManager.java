package org.apache.batik.apps.svgbrowser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.dom.util.DocumentFactory;
import org.apache.batik.dom.util.SAXDocumentFactory;
import org.apache.batik.util.PreferenceManager;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XMLPreferenceManager extends PreferenceManager {
   protected String xmlParserClassName;
   public static final String PREFERENCE_ENCODING = "8859_1";

   public XMLPreferenceManager(String prefFileName) {
      this(prefFileName, (Map)null, XMLResourceDescriptor.getXMLParserClassName());
   }

   public XMLPreferenceManager(String prefFileName, Map defaults) {
      this(prefFileName, defaults, XMLResourceDescriptor.getXMLParserClassName());
   }

   public XMLPreferenceManager(String prefFileName, String parser) {
      this(prefFileName, (Map)null, parser);
   }

   public XMLPreferenceManager(String prefFileName, Map defaults, String parser) {
      super(prefFileName, defaults);
      this.internal = new XMLProperties();
      this.xmlParserClassName = parser;
   }

   protected class XMLProperties extends Properties {
      public synchronized void load(InputStream is) throws IOException {
         BufferedReader r = new BufferedReader(new InputStreamReader(is, "8859_1"));
         DocumentFactory df = new SAXDocumentFactory(GenericDOMImplementation.getDOMImplementation(), XMLPreferenceManager.this.xmlParserClassName);
         Document doc = df.createDocument("http://xml.apache.org/batik/preferences", "preferences", (String)null, (Reader)r);
         Element elt = doc.getDocumentElement();

         for(Node n = elt.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == 1 && n.getNodeName().equals("property")) {
               String name = ((Element)n).getAttributeNS((String)null, "name");
               StringBuffer cont = new StringBuffer();

               for(Node c = n.getFirstChild(); c != null && c.getNodeType() == 3; c = c.getNextSibling()) {
                  cont.append(c.getNodeValue());
               }

               String val = cont.toString();
               this.put(name, val);
            }
         }

      }

      public synchronized void store(OutputStream os, String header) throws IOException {
         BufferedWriter w = new BufferedWriter(new OutputStreamWriter(os, "8859_1"));
         Map m = new HashMap();
         this.enumerate(m);
         w.write("<preferences xmlns=\"http://xml.apache.org/batik/preferences\">\n");

         for(Iterator var5 = m.keySet().iterator(); var5.hasNext(); w.write("</property>\n")) {
            Object o = var5.next();
            String n = (String)o;
            String v = (String)m.get(n);
            w.write("<property name=\"" + n + "\">");

            try {
               w.write(DOMUtilities.contentToString(v, false));
            } catch (IOException var10) {
            }
         }

         w.write("</preferences>\n");
         w.flush();
      }

      private synchronized void enumerate(Map m) {
         Iterator var2;
         Object k;
         if (this.defaults != null) {
            var2 = m.keySet().iterator();

            while(var2.hasNext()) {
               k = var2.next();
               m.put(k, this.defaults.get(k));
            }
         }

         var2 = this.keySet().iterator();

         while(var2.hasNext()) {
            k = var2.next();
            m.put(k, this.get(k));
         }

      }
   }
}
