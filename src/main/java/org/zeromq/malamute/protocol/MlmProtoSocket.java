/* ============================================================================
 * MlmProtoSocket.java
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

import org.zeromq.ZMQ;
import org.zeromq.api.Message;
import org.zeromq.api.Message.Frame;
import org.zeromq.api.MessageFlag;
import org.zeromq.api.Socket;

/**
 * MlmProtoSocket class.
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
public class MlmProtoSocket implements MlmProtoCodec.Constants, java.io.Closeable {
    //  Structure of our class
    private Socket socket;               //  Internal socket handle
    private MlmProtoCodec codec;         //  Serialization codec
    private Frame address;               //  Address of peer if any

    /**
     * Create a new MlmProtoSocket.
     * 
     * @param socket The internal socket
     */
    public MlmProtoSocket(Socket socket) {
        assert socket != null;
        this.socket = socket;
        this.codec = new MlmProtoCodec();
    }

    /**
     * Get the message address.
     * 
     * @return The message address frame
     */
    public Frame getAddress() {
        return address;
    }

    /**
     * Set the message address.
     * 
     * @param address The new message address
     */
    public void setAddress(Frame address) {
        this.address = address;
    }

    /**
     * Get the internal socket.
     *
     * @return The internal socket
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * Get the internal socket.
     *
     * @return The internal socket
     */
    public MlmProtoCodec getCodec() {
        return codec;
    }

    /**
     * Destroy the MlmProtoSocket.
     */
    @Override
    public void close() {
        socket.close();
    }

    /**
     * Receive a message on the socket.
     *
     * @return The MessageType of the received message
     */
    public MlmProtoCodec.MessageType receive() {
        return receive(MessageFlag.NONE);
    }

    /**
     * Receive a message on the socket.
     *
     * @param flag Flag controlling behavior of the receive operation
     * @return The MessageType of the received message, or null if no message received
     */
    public MlmProtoCodec.MessageType receive(MessageFlag flag) {
        //  Read valid message frame from socket; we loop over any
        //  garbage data we might receive from badly-connected peers
        MlmProtoCodec.MessageType type;
        Message frames;
        do {
            frames = socket.receiveMessage(flag);

            //  If we're reading from a ROUTER socket, get address
            if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
                this.address = frames.popFrame();
            }

            //  Get and check protocol signature
            type = codec.deserialize(frames);
        } while (type == null && flag == MessageFlag.NONE);          //  Protocol assertion, drop message if malformed or invalid

        return type;
    }

    /**
     * Get a CONNECTION_OPEN message from the socket.
     *
     * @return The ConnectionOpenMessage last received on this socket
     */
    public ConnectionOpenMessage getConnectionOpen() {
        return codec.connectionOpen;
    }

    /**
     * Get a CONNECTION_PING message from the socket.
     *
     * @return The ConnectionPingMessage last received on this socket
     */
    public ConnectionPingMessage getConnectionPing() {
        return codec.connectionPing;
    }

    /**
     * Get a CONNECTION_PONG message from the socket.
     *
     * @return The ConnectionPongMessage last received on this socket
     */
    public ConnectionPongMessage getConnectionPong() {
        return codec.connectionPong;
    }

    /**
     * Get a CONNECTION_CLOSE message from the socket.
     *
     * @return The ConnectionCloseMessage last received on this socket
     */
    public ConnectionCloseMessage getConnectionClose() {
        return codec.connectionClose;
    }

    /**
     * Get a STREAM_WRITE message from the socket.
     *
     * @return The StreamWriteMessage last received on this socket
     */
    public StreamWriteMessage getStreamWrite() {
        return codec.streamWrite;
    }

    /**
     * Get a STREAM_READ message from the socket.
     *
     * @return The StreamReadMessage last received on this socket
     */
    public StreamReadMessage getStreamRead() {
        return codec.streamRead;
    }

    /**
     * Get a STREAM_SEND message from the socket.
     *
     * @return The StreamSendMessage last received on this socket
     */
    public StreamSendMessage getStreamSend() {
        return codec.streamSend;
    }

    /**
     * Get a STREAM_DELIVER message from the socket.
     *
     * @return The StreamDeliverMessage last received on this socket
     */
    public StreamDeliverMessage getStreamDeliver() {
        return codec.streamDeliver;
    }

    /**
     * Get a MAILBOX_SEND message from the socket.
     *
     * @return The MailboxSendMessage last received on this socket
     */
    public MailboxSendMessage getMailboxSend() {
        return codec.mailboxSend;
    }

    /**
     * Get a MAILBOX_DELIVER message from the socket.
     *
     * @return The MailboxDeliverMessage last received on this socket
     */
    public MailboxDeliverMessage getMailboxDeliver() {
        return codec.mailboxDeliver;
    }

    /**
     * Get a SERVICE_SEND message from the socket.
     *
     * @return The ServiceSendMessage last received on this socket
     */
    public ServiceSendMessage getServiceSend() {
        return codec.serviceSend;
    }

    /**
     * Get a SERVICE_OFFER message from the socket.
     *
     * @return The ServiceOfferMessage last received on this socket
     */
    public ServiceOfferMessage getServiceOffer() {
        return codec.serviceOffer;
    }

    /**
     * Get a SERVICE_DELIVER message from the socket.
     *
     * @return The ServiceDeliverMessage last received on this socket
     */
    public ServiceDeliverMessage getServiceDeliver() {
        return codec.serviceDeliver;
    }

    /**
     * Get a OK message from the socket.
     *
     * @return The OkMessage last received on this socket
     */
    public OkMessage getOk() {
        return codec.ok;
    }

    /**
     * Get a ERROR message from the socket.
     *
     * @return The ErrorMessage last received on this socket
     */
    public ErrorMessage getError() {
        return codec.error;
    }

    /**
     * Get a CREDIT message from the socket.
     *
     * @return The CreditMessage last received on this socket
     */
    public CreditMessage getCredit() {
        return codec.credit;
    }

    /**
     * Get a CONFIRM message from the socket.
     *
     * @return The ConfirmMessage last received on this socket
     */
    public ConfirmMessage getConfirm() {
        return codec.confirm;
    }

    /**
     * Get a STREAM_CANCEL message from the socket.
     *
     * @return The StreamCancelMessage last received on this socket
     */
    public StreamCancelMessage getStreamCancel() {
        return codec.streamCancel;
    }

    /**
     * Send the CONNECTION_OPEN to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public boolean send(ConnectionOpenMessage message) {
        Message frames = codec.serialize(message);

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert address != null;
            frames.pushFrame(address);
        }

        return socket.send(frames);
    }

    /**
     * Send the CONNECTION_PING to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public boolean send(ConnectionPingMessage message) {
        Message frames = codec.serialize(message);

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert address != null;
            frames.pushFrame(address);
        }

        return socket.send(frames);
    }

    /**
     * Send the CONNECTION_PONG to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public boolean send(ConnectionPongMessage message) {
        Message frames = codec.serialize(message);

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert address != null;
            frames.pushFrame(address);
        }

        return socket.send(frames);
    }

    /**
     * Send the CONNECTION_CLOSE to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public boolean send(ConnectionCloseMessage message) {
        Message frames = codec.serialize(message);

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert address != null;
            frames.pushFrame(address);
        }

        return socket.send(frames);
    }

    /**
     * Send the STREAM_WRITE to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public boolean send(StreamWriteMessage message) {
        Message frames = codec.serialize(message);

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert address != null;
            frames.pushFrame(address);
        }

        return socket.send(frames);
    }

    /**
     * Send the STREAM_READ to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public boolean send(StreamReadMessage message) {
        Message frames = codec.serialize(message);

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert address != null;
            frames.pushFrame(address);
        }

        return socket.send(frames);
    }

    /**
     * Send the STREAM_SEND to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public boolean send(StreamSendMessage message) {
        Message frames = codec.serialize(message);

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert address != null;
            frames.pushFrame(address);
        }

        return socket.send(frames);
    }

    /**
     * Send the STREAM_DELIVER to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public boolean send(StreamDeliverMessage message) {
        Message frames = codec.serialize(message);

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert address != null;
            frames.pushFrame(address);
        }

        return socket.send(frames);
    }

    /**
     * Send the MAILBOX_SEND to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public boolean send(MailboxSendMessage message) {
        Message frames = codec.serialize(message);

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert address != null;
            frames.pushFrame(address);
        }

        return socket.send(frames);
    }

    /**
     * Send the MAILBOX_DELIVER to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public boolean send(MailboxDeliverMessage message) {
        Message frames = codec.serialize(message);

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert address != null;
            frames.pushFrame(address);
        }

        return socket.send(frames);
    }

    /**
     * Send the SERVICE_SEND to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public boolean send(ServiceSendMessage message) {
        Message frames = codec.serialize(message);

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert address != null;
            frames.pushFrame(address);
        }

        return socket.send(frames);
    }

    /**
     * Send the SERVICE_OFFER to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public boolean send(ServiceOfferMessage message) {
        Message frames = codec.serialize(message);

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert address != null;
            frames.pushFrame(address);
        }

        return socket.send(frames);
    }

    /**
     * Send the SERVICE_DELIVER to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public boolean send(ServiceDeliverMessage message) {
        Message frames = codec.serialize(message);

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert address != null;
            frames.pushFrame(address);
        }

        return socket.send(frames);
    }

    /**
     * Send the OK to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public boolean send(OkMessage message) {
        Message frames = codec.serialize(message);

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert address != null;
            frames.pushFrame(address);
        }

        return socket.send(frames);
    }

    /**
     * Send the ERROR to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public boolean send(ErrorMessage message) {
        Message frames = codec.serialize(message);

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert address != null;
            frames.pushFrame(address);
        }

        return socket.send(frames);
    }

    /**
     * Send the CREDIT to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public boolean send(CreditMessage message) {
        Message frames = codec.serialize(message);

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert address != null;
            frames.pushFrame(address);
        }

        return socket.send(frames);
    }

    /**
     * Send the CONFIRM to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public boolean send(ConfirmMessage message) {
        Message frames = codec.serialize(message);

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert address != null;
            frames.pushFrame(address);
        }

        return socket.send(frames);
    }

    /**
     * Send the STREAM_CANCEL to the socket in one step.
     *
     * @param message The message to send
     * @return true if the message was sent, false otherwise
     */
    public boolean send(StreamCancelMessage message) {
        Message frames = codec.serialize(message);

        //  If we're sending to a ROUTER, we add the address first
        if (socket.getZMQSocket().getType() == ZMQ.ROUTER) {
            assert address != null;
            frames.pushFrame(address);
        }

        return socket.send(frames);
    }
}

