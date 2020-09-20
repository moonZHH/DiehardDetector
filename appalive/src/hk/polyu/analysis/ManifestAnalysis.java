package hk.polyu.analysis;

import java.util.HashMap;
import java.util.HashSet;

import soot.Scene;
import soot.SootClass;
import soot.jimple.infoflow.android.axml.AXmlNode;
import soot.jimple.infoflow.android.manifest.ProcessManifest;

public class ManifestAnalysis {
	
	// global
	public static String mainApplication;
	public static String mainActivity;
	public static String mainTaskAffinity;
	public static String mainProcess;
	public static HashSet<String> exportedComponent; // for activity, service, broadcast-receiver, content-provider
	public static HashSet<String> processComponent; // for service, content-provider
	public static HashSet<String> taskAffinityComponent; // for activity
	public static HashSet<String> excludeRecentsComponent; // for activity
	public static HashSet<String> sysBroadcast; // for receiver
	public static HashSet<String> appBroadcast; // for receiver
	public static HashMap<String, String> authorityMap; // for content-provider
	
	public static HashSet<String> declaredActivity;
	public static HashSet<String> declaredService;
	public static HashSet<String> declaredReceiver;
	public static HashSet<String> declaredProvider;
	
	public static void reset() {
		mainApplication = null;
		mainActivity = null;
		mainTaskAffinity = null;
		mainProcess = null;
		exportedComponent = new HashSet<String>();
		processComponent = new HashSet<String>();
		taskAffinityComponent = new HashSet<String>();
		excludeRecentsComponent = new HashSet<String>();
		sysBroadcast = new HashSet<String>();
		appBroadcast = new HashSet<String>();
		authorityMap = new HashMap<String, String>();
		
		declaredActivity = new HashSet<String>();
		declaredService = new HashSet<String>();
		declaredReceiver = new HashSet<String>();
		declaredProvider = new HashSet<String>();
	}
	
	public static void analyze(ProcessManifest manifest) {
		String packageName = manifest.getPackageName();
		mainApplication = manifest.getApplicationName(); // set global
		if (mainApplication != null)
			exportedComponent.add(mainApplication);
		// main activity
		for (AXmlNode activityNode : manifest.getActivities()) {
			for (AXmlNode intentFilter : activityNode.getChildrenWithTag("intent-filter")) {
				boolean isMain = false;
				boolean isLaunchable = false;
				for (AXmlNode node : intentFilter.getChildren()) {
					if (node.hasAttribute("name") && node.getAttribute("name").getValue().toString().equals("android.intent.action.MAIN"))
						isMain = true;
					if (node.hasAttribute("name") && node.getAttribute("name").getValue().toString().equals("android.intent.category.LAUNCHER"))
						isLaunchable = true;
				}
				if (isMain && isLaunchable) {
					// name
					if (activityNode.hasAttribute("name")) {
						String activityName = activityNode.getAttribute("name").getValue().toString();
						activityName = adjustComponentName(packageName, activityName);
						mainActivity = activityName; // set global
					} else {
						mainActivity = null; // set global
					}
					// taskAffinity
					if (activityNode.hasAttribute("taskAffinity")) {
						String taskAffinity = activityNode.getAttribute("taskAffinity").getValue().toString();
						mainTaskAffinity = taskAffinity; // set global
					} else {
						mainTaskAffinity = null; // set global
					}
					// process
					if (activityNode.hasAttribute("process")) {
						String process = activityNode.getAttribute("process").getValue().toString();
						mainProcess = process; // set global
					} else {
						mainProcess = null; // set global
					}
				}
			}
		}
		
		// inspect activity
		for (AXmlNode activityNode : manifest.getActivities()) {
			if (!activityNode.hasAttribute("name"))
				continue;
			String activityName = activityNode.getAttribute("name").getValue().toString();
			activityName = adjustComponentName(packageName, activityName);
			declaredActivity.add(activityName); // set global
			// 1-exported
			boolean isExported = false;
			if (activityNode.hasAttribute("exported")) {
				String exported = activityNode.getAttribute("exported").getValue().toString();
				if (exported.equals("true"))
					isExported = true;
			} else {
				if (!activityNode.getChildrenWithTag("intent-filter").isEmpty())
					isExported = true;
			}
			if (isExported)
				exportedComponent.add(activityName); // set global
			// 2-taskAffinity
			if (activityNode.hasAttribute("taskAffinity")) {
				String taskAffinity = activityNode.getAttribute("taskAffinity").getValue().toString();
				if ((mainTaskAffinity == null) || (!taskAffinity.equals(mainTaskAffinity)))
					taskAffinityComponent.add(activityName); // set global
			}
			// 3-excludeRecents
			if (activityNode.hasAttribute("excludeFromRecents")) {
				String excludeFromRecents = activityNode.getAttribute("excludeFromRecents").getValue().toString();
				if (excludeFromRecents.equals("true"))
					excludeRecentsComponent.add(activityName); // set global
			}
		}
		
		// inspect service
		for (AXmlNode serviceNode : manifest.getServices()) {
			if (!serviceNode.hasAttribute("name"))
				continue;
			String serviceName = serviceNode.getAttribute("name").getValue().toString();
			serviceName = adjustComponentName(packageName, serviceName);
			declaredService.add(serviceName); // set global
			// 1-exported
			boolean isExported = false;
			if (serviceNode.hasAttribute("exported")) {
				String exported = serviceNode.getAttribute("exported").getValue().toString();
				if (exported.equals("true"))
					isExported = true;
			} else {
				if (!serviceNode.getChildrenWithTag("intent-filter").isEmpty())
					isExported = true;
			}
			if (isExported)
				exportedComponent.add(serviceName); // set global
			// 2-process
			if (serviceNode.hasAttribute("process")) {
				String process = serviceNode.getAttribute("process").getValue().toString();
				if ((mainProcess == null) || (!process.equals(mainProcess)))
					processComponent.add(serviceName); // set global
			}
		}
		
		// inspect broadcast-receiver
		for (AXmlNode receiverNode : manifest.getReceivers()) {
			if (!receiverNode.hasAttribute("name"))
				continue;
			String receiverName = receiverNode.getAttribute("name").getValue().toString();
			receiverName = adjustComponentName(packageName, receiverName);
			declaredReceiver.add(receiverName); // set global
			// 1-exported
			boolean isExported = false;
			if (receiverNode.hasAttribute("exported")) {
				String exported = receiverNode.getAttribute("exported").getValue().toString();
				if (exported.equals("true"))
					isExported = true;
			} else {
				if (!receiverNode.getChildrenWithTag("intent-filter").isEmpty())
					isExported = true;
			}
			if (isExported)
				exportedComponent.add(receiverName); // set global
			//2-broadcast
			for (AXmlNode filterNode : receiverNode.getChildrenWithTag("intent-filter")) {
				for (AXmlNode actionNode : filterNode.getChildrenWithTag("action")) {
					if (!actionNode.hasAttribute("name"))
						continue;
					String actionName = actionNode.getAttribute("name").getValue().toString();
					if (actionName.startsWith("android.intent.action.")) {
						sysBroadcast.add(actionName); // set global
					} else {
						appBroadcast.add(actionName); // set global
					}
				}
			}
		}
		
		// inspect content provider
		for (AXmlNode providerNode : manifest.getProviders()) {
			if (!providerNode.hasAttribute("name"))
				continue;
			String providerName = providerNode.getAttribute("name").getValue().toString();
			providerName = adjustComponentName(packageName, providerName);
			declaredProvider.add(providerName); // set global
			// 1-exported
			boolean isExported = true; // TODO: assume the content-provider is accessible
			if (providerNode.hasAttribute("exported")) {
				String exported = providerNode.getAttribute("exported").getValue().toString();
				if (exported.equals("true"))
					isExported = true;
			} else {
				if (!providerNode.getChildrenWithTag("intent-filter").isEmpty())
					isExported = true;
			}
			if (isExported)
				exportedComponent.add(providerName); // set global
			// 2-process
			if (providerNode.hasAttribute("process")) {
				String process = providerNode.getAttribute("process").getValue().toString();
				if ((mainProcess == null) || (!process.equals(mainProcess)))
					processComponent.add(providerName); // set global
			}
			// 3-authorities
			if (providerNode.hasAttribute("authorities")) {
				String authority = providerNode.getAttribute("authorities").getValue().toString();
				authorityMap.put(authority, providerName); // set global
			}
		}
	}
	
