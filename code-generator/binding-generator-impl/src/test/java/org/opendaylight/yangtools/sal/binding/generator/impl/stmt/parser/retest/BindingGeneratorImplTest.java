/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl.stmt.parser.retest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveSchemaContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;

public class BindingGeneratorImplTest {

    private static final YangStatementSourceImpl NETWORK_TOPOLOGY_20131021 = new YangStatementSourceImpl(
            "/isis-topology/network-topology@2013-10-21.yang", false);

    private static final YangStatementSourceImpl ISIS_20131021 = new YangStatementSourceImpl(
            "/isis-topology/isis-topology@2013-10-21.yang", false);

    private static final YangStatementSourceImpl L3_20131021 = new YangStatementSourceImpl(
            "/isis-topology/l3-unicast-igp-topology@2013-10-21.yang", false);

    @Test
    public void isisTotpologyStatementParserTest() throws IOException,
            YangSyntaxErrorException, URISyntaxException, SourceException,
            ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR
                .newBuild();

        reactor.addSources(ISIS_20131021, L3_20131021,
                NETWORK_TOPOLOGY_20131021);

        EffectiveSchemaContext context = reactor.buildEffective();
        assertNotNull(context);

        List<Type> generateTypes = new BindingGeneratorImpl(false)
                .generateTypes(context);

        assertFalse(generateTypes.isEmpty());
    }

    @Test
    public void choiceNodeGenerationTest() throws IOException,
            YangSyntaxErrorException, URISyntaxException, SourceException, ReactorException {
        File resourceFile = new File(getClass().getResource(
                "/binding-generator-impl-test/choice-test.yang").toURI());

        SchemaContext context = RetestUtils.parseYangSources(resourceFile);

        List<Type> generateTypes = new BindingGeneratorImpl(false)
                .generateTypes(context);

        GeneratedType choiceTestData = null;
        GeneratedType myRootContainer = null;
        GeneratedType myList = null;
        GeneratedType myContainer = null;
        GeneratedType myList2 = null;
        GeneratedType myContainer2 = null;

        for (Type type : generateTypes) {
            switch (type.getName()) {
            case "ChoiceTestData":
                choiceTestData = (GeneratedType) type;
                break;
            case "Myrootcontainer":
                myRootContainer = (GeneratedType) type;
                break;
            case "Mylist":
                myList = (GeneratedType) type;
                break;
            case "Mylist2":
                myList2 = (GeneratedType) type;
                break;
            case "Mycontainer":
                myContainer = (GeneratedType) type;
                break;
            case "Mycontainer2":
                myContainer2 = (GeneratedType) type;
                break;
            }
        }

        assertNotNull(choiceTestData);
        assertNotNull(myRootContainer);
        assertNotNull(myList);
        assertNotNull(myContainer);
        assertNotNull(myList2);
        assertNotNull(myContainer2);

        List<Type> implements1 = myContainer.getImplements();
        Type childOfParamType = null;
        for (Type type : implements1) {
            if (type.getName().equals("ChildOf")) {
                childOfParamType = ((ParameterizedType) type)
                        .getActualTypeArguments()[0];
                break;
            }
        }
        assertNotNull(childOfParamType);
        assertTrue(childOfParamType.getName().equals("ChoiceTestData"));

        implements1 = myList.getImplements();
        childOfParamType = null;
        for (Type type : implements1) {
            if (type.getName().equals("ChildOf")) {
                childOfParamType = ((ParameterizedType) type)
                        .getActualTypeArguments()[0];
                break;
            }
        }
        assertNotNull(childOfParamType);
        assertTrue(childOfParamType.getName().equals("ChoiceTestData"));

        implements1 = myContainer2.getImplements();
        childOfParamType = null;
        for (Type type : implements1) {
            if (type.getName().equals("ChildOf")) {
                childOfParamType = ((ParameterizedType) type)
                        .getActualTypeArguments()[0];
                break;
            }
        }
        assertNotNull(childOfParamType);
        assertTrue(childOfParamType.getName().equals("Myrootcontainer"));

        implements1 = myList2.getImplements();
        childOfParamType = null;
        for (Type type : implements1) {
            if (type.getName().equals("ChildOf")) {
                childOfParamType = ((ParameterizedType) type)
                        .getActualTypeArguments()[0];
                break;
            }
        }
        assertNotNull(childOfParamType);
        assertTrue(childOfParamType.getName().equals("Myrootcontainer"));

    }

    @Test
    public void notificationGenerationTest() throws IOException,
            YangSyntaxErrorException, URISyntaxException, SourceException, ReactorException {
        File resourceFile = new File(getClass().getResource(
                "/binding-generator-impl-test/notification-test.yang").toURI());

        SchemaContext context = RetestUtils.parseYangSources(resourceFile);

        List<Type> generateTypes = new BindingGeneratorImpl(false)
                .generateTypes(context);

        GeneratedType foo = null;
        for (Type type : generateTypes) {
            if (type.getName().equals("Foo")) {
                foo = (GeneratedType) type;
                break;
            }
        }

        Type childOf = null;
        Type dataObject = null;
        List<Type> impl = foo.getImplements();
        for (Type type : impl) {
            switch (type.getName()) {
            case "ChildOf":
                childOf = type;
                break;
            case "DataObject":
                dataObject = type;
                break;
            }
        }

        assertNull(childOf);
        assertNotNull(dataObject);
    }

}
