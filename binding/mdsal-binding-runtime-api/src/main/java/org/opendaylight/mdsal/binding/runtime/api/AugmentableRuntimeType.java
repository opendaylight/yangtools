/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;

/**
 * A {@link CompositeRuntimeType} which is also can be targeted by {@code augment} statements.
 */
public interface AugmentableRuntimeType extends CompositeRuntimeType {
    /**
     * Return the {@link AugmentRuntimeType}s extending this type, matching the underlying {@link #statement()}.
     *
     * @return {@link AugmentRuntimeType}s extending this type.
     */
    @NonNull List<AugmentRuntimeType> augments();
}
