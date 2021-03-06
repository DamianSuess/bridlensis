Function arguments(a, b)
    b = "there"
    c = "y'all"
    global.R0 = a + b + c
    DetailPrint(global.R0)
FunctionEnd

b = "world"
R0 = "."
arguments("hello", b)
DetailPrint(b)
DetailPrint(R0)

Function return_foo()
    Return "foo"
    DetailPrint "Never gets here"
FunctionEnd

return_foo()

R0 = return_foo()
DetailPrint (R0)

;DetailPrint (return_foo())

Function join(a, b)
    Return a + b
FunctionEnd

; FunctionFile
File("autoexec.bat")
File("autoexec.bat", "/oname=autoexec.tmp")
File("*.*", "/r /nonfatal", instdir + "\doc")
File("*.html", "", instdir + "\doc")

; FunctionRename
Rename("autoexec.bak", "autoexec.bat")
FileRename("autoexec.bak", "autoexec.bat", "/REBOOTOK")

; RMDir
RMDir(pluginsdir)
RMDir($%TEMP% + "\BridleNSIS", "/r /REBOOTOK")

; FunctionReserveFile
ReserveFile("autoexec.bat")
ReserveFile("time.dll", "/plugin")

; DeleteRegKey
root_key = "HKLM"
DeleteRegKey(root_key, "Software\BridleNSIS\temp")
DeleteRegKey(root_key, "Software\BridleNSIS", "/ifempty")

; GetFullPathName
r0 = GetFullPathName("\Program Files")
r0 = GetFullPathName(instdir, "/SHORT")

; WordFind(S)
r1 = WordFind("C:\io.sys C:\Program Files C:\WINDOWS", "-02", " C:\")
r2 = WordFindS("[C:\io.sys];[C:\logo.sys];[C:\WINDOWS]", "+2", "[C:\", "];")
r3 = WordFind("[1.AAB];[2.BAA];[3.BBB];", "+1", "[", "];", "AA")

; WordReplace(S)
r1 = WordReplace("C:\io.sys C:\logo.sys C:\WINDOWS", "SYS", "bmp", "+2")
WordReplaceS("C:\io.sys C:\logo.sys C:\WINDOWS", "SYS", "", "+")

; WordInsert(S)
r1 = WordInsert("C:\io.sys C:\WINDOWS", " ", "C:\logo.sys", "-2")
WordInsertS("C:\io.sys", " ", "C:\WINDOWS", "+2")

; ConfigRead(S)
r0 = ConfigRead("C:\AUTOEXEC.BAT", "SET winbootdir=")
ConfigReadS("C:\apache\conf\httpd.conf", "Timeout ")

; ConfigWrite(S)
r0 = ConfigWrite( "C:\AUTOEXEC.BAT", "SET winbootdir=", "D:\WINDOWS")
ConfigWriteS("C:\apache\conf\httpd.conf", "Timeout ", "30")

Function un.onInit
    ; Pass-through
FunctionEnd

; StrCmp
StrCmp(r0, "a string", "same")
StrCmp(r0, "a string", "same", "different")

; IntCmp
IntCmp(r0, 5, "is5", "lessthan5")
IntCmp(r0, 5, "is5", "lessthan5", "morethan5")

;AdHoc function call
AdHoc(1, 2)
Function AdHoc(a, b)
    Return 3
FunctionEnd
b = AdHoc(1, 2)
