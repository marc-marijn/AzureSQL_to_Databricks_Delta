// Databricks notebook source
val configs = Map(
  "fs.azure.account.auth.type" -> "OAuth",
  "fs.azure.account.oauth.provider.type" -> "org.apache.hadoop.fs.azurebfs.oauth2.ClientCredsTokenProvider",
  "fs.azure.account.oauth2.client.id" -> dbutils.secrets.get(scope = "users", key = "adbspkey"),
  "fs.azure.account.oauth2.client.secret" -> dbutils.secrets.get(scope = "users", key = "adbspsecret"),
  "fs.azure.account.oauth2.client.endpoint" -> "https://login.microsoftonline.com/1c13b526-3056-482b-94ec-298476e01ea5/oauth2/token")

// Optionally, you can add <directory-name> to the source URI of your mount point.
dbutils.fs.mount(
  source = "abfss://adb@sadeltalake.dfs.core.windows.net/",
  mountPoint = "/mnt/sadeltalake",
  extraConfigs = configs)

// COMMAND ----------

// MAGIC %fs ls "mnt/sadeltalake"

// COMMAND ----------

import java.util.Properties
val jdbcHostname = "sqlsrv-nklabs.database.windows.net"
val jdbcPort = 1433
val jdbcDatabase = "Adventureworks"

val jdbcUsername = "sqladmin"
val jdbcPassword = "Tiger123"
// Create the JDBC URL without passing in the user and password parameters.
val jdbcUrl = s"jdbc:sqlserver://${jdbcHostname}:${jdbcPort};database=${jdbcDatabase}"

// Create a Properties() object to hold the parameters.

val driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
val connectionProperties = new Properties()

connectionProperties.put("user", s"${jdbcUsername}")
connectionProperties.put("password", s"${jdbcPassword}")
connectionProperties.setProperty("Driver", driverClass)

// COMMAND ----------

val SQL_Tables = spark.read.jdbc(jdbcUrl, "INFORMATION_SCHEMA.TABLES", connectionProperties)
SQL_Tables.createOrReplaceTempView("SQL_Tables")

// COMMAND ----------

// MAGIC %sql 
// MAGIC select array_join(array('val ',table_schema,'_',table_name,' = spark.read.jdbc(jdbcUrl, "',table_schema,'.',table_name,'", connectionProperties)'),'') Scala_cmd
// MAGIC From SQL_Tables where table_type='base table'

// COMMAND ----------

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
// MAGIC select array_join(array(table_schema,'_',table_name,'.write.format("delta").save("/mnt/sadeltalake/',table_schema,'/',table_name,'")'),'') Scala_cmd
// MAGIC From SQL_Tables where table_type='base table'
// MAGIC 
// MAGIC --SalesLT_Address.write.format("delta").save("/mnt/sadeltalake/Address")

// COMMAND ----------

display(dbo_BuildVersion)

// COMMAND ----------

SalesLT_Customer.write.format("delta").save("/mnt/sadeltalake/SalesLT/Customer")
SalesLT_ProductModel.write.format("delta").save("/mnt/sadeltalake/SalesLT/ProductModel")
SalesLT_ProductDescription.write.format("delta").save("/mnt/sadeltalake/SalesLT/ProductDescription")
SalesLT_Product.write.format("delta").save("/mnt/sadeltalake/SalesLT/Product")
SalesLT_ProductModelProductDescription.write.format("delta").save("/mnt/sadeltalake/SalesLT/ProductModelProductDescription")
SalesLT_ProductCategory.write.format("delta").save("/mnt/sadeltalake/SalesLT/ProductCategory")
dbo_BuildVersion.write.format("delta").save("/mnt/sadeltalake/dbo/BuildVersion")
dbo_ErrorLog.write.format("delta").save("/mnt/sadeltalake/dbo/ErrorLog")
SalesLT_Address.write.format("delta").save("/mnt/sadeltalake/SalesLT/Address")
SalesLT_CustomerAddress.write.format("delta").save("/mnt/sadeltalake/SalesLT/CustomerAddress")
SalesLT_SalesOrderDetail.write.format("delta").save("/mnt/sadeltalake/SalesLT/SalesOrderDetail")
SalesLT_SalesOrderHeader.write.format("delta").save("/mnt/sadeltalake/SalesLT/SalesOrderHeader")

// COMMAND ----------

// MAGIC %sql
// MAGIC 
// MAGIC --spark.sql("CREATE TABLE Address USING DELTA LOCATION '/mnt/sadeltalake/Address'")

// COMMAND ----------

select array_join(array('val ',table_schema,'_',table_name,' = spark.read.jdbc(jdbcUrl, "',table_schema,'.',table_name,'", connectionProperties)'),'') Scala_cmd
From SQL_Tables where table_type='base table'

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