package com.cars.framework.secrets;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DockerSecrets {

  private static final String SECRETS_DIR = "/run/secrets/";

  public static Map<String, String> load() throws DockerSecretLoadException {
    File secretsDir = new File(SECRETS_DIR);
    return load(secretsDir);
  }

  public static Map<String, String> loadFromFile(String fileName) throws DockerSecretLoadException {
    File secretsFile = new File(SECRETS_DIR + fileName);
    return loadFromFile(secretsFile);
  }

  public static Map<String, String> loadFromFile(File secretsFile)
      throws DockerSecretLoadException {

    if (!secretsFile.exists()) {
      throw new DockerSecretLoadException(
          "Unable to read secrets from file at [" + secretsFile.toPath() + "]");
    }

    Map<String, String> secrets = new HashMap<>();

    try {
      List<String> lines = Files.readAllLines(secretsFile.toPath(), Charset.defaultCharset());
      for (String line : lines) {
        int index = line.indexOf("=");
        if (index < 0) {
          throw new DockerSecretLoadException(
              "Invalid secrets in file at [" + secretsFile.toPath() + "]");
        }
        String key = line.substring(0, index);
        String value = line.substring(index + 1);
        secrets.put(key, value);
      }
    } catch (IOException e) {
      throw new DockerSecretLoadException(
          "Unable to read secrets from file at [" + secretsFile.toPath() + "]");
    }
    return secrets;

  }


  public static Map<String, String> load(File secretsDir) throws DockerSecretLoadException {

    if (!secretsDir.exists()) {
      throw new DockerSecretLoadException("Unable to find any secrets under [" + SECRETS_DIR + "]");
    }

    File[] secretFiles = secretsDir.listFiles();

    if (secretFiles == null || secretFiles.length == 0) {
      throw new DockerSecretLoadException("Unable to find any secrets under [" + SECRETS_DIR + "]");
    }

    Map<String, String> secrets = new HashMap<>();

    for (File file : secretFiles) {
      try {
        String secret = new String(Files.readAllBytes(file.toPath()));
        secrets.put(file.getName(), secret);
      } catch (IOException e) {
        throw new DockerSecretLoadException(
            "Unable to load secret from file [" + file.getName() + "]", e);
      }
    }
    return secrets;
  }
}
