package org.apache.batik.apps.svgbrowser;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.print.PrinterException;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import org.apache.batik.anim.dom.SVGOMDocument;
import org.apache.batik.bridge.DefaultExternalResourceSecurity;
import org.apache.batik.bridge.DefaultScriptSecurity;
import org.apache.batik.bridge.EmbededExternalResourceSecurity;
import org.apache.batik.bridge.EmbededScriptSecurity;
import org.apache.batik.bridge.ExternalResourceSecurity;
import org.apache.batik.bridge.NoLoadExternalResourceSecurity;
import org.apache.batik.bridge.NoLoadScriptSecurity;
import org.apache.batik.bridge.RelaxedExternalResourceSecurity;
import org.apache.batik.bridge.RelaxedScriptSecurity;
import org.apache.batik.bridge.ScriptSecurity;
import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.bridge.UpdateManagerEvent;
import org.apache.batik.bridge.UpdateManagerListener;
import org.apache.batik.dom.StyleSheetProcessingInstruction;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.ext.swing.JAffineTransformChooser;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.gvt.GVTTreeRendererListener;
import org.apache.batik.swing.gvt.Overlay;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderListener;
import org.apache.batik.swing.svg.LinkActivationEvent;
import org.apache.batik.swing.svg.LinkActivationListener;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderListener;
import org.apache.batik.swing.svg.SVGFileFilter;
import org.apache.batik.swing.svg.SVGLoadEventDispatcherEvent;
import org.apache.batik.swing.svg.SVGLoadEventDispatcherListener;
import org.apache.batik.swing.svg.SVGUserAgent;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.TIFFTranscoder;
import org.apache.batik.transcoder.print.PrintTranscoder;
import org.apache.batik.transcoder.svg2svg.SVGTranscoder;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.Platform;
import org.apache.batik.util.Service;
import org.apache.batik.util.gui.JErrorPane;
import org.apache.batik.util.gui.LocationBar;
import org.apache.batik.util.gui.MemoryMonitor;
import org.apache.batik.util.gui.URIChooser;
import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.JComponentModifier;
import org.apache.batik.util.gui.resource.MenuFactory;
import org.apache.batik.util.gui.resource.MissingListenerException;
import org.apache.batik.util.gui.resource.ToolBarFactory;
import org.apache.batik.util.gui.xmleditor.XMLDocument;
import org.apache.batik.util.gui.xmleditor.XMLTextEditor;
import org.apache.batik.util.resources.ResourceManager;
import org.apache.batik.xml.XMLUtilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.css.ViewCSS;
import org.w3c.dom.svg.SVGDocument;

public class JSVGViewerFrame extends JFrame implements ActionMap, SVGDocumentLoaderListener, GVTTreeBuilderListener, SVGLoadEventDispatcherListener, GVTTreeRendererListener, LinkActivationListener, UpdateManagerListener {
   private static String EOL;
   protected static boolean priorJDK1_4;
   protected static final String JDK_1_4_PRESENCE_TEST_CLASS = "java.util.logging.LoggingPermission";
   public static final String RESOURCES = "org.apache.batik.apps.svgbrowser.resources.GUI";
   public static final String ABOUT_ACTION = "AboutAction";
   public static final String OPEN_ACTION = "OpenAction";
   public static final String OPEN_LOCATION_ACTION = "OpenLocationAction";
   public static final String NEW_WINDOW_ACTION = "NewWindowAction";
   public static final String RELOAD_ACTION = "ReloadAction";
   public static final String SAVE_AS_ACTION = "SaveAsAction";
   public static final String BACK_ACTION = "BackAction";
   public static final String FORWARD_ACTION = "ForwardAction";
   public static final String FULL_SCREEN_ACTION = "FullScreenAction";
   public static final String PRINT_ACTION = "PrintAction";
   public static final String EXPORT_AS_JPG_ACTION = "ExportAsJPGAction";
   public static final String EXPORT_AS_PNG_ACTION = "ExportAsPNGAction";
   public static final String EXPORT_AS_TIFF_ACTION = "ExportAsTIFFAction";
   public static final String PREFERENCES_ACTION = "PreferencesAction";
   public static final String CLOSE_ACTION = "CloseAction";
   public static final String VIEW_SOURCE_ACTION = "ViewSourceAction";
   public static final String EXIT_ACTION = "ExitAction";
   public static final String RESET_TRANSFORM_ACTION = "ResetTransformAction";
   public static final String ZOOM_IN_ACTION = "ZoomInAction";
   public static final String ZOOM_OUT_ACTION = "ZoomOutAction";
   public static final String PREVIOUS_TRANSFORM_ACTION = "PreviousTransformAction";
   public static final String NEXT_TRANSFORM_ACTION = "NextTransformAction";
   public static final String USE_STYLESHEET_ACTION = "UseStylesheetAction";
   public static final String PLAY_ACTION = "PlayAction";
   public static final String PAUSE_ACTION = "PauseAction";
   public static final String STOP_ACTION = "StopAction";
   public static final String MONITOR_ACTION = "MonitorAction";
   public static final String DOM_VIEWER_ACTION = "DOMViewerAction";
   public static final String SET_TRANSFORM_ACTION = "SetTransformAction";
   public static final String FIND_DIALOG_ACTION = "FindDialogAction";
   public static final String THUMBNAIL_DIALOG_ACTION = "ThumbnailDialogAction";
   public static final String FLUSH_ACTION = "FlushAction";
   public static final String TOGGLE_DEBUGGER_ACTION = "ToggleDebuggerAction";
   public static final Cursor WAIT_CURSOR;
   public static final Cursor DEFAULT_CURSOR;
   public static final String PROPERTY_OS_NAME;
   public static final String PROPERTY_OS_NAME_DEFAULT;
   public static final String PROPERTY_OS_WINDOWS_PREFIX;
   protected static final String OPEN_TITLE = "Open.title";
   protected static Vector handlers;
   protected static SquiggleInputHandler defaultHandler;
   protected static ResourceBundle bundle;
   protected static ResourceManager resources;
   protected Application application;
   protected Canvas svgCanvas;
   protected JPanel svgCanvasPanel;
   protected JWindow window;
   protected static JFrame memoryMonitorFrame;
   protected File currentPath = new File("");
   protected File currentSavePath = new File("");
   protected BackAction backAction = new BackAction();
   protected ForwardAction forwardAction = new ForwardAction();
   protected PlayAction playAction = new PlayAction();
   protected PauseAction pauseAction = new PauseAction();
   protected StopAction stopAction = new StopAction();
   protected PreviousTransformAction previousTransformAction = new PreviousTransformAction();
   protected NextTransformAction nextTransformAction = new NextTransformAction();
   protected UseStylesheetAction useStylesheetAction = new UseStylesheetAction();
   protected boolean debug;
   protected boolean autoAdjust = true;
   protected boolean managerStopped;
   protected SVGUserAgent userAgent = new UserAgent();
   protected SVGDocument svgDocument;
   protected URIChooser uriChooser;
   protected DOMViewer domViewer;
   protected FindDialog findDialog;
   protected ThumbnailDialog thumbnailDialog;
   protected JAffineTransformChooser.Dialog transformDialog;
   protected LocationBar locationBar;
   protected StatusBar statusBar;
   protected String title;
   protected LocalHistory localHistory;
   protected TransformHistory transformHistory = new TransformHistory();
   protected String alternateStyleSheet;
   protected Debugger debugger;
   protected Map listeners = new HashMap();
   long time;

