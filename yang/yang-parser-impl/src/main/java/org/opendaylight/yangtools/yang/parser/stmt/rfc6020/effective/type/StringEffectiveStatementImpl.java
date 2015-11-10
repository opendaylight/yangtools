/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.base.Optional;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;

public class StringEffectiveStatementImpl extends DeclaredEffectiveStatementBase<String, TypeStatement> implements
        StringTypeDefinition {

    public static final String LOCAL_NAME = TypeUtils.STRING;
    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, LOCAL_NAME);
    private static final SchemaPath PATH = SchemaPath.create(true, QNAME);
    private static final String DEFAULT_VALUE = "";
    private static final String DESCRIPTION = "The string built-in type represents human-readable strings in YANG. "
            + "Legal characters are tab, carriage return, line feed, and the legal "
            + "characters of Unicode and ISO/IEC 10646";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.4";
    private static final String UNITS = "";
    private static final Optional<String> OPTIONAL_EMPTY = Optional.of("");

    private final List<LengthConstraint> lengthConstraints;
    private final List<PatternConstraint> patternConstraints;

    public StringEffectiveStatementImpl(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {
        super(ctx);

        final LengthConstraint defLength = new LengthConstraintEffectiveImpl(0, Integer.MAX_VALUE, OPTIONAL_EMPTY,
            OPTIONAL_EMPTY);

        lengthConstraints = Collections.singletonList(defLength);
        patternConstraints = Collections.emptyList();
    }

    @Override
    public List<LengthConstraint> getLengthConstraints() {
        return lengthConstraints;
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
        result = prime * result + Objects.hashCode(lengthConstraints);
        result = prime * result + QNAME.hashCode();
        result = prime * result + PATH.hashCode();
        result = prime * result + Objects.hashCode(patternConstraints);
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
        StringEffectiveStatementImpl other = (StringEffectiveStatementImpl) obj;
        if (!Objects.equals(lengthConstraints, other.lengthConstraints)) {
            return false;
        }
        if (!Objects.equals(patternConstraints, other.patternConstraints)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
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
        builder.append(lengthConstraints);
        builder.append(", patterns=");
        builder.append(patternConstraints);
        builder.append(", units=");
        builder.append(UNITS);
        builder.append("]");
        return builder.toString();
    }
}
