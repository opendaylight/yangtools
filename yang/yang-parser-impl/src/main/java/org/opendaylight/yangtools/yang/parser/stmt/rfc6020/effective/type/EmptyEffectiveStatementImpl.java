package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;

import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.TypeUtils;

public class EmptyEffectiveStatementImpl extends
        EffectiveStatementBase<String, TypeStatement> implements
        EmptyTypeDefinition {

    public static final String LOCAL_NAME = TypeUtils.EMPTY;
    private static final QName QNAME = QName.create(
            YangConstants.RFC6020_YANG_MODULE, LOCAL_NAME);
    private static final SchemaPath PATH = SchemaPath.create(true, QNAME);
    private static final String DESCRIPTION = "The empty built-in type represents a leaf that does not have any value, it conveys information by its presence or absence.";
    private static final String REFERENCE = "https://tools.ietf.org/html/rfc6020#page-131";

    public EmptyEffectiveStatementImpl(
            StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx) {
        super(ctx);

    }

    @Override
    public EmptyTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return null;
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

    @Override
    public String toString() {
        return "type empty " + QNAME;
    }
}
