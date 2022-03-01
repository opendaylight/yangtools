/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;

/**
 * Namespace holding the {@link SchemaTreeAwareEffectiveStatement} representation of the statement being augmented by an
 * {@code augment} statement.
 */
@Beta
// FIXME: Unify this with AugmentImplicitHandlingNamespace, as we should be fine with having a requiresEffectiveCtx()
//        reference, from where we can gen implicit handling, if need be
public interface AugmentTargetNamespace extends ParserNamespace<Empty, SchemaTreeAwareEffectiveStatement<?, ?>> {
    NamespaceBehaviour<Empty, SchemaTreeAwareEffectiveStatement<?, ?>, @NonNull AugmentTargetNamespace>
        BEHAVIOUR = NamespaceBehaviour.statementLocal(AugmentTargetNamespace.class);
}
