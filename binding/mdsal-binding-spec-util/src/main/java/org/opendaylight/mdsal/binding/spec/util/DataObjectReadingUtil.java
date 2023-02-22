/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.spec.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.binding.contract.Naming;

@Deprecated(since = "11.0.3", forRemoval = true)
public final class DataObjectReadingUtil {
    private static final DataObjectReadingStrategy REAUSABLE_AUGMENTATION_READING_STRATEGY =
            new AugmentationReadingStrategy();

    private DataObjectReadingUtil() {
        // Hidden on purpose
    }

    /**
     * Read data from parent at specified path.
     *
     * @param parent
     *            Parent object on which read operation will be performed
     * @param parentPath
     *            Path, to parent object.
     * @param childPath
     *            Path, which is nested to parent, and should be read.
     * @return Value of object.
     */
    public static <T extends DataObject, P extends DataObject> Map<InstanceIdentifier<T>, T> readData(
            final P parent, final InstanceIdentifier<P> parentPath, final InstanceIdentifier<T> childPath) {
        checkArgument(parent != null, "Parent must not be null.");
        checkArgument(parentPath != null, "Parent path must not be null");
        checkArgument(childPath != null, "Child path must not be null");
        checkArgument(parentPath.containsWildcarded(childPath), "Parent object must be parent of child.");

        List<PathArgument> pathArgs = subList(parentPath.getPathArguments(), childPath.getPathArguments());
        @SuppressWarnings("rawtypes")
        Map<InstanceIdentifier, DataContainer> lastFound = Collections.singletonMap(parentPath, parent);
        for (PathArgument pathArgument : pathArgs) {
            @SuppressWarnings("rawtypes")
            final ImmutableMap.Builder<InstanceIdentifier, DataContainer> potentialBuilder = ImmutableMap.builder();
            for (@SuppressWarnings("rawtypes") Entry<InstanceIdentifier, DataContainer> entry : lastFound.entrySet()) {
                potentialBuilder.putAll(readData(entry, pathArgument));
            }
            lastFound = potentialBuilder.build();
            if (lastFound.isEmpty()) {
                return Collections.emptyMap();
            }
        }
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Map<InstanceIdentifier<T>, T> result = (Map) lastFound;
        return result;
    }

    @SuppressWarnings("rawtypes")
    private static Map<InstanceIdentifier, DataContainer> readData(final Entry<InstanceIdentifier, DataContainer> entry,
            final PathArgument pathArgument) {
        return readData(entry.getValue(), entry.getKey(), pathArgument);
    }

    public static <T extends DataObject> Optional<T> readData(final DataObject source, final Class<T> child) {
        checkArgument(source != null, "Object should not be null.");
        checkArgument(child != null, "Child type should not be null");
        Class<? extends DataContainer> parentClass = source.implementedInterface();

        @SuppressWarnings("unchecked")
        T potential = (T) resolveReadStrategy(parentClass, child).read(source, child);
        return Optional.ofNullable(potential);
    }

    @SuppressWarnings("rawtypes")
    private static Map<InstanceIdentifier, DataContainer> readData(final DataContainer parent,
            final InstanceIdentifier parentPath, final PathArgument child) {
        checkArgument(parent != null, "Object should not be null.");
        checkArgument(child != null, "Child argument should not be null");
        Class<? extends DataContainer> parentClass = parent.implementedInterface();
        return resolveReadStrategy(parentClass, child.getType()).readUsingPathArgument(parent, child, parentPath);
    }

    private static DataObjectReadingStrategy resolveReadStrategy(final Class<? extends DataContainer> parentClass,
            final Class<? extends DataContainer> type) {

        // FIXME: Add caching of strategies
        return createReadStrategy(parentClass, type);
    }

    private static DataObjectReadingStrategy createReadStrategy(final Class<? extends DataContainer> parent,
            final Class<? extends DataContainer> child) {

        if (Augmentable.class.isAssignableFrom(parent) && Augmentation.class.isAssignableFrom(child)) {
            return REAUSABLE_AUGMENTATION_READING_STRATEGY;
        }

        /*
         * FIXME Ensure that this strategies also works for children of cases.
         * Possible edge-case is : Parent container uses grouping foo case is
         * added by augmentation also uses foo.
         */
        if (Identifiable.class.isAssignableFrom(child)) {
            @SuppressWarnings("unchecked")
            final Class<? extends Identifiable<?>> identifiableClass = (Class<? extends Identifiable<?>>) child;
            return new ListItemReadingStrategy(parent, identifiableClass);
        }
        return new ContainerReadingStrategy(parent, child);
    }

    @SuppressWarnings("rawtypes")
    private abstract static class DataObjectReadingStrategy {

        private final Class<? extends DataContainer> parentType;
        private final Class<? extends DataContainer> childType;
        private final Method getterMethod;

        @SuppressWarnings("unchecked")
        DataObjectReadingStrategy(final Class parentType, final Class childType) {
            checkArgument(DataContainer.class.isAssignableFrom(parentType));
            checkArgument(DataContainer.class.isAssignableFrom(childType));
            this.parentType = parentType;
            this.childType = childType;
            getterMethod = resolveGetterMethod(parentType, childType);
        }

        @SuppressWarnings("unchecked")
        DataObjectReadingStrategy(final Class parentType, final Class childType, final Method getter) {
            this.parentType = parentType;
            this.childType = childType;
            getterMethod = getter;
        }

        @SuppressWarnings("unused")
        protected Class<? extends DataContainer> getParentType() {
            return parentType;
        }

        protected Class<? extends DataContainer> getChildType() {
            return childType;
        }

        protected Method getGetterMethod() {
            return getterMethod;
        }

