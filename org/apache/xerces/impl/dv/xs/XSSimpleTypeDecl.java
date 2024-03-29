package org.apache.xerces.impl.dv.xs;

import java.math.BigInteger;
import java.util.AbstractList;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.xerces.impl.dv.DatatypeException;
import org.apache.xerces.impl.dv.InvalidDatatypeFacetException;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.ValidatedInfo;
import org.apache.xerces.impl.dv.ValidationContext;
import org.apache.xerces.impl.dv.XSFacets;
import org.apache.xerces.impl.dv.XSSimpleType;
import org.apache.xerces.impl.xpath.regex.RegularExpression;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.util.ObjectListImpl;
import org.apache.xerces.impl.xs.util.ShortListImpl;
import org.apache.xerces.impl.xs.util.StringListImpl;
import org.apache.xerces.impl.xs.util.XSObjectListImpl;
import org.apache.xerces.util.XMLChar;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xs.ShortList;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSFacet;
import org.apache.xerces.xs.XSMultiValueFacet;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.datatypes.ObjectList;
import org.w3c.dom.TypeInfo;

public class XSSimpleTypeDecl implements XSSimpleType, TypeInfo {
   protected static final short DV_STRING = 1;
   protected static final short DV_BOOLEAN = 2;
   protected static final short DV_DECIMAL = 3;
   protected static final short DV_FLOAT = 4;
   protected static final short DV_DOUBLE = 5;
   protected static final short DV_DURATION = 6;
   protected static final short DV_DATETIME = 7;
   protected static final short DV_TIME = 8;
   protected static final short DV_DATE = 9;
   protected static final short DV_GYEARMONTH = 10;
   protected static final short DV_GYEAR = 11;
   protected static final short DV_GMONTHDAY = 12;
   protected static final short DV_GDAY = 13;
   protected static final short DV_GMONTH = 14;
   protected static final short DV_HEXBINARY = 15;
   protected static final short DV_BASE64BINARY = 16;
   protected static final short DV_ANYURI = 17;
   protected static final short DV_QNAME = 18;
   protected static final short DV_PRECISIONDECIMAL = 19;
   protected static final short DV_NOTATION = 20;
   protected static final short DV_ANYSIMPLETYPE = 0;
   protected static final short DV_ID = 21;
   protected static final short DV_IDREF = 22;
   protected static final short DV_ENTITY = 23;
   protected static final short DV_INTEGER = 24;
   protected static final short DV_LIST = 25;
   protected static final short DV_UNION = 26;
   protected static final short DV_YEARMONTHDURATION = 27;
   protected static final short DV_DAYTIMEDURATION = 28;
   protected static final short DV_ANYATOMICTYPE = 29;
   private static final TypeValidator[] gDVs = new TypeValidator[]{new AnySimpleDV(), new StringDV(), new BooleanDV(), new DecimalDV(), new FloatDV(), new DoubleDV(), new DurationDV(), new DateTimeDV(), new TimeDV(), new DateDV(), new YearMonthDV(), new YearDV(), new MonthDayDV(), new DayDV(), new MonthDV(), new HexBinaryDV(), new Base64BinaryDV(), new AnyURIDV(), new QNameDV(), new PrecisionDecimalDV(), new QNameDV(), new IDDV(), new IDREFDV(), new EntityDV(), new IntegerDV(), new ListDV(), new UnionDV(), new YearMonthDurationDV(), new DayTimeDurationDV(), new AnyAtomicDV()};
   static final short NORMALIZE_NONE = 0;
   static final short NORMALIZE_TRIM = 1;
   static final short NORMALIZE_FULL = 2;
   static final short[] fDVNormalizeType = new short[]{0, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 0, 1, 1, 0};
   static final short SPECIAL_PATTERN_NONE = 0;
   static final short SPECIAL_PATTERN_NMTOKEN = 1;
   static final short SPECIAL_PATTERN_NAME = 2;
   static final short SPECIAL_PATTERN_NCNAME = 3;
   static final String[] SPECIAL_PATTERN_STRING = new String[]{"NONE", "NMTOKEN", "Name", "NCName"};
   static final String[] WS_FACET_STRING = new String[]{"preserve", "replace", "collapse"};
   static final String URI_SCHEMAFORSCHEMA = "http://www.w3.org/2001/XMLSchema";
   static final String ANY_TYPE = "anyType";
   public static final short YEARMONTHDURATION_DT = 46;
   public static final short DAYTIMEDURATION_DT = 47;
   public static final short PRECISIONDECIMAL_DT = 48;
   public static final short ANYATOMICTYPE_DT = 49;
   static final int DERIVATION_ANY = 0;
   static final int DERIVATION_RESTRICTION = 1;
   static final int DERIVATION_EXTENSION = 2;
   static final int DERIVATION_UNION = 4;
   static final int DERIVATION_LIST = 8;
   static final ValidationContext fEmptyContext = new ValidationContext() {
      public boolean needFacetChecking() {
         return true;
      }

      public boolean needExtraChecking() {
         return false;
      }

      public boolean needToNormalize() {
         return true;
      }

      public boolean useNamespaces() {
         return true;
      }

      public boolean isEntityDeclared(String var1) {
         return false;
      }

      public boolean isEntityUnparsed(String var1) {
         return false;
      }

      public boolean isIdDeclared(String var1) {
         return false;
      }

      public void addId(String var1) {
      }

      public void addIdRef(String var1) {
      }

      public String getSymbol(String var1) {
         return var1.intern();
      }

      public String getURI(String var1) {
         return null;
      }

      public Locale getLocale() {
         return Locale.getDefault();
      }
   };
   private TypeValidator[] fDVs;
   private boolean fIsImmutable;
   private XSSimpleTypeDecl fItemType;
   private XSSimpleTypeDecl[] fMemberTypes;
   private short fBuiltInKind;
   private String fTypeName;
   private String fTargetNamespace;
   private short fFinalSet;
   private XSSimpleTypeDecl fBase;
   private short fVariety;
   private short fValidationDV;
   private short fFacetsDefined;
   private short fFixedFacet;
   private short fWhiteSpace;
   private int fLength;
   private int fMinLength;
   private int fMaxLength;
   private int fTotalDigits;
   private int fFractionDigits;
   private Vector fPattern;
   private Vector fPatternStr;
   private ValidatedInfo[] fEnumeration;
   private int fEnumerationSize;
   private ShortList fEnumerationTypeList;
   private ObjectList fEnumerationItemTypeList;
   private StringList fLexicalPattern;
   private StringList fLexicalEnumeration;
   private ObjectList fActualEnumeration;
   private Object fMaxInclusive;
   private Object fMaxExclusive;
   private Object fMinExclusive;
   private Object fMinInclusive;
   public XSAnnotation lengthAnnotation;
   public XSAnnotation minLengthAnnotation;
   public XSAnnotation maxLengthAnnotation;
   public XSAnnotation whiteSpaceAnnotation;
   public XSAnnotation totalDigitsAnnotation;
   public XSAnnotation fractionDigitsAnnotation;
   public XSObjectListImpl patternAnnotations;
   public XSObjectList enumerationAnnotations;
   public XSAnnotation maxInclusiveAnnotation;
   public XSAnnotation maxExclusiveAnnotation;
   public XSAnnotation minInclusiveAnnotation;
   public XSAnnotation minExclusiveAnnotation;
   private XSObjectListImpl fFacets;
   private XSObjectListImpl fMultiValueFacets;
   private XSObjectList fAnnotations;
   private short fPatternType;
   private short fOrdered;
   private boolean fFinite;
   private boolean fBounded;
   private boolean fNumeric;
   private XSNamespaceItem fNamespaceItem;
   static final XSSimpleTypeDecl fAnySimpleType = new XSSimpleTypeDecl((XSSimpleTypeDecl)null, "anySimpleType", (short)0, (short)0, false, true, false, true, (short)1);
   static final XSSimpleTypeDecl fAnyAtomicType;
   static final ValidationContext fDummyContext;
   private boolean fAnonymous;

   protected static TypeValidator[] getGDVs() {
      return (TypeValidator[])gDVs.clone();
   }

   protected void setDVs(TypeValidator[] var1) {
      this.fDVs = var1;
   }

   public XSSimpleTypeDecl() {
      this.fDVs = gDVs;
      this.fIsImmutable = false;
      this.fFinalSet = 0;
      this.fVariety = -1;
      this.fValidationDV = -1;
      this.fFacetsDefined = 0;
      this.fFixedFacet = 0;
      this.fWhiteSpace = 0;
      this.fLength = -1;
      this.fMinLength = -1;
      this.fMaxLength = -1;
      this.fTotalDigits = -1;
      this.fFractionDigits = -1;
      this.fAnnotations = null;
      this.fPatternType = 0;
      this.fNamespaceItem = null;
      this.fAnonymous = false;
   }

   protected XSSimpleTypeDecl(XSSimpleTypeDecl var1, String var2, short var3, short var4, boolean var5, boolean var6, boolean var7, boolean var8, short var9) {
      this.fDVs = gDVs;
      this.fIsImmutable = false;
      this.fFinalSet = 0;
      this.fVariety = -1;
      this.fValidationDV = -1;
      this.fFacetsDefined = 0;
      this.fFixedFacet = 0;
      this.fWhiteSpace = 0;
      this.fLength = -1;
      this.fMinLength = -1;
      this.fMaxLength = -1;
      this.fTotalDigits = -1;
      this.fFractionDigits = -1;
      this.fAnnotations = null;
      this.fPatternType = 0;
      this.fNamespaceItem = null;
      this.fAnonymous = false;
      this.fIsImmutable = var8;
      this.fBase = var1;
      this.fTypeName = var2;
      this.fTargetNamespace = "http://www.w3.org/2001/XMLSchema";
      this.fVariety = 1;
      this.fValidationDV = var3;
      this.fFacetsDefined = 16;
      if (var3 != 0 && var3 != 29 && var3 != 1) {
         this.fWhiteSpace = 2;
         this.fFixedFacet = 16;
      } else {
         this.fWhiteSpace = 0;
      }

      this.fOrdered = var4;
      this.fBounded = var5;
      this.fFinite = var6;
      this.fNumeric = var7;
      this.fAnnotations = null;
      this.fBuiltInKind = var9;
   }

   protected XSSimpleTypeDecl(XSSimpleTypeDecl var1, String var2, String var3, short var4, boolean var5, XSObjectList var6, short var7) {
      this(var1, var2, var3, var4, var5, var6);
      this.fBuiltInKind = var7;
   }

   protected XSSimpleTypeDecl(XSSimpleTypeDecl var1, String var2, String var3, short var4, boolean var5, XSObjectList var6) {
      this.fDVs = gDVs;
      this.fIsImmutable = false;
      this.fFinalSet = 0;
      this.fVariety = -1;
      this.fValidationDV = -1;
      this.fFacetsDefined = 0;
      this.fFixedFacet = 0;
      this.fWhiteSpace = 0;
      this.fLength = -1;
      this.fMinLength = -1;
      this.fMaxLength = -1;
      this.fTotalDigits = -1;
      this.fFractionDigits = -1;
      this.fAnnotations = null;
      this.fPatternType = 0;
      this.fNamespaceItem = null;
      this.fAnonymous = false;
      this.fBase = var1;
      this.fTypeName = var2;
      this.fTargetNamespace = var3;
      this.fFinalSet = var4;
      this.fAnnotations = var6;
      this.fVariety = this.fBase.fVariety;
      this.fValidationDV = this.fBase.fValidationDV;
      switch (this.fVariety) {
         case 1:
         default:
            break;
         case 2:
            this.fItemType = this.fBase.fItemType;
            break;
         case 3:
            this.fMemberTypes = this.fBase.fMemberTypes;
      }

      this.fLength = this.fBase.fLength;
      this.fMinLength = this.fBase.fMinLength;
      this.fMaxLength = this.fBase.fMaxLength;
      this.fPattern = this.fBase.fPattern;
      this.fPatternStr = this.fBase.fPatternStr;
      this.fEnumeration = this.fBase.fEnumeration;
      this.fEnumerationSize = this.fBase.fEnumerationSize;
      this.fWhiteSpace = this.fBase.fWhiteSpace;
      this.fMaxExclusive = this.fBase.fMaxExclusive;
      this.fMaxInclusive = this.fBase.fMaxInclusive;
      this.fMinExclusive = this.fBase.fMinExclusive;
      this.fMinInclusive = this.fBase.fMinInclusive;
      this.fTotalDigits = this.fBase.fTotalDigits;
      this.fFractionDigits = this.fBase.fFractionDigits;
      this.fPatternType = this.fBase.fPatternType;
      this.fFixedFacet = this.fBase.fFixedFacet;
      this.fFacetsDefined = this.fBase.fFacetsDefined;
      this.lengthAnnotation = this.fBase.lengthAnnotation;
      this.minLengthAnnotation = this.fBase.minLengthAnnotation;
      this.maxLengthAnnotation = this.fBase.maxLengthAnnotation;
      this.patternAnnotations = this.fBase.patternAnnotations;
      this.enumerationAnnotations = this.fBase.enumerationAnnotations;
      this.whiteSpaceAnnotation = this.fBase.whiteSpaceAnnotation;
      this.maxExclusiveAnnotation = this.fBase.maxExclusiveAnnotation;
      this.maxInclusiveAnnotation = this.fBase.maxInclusiveAnnotation;
      this.minExclusiveAnnotation = this.fBase.minExclusiveAnnotation;
      this.minInclusiveAnnotation = this.fBase.minInclusiveAnnotation;
      this.totalDigitsAnnotation = this.fBase.totalDigitsAnnotation;
      this.fractionDigitsAnnotation = this.fBase.fractionDigitsAnnotation;
      this.calcFundamentalFacets();
      this.fIsImmutable = var5;
      this.fBuiltInKind = var1.fBuiltInKind;
   }

