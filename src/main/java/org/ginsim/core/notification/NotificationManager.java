package org.ginsim.core.notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.ginsim.core.graph.Graph;
import org.ginsim.core.notification.detailed.DetailedErrorNotification;
import org.ginsim.core.notification.detailed.DetailedInformationNotification;
import org.ginsim.core.notification.detailed.DetailedWarningNotification;
import org.ginsim.core.notification.resolvable.NotificationResolution;
import org.ginsim.core.notification.resolvable.ResolvableErrorNotification;
import org.ginsim.core.notification.resolvable.ResolvableWarningNotification;

/**
 * This class is the central class of the register/publish design pattern applied to Notifications
 * This manager receive registration from object that have to receive Notification on specific topics
 * It receive also all the Notification and distribute them to listening objects according to the Notification Topic 
 * 
 * @author Lionel Spinelli
 *
 */


public class NotificationManager {

	// The static instance of the manager
	private static NotificationManager instance;
	
	// List of the registered listeners
	private List<TopicsListener> notificationListerners;
	// List of the memorized notifications (boolean is the publich/delete mode)
	private HashMap<Notification, Boolean> memorizedNotifications;
	
	/**
	 * Default constructor
	 */
	public NotificationManager(){
		
		notificationListerners = new ArrayList<TopicsListener>();
		memorizedNotifications = new HashMap<Notification, Boolean>();
	}
	
	/**
	 * Give access to the manager instance
	 * 
	 * @return the manager instance
	 */
	public static NotificationManager getManager(){
		
		if( instance == null){
			instance = new NotificationManager();
		}
		
		return instance;
	}
	
	
	/**
	 * Register a listener and subscribe it to the given topic
	 * 
	 * @param listener the NotificationListerner to register
	 * @param topic the topic the listener subscribe to
	 */
	public void registerListener( NotificationListener listener, Object topic){
				
		TopicsListener topic_listener = null;
		synchronized( notificationListerners){
			
			// Check if the listener already exists
			for( TopicsListener current_topic_listener : notificationListerners){
				if( current_topic_listener.getListener() == listener){
					topic_listener = current_topic_listener;
					topic_listener.addTopic( topic);
					break;
				}
			}
			
			// If the listener does not exist create it and add it the topic
			if( topic_listener == null){
				topic_listener = new TopicsListener( listener);
				topic_listener.addTopic( topic);
				notificationListerners.add( topic_listener);
			}
		}
		publishMemorizedNotifications( topic_listener);
	}
	
	/**
	 * Publish an error message
	 * 
	 * @param topic the topic of the notification
	 * @param message the message of the notification
	 */
	public static void publishError( Object topic, String message){
		
		if( topic != null && message != null){
			getManager().publish( new ErrorNotification( topic, message));
		}
	}
	
	/**
	 * Publish an error message with details
	 * 
	 * @param topic the topic of the notification
	 * @param message the message of the notification
	 * @param details the details associated to the notification
	 */
	public static void publishDetailedError( Object topic, String message, String details){
		
		if( topic != null && message != null){
			getManager().publish( new DetailedErrorNotification( topic, message, details));
		}
	}
	
	/**
	 * Publish a Java exception 
	 * 
	 * @param topic the topic of the notification
	 * @param message the message of the notification
	 * @param exception the Exception to publish
	 */
	public static void publishException( Object topic, String message, Exception exception){
		
		if( topic != null && message != null){
			getManager().publish( new ExceptionNotification( topic, message, exception));
		}
	}
	
	/**
	 * Publish a warning message
	 * 
	 * @param topic the topic of the notification
	 * @param message the message of the notification
	 */
	public static void publishWarning( Object topic, String message){
		
		if( topic != null && message != null){
			getManager().publish( new WarningNotification( topic, message));
		}
	}
	
	/**
	 * Publish an warning message with details
	 * 
	 * @param topic the topic of the notification
	 * @param message the message of the notification
	 * @param details the details associated to the notification
	 */
	public static void publishDetailedWarning( Object topic, String message, String details){
		
		if( topic != null && message != null){
			getManager().publish( new DetailedWarningNotification( topic, message, details));
		}
	}
	
	/**
	 * Publish an information message
	 * 
	 * @param topic the topic of the notification
	 * @param message the message of the notification
	 */
	public static void publishInformation( Object topic, String message){
		
		if( topic != null && message != null){
			getManager().publish( new InformationNotification( topic, message));
		}
	}
	
	/**
	 * Publish an information message with details
	 * 
	 * @param topic the topic of the notification
	 * @param message the message of the notification
	 * @param details the details associated to the notification
	 */
	public static void publishDetailedInformation( Object topic, String message, String details){
		
		if( topic != null && message != null){
			getManager().publish( new DetailedInformationNotification( topic, message, details));
		}
	}
	
