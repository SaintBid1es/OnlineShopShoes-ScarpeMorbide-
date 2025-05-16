package com.example.customview.customView

/**
 * @author Alex-tech-it
 * Github - https://github.com/Alex-tech-it
 */

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.text.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

import com.example.customview.extension.dpToPx
import com.example.customview.extension.draw
import com.example.customview.extension.spToPx
import com.example.customview.model.AnalyticalPieChartModel
import com.example.customview.model.AnalyticalPieChartState
import com.example.testbundle.R

/**
 * CustomView AnalyticalPieChart
 *
 * Кольцевая диаграмма для отображения статистики объектов в процентном соотношении.
 *
 * AnalyticalPieChart адаптируется под любое значение высоты и ширины, которое передается
 * данной View от parent.
 * Проверено на всех возможных разрешениях экрана (ldpi, mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi).
 *
 * Для удобства использования и сборки в DI, класс имплементирует интерфейс взаимодействия [AnalyticalPieChartInterface]
 *
 *
 * AnalyticalPieChart обладает огромным количество настроек отображения.
 * Все расчеты производятся в пикселях, необходимо это учитываться при изменении кода.
 *
 * @property marginTextFirst   - значение отступа между числом и его описанием.
 * @property marginTextSecond  - значение отступа между объектами, где объект - число и описание.
 * @property marginTextThird   - значение отступа между числом и его описанием общего результата.
 * @property marginSmallCircle - значение отступа между числом и маленьким кругом.
 * @property marginText        - значение суммы отступов [marginTextFirst] и [marginTextSecond].
 * @property circleRect        - объект отрисовки круговой диаграммы.
 * @property circleStrokeWidth - значение толщины круговой диаграммы.
 * @property circleRadius      - значение радиуса круговой диаграммы.
 * @property circlePadding     - padding для всех сторон круговой диаграммы.
 * @property circlePaintRoundSize - значение округления концов линий объектов круга.
 * @property circleSectionSpace   - значение расстояние-процент между линиями круга.
 * @property circleCenterX        - значение координаты X центра круговой диаграммы.
 * @property circleCenterY        - значение координаты Y центра круговой диаграммы.
 * @property numberTextPaint      - объект кисти отрисовки текста чисел.
 * @property descriptionTextPain  - объект кисти отрисовки текста описания.
 * @property amountTextPaint      - объект кисти отрисовки текста результата.
 * @property textStartX           - значение координаты X, откуда отрисовывается текст.
 * @property textStartY           - значение координаты Y, откуда отрисовывается текст.
 * @property textHeight           - значение высоты текста.
 * @property textCircleRadius     - значение радиуса малого круга около текста числа.
 * @property textAmountStr        - строка результата.
 * @property textAmountY          - значение координаты Y, откуда отрисовывается результирующий текст.
 * @property textAmountXNumber    - значение координаты X, откуда отрисовывается результирующий текст числа.
 * @property textAmountXDescription     - значение координаты X, откуда отрисовывается описание результата.
 * @property textAmountYDescription     - значение координаты Y, откуда отрисовывается описание результата.
 * @property totalAmount          - итоговый результат - сумма значений Int в [dataList].
 * @property pieChartColors       - список цветов круговой диаграммы в виде текстового представления.
 * @property percentageCircleList - список моделей для отрисовки.
 * @property textRowList          - список строк, которые необходимо отобразить.
 * @property dataList             - исходный список данных.
 * @property animationSweepAngle  - переменная для анимации.
 */
class AnalyticalPieChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :  View(context, attrs, defStyleAttr), AnalyticalPieChartInterface {

