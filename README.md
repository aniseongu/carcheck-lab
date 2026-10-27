<div align="center">

# CarCheck Lab

중고차를 사기 전에 리콜·결함 신고·안전 정보와 내 구매 성향을 한 화면에 모아 "다음에 뭘 확인할지" 알려 주는 Android 앱

![platform](https://img.shields.io/badge/Android-minSdk%2029-124532)
![language](https://img.shields.io/badge/Kotlin-124532?logo=kotlin&logoColor=white)
![course](https://img.shields.io/badge/%EC%84%B1%EA%B7%A0%EA%B4%80%EB%8C%80-%EB%AA%A8%EB%B0%94%EC%9D%BC%EC%95%B1%ED%94%84%EB%A1%9C%EA%B7%B8%EB%9E%98%EB%B0%8D%EC%8B%A4%EC%8A%B5%202026-555555)

</div>

성균관대학교 모바일앱프로그래밍실습(2026년 1학기) 개인 프로젝트입니다. 기획, 화면 설계, Android 앱, AWS 서버를 혼자 만들었습니다.

중고차 정보는 가격, 주행거리, VIN, 리콜, 결함 신고, 날씨처럼 흩어져 있어서 다 보고도 무엇을 해야 할지 모르기 쉽습니다. CarCheck Lab은 이것들을 Risk Report 하나로 묶고, 점수를 정답이 아니라 **딜러를 만나기 전 1차 판단용 안내**로 보여 줍니다. 전문 점검을 대신하지 않습니다.

<p align="center">
  <img src="docs/screenshots/vehicle-select.png" width="22%" alt="차량 선택">
  <img src="docs/screenshots/risk-report.png" width="22%" alt="Risk Report">
  <img src="docs/screenshots/safety-weather.png" width="22%" alt="안전 정보와 날씨 점검">
  <img src="docs/screenshots/budget-black-hole.png" width="22%" alt="Budget Black Hole">
</p>

## 주요 기능

- **Risk Report:** 선택한 차량의 위험 점수와 등급, 최종 판단, 커뮤니티 평점, 점수 요약(Wallet, Budget Defense, 하차감, Practicality, Clean Data), 리콜·결함 신고 수, 안전 등급을 요약부터 보여 줍니다.
- **차량 찾기:** 데모 차량, 브랜드·모델 검색, 17자리 VIN 디코딩 중 어디서 시작해도 같은 보고서로 이어집니다.
- **구매자 프로필:** 도시와 구매 성향(Safety Nerd, Wallet Survivor 등)에 따라 같은 차도 다르게 해석합니다. 프로필은 서버에 저장됩니다.
- **Weather Buying Lens:** 내 도시의 현재 날씨를 시승 때 확인할 점 하나로 바꿔 줍니다(비, 더위, 냉간 시동, EV 주행거리 등).
- **리뷰와 평점:** 계정마다 차량당 리뷰 하나만 쓸 수 있어 평균이 한 사람에게 쏠리지 않습니다. 수정과 삭제가 됩니다.
- **딜러 코치:** 리콜 조치, 정비 이력, 가격, 배터리 상태처럼 판매자에게 물어볼 질문을 규칙 기반으로 보여 줍니다.
- **예산 대안 Top 3:** 최저가, 지갑 방어, 안전 근거, 국산만, EV 후보 등 기준을 바꿔 대안을 다시 고릅니다.
- **Budget Black Hole:** "그 돈이면 이거 사지"로 예산이 계속 올라가는 심리를 풍자한 독창 기능입니다.

## 기술 구성

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)
![OkHttp](https://img.shields.io/badge/OkHttp-4.12-555555)
![AWS Lambda](https://img.shields.io/badge/AWS%20Lambda-FF9900?logo=awslambda&logoColor=white)
![DynamoDB](https://img.shields.io/badge/DynamoDB-4053D6?logo=amazondynamodb&logoColor=white)

| 구분 | 사용 기술 |
| --- | --- |
| 앱 | Kotlin, Android(minSdk 29, targetSdk 36), XML 레이아웃, Activity 11개 |
| 네트워크 | OkHttp, Gson, Kotlin 코루틴 |
| 공공 API | NHTSA 리콜·결함 신고·안전 등급, NHTSA vPIC VIN 디코딩, OpenWeather |
| 서버 | AWS API Gateway, Lambda(Python), DynamoDB |
| 로컬 데이터 | `market_cars.txt`(차량별 기준 가격과 점수), SharedPreferences |

## 구조

```text
Activity(화면)        LoginActivity → ProfileSetupActivity → VehicleSelectActivity
                      → CarSearchActivity / VinDecodeActivity → RiskDashboardActivity
                      → VehicleReviewActivity, IssueDetailActivity, DealerCoachActivity,
                        SavedCarsActivity, GeudonssiBlackHoleActivity
네트워크 클라이언트     NhtsaClient, WeatherClient, ServerAuthClient
분석                  RiskCalculator, WeatherAdvisor, MarketData, MemeAnalyzer, VehicleCategoryHelper
저장                  ServerSessionManager, UserProfileManager, ReviewManager
서버                  server/lambda_function.py (register, login, getProfile, updateProfile)
```

화면은 연도·제조사·모델·차종만 주고받습니다. 보고서 계산은 앱 안에서 하고, 서버는 다시 열어도 남아 있어야 하는 계정과 프로필만 저장합니다.

## 만들면서 신경 쓴 점

- **빈 데이터에도 무너지지 않는 보고서:** NHTSA는 엔드포인트마다 응답 구조가 다르고 `Not Rated`나 빈 결과가 오기도 합니다. 빈 값을 0점으로 치지 않고, 범위를 제한한 점수와 로컬 기준값으로 보충한 뒤 기준값을 썼다는 사실을 화면에 밝힙니다.
- **차종 분류:** 문자열에 `EV`가 들어 있는지만 보면 하이브리드가 전기차로 분류될 수 있어 `VehicleCategoryHelper`로 EV·BEV와 HEV·PHEV를 나눴습니다. 아이콘, EV 날씨 조언, EV 후보 필터가 이 결과를 씁니다.
- **서버 권한:** 개발 중 Lambda 실행 역할에 DynamoDB 읽기·쓰기 권한이 없어 서버 오류가 났고, IAM 역할을 고쳐 가입·로그인·프로필 저장을 맞췄습니다.
- **요약 먼저:** 대시보드에는 결정에 필요한 정보만 두고, 결함 상세·리뷰·딜러 질문·Budget Black Hole은 별도 화면으로 뺐습니다.

## 실행

1. Android Studio에서 **File > Open**으로 이 저장소 루트를 엽니다. Gradle 동기화가 끝나면 에뮬레이터나 기기에서 실행합니다.
2. 날씨 기능을 쓰려면 `app/src/main/java/edu/skku/map/carchecklab/WeatherClient.kt`의 `OPENWEATHER_API_KEY`에 내 [OpenWeather](https://openweathermap.org/api) 키를 넣습니다. 비워 두면 날씨 카드만 기본 안내로 바뀝니다.
3. 로그인 서버가 필요하면 `server/README_server.md`대로 DynamoDB 테이블(`CarCheckUsers`, 파티션 키 `userId`)과 Lambda, API Gateway를 만들고, `ServerAuthClient.kt`의 `AUTH_API_URL`을 내 주소로 바꿉니다.

NHTSA API는 키가 필요 없습니다.

## 한계

- 로그인 서버는 수업 시연용입니다. 비밀번호를 해시하지 않고 저장하므로 실제 서비스에 그대로 쓰면 안 됩니다.
- 리뷰와 저장한 보고서는 기기 안에만 남습니다.
- 위험 점수는 공공 데이터와 로컬 기준값으로 만든 1차 안내입니다. 실제 구매 전에는 점검과 이력 확인이 필요합니다.
- 공식 제조사 로고는 쓰지 않고 차종별 아이콘을 씁니다. 글꼴은 Plus Jakarta Sans와 Inter(SIL Open Font License)입니다.
