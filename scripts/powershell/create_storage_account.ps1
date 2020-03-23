##https://docs.microsoft.com/en-us/azure/storage/common/storage-account-create?tabs=azure-powershell

$resourceGroup = "rg-we-analytics-dev"
$location = "westeurope"
$account_name = "sadeltalake"

#New-AzResourceGroup -Name $resourceGroup -Location $location

#Get-AzLocation | Select-Object Location

New-AzStorageAccount -ResourceGroupName $resourceGroup `
  -Name $account_name `
  -Location $location `
  -SkuName Standard_RAGRS `
  -Kind StorageV2 `
  -EnableHierarchicalNamespace $True

#Remove-AzStorageAccount -Name $account_name -ResourceGroupName $resourceGroup