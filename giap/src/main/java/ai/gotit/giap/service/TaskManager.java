package ai.gotit.giap.service;

import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import ai.gotit.giap.constant.CommonConstant;
import ai.gotit.giap.constant.CommonProps;
import ai.gotit.giap.constant.StorageKey;
import ai.gotit.giap.constant.TaskType;
import ai.gotit.giap.entity.Event;
import ai.gotit.giap.entity.Task;
import ai.gotit.giap.util.Logger;

public class TaskManager {
    private Queue<Task> taskQueue = new LinkedList<>();
    private Queue<Task> processingQueue = new LinkedList<>();
    private Boolean flushing = false;
    private Boolean started = false;
    private ScheduledExecutorService scheduler = null;
    private ScheduledFuture<?> scheduledJobHandler;
    private Storage storage;
    private IdentityManager identityManager;
    private NetworkManager networkManager;

    public TaskManager(Storage storage, IdentityManager identityManager, NetworkManager networkManager) {
        this.storage = storage;
        this.identityManager = identityManager;
        this.networkManager = networkManager;

        initScheduler();
        start();
    }

    public static TaskManager makeInstance(Storage storage, IdentityManager identityManager, NetworkManager networkManager) {
        return new TaskManager(storage, identityManager, networkManager);
    }

    private void storeTasks() {
        Logger.log("TASK MANAGER: storing tasks into Storage ...");
        JSONArray array = new JSONArray();
        Queue<Task> clonedQueue = new LinkedList<>(taskQueue);
        while (clonedQueue.size() > 0) {
            Task task = clonedQueue.poll();
            JSONObject serializedTask;
            try {
                serializedTask = task.serialize();
            } catch (JSONException exception) {
                Logger.error(exception);
                continue;
            }
            array.put(serializedTask);
        }
        storage.put(StorageKey.STORED_TASKS, array.toString());
        Logger.log("TASK MANAGER: storing has completed!");
    }

