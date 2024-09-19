package addannotation.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/*
 * 用于求解一个map中最大或者是最小的key
 * */
public class Tools {
    public static int invokeAllCount = 0;
    public static int hasRefactored = 0;
    public static int invokeAllInClass = 0;
    
    public static Integer getMaxKey(Map<Integer, ?> map) {
        Integer maxKey = 0;
        for (Integer key : map.keySet()) {
            if (maxKey == 0 || key.compareTo(maxKey) > 0) {
                maxKey = key;
            }
        }
        return maxKey;
    }

    public static Integer getMinKey(Map<Integer, ?> map) {
        Integer maxKey = 0;
        for (Integer key : map.keySet()) {
            if (maxKey == 0 || key.compareTo(maxKey) < 0) {
                maxKey = key;
            }
        }
        return maxKey;
    }

    /**
     * @param node
     * @description: 判断一个节点的父节点是否是block
     */
    @SuppressWarnings({ "unchecked" })
    public static List<Object> hasBlockParent(ASTNode node) {
       
        ASTNode parent = node.getParent();
        Map<Integer, Statement> map = new HashMap<>();
        List<Object> list = new ArrayList<>();
        while (parent != null) {
            if (parent instanceof Block && node instanceof Statement) {
                Block block = (Block) parent;
                int index = block.statements().indexOf(node);
                list.add(index);   //node节点在parent中所处的位置
               
                list.add(node);    //parent的子节点
                list.add(parent);
                return list;
            }
            node = parent;
            parent = parent.getParent();
        }
        return null;
    }
    
    
    @SuppressWarnings({ "unchecked" })
    public static List<Object> hasBlockParent(ASTNode node,Block target) {
//      System.out.println("target is"+target);
       
        ASTNode parent = node.getParent();
        Map<Integer, Statement> map = new HashMap<>();
        List<Object> list = new ArrayList<>();
        while (parent != null) {
//            System.out.println("parent is:"+parent);
            if (parent instanceof Block && parent == target) {
                Block block = (Block) parent;
                int index = block.statements().indexOf(node);  //
                list.add(index);   //node节点在parent中所处的位置
               
                list.add(node);    //parent的子节点
                list.add(parent);
                return list;
            }
            node = parent;
            parent = parent.getParent();
        }
        return null;
    }
    
    public static void CountNumberOfInvokeAll() {
        
        invokeAllCount++;
        invokeAllInClass++;
        
    }
   
    
    
    public static String getPackageName(CompilationUnit astRoot,TypeDeclaration typeDeclaration) {
        PackageDeclaration packageDeclaration = astRoot.getPackage();
      
        String packageName = null;
        if (packageDeclaration != null) {
            packageName = packageDeclaration.getName().getFullyQualifiedName();
            System.out.println("packageName is:" + packageName);
        } else {
            
        }
        
       String className = typeDeclaration.getName().getIdentifier().toString();
        return packageName + "." + className; 
    }
}
