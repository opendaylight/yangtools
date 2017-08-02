/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Sets;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.binding.YangModelBindingProvider;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BindingReflections {

    private static final long EXPIRATION_TIME = 60;
    private static final String ROOT_PACKAGE_PATTERN_STRING = "(org.opendaylight.yang.gen.v1.[a-z0-9_\\.]*\\.rev[0-9][0-9][0-1][0-9][0-3][0-9])";
    private static final Pattern ROOT_PACKAGE_PATTERN = Pattern.compile(ROOT_PACKAGE_PATTERN_STRING);
    private static final Logger LOG = LoggerFactory.getLogger(BindingReflections.class);

    private static final LoadingCache<Class<?>, Optional<QName>> CLASS_TO_QNAME = CacheBuilder.newBuilder()
            .weakKeys()
            .expireAfterAccess(EXPIRATION_TIME, TimeUnit.SECONDS)
            .build(new ClassToQNameLoader());

    private BindingReflections() {
        throw new UnsupportedOperationException("Utility class.");
    }

    /**
     * Find augmentation target class from concrete Augmentation class
     *
     * This method uses first generic argument of implemented
     * {@link Augmentation} interface.
     *
     * @param augmentation
     *            {@link Augmentation} subclass for which we want to determine
     *            augmentation target.
     * @return Augmentation target - class which augmentation provides
     *         additional extensions.
     */
    public static Class<? extends Augmentable<?>> findAugmentationTarget(
            final Class<? extends Augmentation<?>> augmentation) {
        return ClassLoaderUtils.findFirstGenericArgument(augmentation, Augmentation.class);
    }

    /**
     * Find data hierarchy parent from concrete Data class
     *
     * This method uses first generic argument of implemented {@link ChildOf}
     * interface.
     *
     * @param childClass
     *            child class for which we want to find the parent class.
     * @return Parent class, e.g. class of which the childClass is ChildOf.
     */
    public static Class<?> findHierarchicalParent(final Class<? extends ChildOf<?>> childClass) {
        return ClassLoaderUtils.findFirstGenericArgument(childClass, ChildOf.class);
    }

    /**
     * Find data hierarchy parent from concrete Data class
     *
     * This method is shorthand which gets DataObject class by invoking
     * {@link DataObject#getImplementedInterface()} and uses
     * {@link #findHierarchicalParent(Class)}.
     *
     * @param child
     *            Child object for which the parent needs to be located.
     * @return Parent class, or null if a parent is not found.
     */
    public static Class<?> findHierarchicalParent(final DataObject child) {
        if (child instanceof ChildOf) {
            return ClassLoaderUtils.findFirstGenericArgument(child.getImplementedInterface(), ChildOf.class);
        }
        return null;
    }

    /**
     * Returns a QName associated to supplied type
     *
     * @param dataType
     * @return QName associated to supplied dataType. If dataType is
     *         Augmentation method does not return canonical QName, but QName
     *         with correct namespace revision, but virtual local name, since
     *         augmentations do not have name.
     *
     *         May return null if QName is not present.
     */
    public static final QName findQName(final Class<?> dataType) {
        return CLASS_TO_QNAME.getUnchecked(dataType).orNull();
    }

    /**
     * Checks if method is RPC invocation
     *
     * @param possibleMethod
     *            Method to check
     * @return true if method is RPC invocation, false otherwise.
     */
    public static boolean isRpcMethod(final Method possibleMethod) {
        return possibleMethod != null && RpcService.class.isAssignableFrom(possibleMethod.getDeclaringClass())
                && Future.class.isAssignableFrom(possibleMethod.getReturnType())
                // length <= 2: it seemed to be impossible to get correct RpcMethodInvoker because of
                // resolveRpcInputClass() check.While RpcMethodInvoker counts with one argument for
                // non input type and two arguments for input type, resolveRpcInputClass() counting
                // with zero for non input and one for input type
                && possibleMethod.getParameterTypes().length <= 2;
    }

    /**
     *
     * Extracts Output class for RPC method
     *
     * @param targetMethod
     *            method to scan
     * @return Optional.absent() if result type could not be get, or return type
     *         is Void.
     */
    @SuppressWarnings("rawtypes")
    public static Optional<Class<?>> resolveRpcOutputClass(final Method targetMethod) {
        checkState(isRpcMethod(targetMethod), "Supplied method is not a RPC invocation method");
        Type futureType = targetMethod.getGenericReturnType();
        Type rpcResultType = ClassLoaderUtils.getFirstGenericParameter(futureType);
        Type rpcResultArgument = ClassLoaderUtils.getFirstGenericParameter(rpcResultType);
        if (rpcResultArgument instanceof Class && !Void.class.equals(rpcResultArgument)) {
            return Optional.of((Class) rpcResultArgument);
        }
        return Optional.absent();
    }

    /**
     *
     * Extracts input class for RPC method
     *
     * @param targetMethod
     *            method to scan
     * @return Optional.absent() if rpc has no input, Rpc input type otherwise.
     */
    @SuppressWarnings("rawtypes")
    public static Optional<Class<? extends DataContainer>> resolveRpcInputClass(final Method targetMethod) {
        for (Class clazz : targetMethod.getParameterTypes()) {
            if (DataContainer.class.isAssignableFrom(clazz)) {
                return Optional.of(clazz);
            }
        }
        return Optional.absent();
    }

    public static QName getQName(final Class<? extends BaseIdentity> context) {
        return findQName(context);
    }

    /**
     *
     * Checks if class is child of augmentation.
     *
     *
     * @param clazz
     * @return
     */
    public static boolean isAugmentationChild(final Class<?> clazz) {
        // FIXME: Current resolver could be still confused when
        // child node was added by grouping
        checkArgument(clazz != null);

        @SuppressWarnings({ "rawtypes", "unchecked" })
        Class<?> parent = findHierarchicalParent((Class) clazz);
        if (parent == null) {
            LOG.debug("Did not find a parent for class {}", clazz);
            return false;
        }

        String clazzModelPackage = getModelRootPackageName(clazz.getPackage());
        String parentModelPackage = getModelRootPackageName(parent.getPackage());

        return !clazzModelPackage.equals(parentModelPackage);
    }

    /**
     * Returns root package name for suplied package.
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

    public static final QNameModule getQNameModule(final Class<?> clz) {
        if(DataContainer.class.isAssignableFrom(clz) || BaseIdentity.class.isAssignableFrom(clz)) {
            return findQName(clz).getModule();
        }
        try {
            YangModuleInfo modInfo = BindingReflections.getModuleInfo(clz);
            return getQNameModule(modInfo);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to get QName of defining model.", e);
        }
    }

    public static final QNameModule getQNameModule(final YangModuleInfo modInfo) {
        return QNameModule.create(URI.create(modInfo.getNamespace()), QName.parseRevision(modInfo.getRevision()));
    }

    /**
     *
     * Returns instance of {@link YangModuleInfo} of declaring model for
     * specific class.
     *
     * @param cls
     * @return Instance of {@link YangModuleInfo} associated with model, from
     *         which this class was derived.
     * @throws Exception
     */
    public static YangModuleInfo getModuleInfo(final Class<?> cls) throws Exception {
        checkArgument(cls != null);
        String packageName = getModelRootPackageName(cls.getPackage());
        final String potentialClassName = getModuleInfoClassName(packageName);
        return ClassLoaderUtils.withClassLoader(cls.getClassLoader(), (Callable<YangModuleInfo>) () -> {
            Class<?> moduleInfoClass = Thread.currentThread().getContextClassLoader().loadClass(potentialClassName);
            return (YangModuleInfo) moduleInfoClass.getMethod("getInstance").invoke(null);
         });
    }

    public static String getModuleInfoClassName(final String packageName) {
        return packageName + "." + BindingMapping.MODULE_INFO_CLASS_NAME;
    }

    /**
     *
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
     *
     * Checks if supplied method is callback for notifications.
     *
     * @param method
     * @return true if method is notification callback.
     */
    public static boolean isNotificationCallback(final Method method) {
        checkArgument(method != null);
        if (method.getName().startsWith("on") && method.getParameterTypes().length == 1) {
            Class<?> potentialNotification = method.getParameterTypes()[0];
            if (isNotification(potentialNotification)
                    && method.getName().equals("on" + potentialNotification.getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * Checks is supplied class is Notification.
     *
     * @param potentialNotification
     * @return
     */
    public static boolean isNotification(final Class<?> potentialNotification) {
        checkArgument(potentialNotification != null, "potentialNotification must not be null.");
        return Notification.class.isAssignableFrom(potentialNotification);
    }

    /**
     *
     * Loads {@link YangModuleInfo} infos available on current classloader.
     *
     * This method is shorthand for {@link #loadModuleInfos(ClassLoader)} with
     * {@link Thread#getContextClassLoader()} for current thread.
     *
     * @return Set of {@link YangModuleInfo} available for current classloader.
     */
    public static ImmutableSet<YangModuleInfo> loadModuleInfos() {
        return loadModuleInfos(Thread.currentThread().getContextClassLoader());
    }

    /**
     *
     * Loads {@link YangModuleInfo} infos available on supplied classloader.
     *
     * {@link YangModuleInfo} are discovered using {@link ServiceLoader} for
     * {@link YangModelBindingProvider}. {@link YangModelBindingProvider} are
     * simple classes which holds only pointers to actual instance
     * {@link YangModuleInfo}.
     *
     * When {@link YangModuleInfo} is available, all dependencies are
     * recursivelly collected into returning set by collecting results of
     * {@link YangModuleInfo#getImportedModules()}.
     *
     *
     * @param loader
     *            Classloader for which {@link YangModuleInfo} should be
     *            retrieved.
     * @return Set of {@link YangModuleInfo} available for supplied classloader.
     */
    public static ImmutableSet<YangModuleInfo> loadModuleInfos(final ClassLoader loader) {
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

    private static void collectYangModuleInfo(final YangModuleInfo moduleInfo,
            final Builder<YangModuleInfo> moduleInfoSet) {
        moduleInfoSet.add(moduleInfo);
        for (YangModuleInfo dependency : moduleInfo.getImportedModules()) {
            collectYangModuleInfo(dependency, moduleInfoSet);
        }
    }

    /**
     *
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
     *
     * Scans supplied class and returns an iterable of all data children
     * classes.
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
            Optional<Class<? extends DataContainer>> entity = getYangModeledReturnType(method);
            if (entity.isPresent()) {
                ret.add((Class<? extends DataObject>) entity.get());
            }
        }
        return ret;
    }

    /**
     *
     * Scans supplied class and returns an iterable of all data children
     * classes.
     *
     * @param type
     *            YANG Modeled Entity derived from DataContainer
     * @return Iterable of all data children, which have YANG modeled entity
     */
    public static Map<Class<?>, Method> getChildrenClassToMethod(final Class<?> type) {
        checkArgument(type != null, "Target type must not be null");
        checkArgument(DataContainer.class.isAssignableFrom(type), "Supplied type must be derived from DataContainer");
        Map<Class<?>, Method> ret = new HashMap<>();
        for (Method method : type.getMethods()) {
            Optional<Class<? extends DataContainer>> entity = getYangModeledReturnType(method);
            if (entity.isPresent()) {
                ret.put(entity.get(), method);
            }
        }
        return ret;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Optional<Class<? extends DataContainer>> getYangModeledReturnType(final Method method) {
        if ("getClass".equals(method.getName()) || !method.getName().startsWith("get")
                || method.getParameterTypes().length > 0) {
            return Optional.absent();
        }

        @SuppressWarnings("rawtypes")
        Class returnType = method.getReturnType();
        if (DataContainer.class.isAssignableFrom(returnType)) {
            return Optional.of(returnType);
        } else if (List.class.isAssignableFrom(returnType)) {
            try {
                return ClassLoaderUtils.withClassLoader(method.getDeclaringClass().getClassLoader(),
                        (Callable<Optional<Class<? extends DataContainer>>>) () -> {
                            Type listResult = ClassLoaderUtils.getFirstGenericParameter(method.getGenericReturnType());
                            if (listResult instanceof Class
                                    && DataContainer.class.isAssignableFrom((Class) listResult)) {
                                return Optional.of((Class) listResult);
                            }
                            return Optional.absent();
                        });
            } catch (Exception e) {
                /*
                 *
                 * It is safe to log this this exception on debug, since this
                 * method should not fail. Only failures are possible if the
                 * runtime / backing.
                 */
                LOG.debug("Unable to find YANG modeled return type for {}", method, e);
            }
        }
        return Optional.absent();
    }

    private static class ClassToQNameLoader extends CacheLoader<Class<?>, Optional<QName>> {

        @Override
        public Optional<QName> load(@SuppressWarnings("NullableProblems") final Class<?> key) throws Exception {
            return resolveQNameNoCache(key);
        }

        /**
         *
         * Tries to resolve QName for supplied class.
         *
         * Looks up for static field with name from constant {@link BindingMapping#QNAME_STATIC_FIELD_NAME} and returns
         * value if present.
         *
         * If field is not present uses {@link #computeQName(Class)} to compute QName for missing types.
         *
         * @param key
         * @return
         */
        private static Optional<QName> resolveQNameNoCache(final Class<?> key) {
            try {
                Field field = key.getField(BindingMapping.QNAME_STATIC_FIELD_NAME);
                Object obj = field.get(null);
                if (obj instanceof QName) {
                    return Optional.of((QName) obj);
                }

            } catch (NoSuchFieldException e) {
                return Optional.of(computeQName(key));

            } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                /*
                 *
                 * It is safe to log this this exception on debug, since this method
                 * should not fail. Only failures are possible if the runtime /
                 * backing.
                 */
                LOG.debug("Unexpected exception during extracting QName for {}", key, e);
            }
            return Optional.absent();
        }

        /**
         * Computes QName for supplied class
         *
         * Namespace and revision are same as {@link YangModuleInfo} associated with supplied class.
         * <p>
         * If class is
         * <ul>
         * <li>rpc input: local name is "input".
         * <li>rpc output: local name is "output".
         * <li>augmentation: local name is "module name".
         * </ul>
         *
         * There is also fallback, if it is not possible to compute QName using following algorithm returns module
         * QName.
         *
         * FIXME: Extend this algorithm to also provide QName for YANG modeled simple types.
         *
         * @throws IllegalStateException If YangModuleInfo could not be resolved
         * @throws IllegalArgumentException If supplied class was not derived from YANG model.
         *
         */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        private static QName computeQName(final Class key) {
            if (isBindingClass(key)) {
                YangModuleInfo moduleInfo;
                try {
                    moduleInfo = getModuleInfo(key);
                } catch (Exception e) {
                    throw new IllegalStateException("Unable to get QName for " + key
                            + ". YangModuleInfo was not found.", e);
                }
                final QName module = getModuleQName(moduleInfo).intern();
                if (Augmentation.class.isAssignableFrom(key)) {
                    return module;
                } else if (isRpcType(key)) {
                    final String className = key.getSimpleName();
                    if (className.endsWith(BindingMapping.RPC_OUTPUT_SUFFIX)) {
                        return QName.create(module, "output").intern();
                    } else {
                        return QName.create(module, "input").intern();
                    }
                }
                /*
                 * Fallback for Binding types which do not have QNAME field
                 */
                return module;
            } else {
                throw new IllegalArgumentException("Supplied class " + key + "is not derived from YANG.");
            }
        }

    }

    /**
     * Given a {@link YangModuleInfo}, create a QName representing it. The QName
     * is formed by reusing the module's namespace and revision using the
     * module's name as the QName's local name.
     *
     * @param moduleInfo
     *            module information
     * @return QName representing the module
     */
    public static QName getModuleQName(final YangModuleInfo moduleInfo) {
        checkArgument(moduleInfo != null, "moduleInfo must not be null.");
        return QName.create(moduleInfo.getNamespace(), moduleInfo.getRevision(), moduleInfo.getName());
    }

    /**
     * Extracts augmentation from Binding DTO field using reflection
     *
     * @param input
     *            Instance of DataObject which is augmentable and may contain
     *            augmentation
     * @return Map of augmentations if read was successful, otherwise empty map.
     */
    public static Map<Class<? extends Augmentation<?>>, Augmentation<?>> getAugmentations(final Augmentable<?> input) {
        return AugmentationFieldGetter.getGetter(input.getClass()).getAugmentations(input);
    }

    /**
     *
     * Determines if two augmentation classes or case classes represents same
     * data.
     * <p>
     * Two augmentations or cases could be substituted only if and if:
     * <ul>
     * <li>Both implements same interfaces</li>
     * <li>Both have same children</li>
     * <li>If augmentations: Both have same augmentation target class. Target
     * class was generated for data node in grouping.</li>
     * <li>If cases: Both are from same choice. Choice class was generated for
     * data node in grouping.</li>
     * </ul>
     * <p>
     * <b>Explanation:</b> Binding Specification reuses classes generated for
     * groupings as part of normal data tree, this classes from grouping could
     * be used at various locations and user may not be aware of it and may use
     * incorrect case or augmentation in particular subtree (via copy
     * constructors, etc).
     *
     * @param potential
     *            Class which is potential substition
     * @param target
     *            Class which should be used at particular subtree
     * @return true if and only if classes represents same data.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static boolean isSubstitutionFor(final Class potential, final Class target) {
        HashSet<Class> subImplemented = Sets.newHashSet(potential.getInterfaces());
        HashSet<Class> targetImplemented = Sets.newHashSet(target.getInterfaces());
        if (!subImplemented.equals(targetImplemented)) {
            return false;
        }
        if (Augmentation.class.isAssignableFrom(potential)
                && !BindingReflections.findAugmentationTarget(potential).equals(
                        BindingReflections.findAugmentationTarget(target))) {
            return false;
        }
        for (Method potentialMethod : potential.getMethods()) {
            try {
                Method targetMethod = target.getMethod(potentialMethod.getName(), potentialMethod.getParameterTypes());
                if (!potentialMethod.getReturnType().equals(targetMethod.getReturnType())) {
                    return false;
                }
            } catch (NoSuchMethodException e) {
                // Counterpart method is missing, so classes could not be
                // substituted.
                return false;
            } catch (SecurityException e) {
                throw new IllegalStateException("Could not compare methods", e);
            }
        }
        return true;
    }
}
