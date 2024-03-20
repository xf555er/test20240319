package org.apache.xmlgraphics.java2d;

import java.awt.Graphics2D;
import org.apache.xmlgraphics.ps.PSGenerator;

public interface GeneralGraphics2DImagePainter extends Graphics2DImagePainter {
   Graphics2D getGraphics(boolean var1, PSGenerator var2);

   void addFallbackFont(String var1, Object var2);
}
