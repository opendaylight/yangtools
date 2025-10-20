#
# Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html
#

import pathlib


def contains_path_hidden_dir_or_file(path: pathlib.PosixPath) -> bool:
    """Check if any directory on the provided path is hidden.

    Args:
        path (pathlib.PosixPath): A PosixPath to be checked.

    Returns:
        bool: boolean value if the path contains hidden directory
    """
    return any(part for part in path.parts if part.startswith("."))


def get_yang_files(root: str = ".") -> list[str]:
    """Lists all yang files found in root directory recursively.

    Args:
        root (str): Root directory containing yang files

    Returns:
        list[str]: List of all yang files full paths
    """
    root_path = pathlib.Path(root)
    dirs = list(
        [
            str(path.resolve())
            for path in root_path.rglob("*.yang")
            if path.is_file() and not contains_path_hidden_dir_or_file(path)
        ]
    )

    return dirs


def validator_path_option(paths: set[str]) -> str:
    """Returns --path argument with all provided yang files used for yang-model-validator tool.

    Args:
        yang_paths (set[str]): Set of all yang files which should be included in path arg

    Returns:
        str: Path arugment containing all provided yang files
    """
    yang_path_option = "--path " + " ".join(paths)

    return yang_path_option
