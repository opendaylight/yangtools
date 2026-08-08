/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendaylight.yangtools.binding.codegen.FileSearchUtil.getFiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;

public class BuilderGeneratorTest extends BaseCompilationTest {
    private static Path SOURCES;
    private static List<Archetype> TYPES;
    private static Map<String, Path> FILES;

    @BeforeAll
    static void beforeAll() {
        SOURCES = CompilationTestUtils.generatorOutput("test-types");
        TYPES = generateTestSources("/compilation/test-types", SOURCES);
        assertEquals(27, TYPES.size());
        FILES = getFiles(SOURCES);
        assertEquals(47, FILES.size());
    }

    @AfterAll
    static void afterAll() throws Exception {
        CompilationTestUtils.cleanUp(SOURCES);
        FILES = null;
        TYPES = null;
        SOURCES = null;
    }

    private static AbstractStringAssert<?> assertFileContent(final String name) {
        final var file = FILES.get(name);
        assertNotNull(file, name + " not found");
        return assertThat(assertDoesNotThrow(() -> Files.readString(file)));
    }

    @Test
    void builderTemplateGenerateHashcodeWithPropertyTest() {
        assertFileContent("Container1Def.java").contains("""

                @Override
                default int javaHC() {
                    return CodeHelpers.jcHC1(this, getNodeIdString());
                }

            """);
    }

    @Test
    @Disabled("FIXME: define a type which contains the equivalent")
    void builderTemplateGenerateHashCodeWithoutAnyPropertyTest() {
        assertFileContent("").contains("""
            @Override
            default int javaHC() {
                return 1;
            }
            """);
    }

    @Test
    void builderTemplateGenerateHashCodeWithMorePropertiesTest() {
        assertFileContent("IdList.java").contains("""

                @Override
                default int javaHC() {
                    return CodeHelpers.jcHCN(this,
                        getKey1(),
                        getKey2());
                }

            """);
    }

    @Test
    @Disabled("FIXME: define a type which contains the equivalent")
    void builderTemplateGenerateHashCodeWithoutPropertyWithAugmentTest() {
        assertFileContent("").contains("""
            @Override
            default int javaHC() {
                return CodeHelpers.jcHC0(this);
            }
            """);
    }

    @Test
    @Disabled("FIXME: define a type which contains the equivalent")
    void builderTemplateGenerateToStringWithPropertyTest() {
        assertFileContent("").contains("""
            @Override
            default String javaTS() {
                return CodeHelpers.jcTS1(test.test.class, "test", gettest());
            }
            """);
    }

    @Test
    @Disabled("FIXME: define a type which contains the equivalent")
    void builderTemplateGenerateToStringWithoutAnyPropertyTest() {
        assertFileContent("").contains("""
            @Override
            default String javaTS() {
                return CodeHelpers.jcTS0(test.test.class);
            }
            """);
    }

    @Test
    @Disabled("FIXME: define a type which contains the equivalent")
    void builderTemplateGenerateToStringWithMorePropertiesTest() {
        assertFileContent("").contains("""
            @Override
            default String javaTS() {
                return CodeHelpers.jcTSB(test.test.class)
                    .prop("test1", gettest1())
                    .prop("test2", gettest2())
                    .build();
            }
            """);
    }

    @Test
    @Disabled("FIXME: define a type which contains the equivalent")
    void builderTemplateGenerateToStringWithoutPropertyWithAugmentTest() {
        assertFileContent("").contains("""
            @Override
            default String javaTS() {
                return CodeHelpers.jcTS0(this);
            }
            """);
    }

    @Test
    void builderTemplateGenerateToStringWithPropertyWithAugmentTest() {
        assertFileContent("Container1Def.java").endsWith("""

                @Override
                default String javaTS() {
                    return CodeHelpers.jcTS1(this, "nodeIdString", getNodeIdString());
                }
            }
            """);
    }

