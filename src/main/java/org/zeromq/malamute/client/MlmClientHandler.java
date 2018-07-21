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
     * Set a reference to the MlmClientAgent.
     *
     * @param agent Handle to the background agent
     */
    void setMlmClientAgent(MlmClientAgent agent);

    /**
     * Handle "use plain security mechanism" action.
     */
    void usePlainSecurityMechanism();

    /**
     * Handle "signal success" action.
     */
    void signalSuccess();

    /**
     * Handle "remember client address" action.
     */
    void rememberClientAddress();

    /**
     * Handle "connect to server endpoint" action.
     */
    void connectToServerEndpoint();

    /**
     * Handle "set client address" action.
     */
    void setClientAddress();

    /**
     * Handle "use connect timeout" action.
     */
    void useConnectTimeout();

    /**
     * Handle "signal bad endpoint" action.
     */
    void signalBadEndpoint();

    /**
     * Handle "terminate" action.
     */
    void terminate();

    /**
     * Handle "signal failure" action.
     */
    void signalFailure();

    /**
     * Handle "on client is connected" action.
     */
    void onClientIsConnected();

    /**
     * Handle "signal server not present" action.
     */
    void signalServerNotPresent();

    /**
     * Handle "prepare stream write command" action.
     */
    void prepareStreamWriteCommand();

    /**
     * Handle "prepare stream read command" action.
     */
    void prepareStreamReadCommand();

    /**
     * Handle "prepare stream cancel command" action.
     */
    void prepareStreamCancelCommand();

    /**
     * Handle "prepare service offer command" action.
     */
    void prepareServiceOfferCommand();

    /**
     * Handle "signal bad pattern" action.
     */
    void signalBadPattern();

    /**
     * Handle "on server has gone offline" action.
     */
    void onServerHasGoneOffline();

    /**
     * Handle "check status code" action.
     */
    void checkStatusCode();

    /**
     * Handle "pass stream message to app" action.
     */
    void passStreamMessageToApp();

    /**
     * Handle "pass mailbox message to app" action.
     */
    void passMailboxMessageToApp();

    /**
     * Handle "pass service message to app" action.
     */
    void passServiceMessageToApp();

    /**
     * Handle "get first replay command" action.
     */
    void getFirstReplayCommand();

    /**
     * Handle "get next replay command" action.
     */
    void getNextReplayCommand();

    /**
     * Handle "announce unhandled error" action.
     */
    void announceUnhandledError();
}
