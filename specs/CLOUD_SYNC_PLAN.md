# クラウド同期・ユーザー認証・サブスクリプション課金 設計書

## Context
アプリを有料サービスとして展開するため、ユーザー登録、Firestoreへのデータ・写真保存、RevenueCatによるサブスクリプション課金を追加する。基本機能（ローカル栽培管理）は無料、クラウド同期はプレミアム月額課金。

## 方針
- **課金**: フリーミアム + 月額サブスクリプション（RevenueCat）
- **認証**: Firebase Auth（Googleログイン + メール/パスワード）
- **データ同期**: Firestore（ローカルRoom優先 + バックグラウンド同期）
- **写真保存**: Firebase Storage
- **ローカル機能は認証・課金なしで動作**を維持

---

## Phase 1: Firebase Setup & Auth（5コミット）

### 1-1. 依存関係追加
**Files:** `libs.versions.toml`, `build.gradle.kts`（root + app）
- Firebase BOM, Auth, Firestore, Storage
- Google Services plugin
- Play Services Auth（Credential Manager）
- WorkManager + hilt-work
- `google-services.json` を `app/` に配置（要Firebaseプロジェクト作成）

### 1-2. AuthRepository + Hiltモジュール
**New:** `data/auth/AuthRepository.kt`, `di/FirebaseModule.kt`
- `currentUser: StateFlow<FirebaseUser?>`, `isLoggedIn: StateFlow<Boolean>`
- `signInWithGoogle()`, `signInWithEmail()`, `createAccount()`, `signOut()`
- FirebaseModule: `FirebaseAuth`, `FirebaseFirestore`, `FirebaseStorage` をSingleton提供

### 1-3. ログイン画面UI
**New:** `ui/auth/LoginScreen.kt`, `ui/auth/LoginViewModel.kt`
- Googleログインボタン + メール/パスワードフォーム

### 1-4. ナビゲーション統合
**Modify:** `AppNavHost.kt`, `Routes.kt`
- `Login` ルート追加。設定画面からアクセス（必須ではない）

### 1-5. 設定画面にアカウントセクション
**Modify:** `SettingsScreen.kt`, `SettingsViewModel.kt`
- 未ログイン: 「ログイン」ボタン
- ログイン済み: ユーザー情報表示 + 「ログアウト」ボタン

---

## Phase 2: Room Migration & Sync Infrastructure（5コミット）

### 2-1. SyncStatus enum + TypeConverter
**New:** `data/sync/SyncStatus.kt`, `data/db/converter/SyncStatusConverter.kt`
```kotlin
enum class SyncStatus { LOCAL_ONLY, PENDING, SYNCED, CONFLICT }
```

### 2-2. Room v1→v2 マイグレーション
**Modify:** `CultivationEntity`, `GrowthPhotoEntity`, `SodatterDatabase`
- 追加カラム: `sync_status TEXT DEFAULT 'LOCAL_ONLY'`, `last_modified_at INTEGER DEFAULT 0`, `cloud_id TEXT`
- DBバージョン 1→2、ALTER TABLE マイグレーション

### 2-3. SyncRepository
**New:** `data/sync/SyncRepository.kt`
- `uploadPendingCultivations(userId)` / `uploadPendingPhotos(userId)`
- `downloadFromCloud(userId)`
- 競合解決: ローカル優先（PENDING状態のレコードはクラウドで上書きしない）

**Modify:** `CultivationRepository`, `GrowthPhotoRepository`
- insert/update/delete時に `syncStatus = PENDING`, `lastModifiedAt = now` をセット

### 2-4. SyncWorker（WorkManager）
**New:** `data/sync/SyncWorker.kt`, `di/WorkerModule.kt`
- `@HiltWorker` CoroutineWorker
- 認証済み + プレミアムの場合のみ同期実行
- トリガー: 書き込み時（デバウンス5秒）、アプリ起動時、定期1時間

### 2-5. DAO拡張
**Modify:** `CultivationDao`, `GrowthPhotoDao`
- `getPendingSyncCultivations()`, `getByCloudId()` 追加

---

## Phase 3: Photo Cloud Sync（2コミット）

### 3-1. Firebase Storage アップロード
**New:** `data/sync/PhotoSyncManager.kt`
- `uploadPhoto(userId, localUri): String` → Storage URL返却
- パス: `gs://bucket/users/{userId}/photos/{filename}`
- アップロード前にリサイズ（最大1920px、JPEG品質80%）

### 3-2. ダウンロード・キャッシュ
- クラウドURLからローカルにダウンロード → `filesDir/photos/` に保存
- Coilは `file://` と `https://` 両方対応済みなので表示側の変更不要

