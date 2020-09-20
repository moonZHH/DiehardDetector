package hk.polyu.keepalive;

import java.util.HashSet;
import java.util.Iterator;

import hk.polyu.analysis.ManifestAnalysis;
import soot.Scene;
import soot.SootMethod;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.InvokeStmt;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class AnalyzeHTI {
	
	// global
	public static boolean outputExcludeFromRecents;
	
	public static void reset() {
		outputExcludeFromRecents = false;
	}
	
	public static void analyze(ProcessManifest manifest) {
		// 1 - check the manifest file
		String mainActivity = ManifestAnalysis.mainActivity;
		HashSet<String> exportedComponent = ManifestAnalysis.exportedComponent;
		HashSet<String> excludeRecentsComponent = ManifestAnalysis.excludeRecentsComponent;
		
		if (mainActivity != null && excludeRecentsComponent.contains(mainActivity)) {
			outputExcludeFromRecents = true; // set global
			System.out.println("-->> [HTI] found a special case !!!");
			return;
		}
		
		excludeRecentsComponent.retainAll(exportedComponent);
		if (!excludeRecentsComponent.isEmpty()) {
			outputExcludeFromRecents = true; // set global
			for (String componentName : excludeRecentsComponent) {
				System.out.println("[DEBUG-HTI] excludeFromRecents -->> " + componentName);
			}
		}
		
		/*
		for (AXmlNode activityNode : manifest.getActivities()) {
			if (activityNode.hasAttribute("excludeFromRecents")) {
				String flag = activityNode.getAttribute("excludeFromRecents").getValue().toString();
				if (!flag.equals("true"))
					continue;
				// case - 1. main Activity
				for (AXmlNode intentFilter : activityNode.getChildrenWithTag("intent-filter")) {
					boolean isMain = false;
					boolean isLaunchable = false;
					for (AXmlNode node : intentFilter.getChildren()) {
						if (node.hasAttribute("name") && node.getAttribute("name").getValue().toString().equals("android.intent.action.MAIN"))
							isMain = true;
						if (node.hasAttribute("name") && node.getAttribute("name").getValue().toString().equals("android.intent.category.LAUNCHER"))
							isLaunchable = true;
					}
					if (isMain && isLaunchable) {
						isExcludeFromRecents = true;
						System.out.println("-->> [HTI] found a special case !!!");
						break;
					}
				}
				// case - 2. other Activity
				if (!activityNode.hasAttribute("exported"))
					continue;
				String isExported = activityNode.getAttribute("exported").getValue().toString();
				if (isExported.equals("true"))
					isExcludeFromRecents = true;
			}
		}
		*/
		
		// 2 - check the dex file
		if (outputExcludeFromRecents != true)
			outputExcludeFromRecents = parse(); // set global
	}
	
	private static String ATSetExcludeFromRecents = "<android.app.ActivityManager$AppTask: void setExcludeFromRecents(boolean)>";
	private static boolean parse() {
		boolean isExcludeFromRecents = false;
		
		SootMethod tgtMethod = null;
		try {
			tgtMethod = Scene.v().getMethod(ATSetExcludeFromRecents);
		} catch (Exception e) {
			return isExcludeFromRecents;
		}
		assert (tgtMethod != null);
		
		CallGraph cg = Scene.v().getCallGraph();
		Iterator<Edge> tgtIterator = cg.edgesInto(tgtMethod);
		while (tgtIterator.hasNext()) {
			Edge tgtEdge = tgtIterator.next();
			InvokeStmt tgtStmt = (InvokeStmt) tgtEdge.srcStmt();
			Value tgtValue = tgtStmt.getInvokeExpr().getArg(0);
			if (tgtValue instanceof IntConstant) {
				int tgtFlag = ((IntConstant) tgtValue).value;
				if (tgtFlag == 1) {
					isExcludeFromRecents = true;
					System.out.println("[DEBUG-HTI] -->> HTI found in the Dex File");
				}
			}
		}
		
		return isExcludeFromRecents;
	}
	
	// ---- //
	
	// do nothing
	public static void stub() {}

}
