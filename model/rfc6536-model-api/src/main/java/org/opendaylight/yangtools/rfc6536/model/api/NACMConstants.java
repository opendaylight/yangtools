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
 * Constants associated with RFC6536.
 */
@NonNullByDefault
public final class NACMConstants {
    public static final Unqualified MODULE_NAME = Unqualified.of("ietf-netconf-acm").intern();
    public static final XMLNamespace MODULE_NAMESPACE =
        XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-netconf-acm").intern();
    /**
     * RFC6536 model revision.
     */
    public static final Revision RFC6536_REVISION = Revision.of("2012-02-22");
    /**
     * RFC8341 model revision..
     */
    public static final Revision RFC8341_REVISION = Revision.of("2018-02-14");

    /**
     * Runtime RFC6536 identity.
     */
    public static final QNameModule RFC6536_MODULE = QNameModule.create(MODULE_NAMESPACE, RFC6536_REVISION).intern();

    /**
     * Runtime RFC8341 identity.
     */
    public static final QNameModule RFC8341_MODULE = QNameModule.create(MODULE_NAMESPACE, RFC8341_REVISION).intern();

    /**
     * Normative prefix to use when importing {@link #RFC6536_SOURCE}.
     */
    public static final String MODULE_PREFIX = "nacm";

    private NACMConstants() {
        // Hidden on purpose
    }
}
