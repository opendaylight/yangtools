/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.type.builder.TypeBuilder;

@NonNullByDefault
public abstract sealed class AbstractTypeBuilder implements TypeBuilder
        permits AbstractEnumerationBuilder, AbstractGeneratedTypeBuilder, AnnotationTypeBuilderImpl {
    private final JavaTypeName typeName;

    AbstractTypeBuilder(final JavaTypeName typeName) {
        this.typeName = requireNonNull(typeName);
    }

    @Override
    public final JavaTypeName typeName() {
        return typeName;
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitEmptyValues().omitNullValues()).toString();
    }

    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        return toStringHelper.add("typeName", typeName);
    }
}
