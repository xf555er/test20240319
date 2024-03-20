package org.apache.batik.ext.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.io.Serializable;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.text.Document;

public class JAffineTransformChooser extends JGridBagPanel {
   public static final String LABEL_ANGLE = "JAffineTransformChooser.label.angle";
   public static final String LABEL_DEGREE = "JAffineTransformChooser.label.degree";
   public static final String LABEL_PERCENT = "JAffineTransformChooser.label.percent";
   public static final String LABEL_ROTATE = "JAffineTransformChooser.label.rotate";
   public static final String LABEL_SCALE = "JAffineTransformChooser.label.scale";
   public static final String LABEL_RX = "JAffineTransformChooser.label.rx";
   public static final String LABEL_RY = "JAffineTransformChooser.label.ry";
   public static final String LABEL_SX = "JAffineTransformChooser.label.sx";
   public static final String LABEL_SY = "JAffineTransformChooser.label.sy";
   public static final String LABEL_TRANSLATE = "JAffineTransformChooser.label.translate";
   public static final String LABEL_TX = "JAffineTransformChooser.label.tx";
   public static final String LABEL_TY = "JAffineTransformChooser.label.ty";
   public static final String CONFIG_TEXT_FIELD_WIDTH = "JAffineTransformChooser.config.text.field.width";
   public static final String CONFIG_TOP_PAD = "JAffineTransformChooser.config.top.pad";
   public static final String CONFIG_LEFT_PAD = "JAffineTransformChooser.config.left.pad";
   public static final String CONFIG_BOTTOM_PAD = "JAffineTransformChooser.config.bottom.pad";
   public static final String CONFIG_RIGHT_PAD = "JAffineTransformChooser.config.right.pad";
   protected AffineTransform txf;
   protected DoubleDocument txModel = new DoubleDocument();
   protected DoubleDocument tyModel = new DoubleDocument();
   protected DoubleDocument sxModel = new DoubleDocument();
   protected DoubleDocument syModel = new DoubleDocument();
   protected DoubleDocument rxModel = new DoubleDocument();
   protected DoubleDocument ryModel = new DoubleDocument();
   protected DoubleDocument rotateModel = new DoubleDocument();
   protected static final double RAD_TO_DEG = 57.29577951308232;
   protected static final double DEG_TO_RAD = 0.017453292519943295;

   public JAffineTransformChooser() {
      this.build();
      this.setAffineTransform(new AffineTransform());
   }

   protected void build() {
      Component txyCmp = this.buildPanel(Resources.getString("JAffineTransformChooser.label.translate"), Resources.getString("JAffineTransformChooser.label.tx"), this.txModel, Resources.getString("JAffineTransformChooser.label.ty"), this.tyModel, "", "", true);
      Component sxyCmp = this.buildPanel(Resources.getString("JAffineTransformChooser.label.scale"), Resources.getString("JAffineTransformChooser.label.sx"), this.sxModel, Resources.getString("JAffineTransformChooser.label.sy"), this.syModel, Resources.getString("JAffineTransformChooser.label.percent"), Resources.getString("JAffineTransformChooser.label.percent"), true);
      Component rCmp = this.buildRotatePanel();
      this.add(txyCmp, 0, 0, 1, 1, 10, 1, 1.0, 1.0);
      this.add(sxyCmp, 1, 0, 1, 1, 10, 1, 1.0, 1.0);
      this.add(rCmp, 0, 1, 2, 1, 10, 1, 1.0, 1.0);
   }

   protected Component buildRotatePanel() {
      JGridBagPanel panel = new JGridBagPanel();
      Component anglePanel = this.buildPanel(Resources.getString("JAffineTransformChooser.label.rotate"), Resources.getString("JAffineTransformChooser.label.angle"), this.rotateModel, (String)null, (Document)null, Resources.getString("JAffineTransformChooser.label.degree"), (String)null, false);
      Component centerPanel = this.buildPanel("", Resources.getString("JAffineTransformChooser.label.rx"), this.rxModel, Resources.getString("JAffineTransformChooser.label.ry"), this.ryModel, (String)null, (String)null, false);
      panel.add(anglePanel, 0, 0, 1, 1, 10, 1, 1.0, 1.0);
      panel.add(centerPanel, 1, 0, 1, 1, 10, 1, 1.0, 1.0);
      this.setPanelBorder(panel, Resources.getString("JAffineTransformChooser.label.rotate"));
      return panel;
   }

