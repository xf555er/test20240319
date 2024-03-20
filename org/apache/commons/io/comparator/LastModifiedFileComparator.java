package org.apache.commons.io.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import org.apache.commons.io.FileUtils;

public class LastModifiedFileComparator extends AbstractFileComparator implements Serializable {
   private static final long serialVersionUID = 7372168004395734046L;
   public static final Comparator LASTMODIFIED_COMPARATOR = new LastModifiedFileComparator();
   public static final Comparator LASTMODIFIED_REVERSE;

   public int compare(File file1, File file2) {
      long result = FileUtils.lastModifiedUnchecked(file1) - FileUtils.lastModifiedUnchecked(file2);
      if (result < 0L) {
         return -1;
      } else {
         return result > 0L ? 1 : 0;
      }
   }

   static {
      LASTMODIFIED_REVERSE = new ReverseFileComparator(LASTMODIFIED_COMPARATOR);
   }
}
