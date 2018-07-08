/* ============================================================================
 * StreamSendMessage.java
 * 
 * Generated codec class for StreamSendMessage
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

/**
 * StreamSendMessage class.
 */
public class StreamSendMessage {
    public static final MlmProtoCodec.MessageType MESSAGE_TYPE = MlmProtoCodec.MessageType.STREAM_SEND;

    protected String subject;
    protected org.zeromq.api.Message content = new org.zeromq.api.Message();

    /**
     * Get the subject field.
     * 
     * @return The subject field
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Set the subject field.
     * 
     * @param subject The subject field
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Set the subject field.
     *
     * @param subject The subject field
     * @return The StreamSendMessage, for method chaining
     */
    public StreamSendMessage withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Get the content field.
     *
     * @return The content field
     */
    public org.zeromq.api.Message getContent() {
        return content;
    }

    /**
     * Set the content field, and take ownership of supplied message.
     *
     * @param message The new content message
     */
    public void setContent(org.zeromq.api.Message message) {
        this.content = message;
    }

    /**
     * Set the content field, and take ownership of supplied message.
     *
     * @param message The new content message
     * @return The StreamSendMessage, for method chaining
     */
    public StreamSendMessage withContent(org.zeromq.api.Message message) {
        this.content = message;
        return this;
    }
}
