# script to add custom firewall range for Azure Data Bricks

$resourceGroupName= "rg-we-analytics-dev"
$serverName="SqlSrv-Myapp".ToLower()

$serverFirewallRule = New-AzSqlServerFirewallRule -ResourceGroupName $resourceGroupName `
   -ServerName $serverName `
   -FirewallRuleName "ADB2IPs" -StartIpAddress "23.97.136.239" -EndIpAddress "23.97.136.239"
$serverFirewallRule