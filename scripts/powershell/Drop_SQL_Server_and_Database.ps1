#script to drop the database and server



Remove-AzSqlServer -ResourceGroupName $resourceGroupName -ServerName $serverName

Remove-AzStorageAccount -Name $account_name -ResourceGroupName $resourceGroup