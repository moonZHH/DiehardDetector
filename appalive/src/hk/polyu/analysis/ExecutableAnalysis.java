package hk.polyu.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.ClassConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class ExecutableAnalysis {
	
	// global
	/*
	public static HashSet<String> startActivity;
	public static HashSet<String> startService;
	public static HashSet<String> bindService;
	public static HashSet<String> sendReceiver;
	public static HashSet<String> registerReceiver;
	*/
	
	public static HashSet<String> executableComponent;
	
	public static void reset() {
		/*
		startActivity = new HashSet<String>();
		startService = new HashSet<String>();
		bindService = new HashSet<String>();
		sendReceiver = new HashSet<String>();
		registerReceiver = new HashSet<String>();
		*/
		executableComponent = new HashSet<String>();
	}
	
	/*
	public static void analyze() {
		// System.out.println(Scene.v().getCallGraph());
		HashMap<String, HashSet<String>> output = new HashMap<String, HashSet<String>>();
		// activity
		try {
			parse(Scene.v().getMethod(ctxStartActivity1), output);
		} catch(Exception e) {}
		try {
			parse(Scene.v().getMethod(ctxStartActivity2), output);
		} catch(Exception e) {}
		try {
			parse(Scene.v().getMethod(ctxStartActivity3), output);
		} catch(Exception e) {}
		try {
			parse(Scene.v().getMethod(ctxStartActivity4), output);
		} catch(Exception e) {}
		try {
			parse(Scene.v().getMethod(pdStartActivity1), output);
		} catch(Exception e) {}
		// service
		try {
			parse(Scene.v().getMethod(ctxStartService1), output);
		} catch(Exception e) {}
		try {
			parse(Scene.v().getMethod(ctxStartService2), output);
		} catch(Exception e) {}
		try {
			parse(Scene.v().getMethod(ctxStartService3), output);
		} catch(Exception e) {}
		try {
			parse(Scene.v().getMethod(pdStartService1), output);
		} catch(Exception e) {}
		try {
			parse(Scene.v().getMethod(pdStartService2), output);
		} catch(Exception e) {}
		try {
			parse(Scene.v().getMethod(ctxBindService1), output);
		} catch(Exception e) {}
		try {
			parse(Scene.v().getMethod(ctxBindService2), output);
		} catch(Exception e) {}
		try {
			parse(Scene.v().getMethod(ctxBindService3), output);
		} catch(Exception e) {}
		// broadcast-receiver
		try {
			parse(Scene.v().getMethod(ctxSendBroadcast1), output);
		} catch(Exception e) {}
		try {
			parse(Scene.v().getMethod(ctxSendBroadcast2), output);
		} catch(Exception e) {}
		try {
			parse(Scene.v().getMethod(ctxSendBroadcast3), output);
		} catch(Exception e) {}
		try {
			parse(Scene.v().getMethod(pdSendBroadcast1), output);
		} catch(Exception e) {}
		try {
			parse(Scene.v().getMethod(ctxRegisterReceiver1), output);
		} catch(Exception e) {}
		try {
			parse(Scene.v().getMethod(ctxRegisterReceiver2), output);
		} catch(Exception e) {}
		try {
			parse(Scene.v().getMethod(ctxRegisterReceiver3), output);
		} catch(Exception e) {}
		
		// for (String candidate : output.keySet()) {
			// HashSet<String> entrys = output.get(candidate);
			// for (String entry : entrys) {
				// System.out.println(entry + " -> " + candidate);
			// }
		// }
		
		cmpExecutable(output); // set global
	}
	*/
	
	//
	public static void analyze() {
		skip();
	}
	//
	
	private static void cmpExecutable(HashMap<String, HashSet<String>> output) {
		HashSet<String> executable = new HashSet<String>();
		executable.addAll(ManifestAnalysis.exportedComponent);
		boolean stable = false;
		while(stable == false) {
			stable = true;
			for (String candidate : output.keySet()) {
				if (executable.contains(candidate))
					continue;
				
				HashSet<String> entrys = output.get(candidate);
				for (String entry : entrys) {
					if (executable.contains(entry)) {
						executable.add(candidate);
						stable = false;
						break;
					}
				}
			}
		}
		
		executableComponent.addAll(executable); // set global
	}
	
	// activity
	private static final String ctxStartActivity1 = "<android.content.Context: void startActivity(android.content.Intent)>";
	private static final String ctxStartActivity2 = "<android.content.ContextWrapper: void startActivity(android.content.Intent)>";
	private static final String ctxStartActivity3 = "<android.view.ContextThemeWrapper: void startActivity(android.content.Intent)>";
	private static final String ctxStartActivity4 = "<android.app.Activity: void startActivity(android.content.Intent)>";
	private static final String pdStartActivity1  = "<android.app.PendingIntent: android.app.PendingIntent getActivity(android.content.Context,int,android.content.Intent,int)>";
	// service
	private static final String ctxStartService1 = "<android.content.Context: android.content.ComponentName startService(android.content.Intent)>";
	private static final String ctxStartService2 = "<android.content.ContextWrapper: android.content.ComponentName startService(android.content.Intent)>";
	private static final String ctxStartService3 = "<android.view.ContextThemeWrapper: android.content.ComponentName startService(android.content.Intent)>";
	private static final String pdStartService1  = "<android.app.PendingIntent: android.app.PendingIntent getForegroundService(android.content.Context,int,android.content.Intent,int)>";
	private static final String pdStartService2  = "<android.app.PendingIntent: android.app.PendingIntent getService(android.content.Context,int,android.content.Intent,int)>";
	private static final String ctxBindService1  = "<android.content.Context: boolean bindService(android.content.Intent,android.content.ServiceConnection,int)>";
	private static final String ctxBindService2  = "<android.content.ContextWrapper: boolean bindService(android.content.Intent,android.content.ServiceConnection,int)>";
	private static final String ctxBindService3  = "<android.view.ContextThemeWrapper: boolean bindService(android.content.Intent,android.content.ServiceConnection,int)>";
	// broadcast
	private static final String ctxSendBroadcast1 = "<android.content.Context: void sendBroadcast(android.content.Intent)>";
	private static final String ctxSendBroadcast2 = "<android.content.ContextWrapper: void sendBroadcast(android.content.Intent)>";
	private static final String ctxSendBroadcast3 = "<android.view.ContextThemeWrapper: void sendBroadcast(android.content.Intent)>";
	private static final String pdSendBroadcast1  = "<android.app.PendingIntent: android.app.PendingIntent getBroadcast(android.content.Context,int,android.content.Intent,int)>";
	private static final String ctxRegisterReceiver1 = "<android.content.Context: android.content.Intent registerReceiver(android.content.BroadcastReceiver,android.content.IntentFilter)>";
	private static final String ctxRegisterReceiver2 = "<android.content.ContextWrapper: android.content.Intent registerReceiver(android.content.BroadcastReceiver,android.content.IntentFilter)>";
	private static final String ctxRegisterReceiver3 = "<android.view.ContextThemeWrapper: android.content.Intent registerReceiver(android.content.BroadcastReceiver,android.content.IntentFilter)>";
	// intent
	private static final String intSetComponent1 = "<android.content.Intent: android.content.Intent setComponent(android.content.ComponentName)>";
	
	public static void parse(SootMethod tgtMethod, HashMap<String, HashSet<String>> output) {
		CallGraph cg = Scene.v().getCallGraph();
		Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
		while(edgeIterator.hasNext()) {
			SootMethod srcMethod = edgeIterator.next().src();
			
			Body body = null;
			try {
				body = srcMethod.retrieveActiveBody();
			} catch(Exception e) {
				srcMethod.releaseActiveBody();
				body = null;
			}
			if (body == null)
				continue;
			
			for (Unit unit : body.getUnits()) {
				Stmt stmt = (Stmt) unit;
				if (!stmt.containsInvokeExpr())
					continue;
				
				InvokeExpr expr = stmt.getInvokeExpr();
				SootMethod targetMethod = expr.getMethod();
				
				// case-1
				if (targetMethod.getSignature().equals(ctxStartActivity1) || targetMethod.getSignature().equals(ctxStartActivity2) || targetMethod.getSignature().equals(ctxStartActivity3) || targetMethod.getSignature().equals(ctxStartActivity4) 
				 || targetMethod.getSignature().equals(ctxStartService1)  || targetMethod.getSignature().equals(ctxStartService2)  || targetMethod.getSignature().equals(ctxStartService3) 
				 || targetMethod.getSignature().equals(ctxBindService1)   || targetMethod.getSignature().equals(ctxBindService2)   || targetMethod.getSignature().equals(ctxBindService3)
				 || targetMethod.getSignature().equals(ctxSendBroadcast1) || targetMethod.getSignature().equals(ctxSendBroadcast2) || targetMethod.getSignature().equals(ctxSendBroadcast3) 
				 || targetMethod.getSignature().equals(intSetComponent1)) {
					// System.out.println(targetMethod);
					Value intentValue = expr.getArg(0);
					if (!(intentValue instanceof Local))
						continue;
					Local intentLocal = (Local) intentValue;
					
					List<Unit> uses = LocalVariableAnalysis.findUses(body, stmt, intentLocal);
					for (Unit useUnit : uses) {
						if (!(useUnit instanceof Stmt))
							continue;
						
						Stmt useStmt = (Stmt) useUnit;
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
								resolve(srcMethod, constantString, output);
							}
							if (useArg instanceof ClassConstant) {
								String constantString = ((ClassConstant) useArg).value;
								if (constantString.startsWith("L") && constantString.endsWith(";") && constantString.contains("/")) {
									constantString = constantString.substring(1);
									constantString = constantString.replace("/", ".").replace(";", "");
								}
								// System.out.println(constantString);
								resolve(srcMethod, constantString, output);
							}
						}
					}
				}
				// case-2
				if (targetMethod.getSignature().equals(pdStartActivity1)
				 || targetMethod.getSignature().equals(pdStartService1) || targetMethod.getSignature().equals(pdStartService2)
				 || targetMethod.getSignature().equals(pdSendBroadcast1)) {
					// System.out.println(targetMethod);
					Value intentValue = expr.getArg(2);
					if (!(intentValue instanceof Local))
						continue;
					Local intentLocal = (Local) intentValue;
					
					List<Unit> uses = LocalVariableAnalysis.findUses(body, stmt, intentLocal);
					for (Unit useUnit : uses) {
						if (!(useUnit instanceof Stmt))
							continue;
						
						Stmt useStmt = (Stmt) useUnit;
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
								resolve(srcMethod, constantString, output);
							}
							if (useArg instanceof ClassConstant) {
								String constantString = ((ClassConstant) useArg).value;
								if (constantString.startsWith("L") && constantString.endsWith(";") && constantString.contains("/")) {
									constantString = constantString.substring(1);
									constantString = constantString.replace("/", ".").replace(";", "");
								}
								// System.out.println(constantString);
								resolve(srcMethod, constantString, output);
							}
						}
					}
				}
				// case-3
				if (targetMethod.getSignature().equals(ctxRegisterReceiver1) || targetMethod.getSignature().equals(ctxRegisterReceiver2) || targetMethod.getSignature().equals(ctxRegisterReceiver3)) {
					// System.out.println(targetMethod);
					Value receiverValue = expr.getArg(0);
					if (!(receiverValue instanceof Local))
						continue;
					Local receiverLocal = (Local) receiverValue;
					List<Unit> defs = LocalVariableAnalysis.findDefs(body, stmt, receiverLocal);
					for (Unit defUnit : defs) {
						// TODO: currently, we do not support complicated cases
						if (!(defUnit instanceof AssignStmt))
							continue;
						Value rhs = ((AssignStmt) defUnit).getRightOp();
						if (!(rhs instanceof NewExpr))
							continue;
						String receiverName = ((NewExpr) rhs).getType().toString();
						// System.out.println(receiverName);
						resolve(srcMethod, receiverName, output);
					}
				}
			}
		}
	}
	
	private static void resolve(SootMethod leaf, String candidate, HashMap<String, HashSet<String>> output) {
		HashSet<String> declaredComponent = new HashSet<String>();
		declaredComponent.addAll(ManifestAnalysis.declaredActivity);
		declaredComponent.addAll(ManifestAnalysis.declaredService);
		declaredComponent.addAll(ManifestAnalysis.declaredReceiver);
		declaredComponent.addAll(ManifestAnalysis.declaredProvider);
		if (declaredComponent.contains(candidate) || (Scene.v().getSootClassUnsafe(candidate, false) != null)) {
			HashSet<SootMethod> predecessors = ReachableAnalysis.cmpPredecessor(leaf);
			for (SootMethod predecessor : predecessors) {
				String componentName = predecessor.getReturnType().toString();
				if (componentName.equals("void")) {
					componentName = ManifestAnalysis.mainApplication;
					assert (componentName != null);
				}
				// System.out.println("[DEBUG] " + componentName + " -->> " + candidate + " -->> " + leaf.getSignature());
				if (!output.containsKey(candidate))
					output.put(candidate, new HashSet<String>());
				output.get(candidate).add(componentName);
			}
		}
	}
	
	private static void skip() {
		HashSet<String> executable = new HashSet<String>();
		CallGraph cg = Scene.v().getCallGraph();
		SootMethod dummyMain = Scene.v().getMethod("<dummyMainClass: void dummyMainMethod(java.lang.String[])>");
		Iterator<Edge> edgeIterator = cg.edgesOutOf(dummyMain);
		while(edgeIterator.hasNext()) {
			Edge curEdge = edgeIterator.next();
			SootMethod tgtMethod = curEdge.tgt();
			String componentName = tgtMethod.getReturnType().toString();
			if (componentName.equals("void")) {
				componentName = ManifestAnalysis.mainApplication;
			}
			executable.add(componentName);
		}
		
		executableComponent.addAll(executable); // set global
	}
	
	// ---- //
	
	public static void debugPrint() {
		// executable components
		System.out.println("## executable Component ##");
		for (String componentName : executableComponent) {
			System.out.println("  -->> " + componentName);
		}
	}
	
	// do nothing
	public static void stub() {}
}
