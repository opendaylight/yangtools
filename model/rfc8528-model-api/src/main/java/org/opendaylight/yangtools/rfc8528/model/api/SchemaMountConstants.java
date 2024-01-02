/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

/**
 * Constants associated with RFC8528.
 */
@NonNullByDefault
public final class SchemaMountConstants {
    public static final Unqualified MODULE_NAME = Unqualified.of("ietf-yang-schema-mount").intern();
    public static final XMLNamespace MODULE_NAMESPACE =
        XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-yang-schema-mount").intern();
    /**
     * RFC8528 revision.
     */
    public static final Revision RFC8528_REVISION = Revision.of("2019-01-14");

    /**
     * Runtime RFC8528 identity.
     */
    public static final QNameModule RFC8528_MODULE = QNameModule.create(MODULE_NAMESPACE, RFC8528_REVISION).intern();

    /**
     * Normative prefix to use when importing {@link #MODULE_NAME}.
     */
    public static final String MODULE_PREFIX = "yangmnt";

    private SchemaMountConstants() {
        // Hidden on purpose
    }
}
