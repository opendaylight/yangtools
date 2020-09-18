/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.model.api;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * Constants associated with <a href="https://tools.ietf.org/html/rfc8791">RFC8791</a>.
 *
 * @author Robert Varga
 */
@NonNullByDefault
public final class YangDataStructureConstants {
    private static final String MODULE_NAME = "ietf-yang-structure-ext";
    private static final URI MODULE_NAMESPACE = URI.create("urn:ietf:params:xml:ns:yang:ietf-yang-structure-ext");
    private static final Revision RFC8791_REVISION = Revision.of("2020-06-17");

    /**
     * Runtime RFC8791 identity.
     */
    public static final QNameModule RFC8791_MODULE = QNameModule.create(MODULE_NAMESPACE, RFC8791_REVISION).intern();

    /**
     * RFC8040 model source name.
     */
    public static final SourceIdentifier RFC8791_SOURCE = RevisionSourceIdentifier.create(MODULE_NAME,
        RFC8791_REVISION);

    /**
     * Normative prefix to use when importing {@link #RFC8791_SOURCE}.
     */
    public static final String MODULE_PREFIX = "sx";

    private YangDataStructureConstants() {
        // Hidden on purpose
    }

    /**
     * Return identifiers of all sources known to define the metadata extension.
     *
     * @return Collection of identifiers.
     */
    public static Collection<SourceIdentifier> knownModelSources() {
        return ImmutableList.of(RFC8791_SOURCE);
    }
}
