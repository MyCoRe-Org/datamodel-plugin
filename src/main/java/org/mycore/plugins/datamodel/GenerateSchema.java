package org.mycore.plugins.datamodel;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.mojo.xml.TransformMojo;
import org.codehaus.plexus.components.io.filemappers.FileMapper;
import org.w3c.dom.Document;

/**
 * Goal that generates XML Schema files from MyCoRe datamodel 2 files.
 * @author Thomas Scheffler (yagee)
 */
@Mojo(name = "schema", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GenerateSchema extends AbstractDatamodelMojo {
    /**
     * Location of the file.
     */
    @Parameter(required = true, defaultValue = "${project.build.outputDirectory}")
    private File schemaDirectory;

    public void execute() throws MojoExecutionException {
        prepareOutputDirectory(getSchemaDirectory());
        try {
            Document styleDoc = getStylesheet("datamodel2schema.xsl");
            File styleFile = new File(schemaDirectory, this.getClass().getCanonicalName() + ".xsl");
            writeStylesheet(styleDoc, styleFile);
            TransformMojo transformMojo = getTransformMojo(styleFile, getSchemaDirectory(), getDataModelDirectory(), new FileMapper() {
                public String getMappedFileName(String fileName) {
                    return "datamodel-" + fileName.substring(0, fileName.length() - 4) + ".xsd";
                }
            }, null);
            transformMojo.execute();
            if (!styleFile.delete())
                throw new MojoExecutionException("Could not delete temporary file: " + styleFile.getAbsolutePath());
        } catch (Exception e) {
            if (e instanceof MojoExecutionException)
                throw (MojoExecutionException) e;
            throw new MojoExecutionException("Error while executing plugin", e);
        }
    }

    /**
     * @return the outputDirectory
     */
    protected final File getSchemaDirectory() {
        return schemaDirectory;
    }

    /**
     * @param schemaDirectory the outputDirectory to set
     */
    protected final void setSchemaDirectory(File schemaDirectory) {
        this.schemaDirectory = schemaDirectory;
    }
}
