@echo off
REM JoyProxy - push to GitHub and trigger CI build
REM Usage: deploy.bat YOUR_GITHUB_PAT

setlocal
if "%~1"=="" (
  echo Usage: deploy.bat YOUR_GITHUB_PAT
  echo.
  echo Create a PAT at https://github.com/settings/tokens with "repo" scope.
  exit /b 1
)

set GH_TOKEN=%~1
set HTTP_PROXY=http://sg-xx.edge.joyproxy.com:10000
set HTTPS_PROXY=http://sg-xx.edge.joyproxy.com:10000

echo %GH_TOKEN%| gh auth login --hostname github.com --git-protocol https --with-token
if errorlevel 1 exit /b 1

gh repo create joyproxy/joy-proxy-android --public --source=. --remote=origin --push
if errorlevel 1 (
  git push -u origin main
)

echo.
echo Done. Check build status:
echo https://github.com/joyproxy/joy-proxy-android/actions
echo.
echo APK will appear in Releases after CI completes.
