# License Management Tools for Eclipse (Prototype)

This project is a prototype of a toolset for license management for the Eclipse Rich Client Platform. It can serve as a proof of concept and a base for discussion for having such a project under the Eclipse Foundation.

Such discussion is already happening in the [ide-dev@eclipse.org](https://dev.eclipse.org/mailman/listinfo/ide-dev) mailing list: https://dev.eclipse.org/mhonarc/lists/ide-dev/msg00690.html

## Overview

The project consists of several modules:

* Framework - a framework for license management, which provides a user interface for adding and removing license keys and can validate if there is a valid license key registered for installed licensed plugins.
* Maven Plugin - makes the licensed plugins harder to crack by injecting the critical parts of the license management framework in them during the build.
* Demo Project - a very simple plugin that demonstrates how to use the license management framework.
* Helper Tools - helper tools for signing license keys, generating and verifying keys and signatures, etc.

## Building

The provided tools can be easily built from the source code using Maven. All of the command snippets provided below assume that: Maven 3.x is installed, this git repository is cloned locally and a terminal is opened in the root of the cloned git repository.

### Building the framework

The following command will build the Eclipse plugins of the license management framework and will install them in the local Maven repository. This step is a prerequisite for building the demo project.

``` bash
mvn -f framework clean install
```

### Building the Maven plugin

The following command will build the Maven plugin and will install it in the local Maven repository. This step is a prerequisite for building the demo project.

``` bash
mvn -f maven-plugins/eclipse-licensing-plugin clean install
```

### Building the demo project

The following command will build the Eclipse plugins of the demo project.

``` bash
mvn -f demo-projects clean verify
```

Note that the Maven plugin will modify the META-INF/MANIFEST.MF file of the demo project's my.licensed.plugin1. You need to revert the changes to avoid compilation problems in the IDE or next time you are building the demo project:

``` bash
git checkout demo-projects/my.licensed.plugin1/META-INF/MANIFEST.MF
```

## Using the demo project

After successfully building the demo project, you can install it in any Eclipse IDE or Eclipse RCP product that allows installation of additional plugins.

1. Go to Help > Install New Software... from the main menu.
2. Click on the Add... button to add a new software repository.
3. Click on the Local... button.
4. Browse the software repository produced by the build for the framework: ./framework/org.eclipse.licensing.repository/target/repository/
5. Click the OK button to add the selected repository and close the Add Repository dialog.
6. Repeat steps 2-5 to add the software repository for the demo project: ./demo-projects/my.licensed.repository/target/repository/
7. Select the "My Licensed Feature 1" feature from the list and follow the usual steps for installing it.

The install manager will install the demo project and the license management framework as dependency.

Once the installation process completes and Eclipse is restarted a new menu will appear in the main menu.

![image](https://cloud.githubusercontent.com/assets/468091/8045640/6640a496-0e3d-11e5-882a-8e5ce54ac6ff.png)

The menu is contributed by the demo project for demonstration purposes.

If you invoke the Licensing > My Licensed Command 1 menu action, it will fail to run showing an error that there is no valid license key available for this command.

![image](https://cloud.githubusercontent.com/assets/468091/8045672/a0672d16-0e3d-11e5-9bc5-db5cbf5a2cc4.png)

This is a demonstration that the licensed plugin can verify if there is a valid license key registered, and if none is found that it can handle this state in an appropriate manner. Of course, the displayed feedback to the user can be implemented in more sophisticated way than just showing a simple error message - like displaying a wizard for obtaining a valid license key.

There is a valid license key for the demo project in the software repository: demo-projects/mylicense1.elf. You can register it in Eclipse using the following steps:

1. Go to Window > Preferences from the main menu.
2. Select the Licenses page.
3. Click on the Add License... button.
4. Select the license key file from the file system.
5. Click the OK button.

![image](https://cloud.githubusercontent.com/assets/468091/8045721/f5dc76d4-0e3d-11e5-8f13-ec55f5b329cb.png)

If you now invoke the Licensing > My Licensed Command 1 menu action again, you will see that it will display a message that it is executed successfully. The added license key is recognized as valid for the licensed plugin.

![image](https://cloud.githubusercontent.com/assets/468091/8045702/d2573ca8-0e3d-11e5-8d62-11078233cba6.png)

## Using the tools in your own Eclipse plugin

The best way to learn how to use the License Management Tools for your own Eclipse plugin is to examine the [demo project](demo-projects).

### Define the dependencies

In any Eclipse plugin where you want to check for a valid license key, you need to add the following plugins dependencies:

* org.eclipse.licensing.base
* org.eclipse.licensing.core

None of the above dependencies will add any UI elements to Eclipse. If you want to add the user interface for license management, e.g. the Licenses preference page, then it is recommended to import the org.eclipse.licensing feature in your product's feature:

``` xml
<requires>
   <import feature="org.eclipse.licensing" />
</requires>
```

This way the complete license management framework will be installed as dependency when your feature is installed.

### Implement ILicensedProduct

A key moment of implementing license management for your Eclipse plugin is to define a class implementing the [ILicensedProduct](framework/org.eclipse.licensing.core/src/org/eclipse/licensing/core/ILicensedProduct.java) interface. The term "licensed product" refers to the collection of one or more Eclipse features that are considered parts of a single software product distributed with a single license key.

A licensed product has the following attributes:

* ID - universally unique identifier (UUID) of the product. It is important to be _universally_ unique, because this is the ID that will be mapped to the registered license keys.
* Name - display name of the product. Can be any text string.
* Vendor - display name of the product's vendor. Can be any text string. 
* Version - product's version. Must be an OSGi-compatible version.
* Public Key - public key for verifying the signature of the license keys. It's a [java.security.PublicKey](https://docs.oracle.com/javase/7/docs/api/java/security/PublicKey.html) object. Once specified, it should not be changed in future versions of the product.

#### Generating an UUID

There are many ways to generate an UUID. Perhaps, it is easiest to use one of the online generators like: https://www.uuidgenerator.net/

The string representation of the generated UUID can be converted to a [java.util.UUID](https://docs.oracle.com/javase/7/docs/api/java/util/UUID.html) object using the following code snippet:

``` java
UUID.fromString("cb4811fd-64a2-4e95-a758-ac9c716a6c31")
```

#### Generating a public and private key pair

One easy way to generate a DSA key pair is using the provided [GenKeys](signature/src/signature/GenKeys.java) helper tool.

Then you need to create a [java.security.PublicKey](https://docs.oracle.com/javase/7/docs/api/java/security/PublicKey.html) object from the public key file. One way is to include the public key file in your plugin and use the [Java Security API](https://docs.oracle.com/javase/7/docs/technotes/guides/security/overview/jsoverview.html) to create the PublicKey object directly from the file. However, this is not recommended, because it is quite easy for malicious users to replace the public key file with their own and crack the license validation.

Therefore, it is recommended to embed the pubic key as byte array in the plugin's source code. While total protection from cracking is impossible, this way it is significantly harder for malicious users to replace the public key. They would need to decompile the source code, modify it and compile it again. Additional protection can be achieved by using a code obfuscator (not covered here).

To embed the public key in the source code, you need to first convert the content of the public key file into an array of decimal bytes. The [hexdump](http://linux.die.net/man/1/hexdump) command can be used to achieve this:

``` bash
hexdump mydsa.pub -v -e '1/1 "%d" ", "'
```

The output of the command can be used in the source code like this:

``` java
public static byte[] PUBLIC_KEY = { <hexdump_output> };
```

The the [LicensingUtils](framework/org.eclipse.licensing.base/src/org/eclipse/licensing/base/LicensingUtils.java) helper class can be used to convert the byte array to a PublicKey object:

``` java
PublicKey publicKey = LicensingUtils.readPublicKeyFromBytes(PUBLIC_KEY);
```

### Register the ILicensedProduct implementation

The implementation of ILicensedProduct must be registered in the license management framework using the [org.eclipse.licensing.core.licensedProducts](framework/org.eclipse.licensing.core/schema/org.eclipse.licensing.core.licensedProducts.exsd) extensions point.

Add the following snippet in the plugin.xml of your Eclipse plugin:

``` xml
<extension
      point="org.eclipse.licensing.core.licensedProducts">
   <product
         class="my.licensed.plugin1.MyLicensedProduct"
         id="cb4811fd-64a2-4e95-a758-ac9c716a6c31">
   </product>
</extension>
```

The values of the "id" and "class" attributes must be replaced with the product UUID and ILicensedProduct implementation class of your licensed product.

### Check if there is a valid license key registered

Checking if a valid license key is registered is as easy as using an utility method and passing the ILicensedProduct implementation.

``` java
if (LicensingUtils.hasValidLicenseKey(new MyLicensedProduct())) {
	// execute the license-protected logic
} else {
	// handle the invalid license key
}
```

The utility method will check all available license keys if they match your licensed product, it will verify various license constraints like version range, expiration date, etc. If a matching license key is found, the utility method will check if it is authentic by using the product's public key to verify its signature.

### Use the eclipse-licensing-plugin Maven plugin in the build

The use of this Maven plugin is optional, but highly recommended for improving the robustness of the solution against cracking. Due to the extensible nature of the Eclipse Platform and the fact that the license management framework itself is open source, it is very easy for malicious users to create a feature patch that will replace the complete license management framework with their own implementation. Such implementation can just always return "true" in the [LicensingUtils.hasValidLicenseKey()](framework/org.eclipse.licensing.base/src/org/eclipse/licensing/base/LicensingUtils.java#L25) method, effectively cracking the license validation. Such feature patch can be published and easily used by anyone who wants to get an easy free-ride of any Eclipse plugin using this licensing management framework.

In order to avoid the above scenario, all of the critical code responsible for license validation is kept in a separate plugin: org.eclipse.licensing.base. The content of this plugin can be easily copied in every licensed Eclipse plugin by using the eclipse-licensing-plugin Maven plugin. This way if malicious users want to crack the LicensingUtils.hasValidLicenseKey() method, they will need to do it in every Eclipse plugin that makes checks for license validation. This is far more complex task than installing a feature patch and can be additionally protected by using code obfuscators.

Using the Maven plugin is as simple as adding the following snippet in the plugin's pom.xml:

``` xml
<build>
  <plugins>
    <plugin>
      <groupId>org.eclipse.licensing</groupId>
      <artifactId>eclipse-licensing-plugin</artifactId>
      <version>1.0-SNAPSHOT</version>
      <executions>
        <execution>
          <phase>compile</phase>
          <goals>
            <goal>inject-licensing</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

### Create license key files

At this point your Eclipse plugin should be well-protected from unauthorized usage without a valid license key. Now we need to create valid license key files that can be used either for testing, or to be distributed to eligible users to use the Eclipse plugin. 

The license key is a Java Properties file with the following properties:

* Id - A decimal number indentifier of the license key. It is supposed to be unique per issuer.
* Issuer - Who issued this license file. It may be the product vendor itself, or it may be a 3rd party license issuer.
* Type - The license type: Evaluation|Commercial|Subscription|Perpetual|Custom.
* ExpirationDate - A date in YYYY-MM-DD format.
* ConcurrentUsers - The number of concurrent users using the same license key at the same time.
* ProductId - The product UUID. It must be universally unique.
* ProductName - The product display name.
* ProductVendor - The product vendor name.
* ProductVersions - The versions of the product, which this license key is valid for. It must be a version range like [1.0,2.0).
* CustomerId - A decimal number identifier for the customer. It is supposed to be unique per issuer.
* CustomerName - The customer display name.
* Signature - Base64-encoded String representation of the DSA signature of the license key file content. Before calculating the signature, the content is first normalized by sorting all properties and removing the Signature property if one already exists.

It is allowed to have product-specific properties.

Before distributing the license key to a user, it must be signed with the private key for the licensed product. This can be done by using the [SignLicense](signature/src/signature/SignLicense.java) helper tool.
