package addannotation.visitor.invokeAll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.ui.JavaTaskListAdapter;
import org.w3c.dom.ls.LSOutput;

import addannotation.analysis.ThreadDependenceAnalysis;
import addannotation.store.InvokeAllStore;
import addannotation.utils.RefactorType;
import addannotation.utils.Tools;
import addannotation.visitor.GeneralVisitor;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.coffi.constant_element_value;

/*
 * @description:
 *    该类的方法主要签名前缀含有三种
 *        1.acquire：获取invokeAll，future.get方法，供外部类调用的
 *        2.find：   acquire中进一步查询
 *        3.has：    是否含有某种资源 
 *        4.obtain     
 * 
 */
@SuppressWarnings({ "unused" })
public class InvokeAllVisitor {

    public static String tasksName; // invokeAll中的任务名称
    public static String futureType; // future中的泛型类型
    public static String futuresName; // List<Future<Object>> futures= invokeAll.submit(tasks),中futures的名称futureGetMap
    public static String threadPoolName = ""; // ExecutorService pool = Executors.newXXX()中的pool的名字
    public static boolean hasGetMethod = false; // 判断是否存在future.get方法
    public static boolean hasInvokeAllMethod = false;
    public static boolean hasExecutorsInBody = true;
    public static boolean hasFutureList = false; // 是否接收invokeAll.submit的值
    public static boolean inOneTry = false; // 判断invokeAll 和 future.get方法是否在同一个try中
    public static boolean hasGetInTry = false;
    public static boolean hasInvokeAllInTry = false;
    public static boolean hasTasks = false;
    public static boolean inOneForStatement = false;
    public static String taskType; // 任务的类型，可能是callable或者是实现callable的类
    public static String tasKind;  //Callable<String> 中的String的类型

    /**
     * @description：根据invokeAll中的tasks名称，找到定义该任务的位置，获取任务的泛型
     * @param cuu
     * @param storeChanges
     */
    public static void acquireTasks(ASTNode cuu, InvokeAllStore storeChanges) {
        //先从参数中找，找不到从body中找
        hasTasks = false;
        findTasksInParameters(cuu,storeChanges);
        if(!hasTasks) {
            findTasksInBody(cuu,storeChanges);
            
        }

    }
    
    public static void findTasksInBody(ASTNode cuu, InvokeAllStore storeChanges) {
        cuu.accept(new ASTVisitor() {
            @Override
            public boolean visit(VariableDeclarationStatement node) {
                
                if (node.fragments().get(0) instanceof VariableDeclarationFragment) {
                    VariableDeclarationFragment varDecFrag = (VariableDeclarationFragment) node.fragments().get(0);
                    String tasksList = varDecFrag.getName().getIdentifier().toString();
                    obtainTasks(tasksList,node);
          
                }

                return true;
            }
        });
  
    }
    
