/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import com.google.common.base.Optional;
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

public class DataObjectReadingUtil {

    private DataObjectReadingUtil() {
        throw new UnsupportedOperationException("Utility class. Instantion is not allowed.");
    }

    /**
     *
     * @param parent
     *            Parent object on which read operation will be performed
     * @param parentPath
     *            Path, to parent object.
     * @param childPath
     *            Path, which is nested to parent, and should be readed.
     * @return Value of object.
     */
    public static final <T extends DataObject, P extends DataObject> Map<InstanceIdentifier<T>, T> readData(final P parent,
            final InstanceIdentifier<P> parentPath, final InstanceIdentifier<T> childPath) {
        checkArgument(parent != null, "Parent must not be null.");
        checkArgument(parentPath != null, "Parent path must not be null");
        checkArgument(childPath != null, "Child path must not be null");
        checkArgument(parentPath.containsWildcarded(childPath), "Parent object must be parent of child.");

        List<PathArgument> pathArgs = subList(parentPath.getPathArguments(), childPath.getPathArguments());
        @SuppressWarnings("rawtypes")
        Map<InstanceIdentifier, DataContainer> lastFound = Collections
                .<InstanceIdentifier, DataContainer> singletonMap(parentPath, parent);
        for (PathArgument pathArgument : pathArgs) {
            @SuppressWarnings("rawtypes")
            final ImmutableMap.Builder<InstanceIdentifier, DataContainer> potentialBuilder = ImmutableMap.builder();
            for (@SuppressWarnings("rawtypes")
            Entry<InstanceIdentifier, DataContainer> entry : lastFound.entrySet()) {
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

    public static final <T extends DataObject> Optional<T> readData(final DataObject source, final Class<T> child) {
        checkArgument(source != null, "Object should not be null.");
        checkArgument(child != null, "Child type should not be null");
        Class<? extends DataContainer> parentClass = source.getImplementedInterface();

        @SuppressWarnings("unchecked")
        T potential = (T) resolveReadStrategy(parentClass, child).read(source, child);
        return Optional.fromNullable(potential);
    }

    @SuppressWarnings("rawtypes")
    private static final Map<InstanceIdentifier, DataContainer> readData(final DataContainer parent,
            final InstanceIdentifier parentPath, final PathArgument child) {
        checkArgument(parent != null, "Object should not be null.");
        checkArgument(child != null, "Child argument should not be null");
        Class<? extends DataContainer> parentClass = parent.getImplementedInterface();
        return resolveReadStrategy(parentClass, child.getType()).readUsingPathArgument(parent, child, parentPath);
    }

    private static DataObjectReadingStrategy resolveReadStrategy(final Class<? extends DataContainer> parentClass,
            final Class<? extends DataContainer> type) {

        DataObjectReadingStrategy strategy = createReadStrategy(parentClass, type);
        // FIXME: Add caching of strategies
        return strategy;
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

    private static Method resolveGetterMethod(final Class<? extends DataContainer> parent, final Class<?> child) {
        String methodName = "get" + child.getSimpleName();
        try {
            return parent.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (SecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("rawtypes")
    private static abstract class DataObjectReadingStrategy {

        private final Class<? extends DataContainer> parentType;
        private final Class<? extends DataContainer> childType;
        private final Method getterMethod;

        @SuppressWarnings("unchecked")
        public DataObjectReadingStrategy(final Class parentType, final Class childType) {
            super();
            checkArgument(DataContainer.class.isAssignableFrom(parentType));
            checkArgument(DataContainer.class.isAssignableFrom(childType));
            this.parentType = parentType;
            this.childType = childType;
            this.getterMethod = resolveGetterMethod(parentType, childType);
        }

        @SuppressWarnings("unchecked")
        public DataObjectReadingStrategy(final Class parentType, final Class childType, final Method getter) {
            this.parentType = parentType;
            this.childType = childType;
            this.getterMethod = getter;
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

        public abstract DataContainer read(DataContainer parent, Class<?> childType);

    }

    @SuppressWarnings("rawtypes")
    private static class ContainerReadingStrategy extends DataObjectReadingStrategy {

        public ContainerReadingStrategy(final Class<? extends DataContainer> parent, final Class<? extends DataContainer> child) {
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
        public DataContainer read(final DataContainer parent, final Class<?> childType) {
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
    private static class ListItemReadingStrategy extends DataObjectReadingStrategy {

        public ListItemReadingStrategy(final Class<? extends DataContainer> parent, final Class child) {
            super(parent, child);
            checkArgument(Iterable.class.isAssignableFrom(getGetterMethod().getReturnType()));
        }

        @Override
        public DataContainer read(final DataContainer parent, final Class<?> childType) {
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
                    if (childArgument instanceof IdentifiableItem<?, ?>) {
                        return readUsingIdentifiableItem(dataList, (IdentifiableItem) childArgument, builder);
                    } else {
                        return readAll(dataList, builder);
                    }
                }
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(e);
            }
            return Collections.emptyMap();
        }

        private Map<InstanceIdentifier, DataContainer> readAll(final Iterable<Identifiable> dataList,
                final InstanceIdentifier parentPath) {
            Builder<InstanceIdentifier, DataContainer> result = ImmutableMap
                    .<InstanceIdentifier, DataContainer> builder();
            for (Identifiable item : dataList) {
                @SuppressWarnings("unchecked")
                InstanceIdentifier childPath = parentPath.child(getChildType(), item.getKey());
                result.put(childPath, (DataContainer) item);
            }
            return result.build();
        }

        @SuppressWarnings("unchecked")
        private Map<InstanceIdentifier, DataContainer> readUsingIdentifiableItem(final Iterable<Identifiable> dataList,
                final IdentifiableItem childArgument, final InstanceIdentifier parentPath) {
            final Identifier<?> key = childArgument.getKey();
            for (Identifiable item : dataList) {
                if (key.equals(item.getKey()) && item instanceof DataContainer) {
                    checkState(childArgument.getType().isInstance(item),
                            "Found child is not instance of requested type");
                    InstanceIdentifier childPath = parentPath
                            .child(childArgument.getType(), item.getKey());
                    return Collections.singletonMap(childPath, (DataContainer) item);
                }
            }
            return Collections.emptyMap();
        }

    }

    private static final DataObjectReadingStrategy REAUSABLE_AUGMENTATION_READING_STRATEGY = new AugmentationReadingStrategy();

    private static final class AugmentationReadingStrategy extends DataObjectReadingStrategy {

        public AugmentationReadingStrategy() {
            super(Augmentable.class, Augmentation.class, null);
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Map<InstanceIdentifier, DataContainer> readUsingPathArgument(final DataContainer parent,
                final PathArgument childArgument, final InstanceIdentifier builder) {
            checkArgument(childArgument instanceof Item<?>, "Path Argument must be Item without keys");
            DataContainer aug = read(parent, childArgument.getType());
            if (aug != null) {
                @SuppressWarnings("unchecked")
                final InstanceIdentifier childPath = builder.child(childArgument.getType());
                return Collections.singletonMap(childPath, aug);
            } else {
                return Collections.emptyMap();
            }
        }

        @Override
        public DataContainer read(final DataContainer parent, final Class<?> childType) {
            checkArgument(Augmentation.class.isAssignableFrom(childType), "Parent must be Augmentable.");
            checkArgument(parent instanceof Augmentable<?>, "Parent must be Augmentable.");

            @SuppressWarnings({ "rawtypes", "unchecked" })
            Augmentation potential = ((Augmentable) parent).getAugmentation(childType);
            checkState(potential instanceof DataContainer, "Readed augmention must be data object");
            return (DataContainer) potential;
        }
    }

    /**
     * Create sublist view of child from element on [size-of-parent] position to
     * last element.
     *
     * @param parent
     * @param child
     * @return sublist view of child argument
     * @throws IllegalArgumentException
     *             if parent argument is bigger than child
     */
    private static <P, C> List<C> subList(final Iterable<P> parent, final Iterable<C> child) {
        Iterator<P> iParent = parent.iterator();
        List<C> result = new ArrayList<>();
        for (C arg : child) {
            if (iParent.hasNext()) {
                iParent.next();
            } else {
                result.add(arg);
            }
        }
        if (iParent.hasNext()) {
            throw new IllegalArgumentException("Parent argument is bigger than child.");
        }
        return result;
    }

}
