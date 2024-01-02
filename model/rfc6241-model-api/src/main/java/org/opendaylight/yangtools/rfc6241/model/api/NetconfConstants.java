/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

/**
 * Constants associated with RFC6241.
 */
@NonNullByDefault
public final class NetconfConstants {
    public static final Unqualified MODULE_NAME = Unqualified.of("ietf-netconf").intern();
    public static final XMLNamespace MODULE_NAMESPACE =
        XMLNamespace.of("urn:ietf:params:xml:ns:netconf:base:1.0").intern();
    public static final Revision RFC6241_REVISION = Revision.of("2011-06-01");

    /**
     * Runtime RFC6241 identity.
     */
    public static final QNameModule RFC6241_MODULE = QNameModule.create(MODULE_NAMESPACE, RFC6241_REVISION).intern();

    /**
     * Normative prefix to use when importing {@link #MODULE_NAME}.
     */
    public static final String MODULE_PREFIX = "nc";

    private NetconfConstants() {
        // Hidden on purpose
    }
}
