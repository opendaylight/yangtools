/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;

/**
 * A {@link RuntimeType} associated with a {@code choice} statement.
 */
@Beta
public interface ChoiceRuntimeType extends CompositeRuntimeType, DataRuntimeType {
    @Override
    ChoiceEffectiveStatement statement();

    /**
     * Returns resolved {@link CaseRuntimeType} for specified binding class name.
     *
     * @param typeName Binding class name
     * @return {@link CaseRuntimeType}, or null if absent
     * @throws NullPointerException if {@code typeName} is null
     */
    @Nullable CaseRuntimeType bindingCaseChild(JavaTypeName typeName);

    /**
     * Return the runtime type for the original manifestation of this type's {@code choice} statement.
     * Returns {@code null} if this type is the original.
     *
     * @return Original manifestatation, or {@code null} if this is the original manifestation.
     */
    @Nullable ChoiceRuntimeType originalType();

    /**
     * Return all {@link CaseRuntimeType} valid at this type's statement.
     *
     * @return Valid {@link CaseRuntimeType}s
     */
    @NonNull Collection<CaseRuntimeType> validCaseChildren();

    /**
     * Return any additional {@link CaseRuntimeType}s which may be encountered when dealing with DataObjects supported
     * by this type. These are not strictly valid in YANG view of modeled data, but may have potentially-equivalent
     * representation, such as in the following case:
     * <pre>
     *   <code>
     *     grouping one {
     *       container foo {
     *         choice bar;
     *       }
     *     }
     *
     *     container foo {
     *       uses grp;
     *     }
     *
     *     container bar {
     *       uses grp;
     *     }
     *
     *     augment /foo/foo/bar {
     *       case baz
     *     }
     *
     *     augment /bar/foo/bar {
     *       case xyzzy;
     *     }
     *   </code>
     * </pre>
     * and, more notably, the two augments being in different modules. Since {@code choice bar}'s is part of a reusable
     * construct, {@code grouping one}, DataObjects' copy builders can propagate them without translating them to the
     * appropriate manifestation -- and they can do nothing about that as they lack the complete view of the effecitve
     * model.
     *
     * @return Additional {@link CaseRuntimeType}s
     */
    @NonNull Collection<CaseRuntimeType> additionalCaseChildren();
}
