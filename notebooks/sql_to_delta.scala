// Databricks notebook source
// Setup: Mount Storage account to Azure Databricks
// Reference: https://docs.databricks.com/data/data-sources/azure/azure-datalake-gen2.html
val configs = Map(
  "fs.azure.account.auth.type" -> "OAuth",
  "fs.azure.account.oauth.provider.type" -> "org.apache.hadoop.fs.azurebfs.oauth2.ClientCredsTokenProvider",
  "fs.azure.account.oauth2.client.id" -> dbutils.secrets.get(scope = "deltalake", key = "adbspappkey"),
  "fs.azure.account.oauth2.client.secret" -> dbutils.secrets.get(scope = "deltalake", key = "adbspsecret"),
  "fs.azure.account.oauth2.client.endpoint" -> "https://login.microsoftonline.com/<TenantID>/oauth2/token")

// Optionally, you can add <directory-name> to the source URI of your mount point.
dbutils.fs.mount(
  source = "abfss://<Container>@<Storage Account Name>.dfs.core.windows.net/",
  mountPoint = "/mnt/deltalake",
  extraConfigs = configs)

//Command took 11.55 seconds

// COMMAND ----------

//Checkpoint: to check the mounted folder execute below command
%fs ls "mnt"
//Command took 0.24 seconds

// COMMAND ----------

//Steup: Create connection to Azure SQL DB from Data bricks
//Reference: https://docs.microsoft.com/en-us/azure/databricks/data/data-sources/sql-databases
//During Execution below command, there will be an exception that cluster(Scala) IP address is not allowed to connect SQL.
//Note this IP address and add this in Azure SQL firewall using powershell
import java.util.Properties
val jdbcHostname = "sqlsrv-myapp.database.windows.net"
val jdbcPort = 1433
val jdbcDatabase = "Adventureworks"

val jdbcUsername = "sqladmin"
val jdbcPassword = dbutils.secrets.get(scope = "deltalake", key = "sqldbsecret")
// Create the JDBC URL without passing in the user and password parameters.
val jdbcUrl = s"jdbc:sqlserver://${jdbcHostname}:${jdbcPort};database=${jdbcDatabase}"

// Create a Properties() object to hold the parameters.

val driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
val connectionProperties = new Properties()

connectionProperties.put("user", s"${jdbcUsername}")
connectionProperties.put("password", s"${jdbcPassword}")
connectionProperties.setProperty("Driver", driverClass)

// output:
// import java.util.Properties
// jdbcHostname: String = sqlsrv-myapp.database.windows.net
// jdbcPort: Int = 1433
// jdbcDatabase: String = Adventureworks
// jdbcUsername: String = sqladmin
// jdbcPassword: String = [REDACTED]
// jdbcUrl: String = jdbc:sqlserver://sqlsrv-myapp.database.windows.net:1433;database=Adventureworks
// driverClass: String = com.microsoft.sqlserver.jdbc.SQLServerDriver
// connectionProperties: java.util.Properties = {user=sqladmin, password=[REDACTED], Driver=com.microsoft.sqlserver.jdbc.SQLServerDriver}
// res6: Object = null
//Command took 0.87 seconds

// COMMAND ----------

//Migration Command 1: Scala: Collect Information_Schema.Tables from Azure SQL DB to Spark Data Frame SQL_Tables
val SQL_Tables = spark.read.jdbc(jdbcUrl, "INFORMATION_SCHEMA.TABLES", connectionProperties)
SQL_Tables.createOrReplaceTempView("SQL_Tables")

//Output:
// SQL_Tables:org.apache.spark.sql.DataFrame
// TABLE_CATALOG:string
// TABLE_SCHEMA:string
// TABLE_NAME:string
// TABLE_TYPE:string
// SQL_Tables: org.apache.spark.sql.DataFrame = [TABLE_CATALOG: string, TABLE_SCHEMA: string ... 2 more fields]
//Command took 0.73 seconds

// COMMAND ----------

