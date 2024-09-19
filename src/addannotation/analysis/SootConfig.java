package addannotation.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Scale;

import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;
import soot.util.Chain;

public class SootConfig {
      


        public  List<String> excludeClassList;

        public void setupSoot(List<String> filePaths){
            String[] processDirs = {"dir1", "dir2", "dir3"};
            G.reset();
            Options.v().set_prepend_classpath(true);
            Options.v().set_allow_phantom_refs(true);
            Options.v().set_keep_line_number(true);

//          Options.v().set_soot_classpath("/Library/Java/JavaVirtualMachines/jdk1.8.0_361.jdk/Contents/Home/jre/lib/rt.jar:/Library/Java/JavaVirtualMachines/jdk1.8.0_361.jdk/Contents/Home/jre/lib/jce.jar");
            Options.v().set_output_format(Options.output_format_jimple);
//          Options.v().set_process_dir(Collections.singletonList(sourceDirectory));
            Options.v().set_process_dir(filePaths);
            Options.v().set_whole_program(true);
            Options.v().set_verbose(true);
            Options.v().setPhaseOption("jb","use-original-names:true");
//            Options.v().setPhaseOption("jb.dae","only-stack-locals:true"); // 不去优化b = $stack5;的语句，保持原汁原味
//            Options.v().setPhaseOption("jb.cp", "enabled:false");
//            Options.v().setPhaseOption("jb.ls","enabled:false");
//            Options.v().setPhaseOption("jb.dae","enabled:false");
//            Options.v().setPhaseOption("jb.ulp","unsplit-original-locals:false");
//            Options.v().setPhaseOption("jb.a","enabled:false");
//          Options.v().setPhaseOption("jb.cp","enabled:false");
            System.out.println("sootclass path :" + Scene.v().getSootClassPath());
            Scene.v().loadNecessaryClasses();
            

            // add to-exlude classes
            Options.v().set_exclude(addExcludeClasses());

            PackManager.v().runPacks();

            // Enable SPARK call-graph construction
            Options.v().setPhaseOption("cg.spark","on");
            Options.v().setPhaseOption("cg.spark","enabled:true");
            Options.v().setPhaseOption("cg.spark","verbose:true");
            Options.v().setPhaseOption("cg.spark","on-fly-cg:true");
          
            
//          PackManager.v().writeOutput();

        }

         public  void getBasicInfo(String className){
            
//          SootClass mainClass = Scene.v().getMainClass();

            //鑾峰彇main鏂规硶
//            SootMethod mainMethod = Scene.v().getMainMethod();
            SootClass sc = Scene.v().getSootClass(className);
            SootClass sc1 = Scene.v().getSootClass("java.lang.Object");
            
            System.out.println("sc is:" + sc1);
            //鑾峰彇杩愯鏃剁被 搴旂敤绫? 鍩虹绫? 鎵?鏈夌被
            Chain<SootClass> libraryClasses = Scene.v().getLibraryClasses();
            Chain<SootClass> applicationClasses = Scene.v().getApplicationClasses();
            Set<String> basicClasses = Scene.v().getBasicClasses();
            Chain<SootClass> classes = Scene.v().getClasses();

            //鑾峰彇褰撳墠soot鐨勫垎鏋愯矾寰勶細閫氬父涓篶lasspath+app-path
            String sootClassPath = Scene.v().getSootClassPath();
            System.out.println("classPath is " + sootClassPath);

            //鑾峰彇榛樿JVMclasspath鐨勮矾寰?
            String s = Scene.v().defaultClassPath();//rt.jar path
            System.out.println("defaultClassPath is " + s);
        }

        // 澧炲姞涓嶉渶瑕佺殑绫?
        public  List<String> addExcludeClasses(){
            if(excludeClassList == null){
                excludeClassList = new ArrayList<String>();
            }
            // 鍓嶇紑鍚嶅尯鍒?
            excludeClassList.add("java.");
            excludeClassList.add("javax.");
            excludeClassList.add("sun.");
            excludeClassList.add("sunw.");
            excludeClassList.add("com.sun.");
            excludeClassList.add("com.ibm.");

            return excludeClassList;

        }
}

