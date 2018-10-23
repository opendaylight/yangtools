/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.model.api;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * Constants associated with RFC8040.
 *
 * @author Robert Varga
 */
@NonNullByDefault
public final class YangDataConstants {
    private static final String MODULE_NAME = "ietf-restconf";
    private static final URI MODULE_NAMESPACE = URI.create("urn:ietf:params:xml:ns:yang:ietf-restconf");
    private static final Revision RFC8040_REVISION = Revision.of("2017-01-26");

    /**
     * Runtime RFC8040 identity.
     */
    public static final QNameModule RFC8040_MODULE = QNameModule.create(MODULE_NAMESPACE, RFC8040_REVISION).intern();

    /**
     * RFC8040 model source name.
     */
    public static final SourceIdentifier RFC8040_SOURCE = RevisionSourceIdentifier.create(MODULE_NAME,
        RFC8040_REVISION);

    /**
     * Normative prefix to use when importing {@link #RFC8040_SOURCE}.
     */
    public static final String MODULE_PREFIX = "rc";

    private YangDataConstants() {
        throw new UnsupportedOperationException();
    }

    /**
     * Return identifiers of all sources known to define the metadata extension.
     *
     * @return Collection of identifiers.
     */
    public static Collection<SourceIdentifier> knownModelSources() {
        return ImmutableList.of(RFC8040_SOURCE);
    }
}
