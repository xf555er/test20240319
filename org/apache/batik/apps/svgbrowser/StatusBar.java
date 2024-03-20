package org.apache.batik.apps.svgbrowser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import org.apache.batik.util.resources.ResourceManager;

public class StatusBar extends JPanel {
   protected static final String RESOURCES = "org.apache.batik.apps.svgbrowser.resources.StatusBarMessages";
   protected static ResourceBundle bundle = ResourceBundle.getBundle("org.apache.batik.apps.svgbrowser.resources.StatusBarMessages", Locale.getDefault());
   protected static ResourceManager rManager;
   protected JLabel xPosition;
   protected JLabel yPosition;
   protected JLabel zoom;
   protected JLabel message;
   protected String mainMessage;
   protected String temporaryMessage;
   protected DisplayThread displayThread;

   public StatusBar() {
      super(new BorderLayout(5, 5));
      JPanel p = new JPanel(new BorderLayout(0, 0));
      this.add("West", p);
      this.xPosition = new JLabel();
      BevelBorder bb = new BevelBorder(1, this.getBackground().brighter().brighter(), this.getBackground(), this.getBackground().darker().darker(), this.getBackground());
      this.xPosition.setBorder(bb);
      this.xPosition.setPreferredSize(new Dimension(110, 16));
      p.add("West", this.xPosition);
      this.yPosition = new JLabel();
      this.yPosition.setBorder(bb);
      this.yPosition.setPreferredSize(new Dimension(110, 16));
      p.add("Center", this.yPosition);
      this.zoom = new JLabel();
      this.zoom.setBorder(bb);
      this.zoom.setPreferredSize(new Dimension(70, 16));
      p.add("East", this.zoom);
      p = new JPanel(new BorderLayout(0, 0));
      this.message = new JLabel();
      this.message.setBorder(bb);
      p.add(this.message);
      this.add(p);
      this.setMainMessage(rManager.getString("Panel.default_message"));
   }

   public void setXPosition(float x) {
      this.xPosition.setText("x: " + x);
   }

   public void setWidth(float w) {
      this.xPosition.setText(rManager.getString("Position.width_letters") + " " + w);
   }

   public void setYPosition(float y) {
      this.yPosition.setText("y: " + y);
   }

   public void setHeight(float h) {
      this.yPosition.setText(rManager.getString("Position.height_letters") + " " + h);
   }

   public void setZoom(float f) {
      f = f > 0.0F ? f : -f;
      if (f == 1.0F) {
         this.zoom.setText("1:1");
      } else {
         String s;
         if (f >= 1.0F) {
            s = Float.toString(f);
            if (s.length() > 6) {
               s = s.substring(0, 6);
            }

            this.zoom.setText("1:" + s);
         } else {
            s = Float.toString(1.0F / f);
            if (s.length() > 6) {
               s = s.substring(0, 6);
            }

            this.zoom.setText(s + ":1");
         }
      }

   }

   public void setMessage(String s) {
      this.setPreferredSize(new Dimension(0, this.getPreferredSize().height));
      if (this.displayThread != null) {
         this.displayThread.finish();
      }

      this.temporaryMessage = s;
      Thread old = this.displayThread;
      this.displayThread = new DisplayThread(old);
      this.displayThread.start();
   }

   public void setMainMessage(String s) {
      this.mainMessage = s;
      this.message.setText(this.mainMessage = s);
      if (this.displayThread != null) {
         this.displayThread.finish();
         this.displayThread = null;
      }

      this.setPreferredSize(new Dimension(0, this.getPreferredSize().height));
   }

   static {
      rManager = new ResourceManager(bundle);
   }

   protected class DisplayThread extends Thread {
      static final long DEFAULT_DURATION = 5000L;
      long duration;
      Thread toJoin;

      public DisplayThread() {
         this(5000L, (Thread)null);
      }

      public DisplayThread(long duration) {
         this(duration, (Thread)null);
      }

      public DisplayThread(Thread toJoin) {
         this(5000L, toJoin);
      }

      public DisplayThread(long duration, Thread toJoin) {
         this.duration = duration;
         this.toJoin = toJoin;
         this.setPriority(1);
      }

      public synchronized void finish() {
         this.duration = 0L;
         this.notifyAll();
      }

      public void run() {
         synchronized(this) {
            if (this.toJoin != null) {
               while(this.toJoin.isAlive()) {
                  try {
                     this.toJoin.join();
                  } catch (InterruptedException var8) {
                  }
               }

               this.toJoin = null;
            }

            StatusBar.this.message.setText(StatusBar.this.temporaryMessage);

            long cTime;
            for(long lTime = System.currentTimeMillis(); this.duration > 0L; lTime = cTime) {
               try {
                  this.wait(this.duration);
               } catch (InterruptedException var7) {
               }

               cTime = System.currentTimeMillis();
               this.duration -= cTime - lTime;
            }

            StatusBar.this.message.setText(StatusBar.this.mainMessage);
         }
      }
   }
}