// MAGIC %sql
// MAGIC --Migration Command 2: SQL: Prepare statements to Create Schema on Delta
// MAGIC --On Delta, each schema will be treated as a database. Check the data tab to see new database created for each schema
// MAGIC --During Execution of below command, there will be an exception that cluster(Sql) IP address is not allowed to connect SQL.
// MAGIC --Note this IP address and add this in Azure SQL firewall using powershell
// MAGIC 
// MAGIC select array_join(array('create schema ',Table_Schema,';'),'') from SQL_Tables
// MAGIC where
// MAGIC table_schema in ('dbo','SalesLT') --We select only two schemas for example
// MAGIC group by Table_Schema
// MAGIC 
// MAGIC -- Output:
// MAGIC -- create schema SalesLT;
// MAGIC -- create schema dbo;
// MAGIC -- Command took 0.50 seconds

// COMMAND ----------

// MAGIC %sql
// MAGIC --Migration Command 2.1: SQL: Create Schema on Delta
// MAGIC --Copy results from previous cell and execute
// MAGIC create schema SalesLT;
// MAGIC create schema dbo;
// MAGIC -- Output:
// MAGIC -- OK
// MAGIC -- Command took 0.75 seconds

// COMMAND ----------

// MAGIC %sql 
// MAGIC --Migration Command 3: SQL: Prepare statements to Create Dataframe per SQL table
// MAGIC select array_join(array('val ',table_schema,'_',table_name,' = spark.read.jdbc(jdbcUrl, "',table_schema,'.',table_name,'", connectionProperties)'),'') Scala_cmd
// MAGIC From SQL_Tables where table_type='base table'
// MAGIC 
// MAGIC -- Output:
// MAGIC -- val SalesLT_Customer = spark.read.jdbc(jdbcUrl, "SalesLT.Customer", connectionProperties)
// MAGIC -- val SalesLT_ProductModel = spark.read.jdbc(jdbcUrl, "SalesLT.ProductModel", connectionProperties)
// MAGIC -- val SalesLT_ProductDescription = spark.read.jdbc(jdbcUrl, "SalesLT.ProductDescription", connectionProperties)
// MAGIC -- val SalesLT_Product = spark.read.jdbc(jdbcUrl, "SalesLT.Product", connectionProperties)
// MAGIC -- val SalesLT_ProductModelProductDescription = spark.read.jdbc(jdbcUrl, "SalesLT.ProductModelProductDescription", connectionProperties)
// MAGIC -- val SalesLT_ProductCategory = spark.read.jdbc(jdbcUrl, "SalesLT.ProductCategory", connectionProperties)
// MAGIC -- val dbo_BuildVersion = spark.read.jdbc(jdbcUrl, "dbo.BuildVersion", connectionProperties)
// MAGIC -- val dbo_ErrorLog = spark.read.jdbc(jdbcUrl, "dbo.ErrorLog", connectionProperties)
// MAGIC -- val SalesLT_Address = spark.read.jdbc(jdbcUrl, "SalesLT.Address", connectionProperties)
// MAGIC -- val SalesLT_CustomerAddress = spark.read.jdbc(jdbcUrl, "SalesLT.CustomerAddress", connectionProperties)
// MAGIC -- val SalesLT_SalesOrderDetail = spark.read.jdbc(jdbcUrl, "SalesLT.SalesOrderDetail", connectionProperties)
// MAGIC -- val SalesLT_SalesOrderHeader = spark.read.jdbc(jdbcUrl, "SalesLT.SalesOrderHeader", connectionProperties)
// MAGIC -- Command took 0.17 seconds

// COMMAND ----------

