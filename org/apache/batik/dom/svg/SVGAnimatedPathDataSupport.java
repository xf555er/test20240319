package org.apache.batik.dom.svg;

import org.apache.batik.parser.PathHandler;
import org.w3c.dom.svg.SVGPathSeg;
import org.w3c.dom.svg.SVGPathSegArcAbs;
import org.w3c.dom.svg.SVGPathSegArcRel;
import org.w3c.dom.svg.SVGPathSegCurvetoCubicAbs;
import org.w3c.dom.svg.SVGPathSegCurvetoCubicRel;
import org.w3c.dom.svg.SVGPathSegCurvetoCubicSmoothAbs;
import org.w3c.dom.svg.SVGPathSegCurvetoCubicSmoothRel;
import org.w3c.dom.svg.SVGPathSegCurvetoQuadraticAbs;
import org.w3c.dom.svg.SVGPathSegCurvetoQuadraticRel;
import org.w3c.dom.svg.SVGPathSegCurvetoQuadraticSmoothAbs;
import org.w3c.dom.svg.SVGPathSegCurvetoQuadraticSmoothRel;
import org.w3c.dom.svg.SVGPathSegLinetoAbs;
import org.w3c.dom.svg.SVGPathSegLinetoHorizontalAbs;
import org.w3c.dom.svg.SVGPathSegLinetoHorizontalRel;
import org.w3c.dom.svg.SVGPathSegLinetoRel;
import org.w3c.dom.svg.SVGPathSegLinetoVerticalAbs;
import org.w3c.dom.svg.SVGPathSegLinetoVerticalRel;
import org.w3c.dom.svg.SVGPathSegList;
import org.w3c.dom.svg.SVGPathSegMovetoAbs;
import org.w3c.dom.svg.SVGPathSegMovetoRel;

public abstract class SVGAnimatedPathDataSupport {
   public static void handlePathSegList(SVGPathSegList p, PathHandler h) {
      int n = p.getNumberOfItems();
      h.startPath();

      for(int i = 0; i < n; ++i) {
         SVGPathSeg seg = p.getItem(i);
         switch (seg.getPathSegType()) {
            case 1:
               h.closePath();
               break;
            case 2:
               SVGPathSegMovetoAbs s = (SVGPathSegMovetoAbs)seg;
               h.movetoAbs(s.getX(), s.getY());
               break;
            case 3:
               SVGPathSegMovetoRel s = (SVGPathSegMovetoRel)seg;
               h.movetoRel(s.getX(), s.getY());
               break;
            case 4:
               SVGPathSegLinetoAbs s = (SVGPathSegLinetoAbs)seg;
               h.linetoAbs(s.getX(), s.getY());
               break;
            case 5:
               SVGPathSegLinetoRel s = (SVGPathSegLinetoRel)seg;
               h.linetoRel(s.getX(), s.getY());
               break;
            case 6:
               SVGPathSegCurvetoCubicAbs s = (SVGPathSegCurvetoCubicAbs)seg;
               h.curvetoCubicAbs(s.getX1(), s.getY1(), s.getX2(), s.getY2(), s.getX(), s.getY());
               break;
            case 7:
               SVGPathSegCurvetoCubicRel s = (SVGPathSegCurvetoCubicRel)seg;
               h.curvetoCubicRel(s.getX1(), s.getY1(), s.getX2(), s.getY2(), s.getX(), s.getY());
               break;
            case 8:
               SVGPathSegCurvetoQuadraticAbs s = (SVGPathSegCurvetoQuadraticAbs)seg;
               h.curvetoQuadraticAbs(s.getX1(), s.getY1(), s.getX(), s.getY());
               break;
            case 9:
               SVGPathSegCurvetoQuadraticRel s = (SVGPathSegCurvetoQuadraticRel)seg;
               h.curvetoQuadraticRel(s.getX1(), s.getY1(), s.getX(), s.getY());
               break;
            case 10:
               SVGPathSegArcAbs s = (SVGPathSegArcAbs)seg;
               h.arcAbs(s.getR1(), s.getR2(), s.getAngle(), s.getLargeArcFlag(), s.getSweepFlag(), s.getX(), s.getY());
               break;
            case 11:
               SVGPathSegArcRel s = (SVGPathSegArcRel)seg;
               h.arcRel(s.getR1(), s.getR2(), s.getAngle(), s.getLargeArcFlag(), s.getSweepFlag(), s.getX(), s.getY());
               break;
            case 12:
               SVGPathSegLinetoHorizontalAbs s = (SVGPathSegLinetoHorizontalAbs)seg;
               h.linetoHorizontalAbs(s.getX());
               break;
            case 13:
               SVGPathSegLinetoHorizontalRel s = (SVGPathSegLinetoHorizontalRel)seg;
               h.linetoHorizontalRel(s.getX());
               break;
            case 14:
               SVGPathSegLinetoVerticalAbs s = (SVGPathSegLinetoVerticalAbs)seg;
               h.linetoVerticalAbs(s.getY());
               break;
            case 15:
               SVGPathSegLinetoVerticalRel s = (SVGPathSegLinetoVerticalRel)seg;
               h.linetoVerticalRel(s.getY());
               break;
            case 16:
               SVGPathSegCurvetoCubicSmoothAbs s = (SVGPathSegCurvetoCubicSmoothAbs)seg;
               h.curvetoCubicSmoothAbs(s.getX2(), s.getY2(), s.getX(), s.getY());
               break;
            case 17:
               SVGPathSegCurvetoCubicSmoothRel s = (SVGPathSegCurvetoCubicSmoothRel)seg;
               h.curvetoCubicSmoothRel(s.getX2(), s.getY2(), s.getX(), s.getY());
               break;
            case 18:
               SVGPathSegCurvetoQuadraticSmoothAbs s = (SVGPathSegCurvetoQuadraticSmoothAbs)seg;
               h.curvetoQuadraticSmoothAbs(s.getX(), s.getY());
               break;
            case 19:
               SVGPathSegCurvetoQuadraticSmoothRel s = (SVGPathSegCurvetoQuadraticSmoothRel)seg;
               h.curvetoQuadraticSmoothRel(s.getX(), s.getY());
         }
      }

      h.endPath();
   }
}
