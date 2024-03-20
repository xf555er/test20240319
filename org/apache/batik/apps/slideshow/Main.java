package org.apache.batik.apps.slideshow;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JWindow;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.bridge.ViewBox;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.renderer.StaticRenderer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;

public class Main extends JComponent {
   StaticRenderer renderer;
   UserAgent userAgent;
   DocumentLoader loader;
   BridgeContext ctx;
   BufferedImage image;
   BufferedImage display;
   File[] files;
   static int duration = 3000;
   static int frameDelay;
   volatile boolean done = false;
   volatile Thread transitionThread = null;
   long startLastTransition = 0L;
   volatile boolean paused = false;

   public Main(File[] files, Dimension size) {
      this.setBackground(Color.black);
      this.files = files;
      UserAgentAdapter ua = new UserAgentAdapter();
      this.renderer = new StaticRenderer();
      this.userAgent = ua;
      this.loader = new DocumentLoader(this.userAgent);
      this.ctx = new BridgeContext(this.userAgent, this.loader);
      ua.setBridgeContext(this.ctx);
      if (size == null) {
         size = Toolkit.getDefaultToolkit().getScreenSize();
      }

      this.setPreferredSize(size);
      this.setDoubleBuffered(false);
      this.addMouseListener(new MouseAdapter() {
         public void mouseClicked(MouseEvent me) {
            if (Main.this.done) {
               System.exit(0);
            } else {
               Main.this.togglePause();
            }

         }
      });
      size.width += 2;
      size.height += 2;
      this.display = new BufferedImage(size.width, size.height, 4);
      Thread t = new RenderThread();
      t.start();
      JWindow w = new JWindow();
      w.setBackground(Color.black);
      w.getContentPane().setBackground(Color.black);
      w.getContentPane().add(this);
      w.pack();
      w.setLocation(new Point(-1, -1));
      w.setVisible(true);
   }

   public void setTransition(BufferedImage newImg) {
      synchronized(this) {
         while(this.transitionThread != null) {
            try {
               this.wait();
            } catch (InterruptedException var5) {
            }
         }

         this.transitionThread = new TransitionThread(newImg);
         this.transitionThread.start();
      }
   }

   public void togglePause() {
      synchronized(this) {
         this.paused = !this.paused;
         Cursor c;
         if (this.paused) {
            c = new Cursor(3);
         } else {
            c = new Cursor(0);
            if (this.transitionThread != null) {
               synchronized(this.transitionThread) {
                  this.transitionThread.notifyAll();
               }
            }
         }

         this.setCursor(c);
      }
   }

   public void paint(Graphics g) {
      Graphics2D g2d = (Graphics2D)g;
      if (this.display != null) {
         g2d.drawImage(this.display, (BufferedImageOp)null, 0, 0);
      }
   }

   public static void readFileList(String file, List fileVec) {
      BufferedReader br;
      try {
         br = new BufferedReader(new FileReader(file));
      } catch (FileNotFoundException var19) {
         System.err.println("Unable to open file-list: " + file);
         return;
      }

      try {
         URL flURL = (new File(file)).toURI().toURL();

         String line;
         while((line = br.readLine()) != null) {
            String str = line;
            int idx = line.indexOf(35);
            if (idx != -1) {
               str = line.substring(0, idx);
            }

            str = str.trim();
            if (str.length() != 0) {
               try {
                  URL imgURL = new URL(flURL, str);
                  fileVec.add(imgURL.getFile());
               } catch (MalformedURLException var18) {
                  System.err.println("Can't make sense of line:\n  " + line);
               }
            }
         }
      } catch (IOException var20) {
         System.err.println("Error while reading file-list: " + file);
      } finally {
         try {
            br.close();
         } catch (IOException var17) {
         }

      }

   }

