package org.apache.fop.render.rtf.rtflib.tools;

import java.util.Stack;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.fo.FObj;
import org.apache.fop.render.rtf.RTFHandler;
import org.apache.fop.render.rtf.RTFPlaceHolderHelper;
import org.apache.fop.render.rtf.rtflib.exceptions.RtfException;
import org.apache.fop.render.rtf.rtflib.rtfdoc.IRtfOptions;
import org.apache.fop.render.rtf.rtflib.rtfdoc.RtfContainer;

public class BuilderContext {
   protected static final Log LOG = LogFactory.getLog(BuilderContext.class.getName());
   private final Stack containers = new Stack();
   private final Stack tableContexts = new Stack();
   private final Stack builders = new Stack();
   private IRtfOptions options;

   public BuilderContext(IRtfOptions rtfOptions) {
      this.options = rtfOptions;
   }

   private Object getObjectFromStack(Stack s, Class desiredClass) {
      Object result = null;
      Stack copy = (Stack)s.clone();

      while(!copy.isEmpty()) {
         Object o = copy.pop();
         if (desiredClass.isAssignableFrom(o.getClass())) {
            result = o;
            break;
         }
      }

      return result;
   }

   public RtfContainer getContainer(Class containerClass, boolean required, Object forWhichBuilder) throws RtfException {
      RtfContainer result = (RtfContainer)this.getObjectFromStack(this.containers, containerClass);
      if (result == null && required) {
         RTFPlaceHolderHelper placeHolderHelper = new RTFPlaceHolderHelper(this);
         placeHolderHelper.createRTFPlaceholder(containerClass);
         result = this.getContainer(containerClass, required, forWhichBuilder);
         if (result == null) {
            throw new RtfException("No RtfContainer of class '" + containerClass.getName() + "' available for '" + forWhichBuilder.getClass().getName() + "' builder");
         }
      }

      return result;
   }

   public void pushContainer(RtfContainer c) {
      this.containers.push(c);
   }

   public void pushPart(FObj part) {
      this.containers.push(part);
   }

   public void replaceContainer(RtfContainer oldC, RtfContainer newC) throws Exception {
      int index = this.containers.indexOf(oldC);
      if (index < 0) {
         throw new Exception("container to replace not found:" + oldC);
      } else {
         this.containers.setElementAt(newC, index);
      }
   }

   public void popContainer(Class containerClass, RTFHandler handler) {
      this.handlePop(containerClass, handler);
   }

   public void popPart(Class part, RTFHandler handler) {
      this.handlePop(part, handler);
   }

   private void handlePop(Class aClass, RTFHandler handler) {
      Object object = this.containers.pop();
      if (object.getClass() != aClass) {
         this.pushAndClose(aClass, object, handler);
      }

   }

   private void pushAndClose(Class aClass, Object object, RTFHandler handler) {
      this.containers.push(object);
      if (handler.endContainer(object.getClass())) {
         this.popContainer(aClass, handler);
      } else {
         LOG.warn("Unhandled RTF structure tag mismatch detected between " + aClass.getSimpleName() + " and " + object.getClass().getSimpleName());
      }

   }

   public TableContext getTableContext() {
      return (TableContext)this.tableContexts.peek();
   }

   public void pushTableContext(TableContext tc) {
      this.tableContexts.push(tc);
   }

   public void popTableContext() {
      this.tableContexts.pop();
   }
}
