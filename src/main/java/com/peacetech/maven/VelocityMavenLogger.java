package com.peacetech.maven;

import org.apache.maven.plugin.logging.Log;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

public class VelocityMavenLogger implements LogChute {
  private final Log mavenLogger;

  public VelocityMavenLogger(Log mavenLogger) {
    this.mavenLogger = mavenLogger;
  }

  @Override public void init(RuntimeServices rs) throws Exception {
  }

  @Override public void log(int level, String message) {
    switch (level) {
      case LogChute.DEBUG_ID:
        mavenLogger.debug(message);
        break;
      case LogChute.INFO_ID:
        mavenLogger.info(message);
        break;
      case LogChute.WARN_ID:
        mavenLogger.warn(message);
        break;
      case LogChute.ERROR_ID:
        mavenLogger.error(message);
        break;
      default:
        mavenLogger.error("Unsupported Velocity log level:" + level + ", using Info level instead");
        mavenLogger.info(message);
    }
  }

  @Override public void log(int level, String message, Throwable exception) {
    switch (level) {
      case LogChute.DEBUG_ID:
        mavenLogger.debug(message, exception);
        break;
      case LogChute.INFO_ID:
        mavenLogger.info(message, exception);
        break;
      case LogChute.WARN_ID:
        mavenLogger.warn(message, exception);
        break;
      case LogChute.ERROR_ID:
        mavenLogger.error(message, exception);
        break;
      default:
        mavenLogger.error("Unsupported Velocity log level:" + level + ", using Info level instead");
        mavenLogger.info(message, exception);
    }
  }

  @Override public boolean isLevelEnabled(int level) {
    switch (level) {
      case LogChute.TRACE_ID:
        return false;
      case LogChute.DEBUG_ID:
        return mavenLogger.isDebugEnabled();
      case LogChute.INFO_ID:
        return mavenLogger.isInfoEnabled();
      case LogChute.WARN_ID:
        return mavenLogger.isWarnEnabled();
      case LogChute.ERROR_ID:
        return mavenLogger.isErrorEnabled();
      default:
        mavenLogger.error("Unsupported Velocity log level:" + level + ", using Info level instead");
        return mavenLogger.isInfoEnabled();
    }
  }
}