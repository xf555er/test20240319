package org.apache.batik.apps.svgbrowser;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.StringTokenizer;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.batik.ext.swing.GridBagConstants;
import org.apache.batik.ext.swing.JGridBagPanel;
import org.apache.batik.util.Platform;
import org.apache.batik.util.PreferenceManager;
import org.apache.batik.util.gui.CSSMediaPanel;
import org.apache.batik.util.gui.LanguageDialog;

public class PreferenceDialog extends JDialog implements GridBagConstants {
   public static final int OK_OPTION = 0;
   public static final int CANCEL_OPTION = 1;
   public static final String PREFERENCE_KEY_TITLE_PREFIX = "PreferenceDialog.title.";
   public static final String PREFERENCE_KEY_TITLE_DIALOG = "PreferenceDialog.title.dialog";
   public static final String PREFERENCE_KEY_LABEL_RENDERING_OPTIONS = "PreferenceDialog.label.rendering.options";
   public static final String PREFERENCE_KEY_LABEL_ANIMATION_RATE_LIMITING = "PreferenceDialog.label.animation.rate.limiting";
   public static final String PREFERENCE_KEY_LABEL_OTHER_OPTIONS = "PreferenceDialog.label.other.options";
   public static final String PREFERENCE_KEY_LABEL_ENABLE_DOUBLE_BUFFERING = "PreferenceDialog.label.enable.double.buffering";
   public static final String PREFERENCE_KEY_LABEL_SHOW_RENDERING = "PreferenceDialog.label.show.rendering";
   public static final String PREFERENCE_KEY_LABEL_AUTO_ADJUST_WINDOW = "PreferenceDialog.label.auto.adjust.window";
   public static final String PREFERENCE_KEY_LABEL_SELECTION_XOR_MODE = "PreferenceDialog.label.selection.xor.mode";
   public static final String PREFERENCE_KEY_LABEL_ANIMATION_LIMIT_CPU = "PreferenceDialog.label.animation.limit.cpu";
   public static final String PREFERENCE_KEY_LABEL_PERCENT = "PreferenceDialog.label.percent";
   public static final String PREFERENCE_KEY_LABEL_ANIMATION_LIMIT_FPS = "PreferenceDialog.label.animation.limit.fps";
   public static final String PREFERENCE_KEY_LABEL_FPS = "PreferenceDialog.label.fps";
   public static final String PREFERENCE_KEY_LABEL_ANIMATION_LIMIT_UNLIMITED = "PreferenceDialog.label.animation.limit.unlimited";
   public static final String PREFERENCE_KEY_LABEL_SHOW_DEBUG_TRACE = "PreferenceDialog.label.show.debug.trace";
   public static final String PREFERENCE_KEY_LABEL_IS_XML_PARSER_VALIDATING = "PreferenceDialog.label.is.xml.parser.validating";
   public static final String PREFERENCE_KEY_LABEL_GRANT_SCRIPTS_ACCESS_TO = "PreferenceDialog.label.grant.scripts.access.to";
   public static final String PREFERENCE_KEY_LABEL_LOAD_SCRIPTS = "PreferenceDialog.label.load.scripts";
   public static final String PREFERENCE_KEY_LABEL_ALLOWED_SCRIPT_ORIGIN = "PreferenceDialog.label.allowed.script.origin";
   public static final String PREFERENCE_KEY_LABEL_ALLOWED_RESOURCE_ORIGIN = "PreferenceDialog.label.allowed.resource.origin";
   public static final String PREFERENCE_KEY_LABEL_ENFORCE_SECURE_SCRIPTING = "PreferenceDialog.label.enforce.secure.scripting";
   public static final String PREFERENCE_KEY_LABEL_FILE_SYSTEM = "PreferenceDialog.label.file.system";
   public static final String PREFERENCE_KEY_LABEL_ALL_NETWORK = "PreferenceDialog.label.all.network";
   public static final String PREFERENCE_KEY_LABEL_JAVA_JAR_FILES = "PreferenceDialog.label.java.jar.files";
   public static final String PREFERENCE_KEY_LABEL_ECMASCRIPT = "PreferenceDialog.label.ecmascript";
   public static final String PREFERENCE_KEY_LABEL_ORIGIN_ANY = "PreferenceDialog.label.origin.any";
   public static final String PREFERENCE_KEY_LABEL_ORIGIN_DOCUMENT = "PreferenceDialog.label.origin.document";
   public static final String PREFERENCE_KEY_LABEL_ORIGIN_EMBEDDED = "PreferenceDialog.label.origin.embedded";
   public static final String PREFERENCE_KEY_LABEL_ORIGIN_NONE = "PreferenceDialog.label.origin.none";
   public static final String PREFERENCE_KEY_LABEL_USER_STYLESHEET = "PreferenceDialog.label.user.stylesheet";
   public static final String PREFERENCE_KEY_LABEL_CSS_MEDIA_TYPES = "PreferenceDialog.label.css.media.types";
   public static final String PREFERENCE_KEY_LABEL_ENABLE_USER_STYLESHEET = "PreferenceDialog.label.enable.user.stylesheet";
   public static final String PREFERENCE_KEY_LABEL_BROWSE = "PreferenceDialog.label.browse";
   public static final String PREFERENCE_KEY_LABEL_ADD = "PreferenceDialog.label.add";
   public static final String PREFERENCE_KEY_LABEL_REMOVE = "PreferenceDialog.label.remove";
   public static final String PREFERENCE_KEY_LABEL_CLEAR = "PreferenceDialog.label.clear";
   public static final String PREFERENCE_KEY_LABEL_HTTP_PROXY = "PreferenceDialog.label.http.proxy";
   public static final String PREFERENCE_KEY_LABEL_HOST = "PreferenceDialog.label.host";
   public static final String PREFERENCE_KEY_LABEL_PORT = "PreferenceDialog.label.port";
   public static final String PREFERENCE_KEY_LABEL_COLON = "PreferenceDialog.label.colon";
   public static final String PREFERENCE_KEY_BROWSE_TITLE = "PreferenceDialog.BrowseWindow.title";
   public static final String PREFERENCE_KEY_LANGUAGES = "preference.key.languages";
   public static final String PREFERENCE_KEY_IS_XML_PARSER_VALIDATING = "preference.key.is.xml.parser.validating";
   public static final String PREFERENCE_KEY_USER_STYLESHEET = "preference.key.user.stylesheet";
   public static final String PREFERENCE_KEY_USER_STYLESHEET_ENABLED = "preference.key.user.stylesheet.enabled";
   public static final String PREFERENCE_KEY_SHOW_RENDERING = "preference.key.show.rendering";
   public static final String PREFERENCE_KEY_AUTO_ADJUST_WINDOW = "preference.key.auto.adjust.window";
   public static final String PREFERENCE_KEY_ENABLE_DOUBLE_BUFFERING = "preference.key.enable.double.buffering";
   public static final String PREFERENCE_KEY_SHOW_DEBUG_TRACE = "preference.key.show.debug.trace";
   public static final String PREFERENCE_KEY_SELECTION_XOR_MODE = "preference.key.selection.xor.mode";
   public static final String PREFERENCE_KEY_PROXY_HOST = "preference.key.proxy.host";
   public static final String PREFERENCE_KEY_CSS_MEDIA = "preference.key.cssmedia";
   public static final String PREFERENCE_KEY_DEFAULT_FONT_FAMILY = "preference.key.default.font.family";
   public static final String PREFERENCE_KEY_PROXY_PORT = "preference.key.proxy.port";
   public static final String PREFERENCE_KEY_ENFORCE_SECURE_SCRIPTING = "preference.key.enforce.secure.scripting";
   public static final String PREFERENCE_KEY_GRANT_SCRIPT_FILE_ACCESS = "preference.key.grant.script.file.access";
   public static final String PREFERENCE_KEY_GRANT_SCRIPT_NETWORK_ACCESS = "preference.key.grant.script.network.access";
   public static final String PREFERENCE_KEY_LOAD_ECMASCRIPT = "preference.key.load.ecmascript";
   public static final String PREFERENCE_KEY_LOAD_JAVA = "preference.key.load.java.script";
   public static final String PREFERENCE_KEY_ALLOWED_SCRIPT_ORIGIN = "preference.key.allowed.script.origin";
   public static final String PREFERENCE_KEY_ALLOWED_EXTERNAL_RESOURCE_ORIGIN = "preference.key.allowed.external.resource.origin";
   public static final String PREFERENCE_KEY_ANIMATION_RATE_LIMITING_MODE = "preference.key.animation.rate.limiting.mode";
   public static final String PREFERENCE_KEY_ANIMATION_RATE_LIMITING_CPU = "preference.key.animation.rate.limiting.cpu";
   public static final String PREFERENCE_KEY_ANIMATION_RATE_LIMITING_FPS = "preference.key.animation.rate.limiting.fps";
   public static final String LABEL_OK = "PreferenceDialog.label.ok";
   public static final String LABEL_CANCEL = "PreferenceDialog.label.cancel";
   protected PreferenceManager model;
   protected JConfigurationPanel configurationPanel;
   protected JCheckBox userStylesheetEnabled;
   protected JLabel userStylesheetLabel;
   protected JTextField userStylesheet;
   protected JButton userStylesheetBrowse;
   protected JCheckBox showRendering;
   protected JCheckBox autoAdjustWindow;
   protected JCheckBox enableDoubleBuffering;
   protected JCheckBox showDebugTrace;
   protected JCheckBox selectionXorMode;
   protected JCheckBox isXMLParserValidating;
   protected JRadioButton animationLimitUnlimited;
   protected JRadioButton animationLimitCPU;
   protected JRadioButton animationLimitFPS;
   protected JLabel animationLimitCPULabel;
   protected JLabel animationLimitFPSLabel;
   protected JTextField animationLimitCPUAmount;
   protected JTextField animationLimitFPSAmount;
   protected JCheckBox enforceSecureScripting;
   protected JCheckBox grantScriptFileAccess;
   protected JCheckBox grantScriptNetworkAccess;
   protected JCheckBox loadJava;
   protected JCheckBox loadEcmascript;
   protected JComboBox allowedScriptOrigin;
   protected JComboBox allowedResourceOrigin;
   protected JList mediaList;
   protected JButton mediaListRemoveButton;
   protected JButton mediaListClearButton;
   protected JTextField host;
   protected JTextField port;
   protected LanguageDialog.Panel languagePanel;
   protected DefaultListModel mediaListModel = new DefaultListModel();
   protected int returnCode;