   protected XSSimpleTypeDecl(String var1, String var2, short var3, XSSimpleTypeDecl var4, boolean var5, XSObjectList var6) {
      this.fDVs = gDVs;
      this.fIsImmutable = false;
      this.fFinalSet = 0;
      this.fVariety = -1;
      this.fValidationDV = -1;
      this.fFacetsDefined = 0;
      this.fFixedFacet = 0;
      this.fWhiteSpace = 0;
      this.fLength = -1;
      this.fMinLength = -1;
      this.fMaxLength = -1;
      this.fTotalDigits = -1;
      this.fFractionDigits = -1;
      this.fAnnotations = null;
      this.fPatternType = 0;
      this.fNamespaceItem = null;
      this.fAnonymous = false;
      this.fBase = fAnySimpleType;
      this.fTypeName = var1;
      this.fTargetNamespace = var2;
      this.fFinalSet = var3;
      this.fAnnotations = var6;
      this.fVariety = 2;
      this.fItemType = var4;
      this.fValidationDV = 25;
      this.fFacetsDefined = 16;
      this.fFixedFacet = 16;
      this.fWhiteSpace = 2;
      this.calcFundamentalFacets();
      this.fIsImmutable = var5;
      this.fBuiltInKind = 44;
   }

   protected XSSimpleTypeDecl(String var1, String var2, short var3, XSSimpleTypeDecl[] var4, XSObjectList var5) {
      this.fDVs = gDVs;
      this.fIsImmutable = false;
      this.fFinalSet = 0;
      this.fVariety = -1;
      this.fValidationDV = -1;
      this.fFacetsDefined = 0;
      this.fFixedFacet = 0;
      this.fWhiteSpace = 0;
      this.fLength = -1;
      this.fMinLength = -1;
      this.fMaxLength = -1;
      this.fTotalDigits = -1;
      this.fFractionDigits = -1;
      this.fAnnotations = null;
      this.fPatternType = 0;
      this.fNamespaceItem = null;
      this.fAnonymous = false;
      this.fBase = fAnySimpleType;
      this.fTypeName = var1;
      this.fTargetNamespace = var2;
      this.fFinalSet = var3;
      this.fAnnotations = var5;
      this.fVariety = 3;
      this.fMemberTypes = var4;
      this.fValidationDV = 26;
      this.fFacetsDefined = 16;
      this.fWhiteSpace = 2;
      this.calcFundamentalFacets();
      this.fIsImmutable = false;
      this.fBuiltInKind = 45;
   }

   protected XSSimpleTypeDecl setRestrictionValues(XSSimpleTypeDecl var1, String var2, String var3, short var4, XSObjectList var5) {
      if (this.fIsImmutable) {
         return null;
      } else {
         this.fBase = var1;
         this.fAnonymous = false;
         this.fTypeName = var2;
         this.fTargetNamespace = var3;
         this.fFinalSet = var4;
         this.fAnnotations = var5;
         this.fVariety = this.fBase.fVariety;
         this.fValidationDV = this.fBase.fValidationDV;
         switch (this.fVariety) {
            case 1:
            default:
               break;
            case 2:
               this.fItemType = this.fBase.fItemType;
               break;
            case 3:
               this.fMemberTypes = this.fBase.fMemberTypes;
         }

         this.fLength = this.fBase.fLength;
         this.fMinLength = this.fBase.fMinLength;
         this.fMaxLength = this.fBase.fMaxLength;
         this.fPattern = this.fBase.fPattern;
         this.fPatternStr = this.fBase.fPatternStr;
         this.fEnumeration = this.fBase.fEnumeration;
         this.fEnumerationSize = this.fBase.fEnumerationSize;
         this.fWhiteSpace = this.fBase.fWhiteSpace;
         this.fMaxExclusive = this.fBase.fMaxExclusive;
         this.fMaxInclusive = this.fBase.fMaxInclusive;
         this.fMinExclusive = this.fBase.fMinExclusive;
         this.fMinInclusive = this.fBase.fMinInclusive;
         this.fTotalDigits = this.fBase.fTotalDigits;
         this.fFractionDigits = this.fBase.fFractionDigits;
         this.fPatternType = this.fBase.fPatternType;
         this.fFixedFacet = this.fBase.fFixedFacet;
         this.fFacetsDefined = this.fBase.fFacetsDefined;
         this.calcFundamentalFacets();
         this.fBuiltInKind = var1.fBuiltInKind;
         return this;
      }
   }

   protected XSSimpleTypeDecl setListValues(String var1, String var2, short var3, XSSimpleTypeDecl var4, XSObjectList var5) {
      if (this.fIsImmutable) {
         return null;
      } else {
         this.fBase = fAnySimpleType;
         this.fAnonymous = false;
         this.fTypeName = var1;
         this.fTargetNamespace = var2;
         this.fFinalSet = var3;
         this.fAnnotations = var5;
         this.fVariety = 2;
         this.fItemType = var4;
         this.fValidationDV = 25;
         this.fFacetsDefined = 16;
         this.fFixedFacet = 16;
         this.fWhiteSpace = 2;
         this.calcFundamentalFacets();
         this.fBuiltInKind = 44;
         return this;
      }
   }

   protected XSSimpleTypeDecl setUnionValues(String var1, String var2, short var3, XSSimpleTypeDecl[] var4, XSObjectList var5) {
      if (this.fIsImmutable) {
         return null;
      } else {
         this.fBase = fAnySimpleType;
         this.fAnonymous = false;
         this.fTypeName = var1;
         this.fTargetNamespace = var2;
         this.fFinalSet = var3;
         this.fAnnotations = var5;
         this.fVariety = 3;
         this.fMemberTypes = var4;
         this.fValidationDV = 26;
         this.fFacetsDefined = 16;
         this.fWhiteSpace = 2;
         this.calcFundamentalFacets();
         this.fBuiltInKind = 45;
         return this;
      }
   }

   public short getType() {
      return 3;
   }

   public short getTypeCategory() {
      return 16;
   }

   public String getName() {
      return this.getAnonymous() ? null : this.fTypeName;
   }

   public String getTypeName() {
      return this.fTypeName;
   }

   public String getNamespace() {
      return this.fTargetNamespace;
   }

   public short getFinal() {
      return this.fFinalSet;
   }

   public boolean isFinal(short var1) {
      return (this.fFinalSet & var1) != 0;
   }

   public XSTypeDefinition getBaseType() {
      return this.fBase;
   }

   public boolean getAnonymous() {
      return this.fAnonymous || this.fTypeName == null;
   }

   public short getVariety() {
      return this.fValidationDV == 0 ? 0 : this.fVariety;
   }

   public boolean isIDType() {
      switch (this.fVariety) {
         case 1:
            return this.fValidationDV == 21;
         case 2:
            return this.fItemType.isIDType();
         case 3:
            for(int var1 = 0; var1 < this.fMemberTypes.length; ++var1) {
               if (this.fMemberTypes[var1].isIDType()) {
                  return true;
               }
            }
         default:
            return false;
      }
   }

   public short getWhitespace() throws DatatypeException {
      if (this.fVariety == 3) {
         throw new DatatypeException("dt-whitespace", new Object[]{this.fTypeName});
      } else {
         return this.fWhiteSpace;
      }
   }

   public short getPrimitiveKind() {
      if (this.fVariety == 1 && this.fValidationDV != 0) {
         if (this.fValidationDV != 21 && this.fValidationDV != 22 && this.fValidationDV != 23) {
            return this.fValidationDV == 24 ? 3 : this.fValidationDV;
         } else {
            return 1;
         }
      } else {
         return 0;
      }
   }

   public short getBuiltInKind() {
      return this.fBuiltInKind;
   }

   public XSSimpleTypeDefinition getPrimitiveType() {
      if (this.fVariety == 1 && this.fValidationDV != 0) {
         XSSimpleTypeDecl var1;
         for(var1 = this; var1.fBase != fAnySimpleType; var1 = var1.fBase) {
         }

         return var1;
      } else {
         return null;
      }
   }

   public XSSimpleTypeDefinition getItemType() {
      return this.fVariety == 2 ? this.fItemType : null;
   }

   public XSObjectList getMemberTypes() {
      return this.fVariety == 3 ? new XSObjectListImpl(this.fMemberTypes, this.fMemberTypes.length) : XSObjectListImpl.EMPTY_LIST;
   }

   public void applyFacets(XSFacets var1, short var2, short var3, ValidationContext var4) throws InvalidDatatypeFacetException {
      if (var4 == null) {
         var4 = fEmptyContext;
      }

      this.applyFacets(var1, var2, var3, (short)0, var4);
   }

   void applyFacets1(XSFacets var1, short var2, short var3) {
      try {
         this.applyFacets(var1, var2, var3, (short)0, fDummyContext);
      } catch (InvalidDatatypeFacetException var5) {
         throw new RuntimeException("internal error");
      }

      this.fIsImmutable = true;
   }

   void applyFacets1(XSFacets var1, short var2, short var3, short var4) {
      try {
         this.applyFacets(var1, var2, var3, var4, fDummyContext);
      } catch (InvalidDatatypeFacetException var6) {
         throw new RuntimeException("internal error");
      }

      this.fIsImmutable = true;
   }