    public static void findTasksInParameters(ASTNode cuu, InvokeAllStore storeChanges) {
       
        if(cuu instanceof MethodDeclaration) {
            MethodDeclaration  method = (MethodDeclaration)cuu;
            List params = method.parameters();
            if(!params.isEmpty()) {
                for(int i = 0; i < params.size(); i++) {
                    if(params.get(i) instanceof SingleVariableDeclaration) {
                        //execute(ArrayList<Callable<Long>> tasks, ConvolutionParameters params) 
                        //    1.tasksList是tasks的名称
                        SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration)params.get(i);
                        String tasksList = singleVariableDeclaration.getName().getIdentifier().toString();
                        obtainTasks(tasksList,singleVariableDeclaration);
                        System.out.println("在参数上找到任务");
                    }
                    
                    
                }
                
                
            }
            
        }

    }
    
    
    public static void obtainTasks(String tasksList,ASTNode cuu) {
        
        if (tasksList.equals(tasksName)) {
            String callableType = null;
            if(cuu instanceof SingleVariableDeclaration) {
                SingleVariableDeclaration node = (SingleVariableDeclaration)cuu; 
                // 一种是List<Callable>
                // 一种是List<Callable<String>>
                if (node.getType() instanceof ParameterizedType && callableType == null) {
                    ParameterizedType paramType = (ParameterizedType) node.getType();
                    hasTasks(paramType);
//                    SootClass taskClass = Scene.v().getSootClass(callableType);
//                    System.out.println("taskClass :" + taskClass.getName().toString());
//                    SootMethod taskMethod = taskClass.getMethodByName("call");
//                    ThreadDependenceAnalysis.sootMethods.add(taskMethod);

                }
            }
           
            if(cuu instanceof VariableDeclarationStatement) {
                
                VariableDeclarationStatement node = (VariableDeclarationStatement)cuu; 
                // 一种是List<Callable>
                // 一种是List<Callable<String>>
                if (node.getType() instanceof ParameterizedType && callableType == null) {
                    ParameterizedType paramType = (ParameterizedType) node.getType();
                    hasTasks(paramType);
//                    int index = callableType.indexOf('<');
//                    String classname = (String) callableType.subSequence(0, index);
//                    System.out.println("classname is :" + classname);
//                    SootClass taskClass = Scene.v().getSootClass(classname);
//                    System.out.println("taskClass :" + taskClass.getName().toString());
//                    SootMethod taskMethod = taskClass.getMethodByName("call");
//                    ThreadDependenceAnalysis.sootMethods.add(taskMethod);
 
                }
            }

        }

    }
    
    public static void hasTasks(ParameterizedType paramType) {
        if (paramType.typeArguments().get(0) instanceof SimpleType) {
            SimpleType simpleType = (SimpleType) paramType.typeArguments().get(0);
//            callableType = simpleType.getName().toString();
            taskType = simpleType.getName().toString();
            System.out.println("taskType is:" + taskType);
        }
        if (paramType.typeArguments().get(0) instanceof ParameterizedType) {
            ParameterizedType paramType1 = (ParameterizedType) paramType.typeArguments().get(0);
            if (paramType1.getType() instanceof SimpleType) {
                SimpleType paramType2 = (SimpleType) paramType1.getType();
//                callableType = paramType2.getName().toString();
                taskType = paramType2.getName().toString();
              
            }

        }
        hasTasks = true;
          
    }

    /**
     * @description: 只有invokeAll和future.get方法不在同一个try中的场景才能调用
     * @param storeChanges
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void acquireStmtsInInvokeAllAndFor(InvokeAllStore storeChanges) {

        Integer index1 = (Integer) storeChanges.getInvokeAllMap().get("index");
        Integer index2 = (Integer) storeChanges.getFutureGetMap().get("index");

        Block block = (Block) storeChanges.getInvokeAllMap().get("parent");
        List list = new ArrayList<Statement>();
        List statements = block.statements();
        for (int i = index1; i <= index2; i++) {
            // 判断该节点是否是pool.shutdown
            // 判断该节点是否是pool.awaitTermination
            if (statements.get(i) instanceof ExpressionStatement) {
                ExpressionStatement expression = (ExpressionStatement) statements.get(i);
                if (expression.getExpression() instanceof MethodInvocation) {
                    MethodInvocation invocation = (MethodInvocation) expression.getExpression();
                    if (invocation.getExpression() instanceof SimpleName) {
                        SimpleName simpleName = (SimpleName) invocation.getExpression();
                        String executorName = simpleName.getIdentifier().toString();
                        // 查找的是pool.shutdown这样的语句
                        if (executorName.equals(threadPoolName)) {
                            String methodName = invocation.getName().getIdentifier().toString();
                            if (methodName.equals("shutdown")) {
                                // 如果找到对应的方法，直接调用这次循环，不添加该语句
//                                System.out.println("找到了shutdown方法");
                                continue;

                            }

                            if (methodName.equals("awaitTermination")) {
                                // 如果找到对应的方法，直接调用这次循环，不添加该语句
                                continue;

                            }

                        }

                    }

                }

            }
            // 如果不是上述两种情况，才加入到list中去
            list.add(statements.get(i));

        }
        storeChanges.setStatements(list);

    }

    public static void acquireExecutorRelated(ASTNode cuu, InvokeAllStore storeChanges) {
         findExecutorShutdown(cuu, storeChanges);
         findExecutorAwaitTermination(cuu, storeChanges);
        
    }
    
    
    public static void findExecutorShutdown(ASTNode cuu, InvokeAllStore storeChanges) {
        
        cuu.accept(new ASTVisitor() {
            @Override
            public boolean visit(ExpressionStatement node) {
                if (node.getExpression() instanceof MethodInvocation) {
                    MethodInvocation invocation = (MethodInvocation) node.getExpression();
                    if (invocation.getExpression() instanceof SimpleName) {
                        SimpleName simpleName = (SimpleName) invocation.getExpression();
                        String executorName = simpleName.getIdentifier().toString();
                        // 查找的是pool.shutdown这样的语句
                        if (executorName.equals(threadPoolName)) {
                            String methodName = invocation.getName().getIdentifier().toString();
                            if (methodName.equals("shutdown")) {

                                HashMap map = storeChanges.getExecutorMap();
                                map.put("shutdown", node);
                                return false;

                            }

                        }

                    }

                }
                return true;
            }
        });

    }
    
    
    public static void findExecutorAwaitTermination(ASTNode cuu, InvokeAllStore storeChanges) {
        cuu.accept(new ASTVisitor() {
            @Override
            public boolean visit(ExpressionStatement node) {
                if (node.getExpression() instanceof MethodInvocation) {
                    MethodInvocation invocation = (MethodInvocation) node.getExpression();
                    if (invocation.getExpression() instanceof SimpleName) {
                        SimpleName simpleName = (SimpleName) invocation.getExpression();
                        String executorName = simpleName.getIdentifier().toString();
                        // 查找的是pool.shutdown这样的语句
                        if (executorName.equals(threadPoolName)) {
                            String methodName = invocation.getName().getIdentifier().toString();

                            if (methodName.equals("awaitTermination")) {
//                                System.out.println("找到了awaitTermination方法1");
                                HashMap map = storeChanges.getExecutorMap();
                                map.put("awaitTermination", node);
                                return false;

                            }

                        }

                    }

                }
                return true;
            }
        });
        
        
        
        
    }

    /**
     * 
     * @param tryStatement
     * @return 表示一个try结构是否同时存在invokeAll和get方法 确保一个方法中最多只有一对invokeAll和get方法
     */
    public static boolean checkInvokeAllAndGetInOneTry(ASTNode method, InvokeAllStore storeChanges) {
        List<TryStatement> tryStatements = GeneralVisitor.getTryStatement(method);
        for (TryStatement tryStatement : tryStatements) {
            System.out.println("checkSubmitAndgetRelation enter.....");
            boolean flag1 = InvokeAllVisitor.hasFutureGetInTry(tryStatement);
            boolean flag2 = InvokeAllVisitor.hasInvokeAllInTry(tryStatement);
            if (flag1 == true && flag2 == true) {
                System.out.println("在同一个try中");
                storeChanges.setTryStatement(tryStatement);
                return true;

            }

        }

        return false;

    }

    /**
     * 
     * @param cuu
     * @description 判断InvokeAll方法是否在一个try结构中
     */
    public static boolean hasInvokeAllInTry(ASTNode cuu) {
        hasInvokeAllInTry = false;
        cuu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodInvocation node) {
                if (node.getExpression() instanceof SimpleName) {

                    SimpleName SimpleName1 = (SimpleName) node.getExpression();
                    // 可能是invokecation.getExpression.getIdentifier()
                    String invoker = SimpleName1.getIdentifier().toString();
                    if (node.getName() instanceof SimpleName) {
                        // 可能是invokecation.getName.getIdentifier()
                        SimpleName SimpleName2 = (SimpleName) node.getName();
                        String method = SimpleName2.getIdentifier().toString();
                        if (invoker.equals(threadPoolName) && method.equals("invokeAll")) {

                            hasInvokeAllInTry = true;

                        }
                    }
                }
                return true;
            }
        });
        return hasInvokeAllInTry;
    }

    /**
     * 
     * @param cuu
     * @description 判断futureGet方法是否在一个try结构中
     */
    public static boolean hasFutureGetInTry(ASTNode cuu) {
        hasGetInTry = false;
        cuu.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodInvocation node) {
                if (node.getExpression() instanceof SimpleName) {

                    SimpleName SimpleName1 = (SimpleName) node.getName();
                    String method = SimpleName1.getIdentifier().toString();
                    if (method.equals("get")) {

                        hasGetInTry = true;

                    }
                }
                return true;
            }
        });

        return hasGetInTry;
    }
}
