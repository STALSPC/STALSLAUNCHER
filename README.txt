네비 런처 - Android Studio 프로젝트

빌드 방법
1) Android Studio에서 이 폴더(NaviLauncher)를 Open
2) Gradle Sync가 끝나면 Build > Build APK(s)
3) app/build/outputs/apk/debug/app-debug.apk 를 폰에 설치
4) 설치 후 홈 버튼을 누르면 "기본 홈 앱" 선택창이 뜸 -> 네비 런처 선택
   (설정 > 앱 > 기본 앱 > 홈 앱 에서 One UI 홈과 언제든 바꿀 수 있어요)

GitHub Actions로 빌드하기 (PC 없이/설치 없이)
1) GitHub에 새 저장소를 만들고 이 폴더의 모든 파일(.github 폴더 포함)을 올림
2) 저장소의 Actions 탭 > Build APK 실행(올리면 자동 실행됨)
3) 끝나면 실행 결과 하단 Artifacts의 NaviLauncher-apk 다운로드 -> zip 안에 app-debug.apk
