package com.dyzs.svgparser.lib

import android.graphics.Point
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.jvm.internal.Intrinsics.Kotlin
import kotlin.math.abs
import kotlin.math.sqrt


/**
 * author : cbk
 * date   : 2023/3/20
 * desc   :
 * 参考：
 * https://developer.android.google.cn/training/gestures/scale?hl=zh-cn#java
 * https://blog.csdn.net/WPR13005655989/article/details/127093551
 * <p>
 * ACTION_POINTER_DOWN：双指
 */

class SvgTouchController2 : GestureDetector.OnGestureListener {

    private val TAG = "SvgTouchController2"
    private val MIN_THRESHOLD = 3.0
    private val INIT_FINGERS_DISTANCE = 0.0

    // Click 和 long Click 使用参数
    private var mDownX = 0f  // Click 和 long Click 使用参数
    private var mDownY = 0f

    // Drag 使用参数
    private var mLastTouchX = 0f  // Drag 使用参数
    private var mLastTouchY = 0f  // Drag 使用参数
    private var mOffsetX = 0f  // Drag 使用参数
    private var mOffsetY = 0f
    private val mTranslateDownPoint = Point()
    private val mTranslateMovePoint = Point()

    // The ‘active pointer’ is the one currently moving our object.
    // 单指移动
    private var mActivePointerId = MotionEvent.INVALID_POINTER_ID

    private var mGestureDetector: GestureDetector? = null

    private var mViewDelegate: SvgParserView? = null

    private var mTouchSlop = 20

    private var mDownTimeMillis: Long = 0

    private var mConsumeListener: ConsumeListener? =
        null

    private val mZoomCenterPoint = Point()

    private val mFingerPoints: Point? = null

    private var mLastDistance = INIT_FINGERS_DISTANCE

    constructor(view: SvgParserView) {
        this.mViewDelegate = view
        this.mTouchSlop = ViewConfiguration.get(view.context).scaledTouchSlop
        this.mGestureDetector = GestureDetector(view.context, this)
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        mGestureDetector!!.onTouchEvent(event)

        val action = event.action
        LogUtils.v(TAG, "onTouchEvent action:[$action]")

        // onTouchEventActionMask(event)

        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                // Remember click
                mDownX = event.x
                mDownY = event.y
                mDownTimeMillis = System.currentTimeMillis()

                // Remember where we started (for dragging)
                val actionDownPointerIndex = event.actionIndex
                val eventX = event.getX(actionDownPointerIndex)
                val eventY = event.getY(actionDownPointerIndex)
                mTranslateDownPoint.x = eventX.toInt()
                mTranslateDownPoint.y = eventY.toInt()
                printLog("ACTION_DOWN mTranslateDownPoint:x[" + mTranslateDownPoint.x + "], y[" + mTranslateDownPoint.y + "]")
                mLastTouchX = eventX
                mLastTouchY = eventY
                // Save the ID of this pointer (for dragging)
                mActivePointerId = event.getPointerId(actionDownPointerIndex)
                printLog("ACTION_DOWN mActivePointerId:[$mActivePointerId]")
            }

