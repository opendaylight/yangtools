/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.model.api;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * Constants associated with OpenDaylight extension in yang-ext.yang.
 *
 * @author Robert Varga
 */
@NonNullByDefault
public final class OpenConfigConstants {
    private static final String MODULE_NAME = "yang-ext";

    // Package-visible, because openconfig-version applies across all known revisions and needs to bind to all of them
    static final URI MODULE_NAMESPACE = URI.create("http://openconfig.net/yang/openconfig-ext");

    // Initial revision, defining semantic-version
    private static final Revision SEMVER_REVISION = Revision.of("2015-10-09");

    // Revised extension, adds openconfig-encrypted-value
    private static final Revision ENCRYPTED_VALUE_REVISION = Revision.of("2017-01-29");

    // Revised extension, renames openconfig-encrypted-value to openconfig-hashed-value
    private static final Revision HASHED_VALUE_REVISION = Revision.of("2017-04-11");

    // Revised extension, adds extension for POSIX pattern statements
    private static final Revision REGEXP_POSIX_REVISION = Revision.of("2020-06-16");

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
     * Original model source name.
     */
    public static final SourceIdentifier SEMVER_SOURCE = RevisionSourceIdentifier.create(MODULE_NAME,
        SEMVER_REVISION);

    /**
     * Original model source name.
     */
    public static final SourceIdentifier ENCRYPTED_VALUE_SOURCE = RevisionSourceIdentifier.create(MODULE_NAME,
        ENCRYPTED_VALUE_REVISION);

    /**
     * Original model source name.
     */
    public static final SourceIdentifier HASHED_VALUE_SOURCE = RevisionSourceIdentifier.create(MODULE_NAME,
        HASHED_VALUE_REVISION);

    /**
     * Original model source name.
     */
    public static final SourceIdentifier REGEXP_POSIX_SOURCE = RevisionSourceIdentifier.create(MODULE_NAME,
            REGEXP_POSIX_REVISION);
    /**
     * Normative prefix to use when importing {@link #SEMVER_SOURCE} and later.
     */
    public static final String MODULE_PREFIX = "oc-ext";

    private OpenConfigConstants() {
        // Hidden on purpose
    }

    /**
     * Return identifiers of all sources known to define the metadata extension.
     *
     * @return Collection of identifiers.
     */
    public static Collection<SourceIdentifier> knownModelSources() {
        return ImmutableList.of(HASHED_VALUE_SOURCE, ENCRYPTED_VALUE_SOURCE, SEMVER_SOURCE);
    }
}
