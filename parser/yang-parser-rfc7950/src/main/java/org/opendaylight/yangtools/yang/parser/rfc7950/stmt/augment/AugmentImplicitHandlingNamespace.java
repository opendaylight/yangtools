/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.Mutable;

/**
 * Helper namespace for attaching target information to augmentation statements. This is then used to ensure that
 * the effective augment has correct implicit statements created.
 */
@Beta
public final class AugmentImplicitHandlingNamespace {
    public static final @NonNull ParserNamespace<Empty, Mutable<?, ?, ?>> INSTANCE =
        new ParserNamespace<>("augment-implicit-handling");
    public static final @NonNull NamespaceBehaviour<?, ?> BEHAVIOUR = NamespaceBehaviour.statementLocal(INSTANCE);

    private AugmentImplicitHandlingNamespace() {
        // Hidden on purpose
    }
}
