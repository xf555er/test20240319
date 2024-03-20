package org.apache.batik.extension.svg;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.BridgeExtension;
import org.w3c.dom.Element;

public class BatikBridgeExtension implements BridgeExtension {
   public float getPriority() {
      return 1.0F;
   }

   public Iterator getImplementedExtensions() {
      String[] extensions = new String[]{"http://xml.apache.org/batik/ext/poly/1.0", "http://xml.apache.org/batik/ext/star/1.0", "http://xml.apache.org/batik/ext/histogramNormalization/1.0", "http://xml.apache.org/batik/ext/colorSwitch/1.0", "http://xml.apache.org/batik/ext/flowText/1.0"};
      List v = Arrays.asList(extensions);
      return Collections.unmodifiableList(v).iterator();
   }

   public String getAuthor() {
      return "Thomas DeWeese";
   }

   public String getContactAddress() {
      return "deweese@apache.org";
   }

   public String getURL() {
      return "http://xml.apache.org/batik";
   }

   public String getDescription() {
      return "Example extension to standard SVG shape tags";
   }

   public void registerTags(BridgeContext ctx) {
      ctx.putBridge(new BatikRegularPolygonElementBridge());
      ctx.putBridge(new BatikStarElementBridge());
      ctx.putBridge(new BatikHistogramNormalizationElementBridge());
      ctx.putBridge(new BatikFlowTextElementBridge());
      ctx.putBridge(new ColorSwitchBridge());
   }

   public boolean isDynamicElement(Element e) {
      return false;
   }
}
