#!/bin/sh -e 

# (C)2015 Brocade Communications Systems, Inc and others
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html

TARGET_DIR=`pwd`
TARGET_DIR="$TARGET_DIR/$1"

echo "Replacing generated files with human coded where autoconversion is required in $TARGET_DIR" >&2

( 
    cd accelerate/data
    for file in `ls` ; do
        if [ -f "$TARGET_DIR/$file" ] ; then
            echo Replacing $file >&2
            cp $file "$TARGET_DIR/$file"
        fi
    done
)
