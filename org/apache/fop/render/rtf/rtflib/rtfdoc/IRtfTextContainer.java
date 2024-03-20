package org.apache.fop.render.rtf.rtflib.rtfdoc;

import java.io.IOException;
import org.apache.fop.apps.FOPException;

public interface IRtfTextContainer {
   RtfText newText(String var1, RtfAttributes var2) throws IOException;

   RtfText newText(String var1) throws IOException;

   void newLineBreak() throws IOException;

   RtfAttributes getTextContainerAttributes() throws FOPException;
}
