package com.peacetech.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.PropertiesBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class VelocityTemplateMojo extends AbstractMojo {
  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  @Parameter(property = "properties", required = false)
  private Properties properties = new Properties();

  @Parameter(property = "transformation", required = false)
  private Transformation[] transformations;

  @Parameter(property = "encoding", defaultValue = "${project.build.sourceEncoding}", required = false)
  private String encoding;

  private final VelocityEngine velocity = createVelocityEngine();

  public void execute() throws MojoExecutionException {
    System.out.println("Global Properties: " + properties);
    for (Transformation transformation: transformations) {
      for (Template template: transformation.getTemplates()) {
        if (transformation.getPropertyFiles().length == 0) {
          VelocityContext ctx = createVelocityContext(transformation, properties, null, template.getProperties());
          evaluateTemplate(ctx, template, transformation, properties, null, template.getProperties());
        } else {
          for (File propertyFile: transformation.getPropertyFiles()) {
            VelocityContext ctx = createVelocityContext(transformation, properties, propertyFile, template.getProperties());
            evaluateTemplate(ctx, template, transformation, properties, propertyFile, template.getProperties());
          }
        }
      }
    }
  }

  private void evaluateTemplate(VelocityContext ctx, Template template, Transformation transformation,
                                Properties commonProperties, File propertyFile,
                                Properties templateProperties) throws MojoExecutionException {
    try {
      StringSearchInterpolator interpolator = new StringSearchInterpolator();
      Map<Object, Object> properties = transformation.getCombinedProperties(project, transformation.isSplitNestedProperties(),
                                                                            commonProperties, propertyFile, templateProperties);
      interpolator.addValueSource(new MapBasedValueSource(properties));
      getLog().debug("Processing  transformation  for property file " + propertyFile + " and outputFile " +
                     template.getOutputFile() + ", properties=" + properties);

      File outputFile = new File(interpolator.interpolate(template.getOutputFile()));

      File outputDir = outputFile.getParentFile();
      if (!outputDir.isDirectory() && !outputDir.mkdirs()) {
        throw new MojoExecutionException("Error creating output directory: " + outputDir.getAbsolutePath());
      }

      File templateFile = new File(interpolator.interpolate(template.getTemplateFile()));
      //todo implement template file pointing to a directory, require output be a directory as well
      if (!templateFile.exists()) {
        if (!template.isSkipIfAbsent()) {
          throw new MojoExecutionException("Template file " + templateFile + " could not be found");
        } else {
          return;
        }
      }
      if (template.isCopy()) {
        getLog().info("Copying " + templateFile + " to " + outputFile);
        Files.copy(templateFile.toPath(), outputFile.toPath(), REPLACE_EXISTING);
      } else {
        getLog().info("Loading template " + templateFile);
        Reader reader = new InputStreamReader(new FileInputStream(templateFile), getCharset());
        try {
          getLog().info("Writing " + outputFile);
          Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile), getCharset());
          try {
            velocity.evaluate(ctx, writer, "velocity-maven-plugin", reader);
          } finally {
            writer.close();
          }
        } finally {
          reader.close();
        }
      }
    } catch (Exception e) {
      throw new MojoExecutionException("Error processing template", e);
    }
  }

  private Charset getCharset() {
    if (encoding == null) {
      getLog().warn("Using default character set encoding");
      return Charset.defaultCharset();
    } else {
      return Charset.forName(encoding);
    }
  }

  protected VelocityEngine createVelocityEngine() {
    VelocityEngine engine = new VelocityEngine();
    engine.setProperty(VelocityEngine.RUNTIME_LOG_LOGSYSTEM, new VelocityMavenLogger(getLog()));
    engine.init();
    return engine;
  }

  protected VelocityContext createVelocityContext(Transformation transformation, Properties commonProperties,
                                                  File propertyFile, Properties templateProperties) throws MojoExecutionException {
    VelocityContext ctx = new VelocityContext();
    ctx.put("project", project);
    ctx.put("system", System.getProperties());
    ctx.put("env", System.getenv());
    Map<String, Object> util = new HashMap<String, Object>();
    util.put("REF", "$");
    ctx.put("velocityUtil", util);
    getLog().debug("getting combined properties");
    return new VelocityContext(transformation.getCombinedProperties(project, transformation.isSplitNestedProperties(), commonProperties,
                                                                    propertyFile, templateProperties), ctx);
  }

  @Override public String toString() {
    final StringBuilder sb = new StringBuilder("VelocityTemplateMojo{");
    sb.append("transformations=").append(Arrays.toString(transformations) + "\n" + project.getProperties());
    sb.append('}');
    return sb.toString();
  }

  public static void main(String[] args) throws InterpolationException {
    StringSearchInterpolator inter = new StringSearchInterpolator();
    inter.addValueSource(new PropertiesBasedValueSource(System.getProperties()));
    String p = "${java.home}";
    System.out.println(p + ": " + inter.interpolate(p));

  }
}
