/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.model.api;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * Constants associated with OpenDaylight extension in yang-ext.yang.
 *
 * @author Robert Varga
 */
public final class OpenDaylightExtensionsConstants {
    private static final Unqualified MODULE_NAME = Unqualified.of("yang-ext").intern();
    private static final XMLNamespace MODULE_NAMESPACE =
        XMLNamespace.of("urn:opendaylight:yang:extension:yang-ext").intern();
    private static final Revision ORIGINAL_REVISION = Revision.of("2013-07-09");

    /**
     * Runtime baseline identity.
     */
    public static final QNameModule ORIGINAL_MODULE = QNameModule.create(MODULE_NAMESPACE, ORIGINAL_REVISION).intern();

    /**
     * Baseline model source name.
     */
    public static final SourceIdentifier ORIGINAL_SOURCE = new SourceIdentifier(MODULE_NAME, ORIGINAL_REVISION);

    private OpenDaylightExtensionsConstants() {
        // Hidden on purpose
    }

    /**
     * Return identifiers of all sources known to define the metadata extension.
     *
     * @return Collection of identifiers.
     */
    public static Collection<SourceIdentifier> knownModelSources() {
        return ImmutableList.of(ORIGINAL_SOURCE);
    }
}
