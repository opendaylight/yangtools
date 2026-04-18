/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import java.io.Serializable;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.lib.CodeHelpers;

/**
 * Base Identity. Interface generated for {@code identity} statements extend this interface.
 */
public non-sealed interface BaseIdentity extends BindingObject, BindingContract<BaseIdentity>, Serializable {
    /**
     * {@inheritDoc}
     *
     * <p>Implementations are required to return the equivalent of {@code implementedInterface().hashCode()}.
     */
    @Override
    int hashCode();

    /**
     * {@inheritDoc}
     *
     * <p>Implementations are required to compare {@link #implementedInterface()} for equality.
     */
    @Override
    boolean equals(Object obj);

    /**
     * {@inheritDoc}
     *
     * @see CodeHelpers#biTS(Class, org.opendaylight.yangtools.yang.common.QName)
     */
    @Override
    @NonNull String toString();
}
