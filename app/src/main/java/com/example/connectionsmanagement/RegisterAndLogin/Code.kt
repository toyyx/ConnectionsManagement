package com.example.connectionsmanagement.RegisterAndLogin

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.util.Random

class Code {
    //settings decided by the layout xml
    //canvas width and height
    private val width = DEFAULT_WIDTH
    private val height = DEFAULT_HEIGHT

    //random word space and pading_top
    private val base_padding_left = BASE_PADDING_LEFT
    private val range_padding_left = RANGE_PADDING_LEFT
    private val base_padding_top = BASE_PADDING_TOP
    private val range_padding_top = RANGE_PADDING_TOP

    //number of chars, lines; font size
    private val codeLength = DEFAULT_CODE_LENGTH
    private val line_number = DEFAULT_LINE_NUMBER
    private val font_size = DEFAULT_FONT_SIZE

    //variables
    var code: String? = null
        private set
    private var padding_left = 0
    private var padding_top = 0
    private val random = Random()

    //验证码图片
    fun createBitmap(): Bitmap {
        padding_left = 0
        val bp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bp)
        code = createCode()
        c.drawColor(Color.WHITE)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.textSize = font_size.toFloat()
        //画验证码
        for (i in 0 until code!!.length) {
            randomTextStyle(paint)
            randomPadding()
            c.drawText(
                code!![i].toString() + "",
                padding_left.toFloat(),
                padding_top.toFloat(),
                paint
            )
        }
        //画线条
        for (i in 0 until line_number) {
            drawLine(c, paint)
        }

//        c.save( Canvas.ALL_SAVE_FLAG );//保存
        c.save() //保存
        c.restore() //
        return bp
    }

    //生成验证码
    private fun createCode(): String {
        val buffer = StringBuilder()
        for (i in 0 until codeLength) {
            buffer.append(CHARS[random.nextInt(CHARS.size)])
        }
        return buffer.toString()
    }

    //画干扰线
    private fun drawLine(canvas: Canvas, paint: Paint) {
        val color = randomColor()
        val startX = random.nextInt(width)
        val startY = random.nextInt(height)
        val stopX = random.nextInt(width)
        val stopY = random.nextInt(height)
        paint.strokeWidth = 1f
        paint.color = color
        canvas.drawLine(startX.toFloat(), startY.toFloat(), stopX.toFloat(), stopY.toFloat(), paint)
    }

    //生成随机颜色
    private fun randomColor(rate: Int = 1): Int {
        val red = random.nextInt(256) / rate
        val green = random.nextInt(256) / rate
        val blue = random.nextInt(256) / rate
        return Color.rgb(red, green, blue)
    }

    //随机生成文字样式，颜色，粗细，倾斜度
    private fun randomTextStyle(paint: Paint) {
        val color = randomColor()
        paint.color = color
        paint.isFakeBoldText = random.nextBoolean() //true为粗体，false为非粗体
        var skewX = (random.nextInt(11) / 10).toFloat()
        skewX = if (random.nextBoolean()) skewX else -skewX
        paint.textSkewX = skewX //float类型参数，负数表示右斜，整数左斜
        //paint.setUnderlineText(true); //true为下划线，false为非下划线
        //paint.setStrikeThruText(true); //true为删除线，false为非删除线
    }

    //随机生成padding值
    private fun randomPadding() {
        padding_left += base_padding_left + random.nextInt(range_padding_left)
        padding_top = base_padding_top + random.nextInt(range_padding_top)
    }

    companion object {
        /**
         * 随机数数组
         * 去除了易混淆的 数字 0 和 字母 o O
         * 数字 1 和 字母 i I l L
         * 数字 6 和 字母 b
         * 数字 9 和 字母 q
         * 字母 c C 和 G
         * 字母 t （经常和随机线混在一起看不清）
         */
        private val CHARS = charArrayOf(
            '2', '3', '4', '5', '7', '8',
            'a', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm',
            'n', 'p', 'r', 's', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'D', 'E', 'F', 'H', 'J', 'K', 'M',
            'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
        )
        private var bmpCode: Code?=null
        val instance: Code?
            get() {
                if (bmpCode == null) bmpCode = Code()
                return bmpCode
            }

        //default settings
        //验证码默认随机数的个数
        private const val DEFAULT_CODE_LENGTH = 4

        //默认字体大小
        private const val DEFAULT_FONT_SIZE = 25

        //默认线条的条数
        private const val DEFAULT_LINE_NUMBER = 5

        //padding值
        private const val BASE_PADDING_LEFT = 10
        private const val RANGE_PADDING_LEFT = 15
        private const val BASE_PADDING_TOP = 15
        private const val RANGE_PADDING_TOP = 20

        //验证码的默认宽高
        private const val DEFAULT_WIDTH = 100
        private const val DEFAULT_HEIGHT = 40
    }
}