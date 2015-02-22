package org.eclipse.licensing;

import java.io.File;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.SystemUtils;

public class LicenseUtils {
	
	public static boolean hasValidLicense(UUID productId, byte[] encodedPublicKey) {
		PublicKey publicKey;
		try {
			publicKey = readPublicKeyFromBytes(encodedPublicKey);
		} catch (NoSuchAlgorithmException | NoSuchProviderException
				| InvalidKeySpecException e) {
			e.printStackTrace();
			return false;
		}

		File[] licenseFiles = getLicenseFolder().listFiles();
		if (licenseFiles != null) {
			for (File file : licenseFiles) {
				if (file.isFile()) {
					License license = new License(file);
					if (productId.equals(license.getProductId()) && license.isAuthentic(publicKey)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static PublicKey readPublicKeyFromBytes(byte[] bytes) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
		KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
		PublicKey key = keyFactory.generatePublic(keySpec);

		return key;
	}
	
	public static License[] getLicenses() {
		List<License> result = new ArrayList<License>();
		
		File[] licenseFiles = getLicenseFolder().listFiles();
		if (licenseFiles != null) {
			for (File file : licenseFiles) {
				if (file.isFile()) {
					License license = new License(file);
					if (license.getProductId() != null) {
						result.add(license);
					}
				}
			}
		}
	
		return result.toArray(new License[result.size()]);
	}
	
	public static File getLicenseFolder() { 
		return new File(SystemUtils.getUserHome(), ".eclipse/org.eclipse.licensing");
	}

}
