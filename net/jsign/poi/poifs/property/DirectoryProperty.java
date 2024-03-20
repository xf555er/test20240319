package net.jsign.poi.poifs.property;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DirectoryProperty extends Property implements Iterable, Parent {
   private List _children = new ArrayList();
   private Set _children_names = new HashSet();

   public DirectoryProperty(String name) {
      this.setName(name);
      this.setSize(0);
      this.setPropertyType((byte)1);
      this.setStartBlock(0);
      this.setNodeColor((byte)1);
   }

   protected DirectoryProperty(int index, byte[] array, int offset) {
      super(index, array, offset);
   }

   public boolean isDirectory() {
      return true;
   }

   protected void preWrite() {
      if (this._children.size() > 0) {
         Property[] children = (Property[])this._children.toArray(new Property[0]);
         Arrays.sort(children, new PropertyComparator());
         int midpoint = children.length / 2;
         this.setChildProperty(children[midpoint].getIndex());
         children[0].setPreviousChild((Child)null);
         children[0].setNextChild((Child)null);

         int j;
         for(j = 1; j < midpoint; ++j) {
            children[j].setPreviousChild(children[j - 1]);
            children[j].setNextChild((Child)null);
         }

         if (midpoint != 0) {
            children[midpoint].setPreviousChild(children[midpoint - 1]);
         }

         if (midpoint != children.length - 1) {
            children[midpoint].setNextChild(children[midpoint + 1]);

            for(j = midpoint + 1; j < children.length - 1; ++j) {
               children[j].setPreviousChild((Child)null);
               children[j].setNextChild(children[j + 1]);
            }

            children[children.length - 1].setPreviousChild((Child)null);
            children[children.length - 1].setNextChild((Child)null);
         } else {
            children[midpoint].setNextChild((Child)null);
         }
      }

   }

   public Iterator getChildren() {
      return this._children.iterator();
   }

   public Iterator iterator() {
      return this.getChildren();
   }

   public void addChild(Property property) throws IOException {
      String name = property.getName();
      if (this._children_names.contains(name)) {
         throw new IOException("Duplicate name \"" + name + "\"");
      } else {
         this._children_names.add(name);
         this._children.add(property);
      }
   }

   public static class PropertyComparator implements Serializable, Comparator {
      public int compare(Property o1, Property o2) {
         String VBA_PROJECT = "_VBA_PROJECT";
         String name1 = o1.getName();
         String name2 = o2.getName();
         int result = name1.length() - name2.length();
         if (result == 0) {
            if (name1.compareTo(VBA_PROJECT) == 0) {
               result = 1;
            } else if (name2.compareTo(VBA_PROJECT) == 0) {
               result = -1;
            } else if (name1.startsWith("__") && name2.startsWith("__")) {
               result = name1.compareToIgnoreCase(name2);
            } else if (name1.startsWith("__")) {
               result = 1;
            } else if (name2.startsWith("__")) {
               result = -1;
            } else {
               result = name1.compareToIgnoreCase(name2);
            }
         }

         return result;
      }
   }
}
