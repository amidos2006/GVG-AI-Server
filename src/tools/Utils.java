package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class Utils {
    public static void copyFolder(File source, File destination) throws Exception{
	if(source.isDirectory()){
            //Get all files from source directory
            String files[] = source.list();
             
            //Iterate over all files and copy them to destinationFolder one by one
            for (String file : files) 
            {
                File srcFile = new File(source, file);
                File destFile = new File(destination, file);
                copyFolder(srcFile, destFile);
            }
	}
	else{
	    if(!destination.exists()){
		destination.mkdirs();
	    }
	    Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
    }
    
    public static void extractFiles(String inputFile, String outputDest) throws Exception {
	Process pro = Runtime.getRuntime().exec("unzip -o " + inputFile + " -d " + outputDest);
	pro.waitFor();
	
	String line = null;
	BufferedReader in = new BufferedReader(new InputStreamReader(pro.getErrorStream()));
	String errorMsg = "";
	while ((line = in.readLine()) != null) {
	    errorMsg += line;
	}
	if(errorMsg.length() > ("").length()){
	    throw new Exception(errorMsg);
	}
    }
    
    public static File getGeneratorPath(String fileName, File directory) throws Exception{
	File[] generatorFiles = directory.listFiles();
	for(File f : generatorFiles){
	    if(f.getName().equals(fileName)){
		return directory;
	    }
	    if(f.isDirectory()){
		return Utils.getGeneratorPath(fileName, f);
	    }
	}
	throw new Exception(fileName + " doesn't exists in any of the extracted folders.");
    }
    
    public static String getPackageName(String fileName, String trackName, File directory) throws Exception{
	File generatorFile = directory.listFiles(new FileFilter() {
	    @Override
	    public boolean accept(File pathname) {
		return pathname.getName().equals(fileName);
	    }
	})[0];
	
	List<String> codeLines = Files.readAllLines(generatorFile.toPath());
	for(String line : codeLines){
	    if(line.contains("package")){
		String packageName = line.trim().replace(";", "").split(" ")[1];
		if(packageName.split("\\.").length == 3 && packageName.contains("tracks." + trackName)){
		    return packageName.split("\\.")[2];
		}
	    }
	}
	throw new Exception("The package naming is in the incorrect format.");
    }
    
    public static void compileCode(String codePath, String buildPath) throws Exception {
	ProcessBuilder process = new ProcessBuilder("ant", "-buildfile", codePath);
	process.redirectOutput(new File(buildPath));
	Process p = process.start();
	p.waitFor();
	
	String line = null;
	BufferedReader in = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	String errorMsg = "";
	while ((line = in.readLine()) != null) {
	    errorMsg += line;
	}
	if(errorMsg.length() > ("").length()){
	    throw new Exception(errorMsg);
	}
    }
    
    public static void runCode(String compiledFile, String mainClass, String userConsole, 
	    String generator, String currentPath, String outPath) throws Exception{
	String absPath = (new File("")).getAbsolutePath();
	ProcessBuilder process = new ProcessBuilder("java", "-cp", compiledFile, mainClass, 
		generator, absPath + "/" + currentPath, absPath + "/" + outPath);
	process.redirectOutput(new File(userConsole));
	process.start();
    }
    
    public static void copySpritesExamples(String sourcePath, String gvgaiPath) throws Exception{
	Utils.copyFolder(new File(sourcePath + "/examples/"), new File(gvgaiPath + "/dist/examples/"));
	Utils.copyFolder(new File(sourcePath + "/sprites/"), new File(gvgaiPath + "/dist/sprites/"));
    }
    
    public static void deleteFile(File file) throws Exception{
	for (File childFile : file.listFiles()) {
	    if (childFile.isDirectory()) {
		deleteFile(childFile);
	    } 
	    else {
		if (!childFile.delete()) {
		    throw new Exception("Error in deleting " + childFile.getPath());
		}
	    }
	}
	if (!file.delete()) {
	    throw new Exception("Error in deleting " + file.getPath());
	}
    }
    
    public static void newCopy(String sourcePath, String gvgaiPath) throws Exception{
	if(new File(gvgaiPath + "/").exists()){
	    deleteFile(new File(gvgaiPath + "/"));
	}
	(new File(gvgaiPath + "/")).mkdir();
	Utils.copyFolder(new File(sourcePath + "/"), new File(gvgaiPath + "/"));
    }
    
    public static File[] getNewFiles(String inputPath, String outputPath){
	File[] outputFolders = new File(outputPath + "/").listFiles();
	File[] results = new File(inputPath + "/").listFiles(new FileFilter() {
	    @Override
	    public boolean accept(File pathname) {
		boolean r = pathname.isDirectory();
		if(!r){
		    return false;
		}
		for(File f:outputFolders){
		    if(pathname.getName().indexOf(f.getName()) >= 0){
			return false;
		    }
		}
		return true;
	    }
	});
	
	return results;
    }
}
