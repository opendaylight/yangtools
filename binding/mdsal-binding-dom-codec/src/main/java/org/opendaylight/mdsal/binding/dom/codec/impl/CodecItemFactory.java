/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.model.api.AddedByUsesAware;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

sealed class CodecItemFactory {
    private static final class Case extends CodecItemFactory {
        private final Class<?> bindingClass;

        Case(final Class<?> bindingClass) {
            this.bindingClass = requireNonNull(bindingClass);
        }

        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Item<?> createItem(final Class<?> childClass, final EffectiveStatement<?, ?> childSchema) {
            // FIXME: MDSAL-697: see overridden method for further guidance
            return childSchema instanceof AddedByUsesAware aware && aware.isAddedByUses()
                ? Item.of((Class) bindingClass, (Class) childClass) : super.createItem(childClass, childSchema);
        }
    }

    private static final @NonNull CodecItemFactory DEFAULT = new CodecItemFactory();

    private CodecItemFactory() {
        // Hidden on purpose
    }

    // FIXME: MDSAL-697: move this method into BindingRuntimeContext
    //        This method is only called from loadChildPrototype() and exists only to be overridden by
    //        CaseNodeCodecContext. Since we are providing childClass and our schema to BindingRuntimeContext and
    //        receiving childSchema from it via findChildSchemaDefinition, we should be able to receive the equivalent
    //        of Map.Entry<Item, DataSchemaNode>, along with the override we create here. One more input we may need to
    //        provide is our bindingClass().
    @SuppressWarnings("unchecked")
    Item<?> createItem(final Class<?> childClass, final EffectiveStatement<?, ?> childSchema) {
        return Item.of((Class<? extends DataObject>) childClass);
    }

    static @NonNull CodecItemFactory of() {
        return DEFAULT;
    }

    static @NonNull CodecItemFactory of(final Class<?> bindingClass) {
        return new Case(bindingClass);
    }
}
