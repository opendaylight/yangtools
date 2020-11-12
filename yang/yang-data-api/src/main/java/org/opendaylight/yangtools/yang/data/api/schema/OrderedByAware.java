/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.ItemOrder;
import org.opendaylight.yangtools.concepts.ItemOrder.Ordered;
import org.opendaylight.yangtools.concepts.ItemOrder.Unordered;
import org.opendaylight.yangtools.yang.model.api.stmt.OrderedByStatement.Ordering;

/**
 * Marker interfaces for {@link NormalizedNode}s which have distinct ordering requirements.
 */
@Beta
public interface OrderedByAware<O extends ItemOrder<O>> {
    public interface OrderedBySystem extends OrderedByAware<Unordered>, Unordered {
        @Override
        default Ordering orderedBy() {
            return Ordering.SYSTEM;
        }
    }

    public interface OrderedByUser extends OrderedByAware<Ordered>, Ordered {
        @Override
        default Ordering orderedBy() {
            return Ordering.USER;
        }
    }

    @NonNull Ordering orderedBy();
}
