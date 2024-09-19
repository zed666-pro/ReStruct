package addannotation.update;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import addannotation.store.CommonRefactorNode;

public abstract class SourceRefactor {
	
	public CompilationUnit root;
	public TypeDeclaration type;
	public LinkedHashMap<Integer, Statement> executorMap; 
	public MethodDeclaration methodDeclaration;
	
	public SourceRefactor(CompilationUnit root,LinkedHashMap<Integer, Statement> executorMap,MethodDeclaration methodDeclaration) {
		this.root = root;
		this.executorMap = executorMap;
		this.methodDeclaration = methodDeclaration;
		
	}
	
	public void ImportRefactor() {
	
	    List<ImportDeclaration> importList = root.imports();
	    AST ast = root.getAST();
		
		String [] content = {"jdk","incubator","concurrent","StructuredTaskScope"};	//以后需要导入的可能不止一个
		List<String []> contents = new ArrayList<>();
		contents.add(content);
		
		for (String[] con : contents) {
			ImportDeclaration newImport = ast.newImportDeclaration();
			newImport.setName(ast.newName(con));
			importList.add(newImport);
		}

	}
	
	
	public abstract void tryRefactor();
	
	public void startRefactor() {
		ImportRefactor();
		tryRefactor();
		
	}
}
