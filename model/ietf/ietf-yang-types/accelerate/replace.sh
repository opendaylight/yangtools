#!/bin/sh -e 

# (C)2015 Brocade Communications Systems, Inc.
# 130 Holger Way, San Jose, CA 95134.
# All rights reserved.
#
# Brocade, the B-wing symbol, Brocade Assurance, ADX, AnyIO, DCX, Fabric OS,
# FastIron, HyperEdge, ICX, MLX, MyBrocade, NetIron, OpenScript, VCS, VDX, and
# Vyatta are registered trademarks, and The Effortless Network and the On-Demand
# Data Center are trademarks of Brocade Communications Systems, Inc., in the
# United States and in other countries. Other brands and product names mentioned
# may be trademarks of others.
#
# Use of the software files  and documentation is subject to license terms as in
# LICENSE in the top level project directory 


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
