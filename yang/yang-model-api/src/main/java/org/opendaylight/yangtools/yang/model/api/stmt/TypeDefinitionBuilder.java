/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Collection;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A {@link Builder} for {@link TypeDefinition} instances based on {@link EffectiveStatement}
 * instances.
 *
 * @param <T> Subclass of TypeDefinition this builder produces.
 */
@Beta
public interface TypeDefinitionBuilder<T extends TypeDefinition<T>> extends Builder<T> {
    /**
     * Set the schema path of the resulting type, as specified by {@link TypeDefinition#getPath()}. This method may
     * be called multiple times.
     *
     * @param path Resulting type's schema path
     * @return This builder, for fluent-style use.
     * @throws NullPointerException if the argument is null
     */
    TypeDefinitionBuilder<T> setPath(@Nonnull SchemaPath path);

    /**
     * Set the base type (supertype) of the resulting type, as specified by {@link TypeDefinition#getBaseType()}. This
     * method may be called multiple times.
     *
     * @param baseType Resulting type's base type
     * @return This builder, for fluent-style use.
     * @throws NullPointerException if the argument is null
     */
    TypeDefinitionBuilder<T> setBaseType(@Nonnull TypeDefinition<?> baseType);

    /**
     * Add effective statements to the resulting type's definition. This method may be called multiple times, each
     * successive call overriding previous statements. Implementations of this method should check if the type
     * definition as implied by previous calls can be redefined with newly-provided statements. In case they do not,
     * it is left up to the implementation whether it ignores the new statement, uses the new statement or if it throws
     * an exception. Statements not affecting the type definitions are silently ignored.
     *
     * @param statements Statements to be applied to the resulting type's definition.
     * @return This builder, for fluent-style use.
     * @throws NullPointerException if the collection or any of its items is null
     * @throws IllegalArgumentException if one of the statements cannot be applied
     */
    TypeDefinitionBuilder<T> addEffectiveStatements(@Nonnull Collection<? extends EffectiveStatement<?, ?>> statements);
}
