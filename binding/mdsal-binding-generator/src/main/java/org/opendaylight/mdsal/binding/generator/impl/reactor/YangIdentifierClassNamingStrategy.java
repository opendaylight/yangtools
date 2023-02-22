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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.common.AbstractQName;

/**
 * A {@link ClassNamingStrategy} which is based on a YANG node identifier as captured in
 * {@link AbstractQName#getLocalName()}.
 */
@NonNullByDefault
abstract class YangIdentifierClassNamingStrategy extends ClassNamingStrategy {
    private final AbstractQName nodeIdentifier;

    YangIdentifierClassNamingStrategy(final AbstractQName nodeIdentifier) {
        this.nodeIdentifier = requireNonNull(nodeIdentifier);
    }

    @Override
    final String simpleClassName() {
        return Naming.getClassName(rootName());
    }

    @Override
    final String rootName() {
        return nodeIdentifier.getLocalName();
    }

    @Override
    final String childPackage() {
        return CollisionDomain.packageString(nodeIdentifier);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("localName", rootName());
    }
}
