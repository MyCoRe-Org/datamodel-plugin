/**
 * 
 */
package org.mycore.plugins.datamodel;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.mojo.xml.AbstractXmlMojo;
import org.codehaus.mojo.xml.TransformMojo;
import org.codehaus.mojo.xml.transformer.NameValuePair;
import org.codehaus.mojo.xml.transformer.TransformationSet;
import org.codehaus.plexus.components.io.filemappers.FileMapper;
import org.codehaus.plexus.resource.ResourceManager;

/**
 * @author Thomas Scheffler (yagee)
 *
 */
public abstract class AbstractDatamodelMojo extends AbstractMojo {

    /**
     * Datamodel directory
     */
    @Parameter(defaultValue = "${basedir}/src/main/datamodel/def")
    private File dataModelDirectory;

    @Parameter(readonly = true, required = true, defaultValue = "${project}")
    private MavenProject project;

    @Parameter(readonly = true, required = true, defaultValue = "${plugin}")
    private PluginDescriptor plugin;

    @Component
    private ResourceManager resourceManager;

    /**
     * The system settings for Maven. This is the instance resulting from 
     * merging global- and user-level settings files.
     */
    @Parameter(readonly = true, required = true, defaultValue = "${settings}")
    private Settings settings;

    /**
     * The base directory, relative to which directory names are
     * interpreted.
     */
    @Parameter(readonly = true, required = true, defaultValue = "${basedir}")
    private File basedir;

    /**
     * Validate input files
     */
    @Parameter(defaultValue = "true")
    private boolean validate = true;

    /**
     * @return the dataModelDirectory
     */
    protected final File getDataModelDirectory() {
        return dataModelDirectory;
    }

    protected final MavenProject getProject() throws MojoExecutionException {
        if (project == null) {
            throw new MojoExecutionException("\"project\" is not defined.");
        }
        return project;
    }

    protected final PluginDescriptor getPlugin() throws MojoExecutionException {
        if (plugin == null) {
            throw new MojoExecutionException("\"plugin\" is not defined.");
        }
        return plugin;
    }

    protected ResourceManager getResourceManager() throws MojoExecutionException {
        if (resourceManager == null) {
            throw new MojoExecutionException("ResourceManager component was not injected.");
        }
        return resourceManager;
    }

    /**
     * @return the settings
     */
    protected final Settings getSettings() {
        return settings;
    }

    protected final File getBasedir() throws MojoExecutionException {
        if (basedir == null) {
            throw new MojoExecutionException("\"basedir\" is not defined.");
        }
        return basedir;
    }

    /**
     * @param dataModelDirectory the dataModelDirectory to set
     */
    protected final void setDataModelDirectory(File dataModelDirectory) {
        this.dataModelDirectory = dataModelDirectory;
    }

    /**
     * @param project the project to set
     */
    protected final void setProject(MavenProject project) {
        this.project = project;
    }

    /**
     * @param plugin the plugin to set
     */
    protected final void setPlugin(PluginDescriptor plugin) {
        this.plugin = plugin;
    }

    /**
     * @param settings the settings to set
     */
    protected final void setSettings(Settings settings) {
        this.settings = settings;
    }

    /**
     * @param basedir the basedir to set
     */
    protected final void setBasedir(File basedir) {
        this.basedir = basedir;
    }

    protected TransformMojo getTransformMojo(TransformationSet... transformationSet) throws IllegalArgumentException,
        IllegalAccessException, MojoExecutionException, NoSuchFieldException, SecurityException {
        MavenProject currentProject = getProject();
        ResourceManager currentResourceManager = getResourceManager();
        File currentBaseDir = getBasedir();
        TransformMojo transformMojo = new TransformMojo();
        transformMojo.setLog(getLog());
        Field transformationSetsField = TransformMojo.class.getDeclaredField("transformationSets");
        transformationSetsField.setAccessible(true);
        transformationSetsField.set(transformMojo, transformationSet);
        Field projectField = AbstractXmlMojo.class.getDeclaredField("project");
        projectField.setAccessible(true);
        projectField.set(transformMojo, currentProject);
        Field locatorField = AbstractXmlMojo.class.getDeclaredField("locator");
        locatorField.setAccessible(true);
        locatorField.set(transformMojo, currentResourceManager);
        Field basedirField = AbstractXmlMojo.class.getDeclaredField("basedir");
        basedirField.setAccessible(true);
        basedirField.set(transformMojo, currentBaseDir);
        return transformMojo;

    }

    protected TransformationSet getTransformationSet(String stylesheet, File outputDirectory, File inputFileOrDir,
        FileMapper fm, Properties parameters) {
        TransformationSet transformationSet = new TransformationSet();
        if (inputFileOrDir.isFile()) {
            transformationSet.setDir(inputFileOrDir.getParentFile());
            transformationSet.setIncludes(new String[] { inputFileOrDir.getName() });
        } else {
            transformationSet.setDir(inputFileOrDir);
        }
        transformationSet.setOutputDir(outputDirectory);
        transformationSet.setStylesheet(stylesheet);
        transformationSet.setValidating(validate);
        transformationSet.setFileMappers(new FileMapper[] { fm });
        if (parameters != null) {
            @SuppressWarnings("unchecked")
            Entry<String, String>[] entries = parameters.entrySet().toArray(new Map.Entry[0]);
            NameValuePair[] p = new NameValuePair[entries.length];
            for (int i = 0; i < entries.length; i++) {
                p[i] = new NameValuePair();
                p[i].setName(entries[i].getKey());
                p[i].setValue(entries[i].getValue());
            }
            transformationSet.setParameters(p);
        }
        return transformationSet;
    }

    protected void prepareOutputDirectory(File f) throws MojoExecutionException {
        if (!f.exists()) {
            if (!f.mkdirs())
                throw new MojoExecutionException("Could not create directory" + f.getAbsolutePath());
        }
    }

}
