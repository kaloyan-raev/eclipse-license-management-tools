package my.licensed.plugin1;

import static my.licensed.plugin1.MyLicensedProduct.PRODUCT_ID;
import static my.licensed.plugin1.MyLicensedProduct.PUBLIC_KEY;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.licensing.LicenseUtils;
import org.eclipse.ui.handlers.HandlerUtil;

public class MyLicensedHandler1 extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (LicenseUtils.hasValidLicense(PRODUCT_ID, PUBLIC_KEY)) {
			MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
					"Licensed Command", "This is my first licensed command.");
		} else {
			MessageDialog.openError(HandlerUtil.getActiveShell(event),
					"Invalid License", "No valid license to run my first licensed command.");
		}
		return null;
	}

}
