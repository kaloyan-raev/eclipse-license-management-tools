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
