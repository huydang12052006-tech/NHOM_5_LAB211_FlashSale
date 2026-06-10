$ErrorActionPreference = "Stop"

$javaHome = "C:\Program Files\Java\jdk1.8.0_202"
$javac = Join-Path $javaHome "bin\javac.exe"

if (-not (Test-Path $javac)) {
    throw "JDK 17 compiler not found at $javac"
}

Remove-Item -Recurse -Force "bin/classes", "bin/test-classes" -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path "bin/classes", "bin/test-classes" | Out-Null

$mainSources = Get-ChildItem -Recurse "src" -Filter "*.java" |
    Where-Object { $_.FullName -notlike "*\src\test\*" } |
    ForEach-Object { Resolve-Path -Relative $_.FullName }

$testSources = Get-ChildItem -Recurse "src/test" -Filter "*.java" |
    ForEach-Object { Resolve-Path -Relative $_.FullName }

& $javac -encoding UTF-8 -d "bin/classes" $mainSources
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}

& $javac -encoding UTF-8 -cp "lib/junit-platform-console-standalone-1.10.2.jar;bin/classes" -d "bin/test-classes" $testSources
if ($LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
}
