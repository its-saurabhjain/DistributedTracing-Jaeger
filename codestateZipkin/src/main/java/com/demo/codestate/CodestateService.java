package com.demo.codestate;
//
import java.util.Map;
//
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import brave.Tracer;
/*
import com.google.gson.JsonObject;

import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
*/
import brave.Tracing;
import brave.opentracing.BraveTracer;
import brave.sampler.Sampler;
import io.opentracing.propagation.Format;
import zipkin.Span;
import zipkin.reporter.AsyncReporter;
import zipkin.reporter.Encoding;
import zipkin.reporter.okhttp3.OkHttpSender;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication

public class CodestateService {

    public static void main(String[] args) {
        SpringApplication.run(CodestateService.class, args);

    }
    @Bean
	public io.opentracing.Tracer zipkinTracer() {
		OkHttpSender okHttpSender = OkHttpSender.builder()
				.encoding(Encoding.JSON)
				.endpoint("http://localhost:9411/api/v1/spans")
				.build();
		AsyncReporter<Span> reporter = AsyncReporter.builder(okHttpSender).build();
		Tracing braveTracer = Tracing.newBuilder()
				.localServiceName("spring-boot")
				.reporter(reporter)
				.traceId128Bit(true)
				.sampler(Sampler.ALWAYS_SAMPLE)
				.build();
		return BraveTracer.create(braveTracer);
	}

}
@RestController
class CodeStateResource{
	private final OkHttpClient client = new OkHttpClient();
	public static final String serverUrlLocal = "http://localhost:9090";
    //public static final String serverUrl = "http://dataservice.default.svc.cluster.local";
    //public static final String serverUrl = "http://dataservice-k8sdemo.172.17.205.46.nip.io";
	 public String requestProcessedData(String url){
	        RestTemplate request = new RestTemplate();
	        String result = request.getForObject(url,String.class);
	        System.out.print(url);
	        return (result);
	    }
	    @GetMapping("/")
	    public String Hello(){
	        return "I'M YOUR CONVERTOR";
	    }

	    @GetMapping("/codeToState")
	    public String CodeToState(@RequestParam("code") String code){
	        String state = null;
	        String serverUrl = System.getenv().getOrDefault("DATA_SERVICE_URL", serverUrlLocal);
	        try {
	        	
	            String response = requestProcessedData(serverUrl+"/readDataForCode");
	            JSONObject jsonObject = new JSONObject(response);
	            state = jsonObject.getString(code.toUpperCase());
	        } catch (Exception e) {
	            System.out.println("[ERROR] : [CUSTOM_LOG] : " + e);
	        }

	        if(state == null){
	            state = "No Match Found";
	        }
	        return state;
	    }
	    @GetMapping("/stateToCode")
	    public String StateToCode(@RequestParam("state") String state){
	        String value = "";
	        String serverUrl = System.getenv().getOrDefault("DATA_SERVICE_URL", serverUrlLocal);
	        try {
	        	
	            //String response = requestProcessedData(serverUrl+"/readDataForState");
	        	String response = makeRequest(serverUrl+"/readDataForState");
	            JSONArray jsonArray = new JSONArray(response);

	            for(int n = 0; n < jsonArray.length(); n++)
	            {
	                JSONObject object = jsonArray.getJSONObject(n);
	                String name = object.getString("name");
	                if(state.equalsIgnoreCase(name)){
	                    value = object.getString("abbreviation");
	                    break;
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            System.out.println("[ERROR] : [CUSTOM_LOG] : " + e);
	        }
	        if(value == null){
	            value = "No Match Found";
	        }
	        return value;
	    }
	    @GetMapping("/test")
	    public String TestBkEnd(){
	        String serverUrl = System.getenv().getOrDefault("DATA_SERVICE_URL", serverUrlLocal);
	        String response = serverUrl;
	        try {
	            response = requestProcessedData(serverUrl + "/backend");
	        }
	        catch(Exception ex){
	        }
	        return response;
	    }
	    
	    //Scenario-1 creating a span in starting method and no corresponding span in called method 
	    @GetMapping("/test1")
	    public String TestBkEnd1() throws Exception{
	    	
	    	
	        String serverUrl = System.getenv().getOrDefault("DATA_SERVICE_URL", serverUrlLocal);
	        String response = serverUrl;
	        try{
	            response = requestProcessedData(serverUrl + "/backend1");
	        }
	        catch(Exception ex){
	        }
	        return response;
	    }
	  //Scenario-2 creating a span in starting method and no corresponding span in called method 
	    @GetMapping("/test2")
	    public String TestBkEnd2() throws Exception{
	    	
	    	String serverUrl = System.getenv().getOrDefault("DATA_SERVICE_URL", serverUrlLocal);
	        String response = serverUrl;
	        try{
	            response = makeRequest(serverUrl + "/backend2");
	        }
	        catch(Exception ex){
	        }
	        return response;
	    }
	  
	    //Scenario-3 creating a span in starting method and corresponding span in called method 
	    @GetMapping("/test3")
	    public String TestBkEnd3() throws Exception{
	    	
            String serverUrl = System.getenv().getOrDefault("DATA_SERVICE_URL", serverUrlLocal);
	        String response = serverUrl;
	        try{
	            response = makeRequest(serverUrl + "/backend3");
	        }
	        catch(Exception ex){
	        }
	        return response;
	    }
	    private String makeRequest(String url) throws IOException {
	        Request.Builder requestBuilder = new Request.Builder()
	                .url(url);
	        Request request = requestBuilder
	                .build();

	        try (Response response = client.newCall(request).execute()) {
	            return response.body().string();
	        }
	    }
}
