package radon.engine.logging;

import radon.engine.core.RadonSystem;
import radon.engine.core.RadonSystemManager;
import radon.engine.util.ANSIColor;
import radon.engine.util.types.Singleton;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static radon.engine.core.RadonConfigConstants.*;

public final class Log extends RadonSystem {

    private static final int MSG_QUEUE_TERMINATION_WAIT_TIME = 1000;

    private static final String PATTERN = "%s[%s]: %s\n";

    private static final Message TERMINATION_COMMAND = new Message(null, null);

    @Singleton
    private static Log instance;
    private final EnumSet<Level> levelMask;
    private final EnumMap<Level, ANSIColor> levelColors;
    private final List<LogChannel> channels;
    private final BlockingQueue<Message> messageQueue;
    private final AtomicBoolean running;
    private final ExecutorService executor;
    private final DateTimeFormatter dateTimeFormatter;

    private Log(RadonSystemManager systemManager) {
        super(systemManager);
        levelMask = EnumSet.copyOf(LOG_LEVELS);
        levelColors = new EnumMap<>(Level.class);
        levelColors.putAll(LOG_LEVEL_COLORS);
        channels = new ArrayList<>(LOG_CHANNELS);
        messageQueue = new LinkedBlockingDeque<>();
        running = new AtomicBoolean(false);
        dateTimeFormatter = LOG_DATETIME_FORMATTER;
        executor = newSingleThreadExecutor(runnable -> {
            Thread thread = Executors.defaultThreadFactory().newThread(runnable);
            thread.setName("Log Thread");
            thread.setDaemon(true);
            return thread;
        });
    }

    public static void log(Level level, Object msg) {
        instance.logMessage(level, msg);
    }

    public static void log(Level level, Object msg, Throwable throwable) {
        instance.logMessage(level, withThrowable(msg, throwable));
    }

    public static void trace(Object msg) {
        instance.logMessage(Level.TRACE, msg);
    }

    public static void info(Object msg) {
        instance.logMessage(Level.INFO, msg);
    }

    public static void debug(Object msg) {
        instance.logMessage(Level.DEBUG, msg);
    }

    public static void lwjgl(String msg) {
        instance.logMessage(Level.LWJGL, msg.replaceFirst("\\[LWJGL\\] ", ""));
    }

    public static void warning(Object msg) {
        instance.logMessage(Level.WARNING, msg);
    }

    public static void error(Object msg) {
        instance.logMessage(Level.ERROR, withStackTrace(msg));
    }

    public static void error(Object msg, Throwable cause) {
        instance.logMessage(Level.ERROR, withThrowable(msg, cause));
    }

    public static void fatal(Object msg) {
        String msgString = String.valueOf(msg);

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        FatalException exception = new FatalException(msgString);
        exception.setStackTrace(Arrays.copyOfRange(stackTrace, 2, stackTrace.length));

        instance.logMessage(Level.FATAL, withStackTrace(msgString, stackTrace, 2));
        instance.terminate();

        throw exception;
    }

    public static void fatal(Object msg, Throwable cause) {

        instance.logMessage(Level.FATAL, withThrowable(msg, cause));
        instance.terminate();

        FatalException exception = new FatalException(String.valueOf(msg), cause);
        exception.setStackTrace(cause.getStackTrace());

        throw exception;
    }

    private static String withThrowable(Object msg, Throwable throwable) {
        return msg + getThrowableString(throwable);
    }

    private static String getThrowableString(Throwable throwable) {

        StringBuilder builder = new StringBuilder();

        Throwable cause = throwable;

        while (cause != null) {
            builder.append('\n').append('\t').append("Caused by ");
            builder.append(cause.toString());
            builder.append('\n').append(getStackTrace(cause.getStackTrace(), 0));

            cause = cause.getCause();
        }

        return builder.toString();
    }

    private static String withStackTrace(Object msg) {
        return String.valueOf(msg) + '\n' + getStackTrace();
    }

