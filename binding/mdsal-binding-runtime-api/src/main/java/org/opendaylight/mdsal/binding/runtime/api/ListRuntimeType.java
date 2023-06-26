/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;

/**
 * A {@link RuntimeType} associated with a {@code list} statement.
 */
public interface ListRuntimeType extends AugmentableRuntimeType, DataRuntimeType {
    @Override
    ListEffectiveStatement statement();

    /**
     * Return the run-time type for this list's {@code key} statement, if present.
     *
     * @return This list's key run-time type, or null if not present
     */
    @Nullable KeyRuntimeType keyType();
}
