package addannotation.update;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import addannotation.store.InvokeAllStore;
import addannotation.visitor.invokeAll.InvokeAllVisitor;

public class InvokeAllRefactorInOneFor extends InvokeAllRefactor {

    MethodInvocation methodInvocation;
    HashMap enhancedForStatementMap;

    public InvokeAllRefactorInOneFor(InvokeAllStore storeChanges) {
        super(storeChanges);
        // 因为没有tryStatement，所以创建一个
        this.tryStatement = root.newTryStatement();
        this.methodInvocation = storeChanges.getMethodInvocation();
        this.enhancedForStatementMap = storeChanges.getEnhancedForStatementMap();

    }

    @Override
    public boolean update() {
        // 准备具体重构方法所需的参数
        Integer index = (Integer) enhancedForStatementMap.get("index");
        EnhancedForStatement forStatement = (EnhancedForStatement) enhancedForStatementMap.get("node");
        Block parentBlock = (Block) enhancedForStatementMap.get("parent");
        List parentStatements = parentBlock.statements();
        parentStatements.add(index, tryStatement);
        Block block = root.newBlock();
        List<Statement> statementsList = block.statements();
        tryStatement.setBody(block);
        
        addResource(tryStatement);
        System.out.println("tryStatement1 is:" + tryStatement);
        addCollectionsOfFuture(statementsList);
        System.out.println("tryStatement2 is:" + tryStatement);
        deleteExecutorRelated();
        // 先要将目标改为resultnow，不然后续无法获取get方法的位置信息
        addResultNow(methodInvocation);
       

        replaceInvokeAllWithFor(1, statementsList,"futures");
        System.out.println("tryStatement3 is:" + tryStatement);
        addJoin(2, statementsList);
        modifyForStatement(forStatement, statementsList);
        increaseRefactorNumberAndSoOn();
        return true;
    }

    @SuppressWarnings({ "unused", "unchecked" })
    protected void addCollectionsOfFuture(List<Statement> statementsList) {
        // 组装一个形如：List<future<xxx>> = new ArrayList()的语句，定义在
        // try的body中第一句
        
        VariableDeclarationFragment vFragment = root.newVariableDeclarationFragment();
        ClassInstanceCreation creation = addCreation();
        ParameterizedType parameterizedType = addParameterizedType();
        vFragment.setName(root.newSimpleName("futures"));
        vFragment.setInitializer(creation);

        VariableDeclarationStatement vStatement = root.newVariableDeclarationStatement(vFragment);
        vStatement.setType(parameterizedType);

        // 输出生成的代码
        System.out.println("var is: " + vStatement.toString());
        statementsList.add(0, vStatement);

    }

    public ClassInstanceCreation addCreation() {

        ClassInstanceCreation creation = root.newClassInstanceCreation();

        creation.setType(root.newSimpleType(root.newName("ArrayList")));

        return creation;

    }

    public ParameterizedType addParameterizedType() {
        ParameterizedType parameterizedType = root.newParameterizedType(root.newSimpleType(root.newName("List")));
        ParameterizedType argument = root.newParameterizedType(root.newSimpleType(root.newName("Future")));
        // List<Future<Integer<>>> futures=new ArrayList();
        ParameterizedType argument1 = root
                .newParameterizedType(root.newSimpleType(root.newName(InvokeAllVisitor.tasKind)));
        argument.typeArguments().add(argument1);
        parameterizedType.typeArguments().add(argument);

        return parameterizedType;

    }


    public void modifyForStatement(EnhancedForStatement forStatement, List<Statement> statementList) {
        forStatement.setExpression(root.newSimpleName("futures"));
        EnhancedForStatement enhancedForStatement = (EnhancedForStatement) ASTNode.copySubtree(root, forStatement);
        System.out.println("enhancefor :" + enhancedForStatement);
        forStatement.delete();

    }

}
