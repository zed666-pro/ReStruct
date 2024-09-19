package addannotation.analysis;

import java.util.Iterator;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;

public class FieldAccessAnalysis extends BodyTransformer {

//    public static void main(String[] args) {
//        PackManager.v().getPack("jtp").add(new Transform("jtp.myTransform", new FieldAccessAnalysis()));
//        soot.Main.main(args);
//    }

    protected void internalTransform(Body body, String phaseName, Map options) {
        for (Iterator<Unit> i = body.getUnits().snapshotIterator(); i.hasNext();) {
            Stmt stmt = (Stmt)i.next();
          
            // 如果这是一个赋值语句
            if (stmt instanceof AssignStmt) {
                AssignStmt assignStmt = (AssignStmt) stmt;
                Value leftOp = assignStmt.getLeftOp();
                Value rightOp = assignStmt.getRightOp();

                // 检查左侧操作数
                if (leftOp instanceof StaticFieldRef) {
                    System.out.println("写入静态字段: " + leftOp);
                }

                // 检查右侧操作数
                if (rightOp instanceof StaticFieldRef) {
                    System.out.println("读取静态字段: " + rightOp);
                }
            }
        }
  }
}