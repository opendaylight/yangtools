/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext.api;

import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public interface LeafRefContext {

    public boolean hasLeafRefContextChild();

    public boolean hasReferencedByChild();

    public boolean hasReferencingChild();

    public boolean isReferencedBy();

    public boolean isReferencing();

    public LeafRefContext getReferencingChildByName(QName name);

    public Map<QName, LeafRefContext> getReferencingChilds();

    public LeafRefContext getReferencedByChildByName(QName name);

    public Map<QName, LeafRefContext> getReferencedByChilds();

    public SchemaPath getCurrentNodePath();

    public LeafRefPath getLeafRefTargetPath();

    public String getLeafRefTargetPathString();

    public QName getCurrentNodeQName();

    public SchemaContext getSchemaContext();

    public LeafRefPath getAbsoluteLeafRefTargetPath();

    public Module getLeafRefContextModule();

    public LeafRefContext getReferencedByLeafRefCtxByName(QName qname);

    public Map<QName, LeafRefContext> getAllReferencedByLeafRefCtxs();
}