package org.apache.fop.fo.pagination;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.fop.apps.FOPException;
import org.apache.fop.fo.FONode;
import org.apache.fop.fo.FObj;
import org.apache.fop.fo.PropertyList;
import org.apache.fop.fo.ValidationException;
import org.xml.sax.Locator;

public class LayoutMasterSet extends FObj {
   private Map simplePageMasters;
   private Map pageSequenceMasters;

   public LayoutMasterSet(FONode parent) {
      super(parent);
   }

   public void bind(PropertyList pList) throws FOPException {
   }

   public void startOfNode() throws FOPException {
      this.getRoot().setLayoutMasterSet(this);
      this.simplePageMasters = new HashMap();
      this.pageSequenceMasters = new HashMap();
   }

   public void endOfNode() throws FOPException {
      if (this.firstChild == null) {
         this.missingChildElementError("(simple-page-master|page-sequence-master)+");
      }

      this.checkRegionNames();
      this.resolveSubSequenceReferences();
   }

   protected void validateChildNode(Locator loc, String nsURI, String localName) throws ValidationException {
      if ("http://www.w3.org/1999/XSL/Format".equals(nsURI) && !localName.equals("simple-page-master") && !localName.equals("page-sequence-master")) {
         this.invalidChildError(loc, nsURI, localName);
      }

   }

   private void checkRegionNames() throws ValidationException {
      Map allRegions = new HashMap();
      Iterator var2 = this.simplePageMasters.values().iterator();

      while(var2.hasNext()) {
         SimplePageMaster simplePageMaster = (SimplePageMaster)var2.next();
         Map spmRegions = simplePageMaster.getRegions();

         Region region;
         for(Iterator var5 = spmRegions.values().iterator(); var5.hasNext(); allRegions.put(region.getRegionName(), region.getDefaultRegionName())) {
            region = (Region)var5.next();
            if (allRegions.containsKey(region.getRegionName())) {
               String defaultRegionName = (String)allRegions.get(region.getRegionName());
               if (!defaultRegionName.equals(region.getDefaultRegionName())) {
                  this.getFOValidationEventProducer().regionNameMappedToMultipleRegionClasses(this, region.getRegionName(), defaultRegionName, region.getDefaultRegionName(), this.getLocator());
               }
            }
         }
      }

   }

   private void resolveSubSequenceReferences() throws ValidationException {
      Iterator var1 = this.pageSequenceMasters.values().iterator();

      while(var1.hasNext()) {
         PageSequenceMaster psm = (PageSequenceMaster)var1.next();
         Iterator var3 = psm.getSubSequenceSpecifier().iterator();

         while(var3.hasNext()) {
            SubSequenceSpecifier subSequenceSpecifier = (SubSequenceSpecifier)var3.next();
            subSequenceSpecifier.resolveReferences(this);
         }
      }

   }

   protected void addSimplePageMaster(SimplePageMaster sPM) throws ValidationException {
      String masterName = sPM.getMasterName();
      if (this.existsName(masterName)) {
         this.getFOValidationEventProducer().masterNameNotUnique(this, this.getName(), masterName, sPM.getLocator());
      }

      this.simplePageMasters.put(masterName, sPM);
   }

   private boolean existsName(String masterName) {
      return this.simplePageMasters.containsKey(masterName) || this.pageSequenceMasters.containsKey(masterName);
   }

   public SimplePageMaster getSimplePageMaster(String masterName) {
      return (SimplePageMaster)this.simplePageMasters.get(masterName);
   }

   protected void addPageSequenceMaster(String masterName, PageSequenceMaster pSM) throws ValidationException {
      if (this.existsName(masterName)) {
         this.getFOValidationEventProducer().masterNameNotUnique(this, this.getName(), masterName, pSM.getLocator());
      }

      this.pageSequenceMasters.put(masterName, pSM);
   }

   public PageSequenceMaster getPageSequenceMaster(String masterName) {
      return (PageSequenceMaster)this.pageSequenceMasters.get(masterName);
   }

   public boolean regionNameExists(String regionName) {
      Iterator var2 = this.simplePageMasters.values().iterator();

      SimplePageMaster spm;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         spm = (SimplePageMaster)var2.next();
      } while(!spm.regionNameExists(regionName));

      return true;
   }

   public String getLocalName() {
      return "layout-master-set";
   }

   public int getNameId() {
      return 38;
   }

   public String getDefaultRegionNameFor(String flowName) {
      Iterator var2 = this.simplePageMasters.values().iterator();

      while(var2.hasNext()) {
         SimplePageMaster spm = (SimplePageMaster)var2.next();
         Iterator var4 = spm.getRegions().values().iterator();

         while(var4.hasNext()) {
            Region region = (Region)var4.next();
            if (region.getRegionName().equals(flowName)) {
               return region.getDefaultRegionName();
            }
         }
      }

      assert flowName.equals("xsl-before-float-separator") || flowName.equals("xsl-footnote-separator");

      return flowName;
   }
}
