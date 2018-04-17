FROM java:8-jre-alpine

RUN mkdir -p /opt/search-engine/index

ARG JAR_FILE
ADD target/${JAR_FILE} /opt/search-engine/search-engine-backend.jar
ADD target/index /opt/search-engine/index
ADD target/suggestion_index /opt/search-engine/suggestion_index

CMD ["java", "-jar",  "/opt/search-engine/search-engine-backend.jar", "--indexDirectory=/opt/search-engine/index", "--suggestionIndexDirectory=/opt/search-engine/suggestion_index", "--server.port=7777"]
