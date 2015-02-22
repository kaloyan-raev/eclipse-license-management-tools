package org.eclipse.licensing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

public class License {
	
	public final static String PRODUCT_ID = "ProductId";
	public final static String PRODUCT_NAME = "ProductName";
	public final static String CUSTOMER_NAME = "CustomerName";
	
	/**
	 * Base64-encoded string representation of the license signature.
	 */
	public final static String SIGNATURE = "Signature";
	
	private Properties properties;
	
	public License(Properties properties) {
		this.properties = properties;
	}
	
	public License(File file) {
		properties = new Properties();
		try {
			properties.load(new FileInputStream(file));
		} catch (IOException e) {
			// TODO set in invalid status
			e.printStackTrace();
		}
	}
	
	public License(String fileName) {
		this(new File(fileName));
	}
	
	public String getProperty(String key) {
		return properties.getProperty(key);
	}
	
	public UUID getProductId() {
		String productId = getProperty(PRODUCT_ID);
		return (productId == null) ? null : UUID.fromString(productId);
	}
	
	public String getProductName() {
		return getProperty(PRODUCT_NAME);
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
	
	public boolean verifySignature(PublicKey publicKey) {
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
		} catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | SignatureException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
	}

}
