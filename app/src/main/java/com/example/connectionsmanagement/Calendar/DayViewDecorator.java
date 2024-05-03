package com.example.connectionsmanagement.Calendar;

import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

/**
 * Decorate Day views with drawables and text manipulation
 */
public interface DayViewDecorator {

  /**
   * Determine if a specific day should be decorated
   *
   * @param day {@linkplain CalendarDay} to possibly decorate
   * @return true if this decorator should be applied to the provided day
   */
  default boolean shouldDecorate(CalendarDay day){
    return true;
  }

  /**
   * Set decoration options onto a facade to be applied to all relevant days
   *
   * @param view View to decorate
   */
  default void decorate(DayViewFacade view){
    ShapeDrawable circleDrawable = new ShapeDrawable(new OvalShape());
    circleDrawable.getPaint().setColor(0xFF0000FF); // 蓝色
    // 将 Drawable 设置为日期视图的背景
    view.setBackgroundDrawable(circleDrawable);
  }
}
