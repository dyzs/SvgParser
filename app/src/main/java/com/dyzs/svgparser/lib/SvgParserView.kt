package com.dyzs.svgparser.lib

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import android.graphics.Region
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.dyzs.svgparser.R
import kotlin.math.abs

/**
 * author : cbk
 * date   : 2023/3/7
 * desc   :
 */
/**
 * author : cbk
 * date   : 2023/3/7
 * desc   :
 */
class SvgParserView : View, SvgTouchController2.ConsumeListener {
    private var viewWidth = 0
    private var viewHeight = 0
    private var svgWidth = 0
    private var svgHeight = 0 // svg vector 宽高
    private var svgWidthScale = 1f // svg vector 与 view 的宽度比
    private var svgHeightScale = 1f // svg vector 与 view 的高度比
    private var svgMatrix = Matrix()
    private var svgRealWidth = 0
    private var svgRealHeight = 0 // 真实的 svg 图片显示宽高
    private var svgRealWidthScale = 1f
    private var svgRealHeightScale = 1f
    private var svgRealMatrix = Matrix()
    private var svgPaths: MutableList<Path?> = ArrayList()
    private var paint = Paint()
    private var vectorDrawable: VectorDrawable? = null
    private var regionList: List<Region> = ArrayList()
    private var svgElementVector: SvgElementVector? = null
    private var touchController: SvgTouchController2? = null
    private var svgInRect: Rect? = null
    private var svgTouchPath: Path? = null
    private var svgTouchPathPaint = Paint()
    private var svgZoomScale = 1f
    private val svgZoomScaleCenterPoint = Point()
    private val svgActionMatrix = Matrix()
    private val svgTranslatePoint = Point()

    constructor(context: Context?) : super(context) {
        initCreateSvg()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initCreateSvg()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initCreateSvg()
    }

    private fun initCreateSvg() {
        vectorDrawable = ContextCompat.getDrawable(
            context,
            R.drawable.vector_drawable_the_peoples_republic_of_china
        ) as VectorDrawable?
        paint = Paint()
        paint.strokeWidth = 5f
        paint.isAntiAlias = true
        paint.color = Color.MAGENTA
        paint.style = Paint.Style.STROKE
        svgTouchPathPaint = Paint()
        paint.strokeWidth = 5f
        paint.isAntiAlias = true
        paint.color = Color.BLUE
        paint.style = Paint.Style.STROKE

        // 初始化svg相关
        svgElementVector = SvgXmlParser.parserXml2Bean(context)
        svgWidth = svgElementVector!!.getViewportWidth()!!.toInt()
        svgHeight = svgElementVector!!.getViewportHeight()!!.toInt()
        svgPaths.addAll(SvgUtils.enumPath(svgElementVector))
        regionList = SvgUtils.enumPathRegion(svgPaths)
        svgInRect = SvgUtils.exhaustionSvgInRectByPaths(svgPaths)
        svgRealWidth = abs(svgInRect!!.right - svgInRect!!.left)
        svgRealHeight = abs(svgInRect!!.bottom - svgInRect!!.top)
        touchController = SvgTouchController2(this)
        touchController!!.setConsumeListener(this)
        testttttttt()
    }

