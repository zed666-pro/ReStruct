package addannotation.visitor.invokeAll;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import addannotation.store.InvokeAllStore;
import addannotation.utils.Tools;



public class InvokeAllVisitor_futureGet extends InvokeAllVisitor {
    
    /**
     * 
     * @param cuu：methodDeclaration
     * @param storeChanges：存储发生变化的信息
     * @description 查询future.get方法，需要获取到get方法外层的for语句 目前的查询思路是先查询for，在从for中找get；
     *              如果后续存在性能问题，该方法可以优化
     */
    public static void acquireFutureGet(ASTNode cuu, InvokeAllStore storeChanges) {
        if(inOneForStatement) {
            findInvokeAllAndGetInOneFor(storeChanges);
            
        }else {
            // 防止hasGetMethod 没有被重置为false
            hasGetMethod = false;
            //get方法可能会存在两种形式 
//                     1. 增强for循环，直接future.get 
//                     2.一般的for循环，Future future = futures.get(i) future.get()
            findGetMethodInNormalFor(cuu, storeChanges);
            
            if (!hasGetMethod) {
                findGetMethodInEnhancedFor(cuu, storeChanges);
            }
            
            
        }

        
      
    }
    
    
    public static void findGetMethodInNormalFor(ASTNode cuu, InvokeAllStore storeChanges) {
        cuu.accept(new ASTVisitor() {
            @Override
            public boolean visit(EnhancedForStatement node) {
                // TODO Auto-generated method stub
                String futuresName = node.getExpression().toString();
                SingleVariableDeclaration singleVarDec = node.getParameter();
                if (singleVarDec != null) {

                    String futureName = singleVarDec.getName().toString();
                   
                    if (futuresName.equals(InvokeAllVisitor.futuresName)) {

                        Statement statement = node.getBody();
                        // 进一步判断是否含有get方法
                        hasgetInEnhancedFor(node, storeChanges, futureName);
                    }
                }
                return true;
            }
        });

    }
    

    
    

