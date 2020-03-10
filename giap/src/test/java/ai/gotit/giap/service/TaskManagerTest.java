package ai.gotit.giap.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import ai.gotit.giap.constant.CommonConstant;
import ai.gotit.giap.constant.TaskType;
import ai.gotit.giap.entity.Event;
import ai.gotit.giap.mock.JSONObjectMock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TaskManagerTest {
    private TaskManager taskManager;
    private Storage storage;
    private NetworkManager networkManager;
    private IdentityManager identityManager;


    @Before
    public void setUp() throws Exception {
        storage = mock(Storage.class);
        networkManager = mock(NetworkManager.class);
        identityManager = mock(IdentityManager.class);

        taskManager = spy(TaskManager.makeInstance(storage, identityManager, networkManager));
    }

    @Test
    public void constructor() {
        assertTrue(taskManager.hasStarted());
    }

    @Test
    public void createEventTask() {
        Event event = mock(Event.class);
        JSONObject serialized = new JSONObjectMock().getMock();
        try {
            when(event.serialize()).thenReturn(serialized);
        } catch (JSONException e) {
            fail();
        }
        assertEquals(0, taskManager.getTaskQueue().size());
        taskManager.createEventTask(event);
        assertEquals(1, taskManager.getTaskQueue().size());
        assertSame(serialized, taskManager.getTaskQueue().peek().getData());
        assertEquals(TaskType.EVENT, taskManager.getTaskQueue().peek().getType());
    }

    @Test
    public void queueLimit() {
        Event event = mock(Event.class);
        JSONObject serialized = new JSONObjectMock().getMock();
        try {
            when(event.serialize()).thenReturn(serialized);
        } catch (JSONException e) {
            fail();
        }
        for (int i = 1; i < CommonConstant.TASK_QUEUE_SIZE_LIMIT + 100; i++) {
            taskManager.createEventTask(event);
        }
        assertEquals(CommonConstant.TASK_QUEUE_SIZE_LIMIT, taskManager.getTaskQueue().size());
    }

    @Test
    public void createAliasTask() {
        assertEquals(0, taskManager.getTaskQueue().size());
        taskManager.createAliasTask("user id");
        assertEquals(1, taskManager.getTaskQueue().size());
        assertEquals(TaskType.ALIAS, taskManager.getTaskQueue().peek().getType());
    }

    @Test
    public void createIdentifyTask() {
        assertEquals(0, taskManager.getTaskQueue().size());
        taskManager.createIdentifyTask("user id");
        assertEquals(1, taskManager.getTaskQueue().size());
        assertEquals(TaskType.IDENTIFY, taskManager.getTaskQueue().peek().getType());
    }

    @Test
    public void createUpdateProfileTask() {
        assertEquals(0, taskManager.getTaskQueue().size());
        taskManager.createUpdateProfileTask(new JSONObject());
        assertEquals(1, taskManager.getTaskQueue().size());
        assertEquals(TaskType.UPDATE_PROFILE, taskManager.getTaskQueue().peek().getType());
    }

    @Test
    public void createIncreasePropertyTask() {
        assertEquals(0, taskManager.getTaskQueue().size());
        taskManager.createIncreasePropertyTask("count", 1);
        assertEquals(1, taskManager.getTaskQueue().size());
        assertEquals(TaskType.INCREASE_PROPERTY, taskManager.getTaskQueue().peek().getType());
    }

    @Test
    public void createAppendToPropertyTask() {
        JSONArray tags = new JSONArray();
        tags.put("red");
        tags.put("blue");
        assertEquals(0, taskManager.getTaskQueue().size());
        taskManager.createAppendToPropertyTask("tags", tags);
        assertEquals(1, taskManager.getTaskQueue().size());
        assertEquals(TaskType.APPEND_TO_PROPERTY, taskManager.getTaskQueue().peek().getType());
    }

    @Test
    public void createRemoveFromPropertyTask() {
        JSONArray tags = new JSONArray();
        tags.put("red");
        tags.put("blue");
        assertEquals(0, taskManager.getTaskQueue().size());
        taskManager.createRemoveFromPropertyTask("tags", tags);
        assertEquals(1, taskManager.getTaskQueue().size());
        assertEquals(TaskType.REMOVE_FROM_PROPERTY, taskManager.getTaskQueue().peek().getType());
    }

    @Test
    public void start() {
        assertTrue(taskManager.hasStarted());
        // Zero call after constructor
        verify(taskManager, times(0)).startScheduling();
        // If started, also not call startScheduling() again
        taskManager.start();
        verify(taskManager, times(0)).startScheduling();
    }

    @Test
    public void restart() {
        assertTrue(taskManager.hasStarted());
        // Zero call after constructor
        verify(taskManager, times(0)).startScheduling();
        // If started, also not call startScheduling() again
        taskManager.restart();
        verify(taskManager, times(0)).startScheduling();

        taskManager.stop();
        taskManager.restart();
        verify(taskManager, times(1)).startScheduling();
    }

    @Test
    public void stop() {
        assertTrue(taskManager.hasStarted());
        taskManager.stop();
        assertFalse(taskManager.hasStarted());
    }
}