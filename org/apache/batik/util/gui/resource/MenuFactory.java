package org.apache.batik.util.gui.resource;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import org.apache.batik.util.resources.ResourceFormatException;
import org.apache.batik.util.resources.ResourceManager;

public class MenuFactory extends ResourceManager {
   private static final String TYPE_MENU = "MENU";
   private static final String TYPE_ITEM = "ITEM";
   private static final String TYPE_RADIO = "RADIO";
   private static final String TYPE_CHECK = "CHECK";
   private static final String SEPARATOR = "-";
   private static final String TYPE_SUFFIX = ".type";
   private static final String TEXT_SUFFIX = ".text";
   private static final String MNEMONIC_SUFFIX = ".mnemonic";
   private static final String ACCELERATOR_SUFFIX = ".accelerator";
   private static final String ACTION_SUFFIX = ".action";
   private static final String SELECTED_SUFFIX = ".selected";
   private static final String ENABLED_SUFFIX = ".enabled";
   private static final String ICON_SUFFIX = ".icon";
   private ActionMap actions;
   private ButtonGroup buttonGroup;

   public MenuFactory(ResourceBundle rb, ActionMap am) {
      super(rb);
      this.actions = am;
      this.buttonGroup = null;
   }

   public JMenuBar createJMenuBar(String name) throws MissingResourceException, ResourceFormatException, MissingListenerException {
      return this.createJMenuBar(name, (String)null);
   }

   public JMenuBar createJMenuBar(String name, String specialization) throws MissingResourceException, ResourceFormatException, MissingListenerException {
      JMenuBar result = new JMenuBar();
      List menus = this.getSpecializedStringList(name, specialization);
      Iterator var5 = menus.iterator();

      while(var5.hasNext()) {
         Object menu = var5.next();
         result.add(this.createJMenuComponent((String)menu, specialization));
      }

      return result;
   }

   protected String getSpecializedString(String name, String specialization) {
      String s;
      try {
         s = this.getString(name + '.' + specialization);
      } catch (MissingResourceException var5) {
         s = this.getString(name);
      }

      return s;
   }

   protected List getSpecializedStringList(String name, String specialization) {
      List l;
      try {
         l = this.getStringList(name + '.' + specialization);
      } catch (MissingResourceException var5) {
         l = this.getStringList(name);
      }

      return l;
   }

   protected boolean getSpecializedBoolean(String name, String specialization) {
      boolean b;
      try {
         b = this.getBoolean(name + '.' + specialization);
      } catch (MissingResourceException var5) {
         b = this.getBoolean(name);
      }

      return b;
   }

   protected JComponent createJMenuComponent(String name, String specialization) throws MissingResourceException, ResourceFormatException, MissingListenerException {
      if (name.equals("-")) {
         this.buttonGroup = null;
         return new JSeparator();
      } else {
         String type = this.getSpecializedString(name + ".type", specialization);
         JComponent item = null;
         if (type.equals("RADIO")) {
            if (this.buttonGroup == null) {
               this.buttonGroup = new ButtonGroup();
            }
         } else {
            this.buttonGroup = null;
         }

         if (type.equals("MENU")) {
            item = this.createJMenu(name, specialization);
         } else if (type.equals("ITEM")) {
            item = this.createJMenuItem(name, specialization);
         } else if (type.equals("RADIO")) {
            item = this.createJRadioButtonMenuItem(name, specialization);
            this.buttonGroup.add((AbstractButton)item);
         } else {
            if (!type.equals("CHECK")) {
               throw new ResourceFormatException("Malformed resource", this.bundle.getClass().getName(), name + ".type");
            }

            item = this.createJCheckBoxMenuItem(name, specialization);
         }

         return (JComponent)item;
      }
   }

   public JMenu createJMenu(String name) throws MissingResourceException, ResourceFormatException, MissingListenerException {
      return this.createJMenu(name, (String)null);
   }

