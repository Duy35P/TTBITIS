[System.Reflection.Assembly]::LoadWithPartialName('Microsoft.SqlServer.SMO') | Out-Null
$srv = New-Object Microsoft.SqlServer.Management.Smo.Server("localhost")
$srv.ConnectionContext.LoginSecure = $false
$srv.ConnectionContext.Login = "sa"
$srv.ConnectionContext.Password = "duy123"
$db = $srv.Databases["luckydraw"]

$scripter = New-Object Microsoft.SqlServer.Management.Smo.Scripter($srv)
$scripter.Options.ScriptSchema = $true
$scripter.Options.ScriptData = $false
$scripter.Options.ScriptDrops = $false
$scripter.Options.WithDependencies = $false
$scripter.Options.Indexes = $true
$scripter.Options.Triggers = $true
$scripter.Options.IncludeHeaders = $true
$scripter.Options.EnforceScriptingOptions = $true

$objects = New-Object System.Collections.Generic.List[Microsoft.SqlServer.Management.Smo.SqlSmoObject]

foreach ($tb in $db.Tables) { if (!$tb.IsSystemObject) { $objects.Add($tb) } }
foreach ($vw in $db.Views) { if (!$vw.IsSystemObject) { $objects.Add($vw) } }
foreach ($sp in $db.StoredProcedures) { if (!$sp.IsSystemObject) { $objects.Add($sp) } }

$outFile = "D:\webquaymayrui\databaseSQL\luckydraw_schema_2026.sql"
if (Test-Path $outFile) { Remove-Item $outFile }

foreach ($obj in $objects) {
    $scripts = $scripter.Script($obj)
    foreach ($s in $scripts) {
        Add-Content $outFile "$s
GO
" -Encoding UTF8
    }
}
