/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

/**
 * Enumeration of NETCONF layers, as established in
 * <a href="https://www.rfc-editor.org/rfc/rfc4741#section-1.1">NETCONF</a>. This enumeration exists because its
 * semantics are implied by RFC6020 references to {@code error-tag} and its XML encoding.
 *
 * <p>
 * This enumeration corresponds to the {@code Layer} in:
 * <pre><code>
 *   NETCONF can be conceptually partitioned into four layers:
 *
 *              Layer                      Example
 *         +-------------+      +-----------------------------+
 *     (4) |   Content   |      |     Configuration data      |
 *         +-------------+      +-----------------------------+
 *                |                           |
 *         +-------------+      +-----------------------------+
 *     (3) | Operations  |      | &lt;get-config&gt;, &lt;edit-config&gt; |
 *         +-------------+      +-----------------------------+
 *                |                           |
 *         +-------------+      +-----------------------------+
 *     (2) |     RPC     |      |    &lt;rpc&gt;, &lt;rpc-reply&gt;       |
 *         +-------------+      +-----------------------------+
 *                |                           |
 *         +-------------+      +-----------------------------+
 *     (1) |  Transport  |      |   BEEP, SSH, SSL, console   |
 *         |   Protocol  |      |                             |
 *         +-------------+      +-----------------------------+
 * </code></pre>
 * as acknowledged in <a href="https://www.rfc-editor.org/rfc/rfc6241#section-1.2">RFC6241</a>:
 * <pre>
 *   The YANG data modeling language [RFC6020] has been developed for
 *   specifying NETCONF data models and protocol operations, covering the
 *   Operations and the Content layers of Figure 1.
 * </pre>
 */
public enum NetconfLayer {
    /**
     * Content layer, for example configuration data. This layer is implied indirectly in all YANG-based validation and
     * corresponds to {@link ErrorType#APPLICATION}
     */
    CONTENT,
    /**
     * Operations layer, for example {@code <get-config>}, {@code <edit-config>} configuration data. This corresponds to
     * {@link ErrorType#PROTOCOL}.
     */
    OPERATIONS,
    /**
     * RPC layer, for example {@code <rpc>}, {@code <rpc-reply>}. This corresponds to {@link ErrorType#RPC}.
     */
    RPC,
    /**
     * Transport protocol layer, for example BEEP, SSH, TLS, console. This corresponds to {@link ErrorType#TRANSPORT}.
     */
    TRANSPORT;
}
