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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DeclaredEffectiveStatementBase;

public class BinaryEffectiveStatementImpl extends DeclaredEffectiveStatementBase<String, TypeStatement>
        implements BinaryTypeDefinition {

    private static final String DESCRIPTION = "The binary built-in type represents any binary data, i.e., a sequence of octets.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#section-9.8";
    private static final String UNITS = "";

    private static final QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, "binary");
    private static final SchemaPath PATH = SchemaPath.create(true, QNAME);
    private static final Optional<String> OPTIONAL_EMPTY = Optional.of("");
    private final List<LengthConstraint> lengthConstraints;

    public BinaryEffectiveStatementImpl(
            final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {
        super(ctx);

        final LengthConstraint lengthConstraint =
                new LengthConstraintEffectiveImpl(0, Long.MAX_VALUE, OPTIONAL_EMPTY, OPTIONAL_EMPTY);
        lengthConstraints = Collections.singletonList(lengthConstraint);
    }

    @Override
    public List<LengthConstraint> getLengthConstraints() {
        return lengthConstraints;
    }

    @Override
    public BinaryTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return UNITS;
    }

    @Override
    public Object getDefaultValue() {
        return null;
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
}
