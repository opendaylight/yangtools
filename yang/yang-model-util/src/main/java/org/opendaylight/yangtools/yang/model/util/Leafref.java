/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

/**
 * The <code>default</code> implementation of Instance Leafref Type Definition
 * interface.
 *
 * @see LeafrefTypeDefinition
 * @deprecated Use {@link org.opendaylight.yangtools.yang.model.util.type.BaseTypes#leafrefTypeBuilder(SchemaPath)} instead
 */
@Deprecated
public final class Leafref implements LeafrefTypeDefinition {
    private static final QName NAME = BaseTypes.constructQName("leafref");
    private static final SchemaPath PATH = SchemaPath.create(true, NAME);
    private static final String DESCRIPTION = "The leafref type is used to reference a particular leaf instance in the data tree.";
    private static final String REF = "https://tools.ietf.org/html/rfc6020#section-9.9";

    private final RevisionAwareXPath xpath;
    private final SchemaPath path;

    @Deprecated
    public Leafref(final RevisionAwareXPath xpath) {
        this(PATH, xpath);
    }

    private Leafref(final SchemaPath path, final RevisionAwareXPath target) {
        this.path = Preconditions.checkNotNull(path,"path must be specified");
        this.xpath = Preconditions.checkNotNull(target,"target must not be null.");
    }

    public static Leafref create(final SchemaPath path, final RevisionAwareXPath target) {
        return new Leafref(path,target);
    }

    @Override
    public LeafrefTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return "";
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public QName getQName() {
        return NAME;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getReference() {
        return REF;
    }

    @Override
    public Status getStatus() {
        return Status.CURRENT;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }

    @Override
    public RevisionAwareXPath getPathStatement() {
        return xpath;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(xpath);
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Leafref other = (Leafref) obj;
        return Objects.equals(xpath, other.xpath);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("type ");
        builder.append(NAME);
        builder.append(" [xpath=");
        builder.append(xpath);
        builder.append("]");
        return builder.toString();
    }
}
