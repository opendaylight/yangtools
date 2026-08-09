/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 * Copyright (c) 2026 PANTHEON.tech, s.r.o.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model;

import com.google.common.annotations.Beta;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.impl.GetterMethod0;
import org.opendaylight.yangtools.binding.model.impl.GetterMethod1;
import org.opendaylight.yangtools.binding.model.impl.GetterMethodN;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

/**
 * Prototype for a getter method carried in a {@link DataContainer}.
 */
@Beta
@NonNullByDefault
public sealed interface GetterMethod extends Immutable permits GetterMethod0, GetterMethod1, GetterMethodN {
    /**
     * {@return the {@link SchemaTreeEffectiveStatement} which led to this method}
     */
    // TODO: this is separate from returnType construct, but in some cases they overlap, like in:
    //         container foo {
    //           container bar;    <-- generates getBar() with ContainerObjectArchetype which has the same statement
    //         }
    SchemaTreeEffectiveStatement<?> statement();

    /**
     * {@return the method's suffix, e.g. the {@code Foo} in {@code getFoo}. Typically computed on each access.}
     */
    default String suffix() {
        return Naming.getGetterSuffix(statement().argument());
    }

    /**
     * {@return the method return type}
     */
    // FIXME: dedicated 'ReturnType'
    Type returnType();

    /**
     * {@return List of annotation definitions attached to this method}
     */
    List<GetterAnnotation> annotations();

    // FIXME: do not take a name
    static GetterMethod of(final SchemaTreeEffectiveStatement<?> statement, final Type returnType) {
        return new GetterMethod0(statement, returnType);
    }

    static GetterMethod of(final SchemaTreeEffectiveStatement<?> statement, final Type returnType,
            final GetterAnnotation annotation) {
        return new GetterMethod1(statement, returnType, annotation);
    }

    static GetterMethod of(final SchemaTreeEffectiveStatement<?> statement, final Type returnType,
            final List<GetterAnnotation> annotations) {
        return switch (annotations.size()) {
            case 0 -> of(statement, returnType);
            case 1 -> of(statement, returnType, annotations.getFirst());
            default -> {
                final var checked = new ArrayList<GetterAnnotation>(annotations.size());
                for (var annotation : annotations) {
                    if (!annotation.repeatable()) {
                        final var type = annotation.type();
                        for (var existing : checked) {
                            if (annotation.equals(existing)) {
                                throw new IllegalArgumentException("Attempt to repeat " + annotation);
                            }
                            if (type.equals(existing.type())) {
                                throw new IllegalArgumentException(
                                    "Attempt to repeat " + annotation + " after " + existing);
                            }
                        }
                    }
                    checked.add(annotation);
                }
                yield new GetterMethodN(statement, returnType, List.copyOf(checked));
            }
        };
    }
}
