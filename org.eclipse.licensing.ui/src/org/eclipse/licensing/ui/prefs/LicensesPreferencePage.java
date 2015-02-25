package org.eclipse.licensing.ui.prefs;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.licensing.ILicensedProduct;
import org.eclipse.licensing.LicenseKey;
import org.eclipse.licensing.LicensedProducts;
import org.eclipse.licensing.LicensingUtils;
import org.eclipse.licensing.ui.LicensingUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.framework.Bundle;

public class LicensesPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {
	
	private TreeViewer tree;
	private Button removeButton;

	@Override
	public void init(IWorkbench workbench) {
		noDefaultAndApplyButton();
	}

	@Override
	public String getDescription() {
		return "&Installed products and license keys:";
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		composite.setLayout(layout);
		
		createTable(composite);
		createButtons(composite);
		
		return composite;
	}
	
	private void createTable(Composite parent) {
		tree = new TreeViewer(parent, SWT.SINGLE);
		tree.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tree.setContentProvider(new ITreeContentProvider() {
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
			
			@Override
			public void dispose() {
			}
			
			@Override
			public Object[] getElements(Object inputElement) {
				return (ILicensedProduct[]) inputElement;
			}
			
			@Override
			public Object[] getChildren(Object parentElement) {
				ILicensedProduct product = (ILicensedProduct) parentElement;
				LicenseKey[] licenseKeys = LicensingUtils.getLicenseKeys();
				
				List<LicenseKey> children = new ArrayList<LicenseKey>();
				for (LicenseKey licenseKey : licenseKeys) {
					if (product.getProductId().equals(licenseKey.getProductId())) {
						children.add(licenseKey);
					}
				}
				
				return children.toArray();
			}
			
			@Override
			public Object getParent(Object element) {
				if (element instanceof LicenseKey) {
					LicenseKey licenseKey = (LicenseKey) element;
					return LicensedProducts.getLicensedProduct(licenseKey.getProductId());
				}
				return null;
			}
			
			@Override
			public boolean hasChildren(Object element) {
				return element instanceof ILicensedProduct;
			}
		});
		tree.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ILicensedProduct) {
					ILicensedProduct product = (ILicensedProduct) element;
					return product.getProductName();
				}
				
				if (element instanceof LicenseKey) {
					LicenseKey licenseKey = (LicenseKey) element;
					return licenseKey.getProductId().toString();
				}
				
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				String file = "product.png";
				if (element instanceof LicenseKey) {
					file = "key.png";
				}
				
				Bundle bundle = Platform.getBundle(LicensingUI.PLUGIN_ID);
				IPath path = new Path("icons/" + file);
				URL url = FileLocator.find(bundle, path, null);
				ImageDescriptor desc = ImageDescriptor.createFromURL(url);
				return desc.createImage();
			}
		});
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object element = ((IStructuredSelection) event.getSelection()).getFirstElement();
				boolean enable = element != null && element instanceof LicenseKey;
				removeButton.setEnabled(enable);
			}
		});
		refreshTable();
	}
	
	private void refreshTable() {
		tree.setInput(LicensedProducts.getLicensedProducts());
		tree.expandAll();
	}
	
	private void createButtons(final Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		
		Button addButton = new Button(composite, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
		addButton.setText("&Add License...");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(parent.getShell());
				dialog.setText("Select License Key File");
				String filePath = dialog.open();
				
				// check if license file is valid
				LicenseKey licenseKey = new LicenseKey(filePath);
				ILicensedProduct product = LicensedProducts.getLicensedProduct(licenseKey.getProductId());
				if (product == null) {
					MessageDialog.openError(getShell(), "Error", "No product found for the selected license!");
					return;
				}
				
				if (!licenseKey.isAuthentic(product.getPublicKey())) {
					MessageDialog.openError(getShell(), "Error", "Invalid license key selected!");
					return;
				}

				java.nio.file.Path source = Paths.get(filePath);
				java.nio.file.Path licenseFolder = Paths.get(LicensingUtils.getLicenseKeysFolder().getAbsolutePath());
				java.nio.file.Path target = licenseFolder.resolve(source.getFileName());
				
				try {
					Files.createDirectories(licenseFolder);
					Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				refreshTable();
			}
		});
		
		removeButton = new Button(composite, SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
		removeButton.setText("&Remove");
		removeButton.setEnabled(false);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = tree.getSelection();
				if (selection.isEmpty())
					return;
				
				if (!(selection instanceof IStructuredSelection))
					return;
				
				IStructuredSelection ssel = (IStructuredSelection) selection;
				Object element = ssel.getFirstElement();
				if (element == null)
					return;
				
				LicenseKey licenseKey = (LicenseKey) element;
				try {
					Files.delete(Paths.get(licenseKey.getFile().getAbsolutePath()));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				refreshTable();
			}
		});
		
		Button detailsButton = new Button(composite, SWT.PUSH);
		detailsButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
		detailsButton.setText("&Details...");
	}

}
