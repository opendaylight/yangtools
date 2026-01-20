/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.odlext.model.api.AugmentIdentifierEffectiveStatement;
import org.opendaylight.yangtools.odlext.model.api.AugmentIdentifierStatement;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.UserNamespace;

final class AugmentIdentifierNamespace {
    static final @NonNull UserNamespace<
        Empty,
        StmtContext<Unqualified, AugmentIdentifierStatement, AugmentIdentifierEffectiveStatement>> INSTANCE =
        UserNamespace.of("augment-identifier");
    static final @NonNull NamespaceBehaviour<?, ?> BEHAVIOUR = NamespaceBehaviour.statementLocal(INSTANCE);

    private AugmentIdentifierNamespace() {
        // Hidden on purpose
    }
}
