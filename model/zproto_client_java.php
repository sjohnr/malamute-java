<?php

include 'functions.php';
$filename = $argv[1];
$filepath = dirname($filename);
$class = read_xml($filename);
$proto = read_xml($filepath . '/' . $class['protocol_class'] . '.xml');

$path = $class['package'];
$client_class = jclass($class['name']);
$proto_class = jclass($proto['name']);
$states = get_all_states($class);
$events = get_all_events($class);
$actions = get_all_actions($class);
$messages = get_messages_by_name($proto);
$fields = get_all_fields_by_name($class, $messages);
$replies = get_replies_by_name($class);
$recv = array_of($class->recv->message);
$send = array_of($class->send->message);

resolve_includes($class);
create_directories($path);

?>
<?php output("../src/main/java/${path}/${client_class}Agent.java") ?>
/* =============================================================================
 * <?php echo $client_class ?>Agent.java
 *
 * Generated class for <?php echo $client_class ?>.
 * -----------------------------------------------------------------------------
 * <?php echo nl(block_comment($class['license'])) ?>
 * =============================================================================
 */
package <?php echo package($class['package']) ?>;

import <?php echo package($proto['package']) ?>.*;

import org.zeromq.api.Context;
import org.zeromq.api.LoopAdapter;
import org.zeromq.api.LoopHandler;
import org.zeromq.api.Message;
import org.zeromq.api.MessageFlag;
import org.zeromq.api.Reactor;
import org.zeromq.api.Socket;
import org.zeromq.api.SocketType;

import java.util.HashMap;
import java.util.Map;

/**
 * <?php echo $client_class ?>Agent class.
 *
 * @author <?php echo nl(get_current_user()) ?>
 */
public class <?php echo $client_class ?>Agent {
    // Structure of our class
    private <?php echo $client_class ?>Handler handler;
    private Context context;
    private Reactor reactor;
    private Socket pipe;
    private Socket inbox;
    private <?php echo $proto_class ?>Socket socket;
    private State state = State.START;
    private Event event;
    private Event next;
    private Event exception;
    private Event wakeup;
    private LoopHandler wakeupHandler;
<?php if (array_search('heartbeat', $events)): ?>
    private long heartbeat;
    private LoopHandler heartbeatHandler;
<?php endif; ?>
<?php if (array_search('expired', $events)): ?>
    private long expiry;
    private LoopHandler expiryHandler;
<?php endif; ?>
    private boolean verbose = false;
    private boolean connected = false;
    private boolean terminated = false;
    private Map<String, Object> parameters = new HashMap<>();

    /**
     * Create a <?php echo $client_class ?>Agent.
     *
     * @param context The ZeroMQ context
     * @param handler The application callback handler
     */
    public <?php echo $client_class ?>Agent(Context context, <?php echo $client_class ?>Handler handler) {
        this.context = context;
        this.handler = handler;

        handler.set<?php echo $client_class ?>Agent(this);
    }

    /**
     * Start the client's background agent.
     */
    public void start() {
        // Socket must be connected to a real endpoint in application code by
        // message from client interface.
        Socket dealer = context.buildSocket(SocketType.DEALER)
            .withSendHighWatermark(Long.MAX_VALUE)
            .withReceiveHighWatermark(Long.MAX_VALUE)
            .connect(String.format("inproc://dealer-%s", this.toString()));

        this.pipe = context.buildSocket(SocketType.PAIR).bind(String.format("inproc://pipe-%s", this.toString()));
        this.inbox = context.buildSocket(SocketType.PAIR).bind(String.format("inproc://inbox-%s", this.toString()));
        this.socket = new <?php echo $proto_class ?>Socket(dealer);
        this.reactor = context.buildReactor()
            .withInPollable(pipe, new PipeHandler())
            .withInPollable(inbox, new InboxHandler())
            .withInPollable(dealer, new DealerHandler())
            .build();

        // Start the reactor
        reactor.start();
    }

