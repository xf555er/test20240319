package org.apache.batik.ext.swing;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import javax.swing.JPanel;

public class JGridBagPanel extends JPanel implements GridBagConstants {
   public static final InsetsManager ZERO_INSETS = new ZeroInsetsManager();
   public static final InsetsManager DEFAULT_INSETS = new DefaultInsetsManager();
   public InsetsManager insetsManager;

   public JGridBagPanel() {
      this(new DefaultInsetsManager());
   }

   public JGridBagPanel(InsetsManager insetsManager) {
      super(new GridBagLayout());
      if (insetsManager != null) {
         this.insetsManager = insetsManager;
      } else {
         this.insetsManager = new DefaultInsetsManager();
      }

   }

   public void setLayout(LayoutManager layout) {
      if (layout instanceof GridBagLayout) {
         super.setLayout(layout);
      }

   }

   public void add(Component cmp, int gridx, int gridy, int gridwidth, int gridheight, int anchor, int fill, double weightx, double weighty) {
      Insets insets = this.insetsManager.getInsets(gridx, gridy);
      GridBagConstraints constraints = new GridBagConstraints();
      constraints.gridx = gridx;
      constraints.gridy = gridy;
      constraints.gridwidth = gridwidth;
      constraints.gridheight = gridheight;
      constraints.anchor = anchor;
      constraints.fill = fill;
      constraints.weightx = weightx;
      constraints.weighty = weighty;
      constraints.insets = insets;
      this.add(cmp, constraints);
   }

   private static class DefaultInsetsManager implements InsetsManager {
      int leftInset;
      int topInset;
      public Insets positiveInsets;
      public Insets leftInsets;
      public Insets topInsets;
      public Insets topLeftInsets;

      private DefaultInsetsManager() {
         this.leftInset = 5;
         this.topInset = 5;
         this.positiveInsets = new Insets(this.topInset, this.leftInset, 0, 0);
         this.leftInsets = new Insets(this.topInset, 0, 0, 0);
         this.topInsets = new Insets(0, this.leftInset, 0, 0);
         this.topLeftInsets = new Insets(0, 0, 0, 0);
      }

      public Insets getInsets(int gridx, int gridy) {
         if (gridx > 0) {
            return gridy > 0 ? this.positiveInsets : this.topInsets;
         } else {
            return gridy > 0 ? this.leftInsets : this.topLeftInsets;
         }
      }

      // $FF: synthetic method
      DefaultInsetsManager(Object x0) {
         this();
      }
   }

   private static class ZeroInsetsManager implements InsetsManager {
      private Insets insets;

      private ZeroInsetsManager() {
         this.insets = new Insets(0, 0, 0, 0);
      }

      public Insets getInsets(int gridx, int gridy) {
         return this.insets;
      }

      // $FF: synthetic method
      ZeroInsetsManager(Object x0) {
         this();
      }
   }

   public interface InsetsManager {
      Insets getInsets(int var1, int var2);
   }
}
