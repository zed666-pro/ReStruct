package addannotation.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import addannotation.store.InvokeAllStore;
import addannotation.utils.Tools;
import addannotation.visitor.GeneralVisitor;
import addannotation.visitor.invokeAll.InvokeAllVisitor;

@SuppressWarnings("rawtypes")
public abstract class InvokeAllRefactor implements Refactor {
    public static String[] importName = {"jdk","incubator","concurrent","StructuredTaskScope"};
    CompilationUnit astRoot;
    //重构是在一个方法中进行的
    MethodDeclaration methodDeclaration;
    //future.get和invokeAll处于的同一个tryStatement
    TryStatement tryStatement;
    LinkedHashMap executorMap;
    LinkedHashMap invokeAllMap;
    //其中getMap种包含get方法外部的for节点
    LinkedHashMap futureGetMap;
    AST root ;
    
	
	
	public InvokeAllRefactor(InvokeAllStore storeChanges) {
       
        this.astRoot = storeChanges.getAstRoot();
        this.methodDeclaration = storeChanges.getMethodDeclaration();
        this.tryStatement = storeChanges.getTryStatement();
        this.executorMap = storeChanges.getExecutorMap();
        this.invokeAllMap = storeChanges.getInvokeAllMap();
        this.futureGetMap = storeChanges.getFutureGetMap();
        this.root = astRoot.getAST();;
    }

    public void importRefactor() {
	    
	    List<ImportDeclaration> importList = astRoot.imports();
		String [] importContent = importName;	
		ImportDeclaration newImport = root.newImportDeclaration();
		newImport.setName(root.newName(importContent));
		importList.add(newImport);
		
	}
	
    // add resource，公用代码
    public void addResource(TryStatement tryStatement) {

        // 设置variableDeclarationExpression左边的部分
        // 变量类型
        Type type = root.newSimpleType(root.newSimpleName("var"));

        // 变量名
        SimpleName scope = root.newSimpleName("scope");
        // 创建VariableDeclarationFragment，由name和Initializer组成
        VariableDeclarationFragment fragment = root.newVariableDeclarationFragment();
        // 其中的Initializer是由ClassInstanceCreation组成的
        ClassInstanceCreation creation = root.newClassInstanceCreation();
        // ClassInstanceCreation是由Type 和参数组成的 （这里的参数为空，所有没填）
        creation.setType(root.newSimpleType(root.newName("StructuredTaskScope.ShutdownOnFailure")));
        fragment.setInitializer(creation);
        fragment.setName(scope); // 设置变量名
        VariableDeclarationExpression varExpr = root.newVariableDeclarationExpression(fragment);
        varExpr.setType(type); // 设置变量类型
        tryStatement.resources().add(varExpr);


    }
    
    @SuppressWarnings({"unchecked", "unused"})
    protected void replaceInvokeAllWithFor(int index,List<Statement> statementsList,String futuresName) {
        // 将原先的invokeAll语句换成for
       EnhancedForStatement forStatement = root.newEnhancedForStatement();
       // 设置循环变量
       SingleVariableDeclaration loopVar = root.newSingleVariableDeclaration();
       loopVar.setType(root.newSimpleType(root.newName(InvokeAllVisitor.taskType))); // 变量类型为Callable或者其实现类
       loopVar.setName(root.newSimpleName("task1")); // 设置变量名为i
       forStatement.setParameter(loopVar);

       // 设置迭代器
       forStatement.setExpression(root.newSimpleName(InvokeAllVisitor.tasksName));
       
       // 设置循环体
       Block body = root.newBlock();
       // 设置body中的语句

       MethodInvocation methodArguement = root.newMethodInvocation();
       methodArguement.setExpression(root.newSimpleName("scope"));
       methodArguement.setName(root.newSimpleName("fork"));
       methodArguement.arguments().add(root.newSimpleName("task1"));

       MethodInvocation invocation = root.newMethodInvocation();
       invocation.setExpression(root.newSimpleName(futuresName));
       invocation.setName(root.newSimpleName("add"));
       invocation.arguments().add(methodArguement);

       ExpressionStatement newStatement = root.newExpressionStatement(invocation);
       body.statements().add(newStatement);
       forStatement.setBody(body);
       statementsList.add(index,forStatement);

   }
    
    
    @SuppressWarnings({ "unused", "unchecked" })
    protected void addCollectionsOfFuture(String collectionType, List<Statement> statements) {
        // 组装一个形如：List<future<xxx>> = new ArrayList()的语句，定义在
        // try的body中第一句
        ClassInstanceCreation creation = addCreation(collectionType);
       

        // 如果也存在，则删除List<Future<xxx>> futures
        if (invokeAllMap.containsKey("anotherNode")) {
            ExpressionStatement invokeAllExpresstion = (ExpressionStatement) invokeAllMap.get("node");
            VariableDeclarationStatement varStmt = addAndReturnFutureList("anotherNode",creation,statements);
            varStmt.delete();
            invokeAllExpresstion.delete();
        } else {
            VariableDeclarationStatement varStmt = addAndReturnFutureList("node",creation,statements);
            // 删除原先的invokeAll语句
            varStmt.delete();
        }

    }
    
