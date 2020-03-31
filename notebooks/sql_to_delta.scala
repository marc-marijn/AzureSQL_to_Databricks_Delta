// Databricks notebook source
val configs = Map(
  "fs.azure.account.auth.type" -> "OAuth",
  "fs.azure.account.oauth.provider.type" -> "org.apache.hadoop.fs.azurebfs.oauth2.ClientCredsTokenProvider",
  "fs.azure.account.oauth2.client.id" -> dbutils.secrets.get(scope = "deltalake", key = "adbspappkey"),
  "fs.azure.account.oauth2.client.secret" -> dbutils.secrets.get(scope = "deltalake", key = "adbspsecret"),
  "fs.azure.account.oauth2.client.endpoint" -> "https://login.microsoftonline.com/1c13b526-3056-482b-94ec-298476e01ea5/oauth2/token")

// Optionally, you can add <directory-name> to the source URI of your mount point.
dbutils.fs.mount(
  source = "abfss://deltalake@sadeltalake.dfs.core.windows.net/",
  mountPoint = "/mnt/deltalake",
  extraConfigs = configs)

// COMMAND ----------

//to check the mounted folder execute below command
%fs ls "mnt"

// COMMAND ----------

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

// COMMAND ----------

//Collect information schema from SQL DB Information_Schema.Tables into SQL_Tables Spark Data Frame
val SQL_Tables = spark.read.jdbc(jdbcUrl, "INFORMATION_SCHEMA.TABLES", connectionProperties)
SQL_Tables.createOrReplaceTempView("SQL_Tables")

// COMMAND ----------

// MAGIC %sql
// MAGIC --List out Create Schema commands on Delta. Each schema will be treated as a database
// MAGIC select array_join(array('create schema ',Table_Schema,';'),'') from SQL_Tables
// MAGIC where
// MAGIC table_schema in ('dbo','SalesLT')
// MAGIC group by Table_Schema

// COMMAND ----------

// MAGIC %sql
// MAGIC --Copy results from previous cell and execte in a seperate cell to create schemas on delta
// MAGIC create schema SalesLT;
// MAGIC create schema dbo;

// COMMAND ----------

// MAGIC %sql 
// MAGIC --Derive Data Frame creation statements per table on SQL.
// MAGIC select array_join(array('val ',table_schema,'_',table_name,' = spark.read.jdbc(jdbcUrl, "',table_schema,'.',table_name,'", connectionProperties)'),'') Scala_cmd
// MAGIC From SQL_Tables where table_type='base table'

// COMMAND ----------

//Execute dataframe statements from previous cell
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

// COMMAND ----------

// MAGIC %sql
// MAGIC --Derive scala statements to save table data from dataframe to mounted storage account
// MAGIC select array_join(array(table_schema,'_',table_name,'.write.format("delta").save("/mnt/deltalake/',table_schema,'/',table_name,'")'),'') Scala_cmd
// MAGIC From SQL_Tables where table_type='base table'

// COMMAND ----------

display(dbo_BuildVersion)

// COMMAND ----------

//Execte dataframe commands to write data on storage account using delta format
//Exception: SQL Columns which have space are throwing exception
//refer Cmd 20 to remove spaces from column header
//For now, we skip one of the table (dbo_BuildVersion) which have space in column header

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

// COMMAND ----------

dbo_ErrorLog.write.format("delta").save("/mnt/deltalake/dbo/ErrorLog")

// COMMAND ----------

// MAGIC %sql
// MAGIC --Derive sql commands to create table from delta location
// MAGIC select array_join(array('create table ',table_schema,'.',table_name,' using delta location "/mnt/deltalake/',table_schema,'/',table_name,'";'),'') Scala_cmd
// MAGIC From SQL_Tables where table_type='base table'

// COMMAND ----------

// MAGIC %sql
// MAGIC --Copy resutls from previous cell and execute to create delta tables
// MAGIC --create table SalesLT.Customer using delta location "/mnt/deltalake/SalesLT/Customer";
// MAGIC --create table SalesLT.ProductModel using delta location "/mnt/deltalake/SalesLT/ProductModel";
// MAGIC --create table SalesLT.ProductDescription using delta location "/mnt/deltalake/SalesLT/ProductDescription";
// MAGIC --create table SalesLT.Product using delta location "/mnt/deltalake/SalesLT/Product";
// MAGIC --create table SalesLT.ProductModelProductDescription using delta location "/mnt/deltalake/SalesLT/ProductModelProductDescription";
// MAGIC --create table SalesLT.ProductCategory using delta location "/mnt/deltalake/SalesLT/ProductCategory";
// MAGIC --create table dbo.BuildVersion using delta location "/mnt/deltalake/dbo/BuildVersion";
// MAGIC create table dbo.ErrorLog using delta location "/mnt/deltalake/dbo/ErrorLog";
// MAGIC create table SalesLT.Address using delta location "/mnt/deltalake/SalesLT/Address";
// MAGIC create table SalesLT.CustomerAddress using delta location "/mnt/deltalake/SalesLT/CustomerAddress";
// MAGIC create table SalesLT.SalesOrderDetail using delta location "/mnt/deltalake/SalesLT/SalesOrderDetail";
// MAGIC create table SalesLT.SalesOrderHeader using delta location "/mnt/deltalake/SalesLT/SalesOrderHeader";

