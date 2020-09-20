package hk.polyu.keepalive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import hk.polyu.analysis.LocalVariableAnalysis;
import hk.polyu.analysis.ManifestAnalysis;
import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.infoflow.results.DataFlowResult;
import soot.jimple.infoflow.results.InfoflowResults;
import soot.jimple.infoflow.results.ResultSourceInfo;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class AnalyzeACP {
	
	// global
	public static HashSet<String> outputUnreleasedProvider;
	
	public static void reset() {
		outputUnreleasedProvider = new HashSet<String>();
	}
	
	public static void analyze(ProcessManifest manifest, ArrayList<InfoflowResults> flowdroidResults) {
		// analyze manifest
		HashMap<String, String> authorityMap = ManifestAnalysis.authorityMap;
		HashSet<String> processComponent = ManifestAnalysis.processComponent;
		HashSet<String> removeAuthority = new HashSet<String>();
		for (String authority : authorityMap.keySet()) {
			String providerName = authorityMap.get(authority);
			if (!processComponent.contains(providerName))
				removeAuthority.add(authority);
		}
		for (String authority : removeAuthority) {
			authorityMap.remove(authority);
		}
		// System.out.println(authorityMap);
		// analyze code
		HashMap<Stmt, String> unreleasedProviders = null;
		if (!authorityMap.isEmpty()) {
			unreleasedProviders = parse(authorityMap);
		}
		// System.out.println(unreleasedProviders);
		// analyze info-flow result
		for (InfoflowResults infoflowResult : flowdroidResults) {
			if (infoflowResult.getResultSet() == null)
				continue;
			for (DataFlowResult dataFlowResult : infoflowResult.getResultSet()) {
				ResultSourceInfo source = dataFlowResult.getSource();
				Stmt sourceStmt = source.getStmt();
				unreleasedProviders.remove(sourceStmt);
			}
		}
		// output
		if (unreleasedProviders != null && !unreleasedProviders.isEmpty()) {
			for (String uri : unreleasedProviders.values()) {
				String providerName = authorityMap.get(uri);
				outputUnreleasedProvider.add(providerName); // set global
			}
		}
		// System.out.println(outputProviders);
	}
	
	private static String CPCAcquireSTR = "<android.content.ContentResolver: android.content.ContentProviderClient acquireContentProviderClient(java.lang.String)>";
	private static String CPCAcquireURI = "<android.content.ContentResolver: android.content.ContentProviderClient acquireContentProviderClient(android.net.Uri)>";
	private static String CPCUAcquireSTR = "<android.content.ContentResolver: android.content.ContentProviderClient acquireUnstableContentProviderClient(java.lang.String)>";
	private static String CPCUAcquireURI = "<android.content.ContentResolver: android.content.ContentProviderClient acquireUnstableContentProviderClient(android.net.Uri)>";
	// private static String CPCClose = "<android.content.ContentProviderClient: void close()>";
	// private static String CPCRelease = "<android.content.ContentProviderClient: boolean release()>";
	private static HashMap<Stmt, String> parse(HashMap<String, String> providerMap) {
		HashMap<Stmt, String> suspicousMap = new HashMap<Stmt, String>();
		
		HashSet<SootMethod> appMethods = new HashSet<SootMethod>();
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(CPCAcquireSTR);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(CPCAcquireURI);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(CPCUAcquireSTR);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(CPCUAcquireURI);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		
		for (SootMethod appMethod : appMethods) {
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
				if (tgtMethod.getSignature().equals(CPCAcquireSTR) 
				 || tgtMethod.getSignature().equals(CPCAcquireURI)
				 || tgtMethod.getSignature().equals(CPCUAcquireSTR)
				 || tgtMethod.getSignature().equals(CPCUAcquireURI)) {
					Value value = tgtExpr.getArg(0);
					if (value instanceof StringConstant) {
						String providerName = ((StringConstant) value).value;
						System.out.println(providerName);
						continue;
					}
					if (!(value instanceof Local))
						continue;
					Local local = (Local) value;
					List<Unit> defs = LocalVariableAnalysis.findDefs(body, stmt, local);
					// System.out.println(defs);
					for (Unit def : defs) {
						if (!(def instanceof AssignStmt))
							continue;
						Value rhs = ((AssignStmt) def).getRightOp();
						if (!(rhs instanceof InvokeExpr))
							continue;
						InvokeExpr expr = (InvokeExpr) rhs;
						for (Value arg : expr.getArgs()) {
							if (arg instanceof StringConstant) {
								String uri = ((StringConstant) arg).value;
								if (uri.startsWith("content://"))
									uri = uri.replace("content://", "");
								suspicousMap.put(stmt, uri);
							}
						}
					}
				}
			}
		}
		
		return suspicousMap;
	}
	
	// ---- //

	// do nothing
	public static void stub() {}
}
