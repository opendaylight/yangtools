/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

public final class BuiltinEffectiveStatements {
    private BuiltinEffectiveStatements() {
        throw new UnsupportedOperationException();
    }

    public static final TypeEffectiveStatement<TypeStatement> BINARY = new AbstractBuiltinEffectiveStatement() {
        @Nonnull
        @Override
        public BinaryTypeDefinition getTypeDefinition() {
            return BaseTypes.binaryType();
        }
    };
    public static final TypeEffectiveStatement<TypeStatement> BOOLEAN = new AbstractBuiltinEffectiveStatement() {
        @Nonnull
        @Override
        public BooleanTypeDefinition getTypeDefinition() {
            return BaseTypes.booleanType();
        }
    };
    public static final TypeEffectiveStatement<TypeStatement> EMPTY = new AbstractBuiltinEffectiveStatement() {
        @Nonnull
        @Override
        public EmptyTypeDefinition getTypeDefinition() {
            return BaseTypes.emptyType();
        }
    };
    public static final TypeEffectiveStatement<TypeStatement> INSTANCE_IDENTIFIER = new AbstractBuiltinEffectiveStatement() {
        @Nonnull
        @Override
        public InstanceIdentifierTypeDefinition getTypeDefinition() {
            return BaseTypes.instanceIdentifierType();
        }
    };
    public static final TypeEffectiveStatement<TypeStatement> INT8 = new AbstractBuiltinEffectiveStatement() {
        @Nonnull
        @Override
        public IntegerTypeDefinition getTypeDefinition() {
            return BaseTypes.int8Type();
        }
    };
    public static final TypeEffectiveStatement<TypeStatement> INT16 = new AbstractBuiltinEffectiveStatement() {
        @Nonnull
        @Override
        public IntegerTypeDefinition getTypeDefinition() {
            return BaseTypes.int16Type();
        }
    };
    public static final TypeEffectiveStatement<TypeStatement> INT32 = new AbstractBuiltinEffectiveStatement() {
        @Nonnull
        @Override
        public IntegerTypeDefinition getTypeDefinition() {
            return BaseTypes.int32Type();
        }
    };
    public static final TypeEffectiveStatement<TypeStatement> INT64 = new AbstractBuiltinEffectiveStatement() {
        @Nonnull
        @Override
        public IntegerTypeDefinition getTypeDefinition() {
            return BaseTypes.int64Type();
        }
    };
    public static final TypeEffectiveStatement<TypeStatement> STRING = new AbstractBuiltinEffectiveStatement() {
        @Nonnull
        @Override
        public StringTypeDefinition getTypeDefinition() {
            return BaseTypes.stringType();
        }
    };
    public static final TypeEffectiveStatement<TypeStatement> UINT8 = new AbstractBuiltinEffectiveStatement() {
        @Nonnull
        @Override
        public UnsignedIntegerTypeDefinition getTypeDefinition() {
            return BaseTypes.uint8Type();
        }
    };
    public static final TypeEffectiveStatement<TypeStatement> UINT16 = new AbstractBuiltinEffectiveStatement() {
        @Nonnull
        @Override
        public UnsignedIntegerTypeDefinition getTypeDefinition() {
            return BaseTypes.uint16Type();
        }
    };
    public static final TypeEffectiveStatement<TypeStatement> UINT32 = new AbstractBuiltinEffectiveStatement() {
        @Nonnull
        @Override
        public UnsignedIntegerTypeDefinition getTypeDefinition() {
            return BaseTypes.uint32Type();
        }
    };
    public static final TypeEffectiveStatement<TypeStatement> UINT64 = new AbstractBuiltinEffectiveStatement() {
        @Nonnull
        @Override
        public UnsignedIntegerTypeDefinition getTypeDefinition() {
            return BaseTypes.uint64Type();
        }
    };
}
