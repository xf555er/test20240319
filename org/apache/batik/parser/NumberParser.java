package org.apache.batik.parser;

import java.io.IOException;

public abstract class NumberParser extends AbstractParser {
   private static final double[] pow10 = new double[128];

   protected float parseFloat() throws ParseException, IOException {
      int mant = 0;
      int mantDig = 0;
      boolean mantPos = true;
      boolean mantRead = false;
      int exp = 0;
      int expDig = 0;
      int expAdj = 0;
      boolean expPos = true;
      switch (this.current) {
         case 45:
            mantPos = false;
         case 43:
            this.current = this.reader.read();
      }

      label158:
      switch (this.current) {
         case 46:
            break;
         case 47:
         default:
            this.reportUnexpectedCharacterError(this.current);
            return 0.0F;
         case 48:
            mantRead = true;

            label156:
            while(true) {
               this.current = this.reader.read();
               switch (this.current) {
                  case 46:
                  case 69:
                  case 101:
                     break label158;
                  case 48:
                     break;
                  case 49:
                  case 50:
                  case 51:
                  case 52:
                  case 53:
                  case 54:
                  case 55:
                  case 56:
                  case 57:
                     break label156;
                  default:
                     return 0.0F;
               }
            }
         case 49:
         case 50:
         case 51:
         case 52:
         case 53:
         case 54:
         case 55:
         case 56:
         case 57:
            mantRead = true;

            label140:
            while(true) {
               if (mantDig < 9) {
                  ++mantDig;
                  mant = mant * 10 + (this.current - 48);
               } else {
                  ++expAdj;
               }

               this.current = this.reader.read();
               switch (this.current) {
                  case 48:
                  case 49:
                  case 50:
                  case 51:
                  case 52:
                  case 53:
                  case 54:
                  case 55:
                  case 56:
                  case 57:
                     break;
                  default:
                     break label140;
               }
            }
      }

      if (this.current == 46) {
         label175: {
            this.current = this.reader.read();
            switch (this.current) {
               case 48:
                  if (mantDig == 0) {
                     label126:
                     while(true) {
                        this.current = this.reader.read();
                        --expAdj;
                        switch (this.current) {
                           case 48:
                              break;
                           case 49:
                           case 50:
                           case 51:
                           case 52:
                           case 53:
                           case 54:
                           case 55:
                           case 56:
                           case 57:
                              break label126;
                           default:
                              if (!mantRead) {
                                 return 0.0F;
                              }
                              break label175;
                        }
                     }
                  }
               case 49:
               case 50:
               case 51:
               case 52:
               case 53:
               case 54:
               case 55:
               case 56:
               case 57:
                  break;
               case 69:
               case 101:
               default:
                  if (!mantRead) {
                     this.reportUnexpectedCharacterError(this.current);
                     return 0.0F;
                  }
                  break label175;
            }

            label107:
            while(true) {
               if (mantDig < 9) {
                  ++mantDig;
                  mant = mant * 10 + (this.current - 48);
                  --expAdj;
               }

               this.current = this.reader.read();
               switch (this.current) {
                  case 48:
                  case 49:
                  case 50:
                  case 51:
                  case 52:
                  case 53:
                  case 54:
                  case 55:
                  case 56:
                  case 57:
                     break;
                  default:
                     break label107;
               }
            }
         }
      }

      switch (this.current) {
         case 69:
         case 101:
            this.current = this.reader.read();
            switch (this.current) {
               case 44:
               case 46:
               case 47:
               default:
                  this.reportUnexpectedCharacterError(this.current);
                  return 0.0F;
               case 45:
                  expPos = false;
               case 43:
                  this.current = this.reader.read();
                  switch (this.current) {
                     case 48:
                     case 49:
                     case 50:
                     case 51:
                     case 52:
                     case 53:
                     case 54:
                     case 55:
                     case 56:
                     case 57:
                        break;
                     default:
                        this.reportUnexpectedCharacterError(this.current);
                        return 0.0F;
                  }
               case 48:
               case 49:
               case 50:
               case 51:
               case 52:
               case 53:
               case 54:
               case 55:
               case 56:
               case 57:
            }

            label95:
            switch (this.current) {
               case 48:
                  label94:
                  while(true) {
                     this.current = this.reader.read();
                     switch (this.current) {
                        case 48:
                           break;
                        case 49:
                        case 50:
                        case 51:
                        case 52:
                        case 53:
                        case 54:
                        case 55:
                        case 56:
                        case 57:
                           break label94;
                        default:
                           break label95;
                     }
                  }
               case 49:
               case 50:
               case 51:
               case 52:
               case 53:
               case 54:
               case 55:
               case 56:
               case 57:
                  label84:
                  while(true) {
                     if (expDig < 3) {
                        ++expDig;
                        exp = exp * 10 + (this.current - 48);
                     }

                     this.current = this.reader.read();
                     switch (this.current) {
                        case 48:
                        case 49:
                        case 50:
                        case 51:
                        case 52:
                        case 53:
                        case 54:
                        case 55:
                        case 56:
                        case 57:
                           break;
                        default:
                           break label84;
                     }
                  }
            }
         default:
            if (!expPos) {
               exp = -exp;
            }

            exp += expAdj;
            if (!mantPos) {
               mant = -mant;
            }

            return buildFloat(mant, exp);
      }
   }

   public static float buildFloat(int mant, int exp) {
      if (exp >= -125 && mant != 0) {
         if (exp >= 128) {
            return mant > 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
         } else if (exp == 0) {
            return (float)mant;
         } else {
            if (mant >= 67108864) {
               ++mant;
            }

            return (float)(exp > 0 ? (double)mant * pow10[exp] : (double)mant / pow10[-exp]);
         }
      } else {
         return 0.0F;
      }
   }

   static {
      for(int i = 0; i < pow10.length; ++i) {
         pow10[i] = Math.pow(10.0, (double)i);
      }

   }
}
