package com.peacetech.maven;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class VelocityTemplateMojo extends AbstractMojo {
  @Component private MavenProject project;

  @Parameter(property = "transformation", required = false)
  private Transformation[] transformations;

  @Parameter(property = "encoding", defaultValue = "${project.build.sourceEncoding}", required = false)
  private String encoding;

  private final VelocityEngine velocity = createVelocityEngine();

  @Override public void execute() throws MojoExecutionException {
    for (Transformation transformation : transformations) {
      VelocityContext ctx = createVelocityContext(transformation);
      for (Template template : transformation.getTemplates()) {
        evaluateTemplate(ctx, template);
      }
    }
  }

  protected void evaluateTemplate(VelocityContext ctx, Template template) throws MojoExecutionException {
    File outputDir = template.getOutputFile().getParentFile();
    if (!outputDir.isDirectory() && !outputDir.mkdirs()) {
      throw new MojoExecutionException("Error creating output directory: " + outputDir.getAbsolutePath());
    }
    try {
      getLog().debug("Loading template " + template.getTemplateFile());
      Reader reader = new InputStreamReader(new FileInputStream(template.getTemplateFile()), getCharset());
      try {
        Writer writer = new OutputStreamWriter(new FileOutputStream(template.getOutputFile()), getCharset());
        try {
          velocity.evaluate(ctx, writer, "velocity-maven-plugin", reader);
        } finally {
          writer.close();
        }
      } finally {
        reader.close();
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

  protected VelocityContext createVelocityContext(Transformation transformation) {
    VelocityContext ctx = new VelocityContext();
    ctx.put("project", project);
    ctx.put("system", System.getProperties());
    ctx.put("env", System.getenv());
    return new VelocityContext(transformation.getCombinedProperties(transformation.isSplitNestedProperties()), ctx);
  }

  @Override public String toString() {
    final StringBuilder sb = new StringBuilder("VelocityTemplateMojo{");
    sb.append("transformations=").append(Arrays.toString(transformations) + "\n" + project.getProperties());
    sb.append('}');
    return sb.toString();
  }
}
