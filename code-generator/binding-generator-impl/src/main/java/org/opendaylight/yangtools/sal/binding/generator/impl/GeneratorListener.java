/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import java.util.Map;

import org.opendaylight.yangtools.yang.binding.BindingCodec;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;

public interface GeneratorListener {
    void onClassProcessed(Class<?> cl);

    void onCodecCreated(Class<?> codec);
    void onValueCodecCreated(Class<?> valueClass, Class<?> valueCodec);
    void onCaseCodecCreated(Class<?> choiceClass, Class<? extends BindingCodec<Map<QName, Object>, Object>> choiceCodec);
    void onDataContainerCodecCreated(Class<?> dataClass, Class<? extends BindingCodec<?, ?>> dataCodec);

    void onChoiceCodecCreated(Class<?> choiceClass,
                              Class<? extends BindingCodec<Map<QName, Object>, Object>> choiceCodec, ChoiceNode schema);
}
