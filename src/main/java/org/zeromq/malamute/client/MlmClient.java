/* =============================================================================
 * MlmClient.java
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

import org.zeromq.ContextFactory;
import org.zeromq.api.Context;
import org.zeromq.api.Message;
import org.zeromq.api.Socket;
import org.zeromq.api.SocketType;

import java.util.Arrays;

/**
 * MlmClient class.
 * <p>
 * This is a client implementation of the Malamute Protocol.
 *
 * @author sriesenberg
 */
public class MlmClient {
    // Structure of our class
    private Context context;
    private Socket pipe;
    private Socket inbox;
    private MlmClientAgent agent;

    // Fields received in messages and replies
    private String sender;
    private String address;
    private String subject;
    private Message content;
    private String tracker;
    private Integer status;
    private String reason;

    /**
     * Create a MlmClient.
     *
     * @param handler The application callback handler
     */
    public MlmClient(MlmClientHandler handler) {
        this(ContextFactory.createContext(1), handler);
    }

    /**
     * Create a MlmClient.
     *
     * @param context The 0MQ context
     * @param handler The application callback handler
     */
    public MlmClient(Context context, MlmClientHandler handler) {
        this.context = context;
        this.agent = new MlmClientAgent(context, handler);
        this.pipe = context.buildSocket(SocketType.PAIR).connect(String.format("inproc://pipe-%s", agent.toString()));
        this.inbox = context.buildSocket(SocketType.PAIR).connect(String.format("inproc://inbox-%s", agent.toString()));
    }

    /**
     * Destroy the client.
     */
    public void close() {
        pipe.send(new Message("$TERM"));
        
    }


    /**
     * @return Last received sender
     */
    public String getSender() {
        return sender;
    }

    /**
     * @return Last received address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @return Last received subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @return Last received content
     */
    public Message getContent() {
        return content;
    }

    /**
     * @return Last received tracker
     */
    public String getTracker() {
        return tracker;
    }

    /**
     * @return Last received status
     */
    public Integer getStatus() {
        return status;
    }

    /**
     * @return Last received reason
     */
    public String getReason() {
        return reason;
    }

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

    /**
     * Set PLAIN authentication username and password. If you do not call this,
     * the client will use NULL authentication. TODO: add "set curve auth".
     *
     * @return The status field of the reply
     */
    public Integer setPlainAuth(String username, String password) {
        Message message = new Message("SET PLAIN AUTH");
        message.addString(username);
        message.addString(password);
        pipe.send(message);
        accept("SUCCESS", "FAILURE");

        return status;
    }

    /**
     * Connect to server endpoint, with specified timeout in msecs (zero means
     * wait forever). Constructor succeeds if connection is successful. The caller
     * may specify its address.
     *
     * @return The status field of the reply
     */
    public Integer connect(String endpoint, Integer timeout, String address) {
        Message message = new Message("CONNECT");
        message.addString(endpoint);
        message.addInt(timeout);
        message.addString(address);
        pipe.send(message);
        accept("SUCCESS", "FAILURE");

        return status;
    }

    /**
     * Disconnect from server. Waits for a short timeout for confirmation from
     * the server, then disconnects anyhow.
     *
     * @return The status field of the reply
     */
    public Integer destructor() {
        Message message = new Message("DESTRUCTOR");
        pipe.send(message);
        accept("SUCCESS", "FAILURE");

        return status;
    }

    /**
     * Prepare to publish to a specified stream. After this, all messages
     * are sent to this stream exclusively.
     *
     * @return The status field of the reply
     */
    public Integer setProducer(String stream) {
        Message message = new Message("SET PRODUCER");
        message.addString(stream);
        pipe.send(message);
        accept("SUCCESS", "FAILURE");

        return status;
    }

    /**
     * Consume messages with matching subjects. The pattern is a regular
     * expression using the CZMQ zrex syntax. The most useful elements are:
     * ^ and $ to match the start and end, . to match any character, \s and
     * \S to match whitespace and non-whitespace, \d and \D to match a digit
     * and non-digit, \a and \A to match alphabetic and non-alphabetic, \w
     * and \W to match alphanumeric and non-alphanumeric, + for one or more
     * repetitions, * for zero or more repetitions, and ( ) to create groups.
     * Returns true if subscription was successful, else false.
     *
     * @return The status field of the reply
     */
    public Integer setConsumer(String stream, String pattern) {
        Message message = new Message("SET CONSUMER");
        message.addString(stream);
        message.addString(pattern);
        pipe.send(message);
        accept("SUCCESS", "FAILURE");

        return status;
    }

    /**
     * Remove all subscriptions to a stream.
     *
     * @return The status field of the reply
     */
    public Integer removeConsumer(String stream) {
        Message message = new Message("REMOVE CONSUMER");
        message.addString(stream);
        pipe.send(message);
        accept("SUCCESS", "FAILURE");

        return status;
    }

    /**
     * Offer a particular named service, where the pattern matches request
     * subjects using the CZMQ zrex syntax.
     *
     * @return The status field of the reply
     */
    public Integer setWorker(String address, String pattern) {
        Message message = new Message("SET WORKER");
        message.addString(address);
        message.addString(pattern);
        pipe.send(message);
        accept("SUCCESS", "FAILURE");

        return status;
    }

    private void accept(String... replies) {
        Message message = pipe.receiveMessage();
        String reply = message.popString();
        assert Arrays.asList(replies).contains(reply);
        switch (reply) {
            case "SUCCESS":
                status = message.popInt();
                break;
            case "FAILURE":
                status = message.popInt();
                reason = message.popString();
                break;
        }
    }

    /**
     * Send a STREAM SEND message to the server.
     */
    public void send(String subject, Message content) {
        Message message = new Message("STREAM SEND");
        message.addString(subject);
        message.addFrames(content);

        inbox.send(message);
    }

    /**
     * Send a MAILBOX SEND message to the server.
     */
    public void sendto(String address, String subject, String tracker, Integer timeout, Message content) {
        Message message = new Message("MAILBOX SEND");
        message.addString(address);
        message.addString(subject);
        message.addString(tracker);
        message.addInt(timeout);
        message.addFrames(content);

        inbox.send(message);
    }

    /**
     * Send a SERVICE SEND message to the server.
     */
    public void sendfor(String address, String subject, String tracker, Integer timeout, Message content) {
        Message message = new Message("SERVICE SEND");
        message.addString(address);
        message.addString(subject);
        message.addString(tracker);
        message.addInt(timeout);
        message.addFrames(content);

        inbox.send(message);
    }

    /**
     * Receive a message from the server.
     *
     * @return The MessageType of the message
     */
    public MessageType receive() {
        Message message = inbox.receiveMessage();
        MessageType type = MessageType.valueOf(message.popString());
        switch (type) {
            case STREAM_DELIVER:
                sender = message.popString();
                address = message.popString();
                subject = message.popString();
                content = message;
                break;
            case MAILBOX_DELIVER:
                sender = message.popString();
                address = message.popString();
                subject = message.popString();
                tracker = message.popString();
                content = message;
                break;
            case SERVICE_DELIVER:
                sender = message.popString();
                address = message.popString();
                subject = message.popString();
                tracker = message.popString();
                content = message;
                break;
        }

        return type;
    }

    /**
     * Enumeration of message types.
     */
    public enum MessageType {
        STREAM_DELIVER,
        MAILBOX_DELIVER,
        SERVICE_DELIVER
    }
}
