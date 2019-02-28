/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.model.netconf;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.Beta;
import java.io.IOException;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.impl.YangParserFactoryImpl;

@Beta
public final class ModelSupport {
    /**
     * SchemaNode representing the definition of "nc:operation" metadata.
     */
    public static final AnnotationSchemaNode OPERATION_ANNOTATION;

    static {
        final SchemaContext parser;
        try {
            parser = new YangParserFactoryImpl().createParser()
                    .addSource(YangTextSchemaSource.forResource("/ietf-netconf.yang"))
                    .addLibSource(YangTextSchemaSource.forResource("/ietf-yang-metadata@2016-08-05.yang"))
                    .buildSchemaContext();
        } catch (YangParserException | IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        final AnnotationSchemaNode schemaNode = (AnnotationSchemaNode) parser.getUnknownSchemaNodes().get(0);
        checkState("operation".equals(schemaNode.getNodeParameter()), "Unexpected node %s", schemaNode);
        OPERATION_ANNOTATION = schemaNode;
    }

    private ModelSupport() {
        // Hidden
    }
}