   protected Component buildPanel(String panelName, String tfALabel, Document tfAModel, String tfBLabel, Document tfBModel, String tfASuffix, String tfBSuffix, boolean setBorder) {
      JGridBagPanel panel = new JGridBagPanel();
      this.addToPanelAtRow(tfALabel, tfAModel, tfASuffix, panel, 0);
      if (tfBLabel != null) {
         this.addToPanelAtRow(tfBLabel, tfBModel, tfBSuffix, panel, 1);
      }

      if (setBorder) {
         this.setPanelBorder(panel, panelName);
      }

      return panel;
   }

   public void setPanelBorder(JComponent panel, String panelName) {
      Border border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), panelName);
      int topPad = Resources.getInteger("JAffineTransformChooser.config.top.pad");
      int leftPad = Resources.getInteger("JAffineTransformChooser.config.left.pad");
      int bottomPad = Resources.getInteger("JAffineTransformChooser.config.bottom.pad");
      int rightPad = Resources.getInteger("JAffineTransformChooser.config.right.pad");
      Border border = BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(topPad, leftPad, bottomPad, rightPad));
      panel.setBorder(border);
   }

   protected void addToPanelAtRow(String label, Document model, String suffix, JGridBagPanel p, int row) {
      JTextField tf = new JTextField(Resources.getInteger("JAffineTransformChooser.config.text.field.width"));
      tf.setDocument(model);
      p.add(new JLabel(label), 0, row, 1, 1, 17, 2, 0.0, 0.0);
      p.add(tf, 1, row, 1, 1, 10, 2, 1.0, 0.0);
      p.add(new JLabel(suffix), 2, row, 1, 1, 17, 2, 0.0, 0.0);
   }

   public AffineTransform getAffineTransform() {
      double sx = this.sxModel.getValue() / 100.0;
      double sy = this.syModel.getValue() / 100.0;
      double theta = this.rotateModel.getValue() * 0.017453292519943295;
      double rx = this.rxModel.getValue();
      double ry = this.ryModel.getValue();
      double tx = this.txModel.getValue();
      double ty = this.tyModel.getValue();
      double[] m = new double[6];
      double SIN_THETA = Math.sin(theta);
      double COS_THETA = Math.cos(theta);
      m[0] = sx * COS_THETA;
      m[1] = sx * SIN_THETA;
      m[2] = -sy * SIN_THETA;
      m[3] = sy * COS_THETA;
      m[4] = tx + rx - rx * COS_THETA + ry * SIN_THETA;
      m[5] = ty + ry - rx * SIN_THETA - ry * COS_THETA;
      this.txf = new AffineTransform(m);
      return this.txf;
   }

   public void setAffineTransform(AffineTransform txf) {
      if (txf == null) {
         txf = new AffineTransform();
      }

      this.txf = txf;
      double[] m = new double[6];
      txf.getMatrix(m);
      this.txModel.setValue(m[4]);
      this.tyModel.setValue(m[5]);
      double sx = Math.sqrt(m[0] * m[0] + m[1] * m[1]);
      double sy = Math.sqrt(m[2] * m[2] + m[3] * m[3]);
      this.sxModel.setValue(100.0 * sx);
      this.syModel.setValue(100.0 * sy);
      double theta = 0.0;
      if (m[0] > 0.0) {
         theta = Math.atan2(m[1], m[0]);
      }

      this.rotateModel.setValue(57.29577951308232 * theta);
      this.rxModel.setValue(0.0);
      this.ryModel.setValue(0.0);
   }

   public static AffineTransform showDialog(Component cmp, String title) {
      JAffineTransformChooser pane = new JAffineTransformChooser();
      AffineTransformTracker tracker = new AffineTransformTracker(pane);
      JDialog dialog = new Dialog(cmp, title, true, pane, tracker, (ActionListener)null);
      dialog.addWindowListener(new Closer());
      dialog.addComponentListener(new DisposeOnClose());
      dialog.setVisible(true);
      return tracker.getAffineTransform();
   }

   public static Dialog createDialog(Component cmp, String title) {
      JAffineTransformChooser pane = new JAffineTransformChooser();
      AffineTransformTracker tracker = new AffineTransformTracker(pane);
      Dialog dialog = new Dialog(cmp, title, true, pane, tracker, (ActionListener)null);
      dialog.addWindowListener(new Closer());
      dialog.addComponentListener(new DisposeOnClose());
      return dialog;
   }

   public static void main(String[] args) {
      AffineTransform t = showDialog((Component)null, "Hello");
      if (t == null) {
         System.out.println("Cancelled");
      } else {
         System.out.println("t = " + t);
      }

   }

   static class DisposeOnClose extends ComponentAdapter implements Serializable {
      public void componentHidden(ComponentEvent e) {
         Window w = (Window)e.getComponent();
         w.dispose();
      }
   }

   static class Closer extends WindowAdapter implements Serializable {
      public void windowClosing(WindowEvent e) {
         Window w = e.getWindow();
         w.setVisible(false);
      }
   }

   public static class Dialog extends JDialog {
      private JAffineTransformChooser chooserPane;
      private AffineTransformTracker tracker;
      public static final String LABEL_OK = "JAffineTransformChooser.label.ok";
      public static final String LABEL_CANCEL = "JAffineTransformChooser.label.cancel";
      public static final String LABEL_RESET = "JAffineTransformChooser.label.reset";
      public static final String ACTION_COMMAND_OK = "OK";
      public static final String ACTION_COMMAND_CANCEL = "cancel";

      public Dialog(Component c, String title, boolean modal, JAffineTransformChooser chooserPane, AffineTransformTracker okListener, ActionListener cancelListener) {
         super(JOptionPane.getFrameForComponent(c), title, modal);
         this.chooserPane = chooserPane;
         this.tracker = okListener;
         String okString = Resources.getString("JAffineTransformChooser.label.ok");
         String cancelString = Resources.getString("JAffineTransformChooser.label.cancel");
         String resetString = Resources.getString("JAffineTransformChooser.label.reset");
         Container contentPane = this.getContentPane();
         contentPane.setLayout(new BorderLayout());
         contentPane.add(chooserPane, "Center");
         JPanel buttonPane = new JPanel();
         buttonPane.setLayout(new FlowLayout(1));
         JButton okButton = new JButton(okString);
         this.getRootPane().setDefaultButton(okButton);
         okButton.setActionCommand("OK");
         if (okListener != null) {
            okButton.addActionListener(okListener);
         }

         okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Dialog.this.setVisible(false);
            }
         });
         buttonPane.add(okButton);
         JButton cancelButton = new JButton(cancelString);
         this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent evt) {
               if (evt.getKeyCode() == 27) {
                  Dialog.this.setVisible(false);
               }

            }
         });
         cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Dialog.this.setVisible(false);
            }
         });
         buttonPane.add(cancelButton);
         JButton resetButton = new JButton(resetString);
         resetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               Dialog.this.reset();
            }
         });
         buttonPane.add(resetButton);
         contentPane.add(buttonPane, "South");
         this.pack();
         this.setLocationRelativeTo(c);
      }

      public void setVisible(boolean b) {
         if (b) {
            this.tracker.reset();
         }

         super.setVisible(b);
      }

      public AffineTransform showDialog() {
         this.setVisible(true);
         return this.tracker.getAffineTransform();
      }

      public void reset() {
         this.chooserPane.setAffineTransform(new AffineTransform());
      }

      public void setTransform(AffineTransform at) {
         if (at == null) {
            at = new AffineTransform();
         }

         this.chooserPane.setAffineTransform(at);
      }
   }
}
