package addannotation.store;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import addannotation.visitor.GeneralVisitor;

@SuppressWarnings("rawtypes")
public class InvokeAllStore extends StoreNode {
    
    //executor对象
    LinkedHashMap executorMap;

    //invokeAll语句，因为可能需要记录位置信息
    LinkedHashMap invokeAllMap;

    //get和get方法的外层循环
    LinkedHashMap futureGetMap;
    //invokeAll 和 for之间的所有语句，包括invokeAll和for本身
    List<Statement> statements;
    //后续再查找的目标代码的时候加入
    MethodDeclaration methodDeclaration;
    TryStatement tryStatement;
    CompilationUnit astRoot;
    TypeDeclaration typeDeclaration;
    //future.get 和 invokeAll在一个try中使用
    LinkedHashMap enhancedForStatementMap;
    
    MethodInvocation methodInvocation;
    

    
    public InvokeAllStore(CompilationUnit astRoot,MethodDeclaration method) {
        super();
        this.executorMap = new LinkedHashMap();
        this.invokeAllMap = new LinkedHashMap();
        this.futureGetMap = new LinkedHashMap();
        this.enhancedForStatementMap = new LinkedHashMap();
        this.statements = new ArrayList<Statement>();
        this.astRoot = astRoot;
        this.methodDeclaration = method;
    }

    
    //入股重构失败则清空集合中的数据
    public void clear() {
        
        executorMap.clear();
        invokeAllMap.clear();;
        futureGetMap.clear();;
      
    }


    public LinkedHashMap getExecutorMap() {
        return executorMap;
    }




    public void setExecutorMap(LinkedHashMap executorMap) {
        this.executorMap = executorMap;
    }

    public LinkedHashMap getInvokeAllMap() {
        return invokeAllMap;
    }




    public void setInvokeAllMap(LinkedHashMap invokeAllMap) {
        this.invokeAllMap = invokeAllMap;
    }




    public LinkedHashMap getFutureGetMap() {
        return futureGetMap;
    }




    public void setFutureGetMap(LinkedHashMap futureGetMap) {
        this.futureGetMap = futureGetMap;
    }




    public MethodDeclaration getMethodDeclaration() {
        return methodDeclaration;
    }




    public void setMethodDeclaration(MethodDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }




    public CompilationUnit getAstRoot() {
        return astRoot;
    }




    public void setAstRoot(CompilationUnit astRoot) {
        this.astRoot = astRoot;
    }




    public TryStatement getTryStatement() {
        return tryStatement;
    }




    public void setTryStatement(TryStatement tryStatement) {
        this.tryStatement = tryStatement;
    }


   

    public List<Statement> getStatements() {
        return statements;
    }



    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }
    
    


    public TypeDeclaration getTypeDeclaration() {
        return typeDeclaration;
    }


    public void setTypeDeclaration(TypeDeclaration typeDeclaration) {
        this.typeDeclaration = typeDeclaration;
    }
    
    


   
    
    


    public LinkedHashMap getEnhancedForStatementMap() {
        return enhancedForStatementMap;
    }


    public void setEnhancedForStatementMap(LinkedHashMap enhancedForStatementMap) {
        this.enhancedForStatementMap = enhancedForStatementMap;
    }


    public MethodInvocation getMethodInvocation() {
        return methodInvocation;
    }


    public void setMethodInvocation(MethodInvocation methodInvocation) {
        this.methodInvocation = methodInvocation;
    }


    @Override
    public String toString() {
        return "InvokeAllStore [executorMap=" + executorMap + ", invokeAllMap=" + invokeAllMap + ", futureGetMap="
                + futureGetMap + ", statements=" + statements + ", methodDeclaration=" + methodDeclaration
                + ", tryStatement=" + tryStatement + ", astRoot=" + astRoot + "]";
    }

}
