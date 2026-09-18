param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^v\d+\.\d+\.\d+(?:-[0-9A-Za-z.-]+)?$')]
    [string]$Tag
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$giteeOwner = "doggy-original-licensing"
$giteeRepo = "Any"
$giteeRemote = "https://gitee.com/$giteeOwner/$giteeRepo.git"
$githubRemote = "https://github.com/NickHu777/$giteeRepo.git"
$taskJdk = "D:\1HWT\SoftWare\Android Studio\jbr"
$keystore = "C:\Users\HWT\AppData\Local\Any\signing\any-release.jks"
$protectedPassword = "C:\Users\HWT\AppData\Local\Any\signing\password.dpapi"

function Get-GitCredentialPassword {
    $inputText = "protocol=https`nhost=gitee.com`n`n"
    $credential = $inputText | git credential fill
    $line = $credential | Where-Object { $_ -like "password=*" }
    if (-not $line) {
        throw "No saved Gitee credential was found in Windows Credential Manager."
    }
    return ($line -replace '^password=', '')
}

function Invoke-CurlJson {
    param([string[]]$Arguments)
    $json = & curl.exe @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Gitee API request failed."
    }
    return ($json | ConvertFrom-Json)
}

Push-Location $projectRoot
try {
    if ((git status --porcelain)) {
        throw "Working tree is not clean. Commit changes before releasing."
    }
    if (-not (Test-Path -LiteralPath $keystore)) {
        throw "Signing keystore not found: $keystore"
    }

    $passwordText = (Get-Content -Raw -LiteralPath $protectedPassword).Trim()
    $securePassword = $passwordText | ConvertTo-SecureString
    $passwordHandle = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    try {
        $signingPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordHandle)
        $giteeToken = Get-GitCredentialPassword
        $buildCode = [int](Get-Date -Format "yyMMddHHmm")

        git tag -a $Tag -m "Any $Tag"
        git push origin "refs/tags/$Tag"
        git push github-backup "refs/tags/$Tag"

        $env:JAVA_HOME = $taskJdk
        $env:GITHUB_RUN_NUMBER = "$buildCode"
        $env:GITHUB_REF_NAME = $Tag
        $env:ANY_KEYSTORE_FILE = $keystore
        $env:ANY_KEYSTORE_PASSWORD = $signingPassword
        $env:ANY_KEY_ALIAS = "any-release"
        $env:ANY_KEY_PASSWORD = $signingPassword
        & .\gradlew.bat assembleRelease --console=plain
        if ($LASTEXITCODE -ne 0) { throw "Release APK build failed." }

        $releaseUrl = "https://gitee.com/api/v5/repos/$giteeOwner/$giteeRepo/releases/tags/$Tag?access_token=$giteeToken"
        $release = try { Invoke-CurlJson @('-sS', $releaseUrl) } catch { $null }
        if ($null -eq $release -or $null -eq $release.id) {
            $release = Invoke-CurlJson @(
                '-sS', '-X', 'POST',
                "https://gitee.com/api/v5/repos/$giteeOwner/$giteeRepo/releases?access_token=$giteeToken",
                '--data-urlencode', "tag_name=$Tag",
                '--data-urlencode', "name=Any $Tag",
                '--data-urlencode', "body=Any release $Tag",
                '--data-urlencode', 'target_commitish=main',
                '--data-urlencode', 'prerelease=false'
            )
        }

        if ($null -eq $release.id) {
            throw "Gitee Release creation failed."
        }

        $apk = Join-Path $projectRoot 'app\build\outputs\apk\release\app-release.apk'
        $uploadUrl = "https://gitee.com/api/v5/repos/$giteeOwner/$giteeRepo/releases/$($release.id)/attach_files"
        & curl.exe --fail --show-error --silent --max-time 300 -H 'Expect:' `
            -F "access_token=$giteeToken" `
            -F "owner=$giteeOwner" `
            -F "repo=$giteeRepo" `
            -F "release_id=$($release.id)" `
            -F "file=@$apk" $uploadUrl
        if ($LASTEXITCODE -ne 0) { throw "Gitee APK upload failed." }

        Write-Host "Gitee Release: https://gitee.com/$giteeOwner/$giteeRepo/releases/tag/$Tag"
        Write-Host "APK: https://gitee.com/$giteeOwner/$giteeRepo/releases/download/$Tag/app-release.apk"
    }
    finally {
        if ($passwordHandle -ne [IntPtr]::Zero) {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordHandle)
        }
    }
}
finally {
    Pop-Location
}
