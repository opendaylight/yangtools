/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.model.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * Constants associated with RFC8819.
 */
@Beta
@NonNullByDefault
public final class ModuleTagsConstants {
    private static final Unqualified MODULE_NAME = Unqualified.of("ietf-module-tags").intern();
    private static final XMLNamespace MODULE_NAMESPACE =
        XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-module-tags").intern();
    private static final Revision RFC8819_REVISION = Revision.of("2021-01-04");

    /**
     * Runtime RFC88199 identity.
     */
    public static final QNameModule RFC8819_MODULE = QNameModule.create(MODULE_NAMESPACE, RFC8819_REVISION).intern();

    /**
     * RFC8819 model source name.
     */
    public static final SourceIdentifier RFC8819_SOURCE = new SourceIdentifier(MODULE_NAME, RFC8819_REVISION);

    /**
     * Normative prefix to use when importing {@link #RFC8819_SOURCE}.
     */
    public static final String MODULE_PREFIX = "tags";

    private ModuleTagsConstants() {
        // Hidden on purpose
    }

    /**
     * Return identifiers of all sources known to define the tag extension.
     *
     * @return Collection of identifiers.
     */
    public static Collection<SourceIdentifier> knownModelSources() {
        return ImmutableList.of(RFC8819_SOURCE);
    }
}
