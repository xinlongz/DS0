import java.io.Serializable;


public class Message implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String source = null;
	private String dest;
	private String kind;
	private int sequenceNumber = -1;
	private Boolean dupFlag = false;
	private Object payload;
	public Message(String dest, String kind, Object data) {
		this.dest = dest;
		this.kind = kind;
		this.payload = data;
	}
	//duplicate
	public Message clone() throws CloneNotSupportedException {
		return (Message)this.clone();	
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	public void setDestination(String dest) {
		this.dest = dest;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	public void setPayload(String data) {
		this.payload = data;
	}
	public void setDupFlag(Boolean dupFlag) {
		this.dupFlag = dupFlag;
	}
	public void setId(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	
	public void setDuplicate(Boolean dupe) {
		this.dupFlag = dupe;
	}
	
	public String getSource() {
		return source;
	}
	public String getDestination() {
		return dest;
	}
	public String getKind() {
		return kind;
	}
	public Object getPayload() {
		return payload;
	}
	public boolean getDupFlag() {
		return dupFlag;
	}
	public int getId() {
		return sequenceNumber;
	}
	
	public String toString() {
		return sequenceNumber + " " + source + " " + dest + " " + kind + " " + dupFlag;
	}
	
}
