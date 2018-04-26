# Step by step guide to operate the search-engine

This document lists detailed setup-steps for all relevant parts of the search engine.

## Install Prerequisites

Your system needs some requirements: java 8, maven 3 and docker + docker-compose.
Assumed that you use a recent version of Ubuntu, you could install those tools by leveraging:
```
sudo apt-get install openjdk-8-jdk maven docker.io docker-compose 
```

## Crawling (optional)

We used nutch 1.x for crawling.

### Install Nutch 1.X

First you would need to install it by leveraging:
```
# Download Nutch binaries from https://www.apache.org/dyn/closer.lua/nutch/1.14/apache-nutch-1.14-bin.tar.gz
wget http://mirror.yannic-bonenberger.com/apache/nutch/1.14/apache-nutch-1.14-bin.tar.gz

#extract it. E.g. to `opt/nutch/1.14`
sudo mkdir -p /opt/nutch
sudo tar -xf apache-nutch-1.14-bin.tar.gz -C /opt/nutch/
sudo mv /opt/nutch/apache-nutch-1.14/ /opt/nutch/1.14

#Add relevant Nutch executable to the PATH
sudo update-alternatives  --install /usr/bin/nutch nutch /opt/nutch/1.14/bin/nutch 1
sudo update-alternatives  --install /usr/bin/crawl crawl /opt/nutch/1.14/bin/crawl 1
```

Since nutch needs a `JAVA_HOME` environment variable, you could set it by:
```
# See where java is installed:
update-alternatives --list java
# Gives: /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java
# E.g. Java home would be /usr/lib/jvm/java-8-openjdk-amd64/
echo -e 'export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64/"'| sudo tee -a /etc/profile
# Now logout and login, or use . /etc/profile to use the new JAVA_HOME environment variable
```

Now nutch and the crawl script is installed and you could verify that with `nutch -help` and `crawl -help`.
Lastly you should give nutch the ability to write its logs:
```
sudo mkdir /opt/nutch/1.14/logs
# everyone should be able to read and write logs
sudo chmod -R 0777 /opt/nutch/1.14/logs
```

### Configure Nutch
Create a crawl-directory with a dedicated seed directory (e.g. in your home):

```
mkdir -p ~/crawl_uni_leipzig_de/seeds

# Add some seed urls 
curl 'https://raw.githubusercontent.com/mam10eks/check-nutch-regex-urlfilter/master/seed_urls.txt' > ~/crawl_uni_leipzig_de/seeds/seed_urls.txt
```

Next you should configure nutch.
You could simply use our configuration.
Simply replace `/opt/nutch/1.14/conf/` [with our configuration folder](https://github.com/mam10eks/nutch_tools/tree/master/conf).

Next you should use add our regex-urlfilters:
```
curl 'https://raw.githubusercontent.com/mam10eks/check-nutch-regex-urlfilter/master/regex-urlfilter.txt'| sudo tee /opt/nutch/1.14/conf/regex-urlfilter.txt
```

### Start the crawling process

Now you are ready to crawl.
You need to specify how mutch crawling iterations nutch will do.
For example for executing 10 iterations you would leverage:
```
cd ~/crawl_uni_leipzig_de

crawl -s seeds crawl 10
```

You are finished if the crawling script says there is nothing left for crawling (for us it took 14 days).


## Index-Creation (optional)

The process is described in detail in the [associated README](https://github.com/mam10eks/nutch_tools/tree/master/index_lucene).

## Starting the search engine

First you need to get the source-code:
```
git clone https://github.com/mam10eks/search-homepage-of-university-leipzig.git
```

Now you need to build the code:
```
cd search-homepage-of-university-leipzig
mvn clean install
```

The repository already contains a small subset of the data that could be crawled from
http://uni-leipzig.de (namely the domain http://lips.informatik.uni-leipzig.de/).
If you want to use a different index, you need to mount the root directory of the index into the
`searchEngineBackend` service which is declared within [docker-compose.yml](docker-compose.yml).
E.g. if you have the index at `/opt/full-index`, you would need to add a line: `volumes: [ "/opt/full-index:/opt/search-engine/index" ]`.
The service description of `searchEngineBackend` would look like:
```
searchEngineBackend:
  image: uni-search-engine/backend:0.0.1-SNAPSHOT
  network_mode: "host"
  depends_on: [searchEngineStreamProcessor]
  volumes: [ "/opt/full-index:/opt/search-engine/index" ]
```

### Start it

Finally, you could start the search-engine by leveraging:
```
docker-compose up -d
```

The system needs some time to start up (after 20 seconds everything should be up and running).
Now you could access the web interface by pointing your browser to http://localhost:8080.

### Stop it

If you want to stop the search engine, you could use docker-compose.
Execute the following commands next to [docker-compose.yml](docker-compose.yml):
```
# If you want to restart it later, use stop, and later start:
docker-compose stop
# Otherwise you could simply kill it, and remove all containers:
docker-compose kill && docker-compose rm -f
```
