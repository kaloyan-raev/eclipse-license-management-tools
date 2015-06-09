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
package org.eclipse.licensing.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class LicensedProducts {

	private static final String EXTENSION_POINT_ID = "org.eclipse.licensing.core.licensedProducts";

	public static ILicensedProduct getLicensedProduct(UUID productId) {
		IConfigurationElement[] config = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_ID);
		try {
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");
				if (o instanceof ILicensedProduct) {
					ILicensedProduct product = (ILicensedProduct) o;
					if (productId.equals(product.getId())) {
						return product;
					}
				}
			}
		} catch (CoreException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static ILicensedProduct[] getLicensedProducts() {
		List<ILicensedProduct> result = new ArrayList<ILicensedProduct>();

		IConfigurationElement[] config = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSION_POINT_ID);
		try {
			for (IConfigurationElement e : config) {
				final Object o = e.createExecutableExtension("class");
				if (o instanceof ILicensedProduct) {
					result.add((ILicensedProduct) o);
				}
			}
		} catch (CoreException ex) {
			ex.printStackTrace();
		}

		return result.toArray(new ILicensedProduct[result.size()]);
	}

}
