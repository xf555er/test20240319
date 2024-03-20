package org.apache.xalan.xsltc.dom;

import org.apache.xml.dtm.DTMAxisIterator;

public final class FilteredStepIterator extends StepIterator {
   private Filter _filter;

   public FilteredStepIterator(DTMAxisIterator source, DTMAxisIterator iterator, Filter filter) {
      super(source, iterator);
      this._filter = filter;
   }

   public int next() {
      while(true) {
         int node;
         if ((node = super.next()) != -1) {
            if (!this._filter.test(node)) {
               continue;
            }

            return this.returnNode(node);
         }

         return node;
      }
   }
}
