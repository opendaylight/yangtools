/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.binding.contract.StatementNamespace;
import org.opendaylight.yangtools.yang.common.AbstractQName;

@NonNullByDefault
final class CamelCaseNamingStrategy extends YangIdentifierClassNamingStrategy {
    private final StatementNamespace namespace;

    CamelCaseNamingStrategy(final StatementNamespace namespace, final AbstractQName nodeIdentifier) {
        super(nodeIdentifier);
        this.namespace = requireNonNull(namespace);
    }

    StatementNamespace namespace() {
        return namespace;
    }

    @Override
    @NonNull ClassNamingStrategy fallback() {
        return namespace.resistant() ? new ResistedCamelCaseNamingStrategy(this)
            : new CamelCaseWithNamespaceNamingStrategy(this);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper).add("namespace", namespace);
    }
}
