# script to add custom firewall range for Azure Data Bricks

$resourceGroupName= "rg-we-analytics-dev"
$serverName="SqlSrv-Myapp".ToLower()

$serverFirewallRule = New-AzSqlServerFirewallRule -ResourceGroupName $resourceGroupName `
   -ServerName $serverName `
   -FirewallRuleName "ADB2IPs" -StartIpAddress "xxx.xxx.xxx.xxx" -EndIpAddress "xxx.xxx.xxx.xxx"
$serverFirewallRule


$serverFirewallRule = New-AzSqlServerFirewallRule -ResourceGroupName $resourceGroupName `
   -ServerName $serverName `
   -FirewallRuleName "ADB3IPs" -StartIpAddress "xxx.xxx.xxx.xxx" -EndIpAddress "xxx.xxx.xxx.xxx"
$serverFirewallRule