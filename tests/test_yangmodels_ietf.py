#
# Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html
#

from pathlib import Path
import shutil
import glob


from utils.shell import shell
from utils.validator import get_yang_files, validator_path_option

# YANG_MODEL_PATHS is needed to explicitly tell which paths to find yang files to build dependencies from to validate
# the yang file being validated. There is an option (-r) to recursively parse so that you don't have to pass all of these
# paths with the -p argument, but the recursive option makes the tool so slow that it would not work when testing so
# many files
YANG_MODEL_PATHS = {
    "src/main/yang/standard/iana",
    "src/main/yang/standard/ieee/published/802",
    "src/main/yang/standard/ieee/published/802.1",
    "src/main/yang/standard/ieee/draft/802.1",
}


def resolve_and_replace_symlinks(target_directory: str | Path):
    """
    Replaces symlinks in the specified directory with the original files
    they point to, and deletes associated files ending with '@...'.

    Args:
        target_directory (str | Path): Path to the directory to process.
        dry_run (bool): If True, only simulate actions without modifying files.
    """
    dir_path = Path(target_directory).resolve()

    print(f"\nResolving symlinks for dir: {dir_path}")

    if not dir_path.is_dir():
        print("Error: Directory not found")
        return

    try:
        items = list(dir_path.iterdir())
    except OSError as e:
        print(f"Error reading directory: {e}")
        return

    for item in items:
        if not item.is_symlink():
            continue

        link_path = item
        link_name = link_path.name
        link_stem = link_path.stem

        try:
            real_target_file = link_path.resolve(strict=True)
            print(f"Processing symlink: {link_name} -> {real_target_file}")

        except FileNotFoundError:
            print(f"   WARNING: Symlink {link_name} is broken (points to missing file). Skipping.")
            continue
        except RuntimeError as e:
            print(f"   Error resolving symlink {link_name}: {e}. Skipping.")
            continue

        else:
            try:
                link_path.unlink()
            except OSError as e:
                print(f"   Error deleting symlink: {e}")
                continue

            try:
                shutil.move(src=real_target_file, dst=link_path)
            except OSError as e:
                print(f"   CRITICAL ERROR moving file! The original file might remain in the old location. Error: {e}")

        pattern = f"{glob.escape(link_stem)}@*"

        for assoc_file in list(dir_path.glob(pattern)):
            if assoc_file.is_file() and assoc_file != link_path:
                try:
                    assoc_file.unlink()
                    print(f"   Deleted associated file: {assoc_file.name}")
                except OSError as e:
                    print(f"   Failed to delete {assoc_file.name}: {e}")
    print("-" * 50)


def delete_files_with_prefix(directory: str | Path, prefix: str):
    """
    Deletes all files in the specified directory that start with the given prefix.
    WARNING: This action is immediate and irreversible.
    """

    if not prefix:
        print("Error: Prefix cannot be empty. Operation aborted.")
        return

    dir_path = Path(directory).resolve()

    if not dir_path.is_dir():
        print(f"Error: Directory not found: {dir_path}")
        return

    escaped_prefix = glob.escape(prefix)
    pattern = f"{escaped_prefix}*"
    found_items = list(dir_path.glob(pattern))

    if not found_items:
        print("No files found matching prefix.")
        return

    for item in found_items:
        if item.is_file():
            try:
                item.unlink()
            except OSError as e:
                print(f"Error deleting {item.name}: {e}")
        elif item.is_dir():
            print(f"[Skipping] {item.name} (it is a directory)")


def test_validate_yangmodels_ietf_yangs(fixture_models_repos):  # noqa: F811
    resolve_and_replace_symlinks("./src/main/yang/standard/ietf/RFC")
    delete_files_with_prefix("./src/main/yang/standard/ietf/RFC", 'iana')
    yang_files = get_yang_files("./src/main/yang/standard/ietf/RFC")
    yang_files_str = " ".join([f"'{f}'" for f in yang_files])

    rc, test_tool_output = shell(
        f"java -jar yang-model-validator.jar {validator_path_option(YANG_MODEL_PATHS)} -- {yang_files_str}"
    )
    assert (
        rc == 0
    ), f"{test_tool_output}"
