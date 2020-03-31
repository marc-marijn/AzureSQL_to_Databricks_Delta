# Migrate Azure SQL DB Adventureworks to Azure Databricks Delta 
In this solution, I would like to walkthorugh  how to migrate Azure SQL DB to Data Bricks Delta. This use case would be interesting for many working on migration, platform evaluation, PoC, etc. Especially, when team realize the growth of data in terms of Volume (size), Variety (semi structured / un-structured) and Velocity (Scalability) it is required to evaluate and choose best platform for migration. In many of modern data warehouse solution architects, Microsoft recommends Azure Data Lake Store Gen 2 with Azure Data Bricks. This article provides step by step procedure to migrate Azure SQL DB to Azure Data Bricks Delta. 

# Getting Started
Below steps are followed for this PoC
1. Provision Azure SQL DB 
        Created through powershell
2. Setup Azure Data Lake Gen2, Service Principle Account and Access to ADLSG2
        Created through powershell
        Prerequisite: Azure keyvault is created through UI
3. Provision Azure Databricks Workspace and mount ADLSG2 container
        Azure Databricks provisioned through UI

3. Add firewall rule for ADB on Azure SQL DB
3. Create a Storage Account - through powershell
4. Create a service principle in AAD - through powershell
4.5 Grant proxy account to access storage account - through powershell
5. Grant proxy account to use Azure Data Bricks with storage account
6. copy sql table to delta tables
7. check query performance between Azure SQL DB vs Delta
8. Views for data masking

# Build and Test
Tested in Azure Westeurope location

# Contribute
Contributions to AzureSQL_to_Databricks_Delta are welcome. Here is how you can contribute:

Submit bugs and help us verify fixes
Submit pull requests for bug fixes and features and discuss existing proposals

If you want to learn more about creating good readme files then refer the following [guidelines](https://docs.microsoft.com/en-us/azure/devops/repos/git/create-a-readme?view=azure-devops). You can also seek inspiration from the below readme files:
- [ASP.NET Core](https://github.com/aspnet/Home)
- [Visual Studio Code](https://github.com/Microsoft/vscode)
- [Chakra Core](https://github.com/Microsoft/ChakraCore)