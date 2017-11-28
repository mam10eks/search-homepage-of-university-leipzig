#!/bin/bash -e

docker run --rm --env=NEO4J_AUTH=none -p 7474:7474 -p 7687:7687 neo4j:3.2
