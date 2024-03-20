package org.apache.batik.util.gui.resource;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import org.apache.batik.util.resources.ResourceFormatException;
import org.apache.batik.util.resources.ResourceManager;

public class ButtonFactory extends ResourceManager {
   private static final String ICON_SUFFIX = ".icon";
   private static final String TEXT_SUFFIX = ".text";
   private static final String MNEMONIC_SUFFIX = ".mnemonic";
   private static final String ACTION_SUFFIX = ".action";
   private static final String SELECTED_SUFFIX = ".selected";
   private static final String TOOLTIP_SUFFIX = ".tooltip";
   private ActionMap actions;

   public ButtonFactory(ResourceBundle rb, ActionMap am) {
      super(rb);
      this.actions = am;
   }

   public JButton createJButton(String name) throws MissingResourceException, ResourceFormatException, MissingListenerException {
      JButton result;
      try {
         result = new JButton(this.getString(name + ".text"));
      } catch (MissingResourceException var4) {
         result = new JButton();
      }

      this.initializeButton(result, name);
      return result;
   }

   public JButton createJToolbarButton(String name) throws MissingResourceException, ResourceFormatException, MissingListenerException {
      JToolbarButton result;
      try {
         result = new JToolbarButton(this.getString(name + ".text"));
      } catch (MissingResourceException var4) {
         result = new JToolbarButton();
      }

      this.initializeButton(result, name);
      return result;
   }

   public JToggleButton createJToolbarToggleButton(String name) throws MissingResourceException, ResourceFormatException, MissingListenerException {
      JToolbarToggleButton result;
      try {
         result = new JToolbarToggleButton(this.getString(name + ".text"));
      } catch (MissingResourceException var4) {
         result = new JToolbarToggleButton();
      }

      this.initializeButton(result, name);
      return result;
   }

   public JRadioButton createJRadioButton(String name) throws MissingResourceException, ResourceFormatException, MissingListenerException {
      JRadioButton result = new JRadioButton(this.getString(name + ".text"));
      this.initializeButton(result, name);

      try {
         result.setSelected(this.getBoolean(name + ".selected"));
      } catch (MissingResourceException var4) {
      }

      return result;
   }

   public JCheckBox createJCheckBox(String name) throws MissingResourceException, ResourceFormatException, MissingListenerException {
      JCheckBox result = new JCheckBox(this.getString(name + ".text"));
      this.initializeButton(result, name);

      try {
         result.setSelected(this.getBoolean(name + ".selected"));
      } catch (MissingResourceException var4) {
      }

      return result;
   }

   private void initializeButton(AbstractButton b, String name) throws ResourceFormatException, MissingListenerException {
      try {
         Action a = this.actions.getAction(this.getString(name + ".action"));
         if (a == null) {
            throw new MissingListenerException("", "Action", name + ".action");
         }

         b.setAction(a);

         try {
            b.setText(this.getString(name + ".text"));
         } catch (MissingResourceException var8) {
         }

         if (a instanceof JComponentModifier) {
            ((JComponentModifier)a).addJComponent(b);
         }
      } catch (MissingResourceException var9) {
      }

      String s;
      try {
         s = this.getString(name + ".icon");
         URL url = this.actions.getClass().getResource(s);
         if (url != null) {
            b.setIcon(new ImageIcon(url));
         }
      } catch (MissingResourceException var7) {
      }

      try {
         s = this.getString(name + ".mnemonic");
         if (s.length() != 1) {
            throw new ResourceFormatException("Malformed mnemonic", this.bundle.getClass().getName(), name + ".mnemonic");
         }

         b.setMnemonic(s.charAt(0));
      } catch (MissingResourceException var6) {
      }

      try {
         s = this.getString(name + ".tooltip");
         if (s != null) {
            b.setToolTipText(s);
         }
      } catch (MissingResourceException var5) {
      }

   }
}
