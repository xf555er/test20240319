package org.apache.batik.gvt.font;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Vector;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.text.TextPaintInfo;

public class Glyph {
   private String unicode;
   private Vector names;
   private String orientation;
   private String arabicForm;
   private String lang;
   private Point2D horizOrigin;
   private Point2D vertOrigin;
   private float horizAdvX;
   private float vertAdvY;
   private int glyphCode;
   private AffineTransform transform;
   private Point2D.Float position;
   private GVTGlyphMetrics metrics;
   private Shape outline;
   private Rectangle2D bounds;
   private TextPaintInfo tpi;
   private TextPaintInfo cacheTPI;
   private Shape dShape;
   private GraphicsNode glyphChildrenNode;

   public Glyph(String unicode, List names, String orientation, String arabicForm, String lang, Point2D horizOrigin, Point2D vertOrigin, float horizAdvX, float vertAdvY, int glyphCode, TextPaintInfo tpi, Shape dShape, GraphicsNode glyphChildrenNode) {
      if (unicode == null) {
         throw new IllegalArgumentException();
      } else if (horizOrigin == null) {
         throw new IllegalArgumentException();
      } else if (vertOrigin == null) {
         throw new IllegalArgumentException();
      } else {
         this.unicode = unicode;
         this.names = new Vector(names);
         this.orientation = orientation;
         this.arabicForm = arabicForm;
         this.lang = lang;
         this.horizOrigin = horizOrigin;
         this.vertOrigin = vertOrigin;
         this.horizAdvX = horizAdvX;
         this.vertAdvY = vertAdvY;
         this.glyphCode = glyphCode;
         this.position = new Point2D.Float(0.0F, 0.0F);
         this.outline = null;
         this.bounds = null;
         this.tpi = tpi;
         this.dShape = dShape;
         this.glyphChildrenNode = glyphChildrenNode;
      }
   }

   public String getUnicode() {
      return this.unicode;
   }

   public Vector getNames() {
      return this.names;
   }

   public String getOrientation() {
      return this.orientation;
   }

   public String getArabicForm() {
      return this.arabicForm;
   }

   public String getLang() {
      return this.lang;
   }

   public Point2D getHorizOrigin() {
      return this.horizOrigin;
   }

   public Point2D getVertOrigin() {
      return this.vertOrigin;
   }

   public float getHorizAdvX() {
      return this.horizAdvX;
   }

   public float getVertAdvY() {
      return this.vertAdvY;
   }

   public int getGlyphCode() {
      return this.glyphCode;
   }

   public AffineTransform getTransform() {
      return this.transform;
   }

   public void setTransform(AffineTransform transform) {
      this.transform = transform;
      this.outline = null;
      this.bounds = null;
   }

   public Point2D getPosition() {
      return this.position;
   }

   public void setPosition(Point2D position) {
      this.position.x = (float)position.getX();
      this.position.y = (float)position.getY();
      this.outline = null;
      this.bounds = null;
   }

   public GVTGlyphMetrics getGlyphMetrics() {
      if (this.metrics == null) {
         Rectangle2D gb = this.getGeometryBounds();
         this.metrics = new GVTGlyphMetrics(this.getHorizAdvX(), this.getVertAdvY(), new Rectangle2D.Double(gb.getX() - this.position.getX(), gb.getY() - this.position.getY(), gb.getWidth(), gb.getHeight()), (byte)3);
      }

      return this.metrics;
   }

   public GVTGlyphMetrics getGlyphMetrics(float hkern, float vkern) {
      return new GVTGlyphMetrics(this.getHorizAdvX() - hkern, this.getVertAdvY() - vkern, this.getGeometryBounds(), (byte)3);
   }

   public Rectangle2D getGeometryBounds() {
      return this.getOutline().getBounds2D();
   }

   public Rectangle2D getBounds2D() {
      if (this.bounds != null && TextPaintInfo.equivilent(this.tpi, this.cacheTPI)) {
         return this.bounds;
      } else {
         AffineTransform tr = AffineTransform.getTranslateInstance(this.position.getX(), this.position.getY());
         if (this.transform != null) {
            tr.concatenate(this.transform);
         }

         Rectangle2D bounds = null;
         if (this.dShape != null && this.tpi != null) {
            if (this.tpi.fillPaint != null) {
               bounds = tr.createTransformedShape(this.dShape).getBounds2D();
            }

            if (this.tpi.strokeStroke != null && this.tpi.strokePaint != null) {
               Shape s = this.tpi.strokeStroke.createStrokedShape(this.dShape);
               Rectangle2D r = tr.createTransformedShape(s).getBounds2D();
               if (bounds == null) {
                  bounds = r;
               } else {
                  ((Rectangle2D)bounds).add(r);
               }
            }
         }

         if (this.glyphChildrenNode != null) {
            Rectangle2D r = this.glyphChildrenNode.getTransformedBounds(tr);
            if (bounds == null) {
               bounds = r;
            } else {
               ((Rectangle2D)bounds).add(r);
            }
         }

         if (bounds == null) {
            bounds = new Rectangle2D.Double(this.position.getX(), this.position.getY(), 0.0, 0.0);
         }

         this.cacheTPI = new TextPaintInfo(this.tpi);
         return (Rectangle2D)bounds;
      }
   }

   public Shape getOutline() {
      if (this.outline == null) {
         AffineTransform tr = AffineTransform.getTranslateInstance(this.position.getX(), this.position.getY());
         if (this.transform != null) {
            tr.concatenate(this.transform);
         }

         Shape glyphChildrenOutline = null;
         if (this.glyphChildrenNode != null) {
            glyphChildrenOutline = this.glyphChildrenNode.getOutline();
         }

         GeneralPath glyphOutline = null;
         if (this.dShape != null && glyphChildrenOutline != null) {
            glyphOutline = new GeneralPath(this.dShape);
            glyphOutline.append(glyphChildrenOutline, false);
         } else if (this.dShape != null && glyphChildrenOutline == null) {
            glyphOutline = new GeneralPath(this.dShape);
         } else if (this.dShape == null && glyphChildrenOutline != null) {
            glyphOutline = new GeneralPath(glyphChildrenOutline);
         } else {
            glyphOutline = new GeneralPath();
         }

         this.outline = tr.createTransformedShape(glyphOutline);
      }

      return this.outline;
   }

   public void draw(Graphics2D graphics2D) {
      AffineTransform tr = AffineTransform.getTranslateInstance(this.position.getX(), this.position.getY());
      if (this.transform != null) {
         tr.concatenate(this.transform);
      }

      if (this.dShape != null && this.tpi != null) {
         Shape tShape = tr.createTransformedShape(this.dShape);
         if (this.tpi.fillPaint != null) {
            graphics2D.setPaint(this.tpi.fillPaint);
            graphics2D.fill(tShape);
         }

         if (this.tpi.strokeStroke != null && this.tpi.strokePaint != null) {
            graphics2D.setStroke(this.tpi.strokeStroke);
            graphics2D.setPaint(this.tpi.strokePaint);
            graphics2D.draw(tShape);
         }
      }

      if (this.glyphChildrenNode != null) {
         this.glyphChildrenNode.setTransform(tr);
         this.glyphChildrenNode.paint(graphics2D);
      }

   }
}
