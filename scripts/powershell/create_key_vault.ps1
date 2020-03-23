#https://docs.microsoft.com/en-us/powershell/module/az.keyvault/new-azkeyvault?view=azps-3.6.1

#https://docs.microsoft.com/en-us/azure/cloud-adoption-framework/ready/azure-best-practices/naming-and-tagging



$resourceGroupName="rg-we-analytics-dev"
$location = "West Europe"


New-AzKeyVault -VaultName 'kv-adb-test' -ResourceGroupName $resourceGroupName -Location $location