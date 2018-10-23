/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.model.api;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * Constants associated with RFC6536.
 *
 * @author Robert Varga
 */
@NonNullByDefault
public final class NACMConstants {
    private static final String MODULE_NAME = "ietf-netconf-acm";
    private static final URI MODULE_NAMESPACE = URI.create("urn:ietf:params:xml:ns:yang:ietf-netconf-acm");
    private static final Revision RFC6536_REVISION = Revision.of("2012-02-22");

    /**
     * Runtime RFC6536 identity.
     */
    public static final QNameModule RFC6536_MODULE = QNameModule.create(MODULE_NAMESPACE, RFC6536_REVISION).intern();

    /**
     * RFC6536 model source name.
     */
    public static final SourceIdentifier RFC6536_SOURCE = RevisionSourceIdentifier.create(MODULE_NAME,
        RFC6536_REVISION);

    /**
     * Normative prefix to use when importing {@link #RFC6536_SOURCE}.
     */
    public static final String MODULE_PREFIX = "nacm";

    private NACMConstants() {
        throw new UnsupportedOperationException();
    }

    /**
     * Return identifiers of all sources known to define NACM extension.
     *
     * @return Collection of identifiers.
     */
    public static Collection<SourceIdentifier> knownModelSources() {
        return ImmutableList.of(RFC6536_SOURCE);
    }
}
