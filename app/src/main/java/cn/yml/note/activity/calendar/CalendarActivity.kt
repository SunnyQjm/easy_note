package cn.yml.note.activity.calendar

import android.Manifest
import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import cn.yml.note.App
import cn.yml.note.R
import cn.yml.note.activity.tag.TagActivity
import cn.yml.note.base.BaseCalendarActivity
import cn.yml.note.extensions.jumpTo
import cn.yml.note.extensions.toast
import cn.yml.note.model.Image
import cn.yml.note.model.Note
import cn.yml.note.utils.GsonUtil
import cn.yml.note.utils.database
import com.haibin.calendarview.Calendar
import com.haibin.calendarview.CalendarView
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_calendar.*
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.rowParser
import org.jetbrains.anko.db.select
import java.util.HashMap

class CalendarActivity : BaseCalendarActivity(), CalendarView.OnCalendarSelectListener {
    private val rxPermissions = RxPermissions(this)
    private val notes = mutableListOf<Note>()

    override fun onCalendarOutOfRange(calendar: Calendar?) {
    }

    @SuppressLint("SetTextI18n")
    override fun onCalendarSelect(calendar: Calendar?, isClick: Boolean) {
//        tvLunar.visibility = View.VISIBLE
        tvYear.visibility = View.VISIBLE
        tvMonthDay.text = calendar?.month.toString() + "-" + calendar?.day
        tvYear.text = calendar?.year.toString()
//        tvLunar.text = calendar?.lunar
        mYear = calendar?.year ?: 0

        Log.e(
            "onDateSelected", "  -- " + calendar?.year +
                    "  --  " + calendar?.month +
                    "  -- " + calendar?.day +
                    "  --  " + isClick + "  --   " + calendar?.scheme
        )

        App.selectDay = calendar?.timeInMillis ?: 0
        App.selectMode = 1
        jumpTo(TagActivity::class.java)
    }

    private var mYear: Int = 0

    override fun getLayoutId(): Int = R.layout.activity_calendar

    @SuppressLint("SetTextI18n")
    override fun initView() {
        setStatusBarDarkMode()

        tvMonthDay.setOnClickListener {
            calendarView.showYearSelectLayout(mYear)
//            tvLunar.visibility = View.GONE
            tvYear.visibility = View.GONE
            tvMonthDay.text = mYear.toString()
        }

        flCurrent.setOnClickListener {
            calendarView.scrollToCurrent()
        }
        calendarView.setOnCalendarSelectListener(this)
        calendarView.setOnYearChangeListener {
            tvMonthDay.text = it.toString()
        }

        tvYear.text = calendarView.curYear.toString()
        tvMonthDay.text = "${calendarView.curMonth}-${calendarView.curDay}"
//        tvLunar.text = "Today"
        tvCurrentDay.text = calendarView.curDay.toString()
    }

    @SuppressLint("CheckResult")
    override fun initData() {
        rxPermissions
            .request(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .subscribe { granted ->
                if (granted) {
                    getAllNotes()
                    renderReminder()
                } else {
                    toast("Have no write permission")
                }
            }


    }

    /**
     * Get all note
     */
    private fun getAllNotes() {
        database.use {
            select("note")
                .exec {
                    val result =
                        parseList(rowParser { id: String, noteTitle: String, noteContent: String, noteImages: String,
                                              noteRecording: String, tags: String, createTime: Long, reminder: Long, objectId: String ->
                            return@rowParser Note(
                                id,
                                noteTitle,
                                noteContent,
                                GsonUtil.json2ImageList(noteImages) as MutableList<Image>,
                                GsonUtil.json2RecordList(noteRecording),
                                GsonUtil.json2TagList(tags),
                                createTime,
                                reminder,
                                objectId
                            )
                        })
                    notes.clear()
                    notes.addAll(result)
                }
        }
    }

    private fun renderReminder() {
        val map = HashMap<String, Calendar>()
        notes.forEach {
            val instance = java.util.Calendar.getInstance()
            instance.timeInMillis = it.createTime
            val year = instance.get(java.util.Calendar.YEAR)
            val month = instance.get(java.util.Calendar.MONTH) + 1
            val day = instance.get(java.util.Calendar.DAY_OF_MONTH)
            map[getSchemeCalendar(year, month, day, -0xbf24db, "假").toString()] =
                getSchemeCalendar(year, month, day, -0xbf24db, "假")
        }
        //此方法在巨大的数据量上不影响遍历性能，推荐使用
        calendarView.setSchemeDate(map)
    }


    private fun getSchemeCalendar(year: Int, month: Int, day: Int, color: Int, text: String): Calendar {
        val calendar = Calendar()
        calendar.year = year
        calendar.month = month
        calendar.day = day
        calendar.schemeColor = color//如果单独标记颜色、则会使用这个颜色
        calendar.scheme = text
        calendar.addScheme(color, "假")
        calendar.addScheme(if (day % 2 == 0) -0xff3300 else -0x2ea012, "节")
        calendar.addScheme(if (day % 2 == 0) -0x9a0000 else -0xbe961f, "记")
        return calendar
    }

}