   void applyFacets(XSFacets var1, short var2, short var3, short var4, ValidationContext var5) throws InvalidDatatypeFacetException {
      if (!this.fIsImmutable) {
         ValidatedInfo var6 = new ValidatedInfo();
         this.fFacetsDefined = 0;
         this.fFixedFacet = 0;
         boolean var7 = false;
         short var8 = this.fDVs[this.fValidationDV].getAllowedFacets();
         if ((var2 & 1) != 0) {
            if ((var8 & 1) == 0) {
               this.reportError("cos-applicable-facets", new Object[]{"length", this.fTypeName});
            } else {
               this.fLength = var1.length;
               this.lengthAnnotation = var1.lengthAnnotation;
               this.fFacetsDefined = (short)(this.fFacetsDefined | 1);
               if ((var3 & 1) != 0) {
                  this.fFixedFacet = (short)(this.fFixedFacet | 1);
               }
            }
         }

         if ((var2 & 2) != 0) {
            if ((var8 & 2) == 0) {
               this.reportError("cos-applicable-facets", new Object[]{"minLength", this.fTypeName});
            } else {
               this.fMinLength = var1.minLength;
               this.minLengthAnnotation = var1.minLengthAnnotation;
               this.fFacetsDefined = (short)(this.fFacetsDefined | 2);
               if ((var3 & 2) != 0) {
                  this.fFixedFacet = (short)(this.fFixedFacet | 2);
               }
            }
         }

         if ((var2 & 4) != 0) {
            if ((var8 & 4) == 0) {
               this.reportError("cos-applicable-facets", new Object[]{"maxLength", this.fTypeName});
            } else {
               this.fMaxLength = var1.maxLength;
               this.maxLengthAnnotation = var1.maxLengthAnnotation;
               this.fFacetsDefined = (short)(this.fFacetsDefined | 4);
               if ((var3 & 4) != 0) {
                  this.fFixedFacet = (short)(this.fFixedFacet | 4);
               }
            }
         }

         if ((var2 & 8) != 0) {
            if ((var8 & 8) == 0) {
               this.reportError("cos-applicable-facets", new Object[]{"pattern", this.fTypeName});
            } else {
               this.patternAnnotations = var1.patternAnnotations;
               RegularExpression var9 = null;

               try {
                  var9 = new RegularExpression(var1.pattern, "X", var5.getLocale());
               } catch (Exception var24) {
                  this.reportError("InvalidRegex", new Object[]{var1.pattern, var24.getLocalizedMessage()});
               }

               if (var9 != null) {
                  this.fPattern = new Vector();
                  this.fPattern.addElement(var9);
                  this.fPatternStr = new Vector();
                  this.fPatternStr.addElement(var1.pattern);
                  this.fFacetsDefined = (short)(this.fFacetsDefined | 8);
                  if ((var3 & 8) != 0) {
                     this.fFixedFacet = (short)(this.fFixedFacet | 8);
                  }
               }
            }
         }

         if ((var2 & 16) != 0) {
            if ((var8 & 16) == 0) {
               this.reportError("cos-applicable-facets", new Object[]{"whiteSpace", this.fTypeName});
            } else {
               this.fWhiteSpace = var1.whiteSpace;
               this.whiteSpaceAnnotation = var1.whiteSpaceAnnotation;
               this.fFacetsDefined = (short)(this.fFacetsDefined | 16);
               if ((var3 & 16) != 0) {
                  this.fFixedFacet = (short)(this.fFixedFacet | 16);
               }
            }
         }

         int var10;
         if ((var2 & 2048) != 0) {
            if ((var8 & 2048) == 0) {
               this.reportError("cos-applicable-facets", new Object[]{"enumeration", this.fTypeName});
            } else {
               Vector var26 = var1.enumeration;
               var10 = var26.size();
               this.fEnumeration = new ValidatedInfo[var10];
               Vector var11 = var1.enumNSDecls;
               ValidationContextImpl var12 = new ValidationContextImpl(var5);
               this.enumerationAnnotations = var1.enumAnnotations;
               this.fEnumerationSize = 0;

               for(int var13 = 0; var13 < var10; ++var13) {
                  if (var11 != null) {
                     var12.setNSContext((NamespaceContext)var11.elementAt(var13));
                  }

                  try {
                     ValidatedInfo var14 = this.getActualEnumValue((String)var26.elementAt(var13), var12, (ValidatedInfo)null);
                     this.fEnumeration[this.fEnumerationSize++] = var14;
                  } catch (InvalidDatatypeValueException var23) {
                     this.reportError("enumeration-valid-restriction", new Object[]{var26.elementAt(var13), this.getBaseType().getName()});
                  }
               }

               this.fFacetsDefined = (short)(this.fFacetsDefined | 2048);
               if ((var3 & 2048) != 0) {
                  this.fFixedFacet = (short)(this.fFixedFacet | 2048);
               }
            }
         }

         if ((var2 & 32) != 0) {
            if ((var8 & 32) == 0) {
               this.reportError("cos-applicable-facets", new Object[]{"maxInclusive", this.fTypeName});
            } else {
               this.maxInclusiveAnnotation = var1.maxInclusiveAnnotation;

               try {
                  this.fMaxInclusive = this.fBase.getActualValue(var1.maxInclusive, var5, var6, true);
                  this.fFacetsDefined = (short)(this.fFacetsDefined | 32);
                  if ((var3 & 32) != 0) {
                     this.fFixedFacet = (short)(this.fFixedFacet | 32);
                  }
               } catch (InvalidDatatypeValueException var22) {
                  this.reportError(var22.getKey(), var22.getArgs());
                  this.reportError("FacetValueFromBase", new Object[]{this.fTypeName, var1.maxInclusive, "maxInclusive", this.fBase.getName()});
               }

               if ((this.fBase.fFacetsDefined & 32) != 0 && (this.fBase.fFixedFacet & 32) != 0 && this.fDVs[this.fValidationDV].compare(this.fMaxInclusive, this.fBase.fMaxInclusive) != 0) {
                  this.reportError("FixedFacetValue", new Object[]{"maxInclusive", this.fMaxInclusive, this.fBase.fMaxInclusive, this.fTypeName});
               }

               try {
                  this.fBase.validate(var5, var6);
               } catch (InvalidDatatypeValueException var21) {
                  this.reportError(var21.getKey(), var21.getArgs());
                  this.reportError("FacetValueFromBase", new Object[]{this.fTypeName, var1.maxInclusive, "maxInclusive", this.fBase.getName()});
               }
            }
         }

         boolean var27 = true;
         int var25;
         if ((var2 & 64) != 0) {
            if ((var8 & 64) == 0) {
               this.reportError("cos-applicable-facets", new Object[]{"maxExclusive", this.fTypeName});
            } else {
               this.maxExclusiveAnnotation = var1.maxExclusiveAnnotation;

               try {
                  this.fMaxExclusive = this.fBase.getActualValue(var1.maxExclusive, var5, var6, true);
                  this.fFacetsDefined = (short)(this.fFacetsDefined | 64);
                  if ((var3 & 64) != 0) {
                     this.fFixedFacet = (short)(this.fFixedFacet | 64);
                  }
               } catch (InvalidDatatypeValueException var20) {
                  this.reportError(var20.getKey(), var20.getArgs());
                  this.reportError("FacetValueFromBase", new Object[]{this.fTypeName, var1.maxExclusive, "maxExclusive", this.fBase.getName()});
               }

               if ((this.fBase.fFacetsDefined & 64) != 0) {
                  var25 = this.fDVs[this.fValidationDV].compare(this.fMaxExclusive, this.fBase.fMaxExclusive);
                  if ((this.fBase.fFixedFacet & 64) != 0 && var25 != 0) {
                     this.reportError("FixedFacetValue", new Object[]{"maxExclusive", var1.maxExclusive, this.fBase.fMaxExclusive, this.fTypeName});
                  }

                  if (var25 == 0) {
                     var27 = false;
                  }
               }

               if (var27) {
                  try {
                     this.fBase.validate(var5, var6);
                  } catch (InvalidDatatypeValueException var19) {
                     this.reportError(var19.getKey(), var19.getArgs());
                     this.reportError("FacetValueFromBase", new Object[]{this.fTypeName, var1.maxExclusive, "maxExclusive", this.fBase.getName()});
                  }
               } else if ((this.fBase.fFacetsDefined & 32) != 0 && this.fDVs[this.fValidationDV].compare(this.fMaxExclusive, this.fBase.fMaxInclusive) > 0) {
                  this.reportError("maxExclusive-valid-restriction.2", new Object[]{var1.maxExclusive, this.fBase.fMaxInclusive});
               }
            }
         }

         var27 = true;
         if ((var2 & 128) != 0) {
            if ((var8 & 128) == 0) {
               this.reportError("cos-applicable-facets", new Object[]{"minExclusive", this.fTypeName});
            } else {
               this.minExclusiveAnnotation = var1.minExclusiveAnnotation;

               try {
                  this.fMinExclusive = this.fBase.getActualValue(var1.minExclusive, var5, var6, true);
                  this.fFacetsDefined = (short)(this.fFacetsDefined | 128);
                  if ((var3 & 128) != 0) {
                     this.fFixedFacet = (short)(this.fFixedFacet | 128);
                  }
               } catch (InvalidDatatypeValueException var18) {
                  this.reportError(var18.getKey(), var18.getArgs());
                  this.reportError("FacetValueFromBase", new Object[]{this.fTypeName, var1.minExclusive, "minExclusive", this.fBase.getName()});
               }

               if ((this.fBase.fFacetsDefined & 128) != 0) {
                  var25 = this.fDVs[this.fValidationDV].compare(this.fMinExclusive, this.fBase.fMinExclusive);
                  if ((this.fBase.fFixedFacet & 128) != 0 && var25 != 0) {
                     this.reportError("FixedFacetValue", new Object[]{"minExclusive", var1.minExclusive, this.fBase.fMinExclusive, this.fTypeName});
                  }

                  if (var25 == 0) {
                     var27 = false;
                  }
               }

               if (var27) {
                  try {
                     this.fBase.validate(var5, var6);
                  } catch (InvalidDatatypeValueException var17) {
                     this.reportError(var17.getKey(), var17.getArgs());
                     this.reportError("FacetValueFromBase", new Object[]{this.fTypeName, var1.minExclusive, "minExclusive", this.fBase.getName()});
                  }
               } else if ((this.fBase.fFacetsDefined & 256) != 0 && this.fDVs[this.fValidationDV].compare(this.fMinExclusive, this.fBase.fMinInclusive) < 0) {
                  this.reportError("minExclusive-valid-restriction.3", new Object[]{var1.minExclusive, this.fBase.fMinInclusive});
               }
            }
         }

         if ((var2 & 256) != 0) {
            if ((var8 & 256) == 0) {
               this.reportError("cos-applicable-facets", new Object[]{"minInclusive", this.fTypeName});
            } else {
               this.minInclusiveAnnotation = var1.minInclusiveAnnotation;

               try {
                  this.fMinInclusive = this.fBase.getActualValue(var1.minInclusive, var5, var6, true);
                  this.fFacetsDefined = (short)(this.fFacetsDefined | 256);
                  if ((var3 & 256) != 0) {
                     this.fFixedFacet = (short)(this.fFixedFacet | 256);
                  }
               } catch (InvalidDatatypeValueException var16) {
                  this.reportError(var16.getKey(), var16.getArgs());
                  this.reportError("FacetValueFromBase", new Object[]{this.fTypeName, var1.minInclusive, "minInclusive", this.fBase.getName()});
               }

               if ((this.fBase.fFacetsDefined & 256) != 0 && (this.fBase.fFixedFacet & 256) != 0 && this.fDVs[this.fValidationDV].compare(this.fMinInclusive, this.fBase.fMinInclusive) != 0) {
                  this.reportError("FixedFacetValue", new Object[]{"minInclusive", var1.minInclusive, this.fBase.fMinInclusive, this.fTypeName});
               }

               try {
                  this.fBase.validate(var5, var6);
               } catch (InvalidDatatypeValueException var15) {
                  this.reportError(var15.getKey(), var15.getArgs());
                  this.reportError("FacetValueFromBase", new Object[]{this.fTypeName, var1.minInclusive, "minInclusive", this.fBase.getName()});
               }
            }
         }

         if ((var2 & 512) != 0) {
            if ((var8 & 512) == 0) {
               this.reportError("cos-applicable-facets", new Object[]{"totalDigits", this.fTypeName});
            } else {
               this.totalDigitsAnnotation = var1.totalDigitsAnnotation;
               this.fTotalDigits = var1.totalDigits;
               this.fFacetsDefined = (short)(this.fFacetsDefined | 512);
               if ((var3 & 512) != 0) {
                  this.fFixedFacet = (short)(this.fFixedFacet | 512);
               }
            }
         }

         if ((var2 & 1024) != 0) {
            if ((var8 & 1024) == 0) {
               this.reportError("cos-applicable-facets", new Object[]{"fractionDigits", this.fTypeName});
            } else {
               this.fFractionDigits = var1.fractionDigits;
               this.fractionDigitsAnnotation = var1.fractionDigitsAnnotation;
               this.fFacetsDefined = (short)(this.fFacetsDefined | 1024);
               if ((var3 & 1024) != 0) {
                  this.fFixedFacet = (short)(this.fFixedFacet | 1024);
               }
            }
         }

         if (var4 != 0) {
            this.fPatternType = var4;
         }

         if (this.fFacetsDefined != 0) {
            if ((this.fFacetsDefined & 2) != 0 && (this.fFacetsDefined & 4) != 0 && this.fMinLength > this.fMaxLength) {
               this.reportError("minLength-less-than-equal-to-maxLength", new Object[]{Integer.toString(this.fMinLength), Integer.toString(this.fMaxLength), this.fTypeName});
            }

            if ((this.fFacetsDefined & 64) != 0 && (this.fFacetsDefined & 32) != 0) {
               this.reportError("maxInclusive-maxExclusive", new Object[]{this.fMaxInclusive, this.fMaxExclusive, this.fTypeName});
            }

            if ((this.fFacetsDefined & 128) != 0 && (this.fFacetsDefined & 256) != 0) {
               this.reportError("minInclusive-minExclusive", new Object[]{this.fMinInclusive, this.fMinExclusive, this.fTypeName});
            }

            if ((this.fFacetsDefined & 32) != 0 && (this.fFacetsDefined & 256) != 0) {
               var25 = this.fDVs[this.fValidationDV].compare(this.fMinInclusive, this.fMaxInclusive);
               if (var25 != -1 && var25 != 0) {
                  this.reportError("minInclusive-less-than-equal-to-maxInclusive", new Object[]{this.fMinInclusive, this.fMaxInclusive, this.fTypeName});
               }
            }

            if ((this.fFacetsDefined & 64) != 0 && (this.fFacetsDefined & 128) != 0) {
               var25 = this.fDVs[this.fValidationDV].compare(this.fMinExclusive, this.fMaxExclusive);
               if (var25 != -1 && var25 != 0) {
                  this.reportError("minExclusive-less-than-equal-to-maxExclusive", new Object[]{this.fMinExclusive, this.fMaxExclusive, this.fTypeName});
               }
            }

            if ((this.fFacetsDefined & 32) != 0 && (this.fFacetsDefined & 128) != 0 && this.fDVs[this.fValidationDV].compare(this.fMinExclusive, this.fMaxInclusive) != -1) {
               this.reportError("minExclusive-less-than-maxInclusive", new Object[]{this.fMinExclusive, this.fMaxInclusive, this.fTypeName});
            }

            if ((this.fFacetsDefined & 64) != 0 && (this.fFacetsDefined & 256) != 0 && this.fDVs[this.fValidationDV].compare(this.fMinInclusive, this.fMaxExclusive) != -1) {
               this.reportError("minInclusive-less-than-maxExclusive", new Object[]{this.fMinInclusive, this.fMaxExclusive, this.fTypeName});
            }

            if ((this.fFacetsDefined & 1024) != 0 && (this.fFacetsDefined & 512) != 0 && this.fFractionDigits > this.fTotalDigits) {
               this.reportError("fractionDigits-totalDigits", new Object[]{Integer.toString(this.fFractionDigits), Integer.toString(this.fTotalDigits), this.fTypeName});
            }

            if ((this.fFacetsDefined & 1) != 0) {
               if ((this.fBase.fFacetsDefined & 2) != 0 && this.fLength < this.fBase.fMinLength) {
                  this.reportError("length-minLength-maxLength.1.1", new Object[]{this.fTypeName, Integer.toString(this.fLength), Integer.toString(this.fBase.fMinLength)});
               }

               if ((this.fBase.fFacetsDefined & 4) != 0 && this.fLength > this.fBase.fMaxLength) {
                  this.reportError("length-minLength-maxLength.2.1", new Object[]{this.fTypeName, Integer.toString(this.fLength), Integer.toString(this.fBase.fMaxLength)});
               }

               if ((this.fBase.fFacetsDefined & 1) != 0 && this.fLength != this.fBase.fLength) {
                  this.reportError("length-valid-restriction", new Object[]{Integer.toString(this.fLength), Integer.toString(this.fBase.fLength), this.fTypeName});
               }
            }

            if ((this.fBase.fFacetsDefined & 1) != 0 || (this.fFacetsDefined & 1) != 0) {
               if ((this.fFacetsDefined & 2) != 0) {
                  if (this.fBase.fLength < this.fMinLength) {
                     this.reportError("length-minLength-maxLength.1.1", new Object[]{this.fTypeName, Integer.toString(this.fBase.fLength), Integer.toString(this.fMinLength)});
                  }

                  if ((this.fBase.fFacetsDefined & 2) == 0) {
                     this.reportError("length-minLength-maxLength.1.2.a", new Object[]{this.fTypeName});
                  }

                  if (this.fMinLength != this.fBase.fMinLength) {
                     this.reportError("length-minLength-maxLength.1.2.b", new Object[]{this.fTypeName, Integer.toString(this.fMinLength), Integer.toString(this.fBase.fMinLength)});
                  }
               }

               if ((this.fFacetsDefined & 4) != 0) {
                  if (this.fBase.fLength > this.fMaxLength) {
                     this.reportError("length-minLength-maxLength.2.1", new Object[]{this.fTypeName, Integer.toString(this.fBase.fLength), Integer.toString(this.fMaxLength)});
                  }

                  if ((this.fBase.fFacetsDefined & 4) == 0) {
                     this.reportError("length-minLength-maxLength.2.2.a", new Object[]{this.fTypeName});
                  }

                  if (this.fMaxLength != this.fBase.fMaxLength) {
                     this.reportError("length-minLength-maxLength.2.2.b", new Object[]{this.fTypeName, Integer.toString(this.fMaxLength), Integer.toString(this.fBase.fBase.fMaxLength)});
                  }
               }
            }

            if ((this.fFacetsDefined & 2) != 0) {
               if ((this.fBase.fFacetsDefined & 4) != 0) {
                  if (this.fMinLength > this.fBase.fMaxLength) {
                     this.reportError("minLength-less-than-equal-to-maxLength", new Object[]{Integer.toString(this.fMinLength), Integer.toString(this.fBase.fMaxLength), this.fTypeName});
                  }
               } else if ((this.fBase.fFacetsDefined & 2) != 0) {
                  if ((this.fBase.fFixedFacet & 2) != 0 && this.fMinLength != this.fBase.fMinLength) {
                     this.reportError("FixedFacetValue", new Object[]{"minLength", Integer.toString(this.fMinLength), Integer.toString(this.fBase.fMinLength), this.fTypeName});
                  }

                  if (this.fMinLength < this.fBase.fMinLength) {
                     this.reportError("minLength-valid-restriction", new Object[]{Integer.toString(this.fMinLength), Integer.toString(this.fBase.fMinLength), this.fTypeName});
                  }
               }
            }

            if ((this.fFacetsDefined & 4) != 0 && (this.fBase.fFacetsDefined & 2) != 0 && this.fMaxLength < this.fBase.fMinLength) {
               this.reportError("minLength-less-than-equal-to-maxLength", new Object[]{Integer.toString(this.fBase.fMinLength), Integer.toString(this.fMaxLength)});
            }

            if ((this.fFacetsDefined & 4) != 0 && (this.fBase.fFacetsDefined & 4) != 0) {
               if ((this.fBase.fFixedFacet & 4) != 0 && this.fMaxLength != this.fBase.fMaxLength) {
                  this.reportError("FixedFacetValue", new Object[]{"maxLength", Integer.toString(this.fMaxLength), Integer.toString(this.fBase.fMaxLength), this.fTypeName});
               }

               if (this.fMaxLength > this.fBase.fMaxLength) {
                  this.reportError("maxLength-valid-restriction", new Object[]{Integer.toString(this.fMaxLength), Integer.toString(this.fBase.fMaxLength), this.fTypeName});
               }
            }

            if ((this.fFacetsDefined & 512) != 0 && (this.fBase.fFacetsDefined & 512) != 0) {
               if ((this.fBase.fFixedFacet & 512) != 0 && this.fTotalDigits != this.fBase.fTotalDigits) {
                  this.reportError("FixedFacetValue", new Object[]{"totalDigits", Integer.toString(this.fTotalDigits), Integer.toString(this.fBase.fTotalDigits), this.fTypeName});
               }

               if (this.fTotalDigits > this.fBase.fTotalDigits) {
                  this.reportError("totalDigits-valid-restriction", new Object[]{Integer.toString(this.fTotalDigits), Integer.toString(this.fBase.fTotalDigits), this.fTypeName});
               }
            }

            if ((this.fFacetsDefined & 1024) != 0 && (this.fBase.fFacetsDefined & 512) != 0 && this.fFractionDigits > this.fBase.fTotalDigits) {
               this.reportError("fractionDigits-totalDigits", new Object[]{Integer.toString(this.fFractionDigits), Integer.toString(this.fTotalDigits), this.fTypeName});
            }

            if ((this.fFacetsDefined & 1024) != 0) {
               if ((this.fBase.fFacetsDefined & 1024) == 0) {
                  if (this.fValidationDV == 24 && this.fFractionDigits != 0) {
                     this.reportError("FixedFacetValue", new Object[]{"fractionDigits", Integer.toString(this.fFractionDigits), "0", this.fTypeName});
                  }
               } else {
                  if ((this.fBase.fFixedFacet & 1024) != 0 && this.fFractionDigits != this.fBase.fFractionDigits || this.fValidationDV == 24 && this.fFractionDigits != 0) {
                     this.reportError("FixedFacetValue", new Object[]{"fractionDigits", Integer.toString(this.fFractionDigits), Integer.toString(this.fBase.fFractionDigits), this.fTypeName});
                  }

                  if (this.fFractionDigits > this.fBase.fFractionDigits) {
                     this.reportError("fractionDigits-valid-restriction", new Object[]{Integer.toString(this.fFractionDigits), Integer.toString(this.fBase.fFractionDigits), this.fTypeName});
                  }
               }
            }

            if ((this.fFacetsDefined & 16) != 0 && (this.fBase.fFacetsDefined & 16) != 0) {
               if ((this.fBase.fFixedFacet & 16) != 0 && this.fWhiteSpace != this.fBase.fWhiteSpace) {
                  this.reportError("FixedFacetValue", new Object[]{"whiteSpace", this.whiteSpaceValue(this.fWhiteSpace), this.whiteSpaceValue(this.fBase.fWhiteSpace), this.fTypeName});
               }

               if (this.fWhiteSpace == 0 && this.fBase.fWhiteSpace == 2) {
                  this.reportError("whiteSpace-valid-restriction.1", new Object[]{this.fTypeName, "preserve"});
               }

               if (this.fWhiteSpace == 1 && this.fBase.fWhiteSpace == 2) {
                  this.reportError("whiteSpace-valid-restriction.1", new Object[]{this.fTypeName, "replace"});
               }

               if (this.fWhiteSpace == 0 && this.fBase.fWhiteSpace == 1) {
                  this.reportError("whiteSpace-valid-restriction.2", new Object[]{this.fTypeName});
               }
            }
         }

         if ((this.fFacetsDefined & 1) == 0 && (this.fBase.fFacetsDefined & 1) != 0) {
            this.fFacetsDefined = (short)(this.fFacetsDefined | 1);
            this.fLength = this.fBase.fLength;
            this.lengthAnnotation = this.fBase.lengthAnnotation;
         }

         if ((this.fFacetsDefined & 2) == 0 && (this.fBase.fFacetsDefined & 2) != 0) {
            this.fFacetsDefined = (short)(this.fFacetsDefined | 2);
            this.fMinLength = this.fBase.fMinLength;
            this.minLengthAnnotation = this.fBase.minLengthAnnotation;
         }

         if ((this.fFacetsDefined & 4) == 0 && (this.fBase.fFacetsDefined & 4) != 0) {
            this.fFacetsDefined = (short)(this.fFacetsDefined | 4);
            this.fMaxLength = this.fBase.fMaxLength;
            this.maxLengthAnnotation = this.fBase.maxLengthAnnotation;
         }

         if ((this.fBase.fFacetsDefined & 8) != 0) {
            if ((this.fFacetsDefined & 8) == 0) {
               this.fFacetsDefined = (short)(this.fFacetsDefined | 8);
               this.fPattern = this.fBase.fPattern;
               this.fPatternStr = this.fBase.fPatternStr;
               this.patternAnnotations = this.fBase.patternAnnotations;
            } else {
               for(var10 = this.fBase.fPattern.size() - 1; var10 >= 0; --var10) {
                  this.fPattern.addElement(this.fBase.fPattern.elementAt(var10));
                  this.fPatternStr.addElement(this.fBase.fPatternStr.elementAt(var10));
               }

               if (this.fBase.patternAnnotations != null) {
                  if (this.patternAnnotations != null) {
                     for(int var28 = this.fBase.patternAnnotations.getLength() - 1; var28 >= 0; --var28) {
                        this.patternAnnotations.addXSObject(this.fBase.patternAnnotations.item(var28));
                     }
                  } else {
                     this.patternAnnotations = this.fBase.patternAnnotations;
                  }
               }
            }
         }

         if ((this.fFacetsDefined & 16) == 0 && (this.fBase.fFacetsDefined & 16) != 0) {
            this.fFacetsDefined = (short)(this.fFacetsDefined | 16);
            this.fWhiteSpace = this.fBase.fWhiteSpace;
            this.whiteSpaceAnnotation = this.fBase.whiteSpaceAnnotation;
         }

         if ((this.fFacetsDefined & 2048) == 0 && (this.fBase.fFacetsDefined & 2048) != 0) {
            this.fFacetsDefined = (short)(this.fFacetsDefined | 2048);
            this.fEnumeration = this.fBase.fEnumeration;
            this.fEnumerationSize = this.fBase.fEnumerationSize;
            this.enumerationAnnotations = this.fBase.enumerationAnnotations;
         }

         if ((this.fBase.fFacetsDefined & 64) != 0 && (this.fFacetsDefined & 64) == 0 && (this.fFacetsDefined & 32) == 0) {
            this.fFacetsDefined = (short)(this.fFacetsDefined | 64);
            this.fMaxExclusive = this.fBase.fMaxExclusive;
            this.maxExclusiveAnnotation = this.fBase.maxExclusiveAnnotation;
         }

         if ((this.fBase.fFacetsDefined & 32) != 0 && (this.fFacetsDefined & 64) == 0 && (this.fFacetsDefined & 32) == 0) {
            this.fFacetsDefined = (short)(this.fFacetsDefined | 32);
            this.fMaxInclusive = this.fBase.fMaxInclusive;
            this.maxInclusiveAnnotation = this.fBase.maxInclusiveAnnotation;
         }

         if ((this.fBase.fFacetsDefined & 128) != 0 && (this.fFacetsDefined & 128) == 0 && (this.fFacetsDefined & 256) == 0) {
            this.fFacetsDefined = (short)(this.fFacetsDefined | 128);
            this.fMinExclusive = this.fBase.fMinExclusive;
            this.minExclusiveAnnotation = this.fBase.minExclusiveAnnotation;
         }

         if ((this.fBase.fFacetsDefined & 256) != 0 && (this.fFacetsDefined & 128) == 0 && (this.fFacetsDefined & 256) == 0) {
            this.fFacetsDefined = (short)(this.fFacetsDefined | 256);
            this.fMinInclusive = this.fBase.fMinInclusive;
            this.minInclusiveAnnotation = this.fBase.minInclusiveAnnotation;
         }

         if ((this.fBase.fFacetsDefined & 512) != 0 && (this.fFacetsDefined & 512) == 0) {
            this.fFacetsDefined = (short)(this.fFacetsDefined | 512);
            this.fTotalDigits = this.fBase.fTotalDigits;
            this.totalDigitsAnnotation = this.fBase.totalDigitsAnnotation;
         }

         if ((this.fBase.fFacetsDefined & 1024) != 0 && (this.fFacetsDefined & 1024) == 0) {
            this.fFacetsDefined = (short)(this.fFacetsDefined | 1024);
            this.fFractionDigits = this.fBase.fFractionDigits;
            this.fractionDigitsAnnotation = this.fBase.fractionDigitsAnnotation;
         }

         if (this.fPatternType == 0 && this.fBase.fPatternType != 0) {
            this.fPatternType = this.fBase.fPatternType;
         }

         this.fFixedFacet |= this.fBase.fFixedFacet;
         this.calcFundamentalFacets();
      }
   }

