package org.apache.batik.swing;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.constants.XMLConstants;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.swing.gvt.AbstractImageZoomInteractor;
import org.apache.batik.swing.gvt.AbstractPanInteractor;
import org.apache.batik.swing.gvt.AbstractResetTransformInteractor;
import org.apache.batik.swing.gvt.AbstractRotateInteractor;
import org.apache.batik.swing.gvt.AbstractZoomInteractor;
import org.apache.batik.swing.gvt.Interactor;
import org.apache.batik.swing.gvt.JGVTComponent;
import org.apache.batik.swing.svg.JSVGComponent;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGUserAgent;
import org.apache.batik.util.gui.JErrorPane;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.svg.SVGDocument;

public class JSVGCanvas extends JSVGComponent {
   public static final String SCROLL_RIGHT_ACTION = "ScrollRight";
   public static final String SCROLL_LEFT_ACTION = "ScrollLeft";
   public static final String SCROLL_UP_ACTION = "ScrollUp";
   public static final String SCROLL_DOWN_ACTION = "ScrollDown";
   public static final String FAST_SCROLL_RIGHT_ACTION = "FastScrollRight";
   public static final String FAST_SCROLL_LEFT_ACTION = "FastScrollLeft";
   public static final String FAST_SCROLL_UP_ACTION = "FastScrollUp";
   public static final String FAST_SCROLL_DOWN_ACTION = "FastScrollDown";
   public static final String ZOOM_IN_ACTION = "ZoomIn";
   public static final String ZOOM_OUT_ACTION = "ZoomOut";
   public static final String RESET_TRANSFORM_ACTION = "ResetTransform";
   private boolean isZoomInteractorEnabled;
   private boolean isImageZoomInteractorEnabled;
   private boolean isPanInteractorEnabled;
   private boolean isRotateInteractorEnabled;
   private boolean isResetTransformInteractorEnabled;
   protected PropertyChangeSupport pcs;
   protected String uri;
   protected LocationListener locationListener;
   protected Map toolTipMap;
   protected EventListener toolTipListener;
   protected EventTarget lastTarget;
   protected Map toolTipDocs;
   protected static final Object MAP_TOKEN = new Object();
   protected long lastToolTipEventTimeStamp;
   protected EventTarget lastToolTipEventTarget;
   protected Interactor zoomInteractor;
   protected Interactor imageZoomInteractor;
   protected Interactor panInteractor;
   protected Interactor rotateInteractor;
   protected Interactor resetTransformInteractor;

   public JSVGCanvas() {
      this((SVGUserAgent)null, true, true);
      this.addMouseMotionListener(this.locationListener);
   }

