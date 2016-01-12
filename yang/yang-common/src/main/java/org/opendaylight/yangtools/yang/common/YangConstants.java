/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import java.net.URI;

/**
 * Constant definitions present in RFC documents related to the YANG language.
 */
public final class YangConstants {
    /**
     * YANG namespace, as defined in RFC 6020.
     */
    public static final URI RFC6020_YANG_NAMESPACE = URI.create("urn:ietf:params:xml:ns:yang:1");

    /**
     * YIN namespace, as defined in RFC 6020.
     */
    public static final URI RFC6020_YIN_NAMESPACE = URI.create("urn:ietf:params:xml:ns:yang:yin:1");

    /**
     * Base QNameModule for all YANG statements.
     */
    public static final QNameModule RFC6020_YANG_MODULE = QNameModule.create(RFC6020_YANG_NAMESPACE, (ModuleRevision) null).intern();

    /**
     * Base QNameModule for all YIN statements.
     */
    public static final QNameModule RFC6020_YIN_MODULE = QNameModule.create(RFC6020_YIN_NAMESPACE, (ModuleRevision) null).intern();

    private YangConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

}
