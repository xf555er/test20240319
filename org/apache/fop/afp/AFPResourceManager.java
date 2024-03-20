package org.apache.fop.afp;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.afp.fonts.AFPFont;
import org.apache.fop.afp.fonts.CharacterSet;
import org.apache.fop.afp.modca.AbstractAFPObject;
import org.apache.fop.afp.modca.AbstractNamedAFPObject;
import org.apache.fop.afp.modca.AbstractPageObject;
import org.apache.fop.afp.modca.ActiveEnvironmentGroup;
import org.apache.fop.afp.modca.IncludeObject;
import org.apache.fop.afp.modca.IncludedResourceObject;
import org.apache.fop.afp.modca.ObjectContainer;
import org.apache.fop.afp.modca.PageSegment;
import org.apache.fop.afp.modca.Registry;
import org.apache.fop.afp.modca.ResourceGroup;
import org.apache.fop.afp.modca.ResourceObject;
import org.apache.fop.afp.modca.triplets.EncodingTriplet;
import org.apache.fop.afp.util.AFPResourceAccessor;
import org.apache.fop.afp.util.AFPResourceUtil;
import org.apache.fop.apps.io.InternalResourceResolver;
import org.apache.fop.fonts.FontType;
import org.apache.fop.render.afp.AFPFontConfig;

public class AFPResourceManager {
   private static Log log = LogFactory.getLog(AFPResourceManager.class);
   private DataStream dataStream;
   private final Factory factory = new Factory();
   private final AFPStreamer streamer;
   private final AFPDataObjectFactory dataObjectFactory;
   private int instreamObjectCount;
   private final Map includeObjectCache = new HashMap();
   private AFPResourceLevelDefaults resourceLevelDefaults = new AFPResourceLevelDefaults();
   protected boolean includeCached = true;

   public AFPResourceManager(InternalResourceResolver resourceResolver) {
      this.streamer = new AFPStreamer(this.factory, resourceResolver);
      this.dataObjectFactory = new AFPDataObjectFactory(this.factory);
   }

   public DataStream createDataStream(AFPPaintingState paintingState, OutputStream outputStream) throws IOException {
      this.dataStream = this.streamer.createDataStream(paintingState);
      this.streamer.setOutputStream(outputStream);
      return this.dataStream;
   }

   public DataStream getDataStream() {
      return this.dataStream;
   }

   public void writeToStream() throws IOException {
      this.streamer.close();
   }

   public void setDefaultResourceGroupUri(URI uri) {
      this.streamer.setDefaultResourceGroupUri(uri);
   }

   public boolean tryIncludeObject(AFPDataObjectInfo dataObjectInfo) throws IOException {
      AFPResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();
      this.updateResourceInfoUri(resourceInfo);
      return this.includeCachedObject(resourceInfo, dataObjectInfo.getObjectAreaInfo());
   }

   public void createObject(AFPDataObjectInfo dataObjectInfo) throws IOException {
      if (!this.tryIncludeObject(dataObjectInfo)) {
         AbstractNamedAFPObject namedObj = null;
         AFPResourceInfo resourceInfo = dataObjectInfo.getResourceInfo();
         boolean useInclude = true;
         Registry.ObjectType objectType = null;
         if (dataObjectInfo instanceof AFPImageObjectInfo) {
            AFPImageObjectInfo imageObjectInfo = (AFPImageObjectInfo)dataObjectInfo;
            namedObj = this.dataObjectFactory.createImage(imageObjectInfo);
         } else if (dataObjectInfo instanceof AFPGraphicsObjectInfo) {
            AFPGraphicsObjectInfo graphicsObjectInfo = (AFPGraphicsObjectInfo)dataObjectInfo;
            namedObj = this.dataObjectFactory.createGraphic(graphicsObjectInfo);
         } else {
            namedObj = this.dataObjectFactory.createObjectContainer(dataObjectInfo);
            objectType = dataObjectInfo.getObjectType();
            useInclude = objectType != null && objectType.isIncludable();
         }

         AFPResourceLevel resourceLevel = resourceInfo.getLevel();
         ResourceGroup resourceGroup = this.streamer.getResourceGroup(resourceLevel);
         useInclude &= resourceGroup != null;
         if (useInclude) {
            boolean usePageSegment = dataObjectInfo.isCreatePageSegment();
            if (resourceLevel.isPrintFile() || resourceLevel.isExternal()) {
               if (usePageSegment) {
                  String pageSegmentName = "S10" + ((AbstractNamedAFPObject)namedObj).getName().substring(3);
                  ((AbstractNamedAFPObject)namedObj).setName(pageSegmentName);
                  PageSegment seg = new PageSegment(pageSegmentName);
                  seg.addObject((AbstractAFPObject)namedObj);
                  namedObj = seg;
               }

               namedObj = this.dataObjectFactory.createResource((AbstractNamedAFPObject)namedObj, resourceInfo, objectType);
            }

            resourceGroup.addObject((AbstractNamedAFPObject)namedObj);
            this.includeObject((AbstractNamedAFPObject)namedObj, (AFPDataObjectInfo)dataObjectInfo);
         } else {
            this.dataStream.getCurrentPage().addObject(namedObj);
         }

      }
   }

