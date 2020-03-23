#####################################################
# Creates a new AAD Application and Service Principal
# https://nedinthecloud.com/2019/07/16/demystifying-azure-ad-service-principals/
#####################################################

# Sign in as a user that is allowed to create an app.
#Connect-AzureAD

# Set variable values
$DisplayName = "sp_adb5"

$sp = Get-AzAdServicePrincipal -DisplayName $DisplayName

$clientsec = [System.Net.NetworkCredential]::new("testing", $sp.Secret).Password

$credProps = @{
    StartDate = Get-Date
    EndDate = (Get-Date -Year 2024)
    KeyId = (New-Guid).ToString()
    Value = 'MySuperAwesomePasswordIs3373'
 }
 $credentials = New-Object Microsoft.Azure.Graph.RBAC.Models.PasswordCredential -Property $credProps

 
 Set-AzADServicePrincipal -ObjectId $sp.Id -PasswordCredential $credentials

 get-aztenant

 $sp = New-AzADServicePrincipal 
$clientsec = [System.Net.NetworkCredential]::new("testing", $sp.Secret).Password
$tenantID = "1c13b526-3056-482b-94ec-298476e01ea5"
$jsonresp = 
@{client_id=$sp.ApplicationId 
    client_secret=$clientsec
    tenant_id=$tenantID}
$jsonresp | ConvertTo-Json