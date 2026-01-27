/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Effective view of an {@link UnrecognizedStatement}. This class never be instantiated.
 */
public sealed interface UnrecognizedEffectiveStatement
    extends EffectiveStatement<Object, @NonNull UnrecognizedStatement> permits UnrecognizedEffectiveStatementImpl {
    // Nothing else
}
