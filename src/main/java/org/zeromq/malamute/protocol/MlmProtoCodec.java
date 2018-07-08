/* ============================================================================
 * MlmProtoCodec.java
 *
 * Generated codec class for MlmProtoSocket
 * ----------------------------------------------------------------------------
 * Copyright (c) the Contributors as noted in the AUTHORS file.       
 * This file is part of the Malamute Project.                         
 *                                                                    
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.           
 * ============================================================================
 */
package org.zeromq.malamute.protocol;

import org.zeromq.api.Message;
import org.zeromq.api.Message.Frame;
import org.zeromq.api.Message.FrameBuilder;

/**
 * MlmProtoCodec class.
 *
 * The specification for this class is as follows:
 * <pre class="text">
 *  CONNECTION_OPEN - Client opens a connection to the server. Client can ask for a mailbox
by specifying an address. If mailbox does not exist, server creates it.
Server replies with OK or ERROR.
 *    protocol                     string
 *    version                      number 2
 *    address                      string
 *  CONNECTION_PING - Client pings the server. Server replies with CONNECTION-PONG, or
ERROR with status COMMAND-INVALID if the client is not recognized
(e.g. after a server restart or network recovery).
 *  CONNECTION_PONG - Server replies to a client connection ping.
 *  CONNECTION_CLOSE - Client closes the connection. This is polite though not mandatory.
Server will reply with OK or ERROR.
 *  STREAM_WRITE - Client declares intention to write to a stream. If stream does not
exist, server creates it. A client can write to a single stream at
once. Server replies with OK or ERROR.
 *    stream                       string
 *  STREAM_READ - Client opens a stream for reading, specifying a pattern to match
message subjects. An empty pattern matches everything. If the stream
does not exist, the server creates it. A client can read from many
streams at once. It can also read from the same stream many times,
with different patterns. Server replies with OK or ERROR.
 *    stream                       string
 *    pattern                      string
 *  STREAM_SEND - Client publishes a message to the current stream. A stream message
has a subject, and a content of zero or more frames. Server does not
reply to this message. The subject is used to match messages to
readers. A reader will receive a given message at most once.
 *    subject                      string
 *    content                      msg
 *  STREAM_DELIVER - Server delivers a stream message to a client. The delivered message
has the address of the original sending client, so clients can send
messages back to the original sender's mailbox if they need to.
 *    sender                       string
 *    address                      string
 *    subject                      string
 *    content                      msg
 *  MAILBOX_SEND - Client sends a message to a specified mailbox. Client does not open the
mailbox before sending a message to it. Server replies with OK when it
accepts the message, or ERROR if that failed. If the tracker is not
empty, the sender can expect a CONFIRM at some later stage, for this
message. Confirmations are asynchronous. If the message cannot be
delivered within the specified timeout (zero means infinite), the server
discards it and returns a CONFIRM with a TIMEOUT-EXPIRED status.
 *    address                      string
 *    subject                      string
 *    tracker                      string
 *    timeout                      number 4
 *    content                      msg
 *  MAILBOX_DELIVER - Server delivers a mailbox message to client. Note that client does not
open its own mailbox for reading; this is implied in CONNECTION-OPEN.
If tracker is not empty, client must respond with CONFIRM when it
formally accepts delivery of the message, or if the server delivers
the same message a second time.
 *    sender                       string
 *    address                      string
 *    subject                      string
 *    tracker                      string
 *    content                      msg
 *  SERVICE_SEND - Client sends a service request to a service queue. Server replies with
OK when queued, or ERROR if that failed. If the tracker is not
empty, the client can expect a CONFIRM at some later time.
Confirmations are asynchronous. If the message cannot be delivered
within the specified timeout (zero means infinite), the server
discards it and returns CONFIRM with a TIMEOUT-EXPIRED status.
 *    address                      string
 *    subject                      string
 *    tracker                      string
 *    timeout                      number 4
 *    content                      msg
 *  SERVICE_OFFER - Worker client offers a named service, specifying a pattern to match
message subjects. An empty pattern matches anything. A worker can offer
many different services at once. Server replies with OK or ERROR.
 *    address                      string
 *    pattern                      string
 *  SERVICE_DELIVER - Server delivers a service request to a worker client. If tracker
is not empty, worker must respond with CONFIRM when it accepts delivery
of the message. The worker sends replies to the request to the requesting
client's mailbox.
 *    sender                       string
 *    address                      string
 *    subject                      string
 *    tracker                      string
 *    content                      msg
 *  OK - Server replies with success status. Actual status code provides more
information. An OK always has a 2xx status code.
 *    statusCode                   number 2
 *    statusReason                 string
 *  ERROR - Server replies with failure status. Actual status code provides more
information. An ERROR always has a 3xx, 4xx, or 5xx status code.
 *    statusCode                   number 2
 *    statusReason                 string
 *  CREDIT - Client sends credit to allow delivery of messages. Until the client
sends credit, the server will not deliver messages. The client can send
further credit at any time. Credit is measured in number of messages.
Server does not reply to this message. Note that credit applies to all
stream, mailbox, and service deliveries.
 *    amount                       number 2
 *  CONFIRM - Client confirms reception of a message, or server forwards this
confirmation to original sender. If status code is 300 or higher, this
indicates that the message could not be delivered.
 *    tracker                      string
 *    statusCode                   number 2
 *    statusReason                 string
 *  STREAM_CANCEL - Cancels and removes all subscriptions to a stream.
Server replies with OK or ERROR.
 *    stream                       string
 * </pre>
 *
 * @author sriesenberg
 */
