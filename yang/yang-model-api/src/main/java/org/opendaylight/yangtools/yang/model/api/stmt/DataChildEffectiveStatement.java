/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Common interface grouping all {@link EffectiveStatement}s which are accessible via
 * {@link DataChildEffectiveStatementNamespace}.
 *
 * @author Robert Varga
 * @param <D> Declared statement type
 */
public interface DataChildEffectiveStatement<D extends DeclaredStatement<QName>>
    extends NamespacedEffectiveStatement<D> {

}
