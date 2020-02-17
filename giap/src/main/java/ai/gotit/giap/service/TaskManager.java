package ai.gotit.giap.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ai.gotit.giap.constant.CommonProps;
import ai.gotit.giap.constant.RepositoryKey;
import ai.gotit.giap.constant.TaskType;
import ai.gotit.giap.entity.Event;
import ai.gotit.giap.entity.Task;
import ai.gotit.giap.exception.GIAPInstanceExistsException;
import ai.gotit.giap.exception.GIAPJsonException;
import ai.gotit.giap.util.Logger;

public class TaskManager {
    private static TaskManager instance = null;
    private Queue<Task> taskQueue = new LinkedList<>();
    private Queue<Task> processingQueue = new LinkedList<>();
    private Boolean flushing = false;
    private Boolean scheduled = false;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(100);
    private ScheduledFuture<?> scheduledJobHandler;

    private TaskManager() {
    }

    public static TaskManager initialize() {
        if (instance != null) {
            throw new GIAPInstanceExistsException();
        }

        instance = new TaskManager();
        instance.loadStoredTasks();
        instance.schedule();
        return instance;
    }

    public static TaskManager getInstance() {
        return instance;
    }

    public void storeTasks() {
        //TODO: restore processing tasks?
        JSONArray array = new JSONArray();
        while (taskQueue.size() > 0) {
            Task task = taskQueue.poll();
            JSONObject serializedTask;
            try {
                serializedTask = task.serialize();
            } catch (JSONException exception) {
                Logger.error(exception);
                continue;
            }
            array.put(serializedTask);
        }
        Repository.getInstance().put(RepositoryKey.STORED_TASKS, array.toString());
    }

    private void loadStoredTasks() {
        String storedTasks = Repository.getInstance().getString(RepositoryKey.STORED_TASKS);
        if (storedTasks != null) {
            JSONArray array;
            try {
                array = new JSONArray(storedTasks);
            } catch (JSONException e) {
                Logger.error(e);
                return;
            }
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject serializedTask = array.getJSONObject(i);
                    Task task = new Task(serializedTask);
                    taskQueue.add(task);
                } catch (JSONException e) {
                    Logger.error(e);
                }
            }
        }
    }

    public void createEventTask(Event event) throws GIAPJsonException {
        Task task;
        try {
            task = new Task(TaskType.EVENT, event.serialize());
        } catch (JSONException exception) {
            Logger.error(exception);
            throw new GIAPJsonException();
        }
        taskQueue.add(task);
    }

    public void createAliasTask(String userId) throws GIAPJsonException {
        JSONObject json = new JSONObject();
        String distinctId = IdentityManager.getInstance().getDistinctId();
        try {
            json.put(CommonProps.USER_ID, userId);
            json.put(CommonProps.DISTINCT_ID, distinctId);
        } catch (JSONException exception) {
            Logger.error(exception);
            throw new GIAPJsonException();
        }
        Task task = new Task(TaskType.ALIAS, json);
        taskQueue.add(task);
    }

    public void createIdentifyTask(String userId) throws GIAPJsonException {
        JSONObject json = new JSONObject();
        String distinctId = IdentityManager.getInstance().getDistinctId();
        try {
            json.put(CommonProps.USER_ID, userId);
            json.put(CommonProps.CURRENT_DISTINCT_ID, distinctId);
        } catch (JSONException exception) {
            Logger.error(exception);
            throw new GIAPJsonException();
        }
        Task task = new Task(TaskType.IDENTIFY, json);
        taskQueue.add(task);
    }

    private List<JSONObject> dequeueEvents() {
        Logger.log("DEQUEUE: events");
        return dequeueEvents(new ArrayList<JSONObject>());
    }

    private List<JSONObject> dequeueEvents(List<JSONObject> previousList) {
        if (taskQueue.size() == 0) {
            Logger.log("DEQUEUE: nothing left to dequeue");
            return previousList;
        }
        Task topTask = taskQueue.peek();
        if (!topTask.getType().equals(TaskType.EVENT)) {
            Logger.log("DEQUEUE: top item is not event, stop dequeue");
            return previousList;
        }
        processingQueue.add(taskQueue.poll());
        previousList.add(topTask.getData());
        Logger.log("DEQUEUE: added 1 event to the list");
        return dequeueEvents(previousList);
    }

    private void cleanUpProcessingTasks() {
        processingQueue.clear();
        Logger.log("Cleaned up finished tasks!");
    }

    private final Runnable flush = new Runnable() {
        public void run() {
            if (flushing) {
                Logger.log("INCOMING FLUSHING: Another flushing is running. Ignore this flushing.");
                return;
            }
            if (taskQueue.size() == 0) {
                Logger.log("INCOMING FLUSHING: Nothing to flush. Ignore this flushing.");
                return;
            }
            flushing = true;
            Logger.log("INCOMING FLUSHING: New flushing has started!");

            Task topTask = taskQueue.peek();
            switch (topTask.getType()) {
                case TaskType.EVENT: {
                    Logger.log("FLUSHING: trying to flush event tasks");
                    // Try to dequeue a batch of events at the top of the queue
                    List<JSONObject> eventBatch = dequeueEvents();
                    int eventBatchSize = eventBatch.size();
                    if (eventBatchSize > 0) {
                        Logger.log("FLUSHING: dequeue " + eventBatchSize + " events to the batch");
                        JSONArray bodyData = new JSONArray(eventBatch);
                        boolean response = NetworkManager.track(bodyData);
                        Logger.log("FLUSHING: track() returned " + response);
                        // TODO: handle response
                        if (response == true) {
                            cleanUpProcessingTasks();
                        } else {
                            // Only retry (not call cleanUpProcessingTasks) if receive no response or network error
                        }
                    } else {
                        Logger.warn("FLUSHING: empty event batch! (Should not happen)");
                    }
                    break;
                }

                case TaskType.ALIAS: {
                    Logger.log("FLUSHING: try to flush alias task");
                    boolean response = NetworkManager.alias(topTask.getData());
                    // TODO: handle response
                    if (response == true) {
                        cleanUpProcessingTasks();
                    } else {
                        // Only retry (not call cleanUpProcessingTasks) if receive no response or network error
                    }
                    break;
                }

                case TaskType.IDENTIFY: {
                    Logger.log("FLUSHING: try to flush identity task");
                    try {
                        String userId = topTask.getData().getString(CommonProps.USER_ID);
                        String currentDistinctId = topTask.getData().getString(CommonProps.CURRENT_DISTINCT_ID);
                        boolean response = NetworkManager.identify(userId, currentDistinctId);
                        // TODO: handle response
                        if (response == true) {
                            cleanUpProcessingTasks();
                        } else {
                            // Only retry (not call cleanUpProcessingTasks) if receive no response or network error
                        }
                    } catch (JSONException e) {
                        Logger.error(e);
                    }
                    break;
                }
            }

            flushing = false;
            Logger.log("Flushing finished!");
            //    If success, remove processing tasks
            //    Then set flushing = false
        }
    };

    public void schedule() {
        if (scheduled) {
            Logger.warn("Scheduler is started. Call stop() or forceStop() before starting again.");
            return;
        }
        scheduled = true;
        long tasksFlushingInterval = ConfigManager.getInstance().getTasksFlushingInterval();
        scheduledJobHandler = scheduler.scheduleAtFixedRate(
                flush,
                tasksFlushingInterval,
                tasksFlushingInterval,
                TimeUnit.SECONDS
        );
    }

    public void stop() {
        scheduledJobHandler.cancel(false);
    }
}
