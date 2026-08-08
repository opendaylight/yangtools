/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.annotation.processing.Generated;
import javax.management.ConstructorParameters;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.lib.CodeHelpers;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Well-known {@link TypeName} constants which do not have a single user.
 */
@NonNullByDefault
final class TypeNames {
    /**
     * {@code java.lang.Class} as a TypeName.
     */
    static final TypeName CLASS = TypeName.ofClass(Class.class);
    /**
     * {@code java.lang.IllegalArgumentException} as a TypeName.
     */
    static final TypeName IAE = TypeName.ofClass(IllegalArgumentException.class);
    /**
     * {@code java.lang.NullPointerException} as a TypeName.
     */
    static final TypeName NPE = TypeName.ofClass(NullPointerException.class);
    /**
     * {@code java.lang.NoSuchElementException} as a TypeName.
     */
    static final TypeName NSEE = TypeName.ofClass(NoSuchElementException.class);
    /**
     * {@code java.lang.Object} as a TypeName.
     */
    static final TypeName OBJECT = TypeName.ofClass(Object.class);
    /**
     * {@code java.lang.String} as a TypeName.
     */
    static final TypeName STRING = TypeName.ofClass(String.class);
    /**
     * {@code java.lang.Override} as a TypeName.
     */
    static final TypeName OVERRIDE = TypeName.ofClass(Override.class);
    /**
     * {@code java.lang.SuppressWarnings} as a TypeName.
     */
    static final TypeName SUPPRESS_WARNINGS = TypeName.ofClass(SuppressWarnings.class);
    /**
     * {@code java.lang.void} as a TypeName.
     */
    static final TypeName VOID = TypeName.ofClass(void.class);

    /**
     * {@code java.util.Arrays} as a TypeName.
     */
    static final TypeName JU_ARRAYS = TypeName.ofClass(Arrays.class);
    /**
     * {@code java.util.HashMap} as a TypeName.
     */
    static final TypeName JU_BASE64 = TypeName.ofClass(Base64.class);
    /**
     * {@code java.util.HashMap} as a TypeName.
     */
    static final TypeName JU_HASHMAP = TypeName.ofClass(HashMap.class);
    /**
     * {@code java.util.List} as a TypeName.
     */
    static final TypeName JU_LIST = TypeName.ofClass(List.class);
    /**
     * {@code java.util.Map} as a TypeName.
     */
    static final TypeName JU_MAP = TypeName.ofClass(Map.class);
    /**
     * {@code java.util.Objects} as a TypeName.
     */
    static final TypeName JU_OBJECTS = TypeName.ofClass(Objects.class);
    /**
     * {@code java.util.regex.Pattern} as a TypeName.
     */
    static final TypeName JUR_PATTERN = TypeName.ofClass(Pattern.class);

    /**
     * {@code javax.annotation.processing.Generated} as a TypeName.
     */
    static final TypeName GENERATED = TypeName.ofClass(Generated.class);

    /**
     * {@code javax.management.ConstructorParameters} as a TypeName.
     */
    static final TypeName CONSTRUCTOR_PARAMETERS = TypeName.ofClass(ConstructorParameters.class);

    /**
     * {@code org.eclipse.jdt.annotation.NonNull} as a TypeName.
     */
    static final TypeName NONNULL = TypeName.ofClass(NonNull.class);
    /**
     * {@code org.eclipse.jdt.annotation.NonNullByDefault} as a TypeName.
     */
    static final TypeName NONNULL_BY_DEFAULT = TypeName.ofClass(NonNullByDefault.class);
    /**
     * {@code org.eclipse.jdt.annotation.Nullable} as a TypeName.
     */
    static final TypeName NULLABLE = TypeName.ofClass(Nullable.class);

    /**
     * {@code com.google.common.util.concurrent.ListenableFuture} as a TypeName.
     */
    static final TypeName LISTENABLE_FUTURE = TypeName.ofClass(ListenableFuture.class);

    /**
     * {@code org.opendaylight.yangtools.binding.lib.CodeHelpers} as a TypeName.
     */
    static final TypeName CODEHELPERS = TypeName.ofClass(CodeHelpers.class);

    /**
     * {@code org.opendaylight.yangtools.yang.common.RpcResult} as a TypeName.
     */
    static final TypeName RPC_RESULT = TypeName.ofClass(RpcResult.class);

    private TypeNames() {
        // hidden on purpose
    }
}
