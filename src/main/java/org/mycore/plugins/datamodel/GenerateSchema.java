package org.mycore.plugins.datamodel;

import java.io.File;
import java.lang.reflect.Field;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.xml.AbstractXmlMojo;
import org.codehaus.mojo.xml.TransformMojo;
import org.codehaus.mojo.xml.transformer.TransformationSet;
import org.codehaus.plexus.components.io.filemappers.FileMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Goal that generates XML Schema files from MyCoRe datamodel 2 files.
 *
 * @goal schema
 * 
 * @phase generate-resources
 */
public class GenerateSchema extends AbstractDatamodelMojo {
    private String XSL_URI = "http://www.w3.org/1999/XSL/Transform";

    /**
     * Location of the file.
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File outputDirectory;

    /**
     * Validate input files
     * @parameter expression="true";
     */
    private boolean validate;

    public void execute() throws MojoExecutionException {
        File f = outputDirectory;

        if (!f.exists()) {
            f.mkdirs();
        }
        try {
            Document styleDoc = getStylesheet("datamodel2schema.xsl");
            File styleFile = writeStylesheet(styleDoc);
            TransformMojo transformMojo = getTransformMojo(styleFile);
            transformMojo.execute();
            styleFile.delete();
        } catch (Exception e) {
            if (e instanceof MojoExecutionException)
                throw (MojoExecutionException) e;
            throw new MojoExecutionException("Error while executing plugin", e);
        }
    }

    private TransformMojo getTransformMojo(File styleFile) throws NoSuchFieldException, IllegalAccessException {
        TransformationSet transformationSet = new TransformationSet();
        transformationSet.setDir(getDataModelDirectory());
        transformationSet.setOutputDir(outputDirectory);
        transformationSet.setStylesheet(styleFile);
        transformationSet.setValidating(validate);
        FileMapper fm = new FileMapper() {
            public String getMappedFileName(String fileName) {
                return "datamodel-" + fileName.substring(0, fileName.length()-4) + ".xsd";
            }
        };
        transformationSet.setFileMappers(new FileMapper[] { fm });
        TransformMojo transformMojo = new TransformMojo();
        transformMojo.setLog(getLog());
        Field transformationSets = TransformMojo.class.getDeclaredField("transformationSets");
        transformationSets.setAccessible(true);
        transformationSets.set(transformMojo, new TransformationSet[] { transformationSet });
        Field project = AbstractXmlMojo.class.getDeclaredField("project");
        project.setAccessible(true);
        getLog().info("Project: " + getProject());
        project.set(transformMojo, getProject());
        return transformMojo;
    }

    private File writeStylesheet(Document styleDoc) throws TransformerException {
        DOMSource domSource = new DOMSource(styleDoc);
        File styleFile = new File(outputDirectory, this.getClass().getCanonicalName() + ".xsl");
        StreamResult streamResult = new StreamResult(styleFile);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer serializer = tf.newTransformer();
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        serializer.transform(domSource, streamResult);
        return styleFile;
    }

    private Document getStylesheet(String resourceFile) throws ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = doc.createElementNS(XSL_URI, "stylesheet");
        doc.appendChild(root);
        Element include = doc.createElementNS(XSL_URI, "include");
        include.setAttribute("href", resourceFile);
        root.appendChild(include);
        return doc;
    }
}
