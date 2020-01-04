#ElasticSearch Server
#docker run -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:7.5.1
docker-compose
#Jaeger-agent
docker run -d --name agent -p5775:5775/udp -p6831:6831/udp -p6832:6832/udp -p5778:5778/tcp jaegertracing/jaeger-agent --collector.host-port=localhost:14267
#jaeger-collector
docker run -d -e SPAN_STORAGE_TYPE=elasticsearch -p14267:14267 jaegertracing/jaeger-collector --es.server-urls 'http://127.0.0.1:9200'
#jaeger-query
docker run -e SPAN_STORAGE_TYPE=elasticsearch -p16686:16686/tcp jaegertracing/jaeger-query --es.server-urls 'http://127.0.0.1:9200'