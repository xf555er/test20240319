package org.apache.batik.apps.svgbrowser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.batik.util.gui.DropDownComponent;
import org.apache.batik.util.resources.ResourceManager;

public class DropDownHistoryModel implements DropDownComponent.ScrollablePopupMenuModel {
   private static final String RESOURCES = "org.apache.batik.apps.svgbrowser.resources.DropDownHistoryModelMessages";
   private static ResourceBundle bundle = ResourceBundle.getBundle("org.apache.batik.apps.svgbrowser.resources.DropDownHistoryModelMessages", Locale.getDefault());
   private static ResourceManager resources;
   protected ArrayList items = new ArrayList();
   protected HistoryBrowserInterface historyBrowserInterface;
   protected DropDownComponent.ScrollablePopupMenu parent;

   public DropDownHistoryModel(DropDownComponent.ScrollablePopupMenu parent, HistoryBrowserInterface historyBrowserInterface) {
      this.parent = parent;
      this.historyBrowserInterface = historyBrowserInterface;
      historyBrowserInterface.getHistoryBrowser().addListener(new HistoryBrowser.HistoryBrowserAdapter() {
         public void historyReset(HistoryBrowser.HistoryBrowserEvent event) {
            DropDownHistoryModel.this.clearAllScrollablePopupMenuItems("");
         }
      });
   }

   public String getFooterText() {
      return "";
   }

   public DropDownComponent.ScrollablePopupMenuItem createItem(String itemName) {
      return new DropDownComponent.DefaultScrollablePopupMenuItem(this.parent, itemName);
   }

   protected void addItem(DropDownComponent.ScrollablePopupMenuItem item, String details) {
      int oldSize = this.items.size();
      this.items.add(0, item);
      this.parent.add(item, 0, oldSize, this.items.size());
      this.parent.fireItemsWereAdded(new DropDownComponent.ScrollablePopupMenuEvent(this.parent, 1, 1, details));
   }

   protected void removeItem(DropDownComponent.ScrollablePopupMenuItem item, String details) {
      int oldSize = this.items.size();
      this.items.remove(item);
      this.parent.remove(item, oldSize, this.items.size());
      this.parent.fireItemsWereRemoved(new DropDownComponent.ScrollablePopupMenuEvent(this.parent, 2, 1, details));
   }

   protected boolean removeLastScrollablePopupMenuItem(String details) {
      int i = this.items.size() - 1;
      if (i >= 0) {
         DropDownComponent.ScrollablePopupMenuItem item = (DropDownComponent.ScrollablePopupMenuItem)this.items.get(i);
         this.removeItem(item, details);
         return true;
      } else {
         return false;
      }
   }

   protected boolean removeFirstScrollablePopupMenuItem(String details) {
      Iterator var2 = this.items.iterator();
      if (var2.hasNext()) {
         Object item1 = var2.next();
         DropDownComponent.ScrollablePopupMenuItem item = (DropDownComponent.ScrollablePopupMenuItem)item1;
         this.removeItem(item, details);
         return true;
      } else {
         return false;
      }
   }

   protected void clearAllScrollablePopupMenuItems(String details) {
      while(this.removeLastScrollablePopupMenuItem(details)) {
      }

   }

   public void processItemClicked() {
   }

   public void processBeforeShowed() {
      this.historyBrowserInterface.performCurrentCompoundCommand();
   }

   public void processAfterShowed() {
   }

   static {
      resources = new ResourceManager(bundle);
   }

   public static class RedoPopUpMenuModel extends DropDownHistoryModel {
      protected static String REDO_FOOTER_TEXT;
      protected static String REDO_TOOLTIP_PREFIX;

      public RedoPopUpMenuModel(DropDownComponent.ScrollablePopupMenu parent, HistoryBrowserInterface historyBrowserInterface) {
         super(parent, historyBrowserInterface);
         this.init();
      }

