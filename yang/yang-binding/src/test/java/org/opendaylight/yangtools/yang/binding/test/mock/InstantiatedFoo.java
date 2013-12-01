package org.opendaylight.yangtools.yang.binding.test.mock;

import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.DataObject;

public interface InstantiatedFoo extends DataObject, GroupingFoo, ChildOf<Nodes> {

}
