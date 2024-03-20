package org.apache.fop.fo.properties;

import java.util.List;
import java.util.Vector;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.util.CompareUtil;

public class ListProperty extends Property {
   protected final List list;

   protected ListProperty() {
      this.list = new Vector();
   }

   public ListProperty(Property prop) {
      this();
      this.addProperty(prop);
   }

   public void addProperty(Property prop) {
      this.list.add(prop);
   }

   public List getList() {
      return this.list;
   }

   public Object getObject() {
      return this.list;
   }

   public int hashCode() {
      return this.list.hashCode();
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof ListProperty)) {
         return false;
      } else {
         ListProperty other = (ListProperty)obj;
         return CompareUtil.equal(this.list, other.list);
      }
   }

   public static class Maker extends PropertyMaker {
      public Maker(int propId) {
         super(propId);
      }

      public Property convertProperty(Property p, PropertyList propertyList, FObj fo) throws PropertyException {
         return (Property)(p instanceof ListProperty ? p : new ListProperty(p));
      }
   }
}
