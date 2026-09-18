param(
    [string]$Branch = "main",
    [switch]$Tags
)

$ErrorActionPreference = "Stop"

git push origin $Branch
git push github-backup $Branch

if ($Tags) {
    git push origin --tags
    git push github-backup --tags
}

Write-Host "Pushed $Branch to Gitee primary and GitHub backup."
