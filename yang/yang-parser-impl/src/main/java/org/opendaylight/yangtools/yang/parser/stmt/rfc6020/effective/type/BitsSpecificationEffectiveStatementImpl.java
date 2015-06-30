/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.model.util.BitsType;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public class BitsSpecificationEffectiveStatementImpl extends
        EffectiveStatementBase<String, TypeStatement.BitsSpecification> implements BitsTypeDefinition, TypeDefinitionEffectiveBuilder {

    private static final QName QNAME = BaseTypes.BITS_QNAME;
    private static final String DESCRIPTION = "The bits built-in type represents a bit set. "
            + "That is, a bits value is a set of flags identified by small integer position "
            + "numbers starting at 0. Each bit number has an assigned name.";

    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.7";
    private static final String UNITS = "";
    private final SchemaPath path;
    private final List<Bit> bits;

    public BitsSpecificationEffectiveStatementImpl(StmtContext<String, TypeStatement.BitsSpecification, EffectiveStatement<String, TypeStatement.BitsSpecification>> ctx) {
        super(ctx);

        List<Bit> bitsInit = new ArrayList<>();

        path = Utils.getSchemaPath(ctx.getParentContext()).createChild(QNAME);

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof Bit) {
                bitsInit.add(((Bit) effectiveStatement));
            }
        }

        bits = ImmutableList.copyOf(bitsInit);
    }

    @Override
    public List<Bit> getBits() {
        return bits;
    }

    @Override
    public BitsTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return UNITS;
    }

    @Override
    public Object getDefaultValue() {
        return bits;
    }

    @Override
    public QName getQName() {
        return QNAME;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public String getReference() {
        return REFERENCE;
    }

    @Override
    public Status getStatus() {
        return Status.CURRENT;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bits == null) ? 0 : bits.hashCode());
        result = prime * result + QNAME.hashCode();
        result = prime * result + path.hashCode();
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
        BitsSpecificationEffectiveStatementImpl other = (BitsSpecificationEffectiveStatementImpl) obj;
        if (bits == null) {
            if (other.bits != null) {
                return false;
            }
        } else if (!bits.equals(other.bits)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(BitsSpecificationEffectiveStatementImpl.class.getSimpleName());
        builder.append(" [name=");
        builder.append(QNAME);
        builder.append(", path=");
        builder.append(path);
        builder.append(", description=");
        builder.append(DESCRIPTION);
        builder.append(", reference=");
        builder.append(REFERENCE);
        builder.append(", bits=");
        builder.append(bits);
        builder.append(", units=");
        builder.append(UNITS);
        builder.append("]");
        return builder.toString();
    }

    private BitsType bitsTypeInstance = null;
    @Override
    public TypeDefinition<?> buildType() {

        if(bitsTypeInstance != null) {
            return bitsTypeInstance;
        }

        bitsTypeInstance = BitsType.create(path, bits);

        return bitsTypeInstance;
    }
}
