package addannotation.responsibility;

import org.eclipse.jdt.core.dom.ASTNode;

import addannotation.store.InvokeAllStore;

public abstract class Handler {
    Handler handler;
    private InvokeAllStore storeChanges;
    private ASTNode cuu;
    
    
    
    public Handler(InvokeAllStore storeChanges, ASTNode cuu) {
        super();
        this.storeChanges = storeChanges;
        this.cuu = cuu;
    }


    public void setHandle(Handler handler) {
        this.handler = handler;
        
    }
    
    
    public abstract boolean handlerRequest(ASTNode cuu,InvokeAllStore storeChanges);


    
}
