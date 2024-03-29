package org.apache.batik.bridge.svg12;

import java.awt.Point;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeEventSupport;
import org.apache.batik.bridge.FocusManager;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.dom.events.DOMKeyboardEvent;
import org.apache.batik.dom.events.DOMMouseEvent;
import org.apache.batik.dom.events.DOMTextEvent;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.svg12.SVGOMWheelEvent;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.event.EventDispatcher;
import org.apache.batik.gvt.event.GraphicsNodeKeyEvent;
import org.apache.batik.gvt.event.GraphicsNodeMouseEvent;
import org.apache.batik.gvt.event.GraphicsNodeMouseWheelEvent;
import org.apache.batik.gvt.event.GraphicsNodeMouseWheelListener;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.views.AbstractView;

public abstract class SVG12BridgeEventSupport extends BridgeEventSupport {
   protected SVG12BridgeEventSupport() {
   }

   public static void addGVTListener(BridgeContext ctx, Document doc) {
      UserAgent ua = ctx.getUserAgent();
      if (ua != null) {
         EventDispatcher dispatcher = ua.getEventDispatcher();
         if (dispatcher != null) {
            Listener listener = new Listener(ctx, ua);
            dispatcher.addGraphicsNodeMouseListener(listener);
            dispatcher.addGraphicsNodeMouseWheelListener(listener);
            dispatcher.addGraphicsNodeKeyListener(listener);
            EventListener l = new BridgeEventSupport.GVTUnloadListener(dispatcher, listener);
            NodeEventTarget target = (NodeEventTarget)doc;
            target.addEventListenerNS("http://www.w3.org/2001/xml-events", "SVGUnload", l, false, (Object)null);
            storeEventListenerNS(ctx, target, "http://www.w3.org/2001/xml-events", "SVGUnload", l, false);
         }
      }

   }

   protected static class Listener extends BridgeEventSupport.Listener implements GraphicsNodeMouseWheelListener {
      protected SVG12BridgeContext ctx12;
      protected static String[][] IDENTIFIER_KEY_CODES = new String[256][];

      public Listener(BridgeContext ctx, UserAgent u) {
         super(ctx, u);
         this.ctx12 = (SVG12BridgeContext)ctx;
      }

      public void keyPressed(GraphicsNodeKeyEvent evt) {
         if (!this.isDown) {
            this.isDown = true;
            this.dispatchKeyboardEvent("keydown", evt);
         }

         if (evt.getKeyChar() == '\uffff') {
            this.dispatchTextEvent(evt);
         }

      }

      public void keyReleased(GraphicsNodeKeyEvent evt) {
         this.dispatchKeyboardEvent("keyup", evt);
         this.isDown = false;
      }

      public void keyTyped(GraphicsNodeKeyEvent evt) {
         this.dispatchTextEvent(evt);
      }

      protected void dispatchKeyboardEvent(String eventType, GraphicsNodeKeyEvent evt) {
         FocusManager fmgr = this.context.getFocusManager();
         if (fmgr != null) {
            Element targetElement = (Element)fmgr.getCurrentEventTarget();
            if (targetElement == null) {
               targetElement = this.context.getDocument().getDocumentElement();
            }

            DocumentEvent d = (DocumentEvent)targetElement.getOwnerDocument();
            DOMKeyboardEvent keyEvt = (DOMKeyboardEvent)d.createEvent("KeyboardEvent");
            String modifiers = DOMUtilities.getModifiersList(evt.getLockState(), evt.getModifiers());
            keyEvt.initKeyboardEventNS("http://www.w3.org/2001/xml-events", eventType, true, true, (AbstractView)null, this.mapKeyCodeToIdentifier(evt.getKeyCode()), this.mapKeyLocation(evt.getKeyLocation()), modifiers);

            try {
               ((EventTarget)targetElement).dispatchEvent(keyEvt);
            } catch (RuntimeException var9) {
               this.ua.displayError(var9);
            }

         }
      }

