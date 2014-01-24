import java.util.concurrent.*;
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
                    	/** I think we need to stop the current Receiver1 thread also */
                    	
                    	/* Read the config file again, start the server & receiver threads */
                    	MessagePasser m = new MessagePasser(configFile, process);
                    }
                    else
                    {
                            Thread.yield();
                    }
            }
    }

}
