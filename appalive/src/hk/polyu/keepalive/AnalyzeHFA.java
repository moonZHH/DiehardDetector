package hk.polyu.keepalive;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import hk.polyu.analysis.LocalVariableAnalysis;
import hk.polyu.analysis.ReachableAnalysis;
import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class AnalyzeHFA {
	
	// global
	public static ArrayList<String> outputReceiver;
	public static ArrayList<HashSet<String>> outputAction;
	
	public static void reset() {
		outputReceiver = new ArrayList<String>();
		outputAction = new ArrayList<HashSet<String>>();
	}
	
	public static void analyze() {
		parse();
	}
	
	// parse registerReceiver API
	private static String CtxRegisterReceiver1 = "<android.content.Context: android.content.Intent registerReceiver(android.content.BroadcastReceiver,android.content.IntentFilter)>";
	private static String CtxRegisterReceiver2 = "<android.content.ContextWrapper: android.content.Intent registerReceiver(android.content.BroadcastReceiver,android.content.IntentFilter)>";
	private static String CtxRegisterReceiver3 = "<android.view.ContextThemeWrapper: android.content.Intent registerReceiver(android.content.BroadcastReceiver,android.content.IntentFilter)>";
	private static void parse() {
		// Step-1: collect registered receivers
		CallGraph cg = Scene.v().getCallGraph();
		HashSet<SootMethod> appMethods = new HashSet<SootMethod>();
		try {
			Iterator<Edge> edgeIterator = cg.edgesInto(Scene.v().getMethod(CtxRegisterReceiver1));
			while(edgeIterator.hasNext()) {
				Edge edge = edgeIterator.next();
				appMethods.add(edge.src());
			}
		} catch(Exception e) {}
		try {
			Iterator<Edge> edgeIterator = cg.edgesInto(Scene.v().getMethod(CtxRegisterReceiver2));
			while(edgeIterator.hasNext()) {
				Edge edge = edgeIterator.next();
				appMethods.add(edge.src());
			}
		} catch(Exception e) {}
		try {
			Iterator<Edge> edgeIterator = cg.edgesInto(Scene.v().getMethod(CtxRegisterReceiver3));
			while(edgeIterator.hasNext()) {
				Edge edge = edgeIterator.next();
				appMethods.add(edge.src());
			}
		} catch(Exception e) {}
		
		ArrayList<String> receiverList = new ArrayList<String>();
		ArrayList<HashSet<String>> actionList = new ArrayList<HashSet<String>>();
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
				if (tgtMethod.getSignature().equals(CtxRegisterReceiver1) 
				 || tgtMethod.getSignature().equals(CtxRegisterReceiver2)
				 || tgtMethod.getSignature().equals(CtxRegisterReceiver3)) {
					// determine receiver class
					Value receiverValue = tgtExpr.getArg(0);
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
						receiverList.add(receiverName);
					}
					// determine accepted action
					Value filterValue = tgtExpr.getArg(1);
					if (!(filterValue instanceof Local))
						continue;
					Local filterLocal = (Local) filterValue;
					List<Unit> uses = LocalVariableAnalysis.findUses(body, stmt, filterLocal);
					HashSet<String> actionSet = new HashSet<String>();
					for (Unit useUnit : uses) {
						// TODO: currently, we do not support complicated cases
						if (!(useUnit instanceof InvokeStmt))
							continue;
						InvokeStmt useStmt = (InvokeStmt) useUnit;
						InvokeExpr expr = useStmt.getInvokeExpr();
						if (expr.getArgCount() == 0)
							continue;
						for (Value argValue : expr.getArgs()) {
							if (!(argValue instanceof StringConstant))
								continue;
							String actionName = ((StringConstant) argValue).value;
							if (actionName.startsWith("android.intent.action.") || true) {
								// TODO
								actionSet.add(actionName);
							}
						}
					}
					actionList.add(actionSet);
					// check
					if (receiverList.size() != actionList.size()) {
						// System.err.println(receiverList);
						return;
					}
				}
			}
		}
		
		// Step-2: check whether the receiver will launch an Activity
		for (int receiverIdx = 0; receiverIdx < receiverList.size(); receiverIdx++) {
			String receiverName = receiverList.get(receiverIdx);
			SootClass receiverClass = Scene.v().getSootClass(receiverName);
			for (SootMethod receiverMethod : receiverClass.getMethods()) {
				// System.out.println(receiverMethod.getSignature());
				HashSet<SootMethod> successors = ReachableAnalysis.cmpSuccessor(receiverMethod);
				for (SootMethod candidate : successors) {
					// System.out.println(candidate);
					SootClass sc = candidate.getDeclaringClass();
					if (sc.isApplicationClass())
						continue;
					if (candidate.getSignature().contains("startActivit")
					 || candidate.getSignature().equals("<android.app.PendingIntent: android.app.PendingIntent getActivity(android.content.Context,int,android.content.Intent,int)>")) {
						// System.out.println("---- ---- ---- ----");
						outputReceiver.add(receiverName);
						outputAction.add(actionList.get(receiverIdx));
					}
				}
			}
		}
	}
	
	// do nothing
	public static void stub() {}

}
