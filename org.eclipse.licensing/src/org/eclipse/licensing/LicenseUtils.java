package org.eclipse.licensing;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class LicenseUtils {
	
	public static boolean hasValidLicense(UUID productId) {
		File licensesFolder = new File("/home/raev/Work/Licensing");
		for (File file : licensesFolder.listFiles()) {
			if (isLicenseFile(file) && productId.equals(getLicenseProductId(file))) {
				return true;
			}
		}
		return false;
	}

	private static boolean isLicenseFile(File file) {
		return file != null && file.isFile() && file.getName().endsWith(".license");
	}

	private static UUID getLicenseProductId(File file) {
		if (file == null)
			return null;
		
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(file));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		try {
			return UUID.fromString(props.getProperty("ProductId"));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
