package addannotation.refactoring;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.type.DeclaredType;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.ui.refactoring.TextEditChangeNode.ChildNode;
import org.eclipse.text.edits.TextEdit;
import org.w3c.dom.ls.LSOutput;

import addannotation.analysis.FindClassPath;
import addannotation.analysis.SootConfig;
import addannotation.search.FindAndRefactor;
import addannotation.store.StoreNode;
import addannotation.update.Update;
import addannotation.utils.RefactorType;
import addannotation.utils.Statistic;
import addannotation.utils.Tools;
import addannotation.visitor.GeneralVisitor;



/**
 * 此类是重构的动作类 重构的预览也是通过此类完成
 */
@SuppressWarnings("unused")
public class AnnotationRefactoring extends Refactoring {
	// 所有的重构变化
	List<Change> changeManager = new ArrayList<Change>();
	// 所有需要修改的JavaElement
	List<IJavaElement> compilationUnits = new ArrayList<IJavaElement>();
	
    HashSet<Document> doucumentSet = new HashSet();
	
    public static Long startTime = 0l;
    public static long endTime = 0l;
	
	// @Test ' s parameter
	boolean needTimeout = false;
	String timeoutValue = "500";
	
	
    

	/**
	 * 重构的构造方法
	 * 
	 * @param element
	 */
	
	public AnnotationRefactoring(IJavaProject element) {
	    
		findAllCompilationUnits(element);
       
        
	}

	
	/**
	 * 重构的后置条件，用于检查用户输入参数后是否满足某个条件
	 */
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		try {
			collectChanges();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		if (changeManager.size() == 0)
			return RefactoringStatus.createFatalErrorStatus("No testing methods found!");
		else
			return RefactoringStatus.createInfoStatus("Final condition has been checked");
	}

	
	
	/**
	 * 重构初始条件，用于检查重构开始前应满足的条件
	 */
	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
			throws CoreException, OperationCanceledException {
		return RefactoringStatus.createInfoStatus("Initial Condition is OK!");
	}

	/**
	 * 重构的代码变化 如果代码变化多于一处，则通过CompositeChange来完成
	 */
	@Override
	public Change createChange(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		Change[] changes = new Change[changeManager.size()];
//		System.out.println("changeManager size is :" + changeManager.size());
		System.arraycopy(changeManager.toArray(), 0, changes, 0, changeManager.size());
		CompositeChange change = new CompositeChange("Add @Test annotation", changes);
		
		return change;
	}
	
	/**
	 * This method must have a return value, otherwise the finish button is not
	 * available
	 */
	@Override
	public String getName() {
		return "hello world";
	}

    /**
     * iterate the project to find in all IPackageFragment
     * @param project
     */
    private void findAllCompilationUnits(IJavaProject project) {
        startTime = System.currentTimeMillis();
//        IPath filename = project.getJavaProject().getProject().getLocation();
//        FindClassPath findClassPath = new FindClassPath(filename);
//        List<String> filePaths  = findClassPath.getClassesFile();
//        if(filePaths != null) {
//            System.out.println("filePaths:" + filePaths.toString());
//            SootConfig config = new SootConfig();
//            config.setupSoot(filePaths);
//            
//        }
        long programTime = System.currentTimeMillis() - startTime;
     
        
        try {
            for (IJavaElement element : project.getChildren()) { // IPackageFragmentRoot
//              if (element.getElementName().equals("src")) {
                    IPackageFragmentRoot root = (IPackageFragmentRoot) element;
                    for (IJavaElement ele : root.getChildren()) {
                        if (ele instanceof IPackageFragment) {
                            IPackageFragment fragment = (IPackageFragment) ele;
                            for (ICompilationUnit unit : fragment.getCompilationUnits()) {
                                compilationUnits.add(unit);
                            }
                        }
                    }
                }
//          }
            
        } catch (JavaModelException e) {
            e.printStackTrace();
        }
        
    }

	

	/**
	 * create all the changes
	 * @throws JavaModelException
	 * @throws IOException
	 */
	private void collectChanges() throws JavaModelException, IOException {
	   
		for (IJavaElement element : compilationUnits) {
		    
		    //astRoot一个java文件的根
		    //document根据java文件创建的Document对象
			ICompilationUnit cu = (ICompilationUnit) element;
			 IFile file = (IFile) cu.getResource();
			 System.out.println("类的名称为：" + file.getName());
			// 获取对应的.java文件对应的源码
			String source = cu.getSource();
			Document document = new Document(source);
			CompilationUnit astRoot = createParser(cu);
			
			
			findAndRefactor(cu,document,astRoot);
			
			
		}
		
		System.out.println("invokeAll count :" + Tools.invokeAllCount);
		System.out.println("refacotr count :" + Tools.hasRefactored);
		System.out.println("refactor time :" + (System.currentTimeMillis() - startTime));
		System.out.println("type 1 :" + Statistic.IN_ONE_TRY);
		System.out.println("type 2 :" + Statistic.NOT_IN_ONE_TRY);
		
		startTime = 0l;
		

	}
	
	
	//根据ICompilationUnit创建一个AST对象
	private CompilationUnit createParser(ICompilationUnit cu) {
		
		ASTParser parser = ASTParser.newParser(AST.JLS18);
		parser.setSource(cu);
		parser.setResolveBindings(true); // 打开绑定
		parser.setEnvironment(null, null, null, true); // setEnvironment（classpath,sourcepath,encoding,true）
		parser.setUnitName("example.java"); // 参数随意
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		
		return astRoot;
	}
	
	private void findAndRefactor(ICompilationUnit cu,Document document,CompilationUnit astRoot) {
		
		List<TypeDeclaration> typeList = GeneralVisitor.getTypeDeclaration(astRoot);
		astRoot.recordModifications();
		
		for(TypeDeclaration type : typeList) { 
		    
		    Tools.invokeAllInClass = 0;
		    boolean flag = false;
		    List<MethodDeclaration> methods = GeneralVisitor.getMethodDeclaration(type);
		    for(MethodDeclaration method : methods) {
		         
	             if(FindAndRefactor.findAndRefactor(astRoot, type,method)) {
	                 recordChanges(cu,document,astRoot); 
	                 
	             }
		        
		    }
		    if(Tools.invokeAllInClass >= 1) {
		          System.out.println("类名称为：" +type.getName().getFullyQualifiedName() + "invokeAll 数量为：" + Tools.invokeAllInClass);

		        
		    }
		
		}
	}


	
	//记录ast节点的变化情况
	private void recordChanges(ICompilationUnit cu,Document document,CompilationUnit astRoot) {
	    TextEdit edits = astRoot.rewrite(document, cu.getJavaProject().getOptions(true));
        TextFileChange change = new TextFileChange("", (IFile) cu.getResource());
        change.setEdit(edits);
        changeManager.add(change);  
	     
	}
}
