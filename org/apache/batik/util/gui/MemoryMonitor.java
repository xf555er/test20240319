package org.apache.batik.util.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.ButtonFactory;
import org.apache.batik.util.gui.resource.MissingListenerException;
import org.apache.batik.util.resources.ResourceManager;

public class MemoryMonitor extends JFrame implements ActionMap {
   protected static final String RESOURCE = "org.apache.batik.util.gui.resources.MemoryMonitorMessages";
   protected static ResourceBundle bundle = ResourceBundle.getBundle("org.apache.batik.util.gui.resources.MemoryMonitorMessages", Locale.getDefault());
   protected static ResourceManager resources;
   protected Map listeners;
   protected Panel panel;

   public MemoryMonitor() {
      this(1000L);
   }

   public MemoryMonitor(long time) {
      super(resources.getString("Frame.title"));
      this.listeners = new HashMap();
      this.listeners.put("CollectButtonAction", new CollectButtonAction());
      this.listeners.put("CloseButtonAction", new CloseButtonAction());
      this.panel = new Panel(time);
      this.getContentPane().add(this.panel);
      this.panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), resources.getString("Frame.border_title")));
      JPanel p = new JPanel(new FlowLayout(2));
      ButtonFactory bf = new ButtonFactory(bundle, this);
      p.add(bf.createJButton("CollectButton"));
      p.add(bf.createJButton("CloseButton"));
      this.getContentPane().add(p, "South");
      this.pack();
      this.addWindowListener(new WindowAdapter() {
         public void windowActivated(WindowEvent e) {
            RepaintThread t = MemoryMonitor.this.panel.getRepaintThread();
            if (!t.isAlive()) {
               t.start();
            } else {
               t.safeResume();
            }

         }

         public void windowClosing(WindowEvent ev) {
            MemoryMonitor.this.panel.getRepaintThread().safeSuspend();
         }

         public void windowDeiconified(WindowEvent e) {
            MemoryMonitor.this.panel.getRepaintThread().safeResume();
         }

         public void windowIconified(WindowEvent e) {
            MemoryMonitor.this.panel.getRepaintThread().safeSuspend();
         }
      });
   }

   public Action getAction(String key) throws MissingListenerException {
      return (Action)this.listeners.get(key);
   }

   static {
      resources = new ResourceManager(bundle);
   }

   public static class RepaintThread extends Thread {
      protected long timeout;
      protected List components;
      protected Runtime runtime = Runtime.getRuntime();
      protected boolean suspended;
      protected UpdateRunnable updateRunnable;

      public RepaintThread(long timeout, List components) {
         this.timeout = timeout;
         this.components = components;
         this.updateRunnable = this.createUpdateRunnable();
         this.setPriority(1);
      }

      public void run() {
         while(true) {
            try {
               synchronized(this.updateRunnable) {
                  if (!this.updateRunnable.inEventQueue) {
                     EventQueue.invokeLater(this.updateRunnable);
                  }

                  this.updateRunnable.inEventQueue = true;
               }

               sleep(this.timeout);
               synchronized(this) {
                  while(this.suspended) {
                     this.wait();
                  }
               }
            } catch (InterruptedException var6) {
            }
         }
      }

      protected UpdateRunnable createUpdateRunnable() {
         return new UpdateRunnable();
      }

      public synchronized void safeSuspend() {
         if (!this.suspended) {
            this.suspended = true;
         }

      }

      public synchronized void safeResume() {
         if (this.suspended) {
            this.suspended = false;
            this.notify();
         }

      }

      protected class UpdateRunnable implements Runnable {
         public boolean inEventQueue = false;

         public void run() {
            long free = RepaintThread.this.runtime.freeMemory();
            long total = RepaintThread.this.runtime.totalMemory();
            Iterator var5 = RepaintThread.this.components.iterator();

            while(var5.hasNext()) {
               Object component = var5.next();
               Component c = (Component)component;
               ((MemoryChangeListener)c).memoryStateChanged(total, free);
               c.repaint();
            }

            synchronized(this) {
               this.inEventQueue = false;
            }
         }
      }
   }

   public interface MemoryChangeListener {
      void memoryStateChanged(long var1, long var3);
   }

   public static class History extends JPanel implements MemoryChangeListener {
      public static final int PREFERRED_WIDTH = 200;
      public static final int PREFERRED_HEIGHT = 100;
      protected static final Stroke GRID_LINES_STROKE = new BasicStroke(1.0F);
      protected static final Stroke CURVE_STROKE = new BasicStroke(2.0F, 1, 1);
      protected static final Stroke BORDER_STROKE = new BasicStroke(2.0F);
      protected Color gridLinesColor = new Color(0, 130, 0);
      protected Color curveColor;
      protected Color borderColor;
      protected List data;
      protected int xShift;
      protected long totalMemory;
      protected long freeMemory;
      protected GeneralPath path;

      public History() {
         this.curveColor = Color.yellow;
         this.borderColor = Color.green;
         this.data = new LinkedList();
         this.xShift = 0;
         this.path = new GeneralPath();
         this.setBackground(Color.black);
         this.setPreferredSize(new Dimension(200, 100));
      }

      public void memoryStateChanged(long total, long free) {
         this.totalMemory = total;
         this.freeMemory = free;
         this.data.add(this.totalMemory - this.freeMemory);
         if (this.data.size() > 190) {
            this.data.remove(0);
            this.xShift = (this.xShift + 1) % 10;
         }

         Iterator it = this.data.iterator();
         GeneralPath p = new GeneralPath();
         long l = (Long)it.next();
         p.moveTo(5.0F, (float)(this.totalMemory - l) / (float)this.totalMemory * 80.0F + 10.0F);

         for(int i = 6; it.hasNext(); ++i) {
            l = (Long)it.next();
            p.lineTo((float)i, (float)(this.totalMemory - l) / (float)this.totalMemory * 80.0F + 10.0F);
         }

         this.path = p;
      }

      protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         Graphics2D g2d = (Graphics2D)g;
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         Dimension dim = this.getSize();
         double sx = (double)dim.width / 200.0;
         double sy = (double)dim.height / 100.0;
         g2d.transform(AffineTransform.getScaleInstance(sx, sy));
         g2d.setPaint(this.gridLinesColor);
         g2d.setStroke(GRID_LINES_STROKE);

         int i;
         int ly;
         for(i = 1; i < 20; ++i) {
            ly = i * 10 + 5 - this.xShift;
            g2d.draw(new Line2D.Double((double)ly, 5.0, (double)ly, 95.0));
         }

         for(i = 1; i < 9; ++i) {
            ly = i * 10 + 5;
            g2d.draw(new Line2D.Double(5.0, (double)ly, 195.0, (double)ly));
         }

         g2d.setPaint(this.curveColor);
         g2d.setStroke(CURVE_STROKE);
         g2d.draw(this.path);
         g2d.setStroke(BORDER_STROKE);
         g2d.setPaint(this.borderColor);
         g2d.draw(new Rectangle2D.Double(5.0, 5.0, 190.0, 90.0));
      }
   }

   public static class Usage extends JPanel implements MemoryChangeListener {
      public static final int PREFERRED_WIDTH = 90;
      public static final int PREFERRED_HEIGHT = 100;
      protected static final String UNITS;
      protected static final String TOTAL;
      protected static final String USED;
      protected static final boolean POSTFIX;
      protected static final int FONT_SIZE = 9;
      protected static final int BLOCK_MARGIN = 10;
      protected static final int BLOCKS = 15;
      protected static final double BLOCK_WIDTH = 70.0;
      protected static final double BLOCK_HEIGHT = 3.8666666666666667;
      protected static final int[] BLOCK_TYPE;
      protected Color[] usedColors;
      protected Color[] freeColors;
      protected Font font;
      protected Color textColor;
      protected long totalMemory;
      protected long freeMemory;

      public Usage() {
         this.usedColors = new Color[]{Color.red, new Color(255, 165, 0), Color.green};
         this.freeColors = new Color[]{new Color(130, 0, 0), new Color(130, 90, 0), new Color(0, 130, 0)};
         this.font = new Font("SansSerif", 1, 9);
         this.textColor = Color.green;
         this.setBackground(Color.black);
         this.setPreferredSize(new Dimension(90, 100));
      }

      public void memoryStateChanged(long total, long free) {
         this.totalMemory = total;
         this.freeMemory = free;
      }

      public void setTextColor(Color c) {
         this.textColor = c;
      }

      public void setLowUsedMemoryColor(Color c) {
         this.usedColors[2] = c;
      }

      public void setMediumUsedMemoryColor(Color c) {
         this.usedColors[1] = c;
      }

      public void setHighUsedMemoryColor(Color c) {
         this.usedColors[0] = c;
      }

      public void setLowFreeMemoryColor(Color c) {
         this.freeColors[2] = c;
      }

      public void setMediumFreeMemoryColor(Color c) {
         this.freeColors[1] = c;
      }

      public void setHighFreeMemoryColor(Color c) {
         this.freeColors[0] = c;
      }

      protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         Graphics2D g2d = (Graphics2D)g;
         Dimension dim = this.getSize();
         double sx = (double)dim.width / 90.0;
         double sy = (double)dim.height / 100.0;
         g2d.transform(AffineTransform.getScaleInstance(sx, sy));
         g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         int nfree = (int)Math.round(15.0 * (double)this.freeMemory / (double)this.totalMemory);

         int i;
         Rectangle2D.Double rect;
         for(i = 0; i < nfree; ++i) {
            rect = new Rectangle2D.Double(10.0, (double)i * 3.8666666666666667 + (double)i + 9.0 + 5.0, 70.0, 3.8666666666666667);
            g2d.setPaint(this.freeColors[BLOCK_TYPE[i]]);
            g2d.fill(rect);
         }

         for(i = nfree; i < 15; ++i) {
            rect = new Rectangle2D.Double(10.0, (double)i * 3.8666666666666667 + (double)i + 9.0 + 5.0, 70.0, 3.8666666666666667);
            g2d.setPaint(this.usedColors[BLOCK_TYPE[i]]);
            g2d.fill(rect);
         }

         g2d.setPaint(this.textColor);
         g2d.setFont(this.font);
         long total = this.totalMemory / 1024L;
         long used = (this.totalMemory - this.freeMemory) / 1024L;
         String totalText;
         String usedText;
         if (POSTFIX) {
            totalText = total + UNITS + " " + TOTAL;
            usedText = used + UNITS + " " + USED;
         } else {
            totalText = TOTAL + " " + total + UNITS;
            usedText = USED + " " + used + UNITS;
         }

         g2d.drawString(totalText, 10, 10);
         g2d.drawString(usedText, 10, 97);
      }

      static {
         UNITS = MemoryMonitor.resources.getString("Usage.units");
         TOTAL = MemoryMonitor.resources.getString("Usage.total");
         USED = MemoryMonitor.resources.getString("Usage.used");
         POSTFIX = MemoryMonitor.resources.getBoolean("Usage.postfix");
         BLOCK_TYPE = new int[]{0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2};
      }
   }

   public static class Panel extends JPanel {
      protected RepaintThread repaintThread;

      public Panel() {
         this(1000L);
      }

      public Panel(long time) {
         super(new GridBagLayout());
         ExtendedGridBagConstraints constraints = new ExtendedGridBagConstraints();
         constraints.insets = new Insets(5, 5, 5, 5);
         List l = new ArrayList();
         JPanel p = new JPanel(new BorderLayout());
         p.setBorder(BorderFactory.createLoweredBevelBorder());
         JComponent c = new Usage();
         p.add(c);
         constraints.weightx = 0.3;
         constraints.weighty = 1.0;
         constraints.fill = 1;
         constraints.setGridBounds(0, 0, 1, 1);
         this.add(p, constraints);
         l.add(c);
         p = new JPanel(new BorderLayout());
         p.setBorder(BorderFactory.createLoweredBevelBorder());
         JComponent c = new History();
         p.add(c);
         constraints.weightx = 0.7;
         constraints.setGridBounds(1, 0, 1, 1);
         this.add(p, constraints);
         l.add(c);
         this.repaintThread = new RepaintThread(time, l);
      }

      public RepaintThread getRepaintThread() {
         return this.repaintThread;
      }
   }

   protected class CloseButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         MemoryMonitor.this.panel.getRepaintThread().safeSuspend();
         MemoryMonitor.this.dispose();
      }
   }

   protected static class CollectButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         System.gc();
      }
   }
}
