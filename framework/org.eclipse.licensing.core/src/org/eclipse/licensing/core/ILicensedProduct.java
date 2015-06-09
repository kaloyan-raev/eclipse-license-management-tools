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

import java.security.PublicKey;
import java.util.UUID;

public interface ILicensedProduct {

	public UUID getId();

	public String getName();

	public String getVendor();

	public String getVersion();

	public PublicKey getPublicKey();

}
