package edu.cmu.tetrad.util;


import java.util.*;

/**
 * Logger configuration.
 *
 * @author Tyler Gibson
 * @version $Revision: 1.1 $ $Date: Mar 20, 2007 10:54:47 PM $
 */
public class DefaultTetradLoggerConfig implements TetradLoggerConfig {


    /**
     * The events that are supported.
     */
    private List<Event> events;


    /**
     * The event ids that are currently active.
     */
    private Set<String> active = new HashSet<String>();


    /**
     * Constructs the config given the events in it.
     *
     * @param events
     */
    public DefaultTetradLoggerConfig(List<Event> events) {
        if (events == null) {
            throw new NullPointerException("The given list of events must not be null");
        }
        this.events = new ArrayList<Event>(events);
    }


    /**
     * Constructs the config for the given event ids. This will create <code>Event</code>s with
     * no descriptions.
     *
     * @param events
     */
    public DefaultTetradLoggerConfig(String... events) {
        this.events = new ArrayList<Event>(events.length);
        for (String event : events) {
           this.events.add(new DefaultEvent(event, "No Description"));
        }
    }

    //=========================== public methods ================================//

    @Override
	public boolean isEventActive(String id) {
        return this.active.contains(id);
    }

    @Override
	public boolean isActive() {
        return !this.active.isEmpty();
    }

    @Override
	public List<Event> getSupportedEvents() {
        return Collections.unmodifiableList(this.events);
    }

    @Override
	public void setEventActive(String id, boolean active) {
        if (!contains(id)) {
            throw new IllegalArgumentException("There is no event known under the given id.");
        }
        if (active) {
            this.active.add(id);
        } else {
            this.active.remove(id);
        }
    }

    //======================= Private Methods ==================================//

    private boolean contains(String id) {
        for (Event event : this.events) {
            if (id.equals(event.getId())) {
                return true;
            }
        }
        return false;
    }

    //================================= Inner class ==================================//

    public static class DefaultEvent implements TetradLoggerConfig.Event {


        private String id;
        private String description;


        public DefaultEvent(String id, String description) {
            if (id == null) {
                throw new NullPointerException("The given id must not be null");
            }
            if (description == null) {
                throw new NullPointerException("The given description must not be null");
            }
            this.id = id;
            this.description = description;
        }


        @Override
		public String getId() {
            return this.id;
        }

        @Override
		public String getDescription() {
            return this.description;
        }
    }

}
