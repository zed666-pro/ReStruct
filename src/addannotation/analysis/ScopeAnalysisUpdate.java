package addannotation.analysis;


import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.toolkits.annotation.logic.Loop;
import soot.jimple.toolkits.annotation.logic.LoopFinder;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.*;

import java.util.*;

public class ScopeAnalysisUpdate {
    private  String className;
    private  String methodName;

    public ScopeAnalysisUpdate() {
    }

    public ScopeAnalysisUpdate(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }
    

    public  boolean doAnalysis(){
      
        SootClass targetClass = Scene.v().getSootClass(className);
        SootMethod method = targetClass.getMethodByName(methodName);
        Body activeBody = method.retrieveActiveBody();
        ExceptionalUnitGraph graph = new ExceptionalUnitGraph(activeBody);
        //找到所有的定义为Local
        LocalDefs localDefs = new SimpleLocalDefs(graph);
        //找到所有Local使用的位置
        LocalUses localUses = new SimpleLocalUses(activeBody, localDefs);


        Stmt stmt1 = findInvokeAllStmt(activeBody);
        List<Stmt> loopStmts = findStmtsInLoop(graph, activeBody);
        Stmt stmt2 = loopStmts.get(0);
        List<Stmt> stmts = findStmtBetween(stmt1, stmt2, activeBody);
        if (stmts == null) {
            return false;
        }
        //将所有的local和local使用位置信息加入到map中
        HashMap<Local, List<UnitValueBoxPair>> map = findLocalUsesLocation(stmt1, stmt2,stmts, activeBody, localUses);
        //获取到forStmt之后使用的stmt
        List<Unit> units  = getStmtAfterFor(loopStmts.get(1),activeBody);

        return hasSameStmt(map, units);

    }


    public  boolean hasSameStmt(HashMap<Local, List<UnitValueBoxPair>> map, List<Unit> units){
        if (!map.isEmpty()) {
            for(Map.Entry<Local, List<UnitValueBoxPair>> entrySet : map.entrySet()){
                for (UnitValueBoxPair use : entrySet.getValue()) {
                    if (units.contains(use.getUnit())) {
                        System.out.println("找到了是位置：" + use.getUnit().toString());
                        return true;
                    }

                }

            }
        }
        return false;
    }

    private  HashMap<Local, List<UnitValueBoxPair>> findLocalUsesLocation(Stmt stmt1, Stmt stmt2, List<Stmt> stmts,Body activeBody, LocalUses localUses){
        HashMap<Local, List<UnitValueBoxPair>> map = new HashMap<>();
        if (stmt1 != null && stmt2 != null) {
 
          
                for (Stmt stmt : stmts) {
                    if (stmt instanceof AssignStmt) {
                        Value leftOp = ((AssignStmt) stmt).getLeftOp();
                        if (leftOp instanceof Local) { // 检查这个值是否是局部变量
                            Local local = (Local) leftOp;
                            System.out.println("定义了一个变量: " + local.getName().toString());
                            List<UnitValueBoxPair> uses = localUses.getUsesOf(stmt);
                            map.put(local, uses);
                        }
                    }
                }
            }
        

        return map;
    }

    /**
     * 获取for循环之后的stmt
     * @param stmt
     * @param body
     * @return
     */
    private  List<Unit> getStmtAfterFor(Stmt stmt,Body body) {
        List<Unit> res = new ArrayList<>();
        for (Unit unit : body.getUnits()) {
            if (stmt.getJavaSourceStartLineNumber() < unit.getJavaSourceStartLineNumber() && unit instanceof Stmt) {
                res.add(unit);
            }
        }
        return res;
    }


    /**
     * 找到for循环中的第一条stmt和最后一条语句
     * @param body
     * @return
     */
    public List<Stmt> findStmtsInLoop(ExceptionalUnitGraph graph, Body body){
        ArrayList<Stmt> res = new ArrayList<>();
        // 创建 DirectedGraph 对象
        DirectedGraph<Unit> dGraph = graph;

        //将DirectedGraph对象传递给 LoopFinder 对象，然后获取所有的循环
        LoopFinder loopFinder = new LoopFinder();
        loopFinder.transform(body);

        // retrieve all loops
        Set<soot.jimple.toolkits.annotation.logic.Loop> loopList = loopFinder.getLoops(body);
        for (Loop loop : loopList) {
            //System.out.println("Found a loop in the method: " + loop.toString());
            for (Unit unit : loop.getLoopStatements()) {
                if (unit instanceof JAssignStmt) {
                    JAssignStmt assignStmt = (JAssignStmt) unit;
                    // 如果右值是一个方法调用
                    if (assignStmt.getRightOp() instanceof InvokeExpr) {
                        InvokeExpr invokeExpr = (InvokeExpr) assignStmt.getRightOp();
                        // 如果方法名是 "get"
                        if (invokeExpr.getMethod().getName().equals("get") &&
                                invokeExpr.getMethod().getDeclaringClass().getName().equals("java.util.concurrent.Future")) {
//                            System.out.println("Found a call to Future.get() at line: " + unit.getJavaSourceStartLineNumber());
//                            System.out.println("Found for contains Future.get() information: " );
//                            System.out.println("loop.getHead(): " + loop.getHead().getJavaSourceStartLineNumber());
//                            System.out.println("last stmt in for:" + loop.getLoopStatements().get(loop.getLoopStatements().size() - 1).getJavaSourceStartLineNumber());
//                            System.out.println("last stmt in for:" + loop.getLoopStatements().get(loop.getLoopStatements().size() - 1).getJavaSourceStartColumnNumber());
                        

                            res.add(loop.getHead());
                            res.add(loop.getLoopStatements().get(loop.getLoopStatements().size() - 1));

                            return res;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 找到invokeAll stmt所在的位置
     * @param body
     * @return
     */
    public Stmt findInvokeAllStmt(Body body){
        for (Unit unit : body.getUnits()) {
            if (unit instanceof Stmt) {
                Stmt stmt = (Stmt) unit;
                if (stmt.containsInvokeExpr()) {
                    InvokeExpr expr = stmt.getInvokeExpr();
                    if (expr.getMethod().getDeclaringClass().getName().equals("java.util.concurrent.ExecutorService") &&
                            expr.getMethod().getName().equals("invokeAll")) {
                        System.out.println("findInvokeAll method");
                        System.out.println("stmt information: ");
                        System.out.println("stmt.getJavaSourceStartLineNumber(): " + stmt.getJavaSourceStartLineNumber());
                        System.out.println("stmt.getJavaSourceStartColumnNumber(): " + stmt.getJavaSourceStartColumnNumber());
                        // 这个stmt调用了ExecutorService.invokeAll方法
                        return stmt;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 找到invokeAll 和 for循环之间定义的stmt
     * @param start： 表示invokeAll所在的stmt
     * @param end： 表示for循环中的第一个stmt
     * @param body
     * @return
     */
    public  List<Stmt> findStmtBetween(Stmt start, Stmt end,Body body){
        Iterator<Unit> iterator = body.getUnits().iterator();
        ArrayList<Stmt> res = new ArrayList<>();
        while (iterator.hasNext()){
            Unit unit = iterator.next();
            if (unit instanceof Stmt) {
                Stmt stmt = (Stmt) unit;
                if (stmt.getJavaSourceStartLineNumber() > start.getJavaSourceStartLineNumber()
                    && stmt.getJavaSourceStartLineNumber() < end.getJavaSourceStartLineNumber()) {
                    res.add(stmt);
                    System.out.println("target stmt: " + stmt.toString());
                }
            }
        }
        return res;
    }
}
