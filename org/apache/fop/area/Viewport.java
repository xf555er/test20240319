package org.apache.fop.area;

import java.awt.Rectangle;

public interface Viewport {
   boolean hasClip();

   Rectangle getClipRectangle();
}
