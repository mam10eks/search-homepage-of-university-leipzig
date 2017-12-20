# Backend of the Searchengine for University Leipzig (WIP!)

**GOAL:** At the end, this project will contain a seachengine to retrieve informations from the homepage of the university leipzig.
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
* [search-engine-uni-backend](search-engine-uni-backend)
  * The backend.
* [visualize_events](visualize_events)
  * A small visualization of events that are fired from the backend (like user "XY" searched for "abc" and clicked on result 4)
  * Please use this as a first way to see how users use the application
  * This is only there for testing purposes and reviews.
* [example_indices](example_indices)
  * Some **small** example indices that you are able to start everything without any further requirements.
