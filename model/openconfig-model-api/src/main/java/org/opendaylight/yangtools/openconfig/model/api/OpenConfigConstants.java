/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;

/**
 * Constants associated with OpenDaylight extension in yang-ext.yang.
 */
@NonNullByDefault
public final class OpenConfigConstants {
    public static final Unqualified MODULE_NAME = Unqualified.of("openconfig-extensions").intern();

    // Package-visible, because openconfig-version applies across all known revisions and needs to bind to all of them
    public static final XMLNamespace MODULE_NAMESPACE =
        XMLNamespace.of("http://openconfig.net/yang/openconfig-ext").intern();

    // Initial revision, defining semantic-version
    public static final Revision SEMVER_REVISION = Revision.of("2015-10-09");

    // Revised extension, adds openconfig-encrypted-value
    public static final Revision ENCRYPTED_VALUE_REVISION = Revision.of("2017-01-29");

    // Revised extension, renames openconfig-encrypted-value to openconfig-hashed-value
    public static final Revision HASHED_VALUE_REVISION = Revision.of("2017-04-11");

    // Revised extension, adds extension for POSIX pattern statements
    public static final Revision REGEXP_POSIX_REVISION = Revision.of("2020-06-16");

    /**
    * Runtime identity of model which exposed regexp-posix.
    */
    public static final QNameModule REGEXP_POSIX_MODULE = QNameModule.create(MODULE_NAMESPACE, REGEXP_POSIX_REVISION)
            .intern();
    /**
     * Runtime identity of initial model.
     */
    public static final QNameModule SEMVER_MODULE = QNameModule.create(MODULE_NAMESPACE, SEMVER_REVISION).intern();

    /**
     * Runtime identity of model which exposed encrypted-value.
     */
    public static final QNameModule ENCRYPTED_VALUE_MODULE = QNameModule.create(MODULE_NAMESPACE,
        ENCRYPTED_VALUE_REVISION).intern();

    /**
     * Runtime identity of model which exposed encrypted-value.
     */
    public static final QNameModule HASHED_VALUE_MODULE = QNameModule.create(MODULE_NAMESPACE, HASHED_VALUE_REVISION)
            .intern();

    /**
     * Normative prefix to use when importing {@link #MODULE_NAME} and later.
     */
    public static final String MODULE_PREFIX = "oc-ext";

    private OpenConfigConstants() {
        // Hidden on purpose
    }
}
