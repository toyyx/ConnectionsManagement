package com.example.connectionsmanagement.ConnectionsMap

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import com.example.connectionsmanagement.R

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

        val calendarView = thisView.findViewById<CalendarView>(R.id.calendarView)

//        // 设置日期范围
//        calendarView.state().edit()
//            .setMinimumDate(CalendarDay.today())
//            .setMaximumDate(CalendarDay.from(2025, 12, 31))
//            .commit()
//
//        // 设置日期选择模式
//        calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_SINGLE)
//
//        // 设置日期改变监听器
//        calendarView.setOnDateChangedListener { widget, date, selected ->
//            // 在这里处理日期改变事件
//        }

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