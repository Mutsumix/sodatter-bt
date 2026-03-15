package com.mutsumix.sodatterbt.navigation

import kotlinx.serialization.Serializable

// BottomNavタブ（3画面）
@Serializable object Home
@Serializable object History
@Serializable object Settings

// スタックに積む画面（6画面）
@Serializable data class Seeding(val deviceId: Int)
@Serializable data class Detail(val deviceId: Int)

// cultivationId=-1: カメラで手動スキャン
// cultivationId>=0: ディープリンク経由でIDが確定済み (sodatterbt://cultivation/{cultivationId})
@Serializable data class QrScan(val cultivationId: Long = -1L)

@Serializable data class PhotoRecord(val deviceId: Int)
@Serializable data class Harvest(val deviceId: Int)
@Serializable data class LabelPrint(val deviceId: Int)
