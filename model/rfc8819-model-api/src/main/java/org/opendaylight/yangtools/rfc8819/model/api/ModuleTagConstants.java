/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.model.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

@Beta
public final class ModuleTagConstants {
    private static final XMLNamespace RFC8819_NAMESPACE =
            XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-module-tags").intern();
    private static final Revision RFC8819_REVISION = Revision.of("2021-01-04");
    public static final QNameModule RFC8819_MODULE = QNameModule.of(RFC8819_NAMESPACE, RFC8819_REVISION).intern();

    private ModuleTagConstants() {
        // Hidden on purpose
    }
}
