cd C:\Infosys-Citizen\POC\DistributedTracing-Jaeger\codestate
mvn clean install
docker images
docker build -t codestate:v1 ./
cd C:\Infosys-Citizen\POC\DistributedTracing-Jaeger\codestatebkend
mvn clean install
docker images
docker build -t codestatebkend:v1 ./
docker ps --all
#docker run -d -p 6831:6831/udp -p 16686:16686 jaegertracing/all-in-one:latest 
#docker run -d -p 5775:5775/udp -p 16686:16686 jaegertracing/all-in-one:latest
docker run -p5775:5775/udp -p6831:6831/udp -p6832:6832/udp -p16686:16686 jaegertracing/all-in-one:latest --log-level=debug
docker run --rm -it --network=host jaegertracing/all-in-one
docker run -d -p 9411:9411 openzipkin/zipkin:latest
##
cd C:\Infosys-Citizen\POC\DistributedTracing-Jaeger
docker-compose up --build -d


#https://dzone.com/articles/how-to-run-kafka-on-openshift-the-enterprise-kuber
##https://www.digitalocean.com/community/tutorials/how-to-remove-docker-images-containers-and-volumes
#https://stackoverflow.com/questions/51785812/how-to-configure-jaeger-with-elasticsearch

#docker rm $(docker ps -a -q) -f
#docker rmi $(docker images -a -q) -f
#docker exec -it distributedtracing-jaeger_kafka_1 /bin/bash
#docker system prune --all
docker-compose up -d elasticsearch
docker-compose up -d 
docker ps -a
docker start $(docker ps -a -q)
docker exec -it $(docker ps -qf "name=kafka") /bin/bash