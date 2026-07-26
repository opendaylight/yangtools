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

/**
 * An {@link Archetype} which results in an interface with zero or more methods.
 *
 * @since 16.0.0
 */
// TODO: a better name perhaps?
@Beta
public sealed interface InterfaceArchetype extends Archetype permits DataRootArchetype, LegacyArchetype {
    /**
     * {@return the list of annotations attached to interface declaration}
     */
    // FIXME: all type annotations should be implied by specialization and this method should not exist
    @NonNullByDefault
    List<AttachedAnnotation.ToType> annotations();

    /**
     * {@return the list of interfaces the interface extends}
     */
    // FIXME: this method should be replaced with sharper tools:
    //        - only allow GroupingArchetypes here
    //        - have CaseArchetype have a dedicated pointer to its inherited ChoiceArchetype
    //        everything else should be implied by the archetype itself
    @NonNullByDefault
    List<Type> getImplements();

    /**
     * {@return the list of constants the interface defines}
     */
    // FIXME: all constants should be implied by a particular archetype and this method should not exist
    @NonNullByDefault
    List<Constant> getConstantDefinitions();

    /**
     * {@return the list of methods the interface defines}
     */
    // FIXME: yes, these result in methods being generated, but they are somewhat subtle, as they also imply constants
    //        for builders, etc. Most notably, KeyArchetype is presenting a subset of these defined in its corresponding
    //        LegacyArchetype (or EntryObjectArchetype once that is created)
    @NonNullByDefault
    List<MethodSignature> getMethodDefinitions();
}
