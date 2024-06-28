/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;

/**
 * Marker interface for codecs, which functionality will not be affected by schema change (introduction of new YANG
 * modules) they may have one static instance generated when first time needed.
 */
// FIXME: IllegalArgumentCodec is perhaps not appropriate here due to null behavior
abstract class SchemaUnawareCodec extends AbstractValueCodec<Object, Object> {
    static @Nullable SchemaUnawareCodec of(final Class<?> typeClz, final TypeDefinition<?> def) {
        return BindingReflections.isBindingClass(typeClz) ? getCachedSchemaUnawareCodec(typeClz, def) : null;
    }

    private static @NonNull SchemaUnawareCodec getCachedSchemaUnawareCodec(final Class<?> typeClz,
            final TypeDefinition<?> def) {
        // FIXME: extract this only when really needed
        var rootType = requireNonNull(def);
        while (true) {
            final var base = rootType.getBaseType();
            if (base != null) {
                rootType = base;
            } else {
                break;
            }
        }

        try {
            if (rootType instanceof EnumTypeDefinition enumType) {
                return EnumerationCodec.of(typeClz, enumType);
            } else if (rootType instanceof BitsTypeDefinition bitsType) {
                return BitsCodec.of(typeClz, bitsType);
            } else {
                return EncapsulatedValueCodec.of(typeClz);
            }
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }
}
