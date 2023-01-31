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
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.AbstractQName;

@NonNullByDefault
final class CamelCaseNamingStrategy extends ClassNamingStrategy {
    private final StatementNamespace namespace;
    private final AbstractQName nodeIdentifier;

    CamelCaseNamingStrategy(final StatementNamespace namespace, final AbstractQName nodeIdentifier) {
        this.namespace = requireNonNull(namespace);
        this.nodeIdentifier = requireNonNull(nodeIdentifier);
    }

    @Override
    AbstractQName nodeIdentifier() {
        return nodeIdentifier;
    }

    StatementNamespace namespace() {
        return namespace;
    }

    @Override
    @NonNull ClassNamingStrategy fallback() {
        return new CamelCaseWithNamespaceNamingStrategy(this);
    }

    @Override
    String simpleClassName() {
        return BindingMapping.getClassName(nodeIdentifier.getLocalName());
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("localName", nodeIdentifier.getLocalName()).add("namespace", namespace);
    }
}
