package addannotation.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import soot.SootField;
import soot.SootMethod;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;

public class StaticFieldAnalysis {
    public static Set<SootField> getStatics(SootMethod method) {
        
        // 存储静态字段的集合
        Set<SootField> staticFields = new HashSet<SootField>();

        // 获取方法体的所有语句
        for (Unit u : method.getActiveBody().getUnits()) {
            if (u instanceof Stmt) {
                Stmt stmt = (Stmt) u;

                // 获取语句中的所有值
                for (ValueBox valueBox : stmt.getUseAndDefBoxes()) {
                    if (valueBox.getValue() instanceof StaticFieldRef) {
                        StaticFieldRef fieldRef = (StaticFieldRef) valueBox.getValue();

                        // 添加到集合中
                        staticFields.add(fieldRef.getField());
                    }
                }
            }
        }

        // 打印静态字段
        for (SootField field : staticFields) {
            System.out.println("静态字段: " + field);
        }
        return staticFields;
    }
}
