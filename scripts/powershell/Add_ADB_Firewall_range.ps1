# script to add custom firewall range for Azure Data Bricks

$serverFirewallRule = New-AzSqlServerFirewallRule -ResourceGroupName $resourceGroupName `
   -ServerName $serverName `
   -FirewallRuleName "ADB2IPs" -StartIpAddress "40.68.56.32" -EndIpAddress "40.68.56.32"
$serverFirewallRule