      protected void dispatchTextEvent(GraphicsNodeKeyEvent evt) {
         FocusManager fmgr = this.context.getFocusManager();
         if (fmgr != null) {
            Element targetElement = (Element)fmgr.getCurrentEventTarget();
            if (targetElement == null) {
               targetElement = this.context.getDocument().getDocumentElement();
            }

            DocumentEvent d = (DocumentEvent)targetElement.getOwnerDocument();
            DOMTextEvent textEvt = (DOMTextEvent)d.createEvent("TextEvent");
            textEvt.initTextEventNS("http://www.w3.org/2001/xml-events", "textInput", true, true, (AbstractView)null, String.valueOf(evt.getKeyChar()));

            try {
               ((EventTarget)targetElement).dispatchEvent(textEvt);
            } catch (RuntimeException var7) {
               this.ua.displayError(var7);
            }

         }
      }

      protected int mapKeyLocation(int location) {
         return location - 1;
      }

      protected static void putIdentifierKeyCode(String keyIdentifier, int keyCode) {
         if (IDENTIFIER_KEY_CODES[keyCode / 256] == null) {
            IDENTIFIER_KEY_CODES[keyCode / 256] = new String[256];
         }

         IDENTIFIER_KEY_CODES[keyCode / 256][keyCode % 256] = keyIdentifier;
      }

      protected String mapKeyCodeToIdentifier(int keyCode) {
         String[] a = IDENTIFIER_KEY_CODES[keyCode / 256];
         return a == null ? "Unidentified" : a[keyCode % 256];
      }

      public void mouseWheelMoved(GraphicsNodeMouseWheelEvent evt) {
         Document doc = this.context.getPrimaryBridgeContext().getDocument();
         Element targetElement = doc.getDocumentElement();
         DocumentEvent d = (DocumentEvent)doc;
         SVGOMWheelEvent wheelEvt = (SVGOMWheelEvent)d.createEvent("WheelEvent");
         wheelEvt.initWheelEventNS("http://www.w3.org/2001/xml-events", "wheel", true, true, (AbstractView)null, evt.getWheelDelta());

         try {
            ((EventTarget)targetElement).dispatchEvent(wheelEvt);
         } catch (RuntimeException var7) {
            this.ua.displayError(var7);
         }

      }

      public void mouseEntered(GraphicsNodeMouseEvent evt) {
         Point clientXY = evt.getClientPoint();
         GraphicsNode node = evt.getGraphicsNode();
         Element targetElement = this.getEventTarget(node, evt.getPoint2D());
         Element relatedElement = this.getRelatedElement(evt);
         int n = 0;
         if (relatedElement != null && targetElement != null) {
            n = DefaultXBLManager.computeBubbleLimit(targetElement, relatedElement);
         }

         this.dispatchMouseEvent("mouseover", targetElement, relatedElement, clientXY, evt, true, n);
      }

      public void mouseExited(GraphicsNodeMouseEvent evt) {
         Point clientXY = evt.getClientPoint();
         GraphicsNode node = evt.getRelatedNode();
         Element targetElement = this.getEventTarget(node, evt.getPoint2D());
         if (this.lastTargetElement != null) {
            int n = 0;
            if (targetElement != null) {
               n = DefaultXBLManager.computeBubbleLimit(this.lastTargetElement, targetElement);
            }

            this.dispatchMouseEvent("mouseout", this.lastTargetElement, targetElement, clientXY, evt, true, n);
            this.lastTargetElement = null;
         }

      }

      public void mouseMoved(GraphicsNodeMouseEvent evt) {
         Point clientXY = evt.getClientPoint();
         GraphicsNode node = evt.getGraphicsNode();
         Element targetElement = this.getEventTarget(node, evt.getPoint2D());
         Element holdLTE = this.lastTargetElement;
         if (holdLTE != targetElement) {
            int n;
            if (holdLTE != null) {
               n = 0;
               if (targetElement != null) {
                  n = DefaultXBLManager.computeBubbleLimit(holdLTE, targetElement);
               }

               this.dispatchMouseEvent("mouseout", holdLTE, targetElement, clientXY, evt, true, n);
            }

            if (targetElement != null) {
               n = 0;
               if (holdLTE != null) {
                  n = DefaultXBLManager.computeBubbleLimit(targetElement, holdLTE);
               }

               this.dispatchMouseEvent("mouseover", targetElement, holdLTE, clientXY, evt, true, n);
            }
         }

         this.dispatchMouseEvent("mousemove", targetElement, (Element)null, clientXY, evt, false, 0);
      }

