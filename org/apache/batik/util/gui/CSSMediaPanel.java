package org.apache.batik.util.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.ButtonFactory;
import org.apache.batik.util.gui.resource.MissingListenerException;
import org.apache.batik.util.resources.ResourceManager;

public class CSSMediaPanel extends JPanel implements ActionMap {
   protected static final String RESOURCES = "org.apache.batik.util.gui.resources.CSSMediaPanel";
   protected static ResourceBundle bundle = ResourceBundle.getBundle("org.apache.batik.util.gui.resources.CSSMediaPanel", Locale.getDefault());
   protected static ResourceManager resources;
   protected JButton removeButton;
   protected JButton addButton;
   protected JButton clearButton;
   protected DefaultListModel listModel = new DefaultListModel();
   protected JList mediaList;
   protected Map listeners = new HashMap();

   public CSSMediaPanel() {
      super(new GridBagLayout());
      this.listeners.put("AddButtonAction", new AddButtonAction());
      this.listeners.put("RemoveButtonAction", new RemoveButtonAction());
      this.listeners.put("ClearButtonAction", new ClearButtonAction());
      this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), resources.getString("Panel.title")));
      ExtendedGridBagConstraints constraints = new ExtendedGridBagConstraints();
      constraints.insets = new Insets(5, 5, 5, 5);
      this.mediaList = new JList();
      this.mediaList.setSelectionMode(0);
      this.mediaList.setModel(this.listModel);
      this.mediaList.addListSelectionListener(new MediaListSelectionListener());
      this.listModel.addListDataListener(new MediaListDataListener());
      JScrollPane scrollPane = new JScrollPane();
      scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
      constraints.weightx = 1.0;
      constraints.weighty = 1.0;
      constraints.fill = 1;
      constraints.setGridBounds(0, 0, 1, 3);
      scrollPane.getViewport().add(this.mediaList);
      this.add(scrollPane, constraints);
      ButtonFactory bf = new ButtonFactory(bundle, this);
      constraints.weightx = 0.0;
      constraints.weighty = 0.0;
      constraints.fill = 2;
      constraints.anchor = 11;
      this.addButton = bf.createJButton("AddButton");
      constraints.setGridBounds(1, 0, 1, 1);
      this.add(this.addButton, constraints);
      this.removeButton = bf.createJButton("RemoveButton");
      constraints.setGridBounds(1, 1, 1, 1);
      this.add(this.removeButton, constraints);
      this.clearButton = bf.createJButton("ClearButton");
      constraints.setGridBounds(1, 2, 1, 1);
      this.add(this.clearButton, constraints);
      this.updateButtons();
   }

   protected void updateButtons() {
      this.removeButton.setEnabled(!this.mediaList.isSelectionEmpty());
      this.clearButton.setEnabled(!this.listModel.isEmpty());
   }

   public void setMedia(List mediaList) {
      this.listModel.removeAllElements();
      Iterator var2 = mediaList.iterator();

      while(var2.hasNext()) {
         Object aMediaList = var2.next();
         this.listModel.addElement(aMediaList);
      }

   }

   public void setMedia(String media) {
      this.listModel.removeAllElements();
      StringTokenizer tokens = new StringTokenizer(media, " ");

      while(tokens.hasMoreTokens()) {
         this.listModel.addElement(tokens.nextToken());
      }

   }

   public List getMedia() {
      List media = new ArrayList(this.listModel.size());
      Enumeration e = this.listModel.elements();

      while(e.hasMoreElements()) {
         media.add(e.nextElement());
      }

      return media;
   }

   public String getMediaAsString() {
      StringBuffer buffer = new StringBuffer();
      Enumeration e = this.listModel.elements();

      while(e.hasMoreElements()) {
         buffer.append((String)e.nextElement());
         buffer.append(' ');
      }

      return buffer.toString();
   }

   public static int showDialog(Component parent, String title) {
      return showDialog(parent, title, "");
   }

   public static int showDialog(Component parent, String title, List mediaList) {
      Dialog dialog = new Dialog(parent, title, mediaList);
      dialog.setModal(true);
      dialog.pack();
      dialog.setVisible(true);
      return dialog.getReturnCode();
   }

   public static int showDialog(Component parent, String title, String media) {
      Dialog dialog = new Dialog(parent, title, media);
      dialog.setModal(true);
      dialog.pack();
      dialog.setVisible(true);
      return dialog.getReturnCode();
   }

   public Action getAction(String key) throws MissingListenerException {
      return (Action)this.listeners.get(key);
   }

   public static void main(String[] args) {
      String media = "all aural braille embossed handheld print projection screen tty tv";
      int code = showDialog((Component)null, "Test", (String)media);
      System.out.println(code);
      System.exit(0);
   }

   static {
      resources = new ResourceManager(bundle);
   }

   public static class Dialog extends JDialog implements ActionMap {
      public static final int OK_OPTION = 0;
      public static final int CANCEL_OPTION = 1;
      protected int returnCode;
      protected Map listeners;

      public Dialog() {
         this((Component)null, "", (String)"");
      }

      public Dialog(Component parent, String title, List mediaList) {
         super(JOptionPane.getFrameForComponent(parent), title);
         this.listeners = new HashMap();
         this.listeners.put("OKButtonAction", new OKButtonAction());
         this.listeners.put("CancelButtonAction", new CancelButtonAction());
         CSSMediaPanel panel = new CSSMediaPanel();
         panel.setMedia(mediaList);
         this.getContentPane().add(panel, "Center");
         this.getContentPane().add(this.createButtonsPanel(), "South");
      }

      public Dialog(Component parent, String title, String media) {
         super(JOptionPane.getFrameForComponent(parent), title);
         this.listeners = new HashMap();
         this.listeners.put("OKButtonAction", new OKButtonAction());
         this.listeners.put("CancelButtonAction", new CancelButtonAction());
         CSSMediaPanel panel = new CSSMediaPanel();
         panel.setMedia(media);
         this.getContentPane().add(panel, "Center");
         this.getContentPane().add(this.createButtonsPanel(), "South");
      }

      public int getReturnCode() {
         return this.returnCode;
      }

      protected JPanel createButtonsPanel() {
         JPanel p = new JPanel(new FlowLayout(2));
         ButtonFactory bf = new ButtonFactory(CSSMediaPanel.bundle, this);
         p.add(bf.createJButton("OKButton"));
         p.add(bf.createJButton("CancelButton"));
         return p;
      }

      public Action getAction(String key) throws MissingListenerException {
         return (Action)this.listeners.get(key);
      }

      protected class CancelButtonAction extends AbstractAction {
         public void actionPerformed(ActionEvent e) {
            Dialog.this.returnCode = 1;
            Dialog.this.dispose();
         }
      }

      protected class OKButtonAction extends AbstractAction {
         public void actionPerformed(ActionEvent e) {
            Dialog.this.returnCode = 0;
            Dialog.this.dispose();
         }
      }
   }

   public static class AddMediumDialog extends JDialog implements ActionMap {
      public static final int OK_OPTION = 0;
      public static final int CANCEL_OPTION = 1;
      protected JComboBox medium;
      protected int returnCode;
      protected Map listeners = new HashMap();

      public AddMediumDialog(Component parent) {
         super(JOptionPane.getFrameForComponent(parent), CSSMediaPanel.resources.getString("AddMediumDialog.title"));
         this.setModal(true);
         this.listeners.put("OKButtonAction", new OKButtonAction());
         this.listeners.put("CancelButtonAction", new CancelButtonAction());
         this.getContentPane().add(this.createContentPanel(), "Center");
         this.getContentPane().add(this.createButtonsPanel(), "South");
      }

      public String getMedium() {
         return (String)this.medium.getSelectedItem();
      }

      protected Component createContentPanel() {
         JPanel panel = new JPanel(new BorderLayout());
         panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
         panel.add(new JLabel(CSSMediaPanel.resources.getString("AddMediumDialog.label")), "West");
         this.medium = new JComboBox();
         this.medium.setEditable(true);
         String media = CSSMediaPanel.resources.getString("Media.list");
         StringTokenizer tokens = new StringTokenizer(media, " ");

         while(tokens.hasMoreTokens()) {
            this.medium.addItem(tokens.nextToken());
         }

         panel.add(this.medium, "Center");
         return panel;
      }

      protected Component createButtonsPanel() {
         JPanel panel = new JPanel(new FlowLayout(2));
         ButtonFactory bf = new ButtonFactory(CSSMediaPanel.bundle, this);
         panel.add(bf.createJButton("OKButton"));
         panel.add(bf.createJButton("CancelButton"));
         return panel;
      }

      public int getReturnCode() {
         return this.returnCode;
      }

      public Action getAction(String key) throws MissingListenerException {
         return (Action)this.listeners.get(key);
      }

      protected class CancelButtonAction extends AbstractAction {
         public void actionPerformed(ActionEvent e) {
            AddMediumDialog.this.returnCode = 1;
            AddMediumDialog.this.dispose();
         }
      }

      protected class OKButtonAction extends AbstractAction {
         public void actionPerformed(ActionEvent e) {
            AddMediumDialog.this.returnCode = 0;
            AddMediumDialog.this.dispose();
         }
      }
   }

   protected class MediaListDataListener implements ListDataListener {
      public void contentsChanged(ListDataEvent e) {
         CSSMediaPanel.this.updateButtons();
      }

      public void intervalAdded(ListDataEvent e) {
         CSSMediaPanel.this.updateButtons();
      }

      public void intervalRemoved(ListDataEvent e) {
         CSSMediaPanel.this.updateButtons();
      }
   }

   protected class MediaListSelectionListener implements ListSelectionListener {
      public void valueChanged(ListSelectionEvent e) {
         CSSMediaPanel.this.updateButtons();
      }
   }

   protected class ClearButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         CSSMediaPanel.this.mediaList.clearSelection();
         CSSMediaPanel.this.listModel.removeAllElements();
      }
   }

   protected class RemoveButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         int index = CSSMediaPanel.this.mediaList.getSelectedIndex();
         CSSMediaPanel.this.mediaList.clearSelection();
         if (index >= 0) {
            CSSMediaPanel.this.listModel.removeElementAt(index);
         }

      }
   }

   protected class AddButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         AddMediumDialog dialog = new AddMediumDialog(CSSMediaPanel.this);
         dialog.pack();
         dialog.setVisible(true);
         if (dialog.getReturnCode() != 1 && dialog.getMedium() != null) {
            String medium = dialog.getMedium().trim();
            if (medium.length() != 0 && !CSSMediaPanel.this.listModel.contains(medium)) {
               for(int i = 0; i < CSSMediaPanel.this.listModel.size() && medium != null; ++i) {
                  String s = (String)CSSMediaPanel.this.listModel.getElementAt(i);
                  int c = medium.compareTo(s);
                  if (c == 0) {
                     medium = null;
                  } else if (c < 0) {
                     CSSMediaPanel.this.listModel.insertElementAt(medium, i);
                     medium = null;
                  }
               }

               if (medium != null) {
                  CSSMediaPanel.this.listModel.addElement(medium);
               }

            }
         }
      }
   }
}
