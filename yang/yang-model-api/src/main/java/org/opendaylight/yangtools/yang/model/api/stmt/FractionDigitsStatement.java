package org.opendaylight.yangtools.yang.model.api.stmt;

import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;


public interface FractionDigitsStatement extends DeclaredStatement<String> {

    String getValue();
}

