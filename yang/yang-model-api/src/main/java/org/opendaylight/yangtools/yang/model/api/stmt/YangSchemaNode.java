package org.opendaylight.yangtools.yang.model.api.stmt;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.Statement;

public interface YangSchemaNode<T extends YangSchemaNode<T>> extends Statement<QName,T> {

}
