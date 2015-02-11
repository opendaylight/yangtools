package org.opendaylight.yangtools.yang.parser.spi;

import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementNamespace;

public interface SchemaNodeIdentifierNamespace extends StatementNamespace.TreeBased<SchemaNodeIdentifier, DeclaredStatement<?>,EffectiveStatement<?,DeclaredStatement<?>>> {

}