      protected void dispatchMouseEvent(String eventType, Element targetElement, Element relatedElement, Point clientXY, GraphicsNodeMouseEvent evt, boolean cancelable) {
         this.dispatchMouseEvent(eventType, targetElement, relatedElement, clientXY, evt, cancelable, 0);
      }

      protected void dispatchMouseEvent(String eventType, Element targetElement, Element relatedElement, Point clientXY, GraphicsNodeMouseEvent evt, boolean cancelable, int bubbleLimit) {
         if (this.ctx12.mouseCaptureTarget != null) {
            NodeEventTarget net = null;
            if (targetElement != null) {
               for(net = (NodeEventTarget)targetElement; net != null && net != this.ctx12.mouseCaptureTarget; net = net.getParentNodeEventTarget()) {
               }
            }

            if (net == null) {
               if (this.ctx12.mouseCaptureSendAll) {
                  targetElement = (Element)this.ctx12.mouseCaptureTarget;
               } else {
                  targetElement = null;
               }
            }
         }

         if (targetElement != null) {
            Point screenXY = evt.getScreenPoint();
            DocumentEvent d = (DocumentEvent)targetElement.getOwnerDocument();
            DOMMouseEvent mouseEvt = (DOMMouseEvent)d.createEvent("MouseEvents");
            String modifiers = DOMUtilities.getModifiersList(evt.getLockState(), evt.getModifiers());
            mouseEvt.initMouseEventNS("http://www.w3.org/2001/xml-events", eventType, true, cancelable, (AbstractView)null, evt.getClickCount(), screenXY.x, screenXY.y, clientXY.x, clientXY.y, (short)(evt.getButton() - 1), (EventTarget)relatedElement, modifiers);
            mouseEvt.setBubbleLimit(bubbleLimit);

            try {
               ((EventTarget)targetElement).dispatchEvent(mouseEvt);
            } catch (RuntimeException var16) {
               this.ua.displayError(var16);
            } finally {
               this.lastTargetElement = targetElement;
            }
         }

         if (this.ctx12.mouseCaptureTarget != null && this.ctx12.mouseCaptureAutoRelease && "mouseup".equals(eventType)) {
            this.ctx12.stopMouseCapture();
         }

      }

