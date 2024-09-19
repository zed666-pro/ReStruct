package addannotation.update;

import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import addannotation.store.InvokeAllStore;
import addannotation.utils.Tools;
import addannotation.visitor.invokeAll.InvokeAllVisitor;

/*
 * invokeAll重构的一种形式
 *     其中的invokeAll和future.get方法在同一个try结构内
 */
public class InvokeAllRefactorWithTry extends InvokeAllRefactor {

    public InvokeAllRefactorWithTry(InvokeAllStore storeChanges) {
        // TODO Auto-generated constructor stub
        super(storeChanges);
        
    }

    @Override
    public boolean update() {
        // TODO Auto-generated method stub
        
        refactor(); 
        return true;

    }

    @SuppressWarnings("unused")
    public void refactor() {
        // 准备具体重构方法所需的参数
        Integer executorIndex = (Integer) executorMap.get("index");
        VariableDeclarationStatement executorStmt = (VariableDeclarationStatement) executorMap.get("node");
        Integer invokeAllIndex = (Integer) invokeAllMap.get("index");
        String collectinoType = (String) invokeAllMap.get("collectionType");
        Block invokeParent = (Block) invokeAllMap.get("parent");
        List<Statement> BlockStatements = invokeParent.statements();
        Integer forIndex = (Integer) futureGetMap.get("index");
        MethodInvocation methodInvocation = (MethodInvocation) futureGetMap.get("node");
        System.out.println("executorIndex：" + executorIndex);
        System.out.println("invokeAllIndex：" + invokeAllIndex);
        System.out.println("invokeParent：" + invokeParent);
        System.out.println("forIndex：" + forIndex);
        // 调用具体的重构方法
        addResource(tryStatement);
        addJoin(forIndex, BlockStatements);
        replaceInvokeAllWithFor(invokeAllIndex,BlockStatements,InvokeAllVisitor.futuresName);
        addCollectionsOfFuture(collectinoType,BlockStatements);
        addResultNow(methodInvocation);
        deleteExecutorRelated();
        increaseRefactorNumberAndSoOn();

    }

    public void handleFinally(TryStatement tryStatement) {
        // 如果finally中只有一句话，且是shutdown，可以删除掉整个finally
        // 如果有多个语句，只删除掉其中的shutdown语句
        List<Statement> statements = tryStatement.getFinally().statements();
        
        if (statements.size() == 1 && statements.get(0) instanceof ExpressionStatement) {

            ExpressionStatement expression = (ExpressionStatement) statements.get(0);
            if (expression.getExpression() instanceof MethodInvocation) {
                MethodInvocation invocation = (MethodInvocation) expression.getExpression();
                String closeName = invocation.getName().toString();
                if (closeName.equals("shutdown")) {
                    tryStatement.setFinally(null);

                }
            }
        } else {
            for (Statement statement : statements) {
                if (statement instanceof ExpressionStatement) {

                    ExpressionStatement expression = (ExpressionStatement) statement;
                    if (expression.getExpression() instanceof MethodInvocation) {
                        MethodInvocation invocation = (MethodInvocation) expression.getExpression();
                        String closeName = invocation.getName().toString();
                        if (closeName.equals("shutdown")) {
                            tryStatement.getFinally().statements().remove(statement);

                        }
                    }

                }

            }

        }
    }




}
