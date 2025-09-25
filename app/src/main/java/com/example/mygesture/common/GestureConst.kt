package com.example.mygesture.common

object GestureConst {
  /**
   * 手势动作常量
   */
  const val ACTION_BACK = "返回"
  const val ACTION_RECENT_TASKS = "最近任务"
  const val ACTION_NOTIFICATION_PANEL = "展开通知栏"
  const val ACTION_SCREENSHOT = "截屏"
  const val ACTION_BRIGHTNESS_CONTROL = "亮度调节"
  const val ACTION_COPY = "复制"
  const val ACTION_PASTE = "粘贴"
  const val ACTION_CUT = "剪切"
  const val ACTION_LOCK_SCREEN = "锁屏"
  const val ACTION_LAUNCH_APP = "启动应用"

  /**
   * 手势动作常量数组
   */
  @JvmField
  val GESTURE_ACTIONS = arrayOf(
    ACTION_BACK,
    ACTION_RECENT_TASKS,
    ACTION_NOTIFICATION_PANEL,
    ACTION_SCREENSHOT,
    ACTION_BRIGHTNESS_CONTROL,
    ACTION_COPY,
    ACTION_PASTE,
    ACTION_CUT,
    ACTION_LOCK_SCREEN,
    ACTION_LAUNCH_APP
  )

  const val GESTURE_TYPE_CHOOSE = "选择"
  const val GESTURE_TYPE_SWIPE_RIGHT = "右滑"
  const val GESTURE_TYPE_SWIPE_LEFT = "左滑"
  const val GESTURE_TYPE_SWIPE_UP = "上滑"
  const val GESTURE_TYPE_SWIPE_DOWN = "下滑"
  const val GESTURE_TYPE_LONG_PRESS_SWIPE_LEFT = "长按并左滑"
  const val GESTURE_TYPE_LONG_PRESS_SWIPE_RIGHT = "长按并右滑"
  const val GESTURE_TYPE_LONG_PRESS_SWIPE_UP = "长按并上滑"
  const val GESTURE_TYPE_LONG_PRESS_SWIPE_DOWN = "长按并下滑"

  // 悬浮球手势类型常量
  const val FLOATING_BALL_GESTURE_CLICK = "悬浮球点击"
  const val FLOATING_BALL_GESTURE_DOUBLE_CLICK = "悬浮球双击"
  const val FLOATING_BALL_GESTURE_LONG_PRESS = "悬浮球长按"

  const val EDGE_TYPE_LEFT = "左边缘"
  const val EDGE_TYPE_RIGHT = "右边缘"
  const val EDGE_TYPE_FLOATING_BALL = "悬浮球"
  const val TRIGGER_SETTING_HEIGHT = "触发区高度"
  const val TRIGGER_SETTING_WIDTH = "触发区宽度"
  const val TRIGGER_SETTING_POSITION = "触发区位置"

  const val KEY_EDGE_TYPE = "edge_type"
  const val KEY_GESTURE_TYPE = "gesture_type"
  
  // 新增用于数据传递的常量
  const val KEY_SELECTED_ACTION = "selected_action"
  const val KEY_SELECTED_ACTION_TYPE = "selected_action_type"
  const val REQUEST_CODE_CHOOSE_GESTURE = 1001
  const val REQUEST_CODE_SELECT_APP = 1002

  // 操作类型常量
  const val OPERATOR_TYPE_OPERATION = "operation"
  const val OPERATOR_TYPE_APP = "jump"
  const val OPERATOR_TYPE_SHORTCUT = "shortcut"

  // 应用跳转相关常量
  const val KEY_APP_NAME = "app_name"
  const val KEY_APP_PACKAGE = "app_package"
  const val KEY_IS_SYSTEM_APP = "is_system_app"
}
