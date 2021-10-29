/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.rt;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.GeneratedRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;

abstract class AbstractCompositeRuntimeType<S extends EffectiveStatement<?, ?>>
        extends AbstractRuntimeType<S, GeneratedType> implements CompositeRuntimeType {
    private final ImmutableMap<JavaTypeName, GeneratedRuntimeType> byClass;
    private final ImmutableMap<QName, RuntimeType> bySchemaTree;
    private final @NonNull ImmutableList<AugmentRuntimeType> augments;
    private final @NonNull ImmutableList<AugmentRuntimeType> mismatchedAugments;

    AbstractCompositeRuntimeType(final GeneratedType bindingType, final S statement, final List<RuntimeType> children,
            final List<AugmentRuntimeType> augments) {
        super(bindingType, statement);

        final var substatements = statement.effectiveSubstatements();
        final var correctBuilder = ImmutableList.<AugmentRuntimeType>builder();
        final var mismatchedBuilder = ImmutableList.<AugmentRuntimeType>builder();
        for (var aug : augments) {
            if (substatements.contains(aug.statement())) {
                correctBuilder.add(aug);
            } else {
                mismatchedBuilder.add(aug);
            }
        }
        this.augments = correctBuilder.build();
        this.mismatchedAugments = mismatchedBuilder.build();

        byClass = children.stream()
            .filter(GeneratedRuntimeType.class::isInstance)
            .map(GeneratedRuntimeType.class::cast)
            .collect(ImmutableMap.toImmutableMap(GeneratedRuntimeType::getIdentifier, Functions.identity()));

        // Note: this may be over-sized, but we typically deal with schema tree statements, hence it is kind of accurate
        final var builder = ImmutableMap.<QName, RuntimeType>builderWithExpectedSize(children.size());
        for (var child : children) {
            final var stmt = child.statement();
            if (stmt instanceof SchemaTreeEffectiveStatement) {
                builder.put(((SchemaTreeEffectiveStatement<?>)stmt).argument(), child);
            }
        }
        bySchemaTree = builder.build();
    }

    @Override
    public final List<AugmentRuntimeType> augments() {
        return augments;
    }

    @Override
    public final List<AugmentRuntimeType> mismatchedAugments() {
        return mismatchedAugments;
    }

    @Override
    public final RuntimeType schemaTreeChild(final QName qname) {
        return bySchemaTree.get(requireNonNull(qname));
    }

    @Override
    public final GeneratedRuntimeType bindingChild(final JavaTypeName typeName) {
        return byClass.get(requireNonNull(typeName));
    }

    final @NonNull ImmutableCollection<RuntimeType> schemaTreeChildren() {
        return bySchemaTree.values();
    }
}
