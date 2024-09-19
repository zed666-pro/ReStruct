package addannotation.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import addannotation.utils.RefactorType;


@SuppressWarnings("unchecked")
public class GeneralVisitor {

	public static String refactorType = "";
	public static RefactorType type;
	    // 收集一个java文件中所有的类
		public static List<TypeDeclaration> getTypeDeclaration(ASTNode cuu) {
		    
            List<TypeDeclaration> list = new ArrayList<TypeDeclaration>();
			cuu.accept(new ASTVisitor() {
				@Override
				public boolean visit(TypeDeclaration node) {
				    list.add(node);
					return true;
				}
			});
			return list;

	 }
		

		public static List<MethodDeclaration> getMethodDeclaration(ASTNode cuu) {
		    List<MethodDeclaration> list = new ArrayList<MethodDeclaration>();
 			cuu.accept(new ASTVisitor() {
				@Override
				public boolean visit(MethodDeclaration node) {
					list.add(node);
					return true;
				}
			});
 			return list;
		}
		
		
		public static List<EnhancedForStatement> getEnhancedForStatements(ASTNode cuu) {
            List<EnhancedForStatement> list = new ArrayList<EnhancedForStatement>();
            cuu.accept(new ASTVisitor() {
                @Override
                public boolean visit(EnhancedForStatement node) {
                    list.add(node);
                    return true;
                }
            });
            return list;
        }
        
		
		
		public static List<TryStatement> getTryStatement(ASTNode cuu) {
		    List<TryStatement> list = new ArrayList<TryStatement>();
			cuu.accept(new ASTVisitor() {
				@Override
				public boolean visit(TryStatement node) {
//					System.out.println("获取到tryStatement");
					list.add(node);
					return true;
				}
			});
			return list; 
		}
		
		
		/**
		 * 
		 * @param cuu
		 * @return 通过返回的类型来判断程序需要采用哪一种搜索类型
		 */
		public static String checkRefactorType(ASTNode cuu) {
		 
		   
		    refactorType = "";

		    cuu.accept(new ASTVisitor() {
                @Override
                public boolean visit(MethodInvocation node) {
                   
                        String method = node.getName().toString();
                        if (method.equals("invokeAll")) {

                            refactorType = "invokeAll";
                            System.out.println("methodInvocation is:" + node);
                            return false;  
                         }

                    return true;
                }
            });
		    
		    
//		    cuu.accept(new ASTVisitor() {
//              @Override
//              public boolean visit(ClassInstanceCreation node) {
//                  if(node.getType() instanceof ParameterizedType) {
//                      ParameterizedType paramType = (ParameterizedType)node.getType();
//                      SimpleType simpleType = (SimpleType)paramType.getType();
//                      String identifier = simpleType.getName().toString();
//                      if(identifier.equals("ExecutorCompletionService")) {
//                          refactorType = "completionService";
//                          return false;
//                          
//                      }
//                      
//                  } 
//                  return true;
//              }
//          });
            return refactorType;
	   
		}
    
}