            MotionEvent.ACTION_MOVE -> {
                // Find the index of the active pointer and fetch its position
                // LogUtils.v(TAG, "ACTION_MOVE mActivePointerId:[" + mActivePointerId + "]");
                val actionMovePointerIndex = event.findPointerIndex(mActivePointerId)
                val mx = event.getX(actionMovePointerIndex)
                val my = event.getY(actionMovePointerIndex)
                mTranslateMovePoint.x = mx.toInt() //(int) event.getX(0);
                mTranslateMovePoint.y = my.toInt() //(int) event.getY(0);

                // Calculate the distance moved
                val dmx = mx - mLastTouchX
                val dmy = my - mLastTouchY
                mOffsetX += dmx
                mOffsetY += dmy

                // Remember this touch position for the next move event
                mLastTouchX = mx
                mLastTouchY = my
                if (event.pointerCount >= 2) {
                    val point: FloatArray = calcZoomCenterPoint(event)
                    mTranslateMovePoint.x = point[0].toInt()
                    mTranslateMovePoint.y = point[1].toInt()
                    val currDistance: Double = calcFingersDistance(event)
                    val distanceDiff = currDistance - mLastDistance
                    if (abs(distanceDiff) > MIN_THRESHOLD) {
                        if (distanceDiff < 0) {
                            mConsumeListener?.consumeZoomIn(
                                mZoomCenterPoint.x.toDouble(),
                                mZoomCenterPoint.y.toDouble()
                            )
                        } else {
                            mConsumeListener?.consumeZoomOut(
                                mZoomCenterPoint.x.toDouble(),
                                mZoomCenterPoint.y.toDouble()
                            )
                        }
                        mLastDistance = currDistance
                    }
                }
                val tDmx = (mTranslateMovePoint.x - mTranslateDownPoint.x).toFloat()
                val tDmy = (mTranslateMovePoint.y - mTranslateDownPoint.y).toFloat()
                // TODO: 2023/4/3 translate
                printLog("ACTION_MOVE mx:[$mx], my[$my]")
                printLog("ACTION_MOVE mTranslateDownPointX:[" + mTranslateDownPoint.x + "], mTranslateDownPointY[" + mTranslateDownPoint.y + "]")
                printLog("ACTION_MOVE mTranslateMovePointX:[" + mTranslateMovePoint.x + "], mTranslateMovePointY[" + mTranslateMovePoint.y + "]")
                printLog("ACTION_MOVE tDmx:[$tDmx], tDmy[$tDmy]")
                if (abs(tDmx) > MIN_THRESHOLD || abs(tDmy) > MIN_THRESHOLD) {
                    mConsumeListener?.consumeDrag(tDmx, tDmy)
                    // LogUtils.v(TAG, "ACTION_MOVE onDrag:[" + dmx + "]");
                }
                mTranslateDownPoint.x = mTranslateMovePoint.x
                mTranslateDownPoint.y = mTranslateMovePoint.y
            }

            MotionEvent.ACTION_UP -> {
                mActivePointerId = MotionEvent.INVALID_POINTER_ID
                printLog("ACTION_UP mActivePointerId:[$mActivePointerId]")
                val ux = event.x
                val uy = event.y
                val offsetX = abs(ux - mDownX)
                val offsetY = Math.abs(uy - mDownY)
                val validOffset = offsetX < mTouchSlop && offsetY < mTouchSlop
                val upTime = System.currentTimeMillis()
                val validTime = upTime - mDownTimeMillis < 100
                printLog("ACTION_UP offset:[$offsetX]$validOffset//$validTime")
                if (validTime && validOffset) {
                    mConsumeListener?.consumeClick(ux, uy)
                    printLog("ACTION_UP consume click:[$offsetX]")
                }
            }

            MotionEvent.ACTION_CANCEL -> mActivePointerId = MotionEvent.INVALID_POINTER_ID
            MotionEvent.ACTION_POINTER_DOWN -> {
                printLog("ACTION_POINTER_DOWN mActivePointerId:[" + event.getPointerId(event.actionIndex) + "]")
                val point: FloatArray = calcZoomCenterPoint(event)
                mTranslateDownPoint.x = point[0].toInt()
                mTranslateDownPoint.y = point[1].toInt()
                mLastDistance = calcFingersDistance(event)
            }

            MotionEvent.ACTION_POINTER_UP -> {
                val actionPointerUpPointerIndex = event.actionIndex
                val actionPointerUpPointerId = event.getPointerId(actionPointerUpPointerIndex)
                printLog("ACTION_POINTER_UP mActivePointerId:[$actionPointerUpPointerId]")
                mLastDistance = INIT_FINGERS_DISTANCE
                if (actionPointerUpPointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    val newPointerIndex = if (actionPointerUpPointerIndex == 0) 1 else 0
                    mLastTouchX = event.getX(newPointerIndex)
                    mLastTouchY = event.getY(newPointerIndex)
                    mTranslateDownPoint.x = mLastTouchX.toInt()
                    mTranslateDownPoint.y = mLastTouchY.toInt()
                    printLog("ACTION_POINTER_UP mTranslateDownPointX:[" + mTranslateDownPoint.x + "], mTranslateDownPointY[" + mTranslateDownPoint.y + "]")
                    mActivePointerId = event.getPointerId(newPointerIndex)
                } else {
                    //松开的不是第一个按下的手指，直接把基准点重置成第一个手指的坐标
                    mTranslateDownPoint.x =
                        event.getX(event.findPointerIndex(mActivePointerId)).toInt()
                    mTranslateDownPoint.y =
                        event.getY(event.findPointerIndex(mActivePointerId)).toInt()
                }
            }

