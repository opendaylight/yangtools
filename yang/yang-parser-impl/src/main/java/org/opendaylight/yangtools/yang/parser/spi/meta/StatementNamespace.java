package org.opendaylight.yangtools.yang.parser.spi.meta;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

public interface StatementNamespace<K, D extends DeclaredStatement<?>,E extends EffectiveStatement<?,D>>  extends IdentifierNamespace<K, E>  {

    @Override
    @Nullable E get(K key);

    public interface TreeScoped<K, D extends DeclaredStatement<?>,E extends EffectiveStatement<?,D>> extends StatementNamespace<K,D,E> {

        TreeScoped<K,D,E> getParentContext();

    }

    public interface TreeBased<K,D extends DeclaredStatement<?>,E extends EffectiveStatement<?,D>> {

    }


}
