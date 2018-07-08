/* ============================================================================
 * ServiceOfferMessage.java
 * 
 * Generated codec class for ServiceOfferMessage
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
 * ServiceOfferMessage class.
 */
public class ServiceOfferMessage {
    public static final MlmProtoCodec.MessageType MESSAGE_TYPE = MlmProtoCodec.MessageType.SERVICE_OFFER;

    protected String address;
    protected String pattern;

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
     * @return The ServiceOfferMessage, for method chaining
     */
    public ServiceOfferMessage withAddress(String address) {
        this.address = address;
        return this;
    }

    /**
     * Get the pattern field.
     * 
     * @return The pattern field
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Set the pattern field.
     * 
     * @param pattern The pattern field
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * Set the pattern field.
     *
     * @param pattern The pattern field
     * @return The ServiceOfferMessage, for method chaining
     */
    public ServiceOfferMessage withPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }
}
