package com.demo.codestatebkend;

import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.SpringApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication
public class CodestatebkendService {
    
    public static void main(String[] args) {
        SpringApplication.run(CodestatebkendService.class, args);
    }
    @Bean
    public io.opentracing.Tracer tracer() {
    	SamplerConfiguration samplerConfig = SamplerConfiguration.fromEnv()
                .withType(ConstSampler.TYPE)
                .withParam(1);
    	ReporterConfiguration reporterConfig = ReporterConfiguration.fromEnv()
                .withLogSpans(true);
    	Configuration config = new Configuration("codestatebkend-svc")
                .withSampler(samplerConfig)
                .withReporter(reporterConfig);

        return config.getTracer();
    }
}
@RestController
class CodestatebkendResource{
	@Autowired
    private Tracer tracer;
	final static String serverUrl1 = "https://gist.githubusercontent.com/PhantomGrin/a1e8ad30915ecd9d2659400d496d1ed6/raw/8b0dbb93521f5d6889502305335104218454c2bf/states_hash.json";
    final static String serverUrl2 = "https://gist.githubusercontent.com/PhantomGrin/a1e8ad30915ecd9d2659400d496d1ed6/raw/8b0dbb93521f5d6889502305335104218454c2bf/states_titlecase.json";
    
    private final static String[] headers_to_proagate = {"x-request-id","x-b3-traceid","x-b3-spanid","x-b3-sampled","x-b3-flags",
    	      "x-ot-span-context","x-datadog-trace-id","x-datadog-parent-id","x-datadog-sampled", "end-user","user-agent"};
    
    public static String requestProcessedData(int urlid){
        String serverUrl = null;
        if(urlid == 1){
            serverUrl = serverUrl1;
        }else if (urlid == 2){
            serverUrl = serverUrl2;
        }else{
            return "ERROR";
        }

        RestTemplate request = new RestTemplate();
        String result = request.getForObject(serverUrl, String.class);
        System.out.print(serverUrl);
        return (result);
    }

    @GetMapping("/")
    public static String Hello(){
        return "HELLO IM DATA READER";
    }

    @GetMapping("/readDataForCode")
    public String requestCodeData(@RequestHeader HttpHeaders headers){
    	SpanContext parentContext = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapExtractAdapter(headers.toSingleValueMap()));
        Span span = tracer.buildSpan("backend-svc-readDataForCode").asChildOf(parentContext).start();
        String response = requestProcessedData(1);
        span.finish();
        return response;
    }

    @GetMapping("/readDataForState")
    public String requestStateData(@RequestHeader HttpHeaders headers) {
    	SpanContext parentContext = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapExtractAdapter(headers.toSingleValueMap()));
        Span span = tracer.buildSpan("backend-svc-readDataForState").asChildOf(parentContext).start();
        String response = requestProcessedData(2);
        span.finish();
        return response;
    }

    @GetMapping("/backend1")
    public static String Backend1(){
        return "HELLO FROM DATA READER Backend-1";
    }
    @GetMapping("/backend2")
    public static String Backend2(){
        return "HELLO FROM DATA READER Backend-2";
    }
    @GetMapping("/backend3")
    public String Backend3(@RequestHeader HttpHeaders headers){
    	SpanContext parentContext = tracer.extract(Format.Builtin.HTTP_HEADERS, new TextMapExtractAdapter(headers.toSingleValueMap()));
        Span span = tracer.buildSpan("backend-svc-backend3").asChildOf(parentContext).start();
        String response = "HELLO FROM DATA READER Backend-3";
        span.finish();
        return response;
    }
}
