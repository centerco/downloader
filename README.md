# downloader

Simple http file download library

## Installation

Clone repository, then run:

```mvn compile package``` 

go to target directory and install maven library:

```mvn install:install-file -Dfile=./downloader-1.0.3.jar -DgroupId=org.lineate -DartifactId=downloader -Dversion=1.0.3 -Dpackaging=jar -DgeneratePom=true```

Pom import:

```
    <dependency>
      <groupId>org.lineate</groupId>
      <artifactId>downloader</artifactId>
      <version>1.0.3</version>
    </dependency>
```

See usage examples inside the integration tests.
