import java.io.File;

public class ConfigFileHandler extends Thread {
	private final String configFile;
    private final String process;

    public ConfigFileHandler(String configFile, String process)
    {
            this.configFile = configFile;
            this.process = process;
    }

    public void run()
    {
            File file = new File(configFile);
            long initialTime = file.lastModified();
            
            while(true)
            {
                    long nowModified = file.lastModified();
                    if(nowModified != initialTime)
                    {
                    	initialTime = nowModified;
                    	
                    	/** I think we need to stop the current Receiver1 thread also . Something like this:
                    	Code in this java file:
                    	receiver.setFlag();
                    	receiver.interrupt();
                    	
                    	Code in Receiver1 class inside MessagePasser.java:
                    	public void setFlag(){flag = false;}
                    	
                    	and
                    	
                    	Inside that infinite loop: 
                    	while(true) {
                    		 if(!flag)
                             	break;
                    	}
                    	
                    	Please check this add the changes
                    	
                    	*/
                    	
                    	/* Read the config file again, start the server & receiver threads */
						MessagePasser m = new MessagePasser(configFile, process);
						/** This object "m" has to be passed into Initiate.java to get used */
						
                    }
                    else
                    {
                            Thread.yield();
                    }
            }
    }

}
