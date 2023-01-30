/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A {@link ClassNamingStrategy} which considers a {@link StatementNamespace} when assigning names.
 */
@NonNullByDefault
abstract class AbstractNamespacedNamingStrategy extends ClassNamingStrategy {
    private final StatementNamespace namespace;

    AbstractNamespacedNamingStrategy(final StatementNamespace namespace) {
        this.namespace = requireNonNull(namespace);
    }

    @Override
    final @NonNull ClassNamingStrategy fallback() {
        return new AppendNamespaceNamingStrategy(this);
    }

    final StatementNamespace namespace() {
        return namespace;
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("namespace", namespace);
    }
}
