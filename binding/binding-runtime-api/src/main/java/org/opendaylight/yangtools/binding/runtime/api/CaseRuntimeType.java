/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.runtime.api;

import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;

/**
 * A {@link RuntimeType} associated with a {@code case} statement.
 */
public interface CaseRuntimeType extends AugmentableRuntimeType, DataRuntimeType {
    @Override
    CaseEffectiveStatement statement();
}
