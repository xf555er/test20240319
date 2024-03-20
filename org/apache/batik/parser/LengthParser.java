package org.apache.batik.parser;

import java.io.IOException;

public class LengthParser extends AbstractParser {
   protected LengthHandler lengthHandler;

   public LengthParser() {
      this.lengthHandler = DefaultLengthHandler.INSTANCE;
   }

   public void setLengthHandler(LengthHandler handler) {
      this.lengthHandler = handler;
   }

   public LengthHandler getLengthHandler() {
      return this.lengthHandler;
   }

   protected void doParse() throws ParseException, IOException {
      this.lengthHandler.startLength();
      this.current = this.reader.read();
      this.skipSpaces();
      this.parseLength();
      this.skipSpaces();
      if (this.current != -1) {
         this.reportError("end.of.stream.expected", new Object[]{this.current});
      }

      this.lengthHandler.endLength();
   }

   protected void parseLength() throws ParseException, IOException {
      int mant = 0;
      int mantDig = 0;
      boolean mantPos = true;
      boolean mantRead = false;
      int exp = 0;
      int expDig = 0;
      int expAdj = 0;
      boolean expPos = true;
      int unitState = 0;
      switch (this.current) {
         case 45:
            mantPos = false;
         case 43:
            this.current = this.reader.read();
      }

      label182:
      switch (this.current) {
         case 46:
            break;
         case 47:
         default:
            this.reportUnexpectedCharacterError(this.current);
            return;
         case 48:
            mantRead = true;

            label180:
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
                     break label180;
                  default:
                     break label182;
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

            label167:
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
                     break label167;
               }
            }
      }

      if (this.current == 46) {
         this.current = this.reader.read();
         label157:
         switch (this.current) {
            case 48:
               if (mantDig == 0) {
                  label155:
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
                           break label155;
                        default:
                           break label157;
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
                        break label157;
                  }
               }
            case 69:
            case 101:
            default:
               if (!mantRead) {
                  this.reportUnexpectedCharacterError(this.current);
                  return;
               }
         }
      }

      boolean le = false;
      switch (this.current) {
         case 101:
            le = true;
         case 69:
            this.current = this.reader.read();
            label136:
            switch (this.current) {
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
                        return;
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
                  switch (this.current) {
                     case 48:
                        label132:
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
                                 break label132;
                              default:
                                 break label136;
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
                                 break label136;
                           }
                        }
                     default:
                        break label136;
                  }
               case 109:
                  if (!le) {
                     this.reportUnexpectedCharacterError(this.current);
                     return;
                  }

                  unitState = 1;
                  break;
               case 120:
                  if (!le) {
                     this.reportUnexpectedCharacterError(this.current);
                     return;
                  }

                  unitState = 2;
                  break;
               default:
                  this.reportUnexpectedCharacterError(this.current);
                  return;
            }
         default:
            if (!expPos) {
               exp = -exp;
            }

            exp += expAdj;
            if (!mantPos) {
               mant = -mant;
            }

            this.lengthHandler.lengthValue(NumberParser.buildFloat(mant, exp));
            switch (unitState) {
               case 1:
                  this.lengthHandler.em();
                  this.current = this.reader.read();
                  return;
               case 2:
                  this.lengthHandler.ex();
                  this.current = this.reader.read();
                  return;
               default:
                  switch (this.current) {
                     case 37:
                        this.lengthHandler.percentage();
                        this.current = this.reader.read();
                        break;
                     case 99:
                        this.current = this.reader.read();
                        if (this.current != 109) {
                           this.reportCharacterExpectedError('m', this.current);
                        } else {
                           this.lengthHandler.cm();
                           this.current = this.reader.read();
                        }
                        break;
                     case 101:
                        this.current = this.reader.read();
                        switch (this.current) {
                           case 109:
                              this.lengthHandler.em();
                              this.current = this.reader.read();
                              return;
                           case 120:
                              this.lengthHandler.ex();
                              this.current = this.reader.read();
                              return;
                           default:
                              this.reportUnexpectedCharacterError(this.current);
                              return;
                        }
                     case 105:
                        this.current = this.reader.read();
                        if (this.current != 110) {
                           this.reportCharacterExpectedError('n', this.current);
                        } else {
                           this.lengthHandler.in();
                           this.current = this.reader.read();
                        }
                        break;
                     case 109:
                        this.current = this.reader.read();
                        if (this.current != 109) {
                           this.reportCharacterExpectedError('m', this.current);
                        } else {
                           this.lengthHandler.mm();
                           this.current = this.reader.read();
                        }
                        break;
                     case 112:
                        this.current = this.reader.read();
                        switch (this.current) {
                           case 99:
                              this.lengthHandler.pc();
                              this.current = this.reader.read();
                              break;
                           case 116:
                              this.lengthHandler.pt();
                              this.current = this.reader.read();
                              break;
                           case 120:
                              this.lengthHandler.px();
                              this.current = this.reader.read();
                              break;
                           default:
                              this.reportUnexpectedCharacterError(this.current);
                        }
                  }

            }
      }
   }
}
