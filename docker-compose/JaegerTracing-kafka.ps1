#Elastisearch
#docker-compose up -d --build elasticsearch
#Jaeger-query
#docker-compose up -d --build jaeger-query
#Kafka
#Run zookeeper confluentinc/cp-zookeeper:5.0.1
docker run -d --net=host --name=zookeeper -e ZOOKEEPER_CLIENT_PORT=2181 -e ZOOKEEPER_TICK_TIME=2000 confluentinc/cp-zookeeper:5.0.1
docker logs zookeeper
#run kafka
docker run -d --net=host --name=kafka -e KAFKA_ZOOKEEPER_CONNECT=localhost:2181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 confluentinc/cp-kafka:5.0.1
docker logs kafka
docker run --net=host --rm confluentinc/cp-kafka:5.0.1 kafka-topics --create --topic jaeger-spans --partitions 1 --replication-factor 1 --if-not-exists --zookeeper localhost:2181
docker run --net=host --rm confluentinc/cp-kafka:5.0.1 kafka-topics --describe --topic jaeger-spans --zookeeper localhost:2181
#Jaeger-collector
docker run -d -e SPAN_STORAGE_TYPE=kafka jaegertracing/jaeger-collector:1.8 --kafka.brokers 'localhost:9092' --kafka.topic 'jaeger-spans'
#Jaeger-agent
docker run -d --rm -p5775:5775/udp -p6831:6831/udp -p6832:6832/udp -p5778:5778/tcp jaegertracing/jaeger-agent:1.8
#Jaeger-ingester
docker run -d -e SPAN_STORAGE_TYPE=elasticsearch jaegertracing/jaeger-ingester:1.8 --kafka.brokers 'localhost:9092' --kafka.topic 'jaeger-spans' --es.server-urls 'http://127.0.0.1:9200'


docker start $(docker ps -a -q)

docker rm $(docker ps -a -q) -f
docker rmi $(docker images -a -q) -f


docker ps -a