    public VariableDeclarationStatement addAndReturnFutureList(String nodeName,ClassInstanceCreation creation,List<Statement> statements) {
        
        VariableDeclarationStatement varStmt = (VariableDeclarationStatement) invokeAllMap.get(nodeName);
        VariableDeclarationFragment fragment1 = (VariableDeclarationFragment) varStmt.fragments().get(0);
        fragment1.setInitializer(creation);
        VariableDeclarationStatement newVarDecStmt1 = (VariableDeclarationStatement) ASTNode.copySubtree(root,
                varStmt);
        // 在block的中的第一条语句
        statements.add(0, newVarDecStmt1);
        return varStmt;   
        
    }
    
    public ClassInstanceCreation addCreation(String collectionType) {
        
        ClassInstanceCreation creation = root.newClassInstanceCreation();
        if (collectionType.contains("List")) {
            creation.setType(root.newSimpleType(root.newName("ArrayList")));
        } else if (collectionType.contains("Set")) {
            creation.setType(root.newSimpleType(root.newName("HashSet")));
        } else {
            System.out.println("Collection<future<xxx>>中collection的类型是：" + collectionType);
        }
        
        return creation;
        
    }
    
//    @SuppressWarnings({"unchecked", "unused"})
//    protected void replaceInvokeAllWithFor(VariableDeclarationStatement invokeAllStatement, int index) {
//        // 将原先的invokeAll语句换成for
//       EnhancedForStatement forStatement = root.newEnhancedForStatement();
//       // 设置循环变量
//       SingleVariableDeclaration loopVar = root.newSingleVariableDeclaration();
////       System.out.println("taskType is:" + InvokeAllVisitor.taskType);
////       System.out.println("tasksName is:" + InvokeAllVisitor.tasksName);
//       loopVar.setType(root.newSimpleType(root.newName(InvokeAllVisitor.taskType))); // 变量类型为Callable或者其实现类
//       loopVar.setName(root.newSimpleName("task1")); // 设置变量名为i
//       forStatement.setParameter(loopVar);
//
//       // 设置迭代器
//       forStatement.setExpression(root.newSimpleName(InvokeAllVisitor.tasksName));
//
//       // 设置循环体
//       Block body = root.newBlock();
//       // 设置body中的语句
//
//       MethodInvocation methodArguement = root.newMethodInvocation();
//       methodArguement.setExpression(root.newSimpleName("scope"));
//       methodArguement.setName(root.newSimpleName("fork"));
//       methodArguement.arguments().add(root.newSimpleName("task1"));
//
//       MethodInvocation invocation = root.newMethodInvocation();
//       VariableDeclarationFragment fragment = (VariableDeclarationFragment) invokeAllStatement.fragments().get(0);
//       String collectionRef = fragment.getName().toString();
//       invocation.setExpression(root.newSimpleName(collectionRef));
//       invocation.setName(root.newSimpleName("add"));
//       invocation.arguments().add(methodArguement);
//
//       ExpressionStatement newStatement = root.newExpressionStatement(invocation);
//       body.statements().add(newStatement);
//       forStatement.setBody(body);
//
//       tryStatement.getBody().statements().add(index, forStatement);
//       
//       
//   }
    
    
    /**
     * @description:将future.get方法替换成future.resultNow
     */
    public void addResultNow(MethodInvocation methodInvocation) {

        methodInvocation.setName(root.newSimpleName("resultNow"));
       
    }
    
    
    /**
     * 
     * @param index:future.get所在for循环所处的位置
     */
    @SuppressWarnings("unchecked")
    public void addJoin(int index, List<Statement> statements) {
        // 增加一个join方法

        MethodInvocation joinInvocation = root.newMethodInvocation();
        joinInvocation.setExpression(root.newSimpleName("scope"));
        joinInvocation.setName(root.newSimpleName("join"));
        statements.add(index, root.newExpressionStatement(joinInvocation));
        
        MethodInvocation throwIfFailed = root.newMethodInvocation();
        throwIfFailed.setExpression(root.newSimpleName("scope"));
        throwIfFailed.setName(root.newSimpleName("throwIfFailed"));
        statements.add(index + 1, root.newExpressionStatement(throwIfFailed));

    }
    
    public void increaseRefactorNumberAndSoOn() {
        Tools.hasRefactored++;
        GeneralVisitor.refactorType = "";
    }
     
     
     //删除和Executor相关的方法，如shutdown，awaitTermination和定义executor的语句
     public void deleteExecutorRelated() {
         //如果Executors语句是在body中找到的才删除
         if(InvokeAllVisitor.hasExecutorsInBody) {
             VariableDeclarationStatement definition = (VariableDeclarationStatement)executorMap.get("node");
             ExpressionStatement shutdown = (ExpressionStatement)executorMap.get("shutdown");
             ExpressionStatement awaitTermination = (ExpressionStatement)executorMap.get("awaitTermination");
             
             definition.delete();
             if(shutdown != null) {
                 shutdown.delete();
             }
           
             if(awaitTermination != null) {
                 awaitTermination.delete();
             }
          
         }
         
         
     }

    @Override
    public String toString() {
        return "InvokeAllRefactor [astRoot=" + astRoot + ", methodDeclaration=" + methodDeclaration + ", tryStatement="
                + tryStatement + ", executorMap=" + executorMap + ", invokeAllMap=" + invokeAllMap + ", futureGetMap="
                + futureGetMap + ", root=" + root + "]";
    }

 
	

	
   
    
   
}
