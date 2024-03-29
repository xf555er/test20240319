package org.apache.xerces.impl.xs;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidatedInfo;
import org.apache.xerces.impl.dv.ValidationContext;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.xs.models.CMBuilder;
import org.apache.xerces.impl.xs.models.XSCMValidator;
import org.apache.xerces.impl.xs.util.SimpleLocator;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.util.SymbolHash;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSTypeDefinition;

public class XSConstraints {
   static final int OCCURRENCE_UNKNOWN = -2;
   static final XSSimpleType STRING_TYPE;
   private static XSParticleDecl fEmptyParticle;
   private static final Comparator ELEMENT_PARTICLE_COMPARATOR;

   public static XSParticleDecl getEmptySequence() {
      if (fEmptyParticle == null) {
         XSModelGroupImpl var0 = new XSModelGroupImpl();
         var0.fCompositor = 102;
         var0.fParticleCount = 0;
         var0.fParticles = null;
         var0.fAnnotations = XSObjectListImpl.EMPTY_LIST;
         XSParticleDecl var1 = new XSParticleDecl();
         var1.fType = 3;
         var1.fValue = var0;
         var1.fAnnotations = XSObjectListImpl.EMPTY_LIST;
         fEmptyParticle = var1;
      }

      return fEmptyParticle;
   }

   public static boolean checkTypeDerivationOk(XSTypeDefinition var0, XSTypeDefinition var1, short var2) {
      if (var0 == SchemaGrammar.fAnyType) {
         return var0 == var1;
      } else if (var0 != SchemaGrammar.fAnySimpleType) {
         if (var0.getTypeCategory() == 16) {
            if (((XSTypeDefinition)var1).getTypeCategory() == 15) {
               if (var1 != SchemaGrammar.fAnyType) {
                  return false;
               }

               var1 = SchemaGrammar.fAnySimpleType;
            }

            return checkSimpleDerivation((XSSimpleType)var0, (XSSimpleType)var1, var2);
         } else {
            return checkComplexDerivation((XSComplexTypeDecl)var0, (XSTypeDefinition)var1, var2);
         }
      } else {
         return var1 == SchemaGrammar.fAnyType || var1 == SchemaGrammar.fAnySimpleType;
      }
   }

   public static boolean checkSimpleDerivationOk(XSSimpleType var0, XSTypeDefinition var1, short var2) {
      if (var0 != SchemaGrammar.fAnySimpleType) {
         if (((XSTypeDefinition)var1).getTypeCategory() == 15) {
            if (var1 != SchemaGrammar.fAnyType) {
               return false;
            }

            var1 = SchemaGrammar.fAnySimpleType;
         }

         return checkSimpleDerivation(var0, (XSSimpleType)var1, var2);
      } else {
         return var1 == SchemaGrammar.fAnyType || var1 == SchemaGrammar.fAnySimpleType;
      }
   }

   public static boolean checkComplexDerivationOk(XSComplexTypeDecl var0, XSTypeDefinition var1, short var2) {
      if (var0 == SchemaGrammar.fAnyType) {
         return var0 == var1;
      } else {
         return checkComplexDerivation(var0, var1, var2);
      }
   }

