/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.tree;

import com.google.common.annotations.Beta;

/**
 * Tip of a data tree instance. It acts as a point to which modifications can
 * be applied.
 */
@Beta
public interface DataTreeTip {
    /**
     * Validate whether a particular modification can be applied to the data tree.
     * @param modification
     *                  Data tree modification.
     * @throws DataValidationFailedException
     *                  If modification data is not valid.
     */
    void validate(DataTreeModification modification) throws DataValidationFailedException;

    /**
     * Prepare a modification for commit.
     *
     * @param modification
     *                  Data tree modification.
     * @return candidate data tree
     */
    DataTreeCandidate prepare(DataTreeModification modification);

    /**
     * {@inheritDoc}
     *
     * {@link DataTreeTip} implementations must not override the default identity hashCode method.
     */
    @Override
    int hashCode();

    /**
     * {@inheritDoc}
     *
     * {@link DataTreeTip} implementations must not override the default identity hashCode method, meaning their
     * equals implementation must result in identity comparison.
     */
    @Override
    boolean equals(Object o);
}
