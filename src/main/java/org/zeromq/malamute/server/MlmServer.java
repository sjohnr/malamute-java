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

import org.zeromq.ContextFactory;
import org.zeromq.api.Context;
import org.zeromq.api.Message;
import org.zeromq.api.Socket;
import org.zeromq.api.SocketType;

/**
 * MlmServer class.
 * <p>
 * This is a server implementation of the Malamute Protocol.
 *
 * @author sriesenberg
 */
public class MlmServer {
    // Structure of our class
    private Context context;
    private Socket pipe;
    private MlmServerAgent agent;

    /**
     * Create a MlmServer.
     *
     * @param handler The application callback handler
     */
    public MlmServer(MlmServerHandler handler) {
        this(ContextFactory.createContext(1), handler);
    }

    /**
     * Create a MlmServer.
     *
     * @param context The 0MQ context
     * @param handler The application callback handler
     */
    public MlmServer(Context context, MlmServerHandler handler) {
        this.context = context;
        this.agent = new MlmServerAgent(context, handler);
        this.pipe = context.buildSocket(SocketType.PAIR).connect(String.format("inproc://pipe-%s", agent.toString()));
    }

    /**
     * Bind to the given endpoint.
     *
     * @param endpoint The endpoint to bind to
     * @return The ephemeral port that is bound to if ephemeral, else 0
     */
    public int bind(String endpoint) {
        pipe.send(new Message("BIND").addString(endpoint));
        return pipe.receiveMessage().popInt();
    }

    /**
     * Ask the background agent what port it is bound to.
     *
     * @return The port that the background agent is bound to
     */
    public int getPort() {
        pipe.send(new Message("PORT"));
        return pipe.receiveMessage().popInt();
    }

    /**
     * Set the verbose logging flag of the background agent.
     *
     * @param verbose The flag controlling whether verbose logging is enabled
     */
    public void setVerbose(boolean verbose) {
        pipe.send(new Message(verbose ? 1 : 0));
    }

    /**
     * Send a custom command to the background agent.
     *
     * @param command The command to execute
     * @param message The message to send
     * @return The reply from the background agent
     */
    public Message execute(String command, Message message) {
        pipe.send(new Message(command).addFrames(message));
        return pipe.receiveMessage();
    }
}
