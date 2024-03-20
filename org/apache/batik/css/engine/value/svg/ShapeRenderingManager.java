package org.apache.batik.css.engine.value.svg;

import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;

public class ShapeRenderingManager extends IdentifierManager {
   protected static final StringMap values = new StringMap();

   public boolean isInheritedProperty() {
      return true;
   }

   public boolean isAnimatableProperty() {
      return true;
   }

   public boolean isAdditiveProperty() {
      return false;
   }

   public int getPropertyType() {
      return 15;
   }

   public String getPropertyName() {
      return "shape-rendering";
   }

   public Value getDefaultValue() {
      return SVGValueConstants.AUTO_VALUE;
   }

   public StringMap getIdentifiers() {
      return values;
   }

   static {
      values.put("auto", SVGValueConstants.AUTO_VALUE);
      values.put("crispedges", SVGValueConstants.CRISPEDGES_VALUE);
      values.put("geometricprecision", SVGValueConstants.GEOMETRICPRECISION_VALUE);
      values.put("optimizespeed", SVGValueConstants.OPTIMIZESPEED_VALUE);
   }
}
