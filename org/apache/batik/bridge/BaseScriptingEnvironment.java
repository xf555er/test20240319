package org.apache.batik.bridge;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.jar.Manifest;
import org.apache.batik.dom.AbstractElement;
import org.apache.batik.dom.events.AbstractEvent;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.util.XLinkSupport;
import org.apache.batik.script.Interpreter;
import org.apache.batik.script.InterpreterException;
import org.apache.batik.script.ScriptEventWrapper;
import org.apache.batik.util.ParsedURL;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.svg.EventListenerInitializer;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

public class BaseScriptingEnvironment {
   public static final String INLINE_SCRIPT_DESCRIPTION = "BaseScriptingEnvironment.constant.inline.script.description";
   public static final String EVENT_SCRIPT_DESCRIPTION = "BaseScriptingEnvironment.constant.event.script.description";
   protected static final String EVENT_NAME = "event";
   protected static final String ALTERNATE_EVENT_NAME = "evt";
   protected static final String APPLICATION_ECMASCRIPT = "application/ecmascript";
   protected BridgeContext bridgeContext;
   protected UserAgent userAgent;
   protected Document document;
   protected ParsedURL docPURL;
   protected Set languages = new HashSet();
   protected Interpreter interpreter;
   protected Map windowObjects = new HashMap();
   protected WeakHashMap executedScripts = new WeakHashMap();

   public static boolean isDynamicDocument(BridgeContext ctx, Document doc) {
      Element elt = doc.getDocumentElement();
      if (elt != null && "http://www.w3.org/2000/svg".equals(elt.getNamespaceURI())) {
         if (elt.getAttributeNS((String)null, "onabort").length() > 0) {
            return true;
         } else if (elt.getAttributeNS((String)null, "onerror").length() > 0) {
            return true;
         } else if (elt.getAttributeNS((String)null, "onresize").length() > 0) {
            return true;
         } else if (elt.getAttributeNS((String)null, "onunload").length() > 0) {
            return true;
         } else if (elt.getAttributeNS((String)null, "onscroll").length() > 0) {
            return true;
         } else {
            return elt.getAttributeNS((String)null, "onzoom").length() > 0 ? true : isDynamicElement(ctx, doc.getDocumentElement());
         }
      } else {
         return false;
      }
   }

   public static boolean isDynamicElement(BridgeContext ctx, Element elt) {
      List bridgeExtensions = ctx.getBridgeExtensions(elt.getOwnerDocument());
      return isDynamicElement(elt, ctx, bridgeExtensions);
   }

   public static boolean isDynamicElement(Element elt, BridgeContext ctx, List bridgeExtensions) {
      Iterator var3 = bridgeExtensions.iterator();

      BridgeExtension bridgeExtension;
      do {
         if (!var3.hasNext()) {
            if ("http://www.w3.org/2000/svg".equals(elt.getNamespaceURI())) {
               if (elt.getAttributeNS((String)null, "onkeyup").length() > 0) {
                  return true;
               }

               if (elt.getAttributeNS((String)null, "onkeydown").length() > 0) {
                  return true;
               }

               if (elt.getAttributeNS((String)null, "onkeypress").length() > 0) {
                  return true;
               }

               if (elt.getAttributeNS((String)null, "onload").length() > 0) {
                  return true;
               }

               if (elt.getAttributeNS((String)null, "onerror").length() > 0) {
                  return true;
               }

               if (elt.getAttributeNS((String)null, "onactivate").length() > 0) {
                  return true;
               }

               if (elt.getAttributeNS((String)null, "onclick").length() > 0) {
                  return true;
               }

               if (elt.getAttributeNS((String)null, "onfocusin").length() > 0) {
                  return true;
               }

               if (elt.getAttributeNS((String)null, "onfocusout").length() > 0) {
                  return true;
               }

               if (elt.getAttributeNS((String)null, "onmousedown").length() > 0) {
                  return true;
               }

               if (elt.getAttributeNS((String)null, "onmousemove").length() > 0) {
                  return true;
               }

               if (elt.getAttributeNS((String)null, "onmouseout").length() > 0) {
                  return true;
               }

               if (elt.getAttributeNS((String)null, "onmouseover").length() > 0) {
                  return true;
               }

               if (elt.getAttributeNS((String)null, "onmouseup").length() > 0) {
                  return true;
               }
            }

            for(Node n = elt.getFirstChild(); n != null; n = n.getNextSibling()) {
               if (n.getNodeType() == 1 && isDynamicElement(ctx, (Element)n)) {
                  return true;
               }
            }

            return false;
         }

         Object bridgeExtension1 = var3.next();
         bridgeExtension = (BridgeExtension)bridgeExtension1;
      } while(!bridgeExtension.isDynamicElement(elt));

      return true;
   }

