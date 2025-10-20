#
# Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html
#

import subprocess
import logging

log = logging.getLogger(__name__)


def shell(command: str | list | tuple, joiner="; ", cwd: str | None = None):
    exec_command = command
    if isinstance(command, (list, tuple)):
        exec_command = joiner.join(command)

    try:
        log.info(exec_command)
        result = subprocess.run(
            exec_command,
            shell=True,
            check=True,
            capture_output=True,
            text=True,
            cwd=cwd,
        )
        log.info(f"{result.returncode:3d} |--| {result.stdout}")
        return result.returncode, result.stdout
    except subprocess.CalledProcessError as e:
        log.error(
            f"ERROR while command execution '{exec_command}':\n{e.stderr.strip()}"
        )
        return e.returncode, e.stdout
    except FileNotFoundError:
        log.error(f"ERROR command not found: {exec_command}")
        return None
