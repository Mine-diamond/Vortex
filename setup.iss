[Setup]
; --- 应用基本信息 ---
AppName=Vortex
AppVersion=1.0
AppPublisher=Mineyyming Tech
DefaultDirName={autopf}\Vortex
DefaultGroupName=Vortex
AllowNoIcons=yes

; --- 输出设置 ---
; 输出目录为 "Output"，输出文件名为 "Vortex-Setup.exe"
OutputDir=Output
OutputBaseFilename=Vortex-Setup
Compression=lzma2
SolidCompression=yes
WizardStyle=modern

; --- 权限和图标 ---
PrivilegesRequired=lowest ; 以普通用户权限安装，自启写入HKCU，不需要管理员
SetupIconFile=src\main\resources\images\app_icon.ico

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "chinesesimplified"; MessagesFile: "compiler:Languages\ChineseSimplified.isl"

[Tasks]
; --- 安装时选项 ---
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: checked
Name: "startup"; Description: "开机时启动 Vortex"; GroupDescription: "其他任务:"; Flags: unchecked

[Files]
; --- 关键：打包 jpackage 生成的应用镜像 ---
; Source 路径必须和 GitHub Action 中 jpackage 的 --dest 路径一致
Source: "build\app\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
; --- 创建快捷方式 ---
Name: "{group}\Vortex"; Filename: "{app}\Vortex.exe"
Name: "{group}\{cm:UninstallProgram,Vortex}"; Filename: "{uninstallexe}"
Name: "{autodesktop}\Vortex"; Filename: "{app}\Vortex.exe"; Tasks: desktopicon

[Registry]
; --- 关键：实现开机自启 ---
; 如果用户勾选了 "startup" 任务，则写入注册表
Root: HKCU; Subkey: "Software\Microsoft\Windows\CurrentVersion\Run"; ValueType: string; ValueName: "{#AppName}"; ValueData: """{app}\Vortex.exe"""; Tasks: startup

[Run]
; --- 安装完成后运行 ---
Filename: "{app}\Vortex.exe"; Description: "{cm:LaunchProgram,Vortex}"; Flags: nowait postinstall skipifsilent
