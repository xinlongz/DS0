import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.yaml.snakeyaml.Yaml;


public class MessagePasser {
	private String configurationFilePath;
	private String localName;
	private int sequenceNum;
	private int port;
	private ServerSocket serverSocket;
	//last modification time of configuration
	private long modifiedTime;	
	
	
	
	private static HashMap<String, User> processes = new HashMap<String, User>();
	private static ArrayList<Rule> sendRules = new ArrayList<Rule>();
	private static ArrayList<Rule> receiveRules = new ArrayList<Rule>();
	
	private ConcurrentLinkedQueue<Message> delayed_send_buffer = new ConcurrentLinkedQueue<Message>();
	//record existed sockets
	private HashMap<String, Socket> mapSocket = new HashMap<String, Socket>();
	
	/* Resources for Receiver thread */
	public ConcurrentLinkedQueue<Message> rcv_buffer = new ConcurrentLinkedQueue<Message>(); //Store received messages
	public ConcurrentLinkedQueue<Message> delayed_buffer = new ConcurrentLinkedQueue<Message>(); // Store delayed messages
	
	public ConcurrentLinkedQueue<Message> getReceiveBufferQueue(){
		return rcv_buffer;
	}
	
    public ConcurrentLinkedQueue<Message> getDelayedBufferQueue(){
    	return delayed_buffer;
    }
    
	public MessagePasser(String configurationFilePath, String localName) {
		this.configurationFilePath = configurationFilePath;
		File configuration = new File(configurationFilePath);
		if(!configuration.exists() || configuration.isDirectory()) {
			System.err.println("Path for configuration file is not correct!");
		}
		parseConfigurationFile(configurationFilePath);
		this.localName = localName;
		sequenceNum = -1;
		port = processes.get(this.localName).getPort();
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ServerThread server = new ServerThread();
		
	}
	//
	class ServerThread extends Thread {
		public void run() {
			if(serverSocket == null)
				return;
			while(true) {
				Socket socket = null;
				try {
					socket = serverSocket.accept();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Receiver1 receiver = new Receiver1(socket);
				receiver.start();
			}
			
			
		}
	}
	
