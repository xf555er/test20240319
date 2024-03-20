package org.apache.batik.dom.events;

import java.util.HashSet;
import java.util.Iterator;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MouseEvent;
import org.w3c.dom.views.AbstractView;

public class DOMMouseEvent extends DOMUIEvent implements MouseEvent {
   private int screenX;
   private int screenY;
   private int clientX;
   private int clientY;
   private short button;
   private EventTarget relatedTarget;
   protected HashSet modifierKeys = new HashSet();

   public int getScreenX() {
      return this.screenX;
   }

   public int getScreenY() {
      return this.screenY;
   }

   public int getClientX() {
      return this.clientX;
   }

   public int getClientY() {
      return this.clientY;
   }

   public boolean getCtrlKey() {
      return this.modifierKeys.contains("Control");
   }

   public boolean getShiftKey() {
      return this.modifierKeys.contains("Shift");
   }

   public boolean getAltKey() {
      return this.modifierKeys.contains("Alt");
   }

   public boolean getMetaKey() {
      return this.modifierKeys.contains("Meta");
   }

   public short getButton() {
      return this.button;
   }

   public EventTarget getRelatedTarget() {
      return this.relatedTarget;
   }

   public boolean getModifierState(String keyIdentifierArg) {
      return this.modifierKeys.contains(keyIdentifierArg);
   }

   public String getModifiersString() {
      if (this.modifierKeys.isEmpty()) {
         return "";
      } else {
         StringBuffer sb = new StringBuffer(this.modifierKeys.size() * 8);
         Iterator i = this.modifierKeys.iterator();
         sb.append((String)i.next());

         while(i.hasNext()) {
            sb.append(' ');
            sb.append((String)i.next());
         }

         return sb.toString();
      }
   }

   public void initMouseEvent(String typeArg, boolean canBubbleArg, boolean cancelableArg, AbstractView viewArg, int detailArg, int screenXArg, int screenYArg, int clientXArg, int clientYArg, boolean ctrlKeyArg, boolean altKeyArg, boolean shiftKeyArg, boolean metaKeyArg, short buttonArg, EventTarget relatedTargetArg) {
      this.initUIEvent(typeArg, canBubbleArg, cancelableArg, viewArg, detailArg);
      this.screenX = screenXArg;
      this.screenY = screenYArg;
      this.clientX = clientXArg;
      this.clientY = clientYArg;
      if (ctrlKeyArg) {
         this.modifierKeys.add("Control");
      }

      if (altKeyArg) {
         this.modifierKeys.add("Alt");
      }

      if (shiftKeyArg) {
         this.modifierKeys.add("Shift");
      }

      if (metaKeyArg) {
         this.modifierKeys.add("Meta");
      }

      this.button = buttonArg;
      this.relatedTarget = relatedTargetArg;
   }

   public void initMouseEventNS(String namespaceURIArg, String typeArg, boolean canBubbleArg, boolean cancelableArg, AbstractView viewArg, int detailArg, int screenXArg, int screenYArg, int clientXArg, int clientYArg, short buttonArg, EventTarget relatedTargetArg, String modifiersList) {
      this.initUIEventNS(namespaceURIArg, typeArg, canBubbleArg, cancelableArg, viewArg, detailArg);
      this.screenX = screenXArg;
      this.screenY = screenYArg;
      this.clientX = clientXArg;
      this.clientY = clientYArg;
      this.button = buttonArg;
      this.relatedTarget = relatedTargetArg;
      this.modifierKeys.clear();
      String[] modifiers = this.split(modifiersList);
      String[] var15 = modifiers;
      int var16 = modifiers.length;

      for(int var17 = 0; var17 < var16; ++var17) {
         String modifier = var15[var17];
         this.modifierKeys.add(modifier);
      }

   }
}
