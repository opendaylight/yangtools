/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;

/**
 * Default implementation of {@link Block.Builder}. Methods ending with a capital letter terminate the current line,
 * i.e. return the result of {@link #nl()}. Examples include {@link #oB()}, {@link #cB()}, {@link #eS()}.
 *
 * <p>When deciding on the shape of a method and its name, please consider it first and foremost its stringlu structure,
 * as that is the layer we operate on.
 *
 * <p>We can have some common Java language things coming in, but those should be placed here only on temporary basis
 * until they shape a separate interface for high-level access. Examples include {@code #gen(String)} family of methods.
 */
@NonNullByDefault
final class BlockBuilder extends AbstractJavaBlockBuilder<BlockBuilder> {
    private final GeneratedClass javaType;

    /**
     * Default constructor.
     */
    BlockBuilder(final GeneratedClass javaType) {
        this.javaType = requireNonNull(javaType);
    }

    @Override
    protected BlockBuilder self() {
        return this;
    }

    /**
     * Append the contents of a {@link BlockBuilder} to this instance if it is not {@code null}.
     *
     * @param source optional {@link BlockBuilder}
     * @return this instance
     */
    BlockBuilder blk(final Block.@Nullable Builder source) {
        verifyEmptyLine();
        if (source != null) {
            final var blk = source.toBlock();
            if (blk != null) {
                blk.appendTo(this);
            }
        }
        return this;
    }

    /**
     * Append a reference to the specified Java type.
     *
     * @param type the type
     * @return this instance
     */
    BlockBuilder jRef(final TypeName type) {
        return str(javaType.getReferenceString(type));
    }

    /**
     * Append a reference to the specified Java type.
     *
     * @param type the type
     * @return this instance
     */
    BlockBuilder jRef(final Archetype type) {
        return jRef(type.name());
    }

    /**
     * Append a reference to the specified Java type.
     *
     * @param type the type
     * @return this instance
     */
    BlockBuilder jRef(final ConcreteType type) {
        return jRef(type.name());
    }

    // FIXME: split this out into JavadocBuilder
    String toJavadocBlock() {
        if (isEmpty())  {
            return "";
        }
        final var bb = BaseTemplate.wrapToDocumentation(toRawString());
        return bb == null ? "" : bb.toRawString();
    }
}