   public BaseScriptingEnvironment(BridgeContext ctx) {
      this.bridgeContext = ctx;
      this.document = ctx.getDocument();
      this.docPURL = new ParsedURL(((SVGDocument)this.document).getURL());
      this.userAgent = this.bridgeContext.getUserAgent();
   }

   public org.apache.batik.bridge.Window getWindow(Interpreter interp, String lang) {
      org.apache.batik.bridge.Window w = (org.apache.batik.bridge.Window)this.windowObjects.get(interp);
      if (w == null) {
         w = interp == null ? new Window((Interpreter)null, (String)null) : this.createWindow(interp, lang);
         this.windowObjects.put(interp, w);
      }

      return (org.apache.batik.bridge.Window)w;
   }

   public org.apache.batik.bridge.Window getWindow() {
      return this.getWindow((Interpreter)null, (String)null);
   }

   protected org.apache.batik.bridge.Window createWindow(Interpreter interp, String lang) {
      return new Window(interp, lang);
   }

   public Interpreter getInterpreter() {
      if (this.interpreter != null) {
         return this.interpreter;
      } else {
         SVGSVGElement root = (SVGSVGElement)this.document.getDocumentElement();
         String lang = root.getContentScriptType();
         return this.getInterpreter(lang);
      }
   }

   public Interpreter getInterpreter(String lang) {
      this.interpreter = this.bridgeContext.getInterpreter(lang);
      if (this.interpreter == null) {
         if (this.languages.contains(lang)) {
            return null;
         } else {
            this.languages.add(lang);
            return null;
         }
      } else {
         if (!this.languages.contains(lang)) {
            this.languages.add(lang);
            this.initializeEnvironment(this.interpreter, lang);
         }

         return this.interpreter;
      }
   }

   public void initializeEnvironment(Interpreter interp, String lang) {
      interp.bindObject("window", this.getWindow(interp, lang));
   }

   public void loadScripts() {
      NodeList scripts = this.document.getElementsByTagNameNS("http://www.w3.org/2000/svg", "script");
      int len = scripts.getLength();

      for(int i = 0; i < len; ++i) {
         AbstractElement script = (AbstractElement)scripts.item(i);
         this.loadScript(script);
      }

   }

