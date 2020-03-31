#script to drop the database and server



Remove-AzSqlServer -ResourceGroupName $resourceGroupName -ServerName $serverName

Remove-AzStorageAccount -Name $storage_account_name -ResourceGroupName $resourceGroupName

Remove-AzKeyVault -VaultName $key_vault_name