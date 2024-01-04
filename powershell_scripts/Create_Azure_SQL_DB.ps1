# Environment Variables
$location = [Environment]::GetEnvironmentVariable("demo_location", "User")
$tenant = [Environment]::GetEnvironmentVariable("demo_tenant", "User")
$subscriptionId = [Environment]::GetEnvironmentVariable("demo_subscriptionId", "User")
$resourceGroupName = [Environment]::GetEnvironmentVariable("demo_resourceGroupName", "User")
$adminLogin = [Environment]::GetEnvironmentVariable("demo_username", "User")
$password = [Environment]::GetEnvironmentVariable("demo_password", "User")

# Other Variables
$serverName="SqlSrv-Myapp".ToLower()
$databaseName = "Adventureworks"

# Connect to Azure Account
Connect-AzAccount -Tenant $tenant -Subscription $subscriptionId

#1.Create Sql Server with a system wide unique server name
Write-host "Creating primary logical server..."
$server = Get-AzSqlServer -ResourceGroupName $resourceGroupName -ServerName $serverName -ErrorAction SilentlyContinue
if (-Not $server)
{
   $server = New-AzSqlServer -ResourceGroupName $resourceGroupName `
      -ServerName $serverName `
      -Location $location `
      -SqlAdministratorCredentials $(New-Object -TypeName System.Management.Automation.PSCredential `
      -ArgumentList $adminLogin, $(ConvertTo-SecureString -String $password -AsPlainText -Force))
   $server
}

#2.Find your client  ip range. Last part of IP range is modified to be 0 till 255
$publicip = (Invoke-WebRequest -uri "http://ifconfig.me/ip").Content
$startIp = $publicip.Split('.')
$endIp = $publicip.Split('.')
$startIp[-1] = "0"
$endIp[-1] = "255"

$startIp = $startIp -Join "."
$endIp = $endIp -Join "."

#3.Create a server firewall rule that allows access from the specified IP range
Write-host "Configuring firewall for primary logical server..."
$serverFirewallRule = Get-AzSqlServerFirewallRule -ResourceGroupName $resourceGroupName -ServerName $serverName -FirewallRuleName "AllowedIPs" -ErrorAction SilentlyContinue
if (-Not $serverFirewallRule)
{
   $serverFirewallRule = New-AzSqlServerFirewallRule -ResourceGroupName $resourceGroupName `
      -ServerName $serverName `
      -FirewallRuleName "AllowedIPs" -StartIpAddress $startIp -EndIpAddress $endIp
   $serverFirewallRule
}

#4.Create General Purpose Gen4 sample database AdventureWorksLT with 1 vCore
Write-host "Creating a gen5 2 vCore database..."
$database = Get-AzSqlDatabase  -ResourceGroupName $resourceGroupName -ServerName $serverName -DatabaseName $databaseName -ErrorAction SilentlyContinue
if (-Not $database)
{
   $database = New-AzSqlDatabase  -ResourceGroupName $resourceGroupName `
      -ServerName $serverName `
      -DatabaseName $databaseName `
      -Edition GeneralPurpose `
      -VCore 2 `
      -ComputeGeneration Gen5 `
      -MinimumCapacity 2 `
      -SampleName "AdventureWorksLT"
   $database
}
#5.Display sql server fully qualified domain name
$ServerInstance = Get-AzSqlServer -ResourceGroupName $resourceGroupName | Where-Object {$_.ServerName -eq $serverName}
Write-Host $ServerInstance.FullyQualifiedDomainName

#6. Query information_schema.table to list out tables available from Adventureworks
Invoke-Sqlcmd -Query "select table_schema,count(1) Objects_count From information_schema.tables group by table_schema" `
-ServerInstance $ServerInstance.FullyQualifiedDomainName -Username $adminLogin -Password $password `
-Database $databaseName | Format-Table;
