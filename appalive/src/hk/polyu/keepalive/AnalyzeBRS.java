package hk.polyu.keepalive;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import hk.polyu.analysis.ExecutableAnalysis;
import hk.polyu.analysis.LocalVariableAnalysis;
import hk.polyu.analysis.ManifestAnalysis;
import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.ClassConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class AnalyzeBRS {
	
	// global
	public static HashSet<String> outputUnreleasedService;
	
	public static void reset() {
		outputUnreleasedService = new HashSet<String>();
	}
	
	public static void analyze(ProcessManifest manifest) {
		// analyze manifest
		HashSet<String> executableComponent = ExecutableAnalysis.executableComponent;
		executableComponent.retainAll(ManifestAnalysis.declaredService);
		executableComponent.retainAll(ManifestAnalysis.processComponent);
		// analyze Dex file
		if (!executableComponent.isEmpty()) {
			parse(executableComponent); // set global
		}
	}
	
	private static String CtxBindService1 = "<android.content.Context: boolean bindService(android.content.Intent,android.content.ServiceConnection,int)>";
	private static String CtxBindService2 = "<android.content.ContextWrapper: boolean bindService(android.content.Intent,android.content.ServiceConnection,int)>";
	private static String CtxBindService3 = "<android.view.ContextThemeWrapper: boolean bindService(android.content.Intent,android.content.ServiceConnection,int)>";
	private static String CtxUnbindService1 = "<android.content.Context: void unbindService(android.content.ServiceConnection)>";
	private static String CtxUnbindService2 = "<android.content.ContextWrapper: void unbindService(android.content.ServiceConnection)>";
	private static String CtxUnbindService3 = "<android.view.ContextThemeWrapper: void unbindService(android.content.ServiceConnection)>";
	private static void parse(HashSet<String> candidateService) {
		// first loop -> find the bound service
		HashSet<SootMethod> appMethods = new HashSet<SootMethod>();
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(CtxBindService1);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(CtxBindService2);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(CtxBindService3);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		
		HashMap<String, Object> mapServiceConnection = new HashMap<String, Object>();
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
				if (tgtMethod.getSignature().equals(CtxBindService1) 
				 || tgtMethod.getSignature().equals(CtxBindService2)
				 || tgtMethod.getSignature().equals(CtxBindService3)) {
					// System.out.println(appMethod.getSignature());
					Value intentValue = tgtExpr.getArg(0);
					if (!(intentValue instanceof Local))
						continue;
					
					String serviceName = null;
					
					Local intentLocal = (Local) intentValue;
					List<Unit> uses = LocalVariableAnalysis.findUses(body, stmt, intentLocal);
					for (Unit use : uses) {
						Stmt useStmt = (Stmt) use;
						if (!useStmt.containsInvokeExpr())
							continue;
						
						InvokeExpr useExpr = useStmt.getInvokeExpr();
						for (Value useArg : useExpr.getArgs()) {
							if (useArg instanceof StringConstant) {
								String constantString = ((StringConstant) useArg).value;
								if (constantString.startsWith("L") && constantString.endsWith(";") && constantString.contains("/")) {
									constantString = constantString.substring(1);
									constantString = constantString.replace("/", ".").replace(";", "");
								}
								// System.out.println(constantString);
								if (candidateService.contains(constantString)) {
									serviceName = constantString;
								}
							}
							if (useArg instanceof ClassConstant) {
								String constantString = ((ClassConstant) useArg).value;
								if (constantString.startsWith("L") && constantString.endsWith(";") && constantString.contains("/")) {
									constantString = constantString.substring(1);
									constantString = constantString.replace("/", ".").replace(";", "");
								}
								// System.out.println(constantString);
								if (candidateService.contains(constantString)) {
									serviceName = constantString;
								}
							}
						}
					}
					
					if (serviceName == null)
						continue;
					
					// System.out.println("bindService -> " + serviceName);
					
					Value connectionValue = tgtExpr.getArg(1);
					if (!(connectionValue instanceof Local))
						continue;
					Local connectionLocal = (Local) connectionValue;
					List<Unit> defs = LocalVariableAnalysis.findDefs(body, stmt, connectionLocal);
					// System.out.println(defs);
					for (Unit def : defs) {
						Stmt defStmt = (Stmt) def;
						if (!(defStmt instanceof AssignStmt))
							continue;
						Value lhs = ((AssignStmt) defStmt).getLeftOp();
						Value rhs = ((AssignStmt) defStmt).getRightOp();
						if (rhs instanceof Local) {
							Local local = (Local) rhs;
							mapServiceConnection.put(serviceName, local);
						}
						// TODO: currently, we do not support this case
						/*
						if (rhs instanceof InstanceFieldRef) {
							SootField field = ((InstanceFieldRef) rhs).getField();
							mapServiceConnection.put(serviceName, field);
						}
						*/
						if (rhs instanceof StaticFieldRef) {
							SootField field = ((StaticFieldRef) rhs).getField();
							mapServiceConnection.put(serviceName, field);
						}
						if (rhs instanceof NewExpr) {
							if (lhs instanceof Local) {
								Local local = (Local) lhs;
								mapServiceConnection.put(serviceName, local);
							}
						}
					}
				}
			}
		}
			
		/* debug
		for (String service : mapServiceConnection.keySet()) {
			Object connection = mapServiceConnection.get(service);
			System.out.println(service + " -> " + connection);
		}
		*/
			
		if (mapServiceConnection.isEmpty())
			return;
			
		// second loop -> find the unbind service
		appMethods = new HashSet<SootMethod>();
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(CtxUnbindService1);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(CtxUnbindService2);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(CtxUnbindService3);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		
		HashSet<Object> mapUnbindConnection = new HashSet<Object>();
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
				if (tgtMethod.getSignature().equals(CtxUnbindService1) 
				 || tgtMethod.getSignature().equals(CtxUnbindService2)
				 || tgtMethod.getSignature().equals(CtxUnbindService3)) {
					Value connectionValue = tgtExpr.getArg(0);
					if (!(connectionValue instanceof Local))
						continue;
					
					Local connectionLocal = (Local) connectionValue;
					List<Unit> defs = LocalVariableAnalysis.findDefs(body, stmt, connectionLocal);
					// System.out.println(defs);
					for (Unit def : defs) {
						Stmt defStmt = (Stmt) def;
						if (!(defStmt instanceof AssignStmt))
							continue;
						Value rhs = ((AssignStmt) defStmt).getRightOp();
						if (rhs instanceof Local) {
							Local local = (Local) rhs;
							// System.out.println("unbind -> " + local);
							mapUnbindConnection.add(local);
						}
						// TODO: currently, we do not support this case
						/*
						if (rhs instanceof InstanceFieldRef) {
							SootField field = ((InstanceFieldRef) rhs).getField();
							// System.out.println("unbind -> " + field);
							mapUnbindConnection.add(field);
						}
						*/
						if (rhs instanceof StaticFieldRef) {
							SootField field = ((StaticFieldRef) rhs).getField();
							// System.out.println("unbind -> " + field);
							mapUnbindConnection.add(field);
						}
					}
				}
			}
		}
		
		//
		for (String service : mapServiceConnection.keySet()) {
			Object connection = mapServiceConnection.get(service);
			if (!mapUnbindConnection.contains(connection))
				outputUnreleasedService.add(service); // set global
		}
	}
	
	// ---- //
	
	// do nothing
	public static void stub() {}

}
