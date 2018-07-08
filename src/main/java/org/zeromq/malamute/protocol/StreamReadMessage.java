/* ============================================================================
 * StreamReadMessage.java
 * 
 * Generated codec class for StreamReadMessage
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
 * StreamReadMessage class.
 */
public class StreamReadMessage {
    public static final MlmProtoCodec.MessageType MESSAGE_TYPE = MlmProtoCodec.MessageType.STREAM_READ;

    protected String stream;
    protected String pattern;

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
     * @return The StreamReadMessage, for method chaining
     */
    public StreamReadMessage withStream(String stream) {
        this.stream = stream;
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
     * @return The StreamReadMessage, for method chaining
     */
    public StreamReadMessage withPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }
}
