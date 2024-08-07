/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.runtime.api;

import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * A {@link RuntimeType} associated with a {@code notification} statement defined in a {@code grouping}.
 */
public interface NotificationRuntimeType extends CompositeRuntimeType {
    @Override
    NotificationEffectiveStatement statement();
}