	public static void debugPrint() {
		// mainApplication
		if (mainApplication != null)
			System.out.println("mainApplication -> " + mainApplication);
		else
			System.out.println("mainApplication -> " + "null");
		// mainActivity
		if (mainActivity != null)
			System.out.println("mainActivity -> " + mainActivity);
		else
			System.out.println("mainActivity -> " + "null");
		// mainTaskAffinity
		if (mainTaskAffinity != null)
			System.out.println("mainTaskAffinity -> " + mainTaskAffinity);
		else 
			System.out.println("mainTaskAffinity -> " + "null");
		// mainProces
		if (mainProcess != null)
			System.out.println("mainProcess -> " + mainProcess);
		else
			System.out.println("mainProcess -> " + "null");
		// exported component
		System.out.println("## exported Component ##");
		for (String componentName : exportedComponent) {
			System.out.println("  -->> " + componentName);
		}
		// process component
		System.out.println("## process Component ##");
		for (String componentName : processComponent) {
			System.out.println("  -->> " + componentName);
		}
		// taskAffinity component
		System.out.println("## taskAffinity Component ##");
		for (String componentName : taskAffinityComponent) {
			System.out.println("  -->> " + componentName);
		}
		// excludeFromRecents component
		System.out.println("## excludeFromRecents Component ##");
		for (String componentName : excludeRecentsComponent) {
			System.out.println("  -->> " + componentName);
		}
		// system broadcast
		System.out.println("## system broadcast ##");
		for (String broadcast : sysBroadcast) {
			System.out.println("  -->> " + broadcast);
		}
		// system broadcast
		System.out.println("## APP broadcast ##");
		for (String broadcast : appBroadcast) {
			System.out.println("  -->> " + broadcast);
		}
		// authority map
		System.out.println("## authority Map ##");
		for (String authority : authorityMap.keySet()) {
			String providerName = authorityMap.get(authority);
			System.out.println("  -->> " + providerName + " : " + authority);
		}
	}
	
	// ---- //
	
	// auxiliary
	private static String adjustComponentName(String packageName, String componentName) {
		String realName = null;
		SootClass componentClass = Scene.v().getSootClassUnsafe(componentName);
		if (componentClass == null || componentClass.isPhantom()) {
			if (componentClass.getName().startsWith("."))
				componentName = packageName + componentName;
			else
				componentName = packageName + "." + componentName;
			
			componentClass = Scene.v().getSootClassUnsafe(componentName);
			assert componentClass.isApplicationClass();
			
			realName = componentName;
		} else
			realName = componentName;
		
		return realName;
	}
	
	// do nothing
	public static void stub() {}

}
