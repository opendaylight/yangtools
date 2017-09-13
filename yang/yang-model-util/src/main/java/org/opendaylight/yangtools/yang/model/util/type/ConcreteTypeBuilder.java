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
 * Builder of {@link TypeDefinitions} for use in leaf statements. While similar to {@link DerivedTypeBuilder}, this
 * builder does not support adding of unknown nodes and will return the base type if the type is not modified, hence
 * not preserving the schema path.
 *
 * @param <T> Resulting {@link TypeDefinition}
 */
@Beta
public abstract class ConcreteTypeBuilder<T extends TypeDefinition<T>> extends DerivedTypeBuilder<T> {
    ConcreteTypeBuilder(final T baseType, final SchemaPath path) {
        super(baseType, path);

        if (baseType.getDescription() != null) {
            setDescription(baseType.getDescription());
        }
        if (baseType.getReference() != null) {
            setReference(baseType.getReference());
        }
        if (baseType.getStatus() != null) {
            setStatus(baseType.getStatus());
        }
    }

    /**
     * Build the resulting type.
     *
     * @return A new type instance
     */
    @Nonnull abstract T buildType();

    @Override
    public T build() {
        final T base = getBaseType();
        if (Objects.equals(getDefaultValue(), base.getDefaultValue()) && Objects.equals(getUnits(), base.getUnits())) {
            return base;
        }

        return Verify.verifyNotNull(buildType());
    }
}
