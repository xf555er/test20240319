package org.apache.batik.apps.svgbrowser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JWindow;
import org.apache.batik.Version;

public class AboutDialog extends JWindow {
   public static final String ICON_BATIK_SPLASH = "AboutDialog.icon.batik.splash";

   public AboutDialog() {
      this.buildGUI();
   }

   public AboutDialog(Frame owner) {
      super(owner);
      this.buildGUI();
      this.addKeyListener(new KeyAdapter() {
         public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == 27) {
               AboutDialog.this.setVisible(false);
               AboutDialog.this.dispose();
            }

         }
      });
      this.addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent e) {
            AboutDialog.this.setVisible(false);
            AboutDialog.this.dispose();
         }
      });
   }

   public void setLocationRelativeTo(Frame f) {
      Dimension invokerSize = f.getSize();
      Point loc = f.getLocation();
      Point invokerScreenLocation = new Point(loc.x, loc.y);
      Rectangle bounds = this.getBounds();
      int dx = invokerScreenLocation.x + (invokerSize.width - bounds.width) / 2;
      int dy = invokerScreenLocation.y + (invokerSize.height - bounds.height) / 2;
      Dimension screenSize = this.getToolkit().getScreenSize();
      if (dy + bounds.height > screenSize.height) {
         dy = screenSize.height - bounds.height;
         dx = invokerScreenLocation.x < screenSize.width >> 1 ? invokerScreenLocation.x + invokerSize.width : invokerScreenLocation.x - bounds.width;
      }

      if (dx + bounds.width > screenSize.width) {
         dx = screenSize.width - bounds.width;
      }

      if (dx < 0) {
         dx = 0;
      }

      if (dy < 0) {
         dy = 0;
      }

      this.setLocation(dx, dy);
   }

   protected void buildGUI() {
      this.getContentPane().setBackground(Color.white);
      ClassLoader cl = this.getClass().getClassLoader();
      URL url = cl.getResource(Resources.getString("AboutDialog.icon.batik.splash"));
      ImageIcon icon = new ImageIcon(url);
      int w = icon.getIconWidth();
      int h = icon.getIconHeight();
      JLayeredPane p = new JLayeredPane();
      p.setSize(600, 425);
      this.getContentPane().add(p);
      JLabel l = new JLabel(icon);
      l.setBounds(0, 0, w, h);
      p.add(l, 0);
      JLabel l2 = new JLabel("Batik " + Version.getVersion());
      l2.setForeground(new Color(232, 232, 232, 255));
      l2.setOpaque(false);
      l2.setBackground(new Color(0, 0, 0, 0));
      l2.setHorizontalAlignment(4);
      l2.setVerticalAlignment(3);
      l2.setBounds(w - 320, h - 117, 300, 100);
      p.add(l2, 2);
      ((JComponent)this.getContentPane()).setBorder(BorderFactory.createCompoundBorder(BorderFactory.createBevelBorder(0, Color.gray, Color.black), BorderFactory.createCompoundBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3), BorderFactory.createLineBorder(Color.black)), BorderFactory.createEmptyBorder(10, 10, 10, 10))));
   }
}
