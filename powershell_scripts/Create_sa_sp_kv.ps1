Install-Module -Name Az.KeyVault
Install-Module -Name Az.Storage
Install-Module -Name Az.Resources
Install-Module -Name Az.Accounts

Connect-AzAccount

<#
Purpose: Purpose of this powershell scripts is to
1. Create storage account and container
2. Create service principle and assign password for azure data bricks notebook execution
3. Grant "Storage Blob Data Contributor" access to service principle on storage account
4. Create Azure Keyvault, assign access policy and store keys and passwords

Version Number     Date            ModifiedBy                  Description
--------------------------------------------------------------------------------------
v1.0               28-03-2020      Vijaybabu Nakkonda          Initial Version

Execution Method: Execute the whole file from powershell

#>

# Set the Azure context to the provided subscription ID
$subscriptionId = [Environment]::GetEnvironmentVariable("demo_subscriptionId", "User")
Select-AzSubscription -SubscriptionId $subscriptionId

#Begin-------------------Azure Parameters section---------------------------------------
$location = "westeurope"
$resourceGroupName = [Environment]::GetEnvironmentVariable("demo_resourceGroupName", "User")

#Azure Data Lake Gen2 parameters
$storage_account_name = "sadeltalakeadls"
$container = "deltalake"

#Azure Keyvault parameters
$key_vault_name = [Environment]::GetEnvironmentVariable("demo_key_vault_name", "User")

#Azure Active Directory parameters
$Service_Principle_DisplayName = "sp_deltalakeadls"
$credProps = @{
    StartDate = Get-Date
    EndDate = (Get-Date -Year 2029)
    KeyId = (New-Guid).ToString()
    Value = 'SP_deltalakepassword!3373'
 }
 #sql database parameters
 $password = [Environment]::GetEnvironmentVariable("demo_password", "User")
 #End-------------------Azure Parameters section---------------------------------------

#1. Create a new storage account. Enable hierarchical name space for utilizing Azure Data Lake Storage Gen2  


$sa = Get-AzStorageAccount -ResourceGroupName $resourceGroupName -Name $storage_account_name -ErrorAction SilentlyContinue  
If (-Not $sa) {  
    $sa = New-AzStorageAccount -ResourceGroupName $resourceGroupName `
        -Name $storage_account_name `
        -Location $location `
        -SkuName 'Standard_RAGRS' `
        -Kind 'StorageV2' `
        -EnableHierarchicalNamespace $true  
    Write-Host "Storage account name " $sa.StorageAccountName " created successfully."  
} else {  
    Write-Host "Storage account name " $storage_account_name " already exists"  
}  
  
#1.1 Create a new container within storage account  
$con = Get-AzStorageContainer -Name $container -Context $sa.Context -ErrorAction SilentlyContinue  
If (-Not $con) {  
    $con = New-AzStorageContainer -Name $container -Permission Off -Context $sa.Context  
} else {  
    Write-Host "Container  " $con.Name " already exists"  
}  
        
 
#2. Create Service Principle and assign password
$sp = Get-AzAdServicePrincipal -DisplayName $Service_Principle_DisplayName -ErrorAction SilentlyContinue
If (-Not $sp)
{
  $sp = New-AzAdServicePrincipal -DisplayName $Service_Principle_DisplayName
  $credentials = New-Object Microsoft.Azure.Graph.RBAC.Models.PasswordCredential -Property $credProps
  Set-AzADServicePrincipal -ObjectId $sp.Id -PasswordCredential $credentials
} else {
  Write-Host "Service Principle " $sp.DisplayName " already exists"
}
$appId = $sp.ApplicationId
#2.1 Set password to  service princple

#3.Grant service principle to access Storage account (storage account level access)
$ra = Get-AzRoleAssignment -ObjectId $Sp.Id -RoleDefinitionName "Storage Blob Data Contributor" -ErrorAction SilentlyContinue
If (-Not $ra)
{
New-AzRoleAssignment -ObjectId $sp.Id -RoleDefinitionName "Storage Blob Data Contributor" -Scope "/subscriptions/$subscriptionId/resourceGroups/$resourceGroupName/providers/Microsoft.Storage/storageAccounts/$storage_account_name"

#New-AzRoleAssignment -ApplicationId $appId `
 # -RoleDefinitionName "Storage Blob Data Contributor" `
  #-Scope  "/subscriptions/$subscriptionId/resourceGroups/$resourceGroupName/providers/Microsoft.Storage/storageAccounts/$storage_account_name"
} else {
  Write-Host "Role assignment for Service Principle on storage account already exists"
}

#4. Create Azure Keyvault, assign access policy and store keys and passwords
$kv = Get-AzKeyVault -VaultName $key_vault_name -resourceGroupName $resourceGroupName -ErrorAction SilentlyContinue
if (-Not $kv)
{
  $kv = New-AzKeyVault -VaultName $key_vault_name -resourceGroupName $resourceGroupName -Location $location
  #4.1 Assign get,set,list,delete access policy to current user on secrets of Azure Key vault
  $current_user=Get-AzADUser -SearchString (Get-AzContext).Account.Id
  Set-AzKeyVaultAccessPolicy -VaultName $key_vault_name -EmailAddress $current_user.UserPrincipalName -PermissionsToSecrets get,set,list,delete -PassThru

  #4.2 Store the service principle application id in Azure Key vault secret
  $adbspappkey = ConvertTo-SecureString $sp.Id -AsPlainText -Force
  Set-AzKeyVaultSecret -VaultName $key_vault_name -Name 'adbspappkey' -SecretValue $adbspappkey

  #4.3 Store the service principle password in Azure Key vault secret
  $adbspsecret = ConvertTo-SecureString $credProps.Value -AsPlainText -Force
  Set-AzKeyVaultSecret -VaultName $key_vault_name -Name 'adbspsecret' -SecretValue $adbspsecret

  #4.4 Store the sqldb password in Azure Key vault secret
  $sqldbsecret = ConvertTo-SecureString $password -AsPlainText -Force
  Set-AzKeyVaultSecret -VaultName $key_vault_name -Name 'sqldbsecret' -SecretValue $sqldbsecret
} else {
  Write-Host "Key vault " $kv.VaultName " already exists"
}
