package ai.gotit.giap.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
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
            json.put(CommonProps.CURRENT_DISTINCT_ID, distinctId);
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

    private final Runnable flush = new Runnable() {
        public void run() {
            if (flushing) return;
            if (taskQueue.size() == 0) return;
            flushing = true;
            //    TODO
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
        scheduledJobHandler.
    }
}