   public Object validate(String var1, ValidationContext var2, ValidatedInfo var3) throws InvalidDatatypeValueException {
      if (var2 == null) {
         var2 = fEmptyContext;
      }

      if (var3 == null) {
         var3 = new ValidatedInfo();
      } else {
         var3.memberType = null;
      }

      boolean var4 = var2 == null || var2.needToNormalize();
      Object var5 = this.getActualValue(var1, var2, var3, var4);
      this.validate(var2, var3);
      return var5;
   }

   protected ValidatedInfo getActualEnumValue(String var1, ValidationContext var2, ValidatedInfo var3) throws InvalidDatatypeValueException {
      return this.fBase.validateWithInfo(var1, var2, var3);
   }

   public ValidatedInfo validateWithInfo(String var1, ValidationContext var2, ValidatedInfo var3) throws InvalidDatatypeValueException {
      if (var2 == null) {
         var2 = fEmptyContext;
      }

      if (var3 == null) {
         var3 = new ValidatedInfo();
      } else {
         var3.memberType = null;
      }

      boolean var4 = var2 == null || var2.needToNormalize();
      this.getActualValue(var1, var2, var3, var4);
      this.validate(var2, var3);
      return var3;
   }

   public Object validate(Object var1, ValidationContext var2, ValidatedInfo var3) throws InvalidDatatypeValueException {
      if (var2 == null) {
         var2 = fEmptyContext;
      }

      if (var3 == null) {
         var3 = new ValidatedInfo();
      } else {
         var3.memberType = null;
      }

      boolean var4 = var2 == null || var2.needToNormalize();
      Object var5 = this.getActualValue(var1, var2, var3, var4);
      this.validate(var2, var3);
      return var5;
   }

