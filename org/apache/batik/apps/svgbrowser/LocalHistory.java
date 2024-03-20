package org.apache.batik.apps.svgbrowser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

public class LocalHistory {
   protected JSVGViewerFrame svgFrame;
   protected JMenu menu;
   protected int index;
   protected List visitedURIs = new ArrayList();
   protected int currentURI = -1;
   protected ButtonGroup group = new ButtonGroup();
   protected ActionListener actionListener = new RadioListener();
   protected int state;
   protected static final int STABLE_STATE = 0;
   protected static final int BACK_PENDING_STATE = 1;
   protected static final int FORWARD_PENDING_STATE = 2;
   protected static final int RELOAD_PENDING_STATE = 3;

   public LocalHistory(JMenuBar mb, JSVGViewerFrame svgFrame) {
      this.svgFrame = svgFrame;
      int mc = mb.getMenuCount();

      for(int i = 0; i < mc; ++i) {
         JMenu m = mb.getMenu(i);
         int ic = m.getItemCount();

         for(int j = 0; j < ic; ++j) {
            JMenuItem mi = m.getItem(j);
            if (mi != null) {
               String s = mi.getText();
               if ("@@@".equals(s)) {
                  this.menu = m;
                  this.index = j;
                  m.remove(j);
                  return;
               }
            }
         }
      }

      throw new IllegalArgumentException("No '@@@' marker found");
   }

   public void back() {
      this.update();
      this.state = 1;
      this.currentURI -= 2;
      this.svgFrame.showSVGDocument((String)this.visitedURIs.get(this.currentURI + 1));
   }

   public boolean canGoBack() {
      return this.currentURI > 0;
   }

   public void forward() {
      this.update();
      this.state = 2;
      this.svgFrame.showSVGDocument((String)this.visitedURIs.get(this.currentURI + 1));
   }

   public boolean canGoForward() {
      return this.currentURI < this.visitedURIs.size() - 1;
   }

   public void reload() {
      this.update();
      this.state = 3;
      --this.currentURI;
      this.svgFrame.showSVGDocument((String)this.visitedURIs.get(this.currentURI + 1));
   }

   public void update(String uri) {
      if (this.currentURI < -1) {
         throw new IllegalStateException("Unexpected currentURI:" + this.currentURI);
      } else {
         this.state = 0;
         int i;
         JMenuItem mi;
         if (++this.currentURI < this.visitedURIs.size()) {
            if (!this.visitedURIs.get(this.currentURI).equals(uri)) {
               int len = this.menu.getItemCount();

               for(i = len - 1; i >= this.index + this.currentURI + 1; --i) {
                  JMenuItem mi = this.menu.getItem(i);
                  this.group.remove(mi);
                  this.menu.remove(i);
               }

               this.visitedURIs = this.visitedURIs.subList(0, this.currentURI + 1);
            }

            mi = this.menu.getItem(this.index + this.currentURI);
            this.group.remove(mi);
            this.menu.remove(this.index + this.currentURI);
            this.visitedURIs.set(this.currentURI, uri);
         } else {
            if (this.visitedURIs.size() >= 15) {
               this.visitedURIs.remove(0);
               mi = this.menu.getItem(this.index);
               this.group.remove(mi);
               this.menu.remove(this.index);
               --this.currentURI;
            }

            this.visitedURIs.add(uri);
         }

         String text = uri;
         i = uri.lastIndexOf(47);
         if (i == -1) {
            i = uri.lastIndexOf(92);
         }

         if (i != -1) {
            text = uri.substring(i + 1);
         }

         JMenuItem mi = new JRadioButtonMenuItem(text);
         mi.setToolTipText(uri);
         mi.setActionCommand(uri);
         mi.addActionListener(this.actionListener);
         this.group.add(mi);
         mi.setSelected(true);
         this.menu.insert(mi, this.index + this.currentURI);
      }
   }

   protected void update() {
      switch (this.state) {
         case 0:
         case 2:
         default:
            break;
         case 1:
            this.currentURI += 2;
            break;
         case 3:
            ++this.currentURI;
      }

   }

   protected class RadioListener implements ActionListener {
      public void actionPerformed(ActionEvent e) {
         String uri = e.getActionCommand();
         LocalHistory.this.currentURI = this.getItemIndex((JMenuItem)e.getSource()) - 1;
         LocalHistory.this.svgFrame.showSVGDocument(uri);
      }

      public int getItemIndex(JMenuItem item) {
         int ic = LocalHistory.this.menu.getItemCount();

         for(int i = LocalHistory.this.index; i < ic; ++i) {
            if (LocalHistory.this.menu.getItem(i) == item) {
               return i - LocalHistory.this.index;
            }
         }

         throw new IllegalArgumentException("MenuItem is not from my menu!");
      }
   }
}
