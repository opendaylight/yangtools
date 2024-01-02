/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.model.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

@Beta
public final class IetfYangSmiv2Constants {
    public static final Unqualified MODULE_NAME = Unqualified.of("ietf-yang-smiv2").intern();
    public static final XMLNamespace MODULE_NAMESPACE =
        XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-yang-smiv2").intern();
    /**
     * RFC6643 revision.
     */
    public static final Revision RFC6643_REVISION = Revision.of("2012-06-22");

    /**
     * Runtime RFC6643 identity.
     */
    public static final QNameModule RFC6643_MODULE = QNameModule.create(MODULE_NAMESPACE, RFC6643_REVISION).intern();

    /**
     * Normative prefix to use when importing {@link #RFC6643_SOURCE}.
     */
    public static final String MODULE_PREFIX = "smiv2";

    private IetfYangSmiv2Constants() {
        // Hidden on purpose
    }
}
