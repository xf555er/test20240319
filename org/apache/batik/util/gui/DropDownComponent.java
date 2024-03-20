package org.apache.batik.util.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;
import java.util.EventObject;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.EventListenerList;
import javax.swing.plaf.basic.BasicButtonUI;
import org.apache.batik.util.resources.ResourceManager;

public class DropDownComponent extends JPanel {
   private JButton mainButton;
   private JButton dropDownButton;
   private Icon enabledDownArrow;
   private Icon disabledDownArrow;
   private ScrollablePopupMenu popupMenu = this.getPopupMenu();
   private boolean isDropDownEnabled;

   public DropDownComponent(JButton mainButton) {
      super(new BorderLayout());
      this.mainButton = mainButton;
      this.add(this.mainButton, "West");
      this.mainButton.setMaximumSize(new Dimension(24, 24));
      this.mainButton.setPreferredSize(new Dimension(24, 24));
      this.enabledDownArrow = new SmallDownArrow();
      this.disabledDownArrow = new SmallDisabledDownArrow();
      this.dropDownButton = new JButton(this.disabledDownArrow);
      this.dropDownButton.setBorderPainted(false);
      this.dropDownButton.setDisabledIcon(this.disabledDownArrow);
      this.dropDownButton.addMouseListener(new DropDownListener());
      this.dropDownButton.setMaximumSize(new Dimension(18, 24));
      this.dropDownButton.setMinimumSize(new Dimension(18, 10));
      this.dropDownButton.setPreferredSize(new Dimension(18, 10));
      this.dropDownButton.setFocusPainted(false);
      this.add(this.dropDownButton, "East");
      this.setEnabled(false);
   }

