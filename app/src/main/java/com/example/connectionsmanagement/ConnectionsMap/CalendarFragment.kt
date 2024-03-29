package com.example.connectionsmanagement.ConnectionsMap

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.connectionsmanagement.ConnectionsMap.ImageDownloader.RefreshCommunications
import com.example.connectionsmanagement.R
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


/**
 * A simple [Fragment] subclass.
 * Use the [CalendarFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalendarFragment : Fragment(), DayViewDecorator {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val thisView = inflater.inflate(R.layout.fragment_calendar, container, false)

        val calendarView = thisView.findViewById<MaterialCalendarView>(R.id.calendarView)

        GlobalScope.launch {
            val job=async {RefreshCommunications()}
            job.await()
            withContext(Dispatchers.Main) {
                Toast.makeText(ConnectionsManagementApplication.context, "交际获取成功", Toast.LENGTH_SHORT).show()
            }
        }
        // 设置日期范围
        calendarView.state().edit()
            .setMinimumDate(CalendarDay.from(2023, 12, 31))
            .setMaximumDate(CalendarDay.from(2025, 12, 31))
            .commit()

        // 设置日期选择模式
        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE)

        // 设置初始选中今天的日期
        calendarView.selectedDate = CalendarDay.today()
        calendarView.invalidateDecorators()
        calendarView.addDecorator(this)
        // 设置日期改变监听器
        calendarView.setOnDateChangedListener { widget, date, selected ->
            // 在这里处理日期改变事件
            Toast.makeText(ConnectionsManagementApplication.context, "date:${date}", Toast.LENGTH_SHORT).show()
            //进入主页面
            val intent = Intent(this.context, ShowCommunicationActivity::class.java)
            intent.putExtra("date",String.format("%04d-%02d-%02d", date.year, date.month, date.day))
            startActivity(intent)

        }

        return thisView
    }

    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return true
    }

    override fun decorate(view: DayViewFacade?) {
        if (view != null) {
            val rectangleDrawable = ColorDrawable(Color.parseColor("#c3d0d0"))
            val borderDrawable = ColorDrawable(Color.parseColor("#3f8787"))

            val layers = arrayOf<Drawable>(rectangleDrawable, borderDrawable)
            val layerDrawable = LayerDrawable(layers)

            val borderWidth = 2.dpToPx()
            layerDrawable.setLayerInset(1, borderWidth, borderWidth, borderWidth, borderWidth)

            view.setBackgroundDrawable(layerDrawable)
        }
    }
    fun Int.dpToPx(): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CalendarFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CalendarFragment().apply {

            }
    }
}