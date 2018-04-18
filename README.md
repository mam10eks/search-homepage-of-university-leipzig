# A Searchengine for the University Leipzig

**GOAL:** This project contains a seachengine to retrieve informations from the homepage of the university leipzig.
It was created during the 
[information retrieval internship in winter semester 2017](https://www.informatik.uni-leipzig.de/ifi/professuren/angewandteinf/temir/teaching/information-retrieval/).

## Run It

This repository already contains a small subset of the data that could be crawled from http://uni-leipzig.de (namely the domain http://lips.informatik.uni-leipzig.de/).

Assuming that your system meets some requirements (i.e. you need java 8, maven 3 and docker + docker-compose),
you could start everything with the example dataset by leveraging:

```
git clone https://github.com/mam10eks/search-homepage-of-university-leipzig.git
cd search-homepage-of-university-leipzig
mvn install
docker-compose up
```

Now you could access the web interface by pointing your browser to `http://localhost:8080`.

## Modules

This project consists of different modules:
* [search-engine-backend](search-engine-backend)
  * The backend. Contains a rudimentary thymeleaf frontend (all features are available)
* [search-engine-evaluation](search-engine-evaluation)
  * A Tool to create lucene-indizes where every index contains only documents which are for a single evaluation-topic(e.g. a user-query) anotated with relevance feedback. This serves as a first step to optimize the parameters/settings of the retrieval-model or preprocessing.
* [final-presentation](final-presentation)
  * Our last presentation which gives an overview about the search-engine.
    This presentation will get embedded into the search-engine during the build (and is accessible by entering `/presentation/index.html`).
* [search-engine-events](search-engine-events)
  * A collection of Events which are evaluated/stored for logging-purposes within the search-engine.
* [search-engine-kafka-streams](search-engine-kafka-streams)
  * A Kafka-Server with a custom Stream-Processor to enrich events and to classify those events such that consumers could describe very specific in which events they are interested (e.g. events tailored for the suggestion component)
* [visualize_events](visualize_events)
  * A small visualization of events that are fired from the backend (like user "XY" searched for "abc" and clicked on result 4)
  * Please use this as a first way to see how users use the application
  * This is only there for testing purposes and reviews.
* [example_indices](example_indices)
  * Some **small** example indices that you are able to start everything without any further requirements.
* [javascript-frontend](javascript-frontend)
  * A JS frontend which has some more features (server-side-rendering, single-page-application if a client uses JS, support of the browser-history-apu).
