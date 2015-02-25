package org.eclipse.licensing;

import java.util.UUID;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class LicenseProducts {

	private static final String EXTENSION_POINT_ID = "org.eclipse.licensing.licensedProducts";
	
	public static ILicensedProduct getLicensedProduct(UUID productId) {
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID);
		try {
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");
				if (o instanceof ILicensedProduct) {
					ILicensedProduct product = (ILicensedProduct) o;
					if (productId.equals(product.getProductId())) {
						return product;
					}
				}
			}
		} catch (CoreException ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
