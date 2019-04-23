/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.loader;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ClassLoader hosting types generated for a particular type. A root instance is attached to a
 * BindingCodecContext instance, so any generated classes from it can be garbage-collected when the context
 * is destroyed, as well as to prevent two contexts trampling over each other.
 *
 * <p>
 * It semantically combines two class loaders: the class loader in which this class is loaded and the class loader in
 * which a target Binding interface/class is loaded. This inherently supports multi-classloader environments -- the root
 * instance has visibility only into codec classes and for each classloader we encounter when presented with a binding
 * class we create a leaf instance and cache it in the root instance. Leaf instances are using the root loader as their
 * parent, but consult the binding class's class loader if the root loader fails to load a particular class.
 *
 * <p>In single-classloader environments, obviously, the root loader can load all binding classes, and hence no leaf
 * loader is created.
 *
 * <p>
 * Each {@link CodecClassLoader} has a {@link ClassPool} attached to it and can perform operations on it. Leaf loaders
 * specify the root loader's ClassPool as their parent, but are configured to lookup classes first in themselves.
 *
 * @author Robert Varga
 */
@Beta
public abstract class CodecClassLoader extends ClassLoader {
    /**
     * A customizer allowing a generated class to be modified before it is loader.
     */
    @FunctionalInterface
    public interface Customizer {
        /**
         * Customize a generated class before it is instantiated in the loader.
         *
         * @param loader CodecClassLoader which will hold the class. It can be used to lookup/instantiate other classes
         * @param bindingClass Binding class for which the customized class is being generated
         * @param generated The class being generated
         * @return A set of generated classes the generated class depends on
         * @throws CannotCompileException if the customizer cannot perform partial compilation
         * @throws NotFoundException if the customizer cannot find a required class
         * @throws IOException if the customizer cannot perform partial loading
         */
        @NonNull List<Class<?>> customize(@NonNull CodecClassLoader loader, @NonNull CtClass bindingClass,
                @NonNull CtClass generated) throws CannotCompileException, NotFoundException, IOException;

        /**
         * Run the specified loader in a customized environment. The environment customizations must be cleaned up by
         * the time this method returns. The default implementation performs no customization.
         *
         * @param loader Class loader to execute
         * @return Class returned by the loader
         */
        default Class<?> customizeLoading(final @NonNull Supplier<Class<?>> loader) {
            return loader.get();
        }
    }

    static {
        verify(ClassLoader.registerAsParallelCapable());
    }

    private static final Logger LOG = LoggerFactory.getLogger(CodecClassLoader.class);

    private final ClassPool classPool;

    private CodecClassLoader(final ClassLoader parentLoader, final ClassPool parentPool) {
        super(parentLoader);
        this.classPool = new ClassPool(parentPool);
        this.classPool.childFirstLookup = true;
        this.classPool.appendClassPath(new LoaderClassPath(this));
    }

    CodecClassLoader() {
        this(StaticClassPool.LOADER, StaticClassPool.POOL);
    }

    CodecClassLoader(final CodecClassLoader parent) {
        this(parent, parent.classPool);
    }

    /**
     * Turn a Class instance into a CtClass for referencing it in code generation. This method supports both
     * generated- and non-generated classes.
     *
     * @param clazz Class to be looked up.
     * @return A CtClass instance
     * @throws NotFoundException if the class cannot be found
     * @throws NullPointerException if {@code clazz} is null
     */
    public final @NonNull CtClass findClass(final @NonNull Class<?> clazz) throws NotFoundException {
        return BindingReflections.isBindingClass(clazz) ? findClassLoader(clazz).getLocalFrozen(clazz.getName())
                : StaticClassPool.findClass(clazz);
    }

    /**
     * Create a new class by subclassing specified class and running a customizer on it. The name of the target class
     * is formed through concatenation of the name of a {@code bindingInterface} and specified {@code suffix}
     *
     * @param superClass Superclass from which to derive
     * @param bindingInterface Binding compile-time-generated interface
     * @param suffix Suffix to use
     * @param customizer Customizer to use to process the class
     * @return A generated class object
     * @throws CannotCompileException if the resulting generated class cannot be compiled or customized
     * @throws NotFoundException if the binding interface cannot be found or the generated class cannot be customized
     * @throws IOException if the generated class cannot be turned into bytecode or the generator fails with IOException
     * @throws NullPointerException if any argument is null
     */
    public final Class<?> generateSubclass(final CtClass superClass, final Class<?> bindingInterface,
            final String suffix, final Customizer customizer) throws CannotCompileException, IOException,
                NotFoundException {
        return findClassLoader(requireNonNull(bindingInterface))
                .doGenerateSubclass(superClass, bindingInterface, suffix, customizer);
    }

