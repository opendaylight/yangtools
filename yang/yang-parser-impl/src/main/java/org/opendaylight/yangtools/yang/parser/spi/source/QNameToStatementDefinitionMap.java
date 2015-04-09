package org.opendaylight.yangtools.yang.parser.spi.source;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class QNameToStatementDefinitionMap implements QNameToStatementDefinition{

    private Map<QName, StatementDefinition> qNameToStmtDefMap = new HashMap<>();

    public void put(QName qName, StatementDefinition stDef ) {
        qNameToStmtDefMap.put(qName, stDef);
    }

    @Nullable @Override
    public StatementDefinition get(@Nonnull QName identifier) {
        return qNameToStmtDefMap.get(identifier);
    }
}
