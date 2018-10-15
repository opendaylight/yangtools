/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.tailf.common.model.api;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * Constants associated with tailf-common.yang.
 *
 * @author Robert Varga
 */
@NonNullByDefault
public final class TailFCommonConstants {
    private static final String MODULE_NAME = "tailf-common";
    private static final URI MODULE_NAMESPACE = URI.create("http://tail-f.com/yang/common");

    /**
     * Runtime RFC6536 identity.
     */
    public static final QNameModule MODULE = QNameModule.create(MODULE_NAMESPACE).intern();

    /**
     * RFC6536 model source name.
     */
    public static final SourceIdentifier SOURCE = RevisionSourceIdentifier.create(MODULE_NAME);

    /**
     * Normative prefix to use when importing {@link #SOURCE}.
     */
    public static final String MODULE_PREFIX = "tailf";

    private TailFCommonConstants() {
        throw new UnsupportedOperationException();
    }

    /**
     * Return identifiers of all sources known to define NACM extension.
     *
     * @return Collection of identifiers.
     */
    public static Collection<SourceIdentifier> knownModelSources() {
        return ImmutableList.of(SOURCE);
    }
}