    private fun testttttttt() {}
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        LogUtils.i(TAG, "onMeasure...")
        viewWidth = measuredWidth
        viewHeight = measuredHeight
        svgWidthScale = viewWidth * 1f / svgWidth
        svgHeightScale = viewHeight * 1f / svgHeight
        svgMatrix.setScale(svgWidthScale, svgHeightScale)
        svgRealWidthScale = viewWidth * 1f / svgRealWidth
        svgRealHeightScale = viewHeight * 1f / svgRealHeight
        svgRealMatrix.reset()
        // 使用post 解决 translate and scale 无法同时生效问题
        svgRealMatrix.postTranslate(-svgInRect!!.left.toFloat(), -svgInRect!!.top.toFloat())
        svgRealMatrix.postScale(svgRealWidthScale, svgRealHeightScale)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        LogUtils.i(TAG, "onFinishInflate...")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (svgPaths.size > 0) {
//            canvas.save();
//            canvas.setMatrix(svgRealMatrix);
            for (path in svgPaths) {
                if (svgZoomScale != 1f || svgTranslatePoint.x != 0) {
                    svgActionMatrix.reset()
                    svgActionMatrix.postScale(
                        svgZoomScale,
                        svgZoomScale,
                        svgZoomScaleCenterPoint.x.toFloat(),
                        svgZoomScaleCenterPoint.y.toFloat()
                    )
                    svgActionMatrix.postTranslate(
                        svgTranslatePoint.x.toFloat(),
                        svgTranslatePoint.y.toFloat()
                    )
                    // path.offset(svgTranslatePoint.x, svgTranslatePoint.y);
                    path!!.transform(svgActionMatrix)
                }
                //                if (svgTranslatePoint.x != 0) {
//                    svgActionMatrix.reset();
//                    svgActionMatrix.postTranslate(svgTranslatePoint.x, svgTranslatePoint.y);
//                    path.transform(svgActionMatrix);
//                }
                canvas.drawPath(path!!, paint)
            }
            resetPoints()
            //            canvas.restore();
        }
        if (svgTouchPath != null) {
            canvas.drawPath(svgTouchPath!!, svgTouchPathPaint)
        }
        if (true) return
        val vh = vectorDrawable!!.intrinsicHeight
        val vw = vectorDrawable!!.intrinsicWidth
        val w = width
        val scaleRate = w * 1f / vw
        canvas.save()
        val matrix = Matrix()
        matrix.setScale(scaleRate, scaleRate)
        canvas.setMatrix(matrix)
        canvas.drawBitmap(getBitmapFromResource(vectorDrawable), 0f, 0f, paint)
        // canvas.drawBitmap(BitmapDrawable.createFromPath());
        canvas.restore()
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (touchController!!.onTouchEvent(event)) {
            true
        } else super.onTouchEvent(event)
    }

    override fun consumeClick(upX: Float, upY: Float) {
        LogUtils.v(TAG, "consumeClick")
        var region: Region
        var temp: Path?
        if (svgPaths.size == 0) return
        for (i in svgPaths.indices) {
            temp = svgPaths[i]
            // temp.transform(svgRealMatrix);
            region = SvgUtils.createRegion(temp!!)
            if (region.isEmpty) continue
            if (region.contains(upX.toInt(), upY.toInt())) {
                LogUtils.v(
                    TAG,
                    "consumeClick:[" + svgElementVector!!.getSvgElementPathList()[i].getName() + "]"
                )
                svgTouchPath = temp
                postInvalidate()
            }
        }
    }

    override fun consumeZoomIn(centerX: Double, centerY: Double) {
        LogUtils.v(TAG, "consumeZoomIn")
        svgZoomScaleCenterPoint.x = centerX.toInt()
        svgZoomScaleCenterPoint.y = centerY.toInt()
        svgZoomScale = svgZoomScale * 0.99f
        postInvalidate()
    }

    override fun consumeZoomOut(centerX: Double, centerY: Double) {
        LogUtils.v(TAG, "consumeZoomOut")
        svgZoomScaleCenterPoint.x = centerX.toInt()
        svgZoomScaleCenterPoint.y = centerY.toInt()
        svgZoomScale = svgZoomScale * 1.01f
        postInvalidate()
    }

    override fun consumeDrag(tx: Float, ty: Float) {
        LogUtils.v(TAG, "consumeDrag")
        svgTranslatePoint.x = tx.toInt()
        svgTranslatePoint.y = ty.toInt()
        postInvalidate()
    }

    /**
     * 重置参数，确保每一次缩放正常
     */
    private fun resetPoints() {
        svgZoomScale = 1f
        svgZoomScaleCenterPoint.x = 1
        svgZoomScaleCenterPoint.y = 1
        svgTranslatePoint.x = 0
        svgTranslatePoint.y = 0
    }

    companion object {
        var TAG = "SvgParserView"
        private fun getBitmapFromResource(vectorDrawable: VectorDrawable?): Bitmap {
            val bitmap = Bitmap.createBitmap(
                vectorDrawable!!.intrinsicWidth,
                vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            vectorDrawable.draw(canvas)
            return bitmap
        }

        private fun getBitmap(vectorDrawable: VectorDrawable, width: Int, height: Int): Bitmap {
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            vectorDrawable.draw(canvas)
            return bitmap
        }
    }
}
