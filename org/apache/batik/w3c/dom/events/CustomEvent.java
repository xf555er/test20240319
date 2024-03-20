package org.apache.batik.w3c.dom.events;

import org.w3c.dom.events.Event;

public interface CustomEvent extends Event {
   Object getDetail();

   void initCustomEventNS(String var1, String var2, boolean var3, boolean var4, Object var5);
}
