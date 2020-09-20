package soot.jimple.infoflow.android.entryPointCreators.components;

import soot.SootClass;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.Stmt;
import soot.jimple.infoflow.android.entryPointCreators.AndroidEntryPointConstants;

/**
 * Entry point creator for Android broadcast receivers
 * 
 * @author Steven Arzt
 *
 */
public class BroadcastReceiverEntryPointCreator extends AbstractComponentEntryPointCreator {

	public BroadcastReceiverEntryPointCreator(SootClass component, SootClass applicationClass) {
		super(component, applicationClass);
	}

	@Override
	protected void generateComponentLifecycle() {
		Stmt onReceiveStmt = searchAndBuildMethod(AndroidEntryPointConstants.BROADCAST_ONRECEIVE, component, thisLocal);

		// methods
		NopStmt startWhileStmt = Jimple.v().newNopStmt();
		NopStmt endWhileStmt = Jimple.v().newNopStmt();
		body.getUnits().add(startWhileStmt);
		createIfStmt(endWhileStmt);

		addCallbackMethods();

		body.getUnits().add(endWhileStmt);
		createIfStmt(onReceiveStmt);
	}

}
