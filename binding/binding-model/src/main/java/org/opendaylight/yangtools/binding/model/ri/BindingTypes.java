/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri;

import static org.opendaylight.yangtools.binding.model.ri.Types.cachedType;

import com.google.common.annotations.VisibleForTesting;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.lib.JavaDataContainer;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;

@NonNullByDefault
public final class BindingTypes {

    public static final ConcreteType DATA_OBJECT = cachedType(DataObject.class);
    public static final ConcreteType JAVA_DATACONTAINER = cachedType(JavaDataContainer.class);

    @VisibleForTesting
    static final ConcreteType AUGMENTABLE = cachedType(Augmentable.class);
    @VisibleForTesting
    static final ConcreteType AUGMENTATION = cachedType(Augmentation.class);
    @VisibleForTesting
    static final ConcreteType ENTRY_OBJECT = cachedType(EntryObject.class);

    private static final ConcreteType CHILD_OF = cachedType(ChildOf.class);

    private BindingTypes() {
        //  Hidden on purpose
    }

    /**
     * Specialize {@link Augmentable} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code Augmentable<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static ParameterizedType augmentable(final Type type) {
        return ParameterizedType.of(AUGMENTABLE, type);
    }

    /**
     * Specialize {@link Augmentation} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code Augmentation<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static ParameterizedType augmentation(final Type type) {
        return ParameterizedType.of(AUGMENTATION, type);
    }

    /**
     * Specialize {@link ChildOf} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code ChildOf<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static ParameterizedType childOf(final Type type) {
        return ParameterizedType.of(CHILD_OF, type);
    }

    /**
     * {@return a parameterized type corresponding to {@code EntryObject<Type, KeyType>}}
     * @param type Type for which to specialize
     * @param keyType the corresponding {@link KeyArchetype}
     * @throws NullPointerException if any argument is {@code null}
     */
    public static ParameterizedType entryObject(final Type type, final KeyArchetype keyType) {
        return ParameterizedType.of(ENTRY_OBJECT, type, keyType);
    }
}
