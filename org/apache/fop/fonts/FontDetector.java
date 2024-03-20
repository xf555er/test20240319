package org.apache.fop.fonts;

import java.util.List;
import org.apache.fop.apps.FOPException;

public interface FontDetector {
   void detect(FontManager var1, FontAdder var2, boolean var3, FontEventListener var4, List var5) throws FOPException;
}
