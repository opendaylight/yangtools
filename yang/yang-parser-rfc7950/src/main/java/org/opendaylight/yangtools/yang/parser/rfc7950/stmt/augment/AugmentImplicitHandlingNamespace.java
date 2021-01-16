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
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;

/**
 * Helper namespace for attaching target information to augmentation statements. This is then used to ensure that
 * the effective augment has correct implicit statements created.
 */
@Beta
public interface AugmentImplicitHandlingNamespace
        extends ParserNamespace<Empty, StatementContextBase<?, ?, ?>> {
    NamespaceBehaviour<Empty, StatementContextBase<?, ?, ?>, @NonNull AugmentImplicitHandlingNamespace>
        BEHAVIOUR = NamespaceBehaviour.statementLocal(AugmentImplicitHandlingNamespace.class);

}
