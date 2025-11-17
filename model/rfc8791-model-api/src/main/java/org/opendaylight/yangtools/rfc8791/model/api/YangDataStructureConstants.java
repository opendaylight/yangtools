/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

/**
 * Constants associated with <a href="https://tools.ietf.org/html/rfc8791">RFC8791</a>.
 *
 * @since 14.0.21
 */
@NonNullByDefault
public final class YangDataStructureConstants {
    private static final UnresolvedQName.Unqualified MODULE_NAME =
            UnresolvedQName.Unqualified.of("ietf-yang-structure-ext").intern();
    private static final XMLNamespace MODULE_NAMESPACE =
            XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-yang-structure-ext").intern();
    private static final Revision RFC8791_REVISION = Revision.of("2020-06-17");

    /**
     * Runtime RFC8791 identity.
     */
    public static final QNameModule RFC8791_MODULE = QNameModule.of(MODULE_NAMESPACE, RFC8791_REVISION).intern();

    /**
     * RFC8791 model source name.
     */
    public static final SourceIdentifier RFC8791_SOURCE = new SourceIdentifier(MODULE_NAME, RFC8791_REVISION);

    /**
     * Normative prefix to use when importing {@link #RFC8791_SOURCE}.
     */
    public static final String MODULE_PREFIX = "sx";

    private YangDataStructureConstants() {
        // Hidden on purpose
    }
}
