/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.ModuleLike;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

final class Util {
    private Util() {
        // Hidden on purpose
    }

    static @NonNull SourceIdentifier moduleToIdentifier(final ModuleLike module) {
        return new SourceIdentifier(Unqualified.of(module.getName()), module.getRevision().orElse(null));
    }
}
