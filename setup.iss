; -----------------------------------------------------------------------------
; Inno Setup 脚本 —— 生成 Vortex 安装包
; -----------------------------------------------------------------------------
#define AppName "Vortex"
#define AppVersion "1.0.0"
#define AppExeName "Vortex.exe"

[Setup]
AppName={#AppName}
AppVersion={#AppVersion}
AppPublisher=MineDiamond
DefaultDirName={autopf}\{#AppName}
DefaultGroupName={#AppName}
OutputDir=Output
OutputBaseFilename={#AppName}-Setup
Compression=lzma
SolidCompression=yes
WizardStyle=modern
; 让安装器在 Win11 上有圆角
SetupIconFile=src\main\resources\images\app_icon.ico

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
; Name: "chinesesimplified"; MessagesFile: "compiler:Languages\ChineseSimplified.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "startup"; Description: "开机时启动 {#AppName}"; GroupDescription: "其他任务:"; Flags: unchecked

[Files]
; 把 jpackage 生成的 app-image 整个复制到安装目录
Source: "build\app\{#AppName}\*"; DestDir: "{app}"; Flags: recursesubdirs createallsubdirs ignoreversion

[Icons]
Name: "{group}\{#AppName}"; Filename: "{app}\{#AppExeName}"
Name: "{autodesktop}\{#AppName}"; Filename: "{app}\{#AppExeName}"; Tasks: desktopicon

[Registry]
; 勾选“startup”任务，则写入注册表实现自启
Root: HKCU; Subkey: "Software\Microsoft\Windows\CurrentVersion\Run"; \
  ValueType: string; ValueName: "{#AppName}"; \
  ValueData: """{app}\{#AppExeName}"""; Tasks: startup

[Run]
Filename: "{app}\{#AppExeName}"; Description: "{cm:LaunchProgram,{#AppName}}"; \
  Flags: nowait postinstall skipifsilent
