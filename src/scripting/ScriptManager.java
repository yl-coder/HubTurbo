package scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import model.Model;

import org.controlsfx.dialog.Dialogs;

import service.ServiceManager;

public class ScriptManager {

	private String prelude;
	private ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
	private HashMap<String, String> scriptFiles = new HashMap<>();
	private Model model;

	public ScriptManager() {
		prelude = readPrelude();
		model = ServiceManager.getInstance().getModel();
	}

	private ArrayList<Path> getScripts() {

		File directory = new File("scripts");
		if (!directory.exists()) {
			directory.mkdir();
		}

		final ArrayList<Path> result = new ArrayList<Path>();
		try {
			Files.walk(Paths.get("scripts")).forEach(filePath -> {
				if (Files.isRegularFile(filePath) && filePath.getFileName().toString().endsWith(".js")) {
					result.add(filePath);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public List<String> getScriptNames() {
		List<String> names = getScripts().stream().map(p -> {
			String[] temp = p.toString().split("\\/");
			return temp[temp.length - 1].replaceAll("\\..*$", "");
		}).collect(Collectors.toList());

		for (int i = 0; i < names.size(); i++) {
			scriptFiles.put(names.get(i), getScripts().get(i).toString());
		}

		return names;
	}

	public static void alert(String message) {
		Dialogs.create().lightweight().title("Alert").message(message).showInformation();
	}

	public static void scriptError(String message) {
		Dialogs.create().lightweight().title("HubTurbo Script Error").message(message).showInformation();
	}

	public void run(String scriptName) {
		if (scriptFiles.containsKey(scriptName)) {
			System.out.println("Scripting: running " + scriptName);
			try {
				String script = prelude + readFile(scriptFiles.get(scriptName));
				
				List<Issue> issues = model.getIssues().stream().map(issue -> new Issue(issue)).collect(Collectors.toList());
				List<Milestone> milestones = model.getMilestones().stream().map(milestone -> new Milestone(milestone)).collect(Collectors.toList());
				List<User> users = model.getCollaborators().stream().map(user -> new User(user)).collect(Collectors.toList());
				List<Label> labels = model.getLabels().stream().map(label -> new Label(label)).collect(Collectors.toList());
				
				engine.getBindings(ScriptContext.ENGINE_SCOPE).put("issues", issues);
				engine.getBindings(ScriptContext.ENGINE_SCOPE).put("milestones", milestones);
				engine.getBindings(ScriptContext.ENGINE_SCOPE).put("users", users);
				engine.getBindings(ScriptContext.ENGINE_SCOPE).put("labels", labels);

				engine.eval(script);
				System.out.println("Scripting: finished running " + scriptName);
			} catch (Exception e) {
				System.out.println("Scripting: " + scriptName + " failed");
				try {
					engine.eval(String.format("scriptError('%s');", e.toString()));
				} catch (ScriptException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		} else {
			// Do nothing
			System.out.println("Scripting: nothing done");
		}
	}

	private String readFile(String path) throws IOException {
		File file = new File(path);
		BufferedReader reader;
		StringBuilder sb = new StringBuilder();
		reader = new BufferedReader(new FileReader(file));
		String line;
		while ((line = reader.readLine()) != null) {
			sb.append(line);
			sb.append("\n");
		}
		reader.close();
		return sb.toString();
	}

	private String readPrelude() {
		ClassLoader classLoader = ScriptManager.class.getClassLoader();
		File file = new File(classLoader.getResource("scripting/prelude.js").getFile());
		BufferedReader reader;
		StringBuilder sb = new StringBuilder();
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
			reader.close();
			return sb.toString();
		} catch (Exception e) {
			System.out.println("Unable to read Prelude.js");
			e.printStackTrace();
		}
		return "";
	}

//	public static void main(String[] args) throws Exception {
//		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
//		engine.getBindings(ScriptContext.ENGINE_SCOPE).put("a", 1);
//		ScriptObjectMirror o = (ScriptObjectMirror) engine.eval("var y = 7; var a = {x : 1}; a;");
//		o.setMember("y", 2);
//		// System.out.println(o.get("y"));
//
//		SimpleBindings b = new SimpleBindings();
//		b.put("y", o);
//		ArrayList<Integer> asdlkjask = new ArrayList<Integer>();
//		asdlkjask.add(100);
//		b.put("i", asdlkjask);
//		Issue i = new Issue();
//		i.setId(1);
//		b.put("z", i);
//		// b.put("z", new TurboIssue("issue #1", "aksjd", null));
//		engine.setBindings(b, ScriptContext.ENGINE_SCOPE);
//		// System.out.println(engine.eval("i[1];"));
//		// System.out.println(engine.eval("y.x;"));
//		// System.out.println(((ScriptObjectMirror)
//		// engine.eval("[false, 7];")).get("0"));
//		// System.out.println(engine.eval("for (var x in z) {print('x ' + x);}"));
//		System.out.println(engine.eval("z.title;"));
//		System.out.println(engine.eval("z.getTitle;"));
//		System.out.println(engine.eval("z.getTitle();"));
//
//		// this is the hack way
//		// better way is to just pass arbitrary java objects
//
//		// s.eval("manager.test(); manager.class.static.test2();");
//		// call static methd
//
//		// array
//		// JSObject obj = (JSObject)invocable.invokeFunction("avg", srcC, 2);
//		// Collection result = obj.values();
//		// for (Object o : result) {
//		// System.out.println(o);
//		// }
//		// convert to native array
//		// import jdk.nashorn.api.scripting.ScriptUtils;
//		// ...
//		// int[] iarr = (int[])ScriptUtils.convert(arr, int[].class)
//
//		// javadoc
//		// https://wiki.openjdk.java.net/display/Nashorn/Nashorn+jsr223+engine+notes
//	}
}