    @Test
    void builderTemplateGenerateToStringWithMorePropertiesWithAugmentTest() {
        assertFileContent("Nodes.java").endsWith("""

                @Override
                default String javaTS() {
                    return CodeHelpers.jcTSB(this)
                        .prop("id16", getId16())
                        .prop("id16Def", getId16Def())
                        .prop("id32", getId32())
                        .prop("id32Def", getId32Def())
                        .prop("id64", getId64())
                        .prop("id64Def", getId64Def())
                        .prop("id8", getId8())
                        .prop("id8Def", getId8Def())
                        .prop("idBinary", getIdBinary())
                        .prop("idBinaryDef", getIdBinaryDef())
                        .prop("idBits", getIdBits())
                        .prop("idBitsDef", getIdBitsDef())
                        .prop("idBoolean", getIdBoolean())
                        .prop("idBooleanDef", getIdBooleanDef())
                        .prop("idContainer1", getIdContainer1())
                        .prop("idContainer2", getIdContainer2())
                        .prop("idDecimal64", getIdDecimal64())
                        .prop("idDecimal64Def", getIdDecimal64Def())
                        .prop("idEmpty", getIdEmpty())
                        .prop("idEmptyDef", getIdEmptyDef())
                        .prop("idEnumeration", getIdEnumeration())
                        .prop("idEnumerationDef", getIdEnumerationDef())
                        .prop("idGroupContainer", getIdGroupContainer())
                        .prop("idGroupLeafString", getIdGroupLeafString())
                        .prop("idIdentityref", getIdIdentityref())
                        .prop("idIdentityrefDef", getIdIdentityrefDef())
                        .prop("idInstanceIdentifier", getIdInstanceIdentifier())
                        .prop("idInstanceIdentifierDef", getIdInstanceIdentifierDef())
                        .prop("idLeafref", getIdLeafref())
                        .prop("idLeafrefContainer1", getIdLeafrefContainer1())
                        .prop("idLeafrefContainer1Def", getIdLeafrefContainer1Def())
                        .prop("idLeafrefDef", getIdLeafrefDef())
                        .prop("idList", getIdList())
                        .prop("idString", getIdString())
                        .prop("idStringDef", getIdStringDef())
                        .prop("idU16", getIdU16())
                        .prop("idU16Def", getIdU16Def())
                        .prop("idU32", getIdU32())
                        .prop("idU32Def", getIdU32Def())
                        .prop("idU64", getIdU64())
                        .prop("idU64Def", getIdU64Def())
                        .prop("idU8", getIdU8())
                        .prop("idU8Def", getIdU8Def())
                        .prop("idUnion", getIdUnion())
                        .prop("idUnionDef", getIdUnionDef())
                        .build();
                }
            }
            """);
    }

    @Test
    void builderTemplateGenerateToEqualsComparingOrderTest() {
        final var nodesName =
            TypeName.of("org.opendaylight.yang.gen.v1.urn.opendaylight.test.types.rev200513", "Nodes");
        final var nodes = assertInstanceOf(ContainerObjectArchetype.class, TYPES.stream()
            .filter(type -> nodesName.equals(type.name()))
            .findFirst()
            .orElseThrow());

        final var sortedProperties = DataContainerGetters.of(nodes).allMethods()
                .sorted(ByTypeMemberComparator.INSTANCE)
                .map(GetterShape::propName)
                .toList();

        assertEquals(List.of(
                // numeric types (boolean, byte, short, int, long, Uint*, Decimal64), identityrefs, Empty
                "id16", "id16Def", "id32", "id32Def", "id64", "id64Def", "id8", "id8Def", "idBoolean", "idBooleanDef",
                "idDecimal64", "idDecimal64Def","idEmpty", "idEmptyDef", "idIdentityref", "idIdentityrefDef",
                "idLeafref", "idLeafrefDef", "idU16", "idU16Def", "idU32", "idU32Def", "idU64", "idU64Def", "idU8",
                "idU8Def",
                // string, binary, bits
                "idBinary", "idBinaryDef", "idBits", "idBitsDef", "idGroupLeafString", "idLeafrefContainer1",
                "idLeafrefContainer1Def", "idString", "idStringDef",
                // instance identifier
                "idInstanceIdentifier", "idInstanceIdentifierDef",
                // other types
                "idContainer1", "idContainer2", "idEnumeration", "idEnumerationDef",
                "idGroupContainer", "idList", "idUnion", "idUnionDef"), sortedProperties);
    }
}
