package org.opendaylight.yangtools.yang.model.api.meta;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifiable;

public interface IdentifierNamespace<K,V extends Identifiable<K>> {

    @Nullable V get(K identifier);

}
