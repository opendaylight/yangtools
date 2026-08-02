/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.BaseNotification;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

/**
 * An {@link Archetype} for a {@link DataContainer}.
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public sealed interface DataContainerArchetype extends Archetype
        permits AugmentableArchetype, AugmentationArchetype, DataRootArchetype, GroupingArchetype,
                NotificationBodyArchetype, YangDataArchetype {
    /**
     * Base interface for builders resulting in an {@link DataContainerArchetype}.
     *
     * @since 16.0.0
     */
    @Beta
    sealed interface Builder extends Mutable permits DataContainerArchetypeBuilder {
        /**
         * Adds a new enclosed {@link Archetype} into definition of Generated Type.
         *
         * <br>There is no need of specifying of Package Name because enclosing Type is already defined inside Generated
         * Type with specific package name.
         *
         * <br>The name of enclosing Type cannot be same as Name of parent type and if there is already defined
         * enclosing type with the same name, the new enclosing type will simply overwrite the older definition.
         *
         * <br>If the parameter <code>genTOBuilder</code> of enclosing type is <code>null</code> the method SHOULD throw
         * {@link IllegalArgumentException}.
         *
         * @param genType the enclosed {@link Archetype}
         * @return this instance
         */
        Builder addEnclosedType(Archetype genType);

        /**
         * Add new Method Signature definition for Generated Type Builder and returns Method Signature Builder
         * for specifying all Method parameters.<br>
         * Name of Method cannot be <code>null</code>, if it is <code>null</code> the method SHOULD throw
         * {@link IllegalArgumentException}.<br>
         *
         * @param method the {@link MethodSignature}
         * @return this instance
         */
        Builder addMethod(MethodSignature method);

        /**
         * {@return a new immutable {@link DataContainerArchetype} instance}
         */
        DataContainerArchetype build();
    }

    /**
     * An {@link DataContainerArchetype} for a {@code notification} statement. Implementations of this archetype result
     * in a subclass of {@link BaseNotification} and the hierarchy of this class reflects that. These are not to be
     * confused with {@link NotificationBodyArchetype}.
     */
    sealed interface OfNotification extends AugmentableArchetype
            permits InstanceNotificationArchetype, KeyedListNotificationArchetype, NotificationArchetype {
        @Override
        NotificationEffectiveStatement statement();
    }

    /**
     * {@return the list of interfaces the interface extends}
     */
    // FIXME: this method should be replaced with sharper tools:
    //        - only allow GroupingArchetypes here
    //        - have CaseArchetype have a dedicated pointer to its inherited ChoiceArchetype
    //        everything else should be implied by the archetype itself
    List<Type> getImplements();

    /**
     * {@return the list of methods the interface defines}
     */
    // FIXME: yes, these result in methods being generated, but they are somewhat subtle, as they also imply constants
    //        for builders, etc. Most notably, KeyArchetype is presenting a subset of these defined in its corresponding
    //        EntryObjectArchetype
    List<MethodSignature> getMethodDefinitions();

    @Override
    String toString();
}
