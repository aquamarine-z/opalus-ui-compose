package io.github.opalusui.opalus.ui.liquid

import androidx.compose.runtime.Immutable

/**
 * 水滴触碰容器边界的方位枚举
 */
enum class LiquidBoundaryEdge {
    /**
     * 容器起点边角 (在 LTR 从左到右布局下为左边缘，在 RTL 从右到左布局下为右边缘)
     */
    Start,

    /**
     * 容器终点边角 (在 LTR 从左到右布局下为右边缘，在 RTL 从右到左布局下为左边缘)
     */
    End
}

/**
 * iOS HIG 规范：流体导航栏交互与动力学生命周期事件监听器 (LiquidTabBarEvents)
 *
 * 涵盖水滴从按下激活、跨槽滑行、边角触碰、超界拉满到最终平稳停靠的全部关键物理事件。
 * 所有回调默认为 null，按需配置即可。
 */
@Immutable
data class LiquidTabBarEvents(
    /**
     * 手指或光标按下瞬间，水滴从静态膨胀并进入激活态时触发
     * @param index 按下时所在的初始 Tab 槽位索引
     */
    val onDropletActivated: ((index: Int) -> Unit)? = null,

    /**
     * 手指或光标抬起松开时触发，水滴退出拖拽状态并开始收拢
     * @param finalIndex 最终决策选中的目标 Tab 槽位索引
     */
    val onDropletDeactivated: ((finalIndex: Int) -> Unit)? = null,

    /**
     * 水滴几何中心在拖拽穿行过程中，跨过相邻两个槽位的中线分界时触发
     * 适合在此处触发系统的轻微触感振动 (Tick / Haptic) 或音效
     * @param fromIndex 离开的槽位索引
     * @param toIndex 进入的槽位索引
     */
    val onTabCrossed: ((fromIndex: Int, toIndex: Int) -> Unit)? = null,

    /**
     * 拖拽中途水滴当前悬停对齐的潜在落点槽位发生改变时触发
     * @param slotIndex 当前光标若松开将会落入的槽位索引
     */
    val onSlotHovered: ((slotIndex: Int) -> Unit)? = null,

    /**
     * 水滴拖拽拉出最左侧或最右侧槽位边缘，刚进入超界弹性区时触发一次
     * @param edge 触碰的是起点边角 (Start) 还是终点边角 (End)
     */
    val onBoundaryReached: ((edge: LiquidBoundaryEdge) -> Unit)? = null,

    /**
     * 水滴向外拉伸达到物理允许的最大极限位移 (maxOverhang 达到 95%+) 时触发
     * 模拟流体拉满时的强阻尼边界碰撞
     * @param edge 达到极限的外侧边缘
     */
    val onMaxOverhangReached: ((edge: LiquidBoundaryEdge) -> Unit)? = null,

    /**
     * 水滴在容器外部被拉伸时的连续形变进度流 (0.0f 表示刚出界，1.0f 表示拉满极限)
     * 可供业务层联动外层容器做弹性拉伸或动效
     * @param edge 当前拉出的一侧边缘
     * @param pullRatio 归一化的拉伸阻尼深度 (0.0f ~ 1.0f)
     */
    val onBoundaryPull: ((edge: LiquidBoundaryEdge, pullRatio: Float) -> Unit)? = null,

    /**
     * 松手后水滴确定目标并启动平滑飞行吸附弹簧动画时触发
     * @param targetIndex 目标槽位索引
     */
    val onDockingStarted: ((targetIndex: Int) -> Unit)? = null,

    /**
     * 水滴完成定值冲量偏转、欠阻尼果冻回弹并完全恢复静止（物理速度衰减归零）时触发
     * 标志着一次完整交互生命周期的结束
     * @param settledIndex 最终归正停靠的槽位索引
     */
    val onDockingSettled: ((settledIndex: Int) -> Unit)? = null
)
