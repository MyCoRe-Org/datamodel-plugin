package org.mycore.plugins.datamodel;

import java.io.File;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.mojo.xml.TransformMojo;
import org.codehaus.plexus.components.io.filemappers.FileMapper;

/**
 * Goal that generates XML Schema files from MyCoRe datamodel 2 files.
 * @author Thomas Scheffler (yagee)
 */
@Mojo(name = "schema", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public class GenerateSchema extends AbstractDatamodelMojo {
    private static final class SchemaFileMapper implements FileMapper {
        public String getMappedFileName(String fileName) {
            return "datamodel-" + fileName.substring(0, fileName.length() - 4) + ".xsd";
        }
    }

    /**
     * Location of the file.
     */
    @Parameter(required = true, defaultValue = "${project.build.outputDirectory}")
    private File schemaDirectory;

    public void execute() throws MojoExecutionException {
        prepareOutputDirectory(getSchemaDirectory());
        Properties p = new Properties();
        p.setProperty("plugin.groupId", getPlugin().getGroupId());
        p.setProperty("plugin.artifactId", getPlugin().getArtifactId());
        p.setProperty("plugin.version", getPlugin().getVersion());
        try {
            TransformMojo transformMojo = getTransformMojo(getTransformationSet("datamodel2schema.xsl",
                getSchemaDirectory(), getDataModelDirectory(), new SchemaFileMapper(), p));
            transformMojo.execute();
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
