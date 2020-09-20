package hk.polyu.keepalive;

import java.util.HashSet;

import hk.polyu.analysis.ExecutableAnalysis;
import hk.polyu.analysis.ManifestAnalysis;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

public class AnalyzeHFS {
	
	// global
	public static HashSet<String> outputForegroundService;
	
	public static void reset() {
		outputForegroundService = new HashSet<String>();
	}
	
	public static void analyze(ProcessManifest manifest) {
		// 1 - collect declared service components
		HashSet<String> executableComponent = ExecutableAnalysis.executableComponent;
		executableComponent.retainAll(ManifestAnalysis.declaredService);
		// 2 - parse 
		if (!executableComponent.isEmpty()) {
			parse(executableComponent); // set global
		}
	}
	
	private static String startForegroundSig = "<android.app.Service: void startForeground(int,android.app.Notification)>";
	public static void parse(HashSet<String> executableService) {
		for (String serviceName : executableService) {
			SootClass serviceClass = Scene.v().getSootClass(serviceName);
			for (SootMethod appMethod : serviceClass.getMethods()) {
				if (!appMethod.isConcrete())
					continue;
				// System.out.println(appMethod.getSignature());
				
				Body body = null;
				try {
					body = appMethod.retrieveActiveBody();
				} catch(Exception e) {
					appMethod.releaseActiveBody();
					body = null;
				}
				if (body == null)
					continue;
				
				for (Unit unit : body.getUnits()) {
					Stmt stmt = (Stmt) unit;
					if (!stmt.containsInvokeExpr())
						continue;
					
					InvokeExpr tgtExpr = stmt.getInvokeExpr();
					SootMethod tgtMethod = tgtExpr.getMethod();
					if (tgtMethod.getSignature().equals(startForegroundSig)) {
						outputForegroundService.add(serviceName); // set global
					}
				}
			}
		}
	}
	
	// ---- //
	
	// do nothing
	public static void stub() {}
	
}
