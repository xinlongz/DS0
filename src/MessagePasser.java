import java.io.File;
import java.util.ArrayList;


public class MessagePasser {
	private Parsing parse;
	private String configurationFilePath;
	private String localName;
	private static int sequenceNum = -1;
	
	public MessagePasser(String configurationFilePath, String localName) {
		File configuration = new File(configurationFilePath);
		if(!configuration.exists() || configuration.isDirectory()) {
			System.err.println("Path for configuration file is not correct!");
		}
		parse.parseConfigurationFile(configurationFilePath);
		this.localName = localName;
	}
	
	
	public void send(Message message) {
		++sequenceNum;
		message.setId(sequenceNum);
		message.setSource(this.localName);
		Rule rule = RuleChecking(message, 0);
		
		
		
	}
	
	/** Checking whether or not a message matches a rule or not. 
    @param sendOrReceive: 1 -> check message with ReceiveRules, 0 -> check message with SendRules
    @return: The matched rule object is returned so that "Action name" can be used to decide what to do in the Send() function of MessagePasser
*/
	public Rule RuleChecking(Message message, int sendOrReceive)
	   {
	           ArrayList<Rule> rl = null;
	           if(sendOrReceive == 0)
	                   rl = parse.getSendRules();
	           else if(sendOrReceive == 1)
	                   rl = parse.getReceiveRules();
	           else
	           {
	                   System.err.println("Wrong rule type given: " + sendOrReceive);
	                   System.exit(1);
	           }
	           for(Rule rule: rl)
	           {
	                   if((rule.getSource() != null) && !(rule.getSource().equals(message.getSource())))
	                           continue;//not match, check next rule
	                   else if((rule.getDestination() != null) && !(rule.getDestination().equals(message.getDestination())))
	                           continue;
	                   else if((rule.getKind() != null) && !(rule.getKind().equals(message.getKind())))
	                           continue;
	                   else if( (rule.getId() > 0) && (rule.getId() != message.getId()) )
	                           continue;
	                   rule.setComparison();
	                   if((rule.getNth() > 0) && (rule.getNth() != rule.getComparison()) )
	                           continue;
	                   else if( (rule.getEveryNth() > 0) && (rule.getComparison() % rule.getEveryNth()) != 0)
	                           continue;
	                   /* Rule matched, so return it */
	                   return rule;
	           }
	           /* If rule not matched, return null */
	           return null; 
	   }

}
