package org.opendaylight.yangtools.yang.model.api.meta;

import javax.annotation.Nullable;

public interface IdentifierNamespace<K,V> {

    @Nullable V get(K identifier);

}
