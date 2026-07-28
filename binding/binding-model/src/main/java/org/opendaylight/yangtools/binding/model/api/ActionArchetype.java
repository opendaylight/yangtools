/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Action;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;

/**
 * The {@link InterfaceArchetype} for {@link Action} specializations.
 *
 * @since 16.0.0
 */
@Beta
// FIXME: do not extend InterfaceArchetype, just like RpcArchetype does not
public sealed interface ActionArchetype extends InterfaceArchetype permits ActionArchetypeImpl {
    /**
     * A builder of {@link ActionArchetype}s.
     */
    @NonNullByDefault
    final class Builder extends InterfaceArchetypeBuilder<Builder, ActionEffectiveStatement> {
        private Builder(final JavaTypeName typeName, final ActionEffectiveStatement statement, final Archetype input,
                final Archetype output, final JavaTypeName parentName) {
            super(typeName, statement);

            // FIXME: this is something ActionTemplate should be doing
            final var parentType = TypeRef.of(parentName);
            addAnnotation(FunctionalInterfaceAnnotation.INSTANCE);
            addImplementsType(BindingTypes.action(parentType, input, output));
            addMethod(Naming.ACTION_INVOKE_NAME)
                .addAnnotation(OverrideAnnotation.INSTANCE)
                .addParameter(BindingTypes.objectIdentifier(parentType), "path")
                .addParameter(input, "input")
                .setReturnType(Types.listenableFutureTypeFor(BindingTypes.rpcResult(output)));
        }

        @Override
        public ActionArchetype build() {
            return new ActionArchetypeImpl(typeName, statement, annotations(), implementsTypes(), constants(),
                methodDefinitions(), enclosedTypes());
        }

        @Override
        Class<ActionArchetype> archetypeClass() {
            return ActionArchetype.class;
        }

        @Override
        Builder thisInstance() {
            return this;
        }
    }

    @NonNullByDefault
    static Builder builder(final JavaTypeName typeName, final ActionEffectiveStatement statement, final Archetype input,
            final Archetype output, final JavaTypeName parentName) {
        return new Builder(typeName, statement, input, output, parentName);
    }

    @Override
    ActionEffectiveStatement statement();
}
