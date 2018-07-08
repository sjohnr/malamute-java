/* ============================================================================
 * ErrorMessage.java
 * 
 * Generated codec class for ErrorMessage
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
 * ErrorMessage class.
 */
public class ErrorMessage {
    public static final MlmProtoCodec.MessageType MESSAGE_TYPE = MlmProtoCodec.MessageType.ERROR;

    protected Integer statusCode;
    protected String statusReason;

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
     * @return The ErrorMessage, for method chaining
     */
    public ErrorMessage withStatusCode(Integer statusCode) {
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
     * @return The ErrorMessage, for method chaining
     */
    public ErrorMessage withStatusReason(String statusReason) {
        this.statusReason = statusReason;
        return this;
    }
}
