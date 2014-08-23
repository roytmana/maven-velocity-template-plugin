package com.peacetech.maven;

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

  @Override public String toString() {
    final StringBuilder sb = new StringBuilder("Transformation{");
    sb.append("\n  templates=").append(Arrays.toString(templates));
    sb.append("\n  properties=").append(properties);
    sb.append("\n  propertyFiles=").append(Arrays.toString(propertyFiles));
    sb.append('}');
    return sb.toString();
  }

  public Properties getCombinedProperties(boolean splitNestedProperties) {
    Properties ret = new Properties();
    //todo process propertyFiles first so that properties take precedence
    if (properties != null) {
      for (String name : properties.stringPropertyNames()) {
        ret.put(name, properties.getProperty(name));
      }
    }
    if (!splitNestedProperties) {
      return ret;
    }
    Properties ret1 = new Properties();
    for (Map.Entry<Object, Object> entry : ret.entrySet()) {
      setNestedProperty((String)entry.getKey(), entry.getValue(), ret1);
    }
    return ret1;
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
}
