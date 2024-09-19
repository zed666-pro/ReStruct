package addannotation.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;


import addannotation.refactoring.AnnotationRefactoring;
import addannotation.refactoring.AnnotationRefactoringWizard;


public class AnnotationManageAction implements IWorkbenchWindowActionDelegate {
    

    // used by method selectionChanged
    IJavaProject select;
	// open the window
	IWorkbenchWindow window;

	/**
	 * This method is used to open a window
	 */
	@Override
	public void run(IAction action) {
	
		Shell shell = window.getShell();
		
		AnnotationRefactoring refactor = new AnnotationRefactoring(select);
		AnnotationRefactoringWizard wizard = new AnnotationRefactoringWizard(refactor);
		RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
		try {
		
			op.run(shell, "Inserting @Override Annotation");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

    /**
     * changed as user selected
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
     
        if (selection.isEmpty())
            select = null;
        else if (selection instanceof IStructuredSelection) {
            IStructuredSelection strut = ((IStructuredSelection) selection);
            if (strut.size() != 1)
                select = null;
            if(strut.getFirstElement() instanceof IProject) {
                IProject project = (IProject) strut.getFirstElement();
                select = JavaCore.create(project);
            }else if(strut.getFirstElement() instanceof IJavaElement) {
                IJavaElement select = (IJavaElement) strut.getFirstElement();
                select = select.getJavaProject();
            } else
            select = null;
//      action.setEnabled(true);
        action.setEnabled(select != null);
    }
}

	@Override
	public void dispose() {
		
	}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

}
