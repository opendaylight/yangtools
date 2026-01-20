/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnrecognizedStatement;

/**
 * An 'implementation' of an effective UnrecognizedStatement. This class is actually never instantiated and exists
 * only as an implementation-private marker for {@link StatementDefinition#getEffectiveRepresentationClass()}.
 */
@NonNullByDefault
interface UnrecognizedEffectiveStatement extends EffectiveStatement<Object, UnrecognizedStatement> {
    // Nothing to see
}
