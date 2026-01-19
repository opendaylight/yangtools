/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.loader;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Set;
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.AccessControllerCompat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link ClassLoader} hosting types generated for a particular type. A root instance is attached to a particular user
 * a root class loader and should be used to load classes, which are used by a particular user instance. When used
 * correctly, the classes loaded through this instance become eligible for GC when the user instance becomes
 * unreachable.
 *
 * <p>It semantically combines two class loaders: the class loader in which this class is loaded and the class loader in
 * which a target Binding interface/class is loaded. This inherently supports multi-classloader environments -- the root
 * instance has visibility only into codec classes and for each classloader we encounter when presented with a binding
 * class we create a leaf instance and cache it in the root instance. Leaf instances are using the root loader as their
 * parent, but consult the binding class's class loader if the root loader fails to load a particular class.
 *
 * <p>In single-classloader environments, obviously, the root loader can load all binding classes, and hence no leaf
 * loader is created.
 */
public abstract sealed class BindingClassLoader extends ClassLoader
        permits LeafBindingClassLoader, RootBindingClassLoader {
    /**
     * A builder of {@link BindingClassLoader} instances.
     */
    public static final class Builder {
        private final @NonNull ClassLoader parentLoader;

        private @Nullable Path dumpDirectory;

        Builder(final ClassLoader parentLoader) {
            this.parentLoader = requireNonNull(parentLoader);
        }

        public Builder dumpBytecode(final Path toDirectory) {
            dumpDirectory = requireNonNull(toDirectory);
            return this;
        }

        public @NonNull BindingClassLoader build() {
            return AccessControllerCompat.get(() -> new RootBindingClassLoader(parentLoader, dumpDirectory));
        }
    }

    /**
     * A class generator, generating a class of a particular type.
     *
     * @param <T> Type of generated class
     */
    public interface ClassGenerator<T> {
        /**
         * Generate a class.
         *
         * @param fqcn Generated class Fully-qualified class name
         * @param bindingInterface Binding interface for which the class is being generated
         * @return A result.
         */
        GeneratorResult<T> generateClass(BindingClassLoader loader, String fqcn, Class<?> bindingInterface);

        /**
         * Run the specified loader in a customized environment. The environment customizations must be cleaned up by
         * the time this method returns. The default implementation performs no customization.
         *
         * @param loader Class loader to execute
         * @return Class returned by the loader
         */
        default Class<T> customizeLoading(final @NonNull Supplier<Class<T>> loader) {
            return loader.get();
        }
    }

    /**
     * Result of class generation.
     *
     * @param <T> Type of generated class.
     */
    public static final class GeneratorResult<T> {
        private final @NonNull ImmutableSet<Class<?>> dependecies;
        private final @NonNull LoadableClass<T> source;

        GeneratorResult(final LoadableClass<T> source, final ImmutableSet<Class<?>> dependecies) {
            this.source = requireNonNull(source);
            this.dependecies = requireNonNull(dependecies);
        }

        public static <T> @NonNull GeneratorResult<T> of(final LoadableClass<T> source) {
            return new GeneratorResult<>(source, ImmutableSet.of());
        }

        public static <T> @NonNull GeneratorResult<T> of(final LoadableClass<T> source,
                final Collection<Class<?>> dependencies) {
            return dependencies.isEmpty() ? of(source)
                : new GeneratorResult<>(source, ImmutableSet.copyOf(dependencies));
        }

        @NonNull LoadableClass<T> source() {
            return source;
        }

        @NonNull ImmutableSet<Class<?>> getDependencies() {
            return dependecies;
        }
    }

    static {
        verify(ClassLoader.registerAsParallelCapable());
    }

    private static final Logger LOG = LoggerFactory.getLogger(BindingClassLoader.class);

    private final @Nullable Path dumpDir;

    BindingClassLoader(final ClassLoader parentLoader, final @Nullable Path dumpDir) {
        super(parentLoader);
        this.dumpDir = dumpDir;
    }

    BindingClassLoader(final BindingClassLoader parentLoader) {
        this(parentLoader, parentLoader.dumpDir);
    }

    /**
     * Instantiate a new BindingClassLoader, which serves as the root of generated code loading.
     *
     * @param rootClass Class from which to derive the class loader
     * @param dumpDir Directory in which to dump loaded bytecode
     * @return A new BindingClassLoader.
     * @throws NullPointerException if {@code parentLoader} is {@code null}
     * @deprecated Use {@link #builder(Class)} instead
     */
    @Deprecated(since = "14.0.7")
    public static @NonNull BindingClassLoader create(final Class<?> rootClass, final @Nullable File dumpDir) {
        final var builder = builder(rootClass);
        if (dumpDir != null) {
            builder.dumpBytecode(dumpDir.toPath());
        }
        return builder.build();
    }

    public static @NonNull Builder builder(final Class<?> rootClass) {
        return new Builder(rootClass.getClassLoader());
    }

    /**
     * Instantiate a new BindingClassLoader, which serves as the root of generated code loading.
     *
     * @param rootClass Class from which to derive the class loader
     * @return A new BindingClassLoader.
     * @throws NullPointerException if {@code parentLoader} is {@code null}
     */
    public static @NonNull BindingClassLoader ofRootClass(final Class<?> rootClass) {
        return builder(rootClass).build();
    }

    /**
     * Generate a class which is related to specified compile-type-generated interface.
     *
     * @param <T> Type of generated class
     * @param bindingInterface Binding compile-time-generated interface
     * @param fqcn Fully-Qualified Class Name of the generated class
     * @param generator Code generator to run
     * @return A generated class object
     * @throws NullPointerException if any argument is null
     */
    public final <T> @NonNull Class<T> generateClass(final Class<?> bindingInterface, final String fqcn,
            final ClassGenerator<T> generator)  {
        return findClassLoader(requireNonNull(bindingInterface)).doGenerateClass(bindingInterface, fqcn, generator);
    }

    public final @NonNull Class<?> getGeneratedClass(final Class<?> bindingInterface, final String fqcn) {
        final var loader = findClassLoader(requireNonNull(bindingInterface));
        final Class<?> ret;
        synchronized (loader.getClassLoadingLock(fqcn)) {
            ret = loader.findLoadedClass(fqcn);
        }
        if (ret == null) {
            throw new IllegalArgumentException("Failed to find generated class " + fqcn + " for " + bindingInterface);
        }
        return ret;
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        return super.equals(obj);
    }

    @Override
    public final String toString() {
        return addToStringAttributes(MoreObjects.toStringHelper(this).omitNullValues()).toString();
    }

    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("identity", HexFormat.of().toHexDigits(hashCode())).add("parent", getParent());
    }

    /**
     * Append specified loaders to this class loader for the purposes of looking up generated classes. Note that the
     * loaders are expected to have required classes already loaded. This is required to support generation of
     * inter-dependent structures, such as those used for streaming binding interfaces.
     *
     * @param newLoaders Loaders to append
     * @throws NullPointerException if {@code loaders} is null
     */
    abstract void appendLoaders(@NonNull Set<LeafBindingClassLoader> newLoaders);

    /**
     * Find the loader responsible for holding classes related to a binding class.
     *
     * @param bindingClass Class to locate
     * @return a Loader instance
     * @throws NullPointerException if {@code bindingClass} is null
     */
    abstract @NonNull BindingClassLoader findClassLoader(@NonNull Class<?> bindingClass);

    private <T> @NonNull Class<T> doGenerateClass(final Class<?> bindingInterface, final String fqcn,
            final ClassGenerator<T> generator)  {
        synchronized (getClassLoadingLock(fqcn)) {
            // Attempt to find a loaded class
            final var existing = findLoadedClass(fqcn);
            if (existing != null) {
                return (Class<T>) existing;
            }

            final var result = generator.generateClass(this, fqcn, bindingInterface);
            final var source = result.source();
            verify(fqcn.equals(source.name()), "Unexpected class in %s", source);
            dumpBytecode(source);

            processDependencies(result.getDependencies());
            return generator.customizeLoading(() -> source.load(this));
        }
    }

    @NonNullByDefault
    public final Class<?> loadClass(final String fqcn, final byte[] byteCode) {
        synchronized (getClassLoadingLock(fqcn)) {
            final var existing = findLoadedClass(fqcn);
            verify(existing == null, "Attempted to load existing %s", existing);
            return defineClass(fqcn, byteCode, 0, byteCode.length);
        }
    }

    private void processDependencies(final Collection<Class<?>> deps) {
        final var depLoaders = new HashSet<LeafBindingClassLoader>();
        for (var dep : deps) {
            final var depLoader = dep.getClassLoader();
            verify(depLoader instanceof BindingClassLoader, "Dependency %s is not a generated class", dep);
            if (equals(depLoader)) {
                // Same loader, skip
                continue;
            }

            try {
                loadClass(dep.getName());
            } catch (ClassNotFoundException e) {
                LOG.debug("Cannot find {} in local loader, attempting to compensate", dep, e);
                // Root loader is always visible from a leaf, hence the dependency can only be a leaf
                verify(depLoader instanceof LeafBindingClassLoader, "Dependency loader %s is not a leaf", depLoader);
                depLoaders.add((LeafBindingClassLoader) depLoader);
            }
        }

        if (!depLoaders.isEmpty()) {
            appendLoaders(depLoaders);
        }
    }

    private void dumpBytecode(final LoadableClass<?> source) {
        final var dir = dumpDir;
        if (dir != null) {
            try {
                source.saveToDir(dir);
            } catch (IOException | IllegalArgumentException e) {
                LOG.info("Failed to save {}", source.name(), e);
            }
        }
    }
}
