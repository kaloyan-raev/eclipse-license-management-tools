package my.licensed.plugin1;
import java.util.UUID;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.licensing.LicenseUtils;
import org.eclipse.ui.handlers.HandlerUtil;

public class MyLicensedHandler1 extends AbstractHandler {
	
	private static final UUID PRODUCT_ID = UUID.fromString("cb4811fd-64a2-4e95-a758-ac9c716a6c31");

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (LicenseUtils.hasValidLicense(PRODUCT_ID)) {
			MessageDialog.openInformation(HandlerUtil.getActiveShell(event),
					"Licensed Command", "This is my first licensed command.");
		} else {
			MessageDialog.openError(HandlerUtil.getActiveShell(event),
					"Invalid License", "No valid license to run my first licensed command.");
		}
		return null;
	}

}