   private void includeObject(AbstractNamedAFPObject namedObj, AFPDataObjectInfo dataObjectInfo) {
      String objectName = namedObj.getName();
      Object cachedObject;
      if (dataObjectInfo.isCreatePageSegment()) {
         cachedObject = new CachedPageSegment(objectName, dataObjectInfo);
      } else {
         cachedObject = new CachedObject(objectName, dataObjectInfo);
      }

      ((AbstractCachedObject)cachedObject).includeObject();
      this.addToCache(dataObjectInfo.getResourceInfo(), (AbstractCachedObject)cachedObject);
      dataObjectInfo.setData((byte[])null);
   }

   private void addToCache(AFPResourceInfo resourceInfo, AbstractCachedObject cachedObject) {
      List objs = (List)this.includeObjectCache.get(resourceInfo);
      if (objs == null) {
         objs = new ArrayList();
         this.includeObjectCache.put(resourceInfo, objs);
      }

      ((List)objs).add(cachedObject);
   }

   public boolean isObjectCached(AFPResourceInfo resourceInfo) {
      return this.includeObjectCache.containsKey(resourceInfo);
   }

   public boolean includeCachedObject(AFPResourceInfo resourceInfo, AFPObjectAreaInfo areaInfo) {
      List cachedObjectList = (List)this.includeObjectCache.get(resourceInfo);
      if (cachedObjectList != null && this.includeCached) {
         AbstractCachedObject cachedObject;
         for(Iterator var4 = cachedObjectList.iterator(); var4.hasNext(); cachedObject.includeObject()) {
            cachedObject = (AbstractCachedObject)var4.next();
            if (areaInfo != null && cachedObjectList.size() == 1) {
               cachedObject.dataObjectInfo.setObjectAreaInfo(areaInfo);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private void updateResourceInfoUri(AFPResourceInfo resourceInfo) {
      String uri = resourceInfo.getUri();
      if (uri == null) {
         uri = "/";
      }

      if (uri.endsWith("/")) {
         uri = uri + "#" + ++this.instreamObjectCount;
         resourceInfo.setUri(uri);
      }

   }

   private void includeObject(AFPDataObjectInfo dataObjectInfo, String objectName) {
      IncludeObject includeObject = this.dataObjectFactory.createInclude(objectName, dataObjectInfo);
      this.dataStream.getCurrentPage().addObject(includeObject);
   }

   public void embedFont(AFPFont afpFont, CharacterSet charSet) throws IOException {
      if (afpFont.isEmbeddable() && charSet.getResourceAccessor() != null) {
         AFPResourceAccessor accessor = charSet.getResourceAccessor();
         if (afpFont.getFontType() == FontType.TRUETYPE) {
            this.createIncludedResource(afpFont.getFontName(), ((AFPFontConfig.AFPTrueTypeFont)afpFont).getUri(), accessor, (byte)-110, true, ((AFPFontConfig.AFPTrueTypeFont)afpFont).getTTC());
         } else {
            this.createIncludedResource(charSet.getName(), accessor, (byte)64);
            this.createIncludedResource(charSet.getCodePage(), accessor, (byte)65);
         }
      }

   }

   private void includePageSegment(AFPDataObjectInfo dataObjectInfo, String pageSegmentName) {
      int x = dataObjectInfo.getObjectAreaInfo().getX();
      int y = dataObjectInfo.getObjectAreaInfo().getY();
      AbstractPageObject currentPage = this.dataStream.getCurrentPage();
      boolean createHardPageSegments = true;
      currentPage.createIncludePageSegment(pageSegmentName, x, y, createHardPageSegments);
   }

   public void createIncludedResource(String resourceName, AFPResourceAccessor accessor, byte resourceObjectType) throws IOException {
      URI uri;
      try {
         uri = new URI(resourceName.trim());
      } catch (URISyntaxException var6) {
         throw new IOException("Could not create URI from resource name: " + resourceName + " (" + var6.getMessage() + ")");
      }

      this.createIncludedResource(resourceName, uri, accessor, resourceObjectType, false, (String)null);
   }

   public void createIncludedResource(String resourceName, URI uri, AFPResourceAccessor accessor, byte resourceObjectType, boolean truetype, String ttc) throws IOException {
      AFPResourceLevel resourceLevel = new AFPResourceLevel(AFPResourceLevel.ResourceType.PRINT_FILE);
      AFPResourceInfo resourceInfo = new AFPResourceInfo();
      resourceInfo.setLevel(resourceLevel);
      resourceInfo.setName(resourceName);
      resourceInfo.setUri(uri.toASCIIString());
      List cachedObject = (List)this.includeObjectCache.get(resourceInfo);
      if (cachedObject == null) {
         if (log.isDebugEnabled()) {
            log.debug("Adding included resource: " + resourceName);
         }

         ResourceGroup resourceGroup = this.streamer.getResourceGroup(resourceLevel);
         ResourceObject res;
         if (truetype) {
            res = this.factory.createResource();
            res.setType((byte)-110);
            ActiveEnvironmentGroup.setupTruetypeMDR(res, false);
            ObjectContainer oc = this.factory.createObjectContainer();
            InputStream is = accessor.createInputStream(uri);
            if (ttc != null) {
               oc.setData(this.extractTTC(ttc, is));
            } else {
               oc.setData(IOUtils.toByteArray(is));
            }

            ActiveEnvironmentGroup.setupTruetypeMDR(oc, true);
            res.addTriplet(new EncodingTriplet(1200));
            res.setFullyQualifiedName((byte)1, (byte)0, resourceName, true);
            res.setDataObject(oc);
            resourceGroup.addObject(res);
         } else {
            res = this.factory.createResource(resourceName);
            IncludedResourceObject resourceContent = new IncludedResourceObject(resourceName, accessor, uri);
            res.setDataObject(resourceContent);
            res.setType(resourceObjectType);
            resourceGroup.addObject(res);
         }

         CachedObject newcachedObject = new CachedObject(resourceName, (AFPDataObjectInfo)null);
         this.addToCache(resourceInfo, newcachedObject);
      }

   }

   private byte[] extractTTC(String ttc, InputStream is) throws IOException {
      throw new IOException(ttc + " not supported");
   }

   public void createIncludedResourceFromExternal(final String resourceName, final URI uri, final AFPResourceAccessor accessor) throws IOException {
      AFPResourceLevel resourceLevel = new AFPResourceLevel(AFPResourceLevel.ResourceType.PRINT_FILE);
      AFPResourceInfo resourceInfo = new AFPResourceInfo();
      resourceInfo.setLevel(resourceLevel);
      resourceInfo.setName(resourceName);
      resourceInfo.setUri(uri.toASCIIString());
      List resource = (List)this.includeObjectCache.get(resourceInfo);
      if (resource == null) {
         ResourceGroup resourceGroup = this.streamer.getResourceGroup(resourceLevel);
         AbstractNamedAFPObject resourceObject = new AbstractNamedAFPObject((String)null) {
            protected void writeContent(OutputStream os) throws IOException {
               InputStream inputStream = null;

               try {
                  inputStream = accessor.createInputStream(uri);
                  BufferedInputStream bin = new BufferedInputStream(inputStream);
                  AFPResourceUtil.copyNamedResource(resourceName, bin, os);
               } finally {
                  IOUtils.closeQuietly(inputStream);
               }

            }

            protected void writeStart(OutputStream os) throws IOException {
            }

            protected void writeEnd(OutputStream os) throws IOException {
            }
         };
         resourceGroup.addObject(resourceObject);
         CachedObject newresource = new CachedObject(resourceName, (AFPDataObjectInfo)null);
         this.addToCache(resourceInfo, newresource);
      }

   }

   public void setResourceLevelDefaults(AFPResourceLevelDefaults defaults) {
      this.resourceLevelDefaults.mergeFrom(defaults);
   }

   public AFPResourceLevelDefaults getResourceLevelDefaults() {
      return this.resourceLevelDefaults;
   }

   private class CachedObject extends AbstractCachedObject {
      public CachedObject(String objectName, AFPDataObjectInfo dataObjectInfo) {
         super(objectName, dataObjectInfo);
      }

      protected void includeObject() {
         AFPResourceManager.this.includeObject(this.dataObjectInfo, this.objectName);
      }
   }

   private class CachedPageSegment extends AbstractCachedObject {
      public CachedPageSegment(String objectName, AFPDataObjectInfo dataObjectInfo) {
         super(objectName, dataObjectInfo);
      }

      protected void includeObject() {
         AFPResourceManager.this.includePageSegment(this.dataObjectInfo, this.objectName);
      }
   }

   private abstract class AbstractCachedObject {
      protected String objectName;
      protected AFPDataObjectInfo dataObjectInfo;

      public AbstractCachedObject(String objectName, AFPDataObjectInfo dataObjectInfo) {
         this.objectName = objectName;
         this.dataObjectInfo = dataObjectInfo;
      }

      protected abstract void includeObject();
   }
}
