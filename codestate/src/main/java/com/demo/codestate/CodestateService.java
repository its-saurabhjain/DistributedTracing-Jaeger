package com.demo.codestate;
//
import java.util.Map;
//
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonObject;

import io.jaegertracing.Configuration;
import io.jaegertracing.Configuration.ReporterConfiguration;
import io.jaegertracing.Configuration.SamplerConfiguration;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;

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
    public io.opentracing.Tracer tracer() {
    	SamplerConfiguration samplerConfig = SamplerConfiguration.fromEnv()
                .withType(ConstSampler.TYPE)
                .withParam(1);
    	ReporterConfiguration reporterConfig = ReporterConfiguration.fromEnv()
                .withLogSpans(true);
    	Configuration config = new Configuration("codestate-svc")
                .withSampler(samplerConfig)
                .withReporter(reporterConfig);

        return config.getTracer();
    }
}
@RestController
class CodeStateResource{
	private final OkHttpClient client = new OkHttpClient();
	@Autowired
    private Tracer tracer;
	
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
	        try (Scope scope= tracer.buildSpan("CodeToState").startActive(true)){
	        	scope.span().setTag("SCV", "Front End");
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
	        try (Scope scope= tracer.buildSpan("StateToCode").startActive(true)){
	        	scope.span().setTag("SCV", "Front End");
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
	    	
	    	Scope span = tracer.buildSpan("test1").startActive(true);
	    	Span TestBkEnd1 = tracer.buildSpan("backend1").asChildOf(span.span()).start();
	        String serverUrl = System.getenv().getOrDefault("DATA_SERVICE_URL", serverUrlLocal);
	        String response = serverUrl;
	        try{
	            response = requestProcessedData(serverUrl + "/backend1");
	        }
	        catch(Exception ex){
	        }
	        TestBkEnd1.finish();
	        span.close();
	        return response;
	    }
	  //Scenario-2 creating a span in starting method and no corresponding span in called method 
	    @GetMapping("/test2")
	    public String TestBkEnd2() throws Exception{
	    	
	    	Scope span = tracer.buildSpan("test2").startActive(true);
	    	Span TestBkEnd2 = tracer.buildSpan("backend2").asChildOf(span.span()).start();
	        String serverUrl = System.getenv().getOrDefault("DATA_SERVICE_URL", serverUrlLocal);
	        String response = serverUrl;
	        try{
	            response = makeRequest(serverUrl + "/backend2");
	        }
	        catch(Exception ex){
	        }
	        TestBkEnd2.finish();
	        span.close();
	        return response;
	    }
	  
	    //Scenario-3 creating a span in starting method and corresponding span in called method 
	    @GetMapping("/test3")
	    public String TestBkEnd3() throws Exception{
	    	
	    	Scope span = tracer.buildSpan("test3").startActive(true);
	    	Span TestBkEnd3 = tracer.buildSpan("backend3").asChildOf(span.span()).start();
	        String serverUrl = System.getenv().getOrDefault("DATA_SERVICE_URL", serverUrlLocal);
	        String response = serverUrl;
	        try{
	            response = makeRequest(serverUrl + "/backend3");
	        }
	        catch(Exception ex){
	        }
	        TestBkEnd3.finish();
	        span.close();
	        return response;
	    }
	    private String makeRequest(String url) throws IOException {
	        Request.Builder requestBuilder = new Request.Builder()
	                .url(url);

	        tracer.inject(
	                tracer.activeSpan().context(),
	                Format.Builtin.HTTP_HEADERS,
	                new RequestBuilderCarrier(requestBuilder)

	        );

	        Request request = requestBuilder
	                .build();

	        try (Response response = client.newCall(request).execute()) {
	            return response.body().string();
	        }
	    }
}
