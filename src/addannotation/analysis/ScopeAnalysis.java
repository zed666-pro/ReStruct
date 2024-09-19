package addannotation.analysis;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;
import soot.toolkits.graph.ExceptionalUnitGraph;
import addannotation.store.InvokeAllStore;
import soot.Body;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.toolkits.scalar.*;
import soot.*;
import soot.jimple.*;
import soot.tagkit.LineNumberTag;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScopeAnalysis {

    private InvokeAllStore invokeAllStore;
    private CompilationUnit astRoot;
    private Statement invokeAllStmt;
    private Statement futureGetStmt;

    public ScopeAnalysis(CompilationUnit astRoot, InvokeAllStore invokeAllStore) {

        super();
        System.out.println("invokeAllStore is:" + invokeAllStore);
        this.invokeAllStore = invokeAllStore;
        this.astRoot = astRoot;
        invokeAllStmt = (Statement) invokeAllStore.getInvokeAllMap().get("node");
        futureGetStmt = (Statement) invokeAllStore.getFutureGetMap().get("parent");

    }

    public Integer getInvokeAllLineNumber(CompilationUnit astRoot) {
        return astRoot.getLineNumber(invokeAllStmt.getStartPosition());
    }

    public Integer getFutureGetLineNumber(CompilationUnit astRoot) {
        int startLine = futureGetStmt.getStartPosition();
        int endLine = startLine + futureGetStmt.getLength();

        return astRoot.getLineNumber(endLine);
    }

    public boolean doAnalysis(String className, String methodName) {
        System.out.println("变量范围分析：" + className);
        int startLine = getInvokeAllLineNumber(astRoot);
        int endLine = getFutureGetLineNumber(astRoot);

      
        ArrayList<HashMap<Unit, List<Integer>>> unitLines = new ArrayList<>();
        SootClass targetClass = Scene.v().getSootClass(className);

        System.out.println("targetClass：" + targetClass.getName().toString());
        try {
            // 可能会存在静态内部类的情况
            SootMethod method = targetClass.getMethodByName(methodName);

            Body jimpleBody = method.retrieveActiveBody();
            ExceptionalUnitGraph graph = new ExceptionalUnitGraph(jimpleBody);

            LocalDefs localDefs = new SimpleLocalDefs(graph);
            LocalUses localUses = new SimpleLocalUses(jimpleBody, localDefs);

            for (Unit unit : jimpleBody.getUnits()) {
                int line = getLineNumber(unit);
               
                if (unit instanceof DefinitionStmt && line >= startLine && line <= endLine) {

                    DefinitionStmt defStmt = (DefinitionStmt) unit;
                    Value leftValue = defStmt.getLeftOp();
                    Value rightValue = defStmt.getRightOp();
                    if (leftValue instanceof Local) {
                       
                        Local leftLocal = (Local) leftValue;
                        List<UnitValueBoxPair> uses = localUses.getUsesOf(unit);
                        ArrayList<Integer> lines = new ArrayList<>();
                        HashMap map = new HashMap<Unit, List<Integer>>();
                        for (UnitValueBoxPair use : uses) {

                            Unit useUnit = use.getUnit();
                            line = getLineNumber(useUnit);
                            lines.add(line);

                        }
                        map.put(unit, lines);
                        unitLines.add(map);
                        return isOutOfRange(unitLines, endLine);

                    }
                }
            }
        } catch (Exception e) {

        }

        return false;
    }

    public static int getLineNumber(Unit unit) {
        int lineNumber = -1;
        if (unit.hasTag(LineNumberTag.NAME)) {
            LineNumberTag tag = (LineNumberTag) unit.getTag("LineNumberTag");
            lineNumber = tag.getLineNumber();
//            System.out.println("Statement: " + unit.toString() + " Line Number: " + lineNumber);
        }
        return lineNumber;
    }
    
    public static boolean isOutOfRange(ArrayList<HashMap<Unit, List<Integer>>> unitLines,int endLine) {
        for (HashMap<Unit, List<Integer>> unitLine : unitLines) {
            for (Map.Entry<Unit, List<Integer>> entry : unitLine.entrySet()) {
                List<Integer> lines = entry.getValue();
                for (Integer line : lines) {
                    if (line >= endLine) {
                        System.out.println("发生越界");
                        return true;
                    }
                }
            }
        }
        return false;
         
    }

}
