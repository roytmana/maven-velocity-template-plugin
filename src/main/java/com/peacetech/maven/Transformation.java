package com.peacetech.maven;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Transformation {
  private Template[] templates;
  private Properties properties;
  private File[] propertyFiles;
  private boolean splitNestedProperties;
  private String propertiesName;
  private String exposeProjectProperties = "merge";
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

  public Map<Object, Object> getCombinedProperties(Properties projectProperties, boolean splitNestedProperties) {
    Properties ret = new Properties();
    if ("merge".equals(exposeProjectProperties) && projectProperties != null) {
      for (String name : projectProperties.stringPropertyNames()) {
        if (splitNestedProperties && !name.startsWith("project.")) {
          setNestedProperty(name, projectProperties.getProperty(name), ret);
        } else {
          ret.put(name, projectProperties.getProperty(name));
        }
      }
    }
    //todo process propertyFiles first so that properties take precedence
    if (properties != null) {
      for (String name : properties.stringPropertyNames()) {
        if (splitNestedProperties) {
          setNestedProperty(name, properties.getProperty(name), ret);
        } else {
          ret.put(name, properties.getProperty(name));
        }
      }
    }
    if (propertiesName != null) {
      Map<Object, Object> ctx = new HashMap<Object, Object>();
      ctx.put(propertiesName, ret);
      if ("property".equals(exposeProjectProperties) && projectProperties != null) {
        ctx.put((projectPropertiesName == null ? "projectProperties" : projectPropertiesName),
                splitNestedProperties(projectProperties, splitNestedProperties, true));
      }
      return ctx;
    } else {
      if ("property".equals(exposeProjectProperties) && projectProperties != null) {
        ret.put((projectPropertiesName == null ? "projectProperties" : projectPropertiesName),
                splitNestedProperties(projectProperties, splitNestedProperties, true));
      }
      return ret;
    }
  }

  private Properties splitNestedProperties(Properties properties, boolean split, boolean copy) {
    if (properties != null && (split || copy)) {
      Properties splitProperties = new Properties();
      for (String name : properties.stringPropertyNames()) {
        if (split) {
          setNestedProperty(name, properties.getProperty(name), splitProperties);
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
  private void setNestedProperty(String property, Object value, Map<Object, Object> map) {
    String[] split = property.split("\\.", 2);
    if (split.length > 1) {
      Object o = map.get(split[0]);
      if (o == null) {
        o = new HashMap<String, Object>();
        map.put(split[0], o);
      } else if (!(o instanceof Map)) {
        throw new IllegalArgumentException("Error splitting nested property '" + property + ", at " + split[0] + ". Property with name " +
                                           split[0] + " already present and is a scalar value " + o);
      }
      setNestedProperty(split[1], value, (Map<Object, Object>)o);
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
}
