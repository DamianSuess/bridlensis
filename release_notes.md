﻿# BridleNSIS Release Notes

## Version 0.2.0

Release date: TBD

### What's New

*   New convenience functions File, ReserveFile, FileCopy, FileRename, RMDir, DeleteRegKey, and GetFullPathName
*   Function aliases Delete, Copy, Rename, and RMDir
*   Function FileDelete Optional argument rebootok must be passed as defined in NSIS, e.g. `Delete("file.dat", "/REBOOTOK")`
*   Showing off more BridleNSIS features in the example script

### Fixes

*   Fix `Not` not being a reserved word (may have caused ambiguous syntax and parser errors)
*   Fix `r2` ($R2) not being recognized as a built-in variable
*   Fix uninstaller leaving empty paths to registry

## Version 0.1.0

Release date: 2014-04-05

First public release