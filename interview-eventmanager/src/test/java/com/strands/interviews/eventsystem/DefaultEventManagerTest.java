package com.strands.interviews.eventsystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.strands.interviews.eventsystem.events.CreationEvent;
import com.strands.interviews.eventsystem.events.SimpleEvent;
import com.strands.interviews.eventsystem.events.SubEvent;
import com.strands.interviews.eventsystem.impl.DefaultEventManager;

public class DefaultEventManagerTest
{
    private EventManager eventManager = new DefaultEventManager();

    @Test
    public void testPublishNullEvent()
    {
        eventManager.publishEvent(null);
    }

    @Test
    public void testRegisterListenerAndPublishEvent()
    {
        EventListenerMock eventListenerMock = new EventListenerMock(new Class[]{SimpleEvent.class});
        eventManager.registerListener("some.key", eventListenerMock);
        eventManager.publishEvent(new SimpleEvent(this));
        assertTrue(eventListenerMock.isCalled());
    }

    @Test
    public void testListenerWithoutMatchingEventClass()
    {
        EventListenerMock eventListenerMock = new EventListenerMock(new Class[]{SubEvent.class});
        eventManager.registerListener("some.key", eventListenerMock);
        eventManager.publishEvent(new SimpleEvent(this));
        assertFalse(eventListenerMock.isCalled());
    }

    @Test
    public void testUnregisterListener()
    {
        EventListenerMock eventListenerMock = new EventListenerMock(new Class[]{SimpleEvent.class});
        EventListenerMock eventListenerMock2 = new EventListenerMock(new Class[]{SimpleEvent.class});

        eventManager.registerListener("some.key", eventListenerMock);
        eventManager.registerListener("another.key", eventListenerMock2);
        eventManager.unregisterListener("some.key");

        eventManager.publishEvent(new SimpleEvent(this));
        assertFalse(eventListenerMock.isCalled());
        assertTrue(eventListenerMock2.isCalled());
    }


    /**
     * Check that registering and unregistering listeners behaves properly.
     */
    @Test
    public void testRemoveNonexistentListener()
    {
        DefaultEventManager dem = (DefaultEventManager)eventManager;
        assertEquals(0, dem.getListeners().size());
        eventManager.registerListener("some.key", new EventListenerMock(new Class[]{SimpleEvent.class}));
        assertEquals(1, dem.getListeners().size());
        eventManager.unregisterListener("this.key.is.not.registered");
        assertEquals(1, dem.getListeners().size());
        eventManager.unregisterListener("some.key");
        assertEquals(0, dem.getListeners().size());
    }

    /**
     * Registering duplicate keys on different listeners should only fire the most recently added.
     */
    @Test
    public void testDuplicateKeysForListeners()
    {
        EventListenerMock eventListenerMock = new EventListenerMock(new Class[]{SimpleEvent.class});
        EventListenerMock eventListenerMock2 = new EventListenerMock(new Class[]{SimpleEvent.class});

        eventManager.registerListener("some.key", eventListenerMock);
        eventManager.registerListener("some.key", eventListenerMock2);

        eventManager.publishEvent(new SimpleEvent(this));

        assertTrue(eventListenerMock2.isCalled());
        assertFalse(eventListenerMock.isCalled());

        eventListenerMock.resetCalled();
        eventListenerMock2.resetCalled();

        eventManager.unregisterListener("some.key");
        eventManager.publishEvent(new SimpleEvent(this));

        assertFalse(eventListenerMock2.isCalled());
        assertFalse(eventListenerMock.isCalled());
    }

    /**
     * Attempting to register a null with a valid key should result in an illegal argument exception
     */
    @Test
    public void testAddValidKeyWithNullListener()
    {
        try
        {
            eventManager.registerListener("bogus.key", null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException ex)
        {
        }
    }
    
    /**
     * Attempting to publish a subevent   
     */
    @Test
    public void testListenerWithSubEventClass()
    {
        EventListenerMock eventListenerMock = new EventListenerMock(new Class[]{SimpleEvent.class});
        eventManager.registerListener("some.key", eventListenerMock);
        eventManager.publishEvent(new SubEvent(this));
        assertFalse(eventListenerMock.isCalled());
    }
    
    /**
     * Check that the feature about all-typed events listener is working
     */
    @Test
    public void testListeningMultipleEventTypes()
    {
    	 EventListenerMock emptyClassEventListenerMock = new EventListenerMock(new Class[]{});
         eventManager.registerListener("some.key", emptyClassEventListenerMock);
         List<SimpleEvent> multipleEvents = Arrays.asList(
        		 new SimpleEvent(this),new CreationEvent(this),new SubEvent(this));
         for (SimpleEvent event : multipleEvents) {
        	 eventManager.publishEvent(event);
         }
        
         assertTrue(emptyClassEventListenerMock.isCalled());
         assertEquals(multipleEvents.size(), emptyClassEventListenerMock.count);
    }
}
