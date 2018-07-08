/* ============================================================================
 * CreditMessage.java
 * 
 * Generated codec class for CreditMessage
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
 * CreditMessage class.
 */
public class CreditMessage {
    public static final MlmProtoCodec.MessageType MESSAGE_TYPE = MlmProtoCodec.MessageType.CREDIT;

    protected Integer amount;

    /**
     * Get the amount field.
     * 
     * @return The amount field
     */
    public Integer getAmount() {
        return amount;
    }

    /**
     * Set the amount field.
     * 
     * @param amount The amount field
     */
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    /**
     * Set the amount field.
     *
     * @param amount The amount field
     * @return The CreditMessage, for method chaining
     */
    public CreditMessage withAmount(Integer amount) {
        this.amount = amount;
        return this;
    }
}
