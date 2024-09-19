package addannotation.update;

import java.util.List;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import addannotation.store.InvokeAllStore;
import addannotation.utils.Tools;
import addannotation.visitor.invokeAll.InvokeAllVisitor;


/*
 * invokeAll重构的一种形式
 *     其中的invokeAll和future.get方法不在同一个try结构内
 */
public class InvokeAllRefactorNoTry extends InvokeAllRefactor{
    List<Statement> statements;
    
    public InvokeAllRefactorNoTry(InvokeAllStore storeChanges) {
        super(storeChanges);
        this.statements = storeChanges.getStatements();
        // 因为没有tryStatement，所以创建一个
        this.tryStatement = root.newTryStatement();
        
       
    }
    
    @Override
    public boolean update() {
        // TODO Auto-generated method stub
        refactor();
        return true;

    }

    @SuppressWarnings({ "unchecked", "unused", "rawtypes" })
    public void refactor() {
        // 准备具体重构方法所需的参数
        Integer forIndex = (Integer)futureGetMap.get("index");
        Integer invokeAllIndex = (Integer)invokeAllMap.get("index");
        Integer index = forIndex - invokeAllIndex;
        VariableDeclarationStatement executorStmt = (VariableDeclarationStatement)executorMap.get("node");
        String collectinoType = (String)invokeAllMap.get("collectionType");
        Block invokeParent = (Block)invokeAllMap.get("parent");
        MethodInvocation methodInvocation = (MethodInvocation)futureGetMap.get("node");

        //可以先删除和executor相关的部分，前提是可以删除，后续通过分析保证可行性
        deleteExecutorRelated();
        //先要将目标改为resultnow，不然后续无法获取get方法的位置信息
        addResultNow(methodInvocation);
      
        List<Statement> statementsList = getAndRemoveStatementBetweenInvokeAllAndGet(invokeAllIndex,invokeParent);
        addCollectionsOfFuture(collectinoType,statementsList);
        replaceInvokeAllWithFor(1,statementsList,InvokeAllVisitor.futuresName); 
        addJoin(statementsList.size() - 1,statementsList);
        addResource(tryStatement);
        increaseRefactorNumberAndSoOn();
       
    }
    

    
    
    public List<Statement> getAndRemoveStatementBetweenInvokeAllAndGet(Integer invokeAllIndex,Block invokeParent) {
        //1. 将原先的invokeAll和future.get之间的语句复制，并且删除原先的
        //2. 将block放入try的body中
        Block block = root.newBlock();
        List<Statement> statementsList = block.statements();
        //从1开始添加，因为里面第一条语句为invokeall，上面的replaceInvokeAllWithFor已经添加过了
        for(int i = 1;i < statements.size();i++) {
            //一边插入一边删除
            Statement statement = (Statement)ASTNode.copySubtree(root, statements.get(i));
            statementsList.add(statement);
            statements.get(i).delete();
      
         }
        //需要在invoekall和future.get的外层for上加入try
        invokeParent.statements().add(invokeAllIndex,tryStatement);
        tryStatement.setBody(block); 
        return statementsList;
         
    }
    
}
