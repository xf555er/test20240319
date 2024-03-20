package org.apache.fop.render.ps;

import org.apache.xmlgraphics.ps.PSResource;
import org.apache.xmlgraphics.ps.dsc.ResourceTracker;

abstract class PSFontResource {
   static PSFontResource createFontResource(final PSResource fontResource) {
      return new PSFontResource() {
         String getName() {
            return fontResource.getName();
         }

         void notifyResourceUsageOnPage(ResourceTracker resourceTracker) {
            resourceTracker.notifyResourceUsageOnPage(fontResource);
         }
      };
   }

   static PSFontResource createFontResource(final PSResource fontResource, final PSResource procsetCIDInitResource, final PSResource cmapResource, final PSResource cidFontResource) {
      return new PSFontResource() {
         String getName() {
            return fontResource.getName();
         }

         void notifyResourceUsageOnPage(ResourceTracker resourceTracker) {
            resourceTracker.notifyResourceUsageOnPage(fontResource);
            resourceTracker.notifyResourceUsageOnPage(procsetCIDInitResource);
            resourceTracker.notifyResourceUsageOnPage(cmapResource);
            resourceTracker.notifyResourceUsageOnPage(cidFontResource);
         }
      };
   }

   abstract String getName();

   abstract void notifyResourceUsageOnPage(ResourceTracker var1);
}