   private static boolean checkSimpleDerivation(XSSimpleType var0, XSSimpleType var1, short var2) {
      if (var0 == var1) {
         return true;
      } else if ((var2 & 2) == 0 && (var0.getBaseType().getFinal() & 2) == 0) {
         XSSimpleType var3 = (XSSimpleType)var0.getBaseType();
         if (var3 == var1) {
            return true;
         } else if (var3 != SchemaGrammar.fAnySimpleType && checkSimpleDerivation(var3, var1, var2)) {
            return true;
         } else if ((var0.getVariety() == 2 || var0.getVariety() == 3) && var1 == SchemaGrammar.fAnySimpleType) {
            return true;
         } else {
            if (var1.getVariety() == 3) {
               XSObjectList var4 = var1.getMemberTypes();
               int var5 = var4.getLength();

               for(int var6 = 0; var6 < var5; ++var6) {
                  var1 = (XSSimpleType)var4.item(var6);
                  if (checkSimpleDerivation(var0, var1, var2)) {
                     return true;
                  }
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private static boolean checkComplexDerivation(XSComplexTypeDecl var0, XSTypeDefinition var1, short var2) {
      if (var0 == var1) {
         return true;
      } else if ((var0.fDerivedBy & var2) != 0) {
         return false;
      } else {
         XSTypeDefinition var3 = var0.fBaseType;
         if (var3 == var1) {
            return true;
         } else if (var3 != SchemaGrammar.fAnyType && var3 != SchemaGrammar.fAnySimpleType) {
            if (var3.getTypeCategory() == 15) {
               return checkComplexDerivation((XSComplexTypeDecl)var3, (XSTypeDefinition)var1, var2);
            } else if (var3.getTypeCategory() == 16) {
               if (((XSTypeDefinition)var1).getTypeCategory() == 15) {
                  if (var1 != SchemaGrammar.fAnyType) {
                     return false;
                  }

                  var1 = SchemaGrammar.fAnySimpleType;
               }

               return checkSimpleDerivation((XSSimpleType)var3, (XSSimpleType)var1, var2);
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   public static Object ElementDefaultValidImmediate(XSTypeDefinition var0, String var1, ValidationContext var2, ValidatedInfo var3) {
      XSSimpleType var4 = null;
      XSComplexTypeDecl var5;
      if (var0.getTypeCategory() == 16) {
         var4 = (XSSimpleType)var0;
      } else {
         var5 = (XSComplexTypeDecl)var0;
         if (var5.fContentType == 1) {
            var4 = var5.fXSSimpleType;
         } else {
            if (var5.fContentType != 3) {
               return null;
            }

            if (!((XSParticleDecl)var5.getParticle()).emptiable()) {
               return null;
            }
         }
      }

      var5 = null;
      if (var4 == null) {
         var4 = STRING_TYPE;
      }

      try {
         Object var8 = var4.validate(var1, var2, var3);
         if (var3 != null) {
            var8 = var4.validate(var3.stringValue(), var2, var3);
         }

         return var8;
      } catch (InvalidDatatypeValueException var7) {
         return null;
      }
   }

   static void reportSchemaError(XMLErrorReporter var0, SimpleLocator var1, String var2, Object[] var3) {
      if (var1 != null) {
         var0.reportError(var1, "http://www.w3.org/TR/xml-schema-1", var2, var3, (short)1);
      } else {
         var0.reportError("http://www.w3.org/TR/xml-schema-1", var2, var3, (short)1);
      }

   }

   public static void fullSchemaChecking(XSGrammarBucket var0, SubstitutionGroupHandler var1, CMBuilder var2, XMLErrorReporter var3) {
      SchemaGrammar[] var4 = var0.getGrammars();

      for(int var5 = var4.length - 1; var5 >= 0; --var5) {
         var1.addSubstitutionGroup(var4[var5].getSubstitutionGroups());
      }

      XSParticleDecl var6 = new XSParticleDecl();
      XSParticleDecl var7 = new XSParticleDecl();
      var6.fType = 3;
      var7.fType = 3;

      SimpleLocator[] var10;
      for(int var8 = var4.length - 1; var8 >= 0; --var8) {
         XSGroupDecl[] var9 = var4[var8].getRedefinedGroupDecls();
         var10 = var4[var8].getRGLocators();
         int var11 = 0;

         while(var11 < var9.length) {
            XSGroupDecl var12 = var9[var11++];
            XSModelGroupImpl var13 = var12.fModelGroup;
            XSGroupDecl var14 = var9[var11++];
            XSModelGroupImpl var15 = var14.fModelGroup;
            var6.fValue = var13;
            var7.fValue = var15;
            if (var15 == null) {
               if (var13 != null) {
                  reportSchemaError(var3, var10[var11 / 2 - 1], "src-redefine.6.2.2", new Object[]{var12.fName, "rcase-Recurse.2"});
               }
            } else if (var13 == null) {
               if (!var7.emptiable()) {
                  reportSchemaError(var3, var10[var11 / 2 - 1], "src-redefine.6.2.2", new Object[]{var12.fName, "rcase-Recurse.2"});
               }
            } else {
               try {
                  particleValidRestriction(var6, var1, var7, var1);
               } catch (XMLSchemaException var23) {
                  String var17 = var23.getKey();
                  reportSchemaError(var3, var10[var11 / 2 - 1], var17, var23.getArgs());
                  reportSchemaError(var3, var10[var11 / 2 - 1], "src-redefine.6.2.2", new Object[]{var12.fName, var17});
               }
            }
         }
      }

      SymbolHash var28 = new SymbolHash();

      for(int var29 = var4.length - 1; var29 >= 0; --var29) {
         int var27 = 0;
         boolean var26 = var4[var29].fFullChecked;
         XSComplexTypeDecl[] var24 = var4[var29].getUncheckedComplexTypeDecls();
         var10 = var4[var29].getUncheckedCTLocators();

         for(int var16 = 0; var16 < var24.length; ++var16) {
            if (!var26 && var24[var16].fParticle != null) {
               var28.clear();

               try {
                  checkElementDeclsConsistent(var24[var16], var24[var16].fParticle, var28, var1);
               } catch (XMLSchemaException var22) {
                  reportSchemaError(var3, var10[var16], var22.getKey(), var22.getArgs());
               }
            }

            if (var24[var16].fBaseType != null && var24[var16].fBaseType != SchemaGrammar.fAnyType && var24[var16].fDerivedBy == 2 && var24[var16].fBaseType instanceof XSComplexTypeDecl) {
               XSParticleDecl var30 = var24[var16].fParticle;
               XSParticleDecl var18 = ((XSComplexTypeDecl)var24[var16].fBaseType).fParticle;
               if (var30 == null) {
                  if (var18 != null && !var18.emptiable()) {
                     reportSchemaError(var3, var10[var16], "derivation-ok-restriction.5.3.2", new Object[]{var24[var16].fName, var24[var16].fBaseType.getName()});
                  }
               } else if (var18 != null) {
                  try {
                     particleValidRestriction(var24[var16].fParticle, var1, ((XSComplexTypeDecl)var24[var16].fBaseType).fParticle, var1);
                  } catch (XMLSchemaException var21) {
                     reportSchemaError(var3, var10[var16], var21.getKey(), var21.getArgs());
                     reportSchemaError(var3, var10[var16], "derivation-ok-restriction.5.4.2", new Object[]{var24[var16].fName});
                  }
               } else {
                  reportSchemaError(var3, var10[var16], "derivation-ok-restriction.5.4.2", new Object[]{var24[var16].fName});
               }
            }

            XSCMValidator var31 = var24[var16].getContentModel(var2, true);
            boolean var25 = false;
            if (var31 != null) {
               try {
                  var25 = var31.checkUniqueParticleAttribution(var1);
               } catch (XMLSchemaException var20) {
                  reportSchemaError(var3, var10[var16], var20.getKey(), var20.getArgs());
               }
            }

            if (!var26 && var25) {
               var24[var27++] = var24[var16];
            }
         }

         if (!var26) {
            var4[var29].setUncheckedTypeNum(var27);
            var4[var29].fFullChecked = true;
         }
      }

   }

   public static void checkElementDeclsConsistent(XSComplexTypeDecl var0, XSParticleDecl var1, SymbolHash var2, SubstitutionGroupHandler var3) throws XMLSchemaException {
      short var4 = var1.fType;
      if (var4 != 2) {
         if (var4 == 1) {
            XSElementDecl var8 = (XSElementDecl)var1.fValue;
            findElemInTable(var0, var8, var2);
            if (var8.fScope == 1) {
               XSElementDecl[] var9 = var3.getSubstitutionGroup(var8);

               for(int var7 = 0; var7 < var9.length; ++var7) {
                  findElemInTable(var0, var9[var7], var2);
               }
            }

         } else {
            XSModelGroupImpl var5 = (XSModelGroupImpl)var1.fValue;

            for(int var6 = 0; var6 < var5.fParticleCount; ++var6) {
               checkElementDeclsConsistent(var0, var5.fParticles[var6], var2, var3);
            }

         }
      }
   }

   public static void findElemInTable(XSComplexTypeDecl var0, XSElementDecl var1, SymbolHash var2) throws XMLSchemaException {
      String var3 = var1.fName + "," + var1.fTargetNamespace;
      XSElementDecl var4 = null;
      if ((var4 = (XSElementDecl)var2.get(var3)) == null) {
         var2.put(var3, var1);
      } else {
         if (var1 == var4) {
            return;
         }

         if (var1.fType != var4.fType) {
            throw new XMLSchemaException("cos-element-consistent", new Object[]{var0.fName, var1.fName});
         }
      }

   }

   private static boolean particleValidRestriction(XSParticleDecl var0, SubstitutionGroupHandler var1, XSParticleDecl var2, SubstitutionGroupHandler var3) throws XMLSchemaException {
      return particleValidRestriction(var0, var1, var2, var3, true);
   }

   private static boolean particleValidRestriction(XSParticleDecl var0, SubstitutionGroupHandler var1, XSParticleDecl var2, SubstitutionGroupHandler var3, boolean var4) throws XMLSchemaException {
      Vector var5 = null;
      Vector var6 = null;
      int var7 = -2;
      int var8 = -2;
      boolean var9 = false;
      if (var0.isEmpty() && !var2.emptiable()) {
         throw new XMLSchemaException("cos-particle-restrict.a", (Object[])null);
      } else if (!var0.isEmpty() && var2.isEmpty()) {
         throw new XMLSchemaException("cos-particle-restrict.b", (Object[])null);
      } else {
         short var10 = var0.fType;
         if (var10 == 3) {
            var10 = ((XSModelGroupImpl)var0.fValue).fCompositor;
            XSParticleDecl var11 = getNonUnaryGroup(var0);
            if (var11 != var0) {
               var0 = var11;
               var10 = var11.fType;
               if (var10 == 3) {
                  var10 = ((XSModelGroupImpl)var11.fValue).fCompositor;
               }
            }

            var5 = removePointlessChildren(var0);
         }

         int var19 = var0.fMinOccurs;
         int var12 = var0.fMaxOccurs;
         int var15;
         if (var1 != null && var10 == 1) {
            XSElementDecl var13 = (XSElementDecl)var0.fValue;
            if (var13.fScope == 1) {
               XSElementDecl[] var14 = var1.getSubstitutionGroup(var13);
               if (var14.length > 0) {
                  var10 = 101;
                  var7 = var19;
                  var8 = var12;
                  var5 = new Vector(var14.length + 1);

                  for(var15 = 0; var15 < var14.length; ++var15) {
                     addElementToParticleVector(var5, var14[var15]);
                  }

                  addElementToParticleVector(var5, var13);
                  Collections.sort(var5, ELEMENT_PARTICLE_COMPARATOR);
                  var1 = null;
               }
            }
         }

         short var20 = var2.fType;
         if (var20 == 3) {
            var20 = ((XSModelGroupImpl)var2.fValue).fCompositor;
            XSParticleDecl var21 = getNonUnaryGroup(var2);
            if (var21 != var2) {
               var2 = var21;
               var20 = var21.fType;
               if (var20 == 3) {
                  var20 = ((XSModelGroupImpl)var21.fValue).fCompositor;
               }
            }

            var6 = removePointlessChildren(var2);
         }

         int var22 = var2.fMinOccurs;
         var15 = var2.fMaxOccurs;
         if (var3 != null && var20 == 1) {
            XSElementDecl var16 = (XSElementDecl)var2.fValue;
            if (var16.fScope == 1) {
               XSElementDecl[] var17 = var3.getSubstitutionGroup(var16);
               if (var17.length > 0) {
                  var20 = 101;
                  var6 = new Vector(var17.length + 1);

                  for(int var18 = 0; var18 < var17.length; ++var18) {
                     addElementToParticleVector(var6, var17[var18]);
                  }

                  addElementToParticleVector(var6, var16);
                  Collections.sort(var6, ELEMENT_PARTICLE_COMPARATOR);
                  var3 = null;
                  var9 = true;
               }
            }
         }

         switch (var10) {
            case 1:
               switch (var20) {
                  case 1:
                     checkNameAndTypeOK((XSElementDecl)var0.fValue, var19, var12, (XSElementDecl)var2.fValue, var22, var15);
                     return var9;
                  case 2:
                     checkNSCompat((XSElementDecl)var0.fValue, var19, var12, (XSWildcardDecl)var2.fValue, var22, var15, var4);
                     return var9;
                  case 101:
                     var5 = new Vector();
                     var5.addElement(var0);
                     checkRecurseLax(var5, 1, 1, var1, var6, var22, var15, var3);
                     return var9;
                  case 102:
                  case 103:
                     var5 = new Vector();
                     var5.addElement(var0);
                     checkRecurse(var5, 1, 1, var1, var6, var22, var15, var3);
                     return var9;
                  default:
                     throw new XMLSchemaException("Internal-Error", new Object[]{"in particleValidRestriction"});
               }
            case 2:
               switch (var20) {
                  case 1:
                  case 101:
                  case 102:
                  case 103:
                     throw new XMLSchemaException("cos-particle-restrict.2", new Object[]{"any:choice,sequence,all,elt"});
                  case 2:
                     checkNSSubset((XSWildcardDecl)var0.fValue, var19, var12, (XSWildcardDecl)var2.fValue, var22, var15);
                     return var9;
                  default:
                     throw new XMLSchemaException("Internal-Error", new Object[]{"in particleValidRestriction"});
               }
            case 101:
               switch (var20) {
                  case 1:
                  case 102:
                  case 103:
                     throw new XMLSchemaException("cos-particle-restrict.2", new Object[]{"choice:all,sequence,elt"});
                  case 2:
                     if (var7 == -2) {
                        var7 = var0.minEffectiveTotalRange();
                     }

                     if (var8 == -2) {
                        var8 = var0.maxEffectiveTotalRange();
                     }

                     checkNSRecurseCheckCardinality(var5, var7, var8, var1, var2, var22, var15, var4);
                     return var9;
                  case 101:
                     checkRecurseLax(var5, var19, var12, var1, var6, var22, var15, var3);
                     return var9;
                  default:
                     throw new XMLSchemaException("Internal-Error", new Object[]{"in particleValidRestriction"});
               }
            case 102:
               switch (var20) {
                  case 1:
                     throw new XMLSchemaException("cos-particle-restrict.2", new Object[]{"seq:elt"});
                  case 2:
                     if (var7 == -2) {
                        var7 = var0.minEffectiveTotalRange();
                     }

                     if (var8 == -2) {
                        var8 = var0.maxEffectiveTotalRange();
                     }

                     checkNSRecurseCheckCardinality(var5, var7, var8, var1, var2, var22, var15, var4);
                     return var9;
                  case 101:
                     int var23 = var19 * var5.size();
                     int var24 = var12 == -1 ? var12 : var12 * var5.size();
                     checkMapAndSum(var5, var23, var24, var1, var6, var22, var15, var3);
                     return var9;
                  case 102:
                     checkRecurse(var5, var19, var12, var1, var6, var22, var15, var3);
                     return var9;
                  case 103:
                     checkRecurseUnordered(var5, var19, var12, var1, var6, var22, var15, var3);
                     return var9;
                  default:
                     throw new XMLSchemaException("Internal-Error", new Object[]{"in particleValidRestriction"});
               }
            case 103:
               switch (var20) {
                  case 1:
                  case 101:
                  case 102:
                     throw new XMLSchemaException("cos-particle-restrict.2", new Object[]{"all:choice,sequence,elt"});
                  case 2:
                     if (var7 == -2) {
                        var7 = var0.minEffectiveTotalRange();
                     }

                     if (var8 == -2) {
                        var8 = var0.maxEffectiveTotalRange();
                     }

                     checkNSRecurseCheckCardinality(var5, var7, var8, var1, var2, var22, var15, var4);
                     return var9;
                  case 103:
                     checkRecurse(var5, var19, var12, var1, var6, var22, var15, var3);
                     return var9;
                  default:
                     throw new XMLSchemaException("Internal-Error", new Object[]{"in particleValidRestriction"});
               }
            default:
               return var9;
         }
      }
   }

   private static void addElementToParticleVector(Vector var0, XSElementDecl var1) {
      XSParticleDecl var2 = new XSParticleDecl();
      var2.fValue = var1;
      var2.fType = 1;
      var0.addElement(var2);
   }

   private static XSParticleDecl getNonUnaryGroup(XSParticleDecl var0) {
      if (var0.fType != 1 && var0.fType != 2) {
         return var0.fMinOccurs == 1 && var0.fMaxOccurs == 1 && var0.fValue != null && ((XSModelGroupImpl)var0.fValue).fParticleCount == 1 ? getNonUnaryGroup(((XSModelGroupImpl)var0.fValue).fParticles[0]) : var0;
      } else {
         return var0;
      }
   }

   private static Vector removePointlessChildren(XSParticleDecl var0) {
      if (var0.fType != 1 && var0.fType != 2) {
         Vector var1 = new Vector();
         XSModelGroupImpl var2 = (XSModelGroupImpl)var0.fValue;

         for(int var3 = 0; var3 < var2.fParticleCount; ++var3) {
            gatherChildren(var2.fCompositor, var2.fParticles[var3], var1);
         }

         return var1;
      } else {
         return null;
      }
   }

   private static void gatherChildren(int var0, XSParticleDecl var1, Vector var2) {
      int var3 = var1.fMinOccurs;
      int var4 = var1.fMaxOccurs;
      short var5 = var1.fType;
      if (var5 == 3) {
         var5 = ((XSModelGroupImpl)var1.fValue).fCompositor;
      }

      if (var5 != 1 && var5 != 2) {
         if (var3 == 1 && var4 == 1) {
            if (var0 == var5) {
               XSModelGroupImpl var6 = (XSModelGroupImpl)var1.fValue;

               for(int var7 = 0; var7 < var6.fParticleCount; ++var7) {
                  gatherChildren(var5, var6.fParticles[var7], var2);
               }
            } else if (!var1.isEmpty()) {
               var2.addElement(var1);
            }
         } else {
            var2.addElement(var1);
         }

      } else {
         var2.addElement(var1);
      }
   }

   private static void checkNameAndTypeOK(XSElementDecl var0, int var1, int var2, XSElementDecl var3, int var4, int var5) throws XMLSchemaException {
      if (var0.fName == var3.fName && var0.fTargetNamespace == var3.fTargetNamespace) {
         if (!var3.getNillable() && var0.getNillable()) {
            throw new XMLSchemaException("rcase-NameAndTypeOK.2", new Object[]{var0.fName});
         } else if (!checkOccurrenceRange(var1, var2, var4, var5)) {
            throw new XMLSchemaException("rcase-NameAndTypeOK.3", new Object[]{var0.fName, Integer.toString(var1), var2 == -1 ? "unbounded" : Integer.toString(var2), Integer.toString(var4), var5 == -1 ? "unbounded" : Integer.toString(var5)});
         } else {
            if (var3.getConstraintType() == 2) {
               if (var0.getConstraintType() != 2) {
                  throw new XMLSchemaException("rcase-NameAndTypeOK.4.a", new Object[]{var0.fName, var3.fDefault.stringValue()});
               }

               boolean var6 = false;
               if (var0.fType.getTypeCategory() == 16 || ((XSComplexTypeDecl)var0.fType).fContentType == 1) {
                  var6 = true;
               }

               if (!var6 && !var3.fDefault.normalizedValue.equals(var0.fDefault.normalizedValue) || var6 && !var3.fDefault.actualValue.equals(var0.fDefault.actualValue)) {
                  throw new XMLSchemaException("rcase-NameAndTypeOK.4.b", new Object[]{var0.fName, var0.fDefault.stringValue(), var3.fDefault.stringValue()});
               }
            }

            checkIDConstraintRestriction(var0, var3);
            short var8 = var0.fBlock;
            short var7 = var3.fBlock;
            if ((var8 & var7) != var7 || var8 == 0 && var7 != 0) {
               throw new XMLSchemaException("rcase-NameAndTypeOK.6", new Object[]{var0.fName});
            } else if (!checkTypeDerivationOk(var0.fType, var3.fType, (short)25)) {
               throw new XMLSchemaException("rcase-NameAndTypeOK.7", new Object[]{var0.fName, var0.fType.getName(), var3.fType.getName()});
            }
         }
      } else {
         throw new XMLSchemaException("rcase-NameAndTypeOK.1", new Object[]{var0.fName, var0.fTargetNamespace, var3.fName, var3.fTargetNamespace});
      }
   }

   private static void checkIDConstraintRestriction(XSElementDecl var0, XSElementDecl var1) throws XMLSchemaException {
   }

   private static boolean checkOccurrenceRange(int var0, int var1, int var2, int var3) {
      return var0 >= var2 && (var3 == -1 || var1 != -1 && var1 <= var3);
   }

   private static void checkNSCompat(XSElementDecl var0, int var1, int var2, XSWildcardDecl var3, int var4, int var5, boolean var6) throws XMLSchemaException {
      if (var6 && !checkOccurrenceRange(var1, var2, var4, var5)) {
         throw new XMLSchemaException("rcase-NSCompat.2", new Object[]{var0.fName, Integer.toString(var1), var2 == -1 ? "unbounded" : Integer.toString(var2), Integer.toString(var4), var5 == -1 ? "unbounded" : Integer.toString(var5)});
      } else if (!var3.allowNamespace(var0.fTargetNamespace)) {
         throw new XMLSchemaException("rcase-NSCompat.1", new Object[]{var0.fName, var0.fTargetNamespace});
      }
   }

   private static void checkNSSubset(XSWildcardDecl var0, int var1, int var2, XSWildcardDecl var3, int var4, int var5) throws XMLSchemaException {
      if (!checkOccurrenceRange(var1, var2, var4, var5)) {
         throw new XMLSchemaException("rcase-NSSubset.2", new Object[]{Integer.toString(var1), var2 == -1 ? "unbounded" : Integer.toString(var2), Integer.toString(var4), var5 == -1 ? "unbounded" : Integer.toString(var5)});
      } else if (!var0.isSubsetOf(var3)) {
         throw new XMLSchemaException("rcase-NSSubset.1", (Object[])null);
      } else if (var0.weakerProcessContents(var3)) {
         throw new XMLSchemaException("rcase-NSSubset.3", new Object[]{var0.getProcessContentsAsString(), var3.getProcessContentsAsString()});
      }
   }

   private static void checkNSRecurseCheckCardinality(Vector var0, int var1, int var2, SubstitutionGroupHandler var3, XSParticleDecl var4, int var5, int var6, boolean var7) throws XMLSchemaException {
      if (var7 && !checkOccurrenceRange(var1, var2, var5, var6)) {
         throw new XMLSchemaException("rcase-NSRecurseCheckCardinality.2", new Object[]{Integer.toString(var1), var2 == -1 ? "unbounded" : Integer.toString(var2), Integer.toString(var5), var6 == -1 ? "unbounded" : Integer.toString(var6)});
      } else {
         int var8 = var0.size();

         try {
            for(int var9 = 0; var9 < var8; ++var9) {
               XSParticleDecl var10 = (XSParticleDecl)var0.elementAt(var9);
               particleValidRestriction(var10, var3, var4, (SubstitutionGroupHandler)null, false);
            }

         } catch (XMLSchemaException var11) {
            throw new XMLSchemaException("rcase-NSRecurseCheckCardinality.1", (Object[])null);
         }
      }
   }

   private static void checkRecurse(Vector var0, int var1, int var2, SubstitutionGroupHandler var3, Vector var4, int var5, int var6, SubstitutionGroupHandler var7) throws XMLSchemaException {
      if (!checkOccurrenceRange(var1, var2, var5, var6)) {
         throw new XMLSchemaException("rcase-Recurse.1", new Object[]{Integer.toString(var1), var2 == -1 ? "unbounded" : Integer.toString(var2), Integer.toString(var5), var6 == -1 ? "unbounded" : Integer.toString(var6)});
      } else {
         int var8 = var0.size();
         int var9 = var4.size();
         int var10 = 0;

         label59:
         for(int var11 = 0; var11 < var8; ++var11) {
            XSParticleDecl var12 = (XSParticleDecl)var0.elementAt(var11);
            int var13 = var10;

            while(var13 < var9) {
               XSParticleDecl var14 = (XSParticleDecl)var4.elementAt(var13);
               ++var10;

               try {
                  particleValidRestriction(var12, var3, var14, var7);
                  continue label59;
               } catch (XMLSchemaException var16) {
                  if (!var14.emptiable()) {
                     throw new XMLSchemaException("rcase-Recurse.2", (Object[])null);
                  }

                  ++var13;
               }
            }

            throw new XMLSchemaException("rcase-Recurse.2", (Object[])null);
         }

         for(int var17 = var10; var17 < var9; ++var17) {
            XSParticleDecl var18 = (XSParticleDecl)var4.elementAt(var17);
            if (!var18.emptiable()) {
               throw new XMLSchemaException("rcase-Recurse.2", (Object[])null);
            }
         }

      }
   }

   private static void checkRecurseUnordered(Vector var0, int var1, int var2, SubstitutionGroupHandler var3, Vector var4, int var5, int var6, SubstitutionGroupHandler var7) throws XMLSchemaException {
      if (!checkOccurrenceRange(var1, var2, var5, var6)) {
         throw new XMLSchemaException("rcase-RecurseUnordered.1", new Object[]{Integer.toString(var1), var2 == -1 ? "unbounded" : Integer.toString(var2), Integer.toString(var5), var6 == -1 ? "unbounded" : Integer.toString(var6)});
      } else {
         int var8 = var0.size();
         int var9 = var4.size();
         boolean[] var10 = new boolean[var9];
         int var11 = 0;

         label60:
         while(var11 < var8) {
            XSParticleDecl var12 = (XSParticleDecl)var0.elementAt(var11);
            int var13 = 0;

            while(var13 < var9) {
               XSParticleDecl var14 = (XSParticleDecl)var4.elementAt(var13);

               try {
                  particleValidRestriction(var12, var3, var14, var7);
                  if (var10[var13]) {
                     throw new XMLSchemaException("rcase-RecurseUnordered.2", (Object[])null);
                  }

                  var10[var13] = true;
               } catch (XMLSchemaException var16) {
                  ++var13;
                  continue;
               }

               ++var11;
               continue label60;
            }

            throw new XMLSchemaException("rcase-RecurseUnordered.2", (Object[])null);
         }

         for(int var17 = 0; var17 < var9; ++var17) {
            XSParticleDecl var18 = (XSParticleDecl)var4.elementAt(var17);
            if (!var10[var17] && !var18.emptiable()) {
               throw new XMLSchemaException("rcase-RecurseUnordered.2", (Object[])null);
            }
         }

      }
   }

   private static void checkRecurseLax(Vector var0, int var1, int var2, SubstitutionGroupHandler var3, Vector var4, int var5, int var6, SubstitutionGroupHandler var7) throws XMLSchemaException {
      if (!checkOccurrenceRange(var1, var2, var5, var6)) {
         throw new XMLSchemaException("rcase-RecurseLax.1", new Object[]{Integer.toString(var1), var2 == -1 ? "unbounded" : Integer.toString(var2), Integer.toString(var5), var6 == -1 ? "unbounded" : Integer.toString(var6)});
      } else {
         int var8 = var0.size();
         int var9 = var4.size();
         int var10 = 0;

         label45:
         for(int var11 = 0; var11 < var8; ++var11) {
            XSParticleDecl var12 = (XSParticleDecl)var0.elementAt(var11);
            int var13 = var10;

            while(var13 < var9) {
               XSParticleDecl var14 = (XSParticleDecl)var4.elementAt(var13);
               ++var10;

               try {
                  if (particleValidRestriction(var12, var3, var14, var7)) {
                     --var10;
                  }
                  continue label45;
               } catch (XMLSchemaException var16) {
                  ++var13;
               }
            }

            throw new XMLSchemaException("rcase-RecurseLax.2", (Object[])null);
         }

      }
   }

   private static void checkMapAndSum(Vector var0, int var1, int var2, SubstitutionGroupHandler var3, Vector var4, int var5, int var6, SubstitutionGroupHandler var7) throws XMLSchemaException {
      if (!checkOccurrenceRange(var1, var2, var5, var6)) {
         throw new XMLSchemaException("rcase-MapAndSum.2", new Object[]{Integer.toString(var1), var2 == -1 ? "unbounded" : Integer.toString(var2), Integer.toString(var5), var6 == -1 ? "unbounded" : Integer.toString(var6)});
      } else {
         int var8 = var0.size();
         int var9 = var4.size();

         label41:
         for(int var10 = 0; var10 < var8; ++var10) {
            XSParticleDecl var11 = (XSParticleDecl)var0.elementAt(var10);
            int var12 = 0;

            while(var12 < var9) {
               XSParticleDecl var13 = (XSParticleDecl)var4.elementAt(var12);

               try {
                  particleValidRestriction(var11, var3, var13, var7);
                  continue label41;
               } catch (XMLSchemaException var15) {
                  ++var12;
               }
            }

            throw new XMLSchemaException("rcase-MapAndSum.1", (Object[])null);
         }

      }
   }

   public static boolean overlapUPA(XSElementDecl var0, XSElementDecl var1, SubstitutionGroupHandler var2) {
      if (var0.fName == var1.fName && var0.fTargetNamespace == var1.fTargetNamespace) {
         return true;
      } else {
         XSElementDecl[] var3 = var2.getSubstitutionGroup(var0);

         for(int var4 = var3.length - 1; var4 >= 0; --var4) {
            if (var3[var4].fName == var1.fName && var3[var4].fTargetNamespace == var1.fTargetNamespace) {
               return true;
            }
         }

         var3 = var2.getSubstitutionGroup(var1);

         for(int var5 = var3.length - 1; var5 >= 0; --var5) {
            if (var3[var5].fName == var0.fName && var3[var5].fTargetNamespace == var0.fTargetNamespace) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean overlapUPA(XSElementDecl var0, XSWildcardDecl var1, SubstitutionGroupHandler var2) {
      if (var1.allowNamespace(var0.fTargetNamespace)) {
         return true;
      } else {
         XSElementDecl[] var3 = var2.getSubstitutionGroup(var0);

         for(int var4 = var3.length - 1; var4 >= 0; --var4) {
            if (var1.allowNamespace(var3[var4].fTargetNamespace)) {
               return true;
            }
         }

         return false;
      }
   }

   public static boolean overlapUPA(XSWildcardDecl var0, XSWildcardDecl var1) {
      XSWildcardDecl var2 = var0.performIntersectionWith(var1, var0.fProcessContents);
      return var2 == null || var2.fType != 3 || var2.fNamespaceList.length != 0;
   }

   public static boolean overlapUPA(Object var0, Object var1, SubstitutionGroupHandler var2) {
      if (var0 instanceof XSElementDecl) {
         return var1 instanceof XSElementDecl ? overlapUPA((XSElementDecl)var0, (XSElementDecl)var1, var2) : overlapUPA((XSElementDecl)var0, (XSWildcardDecl)var1, var2);
      } else {
         return var1 instanceof XSElementDecl ? overlapUPA((XSElementDecl)var1, (XSWildcardDecl)var0, var2) : overlapUPA((XSWildcardDecl)var0, (XSWildcardDecl)var1);
      }
   }

   static {
      STRING_TYPE = (XSSimpleType)SchemaGrammar.SG_SchemaNS.getGlobalTypeDecl("string");
      fEmptyParticle = null;
      ELEMENT_PARTICLE_COMPARATOR = new Comparator() {
         public int compare(Object var1, Object var2) {
            XSParticleDecl var3 = (XSParticleDecl)var1;
            XSParticleDecl var4 = (XSParticleDecl)var2;
            XSElementDecl var5 = (XSElementDecl)var3.fValue;
            XSElementDecl var6 = (XSElementDecl)var4.fValue;
            String var7 = var5.getNamespace();
            String var8 = var6.getNamespace();
            String var9 = var5.getName();
            String var10 = var6.getName();
            boolean var11 = var7 == var8;
            int var12 = 0;
            if (!var11) {
               if (var7 != null) {
                  if (var8 != null) {
                     var12 = var7.compareTo(var8);
                  } else {
                     var12 = 1;
                  }
               } else {
                  var12 = -1;
               }
            }

            return var12 != 0 ? var12 : var9.compareTo(var10);
         }
      };
   }
}
