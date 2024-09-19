package addannotation.store;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class CommonRefactorNode {

    public LinkedHashMap<Integer, Statement> executorMap;
    public LinkedHashMap<Integer, Statement> futureMap;
    public LinkedHashMap<Integer, Statement> getMap;
    public LinkedHashMap<Integer, Statement> invokeMap;
    public List<Statement> stmtbeSubAndGet;
    public List<Statement> stmtbeGetAndInvoke;
    public MethodDeclaration methodDeclaration;

    public CommonRefactorNode(MethodDeclaration methodDeclaration) {
        this.executorMap = new LinkedHashMap<>();
        this.futureMap = new LinkedHashMap<>();
        this.getMap = new LinkedHashMap<>();
        this.invokeMap = new LinkedHashMap<>();
        this.methodDeclaration = methodDeclaration;
        this.stmtbeSubAndGet = new ArrayList<>();
        this.stmtbeGetAndInvoke = new ArrayList<>();
    }

    /*
     * 如果匹配失败的话，就是删除所有存储的node
     */
    public void delete() {
        futureMap.clear();
        getMap.clear();
        invokeMap.clear();
        executorMap.clear();
        stmtbeSubAndGet.clear();
        stmtbeGetAndInvoke.clear();

    }

    @Override
    public String toString() {
        return "CommonRefactorNode [executorMap=" + executorMap + ", futureMap=" + futureMap + ", getMap=" + getMap
                + ", invokeMap=" + invokeMap + ", stmtbeSubAndGet=" + stmtbeSubAndGet + ", stmtbeGetAndInvoke="
                + stmtbeGetAndInvoke + ", methodDeclaration=" + methodDeclaration + "]";
    }

}
