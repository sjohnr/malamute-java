<?php

include 'functions.php';
$filename = $argv[1];
$filepath = dirname($filename);
$class = read_xml($filename);
$proto = read_xml($filepath . '/' . $class['protocol_class'] . '.xml');

$path = $class['package'];
$server_class = jclass($class['name']);
$proto_class = jclass($proto['name']);
$states = get_all_states($class);
$events = get_all_events($class);
$actions = get_all_actions($class);
$messages = get_messages_by_name($proto);

resolve_includes($class);
create_directories($path);

?>
<?php output("../src/main/java/${path}/${server_class}Agent.java") ?>
/* =============================================================================
 * <?php echo $server_class ?>Agent.java
 *
 * Generated class for <?php echo $server_class ?>.
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
import org.zeromq.api.Message.Frame;
import org.zeromq.api.MessageFlag;
import org.zeromq.api.Reactor;
import org.zeromq.api.Socket;
import org.zeromq.api.SocketType;

import java.util.HashMap;
import java.util.Map;

/**
 * <?php echo $server_class ?>Agent class.
 *
 * @author <?php echo nl(get_current_user()) ?>
 */
public class <?php echo $server_class ?>Agent {
    // Structure of our class
    private <?php echo $server_class ?>Handler handler;
    private Context context;
    private Reactor reactor;
    private Socket pipe;
    private <?php echo $proto_class ?>Socket socket;
    private int port;
<?php if (array_search('heartbeat', $events)): ?>
    private long heartbeat;
    private LoopHandler heartbeatHandler;
<?php endif; ?>
    private boolean verbose = false;
    private boolean connected = false;
    private boolean terminated = false;
    private int clientId;
    private Map<String, Client> clients = new HashMap<>();