//Execute dataframe statements from previous cell
//Migration Command 3.1: Scala: Create Dataframe per SQL table
//Copy results from previous cell and execute
val SalesLT_Customer = spark.read.jdbc(jdbcUrl, "SalesLT.Customer", connectionProperties)
val SalesLT_ProductModel = spark.read.jdbc(jdbcUrl, "SalesLT.ProductModel", connectionProperties)
val SalesLT_ProductDescription = spark.read.jdbc(jdbcUrl, "SalesLT.ProductDescription", connectionProperties)
val SalesLT_Product = spark.read.jdbc(jdbcUrl, "SalesLT.Product", connectionProperties)
val SalesLT_ProductModelProductDescription = spark.read.jdbc(jdbcUrl, "SalesLT.ProductModelProductDescription", connectionProperties)
val SalesLT_ProductCategory = spark.read.jdbc(jdbcUrl, "SalesLT.ProductCategory", connectionProperties)
val dbo_BuildVersion = spark.read.jdbc(jdbcUrl, "dbo.BuildVersion", connectionProperties)
val dbo_ErrorLog = spark.read.jdbc(jdbcUrl, "dbo.ErrorLog", connectionProperties)
val SalesLT_Address = spark.read.jdbc(jdbcUrl, "SalesLT.Address", connectionProperties)
val SalesLT_CustomerAddress = spark.read.jdbc(jdbcUrl, "SalesLT.CustomerAddress", connectionProperties)
val SalesLT_SalesOrderDetail = spark.read.jdbc(jdbcUrl, "SalesLT.SalesOrderDetail", connectionProperties)
val SalesLT_SalesOrderHeader = spark.read.jdbc(jdbcUrl, "SalesLT.SalesOrderHeader", connectionProperties)

// Output :
// SalesLT_Customer: org.apache.spark.sql.DataFrame = [CustomerID: int, NameStyle: boolean ... 13 more fields]
// SalesLT_ProductModel: org.apache.spark.sql.DataFrame = [ProductModelID: int, Name: string ... 3 more fields]
// SalesLT_ProductDescription: org.apache.spark.sql.DataFrame = [ProductDescriptionID: int, Description: string ... 2 more fields]
// SalesLT_Product: org.apache.spark.sql.DataFrame = [ProductID: int, Name: string ... 15 more fields]
// SalesLT_ProductModelProductDescription: org.apache.spark.sql.DataFrame = [ProductModelID: int, ProductDescriptionID: int ... 3 more fields]
// SalesLT_ProductCategory: org.apache.spark.sql.DataFrame = [ProductCategoryID: int, ParentProductCategoryID: int ... 3 more fields]
// dbo_BuildVersion: org.apache.spark.sql.DataFrame = [SystemInformationID: int, Database Version: string ... 2 more fields]
// dbo_ErrorLog: org.apache.spark.sql.DataFrame = [ErrorLogID: int, ErrorTime: timestamp ... 7 more fields]
// SalesLT_Address: org.apache.spark.sql.DataFrame = [AddressID: int, AddressLine1: string ... 7 more fields]
// SalesLT_CustomerAddress: org.apache.spark.sql.DataFrame = [CustomerID: int, AddressID: int ... 3 more fields]
// SalesLT_SalesOrderDetail: org.apache.spark.sql.DataFrame = [SalesOrderID: int, SalesOrderDetailID: int ... 7 more fields]
// SalesLT_SalesOrderHeader: org.apache.spark.sql.DataFrame = [SalesOrderID: int, RevisionNumber: int ... 20 more fields]
// Command took 1.00 second

// COMMAND ----------

// MAGIC %sql
// MAGIC --Migration Command 4: SQL: Prepare statements to Write data from Dataframe to Storage Account
// MAGIC --Derive statements to save table data from dataframe to mounted storage account
// MAGIC select array_join(array(table_schema,'_',table_name,'.write.format("delta").save("/mnt/deltalake/',table_schema,'/',table_name,'")'),'') Scala_cmd
// MAGIC From SQL_Tables where table_type='base table'
// MAGIC 
// MAGIC -- Output:
// MAGIC -- SalesLT_Customer.write.format("delta").save("/mnt/deltalake/SalesLT/Customer")
// MAGIC -- SalesLT_ProductModel.write.format("delta").save("/mnt/deltalake/SalesLT/ProductModel")
// MAGIC -- SalesLT_ProductDescription.write.format("delta").save("/mnt/deltalake/SalesLT/ProductDescription")
// MAGIC -- SalesLT_Product.write.format("delta").save("/mnt/deltalake/SalesLT/Product")
// MAGIC -- SalesLT_ProductModelProductDescription.write.format("delta").save("/mnt/deltalake/SalesLT/ProductModelProductDescription")
// MAGIC -- SalesLT_ProductCategory.write.format("delta").save("/mnt/deltalake/SalesLT/ProductCategory")
// MAGIC -- dbo_BuildVersion.write.format("delta").save("/mnt/deltalake/dbo/BuildVersion")
// MAGIC -- dbo_ErrorLog.write.format("delta").save("/mnt/deltalake/dbo/ErrorLog")
// MAGIC -- SalesLT_Address.write.format("delta").save("/mnt/deltalake/SalesLT/Address")
// MAGIC -- SalesLT_CustomerAddress.write.format("delta").save("/mnt/deltalake/SalesLT/CustomerAddress")
// MAGIC -- SalesLT_SalesOrderDetail.write.format("delta").save("/mnt/deltalake/SalesLT/SalesOrderDetail")
// MAGIC -- SalesLT_SalesOrderHeader.write.format("delta").save("/mnt/deltalake/SalesLT/SalesOrderHeader")
// MAGIC -- Command took 0.12 seconds 