   protected void loadScript(AbstractElement script) {
      if (!this.executedScripts.containsKey(script)) {
         Node n = script;

         do {
            n = ((Node)n).getParentNode();
            if (n == null) {
               return;
            }
         } while(((Node)n).getNodeType() != 9);

         String type = script.getAttributeNS((String)null, "type");
         if (type.length() == 0) {
            type = "text/ecmascript";
         }

         String mediaType;
         if (type.equals("application/java-archive")) {
            try {
               String href = XLinkSupport.getXLinkHref(script);
               ParsedURL purl = new ParsedURL(script.getBaseURI(), href);
               this.checkCompatibleScriptURL(type, purl);
               URL docURL = null;

               try {
                  docURL = new URL(this.docPURL.toString());
               } catch (MalformedURLException var14) {
               }

               DocumentJarClassLoader cll = new DocumentJarClassLoader(new URL(purl.toString()), docURL);
               URL url = cll.findResource("META-INF/MANIFEST.MF");
               if (url == null) {
                  return;
               }

               Manifest man = new Manifest(url.openStream());
               this.executedScripts.put(script, (Object)null);
               mediaType = man.getMainAttributes().getValue("Script-Handler");
               if (mediaType != null) {
                  ScriptHandler h = (ScriptHandler)cll.loadClass(mediaType).getDeclaredConstructor().newInstance();
                  h.run(this.document, this.getWindow());
               }

               mediaType = man.getMainAttributes().getValue("SVG-Handler-Class");
               if (mediaType != null) {
                  EventListenerInitializer initializer = (EventListenerInitializer)cll.loadClass(mediaType).getDeclaredConstructor().newInstance();
                  this.getWindow();
                  initializer.initializeEventListeners((SVGDocument)this.document);
               }
            } catch (Exception var16) {
               if (this.userAgent != null) {
                  this.userAgent.displayError(var16);
               }
            }

         } else {
            Interpreter interpreter = this.getInterpreter(type);
            if (interpreter != null) {
               try {
                  String href = XLinkSupport.getXLinkHref(script);
                  String desc = null;
                  Reader reader = null;
                  if (href.length() > 0) {
                     desc = href;
                     ParsedURL purl = new ParsedURL(script.getBaseURI(), href);
                     this.checkCompatibleScriptURL(type, purl);
                     InputStream is = purl.openStream();
                     mediaType = purl.getContentTypeMediaType();
                     String enc = purl.getContentTypeCharset();
                     if (enc != null) {
                        try {
                           reader = new InputStreamReader(is, enc);
                        } catch (UnsupportedEncodingException var15) {
                           enc = null;
                        }
                     }

                     if (reader == null) {
                        if ("application/ecmascript".equals(mediaType)) {
                           if (purl.hasContentTypeParameter("version")) {
                              return;
                           }

                           PushbackInputStream pbis = new PushbackInputStream(is, 8);
                           byte[] buf = new byte[4];
                           int read = pbis.read(buf);
                           if (read > 0) {
                              pbis.unread(buf, 0, read);
                              if (read >= 2) {
                                 if (buf[0] == -1 && buf[1] == -2) {
                                    if (read >= 4 && buf[2] == 0 && buf[3] == 0) {
                                       enc = "UTF32-LE";
                                       pbis.skip(4L);
                                    } else {
                                       enc = "UTF-16LE";
                                       pbis.skip(2L);
                                    }
                                 } else if (buf[0] == -2 && buf[1] == -1) {
                                    enc = "UTF-16BE";
                                    pbis.skip(2L);
                                 } else if (read >= 3 && buf[0] == -17 && buf[1] == -69 && buf[2] == -65) {
                                    enc = "UTF-8";
                                    pbis.skip(3L);
                                 } else if (read >= 4 && buf[0] == 0 && buf[1] == 0 && buf[2] == -2 && buf[3] == -1) {
                                    enc = "UTF-32BE";
                                    pbis.skip(4L);
                                 }
                              }

                              if (enc == null) {
                                 enc = "UTF-8";
                              }
                           }

                           reader = new InputStreamReader(pbis, enc);
                        } else {
                           reader = new InputStreamReader(is);
                        }
                     }
                  } else {
                     this.checkCompatibleScriptURL(type, this.docPURL);
                     DocumentLoader dl = this.bridgeContext.getDocumentLoader();
                     SVGDocument d = (SVGDocument)script.getOwnerDocument();
                     int line = dl.getLineNumber(script);
                     desc = Messages.formatMessage("BaseScriptingEnvironment.constant.inline.script.description", new Object[]{d.getURL(), "<" + script.getNodeName() + ">", line});
                     Node n = script.getFirstChild();
                     if (n == null) {
                        return;
                     }

                     StringBuffer sb = new StringBuffer();

                     while(true) {
                        if (n == null) {
                           reader = new StringReader(sb.toString());
                           break;
                        }

                        if (n.getNodeType() == 4 || n.getNodeType() == 3) {
                           sb.append(n.getNodeValue());
                        }

                        n = n.getNextSibling();
                     }
                  }

                  this.executedScripts.put(script, (Object)null);
                  interpreter.evaluate((Reader)reader, desc);
               } catch (IOException var17) {
                  if (this.userAgent != null) {
                     this.userAgent.displayError(var17);
                  }

                  return;
               } catch (InterpreterException var18) {
                  System.err.println("InterpExcept: " + var18);
                  this.handleInterpreterException(var18);
                  return;
               } catch (SecurityException var19) {
                  if (this.userAgent != null) {
                     this.userAgent.displayError(var19);
                  }
               }

            }
         }
      }
   }

   protected void checkCompatibleScriptURL(String scriptType, ParsedURL scriptPURL) {
      this.userAgent.checkLoadScript(scriptType, scriptPURL, this.docPURL);
   }

   public void dispatchSVGLoadEvent() {
      SVGSVGElement root = (SVGSVGElement)this.document.getDocumentElement();
      String lang = root.getContentScriptType();
      long documentStartTime = System.currentTimeMillis();
      this.bridgeContext.getAnimationEngine().start(documentStartTime);
      this.dispatchSVGLoad(root, true, lang);
   }

