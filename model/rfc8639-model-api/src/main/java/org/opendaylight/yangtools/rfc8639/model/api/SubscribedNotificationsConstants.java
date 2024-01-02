/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8639.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

/**
 * Constants associated with RFC8639.
 */
@NonNullByDefault
public final class SubscribedNotificationsConstants {
    public static final Unqualified MODULE_NAME = Unqualified.of("ietf-subscribed-notifications").intern();
    public static final XMLNamespace MODULE_NAMESPACE =
        XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-subscribed-notifications").intern();
    /**
     * RFC8639 revision.
     */
    public static final Revision RFC8639_REVISION = Revision.of("2019-09-09");

    /**
     * Runtime RFC8639 identity.
     */
    public static final QNameModule RFC8639_MODULE = QNameModule.create(MODULE_NAMESPACE, RFC8639_REVISION).intern();

    /**
     * Normative prefix to use when importing {@link #RFC8639_SOURCE}.
     */
    public static final String MODULE_PREFIX = "sn";

    private SubscribedNotificationsConstants() {
        // Hidden on purpose
    }
}
