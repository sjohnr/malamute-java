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
     * Handle a custom command from the application.
     *
     * @param command The command to execute
     * @param message The message
     * @return A reply to send
     */
    Message handleCommand(String command, Message message);

    /**
     * Handle "register new client" action.
     *
     * @param client Handle to the current client
     */
    void registerNewClient(MlmServerAgent.Client client);

    /**
     * Handle "check for mailbox messages" action.
     *
     * @param client Handle to the current client
     */
    void checkForMailboxMessages(MlmServerAgent.Client client);

    /**
     * Handle "signal command invalid" action.
     *
     * @param client Handle to the current client
     */
    void signalCommandInvalid(MlmServerAgent.Client client);

    /**
     * Handle "store stream writer" action.
     *
     * @param client Handle to the current client
     */
    void storeStreamWriter(MlmServerAgent.Client client);

    /**
     * Handle "store stream reader" action.
     *
     * @param client Handle to the current client
     */
    void storeStreamReader(MlmServerAgent.Client client);

    /**
     * Handle "cancel stream reader" action.
     *
     * @param client Handle to the current client
     */
    void cancelStreamReader(MlmServerAgent.Client client);

    /**
     * Handle "write message to stream" action.
     *
     * @param client Handle to the current client
     */
    void writeMessageToStream(MlmServerAgent.Client client);

    /**
     * Handle "write message to mailbox" action.
     *
     * @param client Handle to the current client
     */
    void writeMessageToMailbox(MlmServerAgent.Client client);

    /**
     * Handle "write message to service" action.
     *
     * @param client Handle to the current client
     */
    void writeMessageToService(MlmServerAgent.Client client);

    /**
     * Handle "store service offer" action.
     *
     * @param client Handle to the current client
     */
    void storeServiceOffer(MlmServerAgent.Client client);

    /**
     * Handle "dispatch service" action.
     *
     * @param client Handle to the current client
     */
    void dispatchService(MlmServerAgent.Client client);

    /**
     * Handle "on message confirmation" action.
     *
     * @param client Handle to the current client
     */
    void onMessageConfirmation(MlmServerAgent.Client client);

    /**
     * Handle "on credit client request" action.
     *
     * @param client Handle to the current client
     */
    void onCreditClientRequest(MlmServerAgent.Client client);

    /**
     * Handle "on client closed connection" action.
     *
     * @param client Handle to the current client
     */
    void onClientClosedConnection(MlmServerAgent.Client client);

    /**
     * Handle "deregister client" action.
     *
     * @param client Handle to the current client
     */
    void deregisterClient(MlmServerAgent.Client client);

    /**
     * Handle "terminate" action.
     *
     * @param client Handle to the current client
     */
    void terminate(MlmServerAgent.Client client);

    /**
     * Handle "get message to deliver" action.
     *
     * @param client Handle to the current client
     */
    void getMessageToDeliver(MlmServerAgent.Client client);

    /**
     * Handle "on client expired" action.
     *
     * @param client Handle to the current client
     */
    void onClientExpired(MlmServerAgent.Client client);

    /**
     * Handle "signal operation failed" action.
     *
     * @param client Handle to the current client
     */
    void signalOperationFailed(MlmServerAgent.Client client);

    /**
     * Handle "on client exception" action.
     *
     * @param client Handle to the current client
     */
    void onClientException(MlmServerAgent.Client client);
}
