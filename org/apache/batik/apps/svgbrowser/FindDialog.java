package org.apache.batik.apps.svgbrowser;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.apache.batik.bridge.Mark;
import org.apache.batik.bridge.TextNode;
import org.apache.batik.gvt.GVTTreeWalker;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.gui.ExtendedGridBagConstraints;
import org.apache.batik.util.gui.resource.ActionMap;
import org.apache.batik.util.gui.resource.ButtonFactory;
import org.apache.batik.util.gui.resource.MissingListenerException;
import org.apache.batik.util.resources.ResourceManager;

public class FindDialog extends JDialog implements ActionMap {
   protected static final String RESOURCES = "org.apache.batik.apps.svgbrowser.resources.FindDialog";
   public static final String FIND_ACTION = "FindButtonAction";
   public static final String CLEAR_ACTION = "ClearButtonAction";
   public static final String CLOSE_ACTION = "CloseButtonAction";
   protected static ResourceBundle bundle = ResourceBundle.getBundle("org.apache.batik.apps.svgbrowser.resources.FindDialog", Locale.getDefault());
   protected static ResourceManager resources;
   protected ButtonFactory buttonFactory;
   protected GraphicsNode gvtRoot;
   protected GVTTreeWalker walker;
   protected int currentIndex;
   protected JTextField search;
   protected JButton findButton;
   protected JButton clearButton;
   protected JButton closeButton;
   protected JCheckBox caseSensitive;
   protected JSVGCanvas svgCanvas;
   protected JRadioButton highlightButton;
   protected JRadioButton highlightCenterButton;
   protected JRadioButton highlightCenterZoomButton;
   protected Map listeners;

   public FindDialog(JSVGCanvas svgCanvas) {
      this((Frame)null, svgCanvas);
   }

   public FindDialog(Frame owner, JSVGCanvas svgCanvas) {
      super(owner, resources.getString("Dialog.title"));
      this.listeners = new HashMap(10);
      this.svgCanvas = svgCanvas;
      this.buttonFactory = new ButtonFactory(bundle, this);
      this.listeners.put("FindButtonAction", new FindButtonAction());
      this.listeners.put("ClearButtonAction", new ClearButtonAction());
      this.listeners.put("CloseButtonAction", new CloseButtonAction());
      JPanel p = new JPanel(new BorderLayout());
      p.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
      p.add(this.createFindPanel(), "Center");
      p.add(this.createShowResultPanel(), "South");
      this.getContentPane().add(p, "Center");
      this.getContentPane().add(this.createButtonsPanel(), "South");
   }

