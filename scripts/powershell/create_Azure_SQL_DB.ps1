# Set variables for your server and database

$subscriptionId = 'be95106b-e57f-4d40-bcdd-1944f07d035f'
#$resourceGroupName = "myResourceGroup-$(Get-Random)"
$resourceGroupName="rg-we-analytics-dev"
$location = "West Europe"
$adminLogin = "sqladmin"
$password = "Tiger123"
#$serverName = "mysqlserver-$(Get-Random)"
$serverName="SqlSrv-Nklabs".ToLower()
$databaseName = "Adventureworks"

# The ip address range that you want to allow to access your server 
# (leaving at 0.0.0.0 will prevent outside-of-azure connections to your DB)


$publicip = (Invoke-WebRequest -uri "http://ifconfig.me/ip").Content
$startIp = $publicip.Split('.')
$endIp = $publicip.Split('.')
$startIp[-1] = "0"
$endIp[-1] = "255"

$startIp = $startIp -Join "."
$endIp = $endIp -Join "."


# Show randomized variables
Write-host "Resource group name is" $resourceGroupName 
Write-host "Password is" $password  
Write-host "Server name is" $serverName 

# Connect to Azure
#Connect-AzAccount

# Set subscription ID
Set-AzContext -SubscriptionId $subscriptionId 

# Create a resource group
Write-host "Creating resource group..."
#$resourceGroup = New-AzResourceGroup -Name $resourceGroupName -Location $location -Tag @{Owner="SQLDB-Samples"}
$resourceGroup

# Create a server with a system wide unique server name
Write-host "Creating primary logical server..."
$server = New-AzSqlServer -ResourceGroupName $resourceGroupName `
   -ServerName $serverName `
   -Location $location `
   -SqlAdministratorCredentials $(New-Object -TypeName System.Management.Automation.PSCredential `
   -ArgumentList $adminLogin, $(ConvertTo-SecureString -String $password -AsPlainText -Force))
$server

# Create a server firewall rule that allows access from the specified IP range
Write-host "Configuring firewall for primary logical server..."
$serverFirewallRule = New-AzSqlServerFirewallRule -ResourceGroupName $resourceGroupName `
   -ServerName $serverName `
   -FirewallRuleName "AllowedIPs" -StartIpAddress $startIp -EndIpAddress $endIp
$serverFirewallRule



$serverFirewallRule = New-AzSqlServerFirewallRule -ResourceGroupName $resourceGroupName `
   -ServerName $serverName `
   -FirewallRuleName "ADB4IPs" -StartIpAddress "13.80.10.178" -EndIpAddress "13.80.10.178"
$serverFirewallRule

# Create General Purpose Gen4 database with 1 vCore
Write-host "Creating a gen5 2 vCore database..."
$database = New-AzSqlDatabase  -ResourceGroupName $resourceGroupName `
   -ServerName $serverName `
   -DatabaseName $databaseName `
   -Edition GeneralPurpose `
   -VCore 2 `
   -ComputeGeneration Gen5 `
   -MinimumCapacity 2 `
   -SampleName "AdventureWorksLT"
$database



$ServerInstance = Get-AzSqlServer -ResourceGroupName $resourceGroupName | Where-Object {$_.ServerName -eq $serverName}
Write-Host $ServerInstance.FullyQualifiedDomainName

Invoke-Sqlcmd -Query "select table_schema,count(1) Objects_count From information_schema.tables group by table_schema" `
-ServerInstance $ServerInstance.FullyQualifiedDomainName -Username $adminLogin -Password $password `
-Database $databaseName | Format-Table;


#drop the database and server

#Remove-AzSqlServer -ResourceGroupName $resourceGroupName -ServerName $serverName