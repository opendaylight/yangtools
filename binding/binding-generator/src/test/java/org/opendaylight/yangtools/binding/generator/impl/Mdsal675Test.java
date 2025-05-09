/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.BaseYangTypes;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal675Test {
    private static final String PACKAGE = "org.opendaylight.yang.gen.v1.urn.test.yang.data.demo.norev.";
    private static final String PACKAGE2 = "org.opendaylight.yang.gen.v1.urn.test.yang.data.naming.norev.";
    private static final String MODULE_CLASS_NAME = PACKAGE + "YangDataDemoData";
    private static final String ROOT_CONTAINER_CLASS_NAME = PACKAGE + "RootContainer";
    private static final Map<String, Type> INTERFACE_METHODS =
            Map.of("implementedInterface", Types.typeForClass(Class.class));

    @Test
    void yangDataGen() {
        final var allGenTypes = DefaultBindingGenerator.generateFor(
                YangParserTestUtils.parseYangResources(Mdsal675Test.class,
                        "/yang-data-models/ietf-restconf.yang", "/yang-data-models/yang-data-demo.yang"));
        assertNotNull(allGenTypes);
        assertEquals(29, allGenTypes.size());
        final var genTypesMap = allGenTypes.stream()
            .collect(ImmutableMap.toImmutableMap(type -> type.getIdentifier().toString(), Function.identity()));

        // ensure generated yang-data classes contain getters for inner structure types

        // yang-data > container
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithContainer"),
                assertGenType(genTypesMap, PACKAGE + "yang.data.with.container.ContainerFromYangData"),
                List.of("getContainerFromYangData", "nonnullContainerFromYangData"));
        // yang-data > list
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithList"),
                Types.listTypeFor(assertGenType(genTypesMap, PACKAGE + "yang.data.with.list.ListFromYangData")),
                List.of("getListFromYangData", "nonnullListFromYangData"));
        // yang-data > leaf
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithLeaf"),
                BaseYangTypes.STRING_TYPE,
                List.of("getLeafFromYangData", "requireLeafFromYangData"));
        // yang-data > leaf-list
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithLeafList"),
                Types.setTypeFor(BaseYangTypes.STRING_TYPE),
                List.of("getLeafListFromYangData", "requireLeafListFromYangData"));
        // yang-data > anydata
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithAnydata"),
                assertGenType(genTypesMap, PACKAGE + "yang.data.with.anydata.AnydataFromYangData"),
                List.of("getAnydataFromYangData", "requireAnydataFromYangData"));
        // yang-data > anyxml
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithAnyxml"),
                assertGenType(genTypesMap, PACKAGE + "yang.data.with.anyxml.AnyxmlFromYangData"),
                List.of("getAnyxmlFromYangData", "requireAnyxmlFromYangData"));

        // ensure generated yang-data classes extending inner group so group content is reachable

        // yang-data > uses > group > container
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithContainerFromGroup"),
                assertGenType(genTypesMap, PACKAGE + "GrpForContainer"),
                assertGenType(genTypesMap, PACKAGE + "grp._for.container.ContainerFromGroup"),
                List.of("getContainerFromGroup", "nonnullContainerFromGroup"));
        // yang-data > uses > group > list
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithListFromGroup"),
                assertGenType(genTypesMap, PACKAGE + "GrpForList"),
                Types.listTypeFor(assertGenType(genTypesMap, PACKAGE + "grp._for.list.ListFromGroup")),
                List.of("getListFromGroup", "nonnullListFromGroup")
        );
        // yang-data > uses > group > leaf
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithLeafFromGroup"),
                assertGenType(genTypesMap, PACKAGE + "GrpForLeaf"),
                BaseYangTypes.UINT32_TYPE,
                List.of("getLeafFromGroup", "requireLeafFromGroup"));
        // yang-data > uses > group > leaf-list
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithLeafListFromGroup"),
                assertGenType(genTypesMap, PACKAGE + "GrpForLeafList"),
                Types.setTypeFor(BaseYangTypes.UINT32_TYPE),
                List.of("getLeafListFromGroup", "requireLeafListFromGroup"));
        // yang-data > uses > group > anydata
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithAnydataFromGroup"),
                assertGenType(genTypesMap, PACKAGE + "GrpForAnydata"),
                assertGenType(genTypesMap, PACKAGE + "grp._for.anydata.AnydataFromGroup"),
                List.of("getAnydataFromGroup", "requireAnydataFromGroup"));
        // yang-data > uses > group > anyxml
        assertYangDataGenType(
                assertGenType(genTypesMap, PACKAGE + "YangDataWithAnyxmlFromGroup"),
                assertGenType(genTypesMap, PACKAGE + "GrpForAnyxml"),
                assertGenType(genTypesMap, PACKAGE + "grp._for.anyxml.AnyxmlFromGroup"),
                List.of("getAnyxmlFromGroup", "requireAnyxmlFromGroup"));

        // ensure module class has only getter for root container
        final var moduleType = assertGenType(genTypesMap, MODULE_CLASS_NAME);
        assertNotNull(moduleType.getMethodDefinitions());
        assertEquals(List.of("getRootContainer"),
                moduleType.getMethodDefinitions().stream().map(MethodSignature::getName)
                        .filter(methodName -> methodName.startsWith("get")).toList());

        // ensure yang-data at non-top level is ignored (no getters in parent container)
        final var rootContainerType = assertGenType(genTypesMap, ROOT_CONTAINER_CLASS_NAME);
        assertNotNull(rootContainerType.getMethodDefinitions());
        assertThat(rootContainerType.getMethodDefinitions()).noneMatch(method -> method.getName().startsWith("get"));
    }

    @Test
    void yangDataNamingStrategy() {
        final var allGenTypes = DefaultBindingGenerator.generateFor(
                YangParserTestUtils.parseYangResources(Mdsal675Test.class,
                        "/yang-data-models/ietf-restconf.yang", "/yang-data-models/yang-data-naming.yang"));
        assertNotNull(allGenTypes);
        assertEquals(22, allGenTypes.size());
        final var genTypeNames = allGenTypes.stream().map(type -> type.getIdentifier().toString())
            .collect(Collectors.toSet());

        // template name is not compliant to YANG identifier -> char encoding used, name starts with $ char
        // a) latin1, but not a valid Java identifier
        assertThat(genTypeNames).contains(PACKAGE2 + "$ľaľa$20$ho$2C$$20$papľuha$2C$$20$ogrcal$20$mi$20$krpce$21$");
        // b) cyrillic, but a valid Java identifier
        assertThat(genTypeNames).contains(PACKAGE2 + "привет");

        // template name is compliant to yang identifier -> camel-case used
        assertThat(genTypeNames).contains(PACKAGE2 + "IdentifierCompliantName");

        // name collision with a typedef
        assertThat(genTypeNames).contains(PACKAGE2 + "Collision1$T");
        assertThat(genTypeNames).contains(PACKAGE2 + "collision1.Collision1");
        assertThat(genTypeNames).contains(PACKAGE2 + "Collision1$YD");

        // name collision with top level container
        assertThat(genTypeNames).contains(PACKAGE2 + "Collision2");
        assertThat(genTypeNames).contains(PACKAGE2 + "Collision2$YD");

        // name collision with group used
        assertThat(genTypeNames).contains(PACKAGE2 + "Collision3$G");
        assertThat(genTypeNames).contains(PACKAGE2 + "Collision3$YD");

        // rc:yang-data .-/#
        assertThat(genTypeNames).contains(PACKAGE2 + "$$2E$$2D$$2F$$23$");
        assertThat(genTypeNames).contains(PACKAGE2 + "$$2e$$2d$$2f$$23$$.Foo");

        // rc:yang-data -./#
        assertThat(genTypeNames).contains(PACKAGE2 + "$$2D$$2E$$2F$$23$");
        assertThat(genTypeNames).contains(PACKAGE2 + "$$2d$$2e$$2f$$23$$.Foo");
    }

    private static GeneratedType assertGenType(final Map<String, GeneratedType> genTypesMap, final String className) {
        final var ret = genTypesMap.get(className);
        assertNotNull(ret, "no type generated: " + className);
        return ret;
    }

    private static void assertYangDataGenType(final GeneratedType yangDataType, final Type contentType,
            final List<String> getterMethods) {
        assertImplements(yangDataType, BindingTypes.yangData(yangDataType));
        INTERFACE_METHODS.forEach((name, type) -> assertHasMethod(yangDataType, name, type));
        for (final String methodName : getterMethods) {
            assertHasMethod(yangDataType, methodName, contentType);
        }
    }

    private static void assertYangDataGenType(final GeneratedType yangDataType, final GeneratedType groupType,
            final Type contentType, final List<String> getterMethods) {
        assertImplements(yangDataType, BindingTypes.yangData(yangDataType));
        assertImplements(yangDataType, groupType);
        INTERFACE_METHODS.forEach((name, type) -> assertHasMethod(yangDataType, name, type));
        for (final String methodName : getterMethods) {
            assertHasMethod(groupType, methodName, contentType);
        }
    }

    private static void assertHasMethod(final GeneratedType genType, final String methodName,
            final Type returnType) {
        assertThat(genType.getMethodDefinitions())
            .anyMatch(method -> methodName.equals(method.getName()) && returnType.equals(method.getReturnType()));
    }

    private static void assertImplements(final GeneratedType genType, final Type implementedType) {
        assertThat(genType.getImplements()).contains(implementedType);
    }
}
