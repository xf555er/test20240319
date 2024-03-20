package org.apache.batik.util.gui;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.ButtonFactory;
import org.apache.batik.util.gui.resource.MissingListenerException;
import org.apache.batik.util.resources.ResourceManager;

public class LanguageDialog extends JDialog implements ActionMap {
   public static final int OK_OPTION = 0;
   public static final int CANCEL_OPTION = 1;
   protected static final String RESOURCES = "org.apache.batik.util.gui.resources.LanguageDialogMessages";
   protected static ResourceBundle bundle = ResourceBundle.getBundle("org.apache.batik.util.gui.resources.LanguageDialogMessages", Locale.getDefault());
   protected static ResourceManager resources;
   protected Map listeners = new HashMap();
   protected Panel panel = new Panel();
   protected int returnCode;

   public LanguageDialog(JFrame f) {
      super(f);
      this.setModal(true);
      this.setTitle(resources.getString("Dialog.title"));
      this.listeners.put("OKButtonAction", new OKButtonAction());
      this.listeners.put("CancelButtonAction", new CancelButtonAction());
      this.getContentPane().add(this.panel);
      this.getContentPane().add(this.createButtonsPanel(), "South");
      this.pack();
   }

   public int showDialog() {
      this.setVisible(true);
      return this.returnCode;
   }

   public void setLanguages(String s) {
      this.panel.setLanguages(s);
   }

   public String getLanguages() {
      return this.panel.getLanguages();
   }

   public Action getAction(String key) throws MissingListenerException {
      return (Action)this.listeners.get(key);
   }

   protected JPanel createButtonsPanel() {
      JPanel p = new JPanel(new FlowLayout(2));
      ButtonFactory bf = new ButtonFactory(bundle, this);
      p.add(bf.createJButton("OKButton"));
      p.add(bf.createJButton("CancelButton"));
      return p;
   }

   static {
      resources = new ResourceManager(bundle);
   }

