/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeEffectiveStatement;

/**
 * A {@link RuntimeType} associated with a statement with similar semantics as {@code container} statement.
 */
@Beta
public interface ContainerLikeRuntimeType<D extends DeclaredStatement<QName>,
        E extends DataTreeEffectiveStatement<D> & DataTreeAwareEffectiveStatement<QName, D>>
        extends CompositeRuntimeType, DataRuntimeType {
    @Override
    E statement();
}
