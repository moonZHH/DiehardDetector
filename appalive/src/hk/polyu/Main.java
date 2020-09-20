package hk.polyu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

import hk.polyu.analysis.ExecutableAnalysis;
import hk.polyu.analysis.ManifestAnalysis;
import hk.polyu.analysis.ReachableAnalysis;
import hk.polyu.keepalive.AnalyzeACP;
import hk.polyu.keepalive.AnalyzeBRS;
import hk.polyu.keepalive.AnalyzeCOW;
import hk.polyu.keepalive.AnalyzeHFA;
import hk.polyu.keepalive.AnalyzeHFS;
import hk.polyu.keepalive.AnalyzeHTI;
import hk.polyu.keepalive.AnalyzePMI;
import hk.polyu.pullalive.AnalyzeCSS;
import hk.polyu.pullalive.AnalyzeLAS;
import hk.polyu.pullalive.AnalyzeMAB;
import hk.polyu.pullalive.AnalyzeMSB;
import hk.polyu.pullalive.AnalyzeUJS;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.infoflow.cmd.Flowdroid;
import soot.jimple.infoflow.memory.FlowDroidTimeoutWatcher;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.toolkits.callgraph.CallGraph;

public class Main {
	
	private static ArrayList<String> iterateApkDirectory() {
		ArrayList<String> apkPathList = new ArrayList<String>();
		ArrayList<File> apkDirectoryList = new ArrayList<File>();
		/*
		apkDirectoryList.add(new File("/home/zhouhao/KeepAliveWS/samples/auto_and_vehicles"));
		apkDirectoryList.add(new File("/home/zhouhao/KeepAliveWS/samples/communication"));
		apkDirectoryList.add(new File("/home/zhouhao/KeepAliveWS/samples/health_and_fitness"));
		apkDirectoryList.add(new File("/home/zhouhao/KeepAliveWS/samples/maps_and_navigation"));
		apkDirectoryList.add(new File("/home/zhouhao/KeepAliveWS/samples/music_and_audio"));
		apkDirectoryList.add(new File("/home/zhouhao/KeepAliveWS/samples/social"));
		apkDirectoryList.add(new File("/home/zhouhao/KeepAliveWS/samples/travel_and_local"));
		apkDirectoryList.add(new File("/home/zhouhao/KeepAliveWS/samples/video_players"));
		apkDirectoryList.add(new File("/home/zhouhao/KeepAliveWS/samples/weather"));
		*/
		apkDirectoryList.add(new File(Config.apkDirectoryPath));
		
		// File apkDirectory = new File(Config.apkDirectoryPath);
		for (File apkDirectory : apkDirectoryList) {
			if (apkDirectory.exists()) {
				assert apkDirectory.isDirectory();
				for (File apkPath : apkDirectory.listFiles()) {
					if (apkPath.isDirectory())
						continue;
					if (!apkPath.getName().endsWith(".apk"))
						continue;
				
					apkPathList.add(apkPath.getAbsolutePath());
				}
			}
		}
		
		return apkPathList;
	}
	
