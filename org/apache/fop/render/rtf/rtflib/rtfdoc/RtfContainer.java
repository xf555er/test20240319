package org.apache.fop.render.rtf.rtflib.rtfdoc;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.fop.render.rtf.rtflib.exceptions.RtfStructureException;

public class RtfContainer extends RtfElement {
   private LinkedList children;
   private RtfOptions options;
   private RtfElement lastChild;

   RtfContainer(RtfContainer parent, Writer w) throws IOException {
      this(parent, w, (RtfAttributes)null);
   }

   RtfContainer(RtfContainer parent, Writer w, RtfAttributes attr) throws IOException {
      super(parent, w, attr);
      this.options = new RtfOptions();
      this.children = new LinkedList();
   }

   public void setOptions(RtfOptions opt) {
      this.options = opt;
   }

   protected void addChild(RtfElement e) throws RtfStructureException {
      if (this.isClosed()) {
         StringBuffer sb = new StringBuffer();
         sb.append("addChild: container already closed (parent=");
         sb.append(this.getClass().getName());
         sb.append(" child=");
         sb.append(e.getClass().getName());
         sb.append(")");
         String msg = sb.toString();
         RtfFile var4 = this.getRtfFile();
      }

      this.children.add(e);
      this.lastChild = e;
   }

   public List getChildren() {
      return (List)this.children.clone();
   }

   public int getChildCount() {
      return this.children.size();
   }

   private int findChildren(RtfElement aChild, int iStart) {
      Iterator var3 = this.getChildren().iterator();

      while(var3.hasNext()) {
         Object o = var3.next();
         RtfElement e = (RtfElement)o;
         if (aChild == e) {
            return iStart;
         }

         if (e instanceof RtfContainer) {
            int iFound = ((RtfContainer)e).findChildren(aChild, iStart + 1);
            if (iFound != -1) {
               return iFound;
            }
         }
      }

      return -1;
   }

   public int findChildren(RtfElement aChild) {
      return this.findChildren(aChild, 0);
   }

   public boolean setChildren(List list) {
      if (list instanceof LinkedList) {
         this.children = (LinkedList)list;
         return true;
      } else {
         return false;
      }
   }

   protected void writeRtfContent() throws IOException {
      Iterator var1 = this.children.iterator();

      while(var1.hasNext()) {
         Object aChildren = var1.next();
         RtfElement e = (RtfElement)aChildren;
         e.writeRtf();
      }

   }

   RtfOptions getOptions() {
      return this.options;
   }

   boolean containsText() {
      boolean result = false;
      Iterator var2 = this.children.iterator();

      while(var2.hasNext()) {
         Object aChildren = var2.next();
         RtfElement e = (RtfElement)aChildren;
         if (e instanceof RtfText) {
            result = !e.isEmpty();
         } else if (e instanceof RtfContainer && ((RtfContainer)e).containsText()) {
            result = true;
         }

         if (result) {
            break;
         }
      }

      return result;
   }

   void dump(Writer w, int indent) throws IOException {
      super.dump(w, indent);
      Iterator var3 = this.children.iterator();

      while(var3.hasNext()) {
         Object aChildren = var3.next();
         RtfElement e = (RtfElement)aChildren;
         e.dump(w, indent + 1);
      }

   }

   public String toString() {
      return super.toString() + " (" + this.getChildCount() + " children)";
   }

   protected boolean okToWriteRtf() {
      boolean result = super.okToWriteRtf() && !this.isEmpty();
      if (result && !this.options.renderContainer(this)) {
         result = false;
      }

      return result;
   }

   public boolean isEmpty() {
      boolean result = true;
      Iterator var2 = this.children.iterator();

      while(var2.hasNext()) {
         Object aChildren = var2.next();
         RtfElement e = (RtfElement)aChildren;
         if (!e.isEmpty()) {
            result = false;
            break;
         }
      }

      return result;
   }
}
