package com.peacetech.maven;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.ObjectBasedValueSource;
import org.codehaus.plexus.interpolation.PropertiesBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Transformation {

  private Template[] templates;
  private Properties properties;
  private File[] propertyFiles;
  private boolean splitNestedProperties;
  private boolean splitProjectNestedProperties;
  private String propertiesName;
  private String exposeProjectProperties = "no";
  private String projectPropertiesName = "projectProperties";

  public Template[] getTemplates() {
    return templates;
  }

  public void setTemplates(Template[] templates) {
    this.templates = templates;
  }

  public Properties getProperties() {
    return properties;
  }

  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  public File[] getPropertyFiles() {
    return propertyFiles;
  }

  public void setPropertyFiles(File[] propertyFiles) {
    this.propertyFiles = propertyFiles;
  }

  public boolean isSplitNestedProperties() {
    return splitNestedProperties;
  }

  public void setSplitNestedProperties(boolean splitNestedProperties) {
    this.splitNestedProperties = splitNestedProperties;
  }

  public boolean isSplitProjectNestedProperties() {
    return splitProjectNestedProperties;
  }

  public void setSplitProjectNestedProperties(boolean splitProjectNestedProperties) {
    this.splitProjectNestedProperties = splitProjectNestedProperties;
  }

  public String getPropertiesName() {
    return propertiesName;
  }

  public void setPropertiesName(String propertiesName) {
    this.propertiesName = propertiesName;
  }

  public String getExposeProjectProperties() {
    return exposeProjectProperties;
  }

  public void setExposeProjectProperties(String exposeProjectProperties) throws MojoExecutionException {
    if (exposeProjectProperties != null && !"no".equals(exposeProjectProperties) &&
        !"property".equals(exposeProjectProperties) && !"merge".equals(exposeProjectProperties)) {
      throw new MojoExecutionException("Transformation.exposeProjectProperties valid values are: 'no', 'property', 'merge'");
    }
    this.exposeProjectProperties = exposeProjectProperties == null ? "no" : exposeProjectProperties;
  }

  public String getProjectPropertiesName() {
    return projectPropertiesName;
  }

  public void setProjectPropertiesName(String projectPropertiesName) {
    this.projectPropertiesName = projectPropertiesName;
  }

  //todo properties and propertyFiles values must be resolved in case they have variables
  public Map<Object, Object> getCombinedProperties(MavenProject project, boolean splitNestedProperties,
                                                   File propertiesFile) throws MojoExecutionException {

    Properties projectProperties = project.getProperties();
    Properties ret = new Properties();
    if ("merge".equals(exposeProjectProperties) && projectProperties != null) {
      for (String name : projectProperties.stringPropertyNames()) {
        if (splitProjectNestedProperties && !name.startsWith("project.")) {
          setNestedProperty(name, getTyped(projectProperties.getProperty(name)), ret, true);
        } else {
          ret.put(name, getTyped(projectProperties.getProperty(name)));
        }
      }
    }
    if (propertyFiles != null) {
      StringSearchInterpolator interpolator = new StringSearchInterpolator();
      interpolator.addValueSource(new ObjectBasedValueSource(project));
      interpolator.addValueSource(new ObjectBasedValueSource(new GetProject(project)));
      if (projectProperties != null) {
        interpolator.addValueSource(new PropertiesBasedValueSource(projectProperties));
        Properties pp = new Properties();
        for (String name : projectProperties.stringPropertyNames()) {
          pp.put(name.startsWith("project.properties.") ? name : "project.properties." + name, projectProperties.getProperty(name));
        }
        interpolator.addValueSource(new PropertiesBasedValueSource(pp));
      }

      if (propertiesFile != null) {
        Properties p = new Properties();
        try {
          p.load(new FileInputStream(propertiesFile));
        } catch (IOException e) {
          throw new MojoExecutionException("Error loading property file " + propertiesFile, e);
        }
        for (String name : p.stringPropertyNames()) {
          Object value = null;
          try {
            value = getTyped(interpolator.interpolate(p.getProperty(name)));
          } catch (InterpolationException e) {
            throw new MojoExecutionException("Error interpolating property '" + name + "' in file " + propertiesFile, e);
          }
          if (splitNestedProperties) {
            setNestedProperty(name, value, ret, true);
          } else {
            ret.put(name, value);
          }
        }
      }
    }

    if (properties != null) {
      for (String name : properties.stringPropertyNames()) {
        if (splitNestedProperties) {
          setNestedProperty(name, getTyped(properties.getProperty(name)), ret, true);
        } else {
          ret.put(name, getTyped(properties.getProperty(name)));
        }
      }
    }

    if (propertiesName != null) {
      Map<Object, Object> ctx = new HashMap<Object, Object>();
      ctx.put(propertiesName, ret);
      if ("property".equals(exposeProjectProperties) && projectProperties != null) {
        ctx.put((projectPropertiesName == null ? "projectProperties" : projectPropertiesName),
                splitNestedProperties(projectProperties, splitProjectNestedProperties, true));
      }
      return ctx;
    } else {
      if ("property".equals(exposeProjectProperties) && projectProperties != null) {
        ret.put((projectPropertiesName == null ? "projectProperties" : projectPropertiesName),
                splitNestedProperties(projectProperties, splitProjectNestedProperties, true));
      }
      return ret;
    }
  }

  private Object getTyped(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    } else if ("true".equals(str)) {
      return true;
    } else if ("false".equals(str)) {
      return false;
    } else if (NumberUtils.isNumber(str)) {
      return NumberUtils.createNumber(str);
    } else {
      return str;
    }

  }

  private Properties splitNestedProperties(Properties properties, boolean split, boolean copy) {
    if (properties != null && (split || copy)) {
      Properties splitProperties = new Properties();
      for (String name : properties.stringPropertyNames()) {
        if (split) {
          setNestedProperty(name, properties.getProperty(name), splitProperties, true);
        } else {
          splitProperties.put(name, properties.getProperty(name));
        }
      }
      return splitProperties;
    } else {
      return properties;
    }
  }

  @SuppressWarnings("unchecked")
  private void setNestedProperty(String property, Object value, Map<Object, Object> map, boolean copyFlatProperty) {
    String[] split = property.split("\\.", 2);
    if (split.length > 1) {
      if (copyFlatProperty) {
        map.put(property, value);
      }
      Object o = map.get(split[0]);
      if (o == null) {
        o = new HashMap<String, Object>();
        map.put(split[0], o);
      } else if (!(o instanceof Map)) {
        throw new IllegalArgumentException("Error splitting nested property '" + property + ", at " + split[0] + ". Property with name " +
                                           split[0] + " already present and is a scalar value " + o);
      }
      setNestedProperty(split[1], value, (Map<Object, Object>)o, true);
    } else {
      map.put(property, value);
    }
  }

  @Override public String toString() {
    final StringBuilder sb = new StringBuilder("Transformation{");
    sb.append("templates=").append(Arrays.toString(templates));
    sb.append(", properties=").append(properties);
    sb.append(", propertyFiles=").append(Arrays.toString(propertyFiles));
    sb.append(", splitNestedProperties=").append(splitNestedProperties);
    sb.append(", propertiesName='").append(propertiesName).append('\'');
    sb.append(", exposeProjectProperties='").append(exposeProjectProperties).append('\'');
    sb.append(", projectPropertiesName='").append(projectPropertiesName).append('\'');
    sb.append('}');
    return sb.toString();
  }

  public static class GetProject {
    private final MavenProject project;

    public GetProject(MavenProject project) {
      this.project = project;
    }

    public MavenProject getProject() {
      return project;
    }
  }
}
