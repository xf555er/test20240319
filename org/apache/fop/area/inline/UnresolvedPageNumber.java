package org.apache.fop.area.inline;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import org.apache.fop.area.PageViewport;
import org.apache.fop.area.Resolvable;
import org.apache.fop.complexscripts.bidi.InlineRun;
import org.apache.fop.fonts.Font;

public class UnresolvedPageNumber extends TextArea implements Resolvable {
   private static final long serialVersionUID = -1758090835371647980L;
   private boolean resolved;
   private String pageIDRef;
   private String text;
   private boolean pageType;
   public static final boolean FIRST = true;
   public static final boolean LAST = false;
   private transient Font font;

   public UnresolvedPageNumber() {
      this((String)null, (Font)null, true);
   }

   public UnresolvedPageNumber(String id, Font f) {
      this(id, f, true);
   }

   public UnresolvedPageNumber(String id, Font f, boolean type) {
      this.pageIDRef = id;
      this.font = f;
      this.text = "?";
      this.pageType = type;
   }

   private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
      ois.defaultReadObject();
   }

   public String[] getIDRefs() {
      return new String[]{this.pageIDRef};
   }

   public String getText() {
      return this.text;
   }

   public void resolveIDRef(String id, List pages) {
      if (!this.resolved && this.pageIDRef.equals(id) && pages != null) {
         if (log.isDebugEnabled()) {
            log.debug("Resolving pageNumber: " + id);
         }

         this.resolved = true;
         int pageIndex = this.pageType ? 0 : pages.size() - 1;
         PageViewport page = (PageViewport)pages.get(pageIndex);
         this.removeText();
         this.text = page.getPageNumberString();
         this.addWord(this.text, 0, this.getBidiLevel());
         if (this.font != null) {
            this.handleIPDVariation(this.font.getWordWidth(this.text) - this.getIPD());
            this.font = null;
         } else {
            log.warn("Cannot update the IPD of an unresolved page number. No font information available.");
         }
      }

   }

   public boolean isResolved() {
      return this.resolved;
   }

   public boolean applyVariationFactor(double variationFactor, int lineStretch, int lineShrink) {
      return true;
   }

   public List collectInlineRuns(List runs) {
      assert runs != null;

      runs.add(new InlineRun(this, new int[]{this.getBidiLevel()}));
      return runs;
   }
}
