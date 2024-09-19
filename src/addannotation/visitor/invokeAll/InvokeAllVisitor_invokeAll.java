package addannotation.visitor.invokeAll;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
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
import addannotation.visitor.GeneralVisitor;

public class InvokeAllVisitor_invokeAll extends InvokeAllVisitor {

    public static void acquireInvokeAll(ASTNode cuu, InvokeAllStore storeChanges) {

        // 防止hasFutureList 没有被重置为false
        hasFutureList = false;
        inOneForStatement = false;
        
        // 可能有List<Future<String>> futures 有的话语句类型是VariableDeclarationStatement
        findInvokeAllInVariableDeclaration(cuu, storeChanges);

        // 没有的话，在ExpressionStatement中找
        if (!hasFutureList) {
           
            findInvokeAllInExpresstion(cuu, storeChanges);
        }
        
        //可能invokeAll和future.get在同一个for循环中
        if(!hasFutureList) {
          
            findInvokeAllAndGetTogether(cuu, storeChanges);
            
            
        }
    }
          


    public static void findInvokeAllInVariableDeclaration(ASTNode cuu, InvokeAllStore storeChanges) {

        cuu.accept(new ASTVisitor() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public boolean visit(VariableDeclarationStatement node) {

                VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.fragments().get(0); // 存储所有的定义future的信息
                if (fragment.getInitializer() instanceof MethodInvocation) {
                    MethodInvocation invocation = (MethodInvocation) fragment.getInitializer();
                    // 被调用的方法名称
                    String method = invocation.getName().getIdentifier();
                    // 获取方法调用者的名称
                    if (invocation.getExpression() instanceof SimpleName) {
                        SimpleName simpleName = (SimpleName) invocation.getExpression();
                        String invoker = simpleName.getIdentifier().toString();

                        if (invoker.equals(threadPoolName) && method.equals("invokeAll")) {
                            // 需要将语句的左边保留下来，先将node存下来
                            List list = Tools.hasBlockParent(node);
                            System.out.println("list is:" + list);
                            HashMap map = storeChanges.getInvokeAllMap();
                            map.put("index", list.get(0));
                            map.put("node", node);
                            map.put("parent", list.get(2));
                            // 将List的类型也加入到其中
                            map.put("collectionType", node.getType().toString());
                           
                            // 获取相关变量的名称List<Future<Stirng>> futures = executor.invokeAll(tasks);
                            // tasks 是tasks的名称
                            // futureTasksName 是futures的名称
                            // futureType 是future中的泛型信息
                            tasksName = invocation.arguments().get(0).toString();
                            futuresName = fragment.getName().toString();
                            hasFutureList = true;
                            hasInvokeAllMethod = true;
                            // 统计整个项目中的invokeAll的数量和一个类中的invokeAll的数量
                            Tools.CountNumberOfInvokeAll();
                            return false;

                        }

                    }

                }
                return true;
            }
        });

    }

    public static void findInvokeAllInExpresstion(ASTNode cuu, InvokeAllStore storeChanges) {
        
        if(!hasFutureList) {
            cuu.accept(new ASTVisitor() {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                @Override
                public boolean visit(ExpressionStatement node) {
                 
                   if(node.getExpression() instanceof Assignment) {
                     
                       Assignment assignment = (Assignment)node.getExpression();
                       if(assignment.getRightHandSide() instanceof MethodInvocation) {
                           
                           MethodInvocation invocation = (MethodInvocation)assignment.getRightHandSide();
                           if(invocation.getExpression() instanceof SimpleName &&
                                               invocation.getName() instanceof SimpleName) {
                            
                               //futures = pool.invokeAll(tasks)
                               //poolName ---> pool
                               //method ---> invokeAll
                               //futuresName ---> futures
                               //tasks ---> tasks
                               SimpleName simpleName = (SimpleName)invocation.getExpression();
                               String invoker = simpleName.getIdentifier().toString();

                               String method = invocation.getName().getIdentifier();
                               if (invoker.equals(threadPoolName) && method.equals("invokeAll")) {
                                  
                                   if( assignment.getLeftHandSide() instanceof SimpleName) {
                                       SimpleName simpleName1 =  (SimpleName)assignment.getLeftHandSide();
                                       futuresName = simpleName1.getIdentifier().toString();
                                       tasksName = invocation.arguments().get(0).toString();
                                       
                                       // 需要将语句的左边保留下来，先将node存下来
                                       List list = Tools.hasBlockParent(node);
                                       HashMap map = storeChanges.getInvokeAllMap();
                                       //这种情况下还需要将另一条语句 List<future<xxxx>> futures加入到map中去
                                       map.put("index", list.get(0));
                                       map.put("node", node);
                                       map.put("parent", list.get(2));
                                       Tools.CountNumberOfInvokeAll();
                                       //该种情况下，还要查找List<Future<xxx>> futures 语句
                                       findFutureList(cuu,storeChanges);
                                    
                                       return false;
                                      
                                   }  
       
                               }
      
                           }    
                       }
       
                   }
                   return true;
                }
                     
       });         
    }
  }

   

    /**
     * 如果List<future<xxx>> futures 和 futures = pool.invokeAll(tasks)
     * 是分开定义的，则需要通过该方法获取到相关的信息 
     * 1.获取到List信息 
     * 2.获取到该条语句，需要将该条语句删除
     */
    public static void findFutureList(ASTNode cuu, InvokeAllStore storeChanges) {
        cuu.accept(new ASTVisitor() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public boolean visit(VariableDeclarationStatement node) {

                VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.fragments().get(0); // 存储所有的定义future的信息
                String listName = fragment.getName().getIdentifier().toString();
                if (listName.equals(futuresName)) {
                    HashMap map = storeChanges.getInvokeAllMap();

                    map.put("anotherNode", node);
                    // 将List的类型也加入进去

                    map.put("collectionType", node.getType().toString());
                    hasFutureList = true;
                    hasInvokeAllMethod = true;
                  
                    return false;

                }

                return true;
            }
        });
    }
    
    
    public static boolean findInvokeAllAndGetTogether(ASTNode cuu, InvokeAllStore storeChanges){
       
        List<EnhancedForStatement> forStatements = GeneralVisitor.getEnhancedForStatements(cuu);
        for(int i = 0; i < forStatements.size(); i++) {
            EnhancedForStatement enhancedForStatement  = forStatements.get(i);
            Expression expression = enhancedForStatement.getExpression();
            if(expression != null) {
                if(expression instanceof MethodInvocation) {
                    MethodInvocation methodInvocation = (MethodInvocation)expression;
                    Expression methodExpression = methodInvocation.getExpression();
                    if(methodExpression instanceof SimpleName) {
                        SimpleName simpleName = (SimpleName)methodExpression;
                        String executorName = simpleName.getIdentifier().toString();
                        //先要看是否是threadPoolName 调用invokeAll方法
                        if(executorName.equals(threadPoolName)) {
                           SimpleName methodSimpleName = (SimpleName)methodInvocation.getName();
                           String methodName = methodSimpleName.getIdentifier().toString();
                           //先要看是否是方法的名称是否为invokeAll
                           if(methodName.equals("invokeAll")) {
                               if( methodInvocation.arguments().get(0) instanceof SimpleName) {
                                   SimpleName paramSimpleName = (SimpleName)methodInvocation.arguments().get(0);
                                   tasksName  = paramSimpleName.getIdentifier().toString();
                                   System.out.println("tasksName: " + tasksName);
                                   //这条语句要保存下来
                                   
                                   HashMap map = storeChanges.getEnhancedForStatementMap();
                                   List list = Tools.hasBlockParent(enhancedForStatement);
                                   map.put("node",enhancedForStatement);
                                   map.put("index",list.get(0));
                                   map.put("parent",list.get(2));
                                   inOneForStatement = true;
                                   hasInvokeAllMethod = true;
                                   hasFutureList = true;
                                   hasGetMethod = true;
                                   System.out.println("findInvokeAllAndGetTogether parent is: " + list);
                                   return true;
                               }

                           }
                            
                        }
                        
                    }
 
                }

            }
           
        }
        return false;

    }
    
    
    
    

}
