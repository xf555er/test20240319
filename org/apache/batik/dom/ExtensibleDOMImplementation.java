package org.apache.batik.dom;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.css.parser.ExtendedParser;
import org.apache.batik.css.parser.ExtendedParserWrapper;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.Service;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.batik.xml.XMLUtilities;
import org.w3c.css.sac.Parser;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.css.DOMImplementationCSS;
import org.w3c.dom.css.ViewCSS;

public abstract class ExtensibleDOMImplementation extends AbstractDOMImplementation implements DOMImplementationCSS, StyleSheetFactory {
   protected DoublyIndexedTable customFactories;
   protected List customValueManagers;
   protected List customShorthandManagers;
   protected static List extensions = null;

   public ExtensibleDOMImplementation() {
      Iterator var1 = getDomExtensions().iterator();

      while(var1.hasNext()) {
         Object o = var1.next();
         DomExtension de = (DomExtension)o;
         de.registerTags(this);
      }

   }

   public void registerCustomElementFactory(String namespaceURI, String localName, ElementFactory factory) {
      if (this.customFactories == null) {
         this.customFactories = new DoublyIndexedTable();
      }

      this.customFactories.put(namespaceURI, localName, factory);
   }

   public void registerCustomCSSValueManager(ValueManager vm) {
      if (this.customValueManagers == null) {
         this.customValueManagers = new LinkedList();
      }

      this.customValueManagers.add(vm);
   }

   public void registerCustomCSSShorthandManager(ShorthandManager sm) {
      if (this.customShorthandManagers == null) {
         this.customShorthandManagers = new LinkedList();
      }

      this.customShorthandManagers.add(sm);
   }

   public CSSEngine createCSSEngine(AbstractStylableDocument doc, CSSContext ctx) {
      String pn = XMLResourceDescriptor.getCSSParserClassName();

      Parser p;
      try {
         p = (Parser)Class.forName(pn).getDeclaredConstructor().newInstance();
      } catch (ClassNotFoundException var10) {
         throw new DOMException((short)15, this.formatMessage("css.parser.class", new Object[]{pn}));
      } catch (InstantiationException var11) {
         throw new DOMException((short)15, this.formatMessage("css.parser.creation", new Object[]{pn}));
      } catch (IllegalAccessException var12) {
         throw new DOMException((short)15, this.formatMessage("css.parser.access", new Object[]{pn}));
      } catch (NoSuchMethodException var13) {
         throw new DOMException((short)15, this.formatMessage("css.parser.access", new Object[]{pn}));
      } catch (InvocationTargetException var14) {
         throw new DOMException((short)15, this.formatMessage("css.parser.access", new Object[]{pn}));
      }

      ExtendedParser ep = ExtendedParserWrapper.wrap(p);
      ValueManager[] vms;
      if (this.customValueManagers == null) {
         vms = new ValueManager[0];
      } else {
         vms = new ValueManager[this.customValueManagers.size()];
         Iterator it = this.customValueManagers.iterator();

         for(int i = 0; it.hasNext(); vms[i++] = (ValueManager)it.next()) {
         }
      }

      ShorthandManager[] sms;
      if (this.customShorthandManagers == null) {
         sms = new ShorthandManager[0];
      } else {
         sms = new ShorthandManager[this.customShorthandManagers.size()];
         Iterator it = this.customShorthandManagers.iterator();

         for(int i = 0; it.hasNext(); sms[i++] = (ShorthandManager)it.next()) {
         }
      }

      CSSEngine result = this.createCSSEngine(doc, ctx, ep, vms, sms);
      doc.setCSSEngine(result);
      return result;
   }

   public abstract CSSEngine createCSSEngine(AbstractStylableDocument var1, CSSContext var2, ExtendedParser var3, ValueManager[] var4, ShorthandManager[] var5);

   public abstract ViewCSS createViewCSS(AbstractStylableDocument var1);

   public Element createElementNS(AbstractDocument document, String namespaceURI, String qualifiedName) {
      if (namespaceURI != null && namespaceURI.length() == 0) {
         namespaceURI = null;
      }

      if (namespaceURI == null) {
         return new GenericElement(qualifiedName.intern(), document);
      } else {
         if (this.customFactories != null) {
            String name = DOMUtilities.getLocalName(qualifiedName);
            ElementFactory cef = (ElementFactory)this.customFactories.get(namespaceURI, name);
            if (cef != null) {
               return cef.create(DOMUtilities.getPrefix(qualifiedName), document);
            }
         }

         return new GenericElementNS(namespaceURI.intern(), qualifiedName.intern(), document);
      }
   }

   public DocumentType createDocumentType(String qualifiedName, String publicId, String systemId) {
      if (qualifiedName == null) {
         qualifiedName = "";
      }

      int test = XMLUtilities.testXMLQName(qualifiedName);
      if ((test & 1) == 0) {
         throw new DOMException((short)5, this.formatMessage("xml.name", new Object[]{qualifiedName}));
      } else if ((test & 2) == 0) {
         throw new DOMException((short)5, this.formatMessage("invalid.qname", new Object[]{qualifiedName}));
      } else {
         return new GenericDocumentType(qualifiedName, publicId, systemId);
      }
   }

   protected static synchronized List getDomExtensions() {
      if (extensions != null) {
         return extensions;
      } else {
         extensions = new LinkedList();
         Iterator iter = Service.providers(DomExtension.class);

         while(true) {
            label26:
            while(iter.hasNext()) {
               DomExtension de = (DomExtension)iter.next();
               float priority = de.getPriority();
               ListIterator li = extensions.listIterator();

               DomExtension lde;
               do {
                  if (!li.hasNext()) {
                     li.add(de);
                     continue label26;
                  }

                  lde = (DomExtension)li.next();
               } while(!(lde.getPriority() > priority));

               li.previous();
               li.add(de);
            }

            return extensions;
         }
      }
   }

   public interface ElementFactory {
      Element create(String var1, Document var2);
   }
}
