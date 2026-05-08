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
import com.google.common.base.VerifyException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Action;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Grouping;
import org.opendaylight.yangtools.binding.InstanceNotification;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyedListAction;
import org.opendaylight.yangtools.binding.KeyedListNotification;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.binding.NotificationBody;
import org.opendaylight.yangtools.binding.Rpc;
import org.opendaylight.yangtools.binding.RpcInput;
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.YangData;
import org.opendaylight.yangtools.binding.annotations.RoutingContext;
import org.opendaylight.yangtools.binding.contract.BuiltInType;
import org.opendaylight.yangtools.binding.lib.JavaDataContainer;
import org.opendaylight.yangtools.binding.meta.RootMeta;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.YangDataName;

public final class BindingTypes {

    public static final @NonNull ConcreteType BASE_IDENTITY = typeForBuiltIn(BuiltInType.IDENTITYREF);
    public static final @NonNull ConcreteType DATA_CONTAINER = typeForClass(DataContainer.class);
    public static final @NonNull ConcreteType DATA_OBJECT = typeForClass(DataObject.class);
    public static final @NonNull ConcreteType GROUPING = typeForClass(Grouping.class);
    public static final @NonNull ConcreteType QNAME = typeForClass(QName.class);
    public static final @NonNull ConcreteType RPC_INPUT = typeForClass(RpcInput.class);
    public static final @NonNull ConcreteType RPC_OUTPUT = typeForClass(RpcOutput.class);
    public static final @NonNull ConcreteType SCALAR_TYPE_OBJECT = typeForClass(ScalarTypeObject.class);
    public static final @NonNull ConcreteType BITS_TYPE_OBJECT = typeForBuiltIn(BuiltInType.BITS);
    public static final @NonNull ConcreteType UNION_TYPE_OBJECT = typeForBuiltIn(BuiltInType.UNION);
    public static final @NonNull ConcreteType YANG_DATA_NAME = typeForClass(YangDataName.class);
    public static final @NonNull ConcreteType JAVA_DATACONTAINER = typeForClass(JavaDataContainer.class);

    // This is an annotation, we are current just referencing the type
    public static final JavaTypeName ROUTING_CONTEXT = JavaTypeName.create(RoutingContext.class);

    @VisibleForTesting
    static final @NonNull ConcreteType AUGMENTABLE = typeForClass(Augmentable.class);
    @VisibleForTesting
    static final @NonNull ConcreteType AUGMENTATION = typeForClass(Augmentation.class);
    @VisibleForTesting
    static final @NonNull ConcreteType ENTRY_OBJECT = typeForClass(EntryObject.class);
    @VisibleForTesting
    static final @NonNull ConcreteType KEY = typeForClass(Key.class);

    private static final @NonNull ConcreteType ACTION = typeForClass(Action.class);
    private static final @NonNull ConcreteType CHILD_OF = typeForClass(ChildOf.class);
    private static final @NonNull ConcreteType DATA_ROOT = typeForClass(DataRoot.class);
    private static final @NonNull ConcreteType INSTANCE_NOTIFICATION = typeForClass(InstanceNotification.class);
    private static final @NonNull ConcreteType KEYED_LIST_ACTION = typeForClass(KeyedListAction.class);
    private static final @NonNull ConcreteType KEYED_LIST_NOTIFICATION = typeForClass(KeyedListNotification.class);
    private static final @NonNull ConcreteType NOTIFICATION = typeForClass(Notification.class);
    private static final @NonNull ConcreteType NOTIFICATION_BODY = typeForClass(NotificationBody.class);
    private static final @NonNull ConcreteType OBJECT_REFERENCE = typeForClass(DataObjectIdentifier.class);
    private static final @NonNull ConcreteType OBJECT_REFERENCE_WITH_KEY =
        typeForClass(DataObjectIdentifier.WithKey.class);
    private static final @NonNull ConcreteType ROOT_META = typeForClass(RootMeta.class);
    private static final @NonNull ConcreteType RPC = typeForClass(Rpc.class);
    private static final @NonNull ConcreteType RPC_RESULT = typeForClass(RpcResult.class);
    private static final @NonNull ConcreteType YANG_DATA = typeForClass(YangData.class);

    private BindingTypes() {
        //  Hidden on purpose
    }

    /**
     * Type specializing {@link Action} for a particular type.
     *
     * @param parent Type of parent defining the action
     * @param input Type input type
     * @param output Type output type
     * @return A parameterized type corresponding to {@code Action<Parent, Input, Output>}
     * @throws NullPointerException if any argument is {@code null}
     */
    @NonNullByDefault
    public static ParameterizedType action(final Type parent, final Type input, final Type output) {
        return ParameterizedType.of(ACTION, objectIdentifier(parent), input, output);
    }

