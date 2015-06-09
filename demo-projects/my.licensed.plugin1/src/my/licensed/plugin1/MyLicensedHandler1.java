/*******************************************************************************
 * Copyright (c) 2015 Kaloyan Raev.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Kaloyan Raev - initial API and implementation
 *******************************************************************************/
package my.licensed.plugin1;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.licensing.base.LicensingUtils;
import org.eclipse.ui.handlers.HandlerUtil;

public class MyLicensedHandler1 extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (LicensingUtils.hasValidLicenseKey(new MyLicensedProduct())) {
			MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
					"Licensed Command", "This is my first licensed command.");
		} else {
			MessageDialog.openError(HandlerUtil.getActiveShell(event),
					"Invalid License Key",
					"No valid license key to run my first licensed command.");
		}
		return null;
	}

}