   public void validate(ValidationContext var1, ValidatedInfo var2) throws InvalidDatatypeValueException {
      if (var1 == null) {
         var1 = fEmptyContext;
      }

      if (var1.needFacetChecking() && this.fFacetsDefined != 0 && this.fFacetsDefined != 16) {
         this.checkFacets(var2);
      }

      if (var1.needExtraChecking()) {
         this.checkExtraRules(var1, var2);
      }

   }

   private void checkFacets(ValidatedInfo var1) throws InvalidDatatypeValueException {
      Object var2 = var1.actualValue;
      String var3 = var1.normalizedValue;
      short var4 = var1.actualValueType;
      ShortList var5 = var1.itemValueTypes;
      int var6;
      if (this.fValidationDV != 18 && this.fValidationDV != 20) {
         var6 = this.fDVs[this.fValidationDV].getDataLength(var2);
         if ((this.fFacetsDefined & 4) != 0 && var6 > this.fMaxLength) {
            throw new InvalidDatatypeValueException("cvc-maxLength-valid", new Object[]{var3, Integer.toString(var6), Integer.toString(this.fMaxLength), this.fTypeName});
         }

         if ((this.fFacetsDefined & 2) != 0 && var6 < this.fMinLength) {
            throw new InvalidDatatypeValueException("cvc-minLength-valid", new Object[]{var3, Integer.toString(var6), Integer.toString(this.fMinLength), this.fTypeName});
         }

         if ((this.fFacetsDefined & 1) != 0 && var6 != this.fLength) {
            throw new InvalidDatatypeValueException("cvc-length-valid", new Object[]{var3, Integer.toString(var6), Integer.toString(this.fLength), this.fTypeName});
         }
      }

      if ((this.fFacetsDefined & 2048) != 0) {
         boolean var17 = false;
         int var7 = this.fEnumerationSize;
         short var8 = this.convertToPrimitiveKind(var4);

         for(int var9 = 0; var9 < var7; ++var9) {
            short var10 = this.convertToPrimitiveKind(this.fEnumeration[var9].actualValueType);
            if ((var8 == var10 || var8 == 1 && var10 == 2 || var8 == 2 && var10 == 1) && this.fEnumeration[var9].actualValue.equals(var2)) {
               if (var8 != 44 && var8 != 43) {
                  var17 = true;
                  break;
               }

               ShortList var11 = this.fEnumeration[var9].itemValueTypes;
               int var12 = var5 != null ? var5.getLength() : 0;
               int var13 = var11 != null ? var11.getLength() : 0;
               if (var12 == var13) {
                  int var14;
                  for(var14 = 0; var14 < var12; ++var14) {
                     short var15 = this.convertToPrimitiveKind(var5.item(var14));
                     short var16 = this.convertToPrimitiveKind(var11.item(var14));
                     if (var15 != var16 && (var15 != 1 || var16 != 2) && (var15 != 2 || var16 != 1)) {
                        break;
                     }
                  }

                  if (var14 == var12) {
                     var17 = true;
                     break;
                  }
               }
            }
         }

         if (!var17) {
            StringBuffer var18 = new StringBuffer();
            this.appendEnumString(var18);
            throw new InvalidDatatypeValueException("cvc-enumeration-valid", new Object[]{var3, var18.toString()});
         }
      }

      if ((this.fFacetsDefined & 1024) != 0) {
         var6 = this.fDVs[this.fValidationDV].getFractionDigits(var2);
         if (var6 > this.fFractionDigits) {
            throw new InvalidDatatypeValueException("cvc-fractionDigits-valid", new Object[]{var3, Integer.toString(var6), Integer.toString(this.fFractionDigits)});
         }
      }

      if ((this.fFacetsDefined & 512) != 0) {
         var6 = this.fDVs[this.fValidationDV].getTotalDigits(var2);
         if (var6 > this.fTotalDigits) {
            throw new InvalidDatatypeValueException("cvc-totalDigits-valid", new Object[]{var3, Integer.toString(var6), Integer.toString(this.fTotalDigits)});
         }
      }

      if ((this.fFacetsDefined & 32) != 0) {
         var6 = this.fDVs[this.fValidationDV].compare(var2, this.fMaxInclusive);
         if (var6 != -1 && var6 != 0) {
            throw new InvalidDatatypeValueException("cvc-maxInclusive-valid", new Object[]{var3, this.fMaxInclusive, this.fTypeName});
         }
      }

      if ((this.fFacetsDefined & 64) != 0) {
         var6 = this.fDVs[this.fValidationDV].compare(var2, this.fMaxExclusive);
         if (var6 != -1) {
            throw new InvalidDatatypeValueException("cvc-maxExclusive-valid", new Object[]{var3, this.fMaxExclusive, this.fTypeName});
         }
      }

      if ((this.fFacetsDefined & 256) != 0) {
         var6 = this.fDVs[this.fValidationDV].compare(var2, this.fMinInclusive);
         if (var6 != 1 && var6 != 0) {
            throw new InvalidDatatypeValueException("cvc-minInclusive-valid", new Object[]{var3, this.fMinInclusive, this.fTypeName});
         }
      }

      if ((this.fFacetsDefined & 128) != 0) {
         var6 = this.fDVs[this.fValidationDV].compare(var2, this.fMinExclusive);
         if (var6 != 1) {
            throw new InvalidDatatypeValueException("cvc-minExclusive-valid", new Object[]{var3, this.fMinExclusive, this.fTypeName});
         }
      }

   }

   private void checkExtraRules(ValidationContext var1, ValidatedInfo var2) throws InvalidDatatypeValueException {
      Object var3 = var2.actualValue;
      if (this.fVariety == 1) {
         this.fDVs[this.fValidationDV].checkExtraRules(var3, var1);
      } else if (this.fVariety == 2) {
         ListDV.ListData var4 = (ListDV.ListData)var3;
         XSSimpleType var5 = var2.memberType;
         int var6 = var4.getLength();

         try {
            if (this.fItemType.fVariety == 3) {
               XSSimpleTypeDecl[] var7 = (XSSimpleTypeDecl[])var2.memberTypes;

               for(int var8 = var6 - 1; var8 >= 0; --var8) {
                  var2.actualValue = var4.item(var8);
                  var2.memberType = var7[var8];
                  this.fItemType.checkExtraRules(var1, var2);
               }
            } else {
               for(int var13 = var6 - 1; var13 >= 0; --var13) {
                  var2.actualValue = var4.item(var13);
                  this.fItemType.checkExtraRules(var1, var2);
               }
            }
         } finally {
            var2.actualValue = var4;
            var2.memberType = var5;
         }
      } else {
         ((XSSimpleTypeDecl)var2.memberType).checkExtraRules(var1, var2);
      }

   }