    /**
     * Stop the client's background agent and clean up references.
     */
    public void stop() {
        state = State.START;
        try {
            reactor.stop();
            socket.close();
            inbox.close();
            pipe.close();
        } finally {
<?php if (array_search('heartbeat', $events)): ?>
            heartbeatHandler = null;
<?php endif; ?>
<?php if (array_search('expired', $events)): ?>
            expiryHandler = null;
<?php endif; ?>
            pipe = null;
            inbox = null;
            socket = null;
            reactor = null;
            verbose = false;
            terminated = false;
            connected = false;
        }
    }

    /**
     * Connect to a server using the given endpoint.
     *
     * @param endpoint The server endpoint to connect to
     */
    public void connect(String endpoint) {
        socket.getSocket().getZMQSocket().connect(endpoint);
    }

    /**
     * @return The current state
     */
    public State getState() {
        return state;
    }

    /**
     * @return The current event being handled by the state machine
     */
    public Event getEvent() {
        return event;
    }

    /**
     * @return The next event to be handled by the state machine
     */
    public Event getNext() {
        return next;
    }

    /**
     * Set (trigger) the next event to be handled by the state machine.
     * 
     * @param next The next event
     */
    public void triggerEvent(Event next) {
        this.next = next;
    }

    /**
     * Raise an exception, halting any actions in progress.
     * <p>
     * Continues execution of actions defined for the exception event.
     *
     * @param event The exception event
     */
    public void setException(Event event) {
        this.exception = event;
        throw new HaltException();
    }

    /**
     * Set a wakeup alarm.
     * <p>
     * The next state should handle the wakeup event. The alarm is cancelled on
     * any other event.
     *
     * @param wakeup The wakeup timer in milliseconds
     * @param event The event to trigger when the wakeup timer expires
     */
    public void setWakeup(long wakeup, Event event) {
        this.wakeup = event;
        if (wakeupHandler != null) {
            reactor.cancel(wakeupHandler);
            wakeupHandler = null;
        }
        if (wakeup > 0) {
            wakeupHandler = new WakeupHandler();
            reactor.addTimer(wakeup, 1, wakeupHandler);
        }
    }
<?php if (array_search('heartbeat', $events)): ?>

    /**
     * Set a heartbeat timer.
     * <p>
     * The interval is in milliseconds and must be non-zero. The state machine
     * must handle the "heartbeat" event.
     * <p>
     * The heartbeat happens every interval no matter what traffic the client is
     * sending or receiving.
     *
     * @param heartbeat The heartbeat timer in milliseconds
     */
    public void setHeartbeat(long heartbeat) {
        this.heartbeat = heartbeat;
        if (heartbeatHandler != null) {
            reactor.cancel(heartbeatHandler);
            heartbeatHandler = null;
        }
        if (heartbeat > 0) {
            heartbeatHandler = new HeartbeatHandler();
            reactor.addTimer(heartbeat, 1, heartbeatHandler);
        }
    }
<?php endif; ?>
<?php if (array_search('expired', $events)): ?>

    /**
     * Set an expiry timer.
     * <p>
     * Setting a non-zero expiry causes the state machine to receive an "expired"
     * event if there is no incoming traffic for that many milliseconds.
     * <p>
     * This cycles over and over until/unless the code sets a zero expiry. The
     * state machine must handle the "expired" event.
     *
     * @param expiry The expiry timer in milliseconds
     */
    public void setExpiry(long expiry) {
        this.expiry = expiry;
        if (expiryHandler != null) {
            reactor.cancel(expiryHandler);
            expiryHandler = null;
        }
        if (expiry > 0) {
            expiryHandler = new ExpiryHandler();
            reactor.addTimer(expiry, 1, expiryHandler);
        }
    }
<?php endif; ?>

    /**
     * @return The parameters from an API call
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * @return The high level socket, connected to the server
     */
    public <?php echo $proto_class ?>Socket getSocket() {
        return socket;
    }

    /**
     * @return The serialization codec
     */
    public <?php echo $proto_class ?>Codec getCodec() {
        return socket.getCodec();
    }
<?php foreach ($proto->message as $message): ?>

