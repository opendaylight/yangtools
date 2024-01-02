/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

/**
 * Constants associated with RFC7952.
 */
@NonNullByDefault
public final class MetadataConstants {
    public static final Unqualified MODULE_NAME = Unqualified.of("ietf-yang-metadata").intern();
    public static final XMLNamespace MODULE_NAMESPACE =
        XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-yang-metadata").intern();
    /**
     * RFC7952 revision.
     */
    public static final Revision RFC7952_REVISION = Revision.of("2016-08-05");

    /**
     * Runtime RFC7952 identity.
     */
    public static final QNameModule RFC7952_MODULE = QNameModule.create(MODULE_NAMESPACE, RFC7952_REVISION).intern();

    /**
     * Normative prefix to use when importing {@link #MODULE_NAME}.
     */
    public static final String MODULE_PREFIX = "md";

    private MetadataConstants() {
        // Hidden on purpose
    }
}
