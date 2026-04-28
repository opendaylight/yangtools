/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl.reactor;

import com.google.common.collect.Collections2;
import java.util.Set;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.SerialVersionBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;

public final class SerialVersionHelper {
    private static final Set<ConcreteType> IGNORED_INTERFACES =
        Set.of(BindingTypes.BITS_TYPE_OBJECT, BindingTypes.SCALAR_TYPE_OBJECT, BindingTypes.UNION_TYPE_OBJECT);

    private SerialVersionHelper() {
        // Hidden on purpose
    }

    public static long computeDefaultSUID(final GeneratedTypeBuilderBase<?> to) {
        final var svb = new SerialVersionBuilder(to.typeName()).setAbstract(to.isAbstract());

        for (var iface : Collections2.filter(to.getImplementsTypes(), item -> !IGNORED_INTERFACES.contains(item))) {
            svb.addInterface(iface.name());
        }
        for (var property : to.getProperties()) {
            svb.addField(property.getName());
        }
        for (var method : to.getMethodDefinitions()) {
            svb.addMethod(method.getName(), method.getAccessModifier());
        }

        return svb.build();
    }
}
