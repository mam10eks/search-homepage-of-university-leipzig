# Searchengine for University Leipzig (WIP!)

**GOAL:** At the end, this project will contain a seachengine to retrieve informations from the homepage of the university leipzig.
It was created during the 
[information retrieval internship in winter semester 2017](https://www.informatik.uni-leipzig.de/ifi/professuren/angewandteinf/temir/teaching/information-retrieval/).

## Start It

You could start the development version of the search engine by leveraging one of the commands:
* `./scripts/run_me.sh`
  * If you have a bash shell, this will compile the project, execute all unit tests and starting the jar after that.
* `mvn clean install spring-boot:run`
  * For a arbitrary operating system will this command compile the project, execute all unit tests and starting the jar after that.

Then you could point your browser to `localhost:8080` to see the user interface of the search engine.

## Onboarding

If you want to work on the project you find some important tools that are used for the development here.
You dont need a deep understanding of them (basically you should be fine if you have a rough idea what they do).

### General Stuff

* [Maven](https://maven.apache.org/) as build tool
* [Spring Boot](https://projects.spring.io/spring-boot/)
  * Dependency injection
  * Embedded Tomcat
* [Spring MVC](https://spring.io/guides/gs/serving-web-content/)
  * Web-Services
  * Views with [Thymeleaf](http://www.thymeleaf.org/doc/tutorials/2.1/thymeleafspring.html)
* [Spring HATEOAS](http://projects.spring.io/spring-hateoas/) to create links between services
* [Lucene](https://lucene.apache.org/core/) to index and search the documents

### Development Practices

* [Project Lombok](https://projectlombok.org/) to reduce boilerplate code
* [Aspect Oriented Programming](https://en.wikipedia.org/wiki/Aspect-oriented_programming)

### Testing

* [JUnit](http://junit.org/junit4/) + [mockito](http://site.mockito.org/)
* [Approval Tests](https://github.com/approvals/ApprovalTests.Java)
