/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class GeneratedTypesStringTest {
    @Test
    void constantGenerationTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource(
            "/simple-string-demo.yang"));

        boolean typedefStringFound = false;
        boolean constantRegExListFound = false;
        boolean constantRegExListTypeGeneric = false;
        boolean constantRegExListTypeContainer = false;
        boolean noStringInReqExListFound = false;
        boolean constantRegExListValueOK = false;
        boolean constantRegExListTypeOneGeneric = false;
        for (var type : genTypes) {
            if (type instanceof GeneratedTransferObject genTO) {
                if (genTO.getName().equals("TypedefString")) {
                    typedefStringFound = true;

                    for (var con : genTO.getConstantDefinitions()) {
                        if (con.getName().equals("PATTERN_CONSTANTS")) {
                            constantRegExListFound = true;
                        } else {
                            break;
                        }
                        ParameterizedType paramType;
                        if (con.getType() instanceof ParameterizedType parameterized) {
                            paramType = parameterized;
                        } else {
                            break;
                        }

                        Type[] types;
                        if (paramType.getName().equals("List")) {
                            constantRegExListTypeContainer = true;
                            types = paramType.getActualTypeArguments();
                        } else {
                            break;
                        }

                        if (types.length == 1) {
                            constantRegExListTypeOneGeneric = true;
                        } else {
                            break;
                        }

                        if (types[0].getName().equals("String")) {
                            constantRegExListTypeGeneric = true;
                        } else {
                            break;
                        }

                        if (con.getValue() instanceof Map<?, ?> mapValue) {
                            constantRegExListValueOK = true;
                        } else {
                            break;
                        }

                        for (var e : mapValue.entrySet()) {
                            if (!(e.getKey() instanceof String) || !(e.getValue() instanceof String)) {
                                noStringInReqExListFound = true;
                                break;
                            }
                        }

                    }
                }
            }

        }

        assertTrue(typedefStringFound, "Typedef >>TypedefString<< wasn't found");
        assertTrue(constantRegExListFound, "Constant PATTERN_CONSTANTS is missing in TO");
        assertTrue(constantRegExListTypeContainer, "Constant PATTERN_CONSTANTS doesn't have correct container type");
        assertTrue(constantRegExListTypeOneGeneric, "Constant PATTERN_CONSTANTS has more than one generic type");
        assertTrue(constantRegExListTypeGeneric, "Constant PATTERN_CONSTANTS doesn't have correct generic type");
        assertTrue(constantRegExListValueOK, "Constant PATTERN_CONSTANTS doesn't contain List object");
        assertFalse(noStringInReqExListFound, "In list found other type than String");
    }
}
