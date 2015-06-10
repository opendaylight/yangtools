package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;

import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;
import com.google.common.base.Optional;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;

abstract class IntegerEffectiveImplBase extends
        EffectiveStatementBase<String,TypeStatement> implements IntegerTypeDefinition {

    private static final String REFERENCE_INT = "https://tools.ietf.org/html/rfc6020#section-9.2";

    protected QName qName;
    private SchemaPath path;
    private String units = "";
    private final String description;
    private List<RangeConstraint> rangeStatements;

    protected IntegerEffectiveImplBase(final StmtContext<String, TypeStatement, EffectiveStatement<String, TypeStatement>> ctx,
            final String localName, final Number minRange, final Number maxRange, final String description) {

        super(ctx);

        this.qName = QName.create(YangConstants.RFC6020_YANG_MODULE, localName);
        path = Utils.getSchemaPath(ctx);

        final String rangeDescription = "Integer values between " + minRange + " and " + maxRange + ", inclusively.";
        final RangeConstraint defaultRange = new RangeConstraintEffectiveImpl(minRange, maxRange,
                Optional.of(rangeDescription), Optional.of(RangeConstraintEffectiveImpl.DEFAULT_REFERENCE));
        rangeStatements = Collections.singletonList(defaultRange);

        this.description = description;
    }

    @Override
    public List<RangeConstraint> getRangeConstraints() {
        return rangeStatements;
    }

    @Override
    public IntegerTypeDefinition getBaseType() {
        return null;
    }

    @Override
    public String getUnits() {
        return units;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public QName getQName() {
        return qName;
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
        return description;
    }

    @Override
    public String getReference() {
        return REFERENCE_INT;
    }

    @Override
    public Status getStatus() {
        return Status.CURRENT;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + ((qName == null) ? 0 : qName.hashCode());
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((rangeStatements == null) ? 0 : rangeStatements.hashCode());
        result = prime * result + ((units == null) ? 0 : units.hashCode());
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
        IntegerEffectiveImplBase other = (IntegerEffectiveImplBase) obj;
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (qName == null) {
            if (other.qName != null) {
                return false;
            }
        } else if (!qName.equals(other.qName)) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (rangeStatements == null) {
            if (other.rangeStatements != null) {
                return false;
            }
        } else if (!rangeStatements.equals(other.rangeStatements)) {
            return false;
        }
        if (units == null) {
            if (other.units != null) {
                return false;
            }
        } else if (!units.equals(other.units)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append(" [name=");
        builder.append(qName);
        builder.append(", path=");
        builder.append(path);
        builder.append(", description=");
        builder.append(description);
        builder.append(", reference=");
        builder.append(REFERENCE_INT);
        builder.append(", units=");
        builder.append(units);
        builder.append(", rangeStatements=");
        builder.append(rangeStatements);
        builder.append("]");
        return builder.toString();
    }
}