    /**
     * Type specializing {@link KeyedListAction} for a particular type.
     *
     * @param parent Type of parent defining the action
     * @param keyType Type of parent's key
     * @param input Type input type
     * @param output Type output type
     * @return A parameterized type corresponding to {@code KeyedListAction<ParentKey, Parent, Input, Output>}
     * @throws NullPointerException if any argument is {@code null}
     */
    @NonNullByDefault
    public static ParameterizedType keyedListAction(final Type parent, final KeyArchetype keyType, final Type input,
            final Type output) {
        return ParameterizedType.of(KEYED_LIST_ACTION, keyType, parent, input, output);
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
     * Type specializing {@link DataRoot} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code DataRoot<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    @NonNullByDefault
    public static ParameterizedType dataRoot(final Type type) {
        return ParameterizedType.of(DATA_ROOT, type);
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
     * @see #extractEntryObjectKey(GeneratedType)
     */
    @NonNullByDefault
    public static ParameterizedType entryObject(final Type type, final KeyArchetype keyType) {
        return ParameterizedType.of(ENTRY_OBJECT, type, keyType);
    }

    /**
     * Type specializing {@link DataObjectIdentifier} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code DataObjectIdentifier<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    @NonNullByDefault
    public static ParameterizedType objectIdentifier(final Type type) {
        return ParameterizedType.of(OBJECT_REFERENCE, type);
    }

    /**
     * Type specializing {@link DataObjectIdentifier.WithKey} for a particular type.
     *
     * @param type Type for which to specialize
     * @param keyType Type of key
     * @return A parameterized type corresponding to {@code DataObjectIdentifier.WithKey<Type, KeyType>}
     * @throws NullPointerException if any argument is is {@code null}
     */
    @NonNullByDefault
    public static ParameterizedType objectIdentifierWithKey(final Type type, final KeyArchetype keyType) {
        return ParameterizedType.of(OBJECT_REFERENCE_WITH_KEY, type, keyType);
    }

    /**
     * Type specializing {@link RootMeta} for a particular type.
     *
     * @param root the {@link DataRoot} type
     * @return A parameterized type corresponding to {@code RootMeta<Root>}
     * @throws NullPointerException if {@code root} is {@code null}
     */
    @NonNullByDefault
    public static ParameterizedType rootMeta(final Type root) {
        return ParameterizedType.of(ROOT_META, root);
    }

    /**
     * Type specializing {@link Rpc} for a particular type.
     *
     * @param input Type input type
     * @param output Type output type
     * @return A parameterized type corresponding to {@code Rpc<Input, Output>}
     * @throws NullPointerException if any argument is {@code null}
     */
    @NonNullByDefault
    public static ParameterizedType rpc(final Type input, final Type output) {
        return ParameterizedType.of(RPC, input, output);
    }

    /**
     * Type specializing {@link RpcResult} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code RpcResult<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    @NonNullByDefault
    public static ParameterizedType rpcResult(final Type type) {
        return ParameterizedType.of(RPC_RESULT, type);
    }

    /**
     * Type specializing {@link ScalarTypeObject} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code ScalarTypeObject<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    @NonNullByDefault
    public static ParameterizedType scalarTypeObject(final Type type) {
        return ParameterizedType.of(SCALAR_TYPE_OBJECT, type);
    }

    /**
     * Type specializing {@link YangData} for a particular type.
     *
     * @param concreteType The concrete type of this notification
     * @return A parameterized type corresponding to {@code YangData<Type>}
     * @throws NullPointerException if any argument is is {@code null}
     */
    @NonNullByDefault
    public static ParameterizedType yangData(final Type concreteType) {
        return ParameterizedType.of(YANG_DATA, concreteType);
    }

    /**
     * Check if specified type is a generated {@link NotificationBody}.
     *
     * @param type Type to examine
     * @return {@code true} if the type is a generated {@link NotificationBody}
     */
    public static boolean isNotificationBody(final Type type) {
        if (type instanceof GeneratedType generated) {
            for (var iface : generated.getImplements()) {
                if (iface instanceof ParameterizedType parameterized
                    && NOTIFICATION_BODY.equals(parameterized.getRawType())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return the {@link Augmentable} type a parameterized {@link Augmentation} type references.
     *
     * @param type Parameterized type
     * @return Augmentable target, or null if {@code type} does not match the result of {@link #augmentation(Type)}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static @Nullable Type extractAugmentationTarget(final @NonNull ParameterizedType type) {
        return AUGMENTATION.equals(type.getRawType()) ? onlyTypeArgument(type) : null;
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

    /**
     * Recover the {@code keyType} argument from a potential {@link EntryObject} type. This is inverse operation to
     * adding {@link #entryObject(Type, KeyArchetype)} as an implemented interface.
     *
     * @param genType the generated type
     * @return the {@link KeyArchetype} defining the key type, or {@code null} if {@code genType} does not
     *         directly implement {@link EntryObject}
     * @since 16.0.0
     */
    public static @Nullable KeyArchetype extractEntryObjectKey(final @NonNull GeneratedType genType) {
        for (var iface : genType.getImplements()) {
            if (iface instanceof ParameterizedType parameterized && ENTRY_OBJECT.equals(parameterized.getRawType())) {
                final var args = parameterized.getActualTypeArguments();
                if (args.size() != 2) {
                    throw new VerifyException("Unexpected arguments " + args);
                }
                final var keyType = args.getLast();
                if (keyType instanceof KeyArchetype archetype) {
                    return archetype;
                }
                throw new VerifyException("Unexpected key type " + keyType);
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