   protected JPanel createFindPanel() {
      JPanel panel = new JPanel(new GridBagLayout());
      panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), resources.getString("Panel.title")));
      ExtendedGridBagConstraints gbc = new ExtendedGridBagConstraints();
      gbc.insets = new Insets(2, 2, 2, 2);
      gbc.anchor = 13;
      gbc.fill = 0;
      gbc.setWeight(0.0, 0.0);
      gbc.setGridBounds(0, 0, 1, 1);
      panel.add(new JLabel(resources.getString("FindLabel.text")), gbc);
      gbc.fill = 2;
      gbc.setWeight(1.0, 0.0);
      gbc.setGridBounds(1, 0, 2, 1);
      panel.add(this.search = new JTextField(20), gbc);
      gbc.fill = 0;
      gbc.anchor = 17;
      gbc.setWeight(0.0, 0.0);
      gbc.setGridBounds(1, 1, 1, 1);
      this.caseSensitive = this.buttonFactory.createJCheckBox("CaseSensitiveCheckBox");
      panel.add(this.caseSensitive, gbc);
      return panel;
   }

   protected JPanel createShowResultPanel() {
      JPanel panel = new JPanel(new GridBagLayout());
      panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), resources.getString("ShowResultPanel.title")));
      ExtendedGridBagConstraints gbc = new ExtendedGridBagConstraints();
      gbc.insets = new Insets(2, 2, 2, 2);
      gbc.anchor = 17;
      gbc.fill = 0;
      gbc.setWeight(0.0, 0.0);
      ButtonGroup grp = new ButtonGroup();
      this.highlightButton = this.buttonFactory.createJRadioButton("Highlight");
      this.highlightButton.setSelected(true);
      grp.add(this.highlightButton);
      gbc.setGridBounds(0, 0, 1, 1);
      panel.add(this.highlightButton, gbc);
      this.highlightCenterButton = this.buttonFactory.createJRadioButton("HighlightAndCenter");
      grp.add(this.highlightCenterButton);
      gbc.setGridBounds(0, 1, 1, 1);
      panel.add(this.highlightCenterButton, gbc);
      this.highlightCenterZoomButton = this.buttonFactory.createJRadioButton("HighlightCenterAndZoom");
      grp.add(this.highlightCenterZoomButton);
      gbc.setGridBounds(0, 2, 1, 1);
      panel.add(this.highlightCenterZoomButton, gbc);
      return panel;
   }

   protected JPanel createButtonsPanel() {
      JPanel panel = new JPanel(new FlowLayout(2));
      panel.add(this.findButton = this.buttonFactory.createJButton("FindButton"));
      panel.add(this.clearButton = this.buttonFactory.createJButton("ClearButton"));
      panel.add(this.closeButton = this.buttonFactory.createJButton("CloseButton"));
      return panel;
   }

   public void setGraphicsNode(GraphicsNode gvtRoot) {
      this.gvtRoot = gvtRoot;
      if (gvtRoot != null) {
         this.walker = new GVTTreeWalker(gvtRoot);
      } else {
         this.walker = null;
      }

   }

   protected GraphicsNode getNext(String text) {
      if (this.walker == null && this.gvtRoot != null) {
         this.walker = new GVTTreeWalker(this.gvtRoot);
      }

      GraphicsNode gn = this.walker.getCurrentGraphicsNode();
      int index = this.match(gn, text, this.currentIndex + text.length());
      if (index >= 0) {
         this.currentIndex = index;
      } else {
         this.currentIndex = 0;

         for(gn = this.walker.nextGraphicsNode(); gn != null && (this.currentIndex = this.match(gn, text, this.currentIndex)) < 0; gn = this.walker.nextGraphicsNode()) {
            this.currentIndex = 0;
         }
      }

      return gn;
   }

   protected int match(GraphicsNode node, String text, int index) {
      if (node instanceof TextNode && node.isVisible() && text != null && text.length() != 0) {
         String s = ((TextNode)node).getText();
         if (!this.caseSensitive.isSelected()) {
            s = s.toLowerCase();
            text = text.toLowerCase();
         }

         return s.indexOf(text, index);
      } else {
         return -1;
      }
   }

   protected void showSelectedGraphicsNode() {
      GraphicsNode gn = this.walker.getCurrentGraphicsNode();
      if (gn instanceof TextNode) {
         TextNode textNode = (TextNode)gn;
         String text = textNode.getText();
         String pattern = this.search.getText();
         if (!this.caseSensitive.isSelected()) {
            text = text.toLowerCase();
            pattern = pattern.toLowerCase();
         }

         int end = text.indexOf(pattern, this.currentIndex);
         AttributedCharacterIterator aci = textNode.getAttributedCharacterIterator();
         aci.first();

         for(int i = 0; i < end; ++i) {
            aci.next();
         }

         Mark startMark = textNode.getMarkerForChar(aci.getIndex(), true);

         for(int i = 0; i < pattern.length() - 1; ++i) {
            aci.next();
         }

         Mark endMark = textNode.getMarkerForChar(aci.getIndex(), false);
         this.svgCanvas.select(startMark, endMark);
         if (!this.highlightButton.isSelected()) {
            Shape s = textNode.getHighlightShape();
            AffineTransform at;
            if (this.highlightCenterZoomButton.isSelected()) {
               at = this.svgCanvas.getInitialTransform();
            } else {
               at = this.svgCanvas.getRenderingTransform();
            }

            Rectangle2D gnb = at.createTransformedShape(s).getBounds();
            Dimension canvasSize = this.svgCanvas.getSize();
            AffineTransform Tx = AffineTransform.getTranslateInstance(-gnb.getX() - gnb.getWidth() / 2.0, -gnb.getY() - gnb.getHeight() / 2.0);
            if (this.highlightCenterZoomButton.isSelected()) {
               double sx = (double)canvasSize.width / gnb.getWidth();
               double sy = (double)canvasSize.height / gnb.getHeight();
               double scale = Math.min(sx, sy) / 8.0;
               if (scale > 1.0) {
                  Tx.preConcatenate(AffineTransform.getScaleInstance(scale, scale));
               }
            }

            Tx.preConcatenate(AffineTransform.getTranslateInstance((double)(canvasSize.width / 2), (double)(canvasSize.height / 2)));
            AffineTransform newRT = new AffineTransform(at);
            newRT.preConcatenate(Tx);
            this.svgCanvas.setRenderingTransform(newRT);
         }
      }
   }

   public Action getAction(String key) throws MissingListenerException {
      return (Action)this.listeners.get(key);
   }

   static {
      resources = new ResourceManager(bundle);
   }

   protected class CloseButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         FindDialog.this.dispose();
      }
   }

   protected class ClearButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         FindDialog.this.search.setText((String)null);
         FindDialog.this.walker = null;
      }
   }

   protected class FindButtonAction extends AbstractAction {
      public void actionPerformed(ActionEvent e) {
         String text = FindDialog.this.search.getText();
         if (text != null && text.length() != 0) {
            GraphicsNode gn = FindDialog.this.getNext(text);
            if (gn != null) {
               FindDialog.this.showSelectedGraphicsNode();
            } else {
               FindDialog.this.walker = null;
               JOptionPane.showMessageDialog(FindDialog.this, FindDialog.resources.getString("End.text"), FindDialog.resources.getString("End.title"), 1);
            }

         }
      }
   }
}
