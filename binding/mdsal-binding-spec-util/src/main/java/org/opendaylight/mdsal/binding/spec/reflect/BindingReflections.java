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

import com.google.common.annotations.Beta;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.util.concurrent.ListenableFuture;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.regex.qual.Regex;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.BindingContract;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.binding.YangModelBindingProvider;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BindingReflections {

    private static final long EXPIRATION_TIME = 60;

    @Regex
    private static final String ROOT_PACKAGE_PATTERN_STRING =
            "(org.opendaylight.yang.gen.v1.[a-z0-9_\\.]*\\.(?:rev[0-9][0-9][0-1][0-9][0-3][0-9]|norev))";
    private static final Pattern ROOT_PACKAGE_PATTERN = Pattern.compile(ROOT_PACKAGE_PATTERN_STRING);
    private static final Logger LOG = LoggerFactory.getLogger(BindingReflections.class);

    private static final LoadingCache<Class<?>, Optional<QName>> CLASS_TO_QNAME = CacheBuilder.newBuilder()
            .weakKeys()
            .expireAfterAccess(EXPIRATION_TIME, TimeUnit.SECONDS)
            .build(new ClassToQNameLoader());

    private static final LoadingCache<ClassLoader, ImmutableSet<YangModuleInfo>> MODULE_INFO_CACHE =
            CacheBuilder.newBuilder().weakKeys().weakValues().build(
                new CacheLoader<ClassLoader, ImmutableSet<YangModuleInfo>>() {
                    @Override
                    public ImmutableSet<YangModuleInfo> load(final ClassLoader key) {
                        return loadModuleInfos(key);
                    }
                });

    private BindingReflections() {
        // Hidden on purpose
    }

    /**
     * Find augmentation target class from concrete Augmentation class. This method uses first generic argument of
     * implemented {@link Augmentation} interface.
     *
     * @param augmentation
     *            {@link Augmentation} subclass for which we want to determine
     *            augmentation target.
     * @return Augmentation target - class which augmentation provides additional extensions.
     */
    public static Class<? extends Augmentable<?>> findAugmentationTarget(
            final Class<? extends Augmentation<?>> augmentation) {
        final Optional<Class<Augmentable<?>>> opt = ClassLoaderUtils.findFirstGenericArgument(augmentation,
            Augmentation.class);
        return opt.orElse(null);
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

    /**
     * Extracts Output class for RPC method.
     *
     * @param targetMethod method to scan
     * @return Optional.empty() if result type could not be get, or return type is Void.
     * @deprecated This method is unused and scheduled for removal
     */
    @Deprecated(since = "10.0.4", forRemoval = true)
    @SuppressWarnings("rawtypes")
    public static Optional<Class<?>> resolveRpcOutputClass(final Method targetMethod) {
        checkState(isRpcMethod(targetMethod), "Supplied method is not a RPC invocation method");
        Type futureType = targetMethod.getGenericReturnType();
        Type rpcResultType = ClassLoaderUtils.getFirstGenericParameter(futureType).orElse(null);
        Type rpcResultArgument = ClassLoaderUtils.getFirstGenericParameter(rpcResultType).orElse(null);
        if (rpcResultArgument instanceof Class cls && !Void.class.equals(rpcResultArgument)) {
            return Optional.of(cls);
        }
        return Optional.empty();
    }

    /**
     * Extracts input class for RPC method.
     *
     * @param targetMethod method to scan
     * @return Optional.empty() if RPC has no input, RPC input type otherwise.
     * @deprecated This method is unused and scheduled for removal
     */
    @Deprecated(since = "10.0.4", forRemoval = true)
    @SuppressWarnings("rawtypes")
    public static Optional<Class<? extends DataContainer>> resolveRpcInputClass(final Method targetMethod) {
        for (Class clazz : targetMethod.getParameterTypes()) {
            if (DataContainer.class.isAssignableFrom(clazz)) {
                return Optional.of(clazz);
            }
        }
        return Optional.empty();
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

    /**
     * Returns root package name for supplied package.
     *
     * @param pkg
     *            Package for which find model root package.
     * @return Package of model root.
     */
    public static String getModelRootPackageName(final Package pkg) {
        return getModelRootPackageName(pkg.getName());
    }

    /**
     * Returns root package name for supplied package name.
     *
     * @param name
     *            Package for which find model root package.
     * @return Package of model root.
     */
    public static String getModelRootPackageName(final String name) {
        checkArgument(name != null, "Package name should not be null.");
        checkArgument(name.startsWith(BindingMapping.PACKAGE_PREFIX), "Package name not starting with %s, is: %s",
                BindingMapping.PACKAGE_PREFIX, name);
        Matcher match = ROOT_PACKAGE_PATTERN.matcher(name);
        checkArgument(match.find(), "Package name '%s' does not match required pattern '%s'", name,
                ROOT_PACKAGE_PATTERN_STRING);
        return match.group(0);
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
    public static @NonNull YangModuleInfo getModuleInfo(final Class<?> cls) {
        final String packageName = getModelRootPackageName(cls.getPackage());
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
        return packageName + "." + BindingMapping.MODULE_INFO_CLASS_NAME;
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
        return cls.getName().startsWith(BindingMapping.PACKAGE_PREFIX);
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
    public static boolean isNotification(final Class<?> potentialNotification) {
        checkArgument(potentialNotification != null, "potentialNotification must not be null.");
        return Notification.class.isAssignableFrom(potentialNotification);
    }

    /**
     * Loads {@link YangModuleInfo} infos available on current classloader. This method is shorthand for
     * {@link #loadModuleInfos(ClassLoader)} with {@link Thread#getContextClassLoader()} for current thread.
     *
     * @return Set of {@link YangModuleInfo} available for current classloader.
     */
    public static @NonNull ImmutableSet<YangModuleInfo> loadModuleInfos() {
        return loadModuleInfos(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Loads {@link YangModuleInfo} infos available on supplied classloader.
     *
     * <p>
     * {@link YangModuleInfo} are discovered using {@link ServiceLoader} for {@link YangModelBindingProvider}.
     * {@link YangModelBindingProvider} are simple classes which holds only pointers to actual instance
     * {@link YangModuleInfo}.
     *
     * <p>
     * When {@link YangModuleInfo} is available, all dependencies are recursively collected into returning set by
     * collecting results of {@link YangModuleInfo#getImportedModules()}.
     *
     * <p>
     * Consider using {@link #cacheModuleInfos(ClassLoader)} if the classloader is known to be immutable.
     *
     * @param loader Classloader for which {@link YangModuleInfo} should be retrieved.
     * @return Set of {@link YangModuleInfo} available for supplied classloader.
     */
    public static @NonNull ImmutableSet<YangModuleInfo> loadModuleInfos(final ClassLoader loader) {
        Builder<YangModuleInfo> moduleInfoSet = ImmutableSet.builder();
        ServiceLoader<YangModelBindingProvider> serviceLoader = ServiceLoader.load(YangModelBindingProvider.class,
                loader);
        for (YangModelBindingProvider bindingProvider : serviceLoader) {
            YangModuleInfo moduleInfo = bindingProvider.getModuleInfo();
            checkState(moduleInfo != null, "Module Info for %s is not available.", bindingProvider.getClass());
            collectYangModuleInfo(bindingProvider.getModuleInfo(), moduleInfoSet);
        }
        return moduleInfoSet.build();
    }

    /**
     * Loads {@link YangModuleInfo} instances available on supplied {@link ClassLoader}, assuming the set of available
     * information does not change. Subsequent accesses may return cached values.
     *
     * <p>
     * {@link YangModuleInfo} are discovered using {@link ServiceLoader} for {@link YangModelBindingProvider}.
     * {@link YangModelBindingProvider} are simple classes which holds only pointers to actual instance
     * {@link YangModuleInfo}.
     *
     * <p>
     * When {@link YangModuleInfo} is available, all dependencies are recursively collected into returning set by
     * collecting results of {@link YangModuleInfo#getImportedModules()}.
     *
     * @param loader Class loader for which {@link YangModuleInfo} should be retrieved.
     * @return Set of {@link YangModuleInfo} available for supplied classloader.
     */
    @Beta
    public static @NonNull ImmutableSet<YangModuleInfo> cacheModuleInfos(final ClassLoader loader) {
        return MODULE_INFO_CACHE.getUnchecked(loader);
    }

    private static void collectYangModuleInfo(final YangModuleInfo moduleInfo,
            final Builder<YangModuleInfo> moduleInfoSet) {
        moduleInfoSet.add(moduleInfo);
        for (YangModuleInfo dependency : moduleInfo.getImportedModules()) {
            collectYangModuleInfo(dependency, moduleInfoSet);
        }
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

    /**
     * Scans supplied class and returns an iterable of all data children classes.
     *
     * @param type
     *            YANG Modeled Entity derived from DataContainer
     * @return Iterable of all data children, which have YANG modeled entity
     */
    @SuppressWarnings("unchecked")
    public static Iterable<Class<? extends DataObject>> getChildrenClasses(final Class<? extends DataContainer> type) {
        checkArgument(type != null, "Target type must not be null");
        checkArgument(DataContainer.class.isAssignableFrom(type), "Supplied type must be derived from DataContainer");
        List<Class<? extends DataObject>> ret = new LinkedList<>();
        for (Method method : type.getMethods()) {
            Optional<Class<? extends DataContainer>> entity = getYangModeledReturnType(method,
                BindingMapping.GETTER_PREFIX);
            if (entity.isPresent()) {
                ret.add((Class<? extends DataObject>) entity.get());
            }
        }
        return ret;
    }

    /**
     * Scans supplied class and returns an iterable of all data children classes.
     *
     * @param type YANG Modeled Entity derived from DataContainer
     * @return Iterable of all data children, which have YANG modeled entity
     */
    public static Map<Class<? extends DataContainer>, Method> getChildrenClassToMethod(final Class<?> type) {
        return getChildClassToMethod(type, BindingMapping.GETTER_PREFIX);
    }

    @Beta
    public static Map<Class<? extends DataContainer>, Method> getChildrenClassToNonnullMethod(final Class<?> type) {
        return getChildClassToMethod(type, BindingMapping.NONNULL_PREFIX);
    }

    private static Map<Class<? extends DataContainer>, Method> getChildClassToMethod(final Class<?> type,
            final String prefix) {
        checkArgument(type != null, "Target type must not be null");
        checkArgument(DataContainer.class.isAssignableFrom(type), "Supplied type %s must be derived from DataContainer",
            type);
        Map<Class<? extends DataContainer>, Method> ret = new HashMap<>();
        for (Method method : type.getMethods()) {
            Optional<Class<? extends DataContainer>> entity = getYangModeledReturnType(method, prefix);
            if (entity.isPresent()) {
                ret.put(entity.get(), method);
            }
        }
        return ret;
    }

    private static Optional<Class<? extends DataContainer>> getYangModeledReturnType(final Method method,
            final String prefix) {
        final String methodName = method.getName();
        if ("getClass".equals(methodName) || !methodName.startsWith(prefix) || method.getParameterCount() > 0) {
            return Optional.empty();
        }

        final Class<?> returnType = method.getReturnType();
        if (DataContainer.class.isAssignableFrom(returnType)) {
            return optionalDataContainer(returnType);
        } else if (List.class.isAssignableFrom(returnType)) {
            return getYangModeledReturnType(method, 0);
        } else if (Map.class.isAssignableFrom(returnType)) {
            return getYangModeledReturnType(method, 1);
        }
        return Optional.empty();
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private static Optional<Class<? extends DataContainer>> getYangModeledReturnType(final Method method,
            final int parameterOffset) {
        try {
            return ClassLoaderUtils.callWithClassLoader(method.getDeclaringClass().getClassLoader(),
                () -> genericParameter(method.getGenericReturnType(), parameterOffset)
                    .flatMap(result -> result instanceof Class ? optionalCast((Class<?>) result) : Optional.empty()));
        } catch (Exception e) {
            /*
             * It is safe to log this this exception on debug, since this
             * method should not fail. Only failures are possible if the
             * runtime / backing.
             */
            LOG.debug("Unable to find YANG modeled return type for {}", method, e);
        }
        return Optional.empty();
    }

    private static Optional<Class<? extends DataContainer>> optionalCast(final Class<?> type) {
        return DataContainer.class.isAssignableFrom(type) ? optionalDataContainer(type) : Optional.empty();
    }

    private static Optional<Class<? extends DataContainer>> optionalDataContainer(final Class<?> type) {
        return Optional.of(type.asSubclass(DataContainer.class));
    }

    private static Optional<Type> genericParameter(final Type type, final int offset) {
        if (type instanceof ParameterizedType parameterized) {
            final Type[] parameters = parameterized.getActualTypeArguments();
            if (parameters.length > offset) {
                return Optional.of(parameters[offset]);
            }
        }
        return Optional.empty();
    }

    private static class ClassToQNameLoader extends CacheLoader<Class<?>, Optional<QName>> {

        @Override
        public Optional<QName> load(@SuppressWarnings("NullableProblems") final Class<?> key) throws Exception {
            return resolveQNameNoCache(key);
        }

        /**
         * Tries to resolve QName for supplied class. Looks up for static field with name from constant
         * {@link BindingMapping#QNAME_STATIC_FIELD_NAME} and returns value if present. If field is not present uses
         * {@link #computeQName(Class)} to compute QName for missing types.
         */
        private static Optional<QName> resolveQNameNoCache(final Class<?> key) {
            try {
                final Field field;
                try {
                    field = key.getField(BindingMapping.QNAME_STATIC_FIELD_NAME);
                } catch (NoSuchFieldException e) {
                    LOG.debug("{} does not have a {} field, falling back to computation", key,
                        BindingMapping.QNAME_STATIC_FIELD_NAME, e);
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
                if (className.endsWith(BindingMapping.RPC_OUTPUT_SUFFIX)) {
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

    /**
     * Determines if two augmentation classes or case classes represents same data.
     *
     * <p>
     * Two augmentations or cases could be substituted only if and if:
     * <ul>
     *   <li>Both implements same interfaces</li>
     *   <li>Both have same children</li>
     *   <li>If augmentations: Both have same augmentation target class. Target class was generated for data node in a
     *       grouping.</li>
     *   <li>If cases: Both are from same choice. Choice class was generated for data node in grouping.</li>
     * </ul>
     *
     * <p>
     * <b>Explanation:</b>
     * Binding Specification reuses classes generated for groupings as part of normal data tree, this classes from
     * grouping could be used at various locations and user may not be aware of it and may use incorrect case or
     * augmentation in particular subtree (via copy constructors, etc).
     *
     * @param potential Class which is potential substitution
     * @param target Class which should be used at particular subtree
     * @return true if and only if classes represents same data.
     * @throws NullPointerException if any argument is {@code null}
     */
    // FIXME: MDSAL-785: this really should live in BindingRuntimeTypes and should not be based on reflection. The only
    //                   user is binding-dom-codec and the logic could easily be performed on GeneratedType instead. For
    //                   a particular world this boils down to a matrix, which can be calculated either on-demand or
    //                   when we create BindingRuntimeTypes. Achieving that will bring us one step closer to being able
    //                   to have a pre-compiled Binding Runtime.
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static boolean isSubstitutionFor(final Class potential, final Class target) {
        Set<Class> subImplemented = new HashSet<>(Arrays.asList(potential.getInterfaces()));
        Set<Class> targetImplemented = new HashSet<>(Arrays.asList(target.getInterfaces()));
        if (!subImplemented.equals(targetImplemented)) {
            return false;
        }
        if (Augmentation.class.isAssignableFrom(potential)
                && !BindingReflections.findAugmentationTarget(potential).equals(
                        BindingReflections.findAugmentationTarget(target))) {
            return false;
        }
        for (Method potentialMethod : potential.getMethods()) {
            if (Modifier.isStatic(potentialMethod.getModifiers())) {
                // Skip any static methods, as we are not interested in those
                continue;
            }

            try {
                Method targetMethod = target.getMethod(potentialMethod.getName(), potentialMethod.getParameterTypes());
                if (!potentialMethod.getReturnType().equals(targetMethod.getReturnType())) {
                    return false;
                }
            } catch (NoSuchMethodException e) {
                // Counterpart method is missing, so classes could not be substituted.
                return false;
            } catch (SecurityException e) {
                throw new IllegalStateException("Could not compare methods", e);
            }
        }
        return true;
    }
}
