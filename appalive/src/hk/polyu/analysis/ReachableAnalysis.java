package hk.polyu.analysis;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class ReachableAnalysis {
	
	// global
	public static HashSet<SootMethod> reachableMethod;
	
	public static void reset() {
		reachableMethod = new HashSet<SootMethod>();
	}
	
	public static void analyze() {
		// prune call graph
		pruneCG();
		// collect reachable method
		CallGraph cg = Scene.v().getCallGraph();
		Iterator<Edge> cgIterator = cg.iterator();
		while (cgIterator.hasNext()) {
			Edge edge = cgIterator.next();
			SootMethod srcMethod = edge.src();
			reachableMethod.add(srcMethod);
		}
	}
	
	private static void pruneCG() {
		// collect the root Edge to be removed
		HashSet<Edge> removeEdge = new HashSet<Edge>();
		HashSet<String> executableComponent = ExecutableAnalysis.executableComponent;
		CallGraph cg = Scene.v().getCallGraph();
		SootMethod dummyMain = Scene.v().getMethod("<dummyMainClass: void dummyMainMethod(java.lang.String[])>");
		Iterator<Edge> mainIterator = cg.edgesOutOf(dummyMain);
		while(mainIterator.hasNext()) {
			Edge mainEdge = mainIterator.next();
			SootMethod tgtMethod = mainEdge.tgt();
			String returnType = tgtMethod.getReturnType().toString();
			if (returnType.equals("void")) {
				returnType = ManifestAnalysis.mainApplication;
				assert (returnType != null);
			}
			if (!executableComponent.contains(returnType))
				removeEdge.add(mainEdge);
		}
		// prune
		Stack<Edge> stack = new Stack<Edge>();
		for (Edge edge : removeEdge) {
			stack.push(edge);
			while (!stack.isEmpty()) {
				Edge curEdge = stack.pop();
				// System.out.println("[DEBUG] remove -> " + curEdge);
				cg.removeEdge(curEdge);
				
				SootMethod tgtMethod = curEdge.tgt();
				Iterator<Edge> srcIterator = cg.edgesInto(tgtMethod);
				if(srcIterator.hasNext())
					continue;
				
				Iterator<Edge> tgtIterator = cg.edgesOutOf(tgtMethod);
				while(tgtIterator.hasNext()) {
					Edge nextEdge = tgtIterator.next();
					stack.push(nextEdge);
				}
			}
		}
		Scene.v().setCallGraph(cg);
	}
	
	// ---- //
	
	public static HashSet<SootMethod> cmpSuccessor(SootMethod root) {
		HashSet<SootMethod> successor = new HashSet<SootMethod>();
		// compute
		CallGraph cg = Scene.v().getCallGraph();
		Iterator<Edge> edgeIterator = cg.edgesOutOf(root);
		while (edgeIterator.hasNext()) {
			Queue<Edge> queue = new LinkedList<Edge>();
			HashSet<Edge> handled = new HashSet<Edge>();
			
			Edge edge = edgeIterator.next();
			queue.add(edge);
			while (!queue.isEmpty()) {
				Edge curEdge = queue.poll();
				if (handled.contains(curEdge))
					continue;
				handled.add(curEdge);
				
				SootMethod tgtMethod = curEdge.tgt();
				successor.add(tgtMethod);
				Iterator<Edge> curIterator = cg.edgesOutOf(tgtMethod);
				while (curIterator.hasNext()) {
					Edge nxtEdge = curIterator.next();
					queue.add(nxtEdge);
				}
			}
		}
		// output
		return successor;
	}
	
	public static HashSet<SootMethod> cmpPredecessor(SootMethod leaf) {
		HashSet<SootMethod> predecessors = new HashSet<SootMethod>();
		// compute
		CallGraph cg = Scene.v().getCallGraph();
		Iterator<Edge> edgeIterator = cg.edgesInto(leaf);
		while (edgeIterator.hasNext()) {
			Queue<Edge> queue = new LinkedList<Edge>();
			HashSet<Edge> handled = new HashSet<Edge>();
			
			Edge edge = edgeIterator.next();
			queue.add(edge);
			while (!queue.isEmpty()) {
				Edge curEdge = queue.poll();
				if (handled.contains(curEdge))
					continue;
				handled.add(curEdge);
				
				SootMethod srcMethod = curEdge.src();
				if (srcMethod.getSignature().equals("<dummyMainClass: void dummyMainMethod(java.lang.String[])>"))
					predecessors.add(curEdge.tgt());
				
				Iterator<Edge> curIterator = cg.edgesInto(srcMethod);
				while (curIterator.hasNext()) {
					Edge nxtEdge = curIterator.next();
					queue.add(nxtEdge);
				}
			}
		}
		// output
		return predecessors;
	}

}
