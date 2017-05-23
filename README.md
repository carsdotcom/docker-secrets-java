# Docker Secrets

A simple library to load [Docker secrets](https://docs.docker.com/engine/swarm/secrets/) in a swarm cluster as a map.

## Download

### Gradle

```groovy

repositories {
  jcenter()
}

dependencies {
  compile 'com.cars:docker-secrets:0.2.0'
}
```

### Maven

```xml
<dependency>
  <groupId>com.cars</groupId>
  <artifactId>docker-secrets</artifactId>
  <version>0.2.0</version>
</dependency>
```

## Usage

Docker secrets are availble to a container under /run/secrets/   

Given the below secrets :

```bash
$ echo "test-secret1-value" | docker secret create test-secret1 -
$ echo "test-secret2-value" | docker secret create test-secret2 -
$ echo "test-secret3-value" | docker secret create test-secret3 -
```

To load all secrets : 

```java
Map<String, String> secrets = DockerSecrets.load();
System.out.println(secrets.get("test-secret1")) // test-secret1-value
```

Since secrets are files, you can have a secret created with a properties file syntax as below

```properties
//secret-file.txt
dbuser=readonly
dbpass=super-secret-password
apikey=very-secret-api-key
```

Create the secret using the file:

```bash
$ docker secret create test-secret secret-file.txt
```

Then to load that secret:

```java
Map<String, String> secrets = DockerSecrets.loadFromFile("test-secret");
System.out.println(secrets.get("dbuser")) // readonly
```

### Working with Spring framework

Here is an example of how a `SecretsConfig` will look like when using Spring framework. It uses profiles to work with secrets locally. So if you are just testing you application outside of Docker, you can still use the same code

Create a file under `src/main/resource/config/secrets-file`

```properties
//secrets-file
dbuser=readonly
dbpass=secret-pass
```

And use this `@Configuration`:

```java
package com.cars.devops.config;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.cars.framework.secrets.DockerSecretLoadException;
import com.cars.framework.secrets.DockerSecrets;

@Configuration
public class SecretsConfig {

  //File under src/main/resources/config/
  private final String DEFAULT_SECRETS_FILE = "config/secrets-file";

  // This bean will be used in non-local or no profiles
  @Bean(name = "secrets")
  @Profile(value = "!local")
  public Map<String, String> secrets() {
    try {
      return DockerSecrets.loadFromFile("secrets-file");
    } catch (DockerSecretLoadException e) {
      System.out.println("Secrets Load failed : " + e.getMessage());
    }
    return Collections.emptyMap();
  }

  // This bean will be used for 'local' profile
  @Bean(name = "secrets")
  @Profile(value = "local")
  public Map<String, String> localSecrets() {
    try {
      URL url = ClassLoader.getSystemResource(DEFAULT_SECRETS_FILE);
      if (url != null) {
        return DockerSecrets.loadFromFile(new File(url.getPath()));
      } else {
        System.out.println("Secrets Load failed : No file at " + DEFAULT_SECRETS_FILE);
      }
    } catch (DockerSecretLoadException e) {
      System.out.println("Secrets Load failed : " + e.getMessage());
    }
    return Collections.emptyMap();
  }
}
``` 

Now you can run you application with the `local` profile :

```bash
$ java -Dspring.profiles.active=local -jar your.jar
```

Or if using Spring boot:

```bash
$ gradlew bootRun -Dspring.profiles.active=local
```

Use `@Resource` to reference the `secrets` bean in other beans/configs:

```java
public class Application {

  @Resource(name = "secrets")
  private Map<String, String> secrets;

  // TODO Add your application beans here or use @Import as above

  @Bean
  public String somebean() {
    System.out.println("DBuser is : " + secrets.get("dbuser")); //should print readonly
    return "";
  }

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

}
```

## Build

```bash
$ ./gradlew clean build
```

## Test

```bash
$ ./gradlew clean test
```

## License

Apache 2.0
