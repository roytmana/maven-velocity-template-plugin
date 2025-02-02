/**
 * Copyright (c) 2011, Peace Technology, Inc.
 * $Author:$
 * $Revision:$
 * $Date:$
 * $NoKeywords$
 */

package com.peacetech.maven;

import java.util.Properties;

public class Template {
  private String templateFile;
  private String outputFile;
  private boolean copy;
  private boolean skipIfAbsent;

  private Properties properties;

  public String getTemplateFile() {
    return templateFile;
  }

  public void setTemplateFile(String templateFile) {
    this.templateFile = templateFile;
  }

  public String getOutputFile() {
    return outputFile;
  }

  public void setOutputFile(String outputFile) {
    this.outputFile = outputFile;
  }

  public boolean isCopy() {
    return copy;
  }

  public void setCopy(boolean copy) {
    this.copy = copy;
  }

  public boolean isSkipIfAbsent() {
    return skipIfAbsent;
  }

  public void setSkipIfAbsent(boolean skipIfAbsent) {
    this.skipIfAbsent = skipIfAbsent;
  }

  public Properties getProperties() {
    return properties;
  }

  public void setProperties(Properties properties) {
    this.properties = properties;
  }

  @Override public String toString() {
    final StringBuilder sb = new StringBuilder("Template{");
    sb.append("template=").append(templateFile);
    sb.append(", output=").append(outputFile);
    sb.append('}');
    return sb.toString();
  }
}
