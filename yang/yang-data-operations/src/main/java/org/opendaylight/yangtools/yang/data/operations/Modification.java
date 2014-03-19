/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.operations;

import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

import com.google.common.base.Optional;

/**
 * Strategy interface for data modification.
 *
 * @param <S> SchemaNode type
 * @param <N> LeafNode type
 *
 */
public interface Modification<S, N extends NormalizedNode<?, ?>> {

    Optional<N> modify(S schemaNode, Optional<N> actual, Optional<N> modification, OperationStack operations) throws DataModificationException;
}