	// public static HashMap<String, Integer> mapBroadcast = new HashMap<String, Integer>();
	public static void main(String[] args) {
		redirectSystemOutput();
		HashSet<String> analyzeApks = getAnalyzedApks();
		
		ArrayList<String> apkPathList = iterateApkDirectory();
		int apkIdx = 0;
		for (String apkPath : apkPathList) {
			System.out.println(String.format("apk[%d] -> %s", apkIdx, apkPath));
			apkIdx++;
			
			if (analyzeApks.contains(apkPath)) {
				System.out.println("-->> already analyzed, skip ...");
				continue;
			}
			saveAnalyzedApk(apkPath);
			
			// analysis
			ProcessManifest manifest = null;
			try {
				// -- set up FlowDroid environment
				// SootEnvironment.init(apkPath, Config.platformPath);
				FlowdroidEnvironment.reset();
				FlowdroidEnvironment.init(apkPath, Config.platformPath);
				
				// calculate timeout for FlowDroid
				FlowDroidTimeoutWatcher.timeoutFlag = false; // reset - *very important*
				long timeBeforFD = System.currentTimeMillis();
				
				// run Info-Flow analysis
				int flowdroidArgsSize = FlowdroidEnvironment.args.size();
				String[] flowdroidArgs = new String[flowdroidArgsSize];
				FlowdroidEnvironment.args.toArray(flowdroidArgs);
				ArrayList<InfoflowResults> flowdroidResults = Flowdroid.analyze(flowdroidArgs); 
				
				if (Flowdroid.exceptionFlag == true) {
					System.out.println("---->>>> EXCEPTION");
					continue;
				}
				
				for (SootClass sClass : Scene.v().getClasses()) {
					if (sClass.getName().contains("MainActivity")) {
						for (SootMethod sMethod : sClass.getMethods()) {
							if (sMethod.getName().equals("preLaunchActivity") || sMethod.getName().equals("doLaunchActivity")) {
								System.out.println(sMethod.retrieveActiveBody().toString());
							}
						}
					}
				}
				
				// -- parse AndroidManifest.xml
				manifest = new ProcessManifest(apkPath);
				String packageName = manifest.getPackageName();
				System.out.println(String.format("package name: %s", packageName));
				
				// calculate timeout for FlowDroid
				long timeAfterFD = System.currentTimeMillis();
				long durationFD = timeAfterFD - timeBeforFD;
				if (FlowDroidTimeoutWatcher.timeoutFlag == true) {
					System.out.println("---->>>> TIMEOUT: " + durationFD);
					continue;
				}
				
				// -- patch
				if (packageName.equals("org.telegram.plus"))
					continue;
				
				CallGraph cg = Scene.v().getCallGraph(); // debug
				assert (cg != null);
				// System.out.println(cg); // debug
				
				// global analysis (plz pay attention to the order)
				ManifestAnalysis.reset();
				ManifestAnalysis.analyze(manifest);
				ManifestAnalysis.debugPrint();
				ExecutableAnalysis.reset();
				ExecutableAnalysis.analyze();
				ExecutableAnalysis.debugPrint();
				ReachableAnalysis.reset();
				ReachableAnalysis.analyze();
				
				// 1 - Hide Recent-Task List Item (HTI)
				AnalyzeHTI.reset();
				AnalyzeHTI.analyze(manifest);
				boolean HTIExcludeFromRecents = AnalyzeHTI.outputExcludeFromRecents;
				if (HTIExcludeFromRecents == true) {
					System.out.println("-->> (1) HTI found");
				}
				//
				AnalyzeHTI.stub();
				
				// 2 - Produce Multiple Recent-Task List Items (PMI)
				AnalyzePMI.reset();
				AnalyzePMI.analyze(manifest);
				HashSet<String> PMIMultitaskActivity = AnalyzePMI.outputMultitaskActivity;
				if (PMIMultitaskActivity.size() > 0) {
					System.out.println("-->> (2) PMI found");
					for (String activity : PMIMultitaskActivity) {
						System.out.println("---->>>> " + activity);
					}
				}
				//
				AnalyzePMI.stub();
				
				// 3 - Hold Foreground Activity (HFA)
				AnalyzeHFA.reset();
				AnalyzeHFA.analyze();
				ArrayList<String> HFAReceiver = AnalyzeHFA.outputReceiver;
				ArrayList<HashSet<String>> HFAAction = AnalyzeHFA.outputAction;
				if (HFAReceiver.size() > 0) {
					System.out.println("-->> (3) HFA found");
					for (int idx = 0; idx < HFAReceiver.size(); idx++) {
						String receiverName = HFAReceiver.get(idx);
						HashSet<String> actionSet = HFAAction.get(idx);
						System.out.println("---->>>> " + receiverName + " : " + actionSet);
					}
				}
				//
				AnalyzeHFA.stub();
				
				// 4 - Host Foreground Service (HFS)
				AnalyzeHFS.reset();
				AnalyzeHFS.analyze(manifest);
				HashSet<String> HFSForegroundService = AnalyzeHFS.outputForegroundService;
				if (HFSForegroundService.size() > 0) {
					System.out.println("-->> (4) HFS found");
					for (String service : HFSForegroundService) {
						System.out.println("---->>>> " + service);
					}
				}
				//
				AnalyzeHFS.stub();
				
				// 5 - Create Overlay Window (COW)
				AnalyzeCOW.reset();
				AnalyzeCOW.analyze(manifest);
				boolean COWHasOverlay = AnalyzeCOW.outputOverlay;
				if (COWHasOverlay) {
					System.out.println("-->> (5) COW found");
				}
				//
				AnalyzeCOW.stub();
				
				// 6 - Bind Running Service (BRS)
				AnalyzeBRS.reset();
				AnalyzeBRS.analyze(manifest);
				HashSet<String> BRSService = AnalyzeBRS.outputUnreleasedService;
				if (BRSService.size() > 0) {
					System.out.println("-->> (6) BRS found");
					for (String service : BRSService) {
						System.out.println("---->>>> " + service);
					}
				}
				//
				AnalyzeBRS.stub();
				
				// 7 - Acquire Published Content Provider (ACP)
				AnalyzeACP.reset();
				AnalyzeACP.analyze(manifest, flowdroidResults);
				HashSet<String> ACPProvider = AnalyzeACP.outputUnreleasedProvider;
				if (ACPProvider.size() > 0) {
					System.out.println("-->> (7) ACP found");
					for (String provider : ACPProvider) {
						System.out.println("---->>>> " + provider);
					}
				}
				//
				AnalyzeACP.stub();
				
				// 8 - Construct Sticky Service (CSS)
				AnalyzeCSS.reset();
				AnalyzeCSS.analyze(manifest);
				ArrayList<String> CSSService = AnalyzeCSS.outputStickyService; 
				if (CSSService.size() > 0) {
					System.out.println("-->> (8) CSS found");
					for (String service : CSSService) {
						System.out.println("---->>>> " + service);
					}
				}
				//
				AnalyzeCSS.stub();
				
				// 9 - Monitor System Broadcast (MSB)
				/* 9-0 collect information
				{
					HashSet<String> actionSet = new HashSet<String>();
					for (AXmlNode receivers : manifest.getReceivers()) {
						for (AXmlNode filterNode : receivers.getChildrenWithTag("intent-filter")) {
							for (AXmlNode actionNode : filterNode.getChildrenWithTag("action")) {
								if (!actionNode.hasAttribute("name"))
									continue;
								String actionName = actionNode.getAttribute("name").getValue().toString();
								if (actionName.startsWith("android.intent.action.")) {
									actionSet.add(actionName);
								}
							}
						}
					}
					for (String actionName : actionSet) {
						if (mapBroadcast.containsKey(actionName)) {
							int count = mapBroadcast.get(actionName);
							count++;
							mapBroadcast.put(actionName, count);
						} else {
							mapBroadcast.put(actionName, 1);
						}
					}
					{
						List<Entry<String, Integer>> list = new ArrayList<Entry<String, Integer>>(mapBroadcast.entrySet());
						Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
							public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
								return (o2.getValue() - o1.getValue());
							}
						});
						for(Entry<String, Integer> t:list) {
							System.out.print(t.getKey() + "=" + t.getValue() + ",");
						}
						System.out.print("\n");
						System.out.println(mapBroadcast);
					}
				}
				*/
				// 9-1
				AnalyzeMSB.reset();
				AnalyzeMSB.analyze(manifest);
				boolean MSBSuspiciousBroadcast = AnalyzeMSB.outputHasSuspiciousBraodcast;
				if (MSBSuspiciousBroadcast == true) {
					System.out.println("-->> (9) MSB found");
				}
				//
				AnalyzeMSB.stub();
				
				// 10 - Leverage Alarm Service (LAS)
				AnalyzeLAS.reset();
				AnalyzeLAS.analyze();
				boolean LASHasScheduledTask = AnalyzeLAS.outputHasScheduledTask;
				if (LASHasScheduledTask == true) {
					System.out.println("-->> (10) LAS found");
				}
				//
				AnalyzeLAS.stub();
				
				// 11 - Use Job Scheduling Service (UJS)
				AnalyzeUJS.reset();
				AnalyzeUJS.analyze();
				boolean UJSHasScheduledTask = AnalyzeUJS.outputHasScheduledTask;
				if (UJSHasScheduledTask == true) {
					System.out.println("-->> (11) UJS found");
				}
				//
				AnalyzeUJS.stub();
				
				// 12 - Monitor App Broadcast (MAB)
				AnalyzeMAB.reset();
				AnalyzeMAB.analyze(manifest);
				HashSet<String> MABReceiveBroadcast = AnalyzeMAB.outputReceiveBroadcast;
				HashSet<String> MABSendBroadcast = AnalyzeMAB.outputSendBroadcast;
				System.out.println("-->> (12) MAB analysis finish");
				System.out.println("---->>>> Receive: " + MABReceiveBroadcast);
				System.out.println("---->>>> Send: " + MABSendBroadcast);
				// 
				AnalyzeMAB.stub();
			} catch(Exception e) {
				// do nothing
				e.printStackTrace();
			} finally {
				if (manifest != null)
					manifest.close();
			}
		}
	}
	
	// ---- //
	
	private static void redirectSystemOutput() {
		try {
			PrintStream ps = new PrintStream(new FileOutputStream("system_out.log"));
			System.setOut(ps);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static HashSet<String> getAnalyzedApks() {
		HashSet<String> apks = new HashSet<String>();
		
		BufferedReader br = null;
		try {
			File apksFile = new File(Config.apkLog);
			if (!apksFile.exists()) {
				apksFile.createNewFile();
				return apks;
			}
			br = new BufferedReader(new FileReader(apksFile));
			String apk = null;
			while((apk = br.readLine()) != null) {
				apks.add(apk);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch(Exception e) {
					// do nothing
				}
			}
		}
		
		return apks;
	}
	
	private static void saveAnalyzedApk(String apk) {
		BufferedWriter bw = null;
		try {
			File apksFile = new File(Config.apkLog);
			bw = new BufferedWriter(new FileWriter(apksFile, true));
			bw.write(apk);
			bw.write("\n");
			bw.flush();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch(Exception e) {
					// do nothing
				}
			}
		}
	}

}