    /**
     * @return The <?php echo strtoupper($message['name']) ?> message
     */
    public <?php echo jclass($message['name']) ?>Message get<?php echo jclass($message['name']) ?>() {
        return socket.getCodec().get<?php echo jclass($message['name']) ?>();
    }

    /**
     * @param message The <?php echo strtoupper($message['name']) ?> message
     */
    public void set<?php echo jclass($message['name']) ?>(<?php echo jclass($message['name']) ?>Message message) {
        socket.getCodec().set<?php echo jclass($message['name']) ?>(message);
    }
<?php endforeach; ?>

    /**
     * Convert a message type to an event.
     *
     * @param messageType The message type
     * @return The event, or null if no matching event name found
     */
    private Event event(<?php echo $proto_class ?>Codec.MessageType messageType) {
        try {
            return Event.valueOf(messageType.name());
        } catch (IllegalArgumentException ex) {
            System.err.println("Invalid event name. Event names must match message names. Received " + messageType.name());
            terminated = true;
            return null;
        }
    }

    /**
     * Execute the state machine until there are no more events to be processed.
     *
     * @param triggeredEvent The current event to be processed
     */
    private void executeInternal(Event triggeredEvent) {
        next = triggeredEvent;

        // Cancel wakeup timer, if any was pending
        if (wakeupHandler != null) {
            reactor.cancel(wakeupHandler);
            wakeupHandler = null;
        }

        while (!terminated && next != null) {
            event = next;
            next = null;
            exception = null;

            try {
                switch (state) {
<?php foreach ($states as $state): ?>
                    case <?php echo cconst($state['name']) ?>:
                        switch (event) {
<?php     foreach (resolve_events($class, $state) as $event): ?>
<?php         if ((string) $event['name'] != '*'): ?>
                            case <?php echo cconst($event['name']) ?>: {
<?php         else: ?>
                            default: {
<?php         endif; ?>
<?php         foreach ($event->action as $action): ?>
<?php             switch ((string) $action['name']): ?>
<?php                 case 'send': ?>
                                socket.send(socket.getCodec().get<?php echo jclass($action['message']) ?>());
<?php                     break; ?>
<?php                 case 'recv': ?>
<?php                         $message = $messages[(string) $action['message']]; ?>
                                Message <?php echo jvar($message['name']) ?> = new Message("<?php echo $message['name'] ?>");
<?php                     foreach ($message->field as $field): ?>
                                <?php echo jvar($message['name']) ?>.add<?php echo get_pushpop_method($field) ?>(socket.getCodec().get<?php echo jclass($message['name']) ?>().get<?php echo jclass($field['name']) ?>());
<?php                     endforeach; ?>
                                inbox.send(<?php echo jvar($message['name']) ?>);
<?php                     break; ?>
<?php                 case 'stop': ?>
                                stop();
<?php                     break; ?>
<?php                 default: ?>
                                handler.<?php echo jvar($action['name']) ?>();
<?php                     break; ?>
<?php             endswitch; ?>
<?php         endforeach; ?>
<?php         if ($event['next']): ?>
                                state = State.<?php echo cconst($event['next']) ?>;
<?php         endif; ?>
<?php         if ($event['trigger']): ?>
                                next = Event.<?php echo cconst($event['trigger']) ?>;
<?php         endif; ?>
                                break;
                            }
<?php     endforeach; ?>
                        }
                        break;
<?php endforeach; ?>
                }
            } catch (HaltException ex) {
                next = exception;
            }
        }
    }

    /**
     * States we can be in.
     */
    public enum State {
<?php foreach ($states as $i => $state): ?>
        <?php echo cconst($state['name']) ?><?php echo last($states, $i) ? nl('') : nl(',') ?>
<?php endforeach; ?>
    }

    /**
     * Events we can process.
     */
    public enum Event {
<?php foreach ($events as $i => $state): ?>
        <?php echo cconst($state) ?><?php echo last($events, $i) ? nl() : nl(',') ?>
<?php endforeach; ?>
    }

    /**
     * Signal exception to break out of processing current state.
     */
    private static class HaltException extends RuntimeException {
        // Nothing
    }

    /**
     * Handler for commands from interface.
     */
    private class PipeHandler extends LoopAdapter {
        @Override
        protected void execute(Reactor reactor, Socket socket) {
            // Clean up any previous data
            parameters.clear();

            Message message = socket.receiveMessage();
            String method = message.popString();
            switch (method) {
                case "$TERM":
                    terminated = true;
                    break;
                case "$CONNECTED":
                    socket.send(new Message(connected ? 1 : 0));
                    break;
                case "SET VERBOSE":
                    verbose = message.popInt() > 0;
                    break;
<?php foreach ($class->method as $method): ?>
                case "<?php echo strtoupper($method['name']) ?>":
<?php     foreach ($method->field as $field): ?>
                    parameters.put("<?php echo $field['name'] ?>", message.pop<?php echo get_pushpop_method($field) ?>());
<?php     endforeach; ?>
                    executeInternal(Event.<?php echo cconst($method['name']) ?>);
                    break;
<?php endforeach; ?>
            }
        }
    }

    /**
     * Handler for messages from interface.
     */
    private class InboxHandler extends LoopAdapter {
        @Override
        protected void execute(Reactor reactor, Socket socket) {
            Message message = socket.receiveMessage();
            String method = message.popString();
            switch (method) {
                case "$FLUSH":
                    pipe.send(new Message("OK"));
                    break;
<?php foreach ($class->send->message as $method): ?>
<?php     $message = $messages[(string) $method['name']]; ?>
                case "<?php echo $message['name'] ?>": {
                    <?php echo jclass($message['name']) ?>Message outgoing = new <?php echo jclass($message['name']) ?>Message();
<?php     foreach ($message->field as $field): ?>
<?php         if (get_pushpop_method($field) == 'Frames'): ?>
                    outgoing.set<?php echo jclass($field['name']) ?>(message);
<?php         else: ?>
                    outgoing.set<?php echo jclass($field['name']) ?>(message.pop<?php echo get_pushpop_method($field) ?>());
<?php         endif; ?>
<?php     endforeach; ?>
                    getSocket().send(outgoing);
                    break;
                }
<?php endforeach; ?>
            }
        }
    }

    /**
     * Handler for messages from server.
     */
    private class DealerHandler extends LoopAdapter {
        @Override
        protected void execute(Reactor reactor, Socket socket) {
            // We will process as many messages as we can, to reduce the overhead
            // of polling and the reactor.
            <?php echo $proto_class ?>Codec.MessageType messageType;
            while ((messageType = getSocket().receive(MessageFlag.DONT_WAIT)) != null) {
<?php if (array_search('expired', $events)): ?>
                // Any input from server counts as activity
                if (expiryHandler != null) {
                    reactor.cancel(expiryHandler);
                    if (expiry > 0) {
                        reactor.addTimer(expiry, 1, expiryHandler);
                    }
                }

<?php endif; ?>
                Event event = event(messageType);
                executeInternal(event);
            }
        }
    }

    /**
     * Handler for wakeup timer.
     */
    private class WakeupHandler extends LoopAdapter {
        @Override
        protected void execute(Reactor reactor, Socket socket) {
            Event event = wakeup;
            wakeup = null;
            executeInternal(event);
        }
    }
<?php if (array_search('expired', $events)): ?>

    /**
     * Handler for expiry timer.
     */
    private class ExpiryHandler extends LoopAdapter {
        @Override
        protected void execute(Reactor reactor, Socket socket) {
            executeInternal(Event.EXPIRED);
            if (!terminated && expiry > 0) {
                reactor.addTimer(expiry, 1, this);
            }
        }
    }
<?php endif; ?>
<?php if (array_search('heartbeat', $events)): ?>

    /**
     * Handler for heartbeat timer.
     */
    private class HeartbeatHandler extends LoopAdapter {
        @Override
        protected void execute(Reactor reactor, Socket socket) {
            executeInternal(Event.HEARTBEAT);
            if (!terminated && heartbeat > 0) {
                reactor.addTimer(heartbeat, 1, this);
            }
        }
    }
<?php endif; ?>
}
<?php output("../src/main/java/${path}/${client_class}.java") ?>
/* =============================================================================
 * <?php echo $client_class ?>.java
 *
 * Generated class for <?php echo $client_class ?>.
 * -----------------------------------------------------------------------------
 * <?php echo nl(block_comment($class['license'])) ?>
 * =============================================================================
 */
package <?php echo package($class['package']) ?>;

import org.zeromq.ContextFactory;
import org.zeromq.api.Context;
import org.zeromq.api.Message;
import org.zeromq.api.Socket;
import org.zeromq.api.SocketType;

import java.util.Arrays;

/**
 * <?php echo $client_class ?> class.
 * <p>
 * <?php echo trim((string) $class) ?>.
 *
 * @author <?php echo nl(get_current_user()) ?>
 */
public class <?php echo $client_class ?> {
    // Structure of our class
    private Context context;
    private Socket pipe;
    private Socket inbox;
    private <?php echo $client_class ?>Agent agent;

