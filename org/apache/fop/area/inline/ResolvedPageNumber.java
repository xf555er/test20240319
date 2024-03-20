package org.apache.fop.area.inline;

import java.io.IOException;
import java.io.ObjectInputStream;

public class ResolvedPageNumber extends TextArea {
   private static final long serialVersionUID = -1758369835371647979L;

   private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
      ois.defaultReadObject();
   }
}
