package org.opendaylight.yangtools.yang.parser.spi;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;

public interface GroupingNamespace extends StatementNamespace.TreeScoped<QName, GroupingStatement,EffectiveStatement<QName,GroupingStatement>> {

}
