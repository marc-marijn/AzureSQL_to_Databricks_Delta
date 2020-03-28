#Reference Link
#https://docs.microsoft.com/en-us/azure/storage/common/storage-account-create?tabs=azure-powershell

$resourceGroup = "rg-we-analytics-dev"
$location = "westeurope"
$account_name = "sadeltalake"
$subscriptionId = 'be95106b-e57f-4d40-bcdd-1944f07d035f'
$container = "deltalake"
$key_vault_name = "kv-nakkonda-test"

#If you need to create a new resource group
#New-AzResourceGroup -Name $resourceGroup -Location $location

#Get-AzLocation | Select-Object Location

$sa = New-AzStorageAccount -ResourceGroupName $resourceGroup `
  -Name $account_name `
  -Location $location `
  -SkuName Standard_RAGRS `
  -Kind StorageV2 `
  -EnableHierarchicalNamespace $True

#Create a new container within storage account
$con = New-AzStorageContainer -Name $container -Permission Container -Context $sa.Context

# Create Service Principle, assign password
$DisplayName = "sp_adb5"

$sp = Get-AzAdServicePrincipal -DisplayName $DisplayName
Write-host $Sp.ApplicationId
Write-host $sa.StorageAccountName
$sa = Get-AzStorageAccount -ResourceGroupName $resourceGroup -Name $account_name

$credProps = @{
    StartDate = Get-Date
    EndDate = (Get-Date -Year 2024)
    KeyId = (New-Guid).ToString()
    Value = 'MySuperAwesomePasswordIs3373'
 }
 $credentials = New-Object Microsoft.Azure.Graph.RBAC.Models.PasswordCredential -Property $credProps

Set-AzADServicePrincipal -ObjectId $sp.Id -PasswordCredential $credentials

 #Grant service principle to access Storage account (storage account level access)
New-AzRoleAssignment -ApplicationId $Sp.ApplicationId `
  -RoleDefinitionName "Storage Blob Data Contributor" `
  -Scope  "/subscriptions/$subscriptionId/resourceGroups/$resourceGroup/providers/Microsoft.Storage/storageAccounts/$account_name"

#Store the service principle application id and password in Azure Key Vault
$adbspkey = ConvertTo-SecureString $Sp.ApplicationId -AsPlainText -Force
Set-AzKeyVaultSecret -VaultName $key_vault_name -Name 'adbspappkey' -SecretValue $adbspkey

$adbspsecret = ConvertTo-SecureString $credProps.Value -AsPlainText -Force
Set-AzKeyVaultSecret -VaultName $key_vault_name -Name 'adbspsecret' -SecretValue $adbspsecret


