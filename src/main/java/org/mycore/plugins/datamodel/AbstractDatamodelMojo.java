/**
 * 
 */
package org.mycore.plugins.datamodel;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.codehaus.mojo.xml.AbstractXmlMojo;
import org.codehaus.mojo.xml.TransformMojo;
import org.codehaus.mojo.xml.transformer.NameValuePair;
import org.codehaus.mojo.xml.transformer.TransformationSet;
import org.codehaus.plexus.components.io.filemappers.FileMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.apache.maven.plugins.annotations.Parameter;

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

    private String XSL_URI = "http://www.w3.org/1999/XSL/Transform";

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

    /**
     * @return the project
     */
    protected final MavenProject getProject() {
        return project;
    }

    /**
     * @return the settings
     */
    protected final Settings getSettings() {
        return settings;
    }

    /**
     * @return the basedir
     */
    protected final File getBasedir() {
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

    protected TransformMojo getTransformMojo(File styleFile, File outputDirectory, File inputFileOrDir, FileMapper fm,
        Properties parameters) throws NoSuchFieldException, IllegalAccessException {
        TransformationSet transformationSet = new TransformationSet();
        if (inputFileOrDir.isFile()) {
            transformationSet.setDir(inputFileOrDir.getParentFile());
            transformationSet.setIncludes(new String[] { inputFileOrDir.getName() });
        } else {
            transformationSet.setDir(inputFileOrDir);
        }
        transformationSet.setOutputDir(outputDirectory);
        transformationSet.setStylesheet(styleFile.getAbsolutePath());
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
        TransformMojo transformMojo = new TransformMojo();
        transformMojo.setLog(getLog());
        Field transformationSets = TransformMojo.class.getDeclaredField("transformationSets");
        transformationSets.setAccessible(true);
        transformationSets.set(transformMojo, new TransformationSet[] { transformationSet });
        Field project = AbstractXmlMojo.class.getDeclaredField("project");
        project.setAccessible(true);
        project.set(transformMojo, getProject());
        return transformMojo;
    }

    protected void writeStylesheet(Document styleDoc, File styleFile) throws TransformerException {
        DOMSource domSource = new DOMSource(styleDoc);
        StreamResult streamResult = new StreamResult(styleFile);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.transform(domSource, streamResult);
    }

    protected Document getStylesheet(String resourceFile) throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElementNS(XSL_URI, "stylesheet");
        doc.appendChild(root);
        Element include = doc.createElementNS(XSL_URI, "include");
        include.setAttribute("href", resourceFile);
        root.appendChild(include);
        return doc;
    }

    protected void prepareOutputDirectory(File f) throws MojoExecutionException {
        if (!f.exists()) {
            if (!f.mkdirs())
                throw new MojoExecutionException("Could not create directory" + f.getAbsolutePath());
        }
    }

}
