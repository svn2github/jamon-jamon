package org.jamon.eclipse;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

public class JamonProjectPropertyPage extends PropertyPage {

	private static final String PATH_TITLE = "Path:";
	private static final String OWNER_TITLE = "&Owner:";
	private static final String OWNER_PROPERTY = "OWNER";
	private static final String DEFAULT_OWNER = "John Doe";

	private static final int TEXT_FIELD_WIDTH = 50;

	private Text ownerText;

	/**
	 * Constructor for SamplePropertyPage.
	 */
	public JamonProjectPropertyPage() {
		super();
	}

	private Button isJamonProjectCheckbox;
	
	private IJavaProject getJavaProject() throws CoreException {
		IProject project = (IProject) (this.getElement().getAdapter(IProject.class));
		return (IJavaProject) (project.getNature(JavaCore.NATURE_ID));
	}
	
	private void addFirstSection(Composite parent) {
			Composite isTomcatProjectGroup = new Composite(parent,SWT.NONE);
			isTomcatProjectGroup.setLayout(new GridLayout(3, false));
			isTomcatProjectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			// project location entry field
			isJamonProjectCheckbox = new Button(isTomcatProjectGroup, SWT.CHECK | SWT.LEFT);
			isJamonProjectCheckbox.setText("Is Jamon Project");
			isJamonProjectCheckbox.setEnabled(true);

			try {		
				isJamonProjectCheckbox.setSelection(getJavaProject().getProject().hasNature(natureId()));
			} catch (CoreException ex) {
				ex.printStackTrace();
			}
	}
	
	private static String natureId() {
		return JamonPlugin.getDefault().getBundle().getSymbolicName() + ".jamonnature";
	}

	private static String builderId() {
		return JamonPlugin.getDefault().getBundle().getSymbolicName() + ".templateBuilder";
	}

	private void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}

	private void addSecondSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		// Label for owner field
		Label ownerLabel = new Label(composite, SWT.NONE);
		ownerLabel.setText(OWNER_TITLE);

		// Owner text field
		ownerText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(TEXT_FIELD_WIDTH);
		ownerText.setLayoutData(gd);

		// Populate owner text field
		try {
			String owner =
				((IResource) getElement()).getPersistentProperty(
					new QualifiedName("", OWNER_PROPERTY));
			ownerText.setText((owner != null) ? owner : DEFAULT_OWNER);
		} catch (CoreException e) {
			ownerText.setText(DEFAULT_OWNER);
		}
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addFirstSection(composite);
		addSeparator(composite);
		addSecondSection(composite);
		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	protected void performDefaults() {
		// Populate the owner text field with the default value
		ownerText.setText(DEFAULT_OWNER);
	}

	private static void addNatureToProject(IProject project, String natureId) throws CoreException {
		IProject proj = project.getProject(); // Needed if project is a IJavaProject
		IProjectDescription description = proj.getDescription();
		String[] prevNatures= description.getNatureIds();

		int natureIndex = -1;
		for (int i=0; i<prevNatures.length; i++) {
			System.err.println(prevNatures[i]);
			if(prevNatures[i].equals(natureId)) {
				return;
			}
		}
		System.err.println(natureId);
		// Add nature only if it is not already there
		String[] newNatures= new String[prevNatures.length + 1];
		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
		newNatures[prevNatures.length]= natureId;
		description.setNatureIds(newNatures);
		ICommand[] prevBuilders = description.getBuildSpec();
		ICommand[] newBuilders = new ICommand[prevBuilders.length + 1];
		ICommand builder = description.newCommand();
		builder.setBuilderName(builderId());
		System.arraycopy(prevBuilders, 0, newBuilders, 1, prevBuilders.length);
		newBuilders[0] = builder;
		description.setBuildSpec(newBuilders);
		proj.setDescription(description, null);
	}

	private  static void removeNatureFromProject(IProject project, String natureId) throws CoreException {
		IProject proj = project.getProject(); // Needed if project is a IJavaProject
		IProjectDescription description = proj.getDescription();
		String[] prevNatures= description.getNatureIds();

		int natureIndex = -1;
		for (int i=0; i<prevNatures.length; i++) {
			if(prevNatures[i].equals(natureId)) {
				natureIndex	= i;
				i = prevNatures.length;
			}
		}

		// Remove nature only if it exists...
		if(natureIndex != -1) { 				
			String[] newNatures= new String[prevNatures.length - 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, natureIndex);
			System.arraycopy(prevNatures, natureIndex+1, newNatures, natureIndex, prevNatures.length - (natureIndex+1));
			description.setNatureIds(newNatures);
			proj.setDescription(description, null);
		}
	}

	
	public boolean performOk() {
		// store the value in the owner text field
		try {
			if (isJamonProjectCheckbox.getSelection()) {
				addNatureToProject(getJavaProject().getProject(), natureId());
			}
			else {
				removeNatureFromProject(getJavaProject().getProject(), natureId());
			}
		} catch(CoreException ex) {
			ex.printStackTrace();
			return false;
		}
		return true;
	}

}