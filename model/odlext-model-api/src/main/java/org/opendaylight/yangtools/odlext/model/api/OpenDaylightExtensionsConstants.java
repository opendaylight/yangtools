/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.model.api;

import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

/**
 * Constants associated with OpenDaylight extension in yang-ext.yang.
 */
public final class OpenDaylightExtensionsConstants {
    public static final Unqualified MODULE_NAME = Unqualified.of("yang-ext").intern();
    public static final XMLNamespace MODULE_NAMESPACE =
        XMLNamespace.of("urn:opendaylight:yang:extension:yang-ext").intern();
    /**
     * Baseline revision.
     */
    public static final Revision ORIGINAL_REVISION = Revision.of("2013-07-09");

    /**
     * Runtime baseline identity.
     */
    public static final QNameModule ORIGINAL_MODULE = QNameModule.of(MODULE_NAMESPACE, ORIGINAL_REVISION).intern();

    private OpenDaylightExtensionsConstants() {
        // Hidden on purpose
    }
}
