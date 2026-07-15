[System.Reflection.Assembly]::LoadWithPartialName('Microsoft.SqlServer.SMO') | Out-Null
$server = New-Object ('Microsoft.SqlServer.Management.Smo.Server') 'localhost'
$server.ConnectionContext.LoginSecure = $false
$server.ConnectionContext.Login = 'sa'
$server.ConnectionContext.Password = 'duy123'
$db = $server.Databases['luckydraw']
$scripter = New-Object ('Microsoft.SqlServer.Management.Smo.Scripter') $server
$scripter.Options.ScriptSchema = $true
$scripter.Options.ScriptData = $false
$scripter.Options.Indexes = $true
$objects = @()
foreach ($t in $db.Tables) { if (!$t.IsSystemObject) { $objects += $t } }
foreach ($v in $db.Views) { if (!$v.IsSystemObject) { $objects += $v } }
foreach ($p in $db.StoredProcedures) { if (!$p.IsSystemObject) { $objects += $p } }
$scripter.EnumScript($objects) | Out-File 'D:\webquaymayrui\databaseSQL\07_FULL_SCHEMA_DUMP.sql' -Encoding UTF8
