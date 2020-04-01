<#
Purpose: Purpose of this file is to drop provisioned azure resources for this project.

Description:
This script was referred from below link. Included if condition, to avoid errors in case of rerun.
Added a small script to include client ip range into Azure SQL Server firewall
https://docs.microsoft.com/en-us/azure/sql-database/scripts/sql-database-create-and-configure-database-powershell

Version Number     Date            ModifiedBy                  Description
--------------------------------------------------------------------------------------
v1.0               28-03-2020      Vijaybabu Nakkonda          Initial Version

Exeuction Method: Execute the whole file from powershell

#>

$resourceGroupName = "rg-xxxxxxxxxxxx"
$serverName="SqlSrv-Myapp".ToLower()
$storage_account_name = "sadeltalake"
$key_vault_name = "kv-xxxxxxxx"
$Service_Principle_DisplayName = "sp_deltalake"

Remove-AzSqlServer -ResourceGroupName $resourceGroupName -ServerName $serverName
Remove-AzStorageAccount -Name $storage_account_name -ResourceGroupName $resourceGroupName
Remove-AzADServicePrincipal -DisplayName $Service_Principle_DisplayName
Remove-AzKeyVault -VaultName $key_vault_name