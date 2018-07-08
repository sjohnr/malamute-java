/* ============================================================================
 * ConfirmMessage.java
 * 
 * Generated codec class for ConfirmMessage
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
 * ConfirmMessage class.
 */
public class ConfirmMessage {
    public static final MlmProtoCodec.MessageType MESSAGE_TYPE = MlmProtoCodec.MessageType.CONFIRM;

    protected String tracker;
    protected Integer statusCode;
    protected String statusReason;

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
     * @return The ConfirmMessage, for method chaining
     */
    public ConfirmMessage withTracker(String tracker) {
        this.tracker = tracker;
        return this;
    }

    /**
     * Get the statusCode field.
     * 
     * @return The statusCode field
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    /**
     * Set the statusCode field.
     * 
     * @param statusCode The statusCode field
     */
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Set the statusCode field.
     *
     * @param statusCode The statusCode field
     * @return The ConfirmMessage, for method chaining
     */
    public ConfirmMessage withStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    /**
     * Get the statusReason field.
     * 
     * @return The statusReason field
     */
    public String getStatusReason() {
        return statusReason;
    }

    /**
     * Set the statusReason field.
     * 
     * @param statusReason The statusReason field
     */
    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    /**
     * Set the statusReason field.
     *
     * @param statusReason The statusReason field
     * @return The ConfirmMessage, for method chaining
     */
    public ConfirmMessage withStatusReason(String statusReason) {
        this.statusReason = statusReason;
        return this;
    }
}