    // Fields received in messages and replies
<?php foreach (array_values($fields) as $field): ?>
    private <?php echo get_parameter_type($field) ?> <?php echo jvar($field['name']) ?>;
<?php endforeach; ?>

    /**
     * Create a <?php echo $client_class ?>.
     *
     * @param handler The application callback handler
     */
    public <?php echo $client_class ?>(<?php echo $client_class ?>Handler handler) {
        this(ContextFactory.createContext(1), handler);
    }

    /**
     * Create a <?php echo $client_class ?>.
     *
     * @param context The 0MQ context
     * @param handler The application callback handler
     */
    public <?php echo $client_class ?>(Context context, <?php echo $client_class ?>Handler handler) {
        this.context = context;
        this.agent = new <?php echo $client_class ?>Agent(context, handler);
        this.pipe = context.buildSocket(SocketType.PAIR).connect(String.format("inproc://pipe-%s", agent.toString()));
        this.inbox = context.buildSocket(SocketType.PAIR).connect(String.format("inproc://inbox-%s", agent.toString()));
    }

    /**
     * Destroy the client.
     */
    public void close() {
        pipe.send(new Message("$TERM"));
        
    }

<?php foreach (array_values($fields) as $field): ?>

    /**
     * @return Last received <?php echo nl($field['name']) ?>
     */
    public <?php echo get_parameter_type($field) ?> get<?php echo jclass($field['name']) ?>() {
        return <?php echo jvar($field['name']) ?>;
    }
<?php endforeach; ?>

