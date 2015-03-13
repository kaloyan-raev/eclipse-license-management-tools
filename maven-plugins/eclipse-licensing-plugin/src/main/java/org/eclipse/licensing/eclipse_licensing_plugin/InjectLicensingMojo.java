package org.eclipse.licensing.eclipse_licensing_plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.StringUtils;
import org.eclipse.tycho.ArtifactType;
import org.eclipse.tycho.core.utils.TychoProjectUtils;

/**
 * Injects the licesing util classes into the licensed plugin.
 */
@Mojo( name = "inject-licensing")
public class InjectLicensingMojo extends AbstractMojo {
	
	@Parameter(property = "project.build.outputDirectory", required = true)
    protected File classesDirectory;
	
	@Parameter(property = "project", readonly = true)
    private MavenProject project;
	
    public void execute() throws MojoExecutionException {
        try {
        	extractLicensingUtilFiles();
        	updateManifest();
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
    }
    
    private void extractLicensingUtilFiles() throws IOException {
    	JarFile jar = new JarFile(getLicensingUtilsPlugin());
    	Enumeration<JarEntry> entries = jar.entries();
    	while (entries.hasMoreElements()) {
    		JarEntry entry = entries.nextElement();
    		String entryName = entry.getName();
    		if (entryName.startsWith("org")) {
    			File file = new File(classesDirectory, entryName);
    			if (entry.isDirectory()) {
    				file.mkdir();
    			} else {
    				InputStream is = jar.getInputStream(entry);
    				FileOutputStream fos = new FileOutputStream(file);
    				while (is.available() > 0) {
    					fos.write(is.read());
    				}
    				fos.close();
    				is.close();
    			}
    		}
    	}
    	jar.close();
    }
    
    private File getLicensingUtilsPlugin() {
    	return TychoProjectUtils.getDependencyArtifacts(project).getArtifact(ArtifactType.TYPE_ECLIPSE_PLUGIN, "org.eclipse.licensing", null).getLocation();
    }
    
    private void updateManifest() throws IOException {
    	File manifestFile = new File(project.getBasedir(), "META-INF/MANIFEST.MF");
        Manifest manifest = new Manifest(new FileInputStream(manifestFile));
        String[] requiredBundles = manifest.getMainAttributes().getValue("Require-Bundle").split(",");
        List<String> requiredList = new ArrayList<String>(Arrays.asList(requiredBundles));
        requiredList.remove("org.eclipse.licensing");
        requiredList.add("org.apache.commons.io");
        requiredList.add("org.apache.commons.codec");
        requiredList.add("org.apache.commons.lang3");
        String newRequiredBundles = StringUtils.join(requiredList.toArray(), ",");
        manifest.getMainAttributes().putValue("Require-Bundle", newRequiredBundles);
        manifest.write(new FileOutputStream(manifestFile));
    }
    
}
