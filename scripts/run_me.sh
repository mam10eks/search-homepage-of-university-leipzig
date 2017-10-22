#!/bin/bash -e

mvn clean install

java -jar target/search-engine-uni-0.0.1-SNAPSHOT.jar
