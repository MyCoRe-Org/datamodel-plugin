/**
 * 
 */
package org.mycore.plugins.datamodel;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

/**
 * @author Thomas Scheffler (yagee)
 *
 */
public abstract class AbstractDatamodelMojo extends AbstractMojo {

    /**
     * Datamodel directory
     * @parameter expression="${basedir}/src/main/datamodel"
     */
    private File dataModelDirectory;
    /**
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * The system settings for Maven. This is the instance resulting from 
     * merging global- and user-level settings files.
     * 
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;

    /**
     * The base directory, relative to which directory names are
     * interpreted.
     *
     * @parameter expression="${basedir}"
     * @required
     * @readonly
     */
    private File basedir;

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

}
