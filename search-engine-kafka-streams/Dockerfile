FROM wurstmeister/kafka:1.0.0

RUN mkdir /opt/kafka/my_jars

ARG JAR_FILE
ADD target/${JAR_FILE} /opt/kafka/my_jars/search-engine-kafka-stream-processors.jar
