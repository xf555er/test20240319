package org.apache.xmlgraphics.java2d.color;

import java.awt.color.ColorSpace;

public abstract class AbstractDeviceSpecificColorSpace extends ColorSpace {
   private static final long serialVersionUID = -4888985582872101875L;

   protected AbstractDeviceSpecificColorSpace(int type, int numcomponents) {
      super(type, numcomponents);
   }
}