   protected class CancelButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         LanguageDialog.this.returnCode = 1;
         LanguageDialog.this.dispose();
      }
   }

   protected class OKButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         LanguageDialog.this.returnCode = 0;
         LanguageDialog.this.dispose();
      }
   }

   public static class Panel extends JPanel implements ActionMap {
      protected JList userList;
      protected JList languageList;
      protected DefaultListModel userListModel = new DefaultListModel();
      protected DefaultListModel languageListModel = new DefaultListModel();
      protected JButton addLanguageButton;
      protected JButton removeLanguageButton;
      protected JButton upLanguageButton;
      protected JButton downLanguageButton;
      protected JButton clearLanguageButton;
      protected Map listeners = new HashMap();
      private static Map iconMap = null;

      public Panel() {
         super(new GridBagLayout());
         initCountryIcons();
         this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), LanguageDialog.resources.getString("Panel.title")));
         this.listeners.put("AddLanguageButtonAction", new AddLanguageButtonAction());
         this.listeners.put("RemoveLanguageButtonAction", new RemoveLanguageButtonAction());
         this.listeners.put("UpLanguageButtonAction", new UpLanguageButtonAction());
         this.listeners.put("DownLanguageButtonAction", new DownLanguageButtonAction());
         this.listeners.put("ClearLanguageButtonAction", new ClearLanguageButtonAction());
         this.userList = new JList(this.userListModel);
         this.userList.setCellRenderer(new IconAndTextCellRenderer());
         this.languageList = new JList(this.languageListModel);
         this.languageList.setCellRenderer(new IconAndTextCellRenderer());
         StringTokenizer st = new StringTokenizer(LanguageDialog.resources.getString("Country.list"), " ");

         while(st.hasMoreTokens()) {
            this.languageListModel.addElement(st.nextToken());
         }

         ExtendedGridBagConstraints constraints = new ExtendedGridBagConstraints();
         constraints.insets = new Insets(5, 5, 5, 5);
         constraints.weightx = 1.0;
         constraints.weighty = 1.0;
         constraints.fill = 1;
         constraints.setGridBounds(0, 0, 1, 1);
         JScrollPane sp = new JScrollPane();
         sp.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), LanguageDialog.resources.getString("Languages.title")), BorderFactory.createLoweredBevelBorder()));
         sp.getViewport().add(this.languageList);
         this.add(sp, constraints);
         this.languageList.setSelectionMode(0);
         this.languageList.addListSelectionListener(new LanguageListSelectionListener());
         constraints.setGridBounds(2, 0, 1, 1);
         JScrollPane sp2 = new JScrollPane();
         sp2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), LanguageDialog.resources.getString("User.title")), BorderFactory.createLoweredBevelBorder()));
         sp2.getViewport().add(this.userList);
         this.add(sp2, constraints);
         this.userList.setSelectionMode(0);
         this.userList.addListSelectionListener(new UserListSelectionListener());
         constraints.setGridBounds(0, 1, 3, 1);
         constraints.weightx = 0.0;
         constraints.weighty = 0.0;
         this.add(new JLabel(LanguageDialog.resources.getString("InfoLabel.text")), constraints);
         ButtonFactory bf = new ButtonFactory(LanguageDialog.bundle, this);
         JPanel p = new JPanel(new GridLayout(5, 1, 0, 3));
         p.add(this.addLanguageButton = bf.createJButton("AddLanguageButton"));
         this.addLanguageButton.setEnabled(false);
         p.add(this.removeLanguageButton = bf.createJButton("RemoveLanguageButton"));
         this.removeLanguageButton.setEnabled(false);
         p.add(this.upLanguageButton = bf.createJButton("UpLanguageButton"));
         this.upLanguageButton.setEnabled(false);
         p.add(this.downLanguageButton = bf.createJButton("DownLanguageButton"));
         this.downLanguageButton.setEnabled(false);
         p.add(this.clearLanguageButton = bf.createJButton("ClearLanguageButton"));
         this.clearLanguageButton.setEnabled(false);
         JPanel t = new JPanel(new GridBagLayout());
         constraints.setGridBounds(1, 0, 1, 1);
         this.add(t, constraints);
         constraints.fill = 2;
         constraints.setGridBounds(0, 0, 1, 1);
         constraints.insets = new Insets(0, 0, 0, 0);
         t.add(p, constraints);
         sp2.setPreferredSize(sp.getPreferredSize());
      }

      public static synchronized void initCountryIcons() {
         if (iconMap == null) {
            iconMap = new HashMap();
            StringTokenizer st = new StringTokenizer(LanguageDialog.resources.getString("Country.list"), " ");

            while(st.hasMoreTokens()) {
               computeCountryIcon(Panel.class, st.nextToken());
            }
         }

      }

      public String getLanguages() {
         StringBuffer result = new StringBuffer();
         if (this.userListModel.getSize() > 0) {
            result.append(this.userListModel.getElementAt(0));

            for(int i = 1; i < this.userListModel.getSize(); ++i) {
               result.append(',');
               result.append(this.userListModel.getElementAt(i));
            }
         }

         return result.toString();
      }

      public void setLanguages(String str) {
         int len = this.userListModel.getSize();

         for(int i = 0; i < len; ++i) {
            Object o = this.userListModel.getElementAt(0);
            this.userListModel.removeElementAt(0);
            String userListModelStr = (String)o;
            int size = this.languageListModel.getSize();

            int n;
            for(n = 0; n < size; ++n) {
               String s = (String)this.languageListModel.getElementAt(n);
               if (userListModelStr.compareTo(s) > 0) {
                  break;
               }
            }

            this.languageListModel.insertElementAt(o, n);
         }

         StringTokenizer st = new StringTokenizer(str, ",");

         while(st.hasMoreTokens()) {
            String s = st.nextToken();
            this.userListModel.addElement(s);
            this.languageListModel.removeElement(s);
         }

         this.updateButtons();
      }

      protected void updateButtons() {
         int size = this.userListModel.size();
         int i = this.userList.getSelectedIndex();
         boolean empty = size == 0;
         boolean selected = i != -1;
         boolean zeroSelected = i == 0;
         boolean lastSelected = i == size - 1;
         this.removeLanguageButton.setEnabled(!empty && selected);
         this.upLanguageButton.setEnabled(!empty && selected && !zeroSelected);
         this.downLanguageButton.setEnabled(!empty && selected && !lastSelected);
         this.clearLanguageButton.setEnabled(!empty);
         size = this.languageListModel.size();
         i = this.languageList.getSelectedIndex();
         empty = size == 0;
         selected = i != -1;
         this.addLanguageButton.setEnabled(!empty && selected);
      }

      protected String getCountryText(String code) {
         return LanguageDialog.resources.getString(code + ".text");
      }

      protected Icon getCountryIcon(String code) {
         return computeCountryIcon(this.getClass(), code);
      }

      private static Icon computeCountryIcon(Class ref, String code) {
         ImageIcon icon = null;

         try {
            if ((icon = (ImageIcon)iconMap.get(code)) != null) {
               return icon;
            }

            String s = LanguageDialog.resources.getString(code + ".icon");
            URL url = ref.getResource(s);
            if (url != null) {
               iconMap.put(code, icon = new ImageIcon(url));
               return icon;
            }
         } catch (MissingResourceException var5) {
         }

         return new ImageIcon(ref.getResource("resources/blank.gif"));
      }

      public Action getAction(String key) throws MissingListenerException {
         return (Action)this.listeners.get(key);
      }

      protected class IconAndTextCellRenderer extends JLabel implements ListCellRenderer {
         public IconAndTextCellRenderer() {
            this.setOpaque(true);
            this.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
         }

         public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            String s = (String)value;
            this.setText(Panel.this.getCountryText(s));
            this.setIcon(Panel.this.getCountryIcon(s));
            this.setEnabled(list.isEnabled());
            this.setFont(list.getFont());
            if (isSelected) {
               this.setBackground(list.getSelectionBackground());
               this.setForeground(list.getSelectionForeground());
            } else {
               this.setBackground(list.getBackground());
               this.setForeground(list.getForeground());
            }

            return this;
         }
      }

      protected class UserListSelectionListener implements ListSelectionListener {
         public void valueChanged(ListSelectionEvent e) {
            int i = Panel.this.userList.getSelectedIndex();
            Panel.this.languageList.getSelectionModel().clearSelection();
            Panel.this.userList.setSelectedIndex(i);
            Panel.this.updateButtons();
         }
      }

      protected class LanguageListSelectionListener implements ListSelectionListener {
         public void valueChanged(ListSelectionEvent e) {
            int i = Panel.this.languageList.getSelectedIndex();
            Panel.this.userList.getSelectionModel().clearSelection();
            Panel.this.languageList.setSelectedIndex(i);
            Panel.this.updateButtons();
         }
      }

      protected class ClearLanguageButtonAction extends AbstractAction {
         public void actionPerformed(ActionEvent e) {
            int len = Panel.this.userListModel.getSize();

            for(int i = 0; i < len; ++i) {
               Object o = Panel.this.userListModel.getElementAt(0);
               Panel.this.userListModel.removeElementAt(0);
               String userListModelStr = (String)o;
               int size = Panel.this.languageListModel.getSize();

               int n;
               for(n = 0; n < size; ++n) {
                  String s = (String)Panel.this.languageListModel.getElementAt(n);
                  if (userListModelStr.compareTo(s) > 0) {
                     break;
                  }
               }

               Panel.this.languageListModel.insertElementAt(o, n);
            }

            Panel.this.updateButtons();
         }
      }

      protected class DownLanguageButtonAction extends AbstractAction {
         public void actionPerformed(ActionEvent e) {
            int i = Panel.this.userList.getSelectedIndex();
            Object o = Panel.this.userListModel.getElementAt(i);
            Panel.this.userListModel.removeElementAt(i);
            Panel.this.userListModel.insertElementAt(o, i + 1);
            Panel.this.userList.setSelectedIndex(i + 1);
         }
      }

      protected class UpLanguageButtonAction extends AbstractAction {
         public void actionPerformed(ActionEvent e) {
            int i = Panel.this.userList.getSelectedIndex();
            Object o = Panel.this.userListModel.getElementAt(i);
            Panel.this.userListModel.removeElementAt(i);
            Panel.this.userListModel.insertElementAt(o, i - 1);
            Panel.this.userList.setSelectedIndex(i - 1);
         }
      }

      protected class RemoveLanguageButtonAction extends AbstractAction {
         public void actionPerformed(ActionEvent e) {
            int i = Panel.this.userList.getSelectedIndex();
            Object o = Panel.this.userListModel.getElementAt(i);
            Panel.this.userListModel.removeElementAt(i);
            String userListModelStr = (String)o;
            int size = Panel.this.languageListModel.getSize();

            int n;
            for(n = 0; n < size; ++n) {
               String s = (String)Panel.this.languageListModel.getElementAt(n);
               if (userListModelStr.compareTo(s) > 0) {
                  break;
               }
            }

            Panel.this.languageListModel.insertElementAt(o, n);
            Panel.this.languageList.setSelectedValue(o, true);
            Panel.this.updateButtons();
         }
      }

      protected class AddLanguageButtonAction extends AbstractAction {
         public void actionPerformed(ActionEvent e) {
            int i = Panel.this.languageList.getSelectedIndex();
            Object o = Panel.this.languageListModel.getElementAt(i);
            Panel.this.languageListModel.removeElementAt(i);
            Panel.this.userListModel.addElement(o);
            Panel.this.userList.setSelectedValue(o, true);
         }
      }
   }
}
