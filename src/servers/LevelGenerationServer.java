package servers;

import java.io.File;
import java.io.FileFilter;

import tools.Utils;

public class LevelGenerationServer extends Server{
    private String inputPath;
    private String outputPath;
    private String gvgaiPath;
    private String compPath;
    
    public LevelGenerationServer(String gvgaiPath, String inputPath, String outputPath){
	this.inputPath = inputPath;
	this.outputPath = outputPath;
	this.gvgaiPath = gvgaiPath;	
	this.compPath = gvgaiPath + "/dist";
    }
    
    @Override
    public void compile(String filename) throws Exception{
	Utils.compileCode(gvgaiPath + "/", outputPath + "/" + filename + "/build.txt");
    }
    
    @Override
    public void run(String filename, String packageName) throws Exception{
	Utils.runCode(compPath + "/gvgai.jar", "LevelCompetitionRunner", 
		outputPath + "/" + filename + "/console.txt", 
		"tracks.levelGeneration." + packageName + ".LevelGenerator", 
		gvgaiPath + "/dist",
		outputPath + "/" + filename);
    }

    @Override
    public String extract(String filename) throws Exception{
	File[] zipFiles = new File(inputPath + "/" + filename + "/").listFiles(new FileFilter() {
	    @Override
	    public boolean accept(File pathname) {
		return pathname.getName().endsWith(".zip");
	    }
	});
	if(zipFiles.length == 0){
	    throw new Exception("Compressed file with \".zip\" extension doesn't exists.");
	}
	File testingDirectory = new File(inputPath + "/" + filename + "/testing/");
	testingDirectory.mkdirs();
	Utils.extractFiles(inputPath + "/" + filename + "/" + zipFiles[0].getName(), inputPath + "/" + filename + "/testing/");
	File generatorDirectory = Utils.getGeneratorPath("LevelGenerator.java", testingDirectory);
	String packageName = Utils.getPackageName("LevelGenerator.java", "levelGeneration", generatorDirectory);
	File newDirectory = new File(gvgaiPath + "/tracks/levelGeneration/" + packageName);
	newDirectory.mkdirs();
	Utils.copyFolder(generatorDirectory, newDirectory);
	return packageName;
    }
}
