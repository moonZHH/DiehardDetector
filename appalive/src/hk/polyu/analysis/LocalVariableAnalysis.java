package hk.polyu.analysis;

import java.util.ArrayList;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;

public class LocalVariableAnalysis {
	
	public static List<Unit> findDefs(Body body, Stmt stmt, Local local) {
		Unit unit = (Unit) stmt;
		UnitGraph cfg = new BriefUnitGraph(body);
		SimpleLocalDefs defsResolver = new SimpleLocalDefs(cfg);
		List<Unit> defs = defsResolver.getDefsOfAt(local, unit);
		
		return defs;
	}
	
	public static List<Unit> findUses(Body body, Stmt stmt, Local local) {
		Unit unit = (Unit) stmt;
		UnitGraph cfg = new BriefUnitGraph(body);
		SimpleLocalDefs defsResolver = new SimpleLocalDefs(cfg);
		SimpleLocalUses usesResolver = new SimpleLocalUses(cfg, defsResolver);
		
		List<Unit> uses = new ArrayList<Unit>();
		List<Unit> defs = defsResolver.getDefsOfAt(local, unit);
		for (Unit defUnit : defs) {
			List<UnitValueBoxPair> pairs = usesResolver.getUsesOf(defUnit);
			for (UnitValueBoxPair pair : pairs) {
				uses.add(pair.unit);
			}
		}
		
		return uses;
	}

}
