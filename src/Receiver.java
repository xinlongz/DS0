import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/* Receive Messages from other processes. 
 * Storing these messages in a rcv_buffer  
 */
/**
 * This is invoked by:
 * Receiver r = new Receiver(parameter 1, parameter 2);
 * Parameter 1: Object of Parsing class or object of MessagePasser class- wherever we can get the process list and call RuleCheck from
 * Parameter 2: Second argument(arg[1]) given on command line
 * 
 * Then, call r.start();  to trigger the Receiver thread
 * */
public class Receiver extends Thread {

	private Parsing mp;
	private String process;
	private MessagePasser msgPass;

	public Receiver(Parsing mp, String process, MessagePasser msgPass) {
		this.mp = mp;
		this.process = process;
		this.msgPass = msgPass;
	}

	public void run() {

		try {
			
			ConcurrentLinkedQueue<Message> rcv_buffer = msgPass.getReceiveBufferQueue();
			ConcurrentLinkedQueue<Message> delayed_buffer = msgPass.getDelayedBufferQueue();
					
			/* Open the server socket on the specific port of the input process */
			ServerSocket servSoc = new ServerSocket(mp.getAllProcesses()
					.get(process).getPort());
			
			while (true) {
				Socket soc = servSoc.accept();
				/* Read the Messages sent by sender */
				ObjectInputStream objInpStr = new ObjectInputStream(
						soc.getInputStream());
				
				/* Typecast the Message read */
				Message message = (Message) objInpStr.readObject();

				/*
				 * Check the message according to ReceiveRules. Here "1" implies
				 * Receiver
				 */
				String action = mp.RuleChecking(message, 1);
				if (action != null) {
					
					// DUPLICATE
					if (action.equals("duplicate")) {
						rcv_buffer.add(message);
						
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
						delayed_buffer.add(message);
					} else if (action.equals("drop")) //DROP
						;
				} else {
					// Default: "action" doesnt contain anything
					rcv_buffer.add(message);
					synchronized (delayed_buffer) {
						while (!delayed_buffer.isEmpty()) {
							rcv_buffer.add(delayed_buffer.poll());
						}
					}
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

}

