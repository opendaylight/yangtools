/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendaylight.yangtools.binding.codegen.FileSearchUtil.getFiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class YT1812Test extends BaseCompilationTest {
    private static Path SOURCES;
    private static Map<String, Path> FILES;

    @BeforeAll
    static void beforeAll() {
        SOURCES = CompilationTestUtils.generatorOutput("status-propagates-to-key");
        assertEquals(7, generateTestSources("/yt1812", SOURCES).size());
        FILES = getFiles(SOURCES);
        assertEquals(19, FILES.size());
    }

    @AfterAll
    static void afterAll() throws Exception {
        CompilationTestUtils.cleanUp(SOURCES);
        SOURCES = null;
        FILES = null;
    }

    private static void assertFileContent(final String name, final String expected) {
        final var file = FILES.get(name);
        assertNotNull(file, name + " not found");
        assertEquals(expected, assertDoesNotThrow(() -> Files.readString(file)));
    }


    @Test
    void rootDeprecatesGetters() {
        assertFileContent("FooData.java", """
            package org.opendaylight.yang.gen.v1.foo.norev;

            import java.lang.Class;
            import java.lang.Deprecated;
            import java.lang.Override;
            import java.util.Map;
            import javax.annotation.processing.Generated;
            import org.eclipse.jdt.annotation.NonNull;
            import org.eclipse.jdt.annotation.NonNullByDefault;
            import org.eclipse.jdt.annotation.Nullable;
            import org.opendaylight.yang.svc.v1.foo.norev.YangModuleInfoImpl;
            import org.opendaylight.yangtools.binding.DataRoot;
            import org.opendaylight.yangtools.binding.lib.CodeHelpers;
            import org.opendaylight.yangtools.binding.meta.RootMeta;

            /**
             *
             * <p>
             * This class represents the following YANG schema fragment defined in module <b>foo</b>
             * <pre>
             * module foo {
             *   namespace foo;
             *   prefix foo;
             *   list current {
             *     key foo;
             *     leaf foo {
             *       type string;
             *     }
             *   }
             *   list deprecated {
             *     status deprecated;
             *     key foo;
             *     leaf foo {
             *       type string;
             *     }
             *   }
             *   list obsolete {
             *     status obsolete;
             *     key foo;
             *     leaf foo {
             *       type string;
             *     }
             *   }
             * }
             * </pre>
             */
            @Generated("mdsal-binding-generator")
            public interface FooData extends DataRoot<FooData> {
                /**
                 * The {@link RootMeta} associated with this module root.
                 */
                @NonNullByDefault
                RootMeta<org.opendaylight.yang.gen.v1.foo.norev.FooData> META = new RootMeta<>(org.opendaylight.yang.\
            gen.v1.foo.norev.FooData.class, YangModuleInfoImpl.INSTANCE, YangModuleInfoImpl.UNSAFE_ACCESS);

                /**
                 * {@return {@code Map<CurrentKey, Current>} current, or {@code null} if it is not present}
                 */
                @Nullable Map<CurrentKey, Current> getCurrent();

                /**
                 * {@return {@code Map<CurrentKey, Current>} current, or an empty list if it is not present}
                 */
                default @NonNull Map<CurrentKey, Current> nonnullCurrent() {
                    return CodeHelpers.nonnull(getCurrent());
                }

                /**
                 * {@return {@code Map<DeprecatedKey, org.opendaylight.yang.gen.v1.foo.norev.Deprecated>} deprecated, \
            or {@code null} if it is not present}
                 */
                @Deprecated
                @Nullable Map<DeprecatedKey, org.opendaylight.yang.gen.v1.foo.norev.Deprecated> getDeprecated();

                /**
                 * {@return {@code Map<DeprecatedKey, org.opendaylight.yang.gen.v1.foo.norev.Deprecated>} deprecated, \
            or an empty list if it is not present}
                 */
                @Deprecated
                default @NonNull Map<DeprecatedKey, org.opendaylight.yang.gen.v1.foo.norev.Deprecated> \
            nonnullDeprecated() {
                    return CodeHelpers.nonnull(getDeprecated());
                }

                /**
                 * {@return {@code Map<ObsoleteKey, Obsolete>} obsolete, or {@code null} if it is not present}
                 */
                @Deprecated(forRemoval = true)
                @Nullable Map<ObsoleteKey, Obsolete> getObsolete();

                /**
                 * {@return {@code Map<ObsoleteKey, Obsolete>} obsolete, or an empty list if it is not present}
                 */
                @Deprecated(forRemoval = true)
                default @NonNull Map<ObsoleteKey, Obsolete> nonnullObsolete() {
                    return CodeHelpers.nonnull(getObsolete());
                }

                @Override
                default Class<org.opendaylight.yang.gen.v1.foo.norev.FooData> implementedInterface() {
                    return org.opendaylight.yang.gen.v1.foo.norev.FooData.class;
                }
            }
            """);
    }

    @Test
    void currentListIsNotDeprecated() {
        assertFileContent("Current.java", """
            package org.opendaylight.yang.gen.v1.foo.norev;

            import java.lang.Class;
            import java.lang.Override;
            import java.lang.String;
            import java.util.NoSuchElementException;
            import java.util.Objects;
            import javax.annotation.processing.Generated;
            import org.eclipse.jdt.annotation.NonNull;
            import org.opendaylight.yang.svc.v1.foo.norev.YangModuleInfoImpl;
            import org.opendaylight.yangtools.binding.ChildOf;
            import org.opendaylight.yangtools.binding.EntryObject;
            import org.opendaylight.yangtools.binding.lib.CodeHelpers;
            import org.opendaylight.yangtools.yang.common.QName;

            /**
             *
             * <p>
             * This class represents the following YANG schema fragment defined in module <b>foo</b>
             * <pre>
             * list current {
             *   key foo;
             *   leaf foo {
             *     type string;
             *   }
             * }
             * </pre>
             * <p>To create instances of this class use {@link CurrentBuilder}.
             * @see CurrentBuilder
             * @see CurrentKey
             */
            @Generated("mdsal-binding-generator")
            public interface Current
                extends ChildOf<FooData>,
                        EntryObject<Current, CurrentKey> {
                /**
                 * The YANG identifier of the {@code list} statement represented by this interface.
                 */
                @NonNull QName QNAME = YangModuleInfoImpl.qnameOf("current");

                /**
                 * {@return {@code String} foo, or {@code null} if it is not present}
                 */
                String getFoo();

                /**
                 * {@return {@code String} foo, guaranteed to be non-null}
                 * @throws NoSuchElementException if foo is not present
                 */
                default @NonNull String requireFoo() {
                    return CodeHelpers.require(getFoo(), "foo");
                }

                @Override
                default Class<org.opendaylight.yang.gen.v1.foo.norev.Current> implementedInterface() {
                    return org.opendaylight.yang.gen.v1.foo.norev.Current.class;
                }

                @Override
                default int javaHC() {
                    return CodeHelpers.jcHC1(this, getFoo());
                }

                @Override
                default boolean javaEQ(org.opendaylight.yang.gen.v1.foo.norev.Current obj) {
                    return Objects.equals(getFoo(), obj.getFoo())
                        && augmentations().equals(obj.augmentations());
                }

                @Override
                default String javaTS() {
                    return CodeHelpers.jcTS1(this, "foo", getFoo());
                }
            }
            """);
    }

    @Test
    void currentKeyIsNotDeprecated() {
        assertFileContent("CurrentKey.java", """
            package org.opendaylight.yang.gen.v1.foo.norev;

            import java.lang.Object;
            import java.lang.Override;
            import java.lang.String;
            import java.util.Objects;
            import javax.annotation.processing.Generated;
            import org.eclipse.jdt.annotation.NonNull;
            import org.opendaylight.yangtools.binding.Key;
            import org.opendaylight.yangtools.binding.lib.CodeHelpers;

            /**
             * This class represents the key of {@link Current} class.
             *
             * @see Current
             */
            @Generated("mdsal-binding-generator")
            public final class CurrentKey implements Key<Current> {
                @java.io.Serial
                private static final long serialVersionUID = -2581920028991143141L;

                private final @NonNull String _foo;

                /**
                 * Constructs an instance.
                 *
                 * @param _foo the entity foo
                 */
                public CurrentKey(@NonNull String _foo) {
                    this._foo = CodeHelpers.requireKeyProp(_foo, "foo");
                }

                /**
                 * Return foo, guaranteed to be non-null.
                 *
                 * @return {@code String} foo, guaranteed to be non-null.
                 */
                public @NonNull String getFoo() {
                    return _foo;
                }

                @Override
                public int hashCode() {
                    return CodeHelpers.wrapperHashCode(_foo);
                }

                @Override
                public boolean equals(Object obj) {
                    return this == obj || obj instanceof CurrentKey other
                        && Objects.equals(_foo, other._foo);
                }

                @Override
                public String toString() {
                    return CodeHelpers.jcTS1(CurrentKey.class, "foo", _foo);
                }
            }
            """);
    }

    @Test
    void currentBuilderIsNotDeprecated() {
        assertFileContent("CurrentBuilder.java", """
            package org.opendaylight.yang.gen.v1.foo.norev;

            import java.lang.Class;
            import java.lang.NullPointerException;
            import java.lang.Override;
            import java.lang.String;
            import java.lang.SuppressWarnings;
            import java.util.HashMap;
            import java.util.Map;
            import java.util.Objects;
            import javax.annotation.processing.Generated;
            import org.eclipse.jdt.annotation.NonNull;
            import org.opendaylight.yangtools.binding.Augmentation;
            import org.opendaylight.yangtools.binding.lib.AbstractEntryObject;

            /**
             * Class that builds {@link Current} instances. Overall design of the class is that of a
             * <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>, where method chaining is \
            used.
             *
             * <p>In general, this class is supposed to be used like this template:
             * <pre>
             *   <code>
             *     Current createCurrent(int fooXyzzy, int barBaz) {
             *         return new CurrentBuilder()
             *             .setFoo(new FooBuilder().setXyzzy(fooXyzzy).build())
             *             .setBar(new BarBuilder().setBaz(barBaz).build())
             *             .build();
             *     }
             *   </code>
             * </pre>
             *
             * <p>This pattern is supported by the immutable nature of Current, as instances can be freely passed \
            around without
             * worrying about synchronization issues.
             *
             * <p>As a side note: method chaining results in:
             * <ul>
             *   <li>very efficient Java bytecode, as the method invocation result, in this case the Builder \
            reference, is
             *       on the stack, so further method invocations just need to fill method arguments for the next \
            method
             *       invocation, which is terminated by {@link #build()}, which is then returned from the method</li>
             *   <li>better understanding by humans, as the scope of mutable state (the builder) is kept to a minimum \
            and is
             *       very localized</li>
             *   <li>better optimization opportunities, as the object scope is minimized in terms of invocation \
            (rather than
             *       method) stack, making <a href="https://en.wikipedia.org/wiki/Escape_analysis">escape analysis</a> \
            a lot
             *       easier. Given enough compiler (JIT/AOT) prowess, the cost of th builder object can be completely
             *       eliminated</li>
             * </ul>
             *
             * @see Current
             */
            @Generated("mdsal-binding-generator")
            public class CurrentBuilder {

                private String _foo;
                private CurrentKey key;

                Map<Class<? extends Augmentation<Current>>, Augmentation<Current>> augmentation = Map.of();

                /**
                 * Construct an empty builder.
                 */
                public CurrentBuilder() {
                    // No-op
                }


                /**
                 * Construct a builder initialized with state from specified {@link Current}.
                 *
                 * @param base Current from which the builder should be initialized
                 */
                public     CurrentBuilder(final Current base) {
                    final var aug = base.augmentations();
                    if (!aug.isEmpty()) {
                        this.augmentation = new HashMap<>(aug);
                    }
                    this.key = base.key();
                    this._foo = base.getFoo();
                }



                /**
                 * Return current value associated with the property corresponding to {@link Current#key()}.
                 *
                 * @return current value
                 */
                public CurrentKey key() {
                    return key;
                }

                /**
                 * Return current value associated with the property corresponding to {@link Current#getFoo()}.
                 *
                 * @return current value
                 */
                public String getFoo() {
                    return _foo;
                }

                /**
                 * Return the specified augmentation, if it is present in this builder.
                 *
                 * @param <E$$> augmentation type
                 * @param augmentationType augmentation type class
                 * @return Augmentation object from this builder, or {@code null} if not present
                 * @throws NullPointerException if {@code augmentType} is {@code null}
                 */
                @SuppressWarnings({ "unchecked", "checkstyle:methodTypeParameterName"})
                public <E$$ extends Augmentation<Current>> E$$ augmentation(Class<E$$> augmentationType) {
                    return (E$$) augmentation.get(Objects.requireNonNull(augmentationType));
                }

                /**
                 * Set the key value corresponding to {@link Current#key()} to the specified
                 * value.
                 *
                 * @param key desired value
                 * @return this builder
                 */
                public CurrentBuilder withKey(final CurrentKey key) {
                    this.key = key;
                    return this;
                }

                /**
                 * Set the property corresponding to {@link Current#getFoo()} to the specified
                 * value.
                 *
                 * @param value desired value
                 * @return this builder
                 */
                public CurrentBuilder setFoo(final String value) {
                    this._foo = value;
                    return this;
                }

                /**
                 * Add an augmentation to this builder's product.
                 *
                 * @param augmentation augmentation to be added
                 * @return this builder
                 * @throws NullPointerException if {@code augmentation} is null
                 */
                public CurrentBuilder addAugmentation(Augmentation<Current> augmentation) {
                    if (!(this.augmentation instanceof HashMap)) {
                        this.augmentation = new HashMap<>();
                    }
                    this.augmentation.put(augmentation.implementedInterface(), augmentation);
                    return this;
                }

                /**
                 * Remove an augmentation from this builder's product. If this builder does not track such an \
            augmentation
                 * type, this method does nothing.
                 *
                 * @param augmentationType augmentation type to be removed
                 * @return this builder
                 */
                public CurrentBuilder removeAugmentation(Class<? extends Augmentation<Current>> augmentationType) {
                    if (this.augmentation instanceof HashMap) {
                        this.augmentation.remove(augmentationType);
                    }
                    return this;
                }

                /**
                 * {@return A new {@link Current} instance}
                 */
                public @NonNull Current build() {
                    return new CurrentImpl(this);
                }

                private static final class CurrentImpl extends AbstractEntryObject<Current, CurrentKey> implements \
            Current {
                    private final String _foo;

                    CurrentImpl(final CurrentBuilder base) {
                        super(base.augmentation, extractKey(base));
                        final var key = key();
                        this._foo = key.getFoo();
                    }

                    private static @NonNull CurrentKey extractKey(CurrentBuilder base) {
                        final var key = base.key();
                        return key != null ? key
                            : new CurrentKey(base.getFoo());
                    }

                    @Override
                    public String getFoo() {
                        return _foo;
                    }
                }
            }
            """);
    }

    @Test
    void deprecatedListIsDeprecated() {
        assertFileContent("Deprecated.java", """
            package org.opendaylight.yang.gen.v1.foo.norev;

            import java.lang.Class;
            import java.lang.Override;
            import java.lang.String;
            import java.util.NoSuchElementException;
            import java.util.Objects;
            import javax.annotation.processing.Generated;
            import org.eclipse.jdt.annotation.NonNull;
            import org.opendaylight.yang.svc.v1.foo.norev.YangModuleInfoImpl;
            import org.opendaylight.yangtools.binding.ChildOf;
            import org.opendaylight.yangtools.binding.EntryObject;
            import org.opendaylight.yangtools.binding.lib.CodeHelpers;
            import org.opendaylight.yangtools.yang.common.QName;

            /**
             *
             * <p>
             * This class represents the following YANG schema fragment defined in module <b>foo</b>
             * <pre>
             * list deprecated {
             *   status deprecated;
             *   key foo;
             *   leaf foo {
             *     type string;
             *   }
             * }
             * </pre>
             * <p>To create instances of this class use {@link DeprecatedBuilder}.
             * @see DeprecatedBuilder
             * @see DeprecatedKey
             */
            @java.lang.Deprecated
            @Generated("mdsal-binding-generator")
            public interface Deprecated
                extends ChildOf<FooData>,
                        EntryObject<Deprecated, DeprecatedKey> {
                /**
                 * The YANG identifier of the {@code list} statement represented by this interface.
                 */
                @NonNull QName QNAME = YangModuleInfoImpl.qnameOf("deprecated");

                /**
                 * {@return {@code String} foo, or {@code null} if it is not present}
                 */
                String getFoo();

                /**
                 * {@return {@code String} foo, guaranteed to be non-null}
                 * @throws NoSuchElementException if foo is not present
                 */
                default @NonNull String requireFoo() {
                    return CodeHelpers.require(getFoo(), "foo");
                }

                @Override
                default Class<org.opendaylight.yang.gen.v1.foo.norev.Deprecated> implementedInterface() {
                    return org.opendaylight.yang.gen.v1.foo.norev.Deprecated.class;
                }

                @Override
                default int javaHC() {
                    return CodeHelpers.jcHC1(this, getFoo());
                }

                @Override
                default boolean javaEQ(org.opendaylight.yang.gen.v1.foo.norev.Deprecated obj) {
                    return Objects.equals(getFoo(), obj.getFoo())
                        && augmentations().equals(obj.augmentations());
                }

                @Override
                default String javaTS() {
                    return CodeHelpers.jcTS1(this, "foo", getFoo());
                }
            }
            """);
    }

    @Test
    void deprecatedKeyIsDeprecated() {
        assertFileContent("DeprecatedKey.java", """
            package org.opendaylight.yang.gen.v1.foo.norev;

            import java.lang.Object;
            import java.lang.Override;
            import java.lang.String;
            import java.util.Objects;
            import javax.annotation.processing.Generated;
            import org.eclipse.jdt.annotation.NonNull;
            import org.opendaylight.yangtools.binding.Key;
            import org.opendaylight.yangtools.binding.lib.CodeHelpers;

            /**
             * This class represents the key of {@link Deprecated} class.
             *
             * @see Deprecated
             */
            @Generated("mdsal-binding-generator")
            @java.lang.Deprecated
            public final class DeprecatedKey implements Key<Deprecated> {
                @java.io.Serial
                private static final long serialVersionUID = 4921496290213001765L;

                private final @NonNull String _foo;

                /**
                 * Constructs an instance.
                 *
                 * @param _foo the entity foo
                 */
                public DeprecatedKey(@NonNull String _foo) {
                    this._foo = CodeHelpers.requireKeyProp(_foo, "foo");
                }

                /**
                 * Return foo, guaranteed to be non-null.
                 *
                 * @return {@code String} foo, guaranteed to be non-null.
                 */
                public @NonNull String getFoo() {
                    return _foo;
                }

                @Override
                public int hashCode() {
                    return CodeHelpers.wrapperHashCode(_foo);
                }

                @Override
                public boolean equals(Object obj) {
                    return this == obj || obj instanceof DeprecatedKey other
                        && Objects.equals(_foo, other._foo);
                }

                @Override
                public String toString() {
                    return CodeHelpers.jcTS1(DeprecatedKey.class, "foo", _foo);
                }
            }
            """);
    }

    @Test
    void deprecatedBuilderIsDeprecated() {
        assertFileContent("DeprecatedBuilder.java", """
            package org.opendaylight.yang.gen.v1.foo.norev;

            import java.lang.Class;
            import java.lang.NullPointerException;
            import java.lang.Override;
            import java.lang.String;
            import java.lang.SuppressWarnings;
            import java.util.HashMap;
            import java.util.Map;
            import java.util.Objects;
            import javax.annotation.processing.Generated;
            import org.eclipse.jdt.annotation.NonNull;
            import org.opendaylight.yangtools.binding.Augmentation;
            import org.opendaylight.yangtools.binding.lib.AbstractEntryObject;

            /**
             * Class that builds {@link Deprecated} instances. Overall design of the class is that of a
             * <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>, where method chaining is \
            used.
             *
             * <p>In general, this class is supposed to be used like this template:
             * <pre>
             *   <code>
             *     Deprecated createDeprecated(int fooXyzzy, int barBaz) {
             *         return new DeprecatedBuilder()
             *             .setFoo(new FooBuilder().setXyzzy(fooXyzzy).build())
             *             .setBar(new BarBuilder().setBaz(barBaz).build())
             *             .build();
             *     }
             *   </code>
             * </pre>
             *
             * <p>This pattern is supported by the immutable nature of Deprecated, as instances can be freely passed \
            around without
             * worrying about synchronization issues.
             *
             * <p>As a side note: method chaining results in:
             * <ul>
             *   <li>very efficient Java bytecode, as the method invocation result, in this case the Builder \
            reference, is
             *       on the stack, so further method invocations just need to fill method arguments for the next method
             *       invocation, which is terminated by {@link #build()}, which is then returned from the method</li>
             *   <li>better understanding by humans, as the scope of mutable state (the builder) is kept to a minimum \
            and is
             *       very localized</li>
             *   <li>better optimization opportunities, as the object scope is minimized in terms of invocation \
            (rather than
             *       method) stack, making <a href="https://en.wikipedia.org/wiki/Escape_analysis">escape analysis</a> \
            a lot
             *       easier. Given enough compiler (JIT/AOT) prowess, the cost of th builder object can be completely
             *       eliminated</li>
             * </ul>
             *
             * @see Deprecated
             */
            @SuppressWarnings("deprecation")
            @Generated("mdsal-binding-generator")
            public class DeprecatedBuilder {

                private String _foo;
                private DeprecatedKey key;

                Map<Class<? extends Augmentation<Deprecated>>, Augmentation<Deprecated>> augmentation = Map.of();

                /**
                 * Construct an empty builder.
                 */
                public DeprecatedBuilder() {
                    // No-op
                }


                /**
                 * Construct a builder initialized with state from specified {@link Deprecated}.
                 *
                 * @param base Deprecated from which the builder should be initialized
                 */
                public     DeprecatedBuilder(final Deprecated base) {
                    final var aug = base.augmentations();
                    if (!aug.isEmpty()) {
                        this.augmentation = new HashMap<>(aug);
                    }
                    this.key = base.key();
                    this._foo = base.getFoo();
                }



                /**
                 * Return current value associated with the property corresponding to {@link Deprecated#key()}.
                 *
                 * @return current value
                 */
                public DeprecatedKey key() {
                    return key;
                }

                /**
                 * Return current value associated with the property corresponding to {@link Deprecated#getFoo()}.
                 *
                 * @return current value
                 */
                public String getFoo() {
                    return _foo;
                }

                /**
                 * Return the specified augmentation, if it is present in this builder.
                 *
                 * @param <E$$> augmentation type
                 * @param augmentationType augmentation type class
                 * @return Augmentation object from this builder, or {@code null} if not present
                 * @throws NullPointerException if {@code augmentType} is {@code null}
                 */
                @SuppressWarnings({ "unchecked", "checkstyle:methodTypeParameterName"})
                public <E$$ extends Augmentation<Deprecated>> E$$ augmentation(Class<E$$> augmentationType) {
                    return (E$$) augmentation.get(Objects.requireNonNull(augmentationType));
                }

                /**
                 * Set the key value corresponding to {@link Deprecated#key()} to the specified
                 * value.
                 *
                 * @param key desired value
                 * @return this builder
                 */
                public DeprecatedBuilder withKey(final DeprecatedKey key) {
                    this.key = key;
                    return this;
                }

                /**
                 * Set the property corresponding to {@link Deprecated#getFoo()} to the specified
                 * value.
                 *
                 * @param value desired value
                 * @return this builder
                 */
                public DeprecatedBuilder setFoo(final String value) {
                    this._foo = value;
                    return this;
                }

                /**
                 * Add an augmentation to this builder's product.
                 *
                 * @param augmentation augmentation to be added
                 * @return this builder
                 * @throws NullPointerException if {@code augmentation} is null
                 */
                public DeprecatedBuilder addAugmentation(Augmentation<Deprecated> augmentation) {
                    if (!(this.augmentation instanceof HashMap)) {
                        this.augmentation = new HashMap<>();
                    }
                    this.augmentation.put(augmentation.implementedInterface(), augmentation);
                    return this;
                }

                /**
                 * Remove an augmentation from this builder's product. If this builder does not track such an \
            augmentation
                 * type, this method does nothing.
                 *
                 * @param augmentationType augmentation type to be removed
                 * @return this builder
                 */
                public DeprecatedBuilder removeAugmentation(Class<? extends Augmentation<Deprecated>> \
            augmentationType) {
                    if (this.augmentation instanceof HashMap) {
                        this.augmentation.remove(augmentationType);
                    }
                    return this;
                }

                /**
                 * {@return A new {@link Deprecated} instance}
                 */
                public @NonNull Deprecated build() {
                    return new DeprecatedImpl(this);
                }

                @java.lang.Deprecated
                private static final class DeprecatedImpl extends AbstractEntryObject<Deprecated, DeprecatedKey> \
            implements Deprecated {
                    private final String _foo;

                    DeprecatedImpl(final DeprecatedBuilder base) {
                        super(base.augmentation, extractKey(base));
                        final var key = key();
                        this._foo = key.getFoo();
                    }

                    private static @NonNull DeprecatedKey extractKey(DeprecatedBuilder base) {
                        final var key = base.key();
                        return key != null ? key
                            : new DeprecatedKey(base.getFoo());
                    }

                    @Override
                    public String getFoo() {
                        return _foo;
                    }
                }
            }
            """);
    }

    @Test
    void obsoleteListIsDeprecated() {
        assertFileContent("Obsolete.java", """
            package org.opendaylight.yang.gen.v1.foo.norev;

            import java.lang.Class;
            import java.lang.Deprecated;
            import java.lang.Override;
            import java.lang.String;
            import java.util.NoSuchElementException;
            import java.util.Objects;
            import javax.annotation.processing.Generated;
            import org.eclipse.jdt.annotation.NonNull;
            import org.opendaylight.yang.svc.v1.foo.norev.YangModuleInfoImpl;
            import org.opendaylight.yangtools.binding.ChildOf;
            import org.opendaylight.yangtools.binding.EntryObject;
            import org.opendaylight.yangtools.binding.lib.CodeHelpers;
            import org.opendaylight.yangtools.yang.common.QName;

            /**
             *
             * <p>
             * This class represents the following YANG schema fragment defined in module <b>foo</b>
             * <pre>
             * list obsolete {
             *   status obsolete;
             *   key foo;
             *   leaf foo {
             *     type string;
             *   }
             * }
             * </pre>
             * <p>To create instances of this class use {@link ObsoleteBuilder}.
             * @see ObsoleteBuilder
             * @see ObsoleteKey
             */
            @Deprecated(forRemoval = true)
            @Generated("mdsal-binding-generator")
            public interface Obsolete
                extends ChildOf<FooData>,
                        EntryObject<Obsolete, ObsoleteKey> {
                /**
                 * The YANG identifier of the {@code list} statement represented by this interface.
                 */
                @NonNull QName QNAME = YangModuleInfoImpl.qnameOf("obsolete");

                /**
                 * {@return {@code String} foo, or {@code null} if it is not present}
                 */
                String getFoo();

                /**
                 * {@return {@code String} foo, guaranteed to be non-null}
                 * @throws NoSuchElementException if foo is not present
                 */
                default @NonNull String requireFoo() {
                    return CodeHelpers.require(getFoo(), "foo");
                }

                @Override
                default Class<org.opendaylight.yang.gen.v1.foo.norev.Obsolete> implementedInterface() {
                    return org.opendaylight.yang.gen.v1.foo.norev.Obsolete.class;
                }

                @Override
                default int javaHC() {
                    return CodeHelpers.jcHC1(this, getFoo());
                }

                @Override
                default boolean javaEQ(org.opendaylight.yang.gen.v1.foo.norev.Obsolete obj) {
                    return Objects.equals(getFoo(), obj.getFoo())
                        && augmentations().equals(obj.augmentations());
                }

                @Override
                default String javaTS() {
                    return CodeHelpers.jcTS1(this, "foo", getFoo());
                }
            }
            """);
    }

    @Test
    void obsoleteKeyIsDeprecated() {
        assertFileContent("ObsoleteKey.java", """
            package org.opendaylight.yang.gen.v1.foo.norev;

            import java.lang.Deprecated;
            import java.lang.Object;
            import java.lang.Override;
            import java.lang.String;
            import java.util.Objects;
            import javax.annotation.processing.Generated;
            import org.eclipse.jdt.annotation.NonNull;
            import org.opendaylight.yangtools.binding.Key;
            import org.opendaylight.yangtools.binding.lib.CodeHelpers;

            /**
             * This class represents the key of {@link Obsolete} class.
             *
             * @see Obsolete
             */
            @Generated("mdsal-binding-generator")
            @Deprecated(forRemoval = true)
            public final class ObsoleteKey implements Key<Obsolete> {
                @java.io.Serial
                private static final long serialVersionUID = 6696092533010852638L;

                private final @NonNull String _foo;

                /**
                 * Constructs an instance.
                 *
                 * @param _foo the entity foo
                 */
                public ObsoleteKey(@NonNull String _foo) {
                    this._foo = CodeHelpers.requireKeyProp(_foo, "foo");
                }

                /**
                 * Return foo, guaranteed to be non-null.
                 *
                 * @return {@code String} foo, guaranteed to be non-null.
                 */
                public @NonNull String getFoo() {
                    return _foo;
                }

                @Override
                public int hashCode() {
                    return CodeHelpers.wrapperHashCode(_foo);
                }

                @Override
                public boolean equals(Object obj) {
                    return this == obj || obj instanceof ObsoleteKey other
                        && Objects.equals(_foo, other._foo);
                }

                @Override
                public String toString() {
                    return CodeHelpers.jcTS1(ObsoleteKey.class, "foo", _foo);
                }
            }
            """);
    }

    @Test
    void obsoleteBuilderIsDeprecated() {
        assertFileContent("ObsoleteBuilder.java", """
            package org.opendaylight.yang.gen.v1.foo.norev;

            import java.lang.Class;
            import java.lang.Deprecated;
            import java.lang.NullPointerException;
            import java.lang.Override;
            import java.lang.String;
            import java.lang.SuppressWarnings;
            import java.util.HashMap;
            import java.util.Map;
            import java.util.Objects;
            import javax.annotation.processing.Generated;
            import org.eclipse.jdt.annotation.NonNull;
            import org.opendaylight.yangtools.binding.Augmentation;
            import org.opendaylight.yangtools.binding.lib.AbstractEntryObject;

            /**
             * Class that builds {@link Obsolete} instances. Overall design of the class is that of a
             * <a href="https://en.wikipedia.org/wiki/Fluent_interface">fluent interface</a>, where method chaining is \
            used.
             *
             * <p>In general, this class is supposed to be used like this template:
             * <pre>
             *   <code>
             *     Obsolete createObsolete(int fooXyzzy, int barBaz) {
             *         return new ObsoleteBuilder()
             *             .setFoo(new FooBuilder().setXyzzy(fooXyzzy).build())
             *             .setBar(new BarBuilder().setBaz(barBaz).build())
             *             .build();
             *     }
             *   </code>
             * </pre>
             *
             * <p>This pattern is supported by the immutable nature of Obsolete, as instances can be freely passed \
            around without
             * worrying about synchronization issues.
             *
             * <p>As a side note: method chaining results in:
             * <ul>
             *   <li>very efficient Java bytecode, as the method invocation result, in this case the Builder \
            reference, is
             *       on the stack, so further method invocations just need to fill method arguments for the next method
             *       invocation, which is terminated by {@link #build()}, which is then returned from the method</li>
             *   <li>better understanding by humans, as the scope of mutable state (the builder) is kept to a minimum \
            and is
             *       very localized</li>
             *   <li>better optimization opportunities, as the object scope is minimized in terms of invocation \
            (rather than
             *       method) stack, making <a href="https://en.wikipedia.org/wiki/Escape_analysis">escape analysis</a> \
            a lot
             *       easier. Given enough compiler (JIT/AOT) prowess, the cost of th builder object can be completely
             *       eliminated</li>
             * </ul>
             *
             * @see Obsolete
             */
            @Deprecated(forRemoval = true)
            @Generated("mdsal-binding-generator")
            public class ObsoleteBuilder {

                private String _foo;
                private ObsoleteKey key;

                Map<Class<? extends Augmentation<Obsolete>>, Augmentation<Obsolete>> augmentation = Map.of();

                /**
                 * Construct an empty builder.
                 */
                public ObsoleteBuilder() {
                    // No-op
                }


                /**
                 * Construct a builder initialized with state from specified {@link Obsolete}.
                 *
                 * @param base Obsolete from which the builder should be initialized
                 */
                public     ObsoleteBuilder(final Obsolete base) {
                    final var aug = base.augmentations();
                    if (!aug.isEmpty()) {
                        this.augmentation = new HashMap<>(aug);
                    }
                    this.key = base.key();
                    this._foo = base.getFoo();
                }



                /**
                 * Return current value associated with the property corresponding to {@link Obsolete#key()}.
                 *
                 * @return current value
                 */
                public ObsoleteKey key() {
                    return key;
                }

                /**
                 * Return current value associated with the property corresponding to {@link Obsolete#getFoo()}.
                 *
                 * @return current value
                 */
                public String getFoo() {
                    return _foo;
                }

                /**
                 * Return the specified augmentation, if it is present in this builder.
                 *
                 * @param <E$$> augmentation type
                 * @param augmentationType augmentation type class
                 * @return Augmentation object from this builder, or {@code null} if not present
                 * @throws NullPointerException if {@code augmentType} is {@code null}
                 */
                @SuppressWarnings({ "unchecked", "checkstyle:methodTypeParameterName"})
                public <E$$ extends Augmentation<Obsolete>> E$$ augmentation(Class<E$$> augmentationType) {
                    return (E$$) augmentation.get(Objects.requireNonNull(augmentationType));
                }

                /**
                 * Set the key value corresponding to {@link Obsolete#key()} to the specified
                 * value.
                 *
                 * @param key desired value
                 * @return this builder
                 */
                public ObsoleteBuilder withKey(final ObsoleteKey key) {
                    this.key = key;
                    return this;
                }

                /**
                 * Set the property corresponding to {@link Obsolete#getFoo()} to the specified
                 * value.
                 *
                 * @param value desired value
                 * @return this builder
                 */
                public ObsoleteBuilder setFoo(final String value) {
                    this._foo = value;
                    return this;
                }

                /**
                 * Add an augmentation to this builder's product.
                 *
                 * @param augmentation augmentation to be added
                 * @return this builder
                 * @throws NullPointerException if {@code augmentation} is null
                 */
                public ObsoleteBuilder addAugmentation(Augmentation<Obsolete> augmentation) {
                    if (!(this.augmentation instanceof HashMap)) {
                        this.augmentation = new HashMap<>();
                    }
                    this.augmentation.put(augmentation.implementedInterface(), augmentation);
                    return this;
                }

                /**
                 * Remove an augmentation from this builder's product. If this builder does not track such an \
            augmentation
                 * type, this method does nothing.
                 *
                 * @param augmentationType augmentation type to be removed
                 * @return this builder
                 */
                public ObsoleteBuilder removeAugmentation(Class<? extends Augmentation<Obsolete>> augmentationType) {
                    if (this.augmentation instanceof HashMap) {
                        this.augmentation.remove(augmentationType);
                    }
                    return this;
                }

                /**
                 * {@return A new {@link Obsolete} instance}
                 */
                public @NonNull Obsolete build() {
                    return new ObsoleteImpl(this);
                }

                @Deprecated(forRemoval = true)
                private static final class ObsoleteImpl extends AbstractEntryObject<Obsolete, ObsoleteKey> implements \
            Obsolete {
                    private final String _foo;

                    ObsoleteImpl(final ObsoleteBuilder base) {
                        super(base.augmentation, extractKey(base));
                        final var key = key();
                        this._foo = key.getFoo();
                    }

                    private static @NonNull ObsoleteKey extractKey(ObsoleteBuilder base) {
                        final var key = base.key();
                        return key != null ? key
                            : new ObsoleteKey(base.getFoo());
                    }

                    @Override
                    public String getFoo() {
                        return _foo;
                    }
                }
            }
            """);
    }
}
