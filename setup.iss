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
MinVersion=10.0
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
  ValueData: """{app}\{#AppExeName}"" --autostart"; Tasks: startup

[Run]
Filename: "{app}\{#AppExeName}"; Description: "{cm:LaunchProgram,{#AppName}}"; \
  Flags: nowait postinstall skipifsilent

[Code]
var
  DataPage: TInputOptionWizardPage;
  RemoveDataCheckBox: TNewCheckBox;

procedure InitializeUninstall();
begin
  DataPage := CreateInputOptionPage(wpUninstallProgress,
    '卸载选项', '请选择卸载模式',
    '您可以选择是否要彻底删除应用程序及其所有数据。',
    True, False);

  RemoveDataCheckBox := TNewCheckBox.Create(DataPage);
  RemoveDataCheckBox.Parent := DataPage.Surface;
  // 修改提示文字，更准确地描述行为
  RemoveDataCheckBox.Caption := '是，删除所有数据和设置（将彻底移除整个安装文件夹）。';
  RemoveDataCheckBox.Checked := False;
end;

function ShouldRemoveData(): Boolean;
begin
  Result := RemoveDataCheckBox.Checked;
end;

[UninstallDelete]
// Type: 指定删除类型
// Name: 指定要删除的路径
// Check: 关联我们的判断函数

// 核心指令：删除整个应用程序文件夹
// Type: filesandordirs 表示删除目标文件夹以及里面的所有文件和子文件夹
// Name: {app} 就是我们想要删除的安装目录本身
Type: filesandordirs; Name: "{app}"; Check: ShouldRemoveData
