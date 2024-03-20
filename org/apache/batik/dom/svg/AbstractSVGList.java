package org.apache.batik.dom.svg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.batik.parser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.svg.SVGException;

public abstract class AbstractSVGList {
   protected boolean valid;
   protected List itemList;

   protected abstract String getItemSeparator();

   protected abstract SVGItem createSVGItem(Object var1);

   protected abstract void doParse(String var1, ListHandler var2) throws ParseException;

   protected abstract void checkItemType(Object var1) throws SVGException;

   protected abstract String getValueAsString();

   protected abstract void setAttributeValue(String var1);

   protected abstract DOMException createDOMException(short var1, String var2, Object[] var3);

   public int getNumberOfItems() {
      this.revalidate();
      return this.itemList != null ? this.itemList.size() : 0;
   }

   public void clear() throws DOMException {
      this.revalidate();
      if (this.itemList != null) {
         this.clear(this.itemList);
         this.resetAttribute();
      }

   }

   protected SVGItem initializeImpl(Object newItem) throws DOMException, SVGException {
      this.checkItemType(newItem);
      if (this.itemList == null) {
         this.itemList = new ArrayList(1);
      } else {
         this.clear(this.itemList);
      }

      SVGItem item = this.removeIfNeeded(newItem);
      this.itemList.add(item);
      item.setParent(this);
      this.resetAttribute();
      return item;
   }

   protected SVGItem getItemImpl(int index) throws DOMException {
      this.revalidate();
      if (index >= 0 && this.itemList != null && index < this.itemList.size()) {
         return (SVGItem)this.itemList.get(index);
      } else {
         throw this.createDOMException((short)1, "index.out.of.bounds", new Object[]{index});
      }
   }

   protected SVGItem insertItemBeforeImpl(Object newItem, int index) throws DOMException, SVGException {
      this.checkItemType(newItem);
      this.revalidate();
      if (index < 0) {
         throw this.createDOMException((short)1, "index.out.of.bounds", new Object[]{index});
      } else {
         if (index > this.itemList.size()) {
            index = this.itemList.size();
         }

         SVGItem item = this.removeIfNeeded(newItem);
         this.itemList.add(index, item);
         item.setParent(this);
         this.resetAttribute();
         return item;
      }
   }

   protected SVGItem replaceItemImpl(Object newItem, int index) throws DOMException, SVGException {
      this.checkItemType(newItem);
      this.revalidate();
      if (index >= 0 && index < this.itemList.size()) {
         SVGItem item = this.removeIfNeeded(newItem);
         this.itemList.set(index, item);
         item.setParent(this);
         this.resetAttribute();
         return item;
      } else {
         throw this.createDOMException((short)1, "index.out.of.bounds", new Object[]{index});
      }
   }

   protected SVGItem removeItemImpl(int index) throws DOMException {
      this.revalidate();
      if (index >= 0 && index < this.itemList.size()) {
         SVGItem item = (SVGItem)this.itemList.remove(index);
         item.setParent((AbstractSVGList)null);
         this.resetAttribute();
         return item;
      } else {
         throw this.createDOMException((short)1, "index.out.of.bounds", new Object[]{index});
      }
   }

   protected SVGItem appendItemImpl(Object newItem) throws DOMException, SVGException {
      this.checkItemType(newItem);
      this.revalidate();
      SVGItem item = this.removeIfNeeded(newItem);
      this.itemList.add(item);
      item.setParent(this);
      if (this.itemList.size() <= 1) {
         this.resetAttribute();
      } else {
         this.resetAttribute(item);
      }

      return item;
   }

   protected SVGItem removeIfNeeded(Object newItem) {
      SVGItem item;
      if (newItem instanceof SVGItem) {
         item = (SVGItem)newItem;
         if (item.getParent() != null) {
            item.getParent().removeItem(item);
         }
      } else {
         item = this.createSVGItem(newItem);
      }

      return item;
   }

   protected void revalidate() {
      if (!this.valid) {
         try {
            ListBuilder builder = new ListBuilder(this);
            this.doParse(this.getValueAsString(), builder);
            List parsedList = builder.getList();
            if (parsedList != null) {
               this.clear(this.itemList);
            }

            this.itemList = parsedList;
         } catch (ParseException var3) {
            this.itemList = null;
         }

         this.valid = true;
      }
   }

   protected void setValueAsString(List value) throws DOMException {
      String finalValue = null;
      Iterator it = value.iterator();
      if (it.hasNext()) {
         SVGItem item = (SVGItem)it.next();
         StringBuffer buf = new StringBuffer(value.size() * 8);
         buf.append(item.getValueAsString());

         while(it.hasNext()) {
            item = (SVGItem)it.next();
            buf.append(this.getItemSeparator());
            buf.append(item.getValueAsString());
         }

         finalValue = buf.toString();
      }

      this.setAttributeValue(finalValue);
      this.valid = true;
   }

   public void itemChanged() {
      this.resetAttribute();
   }

   protected void resetAttribute() {
      this.setValueAsString(this.itemList);
   }

   protected void resetAttribute(SVGItem item) {
      String newValue = this.getValueAsString() + this.getItemSeparator() + item.getValueAsString();
      this.setAttributeValue(newValue);
      this.valid = true;
   }

   public void invalidate() {
      this.valid = false;
   }

   protected void removeItem(SVGItem item) {
      if (this.itemList.contains(item)) {
         this.itemList.remove(item);
         item.setParent((AbstractSVGList)null);
         this.resetAttribute();
      }

   }

   protected void clear(List list) {
      if (list != null) {
         Iterator var2 = list.iterator();

         while(var2.hasNext()) {
            Object aList = var2.next();
            SVGItem item = (SVGItem)aList;
            item.setParent((AbstractSVGList)null);
         }

         list.clear();
      }
   }
}