   protected void dispatchSVGLoad(Element elt, boolean checkCanRun, String lang) {
      for(Node n = elt.getFirstChild(); n != null; n = n.getNextSibling()) {
         if (n.getNodeType() == 1) {
            this.dispatchSVGLoad((Element)n, checkCanRun, lang);
         }
      }

      DocumentEvent de = (DocumentEvent)elt.getOwnerDocument();
      AbstractEvent ev = (AbstractEvent)de.createEvent("SVGEvents");
      String type;
      if (this.bridgeContext.isSVG12()) {
         type = "load";
      } else {
         type = "SVGLoad";
      }

      ev.initEventNS("http://www.w3.org/2001/xml-events", type, false, false);
      NodeEventTarget t = (NodeEventTarget)elt;
      final String s = elt.getAttributeNS((String)null, "onload");
      if (s.length() == 0) {
         t.dispatchEvent(ev);
      } else {
         final Interpreter interp = this.getInterpreter();
         if (interp == null) {
            t.dispatchEvent(ev);
         } else {
            if (checkCanRun) {
               this.checkCompatibleScriptURL(lang, this.docPURL);
               checkCanRun = false;
            }

            DocumentLoader dl = this.bridgeContext.getDocumentLoader();
            SVGDocument d = (SVGDocument)elt.getOwnerDocument();
            int line = dl.getLineNumber(elt);
            final String desc = Messages.formatMessage("BaseScriptingEnvironment.constant.event.script.description", new Object[]{d.getURL(), "onload", line});
            EventListener l = new EventListener() {
               public void handleEvent(Event evt) {
                  try {
                     Object event;
                     if (evt instanceof ScriptEventWrapper) {
                        event = ((ScriptEventWrapper)evt).getEventObject();
                     } else {
                        event = evt;
                     }

                     interp.bindObject("event", event);
                     interp.bindObject("evt", event);
                     interp.evaluate(new StringReader(s), desc);
                  } catch (IOException var3) {
                  } catch (InterpreterException var4) {
                     BaseScriptingEnvironment.this.handleInterpreterException(var4);
                  }

               }
            };
            t.addEventListenerNS("http://www.w3.org/2001/xml-events", type, l, false, (Object)null);
            t.dispatchEvent(ev);
            t.removeEventListenerNS("http://www.w3.org/2001/xml-events", type, l, false);
         }
      }
   }

   protected void dispatchSVGZoomEvent() {
      if (this.bridgeContext.isSVG12()) {
         this.dispatchSVGDocEvent("zoom");
      } else {
         this.dispatchSVGDocEvent("SVGZoom");
      }

   }

   protected void dispatchSVGScrollEvent() {
      if (this.bridgeContext.isSVG12()) {
         this.dispatchSVGDocEvent("scroll");
      } else {
         this.dispatchSVGDocEvent("SVGScroll");
      }

   }

   protected void dispatchSVGResizeEvent() {
      if (this.bridgeContext.isSVG12()) {
         this.dispatchSVGDocEvent("resize");
      } else {
         this.dispatchSVGDocEvent("SVGResize");
      }

   }

   protected void dispatchSVGDocEvent(String eventType) {
      SVGSVGElement root = (SVGSVGElement)this.document.getDocumentElement();
      DocumentEvent de = (DocumentEvent)this.document;
      AbstractEvent ev = (AbstractEvent)de.createEvent("SVGEvents");
      ev.initEventNS("http://www.w3.org/2001/xml-events", eventType, false, false);
      root.dispatchEvent(ev);
   }

   protected void handleInterpreterException(InterpreterException ie) {
      if (this.userAgent != null) {
         Exception ex = ie.getException();
         this.userAgent.displayError((Exception)(ex == null ? ie : ex));
      }

   }

   protected void handleSecurityException(SecurityException se) {
      if (this.userAgent != null) {
         this.userAgent.displayError(se);
      }

   }

   protected class Window implements org.apache.batik.bridge.Window {
      protected Interpreter interpreter;
      protected String language;

      public Window(Interpreter interp, String lang) {
         this.interpreter = interp;
         this.language = lang;
      }

      public Object setInterval(String script, long interval) {
         return null;
      }

      public Object setInterval(Runnable r, long interval) {
         return null;
      }

      public void clearInterval(Object interval) {
      }

      public Object setTimeout(String script, long timeout) {
         return null;
      }

      public Object setTimeout(Runnable r, long timeout) {
         return null;
      }

      public void clearTimeout(Object timeout) {
      }

      public Node parseXML(String text, Document doc) {
         return null;
      }

      public String printNode(Node n) {
         return null;
      }

      public void getURL(String uri, org.apache.batik.bridge.Window.URLResponseHandler h) {
         this.getURL(uri, h, "UTF8");
      }

      public void getURL(String uri, org.apache.batik.bridge.Window.URLResponseHandler h, String enc) {
      }

      public void postURL(String uri, String content, org.apache.batik.bridge.Window.URLResponseHandler h) {
         this.postURL(uri, content, h, "text/plain", (String)null);
      }

      public void postURL(String uri, String content, org.apache.batik.bridge.Window.URLResponseHandler h, String mimeType) {
         this.postURL(uri, content, h, mimeType, (String)null);
      }

      public void postURL(String uri, String content, org.apache.batik.bridge.Window.URLResponseHandler h, String mimeType, String fEnc) {
      }

      public void alert(String message) {
      }

      public boolean confirm(String message) {
         return false;
      }

      public String prompt(String message) {
         return null;
      }

      public String prompt(String message, String defVal) {
         return null;
      }

      public BridgeContext getBridgeContext() {
         return BaseScriptingEnvironment.this.bridgeContext;
      }

      public Interpreter getInterpreter() {
         return this.interpreter;
      }

      public org.apache.batik.w3c.dom.Location getLocation() {
         return null;
      }

      public org.apache.batik.w3c.dom.Window getParent() {
         return null;
      }
   }
}
