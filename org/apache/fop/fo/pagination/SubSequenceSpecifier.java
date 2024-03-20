package org.apache.fop.fo.pagination;

import org.apache.fop.fo.ValidationException;
import org.apache.fop.layoutmgr.BlockLevelEventProducer;

public interface SubSequenceSpecifier {
   SimplePageMaster getNextPageMaster(boolean var1, boolean var2, boolean var3, boolean var4) throws PageProductionException;

   SimplePageMaster getLastPageMaster(boolean var1, boolean var2, boolean var3, BlockLevelEventProducer var4) throws PageProductionException;

   void reset();

   boolean goToPrevious();

   boolean hasPagePositionLast();

   boolean hasPagePositionOnly();

   void resolveReferences(LayoutMasterSet var1) throws ValidationException;

   boolean canProcess(String var1);

   boolean isInfinite();

   boolean isReusable();
}
