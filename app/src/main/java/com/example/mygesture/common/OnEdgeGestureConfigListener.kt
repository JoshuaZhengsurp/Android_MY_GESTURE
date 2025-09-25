package com.example.mygesture.common

import android.widget.TextView

interface OnEdgeGestureConfigListener {
  fun onEdgeToggled(isEnabled: Boolean)
  fun onGestureActionClicked(edgeType: String, gestureType: String?, actionView: TextView?)
  fun onTriggerSettingChanged(edgeType: String, settingType: String?, value: Int)
}