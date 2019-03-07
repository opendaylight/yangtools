/*
 * Copyright (c) 2019 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8528.model.api;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * Constants associated with RFC8528.
 *
 * @author Robert Varga
 */
@NonNullByDefault
public final class SchemaMountConstants {
    private static final String MODULE_NAME = "ietf-yang-schema-mount";
    private static final URI MODULE_NAMESPACE = URI.create("urn:ietf:params:xml:ns:yang:ietf-yang-schema-mount");
    private static final Revision RFC8528_REVISION = Revision.of("2019-01-14");

    /**
     * Runtime RFC8528 identity.
     */
    public static final QNameModule RFC8528_MODULE = QNameModule.create(MODULE_NAMESPACE, RFC8528_REVISION).intern();

    /**
     * RFC8528 model source name.
     */
    public static final SourceIdentifier RFC8528_SOURCE = RevisionSourceIdentifier.create(MODULE_NAME,
        RFC8528_REVISION);

    /**
     * Normative prefix to use when importing {@link #RFC8528_SOURCE}.
     */
    public static final String MODULE_PREFIX = "yangmnt";

    private SchemaMountConstants() {
        throw new UnsupportedOperationException();
    }

    /**
     * Return identifiers of all sources known to define the metadata extension.
     *
     * @return Collection of identifiers.
     */
    public static Collection<SourceIdentifier> knownModelSources() {
        return ImmutableList.of(RFC8528_SOURCE);
    }
}
