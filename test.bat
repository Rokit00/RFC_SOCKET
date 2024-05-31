set "FOLDER_PATH=C:\Users\biznbank\Downloads\RFC_SOCKET"

if exist "%FOLDER_PATH%" (
    rmdir /s /q "%FOLDER_PATH%"
    if exist "%FOLDER_PATH%" (
        echo 폴더 삭제 실패: %FOLDER_PATH%
    ) else (
        echo 폴더가 삭제되었습니다.
    )
) else (
    echo 폴더를 찾을 수 없습니다.
)