package org.apache.batik.util.gui.resource;

import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.swing.JButton;
import javax.swing.JToolBar;
import org.apache.batik.util.resources.ResourceFormatException;
import org.apache.batik.util.resources.ResourceManager;

public class ToolBarFactory extends ResourceManager {
   private static final String SEPARATOR = "-";
   private ButtonFactory buttonFactory;

   public ToolBarFactory(ResourceBundle rb, ActionMap am) {
      super(rb);
      this.buttonFactory = new ButtonFactory(rb, am);
   }

   public JToolBar createJToolBar(String name) throws MissingResourceException, ResourceFormatException, MissingListenerException {
      JToolBar result = new JToolBar();
      List buttons = this.getStringList(name);
      Iterator var4 = buttons.iterator();

      while(var4.hasNext()) {
         Object button = var4.next();
         String s = (String)button;
         if (s.equals("-")) {
            result.add(new JToolbarSeparator());
         } else {
            result.add(this.createJButton(s));
         }
      }

      return result;
   }

   public JButton createJButton(String name) throws MissingResourceException, ResourceFormatException, MissingListenerException {
      return this.buttonFactory.createJToolbarButton(name);
   }
}
