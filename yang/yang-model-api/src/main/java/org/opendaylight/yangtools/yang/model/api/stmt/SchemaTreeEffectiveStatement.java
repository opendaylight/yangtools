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
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Common interface grouping all {@link EffectiveStatement}s which are accessible via
 * {@link SchemaTreeAwareEffectiveStatement.Namespace}. This such statement corresponds to a {@code schema node}.
 *
 * <p>
 * This interface could be named {@code SchemaNodeEffectiveStatement}, but that could induce a notion that it has
 * something to do with {@link SchemaNode} -- which it has not. SchemaNode semantics are wrong in may aspects
 * and while implementations of this interface may also implement SchemaNode, the semantics of this interface should
 * always be preferred and SchemaNode is to be treated as deprecated whenever possible.
 *
 * @param <D> Declared statement type
 * @author Robert Varga
 */
@Beta
public interface SchemaTreeEffectiveStatement<D extends DeclaredStatement<QName>>
    extends NamespacedEffectiveStatement<D> {

}
