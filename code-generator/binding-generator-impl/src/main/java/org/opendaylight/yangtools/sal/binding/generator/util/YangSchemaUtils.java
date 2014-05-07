/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.net.URI;
import java.util.Date;
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.NamespaceRevisionAware;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;

public final class YangSchemaUtils {
    public static final String AUGMENT_IDENTIFIER = "augment-identifier";

    private YangSchemaUtils() {
        throw new UnsupportedOperationException("Helper class. Instantiation is prohibited");
    }

    public static QName getAugmentationQName(final AugmentationSchema augmentation) {
        checkNotNull(augmentation, "Augmentation must not be null.");
        QName identifier = getAugmentationIdentifier(augmentation);
        if(identifier != null) {
            return identifier;
        }
        URI namespace = null;
        Date revision = null;
        if(augmentation instanceof NamespaceRevisionAware) {
            namespace = ((NamespaceRevisionAware) augmentation).getNamespace();
            revision = ((NamespaceRevisionAware) augmentation).getRevision();
        }
        if(namespace == null || revision == null) {
            for(DataSchemaNode child : augmentation.getChildNodes()) {
                // Derive QName from child nodes
                if(!child.isAugmenting()) {
                    namespace = child.getQName().getNamespace();
                    revision = child.getQName().getRevision();
                    break;
                }
            }
        }
        checkState(namespace != null, "Augmentation namespace must not be null");
        checkState(revision != null, "Augmentation revision must not be null");
        // FIXME: Allways return a qname with module namespace.
        return QName.create(namespace,revision, "foo_augment");
    }

    public static QName getAugmentationIdentifier(final AugmentationSchema augmentation) {
        for(UnknownSchemaNode extension : augmentation.getUnknownSchemaNodes()) {
            if(AUGMENT_IDENTIFIER.equals(extension.getNodeType().getLocalName())) {
                return extension.getQName();
            }
        }
        return null;
    }

    public static TypeDefinition<?> findTypeDefinition(final SchemaContext context, final SchemaPath path) {
        List<QName> arguments = path.getPath();
        QName first = arguments.get(0);
        QName typeQName = arguments.get(arguments.size() -1);
        DataNodeContainer previous = context.findModuleByNamespaceAndRevision(first.getNamespace(), first.getRevision());
        if(previous == null) {
            return null;
        }
        checkArgument(arguments.size() == 1);
        for(TypeDefinition<?> typedef : previous.getTypeDefinitions()) {
            if(typedef.getQName().equals(typeQName)) {
                return typedef;
            }
        }
        return null;
    }
}
