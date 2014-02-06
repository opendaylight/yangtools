/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import java.beans.MethodDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendaylight.yangtools.concepts.util.ClassLoaderUtils;
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
import org.opendaylight.yangtools.yang.binding.annotations.ModuleQName;
import org.opendaylight.yangtools.yang.common.QName;

import static com.google.common.base.Preconditions.*;
import static org.opendaylight.yangtools.concepts.util.ClassLoaderUtils.*;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;

public class BindingReflections {

    private static final long EXPIRATION_TIME = 60;
    private static final String ROOT_PACKAGE_PATTERN_STRING = "(org.opendaylight.yang.gen.v1.[a-z0-9\\.]*.rev[0-9][0-9][0-1][0-9][0-3][0-9])";
    private static final Pattern ROOT_PACKAGE_PATTERN = Pattern.compile(ROOT_PACKAGE_PATTERN_STRING);

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
    public static Class<?> findHierarchicalParent(DataObject childClass) {
        if (childClass instanceof ChildOf) {
            return ClassLoaderUtils.findFirstGenericArgument(childClass.getImplementedInterface(), ChildOf.class);
        }
        return null;
    }

    public static final QName findQName(Class<?> dataType) {
        return classToQName.getUnchecked(dataType).orNull();
    }

    private static class ClassToQNameLoader extends CacheLoader<Class<?>, Optional<QName>> {

        @Override
        public Optional<QName> load(Class<?> key) throws Exception {
            try {
                Field field = key.getField(BindingMapping.QNAME_STATIC_FIELD_NAME);
                Object obj = field.get(null);
                if (obj instanceof QName) {
                    return Optional.of((QName) obj);
                }
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                // NOOP
            }
            return Optional.absent();
        }
    }

    public static boolean isRpcMethod(Method possibleMethod) {
        return possibleMethod != null && RpcService.class.isAssignableFrom(possibleMethod.getDeclaringClass())
                && Future.class.isAssignableFrom(possibleMethod.getReturnType())
                && possibleMethod.getParameterTypes().length <= 1;
    }

    @SuppressWarnings("rawtypes")
    public static Optional<Class<?>> resolveRpcOutputClass(Method targetMethod) {
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
    public static Optional<Class<? extends DataContainer>> resolveRpcInputClass(Method targetMethod) {
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

    public static QName getQName(Class<? extends BaseIdentity> context) {
        return findQName(context);
    }

    public static boolean isAugmentationChild(Class<?> clazz) {
        // FIXME: Current resolver could be still confused when
        // child node was added by grouping
        checkArgument(clazz != null);
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Class<?> parent = findHierarchicalParent((Class) clazz);
        String clazzModelPackage = getModelRootPackageName(clazz.getPackage());
        String parentModelPackage = getModelRootPackageName(parent.getPackage());

        return !clazzModelPackage.equals(parentModelPackage);
    }

    public static String getModelRootPackageName(Package pkg) {
        return getModelRootPackageName(pkg.getName());
    }

    public static String getModelRootPackageName(String name) {
        checkArgument(name != null, "Package name should not be null.");
        checkArgument(name.startsWith(BindingMapping.PACKAGE_PREFIX));
        Matcher match = ROOT_PACKAGE_PATTERN.matcher(name);
        checkArgument(match.find());
        String rootPackage = match.group(0);
        return rootPackage;
    }

    public static YangModuleInfo getModuleInfo(final Class<?> cls) throws Exception {
        checkArgument(cls != null);
        String packageName = getModelRootPackageName(cls.getPackage());
        final String potentialClassName = getModuleInfoClassName(packageName);
        return withClassLoader(cls.getClassLoader(), new Callable<YangModuleInfo>() {

            @Override
            public YangModuleInfo call() throws Exception {
                Class<?> moduleInfoClass = Thread.currentThread().getContextClassLoader().loadClass(potentialClassName);
                return (YangModuleInfo) moduleInfoClass.getMethod("getInstance").invoke(null);
            }
        });
    }

    public static String getModuleInfoClassName(String packageName) {
        return packageName + "." + BindingMapping.MODULE_INFO_CLASS_NAME;
    }

    public static boolean isBindingClass(Class<?> cls) {
        if (DataContainer.class.isAssignableFrom(cls) || Augmentation.class.isAssignableFrom(cls)) {
            return true;
        }
        return (cls.getName().startsWith(BindingMapping.PACKAGE_PREFIX));
    }

    public static boolean isNotificationCallback(Method method) {
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

    public static boolean isNotification(Class<?> potentialNotification) {
        checkArgument(potentialNotification != null);
        return Notification.class.isAssignableFrom(potentialNotification);
    }
    
    public static ImmutableSet<YangModuleInfo> loadModuleInfos() {
        return loadModuleInfos(Thread.currentThread().getContextClassLoader());
    }
    
    public static ImmutableSet<YangModuleInfo> loadModuleInfos(ClassLoader loader) {
        Builder<YangModuleInfo> moduleInfoSet = ImmutableSet.<YangModuleInfo>builder();
        ServiceLoader<YangModelBindingProvider> serviceLoader = ServiceLoader.load(YangModelBindingProvider.class, loader);
        for(YangModelBindingProvider bindingProvider : serviceLoader) {
            collectYangModuleInfo(bindingProvider.getModuleInfo(),moduleInfoSet);
        }
        return  moduleInfoSet.build();
    }

    private static void collectYangModuleInfo(YangModuleInfo moduleInfo, Builder<YangModuleInfo> moduleInfoSet) {
        moduleInfoSet.add(moduleInfo);
        for(YangModuleInfo dependency : moduleInfo.getImportedModules()) {
            collectYangModuleInfo(dependency, moduleInfoSet);
        }
    }

}
