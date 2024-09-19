package addannotation.visitor.invokeAll;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import addannotation.store.InvokeAllStore;
import addannotation.utils.Tools;

public class InvokeAllVisitor_executor extends InvokeAllVisitor {
    
    
    public static void acquireExecutor(ASTNode cuu, InvokeAllStore storeChanges) {
        hasExecutorsInBody = true;
        findExecutorInFieldDeclaration(storeChanges);
        if(hasExecutorsInBody) {
            findExecutorInBody(cuu, storeChanges); 
        }
        
    }
    
    
    /**
     * 
     * @param cuu
     * @param storeChanges
     * @return 是否在成员变量中存在线程池的定义
     */
    public static boolean findExecutorInFieldDeclaration(InvokeAllStore storeChanges) {
            TypeDeclaration type  = storeChanges.getTypeDeclaration();
            List bodyDeclarations = type.bodyDeclarations();
            for(int i = 0; i < bodyDeclarations.size(); i++){
                if(bodyDeclarations.get(i) instanceof FieldDeclaration) {
                    FieldDeclaration fieldDeclaration = (FieldDeclaration)bodyDeclarations.get(i);
                    Type declarationType = fieldDeclaration.getType();
                    if(declarationType instanceof SimpleType) {
                        SimpleType simpleType = (SimpleType)declarationType;
                        String executorType = simpleType.getName().toString();
                        //如果类型是ExecutorService再考虑，获取变量的名称
                        if(executorType.equals("ExecutorService") || executorType.equals("Threads")) {
                          List fragments = fieldDeclaration.fragments();
                          for(int k = 0;k < fragments.size(); k++) {
                              if(fragments.get(k) instanceof VariableDeclarationFragment) {
                                  VariableDeclarationFragment vFragment = (VariableDeclarationFragment)fragments.get(k);
                                  threadPoolName = vFragment.getName().getIdentifier().toString();
                                  System.out.println("acquireExecutorInFieldDeclaration： " + threadPoolName);
                                  //因为Executors在字段中找到，所以说明Executors不在Body中，所以是false
                                  hasExecutorsInBody = false;
                                  return true;    
                                  
                              }
                              
                              
                        
                          }
                            
                            
                        }
                        
                        
                    }
                    
                    
                }

        }
         return false;
    }
    
    public static void findExecutorInBody(ASTNode cuu, InvokeAllStore storeChanges) {
        System.out.println("findExecutorInBody");
        cuu.accept(new ASTVisitor() {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            public boolean visit(VariableDeclarationStatement node) {
                // TODO Auto-generated method stub
                String executorService = node.getType().toString();       
                if(node.fragments().get(0) instanceof VariableDeclarationFragment) {

                    VariableDeclarationFragment fragment = (VariableDeclarationFragment) node.fragments().get(0); // 存储所有的定义future的信息
                    if (fragment.getInitializer() instanceof MethodInvocation) {
        
                        MethodInvocation invocation = (MethodInvocation) fragment.getInitializer();
                        Expression expression = invocation.getExpression();
                        if(expression != null) {
                           String executors = expression.toString();
                           String poolName = invocation.getName().getIdentifier();
                           if (executorService.equals("ExecutorService") && (executors.equals("Executors") || executors.equals("Threads"))
                                   && poolName.substring(0, 3).equals("new")) {
                               //ExecutorService pool = Executors.newXXX()中的pool的名字
                               threadPoolName = fragment.getName().toString();
                               String poolNameRef = fragment.getName().getIdentifier();
                               
                               // 加入到InvokeAllStore中去
                               List list = Tools.hasBlockParent(node);
                               HashMap map = storeChanges.getExecutorMap();
                               map.put("index", list.get(0)); // 当前node所在block中位置
                               map.put("node", node); // 当前node
                               map.put("parent", list.get(2)); // 当前node的父节点block
                               return false;
                               
                           }
                            
                        }
                    
                    }
                }
          
                return true;
            }

        });
        
        
        
    }
}
