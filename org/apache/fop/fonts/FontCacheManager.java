package org.apache.fop.fonts;

import java.net.URI;
import org.apache.fop.apps.FOPException;

public interface FontCacheManager {
   void setCacheFile(URI var1);

   FontCache load();

   void save() throws FOPException;

   void delete() throws FOPException;
}
