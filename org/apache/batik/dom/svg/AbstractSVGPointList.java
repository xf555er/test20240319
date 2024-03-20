package org.apache.batik.dom.svg;

import org.apache.batik.parser.ParseException;
import org.apache.batik.parser.PointsHandler;
import org.apache.batik.parser.PointsParser;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGPoint;
import org.w3c.dom.svg.SVGPointList;

public abstract class AbstractSVGPointList extends AbstractSVGList implements SVGPointList {
   public static final String SVG_POINT_LIST_SEPARATOR = " ";

   protected String getItemSeparator() {
      return " ";
   }

   protected abstract SVGException createSVGException(short var1, String var2, Object[] var3);

   public SVGPoint initialize(SVGPoint newItem) throws DOMException, SVGException {
      return (SVGPoint)this.initializeImpl(newItem);
   }

   public SVGPoint getItem(int index) throws DOMException {
      return (SVGPoint)this.getItemImpl(index);
   }

   public SVGPoint insertItemBefore(SVGPoint newItem, int index) throws DOMException, SVGException {
      return (SVGPoint)this.insertItemBeforeImpl(newItem, index);
   }

   public SVGPoint replaceItem(SVGPoint newItem, int index) throws DOMException, SVGException {
      return (SVGPoint)this.replaceItemImpl(newItem, index);
   }

   public SVGPoint removeItem(int index) throws DOMException {
      return (SVGPoint)this.removeItemImpl(index);
   }

   public SVGPoint appendItem(SVGPoint newItem) throws DOMException, SVGException {
      return (SVGPoint)this.appendItemImpl(newItem);
   }

   protected SVGItem createSVGItem(Object newItem) {
      SVGPoint point = (SVGPoint)newItem;
      return new SVGPointItem(point.getX(), point.getY());
   }

   protected void doParse(String value, ListHandler handler) throws ParseException {
      PointsParser pointsParser = new PointsParser();
      PointsListBuilder builder = new PointsListBuilder(handler);
      pointsParser.setPointsHandler(builder);
      pointsParser.parse(value);
   }

   protected void checkItemType(Object newItem) throws SVGException {
      if (!(newItem instanceof SVGPoint)) {
         this.createSVGException((short)0, "expected.point", (Object[])null);
      }

   }

   protected static class PointsListBuilder implements PointsHandler {
      protected ListHandler listHandler;

      public PointsListBuilder(ListHandler listHandler) {
         this.listHandler = listHandler;
      }

      public void startPoints() throws ParseException {
         this.listHandler.startList();
      }

      public void point(float x, float y) throws ParseException {
         this.listHandler.item(new SVGPointItem(x, y));
      }

      public void endPoints() throws ParseException {
         this.listHandler.endList();
      }
   }
}
