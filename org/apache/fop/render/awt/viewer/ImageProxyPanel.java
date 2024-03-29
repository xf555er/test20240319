package org.apache.fop.render.awt.viewer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import javax.swing.JPanel;
import org.apache.fop.apps.FOPException;
import org.apache.fop.render.awt.AWTRenderer;

public class ImageProxyPanel extends JPanel {
   private transient Reference imageRef;
   private Dimension size;
   private AWTRenderer renderer;
   private int page;

   public ImageProxyPanel(AWTRenderer renderer, int page) {
      this.renderer = renderer;
      this.page = page;

      try {
         Dimension size = renderer.getPageImageSize(page);
         Insets insets = this.getInsets();
         size.width += insets.left + insets.right;
         size.height += insets.top + insets.bottom;
         this.size = size;
      } catch (Exception var5) {
         this.size = new Dimension(10, 10);
      }

      this.setOpaque(false);
   }

   public Dimension getMinimumSize() {
      return this.getPreferredSize();
   }

   public Dimension getPreferredSize() {
      return this.size;
   }

   public synchronized void setPage(int pg) {
      if (this.page != pg) {
         this.page = pg;
         this.imageRef = null;
         this.repaint();
      }

   }

   public synchronized void paintComponent(Graphics graphics) {
      try {
         if (this.isOpaque()) {
            graphics.setColor(this.getBackground());
            graphics.fillRect(0, 0, this.getWidth(), this.getHeight());
         }

         super.paintComponent(graphics);
         BufferedImage image = null;
         if (this.imageRef != null && this.imageRef.get() != null) {
            image = (BufferedImage)this.imageRef.get();
         } else {
            image = this.renderer.getPageImage(this.page);
            this.imageRef = new SoftReference(image);
         }

         if (image != null) {
            int x = (this.getWidth() - image.getWidth()) / 2;
            int y = (this.getHeight() - image.getHeight()) / 2;
            graphics.drawImage(image, x, y, image.getWidth(), image.getHeight(), (ImageObserver)null);
         }
      } catch (FOPException var5) {
         var5.printStackTrace();
      }

   }
}