            MotionEvent.ACTION_POINTER_INDEX_MASK -> printLog("ACTION_POINTER_INDEX_MASK")
            MotionEvent.ACTION_POINTER_INDEX_SHIFT -> printLog("ACTION_POINTER_INDEX_SHIFT")
        }
        return true
    }

    private fun calcZoomCenterPoint(event: MotionEvent): FloatArray {
        val pointerX0 = event.getX(0)
        val pointerY0 = event.getY(0)
        val pointerX1 = event.getX(1)
        val pointerY1 = event.getY(1)
        val centerX = pointerX0 + (pointerX1 - pointerX0) / 2
        val centerY = pointerY0 + (pointerY1 - pointerY0) / 2
        mZoomCenterPoint.x = centerX.toInt()
        mZoomCenterPoint.y = centerY.toInt()
        mTranslateMovePoint.x = centerX.toInt()
        mTranslateMovePoint.y = centerY.toInt()
        printLog("calcZoomCenterPoint:x[$centerX], y[$centerY]")
        printLog("calcZoomCenterPoint mTranslateMovePoint reset:x[$centerX], y[$centerY]")
        return floatArrayOf(centerX, centerY)
    }

    /**
     * 双指距离
     *
     * @param event
     * @return
     */
    private fun calcFingersDistance(event: MotionEvent): Double {
        if (event.pointerCount >= 2) {
            val x = event.getX(0) - event.getX(1)
            val y = event.getY(0) - event.getY(1)
            return sqrt((x * x + y * y).toDouble())
        }
        return 0.0
    }

    fun getConsumeListener(): ConsumeListener? {
        return mConsumeListener
    }

    fun setConsumeListener(mConsumeListener: ConsumeListener?) {
        this.mConsumeListener = mConsumeListener
    }

    interface ConsumeListener {
        fun consumeClick(upX: Float, upY: Float)

        //        void consumeLongClick();
        //
        //        void consumeDrag();
        //
        fun consumeZoomIn(centerX: Double, centerY: Double)

        //
        fun consumeZoomOut(centerX: Double, centerY: Double)
        fun consumeDrag(tx: Float, ty: Float)
    }


    override fun onDown(e: MotionEvent): Boolean {
        return false
    }

    override fun onShowPress(e: MotionEvent) {

    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        return false
    }

    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
        // Scrolling uses math based on the viewport (as opposed to math using pixels).

        // Pixel offset is the offset in screen pixels, while viewport offset is the
        // offset within the current viewport.
//        float viewportOffsetX = distanceX * mCurrentViewport.width()
//                / mContentRect.width();
//        float viewportOffsetY = -distanceY * mCurrentViewport.height()
//                / mContentRect.height();
//        // Updates the viewport, refreshes the display.
//        setViewportBottomLeft(
//                mCurrentViewport.left + viewportOffsetX,
//                mCurrentViewport.bottom + viewportOffsetY);
        return true
    }

    override fun onLongPress(p0: MotionEvent) {

    }

    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
        return false
    }

    /**
     * Sets the current viewport (defined by mCurrentViewport) to the given
     * X and Y positions. Note that the Y value represents the topmost pixel position,
     * and thus the bottom of the mCurrentViewport rectangle.
     */
    private fun setViewportBottomLeft(x: Float, y: Float) {
        /*
         * Constrains within the scroll range. The scroll range is simply the viewport
         * extremes (AXIS_X_MAX, etc.) minus the viewport size. For example, if the
         * extremes were 0 and 10, and the viewport size was 2, the scroll range would
         * be 0 to 8.
         */

//        float curWidth = mCurrentViewport.width();
//        float curHeight = mCurrentViewport.height();
//        x = Math.max(AXIS_X_MIN, Math.min(x, AXIS_X_MAX - curWidth));
//        y = Math.max(AXIS_Y_MIN + curHeight, Math.min(y, AXIS_Y_MAX));
//
//        mCurrentViewport.set(x, y - curHeight, x + curWidth, y);
//
//        // Invalidates the View to update the display.
//        ViewCompat.postInvalidateOnAnimation(this);
    }

    private fun printLog(text: String) {
        LogUtils.v(TAG, text)
    }
}