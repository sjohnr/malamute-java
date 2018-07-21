/* =============================================================================
 * MlmServerAgent.java
 *
 * Generated class for MlmServer.
 * -----------------------------------------------------------------------------
 * Copyright (c) the Contributors as noted in the AUTHORS file.
 * This file is part of the Malamute Project.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * =============================================================================
 */
package org.zeromq.malamute.server;

import org.zeromq.malamute.protocol.*;

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
 * MlmServerAgent class.
 *
 * @author sriesenberg
 */
public class MlmServerAgent {
    // Structure of our class
    private MlmServerHandler handler;
    private Context context;
    private Reactor reactor;
    private Socket pipe;
    private MlmProtoSocket socket;
    private int port;
    private boolean verbose = false;
    private boolean connected = false;
    private boolean terminated = false;
    private int clientId;
    private Map<String, Client> clients = new HashMap<>();

    /**
     * Create a MlmServerAgent.
     *
     * @param context The ZeroMQ context
     * @param handler The application callback handler
     */
    public MlmServerAgent(Context context, MlmServerHandler handler) {
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
        this.socket = new MlmProtoSocket(router);
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
                    case START:
                        switch (client.event) {
                            case CONNECTION_OPEN: {
                                handler.registerNewClient(client);
                                socket.send(socket.getCodec().getOk());
                                handler.checkForMailboxMessages(client);
                                client.state = State.CONNECTED;
                                break;
                            }
                            default: {
                                handler.signalCommandInvalid(client);
                                socket.send(socket.getCodec().getError());
                                break;
                            }
                            case CONNECTION_CLOSE: {
                                socket.send(socket.getCodec().getOk());
                                handler.clientClosedConnection(client);
                                handler.deregisterTheClient(client);
                                handler.terminate(client);
                                break;
                            }
                            case STREAM_MESSAGE: {
                                handler.getMessageToDeliver(client);
                                socket.send(socket.getCodec().getStreamDeliver());
                                client.state = State.CONNECTED;
                                break;
                            }
                            case MAILBOX_MESSAGE: {
                                handler.getMessageToDeliver(client);
                                socket.send(socket.getCodec().getMailboxDeliver());
                                handler.checkForMailboxMessages(client);
                                client.state = State.CONNECTED;
                                break;
                            }
                            case SERVICE_MESSAGE: {
                                handler.getMessageToDeliver(client);
                                socket.send(socket.getCodec().getServiceDeliver());
                                client.state = State.CONNECTED;
                                break;
                            }
                            case EXPIRED: {
                                handler.clientExpired(client);
                                handler.deregisterTheClient(client);
                                handler.terminate(client);
                                break;
                            }
                            case EXCEPTION: {
                                handler.signalOperationFailed(client);
                                socket.send(socket.getCodec().getError());
                                handler.clientHadException(client);
                                handler.deregisterTheClient(client);
                                handler.terminate(client);
                                break;
                            }
                        }
                        break;
                    case CONNECTED:
                        switch (client.event) {
                            case STREAM_WRITE: {
                                handler.storeStreamWriter(client);
                                socket.send(socket.getCodec().getOk());
                                break;
                            }
                            case STREAM_READ: {
                                handler.storeStreamReader(client);
                                socket.send(socket.getCodec().getOk());
                                break;
                            }
                            case STREAM_CANCEL: {
                                handler.cancelStreamReader(client);
                                socket.send(socket.getCodec().getOk());
                                break;
                            }
                            case STREAM_SEND: {
                                handler.writeMessageToStream(client);
                                break;
                            }
                            case MAILBOX_SEND: {
                                handler.writeMessageToMailbox(client);
                                break;
                            }
                            case SERVICE_SEND: {
                                handler.writeMessageToService(client);
                                break;
                            }
                            case SERVICE_OFFER: {
                                handler.storeServiceOffer(client);
                                socket.send(socket.getCodec().getOk());
                                handler.dispatchTheService(client);
                                break;
                            }
                            case CONFIRM: {
                                handler.haveMessageConfirmation(client);
                                break;
                            }
                            case CREDIT: {
                                handler.creditTheClient(client);
                                break;
                            }
                            case CONNECTION_PING: {
                                socket.send(socket.getCodec().getConnectionPong());
                                break;
                            }
                            case CONNECTION_CLOSE: {
                                socket.send(socket.getCodec().getOk());
                                handler.clientClosedConnection(client);
                                handler.deregisterTheClient(client);
                                handler.terminate(client);
                                break;
                            }
                            case STREAM_MESSAGE: {
                                handler.getMessageToDeliver(client);
                                socket.send(socket.getCodec().getStreamDeliver());
                                client.state = State.CONNECTED;
                                break;
                            }
                            case MAILBOX_MESSAGE: {
                                handler.getMessageToDeliver(client);
                                socket.send(socket.getCodec().getMailboxDeliver());
                                handler.checkForMailboxMessages(client);
                                client.state = State.CONNECTED;
                                break;
                            }
                            case SERVICE_MESSAGE: {
                                handler.getMessageToDeliver(client);
                                socket.send(socket.getCodec().getServiceDeliver());
                                client.state = State.CONNECTED;
                                break;
                            }
                            case EXPIRED: {
                                handler.clientExpired(client);
                                handler.deregisterTheClient(client);
                                handler.terminate(client);
                                break;
                            }
                            case EXCEPTION: {
                                handler.signalOperationFailed(client);
                                socket.send(socket.getCodec().getError());
                                handler.clientHadException(client);
                                handler.deregisterTheClient(client);
                                handler.terminate(client);
                                break;
                            }
                            default: {
                                handler.signalCommandInvalid(client);
                                socket.send(socket.getCodec().getError());
                                handler.deregisterTheClient(client);
                                handler.terminate(client);
                                break;
                            }
                        }
                        break;
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
        START,
        CONNECTED
    }

    /**
     * Events we can process.
     */
    public enum Event {
        CONNECTION_OPEN,
        STREAM_WRITE,
        STREAM_READ,
        STREAM_CANCEL,
        STREAM_SEND,
        MAILBOX_SEND,
        SERVICE_SEND,
        SERVICE_OFFER,
        CONFIRM,
        CREDIT,
        CONNECTION_PING,
        CONNECTION_CLOSE,
        STREAM_MESSAGE,
        MAILBOX_MESSAGE,
        SERVICE_MESSAGE,
        EXPIRED,
        EXCEPTION
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
        private long expiry;
        private LoopHandler expiryHandler;

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
            MlmProtoCodec.MessageType messageType;
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

                // Any input from server counts as activity
                if (client.expiryHandler != null) {
                    reactor.cancel(client.expiryHandler);
                    if (client.expiry > 0) {
                        reactor.addTimer(client.expiry, 1, client.expiryHandler);
                    }
                }

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
}
