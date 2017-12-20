FROM java:8-jre-alpine

RUN mkdir -p /opt/search-engine/index

ARG JAR_FILE
ADD target/${JAR_FILE} /opt/search-engine/search-engine-backend.jar
ADD target/index /opt/search-engine/index

CMD ["java", "-jar",  "/opt/search-engine/search-engine-backend.jar", "--indexDirectory=/opt/search-engine/index"]
