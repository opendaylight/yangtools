/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.model.api;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * Constants associated with RFC7952.
 *
 * @author Robert Varga
 */
@NonNullByDefault
public final class MetadataConstants {
    private static final String MODULE_NAME = "ietf-yang-metadata";
    private static final URI MODULE_NAMESPACE = URI.create("urn:ietf:params:xml:ns:yang:ietf-yang-metadata");
    private static final Revision RFC7952_REVISION = Revision.of("2016-08-05");

    /**
     * Runtime RFC7952 identity.
     */
    public static final QNameModule RFC7952_MODULE = QNameModule.create(MODULE_NAMESPACE, RFC7952_REVISION).intern();

    /**
     * RFC7952 model source name.
     */
    public static final SourceIdentifier RFC7952_SOURCE = RevisionSourceIdentifier.create(MODULE_NAME,
        RFC7952_REVISION);

    /**
     * Normative prefix to use when importing {@link #RFC7952_SOURCE}.
     */
    public static final String MODULE_PREFIX = "md";

    private MetadataConstants() {
        throw new UnsupportedOperationException();
    }

    /**
     * Return identifiers of all sources known to define the metadata extension.
     *
     * @return Collection of identifiers.
     */
    public static Collection<SourceIdentifier> knownModelSources() {
        return ImmutableList.of(RFC7952_SOURCE);
    }
}
