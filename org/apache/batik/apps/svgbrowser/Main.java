package org.apache.batik.apps.svgbrowser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.Authenticator;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.util.ApplicationSecurityEnforcer;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.Platform;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.batik.util.resources.ResourceManager;

public class Main implements Application {
   public static final String UNKNOWN_SCRIPT_TYPE_LOAD_KEY_EXTENSION = ".load";
   public static final String PROPERTY_USER_HOME = "user.home";
   public static final String PROPERTY_JAVA_SECURITY_POLICY = "java.security.policy";
   public static final String BATIK_CONFIGURATION_SUBDIRECTORY = ".batik";
   public static final String SQUIGGLE_CONFIGURATION_FILE = "preferences.xml";
   public static final String SQUIGGLE_POLICY_FILE = "__svgbrowser.policy";
   public static final String POLICY_GRANT_SCRIPT_NETWORK_ACCESS = "grant {\n  permission java.net.SocketPermission \"*\", \"listen, connect, resolve, accept\";\n};\n\n";
   public static final String POLICY_GRANT_SCRIPT_FILE_ACCESS = "grant {\n  permission java.io.FilePermission \"<<ALL FILES>>\", \"read\";\n};\n\n";
   public static final String PREFERENCE_KEY_VISITED_URI_LIST = "preference.key.visited.uri.list";
   public static final String PREFERENCE_KEY_VISITED_URI_LIST_LENGTH = "preference.key.visited.uri.list.length";
   public static final String URI_SEPARATOR = " ";
   public static final String DEFAULT_DEFAULT_FONT_FAMILY = "Arial, Helvetica, sans-serif";
   public static final String SVG_INITIALIZATION = "resources/init.svg";
   protected String svgInitializationURI;
   public static final String RESOURCES = "org.apache.batik.apps.svgbrowser.resources.Main";
   public static final String SQUIGGLE_SECURITY_POLICY = "org/apache/batik/apps/svgbrowser/resources/svgbrowser.policy";
   protected static ResourceBundle bundle = ResourceBundle.getBundle("org.apache.batik.apps.svgbrowser.resources.Main", Locale.getDefault());
   protected static ResourceManager resources;
   protected static ImageIcon frameIcon;
   protected XMLPreferenceManager preferenceManager;
   public static final int MAX_VISITED_URIS = 10;
   protected Vector lastVisited = new Vector();
   protected int maxVisitedURIs = 10;
   protected String[] arguments;
   protected boolean overrideSecurityPolicy = false;
   protected ApplicationSecurityEnforcer securityEnforcer;
   protected Map handlers = new HashMap();
   protected List viewerFrames;
   protected PreferenceDialog preferenceDialog;
   protected String uiSpecialization;

   public static void main(String[] args) {
      new Main(args);
   }

