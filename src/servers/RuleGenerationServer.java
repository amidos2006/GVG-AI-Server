package servers;

import java.io.File;
import java.io.FileFilter;

import tools.Utils;

public class RuleGenerationServer extends Server{
    private String inputPath;
    private String outputPath;
    private String gvgaiPath;
    private String compPath;
    
    public RuleGenerationServer(String gvgaiPath, String inputPath, String outputPath){
	this.inputPath = inputPath;
	this.outputPath = outputPath;
	this.gvgaiPath = gvgaiPath;
	this.compPath = gvgaiPath + "/dist";
    }
    
    @Override
    public void compile(String filename) throws Exception {
	Utils.compileCode(gvgaiPath + "/", outputPath + "/" + filename + "/build.txt");
    }

    @Override
    public void run(String filename, String packageName) throws Exception {
	Utils.runCode(compPath + "/gvgai.jar", "RuleCompetitionRunner", 
		outputPath + "/" + filename + "/console.txt", 
		"tracks.ruleGeneration." + packageName + ".RuleGenerator",
		gvgaiPath + "/dist",
		outputPath + "/" + filename);
    }
    
    
    @Override
    public String extract(String filename) throws Exception {
	File[] zipFiles = new File(inputPath + "/" + filename + "/").listFiles(new FileFilter() {
	    @Override
	    public boolean accept(File pathname) {
		return pathname.getName().endsWith(".zip");
	    }
	});
	File testingDirectory = new File(inputPath + "/" + filename + "/testing/");
	testingDirectory.mkdirs();
	Utils.extractFiles(inputPath + "/" + filename + "/" + zipFiles[0].getName(), inputPath + "/" + filename + "/testing/");
	File generatorDirectory = Utils.getGeneratorPath("RuleGenerator.java", testingDirectory);
	String packageName = Utils.getPackageName("RuleGenerator.java", "ruleGeneration", generatorDirectory);
	File newDirectory = new File(gvgaiPath + "/tracks/ruleGeneration/" + packageName);
	newDirectory.mkdirs();
	Utils.copyFolder(generatorDirectory, newDirectory);
	return packageName;
    }

}