   private Object getActualValue(Object var1, ValidationContext var2, ValidatedInfo var3, boolean var4) throws InvalidDatatypeValueException {
      String var5;
      if (var4) {
         var5 = this.normalize(var1, this.fWhiteSpace);
      } else {
         var5 = var1.toString();
      }

      int var7;
      if ((this.fFacetsDefined & 8) != 0) {
         for(var7 = this.fPattern.size() - 1; var7 >= 0; --var7) {
            RegularExpression var6 = (RegularExpression)this.fPattern.elementAt(var7);
            if (!var6.matches(var5)) {
               throw new InvalidDatatypeValueException("cvc-pattern-valid", new Object[]{var1, this.fPatternStr.elementAt(var7), this.fTypeName});
            }
         }
      }

      Object var15;
      if (this.fVariety == 1) {
         if (this.fPatternType != 0) {
            boolean var17 = false;
            if (this.fPatternType == 1) {
               var17 = !XMLChar.isValidNmtoken(var5);
            } else if (this.fPatternType == 2) {
               var17 = !XMLChar.isValidName(var5);
            } else if (this.fPatternType == 3) {
               var17 = !XMLChar.isValidNCName(var5);
            }

            if (var17) {
               throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[]{var5, SPECIAL_PATTERN_STRING[this.fPatternType]});
            }
         }

         var3.normalizedValue = var5;
         var15 = this.fDVs[this.fValidationDV].getActualValue(var5, var2);
         var3.actualValue = var15;
         var3.actualValueType = this.fBuiltInKind;
         var3.actualType = this;
         return var15;
      } else if (this.fVariety == 2) {
         StringTokenizer var16 = new StringTokenizer(var5, " ");
         var7 = var16.countTokens();
         Object[] var19 = new Object[var7];
         boolean var20 = this.fItemType.getVariety() == 3;
         short[] var21 = new short[var20 ? var7 : 1];
         if (!var20) {
            var21[0] = this.fItemType.fBuiltInKind;
         }

         XSSimpleTypeDecl[] var11 = new XSSimpleTypeDecl[var7];

         for(int var12 = 0; var12 < var7; ++var12) {
            var19[var12] = this.fItemType.getActualValue(var16.nextToken(), var2, var3, false);
            if (var2.needFacetChecking() && this.fItemType.fFacetsDefined != 0 && this.fItemType.fFacetsDefined != 16) {
               this.fItemType.checkFacets(var3);
            }

            var11[var12] = (XSSimpleTypeDecl)var3.memberType;
            if (var20) {
               var21[var12] = var11[var12].fBuiltInKind;
            }
         }

         ListDV.ListData var13 = new ListDV.ListData(var19);
         var3.actualValue = var13;
         var3.actualValueType = (short)(var20 ? 43 : 44);
         var3.memberType = null;
         var3.memberTypes = var11;
         var3.itemValueTypes = new ShortListImpl(var21, var21.length);
         var3.normalizedValue = var5;
         var3.actualType = this;
         return var13;
      } else {
         var15 = this.fMemberTypes.length > 1 && var1 != null ? var1.toString() : var1;
         var7 = 0;

         while(var7 < this.fMemberTypes.length) {
            try {
               Object var8 = this.fMemberTypes[var7].getActualValue(var15, var2, var3, true);
               if (var2.needFacetChecking() && this.fMemberTypes[var7].fFacetsDefined != 0 && this.fMemberTypes[var7].fFacetsDefined != 16) {
                  this.fMemberTypes[var7].checkFacets(var3);
               }

               var3.memberType = this.fMemberTypes[var7];
               var3.actualType = this;
               return var8;
            } catch (InvalidDatatypeValueException var14) {
               ++var7;
            }
         }

         StringBuffer var18 = new StringBuffer();

         for(int var10 = 0; var10 < this.fMemberTypes.length; ++var10) {
            if (var10 != 0) {
               var18.append(" | ");
            }

            XSSimpleTypeDecl var9 = this.fMemberTypes[var10];
            if (var9.fTargetNamespace != null) {
               var18.append('{');
               var18.append(var9.fTargetNamespace);
               var18.append('}');
            }

            var18.append(var9.fTypeName);
            if (var9.fEnumeration != null) {
               var18.append(" : ");
               var9.appendEnumString(var18);
            }
         }

         throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.3", new Object[]{var1, this.fTypeName, var18.toString()});
      }
   }

   public boolean isEqual(Object var1, Object var2) {
      return var1 == null ? false : var1.equals(var2);
   }

   public boolean isIdentical(Object var1, Object var2) {
      return var1 == null ? false : this.fDVs[this.fValidationDV].isIdentical(var1, var2);
   }

   public static String normalize(String var0, short var1) {
      int var2 = var0 == null ? 0 : var0.length();
      if (var2 != 0 && var1 != 0) {
         StringBuffer var3 = new StringBuffer();
         char var4;
         int var5;
         if (var1 == 1) {
            for(var5 = 0; var5 < var2; ++var5) {
               var4 = var0.charAt(var5);
               if (var4 != '\t' && var4 != '\n' && var4 != '\r') {
                  var3.append(var4);
               } else {
                  var3.append(' ');
               }
            }
         } else {
            boolean var6 = true;

            for(var5 = 0; var5 < var2; ++var5) {
               var4 = var0.charAt(var5);
               if (var4 != '\t' && var4 != '\n' && var4 != '\r' && var4 != ' ') {
                  var3.append(var4);
                  var6 = false;
               } else {
                  while(var5 < var2 - 1) {
                     var4 = var0.charAt(var5 + 1);
                     if (var4 != '\t' && var4 != '\n' && var4 != '\r' && var4 != ' ') {
                        break;
                     }

                     ++var5;
                  }

                  if (var5 < var2 - 1 && !var6) {
                     var3.append(' ');
                  }
               }
            }
         }

         return var3.toString();
      } else {
         return var0;
      }
   }

   protected String normalize(Object var1, short var2) {
      if (var1 == null) {
         return null;
      } else {
         if ((this.fFacetsDefined & 8) == 0) {
            short var3 = fDVNormalizeType[this.fValidationDV];
            if (var3 == 0) {
               return var1.toString();
            }

            if (var3 == 1) {
               return XMLChar.trim(var1.toString());
            }
         }

         if (!(var1 instanceof StringBuffer)) {
            String var10 = var1.toString();
            return normalize(var10, var2);
         } else {
            StringBuffer var9 = (StringBuffer)var1;
            int var4 = var9.length();
            if (var4 == 0) {
               return "";
            } else if (var2 == 0) {
               return var9.toString();
            } else {
               char var5;
               int var6;
               if (var2 == 1) {
                  for(var6 = 0; var6 < var4; ++var6) {
                     var5 = var9.charAt(var6);
                     if (var5 == '\t' || var5 == '\n' || var5 == '\r') {
                        var9.setCharAt(var6, ' ');
                     }
                  }
               } else {
                  int var7 = 0;
                  boolean var8 = true;

                  for(var6 = 0; var6 < var4; ++var6) {
                     var5 = var9.charAt(var6);
                     if (var5 != '\t' && var5 != '\n' && var5 != '\r' && var5 != ' ') {
                        var9.setCharAt(var7++, var5);
                        var8 = false;
                     } else {
                        while(var6 < var4 - 1) {
                           var5 = var9.charAt(var6 + 1);
                           if (var5 != '\t' && var5 != '\n' && var5 != '\r' && var5 != ' ') {
                              break;
                           }

                           ++var6;
                        }

                        if (var6 < var4 - 1 && !var8) {
                           var9.setCharAt(var7++, ' ');
                        }
                     }
                  }

                  var9.setLength(var7);
               }

               return var9.toString();
            }
         }
      }
   }

   void reportError(String var1, Object[] var2) throws InvalidDatatypeFacetException {
      throw new InvalidDatatypeFacetException(var1, var2);
   }

   private String whiteSpaceValue(short var1) {
      return WS_FACET_STRING[var1];
   }

   public short getOrdered() {
      return this.fOrdered;
   }

   public boolean getBounded() {
      return this.fBounded;
   }

   public boolean getFinite() {
      return this.fFinite;
   }

   public boolean getNumeric() {
      return this.fNumeric;
   }

   public boolean isDefinedFacet(short var1) {
      if (this.fValidationDV != 0 && this.fValidationDV != 29) {
         if ((this.fFacetsDefined & var1) != 0) {
            return true;
         } else if (this.fPatternType != 0) {
            return var1 == 8;
         } else if (this.fValidationDV != 24) {
            return false;
         } else {
            return var1 == 8 || var1 == 1024;
         }
      } else {
         return false;
      }
   }

   public short getDefinedFacets() {
      if (this.fValidationDV != 0 && this.fValidationDV != 29) {
         if (this.fPatternType != 0) {
            return (short)(this.fFacetsDefined | 8);
         } else {
            return this.fValidationDV == 24 ? (short)(this.fFacetsDefined | 8 | 1024) : this.fFacetsDefined;
         }
      } else {
         return 0;
      }
   }

   public boolean isFixedFacet(short var1) {
      if ((this.fFixedFacet & var1) != 0) {
         return true;
      } else if (this.fValidationDV == 24) {
         return var1 == 1024;
      } else {
         return false;
      }
   }

   public short getFixedFacets() {
      return this.fValidationDV == 24 ? (short)(this.fFixedFacet | 1024) : this.fFixedFacet;
   }

   public String getLexicalFacetValue(short var1) {
      switch (var1) {
         case 1:
            return this.fLength == -1 ? null : Integer.toString(this.fLength);
         case 2:
            return this.fMinLength == -1 ? null : Integer.toString(this.fMinLength);
         case 4:
            return this.fMaxLength == -1 ? null : Integer.toString(this.fMaxLength);
         case 16:
            if (this.fValidationDV != 0 && this.fValidationDV != 29) {
               return WS_FACET_STRING[this.fWhiteSpace];
            }

            return null;
         case 32:
            return this.fMaxInclusive == null ? null : this.fMaxInclusive.toString();
         case 64:
            return this.fMaxExclusive == null ? null : this.fMaxExclusive.toString();
         case 128:
            return this.fMinExclusive == null ? null : this.fMinExclusive.toString();
         case 256:
            return this.fMinInclusive == null ? null : this.fMinInclusive.toString();
         case 512:
            return this.fTotalDigits == -1 ? null : Integer.toString(this.fTotalDigits);
         case 1024:
            if (this.fValidationDV == 24) {
               return "0";
            }

            return this.fFractionDigits == -1 ? null : Integer.toString(this.fFractionDigits);
         default:
            return null;
      }
   }

   public StringList getLexicalEnumeration() {
      if (this.fLexicalEnumeration == null) {
         if (this.fEnumeration == null) {
            return StringListImpl.EMPTY_LIST;
         }

         int var1 = this.fEnumerationSize;
         String[] var2 = new String[var1];

         for(int var3 = 0; var3 < var1; ++var3) {
            var2[var3] = this.fEnumeration[var3].normalizedValue;
         }

         this.fLexicalEnumeration = new StringListImpl(var2, var1);
      }

      return this.fLexicalEnumeration;
   }

   public ObjectList getActualEnumeration() {
      if (this.fActualEnumeration == null) {
         this.fActualEnumeration = new AbstractObjectList() {
            public int getLength() {
               return XSSimpleTypeDecl.this.fEnumeration != null ? XSSimpleTypeDecl.this.fEnumerationSize : 0;
            }

            public boolean contains(Object var1) {
               if (XSSimpleTypeDecl.this.fEnumeration == null) {
                  return false;
               } else {
                  for(int var2 = 0; var2 < XSSimpleTypeDecl.this.fEnumerationSize; ++var2) {
                     if (XSSimpleTypeDecl.this.fEnumeration[var2].getActualValue().equals(var1)) {
                        return true;
                     }
                  }

                  return false;
               }
            }

            public Object item(int var1) {
               return var1 >= 0 && var1 < this.getLength() ? XSSimpleTypeDecl.this.fEnumeration[var1].getActualValue() : null;
            }
         };
      }

      return this.fActualEnumeration;
   }

   public ObjectList getEnumerationItemTypeList() {
      if (this.fEnumerationItemTypeList == null) {
         if (this.fEnumeration == null) {
            return null;
         }

         this.fEnumerationItemTypeList = new AbstractObjectList() {
            public int getLength() {
               return XSSimpleTypeDecl.this.fEnumeration != null ? XSSimpleTypeDecl.this.fEnumerationSize : 0;
            }

            public boolean contains(Object var1) {
               if (XSSimpleTypeDecl.this.fEnumeration != null && var1 instanceof ShortList) {
                  for(int var2 = 0; var2 < XSSimpleTypeDecl.this.fEnumerationSize; ++var2) {
                     if (XSSimpleTypeDecl.this.fEnumeration[var2].itemValueTypes == var1) {
                        return true;
                     }
                  }

                  return false;
               } else {
                  return false;
               }
            }

            public Object item(int var1) {
               return var1 >= 0 && var1 < this.getLength() ? XSSimpleTypeDecl.this.fEnumeration[var1].itemValueTypes : null;
            }
         };
      }

      return this.fEnumerationItemTypeList;
   }

   public ShortList getEnumerationTypeList() {
      if (this.fEnumerationTypeList == null) {
         if (this.fEnumeration == null) {
            return ShortListImpl.EMPTY_LIST;
         }

         short[] var1 = new short[this.fEnumerationSize];

         for(int var2 = 0; var2 < this.fEnumerationSize; ++var2) {
            var1[var2] = this.fEnumeration[var2].actualValueType;
         }

         this.fEnumerationTypeList = new ShortListImpl(var1, this.fEnumerationSize);
      }

      return this.fEnumerationTypeList;
   }

   public StringList getLexicalPattern() {
      if (this.fPatternType == 0 && this.fValidationDV != 24 && this.fPatternStr == null) {
         return StringListImpl.EMPTY_LIST;
      } else {
         if (this.fLexicalPattern == null) {
            int var1 = this.fPatternStr == null ? 0 : this.fPatternStr.size();
            String[] var2;
            if (this.fPatternType == 1) {
               var2 = new String[var1 + 1];
               var2[var1] = "\\c+";
            } else if (this.fPatternType == 2) {
               var2 = new String[var1 + 1];
               var2[var1] = "\\i\\c*";
            } else if (this.fPatternType == 3) {
               var2 = new String[var1 + 2];
               var2[var1] = "\\i\\c*";
               var2[var1 + 1] = "[\\i-[:]][\\c-[:]]*";
            } else if (this.fValidationDV == 24) {
               var2 = new String[var1 + 1];
               var2[var1] = "[\\-+]?[0-9]+";
            } else {
               var2 = new String[var1];
            }

            for(int var3 = 0; var3 < var1; ++var3) {
               var2[var3] = (String)this.fPatternStr.elementAt(var3);
            }

            this.fLexicalPattern = new StringListImpl(var2, var2.length);
         }

         return this.fLexicalPattern;
      }
   }

   public XSObjectList getAnnotations() {
      return (XSObjectList)(this.fAnnotations != null ? this.fAnnotations : XSObjectListImpl.EMPTY_LIST);
   }

   private void calcFundamentalFacets() {
      this.setOrdered();
      this.setNumeric();
      this.setBounded();
      this.setCardinality();
   }

   private void setOrdered() {
      if (this.fVariety == 1) {
         this.fOrdered = this.fBase.fOrdered;
      } else if (this.fVariety == 2) {
         this.fOrdered = 0;
      } else if (this.fVariety == 3) {
         int var1 = this.fMemberTypes.length;
         if (var1 == 0) {
            this.fOrdered = 1;
            return;
         }

         short var2 = this.getPrimitiveDV(this.fMemberTypes[0].fValidationDV);
         boolean var3 = var2 != 0;
         boolean var4 = this.fMemberTypes[0].fOrdered == 0;

         for(int var5 = 1; var5 < this.fMemberTypes.length && (var3 || var4); ++var5) {
            if (var3) {
               var3 = var2 == this.getPrimitiveDV(this.fMemberTypes[var5].fValidationDV);
            }

            if (var4) {
               var4 = this.fMemberTypes[var5].fOrdered == 0;
            }
         }

         if (var3) {
            this.fOrdered = this.fMemberTypes[0].fOrdered;
         } else if (var4) {
            this.fOrdered = 0;
         } else {
            this.fOrdered = 1;
         }
      }

   }

   private void setNumeric() {
      if (this.fVariety == 1) {
         this.fNumeric = this.fBase.fNumeric;
      } else if (this.fVariety == 2) {
         this.fNumeric = false;
      } else if (this.fVariety == 3) {
         XSSimpleTypeDecl[] var1 = this.fMemberTypes;

         for(int var2 = 0; var2 < var1.length; ++var2) {
            if (!var1[var2].getNumeric()) {
               this.fNumeric = false;
               return;
            }
         }

         this.fNumeric = true;
      }

   }

   private void setBounded() {
      if (this.fVariety == 1) {
         if ((this.fFacetsDefined & 256) == 0 && (this.fFacetsDefined & 128) == 0 || (this.fFacetsDefined & 32) == 0 && (this.fFacetsDefined & 64) == 0) {
            this.fBounded = false;
         } else {
            this.fBounded = true;
         }
      } else if (this.fVariety == 2) {
         if ((this.fFacetsDefined & 1) == 0 && ((this.fFacetsDefined & 2) == 0 || (this.fFacetsDefined & 4) == 0)) {
            this.fBounded = false;
         } else {
            this.fBounded = true;
         }
      } else if (this.fVariety == 3) {
         XSSimpleTypeDecl[] var1 = this.fMemberTypes;
         short var2 = 0;
         if (var1.length > 0) {
            var2 = this.getPrimitiveDV(var1[0].fValidationDV);
         }

         int var3 = 0;

         while(true) {
            if (var3 >= var1.length) {
               this.fBounded = true;
               break;
            }

            if (!var1[var3].getBounded() || var2 != this.getPrimitiveDV(var1[var3].fValidationDV)) {
               this.fBounded = false;
               return;
            }

            ++var3;
         }
      }

   }

   private boolean specialCardinalityCheck() {
      return this.fBase.fValidationDV == 9 || this.fBase.fValidationDV == 10 || this.fBase.fValidationDV == 11 || this.fBase.fValidationDV == 12 || this.fBase.fValidationDV == 13 || this.fBase.fValidationDV == 14;
   }

   private void setCardinality() {
      if (this.fVariety == 1) {
         if (this.fBase.fFinite) {
            this.fFinite = true;
         } else if ((this.fFacetsDefined & 1) == 0 && (this.fFacetsDefined & 4) == 0 && (this.fFacetsDefined & 512) == 0) {
            if (((this.fFacetsDefined & 256) != 0 || (this.fFacetsDefined & 128) != 0) && ((this.fFacetsDefined & 32) != 0 || (this.fFacetsDefined & 64) != 0)) {
               if ((this.fFacetsDefined & 1024) == 0 && !this.specialCardinalityCheck()) {
                  this.fFinite = false;
               } else {
                  this.fFinite = true;
               }
            } else {
               this.fFinite = false;
            }
         } else {
            this.fFinite = true;
         }
      } else if (this.fVariety == 2) {
         if ((this.fFacetsDefined & 1) == 0 && ((this.fFacetsDefined & 2) == 0 || (this.fFacetsDefined & 4) == 0)) {
            this.fFinite = false;
         } else {
            this.fFinite = true;
         }
      } else if (this.fVariety == 3) {
         XSSimpleTypeDecl[] var1 = this.fMemberTypes;

         for(int var2 = 0; var2 < var1.length; ++var2) {
            if (!var1[var2].getFinite()) {
               this.fFinite = false;
               return;
            }
         }

         this.fFinite = true;
      }

   }

   private short getPrimitiveDV(short var1) {
      if (var1 != 21 && var1 != 22 && var1 != 23) {
         return var1 == 24 ? 3 : var1;
      } else {
         return 1;
      }
   }

   public boolean derivedFromType(XSTypeDefinition var1, short var2) {
      if (var1 == null) {
         return false;
      } else {
         while(var1 instanceof XSSimpleTypeDelegate) {
            var1 = ((XSSimpleTypeDelegate)var1).type;
         }

         if (((XSTypeDefinition)var1).getBaseType() == var1) {
            return true;
         } else {
            Object var3;
            for(var3 = this; var3 != var1 && var3 != fAnySimpleType; var3 = ((XSTypeDefinition)var3).getBaseType()) {
            }

            return var3 == var1;
         }
      }
   }

   public boolean derivedFrom(String var1, String var2, short var3) {
      if (var2 == null) {
         return false;
      } else if ("http://www.w3.org/2001/XMLSchema".equals(var1) && "anyType".equals(var2)) {
         return true;
      } else {
         Object var4;
         for(var4 = this; (!var2.equals(((XSTypeDefinition)var4).getName()) || (var1 != null || ((XSTypeDefinition)var4).getNamespace() != null) && (var1 == null || !var1.equals(((XSTypeDefinition)var4).getNamespace()))) && var4 != fAnySimpleType; var4 = ((XSTypeDefinition)var4).getBaseType()) {
         }

         return var4 != fAnySimpleType;
      }
   }

   public boolean isDOMDerivedFrom(String var1, String var2, int var3) {
      if (var2 == null) {
         return false;
      } else if (SchemaSymbols.URI_SCHEMAFORSCHEMA.equals(var1) && "anyType".equals(var2) && ((var3 & 1) != 0 || var3 == 0)) {
         return true;
      } else if ((var3 & 1) != 0 && this.isDerivedByRestriction(var1, var2, this)) {
         return true;
      } else if ((var3 & 8) != 0 && this.isDerivedByList(var1, var2, this)) {
         return true;
      } else if ((var3 & 4) != 0 && this.isDerivedByUnion(var1, var2, this)) {
         return true;
      } else if ((var3 & 2) != 0 && (var3 & 1) == 0 && (var3 & 8) == 0 && (var3 & 4) == 0) {
         return false;
      } else {
         return (var3 & 2) == 0 && (var3 & 1) == 0 && (var3 & 8) == 0 && (var3 & 4) == 0 ? this.isDerivedByAny(var1, var2, this) : false;
      }
   }

   private boolean isDerivedByAny(String var1, String var2, XSTypeDefinition var3) {
      boolean var4 = false;
      Object var5 = null;

      while(var3 != null && var3 != var5) {
         if (var2.equals(((XSTypeDefinition)var3).getName()) && (var1 == null && ((XSTypeDefinition)var3).getNamespace() == null || var1 != null && var1.equals(((XSTypeDefinition)var3).getNamespace()))) {
            var4 = true;
            break;
         }

         if (this.isDerivedByRestriction(var1, var2, (XSTypeDefinition)var3)) {
            return true;
         }

         if (this.isDerivedByList(var1, var2, (XSTypeDefinition)var3)) {
            return true;
         }

         if (this.isDerivedByUnion(var1, var2, (XSTypeDefinition)var3)) {
            return true;
         }

         var5 = var3;
         if (((XSSimpleTypeDecl)var3).getVariety() != 0 && ((XSSimpleTypeDecl)var3).getVariety() != 1) {
            if (((XSSimpleTypeDecl)var3).getVariety() == 3) {
               byte var6 = 0;
               if (var6 < ((XSSimpleTypeDecl)var3).getMemberTypes().getLength()) {
                  return this.isDerivedByAny(var1, var2, (XSTypeDefinition)((XSSimpleTypeDecl)var3).getMemberTypes().item(var6));
               }
            } else if (((XSSimpleTypeDecl)var3).getVariety() == 2) {
               var3 = ((XSSimpleTypeDecl)var3).getItemType();
            }
         } else {
            var3 = ((XSTypeDefinition)var3).getBaseType();
         }
      }

      return var4;
   }

   private boolean isDerivedByRestriction(String var1, String var2, XSTypeDefinition var3) {
      XSTypeDefinition var4 = null;

      while(true) {
         if (var3 != null && var3 != var4) {
            if (!var2.equals(var3.getName()) || (var1 == null || !var1.equals(var3.getNamespace())) && (var3.getNamespace() != null || var1 != null)) {
               var4 = var3;
               var3 = var3.getBaseType();
               continue;
            }

            return true;
         }

         return false;
      }
   }

   private boolean isDerivedByList(String var1, String var2, XSTypeDefinition var3) {
      if (var3 != null && ((XSSimpleTypeDefinition)var3).getVariety() == 2) {
         XSSimpleTypeDefinition var4 = ((XSSimpleTypeDefinition)var3).getItemType();
         if (var4 != null && this.isDerivedByRestriction(var1, var2, var4)) {
            return true;
         }
      }

      return false;
   }

   private boolean isDerivedByUnion(String var1, String var2, XSTypeDefinition var3) {
      if (var3 != null && ((XSSimpleTypeDefinition)var3).getVariety() == 3) {
         XSObjectList var4 = ((XSSimpleTypeDefinition)var3).getMemberTypes();

         for(int var5 = 0; var5 < var4.getLength(); ++var5) {
            if (var4.item(var5) != null && this.isDerivedByRestriction(var1, var2, (XSSimpleTypeDefinition)var4.item(var5))) {
               return true;
            }
         }
      }

      return false;
   }

   public void reset() {
      if (!this.fIsImmutable) {
         this.fItemType = null;
         this.fMemberTypes = null;
         this.fTypeName = null;
         this.fTargetNamespace = null;
         this.fFinalSet = 0;
         this.fBase = null;
         this.fVariety = -1;
         this.fValidationDV = -1;
         this.fFacetsDefined = 0;
         this.fFixedFacet = 0;
         this.fWhiteSpace = 0;
         this.fLength = -1;
         this.fMinLength = -1;
         this.fMaxLength = -1;
         this.fTotalDigits = -1;
         this.fFractionDigits = -1;
         this.fPattern = null;
         this.fPatternStr = null;
         this.fEnumeration = null;
         this.fLexicalPattern = null;
         this.fLexicalEnumeration = null;
         this.fActualEnumeration = null;
         this.fEnumerationTypeList = null;
         this.fEnumerationItemTypeList = null;
         this.fMaxInclusive = null;
         this.fMaxExclusive = null;
         this.fMinExclusive = null;
         this.fMinInclusive = null;
         this.lengthAnnotation = null;
         this.minLengthAnnotation = null;
         this.maxLengthAnnotation = null;
         this.whiteSpaceAnnotation = null;
         this.totalDigitsAnnotation = null;
         this.fractionDigitsAnnotation = null;
         this.patternAnnotations = null;
         this.enumerationAnnotations = null;
         this.maxInclusiveAnnotation = null;
         this.maxExclusiveAnnotation = null;
         this.minInclusiveAnnotation = null;
         this.minExclusiveAnnotation = null;
         this.fPatternType = 0;
         this.fAnnotations = null;
         this.fFacets = null;
      }
   }

   public XSNamespaceItem getNamespaceItem() {
      return this.fNamespaceItem;
   }

   public void setNamespaceItem(XSNamespaceItem var1) {
      this.fNamespaceItem = var1;
   }

   public String toString() {
      return this.fTargetNamespace + "," + this.fTypeName;
   }

   public XSObjectList getFacets() {
      if (this.fFacets == null && (this.fFacetsDefined != 0 || this.fValidationDV == 24)) {
         XSFacetImpl[] var1 = new XSFacetImpl[10];
         int var2 = 0;
         if ((this.fFacetsDefined & 16) != 0 && this.fValidationDV != 0 && this.fValidationDV != 29) {
            var1[var2] = new XSFacetImpl((short)16, WS_FACET_STRING[this.fWhiteSpace], 0, (Object)null, (this.fFixedFacet & 16) != 0, this.whiteSpaceAnnotation);
            ++var2;
         }

         if (this.fLength != -1) {
            var1[var2] = new XSFacetImpl((short)1, Integer.toString(this.fLength), this.fLength, (Object)null, (this.fFixedFacet & 1) != 0, this.lengthAnnotation);
            ++var2;
         }

         if (this.fMinLength != -1) {
            var1[var2] = new XSFacetImpl((short)2, Integer.toString(this.fMinLength), this.fMinLength, (Object)null, (this.fFixedFacet & 2) != 0, this.minLengthAnnotation);
            ++var2;
         }

         if (this.fMaxLength != -1) {
            var1[var2] = new XSFacetImpl((short)4, Integer.toString(this.fMaxLength), this.fMaxLength, (Object)null, (this.fFixedFacet & 4) != 0, this.maxLengthAnnotation);
            ++var2;
         }

         if (this.fTotalDigits != -1) {
            var1[var2] = new XSFacetImpl((short)512, Integer.toString(this.fTotalDigits), this.fTotalDigits, (Object)null, (this.fFixedFacet & 512) != 0, this.totalDigitsAnnotation);
            ++var2;
         }

         if (this.fValidationDV == 24) {
            var1[var2] = new XSFacetImpl((short)1024, "0", 0, (Object)null, true, this.fractionDigitsAnnotation);
            ++var2;
         } else if (this.fFractionDigits != -1) {
            var1[var2] = new XSFacetImpl((short)1024, Integer.toString(this.fFractionDigits), this.fFractionDigits, (Object)null, (this.fFixedFacet & 1024) != 0, this.fractionDigitsAnnotation);
            ++var2;
         }

         if (this.fMaxInclusive != null) {
            var1[var2] = new XSFacetImpl((short)32, this.fMaxInclusive.toString(), 0, this.fMaxInclusive, (this.fFixedFacet & 32) != 0, this.maxInclusiveAnnotation);
            ++var2;
         }

         if (this.fMaxExclusive != null) {
            var1[var2] = new XSFacetImpl((short)64, this.fMaxExclusive.toString(), 0, this.fMaxExclusive, (this.fFixedFacet & 64) != 0, this.maxExclusiveAnnotation);
            ++var2;
         }

         if (this.fMinExclusive != null) {
            var1[var2] = new XSFacetImpl((short)128, this.fMinExclusive.toString(), 0, this.fMinExclusive, (this.fFixedFacet & 128) != 0, this.minExclusiveAnnotation);
            ++var2;
         }

         if (this.fMinInclusive != null) {
            var1[var2] = new XSFacetImpl((short)256, this.fMinInclusive.toString(), 0, this.fMinInclusive, (this.fFixedFacet & 256) != 0, this.minInclusiveAnnotation);
            ++var2;
         }

         this.fFacets = var2 > 0 ? new XSObjectListImpl(var1, var2) : XSObjectListImpl.EMPTY_LIST;
      }

      return this.fFacets != null ? this.fFacets : XSObjectListImpl.EMPTY_LIST;
   }

   public XSObject getFacet(int var1) {
      XSObjectList var2;
      int var3;
      if (var1 != 2048 && var1 != 8) {
         var2 = this.getFacets();

         for(var3 = 0; var3 < var2.getLength(); ++var3) {
            XSFacet var5 = (XSFacet)var2.item(var3);
            if (var5.getFacetKind() == var1) {
               return var5;
            }
         }
      } else {
         var2 = this.getMultiValueFacets();

         for(var3 = 0; var3 < var2.getLength(); ++var3) {
            XSMultiValueFacet var4 = (XSMultiValueFacet)var2.item(var3);
            if (var4.getFacetKind() == var1) {
               return var4;
            }
         }
      }

      return null;
   }

   public XSObjectList getMultiValueFacets() {
      if (this.fMultiValueFacets == null && ((this.fFacetsDefined & 2048) != 0 || (this.fFacetsDefined & 8) != 0 || this.fPatternType != 0 || this.fValidationDV == 24)) {
         XSMVFacetImpl[] var1 = new XSMVFacetImpl[2];
         int var2 = 0;
         if ((this.fFacetsDefined & 8) != 0 || this.fPatternType != 0 || this.fValidationDV == 24) {
            var1[var2] = new XSMVFacetImpl((short)8, this.getLexicalPattern(), (ObjectList)null, this.patternAnnotations);
            ++var2;
         }

         if (this.fEnumeration != null) {
            var1[var2] = new XSMVFacetImpl((short)2048, this.getLexicalEnumeration(), new ObjectListImpl(this.fEnumeration, this.fEnumerationSize), this.enumerationAnnotations);
            ++var2;
         }

         this.fMultiValueFacets = new XSObjectListImpl(var1, var2);
      }

      return this.fMultiValueFacets != null ? this.fMultiValueFacets : XSObjectListImpl.EMPTY_LIST;
   }

   public Object getMinInclusiveValue() {
      return this.fMinInclusive;
   }

   public Object getMinExclusiveValue() {
      return this.fMinExclusive;
   }

   public Object getMaxInclusiveValue() {
      return this.fMaxInclusive;
   }

   public Object getMaxExclusiveValue() {
      return this.fMaxExclusive;
   }

   public void setAnonymous(boolean var1) {
      this.fAnonymous = var1;
   }

   public String getTypeNamespace() {
      return this.getNamespace();
   }

   public boolean isDerivedFrom(String var1, String var2, int var3) {
      return this.isDOMDerivedFrom(var1, var2, var3);
   }

   private short convertToPrimitiveKind(short var1) {
      if (var1 <= 20) {
         return var1;
      } else if (var1 <= 29) {
         return 2;
      } else {
         return var1 <= 42 ? 4 : var1;
      }
   }

   private void appendEnumString(StringBuffer var1) {
      var1.append('[');

      for(int var2 = 0; var2 < this.fEnumerationSize; ++var2) {
         if (var2 != 0) {
            var1.append(", ");
         }

         var1.append(this.fEnumeration[var2].actualValue);
      }

      var1.append(']');
   }

   static {
      fAnyAtomicType = new XSSimpleTypeDecl(fAnySimpleType, "anyAtomicType", (short)29, (short)0, false, true, false, true, (short)49);
      fDummyContext = new ValidationContext() {
         public boolean needFacetChecking() {
            return true;
         }

         public boolean needExtraChecking() {
            return false;
         }

         public boolean needToNormalize() {
            return false;
         }

         public boolean useNamespaces() {
            return true;
         }

         public boolean isEntityDeclared(String var1) {
            return false;
         }

         public boolean isEntityUnparsed(String var1) {
            return false;
         }

         public boolean isIdDeclared(String var1) {
            return false;
         }

         public void addId(String var1) {
         }

         public void addIdRef(String var1) {
         }

         public String getSymbol(String var1) {
            return var1.intern();
         }

         public String getURI(String var1) {
            return null;
         }

         public Locale getLocale() {
            return Locale.getDefault();
         }
      };
   }

   private abstract static class AbstractObjectList extends AbstractList implements ObjectList {
      private AbstractObjectList() {
      }

      public Object get(int var1) {
         if (var1 >= 0 && var1 < this.getLength()) {
            return this.item(var1);
         } else {
            throw new IndexOutOfBoundsException("Index: " + var1);
         }
      }

      public int size() {
         return this.getLength();
      }

      public abstract Object item(int var1);

      public abstract int getLength();

      // $FF: synthetic method
      AbstractObjectList(Object var1) {
         this();
      }
   }

   private static final class XSMVFacetImpl implements XSMultiValueFacet {
      final short kind;
      final XSObjectList annotations;
      final StringList svalues;
      final ObjectList avalues;

      public XSMVFacetImpl(short var1, StringList var2, ObjectList var3, XSObjectList var4) {
         this.kind = var1;
         this.svalues = var2;
         this.avalues = var3;
         this.annotations = (XSObjectList)(var4 != null ? var4 : XSObjectListImpl.EMPTY_LIST);
      }

      public short getFacetKind() {
         return this.kind;
      }

      public XSObjectList getAnnotations() {
         return this.annotations;
      }

      public StringList getLexicalFacetValues() {
         return this.svalues;
      }

      public ObjectList getEnumerationValues() {
         return this.avalues;
      }

      public String getName() {
         return null;
      }

      public String getNamespace() {
         return null;
      }

      public XSNamespaceItem getNamespaceItem() {
         return null;
      }

      public short getType() {
         return 14;
      }
   }

   private static final class XSFacetImpl implements XSFacet {
      final short kind;
      final String svalue;
      final int ivalue;
      Object avalue;
      final boolean fixed;
      final XSObjectList annotations;

      public XSFacetImpl(short var1, String var2, int var3, Object var4, boolean var5, XSAnnotation var6) {
         this.kind = var1;
         this.svalue = var2;
         this.ivalue = var3;
         this.avalue = var4;
         this.fixed = var5;
         if (var6 != null) {
            this.annotations = new XSObjectListImpl();
            ((XSObjectListImpl)this.annotations).addXSObject(var6);
         } else {
            this.annotations = XSObjectListImpl.EMPTY_LIST;
         }

      }

      public XSAnnotation getAnnotation() {
         return (XSAnnotation)this.annotations.item(0);
      }

      public XSObjectList getAnnotations() {
         return this.annotations;
      }

      public short getFacetKind() {
         return this.kind;
      }

      public String getLexicalFacetValue() {
         return this.svalue;
      }

      public Object getActualFacetValue() {
         if (this.avalue == null) {
            if (this.kind == 16) {
               this.avalue = this.svalue;
            } else {
               this.avalue = BigInteger.valueOf((long)this.ivalue);
            }
         }

         return this.avalue;
      }

      public int getIntFacetValue() {
         return this.ivalue;
      }

      public boolean getFixed() {
         return this.fixed;
      }

      public String getName() {
         return null;
      }

      public String getNamespace() {
         return null;
      }

      public XSNamespaceItem getNamespaceItem() {
         return null;
      }

      public short getType() {
         return 13;
      }
   }

   static final class ValidationContextImpl implements ValidationContext {
      final ValidationContext fExternal;
      NamespaceContext fNSContext;

      ValidationContextImpl(ValidationContext var1) {
         this.fExternal = var1;
      }

      void setNSContext(NamespaceContext var1) {
         this.fNSContext = var1;
      }

      public boolean needFacetChecking() {
         return this.fExternal.needFacetChecking();
      }

      public boolean needExtraChecking() {
         return this.fExternal.needExtraChecking();
      }

      public boolean needToNormalize() {
         return this.fExternal.needToNormalize();
      }

      public boolean useNamespaces() {
         return true;
      }

      public boolean isEntityDeclared(String var1) {
         return this.fExternal.isEntityDeclared(var1);
      }

      public boolean isEntityUnparsed(String var1) {
         return this.fExternal.isEntityUnparsed(var1);
      }

      public boolean isIdDeclared(String var1) {
         return this.fExternal.isIdDeclared(var1);
      }

      public void addId(String var1) {
         this.fExternal.addId(var1);
      }

      public void addIdRef(String var1) {
         this.fExternal.addIdRef(var1);
      }

      public String getSymbol(String var1) {
         return this.fExternal.getSymbol(var1);
      }

      public String getURI(String var1) {
         return this.fNSContext == null ? this.fExternal.getURI(var1) : this.fNSContext.getURI(var1);
      }

      public Locale getLocale() {
         return this.fExternal.getLocale();
      }
   }
}
