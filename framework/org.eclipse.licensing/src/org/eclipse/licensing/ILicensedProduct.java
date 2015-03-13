package org.eclipse.licensing;

import java.security.PublicKey;
import java.util.UUID;

public interface ILicensedProduct {
	
	public UUID getProductId();
	
	public String getProductName();
	
	public PublicKey getPublicKey();

}
