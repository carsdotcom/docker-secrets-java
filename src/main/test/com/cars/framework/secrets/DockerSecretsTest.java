package com.cars.framework.secrets;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class DockerSecretsTest {

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();


  @Test(expected = DockerSecretLoadException.class)
  public void testNoSecretsDir() throws DockerSecretLoadException {
    DockerSecrets.load();
  }

  @Test(expected = DockerSecretLoadException.class)
  public void testNoSecretFiles() throws DockerSecretLoadException, IOException {
    File secretsDir = folder.newFolder("run", "secrets");
    DockerSecrets.load(secretsDir);
  }

  @Test
  public void testSecrets() throws IOException, DockerSecretLoadException {
    File secretsDir = folder.newFolder("run", "secrets");
    String data = "dbUserName=readonly";
    File secretFile = new File(secretsDir, "test-secret");
    FileWriter writer = new FileWriter(secretFile);
    writer.write(data);
    writer.close();
    Map<String, String> secrets = DockerSecrets.loadFromFile(secretFile);
    Assert.assertNotNull(secrets);
    Assert.assertTrue(secrets.get("dbUserName").equals("readonly"));
  }

  @Test(expected = DockerSecretLoadException.class)
  public void testInvalidSecrets() throws IOException, DockerSecretLoadException {
    File secretsDir = folder.newFolder("run", "secrets");
    String data = "dbUserName:readonly";
    File secretFile = new File(secretsDir, "test-secret");
    FileWriter writer = new FileWriter(secretFile);
    writer.write(data);
    writer.close();
    DockerSecrets.loadFromFile(secretFile);
  }

}
