/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import java.util.List;

import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;


public interface CodecRegistry {

    InstanceIdentifierCodec getInstanceIdentifierCodec();

    IdentityCodec<?> getIdentityCodec();

    <T extends DataContainer> DataContainerCodec<T> getCodecForDataObject(Class<T> object);

    <T extends Identifiable<?>> IdentifierCodec<?> getIdentifierCodecForIdentifiable(Class<T> object);

    <T extends Identifier<?>> IdentifierCodec<T> getCodecForIdentifier(Class<T> object);

    <T extends Augmentation<?>> AugmentationCodec<T> getCodecForAugmentation(Class<T> object);

    <T extends Augmentation<?>> AugmentationCodec<T> getCodecForAugmentation(Class<T> object, SchemaPath path);

    <T extends BaseIdentity> IdentityCodec<T> getCodecForIdentity(Class<T> codec);

    Class<?> getClassForPath(List<QName> names);

    IdentifierCodec<?> getKeyCodecForPath(List<QName> names);


    void bindingClassEncountered(Class<?> cls);

    void putPathToClass(List<QName> names, Class<?> cls);

    public abstract QName getQNameForAugmentation(Class<?> cls);
}
