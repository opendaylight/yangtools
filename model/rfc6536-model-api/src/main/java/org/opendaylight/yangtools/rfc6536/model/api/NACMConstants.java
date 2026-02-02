/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

/**
 * Constants associated with RFC6536 and RFC8341.
 */
@NonNullByDefault
public final class NACMConstants {
    /**
     * Type-safe {@code module ietf-netconf-acm} declaration.
     */
    public static final Unqualified MODULE_NAME = Unqualified.of("ietf-netconf-acm").intern();
    /**
     * Type-safe {@code namespace "urn:ietf:params:xml:ns:yang:ietf-netconf-acm"} declaration.
     */
    public static final XMLNamespace MODULE_NAMESPACE =
        XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-netconf-acm").intern();
    /**
     * Normative prefix to use when importing {@link #MODULE_NAME}, corresponding to {@code prefix nacm}.
     */
    public static final String MODULE_PREFIX = "nacm";
    /**
     * RFC6536's type-safe {@code revision "2012-02-22"} declaration.
     */
    public static final Revision RFC6536_REVISION = Revision.of("2012-02-22");
    /**
     * Runtime RFC6536 identity: {@value #MODULE_NAMESPACE} and {@link #RFC6536_REVISION}.
     */
    public static final QNameModule RFC6536_MODULE = QNameModule.of(MODULE_NAMESPACE, RFC6536_REVISION).intern();
    /**
     * RFC8341's type-safe {@code revision "2018-02-14"} declaration.
     */
    public static final Revision RFC8341_REVISION = Revision.of("2018-02-14");
    /**
     * Runtime RFC8341 identity: {@value #MODULE_NAMESPACE} and {@link #RFC8341_REVISION}.
     */
    public static final QNameModule RFC8341_MODULE = QNameModule.of(MODULE_NAMESPACE, RFC8341_REVISION).intern();

    private NACMConstants() {
        // Hidden on purpose
    }
}
