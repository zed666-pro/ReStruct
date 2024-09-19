package addannotation.analysis;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.MonitorStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.util.Chain;

public class ThreadDependenceAnalysis {
    
    public static HashSet<SootMethod> sootMethods = new HashSet<SootMethod>();
    
    public static boolean doAnalysis(String className){
        CallGraph cg = Scene.v().getCallGraph();
        SootClass sootClass = Scene.v().getSootClass(className);
        List<SootMethod> methods = sootClass.getMethods();
        for (SootMethod method : methods) {
            if(hasObjectReference(methods,method) || hasCallingRelationship(methods,method,cg) || hasLockOperation(method)){
                return true;
            }

        }
        return false;
    };

    
    /**
     * @description: 判断sootMethods之间是否存在调用关系
     * @param methods
     * @param method
     * @param cg
     * @return
     */
    public static boolean hasCallingRelationship(List<SootMethod> methods,SootMethod method,CallGraph cg){

            if (method != null) {
                Iterator<Edge> edges = cg.edgesOutOf(method);
                while (edges.hasNext()) {
                    Edge edge = edges.next();
                    SootMethod calleeMethod = edge.tgt();
                    if(methods.contains(calleeMethod)){
                        System.out.println("方法之间存在调用关系");
                        return true;
                    }
                    System.out.println(calleeMethod.getName() + " 调用了 " + calleeMethod.getName());
                }
            }
            return false;
    }

    /**
     * @description: 判断是否使用了相同的局部变量
     * @param methods
     * @param method
     * @return
     */
    private static boolean hasObjectReference(List<SootMethod> methods, SootMethod method) {
        // 获取方法的活动体
        Body srcBody = method.getActiveBody();
        for (SootMethod sootMethod : methods) {
            if(method != sootMethod){
                Body targetBody = sootMethod.getActiveBody();
                // 获取方法中的局部变量和参数
                Chain<Local> locals1 = srcBody.getLocals();
                Chain<Local> locals2 = targetBody.getLocals();
                System.out.println("srcMethod :" + method.getName());
                System.out.println("targetMethod : " + sootMethod.getName());

                if(hasCommonObjectReference(locals1, locals2)){
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * @description：比较两个Chain<Local>中是否有相同的local
     * @param chain1
     * @param chain2
     * @return
     */
    private static boolean hasCommonObjectReference(Chain<Local> chain1, Chain<Local> chain2) {
        // 检查两个列表中是否存在对同一个对象的引用
        for (Local srcLocal : chain1) {
            for (Local targetLocal : chain2) {
                if (isSameObjectReference(srcLocal, targetLocal)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @description: 判断两个local是否是相同的local
     * @param srcLocal
     * @param targetLocal
     * @return true：表示是相同的
     */
    private static boolean isSameObjectReference(Local srcLocal, Local targetLocal) {
        // 检查两个值是否引用了同一个对象
        return srcLocal.equivTo(targetLocal) && srcLocal.getName() != "this";
    }


    /**
     * @param classNames:所有含有call方法类的名称
     * @description: 收集所有实现Callable接口实现类中的call方法
     */
    public static HashSet<SootMethod> collectCallMethods(List<String> classNames){
        HashSet<SootMethod> set = new HashSet<>();

        for (String name : classNames) {
            SootClass sootClass = Scene.v().getSootClass(name);
            List<SootMethod> methods = sootClass.getMethods();
            //其中的一个call方法使用Volatile修饰来包装原本的call方法的
            for (SootMethod method : methods){
                if (method.getName().equals("call") && !hasVolatileModifier(method)) {
                    set.add(method);
                }
            }

        }
        return set;
    }

    /**
     * @description: 判断一个方法是否是被volatile关键字修饰的
     * @param method
     * @return
     */
    private static boolean hasVolatileModifier(SootMethod method) {
        // 检查方法是否带有 volatile 修饰符
        return Modifier.isVolatile(method.getModifiers());
    }

    /**
     *
     * @param method
     * @description: 判断方法中是否使用synchronized关键字
     * @return true：表示使用了
     */
    private static boolean hasLockOperation(SootMethod method) {
        Body body = method.getActiveBody();
        for (Unit unit : body.getUnits()) {

            // 检查方法中是否包含同步相关的指令，如monitor-enter、monitor-exit
            if (unit instanceof MonitorStmt) {
                System.out.println("使用了同步锁");
                System.out.println("当前类是：" + method.getDeclaringClass().getName().toString());
                System.out.println("当前方法是：" + method.getName().toString());
                return true;
            }
        }


        return false;
    }
}
