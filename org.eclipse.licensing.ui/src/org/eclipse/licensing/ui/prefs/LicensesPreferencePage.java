package org.eclipse.licensing.ui.prefs;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.licensing.ILicensedProduct;
import org.eclipse.licensing.LicenseKey;
import org.eclipse.licensing.LicenseProducts;
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
	
	private TableViewer table;

	@Override
	public void init(IWorkbench workbench) {
		noDefaultAndApplyButton();
	}

	@Override
	public String getDescription() {
		return "Installed &license keys:";
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
		table = new TableViewer(parent);
		table.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.setContentProvider(ArrayContentProvider.getInstance());
		table.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof LicenseKey) {
					LicenseKey licenseKey = (LicenseKey) element;
					return licenseKey.getProductName();
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				Bundle bundle = Platform.getBundle(LicensingUI.PLUGIN_ID);
				IPath path = new Path("icons/key.gif");
				URL url = FileLocator.find(bundle, path, null);
				ImageDescriptor desc = ImageDescriptor.createFromURL(url);
				return desc.createImage();
			}
		});
		refreshTable();
	}
	
	private void refreshTable() {
		table.setInput(LicensingUtils.getLicenseKeys());
	}
	
	private void createButtons(final Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		
		Button addButton = new Button(composite, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
		addButton.setText("&Add...");
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(parent.getShell());
				dialog.setText("Select License Key File");
				String filePath = dialog.open();
				
				// check if license file is valid
				LicenseKey licenseKey = new LicenseKey(filePath);
				ILicensedProduct product = LicenseProducts.getLicensedProduct(licenseKey.getProductId());
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
		
		Button removeButton = new Button(composite, SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
		removeButton.setText("&Remove");
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = table.getSelection();
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