   protected static boolean isMetalSteel() {
      if (!UIManager.getLookAndFeel().getName().equals("Metal")) {
         return false;
      } else {
         try {
            LookAndFeel laf = UIManager.getLookAndFeel();
            laf.getClass().getMethod("getCurrentTheme");
            return false;
         } catch (Exception var1) {
            return true;
         }
      }
   }

   public PreferenceDialog(Frame owner, PreferenceManager model) {
      super(owner, true);
      if (model == null) {
         throw new IllegalArgumentException();
      } else {
         this.model = model;
         this.buildGUI();
         this.initializeGUI();
         this.pack();
         this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
               if (Platform.isOSX) {
                  PreferenceDialog.this.savePreferences();
               }

            }
         });
      }
   }

   public PreferenceManager getPreferenceManager() {
      return this.model;
   }

   protected void initializeGUI() {
      this.enableDoubleBuffering.setSelected(this.model.getBoolean("preference.key.enable.double.buffering"));
      this.showRendering.setSelected(this.model.getBoolean("preference.key.show.rendering"));
      this.autoAdjustWindow.setSelected(this.model.getBoolean("preference.key.auto.adjust.window"));
      this.selectionXorMode.setSelected(this.model.getBoolean("preference.key.selection.xor.mode"));
      switch (this.model.getInteger("preference.key.animation.rate.limiting.mode")) {
         case 0:
            this.animationLimitUnlimited.setSelected(true);
            break;
         case 2:
            this.animationLimitFPS.setSelected(true);
            break;
         default:
            this.animationLimitCPU.setSelected(true);
      }

      float f = this.model.getFloat("preference.key.animation.rate.limiting.cpu");
      if (!(f <= 0.0F) && !(f > 100.0F)) {
         f *= 100.0F;
      } else {
         f = 85.0F;
      }

      if ((float)((int)f) == f) {
         this.animationLimitCPUAmount.setText(Integer.toString((int)f));
      } else {
         this.animationLimitCPUAmount.setText(Float.toString(f));
      }

      f = this.model.getFloat("preference.key.animation.rate.limiting.fps");
      if (f <= 0.0F) {
         f = 10.0F;
      }

      if ((float)((int)f) == f) {
         this.animationLimitFPSAmount.setText(Integer.toString((int)f));
      } else {
         this.animationLimitFPSAmount.setText(Float.toString(f));
      }

      this.showDebugTrace.setSelected(this.model.getBoolean("preference.key.show.debug.trace"));
      this.isXMLParserValidating.setSelected(this.model.getBoolean("preference.key.is.xml.parser.validating"));
      this.enforceSecureScripting.setSelected(this.model.getBoolean("preference.key.enforce.secure.scripting"));
      this.grantScriptFileAccess.setSelected(this.model.getBoolean("preference.key.grant.script.file.access"));
      this.grantScriptNetworkAccess.setSelected(this.model.getBoolean("preference.key.grant.script.network.access"));
      this.loadJava.setSelected(this.model.getBoolean("preference.key.load.java.script"));
      this.loadEcmascript.setSelected(this.model.getBoolean("preference.key.load.ecmascript"));
      int i = this.model.getInteger("preference.key.allowed.script.origin");
      switch (i) {
         case 1:
            this.allowedScriptOrigin.setSelectedIndex(0);
            break;
         case 2:
            this.allowedScriptOrigin.setSelectedIndex(1);
            break;
         case 3:
         default:
            this.allowedScriptOrigin.setSelectedIndex(3);
            break;
         case 4:
            this.allowedScriptOrigin.setSelectedIndex(2);
      }

      i = this.model.getInteger("preference.key.allowed.external.resource.origin");
      switch (i) {
         case 1:
            this.allowedResourceOrigin.setSelectedIndex(0);
            break;
         case 2:
            this.allowedResourceOrigin.setSelectedIndex(1);
            break;
         case 3:
         default:
            this.allowedResourceOrigin.setSelectedIndex(3);
            break;
         case 4:
            this.allowedResourceOrigin.setSelectedIndex(2);
      }

      this.languagePanel.setLanguages(this.model.getString("preference.key.languages"));
      String s = this.model.getString("preference.key.cssmedia");
      this.mediaListModel.removeAllElements();
      StringTokenizer st = new StringTokenizer(s, " ");

      while(st.hasMoreTokens()) {
         this.mediaListModel.addElement(st.nextToken());
      }

      this.userStylesheet.setText(this.model.getString("preference.key.user.stylesheet"));
      boolean b = this.model.getBoolean("preference.key.user.stylesheet.enabled");
      this.userStylesheetEnabled.setSelected(b);
      this.host.setText(this.model.getString("preference.key.proxy.host"));
      this.port.setText(this.model.getString("preference.key.proxy.port"));
      b = this.enableDoubleBuffering.isSelected();
      this.showRendering.setEnabled(b);
      b = this.animationLimitCPU.isSelected();
      this.animationLimitCPUAmount.setEnabled(b);
      this.animationLimitCPULabel.setEnabled(b);
      b = this.animationLimitFPS.isSelected();
      this.animationLimitFPSAmount.setEnabled(b);
      this.animationLimitFPSLabel.setEnabled(b);
      b = this.enforceSecureScripting.isSelected();
      this.grantScriptFileAccess.setEnabled(b);
      this.grantScriptNetworkAccess.setEnabled(b);
      b = this.userStylesheetEnabled.isSelected();
      this.userStylesheetLabel.setEnabled(b);
      this.userStylesheet.setEnabled(b);
      this.userStylesheetBrowse.setEnabled(b);
      this.mediaListRemoveButton.setEnabled(!this.mediaList.isSelectionEmpty());
      this.mediaListClearButton.setEnabled(!this.mediaListModel.isEmpty());
   }

   protected void savePreferences() {
      this.model.setString("preference.key.languages", this.languagePanel.getLanguages());
      this.model.setString("preference.key.user.stylesheet", this.userStylesheet.getText());
      this.model.setBoolean("preference.key.user.stylesheet.enabled", this.userStylesheetEnabled.isSelected());
      this.model.setBoolean("preference.key.show.rendering", this.showRendering.isSelected());
      this.model.setBoolean("preference.key.auto.adjust.window", this.autoAdjustWindow.isSelected());
      this.model.setBoolean("preference.key.enable.double.buffering", this.enableDoubleBuffering.isSelected());
      this.model.setBoolean("preference.key.show.debug.trace", this.showDebugTrace.isSelected());
      this.model.setBoolean("preference.key.selection.xor.mode", this.selectionXorMode.isSelected());
      this.model.setBoolean("preference.key.is.xml.parser.validating", this.isXMLParserValidating.isSelected());
      this.model.setBoolean("preference.key.enforce.secure.scripting", this.enforceSecureScripting.isSelected());
      this.model.setBoolean("preference.key.grant.script.file.access", this.grantScriptFileAccess.isSelected());
      this.model.setBoolean("preference.key.grant.script.network.access", this.grantScriptNetworkAccess.isSelected());
      this.model.setBoolean("preference.key.load.java.script", this.loadJava.isSelected());
      this.model.setBoolean("preference.key.load.ecmascript", this.loadEcmascript.isSelected());
      byte i;
      switch (this.allowedScriptOrigin.getSelectedIndex()) {
         case 0:
            i = 1;
            break;
         case 1:
            i = 2;
            break;
         case 2:
            i = 4;
            break;
         default:
            i = 8;
      }

      this.model.setInteger("preference.key.allowed.script.origin", i);
      switch (this.allowedResourceOrigin.getSelectedIndex()) {
         case 0:
            i = 1;
            break;
         case 1:
            i = 2;
            break;
         case 2:
            i = 4;
            break;
         default:
            i = 8;
      }

      this.model.setInteger("preference.key.allowed.external.resource.origin", i);
      i = 1;
      if (this.animationLimitFPS.isSelected()) {
         i = 2;
      } else if (this.animationLimitUnlimited.isSelected()) {
         i = 0;
      }

      this.model.setInteger("preference.key.animation.rate.limiting.mode", i);

      float f;
      try {
         f = Float.parseFloat(this.animationLimitCPUAmount.getText()) / 100.0F;
         if (f <= 0.0F || f >= 1.0F) {
            f = 0.85F;
         }
      } catch (NumberFormatException var6) {
         f = 0.85F;
      }

      this.model.setFloat("preference.key.animation.rate.limiting.cpu", f);

      try {
         f = Float.parseFloat(this.animationLimitFPSAmount.getText());
         if (f <= 0.0F) {
            f = 15.0F;
         }
      } catch (NumberFormatException var5) {
         f = 15.0F;
      }

      this.model.setFloat("preference.key.animation.rate.limiting.fps", f);
      this.model.setString("preference.key.proxy.host", this.host.getText());
      this.model.setString("preference.key.proxy.port", this.port.getText());
      StringBuffer sb = new StringBuffer();
      Enumeration e = this.mediaListModel.elements();

      while(e.hasMoreElements()) {
         sb.append((String)e.nextElement());
         sb.append(' ');
      }

      this.model.setString("preference.key.cssmedia", sb.toString());
   }

   protected void buildGUI() {
      JPanel panel = new JPanel(new BorderLayout());
      this.configurationPanel = new JConfigurationPanel();
      this.addConfigPanel("general", this.buildGeneralPanel());
      this.addConfigPanel("security", this.buildSecurityPanel());
      this.addConfigPanel("language", this.buildLanguagePanel());
      this.addConfigPanel("stylesheet", this.buildStylesheetPanel());
      this.addConfigPanel("network", this.buildNetworkPanel());
      panel.add(this.configurationPanel);
      if (!Platform.isOSX) {
         this.setTitle(Resources.getString("PreferenceDialog.title.dialog"));
         panel.add(this.buildButtonsPanel(), "South");
      }

      this.setResizable(false);
      this.getContentPane().add(panel);
   }

   protected void addConfigPanel(String id, JPanel c) {
      String name = Resources.getString("PreferenceDialog.title." + id);
      ImageIcon icon1 = new ImageIcon(PreferenceDialog.class.getResource("resources/icon-" + id + ".png"));
      ImageIcon icon2 = new ImageIcon(PreferenceDialog.class.getResource("resources/icon-" + id + "-dark.png"));
      this.configurationPanel.addPanel(name, icon1, icon2, c);
   }

   protected JPanel buildButtonsPanel() {
      JPanel p = new JPanel(new FlowLayout(2));
      JButton okButton = new JButton(Resources.getString("PreferenceDialog.label.ok"));
      JButton cancelButton = new JButton(Resources.getString("PreferenceDialog.label.cancel"));
      p.add(okButton);
      p.add(cancelButton);
      okButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            PreferenceDialog.this.setVisible(false);
            PreferenceDialog.this.returnCode = 0;
            PreferenceDialog.this.savePreferences();
            PreferenceDialog.this.dispose();
         }
      });
      cancelButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            PreferenceDialog.this.setVisible(false);
            PreferenceDialog.this.returnCode = 1;
            PreferenceDialog.this.dispose();
         }
      });
      this.addKeyListener(new KeyAdapter() {
         public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
               case 10:
                  PreferenceDialog.this.returnCode = 0;
                  break;
               case 27:
                  PreferenceDialog.this.returnCode = 1;
                  break;
               default:
                  return;
            }

            PreferenceDialog.this.setVisible(false);
            PreferenceDialog.this.dispose();
         }
      });
      return p;
   }

   protected JPanel buildGeneralPanel() {
      JGridBagPanel.InsetsManager im = new JGridBagPanel.InsetsManager() {
         protected Insets i1 = new Insets(5, 5, 0, 0);
         protected Insets i2 = new Insets(5, 0, 0, 0);
         protected Insets i3 = new Insets(0, 5, 0, 0);
         protected Insets i4 = new Insets(0, 0, 0, 0);

         public Insets getInsets(int x, int y) {
            if (y != 4 && y != 9) {
               return x == 0 ? this.i4 : this.i3;
            } else {
               return x == 0 ? this.i2 : this.i1;
            }
         }
      };
      JGridBagPanel p = new JGridBagPanel(im);
      p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
      JLabel renderingLabel = new JLabel(Resources.getString("PreferenceDialog.label.rendering.options"));
      this.enableDoubleBuffering = new JCheckBox(Resources.getString("PreferenceDialog.label.enable.double.buffering"));
      this.enableDoubleBuffering.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            PreferenceDialog.this.showRendering.setEnabled(PreferenceDialog.this.enableDoubleBuffering.isSelected());
         }
      });
      this.showRendering = new JCheckBox(Resources.getString("PreferenceDialog.label.show.rendering"));
      Insets in = this.showRendering.getMargin();
      this.showRendering.setMargin(new Insets(in.top, in.left + 24, in.bottom, in.right));
      this.selectionXorMode = new JCheckBox(Resources.getString("PreferenceDialog.label.selection.xor.mode"));
      this.autoAdjustWindow = new JCheckBox(Resources.getString("PreferenceDialog.label.auto.adjust.window"));
      JLabel animLabel = new JLabel(Resources.getString("PreferenceDialog.label.animation.rate.limiting"));
      this.animationLimitCPU = new JRadioButton(Resources.getString("PreferenceDialog.label.animation.limit.cpu"));
      JPanel cpuPanel = new JPanel();
      cpuPanel.setLayout(new FlowLayout(3, 3, 0));
      cpuPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));
      this.animationLimitCPUAmount = new JTextField();
      this.animationLimitCPUAmount.setPreferredSize(new Dimension(40, 20));
      cpuPanel.add(this.animationLimitCPUAmount);
      this.animationLimitCPULabel = new JLabel(Resources.getString("PreferenceDialog.label.percent"));
      cpuPanel.add(this.animationLimitCPULabel);
      this.animationLimitFPS = new JRadioButton(Resources.getString("PreferenceDialog.label.animation.limit.fps"));
      JPanel fpsPanel = new JPanel();
      fpsPanel.setLayout(new FlowLayout(3, 3, 0));
      fpsPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));
      this.animationLimitFPSAmount = new JTextField();
      this.animationLimitFPSAmount.setPreferredSize(new Dimension(40, 20));
      fpsPanel.add(this.animationLimitFPSAmount);
      this.animationLimitFPSLabel = new JLabel(Resources.getString("PreferenceDialog.label.fps"));
      fpsPanel.add(this.animationLimitFPSLabel);
      this.animationLimitUnlimited = new JRadioButton(Resources.getString("PreferenceDialog.label.animation.limit.unlimited"));
      ButtonGroup g = new ButtonGroup();
      g.add(this.animationLimitCPU);
      g.add(this.animationLimitFPS);
      g.add(this.animationLimitUnlimited);
      ActionListener l = new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
            boolean b = PreferenceDialog.this.animationLimitCPU.isSelected();
            PreferenceDialog.this.animationLimitCPUAmount.setEnabled(b);
            PreferenceDialog.this.animationLimitCPULabel.setEnabled(b);
            b = PreferenceDialog.this.animationLimitFPS.isSelected();
            PreferenceDialog.this.animationLimitFPSAmount.setEnabled(b);
            PreferenceDialog.this.animationLimitFPSLabel.setEnabled(b);
         }
      };
      this.animationLimitCPU.addActionListener(l);
      this.animationLimitFPS.addActionListener(l);
      this.animationLimitUnlimited.addActionListener(l);
      JLabel otherLabel = new JLabel(Resources.getString("PreferenceDialog.label.other.options"));
      this.showDebugTrace = new JCheckBox(Resources.getString("PreferenceDialog.label.show.debug.trace"));
      this.isXMLParserValidating = new JCheckBox(Resources.getString("PreferenceDialog.label.is.xml.parser.validating"));
      p.add(renderingLabel, 0, 0, 1, 1, 13, 0, 0.0, 0.0);
      p.add(this.enableDoubleBuffering, 1, 0, 1, 1, 17, 0, 0.0, 0.0);
      p.add(this.showRendering, 1, 1, 1, 1, 17, 0, 0.0, 0.0);
      p.add(this.autoAdjustWindow, 1, 2, 1, 1, 17, 0, 0.0, 0.0);
      p.add(this.selectionXorMode, 1, 3, 1, 1, 17, 0, 0.0, 0.0);
      p.add(animLabel, 0, 4, 1, 1, 13, 0, 0.0, 0.0);
      p.add(this.animationLimitCPU, 1, 4, 1, 1, 17, 0, 0.0, 0.0);
      p.add(cpuPanel, 1, 5, 1, 1, 17, 0, 0.0, 0.0);
      p.add(this.animationLimitFPS, 1, 6, 1, 1, 17, 0, 0.0, 0.0);
      p.add(fpsPanel, 1, 7, 1, 1, 17, 0, 0.0, 0.0);
      p.add(this.animationLimitUnlimited, 1, 8, 1, 1, 17, 0, 0.0, 0.0);
      p.add(otherLabel, 0, 9, 1, 1, 13, 0, 0.0, 0.0);
      p.add(this.showDebugTrace, 1, 9, 1, 1, 17, 0, 0.0, 0.0);
      p.add(this.isXMLParserValidating, 1, 10, 1, 1, 17, 0, 0.0, 0.0);
      return p;
   }

   protected JPanel buildSecurityPanel() {
      JGridBagPanel.InsetsManager im = new JGridBagPanel.InsetsManager() {
         protected Insets i1 = new Insets(5, 5, 0, 0);
         protected Insets i2 = new Insets(5, 0, 0, 0);
         protected Insets i3 = new Insets(0, 5, 0, 0);
         protected Insets i4 = new Insets(0, 0, 0, 0);

         public Insets getInsets(int x, int y) {
            if (y != 1 && y != 3 && y != 5 && y != 6) {
               return x == 0 ? this.i4 : this.i3;
            } else {
               return x == 0 ? this.i2 : this.i1;
            }
         }
      };
      JGridBagPanel p = new JGridBagPanel(im);
      p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
      this.enforceSecureScripting = new JCheckBox(Resources.getString("PreferenceDialog.label.enforce.secure.scripting"));
      this.enforceSecureScripting.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            boolean b = PreferenceDialog.this.enforceSecureScripting.isSelected();
            PreferenceDialog.this.grantScriptFileAccess.setEnabled(b);
            PreferenceDialog.this.grantScriptNetworkAccess.setEnabled(b);
         }
      });
      JLabel grantScript = new JLabel(Resources.getString("PreferenceDialog.label.grant.scripts.access.to"));
      grantScript.setVerticalAlignment(1);
      grantScript.setOpaque(true);
      this.grantScriptFileAccess = new JCheckBox(Resources.getString("PreferenceDialog.label.file.system"));
      this.grantScriptNetworkAccess = new JCheckBox(Resources.getString("PreferenceDialog.label.all.network"));
      JLabel loadScripts = new JLabel(Resources.getString("PreferenceDialog.label.load.scripts"));
      loadScripts.setVerticalAlignment(1);
      this.loadJava = new JCheckBox(Resources.getString("PreferenceDialog.label.java.jar.files"));
      this.loadEcmascript = new JCheckBox(Resources.getString("PreferenceDialog.label.ecmascript"));
      String[] origins = new String[]{Resources.getString("PreferenceDialog.label.origin.any"), Resources.getString("PreferenceDialog.label.origin.document"), Resources.getString("PreferenceDialog.label.origin.embedded"), Resources.getString("PreferenceDialog.label.origin.none")};
      JLabel scriptOriginLabel = new JLabel(Resources.getString("PreferenceDialog.label.allowed.script.origin"));
      this.allowedScriptOrigin = new JComboBox(origins);
      JLabel resourceOriginLabel = new JLabel(Resources.getString("PreferenceDialog.label.allowed.resource.origin"));
      this.allowedResourceOrigin = new JComboBox(origins);
      p.add(this.enforceSecureScripting, 1, 0, 1, 1, 17, 0, 1.0, 0.0);
      p.add(grantScript, 0, 1, 1, 1, 13, 0, 1.0, 0.0);
      p.add(this.grantScriptFileAccess, 1, 1, 1, 1, 17, 0, 1.0, 0.0);
      p.add(this.grantScriptNetworkAccess, 1, 2, 1, 1, 17, 0, 1.0, 0.0);
      p.add(loadScripts, 0, 3, 1, 1, 13, 0, 1.0, 0.0);
      p.add(this.loadJava, 1, 3, 1, 1, 17, 0, 1.0, 0.0);
      p.add(this.loadEcmascript, 1, 4, 1, 1, 17, 0, 1.0, 0.0);
      p.add(scriptOriginLabel, 0, 5, 1, 1, 13, 0, 1.0, 0.0);
      p.add(this.allowedScriptOrigin, 1, 5, 1, 1, 17, 0, 1.0, 0.0);
      p.add(resourceOriginLabel, 0, 6, 1, 1, 13, 0, 1.0, 0.0);
      p.add(this.allowedResourceOrigin, 1, 6, 1, 1, 17, 0, 1.0, 0.0);
      return p;
   }

   protected JPanel buildLanguagePanel() {
      JPanel p = new JPanel();
      p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
      this.languagePanel = new LanguageDialog.Panel();
      this.languagePanel.setBorder(BorderFactory.createEmptyBorder());
      Color c = UIManager.getColor("Window.background");
      this.languagePanel.getComponent(0).setBackground(c);
      this.languagePanel.getComponent(1).setBackground(c);
      p.add(this.languagePanel);
      return p;
   }

   protected JPanel buildStylesheetPanel() {
      JGridBagPanel.InsetsManager im = new JGridBagPanel.InsetsManager() {
         protected Insets i1 = new Insets(5, 5, 0, 0);
         protected Insets i2 = new Insets(5, 0, 0, 0);
         protected Insets i3 = new Insets(0, 5, 0, 0);
         protected Insets i4 = new Insets(0, 0, 0, 0);

         public Insets getInsets(int x, int y) {
            if (y >= 1 && y <= 5) {
               return x == 0 ? this.i2 : this.i1;
            } else {
               return x == 0 ? this.i4 : this.i3;
            }
         }
      };
      JGridBagPanel p = new JGridBagPanel(im);
      p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
      this.userStylesheetEnabled = new JCheckBox(Resources.getString("PreferenceDialog.label.enable.user.stylesheet"));
      this.userStylesheetEnabled.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            boolean b = PreferenceDialog.this.userStylesheetEnabled.isSelected();
            PreferenceDialog.this.userStylesheetLabel.setEnabled(b);
            PreferenceDialog.this.userStylesheet.setEnabled(b);
            PreferenceDialog.this.userStylesheetBrowse.setEnabled(b);
         }
      });
      this.userStylesheetLabel = new JLabel(Resources.getString("PreferenceDialog.label.user.stylesheet"));
      this.userStylesheet = new JTextField();
      this.userStylesheetBrowse = new JButton(Resources.getString("PreferenceDialog.label.browse"));
      this.userStylesheetBrowse.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            File f = null;
            if (Platform.isOSX) {
               FileDialog fileDialog = new FileDialog((Frame)PreferenceDialog.this.getOwner(), Resources.getString("PreferenceDialog.BrowseWindow.title"));
               fileDialog.setVisible(true);
               String filename = fileDialog.getFile();
               if (filename != null) {
                  String dirname = fileDialog.getDirectory();
                  f = new File(dirname, filename);
               }
            } else {
               JFileChooser fileChooser = new JFileChooser(new File("."));
               fileChooser.setDialogTitle(Resources.getString("PreferenceDialog.BrowseWindow.title"));
               fileChooser.setFileHidingEnabled(false);
               int choice = fileChooser.showOpenDialog(PreferenceDialog.this);
               if (choice == 0) {
                  f = fileChooser.getSelectedFile();
               }
            }

            if (f != null) {
               try {
                  PreferenceDialog.this.userStylesheet.setText(f.getCanonicalPath());
               } catch (IOException var6) {
               }
            }

         }
      });
      JLabel mediaLabel = new JLabel(Resources.getString("PreferenceDialog.label.css.media.types"));
      mediaLabel.setVerticalAlignment(1);
      this.mediaList = new JList();
      this.mediaList.setSelectionMode(0);
      this.mediaList.setModel(this.mediaListModel);
      this.mediaList.addListSelectionListener(new ListSelectionListener() {
         public void valueChanged(ListSelectionEvent e) {
            PreferenceDialog.this.updateMediaListButtons();
         }
      });
      this.mediaListModel.addListDataListener(new ListDataListener() {
         public void contentsChanged(ListDataEvent e) {
            PreferenceDialog.this.updateMediaListButtons();
         }

         public void intervalAdded(ListDataEvent e) {
            PreferenceDialog.this.updateMediaListButtons();
         }

         public void intervalRemoved(ListDataEvent e) {
            PreferenceDialog.this.updateMediaListButtons();
         }
      });
      JScrollPane scrollPane = new JScrollPane();
      scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
      scrollPane.getViewport().add(this.mediaList);
      JButton addButton = new JButton(Resources.getString("PreferenceDialog.label.add"));
      addButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            CSSMediaPanel.AddMediumDialog dialog = new CSSMediaPanel.AddMediumDialog(PreferenceDialog.this);
            dialog.pack();
            dialog.setVisible(true);
            if (dialog.getReturnCode() != 1 && dialog.getMedium() != null) {
               String medium = dialog.getMedium().trim();
               if (medium.length() != 0 && !PreferenceDialog.this.mediaListModel.contains(medium)) {
                  for(int i = 0; i < PreferenceDialog.this.mediaListModel.size() && medium != null; ++i) {
                     String s = (String)PreferenceDialog.this.mediaListModel.getElementAt(i);
                     int c = medium.compareTo(s);
                     if (c == 0) {
                        medium = null;
                     } else if (c < 0) {
                        PreferenceDialog.this.mediaListModel.insertElementAt(medium, i);
                        medium = null;
                     }
                  }

                  if (medium != null) {
                     PreferenceDialog.this.mediaListModel.addElement(medium);
                  }

               }
            }
         }
      });
      this.mediaListRemoveButton = new JButton(Resources.getString("PreferenceDialog.label.remove"));
      this.mediaListRemoveButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            int index = PreferenceDialog.this.mediaList.getSelectedIndex();
            PreferenceDialog.this.mediaList.clearSelection();
            if (index >= 0) {
               PreferenceDialog.this.mediaListModel.removeElementAt(index);
            }

         }
      });
      this.mediaListClearButton = new JButton(Resources.getString("PreferenceDialog.label.clear"));
      this.mediaListClearButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            PreferenceDialog.this.mediaList.clearSelection();
            PreferenceDialog.this.mediaListModel.removeAllElements();
         }
      });
      p.add(this.userStylesheetEnabled, 1, 0, 2, 1, 17, 0, 0.0, 0.0);
      p.add(this.userStylesheetLabel, 0, 1, 1, 1, 13, 0, 0.0, 0.0);
      p.add(this.userStylesheet, 1, 1, 1, 1, 17, 2, 1.0, 0.0);
      p.add(this.userStylesheetBrowse, 2, 1, 1, 1, 17, 2, 0.0, 0.0);
      p.add(mediaLabel, 0, 2, 1, 1, 13, 3, 0.0, 0.0);
      p.add(scrollPane, 1, 2, 1, 4, 17, 1, 1.0, 1.0);
      p.add(new JPanel(), 2, 2, 1, 1, 17, 1, 0.0, 1.0);
      p.add(addButton, 2, 3, 1, 1, 16, 2, 0.0, 0.0);
      p.add(this.mediaListRemoveButton, 2, 4, 1, 1, 16, 2, 0.0, 0.0);
      p.add(this.mediaListClearButton, 2, 5, 1, 1, 16, 2, 0.0, 0.0);
      return p;
   }

   protected void updateMediaListButtons() {
      this.mediaListRemoveButton.setEnabled(!this.mediaList.isSelectionEmpty());
      this.mediaListClearButton.setEnabled(!this.mediaListModel.isEmpty());
   }

   protected JPanel buildNetworkPanel() {
      JGridBagPanel p = new JGridBagPanel();
      p.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
      JLabel proxyLabel = new JLabel(Resources.getString("PreferenceDialog.label.http.proxy"));
      JLabel hostLabel = new JLabel(Resources.getString("PreferenceDialog.label.host"));
      JLabel portLabel = new JLabel(Resources.getString("PreferenceDialog.label.port"));
      JLabel colonLabel = new JLabel(Resources.getString("PreferenceDialog.label.colon"));
      Font f = hostLabel.getFont();
      float size = f.getSize2D() * 0.85F;
      f = f.deriveFont(size);
      hostLabel.setFont(f);
      portLabel.setFont(f);
      this.host = new JTextField();
      this.host.setPreferredSize(new Dimension(200, 20));
      this.port = new JTextField();
      this.port.setPreferredSize(new Dimension(40, 20));
      p.add(proxyLabel, 0, 0, 1, 1, 13, 0, 0.0, 0.0);
      p.add(this.host, 1, 0, 1, 1, 17, 2, 0.0, 0.0);
      p.add(colonLabel, 2, 0, 1, 1, 17, 0, 0.0, 0.0);
      p.add(this.port, 3, 0, 1, 1, 17, 2, 0.0, 0.0);
      p.add(hostLabel, 1, 1, 1, 1, 17, 0, 0.0, 0.0);
      p.add(portLabel, 3, 1, 1, 1, 17, 0, 0.0, 0.0);
      return p;
   }

   public int showDialog() {
      if (Platform.isOSX) {
         this.returnCode = 0;
      } else {
         this.returnCode = 1;
      }

      this.pack();
      this.setVisible(true);
      return this.returnCode;
   }

   protected class JConfigurationPanel extends JPanel {
      protected JToolBar toolbar = new JToolBar();
      protected JPanel panel;
      protected CardLayout layout;
      protected ButtonGroup group;
      protected int page = -1;

      public JConfigurationPanel() {
         this.toolbar.setFloatable(false);
         this.toolbar.setLayout(new FlowLayout(3, 0, 0));
         this.toolbar.add(new JToolBar.Separator(new Dimension(8, 8)));
         if (Platform.isOSX || PreferenceDialog.isMetalSteel()) {
            this.toolbar.setBackground(new Color(248, 248, 248));
         }

         this.toolbar.setOpaque(true);
         this.panel = new JPanel();
         this.layout = (CardLayout)(Platform.isOSX ? new ResizingCardLayout() : new CardLayout());
         this.group = new ButtonGroup();
         this.setLayout(new BorderLayout());
         this.panel.setLayout(this.layout);
         this.add(this.toolbar, "North");
         this.add(this.panel);
      }

      public void addPanel(String text, Icon icon, Icon icon2, JPanel p) {
         JToggleButton button = new JToggleButton(text, icon);
         button.setVerticalTextPosition(3);
         button.setHorizontalTextPosition(0);
         button.setContentAreaFilled(false);

         try {
            AbstractButton.class.getMethod("setIconTextGap", Integer.TYPE).invoke(button, 0);
         } catch (Exception var7) {
         }

         button.setPressedIcon(icon2);
         this.group.add(button);
         this.toolbar.add(button);
         this.toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
         button.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
               JToggleButton b = (JToggleButton)e.getSource();
               switch (e.getStateChange()) {
                  case 1:
                     JConfigurationPanel.this.select(b);
                     break;
                  case 2:
                     JConfigurationPanel.this.unselect(b);
               }

            }
         });
         if (this.panel.getComponentCount() == 0) {
            button.setSelected(true);
            this.page = 0;
         } else {
            this.unselect(button);
         }

         this.panel.add(p, text.intern());
      }

      protected int getComponentIndex(Component c) {
         Container p = c.getParent();
         int count = p.getComponentCount();

         for(int i = 0; i < count; ++i) {
            if (p.getComponent(i) == c) {
               return i;
            }
         }

         return -1;
      }

      protected void select(JToggleButton b) {
         b.setOpaque(true);
         b.setBackground(Platform.isOSX ? new Color(216, 216, 216) : UIManager.getColor("List.selectionBackground"));
         b.setForeground(UIManager.getColor("List.selectionForeground"));
         b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 1, 0, 1, new Color(160, 160, 160)), BorderFactory.createEmptyBorder(4, 3, 4, 3)));
         this.layout.show(this.panel, b.getText().intern());
         this.page = this.getComponentIndex(b) - 1;
         if (Platform.isOSX) {
            PreferenceDialog.this.setTitle(b.getText());
         }

         PreferenceDialog.this.pack();
         this.panel.grabFocus();
      }

      protected void unselect(JToggleButton b) {
         b.setOpaque(false);
         b.setBackground((Color)null);
         b.setForeground(UIManager.getColor("Button.foreground"));
         b.setBorder(BorderFactory.createEmptyBorder(5, 4, 5, 4));
      }

      protected class ResizingCardLayout extends CardLayout {
         public ResizingCardLayout() {
            super(0, 0);
         }

         public Dimension preferredLayoutSize(Container parent) {
            Dimension d = super.preferredLayoutSize(parent);
            if (JConfigurationPanel.this.page != -1) {
               Dimension cur = JConfigurationPanel.this.panel.getComponent(JConfigurationPanel.this.page).getPreferredSize();
               d = new Dimension((int)d.getWidth(), (int)cur.getHeight());
            }

            return d;
         }
      }
   }
}
