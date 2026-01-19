/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ServiceLoader;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.generator.BindingGenerator;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class KeyGeneratorTest {
    @Test
    void compositeKeyClassTest() {
        final var genTypes = ServiceLoader.load(BindingGenerator.class).findFirst().orElseThrow()
            .generateTypes(YangParserTestUtils.parseYangResource("/list-composite-key.yang"));
        assertNotNull(genTypes);
        assertEquals(7, genTypes.size());

        final var root = assertInstanceOf(DataRootArchetype.class, genTypes.getFirst());
        final var keys = genTypes.stream()
            .filter(KeyArchetype.class::isInstance).map(KeyArchetype.class::cast)
            .toList();
        assertEquals(2, keys.size());

        final var first = keys.getFirst();
        assertEquals("InnerListKey", first.name().simpleName());
        assertKeyClass("""
            package org.opendaylight.yang.gen.v1.urn.composite.key.rev130227.list.parent.container.\
            composite.key.list;

            import java.lang.Object;
            import java.lang.Override;
            import java.lang.String;
            import java.util.Objects;
            import javax.annotation.processing.Generated;
            import org.eclipse.jdt.annotation.NonNull;
            import org.opendaylight.yangtools.binding.Key;
            import org.opendaylight.yangtools.binding.lib.CodeHelpers;
            import org.opendaylight.yangtools.yang.common.Uint16;

            /**
             * This class represents the key of {@link InnerList} class.
             *
             * @see InnerList
             */
            @Generated("mdsal-binding-generator")
            public final class InnerListKey implements Key<InnerList> {
                @java.io.Serial
                private static final long serialVersionUID = 2256312821779854996L;

                private final @NonNull Uint16 _key1;

                /**
                 * Constructs an instance.
                 *
                 * @param _key1 the entity key1
                 */
                public InnerListKey(@NonNull Uint16 _key1) {
                    this._key1 = CodeHelpers.requireKeyProp(_key1, "key1");
                }

                /**
                 * Return key1, guaranteed to be non-null.
                 *
                 * @return {@code Uint16} key1, guaranteed to be non-null.
                 */
                public @NonNull Uint16 getKey1() {
                    return _key1;
                }

                @Override
                public int hashCode() {
                    return CodeHelpers.wrapperHashCode(_key1);
                }

                @Override
                public boolean equals(Object obj) {
                    return this == obj || obj instanceof InnerListKey other
                        && Objects.equals(_key1, other._key1);
                }

                @Override
                public String toString() {
                    return CodeHelpers.jcTS1(InnerListKey.class, "key1", _key1);
                }
            }
            """, root, first);

        final var second = keys.getLast();
        assertKeyClass("""
            package org.opendaylight.yang.gen.v1.urn.composite.key.rev130227.list.parent.container;

            import java.lang.Byte;
            import java.lang.Object;
            import java.lang.Override;
            import java.lang.String;
            import java.util.Objects;
            import javax.annotation.processing.Generated;
            import org.eclipse.jdt.annotation.NonNull;
            import org.opendaylight.yangtools.binding.Key;
            import org.opendaylight.yangtools.binding.lib.CodeHelpers;

            /**
             * This class represents the key of {@link CompositeKeyList} class.
             *
             * @see CompositeKeyList
             */
            @Generated("mdsal-binding-generator")
            public final class CompositeKeyListKey implements Key<CompositeKeyList> {
                @java.io.Serial
                private static final long serialVersionUID = 4635577615717332911L;

                private final @NonNull Byte _key1;
                private final @NonNull String _key2;

                /**
                 * Constructs an instance.
                 *
                 * @param _key1 the entity key1
                 * @param _key2 the entity key2
                 */
                public CompositeKeyListKey(@NonNull Byte _key1, @NonNull String _key2) {
                    this._key1 = CodeHelpers.requireKeyProp(_key1, "key1");
                    this._key2 = CodeHelpers.requireKeyProp(_key2, "key2");
                }

                /**
                 * Return key1, guaranteed to be non-null.
                 *
                 * @return {@code Byte} key1, guaranteed to be non-null.
                 */
                public @NonNull Byte getKey1() {
                    return _key1;
                }

                /**
                 * Return key2, guaranteed to be non-null.
                 *
                 * @return {@code String} key2, guaranteed to be non-null.
                 */
                public @NonNull String getKey2() {
                    return _key2;
                }

                @Override
                public int hashCode() {
                    final int prime = 31;
                    int result = 1;
                    result = prime * result + Objects.hashCode(_key1);
                    result = prime * result + Objects.hashCode(_key2);
                    return result;
                }

                @Override
                public boolean equals(Object obj) {
                    return this == obj || obj instanceof CompositeKeyListKey other
                        && Objects.equals(_key1, other._key1)
                        && Objects.equals(_key2, other._key2);
                }

                @Override
                public String toString() {
                    return CodeHelpers.jcTSB(CompositeKeyListKey.class)
                        .prop("key1", _key1)
                        .prop("key2", _key2)
                        .build();
                }
            }
            """, root, second);
    }

    @NonNullByDefault
    private static void assertKeyClass(final String expected, final DataRootArchetype root, final KeyArchetype key) {
        final var sb = new StringBuilder();
        new KeyTemplate(root, key).generateTo(sb);
        assertEquals(expected, sb.toString());
    }
}
