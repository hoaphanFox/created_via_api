 TODO: Don't forget to update this file so it is relevant for your project!

# Spring Boot template

A template for foxsports microservice projects. The templates tries best to adhere to 12factor app principles

# Run It !!!
```
./mvnw spring-boot:run -Dspring-boot.run.profiles=local,demo
```
#NOTE

*DO NOT* rename any BEDROCK packages or classes within! Any project specific implementation should go in its own package,
leaving the framework setup package untouched.

# TODO
1. add locations that need to change after forking this repo. (anything marked as TODO)
2. project lifecycle, git pull request to bedrock etc documentation
3. Testing framework
4. Component Tests 
5. Documentation on philosophy
6. *maven .settings file instructions*
7. feature-flag support builtin
8. Make sure it only logs to console never to a file (file is state) (json / regular both)

*****Contributors Pls send Git Pull*****

# 12-FACTOR-APPS 

If you haven't seen this already, STOP, go and read and digest!

https://12factor.net/ 


# PROJECT LIFECYCLE
TODO: Details
1. Fork
2. rename:
``` 
    <groupId>au.com.foxsports.{your-group}</groupId>
    <artifactId>{your-artifact}</artifactId>
    <version>1.0-SNAPSHOT</version>
```
   
# Resolving Conflicts


How to perform a manual merge:
Step 1: Fetch changes from the upstream repository (saving the upstream branch as FETCH_HEAD).

`git fetch https://bitbucket.foxsports.com.au/scm/bed/bedrock.git master`

Step 2: Checkout the fork branch and merge in the changes from the upstream branch. Resolve conflicts.
```
git checkout master 
git merge FETCH_HEAD
```

Step 3. Resolve conflicts

Step 4. Commit `git commit` (git should show the merged files etc in commit logs)

Step 5. `git push origin master`


# Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

# Prerequisites

What things you need to install the software and how to install them

```
Java 1.8
TODO: maven .settings file instructions
```

## Customising project

### Overview
There is a number of parameters throughout the project that can be customised. Parameters are defined in file `customize.properties` and can be referred from any project file

```
<!--    <name>customize!project.name</name> -->
```

Where `project.name` is a parameter name.

Parameterised line should be commented out, so it doesn't affect file code or structure of a file.

After new project is created by cloning **bedrock** repository, values in `customize.properties` file has to be reviewed and then `customize.sh` script has to be run

```
./customize.sh
```
it will make a copy of every parameterised line and resolve corresponding parameter
```
<!--    <name>customize!project.name</name> -->
    <name>bedrock-k8s</name> <!-- Please do not edit this line! Managed by customize.sh -->
```
`customize` script is idempotent, so it can be run multiple times and every time it will resolve parameters using values from `customize.properties` file.

Files or folders can be excluded from customisation process using `.customizeignore` file.

### Line indentation
In some cases (like `.yaml` or `.py` files), file structure is defined through indentation (number of whitespace characters at the start of a line). In such cases extra care should be taken while preparing customized lines.

First thing to remember is that when parameters are resolved, the entire line following *comment characters* will be copied, so it's important to put between *comment characters* and first non-whitespace character of a line as many spaces (or tabs) as we want to be at the start of the line (note 2 spaces between `#` and `name`):
```
project:
  #  name: customize!project.name
```
will become (2 spaces at the start of the line)
```
project:
  name: bedrock-k8s
```
Due to specific of `customize` script implementation, there has to be a separator between *comment characters* and beginning of the line template. In most cases, such separators are whitespaces like in example above. However, sometimes resulting line required to not have any indentation.

In this case, `_` character can be used as such separator:
```
#_name: customize!project.name
```
will become (note, there is no spaces at the start of the line)
```
name: bedrock-k8s
```

## Installation and running

Download the application into your workspace directory and generate the intellij files

```
./mvnw idea
```

then open the module in intellij.

You can launch the service by running the Application in intellij or via mvnw as follows:

```
./mvnw spring-boot:run
```

and access a test endpoint on 

```
http://localhost:8080/api/greeting/<<name>> or http://localhost:8080/api/greeting/<<name>>?namespace=<<namespace>>
```

a sample error can be generated by navigating to 

```
http://localhost:8080/api/greeting/my-bad or http://localhost:8080/api/greeting/your-badd
```

## Running the tests

All unit, integration and static code analysis (TODO) 

and the reports can be located in the build/reports directory.

By default _Component Tests_ are disabled by default so we can easily run `mvn package` to prepare the target for the component tests.

**Running All Tests (Unit & Component)**

```
mvn test -Dtest.skip.component=false
mvn verify -Dtest.skip.component=false
```

**Debugging Tests**

```
mvn -Dmaven.surefire.debug test
mvn -Dmaven.failsafe.debug verify
```

This will open a debug port on 5005. For more configuration info refer to https://maven.apache.org/surefire/maven-failsafe-plugin/examples/debugging.html

## Component Tests

**Running Component Tests Only**

`mvn verify -Dtest.skip.unit=true -Dtest.skip.component=false`

**Running Single Component Tests**

`mvn verify -Dtest.skip.unit=true -Dtest.skip.component=false -Dit.test=GreetingControllerComponentSpec`

**Running Component Tests Against a Specific URL**

By default component tests will target `http://localhost:8080`. If you want to target a different URL:

`mvn verify -Dtest.skip.unit=true -Dtest.skip.component=false -Dtest.targetUrl=http://localhost:8081`

## Unit Tests

**Running Unit Tests Only**

```
mvn test
mvn verify
```

### Performance tests

```
TODO:
```

## Documentation

SWAGGER:
```
http://localhost:8080/api-doc/index.html#/default/get_greeting__name__matrix_ 
```


## Monitoring

```
http://localhost:8080/actuator/health
```

You can access service metrics (average processing time of an endpoint etc..) here:
```
http://localhost:8080/actuator/metrics
```


You can access details on the current build here:
```

http://localhost:8080/actuator/info
```

To list other available endpoints browse to this url:
```
http://localhost:8080/actuator/mappings
```

## Logging
Always use SLF4J classes for the loggers.

Sleuth is already configured and will automatically work with HTTP controllers and Spring's RestTemplate. All traces will be sent to Zipkin by default (you'll need to set the Zipkin location in the environment configuration for this to work).

Standard logging is enabled by default, so it is easier to read the logs when running locally. To enable JSON logging just enable the "json-logging" profile. E.g.:

```
./mvnw spring-boot:run -Dspring-boot.run.profiles=json-logging
```

TODO: Instructions on how to run Zipkin locally.

## CI -> CD
TODO
## Deployment


## Built With

* [SpringBoot](https://projects.spring.io/spring-boot/) - The base framework used
* [Swagger](https://swagger.io/) - API framework
* TODO: add more
