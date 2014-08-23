/**
 * Copyright (c) 2011, Peace Technology, Inc.
 * $Author:$
 * $Revision:$
 * $Date:$
 * $NoKeywords$
 */

package com.peacetech.maven;

import java.io.File;

public class Template {
  private File templateFile;
  private File outputFile;

  public File getTemplateFile() {
    return templateFile;
  }

  public void setTemplateFile(File templateFile) {
    this.templateFile = templateFile;
  }

  public File getOutputFile() {
    return outputFile;
  }

  public void setOutputFile(File outputFile) {
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
