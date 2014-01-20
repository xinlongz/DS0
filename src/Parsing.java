import java.io.*;
import java.util.*;

import org.yaml.snakeyaml.Yaml;

public class Parsing {
	
	/* I'm assuming process name as the "Name" field in Configuration section of the .yaml file */
	private static HashMap<String, User> processes = new HashMap<String, User>();
	private static ArrayList<Rule> sendRules = new ArrayList<Rule>();
	private static ArrayList<Rule> receiveRules = new ArrayList<Rule>();

	
	public static void main(String args[]) {
		
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
	
	/* 1. I think this function should be in MessagePasser.java 
	 * 2. I am passing the message object into this function. Since you have written this, please call appropriate functions of the message class 
	 * */
	/** Checking whether or not a message matches a rule or not. 
    @param type: 0 - check message with SendRules; 1 - check message with ReceiveRules
    @return action name of the rule which this message matches with; or null if this message matches with no action.
    */
   public Rule RuleChecking(Message message, int sendOrReceive)
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
