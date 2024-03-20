package org.apache.fop.afp.goca;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.fop.afp.Completable;
import org.apache.fop.afp.Startable;
import org.apache.fop.afp.StructuredData;
import org.apache.fop.afp.modca.AbstractNamedAFPObject;

public abstract class AbstractGraphicsDrawingOrderContainer extends AbstractNamedAFPObject implements StructuredData, Completable, Startable {
   protected List objects = new ArrayList();
   private boolean complete;
   private boolean started;

   protected AbstractGraphicsDrawingOrderContainer() {
   }

   protected AbstractGraphicsDrawingOrderContainer(String name) {
      super(name);
   }

   protected void writeStart(OutputStream os) throws IOException {
      this.setStarted(true);
   }

   protected void writeContent(OutputStream os) throws IOException {
      this.writeObjects(this.objects, os);
   }

   public void addObject(StructuredData object) {
      this.objects.add(object);
   }

   public void addAll(AbstractGraphicsDrawingOrderContainer graphicsContainer) {
      Collection objects = graphicsContainer.getObjects();
      objects.addAll(objects);
   }

   private Collection getObjects() {
      return this.objects;
   }

   public StructuredData removeLast() {
      int lastIndex = this.objects.size() - 1;
      StructuredData object = null;
      if (lastIndex > -1) {
         object = (StructuredData)this.objects.get(lastIndex);
         this.objects.remove(lastIndex);
      }

      return object;
   }

   public int getDataLength() {
      int dataLen = 0;

      Object object;
      for(Iterator var2 = this.objects.iterator(); var2.hasNext(); dataLen += ((StructuredData)object).getDataLength()) {
         object = var2.next();
      }

      return dataLen;
   }

   public void setComplete(boolean complete) {
      Iterator var2 = this.objects.iterator();

      while(var2.hasNext()) {
         Object object = var2.next();
         if (object instanceof Completable) {
            ((Completable)object).setComplete(true);
         }
      }

      this.complete = true;
   }

   public boolean isComplete() {
      return this.complete;
   }

   public boolean isStarted() {
      return this.started;
   }

   public void setStarted(boolean started) {
      this.started = started;
   }
}
