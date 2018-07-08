/* ============================================================================
 * StreamCancelMessage.java
 * 
 * Generated codec class for StreamCancelMessage
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
 * StreamCancelMessage class.
 */
public class StreamCancelMessage {
    public static final MlmProtoCodec.MessageType MESSAGE_TYPE = MlmProtoCodec.MessageType.STREAM_CANCEL;

    protected String stream;

    /**
     * Get the stream field.
     * 
     * @return The stream field
     */
    public String getStream() {
        return stream;
    }

    /**
     * Set the stream field.
     * 
     * @param stream The stream field
     */
    public void setStream(String stream) {
        this.stream = stream;
    }

    /**
     * Set the stream field.
     *
     * @param stream The stream field
     * @return The StreamCancelMessage, for method chaining
     */
    public StreamCancelMessage withStream(String stream) {
        this.stream = stream;
        return this;
    }
}