    /**
     * Ask the background agent if it is connected to the server.
     *
     * @return true if the background agent is connected to the server, false otherwise
     */
    public boolean isConnected() {
        pipe.send(new Message("$CONNECTED"));
        return pipe.receiveMessage().popInt() > 0;
    }

    /**
     * Set the verbose logging flag of the background agent.
     *
     * @param verbose The flag controlling whether verbose logging is enabled
     */
    public void setVerbose(boolean verbose) {
        pipe.send(new Message(verbose ? 1 : 0));
    }
<?php foreach ($class->method as $method): ?>

    /**
     * <?php echo nl(block_comment($method, 4)) ?>
<?php     if ($method['return']): ?>
     *
     * @return The <?php echo jvar($method['return']) ?> field of the reply
<?php     endif; ?>
     */
    public <?php echo get_parameter_type($fields[(string) $method['return']]) ?> <?php echo jvar($method['name']) ?>
(<?php    foreach (array_of($method->field) as $i => $field): ?>
<?php echo (first($i)) ? '' : ', ' ?>
<?php echo get_parameter_type($field) ?> <?php echo jvar($field['name']) ?>
<?php     endforeach; ?>
) {
        Message message = new Message("<?php echo strtoupper($method['name']) ?>");
<?php     foreach (array_of($method->field) as $i => $field): ?>
        message.add<?php echo get_pushpop_method($field) ?>(<?php echo jvar($field['name']) ?>);
<?php     endforeach; ?>
        pipe.send(message);
<?php     if ($method->accept): ?>
        accept(<?php foreach (array_of($method->accept) as $i => $accept): ?><?php if (!first($i)): ?>, <?php endif; ?>"<?php echo $accept['reply'] ?>"<?php endforeach; ?>);
<?php     endif; ?>
<?php     if ($method['return']): ?>

        return <?php echo jvar($method['return']) ?>;
<?php     endif; ?>
    }
<?php endforeach; ?>

