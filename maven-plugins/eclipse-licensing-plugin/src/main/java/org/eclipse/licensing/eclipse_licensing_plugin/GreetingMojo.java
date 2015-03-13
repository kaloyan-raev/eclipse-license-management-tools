package org.eclipse.licensing.eclipse_licensing_plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.StringUtils;
import org.eclipse.tycho.ArtifactType;
import org.eclipse.tycho.core.utils.TychoProjectUtils;

/**
 * Says "Hi" to the user.
 *
 */
@Mojo( name = "sayhi")
public class GreetingMojo extends AbstractMojo
{
	@Parameter(property = "project.build.directory", required = true)
    protected File targetDirectory;
	
	@Parameter(property = "project.build.outputDirectory", required = true)
    protected File classesDirectory;
	
	@Parameter(property = "project", readonly = true)
    private MavenProject project;
	
	@Parameter(property = "session", readonly = true)
    private MavenSession session;
	
	@Parameter
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();
	
    public void execute() throws MojoExecutionException
    {
        getLog().info( "Hello, World." );
        getLog().info( "project: " + project.getBasedir().getAbsolutePath() );
        getLog().info( "target: " + targetDirectory.getAbsolutePath() );
        getLog().info( "classes: " + classesDirectory.getAbsolutePath() );

        try {
        	extractLicensingUtilsFiles();
        	
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
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void extractLicensingUtilsFiles() throws IOException {
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
    
}
