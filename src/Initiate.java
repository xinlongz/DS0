import java.io.*;

public class Initiate {

	/* Start point of the lab */
	public static void main(String args[]) {
		if (args.length != 2) {
			System.out
					.println("Usage: $java lab0 <conf_filename> <local_name>");
			System.exit(1);
		}
		/*
		 * Initiate Parsing object 
		 * NOTE: If we can club Parsing.java and
		 * MessagePasser.java, that would be better I guess. This lets us create
		 * just one instance of MessagePasser.java and pass on to sender and
		 * receiver to use all resources
		 */
		//Parsing parse = new Parsing(args[0], args[1]);
		//parse.parseConfigurationFile(args[1]); // 1. Parse the yaml file
		
		/* If we combine Parsing.java with MessagePasser.java, we can create and use only the MessagePasser and ignore Parsing.java */
		MessagePasser mp = new MessagePasser(args[0], args[1]);
		
		//Sender send = new Sender(parse); // 2. Sender thread. You are writing this. I dunno what parameters it takes
		//send.start();
		Receiver recv = new Receiver(args[1], mp); // 3. Receiver thread
		recv.start();
		
		/*
		 * Take input from the user: 
		 * 1) For exiting- input exit or quit 
		 * 2) Send <dest> <kind> 
		 * 3) Receive
		 */
		BufferedReader br = null;
		String inputArg,params,inputData = null;
		
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				
				/* Take input argument(send or receive or exit/quit) from user */
				System.out.println("Enter the option: send/receive/exit");
				inputArg = br.readLine();

					if (inputArg.equals("send")){
						System.out.println("Enter the <Destination> <kind>");
						params = br.readLine();
						String[] temp = params.split(" ");
						inputData = br.readLine(); // Take the data from user
						mp.send(new Message(temp[0], temp[1], inputData)); // This is where we call the send() function
					}
					if (inputArg.equals("receive")) {
						
						Message msg = mp.receive(); // This is where we call the receive() function
						
						if (msg == null)
							System.out.println("No new message.");
						else {
							System.out.println("Message received: "+(String) msg.getPayload());
							
						}
					}

					if (inputArg.equals("quit") || inputArg.equals("exit"))
						System.exit(1);
				
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}

