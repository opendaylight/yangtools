/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.api;

import com.google.common.annotations.Beta;

/**
 * Thrown when user schema for supplied binding class is available in present schema context, but
 * binding class itself is not known to codecs because backing class loading strategy did not
 * provided it.
 */
@Beta
public class MissingClassInLoadingStrategyException extends MissingSchemaException {
    private static final long serialVersionUID = 1L;

    public MissingClassInLoadingStrategyException(final String msg, final Throwable cause) {
        super(msg, cause);
    }
}
