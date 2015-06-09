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
package org.eclipse.licensing.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.licensing.core.ILicensedProduct;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;

public class LicenseKey {

	public final static String ID = "Id";
	public final static String ISSUER = "Issuer";
	public final static String TYPE = "Type";
	public final static String EXPIRATION_DATE = "ExpirationDate";
	public final static String PRODUCT_ID = "ProductId";
	public final static String PRODUCT_NAME = "ProductName";
	public final static String PRODUCT_VENDOR = "ProductVendor";
	public final static String PRODUCT_VERSIONS = "ProductVersions";
	public final static String CUSTOMER_ID = "CustomerId";
	public final static String CUSTOMER_NAME = "CustomerName";

	/**
	 * Base64-encoded string representation of the license key signature.
	 */
	public final static String SIGNATURE = "Signature";

	private Properties properties;
	private File file;

	public LicenseKey(Properties properties) {
		this.properties = properties;
	}

	public LicenseKey(File file) {
		this.file = file;
		properties = new Properties();
		try {
			properties.load(new FileInputStream(file));
		} catch (IOException e) {
			// TODO set in invalid status
			e.printStackTrace();
		}
	}

	public LicenseKey(String fileName) {
		this(new File(fileName));
	}

	public File getFile() {
		return file;
	}

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

	public String getId() {
		return getProperty(ID);
	}

	public String getIssuer() {
		return getProperty(ISSUER);
	}

	public String getType() {
		return getProperty(TYPE);
	}

	public String getExpirationDate() {
		return getProperty(EXPIRATION_DATE);
	}

	public UUID getProductId() {
		String productId = getProperty(PRODUCT_ID);
		return (productId == null) ? null : UUID.fromString(productId);
	}

	public String getProductName() {
		return getProperty(PRODUCT_NAME);
	}

	public String getProductVendor() {
		return getProperty(PRODUCT_VENDOR);
	}

	public VersionRange getProductVersions() {
		String versions = getProperty(PRODUCT_VERSIONS);
		return (versions == null) ? null : VersionRange.valueOf(versions);
	}

	public String getCustomerId() {
		return getProperty(CUSTOMER_ID);
	}

	public String getCustomerName() {
		return getProperty(CUSTOMER_NAME);
	}

	public String getSignatureAsString() {
		return getProperty(SIGNATURE);
	}

	public byte[] getSignature() {
		return Base64.decodeBase64(getSignatureAsString());
	}

	public boolean isValidFor(ILicensedProduct product) {
		return (product.getId().equals(getProductId())
				&& isAuthentic(product.getPublicKey()) && matchesProductVersion(product
					.getVersion()));
	}

	public boolean isAuthentic(PublicKey publicKey) {
		try {
			Signature signature = Signature.getInstance("SHA1withDSA", "SUN");
			signature.initVerify(publicKey);

			String[] propKeys = properties.keySet().toArray(new String[0]);
			Arrays.sort(propKeys);
			for (String propKey : propKeys) {
				if (!SIGNATURE.equals(propKey)) {
					String propValue = getProperty(propKey);
					signature.update(propValue.getBytes("UTF-8"));
				}
			}

			byte[] encodedSignature = getSignature();
			if (encodedSignature == null) {
				return false;
			}

			return signature.verify(getSignature());
		} catch (GeneralSecurityException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean matchesProductVersion(String version) {
		VersionRange versions = getProductVersions();
		return (versions == null) ? true : versions
				.includes(new Version(version));
	}

}