    private void accept(String... replies) {
        Message message = pipe.receiveMessage();
        String reply = message.popString();
        assert Arrays.asList(replies).contains(reply);
        switch (reply) {
<?php foreach ($replies as $reply): ?>
            case "<?php echo $reply['name'] ?>":
<?php     foreach ($reply->field as $field): ?>
                <?php echo jvar($field['name']) ?> = message.pop<?php echo get_pushpop_method($field) ?>();
<?php     endforeach; ?>
                break;
<?php endforeach; ?>
        }
    }
<?php foreach ($send as $method): ?>
<?php $message = $messages[(string) $method['name']]; ?>

    /**
     * Send a <?php echo $method['name'] ?> message to the server.
     */
    public void <?php echo jvar($method['method']) ?>
(<?php    foreach (array_of($message->field) as $i => $field): ?>
<?php echo (first($i)) ? '' : ', ' ?>
<?php echo get_parameter_type($field) ?> <?php echo jvar($field['name']) ?>
<?php     endforeach; ?>
) {
        Message message = new Message("<?php echo $method['name'] ?>");
<?php foreach ($message->field as $field): ?>
        message.add<?php echo get_pushpop_method($field) ?>(<?php echo jvar($field['name']) ?>);
<?php endforeach; ?>

        inbox.send(message);
    }
<?php endforeach; ?>

    /**
     * Receive a message from the server.
     *
     * @return The MessageType of the message
     */
    public MessageType receive() {
        Message message = inbox.receiveMessage();
        MessageType type = MessageType.valueOf(message.popString());
        switch (type) {
<?php foreach ($recv as $method): ?>
<?php $message = $messages[(string) $method['name']]; ?>
            case <?php echo cconst($method['name']) ?>:
<?php foreach ($message->field as $field): ?>
<?php     if (get_pushpop_method($field) == 'Frames'): ?>
                <?php echo jvar($field['name']) ?> = message;
<?php     else: ?>
                <?php echo jvar($field['name']) ?> = message.pop<?php echo get_pushpop_method($field) ?>();
<?php     endif; ?>
<?php endforeach; ?>
                break;
<?php endforeach; ?>
        }

        return type;
    }

    /**
     * Enumeration of message types.
     */
    public enum MessageType {
<?php foreach ($recv as $i => $method): ?>
        <?php echo cconst($method['name']) ?><?php echo last($recv, $i) ? nl() : nl(',') ?>
<?php endforeach; ?>
    }
}
<?php output("../src/main/java/${path}/${client_class}Handler.java") ?>
/* =============================================================================
 * <?php echo $client_class ?>.java
 *
 * Generated class for <?php echo $client_class ?>.
 * -----------------------------------------------------------------------------
 * <?php echo nl(block_comment($class['license'])) ?>
 * =============================================================================
 */
package <?php echo package($class['package']) ?>;

/**
 * <?php echo $client_class ?>Handler interface.
 * <p>
 * The application callback handler interface which performs actions on behalf
 * of background agent.
 *
 * @author <?php echo nl(get_current_user()) ?>
 */
public interface <?php echo $client_class ?>Handler {
    /**
     * Set a reference to the <?php echo $client_class ?>Agent.
     *
     * @param agent Handle to the background agent
     */
    void set<?php echo $client_class ?>Agent(<?php echo $client_class ?>Agent agent);
<?php foreach ($actions as $i => $action): ?>

    /**
     * Handle "<?php echo $action ?>" action.
     */
    void <?php echo jvar($action) ?>();
<?php endforeach; ?>
}
