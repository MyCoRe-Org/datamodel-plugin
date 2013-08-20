/**
 * 
 */
package org.mycore.plugins.datamodel;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;

/**
 * Copies editor definitions from ${editorDirectory} to ${targetEditorDirectory}
 * @author Thomas Scheffler (yagee)
 */
@Mojo(name = "copy-editor", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
public class CopyEditorForms extends AbstractDatamodelMojo {

    /**
     * directory that contains editor definitions
     */
    @Parameter(defaultValue = "${basedir}/src/main/datamodel/editor")
    private File editorDirectory;

    /**
     * target directory for editor definitions
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}/META-INF/resources/editor")
    private File targetEditorDirectory;

    /* (non-Javadoc)
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    public void execute() throws MojoExecutionException, MojoFailureException {
        prepareOutputDirectory(getTargetEditorDirectory());
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(getEditorDirectory());
        ds.setIncludes(new String[] { "**/*.xml" });
        ds.scan();
        String[] includedFiles = ds.getIncludedFiles();
        getLog().info(MessageFormat.format("Copying {0} editor files.", includedFiles.length));
        for (String fileName : includedFiles) {
            File source = new File(getEditorDirectory(), fileName);
            if (!source.exists()) {
                throw new MojoExecutionException("File does not exist: " + source.getAbsolutePath());
            }
            File target = getTargetFile(source);
            try {
                if (copyFileIfNeeded(source, target)) {
                    if (getLog().isDebugEnabled())
                        getLog().debug(
                            MessageFormat.format("Copying {0} to {1}", source.getName(), target.getAbsolutePath()));
                } else {
                    if (getLog().isDebugEnabled())
                        getLog().debug(MessageFormat.format("Skipped copying {0}", source.getName()));
                }
            } catch (IOException e) {
                throw new MojoExecutionException(MessageFormat.format("Could not copy {0} to {1}!",
                    source.getAbsolutePath(), target.getAbsolutePath()), e);
            }
        }
    }

    private boolean copyFileIfNeeded(File source, File target) throws IOException {
        if (getLog().isDebugEnabled()) {
            getLog().debug(
                MessageFormat.format("Source timestamp: {0} target timestamp: {1}", source.lastModified(),
                    target.lastModified()));
        }
        if (target.lastModified() < source.lastModified() || target.length() != source.length()) {
            FileUtils.copyFile(source, target);
            target.setLastModified(source.lastModified());
            return true;
        }
        return false;
    }

    private File getTargetFile(File source) {
        return new File(getTargetEditorDirectory(), source.getName());
    }

    /**
     * @return the editorDirectory
     */
    protected final File getEditorDirectory() {
        return editorDirectory;
    }

    /**
     * @param editorDirectory the editorDirectory to set
     */
    protected final void setEditorDirectory(File editorDirectory) {
        this.editorDirectory = editorDirectory;
    }

    /**
     * @return the targetEditorDirectory
     */
    protected final File getTargetEditorDirectory() {
        return targetEditorDirectory;
    }

    /**
     * @param targetEditorDirectory the targetEditorDirectory to set
     */
    protected final void setTargetEditorDirectory(File targetEditorDirectory) {
        this.targetEditorDirectory = targetEditorDirectory;
    }

}
