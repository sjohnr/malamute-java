/* =============================================================================
 * MlmClientAgent.java
 *
 * Generated class for MlmClient.
 * -----------------------------------------------------------------------------
 * Copyright (c) the Contributors as noted in the AUTHORS file.
 * This file is part of the Malamute Project.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * =============================================================================
 */
package org.zeromq.malamute.client;

import org.zeromq.malamute.protocol.*;

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
 * MlmClientAgent class.
 *
 * @author sriesenberg
 */
public class MlmClientAgent {
    // Structure of our class
    private MlmClientHandler handler;
    private Context context;
    private Reactor reactor;
    private Socket pipe;
    private Socket inbox;
    private MlmProtoSocket socket;
    private State state = State.START;
    private Event event;
    private Event next;
    private Event exception;
    private Event wakeup;
    private LoopHandler wakeupHandler;
    private long heartbeat;
    private LoopHandler heartbeatHandler;
    private long expiry;
    private LoopHandler expiryHandler;
    private boolean verbose = false;
    private boolean connected = false;
    private boolean terminated = false;
    private Map<String, Object> parameters = new HashMap<>();

    /**
     * Create a MlmClientAgent.
     *
     * @param context The ZeroMQ context
     * @param handler The application callback handler
     */
    public MlmClientAgent(Context context, MlmClientHandler handler) {
        this.context = context;
        this.handler = handler;
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
        this.socket = new MlmProtoSocket(dealer);
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
            heartbeatHandler = null;
            expiryHandler = null;
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

    /**
     * @return The parameters from an API call
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * @return The high level socket, connected to the server
     */
    public MlmProtoSocket getSocket() {
        return socket;
    }

    /**
     * @return The serialization codec
     */
    public MlmProtoCodec getCodec() {
        return socket.getCodec();
    }

    /**
     * @return The CONNECTION OPEN message
     */
    public ConnectionOpenMessage getConnectionOpen() {
        return socket.getCodec().getConnectionOpen();
    }

    /**
     * @param message The CONNECTION OPEN message
     */
    public void setConnectionOpen(ConnectionOpenMessage message) {
        socket.getCodec().setConnectionOpen(message);
    }

    /**
     * @return The CONNECTION PING message
     */
    public ConnectionPingMessage getConnectionPing() {
        return socket.getCodec().getConnectionPing();
    }

    /**
     * @param message The CONNECTION PING message
     */
    public void setConnectionPing(ConnectionPingMessage message) {
        socket.getCodec().setConnectionPing(message);
    }

    /**
     * @return The CONNECTION PONG message
     */
    public ConnectionPongMessage getConnectionPong() {
        return socket.getCodec().getConnectionPong();
    }

    /**
     * @param message The CONNECTION PONG message
     */
    public void setConnectionPong(ConnectionPongMessage message) {
        socket.getCodec().setConnectionPong(message);
    }

    /**
     * @return The CONNECTION CLOSE message
     */
    public ConnectionCloseMessage getConnectionClose() {
        return socket.getCodec().getConnectionClose();
    }

    /**
     * @param message The CONNECTION CLOSE message
     */
    public void setConnectionClose(ConnectionCloseMessage message) {
        socket.getCodec().setConnectionClose(message);
    }

    /**
     * @return The STREAM WRITE message
     */
    public StreamWriteMessage getStreamWrite() {
        return socket.getCodec().getStreamWrite();
    }

    /**
     * @param message The STREAM WRITE message
     */
    public void setStreamWrite(StreamWriteMessage message) {
        socket.getCodec().setStreamWrite(message);
    }

    /**
     * @return The STREAM READ message
     */
    public StreamReadMessage getStreamRead() {
        return socket.getCodec().getStreamRead();
    }

    /**
     * @param message The STREAM READ message
     */
    public void setStreamRead(StreamReadMessage message) {
        socket.getCodec().setStreamRead(message);
    }

    /**
     * @return The STREAM SEND message
     */
    public StreamSendMessage getStreamSend() {
        return socket.getCodec().getStreamSend();
    }

    /**
     * @param message The STREAM SEND message
     */
    public void setStreamSend(StreamSendMessage message) {
        socket.getCodec().setStreamSend(message);
    }

    /**
     * @return The STREAM DELIVER message
     */
    public StreamDeliverMessage getStreamDeliver() {
        return socket.getCodec().getStreamDeliver();
    }

    /**
     * @param message The STREAM DELIVER message
     */
    public void setStreamDeliver(StreamDeliverMessage message) {
        socket.getCodec().setStreamDeliver(message);
    }

    /**
     * @return The MAILBOX SEND message
     */
    public MailboxSendMessage getMailboxSend() {
        return socket.getCodec().getMailboxSend();
    }

    /**
     * @param message The MAILBOX SEND message
     */
    public void setMailboxSend(MailboxSendMessage message) {
        socket.getCodec().setMailboxSend(message);
    }

    /**
     * @return The MAILBOX DELIVER message
     */
    public MailboxDeliverMessage getMailboxDeliver() {
        return socket.getCodec().getMailboxDeliver();
    }

    /**
     * @param message The MAILBOX DELIVER message
     */
    public void setMailboxDeliver(MailboxDeliverMessage message) {
        socket.getCodec().setMailboxDeliver(message);
    }

    /**
     * @return The SERVICE SEND message
     */
    public ServiceSendMessage getServiceSend() {
        return socket.getCodec().getServiceSend();
    }

    /**
     * @param message The SERVICE SEND message
     */
    public void setServiceSend(ServiceSendMessage message) {
        socket.getCodec().setServiceSend(message);
    }

    /**
     * @return The SERVICE OFFER message
     */
    public ServiceOfferMessage getServiceOffer() {
        return socket.getCodec().getServiceOffer();
    }

    /**
     * @param message The SERVICE OFFER message
     */
    public void setServiceOffer(ServiceOfferMessage message) {
        socket.getCodec().setServiceOffer(message);
    }

    /**
     * @return The SERVICE DELIVER message
     */
    public ServiceDeliverMessage getServiceDeliver() {
        return socket.getCodec().getServiceDeliver();
    }

    /**
     * @param message The SERVICE DELIVER message
     */
    public void setServiceDeliver(ServiceDeliverMessage message) {
        socket.getCodec().setServiceDeliver(message);
    }

    /**
     * @return The OK message
     */
    public OkMessage getOk() {
        return socket.getCodec().getOk();
    }

    /**
     * @param message The OK message
     */
    public void setOk(OkMessage message) {
        socket.getCodec().setOk(message);
    }

    /**
     * @return The ERROR message
     */
    public ErrorMessage getError() {
        return socket.getCodec().getError();
    }

    /**
     * @param message The ERROR message
     */
    public void setError(ErrorMessage message) {
        socket.getCodec().setError(message);
    }

    /**
     * @return The CREDIT message
     */
    public CreditMessage getCredit() {
        return socket.getCodec().getCredit();
    }

    /**
     * @param message The CREDIT message
     */
    public void setCredit(CreditMessage message) {
        socket.getCodec().setCredit(message);
    }

    /**
     * @return The CONFIRM message
     */
    public ConfirmMessage getConfirm() {
        return socket.getCodec().getConfirm();
    }

    /**
     * @param message The CONFIRM message
     */
    public void setConfirm(ConfirmMessage message) {
        socket.getCodec().setConfirm(message);
    }

    /**
     * @return The STREAM CANCEL message
     */
    public StreamCancelMessage getStreamCancel() {
        return socket.getCodec().getStreamCancel();
    }

    /**
     * @param message The STREAM CANCEL message
     */
    public void setStreamCancel(StreamCancelMessage message) {
        socket.getCodec().setStreamCancel(message);
    }

    /**
     * Convert a message type to an event.
     *
     * @param messageType The message type
     * @return The event, or null if no matching event name found
     */
    private Event event(MlmProtoCodec.MessageType messageType) {
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
                    case START:
                        switch (event) {
                            case SET_PLAIN_AUTH: {
                                handler.usePlainSecurityMechanism(this);
                                handler.signalSuccess(this);
                                break;
                            }
                            case CONNECT: {
                                handler.rememberClientAddress(this);
                                handler.connectToServerEndpoint(this);
                                handler.setClientAddress(this);
                                handler.useConnectTimeout(this);
                                socket.send(socket.getCodec().getConnectionOpen());
                                state = State.CONNECTING;
                                break;
                            }
                            case BAD_ENDPOINT: {
                                handler.signalBadEndpoint(this);
                                break;
                            }
                            case DESTRUCTOR: {
                                handler.signalSuccess(this);
                                handler.terminate(this);
                                break;
                            }
                            case SET_PRODUCER: {
                                handler.signalFailure(this);
                                break;
                            }
                            case SET_CONSUMER: {
                                handler.signalFailure(this);
                                break;
                            }
                            case REMOVE_CONSUMER: {
                                handler.signalFailure(this);
                                break;
                            }
                            case SET_WORKER: {
                                handler.signalFailure(this);
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                        break;
                    case CONNECTING:
                        switch (event) {
                            case OK: {
                                handler.clientIsConnected(this);
                                handler.signalSuccess(this);
                                state = State.CONNECTED;
                                break;
                            }
                            case EXPIRED: {
                                handler.signalServerNotPresent(this);
                                state = State.START;
                                break;
                            }
                            case HEARTBEAT: {
                                socket.send(socket.getCodec().getConnectionPing());
                                break;
                            }
                            case CONNECTION_PONG: {
                                handler.clientIsConnected(this);
                                state = State.CONNECTED;
                                break;
                            }
                            case ERROR: {
                                handler.checkStatusCode(this);
                                state = State.HAVE_ERROR;
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                        break;
                    case CONNECTED:
                        switch (event) {
                            case SET_PRODUCER: {
                                handler.prepareStreamWriteCommand(this);
                                socket.send(socket.getCodec().getStreamWrite());
                                state = State.CONFIRMING;
                                break;
                            }
                            case SET_CONSUMER: {
                                handler.prepareStreamReadCommand(this);
                                socket.send(socket.getCodec().getStreamRead());
                                state = State.CONFIRMING;
                                break;
                            }
                            case REMOVE_CONSUMER: {
                                handler.prepareStreamCancelCommand(this);
                                socket.send(socket.getCodec().getStreamCancel());
                                state = State.CONFIRMING;
                                break;
                            }
                            case SET_WORKER: {
                                handler.prepareServiceOfferCommand(this);
                                socket.send(socket.getCodec().getServiceOffer());
                                state = State.CONFIRMING;
                                break;
                            }
                            case DESTRUCTOR: {
                                socket.send(socket.getCodec().getConnectionClose());
                                state = State.TERMINATING;
                                break;
                            }
                            case BAD_PATTERN: {
                                handler.signalBadPattern(this);
                                break;
                            }
                            case STREAM_DELIVER: {
                                Message streamDeliver = new Message("STREAM DELIVER");
                                streamDeliver.addString(socket.getCodec().getStreamDeliver().getSender());
                                streamDeliver.addString(socket.getCodec().getStreamDeliver().getAddress());
                                streamDeliver.addString(socket.getCodec().getStreamDeliver().getSubject());
                                streamDeliver.addFrames(socket.getCodec().getStreamDeliver().getContent());
                                inbox.send(streamDeliver);
                                break;
                            }
                            case MAILBOX_DELIVER: {
                                Message mailboxDeliver = new Message("MAILBOX DELIVER");
                                mailboxDeliver.addString(socket.getCodec().getMailboxDeliver().getSender());
                                mailboxDeliver.addString(socket.getCodec().getMailboxDeliver().getAddress());
                                mailboxDeliver.addString(socket.getCodec().getMailboxDeliver().getSubject());
                                mailboxDeliver.addString(socket.getCodec().getMailboxDeliver().getTracker());
                                mailboxDeliver.addFrames(socket.getCodec().getMailboxDeliver().getContent());
                                inbox.send(mailboxDeliver);
                                break;
                            }
                            case SERVICE_DELIVER: {
                                Message serviceDeliver = new Message("SERVICE DELIVER");
                                serviceDeliver.addString(socket.getCodec().getServiceDeliver().getSender());
                                serviceDeliver.addString(socket.getCodec().getServiceDeliver().getAddress());
                                serviceDeliver.addString(socket.getCodec().getServiceDeliver().getSubject());
                                serviceDeliver.addString(socket.getCodec().getServiceDeliver().getTracker());
                                serviceDeliver.addFrames(socket.getCodec().getServiceDeliver().getContent());
                                inbox.send(serviceDeliver);
                                break;
                            }
                            case HEARTBEAT: {
                                socket.send(socket.getCodec().getConnectionPing());
                                break;
                            }
                            case CONNECTION_PONG: {
                                handler.clientIsConnected(this);
                                state = State.CONNECTED;
                                break;
                            }
                            case EXPIRED: {
                                handler.serverHasGoneOffline(this);
                                state = State.DISCONNECTED;
                                break;
                            }
                            case ERROR: {
                                handler.checkStatusCode(this);
                                state = State.HAVE_ERROR;
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                        break;
                    case CONFIRMING:
                        switch (event) {
                            case EXPIRED: {
                                handler.serverHasGoneOffline(this);
                                handler.signalFailure(this);
                                state = State.DISCONNECTED;
                                break;
                            }
                            case OK: {
                                handler.signalSuccess(this);
                                state = State.CONNECTED;
                                break;
                            }
                            case ERROR: {
                                handler.signalFailure(this);
                                handler.checkStatusCode(this);
                                state = State.HAVE_ERROR;
                                break;
                            }
                            case CONNECTION_PONG: {
                                break;
                            }
                            case STREAM_DELIVER: {
                                handler.passStreamMessageToApp(this);
                                break;
                            }
                            case MAILBOX_DELIVER: {
                                handler.passMailboxMessageToApp(this);
                                break;
                            }
                            case SERVICE_DELIVER: {
                                handler.passServiceMessageToApp(this);
                                break;
                            }
                            case HEARTBEAT: {
                                socket.send(socket.getCodec().getConnectionPing());
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                        break;
                    case TERMINATING:
                        switch (event) {
                            case OK: {
                                handler.signalSuccess(this);
                                handler.terminate(this);
                                break;
                            }
                            case EXPIRED: {
                                handler.signalFailure(this);
                                handler.terminate(this);
                                break;
                            }
                            case CONNECTION_PONG: {
                                break;
                            }
                            case ERROR: {
                                handler.signalFailure(this);
                                handler.terminate(this);
                                break;
                            }
                            case HEARTBEAT: {
                                socket.send(socket.getCodec().getConnectionPing());
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                        break;
                    case RECONNECTING:
                        switch (event) {
                            case OK: {
                                handler.clientIsConnected(this);
                                handler.getFirstReplayCommand(this);
                                break;
                            }
                            case ERROR: {
                                break;
                            }
                            case SET_PRODUCER: {
                                socket.send(socket.getCodec().getStreamWrite());
                                handler.getNextReplayCommand(this);
                                break;
                            }
                            case SET_CONSUMER: {
                                socket.send(socket.getCodec().getStreamRead());
                                handler.getNextReplayCommand(this);
                                break;
                            }
                            case REMOVE_CONSUMER: {
                                socket.send(socket.getCodec().getStreamCancel());
                                handler.getNextReplayCommand(this);
                                break;
                            }
                            case SET_WORKER: {
                                socket.send(socket.getCodec().getServiceOffer());
                                handler.getNextReplayCommand(this);
                                break;
                            }
                            case REPLAY_READY: {
                                handler.clientIsConnected(this);
                                state = State.CONNECTED;
                                break;
                            }
                            case CONNECTION_PONG: {
                                break;
                            }
                            case HEARTBEAT: {
                                socket.send(socket.getCodec().getConnectionPing());
                                break;
                            }
                            case EXPIRED: {
                                handler.serverHasGoneOffline(this);
                                state = State.DISCONNECTED;
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                        break;
                    case DISCONNECTED:
                        switch (event) {
                            case SET_PRODUCER: {
                                handler.signalFailure(this);
                                break;
                            }
                            case SET_CONSUMER: {
                                handler.signalFailure(this);
                                break;
                            }
                            case REMOVE_CONSUMER: {
                                handler.signalFailure(this);
                                break;
                            }
                            case SET_WORKER: {
                                handler.signalFailure(this);
                                break;
                            }
                            case DESTRUCTOR: {
                                handler.signalSuccess(this);
                                handler.terminate(this);
                                break;
                            }
                            case HEARTBEAT: {
                                socket.send(socket.getCodec().getConnectionPing());
                                break;
                            }
                            case CONNECTION_PONG: {
                                handler.clientIsConnected(this);
                                state = State.CONNECTED;
                                break;
                            }
                            case EXPIRED: {
                                handler.serverHasGoneOffline(this);
                                state = State.DISCONNECTED;
                                break;
                            }
                            case ERROR: {
                                handler.checkStatusCode(this);
                                state = State.HAVE_ERROR;
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                        break;
                    case HAVE_ERROR:
                        switch (event) {
                            case COMMAND_INVALID: {
                                handler.setClientAddress(this);
                                handler.useConnectTimeout(this);
                                socket.send(socket.getCodec().getConnectionOpen());
                                state = State.RECONNECTING;
                                break;
                            }
                            case FAILED: {
                                handler.signalFailure(this);
                                handler.terminate(this);
                                break;
                            }
                            case OTHER: {
                                handler.announceUnhandledError(this);
                                handler.terminate(this);
                                break;
                            }
                        }
                        break;
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
        START,
        CONNECTING,
        CONNECTED,
        CONFIRMING,
        TERMINATING,
        RECONNECTING,
        DISCONNECTED,
        HAVE_ERROR
    }

    /**
     * Events we can process.
     */
    public enum Event {
        SET_PLAIN_AUTH,
        CONNECT,
        BAD_ENDPOINT,
        DESTRUCTOR,
        SET_PRODUCER,
        SET_CONSUMER,
        REMOVE_CONSUMER,
        SET_WORKER,
        OK,
        EXPIRED,
        BAD_PATTERN,
        STREAM_DELIVER,
        MAILBOX_DELIVER,
        SERVICE_DELIVER,
        ERROR,
        CONNECTION_PONG,
        REPLAY_READY,
        HEARTBEAT,
        COMMAND_INVALID,
        FAILED,
        OTHER
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
                case "SET PLAIN AUTH":
                    parameters.put("username", message.popString());
                    parameters.put("password", message.popString());
                    executeInternal(Event.SET_PLAIN_AUTH);
                    break;
                case "CONNECT":
                    parameters.put("endpoint", message.popString());
                    parameters.put("timeout", message.popInt());
                    parameters.put("address", message.popString());
                    executeInternal(Event.CONNECT);
                    break;
                case "DESTRUCTOR":
                    executeInternal(Event.DESTRUCTOR);
                    break;
                case "SET PRODUCER":
                    parameters.put("stream", message.popString());
                    executeInternal(Event.SET_PRODUCER);
                    break;
                case "SET CONSUMER":
                    parameters.put("stream", message.popString());
                    parameters.put("pattern", message.popString());
                    executeInternal(Event.SET_CONSUMER);
                    break;
                case "REMOVE CONSUMER":
                    parameters.put("stream", message.popString());
                    executeInternal(Event.REMOVE_CONSUMER);
                    break;
                case "SET WORKER":
                    parameters.put("address", message.popString());
                    parameters.put("pattern", message.popString());
                    executeInternal(Event.SET_WORKER);
                    break;
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
                case "STREAM SEND": {
                    StreamSendMessage outgoing = new StreamSendMessage();
                    outgoing.setSubject(message.popString());
                    outgoing.setContent(message);
                    getSocket().send(outgoing);
                    break;
                }
                case "MAILBOX SEND": {
                    MailboxSendMessage outgoing = new MailboxSendMessage();
                    outgoing.setAddress(message.popString());
                    outgoing.setSubject(message.popString());
                    outgoing.setTracker(message.popString());
                    outgoing.setTimeout(message.popInt());
                    outgoing.setContent(message);
                    getSocket().send(outgoing);
                    break;
                }
                case "SERVICE SEND": {
                    ServiceSendMessage outgoing = new ServiceSendMessage();
                    outgoing.setAddress(message.popString());
                    outgoing.setSubject(message.popString());
                    outgoing.setTracker(message.popString());
                    outgoing.setTimeout(message.popInt());
                    outgoing.setContent(message);
                    getSocket().send(outgoing);
                    break;
                }
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
            MlmProtoCodec.MessageType messageType;
            while ((messageType = getSocket().receive(MessageFlag.DONT_WAIT)) != null) {
                // Any input from server counts as activity
                if (expiryHandler != null) {
                    reactor.cancel(expiryHandler);
                    if (expiry > 0) {
                        reactor.addTimer(expiry, 1, expiryHandler);
                    }
                }

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
}
