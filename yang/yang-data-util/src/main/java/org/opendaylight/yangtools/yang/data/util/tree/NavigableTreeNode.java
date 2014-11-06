package org.opendaylight.yangtools.yang.data.util.tree;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

interface NavigableTreeNode<S extends NavigableTreeNode<S>> extends Identifiable<PathArgument> {

    @Override
    PathArgument getIdentifier();

    @Nullable S getChild(PathArgument childArg);

}