   public JSVGCanvas(SVGUserAgent ua, boolean eventsEnabled, boolean selectableText) {
      super(ua, eventsEnabled, selectableText);
      this.isZoomInteractorEnabled = true;
      this.isImageZoomInteractorEnabled = true;
      this.isPanInteractorEnabled = true;
      this.isRotateInteractorEnabled = true;
      this.isResetTransformInteractorEnabled = true;
      this.pcs = new PropertyChangeSupport(this);
      this.locationListener = new LocationListener();
      this.toolTipMap = null;
      this.toolTipListener = new ToolTipModifier();
      this.lastTarget = null;
      this.toolTipDocs = null;
      this.zoomInteractor = new AbstractZoomInteractor() {
         public boolean startInteraction(InputEvent ie) {
            int mods = ie.getModifiersEx();
            return ie.getID() == 501 && (mods & 1024) != 0 && (mods & 128) != 0;
         }
      };
      this.imageZoomInteractor = new AbstractImageZoomInteractor() {
         public boolean startInteraction(InputEvent ie) {
            int mods = ie.getModifiersEx();
            return ie.getID() == 501 && (mods & 4096) != 0 && (mods & 64) != 0;
         }
      };
      this.panInteractor = new AbstractPanInteractor() {
         public boolean startInteraction(InputEvent ie) {
            int mods = ie.getModifiersEx();
            return ie.getID() == 501 && (mods & 1024) != 0 && (mods & 64) != 0;
         }
      };
      this.rotateInteractor = new AbstractRotateInteractor() {
         public boolean startInteraction(InputEvent ie) {
            int mods = ie.getModifiersEx();
            return ie.getID() == 501 && (mods & 4096) != 0 && (mods & 128) != 0;
         }
      };
      this.resetTransformInteractor = new AbstractResetTransformInteractor() {
         public boolean startInteraction(InputEvent ie) {
            int mods = ie.getModifiersEx();
            return ie.getID() == 500 && (mods & 4096) != 0 && (mods & 64) != 0 && (mods & 128) != 0;
         }
      };
      this.setPreferredSize(new Dimension(200, 200));
      this.setMinimumSize(new Dimension(100, 100));
      List intl = this.getInteractors();
      intl.add(this.zoomInteractor);
      intl.add(this.imageZoomInteractor);
      intl.add(this.panInteractor);
      intl.add(this.rotateInteractor);
      intl.add(this.resetTransformInteractor);
      this.installActions();
      if (eventsEnabled) {
         this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
               JSVGCanvas.this.requestFocus();
            }
         });
         this.installKeyboardActions();
      }

      this.addMouseMotionListener(this.locationListener);
   }

   protected void installActions() {
      ActionMap actionMap = this.getActionMap();
      actionMap.put("ScrollRight", new ScrollRightAction(10));
      actionMap.put("ScrollLeft", new ScrollLeftAction(10));
      actionMap.put("ScrollUp", new ScrollUpAction(10));
      actionMap.put("ScrollDown", new ScrollDownAction(10));
      actionMap.put("FastScrollRight", new ScrollRightAction(30));
      actionMap.put("FastScrollLeft", new ScrollLeftAction(30));
      actionMap.put("FastScrollUp", new ScrollUpAction(30));
      actionMap.put("FastScrollDown", new ScrollDownAction(30));
      actionMap.put("ZoomIn", new ZoomInAction());
      actionMap.put("ZoomOut", new ZoomOutAction());
      actionMap.put("ResetTransform", new ResetTransformAction());
   }

   public void setDisableInteractions(boolean b) {
      super.setDisableInteractions(b);
      ActionMap actionMap = this.getActionMap();
      actionMap.get("ScrollRight").setEnabled(!b);
      actionMap.get("ScrollLeft").setEnabled(!b);
      actionMap.get("ScrollUp").setEnabled(!b);
      actionMap.get("ScrollDown").setEnabled(!b);
      actionMap.get("FastScrollRight").setEnabled(!b);
      actionMap.get("FastScrollLeft").setEnabled(!b);
      actionMap.get("FastScrollUp").setEnabled(!b);
      actionMap.get("FastScrollDown").setEnabled(!b);
      actionMap.get("ZoomIn").setEnabled(!b);
      actionMap.get("ZoomOut").setEnabled(!b);
      actionMap.get("ResetTransform").setEnabled(!b);
   }

   protected void installKeyboardActions() {
      InputMap inputMap = this.getInputMap(0);
      KeyStroke key = KeyStroke.getKeyStroke(39, 0);
      inputMap.put(key, "ScrollRight");
      key = KeyStroke.getKeyStroke(37, 0);
      inputMap.put(key, "ScrollLeft");
      key = KeyStroke.getKeyStroke(38, 0);
      inputMap.put(key, "ScrollUp");
      key = KeyStroke.getKeyStroke(40, 0);
      inputMap.put(key, "ScrollDown");
      key = KeyStroke.getKeyStroke(39, 64);
      inputMap.put(key, "FastScrollRight");
      key = KeyStroke.getKeyStroke(37, 64);
      inputMap.put(key, "FastScrollLeft");
      key = KeyStroke.getKeyStroke(38, 64);
      inputMap.put(key, "FastScrollUp");
      key = KeyStroke.getKeyStroke(40, 64);
      inputMap.put(key, "FastScrollDown");
      key = KeyStroke.getKeyStroke(73, 128);
      inputMap.put(key, "ZoomIn");
      key = KeyStroke.getKeyStroke(79, 128);
      inputMap.put(key, "ZoomOut");
      key = KeyStroke.getKeyStroke(84, 128);
      inputMap.put(key, "ResetTransform");
   }

   public void addPropertyChangeListener(PropertyChangeListener pcl) {
      this.pcs.addPropertyChangeListener(pcl);
   }

   public void removePropertyChangeListener(PropertyChangeListener pcl) {
      this.pcs.removePropertyChangeListener(pcl);
   }

   public void addPropertyChangeListener(String propertyName, PropertyChangeListener pcl) {
      this.pcs.addPropertyChangeListener(propertyName, pcl);
   }

   public void removePropertyChangeListener(String propertyName, PropertyChangeListener pcl) {
      this.pcs.removePropertyChangeListener(propertyName, pcl);
   }

   public void setEnableZoomInteractor(boolean b) {
      if (this.isZoomInteractorEnabled != b) {
         boolean oldValue = this.isZoomInteractorEnabled;
         this.isZoomInteractorEnabled = b;
         if (this.isZoomInteractorEnabled) {
            this.getInteractors().add(this.zoomInteractor);
         } else {
            this.getInteractors().remove(this.zoomInteractor);
         }

         this.pcs.firePropertyChange("enableZoomInteractor", oldValue, b);
      }

   }

   public boolean getEnableZoomInteractor() {
      return this.isZoomInteractorEnabled;
   }

   public void setEnableImageZoomInteractor(boolean b) {
      if (this.isImageZoomInteractorEnabled != b) {
         boolean oldValue = this.isImageZoomInteractorEnabled;
         this.isImageZoomInteractorEnabled = b;
         if (this.isImageZoomInteractorEnabled) {
            this.getInteractors().add(this.imageZoomInteractor);
         } else {
            this.getInteractors().remove(this.imageZoomInteractor);
         }

         this.pcs.firePropertyChange("enableImageZoomInteractor", oldValue, b);
      }

   }

   public boolean getEnableImageZoomInteractor() {
      return this.isImageZoomInteractorEnabled;
   }

   public void setEnablePanInteractor(boolean b) {
      if (this.isPanInteractorEnabled != b) {
         boolean oldValue = this.isPanInteractorEnabled;
         this.isPanInteractorEnabled = b;
         if (this.isPanInteractorEnabled) {
            this.getInteractors().add(this.panInteractor);
         } else {
            this.getInteractors().remove(this.panInteractor);
         }

         this.pcs.firePropertyChange("enablePanInteractor", oldValue, b);
      }

   }

   public boolean getEnablePanInteractor() {
      return this.isPanInteractorEnabled;
   }

   public void setEnableRotateInteractor(boolean b) {
      if (this.isRotateInteractorEnabled != b) {
         boolean oldValue = this.isRotateInteractorEnabled;
         this.isRotateInteractorEnabled = b;
         if (this.isRotateInteractorEnabled) {
            this.getInteractors().add(this.rotateInteractor);
         } else {
            this.getInteractors().remove(this.rotateInteractor);
         }

         this.pcs.firePropertyChange("enableRotateInteractor", oldValue, b);
      }

   }

   public boolean getEnableRotateInteractor() {
      return this.isRotateInteractorEnabled;
   }

   public void setEnableResetTransformInteractor(boolean b) {
      if (this.isResetTransformInteractorEnabled != b) {
         boolean oldValue = this.isResetTransformInteractorEnabled;
         this.isResetTransformInteractorEnabled = b;
         if (this.isResetTransformInteractorEnabled) {
            this.getInteractors().add(this.resetTransformInteractor);
         } else {
            this.getInteractors().remove(this.resetTransformInteractor);
         }

         this.pcs.firePropertyChange("enableResetTransformInteractor", oldValue, b);
      }

   }

   public boolean getEnableResetTransformInteractor() {
      return this.isResetTransformInteractorEnabled;
   }

   public String getURI() {
      return this.uri;
   }

   public void setURI(String newURI) {
      String oldValue = this.uri;
      this.uri = newURI;
      if (this.uri != null) {
         this.loadSVGDocument(this.uri);
      } else {
         this.setSVGDocument((SVGDocument)null);
      }

      this.pcs.firePropertyChange("URI", oldValue, this.uri);
   }

   protected UserAgent createUserAgent() {
      return new CanvasUserAgent();
   }

   protected JGVTComponent.Listener createListener() {
      return new CanvasSVGListener();
   }

   protected void installSVGDocument(SVGDocument doc) {
      if (this.toolTipDocs != null) {
         Iterator var2 = this.toolTipDocs.keySet().iterator();

         while(var2.hasNext()) {
            Object o = var2.next();
            SVGDocument ttdoc = (SVGDocument)o;
            if (ttdoc != null) {
               NodeEventTarget root = (NodeEventTarget)ttdoc.getRootElement();
               if (root != null) {
                  root.removeEventListenerNS("http://www.w3.org/2001/xml-events", "mouseover", this.toolTipListener, false);
                  root.removeEventListenerNS("http://www.w3.org/2001/xml-events", "mouseout", this.toolTipListener, false);
               }
            }
         }

         this.toolTipDocs = null;
      }

      this.lastTarget = null;
      if (this.toolTipMap != null) {
         this.toolTipMap.clear();
      }

      super.installSVGDocument(doc);
   }

   public void setLastToolTipEvent(long t, EventTarget et) {
      this.lastToolTipEventTimeStamp = t;
      this.lastToolTipEventTarget = et;
   }

   public boolean matchLastToolTipEvent(long t, EventTarget et) {
      return this.lastToolTipEventTimeStamp == t && this.lastToolTipEventTarget == et;
   }

   protected class ToolTipRunnable implements Runnable {
      String theToolTip;

      public ToolTipRunnable(String toolTip) {
         this.theToolTip = toolTip;
      }

      public void run() {
         JSVGCanvas.this.setToolTipText(this.theToolTip);
         MouseEvent e;
         if (this.theToolTip != null) {
            e = new MouseEvent(JSVGCanvas.this, 504, System.currentTimeMillis(), 0, JSVGCanvas.this.locationListener.getLastX(), JSVGCanvas.this.locationListener.getLastY(), 0, false);
            ToolTipManager.sharedInstance().mouseEntered(e);
            e = new MouseEvent(JSVGCanvas.this, 503, System.currentTimeMillis(), 0, JSVGCanvas.this.locationListener.getLastX(), JSVGCanvas.this.locationListener.getLastY(), 0, false);
            ToolTipManager.sharedInstance().mouseMoved(e);
         } else {
            e = new MouseEvent(JSVGCanvas.this, 503, System.currentTimeMillis(), 0, JSVGCanvas.this.locationListener.getLastX(), JSVGCanvas.this.locationListener.getLastY(), 0, false);
            ToolTipManager.sharedInstance().mouseMoved(e);
         }

      }
   }

   protected class ToolTipModifier implements EventListener {
      protected CanvasUserAgent canvasUserAgent;

      public ToolTipModifier() {
      }

      public void handleEvent(Event evt) {
         if (!JSVGCanvas.this.matchLastToolTipEvent(evt.getTimeStamp(), evt.getTarget())) {
            JSVGCanvas.this.setLastToolTipEvent(evt.getTimeStamp(), evt.getTarget());
            EventTarget prevLastTarget = JSVGCanvas.this.lastTarget;
            if ("mouseover".equals(evt.getType())) {
               JSVGCanvas.this.lastTarget = evt.getTarget();
            } else if ("mouseout".equals(evt.getType())) {
               org.w3c.dom.events.MouseEvent mouseEvt = (org.w3c.dom.events.MouseEvent)evt;
               JSVGCanvas.this.lastTarget = mouseEvt.getRelatedTarget();
            }

            if (JSVGCanvas.this.toolTipMap != null) {
               Element e = (Element)JSVGCanvas.this.lastTarget;

               Object o;
               for(o = null; e != null; e = CSSEngine.getParentCSSStylableElement((Element)e)) {
                  o = JSVGCanvas.this.toolTipMap.get(e);
                  if (o != null) {
                     break;
                  }
               }

               String theToolTip = (String)o;
               if (prevLastTarget != JSVGCanvas.this.lastTarget) {
                  EventQueue.invokeLater(JSVGCanvas.this.new ToolTipRunnable(theToolTip));
               }
            }

         }
      }
   }

   protected static class LocationListener extends MouseMotionAdapter {
      protected int lastX = 0;
      protected int lastY = 0;

      public LocationListener() {
      }

      public void mouseMoved(MouseEvent evt) {
         this.lastX = evt.getX();
         this.lastY = evt.getY();
      }

      public int getLastX() {
         return this.lastX;
      }

      public int getLastY() {
         return this.lastY;
      }
   }

   protected class CanvasUserAgent extends JSVGComponent.BridgeUserAgent implements XMLConstants {
      final String TOOLTIP_TITLE_ONLY = "JSVGCanvas.CanvasUserAgent.ToolTip.titleOnly";
      final String TOOLTIP_DESC_ONLY = "JSVGCanvas.CanvasUserAgent.ToolTip.descOnly";
      final String TOOLTIP_TITLE_AND_TEXT = "JSVGCanvas.CanvasUserAgent.ToolTip.titleAndDesc";

      protected CanvasUserAgent() {
         super();
      }

      public void handleElement(Element elt, Object data) {
         super.handleElement(elt, data);
         if (JSVGCanvas.this.isInteractive()) {
            if ("http://www.w3.org/2000/svg".equals(elt.getNamespaceURI())) {
               if (elt.getParentNode() != elt.getOwnerDocument().getDocumentElement()) {
                  Element parent;
                  if (data instanceof Element) {
                     parent = (Element)data;
                  } else {
                     parent = (Element)elt.getParentNode();
                  }

                  Element descPeer = null;
                  Element titlePeer = null;
                  if (elt.getLocalName().equals("title")) {
                     if (data == Boolean.TRUE) {
                        titlePeer = elt;
                     }

                     descPeer = this.getPeerWithTag(parent, "http://www.w3.org/2000/svg", "desc");
                  } else if (elt.getLocalName().equals("desc")) {
                     if (data == Boolean.TRUE) {
                        descPeer = elt;
                     }

                     titlePeer = this.getPeerWithTag(parent, "http://www.w3.org/2000/svg", "title");
                  }

                  String titleTip = null;
                  if (titlePeer != null) {
                     titlePeer.normalize();
                     if (titlePeer.getFirstChild() != null) {
                        titleTip = titlePeer.getFirstChild().getNodeValue();
                     }
                  }

                  String descTip = null;
                  if (descPeer != null) {
                     descPeer.normalize();
                     if (descPeer.getFirstChild() != null) {
                        descTip = descPeer.getFirstChild().getNodeValue();
                     }
                  }

                  final String toolTip;
                  if (titleTip != null && titleTip.length() != 0) {
                     if (descTip != null && descTip.length() != 0) {
                        toolTip = Messages.formatMessage("JSVGCanvas.CanvasUserAgent.ToolTip.titleAndDesc", new Object[]{this.toFormattedHTML(titleTip), this.toFormattedHTML(descTip)});
                     } else {
                        toolTip = Messages.formatMessage("JSVGCanvas.CanvasUserAgent.ToolTip.titleOnly", new Object[]{this.toFormattedHTML(titleTip)});
                     }
                  } else if (descTip != null && descTip.length() != 0) {
                     toolTip = Messages.formatMessage("JSVGCanvas.CanvasUserAgent.ToolTip.descOnly", new Object[]{this.toFormattedHTML(descTip)});
                  } else {
                     toolTip = null;
                  }

                  if (toolTip == null) {
                     this.removeToolTip(parent);
                  } else {
                     if (JSVGCanvas.this.lastTarget != parent) {
                        this.setToolTip(parent, toolTip);
                     } else {
                        Object o = null;
                        if (JSVGCanvas.this.toolTipMap != null) {
                           o = JSVGCanvas.this.toolTipMap.get(parent);
                           JSVGCanvas.this.toolTipMap.put(parent, toolTip);
                        }

                        if (o != null) {
                           EventQueue.invokeLater(new Runnable() {
                              public void run() {
                                 JSVGCanvas.this.setToolTipText(toolTip);
                                 MouseEvent e = new MouseEvent(JSVGCanvas.this, 503, System.currentTimeMillis(), 0, JSVGCanvas.this.locationListener.getLastX(), JSVGCanvas.this.locationListener.getLastY(), 0, false);
                                 ToolTipManager.sharedInstance().mouseMoved(e);
                              }
                           });
                        } else {
                           EventQueue.invokeLater(JSVGCanvas.this.new ToolTipRunnable(toolTip));
                        }
                     }

                  }
               }
            }
         }
      }

      public String toFormattedHTML(String str) {
         StringBuffer sb = new StringBuffer(str);
         this.replace(sb, '&', "&amp;");
         this.replace(sb, '<', "&lt;");
         this.replace(sb, '>', "&gt;");
         this.replace(sb, '"', "&quot;");
         this.replace(sb, '\n', "<br>");
         return sb.toString();
      }

      protected void replace(StringBuffer sb, char c, String r) {
         String v = sb.toString();
         int i = v.length();

         while((i = v.lastIndexOf(c, i - 1)) != -1) {
            sb.deleteCharAt(i);
            sb.insert(i, r);
         }

      }

      public Element getPeerWithTag(Element parent, String nameSpaceURI, String localName) {
         if (parent == null) {
            return null;
         } else {
            for(Node n = parent.getFirstChild(); n != null; n = n.getNextSibling()) {
               if (nameSpaceURI.equals(n.getNamespaceURI()) && localName.equals(n.getLocalName()) && n.getNodeType() == 1) {
                  return (Element)n;
               }
            }

            return null;
         }
      }

      public boolean hasPeerWithTag(Element elt, String nameSpaceURI, String localName) {
         return this.getPeerWithTag(elt, nameSpaceURI, localName) != null;
      }

      public void setToolTip(Element elt, String toolTip) {
         if (JSVGCanvas.this.toolTipMap == null) {
            JSVGCanvas.this.toolTipMap = new WeakHashMap();
         }

         if (JSVGCanvas.this.toolTipDocs == null) {
            JSVGCanvas.this.toolTipDocs = new WeakHashMap();
         }

         SVGDocument doc = (SVGDocument)elt.getOwnerDocument();
         if (JSVGCanvas.this.toolTipDocs.put(doc, JSVGCanvas.MAP_TOKEN) == null) {
            NodeEventTarget root = (NodeEventTarget)doc.getRootElement();
            root.addEventListenerNS("http://www.w3.org/2001/xml-events", "mouseover", JSVGCanvas.this.toolTipListener, false, (Object)null);
            root.addEventListenerNS("http://www.w3.org/2001/xml-events", "mouseout", JSVGCanvas.this.toolTipListener, false, (Object)null);
         }

         JSVGCanvas.this.toolTipMap.put(elt, toolTip);
         if (elt == JSVGCanvas.this.lastTarget) {
            EventQueue.invokeLater(JSVGCanvas.this.new ToolTipRunnable(toolTip));
         }

      }

      public void removeToolTip(Element elt) {
         if (JSVGCanvas.this.toolTipMap != null) {
            JSVGCanvas.this.toolTipMap.remove(elt);
         }

         if (JSVGCanvas.this.lastTarget == elt) {
            EventQueue.invokeLater(JSVGCanvas.this.new ToolTipRunnable((String)null));
         }

      }

      public void displayError(String message) {
         if (JSVGCanvas.this.svgUserAgent != null) {
            super.displayError(message);
         } else {
            JOptionPane pane = new JOptionPane(message, 0);
            JDialog dialog = pane.createDialog(JSVGCanvas.this, "ERROR");
            dialog.setModal(false);
            dialog.setVisible(true);
         }

      }

      public void displayError(Exception ex) {
         if (JSVGCanvas.this.svgUserAgent != null) {
            super.displayError(ex);
         } else {
            JErrorPane pane = new JErrorPane(ex, 0);
            JDialog dialog = pane.createDialog(JSVGCanvas.this, "ERROR");
            dialog.setModal(false);
            dialog.setVisible(true);
         }

      }
   }

   public class ScrollDownAction extends ScrollAction {
      public ScrollDownAction(int inc) {
         super(0.0, (double)(-inc));
      }
   }

   public class ScrollUpAction extends ScrollAction {
      public ScrollUpAction(int inc) {
         super(0.0, (double)inc);
      }
   }

   public class ScrollLeftAction extends ScrollAction {
      public ScrollLeftAction(int inc) {
         super((double)inc, 0.0);
      }
   }

   public class ScrollRightAction extends ScrollAction {
      public ScrollRightAction(int inc) {
         super((double)(-inc), 0.0);
      }
   }

   public class ScrollAction extends AffineAction {
      public ScrollAction(double tx, double ty) {
         super(AffineTransform.getTranslateInstance(tx, ty));
      }
   }

   public class RotateAction extends AffineAction {
      public RotateAction(double theta) {
         super(AffineTransform.getRotateInstance(theta));
      }
   }

   public class ZoomOutAction extends ZoomAction {
      ZoomOutAction() {
         super(0.5);
      }
   }

   public class ZoomInAction extends ZoomAction {
      ZoomInAction() {
         super(2.0);
      }
   }

   public class ZoomAction extends AffineAction {
      public ZoomAction(double scale) {
         super(AffineTransform.getScaleInstance(scale, scale));
      }

      public ZoomAction(double scaleX, double scaleY) {
         super(AffineTransform.getScaleInstance(scaleX, scaleY));
      }
   }

   public class AffineAction extends AbstractAction {
      AffineTransform at;

      public AffineAction(AffineTransform at) {
         this.at = at;
      }

      public void actionPerformed(ActionEvent evt) {
         if (JSVGCanvas.this.gvtRoot != null) {
            AffineTransform rat = JSVGCanvas.this.getRenderingTransform();
            if (this.at != null) {
               Dimension dim = JSVGCanvas.this.getSize();
               int x = dim.width / 2;
               int y = dim.height / 2;
               AffineTransform t = AffineTransform.getTranslateInstance((double)x, (double)y);
               t.concatenate(this.at);
               t.translate((double)(-x), (double)(-y));
               t.concatenate(rat);
               JSVGCanvas.this.setRenderingTransform(t);
            }

         }
      }
   }

   public class ResetTransformAction extends AbstractAction {
      public void actionPerformed(ActionEvent evt) {
         JSVGCanvas.this.fragmentIdentifier = null;
         JSVGCanvas.this.resetRenderingTransform();
      }
   }

   protected class CanvasSVGListener extends JSVGComponent.SVGListener {
      protected CanvasSVGListener() {
         super();
      }

      public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
         super.documentLoadingStarted(e);
         JSVGCanvas.this.setToolTipText((String)null);
      }
   }
}
