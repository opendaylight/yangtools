/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri;

import static org.opendaylight.yangtools.binding.contract.Naming.VALUE_STATIC_FIELD_NAME;
import static org.opendaylight.yangtools.binding.model.ri.Types.parameterizedTypeFor;
import static org.opendaylight.yangtools.binding.model.ri.Types.typeForBuiltIn;
import static org.opendaylight.yangtools.binding.model.ri.Types.typeForClass;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Action;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.BaseIdentity;
import org.opendaylight.yangtools.binding.ChildOf;
import org.opendaylight.yangtools.binding.ChoiceIn;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.DataRoot;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Grouping;
import org.opendaylight.yangtools.binding.InstanceNotification;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyAware;
import org.opendaylight.yangtools.binding.KeyedListAction;
import org.opendaylight.yangtools.binding.KeyedListNotification;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.binding.NotificationBody;
import org.opendaylight.yangtools.binding.OpaqueObject;
import org.opendaylight.yangtools.binding.Rpc;
import org.opendaylight.yangtools.binding.RpcInput;
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.binding.ScalarTypeObject;
import org.opendaylight.yangtools.binding.YangData;
import org.opendaylight.yangtools.binding.YangFeature;
import org.opendaylight.yangtools.binding.annotations.RoutingContext;
import org.opendaylight.yangtools.binding.contract.BuiltInType;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

public final class BindingTypes {

    public static final ConcreteType BASE_IDENTITY = typeForBuiltIn(BuiltInType.IDENTITYREF);
    public static final ConcreteType DATA_CONTAINER = typeForClass(DataContainer.class);
    public static final ConcreteType DATA_OBJECT = typeForClass(DataObject.class);
    public static final ConcreteType GROUPING = typeForClass(Grouping.class);
    public static final ConcreteType QNAME = typeForClass(QName.class);
    public static final ConcreteType RPC_INPUT = typeForClass(RpcInput.class);
    public static final ConcreteType RPC_OUTPUT = typeForClass(RpcOutput.class);
    public static final ConcreteType SCALAR_TYPE_OBJECT = typeForClass(ScalarTypeObject.class);
    public static final ConcreteType BITS_TYPE_OBJECT = typeForBuiltIn(BuiltInType.BITS);
    public static final ConcreteType UNION_TYPE_OBJECT = typeForBuiltIn(BuiltInType.UNION);
    public static final ConcreteType YANG_DATA_NAME = typeForClass(YangDataName.class);

    // This is an annotation, we are current just referencing the type
    public static final JavaTypeName ROUTING_CONTEXT = JavaTypeName.create(RoutingContext.class);

    @VisibleForTesting
    static final ConcreteType AUGMENTABLE = typeForClass(Augmentable.class);
    @VisibleForTesting
    static final ConcreteType AUGMENTATION = typeForClass(Augmentation.class);
    @VisibleForTesting
    static final ConcreteType ENTRY_OBJECT = typeForClass(EntryObject.class);
    @VisibleForTesting
    static final ConcreteType KEY = typeForClass(Key.class);

