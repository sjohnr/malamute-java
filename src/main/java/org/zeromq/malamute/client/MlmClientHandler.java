/* =============================================================================
 * MlmClient.java
 *
 * Generated class for MlmClient.
 * -----------------------------------------------------------------------------
 * Copyright (c) the Contributors as noted in the AUTHORS file.
 * This file is part of the Malamute Project.
 * 
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * =============================================================================
 */
package org.zeromq.malamute.client;

/**
 * MlmClientHandler interface.
 * <p>
 * The application callback handler interface which performs actions on behalf
 * of background agent.
 *
 * @author sriesenberg
 */
public interface MlmClientHandler {
    /**
     * Use plain security mechanism.
     *
     * @param agent Handle to the background agent
     */
    void usePlainSecurityMechanism(MlmClientAgent agent);

    /**
     * Signal success.
     *
     * @param agent Handle to the background agent
     */
    void signalSuccess(MlmClientAgent agent);

    /**
     * Remember client address.
     *
     * @param agent Handle to the background agent
     */
    void rememberClientAddress(MlmClientAgent agent);

    /**
     * Connect to server endpoint.
     *
     * @param agent Handle to the background agent
     */
    void connectToServerEndpoint(MlmClientAgent agent);

    /**
     * Set client address.
     *
     * @param agent Handle to the background agent
     */
    void setClientAddress(MlmClientAgent agent);

    /**
     * Use connect timeout.
     *
     * @param agent Handle to the background agent
     */
    void useConnectTimeout(MlmClientAgent agent);

    /**
     * Send.
     *
     * @param agent Handle to the background agent
     */
    void send(MlmClientAgent agent);

    /**
     * Signal bad endpoint.
     *
     * @param agent Handle to the background agent
     */
    void signalBadEndpoint(MlmClientAgent agent);

    /**
     * Terminate.
     *
     * @param agent Handle to the background agent
     */
    void terminate(MlmClientAgent agent);

    /**
     * Signal failure.
     *
     * @param agent Handle to the background agent
     */
    void signalFailure(MlmClientAgent agent);

    /**
     * Client is connected.
     *
     * @param agent Handle to the background agent
     */
    void clientIsConnected(MlmClientAgent agent);

    /**
     * Signal server not present.
     *
     * @param agent Handle to the background agent
     */
    void signalServerNotPresent(MlmClientAgent agent);

    /**
     * Prepare stream write command.
     *
     * @param agent Handle to the background agent
     */
    void prepareStreamWriteCommand(MlmClientAgent agent);

    /**
     * Prepare stream read command.
     *
     * @param agent Handle to the background agent
     */
    void prepareStreamReadCommand(MlmClientAgent agent);

    /**
     * Prepare stream cancel command.
     *
     * @param agent Handle to the background agent
     */
    void prepareStreamCancelCommand(MlmClientAgent agent);

    /**
     * Prepare service offer command.
     *
     * @param agent Handle to the background agent
     */
    void prepareServiceOfferCommand(MlmClientAgent agent);

    /**
     * Signal bad pattern.
     *
     * @param agent Handle to the background agent
     */
    void signalBadPattern(MlmClientAgent agent);

    /**
     * Recv.
     *
     * @param agent Handle to the background agent
     */
    void recv(MlmClientAgent agent);

    /**
     * Server has gone offline.
     *
     * @param agent Handle to the background agent
     */
    void serverHasGoneOffline(MlmClientAgent agent);

    /**
     * Check status code.
     *
     * @param agent Handle to the background agent
     */
    void checkStatusCode(MlmClientAgent agent);

    /**
     * Pass stream message to app.
     *
     * @param agent Handle to the background agent
     */
    void passStreamMessageToApp(MlmClientAgent agent);

    /**
     * Pass mailbox message to app.
     *
     * @param agent Handle to the background agent
     */
    void passMailboxMessageToApp(MlmClientAgent agent);

    /**
     * Pass service message to app.
     *
     * @param agent Handle to the background agent
     */
    void passServiceMessageToApp(MlmClientAgent agent);

    /**
     * Get first replay command.
     *
     * @param agent Handle to the background agent
     */
    void getFirstReplayCommand(MlmClientAgent agent);

    /**
     * Get next replay command.
     *
     * @param agent Handle to the background agent
     */
    void getNextReplayCommand(MlmClientAgent agent);

    /**
     * Announce unhandled error.
     *
     * @param agent Handle to the background agent
     */
    void announceUnhandledError(MlmClientAgent agent);
}
