$PSVersionTable

#Login and setting up the Azure context
Connect-AzAccount
Get-AzTenant

Get-AzSubscription
$sub = Get-AzSubscription | Select-Object Name
write-output $sub
Set-AzContext -TenandId "1c13b526-3056-482b-94ec-298476e01ea5"


Get-AzContext
Set-AzContext 

Login-AzAccount -TenantId "1c13b526-3056-482b-94ec-298476e01ea5"
#Az module setup
(Get-Module -ListAvailable | Where-Object{ $_.Name -like '*Azure*' }) | Select-Object Version, Name, Author | Format-Table;

Get-InstalledModule -Name AzureRM -AllVersions
Get-InstalledModule
Get-InstalledModule -Name Azure -AllVersions
Get-NetIPAddress | Where-Object {$_.AddressFamily -eq 'IPv4'}

Uninstall-Module Azure

$versions = (Get-InstalledModule Azure -AllVersions | Select-Object Version)
$versions | foreach { Uninstall-AllModules -TargetModule Azure -Version ($_.Version) -Force }

foreach ($module in (Get-Module -ListAvailable AzureRM*).Name |Get-Unique) {
    write-host "Removing Module $module"
    Uninstall-module $module
 }

 Disconnect-AzAccount
 Get-AzureRMSubscription