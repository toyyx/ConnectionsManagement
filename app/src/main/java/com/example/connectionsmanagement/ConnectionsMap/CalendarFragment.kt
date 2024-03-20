package com.example.connectionsmanagement.ConnectionsMap

import android.content.Intent
import android.os.Bundle
import android.provider.Settings.Global
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.Toast
import com.example.connectionsmanagement.ConnectionsMap.ImageDownloader.RefreshCommunications
import com.example.connectionsmanagement.R
import com.prolificinteractive.materialcalendarview.CalendarDay
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
class CalendarFragment : Fragment() {


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