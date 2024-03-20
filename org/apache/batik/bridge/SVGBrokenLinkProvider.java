package org.apache.batik.bridge;

import java.util.HashMap;
import java.util.Map;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.ext.awt.image.spi.DefaultBrokenLinkProvider;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.filter.GraphicsNodeRable8Bit;

public class SVGBrokenLinkProvider extends DefaultBrokenLinkProvider implements ErrorConstants {
   public Filter getBrokenLinkImage(Object base, String code, Object[] params) {
      String message = formatMessage(base, code, params);
      Map props = new HashMap();
      props.put("org.apache.batik.BrokenLinkImage", message);
      CompositeGraphicsNode cgn = new CompositeGraphicsNode();
      return new GraphicsNodeRable8Bit(cgn, props);
   }
}
