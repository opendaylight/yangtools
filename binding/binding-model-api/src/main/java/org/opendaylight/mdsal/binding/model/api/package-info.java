/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
/**
 * Model of Java classes generated from YANG. Some information on naming:
 *
 * <ul>
 * <li>
 * {@link Type} refers to a Java class, interface or primitive type. This is something that is used as the type
 * specifier of a variable declaration.
 * </li>
 * <li>
 * {@link GeneratedType} refers to a Java class or interface. It can either be a top-level or a nested class or
 * interface.
 * </li>
 * <li>
 * {@link GeneratedTransferObject} refers to {@link GeneratedType}, which is a concrete class. These are generated to
 * encapsulate the YANG type hierarchy as expressed by 'typedef' and 'type' statement use. If it has a superclass, it is
 * also referred to as an {@code Extended Type}.
 * </li>
 * </ul>
 */
package org.opendaylight.mdsal.binding.model.api;