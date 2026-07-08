package org.ginsim.core.notification;

import org.ginsim.common.callable.Timeout;
import org.ginsim.common.callable.TimeoutObject;


/**
 * A notification message
 *
 */
public abstract class Notification implements TimeoutObject, Comparable {

	protected Object topic;
	private String message;
    private NotificationType type;

    /**
     * 
     * @param topic the Graph the notification is associated to
     * @param message the message of the notification
     * @param type the type of the notification. See constants on Notification class.
     */
    public Notification ( Object topic, String message, NotificationType type) {
    	
    	this.topic = topic;
		this.message = message;
        this.type = type;
        
        if (type == null) {
        	this.type = NotificationType.ERROR;
        }
        
        if (this.type.timeout > 0) {
            Timeout.addTimeout( this, this.type.timeout*1000);
        }
    }

    /**
     * Return the topic the notification is related to 
     * 
     * @return the topic the notification is related to 
     */
    public Object getTopic(){
    	
    	return this.topic;
    }

    /**
     * Get the message describing this notification.
     * @return the message describing this notification
     */
    public String getMessage() {
		return message;
	}

    @Override
    public void timeout() {
    	NotificationManager.getManager().publishDeletion( this);
    }

    @Override
	public String toString() {
		return message;
	}
    
    /**
     * @return the type of this notification.
     */
    public NotificationType getType() {
        return type;
    }
    
    /**
     * Compare the notification to the given object. If this object is a Notification, the comparison is made on the type 
     * 
     * @param obj the object to compare
     * @return a negative value if the given notification is less important
     * 		   a positive value if the given notification is more important
     *         a zero if the two notification have the same importance or the given object is not a Notification
     */
    @Override
    public int compareTo( Object obj) {
    	
    	if( obj instanceof Notification){
    		return ((Notification) obj).getType().compareTo(this.type);
    	}
    	return 0;
    }
   
}
