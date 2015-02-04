/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.leafrefcontext;

import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public interface LeafRefContext {

    public boolean hasLeafRefContext();

    public boolean hasReferencedByChild();

    public boolean hasReferencingChild();

    public boolean isReferencedBy();

    public void setReferencedBy(boolean isReferencedBy);

    public boolean isReferencing();

    public void setReferencing(boolean isReferencing);

    public void addReferencingChild(LeafRefContext child, QName childQName);

    public LeafRefContext getReferencingChildByName(QName name);

    public Map<QName, LeafRefContext> getReferencingChilds();

    public void addReferencedByChild(LeafRefContext child, QName childQName);

    public LeafRefContext getReferencedByChildByName(QName name);

    public Map<QName, LeafRefContext> getReferencedByChilds();

    public SchemaPath getCurrentNodePath();

    public void setCurrentNodePath(SchemaPath currentNodePath);

    public LeafRefPath getLeafRefTargetPath();

    public void setLeafRefTargetPath(LeafRefPath leafRefPath);

    public String getLeafRefTargetPathString();

    public void setLeafRefTargetPathString(String leafRefPathString);

    public QName getCurrentNodeQName();

    public void setCurrentNodeQName(QName currentNodeQName);

    public SchemaContext getSchemaContext();

    public void setSchemaContext(SchemaContext schemaContext);

    public LeafRefContext getParent();

    public void setParent(LeafRefContext leafRefContext);
}