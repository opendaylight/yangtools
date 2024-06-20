/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;

/**
 * A {@link CompositeRuntimeType} associated with a RESTCONF {@code yang-data} statement.
 */
public interface YangDataRuntimeType extends CompositeRuntimeType {
    @Override
    YangDataEffectiveStatement statement();
}