        public abstract Map<InstanceIdentifier, DataContainer> readUsingPathArgument(DataContainer parent,
                PathArgument childArgument, InstanceIdentifier targetBuilder);

        public abstract DataContainer read(DataContainer parent, Class<?> child);

        private static Method resolveGetterMethod(final Class<? extends DataContainer> parent, final Class<?> child) {
            String methodName = Naming.GETTER_PREFIX + child.getSimpleName();
            try {
                return parent.getMethod(methodName);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(e);
            } catch (SecurityException e) {
                throw new IllegalStateException(e);
            }
        }

    }

    @SuppressWarnings("rawtypes")
    private static final class ContainerReadingStrategy extends DataObjectReadingStrategy {
        ContainerReadingStrategy(final Class<? extends DataContainer> parent,
                final Class<? extends DataContainer> child) {
            super(parent, child);
            checkArgument(child.isAssignableFrom(getGetterMethod().getReturnType()));
        }

        @Override
        public Map<InstanceIdentifier, DataContainer> readUsingPathArgument(final DataContainer parent,
                final PathArgument childArgument, final InstanceIdentifier parentPath) {
            final DataContainer result = read(parent, childArgument.getType());
            if (result != null) {
                @SuppressWarnings("unchecked")
                InstanceIdentifier childPath = parentPath.child(childArgument.getType());
                return Collections.singletonMap(childPath, result);
            }
            return Collections.emptyMap();
        }

        @Override
        public DataContainer read(final DataContainer parent, final Class<?> child) {
            try {
                Object potentialData = getGetterMethod().invoke(parent);
                checkState(potentialData instanceof DataContainer);
                return (DataContainer) potentialData;

            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static final class ListItemReadingStrategy extends DataObjectReadingStrategy {
        ListItemReadingStrategy(final Class<? extends DataContainer> parent, final Class child) {
            super(parent, child);
            checkArgument(Iterable.class.isAssignableFrom(getGetterMethod().getReturnType()));
        }

        @Override
        public DataContainer read(final DataContainer parent, final Class<?> child) {
            // This will always fail since we do not have key.
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Map<InstanceIdentifier, DataContainer> readUsingPathArgument(final DataContainer parent,
                final PathArgument childArgument, final InstanceIdentifier builder) {
            try {
                Object potentialList = getGetterMethod().invoke(parent);
                if (potentialList instanceof Iterable) {
                    final Iterable<Identifiable> dataList = (Iterable<Identifiable>) potentialList;
                    return childArgument instanceof IdentifiableItem
                            ? readUsingIdentifiableItem(dataList, (IdentifiableItem) childArgument, builder)
                                    : readAll(dataList, builder);
                }
            } catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            return Collections.emptyMap();
        }

        private Map<InstanceIdentifier, DataContainer> readAll(final Iterable<Identifiable> dataList,
                final InstanceIdentifier parentPath) {
            Builder<InstanceIdentifier, DataContainer> result = ImmutableMap.builder();
            for (Identifiable item : dataList) {
                @SuppressWarnings("unchecked")
                InstanceIdentifier childPath = parentPath.child(getChildType(), item.key());
                result.put(childPath, (DataContainer) item);
            }
            return result.build();
        }

        @SuppressWarnings("unchecked")
        private static Map<InstanceIdentifier, DataContainer> readUsingIdentifiableItem(
                final Iterable<Identifiable> dataList, final IdentifiableItem childArgument,
                final InstanceIdentifier parentPath) {
            final Identifier<?> key = childArgument.getKey();
            for (Identifiable item : dataList) {
                if (key.equals(item.key()) && item instanceof DataContainer) {
                    checkState(childArgument.getType().isInstance(item),
                            "Found child is not instance of requested type");
                    InstanceIdentifier childPath = parentPath
                            .child(childArgument.getType(), item.key());
                    return Collections.singletonMap(childPath, (DataContainer) item);
                }
            }
            return Collections.emptyMap();
        }

    }

    private static final class AugmentationReadingStrategy extends DataObjectReadingStrategy {
        AugmentationReadingStrategy() {
            super(Augmentable.class, Augmentation.class, null);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Map<InstanceIdentifier, DataContainer> readUsingPathArgument(final DataContainer parent,
                final PathArgument childArgument, final InstanceIdentifier builder) {
            checkArgument(childArgument instanceof Item, "Path Argument must be Item without keys");
            DataContainer aug = read(parent, childArgument.getType());
            if (aug == null) {
                return Collections.emptyMap();
            }

            @SuppressWarnings("unchecked")
            final InstanceIdentifier childPath = builder.child(childArgument.getType());
            return Collections.singletonMap(childPath, aug);
        }

        @Override
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public DataContainer read(final DataContainer parent, final Class<?> child) {
            checkArgument(Augmentation.class.isAssignableFrom(child), "Child must be Augmentation.");
            checkArgument(parent instanceof Augmentable<?>, "Parent must be Augmentable.");
            return ((Augmentable) parent).augmentation(child);
        }
    }

    /**
     * Create sublist view of child from element on [size-of-parent] position to last element.
     *
     * @return sublist view of child argument
     * @throws IllegalArgumentException
     *             if parent argument is bigger than child
     */
    private static <P, C> List<C> subList(final Iterable<P> parent, final Iterable<C> child) {
        Iterator<P> parentIt = parent.iterator();
        List<C> result = new ArrayList<>();
        for (C arg : child) {
            if (parentIt.hasNext()) {
                parentIt.next();
            } else {
                result.add(arg);
            }
        }
        if (parentIt.hasNext()) {
            throw new IllegalArgumentException("Parent argument is bigger than child.");
        }
        return result;
    }
}