    private void loadStoredTasks() {
        Logger.log("TASK MANAGER: loading tasks from Storage ...");
        String storedTasks = storage.getString(StorageKey.STORED_TASKS);
        if (storedTasks != null) {
            JSONArray array;
            try {
                array = new JSONArray(storedTasks);
                Logger.log("TASK MANAGER: found " + array.length() + " task(s) in storage.");
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
        Logger.log("TASK MANAGER: loading saved tasks completed!");
    }

    public void createEventTask(Event event) {
        Task task;
        try {
            task = new Task(TaskType.EVENT, event.serialize());
        } catch (JSONException exception) {
            Logger.error(exception);
            return;
        }
        taskQueue.add(task);
    }

    public void createAliasTask(String userId) {
        JSONObject json = new JSONObject();
        String distinctId = identityManager.getDistinctId();
        try {
            json.put(CommonProps.USER_ID, userId);
            json.put(CommonProps.DISTINCT_ID, distinctId);
        } catch (JSONException exception) {
            Logger.error(exception);
            return;
        }
        Task task = new Task(TaskType.ALIAS, json);
        taskQueue.add(task);
        // TODO: aware of multi-thread -> new events still have chance to use old distinctId
        identityManager.updateDistinctId(userId);
    }

    public void createIdentifyTask(String userId) {
        JSONObject json = new JSONObject();
        String distinctId = identityManager.getDistinctId();
        try {
            json.put(CommonProps.USER_ID, userId);
            json.put(CommonProps.CURRENT_DISTINCT_ID, distinctId);
        } catch (JSONException exception) {
            Logger.error(exception);
            return;
        }
        Task task = new Task(TaskType.IDENTIFY, json);
        taskQueue.add(task);
        // TODO: aware of multi-thread -> new events still have chance to use old distinctId
        identityManager.updateDistinctId(userId);
    }

    public void createUpdateProfileTask(JSONObject props) {
        try {
            String currentDistinctId = identityManager.getDistinctId();
            props.put(CommonProps.CURRENT_DISTINCT_ID, currentDistinctId);
        } catch (JSONException e) {
            Logger.error(e);
            return;
        }
        Task task = new Task(TaskType.UPDATE_PROFILE, props);
        taskQueue.add(task);
    }

    private List<JSONObject> dequeueEvents() {
        Logger.log("DEQUEUE: events");
        return dequeueEvents(new ArrayList<JSONObject>());
    }

    private List<JSONObject> dequeueEvents(List<JSONObject> previousList) {
        if (processingQueue.size() == 0) {
            Logger.log("DEQUEUE: nothing left to dequeue");
            return previousList;
        }
        Task topTask = processingQueue.peek();
        if (!topTask.getType().equals(TaskType.EVENT)) {
            Logger.log("DEQUEUE: top item is not event, stop dequeue");
            return previousList;
        }
        topTask.setProcessing(true);
        processingQueue.poll();
        previousList.add(topTask.getData());
        Logger.log("DEQUEUE: added 1 event to the list");
        return dequeueEvents(previousList);
    }

    private Response.Listener<JSONObject> createSuccessCallback(final String taskName) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Logger.log("FLUSHING: " + taskName + " task(s) succeed! - " + response.toString());
                cleanUpProcessingTasks();
                finishFlushing();
            }
        };
    }

    private Response.ErrorListener createErrorCallback() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                Logger.error(e);
                if (e instanceof NoConnectionError) {
                    Logger.log("FLUSHING: network error, retry!");
                } else {
                    int statusCode = e.networkResponse.statusCode;
                    if (statusCode >= CommonConstant.MIN_SERVER_ERROR_STATUS_CODE && statusCode <= CommonConstant.MAX_SERVER_ERROR_STATUS_CODE
                    ) {
                        Logger.log("FLUSHING: GIAP Platform Core internal error!");
                    } else {
                        cleanUpProcessingTasks();
                    }
                }

                finishFlushing();
            }
        };
    }

    private void cleanUpProcessingTasks() {
        while (taskQueue.size() > 0 && taskQueue.peek().getProcessing()) {
            taskQueue.poll();
        }
        Logger.log("FLUSHING: Cleaned up finished tasks!");
    }

    private void finishFlushing() {
        processingQueue.clear();
        if (!started) {
            Logger.log("FLUSHING: scheduler has stopped due to app's inactivity, save current queue into storage");
            storeTasks();
        }
        flushing = false;
        Logger.log("FLUSHING: Flushing finished!");
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

            processingQueue.clear();
            processingQueue.addAll(taskQueue);

            Task topTask = processingQueue.peek();
            switch (topTask.getType()) {
                case TaskType.EVENT: {
                    Logger.log("FLUSHING: trying to flush event tasks");
                    // Try to dequeue a batch of events at the top of the queue
                    List<JSONObject> eventBatch = dequeueEvents();
                    int eventBatchSize = eventBatch.size();
                    if (eventBatchSize > 0) {
                        Logger.log("FLUSHING: dequeue " + eventBatchSize + " events to the batch");
                        JSONArray bodyData = new JSONArray(eventBatch);
                        networkManager.track(
                                bodyData,
                                createSuccessCallback(topTask.getType()),
                                createErrorCallback()
                        );
                    } else {
                        Logger.warn("FLUSHING: empty event batch! (Should not happen)");
                    }
                    break;
                }

                case TaskType.ALIAS: {
                    Logger.log("FLUSHING: try to flush alias task");
                    topTask.setProcessing(true);
                    processingQueue.poll();
                    networkManager.alias(
                            topTask.getData(),
                            createSuccessCallback(topTask.getType()),
                            createErrorCallback()
                    );
                    break;
                }

                case TaskType.IDENTIFY: {
                    Logger.log("FLUSHING: try to flush identify task");
                    topTask.setProcessing(true);
                    processingQueue.poll();
                    try {
                        String userId = topTask.getData().getString(CommonProps.USER_ID);
                        String currentDistinctId = topTask.getData().getString(CommonProps.CURRENT_DISTINCT_ID);
                        networkManager.identify(
                                userId,
                                currentDistinctId,
                                createSuccessCallback(topTask.getType()),
                                createErrorCallback()
                        );
                    } catch (JSONException e) {
                        Logger.error(e);
                    }
                    break;
                }

                case TaskType.UPDATE_PROFILE: {
                    Logger.log("FLUSHING: try to flush updateProfile task");
                    topTask.setProcessing(true);
                    processingQueue.poll();
                    networkManager.updateProfile(
                            topTask.getData(),
                            createSuccessCallback(topTask.getType()),
                            createErrorCallback()
                    );
                    break;
                }
            }
        }
    };

    private synchronized void initScheduler() {
        if (scheduler != null) {
            return;
        }
        scheduler = new ScheduledThreadPoolExecutor(100) {
            @Override
            public void afterExecute(Runnable r, Throwable t) {
                super.afterExecute(r, t);
                boolean isDone = false;
                if (t == null && r instanceof ScheduledFuture<?>) {
                    try {
                        isDone = ((ScheduledFuture<?>) r).isDone();
                        if (isDone) {
                            Logger.warn("SCHEDULED TASK: Scheduler is shut down.");
                            ((ScheduledFuture<?>) r).get();
                        }
                    } catch (CancellationException e) {
                        Logger.warn("SCHEDULED TASK: Scheduled job is cancelled!");
                    } catch (ExecutionException e) {
                        t = e.getCause();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                if (t != null) {
                    // Exception occurred
                    Logger.error(t, "SCHEDULED TASK: Uncaught exception is detected!");
                    // Restart the runnable again
                    if (isDone) {
                        Logger.log("Trying to restart the scheduler ...");
                        startScheduling();
                    }
                }
            }
        };
    }

    private void startScheduling() {
        if (scheduler == null) {
            Logger.error("TASK MANAGER: scheduler not found (should not be). Can not start scheduling.");
            return;
        }
        if (scheduledJobHandler != null && !scheduledJobHandler.isDone()) {
            Logger.warn("TASK MANAGER: scheduling failed! Previous scheduled job has not done yet!");
            return;
        }
        scheduledJobHandler = scheduler.scheduleAtFixedRate(
                flush,
                CommonConstant.TASKS_FLUSHING_INTERVAL,
                CommonConstant.TASKS_FLUSHING_INTERVAL,
                TimeUnit.SECONDS
        );
        Logger.log("TASK MANAGER: scheduler has started. Flushing tasks every " + CommonConstant.TASKS_FLUSHING_INTERVAL + " second(s).");
    }

    public void start() {
        if (started) {
            Logger.warn("TASK MANAGER: Scheduler has started. Call stop() before starting again.");
            return;
        }
        started = true;
        loadStoredTasks();
        startScheduling();
    }

    public void restart() {
        if (started) {
            Logger.warn("TASK MANAGER: Scheduler has started. Call stop() before starting again.");
            return;
        }
        started = true;
        startScheduling();
    }

    public void stop() {
        if (!started) {
            Logger.warn("TASK MANAGER: Scheduler has not started yet.");
            return;
        }
        Logger.log("TASK MANAGER: stopping scheduler ...");
        if (scheduledJobHandler != null) {
            scheduledJobHandler.cancel(false);
        }
        storeTasks();
        started = false;
    }
}
