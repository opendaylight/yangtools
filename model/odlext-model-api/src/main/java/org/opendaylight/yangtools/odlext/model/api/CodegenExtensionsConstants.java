/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

/**
 * Constants related to {@code odl-codegen-extensions.yang}.
 */
@NonNullByDefault
public final class CodegenExtensionsConstants {
    public static final Unqualified MODULE_NAME = Unqualified.of("odl-codegen-extensions").intern();
    public static final XMLNamespace MODULE_NAMESPACE =
        XMLNamespace.of("urn:opendaylight:yang:extension:codegen").intern();
    /**
     * Baseline revision.
     */
    public static final Revision ORIGINAL_REVISION = Revision.of("2024-06-27");

    /**
     * Runtime baseline identity.
     */
    public static final QNameModule ORIGINAL_MODULE = QNameModule.of(MODULE_NAMESPACE, ORIGINAL_REVISION).intern();

    private CodegenExtensionsConstants() {
        // Hidden on purpose
    }
}
