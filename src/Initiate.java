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
		 * Initiate Parsing object NOTE: If we can club Parsing.java and
		 * MessagePasser.java, that would be better I guess. This lets us create
		 * just one instance of MessagePasser.java and pass on to sender and
		 * receiver to use all resources
		 */
		Parsing parse = new Parsing(args[0], args[1]);
		parse.parseConfigurationFile(args[1]); // 1. Parse the yaml file
		Sender send = new Sender(parse); // 2. Sender thread. You need to write
											// this.
		send.start();
		Receiver recv = new Receiver(parse, args[1]); // 3. Receiver thread
		recv.start();

		/*
		 * Take input from the user: 1) For exiting- input exit or quit 2) Send
		 * <dest> <kind> 3) Receive
		 */
		String input = null;
		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			while (true) {
				/* get user input */
				input = br.readLine();

				if (input.equals("quit") || input.equals("exit"))
					System.exit(1);

				if ((input = parseInput(input)) != null) {
					if (input.equals("send")){
						
					}
					if (input.equals("receive")) {
						
						Message mm = mp.receive(); // This is where we call the receive function
						
						if (mm == null)
							System.out.println("No new message.");
						else {
							System.out.println("Received message ("
									+ mm.getSrc() + "," + mm.getId()
									+ ") from " + mm.getSrc() + " to "
									+ mm.getDest() + ".|Kind: " + mm.getKind()
									+ "|Content: " + (String) mm.getData());
							System.out.println(mp.getReceiveQueue().size()
									+ " more message(s)");
						}
					} else {
						String[] temp = input.split(" ");
						System.out.print("Input message: ");
						input = br.readLine();
						mp.send(new Message(args[1], temp[1], temp[2], input));
					}
				}
				
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
