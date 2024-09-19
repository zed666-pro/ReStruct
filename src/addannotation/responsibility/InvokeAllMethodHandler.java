package addannotation.responsibility;

import org.eclipse.jdt.core.dom.ASTNode;

import addannotation.store.InvokeAllStore;
import addannotation.visitor.invokeAll.InvokeAllVisitor;
import addannotation.visitor.invokeAll.InvokeAllVisitor_invokeAll;

public class InvokeAllMethodHandler extends Handler {

    public InvokeAllMethodHandler(InvokeAllStore storeChanges, ASTNode cuu) {
        super(storeChanges, cuu);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean handlerRequest(ASTNode cuu, InvokeAllStore storeChanges) {
        System.out.println("invoke acquireInvokeALl");
        InvokeAllVisitor_invokeAll.acquireInvokeAll(cuu, storeChanges); 
        
        if(InvokeAllVisitor.hasInvokeAllMethod && InvokeAllVisitor.hasFutureList) {
           
            //如果有invokeAll方法的调用的话，一定会有对应的task，无需在新建一个类
            InvokeAllVisitor.acquireTasks(cuu, storeChanges);
            return handler.handlerRequest(cuu, storeChanges);
            
        }
        
        return false;
       
    }

}
