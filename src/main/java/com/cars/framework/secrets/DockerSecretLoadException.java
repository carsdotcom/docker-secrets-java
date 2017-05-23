package com.cars.framework.secrets;

@SuppressWarnings("serial")
public class DockerSecretLoadException extends Exception {

  public DockerSecretLoadException(String message) {
    super(message);
  }

  public DockerSecretLoadException(String message, Throwable cause) {
    super(message, cause);
  }

}