    public static void findGetMethodInEnhancedFor(ASTNode cuu, InvokeAllStore storeChanges) {
        cuu.accept(new ASTVisitor() {
            @SuppressWarnings("unlikely-arg-type")
            @Override
            public boolean visit(ForStatement node) {
              
                InfixExpression expr = (InfixExpression) node.getExpression();
                // 也就是futures.size
                if (expr.getRightOperand() instanceof MethodInvocation) {
                    MethodInvocation methodInvoke = (MethodInvocation) expr.getRightOperand();
                    if (methodInvoke.getExpression() instanceof SimpleName) {

                        SimpleName simpleName = (SimpleName) methodInvoke.getExpression();
                        String methodInvoker = simpleName.getIdentifier().toString();
                        // 判断该futures.size()方法中的futures是否和invokeAll返回的一致
                        if (methodInvoker.equals(futuresName)) {
                        
                            hasGetInNormalFor(node, storeChanges, methodInvoker);

                        }
                    }
                }
                return true;
            }
        });

    }
    
    
    /**
     * @description: 在增强for循环中查找get方法
     * @return:
     */
    public static void hasgetInEnhancedFor(EnhancedForStatement forStmt, InvokeAllStore storeChanges,
            String futureName) {
        forStmt.accept(new ASTVisitor() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public boolean visit(MethodInvocation node) {
                // TODO Auto-generated method stub
                if(node.getExpression() != null) {
                    String methodInvoker = node.getExpression().toString();
                    String methodName = node.getName().toString();

                    if (methodInvoker.equals(futureName) && methodName.equals("get")) {
                        // 判断for循环是否和invokeAll方法处于同一级
                        List list = Tools.hasBlockParent(forStmt,(Block)storeChanges.getInvokeAllMap().get("parent"));
                       
                        if ((Block) storeChanges.getInvokeAllMap().get("parent") == list.get(2)) {

                            HashMap map = storeChanges.getFutureGetMap();
                          
                            map.put("node", node);
                            map.put("parent", forStmt);
                            map.put("index", list.get(0));
                          
                            // 将hasGetMethod字段改为true
                            hasGetMethod = true;
                            
                            return false;

                        }
                    }
                }
              
         
                return true;
            }
        });
    }
    
    
    
    /**
     * 
     * @param forStmt:一般的for语句
     * @param storeChanges:存储所有需要修改的信息
     * @param methodInvoke:
     * @decription 可能存在两种形式：
     *                 1.future.get() 
     *                 2.future.get(i).get()
     *             如果一种形式找不到，就找下一种形式
     *                 
     * 
     */
    public static void hasGetInNormalFor(ForStatement forStmt, InvokeAllStore storeChanges, String methodInvoke) {
        
        hasGetMethodwithNormalForm(forStmt, storeChanges, methodInvoke);
        
        if(!hasGetMethod) {
            hasGetMethodwithAnotherForm(forStmt, storeChanges, methodInvoke);
            
        }

    }
    
    /**
     * 
     * @param forStmt
     * @param storeChanges
     * @param methodInvoke
     * @decription future.get() 形式
     */
    public static void hasGetMethodwithNormalForm(ForStatement forStmt, InvokeAllStore storeChanges, String methodInvoke) {
        forStmt.accept(new ASTVisitor() {
            @Override
            public boolean visit(VariableDeclarationStatement node) {
               
                VariableDeclarationFragment decFrag = (VariableDeclarationFragment) node.fragments().get(0);
                if(decFrag.getInitializer() instanceof MethodInvocation) {
                    MethodInvocation invocation = (MethodInvocation) decFrag.getInitializer();
                    String invoker = invocation.getExpression().toString();
                  
                    String invokedMethod = invocation.getName().toString();
                    
                
                    if (invoker.equals(methodInvoke) && invokedMethod.equals("get")) {
                        // 获取futures.get(i)方法返回的future变量
                        String future = decFrag.getName().toString();
                        obtainGet(forStmt,storeChanges, future);
                        return false;

                    }
  
                }
                return true;
            }
        });
        
    }
    
    /**
     * 
     * @param forStmt
     * @param storeChanges
     * @param methodInvoke
     * @decription  查找形如futures.get(i).get()的结构
     */
    public static void hasGetMethodwithAnotherForm(ForStatement forStmt, InvokeAllStore storeChanges, String methodInvoke) {
        forStmt.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodInvocation node) {
                
                if(node.getExpression() instanceof MethodInvocation) {
                    MethodInvocation invocation = (MethodInvocation)node.getExpression();
                    if(invocation.getExpression() instanceof SimpleName) {
                        SimpleName simpleName = (SimpleName)invocation.getExpression();
                        //这里的invoker就是futures
                        
                        String invoker = simpleName.getIdentifier().toString();
                        //method为get(i)
                        String method = invocation.getName().getIdentifier().toString();
                      
                        if(invoker != null && method != null && invoker.equals(methodInvoke) && method.equals("get")) {
                            if(invocation.arguments().get(0) instanceof SimpleName) {
                                SimpleName simpleName1 = (SimpleName)invocation.arguments().get(0);
                                String param = simpleName1.getIdentifier().toString();
                                if(param != null) {
                                    List list = Tools.hasBlockParent(forStmt,(Block)storeChanges.getInvokeAllMap().get("parent"));

                                    // 判断for的parent和invokeAll的parent是否在同一级
                                    if (list.get(2) == storeChanges.getInvokeAllMap().get("parent")) {
                                        HashMap map = storeChanges.getFutureGetMap();
                                        map.put("node", node);
                                        map.put("parent", forStmt);
                                        map.put("index", list.get(0));
                                        // 将hasGetMethod改为true
                                        hasGetMethod = true;
                                        return false;
                                    }
                                    
                                }
                                    
                                
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
     * @param forstmt:      一般的for语句
     * @param block:        for中的body
     * @param storeChanges: 存储所有需要修改的信息
     * @param future:       futures.get(i)返回的
     * @description: 1.比对Future future = futures.get(i)中的future
     *               和future.get()中的future 2.判断是否是future.get方法
     */
    public static void obtainGet(ForStatement forstmt, InvokeAllStore storeChanges, String future) {
        forstmt.accept(new ASTVisitor() {

            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public boolean visit(VariableDeclarationStatement node) {

                VariableDeclarationFragment decFrag = (VariableDeclarationFragment) node.fragments().get(0);
                if(decFrag.getInitializer() instanceof MethodInvocation) {
                    MethodInvocation invocation = (MethodInvocation) decFrag.getInitializer();
                    String invoker = invocation.getExpression().toString();
                    String invokedMethod = invocation.getName().toString();
                    if (invoker.equals(future) && invokedMethod.equals("get")) {
                        // 找到了future.get()方法
                        // 后去for所在的外层结构，判断是否和invokeAll的外层处于同一层结构中
                        // 判断for循环是否和invokeAll方法处于同一级
                        List list = Tools.hasBlockParent(forstmt,(Block)storeChanges.getInvokeAllMap().get("parent"));

                        // 判断for的parent和invokeAll的parent是否在同一级
                        if (list.get(2) == storeChanges.getInvokeAllMap().get("parent")) {
                            HashMap map = storeChanges.getFutureGetMap();
                            map.put("node", invocation);
                            map.put("parent", forstmt);
                            map.put("index", list.get(0));

                            // 将hasGetMethod改为true
                            hasGetMethod = true;
                        }

                    }
   
                }
              
                return true;
            }
        });
    }
    
    
    public static void findInvokeAllAndGetInOneFor(InvokeAllStore storeChanges) {
        
        EnhancedForStatement forStatement  = (EnhancedForStatement) storeChanges.getEnhancedForStatementMap().get("node");
        SingleVariableDeclaration sDeclaration = forStatement.getParameter();
        if(sDeclaration.getType() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)sDeclaration.getType();
            hasTasks(parameterizedType);
        }
        
        //Future<Integer> future : pool.invokeAll(tasks)中的future
        String future = sDeclaration.getName().getIdentifier().toString();
        System.out.println("future :" + future);
        Statement bodyStatement = forStatement.getBody();
        System.out.println("bodyStatement :" + bodyStatement);
        bodyStatement.accept(new ASTVisitor() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public boolean visit(MethodInvocation method) {
                System.out.println("find future get1");
                if(method.getExpression() instanceof SimpleName && method.getName() instanceof SimpleName) {
                    System.out.println("find future get2");
                    SimpleName expressionName = (SimpleName)method.getExpression();
                    String getName = method.getName().getIdentifier().toString();
                    
                    String invokeGetName = expressionName.getIdentifier().toString();
                    System.out.println("invokeGetName " + invokeGetName);
                    System.out.println("getname " + getName);
                    if(future.equals(invokeGetName) && getName.equals("get")) {
                        storeChanges.setMethodInvocation(method);
                        System.out.println("find future get3");
                      
                      
                    }
                    
                    
                }
                

                return true;
            }
        });
        

  
    }
    
    
    public static void hasTasks(ParameterizedType paramType) {
        if (paramType.typeArguments().get(0) instanceof SimpleType) {
            SimpleType simpleType = (SimpleType) paramType.typeArguments().get(0);
//            callableType = simpleType.getName().toString();
            tasKind = simpleType.getName().toString();
            System.out.println("tasKind is:" + tasKind);
        }
        if (paramType.typeArguments().get(0) instanceof ParameterizedType) {
            ParameterizedType paramType1 = (ParameterizedType) paramType.typeArguments().get(0);
            if (paramType1.getType() instanceof SimpleType) {
                SimpleType paramType2 = (SimpleType) paramType1.getType();
//                callableType = paramType2.getName().toString();
                tasKind = paramType2.getName().toString();
              
            }

        }
        hasTasks = true;
          
    }   
}
