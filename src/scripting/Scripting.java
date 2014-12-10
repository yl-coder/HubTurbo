package scripting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.controlsfx.dialog.Dialogs;

public class Scripting {
	
	private ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
	private HashMap<String, String> scriptFiles = new HashMap<>();
	
	private ArrayList<Path> getScripts() {

		File directory = new File("scripts");
		if (!directory.exists()) {
			directory.mkdir();
		}

		final ArrayList<Path> result = new ArrayList<Path>();
		try {
			Files.walk(Paths.get("scripts")).forEach(filePath -> {
				if (Files.isRegularFile(filePath)
					&& filePath.getFileName().toString().endsWith(".js")) {
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
				return temp[temp.length-1].replaceAll("\\..*$", "");
			}).collect(Collectors.toList());
		
		for (int i=0; i<names.size(); i++) {
			scriptFiles.put(names.get(i), getScripts().get(i).toString());
		}
		
		return names;
	}
	
	public static void alert(String message) {
		Dialogs.create().lightweight().title("Alert").message(message).showInformation();
	}
	
	public void run(String scriptName) {
		if (scriptFiles.containsKey(scriptName)) {
			System.out.println("Scripting: running " + scriptName);
			try {
				engine.eval(new FileReader(scriptFiles.get(scriptName)));
			} catch (FileNotFoundException | ScriptException e) {
				System.out.println("Scripting: " + scriptName + " failed");
				e.printStackTrace();
			}
		} else {
			System.out.println("Scripting: nothing done");
			// Do nothing
		}
	}
	
	public static void main(String[] args) {
		Scripting s = new Scripting();
		s.getScriptNames();
		s.run("test");
	}
}
