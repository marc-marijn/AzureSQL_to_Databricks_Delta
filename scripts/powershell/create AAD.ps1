# Sign in as a user that is allowed to create an app.
Connect-AzAccount

# Set variable values
$DisplayName = "Basic AAD"
$Homepage = "http://www.myurl.com"
$IdentifierUris = "http://www.myreplyurl.com"

# Create a new AAD web application
$app = New-AzADApplication -DisplayName $DisplayName -HomePage $Homepage -IdentifierUris $IdentifierUris
write-host $app.ObjectId
write-host $app.ApplicationId


# Creates a service principal and displays the Application Id
$sp = New-AzureADServicePrincipal -AppId $app.ApplicationId
Write-Host ("`nService Principal Application Id = " + $app.AppId)

# Get the service principal key
$key = New-AzureADServicePrincipalPasswordCredential -ObjectId $sp.ObjectId

#Display principal object id
Write-Host ("`nService Principal Object Id = " + $sp.ObjectId)

# Display principal key
Write-Host ("Service Principal Key = " + $key.value)

# Display tenant info
$tenantId = Get-AzureADTenantDetail
Write-Host ("Tenant Id = " + $tenantId.ObjectId + "`n")