public class MlmProtoCodec {
    //  Protocol constants
    public interface Constants {
        int SUCCESS           = 200;
        int FAILED            = 300;
        int COMMAND_INVALID   = 500;
        int NOT_IMPLEMENTED   = 501;
        int INTERNAL_ERROR    = 502;
    }

    //  Enumeration of message types
    public enum MessageType {
        CONNECTION_OPEN,
        CONNECTION_PING,
        CONNECTION_PONG,
        CONNECTION_CLOSE,
        STREAM_WRITE,
        STREAM_READ,
        STREAM_SEND,
        STREAM_DELIVER,
        MAILBOX_SEND,
        MAILBOX_DELIVER,
        SERVICE_SEND,
        SERVICE_OFFER,
        SERVICE_DELIVER,
        OK,
        ERROR,
        CREDIT,
        CONFIRM,
        STREAM_CANCEL
    }

    protected ConnectionOpenMessage connectionOpen;
    protected ConnectionPingMessage connectionPing;
    protected ConnectionPongMessage connectionPong;
    protected ConnectionCloseMessage connectionClose;
    protected StreamWriteMessage streamWrite;
    protected StreamReadMessage streamRead;
    protected StreamSendMessage streamSend;
    protected StreamDeliverMessage streamDeliver;
    protected MailboxSendMessage mailboxSend;
    protected MailboxDeliverMessage mailboxDeliver;
    protected ServiceSendMessage serviceSend;
    protected ServiceOfferMessage serviceOffer;
    protected ServiceDeliverMessage serviceDeliver;
    protected OkMessage ok;
    protected ErrorMessage error;
    protected CreditMessage credit;
    protected ConfirmMessage confirm;
    protected StreamCancelMessage streamCancel;