   public JSVGViewerFrame(Application app) {
      this.application = app;
      this.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
            JSVGViewerFrame.this.application.closeJSVGViewerFrame(JSVGViewerFrame.this);
         }
      });
      this.svgCanvas = new Canvas(this.userAgent, true, true) {
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

         {
            this.setMaximumSize(this.screenSize);
         }

         public Dimension getPreferredSize() {
            Dimension s = super.getPreferredSize();
            if (s.width > this.screenSize.width) {
               s.width = this.screenSize.width;
            }

            if (s.height > this.screenSize.height) {
               s.height = this.screenSize.height;
            }

            return s;
         }

         public void setMySize(Dimension d) {
            this.setPreferredSize(d);
            this.invalidate();
            if (JSVGViewerFrame.this.autoAdjust) {
               JSVGViewerFrame.this.setExtendedState(JSVGViewerFrame.this.getExtendedState() & -7);
               JSVGViewerFrame.this.pack();
            }

         }

         public void setDisableInteractions(boolean b) {
            super.setDisableInteractions(b);
            ((Action)JSVGViewerFrame.this.listeners.get("SetTransformAction")).setEnabled(!b);
            if (JSVGViewerFrame.this.thumbnailDialog != null) {
               JSVGViewerFrame.this.thumbnailDialog.setInteractionEnabled(!b);
            }

         }
      };
      javax.swing.ActionMap map = this.svgCanvas.getActionMap();
      map.put("FullScreenAction", new FullScreenAction());
      InputMap imap = this.svgCanvas.getInputMap(0);
      KeyStroke key = KeyStroke.getKeyStroke(122, 0);
      imap.put(key, "FullScreenAction");
      this.svgCanvas.setDoubleBufferedRendering(true);
      this.listeners.put("AboutAction", new AboutAction());
      this.listeners.put("OpenAction", new OpenAction());
      this.listeners.put("OpenLocationAction", new OpenLocationAction());
      this.listeners.put("NewWindowAction", new NewWindowAction());
      this.listeners.put("ReloadAction", new ReloadAction());
      this.listeners.put("SaveAsAction", new SaveAsAction());
      this.listeners.put("BackAction", this.backAction);
      this.listeners.put("ForwardAction", this.forwardAction);
      this.listeners.put("PrintAction", new PrintAction());
      this.listeners.put("ExportAsJPGAction", new ExportAsJPGAction());
      this.listeners.put("ExportAsPNGAction", new ExportAsPNGAction());
      this.listeners.put("ExportAsTIFFAction", new ExportAsTIFFAction());
      this.listeners.put("PreferencesAction", new PreferencesAction());
      this.listeners.put("CloseAction", new CloseAction());
      this.listeners.put("ExitAction", this.application.createExitAction(this));
      this.listeners.put("ViewSourceAction", new ViewSourceAction());
      javax.swing.ActionMap cMap = this.svgCanvas.getActionMap();
      this.listeners.put("ResetTransformAction", cMap.get("ResetTransform"));
      this.listeners.put("ZoomInAction", cMap.get("ZoomIn"));
      this.listeners.put("ZoomOutAction", cMap.get("ZoomOut"));
      this.listeners.put("PreviousTransformAction", this.previousTransformAction);
      key = KeyStroke.getKeyStroke(75, 128);
      imap.put(key, this.previousTransformAction);
      this.listeners.put("NextTransformAction", this.nextTransformAction);
      key = KeyStroke.getKeyStroke(76, 128);
      imap.put(key, this.nextTransformAction);
      this.listeners.put("UseStylesheetAction", this.useStylesheetAction);
      this.listeners.put("PlayAction", this.playAction);
      this.listeners.put("PauseAction", this.pauseAction);
      this.listeners.put("StopAction", this.stopAction);
      this.listeners.put("MonitorAction", new MonitorAction());
      this.listeners.put("DOMViewerAction", new DOMViewerAction());
      this.listeners.put("SetTransformAction", new SetTransformAction());
      this.listeners.put("FindDialogAction", new FindDialogAction());
      this.listeners.put("ThumbnailDialogAction", new ThumbnailDialogAction());
      this.listeners.put("FlushAction", new FlushAction());
      this.listeners.put("ToggleDebuggerAction", new ToggleDebuggerAction());
      JPanel p = null;

      try {
         MenuFactory mf = new MenuFactory(bundle, this);
         JMenuBar mb = mf.createJMenuBar("MenuBar", this.application.getUISpecialization());
         this.setJMenuBar(mb);
         this.localHistory = new LocalHistory(mb, this);
         String[] uri = this.application.getVisitedURIs();
         String[] var10 = uri;
         int var11 = uri.length;

         for(int var12 = 0; var12 < var11; ++var12) {
            String anUri = var10[var12];
            if (anUri != null && !"".equals(anUri)) {
               this.localHistory.update(anUri);
            }
         }

         p = new JPanel(new BorderLayout());
         ToolBarFactory tbf = new ToolBarFactory(bundle, this);
         JToolBar tb = tbf.createJToolBar("ToolBar");
         tb.setFloatable(false);
         this.getContentPane().add(p, "North");
         p.add(tb, "North");
         p.add(new JSeparator(), "Center");
         p.add(this.locationBar = new LocationBar(), "South");
         this.locationBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      } catch (MissingResourceException var14) {
         System.out.println(var14.getMessage());
         System.exit(0);
      }

      this.svgCanvasPanel = new JPanel(new BorderLayout());
      this.svgCanvasPanel.setBorder(BorderFactory.createEtchedBorder());
      this.svgCanvasPanel.add(this.svgCanvas, "Center");
      p = new JPanel(new BorderLayout());
      p.add(this.svgCanvasPanel, "Center");
      p.add(this.statusBar = new StatusBar(), "South");
      this.getContentPane().add(p, "Center");
      this.svgCanvas.addSVGDocumentLoaderListener(this);
      this.svgCanvas.addGVTTreeBuilderListener(this);
      this.svgCanvas.addSVGLoadEventDispatcherListener(this);
      this.svgCanvas.addGVTTreeRendererListener(this);
      this.svgCanvas.addLinkActivationListener(this);
      this.svgCanvas.addUpdateManagerListener(this);
      this.svgCanvas.addMouseMotionListener(new MouseMotionAdapter() {
         public void mouseMoved(MouseEvent e) {
            if (JSVGViewerFrame.this.svgDocument == null) {
               JSVGViewerFrame.this.statusBar.setXPosition((float)e.getX());
               JSVGViewerFrame.this.statusBar.setYPosition((float)e.getY());
            } else {
               try {
                  AffineTransform at = JSVGViewerFrame.this.svgCanvas.getViewBoxTransform();
                  if (at != null) {
                     at = at.createInverse();
                     Point2D p2d = at.transform(new Point2D.Float((float)e.getX(), (float)e.getY()), (Point2D)null);
                     JSVGViewerFrame.this.statusBar.setXPosition((float)p2d.getX());
                     JSVGViewerFrame.this.statusBar.setYPosition((float)p2d.getY());
                     return;
                  }
               } catch (NoninvertibleTransformException var4) {
               }

               JSVGViewerFrame.this.statusBar.setXPosition((float)e.getX());
               JSVGViewerFrame.this.statusBar.setYPosition((float)e.getY());
            }

         }
      });
      this.svgCanvas.addMouseListener(new MouseAdapter() {
         public void mouseExited(MouseEvent e) {
            Dimension dim = JSVGViewerFrame.this.svgCanvas.getSize();
            if (JSVGViewerFrame.this.svgDocument == null) {
               JSVGViewerFrame.this.statusBar.setWidth((float)dim.width);
               JSVGViewerFrame.this.statusBar.setHeight((float)dim.height);
            } else {
               try {
                  AffineTransform at = JSVGViewerFrame.this.svgCanvas.getViewBoxTransform();
                  if (at != null) {
                     at = at.createInverse();
                     Point2D o = at.transform(new Point2D.Float(0.0F, 0.0F), (Point2D)null);
                     Point2D p2d = at.transform(new Point2D.Float((float)dim.width, (float)dim.height), (Point2D)null);
                     JSVGViewerFrame.this.statusBar.setWidth((float)(p2d.getX() - o.getX()));
                     JSVGViewerFrame.this.statusBar.setHeight((float)(p2d.getY() - o.getY()));
                     return;
                  }
               } catch (NoninvertibleTransformException var6) {
               }

               JSVGViewerFrame.this.statusBar.setWidth((float)dim.width);
               JSVGViewerFrame.this.statusBar.setHeight((float)dim.height);
            }

         }
      });
      this.svgCanvas.addComponentListener(new ComponentAdapter() {
         public void componentResized(ComponentEvent e) {
            Dimension dim = JSVGViewerFrame.this.svgCanvas.getSize();
            if (JSVGViewerFrame.this.svgDocument == null) {
               JSVGViewerFrame.this.statusBar.setWidth((float)dim.width);
               JSVGViewerFrame.this.statusBar.setHeight((float)dim.height);
            } else {
               try {
                  AffineTransform at = JSVGViewerFrame.this.svgCanvas.getViewBoxTransform();
                  if (at != null) {
                     at = at.createInverse();
                     Point2D o = at.transform(new Point2D.Float(0.0F, 0.0F), (Point2D)null);
                     Point2D p2d = at.transform(new Point2D.Float((float)dim.width, (float)dim.height), (Point2D)null);
                     JSVGViewerFrame.this.statusBar.setWidth((float)(p2d.getX() - o.getX()));
                     JSVGViewerFrame.this.statusBar.setHeight((float)(p2d.getY() - o.getY()));
                     return;
                  }
               } catch (NoninvertibleTransformException var6) {
               }

               JSVGViewerFrame.this.statusBar.setWidth((float)dim.width);
               JSVGViewerFrame.this.statusBar.setHeight((float)dim.height);
            }

         }
      });
      this.locationBar.addActionListener(new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
            String st = JSVGViewerFrame.this.locationBar.getText().trim();
            int i = st.indexOf(35);
            String t = "";
            if (i != -1) {
               t = st.substring(i + 1);
               st = st.substring(0, i);
            }

            if (!st.equals("")) {
               try {
                  File f = new File(st);
                  if (f.exists()) {
                     if (f.isDirectory()) {
                        return;
                     }

                     try {
                        st = f.getCanonicalPath();
                        if (st.startsWith("/")) {
                           st = "file:" + st;
                        } else {
                           st = "file:/" + st;
                        }
                     } catch (IOException var8) {
                     }
                  }
               } catch (SecurityException var9) {
               }

               String fi = JSVGViewerFrame.this.svgCanvas.getFragmentIdentifier();
               if (JSVGViewerFrame.this.svgDocument != null) {
                  ParsedURL docPURL = new ParsedURL(JSVGViewerFrame.this.svgDocument.getURL());
                  ParsedURL purl = new ParsedURL(docPURL, st);
                  fi = fi == null ? "" : fi;
                  if (docPURL.equals(purl) && t.equals(fi)) {
                     return;
                  }
               }

               if (t.length() != 0) {
                  st = st + '#' + t;
               }

               JSVGViewerFrame.this.locationBar.setText(st);
               JSVGViewerFrame.this.locationBar.addToHistory(st);
               JSVGViewerFrame.this.showSVGDocument(st);
            }
         }
      });
   }

   public void dispose() {
      this.hideDebugger();
      this.svgCanvas.dispose();
      super.dispose();
   }

   public void setDebug(boolean b) {
      this.debug = b;
   }

   public void setAutoAdjust(boolean b) {
      this.autoAdjust = b;
   }

   public JSVGCanvas getJSVGCanvas() {
      return this.svgCanvas;
   }

   private static File makeAbsolute(File f) {
      return !f.isAbsolute() ? f.getAbsoluteFile() : f;
   }

   public void showDebugger() {
      if (this.debugger == null && JSVGViewerFrame.Debugger.isPresent) {
         this.debugger = new Debugger(this, this.locationBar.getText());
         this.debugger.initialize();
      }

   }

   public void hideDebugger() {
      if (this.debugger != null) {
         this.debugger.clearAllBreakpoints();
         this.debugger.go();
         this.debugger.dispose();
         this.debugger = null;
      }

   }

   public void showSVGDocument(String uri) {
      try {
         ParsedURL purl = new ParsedURL(uri);
         SquiggleInputHandler handler = this.getInputHandler(purl);
         handler.handle(purl, this);
      } catch (Exception var4) {
         if (this.userAgent != null) {
            this.userAgent.displayError(var4);
         }
      }

   }

   public SquiggleInputHandler getInputHandler(ParsedURL purl) throws IOException {
      Iterator iter = getHandlers().iterator();
      SquiggleInputHandler handler = null;

      while(iter.hasNext()) {
         SquiggleInputHandler curHandler = (SquiggleInputHandler)iter.next();
         if (curHandler.accept(purl)) {
            handler = curHandler;
            break;
         }
      }

      if (handler == null) {
         handler = defaultHandler;
      }

      return handler;
   }

   protected static Vector getHandlers() {
      if (handlers != null) {
         return handlers;
      } else {
         handlers = new Vector();
         registerHandler(new SVGInputHandler());
         Iterator iter = Service.providers(SquiggleInputHandler.class);

         while(iter.hasNext()) {
            SquiggleInputHandler handler = (SquiggleInputHandler)iter.next();
            registerHandler(handler);
         }

         return handlers;
      }
   }

   public static synchronized void registerHandler(SquiggleInputHandler handler) {
      Vector handlers = getHandlers();
      handlers.addElement(handler);
   }

   public Action getAction(String key) throws MissingListenerException {
      Action result = (Action)this.listeners.get(key);
      if (result == null) {
         throw new MissingListenerException("Can't find action.", "org.apache.batik.apps.svgbrowser.resources.GUI", key);
      } else {
         return result;
      }
   }

   public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
      String msg = resources.getString("Message.documentLoad");
      if (this.debug) {
         System.out.println(msg);
         this.time = System.currentTimeMillis();
      }

      this.statusBar.setMainMessage(msg);
      this.stopAction.update(true);
      this.svgCanvas.setCursor(WAIT_CURSOR);
   }

   public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
      if (this.debug) {
         System.out.print(resources.getString("Message.documentLoadTime"));
         System.out.println(System.currentTimeMillis() - this.time + " ms");
      }

      this.setSVGDocument(e.getSVGDocument(), e.getSVGDocument().getURL(), e.getSVGDocument().getTitle());
   }

   public void setSVGDocument(SVGDocument svgDocument, String svgDocumentURL, String svgDocumentTitle) {
      this.svgDocument = svgDocument;
      if (this.domViewer != null) {
         if (this.domViewer.isVisible() && svgDocument != null) {
            this.domViewer.setDocument(svgDocument, (ViewCSS)svgDocument.getDocumentElement());
         } else {
            this.domViewer.dispose();
            this.domViewer = null;
         }
      }

      this.stopAction.update(false);
      this.svgCanvas.setCursor(DEFAULT_CURSOR);
      this.locationBar.setText(svgDocumentURL);
      if (this.debugger != null) {
         this.debugger.detach();
         this.debugger.setDocumentURL(svgDocumentURL);
      }

      if (this.title == null) {
         this.title = this.getTitle();
      }

      if (svgDocumentTitle.length() != 0) {
         this.setTitle(this.title + ": " + svgDocumentTitle);
      } else {
         int i = svgDocumentURL.lastIndexOf("/");
         if (i == -1) {
            i = svgDocumentURL.lastIndexOf("\\");
         }

         if (i == -1) {
            this.setTitle(this.title + ": " + svgDocumentURL);
         } else {
            this.setTitle(this.title + ": " + svgDocumentURL.substring(i + 1));
         }
      }

      this.localHistory.update(svgDocumentURL);
      this.application.addVisitedURI(svgDocumentURL);
      this.backAction.update();
      this.forwardAction.update();
      this.transformHistory = new TransformHistory();
      this.previousTransformAction.update();
      this.nextTransformAction.update();
      this.useStylesheetAction.update();
   }

   public void documentLoadingCancelled(SVGDocumentLoaderEvent e) {
      String msg = resources.getString("Message.documentCancelled");
      if (this.debug) {
         System.out.println(msg);
      }

      this.statusBar.setMainMessage("");
      this.statusBar.setMessage(msg);
      this.stopAction.update(false);
      this.svgCanvas.setCursor(DEFAULT_CURSOR);
   }

   public void documentLoadingFailed(SVGDocumentLoaderEvent e) {
      String msg = resources.getString("Message.documentFailed");
      if (this.debug) {
         System.out.println(msg);
      }

      this.statusBar.setMainMessage("");
      this.statusBar.setMessage(msg);
      this.stopAction.update(false);
      this.svgCanvas.setCursor(DEFAULT_CURSOR);
   }

   public void gvtBuildStarted(GVTTreeBuilderEvent e) {
      String msg = resources.getString("Message.treeBuild");
      if (this.debug) {
         System.out.println(msg);
         this.time = System.currentTimeMillis();
      }

      this.statusBar.setMainMessage(msg);
      this.stopAction.update(true);
      this.svgCanvas.setCursor(WAIT_CURSOR);
   }

   public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
      if (this.debug) {
         System.out.print(resources.getString("Message.treeBuildTime"));
         System.out.println(System.currentTimeMillis() - this.time + " ms");
      }

      if (this.findDialog != null) {
         if (this.findDialog.isVisible()) {
            this.findDialog.setGraphicsNode(this.svgCanvas.getGraphicsNode());
         } else {
            this.findDialog.dispose();
            this.findDialog = null;
         }
      }

      this.stopAction.update(false);
      this.svgCanvas.setCursor(DEFAULT_CURSOR);
      this.svgCanvas.setSelectionOverlayXORMode(this.application.isSelectionOverlayXORMode());
      this.svgCanvas.requestFocus();
      if (this.debugger != null) {
         this.debugger.attach();
      }

   }

   public void gvtBuildCancelled(GVTTreeBuilderEvent e) {
      String msg = resources.getString("Message.treeCancelled");
      if (this.debug) {
         System.out.println(msg);
      }

      this.statusBar.setMainMessage("");
      this.statusBar.setMessage(msg);
      this.stopAction.update(false);
      this.svgCanvas.setCursor(DEFAULT_CURSOR);
      this.svgCanvas.setSelectionOverlayXORMode(this.application.isSelectionOverlayXORMode());
   }

   public void gvtBuildFailed(GVTTreeBuilderEvent e) {
      String msg = resources.getString("Message.treeFailed");
      if (this.debug) {
         System.out.println(msg);
      }

      this.statusBar.setMainMessage("");
      this.statusBar.setMessage(msg);
      this.stopAction.update(false);
      this.svgCanvas.setCursor(DEFAULT_CURSOR);
      this.svgCanvas.setSelectionOverlayXORMode(this.application.isSelectionOverlayXORMode());
      if (this.autoAdjust) {
         this.pack();
      }

   }

   public void svgLoadEventDispatchStarted(SVGLoadEventDispatcherEvent e) {
      String msg = resources.getString("Message.onload");
      if (this.debug) {
         System.out.println(msg);
         this.time = System.currentTimeMillis();
      }

      this.stopAction.update(true);
      this.statusBar.setMainMessage(msg);
   }

   public void svgLoadEventDispatchCompleted(SVGLoadEventDispatcherEvent e) {
      if (this.debug) {
         System.out.print(resources.getString("Message.onloadTime"));
         System.out.println(System.currentTimeMillis() - this.time + " ms");
      }

      this.stopAction.update(false);
      this.statusBar.setMainMessage("");
      this.statusBar.setMessage(resources.getString("Message.done"));
   }

   public void svgLoadEventDispatchCancelled(SVGLoadEventDispatcherEvent e) {
      String msg = resources.getString("Message.onloadCancelled");
      if (this.debug) {
         System.out.println(msg);
      }

      this.stopAction.update(false);
      this.statusBar.setMainMessage("");
      this.statusBar.setMessage(msg);
   }

   public void svgLoadEventDispatchFailed(SVGLoadEventDispatcherEvent e) {
      String msg = resources.getString("Message.onloadFailed");
      if (this.debug) {
         System.out.println(msg);
      }

      this.stopAction.update(false);
      this.statusBar.setMainMessage("");
      this.statusBar.setMessage(msg);
   }

   public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
      if (this.debug) {
         String msg = resources.getString("Message.treeRenderingPrep");
         System.out.println(msg);
         this.time = System.currentTimeMillis();
      }

      this.stopAction.update(true);
      this.svgCanvas.setCursor(WAIT_CURSOR);
      this.statusBar.setMainMessage(resources.getString("Message.treeRendering"));
   }

   public void gvtRenderingStarted(GVTTreeRendererEvent e) {
      if (this.debug) {
         String msg = resources.getString("Message.treeRenderingPrepTime");
         System.out.print(msg);
         System.out.println(System.currentTimeMillis() - this.time + " ms");
         this.time = System.currentTimeMillis();
         msg = resources.getString("Message.treeRenderingStart");
         System.out.println(msg);
      }

   }

   public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
      if (this.debug) {
         String msg = resources.getString("Message.treeRenderingTime");
         System.out.print(msg);
         System.out.println(System.currentTimeMillis() - this.time + " ms");
      }

      this.statusBar.setMainMessage("");
      this.statusBar.setMessage(resources.getString("Message.done"));
      if (!this.svgCanvas.isDynamic() || this.managerStopped) {
         this.stopAction.update(false);
      }

      this.svgCanvas.setCursor(DEFAULT_CURSOR);
      this.transformHistory.update(this.svgCanvas.getRenderingTransform());
      this.previousTransformAction.update();
      this.nextTransformAction.update();
   }

   public void gvtRenderingCancelled(GVTTreeRendererEvent e) {
      String msg = resources.getString("Message.treeRenderingCancelled");
      if (this.debug) {
         System.out.println(msg);
      }

      this.statusBar.setMainMessage("");
      this.statusBar.setMessage(msg);
      if (!this.svgCanvas.isDynamic()) {
         this.stopAction.update(false);
      }

      this.svgCanvas.setCursor(DEFAULT_CURSOR);
   }

   public void gvtRenderingFailed(GVTTreeRendererEvent e) {
      String msg = resources.getString("Message.treeRenderingFailed");
      if (this.debug) {
         System.out.println(msg);
      }

      this.statusBar.setMainMessage("");
      this.statusBar.setMessage(msg);
      if (!this.svgCanvas.isDynamic()) {
         this.stopAction.update(false);
      }

      this.svgCanvas.setCursor(DEFAULT_CURSOR);
   }

   public void linkActivated(LinkActivationEvent e) {
      String s = e.getReferencedURI();
      if (this.svgDocument != null) {
         ParsedURL docURL = new ParsedURL(this.svgDocument.getURL());
         ParsedURL url = new ParsedURL(docURL, s);
         if (!url.sameFile(docURL)) {
            return;
         }

         if (s.indexOf(35) != -1) {
            this.localHistory.update(s);
            this.locationBar.setText(s);
            if (this.debugger != null) {
               this.debugger.detach();
               this.debugger.setDocumentURL(s);
            }

            this.application.addVisitedURI(s);
            this.backAction.update();
            this.forwardAction.update();
            this.transformHistory = new TransformHistory();
            this.previousTransformAction.update();
            this.nextTransformAction.update();
         }
      }

   }

   public void managerStarted(UpdateManagerEvent e) {
      if (this.debug) {
         String msg = resources.getString("Message.updateManagerStarted");
         System.out.println(msg);
      }

      this.managerStopped = false;
      this.playAction.update(false);
      this.pauseAction.update(true);
      this.stopAction.update(true);
   }

   public void managerSuspended(UpdateManagerEvent e) {
      if (this.debug) {
         String msg = resources.getString("Message.updateManagerSuspended");
         System.out.println(msg);
      }

      this.playAction.update(true);
      this.pauseAction.update(false);
   }

   public void managerResumed(UpdateManagerEvent e) {
      if (this.debug) {
         String msg = resources.getString("Message.updateManagerResumed");
         System.out.println(msg);
      }

      this.playAction.update(false);
      this.pauseAction.update(true);
   }

   public void managerStopped(UpdateManagerEvent e) {
      if (this.debug) {
         String msg = resources.getString("Message.updateManagerStopped");
         System.out.println(msg);
      }

      this.managerStopped = true;
      this.playAction.update(false);
      this.pauseAction.update(false);
      this.stopAction.update(false);
   }

   public void updateStarted(UpdateManagerEvent e) {
   }

   public void updateCompleted(UpdateManagerEvent e) {
   }

   public void updateFailed(UpdateManagerEvent e) {
   }

   static {
      try {
         EOL = System.getProperty("line.separator", "\n");
      } catch (SecurityException var2) {
         EOL = "\n";
      }

      priorJDK1_4 = true;

      try {
         Class.forName("java.util.logging.LoggingPermission");
         priorJDK1_4 = false;
      } catch (ClassNotFoundException var1) {
      }

      WAIT_CURSOR = new Cursor(3);
      DEFAULT_CURSOR = new Cursor(0);
      PROPERTY_OS_NAME = Resources.getString("JSVGViewerFrame.property.os.name");
      PROPERTY_OS_NAME_DEFAULT = Resources.getString("JSVGViewerFrame.property.os.name.default");
      PROPERTY_OS_WINDOWS_PREFIX = Resources.getString("JSVGViewerFrame.property.os.windows.prefix");
      defaultHandler = new SVGInputHandler();
      bundle = ResourceBundle.getBundle("org.apache.batik.apps.svgbrowser.resources.GUI", Locale.getDefault());
      resources = new ResourceManager(bundle);
   }

   protected static class ImageFileFilter extends FileFilter {
      protected String extension;

      public ImageFileFilter(String extension) {
         this.extension = extension;
      }

      public boolean accept(File f) {
         boolean accept = false;
         String fileName = null;
         if (f != null) {
            if (f.isDirectory()) {
               accept = true;
            } else {
               fileName = f.getPath().toLowerCase();
               if (fileName.endsWith(this.extension)) {
                  accept = true;
               }
            }
         }

         return accept;
      }

      public String getDescription() {
         return this.extension;
      }
   }

   protected class UserAgent implements SVGUserAgent {
      public void displayError(String message) {
         if (JSVGViewerFrame.this.debug) {
            System.err.println(message);
         }

         JOptionPane pane = new JOptionPane(message, 0);
         JDialog dialog = pane.createDialog(JSVGViewerFrame.this, "ERROR");
         dialog.setModal(false);
         dialog.setVisible(true);
      }

      public void displayError(Exception ex) {
         if (JSVGViewerFrame.this.debug) {
            ex.printStackTrace();
         }

         JErrorPane pane = new JErrorPane(ex, 0);
         JDialog dialog = pane.createDialog(JSVGViewerFrame.this, "ERROR");
         dialog.setModal(false);
         dialog.setVisible(true);
      }

      public void displayMessage(String message) {
         JSVGViewerFrame.this.statusBar.setMessage(message);
      }

      public void showAlert(String message) {
         JSVGViewerFrame.this.svgCanvas.showAlert(message);
      }

      public String showPrompt(String message) {
         return JSVGViewerFrame.this.svgCanvas.showPrompt(message);
      }

      public String showPrompt(String message, String defaultValue) {
         return JSVGViewerFrame.this.svgCanvas.showPrompt(message, defaultValue);
      }

      public boolean showConfirm(String message) {
         return JSVGViewerFrame.this.svgCanvas.showConfirm(message);
      }

      public float getPixelUnitToMillimeter() {
         return 0.26458332F;
      }

      public float getPixelToMM() {
         return this.getPixelUnitToMillimeter();
      }

      public String getDefaultFontFamily() {
         return JSVGViewerFrame.this.application.getDefaultFontFamily();
      }

      public float getMediumFontSize() {
         return 228.59999F / (72.0F * this.getPixelUnitToMillimeter());
      }

      public float getLighterFontWeight(float f) {
         int weight = (int)((f + 50.0F) / 100.0F) * 100;
         switch (weight) {
            case 100:
               return 100.0F;
            case 200:
               return 100.0F;
            case 300:
               return 200.0F;
            case 400:
               return 300.0F;
            case 500:
               return 400.0F;
            case 600:
               return 400.0F;
            case 700:
               return 400.0F;
            case 800:
               return 400.0F;
            case 900:
               return 400.0F;
            default:
               throw new IllegalArgumentException("Bad Font Weight: " + f);
         }
      }

      public float getBolderFontWeight(float f) {
         int weight = (int)((f + 50.0F) / 100.0F) * 100;
         switch (weight) {
            case 100:
               return 600.0F;
            case 200:
               return 600.0F;
            case 300:
               return 600.0F;
            case 400:
               return 600.0F;
            case 500:
               return 600.0F;
            case 600:
               return 700.0F;
            case 700:
               return 800.0F;
            case 800:
               return 900.0F;
            case 900:
               return 900.0F;
            default:
               throw new IllegalArgumentException("Bad Font Weight: " + f);
         }
      }

      public String getLanguages() {
         return JSVGViewerFrame.this.application.getLanguages();
      }

      public String getUserStyleSheetURI() {
         return JSVGViewerFrame.this.application.getUserStyleSheetURI();
      }

      public String getXMLParserClassName() {
         return JSVGViewerFrame.this.application.getXMLParserClassName();
      }

      public boolean isXMLParserValidating() {
         return JSVGViewerFrame.this.application.isXMLParserValidating();
      }

      public String getMedia() {
         return JSVGViewerFrame.this.application.getMedia();
      }

      public String getAlternateStyleSheet() {
         return JSVGViewerFrame.this.alternateStyleSheet;
      }

      public void openLink(String uri, boolean newc) {
         if (newc) {
            JSVGViewerFrame.this.application.openLink(uri);
         } else {
            JSVGViewerFrame.this.showSVGDocument(uri);
         }

      }

      public boolean supportExtension(String s) {
         return false;
      }

      public void handleElement(Element elt, Object data) {
      }

      public ScriptSecurity getScriptSecurity(String scriptType, ParsedURL scriptURL, ParsedURL docURL) {
         if (!JSVGViewerFrame.this.application.canLoadScriptType(scriptType)) {
            return new NoLoadScriptSecurity(scriptType);
         } else {
            switch (JSVGViewerFrame.this.application.getAllowedScriptOrigin()) {
               case 1:
                  return new RelaxedScriptSecurity(scriptType, scriptURL, docURL);
               case 2:
                  return new DefaultScriptSecurity(scriptType, scriptURL, docURL);
               case 3:
               default:
                  return new NoLoadScriptSecurity(scriptType);
               case 4:
                  return new EmbededScriptSecurity(scriptType, scriptURL, docURL);
            }
         }
      }

      public void checkLoadScript(String scriptType, ParsedURL scriptURL, ParsedURL docURL) throws SecurityException {
         ScriptSecurity s = this.getScriptSecurity(scriptType, scriptURL, docURL);
         if (s != null) {
            s.checkLoadScript();
         }

      }

      public ExternalResourceSecurity getExternalResourceSecurity(ParsedURL resourceURL, ParsedURL docURL) {
         switch (JSVGViewerFrame.this.application.getAllowedExternalResourceOrigin()) {
            case 1:
               return new RelaxedExternalResourceSecurity(resourceURL, docURL);
            case 2:
               return new DefaultExternalResourceSecurity(resourceURL, docURL);
            case 3:
            default:
               return new NoLoadExternalResourceSecurity();
            case 4:
               return new EmbededExternalResourceSecurity(resourceURL);
         }
      }

      public void checkLoadExternalResource(ParsedURL resourceURL, ParsedURL docURL) throws SecurityException {
         ExternalResourceSecurity s = this.getExternalResourceSecurity(resourceURL, docURL);
         if (s != null) {
            s.checkLoadExternalResource();
         }

      }
   }

   public class DOMViewerAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         this.openDOMViewer();
      }

      public void openDOMViewer() {
         if (JSVGViewerFrame.this.domViewer == null || JSVGViewerFrame.this.domViewer.isDisplayable()) {
            JSVGViewerFrame.this.domViewer = new DOMViewer(JSVGViewerFrame.this.svgCanvas.new JSVGViewerDOMViewerController());
            Rectangle fr = JSVGViewerFrame.this.getBounds();
            Dimension td = JSVGViewerFrame.this.domViewer.getSize();
            JSVGViewerFrame.this.domViewer.setLocation(fr.x + (fr.width - td.width) / 2, fr.y + (fr.height - td.height) / 2);
         }

         JSVGViewerFrame.this.domViewer.setVisible(true);
      }

      public DOMViewer getDOMViewer() {
         return JSVGViewerFrame.this.domViewer;
      }
   }

   public class FullScreenAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         if (JSVGViewerFrame.this.window != null && JSVGViewerFrame.this.window.isVisible()) {
            JSVGViewerFrame.this.svgCanvas.getParent().remove(JSVGViewerFrame.this.svgCanvas);
            JSVGViewerFrame.this.svgCanvasPanel.add(JSVGViewerFrame.this.svgCanvas, "Center");
            JSVGViewerFrame.this.window.setVisible(false);
         } else {
            if (JSVGViewerFrame.this.window == null) {
               JSVGViewerFrame.this.window = new JWindow(JSVGViewerFrame.this);
               Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
               JSVGViewerFrame.this.window.setSize(size);
            }

            JSVGViewerFrame.this.svgCanvas.getParent().remove(JSVGViewerFrame.this.svgCanvas);
            JSVGViewerFrame.this.window.getContentPane().add(JSVGViewerFrame.this.svgCanvas);
            JSVGViewerFrame.this.window.setVisible(true);
            JSVGViewerFrame.this.window.toFront();
            JSVGViewerFrame.this.svgCanvas.requestFocus();
         }

      }
   }

   public class ThumbnailDialogAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         if (JSVGViewerFrame.this.thumbnailDialog == null) {
            JSVGViewerFrame.this.thumbnailDialog = new ThumbnailDialog(JSVGViewerFrame.this, JSVGViewerFrame.this.svgCanvas);
            JSVGViewerFrame.this.thumbnailDialog.pack();
            Rectangle fr = JSVGViewerFrame.this.getBounds();
            Dimension td = JSVGViewerFrame.this.thumbnailDialog.getSize();
            JSVGViewerFrame.this.thumbnailDialog.setLocation(fr.x + (fr.width - td.width) / 2, fr.y + (fr.height - td.height) / 2);
         }

         JSVGViewerFrame.this.thumbnailDialog.setInteractionEnabled(!JSVGViewerFrame.this.svgCanvas.getDisableInteractions());
         JSVGViewerFrame.this.thumbnailDialog.setVisible(true);
      }
   }

   public class FindDialogAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         if (JSVGViewerFrame.this.findDialog == null) {
            JSVGViewerFrame.this.findDialog = new FindDialog(JSVGViewerFrame.this, JSVGViewerFrame.this.svgCanvas);
            JSVGViewerFrame.this.findDialog.setGraphicsNode(JSVGViewerFrame.this.svgCanvas.getGraphicsNode());
            JSVGViewerFrame.this.findDialog.pack();
            Rectangle fr = JSVGViewerFrame.this.getBounds();
            Dimension td = JSVGViewerFrame.this.findDialog.getSize();
            JSVGViewerFrame.this.findDialog.setLocation(fr.x + (fr.width - td.width) / 2, fr.y + (fr.height - td.height) / 2);
         }

         JSVGViewerFrame.this.findDialog.setVisible(true);
      }
   }

   public class MonitorAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         if (JSVGViewerFrame.memoryMonitorFrame == null) {
            JSVGViewerFrame.memoryMonitorFrame = new MemoryMonitor();
            Rectangle fr = JSVGViewerFrame.this.getBounds();
            Dimension md = JSVGViewerFrame.memoryMonitorFrame.getSize();
            JSVGViewerFrame.memoryMonitorFrame.setLocation(fr.x + (fr.width - md.width) / 2, fr.y + (fr.height - md.height) / 2);
         }

         JSVGViewerFrame.memoryMonitorFrame.setVisible(true);
      }
   }

   public class SetTransformAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         if (JSVGViewerFrame.this.transformDialog == null) {
            JSVGViewerFrame.this.transformDialog = JAffineTransformChooser.createDialog(JSVGViewerFrame.this, JSVGViewerFrame.resources.getString("SetTransform.title"));
         }

         AffineTransform txf = JSVGViewerFrame.this.transformDialog.showDialog();
         if (txf != null) {
            AffineTransform at = JSVGViewerFrame.this.svgCanvas.getRenderingTransform();
            if (at == null) {
               at = new AffineTransform();
            }

            txf.concatenate(at);
            JSVGViewerFrame.this.svgCanvas.setRenderingTransform(txf);
         }

      }
   }

   public class StopAction extends AbstractAction implements JComponentModifier {
      List components = new LinkedList();

      public void actionPerformed(ActionEvent e) {
         JSVGViewerFrame.this.svgCanvas.stopProcessing();
      }

      public void addJComponent(JComponent c) {
         this.components.add(c);
         c.setEnabled(false);
      }

      public void update(boolean enabled) {
         Iterator var2 = this.components.iterator();

         while(var2.hasNext()) {
            Object component = var2.next();
            ((JComponent)component).setEnabled(enabled);
         }

      }
   }

   public class PauseAction extends AbstractAction implements JComponentModifier {
      List components = new LinkedList();

      public void actionPerformed(ActionEvent e) {
         JSVGViewerFrame.this.svgCanvas.suspendProcessing();
      }

      public void addJComponent(JComponent c) {
         this.components.add(c);
         c.setEnabled(false);
      }

      public void update(boolean enabled) {
         Iterator var2 = this.components.iterator();

         while(var2.hasNext()) {
            Object component = var2.next();
            ((JComponent)component).setEnabled(enabled);
         }

      }
   }

   public class PlayAction extends AbstractAction implements JComponentModifier {
      List components = new LinkedList();

      public void actionPerformed(ActionEvent e) {
         JSVGViewerFrame.this.svgCanvas.resumeProcessing();
      }

      public void addJComponent(JComponent c) {
         this.components.add(c);
         c.setEnabled(false);
      }

      public void update(boolean enabled) {
         Iterator var2 = this.components.iterator();

         while(var2.hasNext()) {
            Object component = var2.next();
            ((JComponent)component).setEnabled(enabled);
         }

      }
   }

   public class UseStylesheetAction extends AbstractAction implements JComponentModifier {
      List components = new LinkedList();

      public void actionPerformed(ActionEvent e) {
      }

      public void addJComponent(JComponent c) {
         this.components.add(c);
         c.setEnabled(false);
      }

      protected void update() {
         JSVGViewerFrame.this.alternateStyleSheet = null;
         Iterator it = this.components.iterator();
         SVGDocument doc = JSVGViewerFrame.this.svgCanvas.getSVGDocument();

         while(it.hasNext()) {
            JComponent stylesheetMenu = (JComponent)it.next();
            stylesheetMenu.removeAll();
            stylesheetMenu.setEnabled(false);
            ButtonGroup buttonGroup = new ButtonGroup();

            for(Node n = doc.getFirstChild(); n != null && n.getNodeType() != 1; n = n.getNextSibling()) {
               if (n instanceof StyleSheetProcessingInstruction) {
                  StyleSheetProcessingInstruction sspi = (StyleSheetProcessingInstruction)n;
                  HashMap attrs = sspi.getPseudoAttributes();
                  final String title = (String)attrs.get("title");
                  String alt = (String)attrs.get("alternate");
                  if (title != null && "yes".equals(alt)) {
                     JRadioButtonMenuItem button = new JRadioButtonMenuItem(title);
                     button.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                           SVGOMDocument doc = (SVGOMDocument)JSVGViewerFrame.this.svgCanvas.getSVGDocument();
                           doc.clearViewCSS();
                           JSVGViewerFrame.this.alternateStyleSheet = title;
                           JSVGViewerFrame.this.svgCanvas.setSVGDocument(doc);
                        }
                     });
                     buttonGroup.add(button);
                     stylesheetMenu.add(button);
                     stylesheetMenu.setEnabled(true);
                  }
               }
            }
         }

      }
   }

   public class NextTransformAction extends AbstractAction implements JComponentModifier {
      List components = new LinkedList();

      public void actionPerformed(ActionEvent e) {
         if (JSVGViewerFrame.this.transformHistory.canGoForward()) {
            JSVGViewerFrame.this.transformHistory.forward();
            this.update();
            JSVGViewerFrame.this.previousTransformAction.update();
            JSVGViewerFrame.this.svgCanvas.setRenderingTransform(JSVGViewerFrame.this.transformHistory.currentTransform());
         }

      }

      public void addJComponent(JComponent c) {
         this.components.add(c);
         c.setEnabled(false);
      }

      protected void update() {
         boolean b = JSVGViewerFrame.this.transformHistory.canGoForward();
         Iterator var2 = this.components.iterator();

         while(var2.hasNext()) {
            Object component = var2.next();
            ((JComponent)component).setEnabled(b);
         }

      }
   }

   public class PreviousTransformAction extends AbstractAction implements JComponentModifier {
      List components = new LinkedList();

      public void actionPerformed(ActionEvent e) {
         if (JSVGViewerFrame.this.transformHistory.canGoBack()) {
            JSVGViewerFrame.this.transformHistory.back();
            this.update();
            JSVGViewerFrame.this.nextTransformAction.update();
            JSVGViewerFrame.this.svgCanvas.setRenderingTransform(JSVGViewerFrame.this.transformHistory.currentTransform());
         }

      }

      public void addJComponent(JComponent c) {
         this.components.add(c);
         c.setEnabled(false);
      }

      protected void update() {
         boolean b = JSVGViewerFrame.this.transformHistory.canGoBack();
         Iterator var2 = this.components.iterator();

         while(var2.hasNext()) {
            Object component = var2.next();
            ((JComponent)component).setEnabled(b);
         }

      }
   }

   public class ToggleDebuggerAction extends AbstractAction {
      public ToggleDebuggerAction() {
         super("Toggle Debugger Action");
      }

      public void actionPerformed(ActionEvent e) {
         if (JSVGViewerFrame.this.debugger == null) {
            JSVGViewerFrame.this.showDebugger();
         } else {
            JSVGViewerFrame.this.hideDebugger();
         }

      }
   }

   public class FlushAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         JSVGViewerFrame.this.svgCanvas.flush();
         JSVGViewerFrame.this.svgCanvas.setRenderingTransform(JSVGViewerFrame.this.svgCanvas.getRenderingTransform());
      }
   }

   public class ViewSourceAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         if (JSVGViewerFrame.this.svgDocument != null) {
            final ParsedURL u = new ParsedURL(JSVGViewerFrame.this.svgDocument.getURL());
            final JFrame fr = new JFrame(u.toString());
            fr.setSize(JSVGViewerFrame.resources.getInteger("ViewSource.width"), JSVGViewerFrame.resources.getInteger("ViewSource.height"));
            final XMLTextEditor ta = new XMLTextEditor();
            ta.setFont(new Font("monospaced", 0, 12));
            JScrollPane scroll = new JScrollPane();
            scroll.getViewport().add(ta);
            scroll.setVerticalScrollBarPolicy(22);
            fr.getContentPane().add(scroll, "Center");
            (new Thread() {
               public void run() {
                  char[] buffer = new char[4096];

                  try {
                     Document doc = new XMLDocument();
                     ParsedURL purl = new ParsedURL(JSVGViewerFrame.this.svgDocument.getURL());
                     InputStream is = u.openStream(JSVGViewerFrame.this.getInputHandler(purl).getHandledMimeTypes());
                     Reader in = XMLUtilities.createXMLDocumentReader(is);

                     int len;
                     while((len = in.read(buffer, 0, buffer.length)) != -1) {
                        doc.insertString(doc.getLength(), new String(buffer, 0, len), (AttributeSet)null);
                     }

                     ta.setDocument(doc);
                     ta.setEditable(false);
                     fr.setVisible(true);
                  } catch (Exception var7) {
                     JSVGViewerFrame.this.userAgent.displayError(var7);
                  }

               }
            }).start();
         }
      }
   }

   public class ExportAsTIFFAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         JFileChooser fileChooser = new JFileChooser(JSVGViewerFrame.makeAbsolute(JSVGViewerFrame.this.currentSavePath));
         fileChooser.setDialogTitle(JSVGViewerFrame.resources.getString("ExportAsTIFF.title"));
         fileChooser.setFileHidingEnabled(false);
         fileChooser.setFileSelectionMode(0);
         fileChooser.addChoosableFileFilter(new ImageFileFilter(".tiff"));
         int choice = fileChooser.showSaveDialog(JSVGViewerFrame.this);
         if (choice == 0) {
            final File f = fileChooser.getSelectedFile();
            BufferedImage buffer = JSVGViewerFrame.this.svgCanvas.getOffScreen();
            if (buffer != null) {
               JSVGViewerFrame.this.statusBar.setMessage(JSVGViewerFrame.resources.getString("Message.exportAsTIFF"));
               int w = buffer.getWidth();
               int h = buffer.getHeight();
               final ImageTranscoder trans = new TIFFTranscoder();
               if (JSVGViewerFrame.this.application.getXMLParserClassName() != null) {
                  trans.addTranscodingHint(JPEGTranscoder.KEY_XML_PARSER_CLASSNAME, JSVGViewerFrame.this.application.getXMLParserClassName());
               }

               final BufferedImage img = trans.createImage(w, h);
               Graphics2D g2d = img.createGraphics();
               g2d.drawImage(buffer, (BufferedImageOp)null, 0, 0);
               (new Thread() {
                  public void run() {
                     try {
                        JSVGViewerFrame.this.currentSavePath = f;
                        OutputStream ostream = new BufferedOutputStream(new FileOutputStream(f));
                        trans.writeImage(img, new TranscoderOutput(ostream));
                        ostream.close();
                     } catch (Exception var2) {
                     }

                     JSVGViewerFrame.this.statusBar.setMessage(JSVGViewerFrame.resources.getString("Message.done"));
                  }
               }).start();
            }
         }

      }
   }

   public class ExportAsPNGAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         JFileChooser fileChooser = new JFileChooser(JSVGViewerFrame.makeAbsolute(JSVGViewerFrame.this.currentSavePath));
         fileChooser.setDialogTitle(JSVGViewerFrame.resources.getString("ExportAsPNG.title"));
         fileChooser.setFileHidingEnabled(false);
         fileChooser.setFileSelectionMode(0);
         fileChooser.addChoosableFileFilter(new ImageFileFilter(".png"));
         int choice = fileChooser.showSaveDialog(JSVGViewerFrame.this);
         if (choice == 0) {
            boolean isIndexed = PNGOptionPanel.showDialog(JSVGViewerFrame.this);
            final File f = fileChooser.getSelectedFile();
            BufferedImage buffer = JSVGViewerFrame.this.svgCanvas.getOffScreen();
            if (buffer != null) {
               JSVGViewerFrame.this.statusBar.setMessage(JSVGViewerFrame.resources.getString("Message.exportAsPNG"));
               int w = buffer.getWidth();
               int h = buffer.getHeight();
               final ImageTranscoder trans = new PNGTranscoder();
               if (JSVGViewerFrame.this.application.getXMLParserClassName() != null) {
                  trans.addTranscodingHint(JPEGTranscoder.KEY_XML_PARSER_CLASSNAME, JSVGViewerFrame.this.application.getXMLParserClassName());
               }

               trans.addTranscodingHint(PNGTranscoder.KEY_FORCE_TRANSPARENT_WHITE, Boolean.TRUE);
               if (isIndexed) {
                  trans.addTranscodingHint(PNGTranscoder.KEY_INDEXED, 8);
               }

               final BufferedImage img = trans.createImage(w, h);
               Graphics2D g2d = img.createGraphics();
               g2d.drawImage(buffer, (BufferedImageOp)null, 0, 0);
               (new Thread() {
                  public void run() {
                     try {
                        JSVGViewerFrame.this.currentSavePath = f;
                        OutputStream ostream = new BufferedOutputStream(new FileOutputStream(f));
                        trans.writeImage(img, new TranscoderOutput(ostream));
                        ostream.close();
                     } catch (Exception var2) {
                     }

                     JSVGViewerFrame.this.statusBar.setMessage(JSVGViewerFrame.resources.getString("Message.done"));
                  }
               }).start();
            }
         }

      }
   }

   public class ExportAsJPGAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         JFileChooser fileChooser = new JFileChooser(JSVGViewerFrame.makeAbsolute(JSVGViewerFrame.this.currentSavePath));
         fileChooser.setDialogTitle(JSVGViewerFrame.resources.getString("ExportAsJPG.title"));
         fileChooser.setFileHidingEnabled(false);
         fileChooser.setFileSelectionMode(0);
         fileChooser.addChoosableFileFilter(new ImageFileFilter(".jpg"));
         int choice = fileChooser.showSaveDialog(JSVGViewerFrame.this);
         if (choice == 0) {
            float quality = JPEGOptionPanel.showDialog(JSVGViewerFrame.this);
            final File f = fileChooser.getSelectedFile();
            BufferedImage buffer = JSVGViewerFrame.this.svgCanvas.getOffScreen();
            if (buffer != null) {
               JSVGViewerFrame.this.statusBar.setMessage(JSVGViewerFrame.resources.getString("Message.exportAsJPG"));
               int w = buffer.getWidth();
               int h = buffer.getHeight();
               final ImageTranscoder trans = new JPEGTranscoder();
               if (JSVGViewerFrame.this.application.getXMLParserClassName() != null) {
                  trans.addTranscodingHint(JPEGTranscoder.KEY_XML_PARSER_CLASSNAME, JSVGViewerFrame.this.application.getXMLParserClassName());
               }

               trans.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, quality);
               final BufferedImage img = trans.createImage(w, h);
               Graphics2D g2d = img.createGraphics();
               g2d.setColor(Color.white);
               g2d.fillRect(0, 0, w, h);
               g2d.drawImage(buffer, (BufferedImageOp)null, 0, 0);
               (new Thread() {
                  public void run() {
                     try {
                        JSVGViewerFrame.this.currentSavePath = f;
                        OutputStream ostream = new BufferedOutputStream(new FileOutputStream(f));
                        trans.writeImage(img, new TranscoderOutput(ostream));
                        ostream.close();
                     } catch (Exception var2) {
                     }

                     JSVGViewerFrame.this.statusBar.setMessage(JSVGViewerFrame.resources.getString("Message.done"));
                  }
               }).start();
            }
         }

      }
   }

   public class SaveAsAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         JFileChooser fileChooser = new JFileChooser(JSVGViewerFrame.makeAbsolute(JSVGViewerFrame.this.currentSavePath));
         fileChooser.setDialogTitle(JSVGViewerFrame.resources.getString("SaveAs.title"));
         fileChooser.setFileHidingEnabled(false);
         fileChooser.setFileSelectionMode(0);
         fileChooser.addChoosableFileFilter(new ImageFileFilter(".svg"));
         int choice = fileChooser.showSaveDialog(JSVGViewerFrame.this);
         if (choice == 0) {
            File f = fileChooser.getSelectedFile();
            SVGOptionPanel sop = SVGOptionPanel.showDialog(JSVGViewerFrame.this);
            final boolean useXMLBase = sop.getUseXMLBase();
            final boolean prettyPrint = sop.getPrettyPrint();
            sop = null;
            final SVGDocument svgDoc = JSVGViewerFrame.this.svgCanvas.getSVGDocument();
            if (svgDoc != null) {
               JSVGViewerFrame.this.statusBar.setMessage(JSVGViewerFrame.resources.getString("Message.saveAs"));
               JSVGViewerFrame.this.currentSavePath = f;
               final OutputStreamWriter w = null;

               try {
                  OutputStream tosx = null;
                  tosx = new FileOutputStream(f);
                  OutputStream tos = new BufferedOutputStream(tosx);
                  w = new OutputStreamWriter(tos, "utf-8");
               } catch (Exception var14) {
                  JSVGViewerFrame.this.userAgent.displayError(var14);
                  return;
               }

               final Runnable doneRun = new Runnable() {
                  public void run() {
                     String doneStr = JSVGViewerFrame.resources.getString("Message.done");
                     JSVGViewerFrame.this.statusBar.setMessage(doneStr);
                  }
               };
               Runnable r = new Runnable() {
                  public void run() {
                     try {
                        w.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
                        w.write(JSVGViewerFrame.EOL);
                        Node fc = svgDoc.getFirstChild();
                        if (fc.getNodeType() != 10) {
                           w.write("<!DOCTYPE svg PUBLIC '");
                           w.write("-//W3C//DTD SVG 1.0//EN");
                           w.write("' '");
                           w.write("http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd");
                           w.write("'>");
                           w.write(JSVGViewerFrame.EOL);
                           w.write(JSVGViewerFrame.EOL);
                        }

                        Element root = svgDoc.getRootElement();
                        boolean doXMLBase = useXMLBase;
                        if (root.hasAttributeNS("http://www.w3.org/XML/1998/namespace", "base")) {
                           doXMLBase = false;
                        }

                        if (doXMLBase) {
                           root.setAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:base", svgDoc.getURL());
                        }

                        if (prettyPrint) {
                           SVGTranscoder trans = new SVGTranscoder();
                           trans.transcode(new TranscoderInput(svgDoc), new TranscoderOutput(w));
                        } else {
                           DOMUtilities.writeDocument(svgDoc, w);
                        }

                        w.close();
                        if (doXMLBase) {
                           root.removeAttributeNS("http://www.w3.org/XML/1998/namespace", "xml:base");
                        }

                        if (EventQueue.isDispatchThread()) {
                           doneRun.run();
                        } else {
                           EventQueue.invokeLater(doneRun);
                        }
                     } catch (Exception var5) {
                        JSVGViewerFrame.this.userAgent.displayError(var5);
                     }

                  }
               };
               UpdateManager um = JSVGViewerFrame.this.svgCanvas.getUpdateManager();
               if (um != null && um.isRunning()) {
                  um.getUpdateRunnableQueue().invokeLater(r);
               } else {
                  r.run();
               }

            }
         }
      }
   }

   public class PrintAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         if (JSVGViewerFrame.this.svgDocument != null) {
            final SVGDocument doc = JSVGViewerFrame.this.svgDocument;
            (new Thread() {
               public void run() {
                  String uri = doc.getURL();
                  String fragment = JSVGViewerFrame.this.svgCanvas.getFragmentIdentifier();
                  if (fragment != null) {
                     uri = uri + '#' + fragment;
                  }

                  PrintTranscoder pt = new PrintTranscoder();
                  if (JSVGViewerFrame.this.application.getXMLParserClassName() != null) {
                     pt.addTranscodingHint(JPEGTranscoder.KEY_XML_PARSER_CLASSNAME, JSVGViewerFrame.this.application.getXMLParserClassName());
                  }

                  pt.addTranscodingHint(PrintTranscoder.KEY_SHOW_PAGE_DIALOG, Boolean.TRUE);
                  pt.addTranscodingHint(PrintTranscoder.KEY_SHOW_PRINTER_DIALOG, Boolean.TRUE);
                  pt.transcode(new TranscoderInput(uri), (TranscoderOutput)null);

                  try {
                     pt.print();
                  } catch (PrinterException var5) {
                     JSVGViewerFrame.this.userAgent.displayError((Exception)var5);
                  }

               }
            }).start();
         }

      }
   }

   public class ForwardAction extends AbstractAction implements JComponentModifier {
      List components = new LinkedList();

      public void actionPerformed(ActionEvent e) {
         if (JSVGViewerFrame.this.localHistory.canGoForward()) {
            JSVGViewerFrame.this.localHistory.forward();
         }

      }

      public void addJComponent(JComponent c) {
         this.components.add(c);
         c.setEnabled(false);
      }

      protected void update() {
         boolean b = JSVGViewerFrame.this.localHistory.canGoForward();
         Iterator var2 = this.components.iterator();

         while(var2.hasNext()) {
            Object component = var2.next();
            ((JComponent)component).setEnabled(b);
         }

      }
   }

   public class BackAction extends AbstractAction implements JComponentModifier {
      List components = new LinkedList();

      public void actionPerformed(ActionEvent e) {
         if (JSVGViewerFrame.this.localHistory.canGoBack()) {
            JSVGViewerFrame.this.localHistory.back();
         }

      }

      public void addJComponent(JComponent c) {
         this.components.add(c);
         c.setEnabled(false);
      }

      protected void update() {
         boolean b = JSVGViewerFrame.this.localHistory.canGoBack();
         Iterator var2 = this.components.iterator();

         while(var2.hasNext()) {
            Object component = var2.next();
            ((JComponent)component).setEnabled(b);
         }

      }
   }

   public class ReloadAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         if ((e.getModifiers() & 1) == 1) {
            JSVGViewerFrame.this.svgCanvas.flushImageCache();
         }

         if (JSVGViewerFrame.this.svgDocument != null) {
            JSVGViewerFrame.this.localHistory.reload();
         }

      }
   }

   public class CloseAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         JSVGViewerFrame.this.application.closeJSVGViewerFrame(JSVGViewerFrame.this);
      }
   }

   public class PreferencesAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         JSVGViewerFrame.this.application.showPreferenceDialog(JSVGViewerFrame.this);
      }
   }

   public class NewWindowAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         JSVGViewerFrame vf = JSVGViewerFrame.this.application.createAndShowJSVGViewerFrame();
         vf.autoAdjust = JSVGViewerFrame.this.autoAdjust;
         vf.debug = JSVGViewerFrame.this.debug;
         vf.svgCanvas.setProgressivePaint(JSVGViewerFrame.this.svgCanvas.getProgressivePaint());
         vf.svgCanvas.setDoubleBufferedRendering(JSVGViewerFrame.this.svgCanvas.getDoubleBufferedRendering());
      }
   }

   public class OpenLocationAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         if (JSVGViewerFrame.this.uriChooser == null) {
            JSVGViewerFrame.this.uriChooser = new URIChooser(JSVGViewerFrame.this);
            JSVGViewerFrame.this.uriChooser.setFileFilter(new SVGFileFilter());
            JSVGViewerFrame.this.uriChooser.pack();
            Rectangle fr = JSVGViewerFrame.this.getBounds();
            Dimension sd = JSVGViewerFrame.this.uriChooser.getSize();
            JSVGViewerFrame.this.uriChooser.setLocation(fr.x + (fr.width - sd.width) / 2, fr.y + (fr.height - sd.height) / 2);
         }

         if (JSVGViewerFrame.this.uriChooser.showDialog() == 0) {
            String s = JSVGViewerFrame.this.uriChooser.getText();
            if (s == null) {
               return;
            }

            int i = s.indexOf(35);
            String t = "";
            if (i != -1) {
               t = s.substring(i + 1);
               s = s.substring(0, i);
            }

            if (!s.equals("")) {
               File f = new File(s);
               if (f.exists()) {
                  if (f.isDirectory()) {
                     s = null;
                  } else {
                     try {
                        s = f.getCanonicalPath();
                        if (s.startsWith("/")) {
                           s = "file:" + s;
                        } else {
                           s = "file:/" + s;
                        }
                     } catch (IOException var9) {
                     }
                  }
               }

               if (s != null) {
                  if (JSVGViewerFrame.this.svgDocument != null) {
                     ParsedURL docPURL = new ParsedURL(JSVGViewerFrame.this.svgDocument.getURL());
                     ParsedURL purl = new ParsedURL(docPURL, s);
                     String fi = JSVGViewerFrame.this.svgCanvas.getFragmentIdentifier();
                     if (docPURL.equals(purl) && t.equals(fi)) {
                        return;
                     }
                  }

                  if (t.length() != 0) {
                     s = s + '#' + t;
                  }

                  JSVGViewerFrame.this.showSVGDocument(s);
               }
            }
         }

      }
   }

   public class OpenAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         File f = null;
         FileDialog fileDialog;
         String filename;
         if (Platform.isOSX) {
            fileDialog = new FileDialog(JSVGViewerFrame.this, Resources.getString("Open.title"));
            fileDialog.setFilenameFilter(new FilenameFilter() {
               public boolean accept(File dir, String name) {
                  Iterator var3 = JSVGViewerFrame.getHandlers().iterator();

                  SquiggleInputHandler handler;
                  do {
                     if (!var3.hasNext()) {
                        return false;
                     }

                     Object o = var3.next();
                     handler = (SquiggleInputHandler)o;
                  } while(!handler.accept(new File(dir, name)));

                  return true;
               }
            });
            fileDialog.setVisible(true);
            filename = fileDialog.getFile();
            if (fileDialog != null) {
               String dirname = fileDialog.getDirectory();
               f = new File(dirname, filename);
            }
         } else {
            fileDialog = null;
            filename = System.getProperty(JSVGViewerFrame.PROPERTY_OS_NAME, JSVGViewerFrame.PROPERTY_OS_NAME_DEFAULT);
            SecurityManager sm = System.getSecurityManager();
            JFileChooser fileChooser;
            if (JSVGViewerFrame.priorJDK1_4 && sm != null && filename.indexOf(JSVGViewerFrame.PROPERTY_OS_WINDOWS_PREFIX) != -1) {
               fileChooser = new JFileChooser(JSVGViewerFrame.makeAbsolute(JSVGViewerFrame.this.currentPath), new WindowsAltFileSystemView());
            } else {
               fileChooser = new JFileChooser(JSVGViewerFrame.makeAbsolute(JSVGViewerFrame.this.currentPath));
            }

            fileChooser.setFileHidingEnabled(false);
            fileChooser.setFileSelectionMode(0);
            Iterator var6 = JSVGViewerFrame.getHandlers().iterator();

            while(var6.hasNext()) {
               Object o = var6.next();
               SquiggleInputHandler handler = (SquiggleInputHandler)o;
               fileChooser.addChoosableFileFilter(new SquiggleInputHandlerFilter(handler));
            }

            int choice = fileChooser.showOpenDialog(JSVGViewerFrame.this);
            if (choice == 0) {
               f = fileChooser.getSelectedFile();
               JSVGViewerFrame.this.currentPath = f;
            }
         }

         if (f != null) {
            try {
               String furl = f.toURI().toURL().toString();
               JSVGViewerFrame.this.showSVGDocument(furl);
            } catch (MalformedURLException var9) {
               if (JSVGViewerFrame.this.userAgent != null) {
                  JSVGViewerFrame.this.userAgent.displayError((Exception)var9);
               }
            }
         }

      }
   }

   public class AboutAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         AboutDialog dlg = new AboutDialog(JSVGViewerFrame.this);
         dlg.setSize(dlg.getPreferredSize());
         dlg.setLocationRelativeTo(JSVGViewerFrame.this);
         dlg.setVisible(true);
         dlg.toFront();
      }
   }

   protected static class Debugger {
      protected static boolean isPresent;
      protected static Class debuggerClass;
      protected static Class contextFactoryClass;
      protected static final int CLEAR_ALL_BREAKPOINTS_METHOD = 0;
      protected static final int GO_METHOD = 1;
      protected static final int SET_EXIT_ACTION_METHOD = 2;
      protected static final int ATTACH_TO_METHOD = 3;
      protected static final int DETACH_METHOD = 4;
      protected static final int DISPOSE_METHOD = 5;
      protected static final int GET_DEBUG_FRAME_METHOD = 6;
      protected static Constructor debuggerConstructor;
      protected static Method[] debuggerMethods;
      protected static Class rhinoInterpreterClass;
      protected static Method getContextFactoryMethod;
      protected Object debuggerInstance;
      protected JSVGViewerFrame svgFrame;

      public Debugger(JSVGViewerFrame frame, String url) {
         this.svgFrame = frame;

         try {
            this.debuggerInstance = debuggerConstructor.newInstance("JavaScript Debugger - " + url);
         } catch (IllegalAccessException var4) {
            throw new RuntimeException(var4.getMessage());
         } catch (InvocationTargetException var5) {
            var5.printStackTrace();
            throw new RuntimeException(var5.getMessage());
         } catch (InstantiationException var6) {
            throw new RuntimeException(var6.getMessage());
         }
      }

      public void setDocumentURL(String url) {
         this.getDebugFrame().setTitle("JavaScript Debugger - " + url);
      }

      public void initialize() {
         JFrame debugGui = this.getDebugFrame();
         JMenuBar menuBar = debugGui.getJMenuBar();
         JMenu menu = menuBar.getMenu(0);
         menu.getItem(0).setEnabled(false);
         menu.getItem(1).setEnabled(false);
         menu.getItem(3).setText(Resources.getString("Close.text"));
         menu.getItem(3).setAccelerator(KeyStroke.getKeyStroke(87, 128));
         debugGui.setSize(600, 460);
         debugGui.pack();
         this.setExitAction(new Runnable() {
            public void run() {
               Debugger.this.svgFrame.hideDebugger();
            }
         });
         WindowAdapter wa = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
               Debugger.this.svgFrame.hideDebugger();
            }
         };
         debugGui.addWindowListener(wa);
         debugGui.setVisible(true);
         this.attach();
      }

      public void attach() {
         Object interpreter = this.svgFrame.svgCanvas.getRhinoInterpreter();
         if (interpreter != null) {
            this.attachTo(this.getContextFactory(interpreter));
         }

      }

      protected JFrame getDebugFrame() {
         try {
            return (JFrame)debuggerMethods[6].invoke(this.debuggerInstance, (Object[])null);
         } catch (InvocationTargetException var2) {
            throw new RuntimeException(var2.getMessage());
         } catch (IllegalAccessException var3) {
            throw new RuntimeException(var3.getMessage());
         }
      }

      protected void setExitAction(Runnable r) {
         try {
            debuggerMethods[2].invoke(this.debuggerInstance, r);
         } catch (InvocationTargetException var3) {
            throw new RuntimeException(var3.getMessage());
         } catch (IllegalAccessException var4) {
            throw new RuntimeException(var4.getMessage());
         }
      }

      public void attachTo(Object contextFactory) {
         try {
            debuggerMethods[3].invoke(this.debuggerInstance, contextFactory);
         } catch (InvocationTargetException var3) {
            throw new RuntimeException(var3.getMessage());
         } catch (IllegalAccessException var4) {
            throw new RuntimeException(var4.getMessage());
         }
      }

      public void detach() {
         try {
            debuggerMethods[4].invoke(this.debuggerInstance, (Object[])null);
         } catch (InvocationTargetException var2) {
            throw new RuntimeException(var2.getMessage());
         } catch (IllegalAccessException var3) {
            throw new RuntimeException(var3.getMessage());
         }
      }

      public void go() {
         try {
            debuggerMethods[1].invoke(this.debuggerInstance, (Object[])null);
         } catch (InvocationTargetException var2) {
            throw new RuntimeException(var2.getMessage());
         } catch (IllegalAccessException var3) {
            throw new RuntimeException(var3.getMessage());
         }
      }

      public void clearAllBreakpoints() {
         try {
            debuggerMethods[0].invoke(this.debuggerInstance, (Object[])null);
         } catch (InvocationTargetException var2) {
            throw new RuntimeException(var2.getMessage());
         } catch (IllegalAccessException var3) {
            throw new RuntimeException(var3.getMessage());
         }
      }

      public void dispose() {
         try {
            debuggerMethods[5].invoke(this.debuggerInstance, (Object[])null);
         } catch (InvocationTargetException var2) {
            throw new RuntimeException(var2.getMessage());
         } catch (IllegalAccessException var3) {
            throw new RuntimeException(var3.getMessage());
         }
      }

      protected Object getContextFactory(Object rhinoInterpreter) {
         try {
            return getContextFactoryMethod.invoke(rhinoInterpreter, (Object[])null);
         } catch (InvocationTargetException var3) {
            throw new RuntimeException(var3.getMessage());
         } catch (IllegalAccessException var4) {
            throw new RuntimeException(var4.getMessage());
         }
      }

      static {
         try {
            Class dc = Class.forName("org.mozilla.javascript.tools.debugger.Main");
            Class cfc = Class.forName("org.mozilla.javascript.ContextFactory");
            rhinoInterpreterClass = Class.forName("org.apache.batik.script.rhino.RhinoInterpreter");
            debuggerConstructor = dc.getConstructor(String.class);
            debuggerMethods = new Method[]{dc.getMethod("clearAllBreakpoints", (Class[])null), dc.getMethod("go", (Class[])null), dc.getMethod("setExitAction", Runnable.class), dc.getMethod("attachTo", cfc), dc.getMethod("detach", (Class[])null), dc.getMethod("dispose", (Class[])null), dc.getMethod("getDebugFrame", (Class[])null)};
            getContextFactoryMethod = rhinoInterpreterClass.getMethod("getContextFactory", (Class[])null);
            debuggerClass = dc;
            isPresent = true;
         } catch (ClassNotFoundException var2) {
         } catch (NoSuchMethodException var3) {
         } catch (SecurityException var4) {
         }

      }
   }

   protected class Canvas extends JSVGCanvas {
      public Canvas(SVGUserAgent ua, boolean eventsEnabled, boolean selectableText) {
         super(ua, eventsEnabled, selectableText);
      }

      public Object getRhinoInterpreter() {
         return this.bridgeContext == null ? null : this.bridgeContext.getInterpreter("text/ecmascript");
      }

      protected class JSVGViewerDOMViewerController implements DOMViewerController {
         public boolean canEdit() {
            return Canvas.this.getUpdateManager() != null;
         }

         public ElementOverlayManager createSelectionManager() {
            return this.canEdit() ? new ElementOverlayManager(Canvas.this) : null;
         }

         public org.w3c.dom.Document getDocument() {
            return Canvas.this.svgDocument;
         }

         public void performUpdate(Runnable r) {
            if (this.canEdit()) {
               Canvas.this.getUpdateManager().getUpdateRunnableQueue().invokeLater(r);
            } else {
               r.run();
            }

         }

         public void removeSelectionOverlay(Overlay selectionOverlay) {
            Canvas.this.getOverlays().remove(selectionOverlay);
         }

         public void selectNode(Node node) {
            DOMViewerAction dViewerAction = (DOMViewerAction)JSVGViewerFrame.this.getAction("DOMViewerAction");
            dViewerAction.openDOMViewer();
            JSVGViewerFrame.this.domViewer.selectNode(node);
         }
      }
   }
}