    private static String withStackTrace(Object msg, StackTraceElement[] stackTrace, int startIndex) {
        return String.valueOf(msg) + '\n' + getStackTrace(stackTrace, startIndex);
    }

    private static String getStackTrace() {
        return getStackTrace(Thread.currentThread().getStackTrace(), 4);
    }

    private static String getStackTrace(StackTraceElement[] stackTrace, int startIndex) {
        StringBuilder builder = new StringBuilder();

        for (int i = startIndex; i < stackTrace.length; i++) {
            builder.append('\t').append("at ").append(stackTrace[i]).append('\n');
        }

        return builder.toString();
    }

    @Override
    protected void init() {
        running.set(true);
        executor.execute(this::run);
    }

    @Override
    protected void terminate() {
        Log.info("Terminating logging system...");
        messageQueue.add(TERMINATION_COMMAND);
        executor.shutdown();
        try {
            executor.awaitTermination(MSG_QUEUE_TERMINATION_WAIT_TIME, MILLISECONDS);
        } catch (InterruptedException e) {
            Logger.getLogger(Log.class.getName()).log(java.util.logging.Level.SEVERE, "Error while terminating logging system", e);
        } finally {
            channels.forEach(this::closeChannel);
        }
    }

    private void closeChannel(LogChannel channel) {
        try {
            channel.close();
        } catch (Exception e) {
            Logger.getLogger(Log.class.getName()).log(java.util.logging.Level.SEVERE, "Error while closing channel", e);
        }
    }

    private void run() {
        while (running.get()) {
            logNextMessage();
        }
    }

    private void logNextMessage() {

        Message message = popMessage();

        if (message == null) {
            return;
        }

        if (message == TERMINATION_COMMAND) {
            running.set(false);
            return;
        }

        log(message);
    }

    private Message popMessage() {
        try {
            return messageQueue.take();
        } catch (InterruptedException e) {
            Logger.getLogger(Log.class.getName()).log(java.util.logging.Level.SEVERE, "Error while popping message", e);
        }
        return null;
    }

    private void logMessage(Level level, Object msg) {
        if (running.get() && levelMask.contains(level)) {
            messageQueue.add(new Message(level, msg));
        }
    }

    private void log(Message message) {
        final String bakedMessage = bakeMessage(message.level, message.contents);
        channels.parallelStream().unordered().filter(channel -> channel.accept(message.level)).forEach(channel -> writeMessage(channel, message.level, bakedMessage));
    }

    private void writeMessage(LogChannel channel, Level level, String bakedMessage) {
        try {
            channel.write(channel.colored() ? colored(level, bakedMessage) : bakedMessage);
        } catch (Exception e) {
            Logger.getLogger(Log.class.getName()).log(java.util.logging.Level.SEVERE, "Error while writing to channel", e);
        }
    }

    private String colored(Level level, String bakedMessage) {
        return colorOf(level) + bakedMessage + ANSIColor.RESET.code;
    }

    private String bakeMessage(Level level, Object msg) {
        return String.format(PATTERN, timestamp(), level.name(), msg);
    }

    private String colorOf(Level level) {
        final ANSIColor ansiColor = levelColors.get(level);
        return ansiColor == null ? ANSIColor.NONE.code : ansiColor.code;
    }

    private String timestamp() {
        final String timestamp = LocalDateTime.now().format(dateTimeFormatter);
        return timestamp.isEmpty() ? "" : '[' + timestamp + ']';
    }

    public enum Level {

        TRACE, INFO, DEBUG, LWJGL, WARNING, ERROR, FATAL;

        public static int compare(Level level1, Level level2) {
            return requireNonNull(level1).compareTo(requireNonNull(level2));
        }

    }

    private static final class Message {

        private final Level level;
        private final Object contents;

        private Message(Level level, Object contents) {
            this.level = level;
            this.contents = contents;
        }
    }

}
