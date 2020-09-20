package hk.polyu;

import java.util.ArrayList;

import soot.G;

public class FlowdroidEnvironment {
	
	public static ArrayList<String> args; 
	
	public static void reset() {
		args = new ArrayList<String>();
	}
	
	public static void init(String apkPath, String platformPath) throws Exception {
		// Clean up any old Soot instance we may have
		G.reset();
		
		// configure Flowdroid arguments
		args.add("-a"); args.add(apkPath);
		args.add("-p"); args.add(platformPath);
		args.add("-s"); args.add(Config.taintFile);
		args.add("-r");
		args.add("-tw"); args.add("NONE");
		args.add("-t"); args.add(Config.wrapperFile);
		args.add("-cp");
		args.add("-d");
		args.add("-ps");
		args.add("-cg"); args.add("SPARK");
		// args.add("-ce"); args.add("NONE");
		
		// timeout
		args.add("-dt"); args.add("60"); // seconds
		args.add("-ct"); args.add("60"); // seconds
		args.add("-rt"); args.add("60"); // seconds
	}

}