    /**
     * Базовые значения для полей и самой [AnalyticalPieChart]
     */
    companion object {
        private const val DEFAULT_MARGIN_TEXT_1 = 2
        private const val DEFAULT_MARGIN_TEXT_2 = 10
        private const val DEFAULT_MARGIN_TEXT_3 = 2
        private const val DEFAULT_MARGIN_SMALL_CIRCLE = 12
        private const val ANALYTICAL_PIE_CHART_KEY = "AnalyticalPieChartArrayData"

        /* Процент ширины для отображения текста от общей ширины View */
        private const val TEXT_WIDTH_PERCENT = 0.40

        /* Процент ширины для отображения круговой диаграммы от общей ширины View */
        private const val CIRCLE_WIDTH_PERCENT = 0.50

        /* Базовые значения ширины и высоты View */
        const val DEFAULT_VIEW_SIZE_HEIGHT = 150
        const val DEFAULT_VIEW_SIZE_WIDTH = 250
    }

    private var marginTextFirst: Float = context.dpToPx(DEFAULT_MARGIN_TEXT_1)
    private var marginTextSecond: Float = context.dpToPx(DEFAULT_MARGIN_TEXT_2)
    private var marginTextThird: Float = context.dpToPx(DEFAULT_MARGIN_TEXT_3)
    private var marginSmallCircle: Float = context.dpToPx(DEFAULT_MARGIN_SMALL_CIRCLE)
    private val marginText: Float = marginTextFirst + marginTextSecond
    private val circleRect = RectF()
    private var circleStrokeWidth: Float = context.dpToPx(6)
    private var circleRadius: Float = 0F
    private var circlePadding: Float = context.dpToPx(8)
    private var circlePaintRoundSize: Boolean = true
    private var circleSectionSpace: Float = 3F
    private var circleCenterX: Float = 0F
    private var circleCenterY: Float = 0F
    private var numberTextPaint: TextPaint = TextPaint()
    private var descriptionTextPain: TextPaint = TextPaint()
    private var amountTextPaint: TextPaint = TextPaint()
    private var textStartX: Float = 0F
    private var textStartY: Float = 0F
    private var textHeight: Int = 0
    private var textCircleRadius: Float = context.dpToPx(4)
    private var textAmountStr: String = ""
    private var textAmountY: Float = 0F
    private var textAmountXNumber: Float = 0F
    private var textAmountXDescription: Float = 0F
    private var textAmountYDescription: Float = 0F
    private var totalAmount: Int = 0
    var pieChartColors: List<String> = listOf()
    private var percentageCircleList: List<AnalyticalPieChartModel> = listOf()
    private var textRowList: MutableList<StaticLayout> = mutableListOf()
    private var dataList: List<Pair<Int, String>> = listOf()
    private var animationSweepAngle: Int = 0
    private var isCenterTextHidden = false

    /**
     * В INIT блоке инициализируются все необходимые поля и переменные.
     * Необходимые значения вытаскиваются из специальных Attr тегов
     * (<declare-styleable name="AnalyticalPieChart">).
     */
    init {
        // Установите значения по умолчанию
        var textAmountSize = context.spToPx(22)
        var textNumberSize = context.spToPx(20)
        var textDescriptionSize = context.spToPx(14)
        var textAmountColor = ContextCompat.getColor(context, android.R.color.white)
        var textNumberColor = ContextCompat.getColor(context, android.R.color.white)
        var textDescriptionColor = ContextCompat.getColor(context, android.R.color.darker_gray)

        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AnalyticalPieChart, 0, 0)