   public static void main(String[] args) {
      List fileVec = new ArrayList();
      Dimension d = null;
      if (args.length == 0) {
         showUsage();
      } else {
         int idx;
         label114:
         for(int i = 0; i < args.length; ++i) {
            if (args[i].equals("-h") || args[i].equals("-help") || args[i].equals("--help")) {
               showUsage();
               return;
            }

            if (args[i].equals("--")) {
               ++i;

               while(true) {
                  if (i >= args.length) {
                     break label114;
                  }

                  fileVec.add(args[i++]);
               }
            }

            if (!args[i].equals("-fl") && !args[i].equals("--file-list")) {
               if (!args[i].equals("-ft") && !args[i].equals("--frame-time")) {
                  if (!args[i].equals("-tt") && !args[i].equals("--transition-time")) {
                     if (!args[i].equals("-ws") && !args[i].equals("--window-size")) {
                        fileVec.add(args[i]);
                     } else {
                        if (i + 1 == args.length) {
                           System.err.println("Must provide window size [w,h] after " + args[i]);
                           break;
                        }

                        try {
                           idx = args[i + 1].indexOf(44);
                           int w;
                           int h;
                           if (idx == -1) {
                              w = h = Integer.decode(args[i + 1]);
                           } else {
                              String wStr = args[i + 1].substring(0, idx);
                              String hStr = args[i + 1].substring(idx + 1);
                              w = Integer.decode(wStr);
                              h = Integer.decode(hStr);
                           }

                           d = new Dimension(w, h);
                           ++i;
                        } catch (NumberFormatException var10) {
                           System.err.println("Can't parse window size: " + args[i + 1]);
                        }
                     }
                  } else {
                     if (i + 1 == args.length) {
                        System.err.println("Must provide time in millis after " + args[i]);
                        break;
                     }

                     try {
                        duration = Integer.decode(args[i + 1]);
                        ++i;
                     } catch (NumberFormatException var11) {
                        System.err.println("Can't parse transition time: " + args[i + 1]);
                     }
                  }
               } else {
                  if (i + 1 == args.length) {
                     System.err.println("Must provide time in millis after " + args[i]);
                     break;
                  }

                  try {
                     frameDelay = Integer.decode(args[i + 1]);
                     ++i;
                  } catch (NumberFormatException var12) {
                     System.err.println("Can't parse frame time: " + args[i + 1]);
                  }
               }
            } else {
               if (i + 1 == args.length) {
                  System.err.println("Must provide name of file list file after " + args[i]);
                  break;
               }

               readFileList(args[i + 1], fileVec);
               ++i;
            }
         }

         File[] files = new File[fileVec.size()];

         for(idx = 0; idx < fileVec.size(); ++idx) {
            try {
               files[idx] = new File((String)fileVec.get(idx));
            } catch (Exception var9) {
               var9.printStackTrace();
            }
         }

         new Main(files, d);
      }
   }

   public static void showUsage() {
      System.out.println("Options:\n                                 -- : Remaining args are file names\n                         -fl <file>\n                 --file-list <file> : file contains list of images to\n                                      show one per line\n             -ws <width>[,<height>]\n    -window-size <width>[,<height>] : Set the size of slideshow window\n                                      defaults to full screen\n                          -ft <int>\n                 --frame-time <int> : Amount of time in millisecs to\n                                      show each frame.\n                                      Includes transition time.\n                          -tt <int>\n            --transition-time <int> : Amount of time in millisecs to\n                                      transition between frames.\n                             <file> : SVG file to display");
   }

   static {
      frameDelay = duration + 7000;
   }

   class TransitionThread extends Thread {
      BufferedImage src;
      int blockw = 75;
      int blockh = 75;

      public TransitionThread(BufferedImage bi) {
         super("TransitionThread");
         this.setDaemon(true);
         this.src = bi;
      }