   public Main(String[] args) {
      this.handlers.put("-font-size", new FontSizeHandler());
      this.viewerFrames = new LinkedList();
      this.arguments = args;
      if (Platform.isOSX) {
         this.uiSpecialization = "OSX";
         System.setProperty("apple.laf.useScreenMenuBar", "true");

         try {
            Class Application = Class.forName("com.apple.eawt.Application");
            Class ApplicationListener = Class.forName("com.apple.eawt.ApplicationListener");
            Class ApplicationEvent = Class.forName("com.apple.eawt.ApplicationEvent");
            Method getApplication = Application.getMethod("getApplication");
            Method addApplicationListener = Application.getMethod("addApplicationListener", ApplicationListener);
            final Method setHandled = ApplicationEvent.getMethod("setHandled", Boolean.TYPE);
            Method setEnabledPreferencesMenu = Application.getMethod("setEnabledPreferencesMenu", Boolean.TYPE);
            InvocationHandler listenerHandler = new InvocationHandler() {
               public Object invoke(Object proxy, Method method, Object[] args) {
                  String name = method.getName();
                  JSVGViewerFrame relativeTo;
                  if (name.equals("handleAbout")) {
                     relativeTo = (JSVGViewerFrame)Main.this.viewerFrames.get(0);
                     AboutDialog dlg = new AboutDialog(relativeTo);
                     dlg.setSize(dlg.getPreferredSize());
                     dlg.setLocationRelativeTo(relativeTo);
                     dlg.setVisible(true);
                     dlg.toFront();
                  } else if (name.equals("handlePreferences")) {
                     relativeTo = (JSVGViewerFrame)Main.this.viewerFrames.get(0);
                     Main.this.showPreferenceDialog(relativeTo);
                  } else if (!name.equals("handleQuit")) {
                     return null;
                  }

                  try {
                     setHandled.invoke(args[0], Boolean.TRUE);
                  } catch (Exception var7) {
                  }

                  return null;
               }
            };
            Object application = getApplication.invoke((Object)null, (Object[])null);
            setEnabledPreferencesMenu.invoke(application, Boolean.TRUE);
            Object listener = Proxy.newProxyInstance(Main.class.getClassLoader(), new Class[]{ApplicationListener}, listenerHandler);
            addApplicationListener.invoke(application, listener);
         } catch (Exception var13) {
            var13.printStackTrace();
            this.uiSpecialization = null;
         }
      }

      Map defaults = new HashMap(11);
      defaults.put("preference.key.languages", Locale.getDefault().getLanguage());
      defaults.put("preference.key.show.rendering", Boolean.FALSE);
      defaults.put("preference.key.auto.adjust.window", Boolean.TRUE);
      defaults.put("preference.key.selection.xor.mode", Boolean.FALSE);
      defaults.put("preference.key.enable.double.buffering", Boolean.TRUE);
      defaults.put("preference.key.show.debug.trace", Boolean.FALSE);
      defaults.put("preference.key.proxy.host", "");
      defaults.put("preference.key.proxy.port", "");
      defaults.put("preference.key.cssmedia", "screen");
      defaults.put("preference.key.default.font.family", "Arial, Helvetica, sans-serif");
      defaults.put("preference.key.is.xml.parser.validating", Boolean.FALSE);
      defaults.put("preference.key.enforce.secure.scripting", Boolean.TRUE);
      defaults.put("preference.key.grant.script.file.access", Boolean.FALSE);
      defaults.put("preference.key.grant.script.network.access", Boolean.FALSE);
      defaults.put("preference.key.load.java.script", Boolean.TRUE);
      defaults.put("preference.key.load.ecmascript", Boolean.TRUE);
      defaults.put("preference.key.allowed.script.origin", 2);
      defaults.put("preference.key.allowed.external.resource.origin", 1);
      defaults.put("preference.key.visited.uri.list", "");
      defaults.put("preference.key.visited.uri.list.length", 10);
      defaults.put("preference.key.animation.rate.limiting.mode", 1);
      defaults.put("preference.key.animation.rate.limiting.cpu", 0.75F);
      defaults.put("preference.key.animation.rate.limiting.fps", 10.0F);
      defaults.put("preference.key.user.stylesheet.enabled", Boolean.TRUE);
      this.securityEnforcer = new ApplicationSecurityEnforcer(this.getClass(), "org/apache/batik/apps/svgbrowser/resources/svgbrowser.policy");

      try {
         this.preferenceManager = new XMLPreferenceManager("preferences.xml", defaults);
         String dir = System.getProperty("user.home");
         File f = new File(dir, ".batik");
         f.mkdir();
         XMLPreferenceManager.setPreferenceDirectory(f.getCanonicalPath());
         this.preferenceManager.load();
         this.setPreferences();
         this.initializeLastVisited();
         Authenticator.setDefault(new JAuthenticator());
      } catch (Exception var12) {
         var12.printStackTrace();
      }

      final AboutDialog initDialog = new AboutDialog();
      ((BorderLayout)initDialog.getContentPane().getLayout()).setVgap(8);
      final JProgressBar pb = new JProgressBar(0, 3);
      initDialog.getContentPane().add(pb, "South");
      Dimension ss = initDialog.getToolkit().getScreenSize();
      Dimension ds = initDialog.getPreferredSize();
      initDialog.setLocation((ss.width - ds.width) / 2, (ss.height - ds.height) / 2);
      initDialog.setSize(ds);
      initDialog.setVisible(true);
      final JSVGViewerFrame v = new JSVGViewerFrame(this);
      JSVGCanvas c = v.getJSVGCanvas();
      c.addSVGDocumentLoaderListener(new SVGDocumentLoaderAdapter() {
         public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
            pb.setValue(1);
         }

         public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
            pb.setValue(2);
         }
      });
      c.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
         public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
            pb.setValue(3);
         }
      });
      c.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
         public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
            initDialog.dispose();
            v.dispose();
            System.gc();
            Main.this.run();
         }
      });
      c.setSize(100, 100);
      this.svgInitializationURI = Main.class.getResource("resources/init.svg").toString();
      c.loadSVGDocument(this.svgInitializationURI);
   }

   public void installCustomPolicyFile() throws IOException {
      String securityPolicyProperty = System.getProperty("java.security.policy");
      if (this.overrideSecurityPolicy || securityPolicyProperty == null || "".equals(securityPolicyProperty)) {
         ParsedURL policyURL = new ParsedURL(this.securityEnforcer.getPolicyURL());
         String dir = System.getProperty("user.home");
         File batikConfigDir = new File(dir, ".batik");
         File policyFile = new File(batikConfigDir, "__svgbrowser.policy");
         Reader r = new BufferedReader(new InputStreamReader(policyURL.openStream()));
         Writer w = new FileWriter(policyFile);
         char[] buf = new char[1024];
         int n = false;

         int n;
         while((n = r.read(buf, 0, buf.length)) != -1) {
            w.write(buf, 0, n);
         }

         r.close();
         boolean grantScriptNetworkAccess = this.preferenceManager.getBoolean("preference.key.grant.script.network.access");
         boolean grantScriptFileAccess = this.preferenceManager.getBoolean("preference.key.grant.script.file.access");
         if (grantScriptNetworkAccess) {
            w.write("grant {\n  permission java.net.SocketPermission \"*\", \"listen, connect, resolve, accept\";\n};\n\n");
         }

         if (grantScriptFileAccess) {
            w.write("grant {\n  permission java.io.FilePermission \"<<ALL FILES>>\", \"read\";\n};\n\n");
         }

         w.close();
         this.overrideSecurityPolicy = true;
         System.setProperty("java.security.policy", policyFile.toURI().toURL().toString());
      }

   }

   public void run() {
      try {
         int i;
         for(i = 0; i < this.arguments.length; ++i) {
            OptionHandler oh = (OptionHandler)this.handlers.get(this.arguments[i]);
            if (oh == null) {
               break;
            }

            i = oh.handleOption(i);
         }

         JSVGViewerFrame frame = this.createAndShowJSVGViewerFrame();

         while(i < this.arguments.length) {
            if (this.arguments[i].length() == 0) {
               ++i;
            } else {
               File file = new File(this.arguments[i]);
               String uri = null;

               try {
                  if (file.canRead()) {
                     uri = file.toURI().toURL().toString();
                  }
               } catch (SecurityException var6) {
               }

               if (uri == null) {
                  uri = this.arguments[i];
                  ParsedURL purl = null;
                  purl = new ParsedURL(this.arguments[i]);
                  if (!purl.complete()) {
                     uri = null;
                  }
               }

               if (uri != null) {
                  if (frame == null) {
                     frame = this.createAndShowJSVGViewerFrame();
                  }

                  frame.showSVGDocument(uri);
                  frame = null;
               } else {
                  JOptionPane.showMessageDialog(frame, resources.getString("Error.skipping.file") + this.arguments[i]);
               }

               ++i;
            }
         }
      } catch (Exception var7) {
         var7.printStackTrace();
         this.printUsage();
      }

   }

   protected void printUsage() {
      System.out.println();
      System.out.println(resources.getString("Command.header"));
      System.out.println(resources.getString("Command.syntax"));
      System.out.println();
      System.out.println(resources.getString("Command.options"));
      Iterator var1 = this.handlers.keySet().iterator();

      while(var1.hasNext()) {
         Object o = var1.next();
         String s = (String)o;
         System.out.println(((OptionHandler)this.handlers.get(s)).getDescription());
      }

   }

   public JSVGViewerFrame createAndShowJSVGViewerFrame() {
      JSVGViewerFrame mainFrame = new JSVGViewerFrame(this);
      mainFrame.setSize(resources.getInteger("Frame.width"), resources.getInteger("Frame.height"));
      mainFrame.setIconImage(frameIcon.getImage());
      mainFrame.setTitle(resources.getString("Frame.title"));
      mainFrame.setVisible(true);
      this.viewerFrames.add(mainFrame);
      this.setPreferences(mainFrame);
      return mainFrame;
   }

   public void closeJSVGViewerFrame(JSVGViewerFrame f) {
      f.getJSVGCanvas().stopProcessing();
      this.viewerFrames.remove(f);
      if (this.viewerFrames.size() == 0) {
         System.exit(0);
      }

      f.dispose();
   }

   public Action createExitAction(JSVGViewerFrame vf) {
      return new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
            System.exit(0);
         }
      };
   }

   public void openLink(String url) {
      JSVGViewerFrame f = this.createAndShowJSVGViewerFrame();
      f.getJSVGCanvas().loadSVGDocument(url);
   }

   public String getXMLParserClassName() {
      return XMLResourceDescriptor.getXMLParserClassName();
   }

   public boolean isXMLParserValidating() {
      return this.preferenceManager.getBoolean("preference.key.is.xml.parser.validating");
   }

   public void showPreferenceDialog(JSVGViewerFrame f) {
      if (this.preferenceDialog == null) {
         this.preferenceDialog = new PreferenceDialog(f, this.preferenceManager);
      }

      if (this.preferenceDialog.showDialog() == 0) {
         try {
            this.preferenceManager.save();
            this.setPreferences();
         } catch (Exception var3) {
         }
      }

   }

   private void setPreferences() throws IOException {
      Iterator var1 = this.viewerFrames.iterator();

      while(var1.hasNext()) {
         Object viewerFrame = var1.next();
         this.setPreferences((JSVGViewerFrame)viewerFrame);
      }

      System.setProperty("proxyHost", this.preferenceManager.getString("preference.key.proxy.host"));
      System.setProperty("proxyPort", this.preferenceManager.getString("preference.key.proxy.port"));
      this.installCustomPolicyFile();
      this.securityEnforcer.enforceSecurity(this.preferenceManager.getBoolean("preference.key.enforce.secure.scripting"));
   }

   private void setPreferences(JSVGViewerFrame vf) {
      boolean db = this.preferenceManager.getBoolean("preference.key.enable.double.buffering");
      vf.getJSVGCanvas().setDoubleBufferedRendering(db);
      boolean sr = this.preferenceManager.getBoolean("preference.key.show.rendering");
      vf.getJSVGCanvas().setProgressivePaint(sr);
      boolean d = this.preferenceManager.getBoolean("preference.key.show.debug.trace");
      vf.setDebug(d);
      boolean aa = this.preferenceManager.getBoolean("preference.key.auto.adjust.window");
      vf.setAutoAdjust(aa);
      boolean dd = this.preferenceManager.getBoolean("preference.key.selection.xor.mode");
      vf.getJSVGCanvas().setSelectionOverlayXORMode(dd);
      int al = this.preferenceManager.getInteger("preference.key.animation.rate.limiting.mode");
      if (al < 0 || al > 2) {
         al = 1;
      }

      float fps;
      switch (al) {
         case 0:
            vf.getJSVGCanvas().setAnimationLimitingNone();
            break;
         case 1:
            fps = this.preferenceManager.getFloat("preference.key.animation.rate.limiting.cpu");
            if (fps <= 0.0F || fps > 1.0F) {
               fps = 0.75F;
            }

            vf.getJSVGCanvas().setAnimationLimitingCPU(fps);
            break;
         case 2:
            fps = this.preferenceManager.getFloat("preference.key.animation.rate.limiting.fps");
            if (fps <= 0.0F) {
               fps = 10.0F;
            }

            vf.getJSVGCanvas().setAnimationLimitingFPS(fps);
      }

   }

   public String getLanguages() {
      String s = this.preferenceManager.getString("preference.key.languages");
      return s == null ? Locale.getDefault().getLanguage() : s;
   }

   public String getUserStyleSheetURI() {
      boolean enabled = this.preferenceManager.getBoolean("preference.key.user.stylesheet.enabled");
      String ssPath = this.preferenceManager.getString("preference.key.user.stylesheet");
      if (enabled && ssPath.length() != 0) {
         try {
            File f = new File(ssPath);
            if (f.exists()) {
               return f.toURI().toURL().toString();
            }
         } catch (IOException var4) {
         }

         return ssPath;
      } else {
         return null;
      }
   }

   public String getDefaultFontFamily() {
      return this.preferenceManager.getString("preference.key.default.font.family");
   }

   public String getMedia() {
      String s = this.preferenceManager.getString("preference.key.cssmedia");
      return s == null ? "screen" : s;
   }

   public boolean isSelectionOverlayXORMode() {
      return this.preferenceManager.getBoolean("preference.key.selection.xor.mode");
   }

   public boolean canLoadScriptType(String scriptType) {
      if (!"text/ecmascript".equals(scriptType) && !"application/ecmascript".equals(scriptType) && !"text/javascript".equals(scriptType) && !"application/javascript".equals(scriptType)) {
         return "application/java-archive".equals(scriptType) ? this.preferenceManager.getBoolean("preference.key.load.java.script") : this.preferenceManager.getBoolean(scriptType + ".load");
      } else {
         return this.preferenceManager.getBoolean("preference.key.load.ecmascript");
      }
   }

   public int getAllowedScriptOrigin() {
      int ret = this.preferenceManager.getInteger("preference.key.allowed.script.origin");
      return ret;
   }

   public int getAllowedExternalResourceOrigin() {
      int ret = this.preferenceManager.getInteger("preference.key.allowed.external.resource.origin");
      return ret;
   }

   public void addVisitedURI(String uri) {
      if (!this.svgInitializationURI.equals(uri)) {
         int maxVisitedURIs = this.preferenceManager.getInteger("preference.key.visited.uri.list.length");
         if (maxVisitedURIs < 0) {
            maxVisitedURIs = 0;
         }

         if (this.lastVisited.contains(uri)) {
            this.lastVisited.removeElement(uri);
         }

         while(this.lastVisited.size() > 0 && this.lastVisited.size() > maxVisitedURIs - 1) {
            this.lastVisited.removeElementAt(0);
         }

         if (maxVisitedURIs > 0) {
            this.lastVisited.addElement(uri);
         }

         StringBuffer lastVisitedBuffer = new StringBuffer(this.lastVisited.size() * 8);

         for(Iterator var4 = this.lastVisited.iterator(); var4.hasNext(); lastVisitedBuffer.append(" ")) {
            Object aLastVisited = var4.next();

            try {
               lastVisitedBuffer.append(URLEncoder.encode(aLastVisited.toString(), Charset.defaultCharset().name()));
            } catch (UnsupportedEncodingException var8) {
               throw new RuntimeException(var8);
            }
         }

         this.preferenceManager.setString("preference.key.visited.uri.list", lastVisitedBuffer.toString());

         try {
            this.preferenceManager.save();
         } catch (Exception var7) {
         }

      }
   }

   public String[] getVisitedURIs() {
      String[] visitedURIs = new String[this.lastVisited.size()];
      this.lastVisited.toArray(visitedURIs);
      return visitedURIs;
   }

   public String getUISpecialization() {
      return this.uiSpecialization;
   }

   protected void initializeLastVisited() {
      String lastVisitedStr = this.preferenceManager.getString("preference.key.visited.uri.list");
      StringTokenizer st = new StringTokenizer(lastVisitedStr, " ");
      int n = st.countTokens();
      int maxVisitedURIs = this.preferenceManager.getInteger("preference.key.visited.uri.list.length");
      if (n > maxVisitedURIs) {
         n = maxVisitedURIs;
      }

      for(int i = 0; i < n; ++i) {
         try {
            this.lastVisited.addElement(URLDecoder.decode(st.nextToken(), Charset.defaultCharset().name()));
         } catch (UnsupportedEncodingException var7) {
            throw new RuntimeException(var7);
         }
      }

   }

   static {
      resources = new ResourceManager(bundle);
      frameIcon = new ImageIcon(Main.class.getResource(resources.getString("Frame.icon")));
   }

   protected class FontSizeHandler implements OptionHandler {
      public int handleOption(int i) {
         ++i;
         int size = Integer.parseInt(Main.this.arguments[i]);
         Font font = new Font("Dialog", 0, size);
         FontUIResource fontRes = new FontUIResource(font);
         UIManager.put("CheckBox.font", fontRes);
         UIManager.put("PopupMenu.font", fontRes);
         UIManager.put("TextPane.font", fontRes);
         UIManager.put("MenuItem.font", fontRes);
         UIManager.put("ComboBox.font", fontRes);
         UIManager.put("Button.font", fontRes);
         UIManager.put("Tree.font", fontRes);
         UIManager.put("ScrollPane.font", fontRes);
         UIManager.put("TabbedPane.font", fontRes);
         UIManager.put("EditorPane.font", fontRes);
         UIManager.put("TitledBorder.font", fontRes);
         UIManager.put("Menu.font", fontRes);
         UIManager.put("TextArea.font", fontRes);
         UIManager.put("OptionPane.font", fontRes);
         UIManager.put("DesktopIcon.font", fontRes);
         UIManager.put("MenuBar.font", fontRes);
         UIManager.put("ToolBar.font", fontRes);
         UIManager.put("RadioButton.font", fontRes);
         UIManager.put("RadioButtonMenuItem.font", fontRes);
         UIManager.put("ToggleButton.font", fontRes);
         UIManager.put("ToolTip.font", fontRes);
         UIManager.put("ProgressBar.font", fontRes);
         UIManager.put("TableHeader.font", fontRes);
         UIManager.put("Panel.font", fontRes);
         UIManager.put("List.font", fontRes);
         UIManager.put("ColorChooser.font", fontRes);
         UIManager.put("PasswordField.font", fontRes);
         UIManager.put("TextField.font", fontRes);
         UIManager.put("Table.font", fontRes);
         UIManager.put("Label.font", fontRes);
         UIManager.put("InternalFrameTitlePane.font", fontRes);
         UIManager.put("CheckBoxMenuItem.font", fontRes);
         return i;
      }

      public String getDescription() {
         return Main.resources.getString("Command.font-size");
      }
   }

   protected interface OptionHandler {
      int handleOption(int var1);

      String getDescription();
   }
}
