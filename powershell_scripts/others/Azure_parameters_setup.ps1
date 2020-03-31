
<#
Purpose: Purpose of this file is to setup Azure parameters and perform Azure login to execute other power shell files

Version Number     Date            ModifiedBy                  Description
--------------------------------------------------------------------------------------
v1.0               28-03-2020      Vijaybabu Nakkonda          Initial Version

Exeuction Method: Execute the whole file from powershell

#>

$location = "westeurope"
# To get list of available locations uncomment and execute below script
#Get-AzLocation | Select-Object Location

$tenant = "1c13b526-3056-482b-94ec-298476e01ea5"
$subscriptionId = 'be95106b-e57f-4d40-bcdd-1944f07d035f'
$resourceGroupName = "rg-we-analytics-dev"
#If you need to create a new resource group uncomment below powershell
#New-AzResourceGroup -Name $resourceGroup -Location $location

#sql database parameters
$adminLogin = "sqladmin"
$password = "Tiger123"
$serverName="SqlSrv-Myapp".ToLower()
$databaseName = "Adventureworks"

#Azure Data Lake Gen2 parameters
$storage_account_name = "sadeltalake"
$container = "deltalake"

#Azure Keyvault parameters
$key_vault_name = "kv-nakkonda-acc"

#Azure Active Directory parameters
$Service_Principle_DisplayName = "sp_deltalake"
$credProps = @{
    StartDate = Get-Date
    EndDate = (Get-Date -Year 2024)
    KeyId = (New-Guid).ToString()
    Value = 'SP_deltalakepassword!3373'
 }


#Do azure login before executing other powershell files
Connect-AzAccount -Tenant $tenant -Subscription $subscriptionId