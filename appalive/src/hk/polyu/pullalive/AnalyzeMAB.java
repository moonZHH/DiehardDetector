package hk.polyu.pullalive;

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
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class AnalyzeMAB {
	
	// global
	public static HashSet<String> outputReceiveBroadcast;
	public static HashSet<String> outputSendBroadcast;
	
	public static void reset() {
		outputReceiveBroadcast = new HashSet<String>();
		outputSendBroadcast = new HashSet<String>();
	}
	
	public static void analyze(ProcessManifest manifest) {
		// analyze manifest -> receive broadcast
		HashSet<String> receiveBraodcast = ManifestAnalysis.appBroadcast;
		// System.out.println(receiveBraodcast);
		// analyze Dex file -> send broadcast
		HashSet<String> sendBraodcast = parse();
		// output
		outputReceiveBroadcast = receiveBraodcast;
		outputSendBroadcast = sendBraodcast;
	}
	
	private static String CtxSendBroadcast1 = "<android.content.Context: void sendBroadcast(android.content.Intent)>";
	private static String CtxSendBroadcast2 = "<android.content.ContextWrapper: void sendBroadcast(android.content.Intent)>";
	private static String CtxSendBroadcast3 = "<android.view.ContextThemeWrapper: void sendBroadcast(android.content.Intent)>";
	private static String pdSendBroadcast1  = "<android.app.PendingIntent: android.app.PendingIntent getBroadcast(android.content.Context,int,android.content.Intent,int)>";
	private static String IntSetAction      = "<android.content.Intent: android.content.Intent setAction(java.lang.String)>";
	private static String IntAddFlags       = "<android.content.Intent: android.content.Intent addFlags(int)>";
	private static String IntSetFlags       = "<android.content.Intent: android.content.Intent setFlags(int)>";
	private static HashSet<String> parse() {
		HashSet<String> sendBraodcast = new HashSet<String>();
		
		HashSet<SootMethod> appMethods = new HashSet<SootMethod>();
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(CtxSendBroadcast1);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(CtxSendBroadcast2);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(CtxSendBroadcast3);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(pdSendBroadcast1);
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
				// case-1
				if (tgtMethod.getSignature().equals(CtxSendBroadcast1) 
				 || tgtMethod.getSignature().equals(CtxSendBroadcast2)
				 || tgtMethod.getSignature().equals(CtxSendBroadcast3)) {
					Value intentValue = tgtExpr.getArg(0);
					if (!(intentValue instanceof Local))
						continue;
					Local intentLocal = (Local) intentValue;
					List<Unit> uses = LocalVariableAnalysis.findUses(body, stmt, intentLocal);
					// System.out.println(uses);
					boolean flag1 = false; // FLAG_INCLUDE_STOPPED_PACKAGE
					boolean flag2 = false; // 
					for (Unit useUnit : uses) {
						if (!(useUnit instanceof InvokeStmt))
							continue;
						InvokeStmt useStmt = (InvokeStmt) useUnit;
						InvokeExpr useExpr = useStmt.getInvokeExpr();
						SootMethod useMethod = useExpr.getMethod();
						if (useMethod.getSignature().equals(IntAddFlags) || useMethod.getSignature().equals(IntSetFlags)) {
							Value intValue = useExpr.getArg(0);
							if (intValue instanceof IntConstant) {
								int flag = ((IntConstant) intValue).value;
								if (flag == 0x00000020)
									flag1 = true; // FLAG_INCLUDE_STOPPED_PACKAGE
								if (flag == 0x01000000)
									flag2 = true; // 
							}
							if (intValue instanceof Local) {
								Local intLocal = (Local) intValue;
								List<Unit> defs = LocalVariableAnalysis.findDefs(body, useStmt, intLocal);
								for (Unit defUnit : defs) {
									if (!(defUnit instanceof AssignStmt))
										continue;
									Value rhs = ((AssignStmt) defUnit).getRightOp();
									if (rhs instanceof IntConstant) {
										int flag = ((IntConstant) rhs).value;
										if (flag == 0x00000020)
											flag1 = true; // FLAG_INCLUDE_STOPPED_PACKAGE
										if (flag == 0x01000000)
											flag2 = true; // 
									}
								}
							}
						}
					}
					if (flag1 && flag2) {
						for (Unit useUnit : uses) {
							if (!(useUnit instanceof InvokeStmt))
								continue;
							InvokeStmt useStmt = (InvokeStmt) useUnit;
							InvokeExpr useExpr = useStmt.getInvokeExpr();
							SootMethod useMethod = useExpr.getMethod();
							if (useMethod.getSignature().equals(IntSetAction)) {
								Value stringValue = useExpr.getArg(0);
								if (stringValue instanceof StringConstant) {
									String actionName = ((StringConstant) stringValue).value;
									if (!actionName.startsWith("android.intent.action."))
										sendBraodcast.add(actionName);
								}
								if (stringValue instanceof Local) {
									Local stringLocal = (Local) stringValue;
									List<Unit> defs = LocalVariableAnalysis.findDefs(body, useStmt, stringLocal);
									for (Unit defUnit : defs) {
										if (!(defUnit instanceof AssignStmt))
											continue;
										Value rhs = ((AssignStmt) defUnit).getRightOp();
										if (rhs instanceof StringConstant) {
											String actionName = ((StringConstant) rhs).value;
											if (!actionName.startsWith("android.intent.action."))
												sendBraodcast.add(actionName);
										}
									}
								}
							}
						}
					}
				}
				// case-2
				if (tgtMethod.getSignature().equals(pdSendBroadcast1)) {
					Value intentValue = tgtExpr.getArg(2);
					if (!(intentValue instanceof Local))
						continue;
					Local intentLocal = (Local) intentValue;
					List<Unit> uses = LocalVariableAnalysis.findUses(body, stmt, intentLocal);
					// System.out.println(uses);
					boolean flag1 = false; // FLAG_INCLUDE_STOPPED_PACKAGE
					boolean flag2 = false; // 
					for (Unit useUnit : uses) {
						if (!(useUnit instanceof InvokeStmt))
							continue;
						InvokeStmt useStmt = (InvokeStmt) useUnit;
						InvokeExpr useExpr = useStmt.getInvokeExpr();
						SootMethod useMethod = useExpr.getMethod();
						if (useMethod.getSignature().equals(IntAddFlags) || useMethod.getSignature().equals(IntSetFlags)) {
							Value intValue = useExpr.getArg(0);
							if (intValue instanceof IntConstant) {
								int flag = ((IntConstant) intValue).value;
								if (flag == 0x00000020)
									flag1 = true; // FLAG_INCLUDE_STOPPED_PACKAGE
								if (flag == 0x01000000)
									flag2 = true; // 
							}
							if (intValue instanceof Local) {
								Local intLocal = (Local) intValue;
								List<Unit> defs = LocalVariableAnalysis.findDefs(body, useStmt, intLocal);
								for (Unit defUnit : defs) {
									if (!(defUnit instanceof AssignStmt))
										continue;
									Value rhs = ((AssignStmt) defUnit).getRightOp();
									if (rhs instanceof IntConstant) {
										int flag = ((IntConstant) rhs).value;
										if (flag == 0x00000020)
											flag1 = true; // FLAG_INCLUDE_STOPPED_PACKAGE
										if (flag == 0x01000000)
											flag2 = true; // 
									}
								}
							}
						}
					}
					if (flag1 && flag2) {
						for (Unit useUnit : uses) {
							if (!(useUnit instanceof InvokeStmt))
								continue;
							InvokeStmt useStmt = (InvokeStmt) useUnit;
							InvokeExpr useExpr = useStmt.getInvokeExpr();
							SootMethod useMethod = useExpr.getMethod();
							if (useMethod.getSignature().equals(IntSetAction)) {
								Value stringValue = useExpr.getArg(0);
								if (stringValue instanceof StringConstant) {
									String actionName = ((StringConstant) stringValue).value;
									if (!actionName.startsWith("android.intent.action."))
										sendBraodcast.add(actionName);
								}
								if (stringValue instanceof Local) {
									Local stringLocal = (Local) stringValue;
									List<Unit> defs = LocalVariableAnalysis.findDefs(body, useStmt, stringLocal);
									for (Unit defUnit : defs) {
										if (!(defUnit instanceof AssignStmt))
											continue;
										Value rhs = ((AssignStmt) defUnit).getRightOp();
										if (rhs instanceof StringConstant) {
											String actionName = ((StringConstant) rhs).value;
											if (!actionName.startsWith("android.intent.action."))
												sendBraodcast.add(actionName);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		return sendBraodcast;
	}
	
	// ---- //
	
	// do nothing
	public static void stub() {}

}
