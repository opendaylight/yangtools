/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import org.opendaylight.yangtools.sal.binding.generator.api.ClassLoadingStrategy;

/**
 *
 * Thrown when user schema for supplied binding class is available in present schema context, but
 * binding class itself is not known to codecs because backing class loading strategy did not
 * provided it.
 *
 */
public class MissingClassInLoadingStrategyException extends MissingSchemaException {

    private static final long serialVersionUID = 1L;

    protected MissingClassInLoadingStrategyException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public static void check(final ClassLoadingStrategy strategy, final Class<?> childClass) {
        try {
            strategy.loadClass(childClass.getName());
        } catch (final ClassNotFoundException e) {
            final String message =
                    String.format("User supplied class %s is not available in %s.", childClass.getName(), strategy);
            throw new MissingClassInLoadingStrategyException(message, e);
        }
    }

}
