package org.opendaylight.yangtools.yang.data.api;

import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;

public interface AttributesContainer {

    
    Map<QName,String> getAttributes();
    
    Object getAttributeValue(QName value);
    
}
