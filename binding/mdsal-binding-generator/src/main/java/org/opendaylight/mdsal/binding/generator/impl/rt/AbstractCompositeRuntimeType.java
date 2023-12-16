/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

import com.google.common.base.Functions;
import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.GeneratedRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

abstract class AbstractCompositeRuntimeType<S extends EffectiveStatement<?, ?>>
        extends AbstractRuntimeType<S, GeneratedType> implements CompositeRuntimeType {
    private static final RuntimeType[] EMPTY = new RuntimeType[0];

    private final ImmutableMap<JavaTypeName, GeneratedRuntimeType> byClass;
    private final Object bySchemaTree;

    AbstractCompositeRuntimeType(final GeneratedType bindingType, final S statement, final List<RuntimeType> children) {
        super(bindingType, statement);

        byClass = children.stream()
            .filter(GeneratedRuntimeType.class::isInstance)
            .map(GeneratedRuntimeType.class::cast)
            .collect(ImmutableMap.toImmutableMap(GeneratedRuntimeType::getIdentifier, Functions.identity()));

        final var tmp = children.stream()
            .filter(child -> child.statement() instanceof SchemaTreeEffectiveStatement)
            .toArray(RuntimeType[]::new);
        bySchemaTree = switch (tmp.length) {
            case 0 -> EMPTY;
            case 1 -> tmp[0];
            default -> {
                Arrays.sort(tmp, (o1, o2) -> {
                    final int cmp = extractQName(o1).compareTo(extractQName(o2));
                    if (cmp == 0) {
                        throw new VerifyException("Type " + o1 + " conflicts with " + o2 + " on schema tree");
                    }
                    return cmp;
                });
                yield tmp;
            }
        };
    }

    @Override
    public final RuntimeType schemaTreeChild(final QName qname) {
        if (bySchemaTree instanceof RuntimeType tmp) {
            return qname.equals(tmp.statement().argument()) ? tmp : null;
        }

        final var tmp = (RuntimeType[]) bySchemaTree;
        // Here we are assuming that Arrays.binarySearch() accepts a null object, so as to help CHA by not introducing
        // a fake RuntimeType class and the corresponding instanceof checks to side-step the statement lookup (which
        // would need more faking).
        // We make a slight assumption that o2 is the what we specify as a key, but can recover if it is the other way
        // around.
        final int offset = Arrays.binarySearch(tmp, null,
            (o1, o2) -> extractQName(requireNonNullElse(o1, o2)).compareTo(qname));
        return offset < 0 ? null : tmp[offset];
    }

    @Override
    public final GeneratedRuntimeType bindingChild(final JavaTypeName typeName) {
        return byClass.get(requireNonNull(typeName));
    }

    // Makes an assertion of all types being of specified type
    @SuppressWarnings("unchecked")
    final <T extends RuntimeType> @NonNull List<T> schemaTree(final Class<T> expectedType) {
        if (expectedType.isInstance(bySchemaTree)) {
            return List.of(expectedType.cast(bySchemaTree));
        }

        final var tmp = (RuntimeType[]) bySchemaTree;
        for (var item : tmp) {
            if (!expectedType.isInstance(item)) {
                throw new VerifyException("Unexpected schema tree child " + item);
            }
        }
        return (List<T>) Collections.unmodifiableList(Arrays.asList(tmp));
    }

    private static @NonNull QName extractQName(final RuntimeType type) {
        final var stmt = type.statement();
        if (stmt instanceof SchemaTreeEffectiveStatement<?> schemaTreeStmt) {
            return schemaTreeStmt.argument();
        }
        throw new VerifyException("Unexpected statement " + stmt + " in " + type);
    }
}