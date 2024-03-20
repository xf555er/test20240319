package org.apache.batik.dom.svg;

import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGRect;

public class SVGOMRect implements SVGRect {
   protected float x;
   protected float y;
   protected float w;
   protected float h;

   public SVGOMRect() {
   }

   public SVGOMRect(float x, float y, float w, float h) {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
   }

   public float getX() {
      return this.x;
   }

   public void setX(float x) throws DOMException {
      this.x = x;
   }

   public float getY() {
      return this.y;
   }

   public void setY(float y) throws DOMException {
      this.y = y;
   }

   public float getWidth() {
      return this.w;
   }

   public void setWidth(float width) throws DOMException {
      this.w = width;
   }

   public float getHeight() {
      return this.h;
   }

   public void setHeight(float height) throws DOMException {
      this.h = height;
   }
}
