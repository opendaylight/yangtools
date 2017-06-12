/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.util;

/**
 * Unchecked exception thrown if yang definition is not valid according to
 * {YangModelBasicValidationListener}.
 *
 * @deprecated This exception is not used anywhere except {@link ModuleDependencySort} and is scheduled for removal
 *             along with that class.
 */
@Deprecated
public final class YangValidationException extends RuntimeException {

    private static final long serialVersionUID = 7414330400390825381L;

    public YangValidationException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public YangValidationException(final String message) {
        super(message);
    }

}