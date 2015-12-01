/**
 * 
 */
package com.tweet.scanner;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;



/**
 * @author Pankaj
 * Provides the REST end points. Starts the Thread Pool and allocate Worker Thread to hash tags
 *
 */
@org.springframework.web.bind.annotation.RestController
public class RestController {

	 private static final String template = "Hello, %s!";
	    private final AtomicLong counter = new AtomicLong();
	    
	  private  ExecutorService executor = Executors.newFixedThreadPool(5);//creating a pool of 5 threads  

	    @RequestMapping("/greeting")
	    public String greeting(@RequestParam(value="name", defaultValue="World") String name) {
	    	
	    	String s=counter.incrementAndGet()+String.format(template, name);
	    	
	        return s;
	    }
	    
	    @RequestMapping("/start")
	    public String start(@RequestParam(value="name", defaultValue="World") String name) {
	    	
	    	String s="Successfully Started Thread Pool";
	    	ArrayList<String> tags=new ArrayList<String>();
	    	tags.add("#ipl");
	    	tags.add("#sjsu");
	   
	    	
	    	
	    	if(!executor.isTerminated()){
	    	try{
	    		  for (int i = 0; i < tags.size(); i++) {  
			            Runnable worker = new WorkerThread(tags.get(i));  
			            executor.execute(worker);//calling execute method of ExecutorService  
			          }  
	    		
	    	}catch(Exception e){
	    		e.printStackTrace();
	    		 s="Exception while starting Thread Pool";
	    	}
	    	
	    	}
	    	else{
	    		executor=Executors.newFixedThreadPool(5);//creating a pool of 5 threads   
	    		s="Created New Thread Pool";
	    	}
	    	
	        return s;
	    }
	    
	    @RequestMapping("/stop")
	    public String stop(@RequestParam(value="name", defaultValue="World") String name) {
	    	
	    	String s="Successfully Stopped Thread Pool";
	    	
	    	if(!executor.isTerminated()){
	    	try{
	    	executor.shutdown();  
	        while (!executor.isTerminated()) { 
	        	
	        }  
	        }catch(Exception e){
	        	e.printStackTrace();
	        	 s="Exception while stopping Thread Pool";
	        }
	    	}else{
	    		s="Already Stopped the Pool";
	    	}
	    	
	        return s;
	    }
	    
	    
}