      public void run() {
         int xblocks = (Main.this.display.getWidth() + this.blockw - 1) / this.blockw;
         int yblocks = (Main.this.display.getHeight() + this.blockh - 1) / this.blockh;
         int nblocks = xblocks * yblocks;
         int tblock = Main.duration / nblocks;
         Point[] rects = new Point[nblocks];

         for(int y = 0; y < yblocks; ++y) {
            for(int x = 0; x < xblocks; ++x) {
               rects[y * xblocks + x] = new Point(x, y);
            }
         }

         Graphics2D g2d = Main.this.display.createGraphics();
         g2d.setColor(Color.black);

         long last;
         for(long currTrans = System.currentTimeMillis(); currTrans - Main.this.startLastTransition < (long)Main.frameDelay; currTrans = System.currentTimeMillis()) {
            try {
               last = (long)Main.frameDelay - (currTrans - Main.this.startLastTransition);
               if (last > 500L) {
                  System.gc();
                  currTrans = System.currentTimeMillis();
                  last = (long)Main.frameDelay - (currTrans - Main.this.startLastTransition);
               }

               if (last > 0L) {
                  sleep(last);
               }
            } catch (InterruptedException var27) {
            }
         }

         synchronized(this) {
            while(Main.this.paused) {
               try {
                  this.wait();
               } catch (InterruptedException var26) {
               }
            }
         }

         last = Main.this.startLastTransition = System.currentTimeMillis();

         for(int i = 0; i < rects.length; ++i) {
            int idx = (int)(Math.random() * (double)(rects.length - i));
            Point pt = rects[idx];
            System.arraycopy(rects, idx + 1, rects, idx + 1 - 1, rects.length - i - idx - 1);
            int xx = pt.x * this.blockw;
            int yx = pt.y * this.blockh;
            int w = this.blockw;
            int h = this.blockh;
            if (xx + w > this.src.getWidth()) {
               w = this.src.getWidth() - xx;
            }

            if (yx + h > this.src.getHeight()) {
               h = this.src.getHeight() - yx;
            }

            synchronized(Main.this.display) {
               g2d.fillRect(xx, yx, w, h);
               BufferedImage sub = this.src.getSubimage(xx, yx, w, h);
               g2d.drawImage(sub, (BufferedImageOp)null, xx, yx);
            }

            Main.this.repaint(xx, yx, w, h);
            long current = System.currentTimeMillis();

            try {
               long dt = current - last;
               if (dt < (long)tblock) {
                  sleep((long)tblock - dt);
               }
            } catch (InterruptedException var24) {
            }

            last = current;
         }

         synchronized(Main.this) {
            Main.this.transitionThread = null;
            Main.this.notifyAll();
         }
      }
   }

   class RenderThread extends Thread {
      RenderThread() {
         super("RenderThread");
         this.setDaemon(true);
      }

      public void run() {
         Main.this.renderer.setDoubleBuffered(true);
         File[] var1 = Main.this.files;
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            File file = var1[var3];
            GraphicsNode gvtRoot = null;
            GVTBuilder builder = new GVTBuilder();

            try {
               String fileName = file.toURI().toURL().toString();
               System.out.println("Reading: " + fileName);
               Document svgDoc = Main.this.loader.loadDocument(fileName);
               System.out.println("Building: " + fileName);
               gvtRoot = builder.build(Main.this.ctx, svgDoc);
               System.out.println("Rendering: " + fileName);
               Main.this.renderer.setTree(gvtRoot);
               Element elt = ((SVGDocument)svgDoc).getRootElement();
               Main.this.renderer.setTransform(ViewBox.getViewTransform((String)null, elt, (float)Main.this.display.getWidth(), (float)Main.this.display.getHeight(), Main.this.ctx));
               Main.this.renderer.updateOffScreen(Main.this.display.getWidth(), Main.this.display.getHeight());
               Rectangle r = new Rectangle(0, 0, Main.this.display.getWidth(), Main.this.display.getHeight());
               Main.this.renderer.repaint((Shape)r);
               System.out.println("Painting: " + fileName);
               Main.this.image = Main.this.renderer.getOffScreen();
               Main.this.setTransition(Main.this.image);
            } catch (Exception var12) {
               var12.printStackTrace();
            }
         }

         if (Main.this.transitionThread != null) {
            try {
               Main.this.transitionThread.join();
            } catch (InterruptedException var11) {
            }

            Main.this.done = true;
            Main.this.setCursor(new Cursor(3));
         }

      }
   }
}
