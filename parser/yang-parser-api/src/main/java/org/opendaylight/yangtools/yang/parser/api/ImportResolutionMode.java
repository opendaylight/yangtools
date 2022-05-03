/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.api;

/**
 * Enumeration of various ways of resolving {@code import} statements.
 */
public enum ImportResolutionMode {
    /**
     * Standard method as specified by <a href="https://tools.ietf.org/html/rfc6020#section-7.1.5">RFC6020</a> and
     * refined by <a href="https://tools.ietf.org/html/rfc7950#section-7.1.5">RFC7950</a>. {@code import}s are resolved
     * based on exact match of {@code revision-date} statement. In case it is not specified, latest available revision
     * is used.
     */
    DEFAULT;
}