	/**
	 * Publish an error message with its resolution options 
	 * 
	 * @param topic the topic of the notification
	 * @param message the message of the notification
	 * @param graph the graph concerned by the notification
	 * @param data the data required for the 
	 * @param resolution the NotificationResolution containing the resolution options
	 */
	public static void publishResolvableError( Object topic, String message, Graph graph, Object[] data, NotificationResolution resolution){
		
		if( topic != null && message != null){
			getManager().publish( new ResolvableErrorNotification( topic, message, graph, data, resolution));
		}
	}
	
	
	/**
	 * Publish a warning message with its resolution options 
	 * 
	 * @param topic the topic of the notification
	 * @param message the message of the notification
	 * @param graph the graph concerned by the notification
	 * @param data the data required for the 
	 * @param resolution the NotificationResolution containing the resolution options
	 */
	public static void publishResolvableWarning( Object topic, String message, Graph graph, Object[] data, NotificationResolution resolution){
		
		if( topic != null && message != null){
			getManager().publish( new ResolvableWarningNotification( topic, message, graph, data, resolution));
		}
	}
	
	
	/**
	 * Publish a notification so NotificationListerner that were registered and
	 * have subscribe to the type of the given notification will add it to their notification list
	 * 
	 * @param message the Notification to publish
	 */
	private void publish( Notification message){
		
		publish( message, false);
	}
	
	/**
	 * Publish a notification so NotificationListerner that were registered and
	 * have subscribe to the type of the given notification will remove it from their notification list
	 * (deletion is due to notification timeout)
	 * 
	 * @param message the Notification to remove
	 */
	public void publishDeletion( Notification message){
		
		// If the message to remove has been memorized, simply remove it from the list
		synchronized ( memorizedNotifications) {
			if( memorizedNotifications.containsKey( message)){
				memorizedNotifications.remove( message);
				return;
			}
		}
		
		// If the message was not memorized, publish the deletion request
		getManager().publish( message, true);
	}
	
	/**
	 * Publish a notification, sending it to the NotificationListerner that were registered and
	 * have subscribe to the type of the given notification
	 * Id the deletion boolean is false, the NotificationListerner receive the order to add the notification
	 * to its list
	 * If the deletion boolean is true, the NotificationListerner receive the order to remove the notification
	 * from its list
	 * 
	 * @param message the Notification to publish
	 * @param deletion true if the notification must be removed from Notification lists, false if it must be added 
	 */
	private void publish( Notification message, boolean deletion){
		
		synchronized( notificationListerners){
			// Parse the registered listeners
			boolean managed= false;
			for( TopicsListener topic_listener : notificationListerners){

				// Test if the Notification topic is part of the listener topics
				List<Object> topics_listened = topic_listener.getTopics();
				if( topics_listened.contains( message.getTopic())){
					if( !deletion){
						topic_listener.getListener().receiveNotification( message);
					}
					else{
						topic_listener.getListener().deleteNotification( message);
					}
					managed = true;
				}
			}
			// If no listener matched the topic, the Notification is memorized
			if( !managed){
				synchronized( memorizedNotifications){
					memorizedNotifications.put( message, deletion);
				}
			}
		}

	}


	/**
	 * Try to publish the memorized notifications to the given listener
	 * 
	 * @param topic_listener the listener to which the memorized notification are sent if required
	 */
	private void publishMemorizedNotifications( TopicsListener topic_listener){
		
		synchronized( memorizedNotifications){
			List<Object> topics_listened = topic_listener.getTopics();
			// Parse the memorized Notifications
			for( Iterator<Notification> message_ite = memorizedNotifications.keySet().iterator(); message_ite.hasNext();){
				Notification message = message_ite.next();
				// if the Notification concerns a topic listened by the listener, publish it 
				if( topics_listened.contains( message.getTopic())){
					boolean deletion = memorizedNotifications.get( message);
					if( !deletion){
						topic_listener.getListener().receiveNotification( message);
					}
					else{
						topic_listener.getListener().deleteNotification( message);
					}
					message_ite.remove();
				}
			}
		}
	}
	
/**
 * 
 * 
 *
 */
private class TopicsListener{
	
	private NotificationListener listener;
	private List<Object> topics;
	
	public TopicsListener( NotificationListener listener){
		
		this.listener = listener;
		topics = new ArrayList<Object>();
	}
	
	/**
	 * Add a topic to the list of topics the listener has subscribed to 
	 * 
	 * @param topic the topic to listen (class inheriting from Notification
	 */
	public void addTopic( Object topic){
		
		if( topic != null && !topics.contains( topic)){
			topics.add( topic);
		}
	}
	
	/**
	 * Give access to the NotificationListener
	 * 
	 * @return the NotificationListener
	 */
	public NotificationListener getListener() {
		
		return listener;
	}
	
	/**
	 * Give access to the list of topics the listener has subscribed to 
	 * 
	 * @return the list of topics the listener has subscribed to 
	 */
	public List<Object> getTopics() {
		
		return topics;
	}
	
	@Override
	public String toString() {
		
		return "Notificationlistener : " + listener + "->" + topics;
	}
}

}