      private void init() {
         this.historyBrowserInterface.getHistoryBrowser().addListener(new HistoryBrowser.HistoryBrowserAdapter() {
            public void executePerformed(HistoryBrowser.HistoryBrowserEvent event) {
               HistoryBrowser.CommandNamesInfo info = (HistoryBrowser.CommandNamesInfo)event.getSource();
               String details = DropDownHistoryModel.RedoPopUpMenuModel.REDO_TOOLTIP_PREFIX + info.getLastRedoableCommandName();
               RedoPopUpMenuModel.this.clearAllScrollablePopupMenuItems(details);
            }

            public void undoPerformed(HistoryBrowser.HistoryBrowserEvent event) {
               HistoryBrowser.CommandNamesInfo info = (HistoryBrowser.CommandNamesInfo)event.getSource();
               String details = DropDownHistoryModel.RedoPopUpMenuModel.REDO_TOOLTIP_PREFIX + info.getLastRedoableCommandName();
               RedoPopUpMenuModel.this.addItem(RedoPopUpMenuModel.this.createItem(info.getCommandName()), details);
            }

            public void redoPerformed(HistoryBrowser.HistoryBrowserEvent event) {
               HistoryBrowser.CommandNamesInfo info = (HistoryBrowser.CommandNamesInfo)event.getSource();
               String details = DropDownHistoryModel.RedoPopUpMenuModel.REDO_TOOLTIP_PREFIX + info.getLastRedoableCommandName();
               RedoPopUpMenuModel.this.removeFirstScrollablePopupMenuItem(details);
            }

            public void doCompoundEdit(HistoryBrowser.HistoryBrowserEvent event) {
               if (RedoPopUpMenuModel.this.parent.isEnabled()) {
                  RedoPopUpMenuModel.this.parent.setEnabled(false);
               }

            }

            public void compoundEditPerformed(HistoryBrowser.HistoryBrowserEvent event) {
            }
         });
      }

      public String getFooterText() {
         return REDO_FOOTER_TEXT;
      }

      public void processItemClicked() {
         this.historyBrowserInterface.getHistoryBrowser().compoundRedo(this.parent.getSelectedItemsCount());
      }

      static {
         REDO_FOOTER_TEXT = DropDownHistoryModel.resources.getString("RedoModel.footerText");
         REDO_TOOLTIP_PREFIX = DropDownHistoryModel.resources.getString("RedoModel.tooltipPrefix");
      }
   }

   public static class UndoPopUpMenuModel extends DropDownHistoryModel {
      protected static String UNDO_FOOTER_TEXT;
      protected static String UNDO_TOOLTIP_PREFIX;

      public UndoPopUpMenuModel(DropDownComponent.ScrollablePopupMenu parent, HistoryBrowserInterface historyBrowserInterface) {
         super(parent, historyBrowserInterface);
         this.init();
      }

      private void init() {
         this.historyBrowserInterface.getHistoryBrowser().addListener(new HistoryBrowser.HistoryBrowserAdapter() {
            public void executePerformed(HistoryBrowser.HistoryBrowserEvent event) {
               HistoryBrowser.CommandNamesInfo info = (HistoryBrowser.CommandNamesInfo)event.getSource();
               String details = DropDownHistoryModel.UndoPopUpMenuModel.UNDO_TOOLTIP_PREFIX + info.getLastUndoableCommandName();
               UndoPopUpMenuModel.this.addItem(UndoPopUpMenuModel.this.createItem(info.getCommandName()), details);
            }

            public void undoPerformed(HistoryBrowser.HistoryBrowserEvent event) {
               HistoryBrowser.CommandNamesInfo info = (HistoryBrowser.CommandNamesInfo)event.getSource();
               String details = DropDownHistoryModel.UndoPopUpMenuModel.UNDO_TOOLTIP_PREFIX + info.getLastUndoableCommandName();
               UndoPopUpMenuModel.this.removeFirstScrollablePopupMenuItem(details);
            }

            public void redoPerformed(HistoryBrowser.HistoryBrowserEvent event) {
               HistoryBrowser.CommandNamesInfo info = (HistoryBrowser.CommandNamesInfo)event.getSource();
               String details = DropDownHistoryModel.UndoPopUpMenuModel.UNDO_TOOLTIP_PREFIX + info.getLastUndoableCommandName();
               UndoPopUpMenuModel.this.addItem(UndoPopUpMenuModel.this.createItem(info.getCommandName()), details);
            }

            public void doCompoundEdit(HistoryBrowser.HistoryBrowserEvent event) {
               if (!UndoPopUpMenuModel.this.parent.isEnabled()) {
                  UndoPopUpMenuModel.this.parent.setEnabled(true);
               }

            }

            public void compoundEditPerformed(HistoryBrowser.HistoryBrowserEvent event) {
            }
         });
      }

      public String getFooterText() {
         return UNDO_FOOTER_TEXT;
      }

      public void processItemClicked() {
         this.historyBrowserInterface.getHistoryBrowser().compoundUndo(this.parent.getSelectedItemsCount());
      }

      static {
         UNDO_FOOTER_TEXT = DropDownHistoryModel.resources.getString("UndoModel.footerText");
         UNDO_TOOLTIP_PREFIX = DropDownHistoryModel.resources.getString("UndoModel.tooltipPrefix");
      }
   }
}