      static {
         putIdentifierKeyCode("U+0030", 48);
         putIdentifierKeyCode("U+0031", 49);
         putIdentifierKeyCode("U+0032", 50);
         putIdentifierKeyCode("U+0033", 51);
         putIdentifierKeyCode("U+0034", 52);
         putIdentifierKeyCode("U+0035", 53);
         putIdentifierKeyCode("U+0036", 54);
         putIdentifierKeyCode("U+0037", 55);
         putIdentifierKeyCode("U+0038", 56);
         putIdentifierKeyCode("U+0039", 57);
         putIdentifierKeyCode("Accept", 30);
         putIdentifierKeyCode("Again", 65481);
         putIdentifierKeyCode("U+0041", 65);
         putIdentifierKeyCode("AllCandidates", 256);
         putIdentifierKeyCode("Alphanumeric", 240);
         putIdentifierKeyCode("AltGraph", 65406);
         putIdentifierKeyCode("Alt", 18);
         putIdentifierKeyCode("U+0026", 150);
         putIdentifierKeyCode("U+0027", 222);
         putIdentifierKeyCode("U+002A", 151);
         putIdentifierKeyCode("U+0040", 512);
         putIdentifierKeyCode("U+005C", 92);
         putIdentifierKeyCode("U+0008", 8);
         putIdentifierKeyCode("U+0042", 66);
         putIdentifierKeyCode("U+0018", 3);
         putIdentifierKeyCode("CapsLock", 20);
         putIdentifierKeyCode("U+005E", 514);
         putIdentifierKeyCode("U+0043", 67);
         putIdentifierKeyCode("Clear", 12);
         putIdentifierKeyCode("CodeInput", 258);
         putIdentifierKeyCode("U+003A", 513);
         putIdentifierKeyCode("U+0301", 129);
         putIdentifierKeyCode("U+0306", 133);
         putIdentifierKeyCode("U+030C", 138);
         putIdentifierKeyCode("U+0327", 139);
         putIdentifierKeyCode("U+0302", 130);
         putIdentifierKeyCode("U+0308", 135);
         putIdentifierKeyCode("U+0307", 134);
         putIdentifierKeyCode("U+030B", 137);
         putIdentifierKeyCode("U+0300", 128);
         putIdentifierKeyCode("U+0345", 141);
         putIdentifierKeyCode("U+0304", 132);
         putIdentifierKeyCode("U+0328", 140);
         putIdentifierKeyCode("U+030A", 136);
         putIdentifierKeyCode("U+0303", 131);
         putIdentifierKeyCode("U+002C", 44);
         putIdentifierKeyCode("Compose", 65312);
         putIdentifierKeyCode("Control", 17);
         putIdentifierKeyCode("Convert", 28);
         putIdentifierKeyCode("Copy", 65485);
         putIdentifierKeyCode("Cut", 65489);
         putIdentifierKeyCode("U+007F", 127);
         putIdentifierKeyCode("U+0044", 68);
         putIdentifierKeyCode("U+0024", 515);
         putIdentifierKeyCode("Down", 40);
         putIdentifierKeyCode("U+0045", 69);
         putIdentifierKeyCode("End", 35);
         putIdentifierKeyCode("Enter", 10);
         putIdentifierKeyCode("U+003D", 61);
         putIdentifierKeyCode("U+001B", 27);
         putIdentifierKeyCode("U+20AC", 516);
         putIdentifierKeyCode("U+0021", 517);
         putIdentifierKeyCode("F10", 121);
         putIdentifierKeyCode("F11", 122);
         putIdentifierKeyCode("F12", 123);
         putIdentifierKeyCode("F13", 61440);
         putIdentifierKeyCode("F14", 61441);
         putIdentifierKeyCode("F15", 61442);
         putIdentifierKeyCode("F16", 61443);
         putIdentifierKeyCode("F17", 61444);
         putIdentifierKeyCode("F18", 61445);
         putIdentifierKeyCode("F19", 61446);
         putIdentifierKeyCode("F1", 112);
         putIdentifierKeyCode("F20", 61447);
         putIdentifierKeyCode("F21", 61448);
         putIdentifierKeyCode("F22", 61449);
         putIdentifierKeyCode("F23", 61450);
         putIdentifierKeyCode("F24", 61451);
         putIdentifierKeyCode("F2", 113);
         putIdentifierKeyCode("F3", 114);
         putIdentifierKeyCode("F4", 115);
         putIdentifierKeyCode("F5", 116);
         putIdentifierKeyCode("F6", 117);
         putIdentifierKeyCode("F7", 118);
         putIdentifierKeyCode("F8", 119);
         putIdentifierKeyCode("F9", 120);
         putIdentifierKeyCode("FinalMode", 24);
         putIdentifierKeyCode("Find", 65488);
         putIdentifierKeyCode("U+0046", 70);
         putIdentifierKeyCode("U+002E", 46);
         putIdentifierKeyCode("FullWidth", 243);
         putIdentifierKeyCode("U+0047", 71);
         putIdentifierKeyCode("U+0060", 192);
         putIdentifierKeyCode("U+003E", 160);
         putIdentifierKeyCode("HalfWidth", 244);
         putIdentifierKeyCode("U+0023", 520);
         putIdentifierKeyCode("Help", 156);
         putIdentifierKeyCode("Hiragana", 242);
         putIdentifierKeyCode("U+0048", 72);
         putIdentifierKeyCode("Home", 36);
         putIdentifierKeyCode("U+0049", 73);
         putIdentifierKeyCode("Insert", 155);
         putIdentifierKeyCode("U+00A1", 518);
         putIdentifierKeyCode("JapaneseHiragana", 260);
         putIdentifierKeyCode("JapaneseKatakana", 259);
         putIdentifierKeyCode("JapaneseRomaji", 261);
         putIdentifierKeyCode("U+004A", 74);
         putIdentifierKeyCode("KanaMode", 262);
         putIdentifierKeyCode("KanjiMode", 25);
         putIdentifierKeyCode("Katakana", 241);
         putIdentifierKeyCode("U+004B", 75);
         putIdentifierKeyCode("U+007B", 161);
         putIdentifierKeyCode("Left", 37);
         putIdentifierKeyCode("U+0028", 519);
         putIdentifierKeyCode("U+005B", 91);
         putIdentifierKeyCode("U+003C", 153);
         putIdentifierKeyCode("U+004C", 76);
         putIdentifierKeyCode("Meta", 157);
         putIdentifierKeyCode("Meta", 157);
         putIdentifierKeyCode("U+002D", 45);
         putIdentifierKeyCode("U+004D", 77);
         putIdentifierKeyCode("ModeChange", 31);
         putIdentifierKeyCode("U+004E", 78);
         putIdentifierKeyCode("Nonconvert", 29);
         putIdentifierKeyCode("NumLock", 144);
         putIdentifierKeyCode("NumLock", 144);
         putIdentifierKeyCode("U+004F", 79);
         putIdentifierKeyCode("PageDown", 34);
         putIdentifierKeyCode("PageUp", 33);
         putIdentifierKeyCode("Paste", 65487);
         putIdentifierKeyCode("Pause", 19);
         putIdentifierKeyCode("U+0050", 80);
         putIdentifierKeyCode("U+002B", 521);
         putIdentifierKeyCode("PreviousCandidate", 257);
         putIdentifierKeyCode("PrintScreen", 154);
         putIdentifierKeyCode("Props", 65482);
         putIdentifierKeyCode("U+0051", 81);
         putIdentifierKeyCode("U+0022", 152);
         putIdentifierKeyCode("U+007D", 162);
         putIdentifierKeyCode("Right", 39);
         putIdentifierKeyCode("U+0029", 522);
         putIdentifierKeyCode("U+005D", 93);
         putIdentifierKeyCode("U+0052", 82);
         putIdentifierKeyCode("RomanCharacters", 245);
         putIdentifierKeyCode("Scroll", 145);
         putIdentifierKeyCode("Scroll", 145);
         putIdentifierKeyCode("U+003B", 59);
         putIdentifierKeyCode("U+309A", 143);
         putIdentifierKeyCode("Shift", 16);
         putIdentifierKeyCode("Shift", 16);
         putIdentifierKeyCode("U+0053", 83);
         putIdentifierKeyCode("U+002F", 47);
         putIdentifierKeyCode("U+0020", 32);
         putIdentifierKeyCode("Stop", 65480);
         putIdentifierKeyCode("U+0009", 9);
         putIdentifierKeyCode("U+0054", 84);
         putIdentifierKeyCode("U+0055", 85);
         putIdentifierKeyCode("U+005F", 523);
         putIdentifierKeyCode("Undo", 65483);
         putIdentifierKeyCode("Unidentified", 0);
         putIdentifierKeyCode("Up", 38);
         putIdentifierKeyCode("U+0056", 86);
         putIdentifierKeyCode("U+3099", 142);
         putIdentifierKeyCode("U+0057", 87);
         putIdentifierKeyCode("U+0058", 88);
         putIdentifierKeyCode("U+0059", 89);
         putIdentifierKeyCode("U+005A", 90);
         putIdentifierKeyCode("U+0030", 96);
         putIdentifierKeyCode("U+0031", 97);
         putIdentifierKeyCode("U+0032", 98);
         putIdentifierKeyCode("U+0033", 99);
         putIdentifierKeyCode("U+0034", 100);
         putIdentifierKeyCode("U+0035", 101);
         putIdentifierKeyCode("U+0036", 102);
         putIdentifierKeyCode("U+0037", 103);
         putIdentifierKeyCode("U+0038", 104);
         putIdentifierKeyCode("U+0039", 105);
         putIdentifierKeyCode("U+002A", 106);
         putIdentifierKeyCode("Down", 225);
         putIdentifierKeyCode("U+002E", 110);
         putIdentifierKeyCode("Left", 226);
         putIdentifierKeyCode("U+002D", 109);
         putIdentifierKeyCode("U+002B", 107);
         putIdentifierKeyCode("Right", 227);
         putIdentifierKeyCode("U+002F", 111);
         putIdentifierKeyCode("Up", 224);
      }
   }
}