---

## Phase 4: RevenueCat Subscription（4コミット）

### 4-1. RevenueCat依存関係追加
**Files:** `libs.versions.toml`, `app/build.gradle.kts`

### 4-2. SubscriptionRepository
**New:** `data/billing/SubscriptionRepository.kt`, `di/BillingModule.kt`
- `isPremium: StateFlow<Boolean>`, `offerings`, `purchase()`, `restorePurchases()`
- RevenueCat初期化時にFirebase Auth UIDでログイン

### 4-3. サブスクリプション画面UI
**New:** `ui/subscription/PaywallScreen.kt`, `PaywallViewModel.kt`
- プラン詳細、価格、購入ボタン、復元ボタン

### 4-4. 設定画面に同期・課金ステータス表示
**Modify:** `SettingsScreen.kt`, `SettingsViewModel.kt`
- 「クラウド同期」セクション: 同期状態、手動同期ボタン
- 「プレミアム」セクション: 現在のプラン、ペイウォールへのリンク
- ログイン済みの場合のみ表示

---

## Firestore コレクション構造
```
users/{userId}/
  cultivations/{cloudId}     - 栽培記録
  growthPhotos/{cloudId}     - 生育写真（cultivationCloudIdで紐付け）
  devices/{cloudId}          - 容器設定（バックアップ用）
  settings/{key}             - アプリ設定
```

## 追加予定の依存関係

```toml
# libs.versions.toml に追加
[versions]
firebaseBom = "33.7.0"
playServicesAuth = "21.3.0"
revenueCat = "8.10.7"
workManager = "2.10.0"

[libraries]
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth-ktx" }
firebase-firestore = { group = "com.google.firebase", name = "firebase-firestore-ktx" }
firebase-storage = { group = "com.google.firebase", name = "firebase-storage-ktx" }
play-services-auth = { group = "com.google.android.gms", name = "play-services-auth", version.ref = "playServicesAuth" }
revenuecat-purchases = { group = "com.revenuecat.purchases", name = "purchases", version.ref = "revenueCat" }
androidx-work-runtime = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workManager" }
hilt-work = { group = "androidx.hilt", name = "hilt-work", version = "1.2.0" }

[plugins]
google-services = { id = "com.google.gms.google-services", version = "4.4.2" }
```

## 新規パッケージ構造
```
com.mutsumix.sodatterbt/
  data/
    auth/
      AuthRepository.kt
    billing/
      SubscriptionRepository.kt
    sync/
      SyncStatus.kt
      SyncRepository.kt
      SyncWorker.kt
      PhotoSyncManager.kt
    db/
      converter/
        SyncStatusConverter.kt
  di/
    FirebaseModule.kt
    BillingModule.kt
    WorkerModule.kt
  ui/
    auth/
      LoginScreen.kt
      LoginViewModel.kt
    subscription/
      PaywallScreen.kt
      PaywallViewModel.kt
```

## 注意事項
- Firestoreのオフラインキャッシュは**無効化**（Roomが唯一のオフラインストア）
- RevenueCatは月額$2,500 MTRまで無料。超過分は1%
- Google Sign-InはminSdk 31なのでCredential Manager APIを使用
- 初回プレミアム化時に既存ローカルデータを一括アップロード（LOCAL_ONLY→PENDING）
- 写真はアップロード前にリサイズ（最大1920px、JPEG品質80%、約1MB以下）

## 前提条件（実装前に必要）
1. Firebaseプロジェクト作成 + `google-services.json` 取得
2. Google Play Consoleでサブスクリプション商品を作成
3. RevenueCatアカウント作成 + APIキー取得
4. Firebase AuthでGoogleログインプロバイダを有効化

## 検証方法
- Phase 1: ログイン/ログアウトが動作、設定画面にユーザー情報表示
- Phase 2: ローカルデータ変更 → Firestore同期 → 別デバイスで確認
- Phase 3: 写真がFirebase Storageにアップロード、別デバイスで表示
- Phase 4: サブスクリプション購入 → クラウド同期が有効化

## 参考リンク
- [RevenueCat Android SDK](https://www.revenuecat.com/docs/getting-started/installation/android)
- [RevenueCat SDK Quickstart](https://www.revenuecat.com/docs/getting-started/quickstart)
- [RevenueCat Jetpack Compose Paywalls](https://www.revenuecat.com/blog/engineering/build-paywalls-compose/)
- [RevenueCat Pricing](https://www.revenuecat.com/pricing/) - $2,500 MTRまで無料
