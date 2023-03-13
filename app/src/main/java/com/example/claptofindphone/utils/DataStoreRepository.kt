package com.example.claptofindphone.utils


import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.claptofindphone.model.OfflinePermission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("pref")

class DataStoreRepository(private val context: Context) {

    private companion object {

        private val isFlashValue = booleanPreferencesKey("isFlashValue")
        private val isVibrationValue = booleanPreferencesKey("isVibrationValue")
        private val isSmartModeValue = booleanPreferencesKey("isSmartMode")
        private val isTouchModeValue = booleanPreferencesKey("isTouchModeValue")
        private val isPocketModeValue = booleanPreferencesKey("isPocketModeValue")
        private val isPinValue = stringPreferencesKey("isPinValue")
        private val isQuestionValue = stringPreferencesKey("isQuestion")
        private val isAnsValue = stringPreferencesKey("isAns")
        private val isPinSetValue = booleanPreferencesKey("isPinSetValue")
        private val isCall = booleanPreferencesKey("isCall")
        private val isClapToFindPhone = booleanPreferencesKey("isClapToFindPhone")
        private val isWhistleToFindPhone = booleanPreferencesKey("isWhistleToFindPhone")
        private val isDontTouchPhone = booleanPreferencesKey("isDontTouchPhone")
        private val isPocketMode = booleanPreferencesKey("isPocketMode")
        private val isChargerMode = booleanPreferencesKey("isChargerMode")
        private val isBatteryAlert = booleanPreferencesKey("isBatteryAlert")
        private val isChildMode = booleanPreferencesKey("isChildMode")
        private val isCallerTalker = booleanPreferencesKey("isCallerTalker")
        private val isSmsTalker = booleanPreferencesKey("isSmsTalker")
        private val isAudio = booleanPreferencesKey("isAudio")
        private val isPin = booleanPreferencesKey("isPin")


    }

    suspend fun storeFeaturesAllowedValue(offlinePermission: OfflinePermission) {
        context.dataStore.edit {
            offlinePermission.apply {
                it[isClapToFindPhone] = clapToFindPhone
                it[isWhistleToFindPhone] = WhistleToFindPhone
                it[isDontTouchPhone] = DontTouchPhone
                it[isPocketMode] = PocketMode
                it[isChargerMode] = ChargerMode
                it[isBatteryAlert] = BatteryAlert
                it[isChildMode] = ChildMode
                it[isCallerTalker] = CallerTalker
                it[isSmsTalker] = SmsTalker
                it[isAudio] = audio
                it[isPin] = pin
            }

        }
    }

    suspend fun getFeaturesAllowedValue(): Flow<OfflinePermission> =
        context.dataStore.data.map {
            OfflinePermission(
                it[isClapToFindPhone] ?: false,
                it[isWhistleToFindPhone] ?: false,
                it[isDontTouchPhone] ?: false,
                it[isPocketMode] ?: false,
                it[isChargerMode] ?: false,
                it[isBatteryAlert] ?: false,
                it[isChildMode] ?: false,
                it[isCallerTalker] ?: false,
                it[isSmsTalker] ?: false,
                it[isAudio] ?: false,
                it[isPin] ?: false
            )

        }


    suspend fun storeFlashValue(isFlash: Boolean) {
        context.dataStore.edit {
            it[isFlashValue] = isFlash
        }
    }

    val getFlashValue: Flow<Boolean> =
        context.dataStore.data.map {
            it[isFlashValue] ?: false
        }

    suspend fun storeVibrateValue(isVibration: Boolean) {
        context.dataStore.edit {
            it[isVibrationValue] = isVibration
        }
    }

    val getVibrateValue: Flow<Boolean> =
        context.dataStore.data.map {
            it[isVibrationValue] ?: false
        }

    suspend fun storeSmartModeValue(isSmartMode: Boolean) {
        context.dataStore.edit {
            it[isSmartModeValue] = isSmartMode
        }
    }

    val getSmartModeValue: Flow<Boolean> =
        context.dataStore.data.map {
            it[isSmartModeValue] ?: false
        }

    suspend fun storeTouchModeFlashValue(isTouchFlashMode: Boolean) {
        context.dataStore.edit {
            it[isTouchModeValue] = isTouchFlashMode
        }
    }

    val getTouchModeFlashValue: Flow<Boolean> =
        context.dataStore.data.map {
            it[isTouchModeValue] ?: false
        }

    suspend fun storePocketModeValue(isPocketValue: Boolean) {
        context.dataStore.edit {
            it[isPocketModeValue] = isPocketValue
        }
    }

    val getPocketModeValue: Flow<Boolean> =
        context.dataStore.data.map {
            it[isPocketModeValue] ?: false
        }

    suspend fun storePinValue(isPin: String) {
        context.dataStore.edit {
            it[isPinValue] = isPin
        }
    }

    val getPinValue: Flow<String> =
        context.dataStore.data.map {
            it[isPinValue] ?: ""
        }

    suspend fun storeQuestionValue(isQuestion: String) {
        context.dataStore.edit {
            it[isQuestionValue] = isQuestion
        }
    }

    val getQuestionValue: Flow<String> =
        context.dataStore.data.map {
            it[isQuestionValue] ?: ""
        }

    suspend fun storeAnsValue(isAns: String) {
        context.dataStore.edit {
            it[isAnsValue] = isAns
        }
    }

    val getAnsValue: Flow<String> =
        context.dataStore.data.map {
            it[isAnsValue] ?: ""
        }

    suspend fun storePinSetValue(isPin: Boolean) {
        context.dataStore.edit {
            it[isPinSetValue] = isPin
        }
    }

    val getPinSetValue: Flow<Boolean> =
        context.dataStore.data.map {
            it[isPinSetValue] ?: false
        }

    suspend fun storeCallValue(isCallValue: Boolean) {
        context.dataStore.edit {
            it[isCall] = isCallValue
        }
    }

    val getCallValue: Flow<Boolean> =
        context.dataStore.data.map {
            it[isCall] ?: false
        }
}