    /**
     * Deserialize a message.
     *
     * @return The MessageType of the deserialized message, or null
     */
    public MessageType deserialize(Message frames) {
        MessageType type = null;
        try {
            //  Read and parse command in frame
            Frame needle = frames.popFrame();

            //  Get and check protocol signature
            int signature = (0xffff) & needle.getShort();
            if (signature != (0xaaa0 | 8)) {
                return null;             //  Invalid signature
            }

            //  Get message id, which is first byte in frame
            int id = (0xff) & needle.getByte();
            type = MessageType.values()[id-1];
            switch (type) {
                case CONNECTION_OPEN: {
                    ConnectionOpenMessage message = this.connectionOpen = new ConnectionOpenMessage();
                    message.protocol = needle.getString();
                    if (!message.protocol.equals( "MALAMUTE")) {
                        throw new IllegalArgumentException();
                    }
                    message.version = (0xffff) & needle.getShort();
                    if (message.version != 1) {
                        throw new IllegalArgumentException();
                    }
                    message.address = needle.getString();
                    break;
                }
                case CONNECTION_PING: {
                    ConnectionPingMessage message = this.connectionPing = new ConnectionPingMessage();
                    break;
                }
                case CONNECTION_PONG: {
                    ConnectionPongMessage message = this.connectionPong = new ConnectionPongMessage();
                    break;
                }
                case CONNECTION_CLOSE: {
                    ConnectionCloseMessage message = this.connectionClose = new ConnectionCloseMessage();
                    break;
                }
                case STREAM_WRITE: {
                    StreamWriteMessage message = this.streamWrite = new StreamWriteMessage();
                    message.stream = needle.getString();
                    break;
                }
                case STREAM_READ: {
                    StreamReadMessage message = this.streamRead = new StreamReadMessage();
                    message.stream = needle.getString();
                    message.pattern = needle.getString();
                    break;
                }
                case STREAM_SEND: {
                    StreamSendMessage message = this.streamSend = new StreamSendMessage();
                    message.subject = needle.getString();
                    //  Get remaining frames, leave current untouched
                    if (!frames.isEmpty()) {
                        while (!frames.isEmpty()) {
                            message.content.addFrame(frames.popFrame());
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid message: missing frame: content");
                    }
                    break;
                }
                case STREAM_DELIVER: {
                    StreamDeliverMessage message = this.streamDeliver = new StreamDeliverMessage();
                    message.sender = needle.getString();
                    message.address = needle.getString();
                    message.subject = needle.getString();
                    //  Get remaining frames, leave current untouched
                    if (!frames.isEmpty()) {
                        while (!frames.isEmpty()) {
                            message.content.addFrame(frames.popFrame());
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid message: missing frame: content");
                    }
                    break;
                }
                case MAILBOX_SEND: {
                    MailboxSendMessage message = this.mailboxSend = new MailboxSendMessage();
                    message.address = needle.getString();
                    message.subject = needle.getString();
                    message.tracker = needle.getString();
                    message.timeout = needle.getInt();
                    //  Get remaining frames, leave current untouched
                    if (!frames.isEmpty()) {
                        while (!frames.isEmpty()) {
                            message.content.addFrame(frames.popFrame());
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid message: missing frame: content");
                    }
                    break;
                }
                case MAILBOX_DELIVER: {
                    MailboxDeliverMessage message = this.mailboxDeliver = new MailboxDeliverMessage();
                    message.sender = needle.getString();
                    message.address = needle.getString();
                    message.subject = needle.getString();
                    message.tracker = needle.getString();
                    //  Get remaining frames, leave current untouched
                    if (!frames.isEmpty()) {
                        while (!frames.isEmpty()) {
                            message.content.addFrame(frames.popFrame());
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid message: missing frame: content");
                    }
                    break;
                }
                case SERVICE_SEND: {
                    ServiceSendMessage message = this.serviceSend = new ServiceSendMessage();
                    message.address = needle.getString();
                    message.subject = needle.getString();
                    message.tracker = needle.getString();
                    message.timeout = needle.getInt();
                    //  Get remaining frames, leave current untouched
                    if (!frames.isEmpty()) {
                        while (!frames.isEmpty()) {
                            message.content.addFrame(frames.popFrame());
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid message: missing frame: content");
                    }
                    break;
                }
                case SERVICE_OFFER: {
                    ServiceOfferMessage message = this.serviceOffer = new ServiceOfferMessage();
                    message.address = needle.getString();
                    message.pattern = needle.getString();
                    break;
                }
                case SERVICE_DELIVER: {
                    ServiceDeliverMessage message = this.serviceDeliver = new ServiceDeliverMessage();
                    message.sender = needle.getString();
                    message.address = needle.getString();
                    message.subject = needle.getString();
                    message.tracker = needle.getString();
                    //  Get remaining frames, leave current untouched
                    if (!frames.isEmpty()) {
                        while (!frames.isEmpty()) {
                            message.content.addFrame(frames.popFrame());
                        }
                    } else {
                        throw new IllegalArgumentException("Invalid message: missing frame: content");
                    }
                    break;
                }
                case OK: {
                    OkMessage message = this.ok = new OkMessage();
                    message.statusCode = (0xffff) & needle.getShort();
                    message.statusReason = needle.getString();
                    break;
                }
                case ERROR: {
                    ErrorMessage message = this.error = new ErrorMessage();
                    message.statusCode = (0xffff) & needle.getShort();
                    message.statusReason = needle.getString();
                    break;
                }
                case CREDIT: {
                    CreditMessage message = this.credit = new CreditMessage();
                    message.amount = (0xffff) & needle.getShort();
                    break;
                }
                case CONFIRM: {
                    ConfirmMessage message = this.confirm = new ConfirmMessage();
                    message.tracker = needle.getString();
                    message.statusCode = (0xffff) & needle.getShort();
                    message.statusReason = needle.getString();
                    break;
                }
                case STREAM_CANCEL: {
                    StreamCancelMessage message = this.streamCancel = new StreamCancelMessage();
                    message.stream = needle.getString();
                    break;
                }
                default:
                    throw new IllegalArgumentException("Invalid message: unrecognized type: " + type);
            }

            return type;
        } catch (Exception ex) {
            //  Error returns
            System.err.printf("E: Malformed message: %s\n", type);
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Get a CONNECTION_OPEN message from the socket.
     *
     * @return The ConnectionOpenMessage last received on this socket
     */
    public ConnectionOpenMessage getConnectionOpen() {
        if (connectionOpen != null) {
            connectionOpen = new ConnectionOpenMessage();
        }

        return connectionOpen;
    }

    /**
     * Set a CONNECTION_OPEN message.
     *
     * @param message The ConnectionOpenMessage
     */
    public void setConnectionOpen(ConnectionOpenMessage message) {
        this.connectionOpen = message;
    }

    /**
     * Get a CONNECTION_PING message from the socket.
     *
     * @return The ConnectionPingMessage last received on this socket
     */
    public ConnectionPingMessage getConnectionPing() {
        if (connectionPing != null) {
            connectionPing = new ConnectionPingMessage();
        }

        return connectionPing;
    }

    /**
     * Set a CONNECTION_PING message.
     *
     * @param message The ConnectionPingMessage
     */
    public void setConnectionPing(ConnectionPingMessage message) {
        this.connectionPing = message;
    }

    /**
     * Get a CONNECTION_PONG message from the socket.
     *
     * @return The ConnectionPongMessage last received on this socket
     */
    public ConnectionPongMessage getConnectionPong() {
        if (connectionPong != null) {
            connectionPong = new ConnectionPongMessage();
        }

        return connectionPong;
    }

    /**
     * Set a CONNECTION_PONG message.
     *
     * @param message The ConnectionPongMessage
     */
    public void setConnectionPong(ConnectionPongMessage message) {
        this.connectionPong = message;
    }

    /**
     * Get a CONNECTION_CLOSE message from the socket.
     *
     * @return The ConnectionCloseMessage last received on this socket
     */
    public ConnectionCloseMessage getConnectionClose() {
        if (connectionClose != null) {
            connectionClose = new ConnectionCloseMessage();
        }

        return connectionClose;
    }

    /**
     * Set a CONNECTION_CLOSE message.
     *
     * @param message The ConnectionCloseMessage
     */
    public void setConnectionClose(ConnectionCloseMessage message) {
        this.connectionClose = message;
    }

    /**
     * Get a STREAM_WRITE message from the socket.
     *
     * @return The StreamWriteMessage last received on this socket
     */
    public StreamWriteMessage getStreamWrite() {
        if (streamWrite != null) {
            streamWrite = new StreamWriteMessage();
        }

        return streamWrite;
    }

    /**
     * Set a STREAM_WRITE message.
     *
     * @param message The StreamWriteMessage
     */
    public void setStreamWrite(StreamWriteMessage message) {
        this.streamWrite = message;
    }

    /**
     * Get a STREAM_READ message from the socket.
     *
     * @return The StreamReadMessage last received on this socket
     */
    public StreamReadMessage getStreamRead() {
        if (streamRead != null) {
            streamRead = new StreamReadMessage();
        }

        return streamRead;
    }

    /**
     * Set a STREAM_READ message.
     *
     * @param message The StreamReadMessage
     */
    public void setStreamRead(StreamReadMessage message) {
        this.streamRead = message;
    }

    /**
     * Get a STREAM_SEND message from the socket.
     *
     * @return The StreamSendMessage last received on this socket
     */
    public StreamSendMessage getStreamSend() {
        if (streamSend != null) {
            streamSend = new StreamSendMessage();
        }

        return streamSend;
    }

    /**
     * Set a STREAM_SEND message.
     *
     * @param message The StreamSendMessage
     */
    public void setStreamSend(StreamSendMessage message) {
        this.streamSend = message;
    }

    /**
     * Get a STREAM_DELIVER message from the socket.
     *
     * @return The StreamDeliverMessage last received on this socket
     */
    public StreamDeliverMessage getStreamDeliver() {
        if (streamDeliver != null) {
            streamDeliver = new StreamDeliverMessage();
        }

        return streamDeliver;
    }

    /**
     * Set a STREAM_DELIVER message.
     *
     * @param message The StreamDeliverMessage
     */
    public void setStreamDeliver(StreamDeliverMessage message) {
        this.streamDeliver = message;
    }

    /**
     * Get a MAILBOX_SEND message from the socket.
     *
     * @return The MailboxSendMessage last received on this socket
     */
    public MailboxSendMessage getMailboxSend() {
        if (mailboxSend != null) {
            mailboxSend = new MailboxSendMessage();
        }

        return mailboxSend;
    }

    /**
     * Set a MAILBOX_SEND message.
     *
     * @param message The MailboxSendMessage
     */
    public void setMailboxSend(MailboxSendMessage message) {
        this.mailboxSend = message;
    }

    /**
     * Get a MAILBOX_DELIVER message from the socket.
     *
     * @return The MailboxDeliverMessage last received on this socket
     */
    public MailboxDeliverMessage getMailboxDeliver() {
        if (mailboxDeliver != null) {
            mailboxDeliver = new MailboxDeliverMessage();
        }

        return mailboxDeliver;
    }

    /**
     * Set a MAILBOX_DELIVER message.
     *
     * @param message The MailboxDeliverMessage
     */
    public void setMailboxDeliver(MailboxDeliverMessage message) {
        this.mailboxDeliver = message;
    }

    /**
     * Get a SERVICE_SEND message from the socket.
     *
     * @return The ServiceSendMessage last received on this socket
     */
    public ServiceSendMessage getServiceSend() {
        if (serviceSend != null) {
            serviceSend = new ServiceSendMessage();
        }

        return serviceSend;
    }

    /**
     * Set a SERVICE_SEND message.
     *
     * @param message The ServiceSendMessage
     */
    public void setServiceSend(ServiceSendMessage message) {
        this.serviceSend = message;
    }

    /**
     * Get a SERVICE_OFFER message from the socket.
     *
     * @return The ServiceOfferMessage last received on this socket
     */
    public ServiceOfferMessage getServiceOffer() {
        if (serviceOffer != null) {
            serviceOffer = new ServiceOfferMessage();
        }

        return serviceOffer;
    }

    /**
     * Set a SERVICE_OFFER message.
     *
     * @param message The ServiceOfferMessage
     */
    public void setServiceOffer(ServiceOfferMessage message) {
        this.serviceOffer = message;
    }

    /**
     * Get a SERVICE_DELIVER message from the socket.
     *
     * @return The ServiceDeliverMessage last received on this socket
     */
    public ServiceDeliverMessage getServiceDeliver() {
        if (serviceDeliver != null) {
            serviceDeliver = new ServiceDeliverMessage();
        }

        return serviceDeliver;
    }

    /**
     * Set a SERVICE_DELIVER message.
     *
     * @param message The ServiceDeliverMessage
     */
    public void setServiceDeliver(ServiceDeliverMessage message) {
        this.serviceDeliver = message;
    }

    /**
     * Get a OK message from the socket.
     *
     * @return The OkMessage last received on this socket
     */
    public OkMessage getOk() {
        if (ok != null) {
            ok = new OkMessage();
        }

        return ok;
    }

    /**
     * Set a OK message.
     *
     * @param message The OkMessage
     */
    public void setOk(OkMessage message) {
        this.ok = message;
    }

    /**
     * Get a ERROR message from the socket.
     *
     * @return The ErrorMessage last received on this socket
     */
    public ErrorMessage getError() {
        if (error != null) {
            error = new ErrorMessage();
        }

        return error;
    }

    /**
     * Set a ERROR message.
     *
     * @param message The ErrorMessage
     */
    public void setError(ErrorMessage message) {
        this.error = message;
    }

    /**
     * Get a CREDIT message from the socket.
     *
     * @return The CreditMessage last received on this socket
     */
    public CreditMessage getCredit() {
        if (credit != null) {
            credit = new CreditMessage();
        }

        return credit;
    }

    /**
     * Set a CREDIT message.
     *
     * @param message The CreditMessage
     */
    public void setCredit(CreditMessage message) {
        this.credit = message;
    }

    /**
     * Get a CONFIRM message from the socket.
     *
     * @return The ConfirmMessage last received on this socket
     */
    public ConfirmMessage getConfirm() {
        if (confirm != null) {
            confirm = new ConfirmMessage();
        }

        return confirm;
    }

    /**
     * Set a CONFIRM message.
     *
     * @param message The ConfirmMessage
     */
    public void setConfirm(ConfirmMessage message) {
        this.confirm = message;
    }

    /**
     * Get a STREAM_CANCEL message from the socket.
     *
     * @return The StreamCancelMessage last received on this socket
     */
    public StreamCancelMessage getStreamCancel() {
        if (streamCancel != null) {
            streamCancel = new StreamCancelMessage();
        }

        return streamCancel;
    }

    /**
     * Set a STREAM_CANCEL message.
     *
     * @param message The StreamCancelMessage
     */
    public void setStreamCancel(StreamCancelMessage message) {
        this.streamCancel = message;
    }

    /**
     * Send the CONNECTION_OPEN to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public Message serialize(ConnectionOpenMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xaaa0 | 8));
        builder.putByte((byte) 1);       //  Message ID

        builder.putString("MALAMUTE");
        builder.putShort((short) 1);
        if (message.address != null) {
            builder.putString(message.address);
        } else {
            builder.putString("");       //  Empty string
        }

        //  Create multi-frame message
        Message frames = new Message();

        //  Now add the data frame
        frames.addFrame(builder.build());

        return frames;
    }

    /**
     * Send the CONNECTION_PING to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public Message serialize(ConnectionPingMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xaaa0 | 8));
        builder.putByte((byte) 2);       //  Message ID


        //  Create multi-frame message
        Message frames = new Message();

        //  Now add the data frame
        frames.addFrame(builder.build());

        return frames;
    }

    /**
     * Send the CONNECTION_PONG to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public Message serialize(ConnectionPongMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xaaa0 | 8));
        builder.putByte((byte) 3);       //  Message ID


        //  Create multi-frame message
        Message frames = new Message();

        //  Now add the data frame
        frames.addFrame(builder.build());

        return frames;
    }

    /**
     * Send the CONNECTION_CLOSE to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public Message serialize(ConnectionCloseMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xaaa0 | 8));
        builder.putByte((byte) 4);       //  Message ID


        //  Create multi-frame message
        Message frames = new Message();

        //  Now add the data frame
        frames.addFrame(builder.build());

        return frames;
    }

    /**
     * Send the STREAM_WRITE to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public Message serialize(StreamWriteMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xaaa0 | 8));
        builder.putByte((byte) 5);       //  Message ID

        if (message.stream != null) {
            builder.putString(message.stream);
        } else {
            builder.putString("");       //  Empty string
        }

        //  Create multi-frame message
        Message frames = new Message();

        //  Now add the data frame
        frames.addFrame(builder.build());

        return frames;
    }

    /**
     * Send the STREAM_READ to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public Message serialize(StreamReadMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xaaa0 | 8));
        builder.putByte((byte) 6);       //  Message ID

        if (message.stream != null) {
            builder.putString(message.stream);
        } else {
            builder.putString("");       //  Empty string
        }
        if (message.pattern != null) {
            builder.putString(message.pattern);
        } else {
            builder.putString("");       //  Empty string
        }

        //  Create multi-frame message
        Message frames = new Message();

        //  Now add the data frame
        frames.addFrame(builder.build());

        return frames;
    }

    /**
     * Send the STREAM_SEND to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public Message serialize(StreamSendMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xaaa0 | 8));
        builder.putByte((byte) 7);       //  Message ID

        if (message.subject != null) {
            builder.putString(message.subject);
        } else {
            builder.putString("");       //  Empty string
        }

        //  Create multi-frame message
        Message frames = new Message();

        //  Now add the data frame
        frames.addFrame(builder.build());
        //  Now add message field
        frames.addFrames(message.content.getFrames());

        return frames;
    }

    /**
     * Send the STREAM_DELIVER to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public Message serialize(StreamDeliverMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xaaa0 | 8));
        builder.putByte((byte) 8);       //  Message ID

        if (message.sender != null) {
            builder.putString(message.sender);
        } else {
            builder.putString("");       //  Empty string
        }
        if (message.address != null) {
            builder.putString(message.address);
        } else {
            builder.putString("");       //  Empty string
        }
        if (message.subject != null) {
            builder.putString(message.subject);
        } else {
            builder.putString("");       //  Empty string
        }

        //  Create multi-frame message
        Message frames = new Message();

        //  Now add the data frame
        frames.addFrame(builder.build());
        //  Now add message field
        frames.addFrames(message.content.getFrames());

        return frames;
    }

    /**
     * Send the MAILBOX_SEND to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public Message serialize(MailboxSendMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xaaa0 | 8));
        builder.putByte((byte) 9);       //  Message ID

        if (message.address != null) {
            builder.putString(message.address);
        } else {
            builder.putString("");       //  Empty string
        }
        if (message.subject != null) {
            builder.putString(message.subject);
        } else {
            builder.putString("");       //  Empty string
        }
        if (message.tracker != null) {
            builder.putString(message.tracker);
        } else {
            builder.putString("");       //  Empty string
        }
        builder.putInt(message.timeout);

        //  Create multi-frame message
        Message frames = new Message();

        //  Now add the data frame
        frames.addFrame(builder.build());
        //  Now add message field
        frames.addFrames(message.content.getFrames());

        return frames;
    }

    /**
     * Send the MAILBOX_DELIVER to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public Message serialize(MailboxDeliverMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xaaa0 | 8));
        builder.putByte((byte) 10);      //  Message ID

        if (message.sender != null) {
            builder.putString(message.sender);
        } else {
            builder.putString("");       //  Empty string
        }
        if (message.address != null) {
            builder.putString(message.address);
        } else {
            builder.putString("");       //  Empty string
        }
        if (message.subject != null) {
            builder.putString(message.subject);
        } else {
            builder.putString("");       //  Empty string
        }
        if (message.tracker != null) {
            builder.putString(message.tracker);
        } else {
            builder.putString("");       //  Empty string
        }

        //  Create multi-frame message
        Message frames = new Message();

        //  Now add the data frame
        frames.addFrame(builder.build());
        //  Now add message field
        frames.addFrames(message.content.getFrames());

        return frames;
    }

    /**
     * Send the SERVICE_SEND to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public Message serialize(ServiceSendMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xaaa0 | 8));
        builder.putByte((byte) 11);      //  Message ID

        if (message.address != null) {
            builder.putString(message.address);
        } else {
            builder.putString("");       //  Empty string
        }
        if (message.subject != null) {
            builder.putString(message.subject);
        } else {
            builder.putString("");       //  Empty string
        }
        if (message.tracker != null) {
            builder.putString(message.tracker);
        } else {
            builder.putString("");       //  Empty string
        }
        builder.putInt(message.timeout);

        //  Create multi-frame message
        Message frames = new Message();

        //  Now add the data frame
        frames.addFrame(builder.build());
        //  Now add message field
        frames.addFrames(message.content.getFrames());

        return frames;
    }

    /**
     * Send the SERVICE_OFFER to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public Message serialize(ServiceOfferMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xaaa0 | 8));
        builder.putByte((byte) 12);      //  Message ID

        if (message.address != null) {
            builder.putString(message.address);
        } else {
            builder.putString("");       //  Empty string
        }
        if (message.pattern != null) {
            builder.putString(message.pattern);
        } else {
            builder.putString("");       //  Empty string
        }

        //  Create multi-frame message
        Message frames = new Message();

        //  Now add the data frame
        frames.addFrame(builder.build());

        return frames;
    }

    /**
     * Send the SERVICE_DELIVER to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public Message serialize(ServiceDeliverMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xaaa0 | 8));
        builder.putByte((byte) 13);      //  Message ID

        if (message.sender != null) {
            builder.putString(message.sender);
        } else {
            builder.putString("");       //  Empty string
        }
        if (message.address != null) {
            builder.putString(message.address);
        } else {
            builder.putString("");       //  Empty string
        }
        if (message.subject != null) {
            builder.putString(message.subject);
        } else {
            builder.putString("");       //  Empty string
        }
        if (message.tracker != null) {
            builder.putString(message.tracker);
        } else {
            builder.putString("");       //  Empty string
        }

        //  Create multi-frame message
        Message frames = new Message();

        //  Now add the data frame
        frames.addFrame(builder.build());
        //  Now add message field
        frames.addFrames(message.content.getFrames());

        return frames;
    }

    /**
     * Send the OK to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public Message serialize(OkMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xaaa0 | 8));
        builder.putByte((byte) 14);      //  Message ID

        builder.putShort((short) (int) message.statusCode);
        if (message.statusReason != null) {
            builder.putString(message.statusReason);
        } else {
            builder.putString("");       //  Empty string
        }

        //  Create multi-frame message
        Message frames = new Message();

        //  Now add the data frame
        frames.addFrame(builder.build());

        return frames;
    }

    /**
     * Send the ERROR to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public Message serialize(ErrorMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xaaa0 | 8));
        builder.putByte((byte) 15);      //  Message ID

        builder.putShort((short) (int) message.statusCode);
        if (message.statusReason != null) {
            builder.putString(message.statusReason);
        } else {
            builder.putString("");       //  Empty string
        }

        //  Create multi-frame message
        Message frames = new Message();

        //  Now add the data frame
        frames.addFrame(builder.build());

        return frames;
    }

    /**
     * Send the CREDIT to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public Message serialize(CreditMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xaaa0 | 8));
        builder.putByte((byte) 16);      //  Message ID

        builder.putShort((short) (int) message.amount);

        //  Create multi-frame message
        Message frames = new Message();

        //  Now add the data frame
        frames.addFrame(builder.build());

        return frames;
    }

    /**
     * Send the CONFIRM to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public Message serialize(ConfirmMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xaaa0 | 8));
        builder.putByte((byte) 17);      //  Message ID

        if (message.tracker != null) {
            builder.putString(message.tracker);
        } else {
            builder.putString("");       //  Empty string
        }
        builder.putShort((short) (int) message.statusCode);
        if (message.statusReason != null) {
            builder.putString(message.statusReason);
        } else {
            builder.putString("");       //  Empty string
        }

        //  Create multi-frame message
        Message frames = new Message();

        //  Now add the data frame
        frames.addFrame(builder.build());

        return frames;
    }

    /**
     * Send the STREAM_CANCEL to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public Message serialize(StreamCancelMessage message) {
        //  Now serialize message into the frame
        FrameBuilder builder = new FrameBuilder();
        builder.putShort((short) (0xaaa0 | 8));
        builder.putByte((byte) 18);      //  Message ID

        if (message.stream != null) {
            builder.putString(message.stream);
        } else {
            builder.putString("");       //  Empty string
        }

        //  Create multi-frame message
        Message frames = new Message();

        //  Now add the data frame
        frames.addFrame(builder.build());

        return frames;
    }
}