            try {
                // Безопасное получение массива цветов
                val colorResId = typedArray.getResourceId(R.styleable.AnalyticalPieChart_pieChartColors, 0)
                pieChartColors = if (colorResId != 0) {
                    try {
                        context.resources.getStringArray(colorResId).toList()
                    } catch (e: Exception) {
                        listOf("#FF0000", "#00FF00") // цвета по умолчанию
                    }
                } else {
                    listOf("#FF0000", "#00FF00") // цвета по умолчанию
                }

                // Получение остальных атрибутов с безопасными fallback-значениями
                marginTextFirst = typedArray.getDimension(R.styleable.AnalyticalPieChart_pieChartMarginTextFirst, marginTextFirst)

                textAmountColor = try {
                    typedArray.getColor(R.styleable.AnalyticalPieChart_pieChartTextAmountColor, textAmountColor)
                } catch (e: Exception) {
                    ContextCompat.getColor(context, android.R.color.white)
                }

                textNumberColor = try {
                    typedArray.getColor(R.styleable.AnalyticalPieChart_pieChartTextNumberColor, textNumberColor)
                } catch (e: Exception) {
                    ContextCompat.getColor(context, android.R.color.white)
                }

                textDescriptionColor = try {
                    typedArray.getColor(R.styleable.AnalyticalPieChart_pieChartTextDescriptionColor, textDescriptionColor)
                } catch (e: Exception) {
                    ContextCompat.getColor(context, android.R.color.darker_gray)
                }

                textAmountStr = typedArray.getString(R.styleable.AnalyticalPieChart_pieChartTextAmount) ?: ""
            } finally {
                typedArray.recycle()
            }
        }

        circlePadding += circleStrokeWidth

        // Инициализация кистей
        initPaints(textAmountSize, textAmountColor, textNumberSize, textNumberColor, textDescriptionSize, textDescriptionColor)
    }

    private fun initPaints(
        amountSize: Float, amountColor: Int,
        numberSize: Float, numberColor: Int,
        descriptionSize: Float, descriptionColor: Int
    ) {
        amountTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = amountColor
            textSize = amountSize
            typeface = Typeface.DEFAULT_BOLD
        }

        numberTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = numberColor
            textSize = numberSize
            typeface = Typeface.DEFAULT_BOLD
        }

        descriptionTextPain = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = descriptionColor
            textSize = descriptionSize
        }
    }

    /**
     * Метод жизненного цикла View.
     * Расчет необходимой ширины и высоты View.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        textRowList.clear()

        val initSizeWidth = resolveDefaultSize(widthMeasureSpec, DEFAULT_VIEW_SIZE_WIDTH)

        val textTextWidth = (initSizeWidth * TEXT_WIDTH_PERCENT)
        val initSizeHeight = calculateViewHeight(heightMeasureSpec, textTextWidth.toInt())

        textStartX = width * 0.55f
        textStartY = initSizeHeight.toFloat() / 2 - textHeight / 2

        calculateCircleRadius(initSizeWidth, initSizeHeight)

        setMeasuredDimension(initSizeWidth, initSizeHeight)
    }

    /**
     * Метод жизненного цикла View.
     * Отрисовка всех необходимых компонентов на Canvas.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawCircle(canvas)
        drawText(canvas)
    }
    fun hideCenterText(hide: Boolean) {
        isCenterTextHidden = hide
        invalidate()  // Перерисовать View
    }

    /**
     * Восстановление данных из [AnalyticalPieChartState]
     */
    override fun onRestoreInstanceState(state: Parcelable?) {
        val analyticalPieChartState = state as? AnalyticalPieChartState
        super.onRestoreInstanceState(analyticalPieChartState?.superState ?: state)

        dataList = analyticalPieChartState?.dataList ?: listOf()
    }

    /**
     * Сохранение [dataList] в собственный [AnalyticalPieChartState]
     */
    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return AnalyticalPieChartState(superState, dataList)
    }

    /**
     * Имплиментируемый метод интерфейса взаимодействия [AnalyticalPieChartInterface].
     * Добавление данных в View.
     */
    override fun setDataChart(list: List<Pair<Int, String>>) {
        dataList = list
        calculatePercentageOfData()
    }

    /**
     * Имплиментируемый метод интерфейса взаимодействия [AnalyticalPieChartInterface].
     * Запуск анимации отрисовки View.
     */
    override fun startAnimation() {
        // Проход значений от 0 до 360 (целый круг), с длительностью - 1.5 секунды
        val animator = ValueAnimator.ofInt(0, 360).apply {
            duration = 1500
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener { valueAnimator ->
                animationSweepAngle = valueAnimator.animatedValue as Int
                invalidate()
            }
        }
        animator.start()
    }

    /**
     * Метод отрисовки круговой диаграммы на Canvas.
     */
    private fun drawCircle(canvas: Canvas) {
        for(percent in percentageCircleList) {
            if (animationSweepAngle > percent.percentToStartAt + percent.percentOfCircle){
                canvas.drawArc(circleRect, percent.percentToStartAt, percent.percentOfCircle, false, percent.paint)
            } else if (animationSweepAngle > percent.percentToStartAt) {
                canvas.drawArc(circleRect, percent.percentToStartAt, animationSweepAngle - percent.percentToStartAt, false, percent.paint)
            }
        }
    }
    fun setCircleSize(radiusDp: Int) {
        // Конвертируем dp в пиксели
        this.circleRadius = context.dpToPx(radiusDp)
        requestLayout() // Пересчитываем layout
        invalidate()    // Перерисовываем
    }

    /**
     * Метод отрисовки всего текста диаграммы на Canvas.
     */
    private fun drawText(canvas: Canvas) {
        var textBuffY = textStartY
        textRowList.forEachIndexed { index, staticLayout ->
            if (index % 2 == 0) {
                // Уменьшаем расстояние между кружком и текстом
                val circleX = textStartX + marginSmallCircle / 2 // Было / 2
                val textX = circleX + textCircleRadius * 1.5f // Было 1.5f

                // Рисуем кружок
                canvas.drawCircle(
                    circleX,
                    textBuffY + staticLayout.height / 2,
                    textCircleRadius,
                    Paint().apply {
                      color=  Color.parseColor(pieChartColors[(index / 2) % pieChartColors.size])
                    }
                )

                // Рисуем текст ближе к кружку
                staticLayout.draw(canvas, textX, textBuffY)
                textBuffY += staticLayout.height + marginTextFirst
            } else {
                // Описание рисуем с минимальным отступом
                staticLayout.draw(canvas, textStartX + marginSmallCircle / 4, textBuffY)
                textBuffY += staticLayout.height + marginTextSecond
            }
        }

        // Отрисовка центрального текста
        if (!isCenterTextHidden) {
            canvas.drawText(totalAmount.toString(), textAmountXNumber, textAmountY, amountTextPaint)
            canvas.drawText(textAmountStr, textAmountXDescription, textAmountYDescription, descriptionTextPain)
        }
    }

    /**
     * Метод инициализации переданной TextPaint
     */
    private fun initPains(textPaint: TextPaint, textSize: Float, textColor: Int, isDescription: Boolean = false) {
        textPaint.color = textColor
        textPaint.textSize = textSize
        textPaint.isAntiAlias = true

        if (!isDescription) textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    /**
     * Метод получения размера View по переданному Mode.
     */
    private fun resolveDefaultSize(spec: Int, defValue: Int): Int {
        return when(MeasureSpec.getMode(spec)) {
            MeasureSpec.UNSPECIFIED -> context.dpToPx(defValue).toInt()
            else -> MeasureSpec.getSize(spec)
        }
    }

    /**
     * Метод расчёта высоты всего текста, включая отступы.
     */
    private fun calculateViewHeight(heightMeasureSpec: Int, textWidth: Int): Int {
        val initSizeHeight = resolveDefaultSize(heightMeasureSpec, DEFAULT_VIEW_SIZE_HEIGHT)
        textHeight = (dataList.size * marginText + getTextViewHeight(textWidth)).toInt()

        val textHeightWithPadding = textHeight + paddingTop + paddingBottom
        return if (textHeightWithPadding > initSizeHeight) textHeightWithPadding else initSizeHeight
    }

    /**
     * Метод расчёта радиуса круговой диаграммы, установка координат для отрисовки.
     */
    private fun calculateCircleRadius(width: Int, height: Int) {
        val circleViewWidth = (width * CIRCLE_WIDTH_PERCENT)

        // Рассчитываем необходимый радиус на основе размера текста
        val textBounds = Rect()
        amountTextPaint.getTextBounds(totalAmount.toString(), 0, totalAmount.toString().length, textBounds)
        val textWidth = textBounds.width()
        val textHeight = textBounds.height()

        // Минимальный радиус, чтобы текст помещался
        val minRadius = textWidth.coerceAtLeast(textHeight) * 0.7f

                circleRadius = if (circleViewWidth > height) {
            ((height.toFloat() - circlePadding) / 2).coerceAtLeast(minRadius)
        } else {
            (circleViewWidth.toFloat() / 2).coerceAtLeast(minRadius)
        }

        with(circleRect) {
            left = circlePadding
            top = height / 2 - circleRadius
            right = circleRadius * 2 + circlePadding
            bottom = height / 2 + circleRadius
        }

        // Остальные расчеты остаются без изменений
        circleCenterX = (circleRadius * 2 + circlePadding + circlePadding) / 2
        circleCenterY = (height / 2 + circleRadius + (height / 2 - circleRadius)) / 2
        textAmountY = circleCenterY

        textAmountXNumber = circleCenterX - textWidth / 2
        textAmountXDescription = circleCenterX - getWidthOfAmountText(textAmountStr, descriptionTextPain).width() / 2
        textAmountYDescription = circleCenterY + textHeight + marginTextThird
    }

    fun setCirclePadding(padding: Float) {
        circlePadding = padding  // Обновляем внутренний отступ
        requestLayout()          // Триггерит перерасчет размеров
    }

    /**
     * Метод расчёта высоты объектов, где объект - это число и его описание.
     * Добавление объекта в список строк для отрисовки [textRowList].
     */
    private fun getTextViewHeight(maxWidth: Int): Double {
        var textHeight = 0.0
        dataList.forEach {
            // Пропускаем отрисовку если текст пустой
            if (it.second.isNotEmpty()) {
                val textLayoutDescription = getMultilineText(
                    text = it.second,
                    textPaint = descriptionTextPain,
                    width = maxWidth
                )
                textRowList.add(textLayoutDescription)
                textHeight += textLayoutDescription.height + marginTextSecond
            }
        }
        return textHeight
    }

    /**
     * Метод заполнения поля [percentageCircleList]
     */
    private fun calculatePercentageOfData() {
        totalAmount = dataList.fold(0) { res, value -> res + value.first}

        var startAt = circleSectionSpace
        percentageCircleList = dataList.mapIndexed { index, pair ->
            var percent = pair.first * 100 / totalAmount.toFloat() - circleSectionSpace
            percent = if (percent < 0F) 0F else percent

            val resultModel = AnalyticalPieChartModel(
                percentOfCircle = percent,
                percentToStartAt = startAt,
                colorOfLine = Color.parseColor(pieChartColors[index % pieChartColors.size]),
                stroke = circleStrokeWidth,
                paintRound = circlePaintRoundSize
            )
            if (percent != 0F) startAt += percent + circleSectionSpace
            resultModel
        }
    }

    /**
     * Метод обертки текста в класс [Rect]
     */
    private fun getWidthOfAmountText(text: String, textPaint: TextPaint): Rect {
        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)
        return bounds
    }

    /**
     * Метод создания [StaticLayout] с переданными значениями
     */
    private fun getMultilineText(
        text: CharSequence,
        textPaint: TextPaint,
        width: Int,
        start: Int = 0,
        end: Int = text.length,
        alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
        textDir: TextDirectionHeuristic = TextDirectionHeuristics.LTR,
        spacingMult: Float = 1f,
        spacingAdd: Float = 0f) : StaticLayout {

        return StaticLayout.Builder
            .obtain(text, start, end, textPaint, width)
            .setAlignment(alignment)
            .setTextDirection(textDir)
            .setLineSpacing(spacingAdd, spacingMult)
            .build()
    }
}
