/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spec.reflect;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.BindingContract;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BindingReflections {
    private static final Logger LOG = LoggerFactory.getLogger(BindingReflections.class);
    private static final LoadingCache<Class<?>, Optional<QName>> CLASS_TO_QNAME = CacheBuilder.newBuilder()
            .weakKeys()
            .expireAfterAccess(60, TimeUnit.SECONDS)
            .build(new ClassToQNameLoader());

    private BindingReflections() {
        // Hidden on purpose
    }

    /**
     * Returns a QName associated to supplied type.
     *
     * @param dataType Data type class
     * @return QName associated to supplied dataType. If dataType is Augmentation method does not return canonical
     *         QName, but QName with correct namespace revision, but virtual local name, since augmentations do not
     *         have name. May return null if QName is not present.
     */
    public static QName findQName(final Class<?> dataType) {
        return CLASS_TO_QNAME.getUnchecked(dataType).orElse(null);
    }

    /**
     * Checks if method is RPC invocation.
     *
     * @param possibleMethod
     *            Method to check
     * @return true if method is RPC invocation, false otherwise.
     */
    public static boolean isRpcMethod(final Method possibleMethod) {
        return possibleMethod != null && RpcService.class.isAssignableFrom(possibleMethod.getDeclaringClass())
                && ListenableFuture.class.isAssignableFrom(possibleMethod.getReturnType())
                // length <= 2: it seemed to be impossible to get correct RpcMethodInvoker because of
                // resolveRpcInputClass() check.While RpcMethodInvoker counts with one argument for
                // non input type and two arguments for input type, resolveRpcInputClass() counting
                // with zero for non input and one for input type
                && possibleMethod.getParameterCount() <= 2;
    }

    public static @NonNull QName getQName(final BaseIdentity identity) {
        return getContractQName(identity);
    }

    public static @NonNull QName getQName(final Rpc<?, ?> rpc) {
        return getContractQName(rpc);
    }

    private static @NonNull QName getContractQName(final BindingContract<?> contract) {
        return CLASS_TO_QNAME.getUnchecked(contract.implementedInterface())
            .orElseThrow(() -> new IllegalStateException("Failed to resolve QName of " + contract));
    }

    public static QNameModule getQNameModule(final Class<?> clz) {
        if (DataContainer.class.isAssignableFrom(clz) || BaseIdentity.class.isAssignableFrom(clz)
                || Action.class.isAssignableFrom(clz)) {
            return findQName(clz).getModule();
        }

        return getModuleInfo(clz).getName().getModule();
    }

    /**
     * Returns instance of {@link YangModuleInfo} of declaring model for specific class.
     *
     * @param cls data object class
     * @return Instance of {@link YangModuleInfo} associated with model, from which this class was derived.
     */
    private static @NonNull YangModuleInfo getModuleInfo(final Class<?> cls) {
        final String packageName = Naming.getModelRootPackageName(cls.getPackage().getName());
        final String potentialClassName = getModuleInfoClassName(packageName);
        final Class<?> moduleInfoClass;
        try {
            moduleInfoClass = cls.getClassLoader().loadClass(potentialClassName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load " + potentialClassName, e);
        }

        final Object infoInstance;
        try {
            infoInstance = moduleInfoClass.getMethod("getInstance").invoke(null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException("Failed to get instance of " + moduleInfoClass, e);
        }

        checkState(infoInstance instanceof YangModuleInfo, "Unexpected instance %s", infoInstance);
        return (YangModuleInfo) infoInstance;
    }

    public static @NonNull String getModuleInfoClassName(final String packageName) {
        return packageName + "." + Naming.MODULE_INFO_CLASS_NAME;
    }

    /**
     * Check if supplied class is derived from YANG model.
     *
     * @param cls
     *            Class to check
     * @return true if class is derived from YANG model.
     */
    public static boolean isBindingClass(final Class<?> cls) {
        if (DataContainer.class.isAssignableFrom(cls) || Augmentation.class.isAssignableFrom(cls)) {
            return true;
        }
        return cls.getName().startsWith(Naming.PACKAGE_PREFIX);
    }

    /**
     * Checks if supplied method is callback for notifications.
     *
     * @param method method to check
     * @return true if method is notification callback.
     */
    public static boolean isNotificationCallback(final Method method) {
        checkArgument(method != null);
        if (method.getName().startsWith("on") && method.getParameterCount() == 1) {
            Class<?> potentialNotification = method.getParameterTypes()[0];
            if (isNotification(potentialNotification)
                    && method.getName().equals("on" + potentialNotification.getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks is supplied class is a {@link Notification}.
     *
     * @param potentialNotification class to examine
     * @return True if the class represents a Notification.
     */
    @VisibleForTesting
    static boolean isNotification(final Class<?> potentialNotification) {
        checkArgument(potentialNotification != null, "potentialNotification must not be null.");
        return Notification.class.isAssignableFrom(potentialNotification);
    }

    /**
     * Checks if supplied class represents RPC Input / RPC Output.
     *
     * @param targetType
     *            Class to be checked
     * @return true if class represents RPC Input or RPC Output class.
     */
    public static boolean isRpcType(final Class<? extends DataObject> targetType) {
        return DataContainer.class.isAssignableFrom(targetType)
                && !ChildOf.class.isAssignableFrom(targetType)
                && !Notification.class.isAssignableFrom(targetType)
                && (targetType.getName().endsWith("Input") || targetType.getName().endsWith("Output"));
    }

    private static class ClassToQNameLoader extends CacheLoader<Class<?>, Optional<QName>> {

        @Override
        public Optional<QName> load(@SuppressWarnings("NullableProblems") final Class<?> key) throws Exception {
            return resolveQNameNoCache(key);
        }

        /**
         * Tries to resolve QName for supplied class. Looks up for static field with name from constant
         * {@link Naming#QNAME_STATIC_FIELD_NAME} and returns value if present. If field is not present uses
         * {@link #computeQName(Class)} to compute QName for missing types.
         */
        private static Optional<QName> resolveQNameNoCache(final Class<?> key) {
            try {
                final Field field;
                try {
                    field = key.getField(Naming.QNAME_STATIC_FIELD_NAME);
                } catch (NoSuchFieldException e) {
                    LOG.debug("{} does not have a {} field, falling back to computation", key,
                        Naming.QNAME_STATIC_FIELD_NAME, e);
                    return Optional.of(computeQName(key));
                }

                final Object obj = field.get(null);
                if (obj instanceof QName qname) {
                    return Optional.of(qname);
                }
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                /*
                 * It is safe to log this this exception on debug, since this method should not fail. Only failures are
                 * possible if the runtime / backing is inconsistent.
                 */
                LOG.debug("Unexpected exception during extracting QName for {}", key, e);
            }
            return Optional.empty();
        }

        /**
         * Computes QName for supplied class. Namespace and revision are same as {@link YangModuleInfo} associated with
         * supplied class.
         *
         * <p>
         * If class is
         * <ul>
         * <li>rpc input: local name is "input".
         * <li>rpc output: local name is "output".
         * <li>augmentation: local name is "module name".
         * </ul>
         *
         * <p>
         * There is also fallback, if it is not possible to compute QName using following algorithm returns module
         * QName.
         *
         * @throws IllegalStateException If YangModuleInfo could not be resolved
         * @throws IllegalArgumentException If supplied class was not derived from YANG model.
         */
        // FIXME: Extend this algorithm to also provide QName for YANG modeled simple types.
        @SuppressWarnings({ "rawtypes", "unchecked" })
        private static QName computeQName(final Class key) {
            checkArgument(isBindingClass(key), "Supplied class %s is not derived from YANG.", key);

            final QName module = getModuleInfo(key).getName();
            if (Augmentation.class.isAssignableFrom(key)) {
                return module;
            } else if (isRpcType(key)) {
                final String className = key.getSimpleName();
                if (className.endsWith(Naming.RPC_OUTPUT_SUFFIX)) {
                    return YangConstants.operationOutputQName(module.getModule()).intern();
                }

                return YangConstants.operationInputQName(module.getModule()).intern();
            }

            /*
             * Fallback for Binding types which do not have QNAME field
             */
            return module;
        }
    }
}
