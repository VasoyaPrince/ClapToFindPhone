package com.example.claptofindphone.model


data class OfflinePermission(
    var clapToFindPhone: Boolean,
    var WhistleToFindPhone: Boolean,
    var DontTouchPhone: Boolean,
    var PocketMode: Boolean,
    var ChargerMode: Boolean,
    var BatteryAlert: Boolean,
    var ChildMode: Boolean,
    var CallerTalker: Boolean,
    var SmsTalker: Boolean,
    var audio: Boolean,
    var pin: Boolean
)