package addannotation.responsibility;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import addannotation.analysis.ScopeAnalysis;
import addannotation.analysis.ScopeAnalysisUpdate;
import addannotation.analysis.ThreadDependenceAnalysis;
import addannotation.store.InvokeAllStore;
import addannotation.update.InvokeAllRefactorNoTry;
import addannotation.update.InvokeAllRefactorInOneFor;
import addannotation.update.InvokeAllRefactorWithTry;

import addannotation.update.Update;
import addannotation.utils.RefactorType;
import addannotation.utils.Statistic;
import addannotation.utils.Tools;
import addannotation.visitor.GeneralVisitor;
import addannotation.visitor.invokeAll.InvokeAllVisitor;

public class InvokeAllRefactorHandler extends Handler {
    private CompilationUnit astRoot;
    private TypeDeclaration typeDeclaration;

    public InvokeAllRefactorHandler(CompilationUnit astRoot, InvokeAllStore storeChanges,
            TypeDeclaration typeDeclaration, ASTNode cuu) {
        // TODO Auto-generated constructor stub
        super(storeChanges, cuu);
        this.astRoot = astRoot;
        this.typeDeclaration = typeDeclaration;

    }

    @Override
    public boolean handlerRequest(ASTNode cuu, InvokeAllStore storeChanges) {
        /**
         * 如果是InvokeAll和get方法在一个try中
         *    1. 通过方法判断是否在同一个try结构中 
         *    2. 如果在调用对应的重构方法完成重构
         */
        
//        ThreadDependenceAnalysis.doAnalysis(null, null);
        if (InvokeAllVisitor.inOneForStatement) {
            Statistic.IN_ONE_TRY++;
            return new Update(new InvokeAllRefactorInOneFor(storeChanges)).refactor();
        }
        if (InvokeAllVisitor.checkInvokeAllAndGetInOneTry(cuu, storeChanges)) { 
            Statistic.IN_ONE_TRY++;
            return new Update(new InvokeAllRefactorWithTry(storeChanges)).refactor();
        } else {
            Statistic.NOT_IN_ONE_TRY++;
            MethodDeclaration methodDeclaration = (MethodDeclaration) cuu;
            String methodName = methodDeclaration.getName().getFullyQualifiedName();
            String className = Tools.getPackageName(astRoot, typeDeclaration);

            ScopeAnalysisUpdate scopeAnalysis = new ScopeAnalysisUpdate(className, methodName);
//            if (scopeAnalysis.doAnalysis())
//                return false;

            InvokeAllVisitor.acquireStmtsInInvokeAllAndFor(storeChanges);
            return new Update(new InvokeAllRefactorNoTry(storeChanges)).refactor();

        }

    }

}
