#!/usr/bin/env python3

import json
from threading import Thread
from kafka import KafkaConsumer
from neo4j.v1 import GraphDatabase

_driver = GraphDatabase.driver("bolt://localhost:7687", auth=("neo4j", "neo4j"))

def executeCypher(cypherStatement):
	with _driver.session() as session:
		with session.begin_transaction() as tx:
			tx.run(cypherStatement)


def webEventsConsumer():
	for msg in KafkaConsumer("web_events", auto_offset_reset='earliest'):
		print("web_events: Handle Message-Offset: "+ str(msg.offset))
		event = json.loads(msg.value.decode("utf-8"))
		request = event.get("request")
	
		executeCypher("MERGE (client:client {clientId: '"+ event.get("clientId") +"'}) "+
			"MERGE (address:ip {address: '"+ request.get("remoteAddr") +"'}) "+
			"MERGE (userAgent:userAgent {name: '"+ request.get("headers").get("user-agent") +"'}) "+
			"MERGE (client)-[userAgentRelationship:executed_request_from]->(userAgent) "+
				"ON CREATE SET userAgentRelationship.count = 1 ON MATCH SET userAgentRelationship.count = userAgentRelationship.count +1 "+
			"MERGE (client)-[request:executed_request_from]->(address) "+
				"ON CREATE SET request.count = 1 ON MATCH SET request.count = request.count +1;")


def searchEventsConsumer():
	for msg in KafkaConsumer("search_events", auto_offset_reset='earliest'):
		print("search_events: Handle Message-Offset: "+ str(msg.offset))
		event = json.loads(msg.value.decode("utf-8"))
		request = event.get("request")

		executeCypher("MERGE (client:client {clientId: '"+ event.get("clientId") +"'}) "+
			"MERGE (query:query {query: '"+ event.get("query") +"', resultPage:"+ str(event.get("resultPage")) +"}) "+
			"MERGE (client)-[r:executedQuery]->(query) "+
				"ON CREATE SET r.count = 1 ON MATCH SET r.count = r.count +1;");


def selectSearchResultEventsConsumer():
	for msg in KafkaConsumer("select_search_result_events", auto_offset_reset='earliest'):
		print("select_search_result_events: Handle Message-Offset: "+ str(msg.offset))
		event = json.loads(msg.value.decode("utf-8"))
		request = event.get("request")

		executeCypher("MERGE (client:client {clientId: '"+ event.get("clientId") +"'}) "+
			"MERGE (query:query {query: '"+ event.get("query") +"', resultPage: "+ str(event.get("searchPage")) +"}) "+
			"MERGE (clickedResult:clicked {documentId: "+ str(event.get("resultId")) +", "+
				"documentHref: '"+ event.get("documentUri") +"'}) "+
			"MERGE (client)-[clicked:selectedResult]->(clickedResult) "+
				"ON CREATE SET clicked.count = 1 ON MATCH SET clicked.count = clicked.count +1 "+
			"MERGE (clickedResult)-[:from]->(query);")


executeCypher("MATCH (n) DETACH DELETE n;")

webEventsConsumerThread = Thread(target=webEventsConsumer, args=())
searchEventsConsumerThread = Thread(target=searchEventsConsumer, args=())
selectSearchResultEventsConsumerThread = Thread(target=selectSearchResultEventsConsumer, args=())

webEventsConsumerThread.start()
searchEventsConsumerThread.start()
selectSearchResultEventsConsumerThread.start()

webEventsConsumerThread.join()
searchEventsConsumerThread.join()
selectSearchResultEventsConsumerThread.join()
