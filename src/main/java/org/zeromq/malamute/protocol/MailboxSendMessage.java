/* ============================================================================
 * MailboxSendMessage.java
 * 
 * Generated codec class for MailboxSendMessage
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
 * MailboxSendMessage class.
 */
public class MailboxSendMessage {
    public static final MlmProtoCodec.MessageType MESSAGE_TYPE = MlmProtoCodec.MessageType.MAILBOX_SEND;

    protected String address;
    protected String subject;
    protected String tracker;
    protected Integer timeout;
    protected org.zeromq.api.Message content = new org.zeromq.api.Message();

    /**
     * Get the address field.
     * 
     * @return The address field
     */
    public String getAddress() {
        return address;
    }

    /**
     * Set the address field.
     * 
     * @param address The address field
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Set the address field.
     *
     * @param address The address field
     * @return The MailboxSendMessage, for method chaining
     */
    public MailboxSendMessage withAddress(String address) {
        this.address = address;
        return this;
    }

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
     * @return The MailboxSendMessage, for method chaining
     */
    public MailboxSendMessage withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Get the tracker field.
     * 
     * @return The tracker field
     */
    public String getTracker() {
        return tracker;
    }

    /**
     * Set the tracker field.
     * 
     * @param tracker The tracker field
     */
    public void setTracker(String tracker) {
        this.tracker = tracker;
    }

    /**
     * Set the tracker field.
     *
     * @param tracker The tracker field
     * @return The MailboxSendMessage, for method chaining
     */
    public MailboxSendMessage withTracker(String tracker) {
        this.tracker = tracker;
        return this;
    }

    /**
     * Get the timeout field.
     * 
     * @return The timeout field
     */
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * Set the timeout field.
     * 
     * @param timeout The timeout field
     */
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * Set the timeout field.
     *
     * @param timeout The timeout field
     * @return The MailboxSendMessage, for method chaining
     */
    public MailboxSendMessage withTimeout(Integer timeout) {
        this.timeout = timeout;
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
     * @return The MailboxSendMessage, for method chaining
     */
    public MailboxSendMessage withContent(org.zeromq.api.Message message) {
        this.content = message;
        return this;
    }
}
