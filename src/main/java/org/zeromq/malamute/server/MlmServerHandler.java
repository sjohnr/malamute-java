/* =============================================================================
 * MlmServer.java
 *
 * Generated class for MlmServer.
 * -----------------------------------------------------------------------------
 * Copyright (c) the Contributors as noted in the AUTHORS file.
 * This file is part of the Malamute Project.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * =============================================================================
 */
package org.zeromq.malamute.server;

import org.zeromq.api.Message;

/**
 * MlmServerHandler interface.
 * <p>
 * The application callback handler interface which performs actions on behalf
 * of background agent.
 *
 * @author sriesenberg
 */
public interface MlmServerHandler {
    /**
     * Register new client.
     *
     * @param client Handle to the current client
     */
    void registerNewClient(MlmServerAgent.Client client);

    /**
     * Send.
     *
     * @param client Handle to the current client
     */
    void send(MlmServerAgent.Client client);

    /**
     * Check for mailbox messages.
     *
     * @param client Handle to the current client
     */
    void checkForMailboxMessages(MlmServerAgent.Client client);

    /**
     * Signal command invalid.
     *
     * @param client Handle to the current client
     */
    void signalCommandInvalid(MlmServerAgent.Client client);

    /**
     * Store stream writer.
     *
     * @param client Handle to the current client
     */
    void storeStreamWriter(MlmServerAgent.Client client);

    /**
     * Store stream reader.
     *
     * @param client Handle to the current client
     */
    void storeStreamReader(MlmServerAgent.Client client);

    /**
     * Cancel stream reader.
     *
     * @param client Handle to the current client
     */
    void cancelStreamReader(MlmServerAgent.Client client);

    /**
     * Write message to stream.
     *
     * @param client Handle to the current client
     */
    void writeMessageToStream(MlmServerAgent.Client client);

    /**
     * Write message to mailbox.
     *
     * @param client Handle to the current client
     */
    void writeMessageToMailbox(MlmServerAgent.Client client);

    /**
     * Write message to service.
     *
     * @param client Handle to the current client
     */
    void writeMessageToService(MlmServerAgent.Client client);

    /**
     * Store service offer.
     *
     * @param client Handle to the current client
     */
    void storeServiceOffer(MlmServerAgent.Client client);

    /**
     * Dispatch the service.
     *
     * @param client Handle to the current client
     */
    void dispatchTheService(MlmServerAgent.Client client);

    /**
     * Have message confirmation.
     *
     * @param client Handle to the current client
     */
    void haveMessageConfirmation(MlmServerAgent.Client client);

    /**
     * Credit the client.
     *
     * @param client Handle to the current client
     */
    void creditTheClient(MlmServerAgent.Client client);

    /**
     * Client closed connection.
     *
     * @param client Handle to the current client
     */
    void clientClosedConnection(MlmServerAgent.Client client);

    /**
     * Deregister the client.
     *
     * @param client Handle to the current client
     */
    void deregisterTheClient(MlmServerAgent.Client client);

    /**
     * Terminate.
     *
     * @param client Handle to the current client
     */
    void terminate(MlmServerAgent.Client client);

    /**
     * Get message to deliver.
     *
     * @param client Handle to the current client
     */
    void getMessageToDeliver(MlmServerAgent.Client client);

    /**
     * Client expired.
     *
     * @param client Handle to the current client
     */
    void clientExpired(MlmServerAgent.Client client);

    /**
     * Signal operation failed.
     *
     * @param client Handle to the current client
     */
    void signalOperationFailed(MlmServerAgent.Client client);

    /**
     * Client had exception.
     *
     * @param client Handle to the current client
     */
    void clientHadException(MlmServerAgent.Client client);

    /**
     * Handle a custom command from the application.
     *
     * @param command The command to execute
     * @param message The message
     * @return A reply to send
     */
    Message handleCommand(String command, Message message);
}
