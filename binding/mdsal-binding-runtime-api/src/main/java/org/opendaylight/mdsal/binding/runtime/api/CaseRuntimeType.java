/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;

/**
 * A {@link RuntimeType} associated with a {@code case} statement.
 */
@Beta
public interface CaseRuntimeType extends CompositeRuntimeType, DataRuntimeType {
    @Override
    CaseEffectiveStatement statement();

    /**
     * Return the runtime type for the original manifestation of this type's {@code case} statement. Returns
     * {@code null} if this type is the original.
     *
     * @return Original manifestatation, or {@code null} if this is the original manifestation.
     */
    @Nullable CaseRuntimeType originalType();
}
