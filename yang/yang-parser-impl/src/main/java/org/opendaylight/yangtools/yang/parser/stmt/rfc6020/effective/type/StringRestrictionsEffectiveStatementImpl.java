/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import java.util.Collections;
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;

public class StringRestrictionsEffectiveStatementImpl extends
        EffectiveStatementBase<String, TypeStatement.StringRestrictions> implements StringTypeDefinition {

    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, "string");
    private static final SchemaPath PATH = SchemaPath.create(true, QNAME);
    private static final String DEFAULT_VALUE = "";
    private static final String DESCRIPTION = "";
    private static final String REFERENCE = "";
    private final List<LengthConstraint> lengthStatements;
    private final List<PatternConstraint> patternConstraints;
    private static final String UNITS = "";

    public StringRestrictionsEffectiveStatementImpl(StmtContext<String, TypeStatement.StringRestrictions, ?> ctx) {
        super(ctx);

        final LengthConstraint defLength = new LengthConstraintEffectiveImpl(0, Integer.MAX_VALUE, Optional.of(""),
                Optional.of(""));

        lengthStatements = Collections.singletonList(defLength);
        patternConstraints = Collections.emptyList();
    }

    @Override
    public List<LengthConstraint> getLengthConstraints() {
        return lengthStatements;
    }

    @Override
    public List<PatternConstraint> getPatternConstraints() {
        return patternConstraints;
    }

    @Override
    public StringTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return UNITS;
    }

    @Override
    public Object getDefaultValue() {
        return DEFAULT_VALUE;
    }

    @Override
    public QName getQName() {
        return QNAME;
    }

    @Override
    public SchemaPath getPath() {
        return PATH;
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
        result = prime * result + ((lengthStatements == null) ? 0 : lengthStatements.hashCode());
        result = prime * result + QNAME.hashCode();
        result = prime * result + PATH.hashCode();
        result = prime * result + ((patternConstraints == null) ? 0 : patternConstraints.hashCode());
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
        StringRestrictionsEffectiveStatementImpl other = (StringRestrictionsEffectiveStatementImpl) obj;
        if (lengthStatements == null) {
            if (other.lengthStatements != null) {
                return false;
            }
        } else if (!lengthStatements.equals(other.lengthStatements)) {
            return false;
        }
        if (patternConstraints == null) {
            if (other.patternConstraints != null) {
                return false;
            }
        } else if (!patternConstraints.equals(other.patternConstraints)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(StringRestrictionsEffectiveStatementImpl.class.getSimpleName());
        builder.append(" [name=");
        builder.append(QNAME);
        builder.append(", path=");
        builder.append(PATH);
        builder.append(", defaultValue=");
        builder.append(DEFAULT_VALUE);
        builder.append(", description=");
        builder.append(DESCRIPTION);
        builder.append(", reference=");
        builder.append(REFERENCE);
        builder.append(", lengthStatements=");
        builder.append(lengthStatements);
        builder.append(", patterns=");
        builder.append(patternConstraints);
        builder.append(", units=");
        builder.append(UNITS);
        builder.append("]");
        return builder.toString();
    }
}
