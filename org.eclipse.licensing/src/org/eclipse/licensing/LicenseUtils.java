package org.eclipse.licensing;

import java.io.File;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.SystemUtils;

public class LicenseUtils {
	
	public static boolean hasValidLicenseKey(UUID productId, byte[] encodedPublicKey) {
		PublicKey publicKey;
		try {
			publicKey = readPublicKeyFromBytes(encodedPublicKey);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
			return false;
		}

		File[] licenseKeyFiles = getLicenseKeysFolder().listFiles();
		if (licenseKeyFiles != null) {
			for (File file : licenseKeyFiles) {
				if (file.isFile()) {
					LicenseKey licenseKey = new LicenseKey(file);
					if (productId.equals(licenseKey.getProductId()) && licenseKey.isAuthentic(publicKey)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static PublicKey readPublicKeyFromBytes(byte[] bytes) throws GeneralSecurityException {
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
		KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
		PublicKey key = keyFactory.generatePublic(keySpec);

		return key;
	}
	
	public static LicenseKey[] getLicenseKeys() {
		List<LicenseKey> result = new ArrayList<LicenseKey>();
		
		File[] licenseKeyFiles = getLicenseKeysFolder().listFiles();
		if (licenseKeyFiles != null) {
			for (File file : licenseKeyFiles) {
				if (file.isFile()) {
					LicenseKey licenseKey = new LicenseKey(file);
					if (licenseKey.getProductId() != null) {
						result.add(licenseKey);
					}
				}
			}
		}
	
		return result.toArray(new LicenseKey[result.size()]);
	}
	
	public static File getLicenseKeysFolder() { 
		return new File(SystemUtils.getUserHome(), ".eclipse/org.eclipse.licensing");
	}

}
