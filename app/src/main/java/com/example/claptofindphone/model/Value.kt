package com.example.claptofindphone.model

object Value {

    val googleAds = GoogleAds
    val unityAds = UnityAds

    object UnityAds {
        var unityGameId: String = ""
        var rewardedAd: String = ""
        var interstitialAd: String = ""
    }

    object GoogleAds {
        var interstitialAd: String = ""
        var nativeAd: String = " "
        var rewardedAd: String = " "
        var appId: String = ""
    }

    object InfoBackPress {
        var isBackPress: Int = 0
    }

    object CallerTalker {
        var isBackPress: Int = 0
    }

    object ClapToFindPhone {
        var isBackPress: Int = 0
    }

    object DontTouchPhone {
        var isBackPress: Int = 0
    }


    object ChargerPopupActivity {
        var isBackPress: Int = 0
    }

    object BatteryAlertMode {
        var isBackPress: Int = 0
    }

    object ChildMode {
        var isBackPress: Int = 0
    }

    object SmsTalker {
        var isBackPress: Int = 0
    }

    object PocketMode {
        var isBackPress: Int = 0
    }

    object SettingsBackPress {
        var isBackPress: Int = 0
    }

    object AudioMode {
        var isBackPress: Int = 0
    }

    object PinMode {
        var isBackPress: Int = 0
    }

    object ToneBackPress {
        var isBackPress: Int = 0
    }

    object PocketInfo {
        var isBackPress: Int = 0
    }
}

