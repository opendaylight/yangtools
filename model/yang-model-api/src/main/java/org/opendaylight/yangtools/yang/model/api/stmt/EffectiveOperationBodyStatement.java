/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.opendaylight.yangtools.yang.common.QName;

/**
 * Common capture of effective traits shared by {@code input} and {@code output} statements.
 */
public sealed interface EffectiveOperationBodyStatement<D extends DeclaredOperationBodyStatement>
    extends DataTreeEffectiveStatement<D>, DataTreeAwareEffectiveStatement<QName, D>,
            TypedefAwareEffectiveStatement<QName, D>
    permits InputEffectiveStatement, OutputEffectiveStatement {
    // Nothing else
}
