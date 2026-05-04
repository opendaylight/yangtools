/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenGeneratedTOBuilder;

class ClassCodeGeneratorTest {
    /**
     * Test for testing of false scenario. Test tests value types. Value types are not allowed to have default
     * constructor.
     */
    @Test
    void defaultConstructorNotPresentInValueTypeTest() {
        final var toBuilder = new CodegenGeneratedTOBuilder(JavaTypeName.create("simple.pack", "DefCtor"));
        toBuilder.setTypedef(true);

        var propBuilder = toBuilder.addProperty("foo");
        propBuilder.setReturnType(Types.typeForClass(String.class));
        propBuilder.setReadOnly(false);

        propBuilder = toBuilder.addProperty("bar");
        propBuilder.setReturnType(Types.typeForClass(Integer.class));
        propBuilder.setReadOnly(false);

        assertEquals("""
            package simple.pack;

            import java.lang.Integer;
            import java.lang.Object;
            import java.lang.Override;
            import java.lang.String;
            import java.util.Objects;
            import javax.annotation.processing.Generated;
            import org.opendaylight.yangtools.binding.lib.CodeHelpers;

            @Generated("mdsal-binding-generator")
            public class DefCtor {
                private String _foo;
                private Integer _bar;

                public DefCtor(Integer _bar, String _foo) {
                    this._foo = _foo;
                    this._bar = _bar;
                }

                /**
                 * Creates a copy from Source Object.
                 *
                 * @param source Source object
                 */
                public DefCtor(DefCtor source) {
                    this._foo = source._foo;
                    this._bar = source._bar;
                }

                public static DefCtor getDefaultInstance(final String defaultValue) {
                    return new DefCtor(Integer.valueOf(defaultValue));
                }

                public String getFoo() {
                    return _foo;
                }

                public DefCtor setFoo(String value) {
                    this._foo = value;
                    return this;
                }

                public Integer getBar() {
                    return _bar;
                }

                public DefCtor setBar(Integer value) {
                    this._bar = value;
                    return this;
                }

                @Override
                public int hashCode() {
                    final int prime = 31;
                    int result = 1;
                    result = prime * result + Objects.hashCode(_foo);
                    result = prime * result + Objects.hashCode(_bar);
                    return result;
                }

                @Override
                public final boolean equals(Object obj) {
                    return this == obj || obj instanceof DefCtor other
                        && Objects.equals(_foo, other._foo)
                        && Objects.equals(_bar, other._bar);
                }

                @Override
                public String toString() {
                    return CodeHelpers.jcTSB(DefCtor.class)
                        .prop("foo", _foo)
                        .prop("bar", _bar)
                        .build();
                }
            }

            """, new TOGenerator(toBuilder.build()).generate());
    }

    @Test
    void toStringTest() {
        final var toBuilder = new CodegenGeneratedTOBuilder(JavaTypeName.create("simple.pack", "DefCtor"));
        toBuilder.setTypedef(true);

        var propBuilder = toBuilder.addProperty("foo");
        propBuilder.setReturnType(Types.typeForClass(String.class));
        propBuilder.setReadOnly(false);

        propBuilder = toBuilder.addProperty("bar");
        propBuilder.setReturnType(Types.typeForClass(Integer.class));
        propBuilder.setReadOnly(false);

        assertEquals("""
            package simple.pack;

            import java.lang.Integer;
            import java.lang.Object;
            import java.lang.Override;
            import java.lang.String;
            import java.util.Objects;
            import javax.annotation.processing.Generated;
            import org.opendaylight.yangtools.binding.lib.CodeHelpers;

            @Generated("mdsal-binding-generator")
            public class DefCtor {
                private String _foo;
                private Integer _bar;

                public DefCtor(Integer _bar, String _foo) {
                    this._foo = _foo;
                    this._bar = _bar;
                }

                /**
                 * Creates a copy from Source Object.
                 *
                 * @param source Source object
                 */
                public DefCtor(DefCtor source) {
                    this._foo = source._foo;
                    this._bar = source._bar;
                }

                public static DefCtor getDefaultInstance(final String defaultValue) {
                    return new DefCtor(Integer.valueOf(defaultValue));
                }

                public String getFoo() {
                    return _foo;
                }

                public DefCtor setFoo(String value) {
                    this._foo = value;
                    return this;
                }

                public Integer getBar() {
                    return _bar;
                }

                public DefCtor setBar(Integer value) {
                    this._bar = value;
                    return this;
                }

                @Override
                public int hashCode() {
                    final int prime = 31;
                    int result = 1;
                    result = prime * result + Objects.hashCode(_foo);
                    result = prime * result + Objects.hashCode(_bar);
                    return result;
                }

                @Override
                public final boolean equals(Object obj) {
                    return this == obj || obj instanceof DefCtor other
                        && Objects.equals(_foo, other._foo)
                        && Objects.equals(_bar, other._bar);
                }

                @Override
                public String toString() {
                    return CodeHelpers.jcTSB(DefCtor.class)
                        .prop("foo", _foo)
                        .prop("bar", _bar)
                        .build();
                }
            }

            """, new TOGenerator(toBuilder.build()).generate());
    }
}
