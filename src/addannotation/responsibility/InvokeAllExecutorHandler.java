package addannotation.responsibility;

import org.eclipse.jdt.core.dom.ASTNode;

import addannotation.store.InvokeAllStore;
import addannotation.visitor.invokeAll.InvokeAllVisitor;
import addannotation.visitor.invokeAll.InvokeAllVisitor_executor;

public class InvokeAllExecutorHandler extends Handler {

    
    public InvokeAllExecutorHandler(InvokeAllStore storeChanges, ASTNode cuu) {
        super(storeChanges, cuu);
       
    }

    @Override
    public boolean handlerRequest(ASTNode cuu,InvokeAllStore storeChanges) {
        InvokeAllVisitor_executor.acquireExecutor(cuu, storeChanges);
        
        return InvokeAllVisitor.threadPoolName.equals("")? false : handler.handlerRequest(cuu, storeChanges);
       
        
    }

}