    private static final ConcreteType ACTION = typeForClass(Action.class);
    private static final ConcreteType CHILD_OF = typeForClass(ChildOf.class);
    private static final ConcreteType CHOICE_IN = typeForClass(ChoiceIn.class);
    private static final ConcreteType DATA_ROOT = typeForClass(DataRoot.class);
    private static final ConcreteType INSTANCE_NOTIFICATION = typeForClass(InstanceNotification.class);
    private static final ConcreteType KEYED_LIST_ACTION = typeForClass(KeyedListAction.class);
    private static final ConcreteType KEYED_LIST_NOTIFICATION = typeForClass(KeyedListNotification.class);
    private static final ConcreteType NOTIFICATION_BODY = typeForClass(NotificationBody.class);
    private static final ConcreteType OBJECT_REFERENCE = typeForClass(DataObjectIdentifier.class);
    private static final ConcreteType OBJECT_REFERENCE_WITH_KEY = typeForClass(DataObjectIdentifier.WithKey.class);
    private static final ConcreteType NOTIFICATION = typeForClass(Notification.class);
    private static final ConcreteType OPAQUE_OBJECT = typeForClass(OpaqueObject.class);
    private static final ConcreteType RPC = typeForClass(Rpc.class);
    private static final ConcreteType RPC_RESULT = typeForClass(RpcResult.class);
    private static final ConcreteType YANG_FEATURE = typeForClass(YangFeature.class);
    private static final ConcreteType YANG_DATA = typeForClass(YangData.class);

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
    public static ParameterizedType action(final Type parent, final Type input, final Type output) {
        return parameterizedTypeFor(ACTION, objectIdentifier(parent), input, output);
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
    public static ParameterizedType keyedListAction(final Type parent, final Type keyType, final Type input,
            final Type output) {
        return parameterizedTypeFor(KEYED_LIST_ACTION, keyType, parent, input, output);
    }

    /**
     * Type specializing {@link Notification} for a particular type.
     *
     * @param concreteType The concrete type of this notification
     * @return A parameterized type corresponding to {@code Notification<ConcreteType>}
     * @throws NullPointerException if any argument is {@code null}
     */
    public static ParameterizedType notification(final Type concreteType) {
        return parameterizedTypeFor(NOTIFICATION, concreteType);
    }

    /**
     * Type specializing {@link NotificationBody} for a particular type.
     *
     * @param concreteType The concrete type of this notification
     * @return A parameterized type corresponding to {@code GroupingNotification<ConcreteType>}
     * @throws NullPointerException if {@code parent} is {@code null}
     */
    public static ParameterizedType notificationBody(final Type concreteType) {
        return parameterizedTypeFor(NOTIFICATION_BODY, concreteType);
    }

    /**
     * Type specializing {@link InstanceNotification} for a particular type.
     *
     * @param concreteType The concrete type of this notification
     * @param parent Type of parent defining the notification
     * @return A parameterized type corresponding to {@code InstanceNotification<ConcreteType, Parent>}
     * @throws NullPointerException if {@code parent} is {@code null}
     */
    public static ParameterizedType instanceNotification(final Type concreteType, final Type parent) {
        return parameterizedTypeFor(INSTANCE_NOTIFICATION, concreteType, parent);
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
    public static ParameterizedType keyedListNotification(final Type concreteType, final Type parent,
            final Type keyType) {
        return parameterizedTypeFor(KEYED_LIST_NOTIFICATION, concreteType, parent, keyType);
    }

    /**
     * Specialize {@link Augmentable} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code Augmentable<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static @NonNull ParameterizedType augmentable(final Type type) {
        return parameterizedTypeFor(AUGMENTABLE, type);
    }

    /**
     * Specialize {@link Augmentation} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code Augmentation<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static @NonNull ParameterizedType augmentation(final Type type) {
        return parameterizedTypeFor(AUGMENTATION, type);
    }

    /**
     * Specialize {@link ChildOf} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code ChildOf<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static ParameterizedType childOf(final Type type) {
        return parameterizedTypeFor(CHILD_OF, type);
    }

    /**
     * Type specializing {@link DataRoot} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code DataRoot<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static ParameterizedType dataRoot(final Type type) {
        return parameterizedTypeFor(DATA_ROOT, type);
    }

    /**
     * Type specializing {@link ChoiceIn} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code ChoiceIn<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static ParameterizedType choiceIn(final Type type) {
        return parameterizedTypeFor(CHOICE_IN, type);
    }

    /**
     * Type specializing {@link Key} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code Key<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static ParameterizedType key(final Type type) {
        return parameterizedTypeFor(KEY, type);
    }

    /**
     * Type specializing {@link EntryObject} for a particular type.
     *
     * @param type Type for which to specialize
     * @param keyType the corresponding {@link #key(Type)}
     * @return A parameterized type corresponding to {@code EntryObject<Type, KeyType>}
     * @throws NullPointerException if any argument is {@code null}
     */
    public static ParameterizedType entryObject(final Type type, final Type keyType) {
        return parameterizedTypeFor(ENTRY_OBJECT, type, keyType);
    }

    /**
     * Type specializing {@link DataObjectIdentifier} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code DataObjectIdentifier<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static ParameterizedType objectIdentifier(final Type type) {
        return parameterizedTypeFor(OBJECT_REFERENCE, type);
    }

    /**
     * Type specializing {@link DataObjectIdentifier.WithKey} for a particular type.
     *
     * @param type Type for which to specialize
     * @param keyType Type of key
     * @return A parameterized type corresponding to {@code DataObjectIdentifier.WithKey<Type, KeyType>}
     * @throws NullPointerException if any argument is is {@code null}
     */
    public static ParameterizedType objectIdentifierWithKey(final Type type, final Type keyType) {
        return parameterizedTypeFor(OBJECT_REFERENCE_WITH_KEY, type, keyType);
    }

    /**
     * Type specializing {@link OpaqueObject} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code OpaqueObject<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static ParameterizedType opaqueObject(final Type type) {
        return parameterizedTypeFor(OPAQUE_OBJECT, type);
    }

    /**
     * Type specializing {@link Rpc} for a particular type.
     *
     * @param input Type input type
     * @param output Type output type
     * @return A parameterized type corresponding to {@code Rpc<Input, Output>}
     * @throws NullPointerException if any argument is {@code null}
     */
    public static @NonNull ParameterizedType rpc(final Type input, final Type output) {
        return parameterizedTypeFor(RPC, input, output);
    }

    /**
     * Type specializing {@link RpcResult} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code RpcResult<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static ParameterizedType rpcResult(final Type type) {
        return parameterizedTypeFor(RPC_RESULT, type);
    }

    /**
     * Type specializing {@link ScalarTypeObject} for a particular type.
     *
     * @param type Type for which to specialize
     * @return A parameterized type corresponding to {@code ScalarTypeObject<Type>}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static ParameterizedType scalarTypeObject(final Type type) {
        return parameterizedTypeFor(SCALAR_TYPE_OBJECT, type);
    }

    /**
     * Type specializing {@link YangData} for a particular type.
     *
     * @param concreteType The concrete type of this notification
     * @return A parameterized type corresponding to {@code YangData<Type>}
     * @throws NullPointerException if any argument is is {@code null}
     */
    public static ParameterizedType yangData(final Type concreteType) {
        return parameterizedTypeFor(YANG_DATA, concreteType);
    }

    /**
     * Type specializing {@link YangFeature} for a particular type.
     *
     * @param concreteType The concrete type of this feature
     * @param parent Type of parent defining the feature
     * @return A parameterized type corresponding to {@code YangFeature<Type, DataRootType>}
     * @throws NullPointerException if any argument is is {@code null}
     */
    public static ParameterizedType yangFeature(final Type concreteType, final Type parent) {
        return parameterizedTypeFor(YANG_FEATURE, concreteType, parent);
    }

    /**
     * Check if specified type is generated for a {@code type bits}.
     *
     * @param type Type to examine
     * @return {@code true} if the type is generated for a {@code type bits}
     */
    public static boolean isBitsType(final Type type) {
        return type instanceof GeneratedTransferObject gto && isBitsType(gto);
    }

    /**
     * Check if specified type is generated for a {@code type bits}.
     *
     * @param gto Type to examine
     * @return {@code true} if the type is generated for a {@code type bits}
     */
    public static boolean isBitsType(final GeneratedTransferObject gto) {
        return gto.isTypedef() && gto.getBaseType() instanceof BitsTypeDefinition;
    }

    /**
     * Check if specified type is generated for an identity.
     *
     * @param type Type to examine
     * @return {@code true} if the type is generated for an identity
     */
    public static boolean isIdentityType(final Type type) {
        if (type instanceof GeneratedType generated) {
            for (var constant : generated.getConstantDefinitions()) {
                if (VALUE_STATIC_FIELD_NAME.equals(constant.getName())
                    && BaseIdentity.class.equals(constant.getValue())) {
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
    public static @Nullable Type extractAugmentationTarget(final ParameterizedType type) {
        return AUGMENTATION.equals(type.getRawType()) ? onlyTypeArgument(type) : null;
    }

    /**
     * Return the {@link Augmentable} type a parameterized {@link Augmentable} type references.
     *
     * @param type Parameterized type
     * @return Augmentable target, or null if {@code type} does not match the result of {@link #augmentable(Type)} or
     *         {@link #entryObject(Type, Type)}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static @Nullable Type extractAugmentableTarget(final ParameterizedType type) {
        final var rawType = type.getRawType();
        if (AUGMENTABLE.equals(rawType)) {
            return onlyTypeArgument(type);
        }
        if (ENTRY_OBJECT.equals(rawType)) {
            final var args = type.getActualTypeArguments();
            if (args.length == 2) {
                final var arg = args[0];
                if (arg != null) {
                    return arg;
                }
            }
        }
        return null;
    }

    /**
     * Return the {@link KeyAware} type a parameterized {@link Key} type references.
     *
     * @param type Parameterized type
     * @return Identifiable target, or null if {@code type} does not match the result of {@link #key(Type)}
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public static @Nullable Type extractKeyType(final ParameterizedType type) {
        return KEY.equals(type.getRawType()) ? onlyTypeArgument(type) : null;
    }

    private static Type onlyTypeArgument(final ParameterizedType type) {
        final var args = type.getActualTypeArguments();
        if (args.length == 1) {
            final var arg = args[0];
            if (arg != null) {
                return arg;
            }
        }
        return null;
    }

    @Beta
    public static @Nullable Type extractYangFeatureDataRoot(final GeneratedTransferObject gto) {
        if (!gto.isAbstract() && gto.getSuperType() == null) {
            final var impls = gto.getImplements();
            if (impls.size() == 1 && impls.get(0) instanceof ParameterizedType param
                && YANG_FEATURE.equals(param.getRawType())) {
                final var args = param.getActualTypeArguments();
                if (args.length == 2) {
                    return args[1];
                }
            }
        }
        return null;
    }
}