	class Receiver1 extends Thread {
		private Socket socket;
		public Receiver1(Socket socket) {
			this.socket = socket;
		}
		public void run() {
			while(true) {
				ObjectInputStream objInpStr = null;
				try {
					objInpStr = new ObjectInputStream(
							socket.getInputStream());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Message receiveMesg = null;
				try {
					receiveMesg = (Message) objInpStr.readObject();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(receiveMesg == null)
					continue;
				String action = RuleChecking(receiveMesg, 1);
				if (action != null) {
					
					// DUPLICATE
					if (action.equals("duplicate")) {
						rcv_buffer.add(receiveMesg);
						
						/* Not sure if the following is correct or not: adding the Message from delayed_buffer into rcv_buffer */
						synchronized (delayed_buffer) {
							while (!delayed_buffer.isEmpty()) {
								rcv_buffer.add(delayed_buffer.poll());
							}
						}
						/* TODO
						 * Copy the message and add to the rcv_buffer here 
						 * */
						
					} else if (action.equals("delay")) { //DELAY
						delayed_buffer.add(receiveMesg);
					} else if (action.equals("drop")) //DROP
						;
				} else {
					// Default: "action" doesnt contain anything
					rcv_buffer.add(receiveMesg);
					synchronized (delayed_buffer) {
						while (!delayed_buffer.isEmpty()) {
							rcv_buffer.add(delayed_buffer.poll());
						}
					}
				}
			}
		}
	}
	
	
	
	public void send(Message message) {
		if(message == null)
			return;
		updateRules(configurationFilePath);
		if(mapSocket.get(message.getDestination()) == null) {
			User dest = processes.get(message.getDestination());
			Socket socket = null;
			try {
				socket = new Socket(dest.getIp(), dest.getPort());
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mapSocket.put(message.getDestination(), socket);
			
		}
		++sequenceNum;
		message.setId(sequenceNum);
		message.setSource(this.localName);
		String action = RuleChecking(message, 0);
		if(action == null) {
			sendOneMesg(message, mapSocket.get(message.getDestination()));
			//send all delayed message
			while(delayed_send_buffer.size() != 0) {
				Message delayedMsg = delayed_send_buffer.poll();
				sendOneMesg(delayedMsg, mapSocket.get(delayedMsg.getDestination()));
			}
		}
		else {
			if(action.equals("drop"))
				return;
			else if(action.equals("delay")) {
				delayed_send_buffer.add(message);
			}
			else if(action.endsWith("duplicate")) {
				Message dup = null;
				try {
					dup = message.clone();
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				message.setDupFlag(false);
				dup.setDupFlag(true);
				sendOneMesg(message, mapSocket.get(message.getDestination()));
				sendOneMesg(dup, mapSocket.get(dup.getDestination()));
			}
		}
		
		
		
	}
	
	public static void sendOneMesg(Message message, Socket socket) {
		OutputStream outputStream = null;
		ObjectOutputStream objectOutputStream = null;
		
		try {
			outputStream = socket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			objectOutputStream = new ObjectOutputStream(outputStream);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			objectOutputStream.writeObject(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* Here is the receive() function */
	public Message receive() {
		Message msg = rcv_buffer.poll(); // if ConcurrentLinkedQueue is empty, cq.poll return null; cq.poll() is atomic operation
        return msg;
	}
	
	public void updateRules(String configurationFilePath) {
		File file = new File(configurationFilePath);
		if(file.lastModified() == modifiedTime)
			return;
		modifiedTime = file.lastModified();
		processes.clear();
		sendRules.clear();
		receiveRules.clear();
		parseConfigurationFile(configurationFilePath);
	}
	
	//parse the configuration file to get the list of process, receive rfileules and send rules
    public void parseConfigurationFile(String configurationFilePath) {
		
		/* Parsing code begins here*/
		FileInputStream f = null;
        try
        {
                f = new FileInputStream("conf/config.yaml");
                Yaml yaml = new Yaml();
                Map<String, Object> data = (Map<String, Object>)yaml.load(f);
                
                ArrayList<HashMap<String, Object> > conf = (ArrayList<HashMap<String, Object> >)data.get("Configuration");
                System.out.println("--Names--");
                for(HashMap<String, Object> usr : conf)
                {
                        User u = new User((String)usr.get("Name"),(String)usr.get("IP"),(Integer)usr.get("Port") );
                        System.out.println(u.getName());
                        processes.put((String)usr.get("Name"), u);
                }
                
                
                ArrayList<HashMap<String, Object> > parsedSendRules = (ArrayList<HashMap<String, Object> >)data.get("SendRules");
                System.out.println("--SendRules--");
                for(HashMap<String, Object> rule : parsedSendRules)
                {
                		System.out.println((String)rule.get("Action"));
                        Rule rl = new Rule((String)rule.get("Action"));
                        for(String key: rule.keySet())
                        {
                                if(key.equals("Src"))
                                        rl.setSource((String)rule.get(key));
                                if(key.equals("Dest"))
                                        rl.setDestination((String)rule.get(key));
                                if(key.equals("Kind"))
                                        rl.setKind((String)rule.get(key));
                                if(key.equals("ID"))
                                        rl.setId((Integer)rule.get(key));
                                if(key.equals("Nth"))
                                        rl.setNth((Integer)rule.get(key));
                                if(key.equals("EveryNth"))
                                        rl.setEveryNth((Integer)rule.get(key));
                        }
                        sendRules.add(rl);
                }

                
                ArrayList<HashMap<String, Object> > parsedReceiveRules = (ArrayList<HashMap<String, Object> >)data.get("ReceiveRules");
                System.out.println("--ReceiveRules--");
                for(HashMap<String, Object> rule : parsedReceiveRules)
                {
                		System.out.println((String)rule.get("Action"));
                        Rule rl = new Rule((String)rule.get("Action"));
                        for(String key: rule.keySet())
                        {
                                if(key.equals("Src"))
                                        rl.setSource((String)rule.get(key));
                                if(key.equals("Dest"))
                                        rl.setDestination((String)rule.get(key));
                                if(key.equals("Kind"))
                                        rl.setKind((String)rule.get(key));
                                if(key.equals("ID"))
                                        rl.setId((Integer)rule.get(key));
                                if(key.equals("Nth"))
                                        rl.setNth((Integer)rule.get(key));
                                if(key.equals("EveryNth"))
                                        rl.setEveryNth((Integer)rule.get(key));
                        }
                        receiveRules.add(rl);
                }
               
        }
        catch(Exception e)
        {
                e.printStackTrace();
        }
        finally
        {
                try
                {
                        if(f != null)
                        	f.close();
                }
                catch(IOException ioe)
                {
                        ioe.printStackTrace();
                }
        }
	}
	
	/** Checking whether or not a message matches a rule or not. 
    @param sendOrReceive: 1 -> check message with ReceiveRules, 0 -> check message with SendRules
    @return: The matched rule object is returned so that "Action name" can be used to decide what to do in the Send() function of MessagePasser
*/
	public String RuleChecking(Message message, int sendOrReceive)
   {
           ArrayList<Rule> rl = null;
           if(sendOrReceive == 0)
                   rl = sendRules;
           else if(sendOrReceive == 1)
                   rl = receiveRules;
           else
           {
                   System.err.println("Wrong rule type given: " + sendOrReceive);
                   System.exit(1);
           }
           for(Rule rule: rl)
           {
                   if((rule.getSource() != null) && !(rule.getSource().equals(message.getSource()  )))
                           continue;//not match, check next rule
                   else if((rule.getDestination() != null) && !(rule.getDestination().equals(message.getDestination())))
                           continue;
                   else if((rule.getKind() != null) && !(rule.getKind().equals(message.getKind())))
                           continue;
                   /*
                   else if( (rule.getId() > 0) && (rule.getId() != message.getId()) )
                           continue;
                   */
                   rule.setComparison();
                   if((rule.getNth() > 0) && (rule.getNth() != rule.getComparison()) )
                           continue;
                   else if( (rule.getEveryNth() > 0) && (rule.getComparison() % rule.getEveryNth()) != 0)
                           continue;
                   /* Rule matched, so return it*/
                   return rule.getAction() ;
           }
           /* If rule not matched, return null */
           return null; 
   }
	
	public HashMap<String, User> getProcesses() {
		return processes;
	}
	public ArrayList<Rule> getSendRules() {
		return sendRules;
	}
	public ArrayList<Rule> getReceiveRules() {
		return receiveRules;
	}
	
	
}

	