// COMMAND ----------

//Migration Command 4.1: Scala: Write data from Dataframe to Storage Account
//Execte dataframe commands to write data on storage account using delta format
//Exception: SQL Columns which are having space are throws exception. To avoid, remove any spaces or special characters from column names
//refer Cmd below to remove any spaces from column header
// dbo_BuildVersion.printSchema()
// var newDf =  dbo_BuildVersion 
//   dbo_BuildVersion.columns.foreach {col => newDf = newDf.withColumnRenamed(col, col.replaceAll(" ", "") )}
//   newDf.printSchema()
//For now, we skip one of the table (dbo_BuildVersion) which is having a space in one of column header

SalesLT_Customer.write.format("delta").save("/mnt/deltalake/SalesLT/Customer")
SalesLT_ProductModel.write.format("delta").save("/mnt/deltalake/SalesLT/ProductModel")
SalesLT_ProductDescription.write.format("delta").save("/mnt/deltalake/SalesLT/ProductDescription")
SalesLT_Product.write.format("delta").save("/mnt/deltalake/SalesLT/Product")
SalesLT_ProductModelProductDescription.write.format("delta").save("/mnt/deltalake/SalesLT/ProductModelProductDescription")
SalesLT_ProductCategory.write.format("delta").save("/mnt/deltalake/SalesLT/ProductCategory")
//dbo_BuildVersion.write.format("delta").save("/mnt/deltalake/dbo/BuildVersion")
dbo_ErrorLog.write.format("delta").save("/mnt/deltalake/dbo/ErrorLog")
SalesLT_Address.write.format("delta").save("/mnt/deltalake/SalesLT/Address")
SalesLT_CustomerAddress.write.format("delta").save("/mnt/deltalake/SalesLT/CustomerAddress")
SalesLT_SalesOrderDetail.write.format("delta").save("/mnt/deltalake/SalesLT/SalesOrderDetail")
SalesLT_SalesOrderHeader.write.format("delta").save("/mnt/deltalake/SalesLT/SalesOrderHeader")

// Output:
// Command took 21.63 seconds

// COMMAND ----------

// MAGIC %sql
// MAGIC -- Migration Command 5: SQL: Prepare statements to create table by referring location on Storage account, where data was written.
// MAGIC 
// MAGIC select array_join(array('create table ',table_schema,'.',table_name,' using delta location "/mnt/deltalake/',table_schema,'/',table_name,'";'),'') Scala_cmd
// MAGIC From SQL_Tables where table_type='base table'
// MAGIC 
// MAGIC -- Output:
// MAGIC -- create table SalesLT.Customer using delta location "/mnt/deltalake/SalesLT/Customer";
// MAGIC -- create table SalesLT.ProductModel using delta location "/mnt/deltalake/SalesLT/ProductModel";
// MAGIC -- create table SalesLT.ProductDescription using delta location "/mnt/deltalake/SalesLT/ProductDescription";
// MAGIC -- create table SalesLT.Product using delta location "/mnt/deltalake/SalesLT/Product";
// MAGIC -- create table SalesLT.ProductModelProductDescription using delta location "/mnt/deltalake/SalesLT/ProductModelProductDescription";
// MAGIC -- create table SalesLT.ProductCategory using delta location "/mnt/deltalake/SalesLT/ProductCategory";
// MAGIC -- create table dbo.BuildVersion using delta location "/mnt/deltalake/dbo/BuildVersion";
// MAGIC -- create table dbo.ErrorLog using delta location "/mnt/deltalake/dbo/ErrorLog";
// MAGIC -- create table SalesLT.Address using delta location "/mnt/deltalake/SalesLT/Address";
// MAGIC -- create table SalesLT.CustomerAddress using delta location "/mnt/deltalake/SalesLT/CustomerAddress";
// MAGIC -- create table SalesLT.SalesOrderDetail using delta location "/mnt/deltalake/SalesLT/SalesOrderDetail";
// MAGIC -- create table SalesLT.SalesOrderHeader using delta location "/mnt/deltalake/SalesLT/SalesOrderHeader";
// MAGIC -- Command took 0.16 seconds

