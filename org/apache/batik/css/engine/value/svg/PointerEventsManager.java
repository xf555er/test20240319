package org.apache.batik.css.engine.value.svg;

import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;

public class PointerEventsManager extends IdentifierManager {
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
      return "pointer-events";
   }

   public Value getDefaultValue() {
      return SVGValueConstants.VISIBLEPAINTED_VALUE;
   }

   public StringMap getIdentifiers() {
      return values;
   }

   static {
      values.put("all", SVGValueConstants.ALL_VALUE);
      values.put("fill", SVGValueConstants.FILL_VALUE);
      values.put("fillstroke", SVGValueConstants.FILLSTROKE_VALUE);
      values.put("none", SVGValueConstants.NONE_VALUE);
      values.put("painted", SVGValueConstants.PAINTED_VALUE);
      values.put("stroke", SVGValueConstants.STROKE_VALUE);
      values.put("visible", SVGValueConstants.VISIBLE_VALUE);
      values.put("visiblefill", SVGValueConstants.VISIBLEFILL_VALUE);
      values.put("visiblefillstroke", SVGValueConstants.VISIBLEFILLSTROKE_VALUE);
      values.put("visiblepainted", SVGValueConstants.VISIBLEPAINTED_VALUE);
      values.put("visiblestroke", SVGValueConstants.VISIBLESTROKE_VALUE);
   }
}
