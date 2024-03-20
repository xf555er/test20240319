package org.apache.batik.svggen;

import java.awt.Component;
import java.awt.Rectangle;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import org.w3c.dom.Element;

public abstract class SwingSVGPrettyPrint implements SVGSyntax {
   public static void print(JComponent cmp, SVGGraphics2D svgGen) {
      if (!(cmp instanceof JComboBox) && !(cmp instanceof JScrollBar)) {
         SVGGraphics2D g = (SVGGraphics2D)svgGen.create();
         g.setColor(cmp.getForeground());
         g.setFont(cmp.getFont());
         Element topLevelGroup = g.getTopLevelGroup();
         if (cmp.getWidth() > 0 && cmp.getHeight() > 0) {
            Rectangle clipRect = g.getClipBounds();
            if (clipRect == null) {
               g.setClip(0, 0, cmp.getWidth(), cmp.getHeight());
            }

            paintComponent(cmp, g);
            paintBorder(cmp, g);
            paintChildren(cmp, g);
            Element cmpGroup = g.getTopLevelGroup();
            cmpGroup.setAttributeNS((String)null, "id", svgGen.getGeneratorContext().idGenerator.generateID(cmp.getClass().getName()));
            topLevelGroup.appendChild(cmpGroup);
            svgGen.setTopLevelGroup(topLevelGroup);
         }
      } else {
         printHack(cmp, svgGen);
      }
   }

   private static void printHack(JComponent cmp, SVGGraphics2D svgGen) {
      SVGGraphics2D g = (SVGGraphics2D)svgGen.create();
      g.setColor(cmp.getForeground());
      g.setFont(cmp.getFont());
      Element topLevelGroup = g.getTopLevelGroup();
      if (cmp.getWidth() > 0 && cmp.getHeight() > 0) {
         Rectangle clipRect = g.getClipBounds();
         if (clipRect == null) {
            g.setClip(0, 0, cmp.getWidth(), cmp.getHeight());
         }

         cmp.paint(g);
         Element cmpGroup = g.getTopLevelGroup();
         cmpGroup.setAttributeNS((String)null, "id", svgGen.getGeneratorContext().idGenerator.generateID(cmp.getClass().getName()));
         topLevelGroup.appendChild(cmpGroup);
         svgGen.setTopLevelGroup(topLevelGroup);
      }
   }

   private static void paintComponent(JComponent cmp, SVGGraphics2D svgGen) {
      ComponentUI ui = UIManager.getUI(cmp);
      if (ui != null) {
         ui.installUI(cmp);
         ui.update(svgGen, cmp);
      }

   }

   private static void paintBorder(JComponent cmp, SVGGraphics2D svgGen) {
      Border border = cmp.getBorder();
      if (border != null) {
         if (!(cmp instanceof AbstractButton) && !(cmp instanceof JPopupMenu) && !(cmp instanceof JToolBar) && !(cmp instanceof JMenuBar) && !(cmp instanceof JProgressBar)) {
            border.paintBorder(cmp, svgGen, 0, 0, cmp.getWidth(), cmp.getHeight());
         } else if (cmp instanceof AbstractButton && ((AbstractButton)cmp).isBorderPainted() || cmp instanceof JPopupMenu && ((JPopupMenu)cmp).isBorderPainted() || cmp instanceof JToolBar && ((JToolBar)cmp).isBorderPainted() || cmp instanceof JMenuBar && ((JMenuBar)cmp).isBorderPainted() || cmp instanceof JProgressBar && ((JProgressBar)cmp).isBorderPainted()) {
            border.paintBorder(cmp, svgGen, 0, 0, cmp.getWidth(), cmp.getHeight());
         }
      }

   }

   private static void paintChildren(JComponent cmp, SVGGraphics2D svgGen) {
      int i = cmp.getComponentCount() - 1;

      for(Rectangle tmpRect = new Rectangle(); i >= 0; --i) {
         Component comp = cmp.getComponent(i);
         if (comp != null && JComponent.isLightweightComponent(comp) && comp.isVisible()) {
            Rectangle cr = null;
            boolean isJComponent = comp instanceof JComponent;
            if (isJComponent) {
               cr = tmpRect;
               comp.getBounds(tmpRect);
            } else {
               cr = comp.getBounds();
            }

            boolean hitClip = svgGen.hitClip(cr.x, cr.y, cr.width, cr.height);
            if (hitClip) {
               SVGGraphics2D cg = (SVGGraphics2D)svgGen.create(cr.x, cr.y, cr.width, cr.height);
               cg.setColor(comp.getForeground());
               cg.setFont(comp.getFont());
               if (comp instanceof JComponent) {
                  print((JComponent)comp, cg);
               } else {
                  comp.paint(cg);
               }
            }
         }
      }

   }
}
