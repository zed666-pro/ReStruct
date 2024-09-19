package addannotation.search;


import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.w3c.dom.ls.LSOutput;

import addannotation.responsibility.InvokeAllExecutorHandler;
import addannotation.responsibility.InvokeAllFutureGetHandler;
import addannotation.responsibility.InvokeAllRefactorHandler;
import addannotation.responsibility.InvokeAllMethodHandler;
import addannotation.store.InvokeAllStore;
import addannotation.visitor.GeneralVisitor;



@SuppressWarnings("unchecked")
public class FindAndRefactor {
    
    @SuppressWarnings("unused")
    public static boolean findAndRefactor(CompilationUnit astRoot,TypeDeclaration typeDeclaration,MethodDeclaration method) {
           

           
            //最好做一下前置检查，判断具体采用哪种类型的分析方式
            String type = checkRefactorType(method);
               
                 try {
                     // 找到需要修改的目标代码······
                     if(type.equals("invokeAll")) {
                             
                             InvokeAllStore storeChanges  = new InvokeAllStore(astRoot,method);
                             storeChanges.setTypeDeclaration(typeDeclaration);
                             //责任链模式
                             InvokeAllExecutorHandler executorHandler = new InvokeAllExecutorHandler(storeChanges, method);
                             InvokeAllMethodHandler methodHandler = new InvokeAllMethodHandler(storeChanges, method);
                             InvokeAllFutureGetHandler futureGetHandler = new InvokeAllFutureGetHandler(storeChanges, method);
                             InvokeAllRefactorHandler refactorHandler = new InvokeAllRefactorHandler(astRoot,storeChanges,typeDeclaration, method); 
                         
                             executorHandler.setHandle(methodHandler);
                             methodHandler.setHandle(futureGetHandler);
                             futureGetHandler.setHandle(refactorHandler);
                             return executorHandler.handlerRequest(method, storeChanges);
                               
                     }
                     
                     
                 }catch (Exception e) {
                    // TODO: handle exception
//                    throw new RuntimeException(e);
                   System.out.println("该方法不支持重构");
                }
               
              
           
              return false;
         }
       
            
    
            
        
 
    
    /**
     * @describtion:该方法返回一个String类型的结果，告知后续程序，采用哪种分析方式
     * @return 返回string类型
     */
    public static String checkRefactorType(MethodDeclaration method) {
        
           return GeneralVisitor.checkRefactorType(method);
    }
    


    
    
    
 }
 
    
   


