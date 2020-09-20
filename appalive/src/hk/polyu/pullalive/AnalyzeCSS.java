package hk.polyu.pullalive;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import hk.polyu.analysis.ExecutableAnalysis;
import hk.polyu.analysis.LocalVariableAnalysis;
import hk.polyu.analysis.ManifestAnalysis;
import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.ClassConstant;
import soot.jimple.IdentityStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.infoflow.android.manifest.ProcessManifest;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class AnalyzeCSS {
	
	// global
	public static ArrayList<String> outputStickyService;
	
	public static void reset() {
		outputStickyService = new ArrayList<String>();
	}
	
	public static void analyze(ProcessManifest manifest) {
		// assume StartServiceAnalysis.analyze() has been invoked
		HashSet<String> executableComponent = ExecutableAnalysis.executableComponent;
		executableComponent.retainAll(ManifestAnalysis.declaredService); // executable service
		if (!executableComponent.isEmpty()) {
			HashSet<String> serviceSet = findStartService(executableComponent);
			HashSet<String> exportedComponent = ManifestAnalysis.exportedComponent;
			exportedComponent.retainAll(ManifestAnalysis.declaredService); // exported service
			serviceSet.addAll(exportedComponent);
			// 
			HashSet<String> stickyServices = parse(serviceSet);
			// output
			outputStickyService = new ArrayList<String>(stickyServices);
		}
	}
	
	// service
	private static final String ctxStartService1 = "<android.content.Context: android.content.ComponentName startService(android.content.Intent)>";
	private static final String ctxStartService2 = "<android.content.ContextWrapper: android.content.ComponentName startService(android.content.Intent)>";
	private static final String ctxStartService3 = "<android.view.ContextThemeWrapper: android.content.ComponentName startService(android.content.Intent)>";
	private static final String pdStartService1  = "<android.app.PendingIntent: android.app.PendingIntent getForegroundService(android.content.Context,int,android.content.Intent,int)>";
	private static final String pdStartService2  = "<android.app.PendingIntent: android.app.PendingIntent getService(android.content.Context,int,android.content.Intent,int)>";
	
	private static HashSet<String> findStartService(HashSet<String> candidateService) {
		HashSet<String> startedService = new HashSet<String>();
		
		HashSet<SootMethod> appMethods = new HashSet<SootMethod>();
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(ctxStartService1);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(ctxStartService2);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(ctxStartService3);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(pdStartService1);
			Iterator<Edge> edgeIterator = cg.edgesInto(tgtMethod);
			while(edgeIterator.hasNext()) {
				SootMethod srcMethod = edgeIterator.next().src();
				appMethods.add(srcMethod);
			}
		} catch(Exception e) {}
		try {
			CallGraph cg = Scene.v().getCallGraph();
			SootMethod tgtMethod = Scene.v().getMethod(pdStartService2);
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
				if (tgtMethod.getSignature().equals(ctxStartService1)  
				 || tgtMethod.getSignature().equals(ctxStartService2)  
				 || tgtMethod.getSignature().equals(ctxStartService3)) {
					Value intentValue = tgtExpr.getArg(0);
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
								if (candidateService.contains(constantString))
									startedService.add(constantString);
							}
							if (useArg instanceof ClassConstant) {
								String constantString = ((ClassConstant) useArg).value;
								if (constantString.startsWith("L") && constantString.endsWith(";") && constantString.contains("/")) {
									constantString = constantString.substring(1);
									constantString = constantString.replace("/", ".").replace(";", "");
								}
								// System.out.println(constantString);
								if (candidateService.contains(constantString))
									startedService.add(constantString);
							}
						}
					}
				}
				// case-2
				if (tgtMethod.getSignature().equals(pdStartService1) 
				 || tgtMethod.getSignature().equals(pdStartService2)) {
					Value intentValue = tgtExpr.getArg(2);
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
								if (candidateService.contains(constantString))
									startedService.add(constantString);
							}
							if (useArg instanceof ClassConstant) {
								String constantString = ((ClassConstant) useArg).value;
								if (constantString.startsWith("L") && constantString.endsWith(";") && constantString.contains("/")) {
									constantString = constantString.substring(1);
									constantString = constantString.replace("/", ".").replace(";", "");
								}
								// System.out.println(constantString);
								if (candidateService.contains(constantString))
									startedService.add(constantString);
							}
						}
					}
				}
			}
		}
		
		return startedService;
	}
	
	// ---- //
	
	private static int START_STICKY_COMPATIBILITY = 0x00000000;
	private static int START_STICKY = 0x00000001;
	private static int START_NOT_STICKY = 0x00000002;
	private static int START_REDELIVER_INTENT = 0x00000003;
	
	private static int START_UNKNOWN = -1; // fake stub
	
	private static final String serviceOnStartCommand = "<android.app.Service: int onStartCommand(android.content.Intent,int,int)>";
	private static final String intentServiceOnStartCommand = "<android.app.IntentService: int onStartCommand(android.content.Intent,int,int)>";
	
	/*
	 * Totally, there are three conditions:
	 * Condition 1: return a constant value;
	 * Condition 2: return a variable, which is the second parameter of onStartCommand(Intent intent, int flags, int startId);
	 * Condition 3: return a variable, which is assigned with the output of a method invocation or a constant value;
	 */
	public static HashSet<String> parse(HashSet<String> serviceSet) {
		HashSet<String> stickySet = new HashSet<String>();
		
		for (String serviceName : serviceSet) {
			boolean isStickyService = true;
			
			SootClass cService = Scene.v().getSootClass(serviceName);
			assert cService != null;
			
			SootMethod mOnStartCommand = null;
			try {
				mOnStartCommand = cService.getMethod("int onStartCommand(android.content.Intent,int,int)");
			} catch(Exception e) {
				// do nothing
			}
			
			if (mOnStartCommand != null) {
				// System.out.println("source -> " + mOnStartCommand.getSignature());
				Body body = null;
				try {
					body = mOnStartCommand.retrieveActiveBody();
				} catch(Exception e) {
					// do nothing
				}
				if (body == null)
					continue;
				
				for (Unit unit : body.getUnits()) {
					Stmt stmt = (Stmt) unit;
					if (!(stmt instanceof ReturnStmt))
						continue;
					
					// imply stmt is an instance of ReturnStmt
					Value value = ((ReturnStmt) stmt).getOp();	
					if (value instanceof IntConstant) {
						// Condition 1
						List<Integer> outputC1 = parseCondition1(mOnStartCommand, (IntConstant) value);
						isStickyService &= resolveOnStartCommandOutput("[C1]", outputC1);
					} else {
						assert (value instanceof Local);
						Local local = (Local) value;
						
						List<Unit> defs = LocalVariableAnalysis.findDefs(body, stmt, local);
						for (Unit defUnit : defs) {
							Stmt defStmt = (Stmt) defUnit;
							if (defStmt instanceof IdentityStmt) {
								// Condition 2
								List<Integer> outputC2 = parseCondition2(mOnStartCommand, (IdentityStmt) defStmt);
								isStickyService &= resolveOnStartCommandOutput("[C2]", outputC2);
							}
							if (defStmt instanceof AssignStmt) {
								// Condition 3
								List<Integer> outputC3 = parseCondition3(mOnStartCommand, (AssignStmt) defStmt);
								isStickyService &= resolveOnStartCommandOutput("[C3]", outputC3);
							}
						}
					}
				}
			}
			
			if (isStickyService)
				stickySet.add(serviceName);
		}
		
		return stickySet;
	}
	
	// Condition 1
	
	private static List<Integer> parseCondition1(SootMethod sourceMethod, IntConstant intConstant) {
		List<Integer> output = new ArrayList<Integer>();
		output.add(Integer.valueOf(intConstant.value));
		
		return output;
	}
	
	// ----
	
	
	// Condition 2
	
	private static List<Integer> parseCondition2(SootMethod sourceMethod, IdentityStmt identityStmt) {
		List<Integer> output = new ArrayList<Integer>();
		
		// according to AOSP, "(flags & START_FLAG_REDELIVERY) != 0" if and only if "onStartCommand() returns START_REDELIVER_INTENT"
		// conservatively, we treat it as START_UNKNOWN
		output.add(Integer.valueOf(START_UNKNOWN));
		
		return output;
	}
	
	// ----
	
	// Condition 3
	
	private static List<Integer> parseRetIdentityStmt(SootMethod sourceMethod, IdentityStmt identityStmt) {
		List<Integer> output = new ArrayList<Integer>();

		// TODO: future work
		System.err.println("[StickyService]" + " " + identityStmt);
		output.add(Integer.valueOf(START_UNKNOWN));
		
		return output;
	}
	
	private static List<Integer> parseCondition3(SootMethod sourceMethod, AssignStmt assignStmt) {
		List<Integer> output = new ArrayList<Integer>();
		
		if (assignStmt.containsInvokeExpr()) {
			SootMethod targetMethod = assignStmt.getInvokeExpr().getMethod();
			// System.out.println("target -> " + targetMethod.getSignature());
			if (targetMethod.getDeclaringClass().isApplicationClass()) {
				Body body = null;
				try {
					body = targetMethod.retrieveActiveBody();
				} catch(Exception e) {
					// do nothing
				}
				if (body == null) {
					output.add(Integer.valueOf(START_UNKNOWN));
					return output;
				}
				
				for (Unit unit : body.getUnits()) {
					Stmt stmt = (Stmt) unit;
					if (!(stmt instanceof ReturnStmt))
						continue;
					
					// imply stmt is an instance of ReturnStmt
					Value value = ((ReturnStmt) stmt).getOp();	
					if (value instanceof IntConstant) {
						// treat it as Condition 1
						List<Integer> otp = parseCondition1(targetMethod, (IntConstant) value);
						output.addAll(otp);
					} else {
						assert (value instanceof Local);
						Local local = (Local) value;
						
						List<Unit> defs = LocalVariableAnalysis.findDefs(body, stmt, local);
						for (Unit defUnit : defs) {
							Stmt defStmt = (Stmt) defUnit;
							if (defStmt instanceof IdentityStmt) {
								List<Integer> otp = parseRetIdentityStmt(targetMethod, (IdentityStmt) defStmt);
								output.addAll(otp);
							}
							if (defStmt instanceof AssignStmt) {
								// treat it as Condition 3
								List<Integer> otp = parseCondition3(targetMethod, (AssignStmt) defStmt);
								output.addAll(otp);
							}
						}
					}
				}
			} else {
				// targetMethod is a framework API or a supported library method
				if (targetMethod.getSignature().equals(serviceOnStartCommand)) {
					output.add(Integer.valueOf(START_STICKY));
				} else if (targetMethod.getSignature().equals(intentServiceOnStartCommand)) {
					// simply, we check whether the sourceMethod contains the invocation to "setIntentRedelivery()"
					boolean found = false; // indicate whether we have found the critical method
					
					Queue<SootMethod> bfs = new LinkedList<SootMethod>();
					bfs.add(sourceMethod);
					
					// deal with iterated functions
					HashSet<SootMethod> processed = new HashSet<SootMethod>();
					
					// conduct BFS search to find the critical method
					while (!bfs.isEmpty()) {
						SootMethod currentMethod = bfs.poll();
						processed.add(currentMethod);
						
						Body body = null;
						try {
							body = currentMethod.retrieveActiveBody();
						} catch(Exception e) {
							// do nothing
						}
						if (body == null)
							continue;
						
						for (Unit unit : body.getUnits()) {
							Stmt stmt = (Stmt) unit;
							if (!stmt.containsInvokeExpr())
								continue;
						
							InvokeExpr expr = stmt.getInvokeExpr();
							if (expr.getMethod().getDeclaringClass().isApplicationClass()) {
								SootMethod candidateMethod = expr.getMethod();
								if (!processed.contains(candidateMethod))
									bfs.add(candidateMethod);
							} else {
								if (expr.getMethod().getSignature().equals("<android.app.IntentService: void setIntentRedelivery(boolean)>")) {
									found = true;
							
									Value value = expr.getArg(0);
									if (value instanceof IntConstant) {
										if (((IntConstant) value).value == 1)
											output.add(Integer.valueOf(START_REDELIVER_INTENT));
										else
											output.add(Integer.valueOf(START_NOT_STICKY));
									} else {
										// conservatively, we treat the unresolved value as START_NOT_STICKY
										output.add(Integer.valueOf(START_NOT_STICKY));
									}
								}
							}
						}
					}
					
					if (!found)
						output.add(Integer.valueOf(START_NOT_STICKY));
				} else {
					// TODO: future work
					System.err.println("[StickyService]" + " " + targetMethod.getSignature());
					output.add(Integer.valueOf(START_UNKNOWN));
				}
			}
		} else if (assignStmt.getRightOp() instanceof IntConstant) {
			// treat it as Condition 1
			List<Integer> outputC1 = parseCondition1(sourceMethod, (IntConstant) assignStmt.getRightOp());
			output.addAll(outputC1);
		} else {
			// TODO: future work
			System.err.println("[StickyService]" + " " + assignStmt);
			output.add(Integer.valueOf(START_UNKNOWN));
		}
		
		return output;
	}
	
	// ----

	private static boolean resolveOnStartCommandOutput(String banner, List<Integer> output) {
		boolean isStickyService = true;
		
		for (Integer flag : output) {
			// System.out.println(banner + " -> " + flag);
			if (flag == START_STICKY_COMPATIBILITY || flag == START_STICKY || flag == START_REDELIVER_INTENT)
				isStickyService &= true;
			else if (flag == START_NOT_STICKY || flag == START_UNKNOWN)
				isStickyService &= false;
			else
				System.err.println("[StickyService] something wrong ??");
		}
		
		return isStickyService;
	}
	
	// ---- //
	
	// do nothing
	public static void stub() {}
	
}
