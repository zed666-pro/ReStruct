package addannotation.responsibility;

import org.eclipse.jdt.core.dom.ASTNode;
import addannotation.store.InvokeAllStore;
import addannotation.visitor.invokeAll.InvokeAllVisitor;
import addannotation.visitor.invokeAll.InvokeAllVisitor_futureGet;


public class InvokeAllFutureGetHandler extends Handler {

    public InvokeAllFutureGetHandler(InvokeAllStore storeChanges, ASTNode cuu) {
        super(storeChanges, cuu);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean handlerRequest(ASTNode cuu, InvokeAllStore storeChanges) {
        //无论是否有get方法都应该返回true
        InvokeAllVisitor_futureGet.acquireFutureGet(cuu, storeChanges);
        InvokeAllVisitor.acquireExecutorRelated(cuu, storeChanges);
        if(InvokeAllVisitor.hasGetMethod) {
            System.out.println("hasGetMethod....");
            return handler.handlerRequest(cuu, storeChanges);   
        }
        return false;
    }

}
