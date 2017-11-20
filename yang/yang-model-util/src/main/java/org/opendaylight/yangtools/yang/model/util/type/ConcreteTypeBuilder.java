/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.annotations.Beta;
import com.google.common.base.Verify;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Builder of {@link TypeDefinition}s for use in leaf statements. While similar to {@link DerivedTypeBuilder}, this
 * builder does not support adding of unknown nodes and will return the base type if the type is not modified, hence
 * not preserving the schema path.
 *
 * @param <T> Resulting {@link TypeDefinition}
 */
@Beta
public abstract class ConcreteTypeBuilder<T extends TypeDefinition<T>, N> extends DerivedTypeBuilder<T, N> {
    ConcreteTypeBuilder(final T baseType, final SchemaPath path) {
        super(baseType, path);

        setStatus(baseType.getStatus());
        baseType.getDescription().ifPresent(this::setDescription);
        baseType.getReference().ifPresent(this::setReference);
    }

    /**
     * Build the resulting type.
     *
     * @return A new type instance
     */
    @Nonnull abstract T buildType();

    @Override
    public final T build() {
        final T base = getBaseType();
        if (Objects.equals(getDefaultValue(), base.getDefaultValue().orElse(null))
                && Objects.equals(getUnits(), base.getUnits().orElse(null))) {
            return base;
        }

        return Verify.verifyNotNull(buildType());
    }
}
