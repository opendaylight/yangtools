/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Thrown when codec was used with data which are not modeled
 * and available in schema used by codec.
 *
 */
public class MissingSchemaException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    protected MissingSchemaException(final String msg) {
        super(msg);
    }

    protected MissingSchemaException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    private static MissingSchemaException create(final String format, final Object... args) {
        return new MissingSchemaException(String.format(format, args));
    }

    static void checkModulePresent(final SchemaContext schemaContext, final QName name) {
        if(schemaContext.findModuleByNamespaceAndRevision(name.getNamespace(), name.getRevision()) == null) {
            throw MissingSchemaException.create("Module %s is not present in current schema context.",name.getModule());
        }
    }

    static void checkModulePresent(final SchemaContext schemaContext, final YangInstanceIdentifier.PathArgument child) {
        checkModulePresent(schemaContext, extractName(child));
    }

    private static QName extractName(final PathArgument child) {
        if(child instanceof YangInstanceIdentifier.AugmentationIdentifier) {
            final Set<QName> children = ((YangInstanceIdentifier.AugmentationIdentifier) child).getPossibleChildNames();
            Preconditions.checkArgument(!children.isEmpty(),"Augmentation without childs must not be used in data");
            return children.iterator().next();
        }
        return child.getNodeType();
    }




}
