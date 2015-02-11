package org.opendaylight.yangtools.yang.parser.spi;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;

public interface FeatureNamespace extends StatementNamespace<QName, FeatureStatement, EffectiveStatement<QName,FeatureStatement>> {

}
