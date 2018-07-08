/* ============================================================================
 * ConnectionOpenMessage.java
 * 
 * Generated codec class for ConnectionOpenMessage
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
 * ConnectionOpenMessage class.
 */
public class ConnectionOpenMessage {
    public static final MlmProtoCodec.MessageType MESSAGE_TYPE = MlmProtoCodec.MessageType.CONNECTION_OPEN;

    protected String protocol;
    protected Integer version;
    protected String address;

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
     * @return The ConnectionOpenMessage, for method chaining
     */
    public ConnectionOpenMessage withAddress(String address) {
        this.address = address;
        return this;
    }
}
