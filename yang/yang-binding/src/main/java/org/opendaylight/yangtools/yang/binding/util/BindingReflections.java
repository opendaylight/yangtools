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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class BindingReflections {

    private static final long EXPIRATION_TIME = 60;
    private static final String ROOT_PACKAGE_PATTERN_STRING = "(org.opendaylight.yang.gen.v1.[a-z0-9_\\.]*\\.rev[0-9][0-9][0-1][0-9][0-3][0-9])";
    private static final Pattern ROOT_PACKAGE_PATTERN = Pattern.compile(ROOT_PACKAGE_PATTERN_STRING);
    private static final Logger LOG = LoggerFactory.getLogger(BindingReflections.class);

    private static final LoadingCache<Class<?>, Optional<QName>> classToQName = CacheBuilder.newBuilder() //
            .weakKeys() //
            .expireAfterAccess(EXPIRATION_TIME, TimeUnit.SECONDS) //
            .build(new ClassToQNameLoader());

    /**
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
     *
     * @param augmentation
     *            {@link Augmentation} subclass for which we want to determine
     *            augmentation target.
     * @return Augmentation target - class which augmentation provides
     *         additional extensions.
     */
    public static Class<?> findHierarchicalParent(final Class<? extends ChildOf<?>> childClass) {
        return ClassLoaderUtils.findFirstGenericArgument(childClass, ChildOf.class);
    }

    /**
     *
     * @param augmentation
     *            {@link Augmentation} subclass for which we want to determine
     *            augmentation target.
     * @return Augmentation target - class which augmentation provides
     *         additional extensions.
     */
    public static Class<?> findHierarchicalParent(final DataObject childClass) {
        if (childClass instanceof ChildOf) {
            return ClassLoaderUtils.findFirstGenericArgument(childClass.getImplementedInterface(), ChildOf.class);
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
     */
    public static final QName findQName(final Class<?> dataType) {
        return classToQName.getUnchecked(dataType).orNull();
    }

    private static class ClassToQNameLoader extends CacheLoader<Class<?>, Optional<QName>> {

        @Override
        public Optional<QName> load(final Class<?> key) throws Exception {
            try {
                Field field = key.getField(BindingMapping.QNAME_STATIC_FIELD_NAME);
                Object obj = field.get(null);
                if (obj instanceof QName) {
                    return Optional.of((QName) obj);
                }
            } catch (NoSuchFieldException e) {
                if (Augmentation.class.isAssignableFrom(key)) {
                    YangModuleInfo moduleInfo = getModuleInfo(key);
                    return Optional.of(QName.create(moduleInfo.getNamespace(), moduleInfo.getRevision(),
                            moduleInfo.getName()));
                }
            } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // NOOP
            }
            return Optional.absent();
        }
    }

    public static boolean isRpcMethod(final Method possibleMethod) {
        return possibleMethod != null && RpcService.class.isAssignableFrom(possibleMethod.getDeclaringClass())
                && Future.class.isAssignableFrom(possibleMethod.getReturnType())
                && possibleMethod.getParameterTypes().length <= 1;
    }

    @SuppressWarnings("rawtypes")
    public static Optional<Class<?>> resolveRpcOutputClass(final Method targetMethod) {
        checkState(isRpcMethod(targetMethod), "Supplied method is not Rpc invocation method");
        Type futureType = targetMethod.getGenericReturnType();
        Type rpcResultType = ClassLoaderUtils.getFirstGenericParameter(futureType);
        Type rpcResultArgument = ClassLoaderUtils.getFirstGenericParameter(rpcResultType);
        if (rpcResultArgument instanceof Class && !Void.class.equals(rpcResultArgument)) {
            return Optional.<Class<?>> of((Class) rpcResultArgument);
        }
        return Optional.absent();
    }

    @SuppressWarnings("unchecked")
    public static Optional<Class<? extends DataContainer>> resolveRpcInputClass(final Method targetMethod) {
        @SuppressWarnings("rawtypes")
        Class[] types = targetMethod.getParameterTypes();
        if (types.length == 0) {
            return Optional.absent();
        }
        if (types.length == 1) {
            return Optional.<Class<? extends DataContainer>> of(types[0]);
        }
        throw new IllegalArgumentException("Method has 2 or more arguments.");
    }

    public static QName getQName(final Class<? extends BaseIdentity> context) {
        return findQName(context);
    }

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

    public static String getModelRootPackageName(final Package pkg) {
        return getModelRootPackageName(pkg.getName());
    }

    public static String getModelRootPackageName(final String name) {
        checkArgument(name != null, "Package name should not be null.");
        checkArgument(name.startsWith(BindingMapping.PACKAGE_PREFIX), "Package name not starting with %s, is: %s",
                BindingMapping.PACKAGE_PREFIX, name);
        Matcher match = ROOT_PACKAGE_PATTERN.matcher(name);
        checkArgument(match.find(), "Package name '%s' does not match required pattern '%s'", name,
                ROOT_PACKAGE_PATTERN_STRING);
        return match.group(0);
    }

    public static YangModuleInfo getModuleInfo(final Class<?> cls) throws Exception {
        checkArgument(cls != null);
        String packageName = getModelRootPackageName(cls.getPackage());
        final String potentialClassName = getModuleInfoClassName(packageName);
        return ClassLoaderUtils.withClassLoader(cls.getClassLoader(), new Callable<YangModuleInfo>() {
            @Override
            public YangModuleInfo call() throws ClassNotFoundException, IllegalAccessException,
                    IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
                Class<?> moduleInfoClass = Thread.currentThread().getContextClassLoader().loadClass(potentialClassName);
                return (YangModuleInfo) moduleInfoClass.getMethod("getInstance").invoke(null);
            }
        });
    }

    public static String getModuleInfoClassName(final String packageName) {
        return packageName + "." + BindingMapping.MODULE_INFO_CLASS_NAME;
    }

    public static boolean isBindingClass(final Class<?> cls) {
        if (DataContainer.class.isAssignableFrom(cls) || Augmentation.class.isAssignableFrom(cls)) {
            return true;
        }
        return (cls.getName().startsWith(BindingMapping.PACKAGE_PREFIX));
    }

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

    public static boolean isNotification(final Class<?> potentialNotification) {
        checkArgument(potentialNotification != null);
        return Notification.class.isAssignableFrom(potentialNotification);
    }

    public static ImmutableSet<YangModuleInfo> loadModuleInfos() {
        return loadModuleInfos(Thread.currentThread().getContextClassLoader());
    }

    public static ImmutableSet<YangModuleInfo> loadModuleInfos(final ClassLoader loader) {
        Builder<YangModuleInfo> moduleInfoSet = ImmutableSet.<YangModuleInfo> builder();
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

    public static boolean isRpcType(final Class<? extends DataObject> targetType) {
        return DataContainer.class.isAssignableFrom(targetType) //
                && !ChildOf.class.isAssignableFrom(targetType) //
                && !Notification.class.isAssignableFrom(targetType) //
                && (targetType.getName().endsWith("Input") || targetType.getName().endsWith("Output"));
    }

    /**
     *
     * Scans supplied class and returns an iterable of all data children classes.
     *
     * @param type YANG Modeled Entity derived from DataContainer
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

    @SuppressWarnings("unchecked")
    private static Optional<Class<? extends DataContainer>> getYangModeledReturnType(final Method method) {
        if (method.getName().equals("getClass") || !method.getName().startsWith("get")
                || method.getParameterTypes().length > 0) {
            return Optional.absent();
        }

        @SuppressWarnings("rawtypes")
        Class returnType = method.getReturnType();
        if (DataContainer.class.isAssignableFrom(returnType)) {
            return Optional.<Class<? extends DataContainer>> of(returnType);
        } else if (List.class.isAssignableFrom(returnType)) {
            try {
                return ClassLoaderUtils.withClassLoader(method.getDeclaringClass().getClassLoader(),
                        new Callable<Optional<Class<? extends DataContainer>>>() {
                            @SuppressWarnings("rawtypes")
                            @Override
                            public Optional<Class<? extends DataContainer>> call() {
                                Type listResult = ClassLoaderUtils.getFirstGenericParameter(method
                                        .getGenericReturnType());
                                if (listResult instanceof Class
                                        && DataContainer.class.isAssignableFrom((Class) listResult)) {
                                    return Optional.<Class<? extends DataContainer>> of((Class) listResult);
                                }
                                return Optional.absent();
                            }

                        });
            } catch (Exception e) {
                LOG.debug("Unable to find YANG modeled return type for {}", method, e);
            }
        }
        return Optional.absent();
    }

}
