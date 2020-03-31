# Migrate Azure SQL DB Adventureworks to Azure Databricks Delta 
In this article, I would like show you how to migrate a database platform from Azure SQL DB to Databricks Delta. This use case would be interesting for many teams working on migration, platform evaluation, PoC, etc. Especially, in foreseen / unforeseen data growth situations in terms of Volume (size), Variety (semi structured / un-structured) and Velocity (scalability) it is required to evaluate and choose best platform for migration. In many modern data warehouse solution architects, Microsoft recommends Azure Data Lake Store Gen 2 with Azure Databricks. This article provides step by step procedure to migrate database from Azure SQL DB to Azure Databricks Delta.

I would like to limit the scope of this article to focus only on migrating SQL Tables to Delta. Migration of Views, Functions, Procedures, Synonyms, user accounts, roles, etc are not covered. An interesting and complex topic of performance comparison between SQL DB and Delta is not covered in this article. 

# Getting Started
Below steps are followed for this PoC

1. Provision Azure SQL DB
2. Setup Azure Data Lake Gen2, Key Vault, Service Principle Account and Access to ADLSG2
3. Provision Azure Databricks Workspace and mount ADLSG2 container   
   3.1 Spin up Azure Databricks workspace
   3.2 Create secret scope on Azure Databricks to connect Azure Key Vault
4. Azure SQL DB to Azure Databricks Delta Migration

# Build and Test
Tested in Azure Westeurope location

# Contribute
Contributions to AzureSQL_to_Databricks_Delta are welcome. Here is how you can contribute:

Submit bugs and help us verify fixes
Submit pull requests for bug fixes and features and discuss existing proposals

