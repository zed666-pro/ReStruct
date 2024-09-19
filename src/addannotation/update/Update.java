package addannotation.update;

import addannotation.refactoring.AnnotationRefactoring;
import addannotation.store.InvokeAllStore;
import addannotation.store.StoreNode;
import addannotation.utils.RefactorType;
import addannotation.visitor.GeneralVisitor;

public class Update {
    Refactor refactor;
    
    

    public Update(Refactor refactor) {
        super();
        this.refactor = refactor;
    }



    public boolean refactor() {
        
      
        return refactor.update();
    }
    
   

}
