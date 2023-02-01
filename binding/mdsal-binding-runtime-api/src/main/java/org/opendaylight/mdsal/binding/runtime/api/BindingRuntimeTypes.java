/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * The result of BindingGenerator run. Contains mapping between Types and SchemaNodes.
 */
@Beta
public interface BindingRuntimeTypes extends EffectiveModelContextProvider, RuntimeTypeContainer, Immutable {

    Optional<IdentityRuntimeType> findIdentity(QName qname);

    Optional<RuntimeType> findSchema(JavaTypeName typeName);

    Optional<InputRuntimeType> findRpcInput(QName rpcName);

    Optional<OutputRuntimeType> findRpcOutput(QName rpcName);

    default @Nullable RuntimeType schemaTreeChild(final Absolute path) {
        final var it = path.getNodeIdentifiers().iterator();
        var tmp = schemaTreeChild(it.next());
        while (it.hasNext() && tmp instanceof RuntimeTypeContainer container) {
            tmp = container.schemaTreeChild(it.next());
        }
        return tmp;
    }

    /**
     * Lookup to all {@link CaseRuntimeType}s related to a {@link ChoiceRuntimeType}. This is important when dealing
     * with sharing incurred by Binding Spec's reuse of constructs defined in a {@code grouping}.
     *
     * <p>
     * As an example, consider {@link ChoiceRuntimeType} and {@link CaseRuntimeType} relationship to
     * {@link GeneratedType}s in the following model:
     * <pre>
     *   <code>
     *     grouping grp {
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
     * YANG view of what is valid in {@code /foo/foo/bar} differs from what is valid in {@code /bar/foo/bar}, but this
     * difference is not reflected in generated Java constructs. More notably, the two augments being in different
     * modules. Since {@code choice bar}'s is part of a reusable construct, {@code grouping one}, DataObjects' copy
     * builders can propagate them without translating them to the appropriate manifestation -- and they can do nothing
     * about that as they lack the complete view of the effective model.
     *
     * <p>
     * This method provides a bridge between a particular instantiation of a {@code choice} to {@link CaseRuntimeType}s
     * valid in all instantiations.
     *
     * @param choiceType A ChoiceRuntimeType
     * @return The set of {@link CaseRuntimeType}s known to this instance
     * @throws NullPointerException if {@code ChoiceRuntimeType} is null
     */
    @NonNull Set<CaseRuntimeType> allCaseChildren(ChoiceRuntimeType choiceType);
}