// COMMAND ----------

// MAGIC %sql select SalesOrderID,sum(orderqty),sum(unitprice) from SalesLT.SalesOrderDetail
// MAGIC group by SalesOrderID
// MAGIC order by 3 desc

// COMMAND ----------

// MAGIC %sql
// MAGIC update SalesLT.Address set city='Toronto1'
// MAGIC where
// MAGIC city='Toronto'

// COMMAND ----------

// MAGIC %sql update SalesLT.Address set city='Toronto'
// MAGIC where
// MAGIC city='Toronto1'

// COMMAND ----------

// MAGIC %sql create view SalesLT.newaddress
// MAGIC as
// MAGIC WITH CategoryCTE([ParentProductCategoryID], [ProductCategoryID], [Name]) AS
// MAGIC (
// MAGIC     SELECT [ParentProductCategoryID], [ProductCategoryID], [Name]
// MAGIC     FROM SalesLT.ProductCategory
// MAGIC     WHERE ParentProductCategoryID IS NULL
// MAGIC 
// MAGIC UNION ALL
// MAGIC 
// MAGIC     SELECT C.[ParentProductCategoryID], C.[ProductCategoryID], C.[Name]
// MAGIC     FROM SalesLT.ProductCategory AS C
// MAGIC     INNER JOIN CategoryCTE AS BC ON BC.ProductCategoryID = C.ParentProductCategoryID
// MAGIC )
// MAGIC 
// MAGIC SELECT PC.[Name] AS [ParentProductCategoryName], CCTE.[Name] as [ProductCategoryName], CCTE.[ProductCategoryID]
// MAGIC FROM CategoryCTE AS CCTE
// MAGIC JOIN SalesLT.ProductCategory AS PC
// MAGIC ON PC.[ProductCategoryID] = CCTE.[ParentProductCategoryID]

// COMMAND ----------

// MAGIC %sql select * From SalesLT.newaddress

// COMMAND ----------

//dbo_BuildVersion.write.format("delta").save("/mnt/sadeltalake/dbo/BuildVersion")
dbo_BuildVersion.printSchema()

var newDf =  dbo_BuildVersion 
  dbo_BuildVersion.columns.foreach {col => newDf = newDf.withColumnRenamed(col, col.replaceAll(" ", "") )}
  newDf.printSchema()
//--spark.sql("CREATE TABLE Address USING DELTA LOCATION '/mnt/sadeltalake/Address'")

// COMMAND ----------

newDf.show()

// COMMAND ----------

// MAGIC %sql select * From SalesLT_Customer

// COMMAND ----------

val df = spark.sql("select array_join(array('val ',table_schema,'_',table_name,' = spark.read.jdbc(jdbcUrl, \"',table_schema,'.',table_name,'\", connectionProperties)'),'') Scala_cmd From SQL_Tables where table_type='base table'")

// COMMAND ----------

object HelloWorld {
  def main(): Unit = {
    println("Hello, world!")
  }
}

// COMMAND ----------

HelloWorld.main()

// COMMAND ----------

SalesLT_Address.write.format("delta").save("/mnt/sadeltalake/Address")
spark.sql("CREATE TABLE Address USING DELTA LOCATION '/mnt/sadeltalake/Address'")

// COMMAND ----------

// MAGIC %sql select * From scala_df_creation_commands

// COMMAND ----------

// MAGIC %sql select array_join(array('val ',table_schema,'_',table_name,'.write.format("delta").save("/mnt/sadeltalake/',table_schema,'/',table_name),'') Scala_cmd
// MAGIC From SQL_Tables where table_type='base table'

// COMMAND ----------


SalesLT_Address.write.format("delta").save("/mnt/sadeltalake/Address")
spark.sql("CREATE TABLE Address USING DELTA LOCATION '/mnt/sadeltalake/Address'")

// COMMAND ----------

// MAGIC %sql select * From Tables

// COMMAND ----------

val SalesLT_Address = spark.read.jdbc(jdbcUrl, "SalesLT.Address", connectionProperties)
val SalesLT_Customer = spark.read.jdbc(jdbcUrl, "SalesLT.Customer", connectionProperties)
val SalesLT_CustomerAddress = spark.read.jdbc(jdbcUrl, "SalesLT.CustomerAddress", connectionProperties)

// COMMAND ----------

SalesLT_Address.write.format("delta").save("/mnt/sadeltalake/Address")
spark.sql("CREATE TABLE Address USING DELTA LOCATION '/mnt/sadeltalake/Address'")

// COMMAND ----------

SalesLT_Customer.write.format("delta").save("/mnt/sadeltalake/Customer")

// COMMAND ----------



// COMMAND ----------

// MAGIC %sql select * from Address