package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import java.util.Set;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.TypeOfCopy;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

public class GroupingEffectiveStatementImpl extends
        AbstractEffectiveDocumentedDataNodeContainer<QName, GroupingStatement> implements GroupingDefinition {
    private final QName qname;
    private final SchemaPath path;

    private boolean addedByUses;
    private List<UnknownSchemaNode> unknownNodes;

    public GroupingEffectiveStatementImpl(
            StmtContext<QName, GroupingStatement, EffectiveStatement<QName, GroupingStatement>> ctx) {
        super(ctx);

        qname = ctx.getStatementArgument();
        path = Utils.getSchemaPath(ctx);

        initCopyType(ctx);
        initSubstatementCollections();
    }

    private void initCopyType(
            StmtContext<QName, GroupingStatement, EffectiveStatement<QName, GroupingStatement>> ctx) {

        Set<TypeOfCopy> copyTypesFromOriginal = StmtContextUtils.getCopyTypesFromOriginal(ctx);

        if(copyTypesFromOriginal.contains(TypeOfCopy.ADDED_BY_USES)) {
            addedByUses = true;
        }
    }

    private void initSubstatementCollections() {
        Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements = effectiveSubstatements();

        unknownNodes = new LinkedList<>();

        for (EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements) {
            if (effectiveStatement instanceof UnknownSchemaNode) {
                UnknownSchemaNode unknownNode = (UnknownSchemaNode) effectiveStatement;
                unknownNodes.add(unknownNode);
            }
        }

    }

    @Override
    public QName getQName() {
        return qname;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public boolean isAddedByUses() {
        return addedByUses;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((qname == null) ? 0 : qname.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
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
        final GroupingEffectiveStatementImpl other = (GroupingEffectiveStatementImpl) obj;
        if (qname == null) {
            if (other.qname != null) {
                return false;
            }
        } else if (!qname.equals(other.qname)) {
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
        StringBuilder sb = new StringBuilder(GroupingEffectiveStatementImpl.class.getSimpleName());
        sb.append("[");
        sb.append("qname=").append(qname);
        sb.append("]");
        return sb.toString();
    }
}