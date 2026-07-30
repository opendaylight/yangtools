/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri;

import static org.opendaylight.yangtools.binding.model.ri.Types.typeForBuiltIn;
import static org.opendaylight.yangtools.binding.model.ri.Types.typeForClass;

import com.google.common.annotations.VisibleForTesting;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.InstanceNotification;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyedListNotification;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.binding.NotificationBody;
import org.opendaylight.yangtools.binding.contract.BuiltInType;
import org.opendaylight.yangtools.binding.lib.JavaDataContainer;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;

public final class BindingTypes {

    public static final @NonNull ConcreteType BASE_IDENTITY = typeForBuiltIn(BuiltInType.IDENTITYREF);
    public static final @NonNull ConcreteType DATA_CONTAINER = typeForClass(DataContainer.class);
    public static final @NonNull ConcreteType DATA_OBJECT = typeForClass(DataObject.class);
    public static final @NonNull ConcreteType UNION_TYPE_OBJECT = typeForBuiltIn(BuiltInType.UNION);
    public static final @NonNull ConcreteType JAVA_DATACONTAINER = typeForClass(JavaDataContainer.class);

    @VisibleForTesting
    static final @NonNull ConcreteType AUGMENTABLE = typeForClass(Augmentable.class);
    @VisibleForTesting
    static final @NonNull ConcreteType AUGMENTATION = typeForClass(Augmentation.class);
    @VisibleForTesting
    static final @NonNull ConcreteType ENTRY_OBJECT = typeForClass(EntryObject.class);
    @VisibleForTesting
    static final @NonNull ConcreteType KEY = typeForClass(Key.class);

    private static final @NonNull ConcreteType CHILD_OF = typeForClass(ChildOf.class);
    private static final @NonNull ConcreteType INSTANCE_NOTIFICATION = typeForClass(InstanceNotification.class);
    private static final @NonNull ConcreteType KEYED_LIST_NOTIFICATION = typeForClass(KeyedListNotification.class);
    private static final @NonNull ConcreteType NOTIFICATION = typeForClass(Notification.class);
    private static final @NonNull ConcreteType NOTIFICATION_BODY = typeForClass(NotificationBody.class);

    private BindingTypes() {
        //  Hidden on purpose
    }

    /**
     * Type specializing {@link Notification} for a particular type.
     *
     * @param concreteType The concrete type of this notification
     * @return A parameterized type corresponding to {@code Notification<ConcreteType>}
     * @throws NullPointerException if any argument is {@code null}
     */
    @NonNullByDefault
    public static ParameterizedType notification(final Type concreteType) {
        return ParameterizedType.of(NOTIFICATION, concreteType);
    }

    /**
     * Type specializing {@link NotificationBody} for a particular type.
     *
     * @param concreteType The concrete type of this notification
     * @return A parameterized type corresponding to {@code NotificationBody<ConcreteType>}
     * @throws NullPointerException if {@code parent} is {@code null}
     */
    @NonNullByDefault
    public static ParameterizedType notificationBody(final Type concreteType) {
        return ParameterizedType.of(NOTIFICATION_BODY, concreteType);
    }

    /**
     * Type specializing {@link InstanceNotification} for a particular type.
     *
     * @param concreteType The concrete type of this notification
     * @param parent Type of parent defining the notification
     * @return A parameterized type corresponding to {@code InstanceNotification<ConcreteType, Parent>}
     * @throws NullPointerException if {@code parent} is {@code null}
     */
    @NonNullByDefault
    public static ParameterizedType instanceNotification(final Type concreteType, final Type parent) {
        return ParameterizedType.of(INSTANCE_NOTIFICATION, concreteType, parent);
    }

    /**
     * Type specializing {@link InstanceNotification} for a particular type.
     *
     * @param concreteType The concrete type of this notification
     * @param parent Type of parent defining the notification
     * @param keyType Type of parent's key
     * @return A parameterized type corresponding to {@code KeyedInstanceNotification<ConcreteType, ParentKey, Parent>}
     * @throws NullPointerException if any argument is {@code null}
     */
    @NonNullByDefault
    public static ParameterizedType keyedListNotification(final Type concreteType, final Type parent,
            final KeyArchetype keyType) {
        return ParameterizedType.of(KEYED_LIST_NOTIFICATION, concreteType, parent, keyType);
    }

    /**
     * Specialize {@link Augmentable} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code Augmentable<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    @NonNullByDefault
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
    @NonNullByDefault
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
    @NonNullByDefault
    public static ParameterizedType childOf(final Type type) {
        return ParameterizedType.of(CHILD_OF, type);
    }

    /**
     * Type specializing {@link Key} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code Key<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    @NonNullByDefault
    public static ParameterizedType key(final Type type) {
        return ParameterizedType.of(KEY, type);
    }

    /**
     * {@return a parameterized type corresponding to {@code EntryObject<Type, KeyType>}}
     * @param type Type for which to specialize
     * @param keyType the corresponding {@link #key(Type)}
     * @throws NullPointerException if any argument is {@code null}
     */
    @NonNullByDefault
    public static ParameterizedType entryObject(final Type type, final KeyArchetype keyType) {
        return ParameterizedType.of(ENTRY_OBJECT, type, keyType);
    }

    /**
     * Return the {@link Augmentable} type a parameterized {@link Augmentable} type references.
     *
     * @param type Parameterized type
     * @return Augmentable target, or null if {@code type} does not match the result of {@link #augmentable(Type)} or
     *         {@link #entryObject(Type, KeyArchetype)}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static @Nullable Type extractAugmentableTarget(final @NonNull ParameterizedType type) {
        final var rawType = type.getRawType();
        if (AUGMENTABLE.equals(rawType)) {
            return onlyTypeArgument(type);
        }
        if (ENTRY_OBJECT.equals(rawType)) {
            final var args = type.getActualTypeArguments();
            if (args.size() == 2) {
                final var arg = args.getFirst();
                if (arg != null) {
                    return arg;
                }
            }
        }
        return null;
    }

    private static @Nullable Type onlyTypeArgument(final @NonNull ParameterizedType type) {
        final var args = type.getActualTypeArguments();
        if (args.size() == 1) {
            final var arg = args.getFirst();
            if (arg != null) {
                return arg;
            }
        }
        return null;
    }
}
