/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.model.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

@Beta
public final class IetfYangSmiv2Constants {
    private static final String MODULE_NAME = "ietf-yang-smiv2";
    private static final URI MODULE_NAMESPACE = URI.create("urn:ietf:params:xml:ns:yang:ietf-yang-smiv2");
    private static final Revision RFC6643_REVISION = Revision.of("2012-06-22");

    /**
     * Runtime RFC6643 identity.
     */
    public static final QNameModule RFC6643_MODULE = QNameModule.create(MODULE_NAMESPACE, RFC6643_REVISION).intern();

    /**
     * RFC6643 model source name.
     */
    public static final SourceIdentifier RFC6643_SOURCE = RevisionSourceIdentifier.create(MODULE_NAME,
        RFC6643_REVISION);

    /**
     * Normative prefix to use when importing {@link #RFC6643_SOURCE}.
     */
    public static final String MODULE_PREFIX = "smiv2";

    /**
     * Return identifiers of all sources known to define NACM extension.
     *
     * @return Collection of identifiers.
     */
    public static Collection<SourceIdentifier> knownModelSources() {
        return ImmutableList.of(RFC6643_SOURCE);
    }

    private IetfYangSmiv2Constants() {
        // Hidden on purpose
    }
}
