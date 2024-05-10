package com.example.connectionsmanagement.Calendar

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.connectionsmanagement.Communications.Show.ShowCommunicationActivity
import com.example.connectionsmanagement.R
import com.example.connectionsmanagement.Tools.ConnectionsManagementApplication
import com.example.connectionsmanagement.Tools.Tools.RefreshCommunications
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//交际日历activity
class CalendarFragment : Fragment(), com.prolificinteractive.materialcalendarview.DayViewDecorator {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Toast.makeText(ConnectionsManagementApplication.context, "Calendar_Fragment_onCreate", Toast.LENGTH_SHORT).show()//调试使用
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        Toast.makeText(ConnectionsManagementApplication.context, "Calendar_Fragment_onCreateView", Toast.LENGTH_SHORT).show()//调试使用
        val thisView = inflater.inflate(R.layout.fragment_calendar, container, false)

        val calendarView = thisView.findViewById<MaterialCalendarView>(R.id.calendarView)

        //获取交际
        GlobalScope.launch {
            val job=async {RefreshCommunications()}
            job.await()
            withContext(Dispatchers.Main) {
                Toast.makeText(ConnectionsManagementApplication.context, "交际获取成功", Toast.LENGTH_SHORT).show()
            }
        }

        // 设置日期范围
        calendarView.state().edit()
            .setMinimumDate(CalendarDay.from(2000, 1, 1))
            .setMaximumDate(CalendarDay.from(2100, 12, 31))
            .commit()

        // 设置日期选择模式
        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE)

        // 设置初始选中今天的日期
        calendarView.selectedDate = CalendarDay.today()
        //设置单个日期格的样式
        calendarView.invalidateDecorators()
        calendarView.addDecorator(this)
        // 设置日期改变监听器
        calendarView.setOnDateChangedListener { widget, date, selected ->
            // 在这里处理日期改变事件
//            Toast.makeText(ConnectionsManagementApplication.context, "date:${date}", Toast.LENGTH_SHORT).show()//调试使用
            //进入交际展示页面
            val intent = Intent(this.context, ShowCommunicationActivity::class.java)
            intent.putExtra("date",String.format("%04d-%02d-%02d", date.year, date.month, date.day))
            startActivity(intent)
        }
        return thisView
    }
    override fun onResume() {
        super.onResume()
    }

    //调整日历的当前选中为今天
    fun adjustCalendar(){
        val calendarView=view?.findViewById<MaterialCalendarView>(R.id.calendarView)
        // 获取当前日期
        val currentDate = CalendarDay.today()
        // 设置当前日期
        calendarView?.setCurrentDate(currentDate, false)
        // 设置初始选中今天的日期
        calendarView?.selectedDate = CalendarDay.today()
    }

    //重写Calendar的接口，表示允许装饰
    override fun shouldDecorate(day: CalendarDay?): Boolean {
        return true
    }

    //日期格装饰样式
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
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CalendarFragment().apply {

            }
    }
}