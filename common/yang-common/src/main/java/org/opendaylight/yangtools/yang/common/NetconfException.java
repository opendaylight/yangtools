/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.yang.common.Netconf.Layer;

public abstract class NetconfException extends Exception implements YangError {
    private static final long serialVersionUID = 1L;

    private final Layer layer;
    // FIXME: error-app-tag

    protected NetconfException(final Layer layer, final String message, final Throwable cause) {
        super(requireNonNull(message), cause);
        this.layer = requireNonNull(layer);
    }

    protected NetconfException(final Layer layer, final String message) {
        this(layer, message, null);
    }

    public final Layer getLayer() {
        return layer;
    }
}