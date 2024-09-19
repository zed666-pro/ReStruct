package addannotation.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;

public class FindClassPath {
    
    IPath fileName;
    
    
    public FindClassPath(IPath fileName) {
        super();
        this.fileName = fileName;
    }

    public List<String> getClassesFile() {
        ArrayList<String> filePaths = new ArrayList();
        File fileClass = new File(fileName.toString());
        if (fileClass.isDirectory()) {

            File[] files = fileClass.listFiles();
            for (File fileTemp : files) {

                if (fileTemp.getName().equals("out")) {
                    File[] fileTarget = fileTemp.listFiles();

                    for (File fileinTarget : fileTarget) {
                        if (fileinTarget.getName().equals("production")) {
                            filePaths.add(fileinTarget.getAbsolutePath());
                         
//                            return filePaths;
                        }
                   
                  }
               } else if (fileTemp.getName().equals("bin")) {
                    filePaths.add(fileTemp.getAbsolutePath());
//                    return filePaths;
                } else if (fileTemp.getName().equals("build")) {
                    filePaths.add(fileTemp.getAbsolutePath());
//                    return filePaths;
                } else if (fileTemp.getName().equals("target") && fileTemp.isDirectory()) {
                    File[] fileTarget = fileTemp.listFiles();

                    for (File fileinTarget : fileTarget) {
                        if (fileinTarget.getName().equals("classes") || fileinTarget.getName().equals("test-classes") && containClass(fileinTarget)) {
                            filePaths.add(fileinTarget.getAbsolutePath());
                            
                        }
                        
                    }
//                    return filePaths;
                    
                }

            }
        }
        return filePaths;
    }

    public  boolean containClass(File fileTemp) {

        if (fileTemp.isDirectory()) {
            boolean label = false;
            for (File f : fileTemp.listFiles()) {
                if (containClass(f))
                    label = true;
            }
            return label;
        } else {
            return fileTemp.getName().endsWith(".class");
        }

    }

}
