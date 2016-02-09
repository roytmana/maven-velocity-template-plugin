/**
 * Copyright (c) 2011, Peace Technology, Inc.
 * $Author:$
 * $Revision:$
 * $Date:$
 * $NoKeywords$
 */

package com.peacetech.maven;

public class Template {
  private String templateFile;
  private String outputFile;

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

  @Override public String toString() {
    final StringBuilder sb = new StringBuilder("Template{");
    sb.append("template=").append(templateFile);
    sb.append(", output=").append(outputFile);
    sb.append('}');
    return sb.toString();
  }
}