// COMMAND ----------

// MAGIC %sql
// MAGIC -- Migration Command 5.1: SQL: Execute create table by referring location on Storage account, where data was written.
// MAGIC -- Copy resutls from previous cell and execute to create delta tables. Skip dbo.BuildVersion (This was not persisted on storage account due to spaces in column header)
// MAGIC create table SalesLT.Customer using delta location "/mnt/deltalake/SalesLT/Customer";
// MAGIC create table SalesLT.ProductModel using delta location "/mnt/deltalake/SalesLT/ProductModel";
// MAGIC create table SalesLT.ProductDescription using delta location "/mnt/deltalake/SalesLT/ProductDescription";
// MAGIC create table SalesLT.Product using delta location "/mnt/deltalake/SalesLT/Product";
// MAGIC create table SalesLT.ProductModelProductDescription using delta location "/mnt/deltalake/SalesLT/ProductModelProductDescription";
// MAGIC create table SalesLT.ProductCategory using delta location "/mnt/deltalake/SalesLT/ProductCategory";
// MAGIC --create table dbo.BuildVersion using delta location "/mnt/deltalake/dbo/BuildVersion";
// MAGIC create table dbo.ErrorLog using delta location "/mnt/deltalake/dbo/ErrorLog";
// MAGIC create table SalesLT.Address using delta location "/mnt/deltalake/SalesLT/Address";
// MAGIC create table SalesLT.CustomerAddress using delta location "/mnt/deltalake/SalesLT/CustomerAddress";
// MAGIC create table SalesLT.SalesOrderDetail using delta location "/mnt/deltalake/SalesLT/SalesOrderDetail";
// MAGIC create table SalesLT.SalesOrderHeader using delta location "/mnt/deltalake/SalesLT/SalesOrderHeader";
// MAGIC 
// MAGIC -- Output:
// MAGIC -- OK
// MAGIC -- Command took 2.40 seconds

// COMMAND ----------

// Migration Completed Successfully: We have finished migrating tables from Azure SQL DB to Delta

// COMMAND ----------

// MAGIC %sql
// MAGIC -- Checkpoint: Execute few sql select queries to see if you are able to query the tables
// MAGIC 
// MAGIC select SalesOrderID,sum(orderqty),sum(unitprice) from SalesLT.SalesOrderDetail
// MAGIC group by SalesOrderID
// MAGIC order by 3 desc
// MAGIC 
// MAGIC -- Output: 
// MAGIC -- Able to see the records from Delta
// MAGIC -- Command took 0.92 seconds. 
// MAGIC -- Out of curiosity, in SQL it took 1 milliseconds. Its not fair to do blind comparison. Also, Performance comparison was not scoped for this project!!!

// COMMAND ----------

// MAGIC %sql
// MAGIC -- Checkpoint: Execute few update queries
// MAGIC update SalesLT.Address set city='Vancouver'
// MAGIC where
// MAGIC city='Toronto'
// MAGIC 
// MAGIC -- Output: 
// MAGIC -- Able to update the records from Delta
// MAGIC -- Command took 3.78 seconds 
// MAGIC -- In SQL it took 11 milliseconds.
// MAGIC 
// MAGIC -- Happy Learning