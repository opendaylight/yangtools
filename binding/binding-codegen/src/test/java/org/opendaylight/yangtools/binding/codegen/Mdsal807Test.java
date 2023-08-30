/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Mdsal807Test extends BaseCompilationTest {
    private Path sourcesOutputDir;
    private Path compiledOutputDir;

    @BeforeEach
    void before() {
        sourcesOutputDir = CompilationTestUtils.generatorOutput("mdsal807");
        compiledOutputDir = CompilationTestUtils.compilerOutput("mdsal807");
    }

    @AfterEach
    void after() throws Exception {
        CompilationTestUtils.cleanUp(sourcesOutputDir, compiledOutputDir);
    }

    @Test
    void testBitsTypedef() throws Exception {
        generateTestSources("/compilation/mdsal807", sourcesOutputDir);
        final var pmDataType = FileSearchUtil.getFiles(sourcesOutputDir).get("TableConfig.java");
        assertNotNull(pmDataType);

        assertEquals("""
            package org.opendaylight.yang.gen.v1.foo.norev;

            import com.google.common.collect.ImmutableSet;
            import java.lang.Override;
            import java.lang.String;
            import java.util.Arrays;
            import org.opendaylight.yangtools.binding.BitsTypeObject;
            import org.opendaylight.yangtools.binding.lib.CodeHelpers;

            /**
             *
             * <p>
             * This class represents the following YANG schema fragment defined in module <b>foo</b>
             * <pre>
             * typedef table-config {
             *   type bits {
             *     bit OFPTC_DEPRECATED_MASK {
             *       position 3;
             *     }
             *   }
             * }
             * </pre>
             */
            @javax.annotation.processing.Generated("mdsal-binding-generator")
            public class TableConfig implements BitsTypeObject, java.io.Serializable {
                @java.io.Serial
                private static final long serialVersionUID = 7965671183838203126L;

                protected static final ImmutableSet<String> VALID_NAMES = ImmutableSet.of("OFPTC_DEPRECATED_MASK");

                private final boolean _oFPTCDEPRECATEDMASK;

                public TableConfig(boolean _oFPTCDEPRECATEDMASK) {
                    this._oFPTCDEPRECATEDMASK = _oFPTCDEPRECATEDMASK;
                }

                protected TableConfig(TableConfig source) {
                    this._oFPTCDEPRECATEDMASK = source._oFPTCDEPRECATEDMASK;
                }

                public static TableConfig ofStringValue(String str) {
                    var values = CodeHelpers.btoValues(str, VALID_NAMES);
                    return new TableConfig(
                        values[0]);
                }

                public boolean getOFPTCDEPRECATEDMASK() {
                    return _oFPTCDEPRECATEDMASK;
                }

                @Override
                public ImmutableSet<String> validNames() {
                    return VALID_NAMES;
                }

                @Override
                public boolean[] values() {
                    return new boolean[] {
                        getOFPTCDEPRECATEDMASK()
                    };
                }

                @Override
                public final String stringValue() {
                    return CodeHelpers.btoSVB()
                        .bit("OFPTC_DEPRECATED_MASK", _oFPTCDEPRECATEDMASK)
                        .build();
                }

                @Override
                public final int hashCode() {
                    return Arrays.hashCode(values());
                }

                @Override
                public final boolean equals(Object obj) {
                    return this == obj || obj instanceof TableConfig other
                        && _oFPTCDEPRECATEDMASK == other._oFPTCDEPRECATEDMASK;
                }

                @Override
                public final String toString() {
                    return CodeHelpers.jcTSB(getClass())
                        .bit("OFPTC_DEPRECATED_MASK", _oFPTCDEPRECATEDMASK)
                        .build();
                }
            }
            """, Files.readString(pmDataType));
        CompilationTestUtils.testCompilation(sourcesOutputDir, compiledOutputDir);
    }
}
