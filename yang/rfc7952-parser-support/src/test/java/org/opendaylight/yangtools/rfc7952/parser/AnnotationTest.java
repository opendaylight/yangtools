/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;

public class AnnotationTest {
    private static final QName LAST_MODIFIED_QNAME = QName.create("http://example.org/example-last-modified",
            "last-modified");
    private static CrossSourceStatementReactor reactor;

    @BeforeClass
    public static void createReactor() {
        reactor = Metadata.addToReactorBuilder(RFC7950Reactors.vanillaReactorBuilder()).build();
    }

    @Test
    public void testAnnotationResolution() throws ReactorException, IOException, YangSyntaxErrorException {
        final BuildAction build = reactor.newBuild();
        build.addSource(YangStatementStreamSource.create(
            YangTextSchemaSource.forResource("/ietf-yang-metadata@2016-08-05.yang")));
        build.addSource(YangStatementStreamSource.create(
            YangTextSchemaSource.forResource("/example-last-modified.yang")));
        final SchemaContext context = build.buildEffective();

        final Map<QName, AnnotationSchemaNode> annotations = AnnotationSchemaNode.findAll(context);
        assertEquals(1, annotations.size());
        final AnnotationSchemaNode annotation = annotations.get(LAST_MODIFIED_QNAME);
        assertNotNull(annotation);

        final Optional<AnnotationSchemaNode> findAnnotation = AnnotationSchemaNode.find(context, LAST_MODIFIED_QNAME);
        assertTrue(findAnnotation.isPresent());
        assertSame(annotation, findAnnotation.get());

        assertEquals(BaseTypes.stringType(), annotation.getType());
        assertEquals(Optional.empty(), annotation.getReference());
        assertEquals(Optional.of("This annotation contains the date and time when the\n"
                + "           annotated instance was last modified (or created)."), annotation.getDescription());
    }
}
