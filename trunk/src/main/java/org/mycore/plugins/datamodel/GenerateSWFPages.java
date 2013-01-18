/**
 * 
 */
package org.mycore.plugins.datamodel;

import java.io.File;
import java.io.FileFilter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.mojo.xml.TransformMojo;
import org.codehaus.plexus.components.io.filemappers.FileMapper;
import org.w3c.dom.Document;

/**
 * @author Thomas Scheffler
 * 
 * @goal swf-pages
 * @phase generate-resources
 */
public class GenerateSWFPages extends AbstractDatamodelMojo {

    /**
     * SWF steps
     * @parameter
     */
    private String[] steps = new String[] { "author", "commit" };

    /**
     * Layout definitions
     * @parameter
     */
    private LayoutDefinition[] layoutDefinitions = new LayoutDefinition[0];

    /**
     * Where to put generated webpages to.
     * @parameter expression="${project.build.outputDirectory}/web"
     */
    private File webDirectory;

    /**
     * Template with titles files.
     * @parameter expression="${basedir}/src/main/datamodel/swf/titles.xml"
     */
    private File titles;

    /**
     * Template with titles files.
     * @parameter expression="${basedir}/src/main/datamodel/swf/template.xml"
     */
    private File template;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (steps.length == 0)
            throw new MojoExecutionException("There are no workflow steps defined.");
        prepareOutputDirectory(getWebDirectory());
        try {
            Document styleDoc = getStylesheet("swf-page.xsl");
            File styleFile = new File(getWebDirectory(), this.getClass().getCanonicalName() + ".xsl");
            writeStylesheet(styleDoc, styleFile);

            List<String> objectTypes = getObjectTypes();
            if (objectTypes.isEmpty())
                throw new MojoExecutionException("Could not found any datamodel definitions in "
                        + getDataModelDirectory().getAbsolutePath());
            for (final String objectType : objectTypes) {
                LayoutDefinition layout = getLayout(objectType);
                for (final String step : steps) {
                    Properties parameters = getParameters(objectType, step);
                    if (layout.getLayouts().length == 0)
                        throw new MojoExecutionException("Could not get layout definition for object type " + objectType);
                    for (final String layParam : layout.getLayouts()) {
                        if (layParam != null)
                            parameters.put("layout", layParam);
                        getLog().info(MessageFormat.format("objectType: {0}, step: {1}, layout: {2}", objectType, step, layParam));
                        TransformMojo transformMojo = getTransformMojo(styleFile, getWebDirectory(), getTemplate(), new FileMapper() {
                            public String getMappedFileName(String fileName) {
                                StringBuilder finalName = new StringBuilder();
                                finalName.append("editor_form_");
                                finalName.append(step);
                                finalName.append("-");
                                finalName.append(objectType);
                                if (layParam != null) {
                                    finalName.append("-");
                                    finalName.append(layParam);
                                }
                                finalName.append(".xml");
                                return finalName.toString();
                            }
                        }, parameters);
                        transformMojo.execute();
                    }
                }
            }
            if (!styleFile.delete())
                throw new MojoExecutionException("Could not delete temporary file: " + styleFile.getAbsolutePath());
        } catch (Exception e) {
            if (e instanceof MojoExecutionException)
                throw (MojoExecutionException) e;
            throw new MojoExecutionException("Error while executing plugin", e);
        }
    }

    private List<String> getObjectTypes() {
        File dataModelDirectory = getDataModelDirectory();
        File[] dataModelFiles = dataModelDirectory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".xml");
            }
        });
        ArrayList<String> objectTypes = new ArrayList<String>(dataModelFiles.length);
        for (File dataModelFile : dataModelFiles) {
            String name = dataModelFile.getName();
            objectTypes.add(name.substring(0, name.length() - 4));
        }
        return objectTypes;
    }

    private Properties getParameters(String objectType, String step) {
        Properties props = new Properties();
        props.put("objectType", objectType);
        props.put("titleuri", getTitles().toURI().toString());
        props.put("step", step);
        return props;
    }

    private LayoutDefinition getLayout(String objectType) {
        for (LayoutDefinition layout : layoutDefinitions) {
            if (layout.getObjectType().equals(objectType))
                return layout;
        }
        LayoutDefinition layout = new LayoutDefinition();
        layout.setObjectType(objectType);
        layout.setLayouts(new String[] { null });
        return layout;
    }

    /**
     * @return the steps
     */
    protected final String[] getSteps() {
        return steps;
    }

    /**
     * @param steps the steps to set
     */
    protected final void setSteps(String[] steps) {
        this.steps = steps;
    }

    /**
     * @return the layouts
     */
    protected final LayoutDefinition[] getLayouts() {
        return layoutDefinitions;
    }

    /**
     * @param layouts the layouts to set
     */
    protected final void setLayouts(LayoutDefinition[] layouts) {
        this.layoutDefinitions = layouts;
    }

    /**
     * @return the webDirectory
     */
    protected final File getWebDirectory() {
        return webDirectory;
    }

    /**
     * @param webDirectory the webDirectory to set
     */
    protected final void setWebDirectory(File webDirectory) {
        this.webDirectory = webDirectory;
    }

    /**
     * @return the titles
     */
    protected final File getTitles() {
        return titles;
    }

    /**
     * @param titles the titles to set
     */
    protected final void setTitles(File titles) {
        this.titles = titles;
    }

    /**
     * @return the template
     */
    protected final File getTemplate() {
        return template;
    }

    /**
     * @param template the template to set
     */
    protected final void setTemplate(File template) {
        this.template = template;
    }

}
