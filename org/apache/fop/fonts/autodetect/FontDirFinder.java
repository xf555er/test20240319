package org.apache.fop.fonts.autodetect;

import java.io.IOException;
import java.util.List;

public interface FontDirFinder {
   List find() throws IOException;
}