    /**
     * Create a <?php echo $server_class ?>Agent.
     *
     * @param context The ZeroMQ context
     * @param handler The application callback handler
     */
    public <?php echo $server_class ?>Agent(Context context, <?php echo $server_class ?>Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    /**
     * Start the client's background agent.
     */
    public void start() {
        // Socket must be bound to a real endpoint in application code by
        // message from server interface.
        Socket router = context.buildSocket(SocketType.ROUTER)
            .withSendHighWatermark(Long.MAX_VALUE)
            .withReceiveHighWatermark(Long.MAX_VALUE)
            .bind(String.format("inproc://router-%s", this.toString()));

        this.pipe = context.buildSocket(SocketType.PAIR).bind(String.format("inproc://pipe-%s", this.toString()));
        this.socket = new <?php echo $proto_class ?>Socket(router);
        this.reactor = context.buildReactor()
            .withInPollable(pipe, new PipeHandler())
            .withInPollable(router, new RouterHandler())
            .build();

        // Start the reactor
        reactor.start();
    }

    /**
     * Stop the client's background agent and clean up references.
     */
    public void stop() {
        try {
            reactor.stop();
            socket.close();
            pipe.close();
        } finally {
            clients.clear();
<?php if (array_search('heartbeat', $events)): ?>
            heartbeatHandler = null;
<?php endif; ?>
            pipe = null;
            socket = null;
            reactor = null;
        }
    }

    /**
     * Bind to a port using the given endpoint.
     *
     * @param endpoint The endpoint to bind to
     * @return The bound port
     */
    public int bind(String endpoint) {
        return socket.getSocket().getZMQSocket().bindToRandomPort(endpoint);
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
    private void executeInternal(Client client, Event triggeredEvent) {
        client.next = triggeredEvent;

        // Cancel wakeup timer, if any was pending
        if (client.wakeupHandler != null) {
            reactor.cancel(client.wakeupHandler);
            client.wakeupHandler = null;
        }

        while (!terminated && client.next != null) {
            client.event = client.next;
            client.next = null;
            client.exception = null;

            try {
                switch (client.state) {
<?php foreach ($states as $state): ?>
                    case <?php echo cconst($state['name']) ?>:
                        switch (client.event) {
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
<?php                 case 'stop': ?>
                                stop();
<?php                     break; ?>
<?php                 default: ?>
                                handler.<?php echo jvar($action['name']) ?>(client);
<?php                     break; ?>
<?php             endswitch; ?>
<?php         endforeach; ?>
<?php         if ($event['next']): ?>
                                client.state = State.<?php echo cconst($event['next']) ?>;
<?php         endif; ?>
<?php         if ($event['trigger']): ?>
                                client.next = Event.<?php echo cconst($event['trigger']) ?>;
<?php         endif; ?>
                                break;
                            }
<?php     endforeach; ?>
                        }
                        break;
<?php endforeach; ?>
                }
            } catch (HaltException ex) {
                client.next = client.exception;
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
     * Connected client.
     */
    public class Client {
        private String identity;
        private Frame address;
        private int clientId;
        private State state = State.START;
        private Event event;
        private Event next;
        private Event exception;
        private Event wakeup;
        private LoopHandler wakeupHandler;
<?php if (array_search('expired', $events)): ?>
        private long expiry;
        private LoopHandler expiryHandler;
<?php endif; ?>

        /**
         * @return The client's identity
         */
        public String getIdentity() {
            return identity;
        }

        /**
         * @return The client's address
         */
        public Frame getAddress() {
            return address;
        }

        /**
         * @return The client's identifier counter
         */
        public int getClientId() {
            return clientId;
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
         * Set (trigger) the next event to be handled by the state machine for
         * a specified client.
         *
         * @param client The client on which to trigger the event
         * @param next The next event for that client
         */
        public void sendEvent(Client client, Event next) {
            executeInternal(client, next);
        }

        /**
         * Set (trigger) the next event to be handled by the state machine for
         * all clients.
         *
         * @param next The next event for that client
         */
        public void broadcastEvent(Event next) {
            for (Client client : clients.values()) {
                executeInternal(client, next);
            }
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
                wakeupHandler = new WakeupHandler(this);
                reactor.addTimer(wakeup, 1, wakeupHandler);
            }
        }
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
                expiryHandler = new ExpiryHandler(this);
                reactor.addTimer(expiry, 1, expiryHandler);
            }
        }
<?php endif; ?>
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
                case "BIND":
                    port = bind(message.popString());
                    socket.send(new Message(port));
                    break;
                case "PORT":
                    socket.send(new Message(port));
                    break;
                default:
                    socket.send(handler.handleCommand(method, message));
                    break;
            }
        }
    }

    /**
     * Handler for messages from server.
     */
    private class RouterHandler extends LoopAdapter {
        @Override
        protected void execute(Reactor reactor, Socket socket) {
            // We will process as many messages as we can, to reduce the overhead
            // of polling and the reactor.
            <?php echo $proto_class ?>Codec.MessageType messageType;
            while ((messageType = getSocket().receive(MessageFlag.DONT_WAIT)) != null) {
                Frame address = getSocket().getAddress();
                String identity = address.toString();
                Client client = clients.get(identity);
                if (client == null) {
                    client = new Client();
                    client.address = address;
                    client.identity = identity;
                    clients.put(identity, client);
                }
<?php if (array_search('expired', $events)): ?>

                // Any input from server counts as activity
                if (client.expiryHandler != null) {
                    reactor.cancel(client.expiryHandler);
                    if (client.expiry > 0) {
                        reactor.addTimer(client.expiry, 1, client.expiryHandler);
                    }
                }
<?php endif; ?>

                Event event = event(messageType);
                executeInternal(client, event);
            }
        }
    }

    /**
     * Handler for wakeup timer.
     */
    private class WakeupHandler extends LoopAdapter {
        private Client client;

        public WakeupHandler(Client client) {
            this.client = client;
        }

        @Override
        protected void execute(Reactor reactor, Socket socket) {
            Event event = client.wakeup;
            client.wakeup = null;
            executeInternal(client, event);
        }
    }
<?php if (array_search('expired', $events)): ?>

