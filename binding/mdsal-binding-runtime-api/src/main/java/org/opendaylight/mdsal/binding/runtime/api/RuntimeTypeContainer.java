/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * An object containing {@link RuntimeType}s.
 */
@Beta
public interface RuntimeTypeContainer extends Immutable {
    /**
     * Look up a child {@link RuntimeType} by its {@code schema tree} {@link QName}. Note the returned child does not
     * necessarily match usual data addressing rules and will resolve non-data tree statements, such as
     * {@code notification}, {@code rpc} and {@code action}. Callers should check the traits exposed by the returned
     * object before accepting it.
     *
     * <p>
     * One important omission is this method <b>does not</b> resolve nodes which have been added via {@code augment}
     * statement. Those are exposed indirectly as children of {@link AugmentRuntimeType}s returned via
     * {@link AugmentableRuntimeType#augments()}, if applicable.
     *
     * @param qname {@code schema node} identifier
     * @return Corresponding {@link RuntimeType}, or null if not found
     * @throws NullPointerException if {@code qname} is null
     */
    @Nullable RuntimeType schemaTreeChild(QName qname);

    /**
     * Look up a child by the {@link JavaTypeName} of its generated class. This lookup, while very similar to
     * {@link #schemaTreeChild(QName)}, does not precisely match the {@code schema tree} nor does it match
     * {@code YangInstanceIdentifier} addressing.
     * It will resolve constructs generated for {@code choice}, {@code case}, {@code notification}, {@code rpc},
     * {@code action} and most notably for {@code grouping}, {@code identity}, {@code typedef} and {@code augment}
     * statements. Callers should check the traits exposed by the returned object before accepting it.
     *
     * @param typeName Java class name of the generated class.
     * @return Corresponding {@link RuntimeType}, or null if not found
     * @throws NullPointerException if {@code typeName} is null
     */
    @Nullable GeneratedRuntimeType bindingChild(JavaTypeName typeName);

    // FIXME: consider removing this method
    default @Nullable RuntimeType bindingChild(final Type type) {
        return bindingChild(type.getIdentifier());
    }
}
