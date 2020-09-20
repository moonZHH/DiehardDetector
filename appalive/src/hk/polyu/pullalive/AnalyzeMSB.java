package hk.polyu.pullalive;

import java.util.HashSet;

import hk.polyu.analysis.ManifestAnalysis;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

public class AnalyzeMSB {

	// global
	public static boolean outputHasSuspiciousBraodcast;
	
	public static void reset() {
		outputHasSuspiciousBraodcast = false;
	}
	
	private static String BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
	private static String LOCALE_CHANGED = "android.intent.action.LOCALE_CHANGED";
	private static String LOCKED_BOOT_COMPLETED = "android.intent.action.LOCKED_BOOT_COMPLETED";
	private static String MEDIA_MOUNTED = "android.intent.action.MEDIA_MOUNTED";
	private static String MEDIA_REMOVED = "android.intent.action.MEDIA_REMOVED";
	private static String NEW_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL";
	private static String PACKAGE_FULLY_REMOVED = "android.intent.action.PACKAGE_FULLY_REMOVED";
	private static String TIME_CHANGED = "android.intent.action.TIME_SET";
	private static String TIMEZONE_CHANGED = "android.intent.action.TIMEZONE_CHANGED";
	
	public static void analyze(ProcessManifest manifest) {
		// analyze manifest
		HashSet<String> actionSet = ManifestAnalysis.sysBroadcast;
		if (actionSet.contains(BOOT_COMPLETED)
		 || actionSet.contains(LOCALE_CHANGED)
		 || actionSet.contains(LOCKED_BOOT_COMPLETED)
		 || actionSet.contains(MEDIA_MOUNTED)
		 || actionSet.contains(MEDIA_REMOVED)
		 || actionSet.contains(NEW_OUTGOING_CALL)
		 || actionSet.contains(PACKAGE_FULLY_REMOVED)
		 || actionSet.contains(TIME_CHANGED)
		 || actionSet.contains(TIMEZONE_CHANGED)) {
			outputHasSuspiciousBraodcast = true; // set global
		}
	}
	
	// ---- //
	
	// do nothing
	public static void stub() {}
	
}