   public ScrollablePopupMenu getPopupMenu() {
      if (this.popupMenu == null) {
         this.popupMenu = new ScrollablePopupMenu(this);
         this.popupMenu.setEnabled(false);
         this.popupMenu.addPropertyChangeListener("enabled", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
               DropDownComponent.this.setEnabled((Boolean)evt.getNewValue());
            }
         });
         this.popupMenu.addListener(new ScrollablePopupMenuAdapter() {
            public void itemsWereAdded(ScrollablePopupMenuEvent ev) {
               DropDownComponent.this.updateMainButtonTooltip(ev.getDetails());
            }

            public void itemsWereRemoved(ScrollablePopupMenuEvent ev) {
               DropDownComponent.this.updateMainButtonTooltip(ev.getDetails());
            }
         });
      }

      return this.popupMenu;
   }

   public void setEnabled(boolean enable) {
      this.isDropDownEnabled = enable;
      this.mainButton.setEnabled(enable);
      this.dropDownButton.setEnabled(enable);
      this.dropDownButton.setIcon(enable ? this.enabledDownArrow : this.disabledDownArrow);
   }

   public boolean isEnabled() {
      return this.isDropDownEnabled;
   }

   public void updateMainButtonTooltip(String newTooltip) {
      this.mainButton.setToolTipText(newTooltip);
   }

   public static class ScrollablePopupMenuAdapter implements ScrollablePopupMenuListener {
      public void itemsWereAdded(ScrollablePopupMenuEvent ev) {
      }

      public void itemsWereRemoved(ScrollablePopupMenuEvent ev) {
      }
   }

   public interface ScrollablePopupMenuListener extends EventListener {
      void itemsWereAdded(ScrollablePopupMenuEvent var1);

      void itemsWereRemoved(ScrollablePopupMenuEvent var1);
   }

   public static class ScrollablePopupMenuEvent extends EventObject {
      public static final int ITEMS_ADDED = 1;
      public static final int ITEMS_REMOVED = 2;
      private int type;
      private int itemNumber;
      private String details;

      public ScrollablePopupMenuEvent(Object source, int type, int itemNumber, String details) {
         super(source);
         this.initEvent(type, itemNumber, details);
      }

      public void initEvent(int type, int itemNumber, String details) {
         this.type = type;
         this.itemNumber = itemNumber;
         this.details = details;
      }

      public String getDetails() {
         return this.details;
      }

      public int getItemNumber() {
         return this.itemNumber;
      }

      public int getType() {
         return this.type;
      }
   }

   public static class ScrollablePopupMenu extends JPopupMenu {
      private static final String RESOURCES = "org.apache.batik.util.gui.resources.ScrollablePopupMenuMessages";
      private static ResourceBundle bundle = ResourceBundle.getBundle("org.apache.batik.util.gui.resources.ScrollablePopupMenuMessages", Locale.getDefault());
      private static ResourceManager resources;
      private JPanel menuPanel = new JPanel();
      private JScrollPane scrollPane;
      private int preferredHeight;
      private ScrollablePopupMenuModel model;
      private JComponent ownerComponent;
      private ScrollablePopupMenuItem footer;
      private EventListenerList eventListeners;

      public ScrollablePopupMenu(JComponent owner) {
         this.preferredHeight = resources.getInteger("PreferredHeight");
         this.eventListeners = new EventListenerList();
         this.setLayout(new BorderLayout());
         this.menuPanel.setLayout(new GridLayout(0, 1));
         this.ownerComponent = owner;
         this.init();
      }

      private void init() {
         super.removeAll();
         this.scrollPane = new JScrollPane();
         this.scrollPane.setViewportView(this.menuPanel);
         this.scrollPane.setBorder((Border)null);
         int minWidth = resources.getInteger("ScrollPane.minWidth");
         int minHeight = resources.getInteger("ScrollPane.minHeight");
         int maxWidth = resources.getInteger("ScrollPane.maxWidth");
         int maxHeight = resources.getInteger("ScrollPane.maxHeight");
         this.scrollPane.setMinimumSize(new Dimension(minWidth, minHeight));
         this.scrollPane.setMaximumSize(new Dimension(maxWidth, maxHeight));
         this.scrollPane.setHorizontalScrollBarPolicy(31);
         this.add(this.scrollPane, "Center");
         this.addFooter(new DefaultScrollablePopupMenuItem(this, ""));
      }

      public void showMenu(Component invoker, Component refComponent) {
         this.model.processBeforeShowed();
         Point abs = new Point(0, refComponent.getHeight());
         SwingUtilities.convertPointToScreen(abs, refComponent);
         this.setLocation(abs);
         this.setInvoker(invoker);
         this.setVisible(true);
         this.revalidate();
         this.repaint();
         this.model.processAfterShowed();
      }

      public void add(ScrollablePopupMenuItem menuItem, int index, int oldSize, int newSize) {
         this.menuPanel.add((Component)menuItem, index);
         if (oldSize == 0) {
            this.setEnabled(true);
         }

      }

      public void remove(ScrollablePopupMenuItem menuItem, int oldSize, int newSize) {
         this.menuPanel.remove((Component)menuItem);
         if (newSize == 0) {
            this.setEnabled(false);
         }

      }

      private int getPreferredWidth() {
         Component[] components = this.menuPanel.getComponents();
         int maxWidth = 0;
         Component[] var3 = components;
         int var4 = components.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            Component component = var3[var5];
            int currentWidth = component.getPreferredSize().width;
            if (maxWidth < currentWidth) {
               maxWidth = currentWidth;
            }
         }

         int footerWidth = ((Component)this.footer).getPreferredSize().width;
         if (footerWidth > maxWidth) {
            maxWidth = footerWidth;
         }

         int widthOffset = 30;
         return maxWidth + widthOffset;
      }

      private int getPreferredHeight() {
         if (this.scrollPane.getPreferredSize().height < this.preferredHeight) {
            int heightOffset = 10;
            return this.scrollPane.getPreferredSize().height + ((Component)this.footer).getPreferredSize().height + heightOffset;
         } else {
            return this.preferredHeight + ((Component)this.footer).getPreferredSize().height;
         }
      }

      public Dimension getPreferredSize() {
         return new Dimension(this.getPreferredWidth(), this.getPreferredHeight());
      }

      public void selectionChanged(ScrollablePopupMenuItem targetItem, boolean wasSelected) {
         Component[] comps = this.menuPanel.getComponents();
         int n = comps.length;
         if (!wasSelected) {
            for(int i = n - 1; i >= 0; --i) {
               ScrollablePopupMenuItem item = (ScrollablePopupMenuItem)comps[i];
               item.setSelected(wasSelected);
            }
         } else {
            Component[] var10 = comps;
            int var11 = comps.length;

            for(int var7 = 0; var7 < var11; ++var7) {
               Component comp = var10[var7];
               ScrollablePopupMenuItem item = (ScrollablePopupMenuItem)comp;
               if (item == targetItem) {
                  break;
               }

               item.setSelected(true);
            }
         }

         this.footer.setText(this.model.getFooterText() + this.getSelectedItemsCount());
         this.repaint();
      }

      public void setModel(ScrollablePopupMenuModel model) {
         this.model = model;
         this.footer.setText(model.getFooterText());
      }

      public ScrollablePopupMenuModel getModel() {
         return this.model;
      }

      public int getSelectedItemsCount() {
         int selectionCount = 0;
         Component[] components = this.menuPanel.getComponents();
         Component[] var3 = components;
         int var4 = components.length;

         for(int var5 = 0; var5 < var4; ++var5) {
            Component component = var3[var5];
            ScrollablePopupMenuItem item = (ScrollablePopupMenuItem)component;
            if (item.isSelected()) {
               ++selectionCount;
            }
         }

         return selectionCount;
      }

      public void processItemClicked() {
         this.footer.setText(this.model.getFooterText() + 0);
         this.setVisible(false);
         this.model.processItemClicked();
      }

      public JComponent getOwner() {
         return this.ownerComponent;
      }

      private void addFooter(ScrollablePopupMenuItem footer) {
         this.footer = footer;
         this.footer.setEnabled(false);
         this.add((Component)this.footer, "South");
      }

      public ScrollablePopupMenuItem getFooter() {
         return this.footer;
      }

      public void addListener(ScrollablePopupMenuListener listener) {
         this.eventListeners.add(ScrollablePopupMenuListener.class, listener);
      }

      public void fireItemsWereAdded(ScrollablePopupMenuEvent event) {
         Object[] listeners = this.eventListeners.getListenerList();
         int length = listeners.length;

         for(int i = 0; i < length; i += 2) {
            if (listeners[i] == ScrollablePopupMenuListener.class) {
               ((ScrollablePopupMenuListener)listeners[i + 1]).itemsWereAdded(event);
            }
         }

      }

      public void fireItemsWereRemoved(ScrollablePopupMenuEvent event) {
         Object[] listeners = this.eventListeners.getListenerList();
         int length = listeners.length;

         for(int i = 0; i < length; i += 2) {
            if (listeners[i] == ScrollablePopupMenuListener.class) {
               ((ScrollablePopupMenuListener)listeners[i + 1]).itemsWereRemoved(event);
            }
         }

      }

      static {
         resources = new ResourceManager(bundle);
      }
   }

   public interface ScrollablePopupMenuModel {
      String getFooterText();

      void processItemClicked();

      void processBeforeShowed();

      void processAfterShowed();
   }

   public static class DefaultScrollablePopupMenuItem extends JButton implements ScrollablePopupMenuItem {
      public static final Color MENU_HIGHLIGHT_BG_COLOR = UIManager.getColor("MenuItem.selectionBackground");
      public static final Color MENU_HIGHLIGHT_FG_COLOR = UIManager.getColor("MenuItem.selectionForeground");
      public static final Color MENUITEM_BG_COLOR = UIManager.getColor("MenuItem.background");
      public static final Color MENUITEM_FG_COLOR = UIManager.getColor("MenuItem.foreground");
      private ScrollablePopupMenu parent;

      public DefaultScrollablePopupMenuItem(ScrollablePopupMenu parent, String text) {
         super(text);
         this.parent = parent;
         this.init();
      }

      private void init() {
         this.setUI(BasicButtonUI.createUI(this));
         this.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 20));
         this.setMenuItemDefaultColors();
         this.setAlignmentX(0.0F);
         this.setSelected(false);
         this.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
               if (DefaultScrollablePopupMenuItem.this.isEnabled()) {
                  DefaultScrollablePopupMenuItem.this.setSelected(true);
                  DefaultScrollablePopupMenuItem.this.parent.selectionChanged(DefaultScrollablePopupMenuItem.this, true);
               }

            }

            public void mouseExited(MouseEvent e) {
               if (DefaultScrollablePopupMenuItem.this.isEnabled()) {
                  DefaultScrollablePopupMenuItem.this.setSelected(false);
                  DefaultScrollablePopupMenuItem.this.parent.selectionChanged(DefaultScrollablePopupMenuItem.this, false);
               }

            }

            public void mouseClicked(MouseEvent e) {
               DefaultScrollablePopupMenuItem.this.parent.processItemClicked();
            }
         });
      }

      private void setMenuItemDefaultColors() {
         this.setBackground(MENUITEM_BG_COLOR);
         this.setForeground(MENUITEM_FG_COLOR);
      }

      public void setSelected(boolean selected) {
         super.setSelected(selected);
         if (selected) {
            this.setBackground(MENU_HIGHLIGHT_BG_COLOR);
            this.setForeground(MENU_HIGHLIGHT_FG_COLOR);
         } else {
            this.setMenuItemDefaultColors();
         }

      }

      public String getText() {
         return super.getText();
      }

      public void setText(String text) {
         super.setText(text);
      }

      public void setEnabled(boolean b) {
         super.setEnabled(b);
      }
   }

   public interface ScrollablePopupMenuItem {
      void setSelected(boolean var1);

      boolean isSelected();

      String getText();

      void setText(String var1);

      void setEnabled(boolean var1);
   }

   private static class SmallDisabledDownArrow extends SmallDownArrow {
      public SmallDisabledDownArrow() {
         super(null);
         this.arrowColor = new Color(140, 140, 140);
      }

      public void paintIcon(Component c, Graphics g, int x, int y) {
         super.paintIcon(c, g, x, y);
         g.setColor(Color.white);
         g.drawLine(x + 3, y + 2, x + 4, y + 1);
         g.drawLine(x + 3, y + 3, x + 5, y + 1);
      }
   }

   private static class SmallDownArrow implements Icon {
      protected Color arrowColor;

      private SmallDownArrow() {
         this.arrowColor = Color.black;
      }

      public void paintIcon(Component c, Graphics g, int x, int y) {
         g.setColor(this.arrowColor);
         g.drawLine(x, y, x + 4, y);
         g.drawLine(x + 1, y + 1, x + 3, y + 1);
         g.drawLine(x + 2, y + 2, x + 2, y + 2);
      }

      public int getIconWidth() {
         return 6;
      }

      public int getIconHeight() {
         return 4;
      }

      // $FF: synthetic method
      SmallDownArrow(Object x0) {
         this();
      }
   }

   private class DropDownListener extends MouseAdapter {
      private DropDownListener() {
      }

      public void mousePressed(MouseEvent e) {
         if (DropDownComponent.this.popupMenu.isShowing() && DropDownComponent.this.isDropDownEnabled) {
            DropDownComponent.this.popupMenu.setVisible(false);
         } else if (DropDownComponent.this.isDropDownEnabled) {
            DropDownComponent.this.popupMenu.showMenu((Component)e.getSource(), DropDownComponent.this);
         }

      }

      public void mouseEntered(MouseEvent ev) {
         DropDownComponent.this.dropDownButton.setBorderPainted(true);
      }

      public void mouseExited(MouseEvent ev) {
         DropDownComponent.this.dropDownButton.setBorderPainted(false);
      }

      // $FF: synthetic method
      DropDownListener(Object x1) {
         this();
      }
   }
}
