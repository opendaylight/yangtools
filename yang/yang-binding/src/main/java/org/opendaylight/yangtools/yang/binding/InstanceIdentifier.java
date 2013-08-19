/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Uniquely identifies instance of data tree. 
 *
 *
 */
public class InstanceIdentifier <T extends DataObject> {

    
    private final List<PathArgument> path;
    private final Class<T> targetType;
    
    public InstanceIdentifier(Class<T> type) {
        path = Collections.emptyList();
        this.targetType = type;
    }
    
    
    public InstanceIdentifier(List<PathArgument> path,Class<T> type) {
        this.path = Collections.<PathArgument>unmodifiableList(new ArrayList<>(path));
        this.targetType = type;
    }
    

    /**
     * 
     * @return
     */
    public List<PathArgument> getPath() {
        return this.path;
    }
    
    public Class<T> getTargetType() {
        return this.targetType;
    }
    
    
    /**
     * Path argument of instance identifier.
     * 
     * Interface which implementations are used as path components
     * of the instance path.
     * 
     * @author ttkacik
     *
     */
    public static interface PathArgument {
        
    }
    
    public static class IdentifiableItem<I extends Identifiable<T>,T extends Identifier<I>>  implements PathArgument {
           
        private final T key;
        private final Class<? extends I> type;

        public IdentifiableItem(Class<I> type, T key) {
            this.type = type;
            this.key = key;
        }
        
        T getKey() {
            return this.key;
        }
        
        Class<? extends I> getType() {
            return this.type;
        }
    }
}