   public JMenu createJMenu(String name, String specialization) throws MissingResourceException, ResourceFormatException, MissingListenerException {
      JMenu result = new JMenu(this.getSpecializedString(name + ".text", specialization));
      this.initializeJMenuItem(result, name, specialization);
      List items = this.getSpecializedStringList(name, specialization);
      Iterator var5 = items.iterator();

      while(var5.hasNext()) {
         Object item = var5.next();
         result.add(this.createJMenuComponent((String)item, specialization));
      }

      return result;
   }

   public JMenuItem createJMenuItem(String name) throws MissingResourceException, ResourceFormatException, MissingListenerException {
      return this.createJMenuItem(name, (String)null);
   }

   public JMenuItem createJMenuItem(String name, String specialization) throws MissingResourceException, ResourceFormatException, MissingListenerException {
      JMenuItem result = new JMenuItem(this.getSpecializedString(name + ".text", specialization));
      this.initializeJMenuItem(result, name, specialization);
      return result;
   }

   public JRadioButtonMenuItem createJRadioButtonMenuItem(String name) throws MissingResourceException, ResourceFormatException, MissingListenerException {
      return this.createJRadioButtonMenuItem(name, (String)null);
   }

   public JRadioButtonMenuItem createJRadioButtonMenuItem(String name, String specialization) throws MissingResourceException, ResourceFormatException, MissingListenerException {
      JRadioButtonMenuItem result = new JRadioButtonMenuItem(this.getSpecializedString(name + ".text", specialization));
      this.initializeJMenuItem(result, name, specialization);

      try {
         result.setSelected(this.getSpecializedBoolean(name + ".selected", specialization));
      } catch (MissingResourceException var5) {
      }

      return result;
   }

   public JCheckBoxMenuItem createJCheckBoxMenuItem(String name) throws MissingResourceException, ResourceFormatException, MissingListenerException {
      return this.createJCheckBoxMenuItem(name, (String)null);
   }

   public JCheckBoxMenuItem createJCheckBoxMenuItem(String name, String specialization) throws MissingResourceException, ResourceFormatException, MissingListenerException {
      JCheckBoxMenuItem result = new JCheckBoxMenuItem(this.getSpecializedString(name + ".text", specialization));
      this.initializeJMenuItem(result, name, specialization);

      try {
         result.setSelected(this.getSpecializedBoolean(name + ".selected", specialization));
      } catch (MissingResourceException var5) {
      }

      return result;
   }

   protected void initializeJMenuItem(JMenuItem item, String name, String specialization) throws ResourceFormatException, MissingListenerException {
      try {
         Action a = this.actions.getAction(this.getSpecializedString(name + ".action", specialization));
         if (a == null) {
            throw new MissingListenerException("", "Action", name + ".action");
         }

         item.setAction(a);
         item.setText(this.getSpecializedString(name + ".text", specialization));
         if (a instanceof JComponentModifier) {
            ((JComponentModifier)a).addJComponent(item);
         }
      } catch (MissingResourceException var10) {
      }

      String str;
      try {
         str = this.getSpecializedString(name + ".icon", specialization);
         URL url = this.actions.getClass().getResource(str);
         if (url != null) {
            item.setIcon(new ImageIcon(url));
         }
      } catch (MissingResourceException var9) {
      }

      try {
         str = this.getSpecializedString(name + ".mnemonic", specialization);
         if (str.length() != 1) {
            throw new ResourceFormatException("Malformed mnemonic", this.bundle.getClass().getName(), name + ".mnemonic");
         }

         item.setMnemonic(str.charAt(0));
      } catch (MissingResourceException var8) {
      }

      try {
         if (!(item instanceof JMenu)) {
            str = this.getSpecializedString(name + ".accelerator", specialization);
            KeyStroke ks = KeyStroke.getKeyStroke(str);
            if (ks == null) {
               throw new ResourceFormatException("Malformed accelerator", this.bundle.getClass().getName(), name + ".accelerator");
            }

            item.setAccelerator(ks);
         }
      } catch (MissingResourceException var7) {
      }

      try {
         item.setEnabled(this.getSpecializedBoolean(name + ".enabled", specialization));
      } catch (MissingResourceException var6) {
      }

   }
}