    /**
     * Handler for expiry timer.
     */
    private class ExpiryHandler extends LoopAdapter {
        private Client client;

        public ExpiryHandler(Client client) {
            this.client = client;
        }

        @Override
        protected void execute(Reactor reactor, Socket socket) {
            executeInternal(client, Event.EXPIRED);
            if (!terminated && client.expiry > 0) {
                reactor.addTimer(client.expiry, 1, this);
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
<?php output("../src/main/java/${path}/${server_class}.java") ?>
/* =============================================================================
 * <?php echo $server_class ?>.java
 *
 * Generated class for <?php echo $server_class ?>.
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

/**
 * <?php echo $server_class ?> class.
 * <p>
 * <?php echo trim((string) $class) ?>.
 *
 * @author <?php echo nl(get_current_user()) ?>
 */
public class <?php echo $server_class ?> {
    // Structure of our class
    private Context context;
    private Socket pipe;
    private <?php echo $server_class ?>Agent agent;

    /**
     * Create a <?php echo $server_class ?>.
     *
     * @param handler The application callback handler
     */
    public <?php echo $server_class ?>(<?php echo $server_class ?>Handler handler) {
        this(ContextFactory.createContext(1), handler);
    }

    /**
     * Create a <?php echo $server_class ?>.
     *
     * @param context The 0MQ context
     * @param handler The application callback handler
     */
    public <?php echo $server_class ?>(Context context, <?php echo $server_class ?>Handler handler) {
        this.context = context;
        this.agent = new <?php echo $server_class ?>Agent(context, handler);
        this.pipe = context.buildSocket(SocketType.PAIR).connect(String.format("inproc://pipe-%s", agent.toString()));
    }

    /**
     * Bind to the given endpoint.
     *
     * @param endpoint The endpoint to bind to
     * @return The ephemeral port that is bound to if ephemeral, else 0
     */
    public int bind(String endpoint) {
        pipe.send(new Message("BIND").addString(endpoint));
        return pipe.receiveMessage().popInt();
    }

    /**
     * Ask the background agent what port it is bound to.
     *
     * @return The port that the background agent is bound to
     */
    public int getPort() {
        pipe.send(new Message("PORT"));
        return pipe.receiveMessage().popInt();
    }

    /**
     * Set the verbose logging flag of the background agent.
     *
     * @param verbose The flag controlling whether verbose logging is enabled
     */
    public void setVerbose(boolean verbose) {
        pipe.send(new Message(verbose ? 1 : 0));
    }

    /**
     * Send a custom command to the background agent.
     *
     * @param command The command to execute
     * @param message The message to send
     * @return The reply from the background agent
     */
    public Message execute(String command, Message message) {
        pipe.send(new Message(command).addFrames(message));
        return pipe.receiveMessage();
    }
}
<?php output("../src/main/java/${path}/${server_class}Handler.java") ?>
/* =============================================================================
 * <?php echo $server_class ?>.java
 *
 * Generated class for <?php echo $server_class ?>.
 * -----------------------------------------------------------------------------
 * <?php echo nl(block_comment($class['license'])) ?>
 * =============================================================================
 */
package <?php echo package($class['package']) ?>;

import org.zeromq.api.Message;

/**
 * <?php echo $server_class ?>Handler interface.
 * <p>
 * The application callback handler interface which performs actions on behalf
 * of background agent.
 *
 * @author <?php echo nl(get_current_user()) ?>
 */
public interface <?php echo $server_class ?>Handler {
<?php foreach ($actions as $i => $action): ?>
<?php echo !first($i) ? nl() : '' ?>
    /**
     * <?php echo nl(ccomment($action)) ?>
     *
     * @param client Handle to the current client
     */
    void <?php echo jvar($action) ?>(<?php echo $server_class ?>Agent.Client client);
<?php endforeach; ?>

    /**
     * Handle a custom command from the application.
     *
     * @param command The command to execute
     * @param message The message
     * @return A reply to send
     */
    Message handleCommand(String command, Message message);
}