    final @NonNull CtClass getLocalFrozen(final String name) throws NotFoundException {
        synchronized (getClassLoadingLock(name)) {
            final CtClass result = classPool.get(name);
            result.freeze();
            return result;
        }
    }

    /**
     * Append specified loaders to this class loader for the purposes of looking up generated classes. Note that the
     * loaders are expected to have required classes already loaded. This is required to support generation of
     * inter-dependent structures, such as those used for streaming binding interfaces.
     *
     * @param newLoaders Loaders to append
     * @throws NullPointerException if {@code loaders} is null
     */
    abstract void appendLoaders(@NonNull Set<LeafCodecClassLoader> newLoaders);

    /**
     * Find the loader responsible for holding classes related to a binding class.
     *
     * @param bindingClass Class to locate
     * @return a Loader instance
     * @throws NullPointerException if {@code bindingClass} is null
     */
    abstract @NonNull CodecClassLoader findClassLoader(@NonNull Class<?> bindingClass);

    private Class<?> doGenerateSubclass(final CtClass superClass, final Class<?> bindingInterface, final String suffix,
            final Customizer customizer) throws CannotCompileException, IOException, NotFoundException {
        checkArgument(!superClass.isInterface(), "%s must not be an interface", superClass);
        checkArgument(bindingInterface.isInterface(), "%s is not an interface", bindingInterface);
        checkArgument(!Strings.isNullOrEmpty(suffix));

        final String bindingName = bindingInterface.getName();
        final String fqn = bindingName + "$$$" + suffix;
        synchronized (getClassLoadingLock(fqn)) {
            // Attempt to find a loaded class
            final Class<?> loaded = findLoadedClass(fqn);
            if (loaded != null) {
                return loaded;
            }

            // Get the interface
            final CtClass bindingCt = getLocalFrozen(bindingName);
            try {
                final byte[] byteCode;
                final CtClass generated = verifyNotNull(classPool.makeClass(fqn, superClass));
                try {
                    final List<Class<?>> deps = customizer.customize(this, bindingCt, generated);
                    final String ctName = generated.getName();
                    verify(fqn.equals(ctName), "Target class is %s returned result is %s", fqn, ctName);
                    processDependencies(deps);

                    byteCode = generated.toBytecode();
                } finally {
                    // Always detach the result, as we will never use it again
                    generated.detach();
                }

                return customizer.customizeLoading(() -> {
                    final Class<?> newClass = defineClass(fqn, byteCode, 0, byteCode.length);
                    resolveClass(newClass);
                    return newClass;
                });
            } finally {
                // Binding interfaces are used only a few times, hence it does not make sense to cache them in the class
                // pool.
                // TODO: this hinders caching, hence we should re-think this
                bindingCt.detach();
            }
        }
    }

    private void processDependencies(final List<Class<?>> deps) {
        final Set<LeafCodecClassLoader> depLoaders = new HashSet<>();
        for (Class<?> dep : deps) {
            final ClassLoader depLoader = dep.getClassLoader();
            verify(depLoader instanceof CodecClassLoader, "Dependency %s is not a generated class", dep);
            if (this.equals(depLoader)) {
                // Same loader, skip
                continue;
            }

            try {
                loadClass(dep.getName());
            } catch (ClassNotFoundException e) {
                LOG.debug("Cannot find {} in local loader, attempting to compensate", dep, e);
                // Root loader is always visible from a leaf, hence the dependency can only be a leaf
                verify(depLoader instanceof LeafCodecClassLoader, "Dependency loader %s is not a leaf", depLoader);
                depLoaders.add((LeafCodecClassLoader) depLoader);
            }
        }

        if (!depLoaders.isEmpty()) {
            appendLoaders(depLoaders);
        }
    }
}
