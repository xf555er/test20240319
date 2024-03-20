package org.apache.batik.dom.events;

import java.util.ArrayList;
import java.util.List;
import org.apache.batik.xml.XMLUtilities;
import org.w3c.dom.events.UIEvent;
import org.w3c.dom.views.AbstractView;

public class DOMUIEvent extends AbstractEvent implements UIEvent {
   private AbstractView view;
   private int detail;

   public AbstractView getView() {
      return this.view;
   }

   public int getDetail() {
      return this.detail;
   }

   public void initUIEvent(String typeArg, boolean canBubbleArg, boolean cancelableArg, AbstractView viewArg, int detailArg) {
      this.initEvent(typeArg, canBubbleArg, cancelableArg);
      this.view = viewArg;
      this.detail = detailArg;
   }

   public void initUIEventNS(String namespaceURIArg, String typeArg, boolean canBubbleArg, boolean cancelableArg, AbstractView viewArg, int detailArg) {
      this.initEventNS(namespaceURIArg, typeArg, canBubbleArg, cancelableArg);
      this.view = viewArg;
      this.detail = detailArg;
   }

   protected String[] split(String s) {
      List a = new ArrayList(8);
      int i = 0;
      int len = s.length();

      while(true) {
         char c;
         do {
            if (i >= len) {
               return (String[])((String[])a.toArray(new String[a.size()]));
            }

            c = s.charAt(i++);
         } while(XMLUtilities.isXMLSpace(c));

         StringBuffer sb = new StringBuffer();
         sb.append(c);

         while(i < len) {
            c = s.charAt(i++);
            if (XMLUtilities.isXMLSpace(c)) {
               a.add(sb.toString());
               break;
            }

            sb.append(c);
         }

         if (i == len) {
            a.add(sb.toString());
         }
      }
   }
}
