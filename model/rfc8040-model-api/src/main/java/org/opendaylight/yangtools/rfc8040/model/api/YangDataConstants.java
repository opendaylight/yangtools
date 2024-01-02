/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

/**
 * Constants associated with RFC8040.
 */
@NonNullByDefault
public final class YangDataConstants {
    public static final Unqualified MODULE_NAME = Unqualified.of("ietf-restconf").intern();
    public static final XMLNamespace MODULE_NAMESPACE =
        XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-restconf").intern();
    /**
     * RFC8040 revision.
     */
    private static final Revision RFC8040_REVISION = Revision.of("2017-01-26");

    /**
     * Runtime RFC8040 identity.
     */
    public static final QNameModule RFC8040_MODULE = QNameModule.create(MODULE_NAMESPACE, RFC8040_REVISION).intern();

    /**
     * Normative prefix to use when importing {@link #RFC8040_SOURCE}.
     */
    public static final String MODULE_PREFIX = "rc";

    private YangDataConstants() {
        // Hidden on purpose
    }
}
