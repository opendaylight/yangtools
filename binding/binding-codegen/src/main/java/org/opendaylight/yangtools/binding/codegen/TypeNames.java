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
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Well-known {@link JavaTypeName} constants which do not have a single user.
 */
@NonNullByDefault
final class TypeNames {
    /**
     * {@code java.lang.Class} as a JavaTypeName.
     */
    static final JavaTypeName CLASS = JavaTypeName.create(Class.class);
    /**
     * {@code java.lang.Deprecated} as a JavaTypeName.
     */
    static final JavaTypeName DEPRECATED = JavaTypeName.create(Deprecated.class);
    /**
     * {@code java.lang.IllegalArgumentException} as a JavaTypeName.
     */
    static final JavaTypeName IAE = JavaTypeName.create(IllegalArgumentException.class);
    /**
     * {@code java.lang.NullPointerException} as a JavaTypeName.
     */
    static final JavaTypeName NPE = JavaTypeName.create(NullPointerException.class);
    /**
     * {@code java.lang.NoSuchElementException} as a JavaTypeName.
     */
    static final JavaTypeName NSEE = JavaTypeName.create(NoSuchElementException.class);
    /**
     * {@code java.lang.Object} as a JavaTypeName.
     */
    static final JavaTypeName OBJECT = JavaTypeName.create(Object.class);
    /**
     * {@code java.lang.String} as a JavaTypeName.
     */
    static final JavaTypeName STRING = JavaTypeName.create(String.class);
    /**
     * {@code java.lang.Override} as a JavaTypeName.
     */
    static final JavaTypeName OVERRIDE = JavaTypeName.create(Override.class);
    /**
     * {@code java.lang.SuppressWarnings} as a JavaTypeName.
     */
    static final JavaTypeName SUPPRESS_WARNINGS = JavaTypeName.create(SuppressWarnings.class);
    /**
     * {@code java.lang.void} as a JavaTypeName.
     */
    static final JavaTypeName VOID = JavaTypeName.create(void.class);

    /**
     * {@code java.util.Arrays} as a JavaTypeName.
     */
    static final JavaTypeName JU_ARRAYS = JavaTypeName.create(Arrays.class);
    /**
     * {@code java.util.HashMap} as a JavaTypeName.
     */
    static final JavaTypeName JU_BASE64 = JavaTypeName.create(Base64.class);
    /**
     * {@code java.util.HashMap} as a JavaTypeName.
     */
    static final JavaTypeName JU_HASHMAP = JavaTypeName.create(HashMap.class);
    /**
     * {@code java.util.List} as a JavaTypeName.
     */
    static final JavaTypeName JU_LIST = JavaTypeName.create(List.class);
    /**
     * {@code java.util.Map} as a JavaTypeName.
     */
    static final JavaTypeName JU_MAP = JavaTypeName.create(Map.class);
    /**
     * {@code java.util.Objects} as a JavaTypeName.
     */
    static final JavaTypeName JU_OBJECTS = JavaTypeName.create(Objects.class);
    /**
     * {@code java.util.regex.Pattern} as a JavaTypeName.
     */
    static final JavaTypeName JUR_PATTERN = JavaTypeName.create(Pattern.class);

    /**
     * {@code javax.annotation.processing.Generated} as a JavaTypeName.
     */
    static final JavaTypeName GENERATED = JavaTypeName.create(Generated.class);

    /**
     * {@code javax.management.ConstructorParameters} as a JavaTypeName.
     */
    static final JavaTypeName CONSTRUCTOR_PARAMETERS = JavaTypeName.create(ConstructorParameters.class);

    /**
     * {@code org.eclipse.jdt.annotation.NonNull} as a JavaTypeName.
     */
    static final JavaTypeName NONNULL = JavaTypeName.create(NonNull.class);
    /**
     * {@code org.eclipse.jdt.annotation.NonNullByDefault} as a JavaTypeName.
     */
    static final JavaTypeName NONNULL_BY_DEFAULT = JavaTypeName.create(NonNullByDefault.class);
    /**
     * {@code org.eclipse.jdt.annotation.Nullable} as a JavaTypeName.
     */
    static final JavaTypeName NULLABLE = JavaTypeName.create(Nullable.class);

    /**
     * {@code com.google.common.util.concurrent.ListenableFuture} as a JavaTypeName.
     */
    static final JavaTypeName LISTENABLE_FUTURE = JavaTypeName.create(ListenableFuture.class);

    /**
     * {@code org.opendaylight.yangtools.binding.lib.CodeHelpers} as a JavaTypeName.
     */
    static final JavaTypeName CODEHELPERS = JavaTypeName.create(CodeHelpers.class);

    /**
     * {@code org.opendaylight.yangtools.yang.common.RpcResult} as a JavaTypeName.
     */
    static final JavaTypeName RPC_RESULT = JavaTypeName.create(RpcResult.class);

    private TypeNames() {
        // hidden on purpose
    }
}
