set "FOLDER_PATH=C:\Users\biznbank\Downloads\RFC_SOCKET"

if exist "%FOLDER_PATH%" (
    rmdir /s /q "%FOLDER_PATH%"
    if exist "%FOLDER_PATH%" (
        echo  %FOLDER_PATH%
    ) else (
        echo f
    )
) else (
    echo ff
)