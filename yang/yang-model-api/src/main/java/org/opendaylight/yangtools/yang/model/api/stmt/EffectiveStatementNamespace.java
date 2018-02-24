/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 * Common super-interface for {@link IdentifierNamespace}s which hold {@link EffectiveStatement}s.
 *
 * @author Robert Varga
 *
 * @param <E> Effective statement type
 */
@Beta
public abstract class EffectiveStatementNamespace<E extends NamespacedEffectiveStatement<?>>
    implements IdentifierNamespace<QName, E> {

    protected EffectiveStatementNamespace() {
        // Subclasses should guard against instantiation
    }
}
