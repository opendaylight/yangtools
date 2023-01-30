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
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.common.AbstractQName;

final class ModuleNamingStrategy extends ClassNamingStrategy {
    private final @NonNull AbstractQName name;

    ModuleNamingStrategy(final AbstractQName name) {
        this.name = requireNonNull(name);
    }

    @Override
    AbstractQName nodeIdentifier() {
        return name;
    }

    @Override
    String simpleClassName() {
        return BindingMapping.getClassName(name.getLocalName());
    }

    @Override
    ClassNamingStrategy fallback() {
        return null;
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("localName", name.getLocalName());
    }
}
