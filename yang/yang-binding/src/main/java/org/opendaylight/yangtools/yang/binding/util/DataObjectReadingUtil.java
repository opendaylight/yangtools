package org.opendaylight.yangtools.yang.binding.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;

import com.google.common.base.Optional;

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
    public static final <T extends DataObject, P extends DataObject> Map<InstanceIdentifier<T>,T> readData(P parent,
            InstanceIdentifier<P> parentPath, InstanceIdentifier<T> childPath) {
        checkArgument(parent != null, "Parent must not be null.");
        checkArgument(parentPath != null, "Parent path must not be null");
        checkArgument(childPath != null, "Child path must not be null");
        checkArgument(parentPath.containsWildcarded(childPath), "Parent object must be parent of child.");

        @SuppressWarnings("rawtypes")
        InstanceIdentifierBuilder newPath = InstanceIdentifier.builder(parentPath);
        final int commonOffset = parentPath.getPath().size();
        final int lastIndex = childPath.getPath().size();
        List<PathArgument> pathArgs = childPath.getPath().subList(commonOffset, lastIndex);
        DataContainer lastFound = parent;
        for (PathArgument pathArgument : pathArgs) {
            DataContainer potential = readDataOrNull(lastFound, pathArgument,newPath);
            if (potential == null) {
                return Collections.emptyMap();
            }
            lastFound = potential;
        }
        @SuppressWarnings("unchecked")
        T result = (T) lastFound;
        @SuppressWarnings("unchecked")
        InstanceIdentifier<T> resultPath = newPath.build();

        return Collections.singletonMap(resultPath, result);
    }

    public static final <T extends DataObject> Optional<T> readData(DataObject source, Class<T> child) {
        checkArgument(source != null, "Object should not be null.");
        checkArgument(child != null, "Child type should not be null");
        Class<? extends DataContainer> parentClass = source.getImplementedInterface();

        @SuppressWarnings("unchecked")
        T potential = (T) resolveReadStrategy(parentClass, child).read(source, child);
        return Optional.fromNullable(potential);
    }

    private static final DataContainer readDataOrNull(DataContainer parent, PathArgument childArgument,@SuppressWarnings("rawtypes") InstanceIdentifierBuilder targetBuilder) {
        checkArgument(parent != null, "Object should not be null.");
        checkArgument(childArgument != null, "Child argument should not be null");
        Class<? extends DataContainer> parentClass = parent.getImplementedInterface();

        return resolveReadStrategy(parentClass, childArgument.getType()).readUsingPathArgument(parent, childArgument,targetBuilder);
    }

    private static DataObjectReadingStrategy resolveReadStrategy(Class<? extends DataContainer> parentClass,
            Class<? extends DataContainer> type) {

        DataObjectReadingStrategy strategy = createReadStrategy(parentClass, type);
        // FIXME: Add caching of strategies
        return strategy;
    }

    private static DataObjectReadingStrategy createReadStrategy(Class<? extends DataContainer> parent,
            Class<? extends DataContainer> child) {

        if (Augmentable.class.isAssignableFrom(parent) && Augmentation.class.isAssignableFrom(child)) {
            return REAUSABLE_AUGMENTATION_READING_STRATEGY;
        }

        /*
         * FIXME Ensure that this strategies also works for children of cases.
         * Possible edge-case is :
         * Parent container uses grouping foo case is
         * added by augmentation also uses foo.
         */
        if (Identifiable.class.isAssignableFrom(child)) {
            @SuppressWarnings("unchecked")
            final Class<? extends Identifiable<?>> identifiableClass = (Class<? extends Identifiable<?>>) child;
            return new ListItemReadingStrategy(parent, identifiableClass);
        }
        return new ContainerReadingStrategy(parent, child);
    }

    private static Method getGetterMethod(Class<? extends DataContainer> parent, Class<?> child) {
        String methodName = "get" + child.getSimpleName();
        try {
            return parent.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (SecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static abstract class DataObjectReadingStrategy {

        abstract public DataContainer readUsingPathArgument(DataContainer parent, PathArgument childArgument, @SuppressWarnings("rawtypes") InstanceIdentifierBuilder targetBuilder);

        abstract public DataContainer read(DataContainer parent, Class<?> childType);

    }

    private static class ContainerReadingStrategy extends DataObjectReadingStrategy {

        private final Method getterMethod;

        public ContainerReadingStrategy(Class<? extends DataContainer> parent, Class<? extends DataContainer> child) {
            this.getterMethod = getGetterMethod(parent, child);
            checkArgument(child.isAssignableFrom(getterMethod.getReturnType()));
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public DataContainer readUsingPathArgument(DataContainer parent, PathArgument childArgument,InstanceIdentifierBuilder builder) {
            final DataContainer result = read(parent, childArgument.getType());
            builder.child(childArgument.getType());
            return result;
        }

        @Override
        public DataContainer read(DataContainer parent, Class<?> childType) {
            try {
                Object potentialData = getterMethod.invoke(parent);
                checkState(potentialData instanceof DataContainer);
                return (DataContainer) potentialData;

            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalArgumentException(e);
            }
        }

    }

    private static class ListItemReadingStrategy extends DataObjectReadingStrategy {

        private final Method getterMethod;

        public ListItemReadingStrategy(Class<? extends DataContainer> parent, Class<? extends Identifiable<?>> child) {
            Method method = getGetterMethod(parent, child);
            checkArgument(Iterable.class.isAssignableFrom(method.getReturnType()));
            this.getterMethod = method;
        }

        @Override
        public DataContainer read(DataContainer parent, Class<?> childType) {
            // This will always fail since we do not have key.
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public DataContainer readUsingPathArgument(DataContainer parent, PathArgument childArgument,@SuppressWarnings("rawtypes") InstanceIdentifierBuilder builder) {
            checkArgument(childArgument instanceof IdentifiableItem<?, ?>);
            @SuppressWarnings("rawtypes")
            Identifier key = ((IdentifiableItem) childArgument).getKey();
            try {
                Object potentialList = getterMethod.invoke(parent);
                if (potentialList instanceof Iterable) {
                    @SuppressWarnings("rawtypes")
                    Iterable<Identifiable> dataList = (Iterable<Identifiable>) potentialList;
                    for (@SuppressWarnings("rawtypes")
                    Identifiable item : dataList) {
                        if (key.equals(item.getKey()) && item instanceof DataContainer) {
                            checkState(childArgument.getType().isInstance(item),
                                    "Found child is not instance of requested type");
                            builder.child(childArgument.getType(), key);
                            return (DataContainer) item;
                        }
                    }
                }
            } catch (InvocationTargetException e) {
                throw new IllegalStateException(e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(e);
            }
            return null;
        }

    }

    private static final DataObjectReadingStrategy REAUSABLE_AUGMENTATION_READING_STRATEGY = new AugmentationReadingStrategy();

    private static final class AugmentationReadingStrategy extends DataObjectReadingStrategy {

        @SuppressWarnings("unchecked")
        @Override
        public DataContainer readUsingPathArgument(DataContainer parent, PathArgument childArgument,@SuppressWarnings("rawtypes") InstanceIdentifierBuilder builder) {
            checkArgument(childArgument instanceof Item<?>, "Path Argument must be Item without keys");
            builder.augmentation(childArgument.getType());
            return read(parent, childArgument.getType());
        }

        @Override
        public DataContainer read(DataContainer parent, Class<?> childType) {
            checkArgument(Augmentation.class.isAssignableFrom(childType), "Parent must be Augmentable.");
            checkArgument(parent instanceof Augmentable<?>, "Parent must be Augmentable.");

            @SuppressWarnings({ "rawtypes", "unchecked" })
            Augmentation potential = ((Augmentable) parent).getAugmentation(childType);
            checkState(potential instanceof DataContainer, "Readed augmention must be data object");
            return (DataContainer) potential;
        }
    }
}
